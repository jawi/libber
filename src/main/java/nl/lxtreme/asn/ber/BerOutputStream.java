/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn.ber;


import static nl.lxtreme.asn.AsnType.*;

import java.io.*;

import nl.lxtreme.asn.*;
import nl.lxtreme.asn.AsnSequence.*;


/**
 * Provides a {@link OutputStream} for writing BER-encoded values in a
 * stream-like fashion.
 */
public class BerOutputStream extends OutputStream
{
  // VARIABLES

  private final OutputStream out;

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
    this.out = aOutStream;
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws IOException
  {
    this.out.close();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void flush() throws IOException
  {
    this.out.flush();
  }

  /**
   * Writes a primitive boolean value.
   * 
   * @param aValue
   *          the boolean value to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeBoolean( boolean aValue ) throws IOException
  {
    writeTLV( BOOLEAN, 0x01, aValue ? 0xFF : 0x00 );
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
   * Writes a primitive integer value.
   * 
   * @param aValue
   *          the integer value to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeInt( int aValue ) throws IOException
  {
    int intsize = INTEGER.getLength( aValue );

    final int mask = 0xff800000;
    int value = ( aValue << ( ( 4 - intsize ) * 8 ) );

    writeTLV( INTEGER, intsize );
    while ( intsize-- > 0 )
    {
      write( ( byte )( ( value & mask ) >> 24 ) );
      value <<= 8;
    }
  }

  /**
   * Writes a primitive octet-string value.
   * 
   * @param aString
   *          the octet-string value to write.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeOctetString( byte[] aString ) throws IOException
  {
    writeTLV( OCTET_STRING, aString.length );
    for ( byte b : aString )
    {
      write( b );
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
  public void writeString( String aString ) throws IOException
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
  public void writeUTF8String( String aString ) throws IOException
  {
    writeOctetString( aString.getBytes( "UTF8" ) );
  }

  /**
   * Writes a ASN.1 sequence.
   * 
   * @param aSequence
   *          the sequence to write, cannot be <code>null</code>.
   * @throws IOException
   *           in case of I/O problems.
   */
  public void writeSequence( AsnSequence aSequence ) throws IOException
  {
    writeTLV( aSequence.getMainType(), aSequence.getLength(), null );
    for ( AsnSequenceType seqType : aSequence )
    {
      writeTLV( seqType.getType(), seqType.getLength(), seqType.getValue() );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void write( int aValue ) throws IOException
  {
    this.out.write( aValue );
  }

  /**
   * @param aType
   * @param aLength
   * @param aContent
   * @throws IOException
   */
  private void writeTLV( AsnType aType, int aLength, int... aContent ) throws IOException
  {
    write( ( byte )aType.ordinal() );
    writeLength( aLength );
    for ( int c : aContent )
    {
      write( ( byte )c );
    }
  }

  /**
   * @param aType
   * @param aLength
   * @param aContent
   * @throws IOException
   */
  private void writeTLV( AsnIdentifier aIdentifier, int aLength, Object aContent ) throws IOException
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
   * @param aLength
   * @throws IOException
   */
  private void writeLength( int aLength ) throws IOException
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
}
