package com.yoshione.fingen.utils.winzipaes.impl;


/**
 * Base class for crypto "adapters" to support aes operations
 * needed for winzip aes.
 *
 * @author olaf@merkert.de
 */
public class AESCryptoBase {

	public static final int KEY_SIZE_BIT = 256;

	public static final int KEY_SIZE_BYTE = KEY_SIZE_BIT / 8;

	public static final int ITERATION_COUNT = 1000;

	// --------------------------------------------------------------------------

	protected byte[] saltBytes;

	protected byte[] cryptoKeyBytes;

	protected byte[] authenticationCodeBytes;

	protected byte[] pwVerificationBytes;

	protected int blockSize;

	protected int nonce;

}
