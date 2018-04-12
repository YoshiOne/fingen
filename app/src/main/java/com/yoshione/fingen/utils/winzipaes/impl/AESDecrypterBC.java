package com.yoshione.fingen.utils.winzipaes.impl;

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
 * Adapter for bouncy castle crypto implementation (decryption).
 *
 * @author olaf@merkert.de
 */
public class AESDecrypterBC extends AESCryptoBase implements AESDecrypter {

	// TODO consider keySize (but: we probably need to adapt the key size for the zip file as well)
	public void init( String pwStr, int keySize, byte[] salt, byte[] pwVerification ) throws ZipException {
		byte[] pwBytes = pwStr.getBytes();
		
		super.saltBytes = salt;

		PBEParametersGenerator generator = new PKCS5S2ParametersGenerator();
		generator.init( pwBytes, salt, ITERATION_COUNT );

		cipherParameters = generator.generateDerivedParameters(KEY_SIZE_BIT*2 + 16);
		byte[] keyBytes = ((KeyParameter)cipherParameters).getKey();

		this.cryptoKeyBytes = new byte[ KEY_SIZE_BYTE ];
		System.arraycopy( keyBytes, 0, cryptoKeyBytes, 0, KEY_SIZE_BYTE );

		this.authenticationCodeBytes = new byte[ KEY_SIZE_BYTE ];
		System.arraycopy( keyBytes, KEY_SIZE_BYTE, authenticationCodeBytes, 0, KEY_SIZE_BYTE );

		// based on SALT + PASSWORD (password is probably correct)
		this.pwVerificationBytes = new byte[ 2 ];
		System.arraycopy( keyBytes, KEY_SIZE_BYTE*2, this.pwVerificationBytes, 0, 2 );

		if( !ByteArrayHelper.isEqual( this.pwVerificationBytes, pwVerification ) ) {
			throw new ZipException("wrong password - " + ByteArrayHelper.toString(this.pwVerificationBytes) + "/ " + ByteArrayHelper.toString(pwVerification));
		}

		// create the first 16 bytes of the key sequence again (using pw+salt)
		generator.init( pwBytes, salt, ITERATION_COUNT );
		cipherParameters = generator.generateDerivedParameters(KEY_SIZE_BIT);

		// checksum added to the end of the encrypted data, update on each encryption call
		this.mac = new HMac( new SHA1Digest() );
		mac.init( new KeyParameter(authenticationCodeBytes) );

		this.aesCipher = new SICBlockCipher(new AESEngine());
		this.blockSize = aesCipher.getBlockSize();

		// incremented on each 16 byte block and used as encryption NONCE (ivBytes)
		nonce = 1;
	}

	// --------------------------------------------------------------------------

	protected CipherParameters cipherParameters;

	protected SICBlockCipher aesCipher;

	protected HMac mac;

	/**
	 * perform pseudo "in-place" encryption
	 */
	public void decrypt( byte[] in, int length ) {
		int pos = 0;
		while( pos<in.length && pos<length ) {
			decryptBlock( in, pos, length );
			pos += blockSize;
		}
	}

	/**
	 * encrypt 16 bytes (AES standard block size) or less
	 * starting at "pos" within "in" byte[]
	 */
	protected void decryptBlock( byte[] in, int pos, int length ) {
		byte[] decryptedIn = new byte[blockSize];
		byte[] ivBytes = ByteArrayHelper.toByteArray( nonce++, 16 );
		ParametersWithIV ivParams = new ParametersWithIV(cipherParameters, ivBytes);
		aesCipher.init( false, ivParams );

		int remainingCount = length-pos;
		if( remainingCount>=blockSize ) {
			mac.update( in, pos, blockSize );
			aesCipher.processBlock( in, pos, decryptedIn, 0 );
			System.arraycopy( decryptedIn, 0, in, pos, blockSize );
		} else {
			mac.update( in, pos, remainingCount );
			byte[] extendedIn = new byte[blockSize];
			System.arraycopy( in, pos, extendedIn, 0, remainingCount );
			aesCipher.processBlock( extendedIn, 0, decryptedIn, 0 );
			System.arraycopy( decryptedIn, 0, in, pos, remainingCount );
		}
	}

	public byte[] getFinalAuthentication() {
		byte[] macBytes = new byte[ mac.getMacSize() ];
		mac.doFinal( macBytes, 0 );
		byte[] macBytes10 = new byte[10];
		System.arraycopy( macBytes, 0, macBytes10, 0, 10 );
		return macBytes10;
	}

}
