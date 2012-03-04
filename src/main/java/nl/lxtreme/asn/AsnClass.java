/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn;


/**
 * Denotes the class of an ASN.1 type.
 */
public enum AsnClass
{
  // CONSTANTS

  /** Denotes a value is of a type native to ASN.1 (e.g. INTEGER). */
  UNIVERSAL( 0x00 ),
  /** Denotes a value is only valid for one specific application. */
  APPLICATION( 0x40 ),
  /**
   * Denotes a value depends on the context (such as within a sequence, set or
   * choice).
   */
  CONTEXT_SPECIFIC( 0x80 ),
  /** Denotes a value can be defined in private specifications. */
  PRIVATE( 0xC0 );

  // VARIABLES

  private final int mask;

  // CONSTRUCTORS

  /**
   * @param aMask
   *          the mask of the class value.
   */
  private AsnClass( final int aMask )
  {
    this.mask = aMask;
  }

  // METHODS

  /**
   * @param aValue
   *          the value to extract the class for.
   * @return the {@link AsnClass} corresponding to the given value, defaults to
   *         {@link #UNIVERSAL}, never <code>null</code>.
   */
  public static AsnClass valueOf( int aValue )
  {
    if ( ( aValue & PRIVATE.mask ) == PRIVATE.mask )
    {
      return PRIVATE;
    }
    else if ( ( aValue & CONTEXT_SPECIFIC.mask ) == CONTEXT_SPECIFIC.mask )
    {
      return CONTEXT_SPECIFIC;
    }
    else if ( ( aValue & APPLICATION.mask ) == APPLICATION.mask )
    {
      return APPLICATION;
    }
    return UNIVERSAL;
  }

  /**
   * @return the mask of this ASN.1 class.
   */
  public int getMask()
  {
    return this.mask;
  }
}
