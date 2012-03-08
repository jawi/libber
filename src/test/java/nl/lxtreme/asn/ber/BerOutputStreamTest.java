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
   * Test for {@link BerOutputStream#writeSequenceAsStream()}.
   */
  @Test
  public void testWriteConsecutiveSequencesAsStream() throws IOException
  {
    final BerOutputStream seqStream1 = this.bos.writeSequenceAsStream();
    final BerOutputStream seqStream2 = this.bos.writeSequenceAsStream();

    seqStream1.writeBoolean( true );
    seqStream1.writeInt( 0x1234 );
    seqStream1.writeString( "1234" );
    seqStream1.close();

    seqStream2.writeString( "4567" );
    seqStream2.writeInt( 0x1892 );
    seqStream2.writeBoolean( false );
    seqStream2.close();

    assertContent( SEQUENCE.ordinal() | 0x20, 0x0D, //
        BOOLEAN.ordinal(), 0x01, 0xFF, //
        INTEGER.ordinal(), 0x02, 0x12, 0x34, //
        OCTET_STRING.ordinal(), 0x04, '1', '2', '3', '4', //
        SEQUENCE.ordinal() | 0x20, 0x0D, //
        OCTET_STRING.ordinal(), 0x04, '4', '5', '6', '7', //
        INTEGER.ordinal(), 0x02, 0x18, 0x92, //
        BOOLEAN.ordinal(), 0x01, 0x00 //
    );
  }

  /**
   * Test case for {@link BerOutputStream#writeGeneralizedTime(Calendar)}.
   */
  @Test
  public void testWriteGeneralizedTime() throws IOException
  {
    Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
    cal.set( 2008, Calendar.OCTOBER, 15, 15, 03, 41 );
    cal.set( Calendar.MILLISECOND, 0 );

    this.bos.writeGeneralizedTime( cal );
    assertContent( GENERALIZED_TIME.ordinal(), "20081015150341Z" );

    cal = Calendar.getInstance( TimeZone.getTimeZone( "PST" ) );
    cal.set( 1991, Calendar.MAY, 6, 16, 45, 40 );
    cal.set( Calendar.MILLISECOND, 0 );

    this.buffer.reset();

    this.bos.writeGeneralizedTime( cal );
    assertContent( GENERALIZED_TIME.ordinal(), "19910506164540-0700" );

    cal.set( Calendar.MILLISECOND, 1 );

    this.buffer.reset();

    this.bos.writeGeneralizedTime( cal );
    assertContent( GENERALIZED_TIME.ordinal(), "19910506164540.1-0700" );

    cal.set( Calendar.MILLISECOND, 12 );

    this.buffer.reset();

    this.bos.writeGeneralizedTime( cal );
    assertContent( GENERALIZED_TIME.ordinal(), "19910506164540.12-0700" );

    cal.set( Calendar.MILLISECOND, 10 );

    this.buffer.reset();

    this.bos.writeGeneralizedTime( cal );
    assertContent( GENERALIZED_TIME.ordinal(), "19910506164540.10-0700" );

    cal.set( Calendar.MILLISECOND, 123 );

    this.buffer.reset();

    this.bos.writeGeneralizedTime( cal );
    assertContent( GENERALIZED_TIME.ordinal(), "19910506164540.123-0700" );
  }

  /**
   * Test for {@link BerOutputStream#writeIA5String(String)}.
   */
  @Test
  public void testWriteIA5String() throws IOException
  {
    this.bos.writeIA5String( "test1@rsa.com" );
    assertContent( IA5_STRING, 0x0d, 0x74, 0x65, 0x73, 0x74, 0x31, 0x40, 0x72, 0x73, 0x61, 0x2e, 0x63, 0x6f, 0x6d );
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
   * Test for {@link BerOutputStream#writeOctetString(byte[])}.
   */
  @Test
  public void testWriteLongOctetString() throws IOException
  {
    byte[] asciiTable = new byte[256];
    for ( int i = 0; i < asciiTable.length; i++ )
    {
      asciiTable[i] = ( byte )( 255 - i );
    }

    int[] content = new int[asciiTable.length + 3];
    content[0] = 0x82;
    content[1] = 0x01;
    content[2] = 0x00;
    for ( int i = 0; i < asciiTable.length; i++ )
    {
      content[i + 3] = ( 255 - i ) & 0xFF;
    }

    this.bos.writeOctetString( asciiTable );
    assertContent( OCTET_STRING.ordinal(), content );
  }

  /**
   * Test for {@link BerOutputStream#writeSequenceAsStream()}.
   */
  @Test
  public void testWriteNestedSequencesAsStream() throws IOException
  {
    final BerOutputStream mainSeqStream = this.bos.writeSequenceAsStream();

    mainSeqStream.writeBoolean( true );
    mainSeqStream.writeInt( 0x1234 );

    final BerOutputStream nestedSeqStream = mainSeqStream.writeSequenceAsStream();
    nestedSeqStream.writeString( "4567" );
    nestedSeqStream.writeInt( 0x1892 );
    nestedSeqStream.writeBoolean( false );
    nestedSeqStream.close();

    mainSeqStream.writeString( "1234" );
    mainSeqStream.close();

    assertContent( SEQUENCE.ordinal() | 0x20, 0x1C, //
        BOOLEAN.ordinal(), 0x01, 0xFF, //
        INTEGER.ordinal(), 0x02, 0x12, 0x34, //
        SEQUENCE.ordinal() | 0x20, 0x0D, //
        OCTET_STRING.ordinal(), 0x04, '4', '5', '6', '7', //
        INTEGER.ordinal(), 0x02, 0x18, 0x92, //
        BOOLEAN.ordinal(), 0x01, 0x00, //
        OCTET_STRING.ordinal(), 0x04, '1', '2', '3', '4' //
    );
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
   * Test for {@link BerOutputStream#writePrintableString(String)}.
   */
  @Test
  public void testWritePrintableString() throws IOException
  {
    this.bos.writePrintableString( "Hello world" );
    assertContent( PRINTABLE_STRING, 0x0B, 'H', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' );

    this.buffer.reset();

    this.bos.writePrintableString( "H\u20ACllo world" );
    assertContent( PRINTABLE_STRING, 0x0A, 'H', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' );

    this.buffer.reset();

    this.bos.writePrintableString( "" );
    assertContent( PRINTABLE_STRING, 0x00 );
  }

  /**
   * Test for {@link BerOutputStream#writeRelativeObjectIdentifier(int[])}.
   */
  @Test
  public void testWriteRelativeObjectIdentifier() throws IOException
  {
    this.bos.writeRelativeObjectIdentifier( new int[] { 2, 100, 3 } );
    assertContent( RELATIVE_OID, 0x03, 0x02, 0x64, 0x03 );

    this.buffer.reset();

    this.bos.writeRelativeObjectIdentifier( new int[] { 1, 3, 6, 1, 2, 1, 1, 1, 0 } );
    assertContent( RELATIVE_OID, 0x09, 0x01, 0x03, 0x06, 0x01, 0x02, 0x01, 0x01, 0x01, 0x00 );

    this.buffer.reset();

    this.bos.writeRelativeObjectIdentifier( new int[] { 1, 2, 840, 113549 } );
    assertContent( RELATIVE_OID, 0x07, 0x01, 0x02, 0x86, 0x48, 0x86, 0xf7, 0x0d );
  }

  /**
   * Test for {@link BerOutputStream#writeSequenceAsStream()}.
   */
  @Test
  public void testWriteSequenceAsStream() throws IOException
  {
    final BerOutputStream seqStream = this.bos.writeSequenceAsStream();
    seqStream.writeBoolean( true );
    seqStream.writeInt( 0x1234 );
    seqStream.writeString( "1234" );
    seqStream.close();

    assertContent( SEQUENCE.ordinal() | 0x20, 0x0D, //
        BOOLEAN.ordinal(), 0x01, 0xFF, //
        INTEGER.ordinal(), 0x02, 0x12, 0x34, //
        OCTET_STRING.ordinal(), 0x04, '1', '2', '3', '4' );
  }

  /**
   * Test for {@link BerOutputStream#writeSetAsStream()}.
   */
  @Test
  public void testWriteSetAsStream() throws IOException
  {
    final BerOutputStream setStream = this.bos.writeSetAsStream();
    setStream.writeBoolean( true );
    setStream.writeInt( 0x1234 );
    setStream.writeString( "1234" );
    setStream.close();

    assertContent( SET.ordinal() | 0x20, 0x0D, //
        BOOLEAN.ordinal(), 0x01, 0xFF, //
        INTEGER.ordinal(), 0x02, 0x12, 0x34, //
        OCTET_STRING.ordinal(), 0x04, '1', '2', '3', '4' );
  }

  /**
   * Test case for {@link BerOutputStream#writeUtcTime(Date)}.
   */
  @Test
  public void testWriteUtcTime() throws IOException
  {
    Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "PST" ) );
    cal.set( 1991, Calendar.MAY, 6, 16, 45, 40 );
    cal.set( Calendar.MILLISECOND, 0 );

    this.bos.writeUtcTime( cal );
    assertContent( UTC_TIME.ordinal(), "910506234540Z" );

    cal = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
    cal.set( 2008, Calendar.OCTOBER, 15, 15, 03, 41 );
    cal.set( Calendar.MILLISECOND, 0 );

    this.buffer.reset();

    this.bos.writeUtcTime( cal );
    assertContent( UTC_TIME.ordinal(), "081015150341Z" );

    cal.set( Calendar.MILLISECOND, 12 );

    this.buffer.reset();

    this.bos.writeUtcTime( cal );
    assertContent( UTC_TIME.ordinal(), "081015150341Z" );
  }

  /**
   * Test for {@link BerOutputStream#writeUTF8String(String)}.
   */
  @Test
  public void testWriteUTF8String() throws IOException
  {
    this.bos.writeUTF8String( "H\u20ACllo world" );
    // \u20AC == 0xE2 0x82 0xAC
    assertContent( UTF8_STRING, 0x0D, 'H', 0xE2, 0x82, 0xAC, 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' );
  }

  /**
   * @param aType
   *          the type of the BER encoded type to check;
   * @param aBytes
   *          the bytes to verify.
   * @throws IOException
   *           in case of I/O problems.
   */
  private void assertContent( final AsnType aType, final int... aBytes ) throws IOException
  {
    assertContent( aType.ordinal(), aBytes );
  }

  /**
   * @param aType
   *          the type of the BER encoded type to check;
   * @param aBytes
   *          the bytes to verify.
   * @throws IOException
   *           in case of I/O problems.
   */
  private void assertContent( final int aType, final int... aBytes ) throws IOException
  {
    final byte[] expected = new byte[aBytes.length + 1];
    expected[0] = ( byte )aType;
    for ( int i = 0; i < aBytes.length; i++ )
    {
      expected[i + 1] = ( byte )aBytes[i];
    }

    this.bos.flush();

    final byte[] real = this.buffer.toByteArray();
    assertArrayEquals( expected, real );
  }

  /**
   * @param aType
   *          the type of the BER encoded type to check;
   * @param aContent
   *          the string bytes to verify.
   * @throws IOException
   *           in case of I/O problems.
   */
  private void assertContent( final int aType, final String aContent ) throws IOException
  {
    final byte[] bytes = aContent.getBytes();
    final byte[] expected = new byte[bytes.length + 2];
    expected[0] = ( byte )aType;
    expected[1] = ( byte )bytes.length;
    System.arraycopy( bytes, 0, expected, 2, bytes.length );

    this.bos.flush();

    final byte[] real = this.buffer.toByteArray();
    assertArrayEquals( expected, real );
  }
}
