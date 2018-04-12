package com.yoshione.fingen.utils.winzipaes.impl;

/**
 * Copy&Paste of constants needed from java.util.ZipConstants
 * as this "baseinterface" prevents reuse by its package only
 * visibility.
 *
 * @author olaf@merkert.de
 */
public interface ZipConstants {

  /*
   * Header signatures
   */
  static long LOCSIG = 0x04034b50L; // "PK\003\004"
  static long EXTSIG = 0x08074b50L; // "PK\007\008"
  static long CENSIG = 0x02014b50L; // "PK\001\002"
  static long ENDSIG = 0x06054b50L; // "PK\005\006"

}
