package com.yoshione.fingen.utils.winzipaes.impl;

/**
 * byte[] functionality
 *
 * @author olaf@merkert.de
 */
public class ByteArrayHelper {

  public static long toLong(byte[] in) {
    long out = 0;
    for( int i=in.length-1; i>0; i-- ) {
      out |= in[i] & 0xff;
      out <<= 8;
    }
    out |= in[0] & 0xff;
    return out;
  }

  public static int toInt(byte[] in) {
    int out = 0;
    for( int i=in.length-1; i>0; i-- ) {
      out |= in[i] & 0xff;
      out <<= 8;
    }
    out |= in[0] & 0xff;
    return out;
  }

  public static short toShort(byte[] in) {
    short out = 0;
    for( int i=in.length-1; i>0; i-- ) {
      out |= in[i] & 0xff;
      out <<= 8;
    }
    out |= in[0] & 0xff;
    return out;
  }

	public static byte[] toByteArray(int in) {
		byte[] out = new byte[4];

		out[0] = (byte)in;
		out[1] = (byte)(in >> 8);
		out[2] = (byte)(in >> 16);
		out[3] = (byte)(in >> 24);

		return out;
	}

	public static byte[] toByteArray(int in,int outSize) {
		byte[] out = new byte[outSize];
		byte[] intArray = toByteArray(in);
		for( int i=0; i<intArray.length && i<outSize; i++ ) {
			out[i] = intArray[i];
		}
		return out;
	}

	public static String toString( byte[] theByteArray ){
		StringBuffer out = new StringBuffer();
		for( int i=0; i<theByteArray.length; i++ ) {
			String s = Integer.toHexString(theByteArray[i]&0xff);
			if( s.length()<2 ) {
				out.append( '0' );
			}
			out.append( s ).append(' ');
		}
		return out.toString();
	}

	public static boolean isEqual( byte[] first, byte[] second ) {
		boolean out = first!=null && second!=null && first.length==second.length;
		for( int i=0; out && i<first.length; i++ ) {
			if( first[i]!=second[i] ) {
				out = false;
			}
		}
		return out;
	}

}
