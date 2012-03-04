/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn.ber;


import static nl.lxtreme.asn.AsnType.*;

import java.io.*;

import nl.lxtreme.asn.*;


/**
 * Provides a {@link InputStream} for reading BER-encoded values in a
 * stream-like fashion.
 */
public class BerInputStream extends InputStream
{
  // INNER TYPES

  /**
   * Denotes an {@link AsnValue}.
   */
  private static class AsnValue
  {
    // VARIABLES

    private final AsnIdentifier id;
    private final int length;
    private final byte[] content;

    // CONSTRUCTORS

    /**
     * @param aId
     * @param aLength
     * @param aContent
     */
    public AsnValue( final AsnIdentifier aId, final int aLength, final byte[] aContent )
    {
      this.id = aId;
      this.length = aLength;
      this.content = aContent;
    }

    /**
     * @param aExpectedValues
     */
    public boolean contentEquals( int... aExpectedValues )
    {
      if ( this.length != aExpectedValues.length )
      {
        return false;
      }
      for ( int i = 0; i < this.length; i++ )
      {
        if ( this.content[i] != ( byte )aExpectedValues[i] )
        {
          return false;
        }
      }
      return true;
    }
  }

  // VARIABLES

  private final InputStream in;

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
    this.in = aInputStream;
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws IOException
  {
    this.in.close();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int read() throws IOException
  {
    return this.in.read();
  }

  /**
   * Reads a ASN.1 boolean from the input stream and returns its value.
   * 
   * @return a boolean value.
   * @throws IOException
   *           in case of I/O errors.
   */
  public boolean readBoolean() throws IOException
  {
    final AsnValue v = readAsnValue( BOOLEAN );
    return v.contentEquals( 0xFF );
  }

  /**
   * Reads a ASN.1 integer from the input stream and returns its value.
   * 
   * @return a integer value.
   * @throws IOException
   *           in case of I/O errors.
   */
  public int readInt() throws IOException
  {
    final AsnValue v = readAsnValue( INTEGER );

    int result = v.content[0];
    for ( int i = 1; i < v.length; i++ )
    {
      result <<= 8;
      result |= ( v.content[i] & 0xFF );
    }

    return result;
  }

  /**
   * Reads a ASN.1 octet string from the input stream and returns its value.
   * 
   * @return a byte-array value.
   * @throws IOException
   *           in case of I/O errors.
   */
  public byte[] readOctetString() throws IOException
  {
    final AsnValue v = readAsnValue( OCTET_STRING );
    return v.content;
  }

  /**
   * Reads a ASN.1 ISO8859-1 encoded string from the input stream and returns
   * its value.
   * 
   * @return a ISO8859-1 encoded string value.
   * @throws IOException
   *           in case of I/O errors.
   */
  public String readString() throws IOException
  {
    final AsnValue v = readAsnValue( OCTET_STRING );
    return new String( v.content, "8859_1" );
  }

  /**
   * Reads a ASN.1 UTF-8 encoded string from the input stream and returns its
   * value.
   * 
   * @return a UTF-8 encoded string value.
   * @throws IOException
   *           in case of I/O errors.
   */
  public String readUTF8String() throws IOException
  {
    final AsnValue v = readAsnValue( OCTET_STRING );
    return new String( v.content, "UTF8" );
  }

  /**
   * Reads a ASN.1 sequence from the input stream and returns its value.
   * 
   * @return a sequence value.
   * @throws IOException
   *           in case of I/O errors.
   */
  public AsnSequence readSequence() throws IOException
  {
    final AsnValue v = readAsnValue( SEQUENCE );
    return null;
  }

  /**
   * Reads the next bytes and interprets it as an {@link AsnValue}.
   * 
   * @return the read {@link AsnIdentifier}, never <code>null</code>.
   * @throws IOException
   *           in case of I/O errors.
   */
  private AsnValue readAsnValue() throws IOException
  {
    final AsnIdentifier id = new AsnIdentifier( read() );

    final int length = read();

    final byte[] content = new byte[length];
    read( content );

    return new AsnValue( id, length, content );
  }

  /**
   * Reads the next bytes and interprets it as an {@link AsnValue}.
   * 
   * @param aExpectedType
   *          the expected type, cannot be <code>null</code>.
   * @return the read {@link AsnIdentifier}, never <code>null</code>.
   * @throws IOException
   *           in case of I/O errors.
   */
  private AsnValue readAsnValue( final AsnType aExpectedType ) throws IOException
  {
    final AsnValue result = readAsnValue();
    if ( result.id.getType() != aExpectedType )
    {
      throw new IOException( "Unexpected type!" );
    }
    return result;
  }
}
