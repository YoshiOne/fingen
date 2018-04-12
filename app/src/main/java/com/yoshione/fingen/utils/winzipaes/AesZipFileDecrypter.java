package com.yoshione.fingen.utils.winzipaes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.yoshione.fingen.utils.winzipaes.impl.AESDecrypter;
import com.yoshione.fingen.utils.winzipaes.impl.ByteArrayHelper;
import com.yoshione.fingen.utils.winzipaes.impl.CentralDirectoryEntry;
import com.yoshione.fingen.utils.winzipaes.impl.ExtRandomAccessFile;
import com.yoshione.fingen.utils.winzipaes.impl.ExtZipEntry;
import com.yoshione.fingen.utils.winzipaes.impl.ExtZipOutputStream;
import com.yoshione.fingen.utils.winzipaes.impl.ZipConstants;

/**
 * List/Extract data from AES encrypted WinZip file (readOnly).
 *
 * TODO - support 128 + 192 keys
 * TODO - refactor this class to use an ExtZipInputStream and put all "offset handling" there
 *
 * @author olaf@merkert.de
 * @author jos.v.roosmalen@gmail.com
 */
public class AesZipFileDecrypter implements ZipConstants {

	private static final Logger LOG = Logger.getLogger( AesZipFileDecrypter.class.getName() );

	// --------------------------------------------------------------------------

	/** charset to use for filename(s) - defaults to iso-8859-1 */
	public static String charset = "iso-8859-1";

	/** size of buffer to use for byte[] operations - defaults to 1024 */
	protected static int bufferSize = 1024 * 10;

	// --------------------------------------------------------------------------

	protected AESDecrypter decrypter;
	
	// --------------------------------------------------------------------------

	/** random access file to access the archive data */
	protected ExtRandomAccessFile raFile;

	/** where does the directory (after file data) start? */
	protected long dirOffsetPos;

	protected File zipFile;
	
	protected String comment;
	
	public AesZipFileDecrypter( File zipFile, AESDecrypter decrypter ) throws IOException {
		this.zipFile = zipFile;
		this.decrypter = decrypter;
		this.raFile = new ExtRandomAccessFile( zipFile );
		initDirOffsetPosAndComment();
	}

	protected void initDirOffsetPosAndComment() throws IOException {
		// zip files without a comment contain the offset/position of the central directory at this fixed position
		this.dirOffsetPos = zipFile.length() - 6;
		final int dirOffset = raFile.readInt( this.dirOffsetPos - 16 );
		if( dirOffset!=ENDSIG ) {
			// if a comment is present, search the ENDSIG constant, starting at the end of the zip file
			byte[] endsig = ByteArrayHelper.toByteArray((int)ZipConstants.ENDSIG);
			long endsigPos = raFile.lastPosOf(endsig);
			if( endsigPos==-1 ) {
				throw new ZipException("expected ENDSIC not found (marks the beginning of the central directory at end of the zip file)");
			} else {
				this.dirOffsetPos = endsigPos+16;
				short commentLength = raFile.readShort( this.dirOffsetPos + 4 );
				this.comment = new String( raFile.readByteArray( this.dirOffsetPos+6, commentLength ) );
			}
		}		
	}

	public void close() throws IOException {
		raFile.close();
	}
	
	// --------------------------------------------------------------------------

	/**
	 * return list of entries from zip file - the list contains files as well as non-decryptable (!)
	 * directories, that can be determined by using the isDirectory() method
	 */
	public List<ExtZipEntry> getEntryList() throws IOException, ZipException {
		List<ExtZipEntry> out = new ArrayList<ExtZipEntry>();

		short totalNumberOfEntries = this.getNumberOfEntries();
		final int dirOffset = raFile.readInt( this.dirOffsetPos );

		long fileOffset = dirOffset;
		for( int i=0; i<totalNumberOfEntries; i++ ) {
			int censig = raFile.readInt( fileOffset );
			if( censig!=CENSIG ) {
				throw new ZipException("expected CENSIC not found at entry no " + (i+1) + " in central directory at end of zip file at " + fileOffset);
			}
			
			short fileNameLength = raFile.readShort( fileOffset + 28 );
			short extraFieldLength = raFile.readShort( fileOffset + 30 );
			long fileOffsetPos = fileOffset + 28 + 14;
			long fileDataOffset = raFile.readInt( fileOffsetPos );
			int locsig = raFile.readInt( fileDataOffset );
			if( locsig!=LOCSIG ) {
				throw new ZipException("expected LOCSIC not found at alleged position of data for file no " + (i+1));
			}

			byte[] fileNameBytes = raFile.readByteArray( fileOffsetPos+4, fileNameLength );
			long nextFileOffset = raFile.getFilePointer();
			String fileName = new String( fileNameBytes, charset );
			
			CentralDirectoryEntry cde = new CentralDirectoryEntry( raFile, fileOffset );
			ExtZipEntry zipEntry = new ExtZipEntry( fileName, cde );

			zipEntry.setCompressedSize( cde.getCompressedSize() );
			zipEntry.setSize( cde.getUncompressedSize() );

			long dosTime = raFile.readInt( fileOffset + 12 );
			zipEntry.setTime( ExtZipEntry.dosToJavaTime(dosTime) );
			
			if( cde.isEncrypted() ) {
				zipEntry.setMethod( cde.getActualCompressionMethod() );
				zipEntry.setOffset( (int)(cde.getLocalHeaderOffset() + cde.getLocalHeaderSize()) + cde.getCryptoHeaderLength() );
				zipEntry.initEncryptedEntry();
			} else {
				zipEntry.setMethod( ZipEntry.DEFLATED );
				zipEntry.setPrimaryCompressionMethod( ZipEntry.DEFLATED );
			}

			nextFileOffset += extraFieldLength;

			out.add(zipEntry);

			fileOffset = nextFileOffset;
		}

		return out;
	}

	public ExtZipEntry getEntry( String name ) throws IOException, ZipException, DataFormatException {
		for( ExtZipEntry zipEntry : getEntryList() ) {
			if( name.equals(zipEntry.getName()) ) {
				return zipEntry;
			}
		}
		return null;
	}

	protected void checkZipEntry(ExtZipEntry zipEntry) throws ZipException {
		if( zipEntry==null ) {
			throw new ZipException("zipEntry must NOT be NULL");
		}
		if( zipEntry.isDirectory() ) {
			throw new ZipException("directory entries cannot be decrypted");
		}
		if( !zipEntry.isEncrypted() ) {
			throw new ZipException( "currently only extracts encrypted files - use java.util.zip to unzip" );
		}
	}
	
	public void extractEntryWithTmpFile( ExtZipEntry zipEntry, File outFile, String password ) throws IOException, ZipException, DataFormatException {
		checkZipEntry(zipEntry);

		CentralDirectoryEntry cde = zipEntry.getCentralDirectoryEntry();
		if( !cde.isAesEncrypted() ) {
			throw new ZipException("only AES encrypted files are supported");
		}
		
		int cryptoHeaderOffset = zipEntry.getOffset() - cde.getCryptoHeaderLength();
		
		byte[] salt = raFile.readByteArray( cryptoHeaderOffset, 16 );
		byte[] pwVerification = raFile.readByteArray( cryptoHeaderOffset+16, 2 );

		if( LOG.isLoggable(Level.FINEST) ) {
			LOG.finest( "\n" + cde.toString() );
			LOG.finest( "offset    = " + zipEntry.getOffset() );
			LOG.finest( "cryptoOff = " + cryptoHeaderOffset );
			LOG.finest( "password  = " + password + " - " + password.length() );
			LOG.finest( "salt      = " + ByteArrayHelper.toString(salt) + " - " + salt.length );
			LOG.finest( "pwVerif   = " + ByteArrayHelper.toString(pwVerification) + " - " + pwVerification.length );
		}
		
		// encrypter throws ZipException for wrong password
		decrypter.init( password, 256, salt, pwVerification );

		// create tmp file that contains the decrypted, but still compressed data
		File tmpFile = new File( outFile.getPath() + "_TMP.zip" );
		makeDir( tmpFile.getParent() );
		
		ExtZipOutputStream zos = null;
		ZipFile zf = null;
		FileOutputStream fos = null;
		InputStream is = null;		
		try {
			zos = new ExtZipOutputStream( tmpFile );
			ExtZipEntry tmpEntry = new ExtZipEntry( zipEntry );
			tmpEntry.setPrimaryCompressionMethod( zipEntry.getMethod() );
			zos.putNextEntry( tmpEntry );
	
			raFile.seek( cde.getOffset() );
			byte[] buffer = new byte[bufferSize];
			int remaining = (int)zipEntry.getEncryptedDataSize();
			while( remaining>0 ) {
				int len = (remaining>buffer.length) ? buffer.length : remaining;
				int read = raFile.readByteArray(buffer,len);
				decrypter.decrypt( buffer, read );
				zos.writeBytes( buffer, 0, read );
				remaining -= len;
			}
			zos.finish();
			zos = null;
	
			byte[] storedMac = new byte[10];
			raFile.readByteArray(storedMac,10);
			byte[] calcMac = decrypter.getFinalAuthentication();
			if( LOG.isLoggable(Level.FINE) ) {
				LOG.fine(	"storedMac=" + Arrays.toString(storedMac) );
				LOG.fine(	"calcMac=" + Arrays.toString(calcMac) );
			}
			if( !Arrays.equals(storedMac, calcMac ) ) {
				throw new ZipException("stored authentication (mac) value does not match calculated one");
			}
	
			zf = new ZipFile( tmpFile );
			ZipEntry ze = zf.entries().nextElement();
			is = zf.getInputStream( ze );
			fos = new FileOutputStream ( outFile.getPath() );
			int read = is.read( buffer );
			while( read>0 ) {
				fos.write( buffer, 0, read );
				read = is.read( buffer );
			}
		} finally {
			if (zos != null) {
				zos.close();
			}
			if (zf != null) {
				zf.close();
			}
			if (fos != null) {
				fos.close();
			}
			if (is != null) {
				is.close();
			}
		}

		tmpFile.delete();
	}

	/** number of entries in file (files AND directories) */
	public short getNumberOfEntries() throws IOException {
		return raFile.readShort( this.dirOffsetPos-6 );
	}

	protected static void makeDir(String dirStr) {
		if(dirStr!=null) {
			makeDir(new File(dirStr));
		}
	}

	protected static void makeDir(File dir) {
	  if( dir!=null ) { 
	    if( !dir.exists() ) {
	    	if( dir.getParent()!=null ) {
		      File parentDir = new File(dir.getParent());
		      if( !parentDir.exists() ) {
		      	makeDir(parentDir);
		      }
	    	}
	      dir.mkdir();
	    }
	  }
	}

	/** return the zip file's comment (if defined) */
	public String getComment() {
		return comment;
	}
	
	// --------------------------------------------------------------------------

	/**
	 * extract zipEntry - uses in-memory, so your file (stream contents) should not be too big 
	 */
	public void extractEntry(ExtZipEntry zipEntry, OutputStream outStream, String password)
			throws IOException, ZipException, DataFormatException {
		checkZipEntry(zipEntry);

		ZipInputStream zipInputStream = null;
		ByteArrayOutputStream bos = null;
		try {
			CentralDirectoryEntry cde = zipEntry.getCentralDirectoryEntry();
			if (!cde.isAesEncrypted()) {
				throw new ZipException("only AES encrypted files are supported");
			}
			int cryptoHeaderOffset = zipEntry.getOffset() - cde.getCryptoHeaderLength();
			byte[] salt = raFile.readByteArray(cryptoHeaderOffset, 16);
			byte[] pwVerification = raFile.readByteArray(cryptoHeaderOffset + 16, 2);			
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("\n" + cde.toString());
				LOG.finest("offset    = " + zipEntry.getOffset());
				LOG.finest("cryptoOff = " + cryptoHeaderOffset);
				LOG.finest("password  = " + password + " - " + password.length());
				LOG.finest("salt      = " + ByteArrayHelper.toString(salt) + " - " + salt.length);
				LOG.finest("pwVerif   = " + ByteArrayHelper.toString(pwVerification) + " - " + pwVerification.length);
			}
			// encrypter throws ZipException for wrong password
			decrypter.init(password, 256, salt, pwVerification);

			bos = new ByteArrayOutputStream(bufferSize);
			ExtZipOutputStream zos = new ExtZipOutputStream(bos);
			ExtZipEntry tmpEntry = new ExtZipEntry(zipEntry);
			tmpEntry.setPrimaryCompressionMethod(zipEntry.getMethod());
			tmpEntry.setCompressedSize(zipEntry.getEncryptedDataSize());
			zos.putNextEntry(tmpEntry);
			raFile.seek(cde.getOffset());
			byte[] buffer = new byte[bufferSize];
			CRC32 crc32 = new CRC32();
			int remaining = (int) zipEntry.getEncryptedDataSize();
			while (remaining > 0) {
				int len = (remaining > buffer.length) ? buffer.length : remaining;
				int read = raFile.readByteArray(buffer, len);
				decrypter.decrypt(buffer, read);
				zos.writeBytes(buffer, 0, read);
				remaining -= len;
				crc32.update(buffer, 0, read);
			}
			tmpEntry.setCrc(crc32.getValue());
			zos.finish();
			byte[] storedMac = new byte[10];
			raFile.readByteArray(storedMac, 10);
			byte[] calcMac = decrypter.getFinalAuthentication();
			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("storedMac=" + Arrays.toString(storedMac));
				LOG.fine("calcMac=" + Arrays.toString(calcMac));
			}
			if (!Arrays.equals(storedMac, calcMac)) {
				throw new ZipException("stored authentication (mac) value does not match calculated one");
			}

			zipInputStream = new ZipInputStream(new ByteArrayInputStream(bos.toByteArray()));
			ZipEntry entry = zipInputStream.getNextEntry();
			// At the end of the entry read-cycle a CRC check is performed.
			// Because our entry doesn't have a CRC this will result in an Exception
			// we solve this by updating a CRC and pass this to the entry.
			entry.setCrc( crc32.getValue() );
			if( entry.getSize()!=0 ) {
				crc32 = new CRC32();
				int read = zipInputStream.read(buffer);
				while (read > 0) {
					outStream.write(buffer, 0, read);
					crc32.update(buffer, 0, read);
					entry.setCrc(crc32.getValue());
					read = zipInputStream.read(buffer);
				}
			}

		} finally {
			if (bos != null) {
				bos.close();
			}
			if (zipInputStream != null) {
				zipInputStream.close();
			}
// not opened here, so we don't close it here
//			if (outStream != null) {
//				outStream.close();
//			}
		}
	}

	/**
	 * extract zipEntry - uses in-memory, so your file should not be too big 
	 */
	public void extractEntry(ExtZipEntry zipEntry, File outFile, String password) throws IOException,
			ZipException, DataFormatException {
		ByteArrayOutputStream bos = null;
		FileOutputStream fos = null;
		try {
			bos = new ByteArrayOutputStream(bufferSize);
			fos = new FileOutputStream(outFile);
			extractEntry(zipEntry, bos, password);
			byte[] buffer = bos.toByteArray();
			fos.write(buffer);
		} finally {
			if (bos != null) {
				bos.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}

}
