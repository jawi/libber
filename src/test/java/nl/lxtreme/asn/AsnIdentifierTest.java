/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn;


import static org.junit.Assert.*;

import org.junit.*;


/**
 * Test cases for {@link AsnIdentifier}.
 */
public class AsnIdentifierTest
{
  // METHODS

  /**
   * Test method for
   * {@link nl.lxtreme.asn.AsnIdentifier#AsnIdentifier(nl.lxtreme.asn.AsnType)}.
   */
  @Test
  public void testAsnIdentifierAsnType()
  {
    AsnIdentifier id = new AsnIdentifier( AsnType.SET );

    assertEquals( AsnClass.UNIVERSAL, id.getClazz() );
    assertEquals( AsnType.SET, id.getType() );
    assertFalse( id.isConstructed() );
  }

  /**
   * Test method for
   * {@link nl.lxtreme.asn.AsnIdentifier#AsnIdentifier(nl.lxtreme.asn.AsnClass, boolean, nl.lxtreme.asn.AsnType)}
   * .
   */
  @Test
  public void testAsnIdentifierComplete()
  {
    AsnIdentifier id = new AsnIdentifier( AsnClass.UNIVERSAL, true /* constructed */, AsnType.SET );

    assertEquals( AsnClass.UNIVERSAL, id.getClazz() );
    assertEquals( AsnType.SET, id.getType() );
    assertTrue( id.isConstructed() );
  }

  /**
   * Test method for {@link nl.lxtreme.asn.AsnIdentifier#AsnIdentifier(int)}.
   */
  @Test
  public void testAsnIdentifierWithConstructedTagValue()
  {
    AsnIdentifier id = new AsnIdentifier( 0x21 );

    assertEquals( AsnClass.UNIVERSAL, id.getClazz() );
    assertEquals( AsnType.BOOLEAN, id.getType() );
    assertTrue( id.isConstructed() );
  }

  /**
   * Test method for {@link nl.lxtreme.asn.AsnIdentifier#AsnIdentifier(int)}.
   */
  @Test( expected = IllegalArgumentException.class )
  public void testAsnIdentifierWithNegativeValueFail()
  {
    new AsnIdentifier( -1 );
  }

  /**
   * Test method for {@link nl.lxtreme.asn.AsnIdentifier#AsnIdentifier(int)}.
   */
  @Test
  public void testAsnIdentifierWithTagValue()
  {
    AsnIdentifier id = new AsnIdentifier( 0x01 );

    assertEquals( AsnClass.UNIVERSAL, id.getClazz() );
    assertEquals( AsnType.BOOLEAN, id.getType() );
    assertFalse( id.isConstructed() );
  }

  /**
   * Test method for {@link nl.lxtreme.asn.AsnIdentifier#AsnIdentifier(int)}.
   */
  @Test( expected = IllegalArgumentException.class )
  public void testAsnIdentifierWithTooLargeValueFail()
  {
    new AsnIdentifier( 0x100 );
  }

  /**
   * Test method for {@link AsnIdentifier#AsnIdentifier(AsnClass, AsnType)}.
   */
  @Test
  public void testCreateWithClassAndType()
  {
    AsnIdentifier id = new AsnIdentifier( AsnClass.UNIVERSAL, AsnType.BIT_STRING );

    assertEquals( AsnClass.UNIVERSAL, id.getClazz() );
    assertEquals( AsnType.BIT_STRING, id.getType() );
    assertFalse( id.isConstructed() );
  }

  /**
   * Test method for {@link AsnIdentifier#AsnIdentifier(AsnClass, AsnType)}.
   */
  @Test( expected = IllegalArgumentException.class )
  public void testCreateWithNullClassFail()
  {
    new AsnIdentifier( null, AsnType.BIT_STRING );
  }

  /**
   * Test method for {@link AsnIdentifier#AsnIdentifier(AsnClass, AsnType)}.
   */
  @Test( expected = IllegalArgumentException.class )
  public void testCreateWithNullTypeFail()
  {
    new AsnIdentifier( AsnClass.UNIVERSAL, null );
  }

  /**
   * Test method for {@link nl.lxtreme.asn.AsnIdentifier#getTag()}.
   */
  @Test
  public void testGetTag()
  {
    AsnIdentifier id;

    id = new AsnIdentifier( AsnClass.UNIVERSAL, AsnType.BIT_STRING );
    assertEquals( 0x03, id.getTag() );

    id = new AsnIdentifier( AsnClass.UNIVERSAL, true /* constructed */, AsnType.BIT_STRING );
    assertEquals( 0x23, id.getTag() );
  }
}
