package com.yoshione.fingen.utils.winzipaes.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * Provide InputStream access to <b>compressed data</b> from one ZipEntry contained
 * within one ZipFile. Necessary as java.util.zip.ZipInputStream only provides access to
 * the <b>uncompressed data</b>.
 *
 * @author olaf@merkert.de
 */
public class ZipFileEntryInputStream implements ZipConstants {

	private static final Logger LOG = Logger.getLogger(ZipFileEntryInputStream.class.getName());

	protected FileInputStream fis;
	
	protected long startPos;

	protected long endPos;

	protected long currentPos;

	protected long compressedSize;

	public long getCompressedSize() {
		return this.compressedSize;
	}

	public ZipFileEntryInputStream( String fileName ) throws IOException {
		fis = new FileInputStream(fileName);
	}

	/**
	 * position input stream to start of ZipEntry this instance was created for
	 *
	 * @throws IOException
	 */
	public void nextEntry( ZipEntry ze ) throws IOException {
		LOG.fine("nextEntry().currentPos=" + currentPos);
		
		byte[] intBuffer = new byte[4];
		int bytesRead = fis.read(intBuffer);
		LOG.fine("bytes read="+bytesRead);
		if( bytesRead==-1 ) {
			// this occurred on android once, with FileInputStream as my superclass
			throw new IOException("no data available - available=" + fis.available());
		}
		
		int dataDescriptorLength = 0;
		if( Arrays.equals(intBuffer, new byte[] { 0x50, 0x4b, 0x07, 0x08 }) ) {
			// header does not belong to next file, but is start of the "data descriptor" of last file
			// skip this data descriptor containing crc32(4), compressedSize(4), uncompressedSize(4)
			dataDescriptorLength = 4 + 4 + 4;
			fis.skip( dataDescriptorLength );
			// read local file header signature
			fis.read(intBuffer);
		}
		
		if( !Arrays.equals(intBuffer, new byte[] { 0x50, 0x4b, 0x03, 0x04 }) ) {
			throw new IOException("wrong local file header signature - value=" + ByteArrayHelper.toString(intBuffer) );
		}

		// info only - if bit-3 is set, current entry is followed by data descriptor
		boolean hasDataDescriptor = (ze.getMethod() & 8) > 0;
		LOG.fine( "nextEntry().hasDataDescriptor=" + hasDataDescriptor );

		this.compressedSize = ze.getCompressedSize();
		
		fis.skip(14 + 4 + 4); // 14 + localFileHeaderSignature(4) + compressedSize(4) + size(4)

		byte[] shortBuffer = new byte[2];
		fis.read(shortBuffer);
		int fileNameLength = ByteArrayHelper.toInt(shortBuffer);

		fis.read(shortBuffer);
		int extraFieldLength = ByteArrayHelper.toInt(shortBuffer);

		startPos = 18 + 12 + fileNameLength + extraFieldLength + dataDescriptorLength;
		currentPos = startPos;
		endPos = startPos + this.compressedSize;

		fis.skip( fileNameLength + extraFieldLength );
	}

	// should work without this, but never trust an OO system
	public int read( byte[] b ) throws IOException {
		return this.read(b, 0, b.length);
	}

	public int read( byte[] b, int off, int len ) throws IOException {
		int bytesRead = -1;
		int remainingBytes = (int) (endPos - currentPos);
		if( remainingBytes > 0 ) {
			if( currentPos + len < endPos ) {
				bytesRead = fis.read(b, off, len);
				currentPos += bytesRead;
			} else {
				bytesRead = fis.read(b, off, remainingBytes);
				currentPos += bytesRead;
			}
		}
		return bytesRead;
	}

	public void close() throws IOException {
		fis.close();
	}
	
}
