package com.yoshione.fingen.utils.winzipaes.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 * Write zip entries to Zip-File, encrypted or not encrypted.
 * 
 * @author olaf@merkert.de
 */
public class ExtZipOutputStream implements ZipConstants {

	public ExtZipOutputStream(File file) throws IOException {
		out = new FileOutputStream(file);
	}

	public ExtZipOutputStream(OutputStream out) {
		this.out = out;
	}

	protected String comment;
	
	protected OutputStream out;

	/** number of bytes written to out */
	protected int written;

	public int getWritten() {
		return this.written;
	}

	public void writeBytes(byte[] b) throws IOException {
		out.write(b);
		written += b.length;
	}

	public void writeShort(int v) throws IOException {
		out.write((v >>> 0) & 0xff);
		out.write((v >>> 8) & 0xff);
		written += 2;
	}

	public void writeInt(long v) throws IOException {
		out.write((int) ((v >>> 0) & 0xff));
		out.write((int) ((v >>> 8) & 0xff));
		out.write((int) ((v >>> 16) & 0xff));
		out.write((int) ((v >>> 24) & 0xff));
		written += 4;
	}

	public void writeBytes(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		written += len;
	}

	// --------------------------------------------------------------------------

	protected final static short ZIP_VERSION = 20; // version set by
													// java.util.zip

	protected void writeFileInfo(ExtZipEntry entry) throws IOException {
		writeShort(ZIP_VERSION); // version needed to extract

		// general purpose bit flag - 0x0001 indicates encryption 2 bytes
		writeShort(entry.getFlag());

		writeShort(entry.getPrimaryCompressionMethod()); // primary compression
															// method -
															// 0x63==encryption

		writeInt(entry.getDosTime()); // 2 bytes last mod file time + 2 bytes
										// last mod file date

		writeInt(entry.getCrc());

		// 28 bytes is the encryption overhead (caused by 256-bit AES key)
		// 2 bytes pwVerification + 16 bytes SALT + 10 bytes AUTHENTICATION

		writeInt((int) entry.getCompressedSize()); // compressed size
		writeInt((int) entry.getSize()); // uncompressed size

		writeShort(entry.getName().length()); // file name length
		if (entry.getExtra() != null) {
			writeShort(entry.getExtra().length); // extra field length
		} else {
			writeShort(0);
		}
	}

	private List<ExtZipEntry> entries = new ArrayList<ExtZipEntry>();

	protected void writeDirEntry(ExtZipEntry entry) throws IOException {
		writeInt(CENSIG); // writeBytes( new byte[] { 0x50, 0x4b, 0x01, 0x02 }
							// ); // directory signature
		writeShort(ZIP_VERSION); // version made by
		writeFileInfo(entry);

		writeShort(0x00); // file comment length 2 bytes
		writeShort(0x00); // disk number start (unused) 2 bytes
		writeShort(0x00); // internal file attributes (unsued) 2 bytes
		writeInt(0x00); // external file attributes (unused) 4 bytes

		writeInt(entry.getOffset()); // relative offset of local header 4 bytes

		writeBytes(entry.getName().getBytes("iso-8859-1"));

		writeExtraBytes(entry);
	}

	protected void writeExtraBytes(ZipEntry entry) throws IOException {
		byte[] extraBytes = entry.getExtra();
		if (extraBytes != null) {
			writeBytes(extraBytes);
		}
	}

	// --------------------------------------------------------------------------

	public void putNextEntry(ExtZipEntry entry) throws IOException {
		entries.add(entry);

		entry.setOffset(written);

		// file header signature
		writeInt(LOCSIG);

		writeFileInfo(entry);
		writeBytes(entry.getName().getBytes("iso-8859-1"));
		writeExtraBytes(entry);
	}

	/**
	 * Finishes writing the contents of the ZIP output stream.
	 */
	public void finish() throws IOException {
		int dirOffset = written; // central directory (at end of zip file)
								 // starts here

		int startOfCentralDirectory = written;

		Iterator<ExtZipEntry> it = entries.iterator();
		while (it.hasNext()) {
			ExtZipEntry entry = it.next();
			writeDirEntry(entry);
		}
		int centralDirectorySize = written - startOfCentralDirectory;

		writeInt(ENDSIG); // end of central dir signature 4 bytes

		writeShort(0x00); // number of this disk 2 bytes
		writeShort(0x00); // number of the disk with the start of the central directory 2 bytes

		writeShort(entries.size()); // total number of entries in central directory on this disk 2 bytes
		writeShort(entries.size()); // total number of entries in the central directory 2 bytes

		writeInt(centralDirectorySize); // size of the central directory 4 bytes

		writeInt(dirOffset);	// offset of start of central dir, with respect to starting disk 4 bytes
		
		byte[] commentBytes = this.comment!=null ? this.comment.getBytes() : new byte[0];		
		writeShort(commentBytes.length); // .ZIP file comment length 2 bytes
		if( commentBytes.length>0 ) {
			writeBytes(commentBytes);
		}

		out.close();
	}
	
	public void close() throws IOException {
		out.close();
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
