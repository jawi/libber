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
import java.util.*;

import nl.lxtreme.asn.*;
import org.junit.*;


/**
 * Test cases for {@link BerInputStream}.
 */
public class BerInputStreamTest
{
  // CONSTANTS

  private static int CONSTRUCTED = 0x20;

  // VARIABLES

  private BerInputStream bis;

  // METHODS

  /**
   * Test case for {@link BerInputStream#readSequenceAsStream()}.
   */
  @Test
  public void testReadBeyondSequenceStreamFails() throws IOException
  {
    prepareContent( SEQUENCE.ordinal() | CONSTRUCTED, 0x0D, //
        BOOLEAN.ordinal(), 0x01, 0xFF, //
        INTEGER.ordinal(), 0x02, 0x12, 0x34, //
        OCTET_STRING.ordinal(), 0x04, '1', '2', '3', '4', //
        SEQUENCE.ordinal() | CONSTRUCTED, 0x0C, //
        BOOLEAN.ordinal(), 0x01, 0x00, //
        INTEGER.ordinal(), 0x02, 0x56, 0x78, //
        OCTET_STRING.ordinal(), 0x03, '3', '2', '1' );

    BerInputStream seqIS = this.bis.readSequenceAsStream();

    assertNotNull( seqIS );
    assertEquals( true, seqIS.readBoolean() );
    assertEquals( Integer.valueOf( 0x1234 ), seqIS.readInt() );
    assertEquals( "1234", seqIS.readString() );
    // This should yield null!
    assertNull( seqIS.readBoolean() );
  }

  /**
   * Test case for {@link BerInputStream#readBitString()}.
   */
  @Test
  public void testReadBitString() throws IOException
  {
    BigInteger expectedValue = new BigInteger( "113015" );

    // padded with 6 zeros (000000)...
    prepareContent( BIT_STRING, 0x04, 0x06, 0x6e, 0x5d, 0xc0 );
    assertEquals( expectedValue, this.bis.readBitString() );

    // padded with '100000'
    prepareContent( BIT_STRING, 0x04, 0x06, 0x6e, 0x5d, 0xe0 );
    assertEquals( expectedValue, this.bis.readBitString() );

    // long length form...
    prepareContent( BIT_STRING, 0x81, 0x04, 0x06, 0x6e, 0x5d, 0xc0 );
    assertEquals( expectedValue, this.bis.readBitString() );

    // constructed form...
    prepareContent( BIT_STRING.ordinal() | CONSTRUCTED, 0x09, //
        BIT_STRING.ordinal(), 0x03, 0x00, 0x6e, 0x5d, //
        BIT_STRING.ordinal(), 0x02, 0x06, 0xc0 );
    assertEquals( expectedValue, this.bis.readBitString() );
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

    prepareContent( BOOLEAN, 0x81, 0x01, 0xFF );
    assertTrue( this.bis.readBoolean() );
  }

  /**
   * Test case for {@link BerInputStream#readIA5String()}.
   */
  @Test
  public void testReadIA5String() throws IOException
  {
    prepareContent( IA5_STRING, 0x0d, 0x74, 0x65, 0x73, 0x74, 0x31, 0x40, 0x72, 0x73, 0x61, 0x2e, 0x63, 0x6f, 0x6d );
    assertEquals( "test1@rsa.com", this.bis.readIA5String() );

    prepareContent( IA5_STRING, 0x81, 0x0d, 0x74, 0x65, 0x73, 0x74, 0x31, 0x40, 0x72, 0x73, 0x61, 0x2e, 0x63, 0x6f,
        0x6d );
    assertEquals( "test1@rsa.com", this.bis.readIA5String() );

    prepareContent( IA5_STRING.ordinal() | CONSTRUCTED, 0x13, //
        IA5_STRING.ordinal(), 0x05, 0x74, 0x65, 0x73, 0x74, 0x31, //
        IA5_STRING.ordinal(), 0x01, 0x40, //
        IA5_STRING.ordinal(), 0x07, 0x72, 0x73, 0x61, 0x2e, 0x63, 0x6f, 0x6d );
    assertEquals( "test1@rsa.com", this.bis.readIA5String() );
  }

  /**
   * Test case for {@link BerInputStream#readSequenceAsStream()}.
   */
  @Test( expected = IOException.class )
  public void testReadIncompleteBerValueFails() throws IOException
  {
    prepareContent( BOOLEAN.ordinal(), 0x01 );

    // This should throw an exception!
    assertEquals( false, this.bis.readBoolean() );
  }

  /**
   * Test case for {@link BerInputStream#readSequenceAsStream()}.
   */
  @Test( expected = IOException.class )
  public void testReadIncorrectBerTypeFails() throws IOException
  {
    prepareContent( BOOLEAN.ordinal(), 0x01, 0xFF );

    // This should throw an exception!
    this.bis.readInt();
  }

  /**
   * Test case for {@link BerInputStream#readInt()}.
   */
  @Test
  public void testReadInt() throws IOException
  {
    prepareContent( INTEGER, 0x01, 0x00 );
    assertEquals( Integer.valueOf( 0x00 ), this.bis.readInt() );

    prepareContent( INTEGER, 0x01, 127 );
    assertEquals( Integer.valueOf( 127 ), this.bis.readInt() );

    prepareContent( INTEGER, 0x02, 0x00, 0x80 );
    assertEquals( Integer.valueOf( 128 ), this.bis.readInt() );

    prepareContent( INTEGER, 0x02, 0x01, 0x00 );
    assertEquals( Integer.valueOf( 256 ), this.bis.readInt() );

    prepareContent( INTEGER, 0x01, 0x80 );
    assertEquals( Integer.valueOf( -128 ), this.bis.readInt() );

    prepareContent( INTEGER, 0x02, 0xFF, 0x7F );
    assertEquals( Integer.valueOf( -129 ), this.bis.readInt() );

    prepareContent( INTEGER, 0x04, 0x7F, 0xFF, 0xFF, 0xFF );
    assertEquals( Integer.valueOf( Integer.MAX_VALUE ), this.bis.readInt() );

    prepareContent( INTEGER, 0x04, 0x80, 0x00, 0x00, 0x00 );
    assertEquals( Integer.valueOf( Integer.MIN_VALUE ), this.bis.readInt() );
  }

  /**
   * Test case for {@link BerInputStream#readSequenceAsStream()}.
   */
  @Test( expected = IOException.class )
  public void testReadInvalidBerValueFails() throws IOException
  {
    prepareContent( BOOLEAN.ordinal() | CONSTRUCTED );

    // This should throw an exception!
    assertEquals( false, this.bis.readBoolean() );
  }

  /**
   * Test case for {@link BerInputStream#readOctetString()}.
   */
  @Test
  public void testReadLongOctetString() throws IOException
  {
    int[] content = new int[259];
    content[0] = 0x82;
    content[1] = 0x01;
    content[2] = 0x00;
    for ( int i = 0; i < 256; i++ )
    {
      content[i + 3] = ( 255 - i );
    }
    byte[] rawBuffer = prepareContent( OCTET_STRING, content );

    byte[] asciiTable = new byte[256];
    System.arraycopy( rawBuffer, 4, asciiTable, 0, asciiTable.length );

    assertArrayEquals( asciiTable, this.bis.readOctetString() );
  }

  /**
   * Test case for {@link BerInputStream#readSequenceAsStream()}.
   */
  @Test
  public void testReadMultipleSequenceStreams() throws IOException
  {
    prepareContent( SEQUENCE.ordinal() | CONSTRUCTED, 0x0D, //
        BOOLEAN.ordinal(), 0x01, 0xFF, //
        INTEGER.ordinal(), 0x02, 0x12, 0x34, //
        OCTET_STRING.ordinal(), 0x04, '1', '2', '3', '4', //
        SEQUENCE.ordinal() | CONSTRUCTED, 0x0C, //
        BOOLEAN.ordinal(), 0x01, 0x00, //
        INTEGER.ordinal(), 0x02, 0x56, 0x78, //
        OCTET_STRING.ordinal(), 0x03, '3', '2', '1' );

    BerInputStream seqIS = this.bis.readSequenceAsStream();

    assertNotNull( seqIS );
    assertEquals( true, seqIS.readBoolean() );
    assertEquals( Integer.valueOf( 0x1234 ), seqIS.readInt() );
    assertEquals( "1234", seqIS.readString() );
    assertEquals( -1, seqIS.read() );

    seqIS = this.bis.readSequenceAsStream();

    assertNotNull( seqIS );
    assertEquals( false, seqIS.readBoolean() );
    assertEquals( Integer.valueOf( 0x5678 ), seqIS.readInt() );
    assertEquals( "321", seqIS.readString() );
    assertEquals( -1, seqIS.read() );
  }

  /**
   * Test case for {@link BerInputStream#readNull()}.
   */
  @Test
  public void testReadNull() throws IOException
  {
    prepareContent( NULL, 0x00 );
    assertNull( this.bis.readNull() );

    prepareContent( NULL, 0x81, 0x00 );
    assertNull( this.bis.readNull() );

    prepareContent( NULL, 0x82, 0x00, 0x00 );
    assertNull( this.bis.readNull() );
  }

  /**
   * Test case for {@link BerInputStream#readObjectIdentifier()}.
   */
  @Test
  public void testReadObjectIdentifier() throws IOException
  {
    prepareContent( OBJECT_ID, 0x03, 0x81, 0x34, 0x03 );
    assertArrayEquals( new int[] { 2, 100, 3 }, this.bis.readObjectIdentifier() );

    prepareContent( OBJECT_ID, 0x08, 0x2B, 0x06, 0x01, 0x02, 0x01, 0x01, 0x01, 0x00 );
    assertArrayEquals( new int[] { 1, 3, 6, 1, 2, 1, 1, 1, 0 }, this.bis.readObjectIdentifier() );
  }

  /**
   * Test case for {@link BerInputStream#readOctetString()}.
   */
  @Test
  public void testReadOctetString() throws IOException
  {
    prepareContent( OCTET_STRING, 0x05, 'h', 'e', 'l', 'l', 'o' );
    assertArrayEquals( new byte[] { 'h', 'e', 'l', 'l', 'o' }, this.bis.readOctetString() );

    byte[] bs = new byte[] { 0x01, 0x23, 0x45, 0x67, ( byte )0x89, ( byte )0xab, ( byte )0xcd, ( byte )0xef };

    prepareContent( OCTET_STRING, 0x81, 0x08, 0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef );
    assertArrayEquals( bs, this.bis.readOctetString() );

    prepareContent( OCTET_STRING.ordinal() | CONSTRUCTED, 0x0c, //
        OCTET_STRING.ordinal(), 0x04, 0x01, 0x23, 0x45, 0x67, //
        OCTET_STRING.ordinal(), 0x04, 0x89, 0xab, 0xcd, 0xef );
    assertArrayEquals( bs, this.bis.readOctetString() );
  }

  /**
   * Test case for {@link BerInputStream#readSequenceAsStream()}.
   */
  @Test( expected = IOException.class )
  public void testReadPrimitiveSequenceFail() throws IOException
  {
    prepareContent( SEQUENCE.ordinal(), 0x03, //
        BOOLEAN.ordinal(), 0x01, 0xFF );

    // Should fail...
    this.bis.readSequenceAsStream();
  }

  /**
   * Test case for {@link BerInputStream#readPrintableString()}.
   */
  @Test
  public void testReadPrintableString() throws IOException
  {
    prepareContent( PRINTABLE_STRING, 0x0b, 0x54, 0x65, 0x73, 0x74, 0x20, 0x55, 0x73, 0x65, 0x72, 0x20, 0x31 );
    assertEquals( "Test User 1", this.bis.readPrintableString() );

    prepareContent( PRINTABLE_STRING, 0x81, 0x0b, 0x54, 0x65, 0x73, 0x74, 0x20, 0x55, 0x73, 0x65, 0x72, 0x20, 0x31 );
    assertEquals( "Test User 1", this.bis.readPrintableString() );

    prepareContent( PRINTABLE_STRING.ordinal() | CONSTRUCTED, 0x0f, //
        PRINTABLE_STRING.ordinal(), 0x05, 0x54, 0x65, 0x73, 0x74, 0x20, //
        PRINTABLE_STRING.ordinal(), 0x06, 0x55, 0x73, 0x65, 0x72, 0x20, 0x31 );
    assertEquals( "Test User 1", this.bis.readPrintableString() );
  }

  /**
   * Test case for {@link BerInputStream#readRelativeObjectIdentifier()}.
   */
  @Test
  public void testReadRelativeObjectIdentifier() throws IOException
  {
    prepareContent( RELATIVE_OID, 0x04, 0xC2, 0x7B, 0x03, 0x02 );
    assertArrayEquals( new int[] { 8571, 3, 2 }, this.bis.readRelativeObjectIdentifier() );
  }

  /**
   * Test case for {@link BerInputStream#readSequenceAsStream()}.
   */
  @Test
  public void testReadSequenceStream() throws IOException
  {
    prepareContent( SEQUENCE.ordinal() | CONSTRUCTED, 0x0D, //
        BOOLEAN.ordinal(), 0x01, 0xFF, //
        INTEGER.ordinal(), 0x02, 0x12, 0x34, //
        OCTET_STRING.ordinal(), 0x04, '1', '2', '3', '4' );

    BerInputStream seqIS = this.bis.readSequenceAsStream();

    assertNotNull( seqIS );
    assertEquals( true, seqIS.readBoolean() );
    assertEquals( Integer.valueOf( 0x1234 ), seqIS.readInt() );
    assertEquals( "1234", seqIS.readString() );
    assertEquals( -1, seqIS.read() );
  }

  /**
   * Test case for {@link BerInputStream#readSetAsStream()}.
   */
  @Test
  public void testReadSetStream() throws IOException
  {
    prepareContent( SET.ordinal() | CONSTRUCTED, 0x0D, //
        BOOLEAN.ordinal(), 0x01, 0xFF, //
        INTEGER.ordinal(), 0x02, 0x12, 0x34, //
        OCTET_STRING.ordinal(), 0x04, '1', '2', '3', '4' );

    BerInputStream seqIS = this.bis.readSetAsStream();

    assertNotNull( seqIS );
    assertEquals( true, seqIS.readBoolean() );
    assertEquals( Integer.valueOf( 0x1234 ), seqIS.readInt() );
    assertEquals( "1234", seqIS.readString() );
    assertEquals( -1, seqIS.read() );
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
   * Test case for {@link BerInputStream#readUtcTime()}.
   */
  @Test( expected = IOException.class )
  public void testReadTooLongUtcTimeFail() throws IOException
  {
    prepareContent( UTC_TIME, "910506234500+12345" );
    // Should fail!
    this.bis.readUtcTime();
  }

  /**
   * Test case for {@link BerInputStream#readUtcTime()}.
   */
  @Test( expected = IOException.class )
  public void testReadTooShortUtcTimeFail() throws IOException
  {
    prepareContent( UTC_TIME, "9105062345" );
    // Should fail!
    this.bis.readUtcTime();
  }

  /**
   * Test case for {@link BerInputStream#readUtcTime()}.
   */
  @Test
  public void testReadUtcTime() throws IOException
  {
    Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "PST" ) );
    cal.set( 1991, Calendar.MAY, 6, 16, 45, 40 );
    cal.set( Calendar.MILLISECOND, 0 );

    // YYMMDDhhmmssZ
    prepareContent( UTC_TIME, "910506234540Z" );
    assertEquals( cal.getTime(), this.bis.readUtcTime() );

    // YYMMDDhhmmss-hh'mm'
    prepareContent( UTC_TIME, "910506164540-0700" );
    assertEquals( cal.getTime(), this.bis.readUtcTime() );

    cal.set( Calendar.SECOND, 0 );

    // YYMMDDhhmmZ
    prepareContent( UTC_TIME, "9105062345Z" );
    assertEquals( cal.getTime(), this.bis.readUtcTime() );

    // YYMMDDhhmmss-hh'mm'
    prepareContent( UTC_TIME, "9105061645-0700" );
    assertEquals( cal.getTime(), this.bis.readUtcTime() );

    cal = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
    cal.set( 2008, Calendar.OCTOBER, 15, 15, 03, 41 );
    cal.set( Calendar.MILLISECOND, 0 );

    prepareContent( UTC_TIME, 0x0D, 0x30, 0x38, 0x31, 0x30, 0x31, 0x35, 0x31, 0x35, 0x30, 0x33, 0x34, 0x31, 0x5A );
    assertEquals( cal.getTime(), this.bis.readUtcTime() );
  }

  /**
   * Test case for {@link BerInputStream#readUTF8String()}.
   */
  @Test
  public void testReadUTF8String() throws IOException
  {
    prepareContent( UTF8_STRING, 0x07, 'h', 0xE2, 0x82, 0xAC, 'l', 'l', 'o' );
    assertEquals( "h\u20ACllo", this.bis.readUTF8String() );
  }

  /**
   * @param aType
   * @param aValues
   */
  private byte[] prepareContent( final AsnType aType, final int... aValues ) throws IOException
  {
    return prepareContent( aType.ordinal(), aValues );
  }

  /**
   * @param aType
   * @param aValues
   */
  private byte[] prepareContent( final AsnType aType, final String aByteRepresentation ) throws IOException
  {
    byte[] content = aByteRepresentation.getBytes();
    byte[] buffer = new byte[content.length + 2];
    Arrays.fill( buffer, ( byte )0x00 );

    buffer[0] = ( byte )aType.ordinal();
    buffer[1] = ( byte )content.length;
    for ( int i = 0; i < content.length; i++ )
    {
      buffer[i + 2] = content[i];
    }

    this.bis = new BerInputStream( new ByteArrayInputStream( buffer ) );
    return buffer;
  }

  /**
   * @param aType
   * @param aValues
   */
  private byte[] prepareContent( final int aType, final int... aValues ) throws IOException
  {
    byte[] buffer = new byte[aValues.length + 1];
    Arrays.fill( buffer, ( byte )0x00 );

    buffer[0] = ( byte )aType;
    for ( int i = 0; i < aValues.length; i++ )
    {
      buffer[i + 1] = ( byte )aValues[i];
    }

    this.bis = new BerInputStream( new ByteArrayInputStream( buffer ) );
    return buffer;
  }
}
