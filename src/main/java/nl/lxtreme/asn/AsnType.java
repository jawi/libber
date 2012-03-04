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

  /** End-of-content */
  EOC,
  /** A boolean type. */
  BOOLEAN,
  /** A integer type. */
  INTEGER,
  /** A bitstring (series of zeros and ones). */
  BIT_STRING,
  /** A simple/octet string (8-bit values). */
  OCTET_STRING,
  /** A null-value. */
  NULL,
  /** A sequence of integer components that identify an object. */
  OBJECT_ID,
  /** */
  OBJECT_DESCRIPTOR,
  /** */
  EXTERNAL,
  /** A real/floating point number. */
  REAL,
  /** */
  ENUMERATED,
  /** */
  EMBEDDED_PDV,
  /** UTF-8 encoded string */
  UTF8_STRING,
  /** */
  RELATIVE_OID,
  /** Do not use. */
  RESERVED1,
  /** Do not use. */
  RESERVED2,
  /** An ordered collection of one or more types. */
  SEQUENCE,
  /** An unordered collections of one or more types. */
  SET,
  /** */
  NUMERIC_STRING,
  /** */
  PRINTABLE_STRING,
  /** */
  T61_STRING,
  /** */
  VIDEOTEX_STRING,
  /** ASCII encoded string. */
  IA5_STRING,
  /** Timestamp, UTC time. */
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
  public static int determineLength( Object aValue )
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
  public static AsnType valueOf( int aValue )
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
  public int getLength( Object aValue )
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
