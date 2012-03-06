/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn;


/**
 * Represents a ASN.1 type identifier.
 */
public class AsnIdentifier
{
  // CONSTANTS

  /** Distinguishes between primitive and constructed types. */
  private final int CONSTRUCTED = 0x20;

  // VARIABLES

  private final AsnClass clazz;
  private final boolean constructed;
  private final AsnType type;

  // CONTRUCTORS

  /**
   * Creates a new {@link AsnIdentifier} instance as primitive type.
   * 
   * @param aClass
   *          the {@link AsnClass} of this identifier;
   * @param aType
   *          the {@link AsnType} of this identifier.
   */
  public AsnIdentifier( final AsnClass aClass, final AsnType aType )
  {
    this( aClass, false /* aConstructed */, aType );
  }

  /**
   * Creates a new {@link AsnIdentifier} instance.
   * 
   * @param aClass
   *          the {@link AsnClass} of this identifier;
   * @param aConstructed
   *          <code>true</code> if this identifier is <em>constructed</em>,
   *          <code>false</code> if it is <em>primitive</em>;
   * @param aType
   *          the {@link AsnType} of this identifier.
   */
  public AsnIdentifier( final AsnClass aClass, final boolean aConstructed, final AsnType aType )
  {
    if ( aClass == null )
    {
      throw new IllegalArgumentException( "AsnClass cannot be null!" );
    }
    if ( aType == null )
    {
      throw new IllegalArgumentException( "AsnType cannot be null!" );
    }
    this.type = aType;
    this.clazz = aClass;
    this.constructed = aConstructed;
  }

  /**
   * Creates a new {@link AsnIdentifier} instance as universal primitive type.
   * 
   * @param aType
   *          the {@link AsnType} of this identifier.
   */
  public AsnIdentifier( final AsnType aType )
  {
    this( AsnClass.UNIVERSAL, false /* aConstructed */, aType );
  }

  /**
   * Creates a new {@link AsnIdentifier} instance.
   * 
   * @param aOctet
   *          the octet to convert into an identifier.
   * @throws IllegalArgumentException
   *           in case a long-form tag is given, or a non-UNIVERSAL identifier.
   */
  public AsnIdentifier( final int aOctet )
  {
    AsnType _type = AsnType.valueOf( aOctet );
    if ( _type == AsnType.LONG_FORM_TYPE )
    {
      throw new IllegalArgumentException( "Long form tag found!" );
    }
    this.type = _type;

    AsnClass _class = AsnClass.valueOf( aOctet );
    if ( ( _class != AsnClass.UNIVERSAL ) && ( _class != AsnClass.CONTEXT_SPECIFIC ) )
    {
      throw new IllegalArgumentException( "Non-UNIVERSAL class found: " + _class );
    }
    this.clazz = _class;

    this.constructed = ( aOctet & this.CONSTRUCTED ) != 0;
  }

  // METHODS

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals( final Object aObject )
  {
    if ( this == aObject )
    {
      return true;
    }
    if ( ( aObject == null ) || !( aObject instanceof AsnIdentifier ) )
    {
      return false;
    }

    final AsnIdentifier other = ( AsnIdentifier )aObject;
    if ( this.clazz != other.clazz )
    {
      return false;
    }

    if ( this.constructed != other.constructed )
    {
      return false;
    }

    if ( this.type != other.type )
    {
      return false;
    }

    return true;
  }

  /**
   * @return
   */
  public AsnClass getClazz()
  {
    return this.clazz;
  }

  /**
   * Calculates the length of this identifier, based on the given value.
   * 
   * @param aValue
   *          the value to calculate the length for, can be <code>null</code> in
   *          which case this method returns 0.
   * @return a length, >= 0.
   */
  public int getLength( final Object aValue )
  {
    if ( !AsnClass.UNIVERSAL.equals( this.clazz ) )
    {
      throw new IllegalArgumentException( "Unable to determine length for non-UNIVERSAL identifiers!" );
    }
    return this.type.getLength( aValue );
  }

  /**
   * Returns a 8-bit tag value for this identifier.
   * 
   * @return a tag value for this identifier, >= 0.
   */
  public int getTag()
  {
    return this.clazz.getMask() | ( this.constructed ? this.CONSTRUCTED : 0 ) | this.type.ordinal();
  }

  /**
   * @return
   */
  public AsnType getType()
  {
    return this.type;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = ( prime * result ) + ( ( this.clazz == null ) ? 0 : this.clazz.hashCode() );
    result = ( prime * result ) + ( this.constructed ? 1231 : 1237 );
    result = ( prime * result ) + ( ( this.type == null ) ? 0 : this.type.hashCode() );
    return result;
  }

  /**
   * @return
   */
  public boolean isConstructed()
  {
    return this.constructed;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    return getType() + "[constructed: " + isConstructed() + ", class = " + getClazz() + "]";
  }
}
