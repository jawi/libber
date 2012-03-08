/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn;


/**
 * Denotes the various native types of ASN.1 (UNIVERSAL class).
 */
public enum AsnType
{
  // CONSTANTS

  /** End-of-content. */
  EOC,
  /** A boolean type (supported). */
  BOOLEAN,
  /** A integer type (supported). */
  INTEGER,
  /** A bitstring (series of zeros and ones, supported). */
  BIT_STRING,
  /** A simple/octet string (8-bit values, supported). */
  OCTET_STRING,
  /** A null-value (supported). */
  NULL,
  /** A sequence of integer components that identify an object (supported). */
  OBJECT_ID,
  /** */
  OBJECT_DESCRIPTOR,
  /** */
  EXTERNAL,
  /** A real/floating point number. */
  REAL,
  /** Denotes an enumerated value (supported). */
  ENUMERATED,
  /** */
  EMBEDDED_PDV,
  /** UTF-8 encoded string (supported). */
  UTF8_STRING,
  /**
   * A sequence of integer components that relatively identify an object
   * (supported).
   */
  RELATIVE_OID,
  /** Do not use. */
  RESERVED1,
  /** Do not use. */
  RESERVED2,
  /** An ordered collection of one or more types (supported). */
  SEQUENCE,
  /** An unordered collections of one or more types (supported). */
  SET,
  /** */
  NUMERIC_STRING,
  /** Denotes a string with a limited set of characters (supported). */
  PRINTABLE_STRING,
  /** Denotes a string with TELETEXT encoding. */
  T61_STRING,
  /** Denotes a string with VIDEOTEXT encoding. */
  VIDEOTEX_STRING,
  /** ASCII encoded string (supported). */
  IA5_STRING,
  /** Timestamp, UTC time (supported). */
  UTC_TIME,
  /** Generalized time (supported). */
  GENERALIZED_TIME,
  /** */
  GRAPHIC_STRING,
  /** */
  VISIBLE_STRING,
  /** */
  GENERAL_STRING,
  /** */
  UNIVERSAL_STRING,
  /** */
  CHARACTER_STRING,
  /** */
  BMP_STRING,
  /** Not a real type; but denotes a long-form ASN.1 type. */
  LONG_FORM_TYPE;

  // METHODS

  /**
   * @param aValue
   *          the ASN.1 tag-value of the type to return.
   * @return a {@link AsnType} corresponding to the given value, or
   *         <code>null</code> if no such type was found.
   */
  public static AsnType valueOf( final int aValue )
  {
    // Take only the lower 5 bits into account...
    final int value = ( aValue & LONG_FORM_TYPE.ordinal() );
    for ( AsnType type : values() )
    {
      if ( type.ordinal() == value )
      {
        return type;
      }
    }
    return null;
  }
}
