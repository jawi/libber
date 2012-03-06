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
 * Provides a {@link InputStream} for reading BER-encoded values in a
 * stream-like fashion.
 */
public class BerInputStream extends FilterInputStream
{
  // INNER TYPES

  /**
   * Denotes an {@link BerValue}.
   */
  private static class BerValue
  {
    // VARIABLES

    private final AsnIdentifier id;
    private final int length;
    private final byte[] content;
    private final boolean hasByteContent;
    private final List<BerValue> subValues;

    // CONSTRUCTORS

    /**
     * Creates a new {@link BerValue} for the given identifier, length and byte
     * content.
     * 
     * @param aId
     *          the identifier;
     * @param aLength
     *          the content length;
     * @param aContent
     *          the byte content.
     */
    public BerValue( final AsnIdentifier aId, final int aLength, final byte... aContent )
    {
      this.id = aId;
      this.length = aLength;
      this.hasByteContent = true;
      this.content = aContent;
      this.subValues = null;
    }

    /**
     * Creates a new {@link BerValue} for the given identifier, length and byte
     * content.
     * 
     * @param aId
     *          the identifier;
     * @param aLength
     *          the content length;
     * @param aSubValues
     *          the list with {@link BerValue}s as sub-values.
     */
    public BerValue( final AsnIdentifier aId, final int aLength, final List<BerValue> aSubValues )
    {
      this.id = aId;
      this.length = aLength;
      this.hasByteContent = false;
      this.content = null;
      this.subValues = aSubValues;
    }
  }

  /**
   * Provides an input stream that only reads a maximum number of bytes.
   */
  private static class BoundInputStream extends InputStream
  {
    // VARIABLES

    private final InputStream input;
    private final int streamLength;
    private volatile int read;

    // CONSTRUCTORS

    /**
     * Creates a new {@link BoundInputStream} instance.
     * 
     * @param aInput
     *          the input stream to read the original content from;
     * @param aStreamLength
     *          the length of the stream to return.
     */
    public BoundInputStream( final InputStream aInput, final int aStreamLength )
    {
      this.input = aInput;
      this.streamLength = aStreamLength;
    }

    // METHODS

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int available() throws IOException
    {
      return ( this.streamLength - this.read );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException
    {
      if ( this.read++ >= this.streamLength )
      {
        return -1;
      }
      return this.input.read();
    }
  }

  // CONSTRUCTORS

  /**
   * Creates a new {@link BerInputStream}
   * 
   * @param aInputStream
   *          the input stream to read the BER-encoded bytes from, cannot be
   *          <code>null</code>.
   */
  public BerInputStream( final InputStream aInputStream )
  {
    super( aInputStream );
  }

  // METHODS

  /**
   * Reads a ASN.1 bitstring from the input stream and returns its value as byte
   * array.
   * 
   * @return a byte array representing the bit stream, can be <code>null</code>
   *         if an end-of-stream is reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public BigInteger readBitString() throws IOException
  {
    final BerValue v = readBerValue( BIT_STRING );
    if ( v == null )
    {
      return null;
    }

    if ( v.hasByteContent )
    {
      return convertToBigInteger( v.content );
    }
    else
    {
      byte[] buffer = new byte[v.length];
      int offset = 1;

      final int count = v.subValues.size();
      for ( int i = 0; i < count; i++ )
      {
        BerValue tmpV = v.subValues.get( i );
        byte ignoredBits = tmpV.content[0];

        if ( i == ( count - 1 ) )
        {
          // last element; take the ignoredBits...
          buffer[0] = ignoredBits;
        }
        else
        {
          // non-last element; all ignoredBits should be zero!
          if ( ignoredBits != 0 )
          {
            throw new IOException( "Invalid bit-string!" );
          }
        }

        int length = tmpV.length - 1;
        System.arraycopy( tmpV.content, 1, buffer, offset, length );
        offset += length;
      }

      return convertToBigInteger( Arrays.copyOf( buffer, offset ) );
    }
  }

  /**
   * Reads a ASN.1 boolean from the input stream and returns its value.
   * 
   * @return a Boolean value, can only be <code>null</code> if end-of-stream is
   *         reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public Boolean readBoolean() throws IOException
  {
    final BerValue v = readBerValue( BOOLEAN );
    if ( v == null )
    {
      return null;
    }
    if ( v.id.isConstructed() )
    {
      throw new IOException( "Invalid BOOLEAN encoding; should be primitive!" );
    }
    return convertToBoolean( v.content );
  }

  /**
   * Reads a ASN.1 enumerated value from the input stream and returns its value.
   * 
   * @return an integer representing the enumerated value, can only be
   *         <code>null</code> if end-of-stream is reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public Integer readEnumeratedValue() throws IOException
  {
    final BerValue v = readBerValue( ENUMERATED );
    if ( v == null )
    {
      return null;
    }
    if ( v.id.isConstructed() )
    {
      throw new IOException( "Invalid ENUMERATED encoding; should be primitive!" );
    }
    return convertToInteger( v.content );
  }

  /**
   * Reads a ASN.1 IA5 (ASCII) encoded string from the input stream and returns
   * its value.
   * 
   * @return a IA5/ASCII encoded string value, can only be <code>null</code> if
   *         end-of-stream is reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public String readIA5String() throws IOException
  {
    final BerValue v = readBerValue( IA5_STRING );
    if ( v == null )
    {
      return null;
    }
    return convertToString( v, "ASCII" );
  }

  /**
   * Reads a ASN.1 integer from the input stream and returns its value.
   * 
   * @return a integer value, can only be <code>null</code> if end-of-stream is
   *         reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public Integer readInt() throws IOException
  {
    final BerValue v = readBerValue( INTEGER );
    if ( v == null )
    {
      return null;
    }
    if ( v.id.isConstructed() )
    {
      throw new IOException( "Invalid INTEGER encoding; should be primitive!" );
    }
    return convertToInteger( v.content );
  }

  /**
   * Reads a ANS.1 null-value from the input stream.
   * 
   * @return always <code>null</code>.
   * @throws IOException
   *           in case of I/O errors.
   */
  public Object readNull() throws IOException
  {
    final BerValue v = readBerValue( NULL );
    if ( ( v.content == null ) || ( v.content.length != 0 ) )
    {
      throw new IOException( "Failed to read null: invalid content!" );
    }
    if ( v.id.isConstructed() )
    {
      throw new IOException( "Invalid NULL encoding; should be primitive!" );
    }
    return null;
  }

  /**
   * Reads a ANS.1 object identifier-value from the input stream.
   * 
   * @return an array of values denoting the read object identifier, can only be
   *         <code>null</code> if the end-of-stream is reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public int[] readObjectIdentifier() throws IOException
  {
    final BerValue v = readBerValue( OBJECT_ID );
    if ( v == null )
    {
      return null;
    }
    if ( v.id.isConstructed() )
    {
      throw new IOException( "Invalid OBJECT_ID encoding; should be primitive!" );
    }

    final List<Integer> subIDs = convertToIDs( v );

    // The first sub-identifier is calculated by means of: Z = (X*40)+Y...
    int z = subIDs.get( 0 );
    int x = z / 40;
    int y = z % 40;
    // X = {0, 1, 2}; see X.690-0207, 8.19.4...
    if ( x > 2 )
    {
      y += ( x - 2 ) * 40;
      x = 2;
    }
    // Overwrite the original value...
    subIDs.set( 0, x );
    // Insert the new sub identifier...
    subIDs.add( 1, y );

    int[] result = new int[subIDs.size()];
    for ( int i = 0; i < result.length; i++ )
    {
      result[i] = subIDs.get( i ).intValue();
    }

    return result;
  }

  /**
   * Reads a ASN.1 octet string from the input stream and returns its value.
   * 
   * @return a byte-array value, can only be <code>null</code> if end-of-stream
   *         is reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public byte[] readOctetString() throws IOException
  {
    final BerValue v = readBerValue( OCTET_STRING );
    if ( v == null )
    {
      return null;
    }

    if ( v.hasByteContent )
    {
      return v.content;
    }
    else
    {
      byte[] buffer = new byte[v.length];
      int offset = 0;

      final int count = v.subValues.size();
      for ( int i = 0; i < count; i++ )
      {
        BerValue tmpV = v.subValues.get( i );
        int length = tmpV.length;
        System.arraycopy( tmpV.content, 0, buffer, offset, length );
        offset += length;
      }

      return Arrays.copyOf( buffer, offset );
    }
  }

  /**
   * Reads a ASN.1 printable string from the input stream and returns its value.
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
   * @return a printable string value, can only be <code>null</code> if
   *         end-of-stream is reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public String readPrintableString() throws IOException
  {
    final BerValue v = readBerValue( PRINTABLE_STRING );
    if ( v == null )
    {
      return null;
    }
    return convertToString( v, "ASCII" );
  }

  /**
   * Reads a ANS.1 relative object identifier-value from the input stream.
   * 
   * @return an array of values denoting the read relative object identifier,
   *         can only be <code>null</code> if the end-of-stream is reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public int[] readRelativeObjectIdentifier() throws IOException
  {
    final BerValue v = readBerValue( RELATIVE_OID );
    if ( v == null )
    {
      return null;
    }
    if ( v.id.isConstructed() )
    {
      throw new IOException( "Invalid RELATIVE_OID encoding; should be primitive!" );
    }

    final List<Integer> subIDs = convertToIDs( v );

    int[] result = new int[subIDs.size()];
    for ( int i = 0; i < result.length; i++ )
    {
      result[i] = subIDs.get( i ).intValue();
    }

    return result;
  }

  /**
   * Reads a ASN.1 sequence from the input stream and returns an input stream
   * that allows one to read specific items from the sequence.
   * <p>
   * NOTE: the pointer in the underlying input stream will point to the first
   * sequence-item after this method has been called. This means that you
   * <b>must</b> first read the entire sequence, in order to read the item
   * succeeding the sequence itself.
   * </p>
   * 
   * @return a {@link BerInputStream} instance for the sequence' values, can
   *         only be <code>null</code> if end-of-stream is reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public BerInputStream readSequenceAsStream() throws IOException
  {
    final AsnIdentifier id = readIdentifier();
    if ( id == null )
    {
      return null;
    }

    validateType( id, SEQUENCE );
    if ( !id.isConstructed() )
    {
      throw new IOException( "Non-constructed sequence found?!" );
    }
    final int length = readLength();

    return new BerInputStream( new BoundInputStream( this, length ) );
  }

  /**
   * Reads a ASN.1 set from the input stream and returns an input stream that
   * allows one to read specific items from the set.
   * <p>
   * NOTE: the pointer in the underlying input stream will point to the first
   * set-item after this method has been called. This means that you <b>must</b>
   * first read the entire set, in order to read the item succeeding the set
   * itself.
   * </p>
   * 
   * @return a {@link BerInputStream} instance for the set' values, can only be
   *         <code>null</code> if end-of-stream is reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public BerInputStream readSetAsStream() throws IOException
  {
    final AsnIdentifier id = readIdentifier();
    if ( id == null )
    {
      return null;
    }

    validateType( id, SET );
    if ( !id.isConstructed() )
    {
      throw new IOException( "Non-constructed set found?!" );
    }
    final int length = readLength();

    return new BerInputStream( new BoundInputStream( this, length ) );
  }

  /**
   * Reads a ASN.1 ISO8859-1 encoded string from the input stream and returns
   * its value.
   * 
   * @return a ISO8859-1 encoded string value, can only be <code>null</code> if
   *         end-of-stream is reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public String readString() throws IOException
  {
    final BerValue v = readBerValue( OCTET_STRING );
    if ( v == null )
    {
      return null;
    }
    return convertToString( v, "8859_1" );
  }

  /**
   * Reads a ASN.1 string representation of a UTC timestamp from the input
   * stream and returns its value.
   * <p>
   * The following time representations are supported:
   * </p>
   * <ul>
   * <li>YYMMDDhhmmZ</li>
   * <li>YYMMDDhhmm+hh'mm'</li>
   * <li>YYMMDDhhmm-hh'mm'</li>
   * <li>YYMMDDhhmmssZ</li>
   * <li>YYMMDDhhmmss+hh'mm'</li>
   * <li>YYMMDDhhmmss-hh'mm'</li>
   * </ul>
   * 
   * @return a UTC date value, can only be <code>null</code> if end-of-stream is
   *         reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public Date readUtcTime() throws IOException
  {
    final BerValue v = readBerValue( UTC_TIME );
    if ( v == null )
    {
      return null;
    }
    return convertToUTCDate( v );
  }

  /**
   * Reads a ASN.1 UTF-8 encoded string from the input stream and returns its
   * value.
   * 
   * @return a UTF-8 encoded string value, can only be <code>null</code> if
   *         end-of-stream is reached.
   * @throws IOException
   *           in case of I/O errors.
   */
  public String readUTF8String() throws IOException
  {
    final BerValue v = readBerValue( UTF8_STRING );
    if ( v == null )
    {
      return null;
    }
    return convertToString( v, "UTF8" );
  }

  /**
   * Converts a given byte array to a {@link BigInteger} value.
   * 
   * @param aContent
   *          the byte array to convert, cannot be <code>null</code>.
   * @return the {@link BigInteger} value.
   * @throws IOException
   *           in case the given byte array was <code>null</code> or not of the
   *           correct length.
   */
  private BigInteger convertToBigInteger( final byte[] aBytes ) throws IOException
  {
    // byte 0 == ignoredBits for this part...
    int ignoredBits = ( aBytes[0] & 0xFF );
    if ( ignoredBits >= 8 )
    {
      throw new IOException( "Invalid bit-string: ignored bits should be less than 8!" );
    }

    byte[] rawData = new byte[aBytes.length - 1];
    System.arraycopy( aBytes, 1, rawData, 0, rawData.length );

    BigInteger bigInteger = new BigInteger( rawData );
    if ( ignoredBits > 0 )
    {
      return bigInteger.shiftRight( ignoredBits );
    }

    return bigInteger;
  }

  /**
   * Converts a given byte array to a boolean value.
   * 
   * @param aContent
   *          the byte array to convert, cannot be <code>null</code>.
   * @return the boolean value.
   * @throws IOException
   *           in case the given byte array was <code>null</code> or not of
   *           length 1.
   */
  private boolean convertToBoolean( final byte[] aContent ) throws IOException
  {
    if ( ( aContent == null ) || ( aContent.length != 1 ) )
    {
      throw new IOException( "Failed to instantiate boolean: no/invalid content!" );
    }
    return aContent[0] == ( byte )0xFF;
  }

  /**
   * Converts a given byte array to a integer value.
   * 
   * @param aContent
   *          the byte array to convert, cannot be <code>null</code>.
   * @return the integer value.
   * @throws IOException
   *           in case the given byte array was <code>null</code> or not of an
   *           expected length (1..4).
   */
  private List<Integer> convertToIDs( final BerValue aBerValue )
  {
    List<Integer> subIDs = new ArrayList<Integer>();

    int value = 0;
    for ( byte b : aBerValue.content )
    {
      // only the first 7 bits are relevant..
      value <<= 7;
      value |= ( b & 0x7F );

      if ( ( b & 0x80 ) != 0x80 )
      {
        // last octet found...
        subIDs.add( value );
        value = 0;
      }
    }

    return subIDs;
  }

  /**
   * Converts a given byte array to a integer value.
   * 
   * @param aContent
   *          the byte array to convert, cannot be <code>null</code>.
   * @return the integer value.
   * @throws IOException
   *           in case the given byte array was <code>null</code> or not of an
   *           expected length (1..4).
   */
  private int convertToInteger( final byte[] aContent ) throws IOException
  {
    if ( ( aContent == null ) || ( ( aContent.length <= 0 ) || ( aContent.length > 4 ) ) )
    {
      throw new IOException( "Failed to instantiate integer: no/invalid content!" );
    }

    int result = aContent[0];
    for ( int i = 1; i < aContent.length; i++ )
    {
      result <<= 8;
      result |= ( aContent[i] & 0xFF );
    }

    return result;
  }

  /**
   * Converts the given {@link BerValue} to a string representation.
   * 
   * @param aBerValue
   *          the {@link BerValue} to convert to a string;
   * @param aEncoding
   *          the encoding to use for the byte-values in the given
   *          {@link BerValue}.
   * @return the string value in the requested encoding, never <code>null</code>
   *         .
   * @throws IOException
   *           in case of I/O problems.
   */
  private String convertToString( final BerValue aBerValue, final String aEncoding ) throws IOException
  {
    if ( aBerValue.hasByteContent )
    {
      return new String( aBerValue.content, aEncoding );
    }
    else
    {
      StringBuilder sb = new StringBuilder();
      for ( BerValue tmpV : aBerValue.subValues )
      {
        sb.append( new String( tmpV.content, aEncoding ) );
      }

      return sb.toString();
    }
  }

  /**
   * Converts the given {@link BerValue} to a UTC-date representation.
   * 
   * @param aBerValue
   *          the {@link BerValue} to convert to a date;
   * @param aEncoding
   *          the encoding to use for the byte-values in the given
   *          {@link BerValue}.
   * @return the {@link Date} value, never <code>null</code>.
   * @throws IOException
   *           in case of I/O problems.
   */
  private Date convertToUTCDate( final BerValue aBerValue ) throws IOException
  {
    final String timeStr = convertToString( aBerValue, "ASCII" );
    if ( ( timeStr.length() <= 10 ) || ( timeStr.length() > 17 ) )
    {
      throw new IOException( "Invalid UTC timestamp: " + timeStr );
    }

    // Always consider the short date/time representation...
    StringBuilder formatStr = new StringBuilder( "yyMMddHHmm" );

    // Check whether the time contains the (optional) seconds...
    if ( Character.isDigit( timeStr.charAt( 10 ) ) )
    {
      formatStr.append( "ss" );
    }
    // Check whether the 'Z' marker is used to represent GMT+0...
    if ( timeStr.endsWith( "Z" ) )
    {
      formatStr.append( "'Z'" );
    }
    else
    {
      formatStr.append( "Z" );
    }

    try
    {
      final SimpleDateFormat parser = new SimpleDateFormat( formatStr.toString() );
      // We should expect everything in UTC!
      parser.setTimeZone( TimeZone.getTimeZone( "UTC" ) );

      return parser.parse( timeStr );
    }
    catch ( ParseException exception )
    {
      throw new IOException( "Invalid UTC timestamp: " + timeStr );
    }
  }

  /**
   * Reads the next bytes and interprets it as an {@link BerValue}.
   * 
   * @return the read {@link BerValue}, never <code>null</code>.
   * @throws IOException
   *           in case of I/O errors.
   */
  private BerValue readBerValue() throws IOException
  {
    final AsnIdentifier id = readIdentifier();
    if ( id == null )
    {
      // End-of-stream reached?!
      return null;
    }

    final int length = readLength();

    if ( id.isConstructed() )
    {
      // Constructed form; either definite-length or indefinite-length...
      if ( length != 0x80 )
      {
        // Definite-length...
        return new BerValue( id, length, readContentAsBerValues( length ) );
      }

      // Indefinite length value...
      return new BerValue( id, -1 );
    }

    // Primitive form...
    return new BerValue( id, length, readContentData( length ) );
  }

  /**
   * Reads the next bytes and interprets it as an {@link BerValue}.
   * 
   * @param aExpectedType
   *          the expected type, cannot be <code>null</code>.
   * @return the read {@link BerValue}, never <code>null</code>.
   * @throws IOException
   *           in case of I/O errors.
   */
  private BerValue readBerValue( final AsnType aExpectedType ) throws IOException
  {
    final BerValue result = readBerValue();
    if ( result != null )
    {
      validateType( result.id, aExpectedType );
    }
    return result;
  }

  /**
   * Reads the next N bytes from the input stream and before returning it,
   * breaks it apart on all found {@link BerValue}s in those bytes.
   * 
   * @param aLength
   *          the number of bytes to read, >= 0.
   * @throws IOException
   *           in case of I/O problems, such as end of stream.
   */
  private List<BerValue> readContentAsBerValues( final int aLength ) throws IOException
  {
    byte[] scratch = new byte[aLength];
    final int readCount = read( scratch );
    if ( readCount != aLength )
    {
      throw new IOException( "Insufficient data! Expected " + aLength + " bytes, got only " + readCount + " bytes!" );
    }

    List<BerValue> result = new ArrayList<BerValue>();
    for ( int i = 0; i < scratch.length; )
    {
      AsnIdentifier id = new AsnIdentifier( scratch[i++] );

      int length = scratch[i++];
      if ( length > 127 )
      {
        // long form length...
        final int count = ( length & 0x7F );
        final byte[] buf = new byte[count];
        System.arraycopy( scratch, i, buf, 0, count );
        length = convertToInteger( buf );
      }

      byte[] content = new byte[length];
      System.arraycopy( scratch, i, content, 0, content.length );
      i += length;

      result.add( new BerValue( id, length, content ) );
    }

    return result;
  }

  /**
   * Reads the next N bytes from the input stream and returns it as byte array.
   * 
   * @param aLength
   *          the number of bytes to read, >= 0.
   * @throws IOException
   *           in case of I/O problems, such as end of stream.
   */
  private byte[] readContentData( final int aLength ) throws IOException
  {
    byte[] result = new byte[aLength];
    if ( aLength > 0 )
    {
      final int readCount = read( result );
      if ( readCount != aLength )
      {
        throw new IOException( "Insufficient data! Expected " + aLength + " bytes, got only " + readCount + " bytes!" );
      }
    }
    return result;
  }

  /**
   * Reads the next byte from the input stream and returns it
   * {@link AsnIdentifier}.
   * 
   * @return the ASN.1 identifier, never <code>null</code>.
   * @throws IOException
   *           in case of I/O problems, such as end of stream.
   */
  private AsnIdentifier readIdentifier() throws IOException
  {
    final int idOctet = read();
    if ( idOctet < 0 )
    {
      return null;
    }
    return new AsnIdentifier( idOctet );
  }

  /**
   * Reads the next (and possible more) byte(s) from the input stream and
   * returns it as content length.
   * 
   * @return the length of the succeeding content, >= 0.
   * @throws IOException
   *           in case of I/O errors.
   */
  private int readLength() throws IOException
  {
    int length = read();
    if ( length < 0 )
    {
      throw new EOFException();
    }
    if ( length > 127 )
    {
      // long form length...
      final int count = ( length & 0x7F );
      final byte[] buf = new byte[count];
      read( buf );
      return convertToInteger( buf );
    }
    // Default: short form length...
    return length;
  }

  /**
   * @param aIdentifier
   * @param aExpectedType
   * @throws IOException
   */
  private void validateType( final AsnIdentifier aIdentifier, final AsnType aExpectedType ) throws IOException
  {
    if ( aIdentifier.getType() != aExpectedType )
    {
      throw new IOException( "Unexpected type: " + aIdentifier.getType() + ", expected: " + aExpectedType );
    }
  }
}
