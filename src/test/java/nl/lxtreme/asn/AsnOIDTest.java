/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn;


import static org.junit.Assert.*;

import org.junit.*;


/**
 * Test cases for {@link AsnOID}.
 */
public class AsnOIDTest
{
  // METHODS

  /**
   * Tests the {@link AsnOID#getInfo(AsnOID)} method.
   */
  @Test( expected = IllegalArgumentException.class )
  public void testCreateEmptyOIDFail() throws Exception
  {
    new AsnOID( "" );
  }

  /**
   * Tests the {@link AsnOID#getInfo(AsnOID)} method.
   */
  @Test
  public void testCreateNewOID() throws Exception
  {
    AsnOID oid = new AsnOID( "1.2.840.113549.1.7.2" );
    assertNotNull( oid );
    assertArrayEquals( new int[] { 1, 2, 840, 113549, 1, 7, 2 }, oid.getSubIDs() );
  }

  /**
   * Tests the {@link AsnOID#getInfo(AsnOID)} method.
   */
  @Test( expected = IllegalArgumentException.class )
  public void testCreateNullOIDFail() throws Exception
  {
    new AsnOID();
  }

}
