package com.yoshione.fingen.utils.winzipaes.impl;

import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;



/**
 * Adapter for bouncy castle crypto implementation (encryption).
 *
 * AES256 encrypter for 1 file using 1 PASSWORD + 1 SALT
 * to create 1 KEY used for subsequent calls to encrypt() method.
 *
 * @author olaf@merkert.de
 */
public class AESEncrypterBC extends AESCryptoBase implements AESEncrypter {

	private static final Logger LOG = Logger.getLogger( AESEncrypterBC.class.getName() );

	// --------------------------------------------------------------------------

	protected CipherParameters cipherParameters;

	protected SICBlockCipher aesCipher;

	protected HMac mac;

	/**
	 * Setup AES encryption based on pwBytes using WinZipAES approach
	 * with SALT and pwVerification bytes based on password+salt.
	 */
	public void init( String pwStr, int keySize ) throws ZipException {
		byte[] pwBytes = pwStr.getBytes();
		PBEParametersGenerator generator = new PKCS5S2ParametersGenerator();
		this.saltBytes = createSalt();
		generator.init( pwBytes, saltBytes, ITERATION_COUNT );

		// create 2 byte[16] for two keys and one byte[2] for pwVerification
		// 1. encryption / 2. athentication (via HMAC/hash) /
		cipherParameters = generator.generateDerivedParameters(KEY_SIZE_BIT*2 + 16);
		byte[] keyBytes = ((KeyParameter)cipherParameters).getKey();

		this.cryptoKeyBytes = new byte[ KEY_SIZE_BYTE ];
		System.arraycopy( keyBytes, 0, cryptoKeyBytes, 0, KEY_SIZE_BYTE );

		this.authenticationCodeBytes = new byte[ KEY_SIZE_BYTE ];
		System.arraycopy( keyBytes, KEY_SIZE_BYTE, authenticationCodeBytes, 0, KEY_SIZE_BYTE );

		// based on SALT + PASSWORD (password is probably correct)
		this.pwVerificationBytes = new byte[ 2 ];
		System.arraycopy( keyBytes, KEY_SIZE_BYTE*2, pwVerificationBytes, 0, 2 );

		// create the first 16 bytes of the key sequence again (using pw+salt)
		generator.init( pwBytes, saltBytes, ITERATION_COUNT );
		cipherParameters = generator.generateDerivedParameters(KEY_SIZE_BIT);

		// checksum added to the end of the encrypted data, update on each encryption call
		this.mac = new HMac( new SHA1Digest() );
		mac.init( new KeyParameter(authenticationCodeBytes) );

		this.aesCipher = new SICBlockCipher(new AESEngine());
		this.blockSize = aesCipher.getBlockSize();

		// incremented on each 16 byte block and used as encryption NONCE (ivBytes)
		nonce = 1;
		
		if( LOG.isLoggable(Level.FINEST) ) {
			LOG.finest( "pwBytes   = " + ByteArrayHelper.toString(pwBytes) + " - " + pwBytes.length );
			LOG.finest( "salt      = " + ByteArrayHelper.toString(saltBytes) + " - " + saltBytes.length );
			LOG.finest( "pwVerif   = " + ByteArrayHelper.toString(pwVerificationBytes) + " - " + pwVerificationBytes.length );
		}
	}

	/**
	 * perform pseudo "in-place" encryption
	 */
	public void encrypt( byte[] in, int length ) {
		int pos = 0;
		while( pos<in.length && pos<length ) {
			encryptBlock( in, pos, length );
			pos += blockSize;
		}
	}

	/**
	 * encrypt 16 bytes (AES standard block size) or less
	 * starting at "pos" within "in" byte[]
	 */
	protected void encryptBlock( byte[] in, int pos, int length ) {
		byte[] encryptedIn = new byte[blockSize];
		byte[] ivBytes = ByteArrayHelper.toByteArray( nonce++, 16 );
		ParametersWithIV ivParams = new ParametersWithIV(cipherParameters, ivBytes);
		aesCipher.init( true, ivParams );

		int remainingCount = length-pos;
		if( remainingCount>=blockSize ) {
			aesCipher.processBlock( in, pos, encryptedIn, 0 );
			System.arraycopy( encryptedIn, 0, in, pos, blockSize );
			mac.update( encryptedIn, 0, blockSize );
		} else {
			byte[] extendedIn = new byte[blockSize];
			System.arraycopy( in, pos, extendedIn, 0, remainingCount );
			aesCipher.processBlock( extendedIn, 0, encryptedIn, 0 );
			System.arraycopy( encryptedIn, 0, in, pos, remainingCount );
			mac.update( encryptedIn, 0, remainingCount );
		}
	}

	/** 16 bytes (AES-256) set in constructor */
	public byte[] getSalt() {
		return saltBytes;
	}

	/** 2 bytes for password verification set in constructor */
	public byte[] getPwVerification() {
		return pwVerificationBytes;
	}

	/** 10 bytes */
	public byte[] getFinalAuthentication() {
		// MAC / based on encIn + PASSWORD + SALT (encryption was successful)
		byte[] macBytes = new byte[ mac.getMacSize() ];
		mac.doFinal( macBytes, 0 );
		byte[] macBytes10 = new byte[10];
		System.arraycopy( macBytes, 0, macBytes10, 0, 10 );
		return macBytes10;
	}

	// --------------------------------------------------------------------------

	private static final Random RANDOM = new SecureRandom();	
	
	/**
	 * create 16 bytes salt by using SecureRandom instance
	 */
	protected static byte[] createSalt() {
		byte[] salt = new byte[16];
		RANDOM.nextBytes(salt);
		return salt;
	}

}
