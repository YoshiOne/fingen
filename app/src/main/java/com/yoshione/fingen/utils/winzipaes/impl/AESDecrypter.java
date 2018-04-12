package com.yoshione.fingen.utils.winzipaes.impl;

import java.util.zip.ZipException;

/**
 * Decrypt.
 *
 * @author olaf@merkert.de
 */
public interface AESDecrypter {

	public void init(String pwStr, int keySize, byte[] salt, byte[] pwVerification ) throws ZipException;
	
	public void decrypt( byte[] in, int length );

	public byte[] getFinalAuthentication();

}
