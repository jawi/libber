/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn.ber;


import static nl.lxtreme.asn.AsnType.*;

import java.io.*;
import java.math.*;
import java.util.*;

import nl.lxtreme.asn.*;
import nl.lxtreme.asn.AsnSequence.AsnSequenceType;


/**
 * Provides a {@link OutputStream} for writing BER-encoded values in a
 * stream-like fashion.
 */
public class BerOutputStream extends FilterOutputStream
{
  // CONSTRUCTORS

  /**
   * Creates a new {@link BerOutputStream} instance.
   * 
   * @param aOutStream
   *          the output stream to write the BER-encoded bytes to, cannot be
   *          <code>null</code>.
   */
  public BerOutputStream( final OutputStream aOutStream )
  {
    super( aOutStream );
  }

  // METHODS

  /**
   * Writes a primitive bit string value.
   * 
   * @param aBitString
   *          the bit string value to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeBitString( final BigInteger aBitString ) throws IOException
  {
    int bitLength = aBitString.bitLength();
    final int stuffBits = ( int )( ( Math.ceil( bitLength / 8.0 ) * 8.0 ) - bitLength );

    final byte[] bytes = aBitString.shiftLeft( stuffBits ).toByteArray();
    final int[] values = new int[bytes.length + 1];
    values[0] = stuffBits;
    for ( int i = 0; i < bytes.length; i++ )
    {
      values[i + 1] = ( bytes[i] & 0xFF );
    }

    writeTLV( BIT_STRING, values.length, values );
  }

  /**
   * Writes a primitive boolean value.
   * 
   * @param aValue
   *          the boolean value to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeBoolean( final boolean aValue ) throws IOException
  {
    writeTLV( BOOLEAN, 0x01, aValue ? 0xFF : 0x00 );
  }

  /**
   * Writes a primitive integer value.
   * 
   * @param aValue
   *          the integer value to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeEnumeratedValue( final int aValue ) throws IOException
  {
    int intsize = INTEGER.getLength( aValue );

    final int mask = 0xff800000;
    int value = ( aValue << ( ( 4 - intsize ) * 8 ) );

    writeTLV( ENUMERATED, intsize );
    while ( intsize-- > 0 )
    {
      write( ( byte )( ( value & mask ) >> 24 ) );
      value <<= 8;
    }
  }

  /**
   * Writes an IA5 (ASCII) encoded string value.
   * 
   * @param aString
   *          the IA5 (ASCII) string value to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeIA5String( final String aString ) throws IOException
  {
    byte[] bytes = aString.getBytes( "ASCII" );
    writeTLV( IA5_STRING, bytes.length );
    for ( byte b : bytes )
    {
      write( b );
    }
  }

  /**
   * Writes a primitive integer value.
   * 
   * @param aValue
   *          the integer value to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeInt( final int aValue ) throws IOException
  {
    byte[] content = encodeInteger( aValue );
    writeTLV( INTEGER, content.length );
    for ( byte b : content )
    {
      write( b );
    }
  }

  /**
   * Writes a primitive null value.
   * 
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeNull() throws IOException
  {
    writeTLV( NULL, 0x00 );
  }

  /**
   * Writes a object identifier.
   * 
   * @param aSubIDs
   *          the integer parts of the object identifier, cannot be
   *          <code>null</code>.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeObjectIdentifier( final int[] aSubIDs ) throws IOException
  {
    int[] content = new int[aSubIDs.length * 4];
    int offset = 0;
    for ( int i = 0; i < aSubIDs.length; i++ )
    {
      final int value;
      if ( i == 0 )
      {
        // First sub identifier is packed...
        value = ( aSubIDs[i] * 40 ) + aSubIDs[++i];
      }
      else
      {
        value = aSubIDs[i];
      }

      final int size = INTEGER.getLength( value );
      int j = size;
      while ( j > 0 )
      {
        int s = ( size - j );
        int v = ( ( value >> ( 7 * s ) ) & 0xFF );
        if ( v >= 0x80 )
        {
          v &= 0x7f;
        }
        if ( j < size )
        {
          v |= 0x80;
        }
        content[offset + --j] = v;
      }

      offset += size;
    }

    writeTLV( OBJECT_ID, offset, Arrays.copyOf( content, offset ) );
  }

  /**
   * Writes a primitive octet-string value.
   * 
   * @param aString
   *          the octet-string value to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeOctetString( final byte[] aString ) throws IOException
  {
    writeTLV( OCTET_STRING, aString.length );
    for ( byte b : aString )
    {
      write( b );
    }
  }

  /**
   * Writes a object identifier.
   * 
   * @param aSubIDs
   *          the integer parts of the object identifier, cannot be
   *          <code>null</code>.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeRelativeObjectIdentifier( final int[] aSubIDs ) throws IOException
  {
    int[] content = new int[aSubIDs.length * 4];
    int offset = 0;
    for ( final int value : aSubIDs )
    {
      final int size = INTEGER.getLength( value );
      int j = size;
      while ( j > 0 )
      {
        int s = ( size - j );
        int v = ( ( value >> ( 7 * s ) ) & 0xFF );
        if ( v >= 0x80 )
        {
          v &= 0x7f;
        }
        if ( j < size )
        {
          v |= 0x80;
        }
        content[offset + --j] = v;
      }

      offset += size;
    }

    writeTLV( RELATIVE_OID, offset, Arrays.copyOf( content, offset ) );
  }

  /**
   * Writes a ASN.1 sequence.
   * 
   * @param aSequence
   *          the sequence to write, cannot be <code>null</code>.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeSequence( final AsnSequence aSequence ) throws IOException
  {
    writeTLV( aSequence.getMainType(), aSequence.getLength(), null );
    for ( AsnSequenceType seqType : aSequence )
    {
      writeTLV( seqType.getType(), seqType.getLength(), seqType.getValue() );
    }
  }

  /**
   * Writes a primitive ISO8859-1 encoded string value.
   * 
   * @param aString
   *          the string value to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeString( final String aString ) throws IOException
  {
    writeOctetString( aString.getBytes( "8859_1" ) );
  }

  /**
   * Writes a primitive UTF-8 encoded string value.
   * 
   * @param aString
   *          the string value to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeUTF8String( final String aString ) throws IOException
  {
    writeOctetString( aString.getBytes( "UTF8" ) );
  }

  private byte[] encodeInteger( final int aValue )
  {
    int intsize = INTEGER.getLength( aValue );

    final int mask = 0xff800000;
    int value = ( aValue << ( ( 4 - intsize ) * 8 ) );

    byte[] result = new byte[intsize];
    int i = 0;
    while ( intsize-- > 0 )
    {
      result[i++] = ( byte )( ( value & mask ) >> 24 );
      value <<= 8;
    }

    return result;
  }

  /**
   * @param aLength
   * @throws IOException
   */
  private void writeLength( final int aLength ) throws IOException
  {
    if ( aLength < 128 )
    {
      write( ( byte )aLength );
    }
    else if ( aLength <= 0xff )
    {
      write( ( byte )0x81 );
      write( ( byte )aLength );
    }
    else if ( aLength <= 0xffff )
    {
      write( ( byte )0x82 );
      write( ( byte )( aLength >> 8 ) );
      write( ( byte )( aLength & 0xff ) );
    }
    else if ( aLength <= 0xffffff )
    {
      write( ( byte )0x83 );
      write( ( byte )( aLength >> 16 ) );
      write( ( byte )( aLength >> 8 ) );
      write( ( byte )( aLength & 0xff ) );
    }
  }

  /**
   * @param aType
   * @param aLength
   * @param aContent
   * @throws IOException
   */
  private void writeTLV( final AsnIdentifier aIdentifier, final int aLength, final Object aContent ) throws IOException
  {
    if ( aContent != null )
    {
      switch ( aIdentifier.getType() )
      {
        case BOOLEAN:
          writeBoolean( ( ( Boolean )aContent ).booleanValue() );
          break;

        case INTEGER:
          writeInt( ( ( Integer )aContent ).intValue() );
          break;

        case OCTET_STRING:
          writeOctetString( ( byte[] )aContent );
          break;

        default:
          throw new IOException( "Unsupported tag: " + aIdentifier.getType() );
      }
    }
    else
    {
      write( ( byte )aIdentifier.getTag() );
      writeLength( aLength );
    }
  }

  /**
   * @param aType
   * @param aLength
   * @param aContent
   * @throws IOException
   */
  private void writeTLV( final AsnType aType, final int aLength, final int... aContent ) throws IOException
  {
    write( ( byte )aType.ordinal() );
    writeLength( aLength );
    for ( int c : aContent )
    {
      write( ( byte )c );
    }
  }
}
