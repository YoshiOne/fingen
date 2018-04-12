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

import java.util.Arrays;
import java.util.zip.ZipException;

/**
 * Decrypter adapter for the Java Cryptography Architecture.
 *
 * @author Matthew Dempsky <mdempsky@google.com>
 */
public class AESDecrypterJCA implements AESDecrypter {

	private AESUtilsJCA utils;

	public void init(String password, int keySize, byte[] salt, byte[] passwordVerifier) throws ZipException {
		this.utils = new AESUtilsJCA(password, keySize, salt);
		if (!Arrays.equals(passwordVerifier, utils.getPasswordVerifier()))
			throw new ZipException("Password verification failed");
	}

	@Override
	public void decrypt(byte[] in, int length) {
		utils.authUpdate(in, length);
		utils.cryptUpdate(in, length);
	}

	@Override
	public byte[] getFinalAuthentication() {
		return utils.getFinalAuthentifier();
	}
}
