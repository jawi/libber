package nl.lxtreme.asn;


import static org.junit.Assert.*;

import nl.lxtreme.asn.AsnSequence.AsnSequenceType;

import org.junit.*;


/**
 * Test cases for {@link AsnSequence}.
 */
public class AsnSequenceTest
{
  // METHODS

  /**
   * Tests the {@link AsnSequence#getLength()} method.
   */
  @Test
  public void testGetLength()
  {
    AsnSequence seq;
    AsnSequenceBuilder builder = new AsnSequenceBuilder();

    final byte[] bytes = "Smith".getBytes();

    seq = builder.addBoolean( false ).toSequence();
    assertEquals( 3, seq.getLength() );

    seq = builder.addString( bytes ).addBoolean( true ).toSequence();
    assertEquals( 10, seq.getLength() );

    seq = builder.addContextValue( 0, bytes ).toSequence();
    assertEquals( 7, seq.getLength() );

    seq = builder.addContextValue( 0, bytes ).addContextValue( 1, false ).toSequence();
    assertEquals( 10, seq.getLength() );
  }

  /**
   * Tests the {@link AsnSequence#iterator()} method.
   */
  @Test
  public void testIterator()
  {
    AsnSequence seq;
    AsnSequenceBuilder builder = new AsnSequenceBuilder();

    seq = builder.addBoolean( false ).toSequence();
    for ( AsnSequenceType seqType : seq )
    {
      assertEquals( AsnType.BOOLEAN, seqType.getType().getType() );
      assertEquals( 3, seqType.getLength() );
      assertEquals( Boolean.FALSE, seqType.getValue() );
    }

    final byte[] bytes = "Smith".getBytes();
    
    seq = builder.addString( bytes ).addBoolean( true ).toSequence();
    int i = 0;
    for ( AsnSequenceType seqType : seq )
    {
      if ( i == 0 )
      {
        assertEquals( AsnType.OCTET_STRING, seqType.getType().getType() );
        assertEquals( 7, seqType.getLength() );
        assertArrayEquals( bytes, ( byte[] )seqType.getValue() );
        i++;
      }
      else if ( i == 1 )
      {
        assertEquals( AsnType.BOOLEAN, seqType.getType().getType() );
        assertEquals( 3, seqType.getLength() );
        assertEquals( Boolean.TRUE, seqType.getValue() );
        i++;
      }
      else
      {
        fail( "Unknown type: " + seqType.getType().getType() );
      }
    }
  }
}
