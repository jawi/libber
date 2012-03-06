/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn;


import java.lang.reflect.*;


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
  /** */
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
   * @return
   */
  public static int determineLength( final Object aValue )
  {
    if ( aValue == null )
    {
      return 0;
    }

    if ( aValue.getClass().isArray() )
    {
      return Array.getLength( aValue );
    }

    if ( ( Boolean.class == aValue.getClass() ) || ( Boolean.TYPE == aValue.getClass() ) )
    {
      return AsnType.BOOLEAN.getLength( aValue );
    }
    if ( ( Integer.class == aValue.getClass() ) || ( Integer.TYPE == aValue.getClass() ) )
    {
      return AsnType.INTEGER.getLength( aValue );
    }

    throw new RuntimeException( "Unable to determine length for context-specific value: " + aValue.getClass() );
  }

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

  /**
   * Calculates the length of this identifier, based on the given value.
   * 
   * @param aValue
   *          the value to calculate the length for, can be <code>null</code> in
   *          which case this method returns 0.
   * @return a length, >= 0.
   */
  public int getLength( final Object aValue )
  {
    if ( aValue == null )
    {
      return 0;
    }

    switch ( this )
    {
      case BOOLEAN:
        return 1;

      case INTEGER:
        final int mask = 0xff800000;
        int intsize = 4;
        int value = ( ( Integer )aValue ).intValue();

        while ( ( ( ( value & mask ) == 0 ) || ( ( value & mask ) == mask ) ) && ( intsize > 1 ) )
        {
          intsize--;
          value <<= 8;
        }
        return intsize;

      case OCTET_STRING:
        return ( ( byte[] )aValue ).length;

      default:
        throw new UnsupportedOperationException( "Unsupported type for length: " + this );
    }
  }

}
