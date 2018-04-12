package com.yoshione.fingen.utils.winzipaes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.yoshione.fingen.utils.winzipaes.impl.AESEncrypter;
import com.yoshione.fingen.utils.winzipaes.impl.ExtZipEntry;
import com.yoshione.fingen.utils.winzipaes.impl.ExtZipOutputStream;
import com.yoshione.fingen.utils.winzipaes.impl.ZipFileEntryInputStream;

/**
 * Create ZIP archive containing AES-256 encrypted entries. <br>
 * One instance of this class represents one encrypted ZIP file, that can receive add() method calls
 * that must be followed by one final call to close() to write the final archive part and close the
 * output stream.
 * 
 * TODO - support 128 + 192 keys
 * 
 * @author olaf@merkert.de
 * @author jean-luc.cooke@greenparty.ca
 */
public class AesZipFileEncrypter {

	private static final Logger LOG = Logger.getLogger(AesZipFileEncrypter.class.getName());

	// --------------------------------------------------------------------------

	protected AESEncrypter encrypter;
	
	// --------------------------------------------------------------------------

	protected ExtZipOutputStream zipOS;

	/**
	 * 
	 * @param pathName
	 *          to output zip file (aes encrypted zip file)
	 */
	public AesZipFileEncrypter(String pathName, AESEncrypter encrypter) throws IOException {
		zipOS = new ExtZipOutputStream(new File(pathName));
		this.encrypter = encrypter;
	}

	/**
	 * 
	 * @param outFile
	 *          output file (aes encrypted zip file)
	 */
	public AesZipFileEncrypter(File outFile, AESEncrypter encrypter) throws IOException {
		zipOS = new ExtZipOutputStream(outFile);
		this.encrypter = encrypter;
	}

	public AesZipFileEncrypter(OutputStream outFile, AESEncrypter encrypter) throws IOException {
		zipOS = new ExtZipOutputStream(outFile);
		this.encrypter = encrypter;
	}

	// --------------------------------------------------------------------------

	protected void add(ExtZipEntry zipEntry, InputStream zipData) throws IOException,
			UnsupportedEncodingException {
		zipOS.putNextEntry(zipEntry);

		byte[] data = new byte[1024];
		int read = zipData.read(data);
		while (read != -1) {
			zipOS.writeBytes(data, 0, read);
			read = zipData.read(data);
		}
	}

	protected void add(ZipFile inFile, String password) throws IOException,
			UnsupportedEncodingException {
		ZipFileEntryInputStream zfe = new ZipFileEntryInputStream(inFile.getName());			
		try {
			Enumeration<? extends ZipEntry> en = inFile.entries();
			while (en.hasMoreElements()) {
				ZipEntry ze = en.nextElement();
				zfe.nextEntry(ze);
				add(ze, zfe, password);
			}
		} finally {
			zfe.close();
		}
	}

	// TODO - zipEntry might use extended local header
	protected void add(ZipEntry zipEntry, ZipFileEntryInputStream zipData, String password)
			throws IOException, UnsupportedEncodingException {
		encrypter.init(password, 256);

		ExtZipEntry entry = new ExtZipEntry(zipEntry.getName());
		entry.setMethod(zipEntry.getMethod());
		entry.setSize(zipEntry.getSize());
		entry.setCompressedSize(zipEntry.getCompressedSize() + 28);
		entry.setTime(zipEntry.getTime());
		entry.initEncryptedEntry();

		zipOS.putNextEntry(entry);
		// ZIP-file data contains: 1. salt 2. pwVerification 3. encryptedContent 4. authenticationCode
		zipOS.writeBytes(encrypter.getSalt());
		zipOS.writeBytes(encrypter.getPwVerification());

		byte[] data = new byte[1024];
		int read = zipData.read(data);
		while (read != -1) {
			encrypter.encrypt(data, read);
			zipOS.writeBytes(data, 0, read);
			read = zipData.read(data);
		}

		byte[] finalAuthentication = encrypter.getFinalAuthentication();
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("finalAuthentication=" + Arrays.toString(finalAuthentication) + " at pos="
					+ zipOS.getWritten());
		}

		zipOS.writeBytes(finalAuthentication);
	}

	/**
	 * Add un-encrypted + un-zipped file to encrypted zip file.<br>
	 *
	 * @param file to add
	 * @param pathForEntry to be used for addition of the file (path within zip file)
	 * @param password to be used for encryption
	 */
	public void add(File file, String pathForEntry, String password) throws IOException, UnsupportedEncodingException {
		FileInputStream fis = new FileInputStream(file);
		try {
			add(pathForEntry, fis, password);
		} finally {
			fis.close();
		}
	}

	/**
	 * Add un-encrypted + un-zipped file to encrypted zip file.
	 * 
	 * @param file to add, provides the path of the file within the zip file via its getPath()
	 * @param password to be used for encryption
	 */
	public void add(File file, String password) throws IOException, UnsupportedEncodingException {
		FileInputStream fis = new FileInputStream(file);
		try {
			add(file.getName(), fis, password);
		} finally {
			fis.close();
		}
	}

	/**
	 * Add un-encrypted + un-zipped InputStream contents as file "name" to encrypted zip file.
	 * 
	 * @param name of the new zipEntry within the zip file
	 * @param is provides the data to be added. It is left open and should be closed by the caller 
	 * 				(it is safe to do so immediately after the call).
	 * @param password to be used for encryption
	 */
	public void add(String name, InputStream is, String password) throws IOException,	UnsupportedEncodingException {
		encrypter.init(password, 256);

		// Compress contents of inputStream and report on bytes read
		// we need to first compress to know details of entry
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DeflaterOutputStream dos = new DeflaterOutputStream(bos, new Deflater(9, true), 8 * 1024);
		int read=0;
		long inputLen = 0;
		byte[] buf = new byte[8 * 1024];
		while ((read = is.read(buf)) > 0) {
			inputLen += read;
			dos.write(buf, 0, read);
		}
		dos.close();
		byte[] data = bos.toByteArray();

		ExtZipEntry entry = new ExtZipEntry(name);
		entry.setMethod(ZipEntry.DEFLATED);
		entry.setSize(inputLen);
		entry.setCompressedSize(data.length + 28);
		entry.setTime((new java.util.Date()).getTime());
		entry.initEncryptedEntry();

		zipOS.putNextEntry(entry);
		// ZIP-file data contains: 1. salt 2. pwVerification 3. encryptedContent 4. authenticationCode
		zipOS.writeBytes(encrypter.getSalt());
		zipOS.writeBytes(encrypter.getPwVerification());

		encrypter.encrypt(data, data.length);
		zipOS.writeBytes(data, 0, data.length);

		byte[] finalAuthentication = encrypter.getFinalAuthentication();
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("finalAuthentication=" + Arrays.toString(finalAuthentication) + " at pos="
					+ zipOS.getWritten());
		}

		zipOS.writeBytes(finalAuthentication);
	}

	// --------------------------------------------------------------------------

	/**
	 * Zip contents of inFile to outFile.
	 */
	public static void zip(File inFile, File outFile) throws IOException {
		FileInputStream fin = new FileInputStream(inFile);
		FileOutputStream fout = new FileOutputStream(outFile);
		ZipOutputStream zout = new ZipOutputStream(fout);

		try {
			zout.putNextEntry(new ZipEntry(inFile.getName()));
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fin.read(buffer)) > 0) {
				zout.write(buffer, 0, len);
			}
			zout.closeEntry();
		} finally {
			zout.close();
			fin.close();
		}
	}

	// --------------------------------------------------------------------------

	/**
	 * Take all elements (files) from zip file and add them ENCRYPTED with password to the new zip
	 * file created with this instance. <br>
	 * Encrypted data of each file has the same size as the compressed data, though the file size is
	 * increased by 26 bytes for salt and pw-verification bytes.<br>
	 * While the {@link #add(File, String)} method does not need an additional zip file, this method
	 * comes in handy, when your input data is larger than your available memory. 
	 * 
	 * @param pathToZipFile
	 *          provides zipFileEntries for encryption
	 * @param password
	 *          used to perform the encryption
	 * @throws IOException
	 */
	public void addAll(File pathToZipFile, String password) throws IOException {
		ZipFile zipFile = new ZipFile(pathToZipFile);
		try {
			add(zipFile, password);
		} finally {
			zipFile.close();
		}
	}

	// --------------------------------------------------------------------------

	public void setComment( String comment ) {
		zipOS.setComment(comment);
	}

	// --------------------------------------------------------------------------

	/**
	 * Client is required to call this method after he added all entries so the final archive part is
	 * written.
	 */
	public void close() throws IOException {
		zipOS.finish();
	}

	// --------------------------------------------------------------------------

	/**
	 * Zip + encrypt one "inFile" to one "outZipFile" using "password".
	 */
	public static void zipAndEncrypt(File inFile, File outFile, String password, AESEncrypter encrypter) throws IOException {
		AesZipFileEncrypter enc = new AesZipFileEncrypter(outFile,encrypter);
		try {
			enc.add(inFile, password);
		} finally {
			enc.close();
		}
	}

	// --------------------------------------------------------------------------

	/**
	 * Encrypt all files from an existing zip to one new "zipOutFile" using "password".
	 */
	public static void zipAndEncryptAll(File inZipFile, File outFile, String password, AESEncrypter encrypter) throws IOException {
		AesZipFileEncrypter enc = new AesZipFileEncrypter(outFile,encrypter);
		try {
			enc.addAll(inZipFile, password);
		} finally {
			enc.close();
		}
	}

}
