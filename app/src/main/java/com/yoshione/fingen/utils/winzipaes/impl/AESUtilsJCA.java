/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.yoshione.fingen.utils.winzipaes.impl;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility methods for handling WinZip AES cryptography using the Java Cryptography Architecture.
 *
 * @author Matthew Dempsky <mdempsky@google.com>
 */
public class AESUtilsJCA {

	public static final int ITERATION_COUNT = 1000;
	public static final int BLOCK_SIZE = 16;

	private final Cipher cipher;
	private final Mac mac;
	private final byte[] passwordVerifier;

	/* State for implementing AES-CTR. */
	private final byte[] iv = new byte[BLOCK_SIZE];
	private final byte[] keystream = new byte[BLOCK_SIZE];
	private int next = BLOCK_SIZE;

	public AESUtilsJCA(String password, int keySize, byte[] salt) {
		if (keySize != 128 && keySize != 192 && keySize != 256)
			throw new IllegalArgumentException("Illegal keysize: " + keySize);

		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			char[] passwordChars = password.toCharArray();
			PBEKeySpec keySpec = new PBEKeySpec(passwordChars, salt, ITERATION_COUNT, keySize * 2 + 16);
			SecretKey sk = skf.generateSecret(keySpec);
			byte[] keyBytes = sk.getEncoded();

			cipher = Cipher.getInstance("AES");
			SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, 0, keySize / 8, "AES");
			
			//int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
			//System.out.println( "maxKeyLen=" + maxKeyLen );
			
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

			mac = Mac.getInstance("HmacSHA1");
			mac.init(new SecretKeySpec(keyBytes, keySize / 8, keySize / 8, "HmacSHA1"));

			passwordVerifier = new byte[2];
			System.arraycopy(keyBytes, 2 * (keySize / 8), passwordVerifier, 0, 2);
		} catch (NoSuchAlgorithmException e) {
			/* 
			 * XXX(mdempsky): Could happen if the user's JRE doesn't support PBKDF2,
			 * AES, and/or HMAC-SHA1.  Throw a better exception?
			 */
			throw new TypeNotPresentException(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			/* Shouldn't happen: our key specs match our algorithms. */
			throw new TypeNotPresentException(e.getMessage(), e);
		} catch (InvalidKeySpecException e) {
			/* Shouldn't happen: our key specs match our algorithms. */
			throw new TypeNotPresentException(e.getMessage(), e);
		} catch (NoSuchPaddingException e) {
			/* Shouldn't happen: we don't specify any padding schemes. */
			throw new TypeNotPresentException(e.getMessage(), e);
		}
	}

	public void cryptUpdate(byte[] in, int length) {
		try {
			/*
			 * We must implement CTR mode by hand, because WinZip's AES encryption
			 * scheme is incompatible with Java's AES/CTR/NoPadding.
			 */
			for (int i = 0; i < length; ++i) {
				/*
				 * If we've exhausted the current keystream block, we need to
				 * increment the iv and generate another one.
				 */
				if (next == BLOCK_SIZE) {
					for (int j = 0; j < BLOCK_SIZE; ++j)
						if (++iv[j] != 0)
							break;
					cipher.update(iv, 0, BLOCK_SIZE, keystream);
					next = 0;
				}

				in[i] ^= keystream[next++];
			}
		} catch (ShortBufferException e) {
			/* Shouldn't happen: our output buffer is always appropriately sized. */
			throw new Error();
		}
	}

	public void authUpdate(byte[] in, int length) {
		mac.update(in, 0, length);
	}

	public byte[] getFinalAuthentifier() {
		byte[] auth = new byte[10];
		System.arraycopy(mac.doFinal(), 0, auth, 0, 10);
		return auth;
	}

	public byte[] getPasswordVerifier() {
		return passwordVerifier;
	}

}
