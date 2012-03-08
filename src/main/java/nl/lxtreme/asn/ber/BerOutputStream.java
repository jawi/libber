/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn.ber;


import static nl.lxtreme.asn.AsnType.*;

import java.io.*;
import java.math.*;
import java.text.*;
import java.util.*;

import nl.lxtreme.asn.*;


/**
 * Provides a {@link OutputStream} for writing BER-encoded values in a
 * stream-like fashion.
 */
public class BerOutputStream extends FilterOutputStream
{
  // INNER TYPES

  /**
   * Provides a {@link ByteArrayOutputStream} that allows us to directly access
   * its internal buffer, without creating a copy of it.
   */
  private static class DirectByteArrayOutputStream extends ByteArrayOutputStream
  {
    // CONSTRUCTORS

    /**
     * Creates a new {@link DirectByteArrayOutputStream} instance.
     * 
     * @param aBufferSize
     *          the initial buffer size to use.
     */
    public DirectByteArrayOutputStream( final int aBufferSize )
    {
      super( aBufferSize );
    }

    // METHODS

    /**
     * Provides direct access to the internal buffer of this stream.
     * 
     * @return the internal buffer, use with care!
     */
    private byte[] getDirectBuffer()
    {
      return this.buf;
    }
  }

  // CONSTANTS

  /** Denotes the constructed value bit; used for sequences and sets. */
  private static final int CONSTRUCTED = 0x20;
  /** Denotes the initial buffer size for the sequence/set output stream. */
  private static final int BUFFER_SIZE = 256;

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
    final byte[] values = new byte[bytes.length + 1];
    values[0] = ( byte )stuffBits;
    System.arraycopy( bytes, 0, values, 1, bytes.length );

    writeTLV( BIT_STRING, values );
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
    writeTLV( BOOLEAN, ( byte )( aValue ? 0xFF : 0x00 ) );
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
    final byte[] content = encodeInteger( aValue );
    writeTLV( ENUMERATED, content );
  }

  /**
   * Writes a primitive generalized timestamp as UTC string value.
   * 
   * @param aTimestamp
   *          the generalized timestamp to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeGeneralizedTime( final Calendar aTimestamp ) throws IOException
  {
    byte[] content = encodeISO8601Time( aTimestamp );
    writeTLV( GENERALIZED_TIME, content );
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
    final byte[] content = encodeString( aString, "ASCII" );
    writeTLV( IA5_STRING, content );
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
    final byte[] content = encodeInteger( aValue );
    writeTLV( INTEGER, content );
  }

  /**
   * Writes a primitive null value.
   * 
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeNull() throws IOException
  {
    writeTLV( NULL );
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
    // Create an encoded version of the sub IDs, in which element 0 + 1 are
    // packed into a single value...
    int[] subIDs = new int[aSubIDs.length - 1];
    subIDs[0] = ( aSubIDs[0] * 40 ) + aSubIDs[1];
    System.arraycopy( aSubIDs, 2, subIDs, 1, aSubIDs.length - 2 );

    final byte[] content = encodeObjectIdentifier( subIDs );
    writeTLV( OBJECT_ID, content );
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
    writeTLV( OCTET_STRING, aString );
  }

  /**
   * Writes a printable string.
   * <p>
   * A printable string contains of the following characters:
   * </p>
   * 
   * <pre>
   * A, B, ..., Z
   * a, b, ..., z
   * 0, 1, ..., 9
   * (space) ' ( ) + , - . / : = ?
   * </pre>
   * 
   * @param aValue
   *          the printable string value to write, cannot be <code>null</code>.
   * @throws IOException
   *           in case of I/O errors.
   */
  public void writePrintableString( final String aValue ) throws IOException
  {
    final byte[] content = encodeString( aValue.replaceAll( "[^a-zA-Z0-9 '()+,./:=?-]", "" ), "ASCII" );
    writeTLV( PRINTABLE_STRING, content );
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
    final byte[] content = encodeObjectIdentifier( aSubIDs );
    writeTLV( RELATIVE_OID, content );
  }

  /**
   * Allows one to write a ASN.1 sequence in a stream-like fashion.
   * <p>
   * NOTE: the pointer in the underlying output stream will not change directly
   * by calls to the various write methods on the returned output stream. Hence,
   * you first need to write your entire sequence before continuing on the main
   * output stream!
   * </p>
   * 
   * @return a {@link BerOutputStream} instance, never <code>null</code>. When
   *         {@link BerOutputStream#close()} is called, the entire sequence is
   *         written to the main output stream.
   * @throws IOException
   *           in case of I/O problems.
   */
  public BerOutputStream writeSequenceAsStream() throws IOException
  {
    final DirectByteArrayOutputStream baos = new DirectByteArrayOutputStream( BUFFER_SIZE );
    final BerOutputStream bos = new BerOutputStream( baos )
    {
      @Override
      public void close() throws IOException
      {
        super.close();

        final int length = baos.size();

        BerOutputStream.this.write( SEQUENCE.ordinal() | CONSTRUCTED );
        BerOutputStream.this.writeLength( length );
        BerOutputStream.this.write( baos.getDirectBuffer(), 0, length );
      }
    };
    return bos;
  }

  /**
   * Allows one to write a ASN.1 set in a stream-like fashion.
   * <p>
   * NOTE: the pointer in the underlying output stream will not change directly
   * by calls to the various write methods on the returned output stream. Hence,
   * you first need to write your entire set before continuing on the main
   * output stream!
   * </p>
   * 
   * @return a {@link BerOutputStream} instance, never <code>null</code>. When
   *         {@link BerOutputStream#close()} is called, the entire set is
   *         written to the main output stream.
   * @throws IOException
   *           in case of I/O problems.
   */
  public BerOutputStream writeSetAsStream() throws IOException
  {
    final DirectByteArrayOutputStream baos = new DirectByteArrayOutputStream( BUFFER_SIZE );
    final BerOutputStream bos = new BerOutputStream( baos )
    {
      @Override
      public void close() throws IOException
      {
        super.close();

        final int length = baos.size();

        BerOutputStream.this.write( SET.ordinal() | CONSTRUCTED );
        BerOutputStream.this.writeLength( length );
        BerOutputStream.this.write( baos.getDirectBuffer(), 0, length );
      }
    };
    return bos;
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
    final byte[] content = aString.getBytes( "8859_1" );
    writeTLV( OCTET_STRING, content );
  }

  /**
   * Writes a primitive timestamp as UTC string value.
   * 
   * @param aTimestamp
   *          the timestamp to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeUtcTime( final Calendar aTimestamp ) throws IOException
  {
    byte[] content = encodeUTCTime( aTimestamp );
    writeTLV( UTC_TIME, content );
  }

  /**
   * Writes a primitive timestamp as UTC string value.
   * 
   * @param aTimestamp
   *          the timestamp to write, will be always in the current default
   *          timezone!
   * @throws IOException
   *           in case of I/O problems.
   * @see #writeUtcTime(Calendar)
   */
  public void writeUtcTime( final Date aTimestamp ) throws IOException
  {
    Calendar cal = Calendar.getInstance();
    cal.setTime( aTimestamp );
    writeUtcTime( cal );
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
    final byte[] content = encodeString( aString, "UTF8" );
    writeTLV( UTF8_STRING, content );
  }

  /**
   * Encodes the given integer value into a series of bytes.
   * 
   * @param aValue
   *          the integer value to encode.
   * @return a int-array denoting the individual bytes of the encoded integer,
   *         never <code>null</code>.
   */
  private byte[] encodeInteger( final int aValue )
  {
    int intsize = getIntegerLength( aValue );

    final int mask = 0xff800000;
    int value = ( aValue << ( ( 4 - intsize ) * 8 ) );

    byte[] result = new byte[intsize];
    int i = 0;
    while ( intsize-- > 0 )
    {
      result[i++] = ( byte )( ( ( value & mask ) >> 24 ) & 0xFF );
      value <<= 8;
    }

    return result;
  }

  /**
   * Encodes a given timestamp as a series of bytes representing a ISO8601
   * timestamp.
   * 
   * @param aTimestamp
   *          the timestamp to encode, cannot be <code>null</code>.
   * @return the encoded timestamp, never <code>null</code>.
   * @throws IOException
   *           in case of I/O problems.
   */
  private byte[] encodeISO8601Time( final Calendar aTimestamp ) throws IOException
  {
    final TimeZone utcTZ = TimeZone.getTimeZone( "UTC" );

    String format = "yyyyMMddHHmmss";
    if ( aTimestamp.get( Calendar.MILLISECOND ) > 0 )
    {
      format = format.concat( ".S" );
    }
    if ( utcTZ.equals( aTimestamp.getTimeZone() ) )
    {
      format = format.concat( "'Z'" );
    }
    else
    {
      format = format.concat( "Z" );
    }

    final SimpleDateFormat formatter = new SimpleDateFormat( format );
    formatter.setTimeZone( aTimestamp.getTimeZone() );

    String result = formatter.format( aTimestamp.getTime() );
    return encodeString( result, "ASCII" );
  }

  /**
   * Encodes the given array of sub identifiers into a series of bytes.
   * 
   * @param aSubIDs
   *          the sub identifiers to encode, cannot be <code>null</code>.
   * @return the encoded object identifiers, as int-array with byte values.
   */
  private byte[] encodeObjectIdentifier( final int[] aSubIDs )
  {
    byte[] content = new byte[aSubIDs.length * 4];
    int offset = 0;

    for ( final int value : aSubIDs )
    {
      final int size = getIntegerLength( value );
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
        content[offset + --j] = ( byte )v;
      }
      offset += size;
    }

    return Arrays.copyOf( content, offset );
  }

  /**
   * Encodes a given string into bytes, using the given encoding.
   * 
   * @param aValue
   *          the string value to encode, may be <code>null</code>;
   * @param aEncoding
   *          the encoding to use for the resulting bytes.
   * @return a byte array, never <code>null</code>.
   * @throws IOException
   *           in case of an unsupported encoding.
   */
  private byte[] encodeString( final String aValue, final String aEncoding ) throws IOException
  {
    if ( aValue == null )
    {
      return new byte[0];
    }
    return aValue.getBytes( aEncoding );
  }

  /**
   * Encodes a given timestamp as a series of bytes represting a UTC timestamp.
   * 
   * @param aTimestamp
   *          the timestamp to encode, cannot be <code>null</code>.
   * @return the encoded timestamp, never <code>null</code>.
   * @throws IOException
   *           in case of I/O problems.
   */
  private byte[] encodeUTCTime( final Calendar aTimestamp ) throws IOException
  {
    String format = "yyMMddHHmmss'Z'";

    SimpleDateFormat formatter = new SimpleDateFormat( format );
    formatter.setTimeZone( TimeZone.getTimeZone( "UTC" ) );

    String result = formatter.format( aTimestamp.getTime() );
    return encodeString( result, "ASCII" );
  }

  /**
   * Uses a fast way to determine the length of a given integer when it should
   * be encoded in bytes.
   * 
   * @param aValue
   *          the value to return its length for.
   * @return the length of the given value; 1..10.
   */
  private int getIntegerLength( final int aValue )
  {
    final int mask = 0xff800000;
    int intsize = 4;
    int value = ( ( Integer )aValue ).intValue();

    while ( ( ( ( value & mask ) == 0 ) || ( ( value & mask ) == mask ) ) && ( intsize > 1 ) )
    {
      intsize--;
      value <<= 8;
    }
    return intsize;
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
  private void writeTLV( final AsnType aType, final byte... aContent ) throws IOException
  {
    write( ( byte )aType.ordinal() );
    writeLength( aContent.length );
    for ( byte c : aContent )
    {
      write( c );
    }
  }
}
