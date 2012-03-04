/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn.ber;


import static nl.lxtreme.asn.AsnType.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import nl.lxtreme.asn.*;
import nl.lxtreme.asn.AsnSequence.AsnSequenceType;

import org.junit.*;


/**
 * Test cases for {@link BerInputStream}.
 */
public class BerInputStreamTest
{
  // VARIABLES

  private byte[] buffer = new byte[512];
  private ByteArrayInputStream bais;
  private BerInputStream bis;

  // METHODS

  /**
   * Sets up each test case.
   */
  @Before
  public void setUp()
  {
    this.bais = new ByteArrayInputStream( this.buffer );
    this.bis = new BerInputStream( this.bais );
  }

  /**
   * Test case for {@link BerInputStream#readBoolean()}.
   */
  @Test
  public void testReadBoolean() throws IOException
  {
    prepareContent( BOOLEAN, 0x01, 0xFF );
    assertTrue( this.bis.readBoolean() );

    prepareContent( BOOLEAN, 0x01, 0x00 );
    assertFalse( this.bis.readBoolean() );
  }

  /**
   * Test case for {@link BerInputStream#readInt()}.
   */
  @Test
  public void testReadInt() throws IOException
  {
    prepareContent( INTEGER, 0x01, 0x00 );
    assertEquals( 0x00, this.bis.readInt() );

    prepareContent( INTEGER, 0x01, 127 );
    assertEquals( 127, this.bis.readInt() );

    prepareContent( INTEGER, 0x02, 0x00, 0x80 );
    assertEquals( 128, this.bis.readInt() );

    prepareContent( INTEGER, 0x02, 0x01, 0x00 );
    assertEquals( 256, this.bis.readInt() );

    prepareContent( INTEGER, 0x01, 0x80 );
    assertEquals( -128, this.bis.readInt() );

    prepareContent( INTEGER, 0x02, 0xFF, 0x7F );
    assertEquals( -129, this.bis.readInt() );

    prepareContent( INTEGER, 0x04, 0x7F, 0xFF, 0xFF, 0xFF );
    assertEquals( Integer.MAX_VALUE, this.bis.readInt() );

    prepareContent( INTEGER, 0x04, 0x80, 0x00, 0x00, 0x00 );
    assertEquals( Integer.MIN_VALUE, this.bis.readInt() );
  }

  /**
   * Test case for {@link BerInputStream#readOctetString()}.
   */
  @Test
  public void testReadOctetString() throws IOException
  {
    prepareContent( OCTET_STRING, 0x05, 'h', 'e', 'l', 'l', 'o' );
    assertArrayEquals( new byte[] { 'h', 'e', 'l', 'l', 'o' }, this.bis.readOctetString() );
  }

  /**
   * Test case for {@link BerInputStream#readString()}.
   */
  @Test
  public void testReadString() throws IOException
  {
    prepareContent( OCTET_STRING, 0x05, 'h', 'e', 'l', 'l', 'o' );
    assertEquals( "hello", this.bis.readString() );
  }

  /**
   * Test case for {@link BerInputStream#readUTF8String()}.
   */
  @Test
  public void testReadUTF8String() throws IOException
  {
    prepareContent( OCTET_STRING, 0x07, 'h', 0xE2, 0x82, 0xAC, 'l', 'l', 'o' );
    assertEquals( "h\u20ACllo", this.bis.readUTF8String() );
  }

  /**
   * Test case for {@link BerInputStream#readSequence()}.
   */
  @Test
  public void testReadSequence() throws IOException
  {
    prepareContent( SEQUENCE.ordinal() | 0x20, 0x0D, //
        BOOLEAN.ordinal(), 0x01, 0xFF, //
        INTEGER.ordinal(), 0x02, 0x12, 0x34, //
        OCTET_STRING.ordinal(), 0x04, '1', '2', '3', '4' );

    AsnSequenceBuilder builder = new AsnSequenceBuilder();
    AsnSequence expectedSeq = builder.addBoolean( true ).addInt( 0x1234 ).addString( "1234" ).toSequence();

    AsnSequence readSeq = this.bis.readSequence();

    assertEquals( expectedSeq.getItemCount(), readSeq.getItemCount() );
    for ( int i = 0; i < expectedSeq.getItemCount(); i++ )
    {
      AsnSequenceType expectedSeqType = expectedSeq.getSequenceTypes().get( i );
      AsnSequenceType readSeqType = readSeq.getSequenceTypes().get( i );

      assertEquals( "Types incorrect!", expectedSeqType.getType(), readSeqType.getType() );
      assertEquals( "Lengths incorrect!", expectedSeqType.getLength(), readSeqType.getLength() );
      assertEquals( "Values incorrect!", expectedSeqType.getValue(), readSeqType.getValue() );
    }
  }

  /**
   * @param aType
   * @param aValues
   */
  private void prepareContent( AsnType aType, int... aValues ) throws IOException
  {
    prepareContent( aType.ordinal(), aValues );
  }

  /**
   * @param aType
   * @param aValues
   */
  private void prepareContent( int aType, int... aValues ) throws IOException
  {
    Arrays.fill( this.buffer, ( byte )0x00 );

    this.bais.reset();

    this.buffer[0] = ( byte )aType;
    for ( int i = 0; i < aValues.length; i++ )
    {
      this.buffer[i + 1] = ( byte )aValues[i];
    }
  }
}
