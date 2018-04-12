package com.yoshione.fingen.utils.winzipaes.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;

/**
 * information about one zip entry that is written to an encrypted zip archive
 * or read from one
 * 
 * @author olaf@merkert.de
 */
public class ExtZipEntry extends ZipEntry {

	private CentralDirectoryEntry centralDirectoryEntry;
	
	/** empty instance with only a name */
	public ExtZipEntry(String name) {
		super(name);
	}

	/** copy all "non-compression" attributes */
	public ExtZipEntry(ExtZipEntry entry) {
		super(entry.getName());
		setCompressedSize(entry.getCompressedSize());
		setSize(entry.getSize());
		setComment(entry.getComment());
		setTime(entry.getTime());
		setMethod(entry.getMethod());
	}

	public ExtZipEntry(String name,CentralDirectoryEntry centralDirectoryEntry) {
		super(name);
		this.centralDirectoryEntry = centralDirectoryEntry;
	}
	
	public void initEncryptedEntry() {
		setCrc(0); // CRC-32 / for encrypted files it's 0 as AES/MAC checks integritiy

		this.flag |= 1; // bit0 - encrypted
		// flag |= 8; // bit3 - use data descriptor

		this.primaryCompressionMethod = 0x63;

		byte[] extraBytes = new byte[11];
		extraBytes = new byte[11];

		// extra data header ID for AES encryption is 0x9901
		extraBytes[0] = 0x01;
		extraBytes[1] = (byte)0x99;

		// data size (currently 7, but subject to possible increase in the
		// future)
		extraBytes[2] = 0x07; // data size
		extraBytes[3] = 0x00; // data size

		// Integer version number specific to the zip vendor
		extraBytes[4] = 0x02; // version number
		extraBytes[5] = 0x00; // version number

		// 2-character vendor ID
		extraBytes[6] = 0x41; // vendor id
		extraBytes[7] = 0x45; // vendor id

		// AES encryption strength - 1=128, 2=192, 3=256
		extraBytes[8] = 0x03;

		// actual compression method - 0x0000==stored (no compression) - 2 bytes
		extraBytes[9] = (byte) (getMethod() & 0xff);
		extraBytes[10] = (byte) ((getMethod() & 0xff00) >> 8);

		setExtra(extraBytes);
	}

	protected int flag;

	public int getFlag() {
		return this.flag;
	}

	public boolean isAesEncrypted() {
		return isEncrypted() && centralDirectoryEntry!=null && centralDirectoryEntry.isAesEncrypted();
	}
	
	public boolean isEncrypted() {
		return (flag & 1) > 0;
	}

	protected int offset;

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	// 0x63 for encryption
	protected int primaryCompressionMethod;

	public int getPrimaryCompressionMethod() {
		return primaryCompressionMethod;
	}

	public void setPrimaryCompressionMethod(int primaryCompressionMethod) {
		this.primaryCompressionMethod = primaryCompressionMethod;
	}

	/**
	 * Encrypted files: Note that the value in the "compressed size" fields of
	 * the local file header and the central directory entry is the total size
	 * of all the items listed above. In other words, it is the total size of
	 * the salt value, password verification value, encrypted data, and
	 * authentication code.
	 * 
	 * @return data size only
	 */
	public long getEncryptedDataSize() {
		// authentication (10), salt (16), verification (2)
		return getCompressedSize() - 10 - 16 - 2;
	}

	public CentralDirectoryEntry getCentralDirectoryEntry() {
		return centralDirectoryEntry;
	}

	@Override
	public void setSize(long size) {
		if( size<0 ) {
			size = (size & 0xffffffffL);
		}
		super.setSize(size);
	}
	
	// --------------------------------------------------------------------------

	/**
	 * ZipEntry (my superclass) uses dosTime internally. On getTime() you get a
	 * java time based long value. This method provides the DOS value that is
	 * stored in the zip file.
	 */
	public long getDosTime() {
		return javaToDosTime(getTime());
	}

	public static long javaToDosTime(long javaTime) {
		Date d = new Date(javaTime);
		Calendar ca = Calendar.getInstance();
		ca.setTime(d);
		int year = ca.get(Calendar.YEAR);
		if (year < 1980) {
			return (1 << 21) | (1 << 16);
		}
		return (year - 1980) << 25 | (ca.get(Calendar.MONTH) + 1) << 21
				| ca.get(Calendar.DAY_OF_MONTH) << 16
				| ca.get(Calendar.HOUR_OF_DAY) << 11
				| ca.get(Calendar.MINUTE) << 5 | ca.get(Calendar.SECOND) >> 1;
	}

	public static long dosToJavaTime(long dosTime) {
		Calendar ca = Calendar.getInstance();
		ca.set(Calendar.YEAR, (int) ((dosTime >> 25) & 0x7f) + 1980);
		ca.set(Calendar.MONTH, (int) ((dosTime >> 21) & 0x0f) - 1);
		ca.set(Calendar.DATE, (int) (dosTime >> 16) & 0x1f);
		ca.set(Calendar.HOUR_OF_DAY, (int) (dosTime >> 11) & 0x1f);
		ca.set(Calendar.MINUTE, (int) (dosTime >> 5) & 0x3f);
		ca.set(Calendar.SECOND, (int) (dosTime << 1) & 0x3e);
		return ca.getTime().getTime();
	}

}
