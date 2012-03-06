/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn.ber;


import static nl.lxtreme.asn.AsnType.*;
import static org.junit.Assert.*;

import java.io.*;
import java.math.*;

import nl.lxtreme.asn.*;

import org.junit.*;


/**
 * Test cases for {@link BerOutputStream}.
 */
public class BerOutputStreamTest
{
  // VARIABLES

  private ByteArrayOutputStream buffer;
  private BerOutputStream bos;

  // METHODS

  /**
   * Set up for each test case.
   */
  @Before
  public void setUp()
  {
    this.buffer = new ByteArrayOutputStream();
    this.bos = new BerOutputStream( this.buffer );
  }

  /**
   * Test for {@link BerOutputStream#writeBitString(java.math.BigInteger)}.
   */
  @Test
  public void testWriteBitString() throws IOException
  {
    this.bos.writeBitString( new BigInteger( "101011100101110111", 2 ) );
    assertContent( BIT_STRING, 0x05, 0x06, 0x00, 0xae, 0x5d, 0xc0 );

    this.buffer.reset();

    this.bos.writeBitString( new BigInteger( "A3B5F291CD", 16 ) );
    assertContent( BIT_STRING, 0x07, 0x00, 0x00, 0xa3, 0xb5, 0xf2, 0x91, 0xcd );
  }

  /**
   * Test for {@link BerOutputStream#writeBoolean(boolean)}.
   */
  @Test
  public void testWriteBoolean() throws IOException
  {
    this.bos.writeBoolean( false );
    assertContent( BOOLEAN, 0x01, 0x00 );

    this.buffer.reset();

    this.bos.writeBoolean( true );
    assertContent( BOOLEAN, 0x01, 0xFF );
  }

  /**
   * Test for {@link BerOutputStream#writeInt(int)}.
   */
  @Test
  public void testWriteInt() throws IOException
  {
    this.bos.writeInt( 0x00 );
    assertContent( INTEGER, 0x01, 0x00 );

    this.buffer.reset();

    this.bos.writeInt( 127 );
    assertContent( INTEGER, 0x01, 0x7F );

    this.buffer.reset();

    this.bos.writeInt( 128 );
    assertContent( INTEGER, 0x02, 0x00, 0x80 );

    this.buffer.reset();

    this.bos.writeInt( 256 );
    assertContent( INTEGER, 0x02, 0x01, 0x00 );

    this.buffer.reset();

    this.bos.writeInt( -128 );
    assertContent( INTEGER, 0x01, 0x80 );

    this.buffer.reset();

    this.bos.writeInt( -129 );
    assertContent( INTEGER, 0x02, 0xFF, 0x7F );

    this.buffer.reset();

    this.bos.writeInt( Integer.MAX_VALUE );
    assertContent( INTEGER, 0x04, 0x7F, 0xFF, 0xFF, 0xFF );

    this.buffer.reset();

    this.bos.writeInt( Integer.MIN_VALUE );
    assertContent( INTEGER, 0x04, 0x80, 0x00, 0x00, 0x00 );
  }

  /**
   * Test for {@link BerOutputStream#writeNull()}.
   */
  @Test
  public void testWriteNull() throws IOException
  {
    this.bos.writeNull();
    assertContent( NULL, 0x00 );
  }

  /**
   * Test for {@link BerOutputStream#writeObjectIdentifier(int[])}.
   */
  @Test
  public void testWriteObjectIdentifier() throws IOException
  {
    this.bos.writeObjectIdentifier( new int[] { 2, 100, 3 } );
    assertContent( OBJECT_ID, 0x03, 0x81, 0x34, 0x03 );

    this.buffer.reset();

    this.bos.writeObjectIdentifier( new int[] { 1, 3, 6, 1, 2, 1, 1, 1, 0 } );
    assertContent( OBJECT_ID, 0x08, 0x2B, 0x06, 0x01, 0x02, 0x01, 0x01, 0x01, 0x00 );

    this.buffer.reset();

    this.bos.writeObjectIdentifier( new int[] { 1, 2, 840, 113549 } );
    assertContent( OBJECT_ID, 0x06, 0x2a, 0x86, 0x48, 0x86, 0xf7, 0x0d );
  }

  /**
   * Test for {@link BerOutputStream#writeOctetString(byte[])}.
   */
  @Test
  public void testWriteOctetString() throws IOException
  {
    byte[] content = new byte[] { 0x12, 0x34, 0x56, 0x78, 0x78, 0x65, 0x43, 0x21 };
    this.bos.writeOctetString( content );
    assertContent( OCTET_STRING, content.length, 0x12, 0x34, 0x56, 0x78, 0x78, 0x65, 0x43, 0x21 );
  }

  /**
   * Test for {@link BerOutputStream#writeSequence(nl.lxtreme.asn.AsnSequence)}.
   */
  @Test
  public void testWriteSequence() throws IOException
  {
    AsnSequenceBuilder builder = new AsnSequenceBuilder();
    AsnSequence seq = builder.addBoolean( true ).addInt( 0x1234 ).addString( "1234" ).toSequence();

    this.bos.writeSequence( seq );

    assertContent( SEQUENCE.ordinal() | 0x20, 0x0D, //
        BOOLEAN.ordinal(), 0x01, 0xFF, //
        INTEGER.ordinal(), 0x02, 0x12, 0x34, //
        OCTET_STRING.ordinal(), 0x04, '1', '2', '3', '4' );
  }

  /**
   * Test for {@link BerOutputStream#writeUTF8String(String)}.
   */
  @Test
  public void testWriteUTF8String() throws IOException
  {
    this.bos.writeUTF8String( "H\u20ACllo world" );
    // \u20AC == 0xE2 0x82 0xAC
    assertContent( OCTET_STRING, 0x0D, 'H', 0xE2, 0x82, 0xAC, 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' );
  }

  /**
   * @param aType
   *          the type of the BER encoded type to check;
   * @param aBytes
   *          the bytes to verify.
   */
  private void assertContent( final AsnType aType, final int... aBytes )
  {
    assertContent( aType.ordinal(), aBytes );
  }

  /**
   * @param aType
   *          the type of the BER encoded type to check;
   * @param aBytes
   *          the bytes to verify.
   */
  private void assertContent( final int aType, final int... aBytes )
  {
    final byte[] expected = new byte[aBytes.length + 1];
    expected[0] = ( byte )aType;
    for ( int i = 0; i < aBytes.length; i++ )
    {
      expected[i + 1] = ( byte )aBytes[i];
    }

    final byte[] real = this.buffer.toByteArray();
    assertArrayEquals( expected, real );
  }
}
