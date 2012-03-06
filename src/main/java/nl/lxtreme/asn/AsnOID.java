/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn;


import java.util.*;


/**
 * Denotes a (relative) object identifier.
 */
public final class AsnOID
{
  // VARIABLES

  private final int[] subIDs;
  private final boolean relative;

  // CONSTRUCTORS

  /**
   * Creates a new {@link AsnOID} instance.
   * 
   * @param aRelative
   *          <code>true</code> if this OID is a relative object identifier,
   *          <code>false</code> if it is an (absolute) object identifier;
   * @param aSubIDs
   *          the sub identifiers of this OID, cannot be <code>null</code> or
   *          empty.
   */
  public AsnOID( final boolean aRelative, final int... aSubIDs )
  {
    if ( ( aSubIDs == null ) || ( aSubIDs.length < 1 ) )
    {
      throw new IllegalArgumentException( "Need at least one sub identifier!" );
    }
    this.subIDs = aSubIDs;
    this.relative = aRelative;
  }

  /**
   * Creates a new {@link AsnOID} instance.
   * 
   * @param aRelative
   *          <code>true</code> if this OID is a relative object identifier,
   *          <code>false</code> if it is an (absolute) object identifier;
   * @param aOID
   *          the string OID to parse, cannot be <code>null</code>.
   */
  public AsnOID( final boolean aRelative, final String aOID )
  {
    if ( ( aOID == null ) || aOID.trim().isEmpty() )
    {
      throw new IllegalArgumentException( "OID cannot be null or empty!" );
    }
    this.relative = aRelative;

    String[] parts = aOID.split( "[. ]" );
    this.subIDs = new int[parts.length];
    for ( int i = 0; i < parts.length; i++ )
    {
      this.subIDs[i] = Integer.parseInt( parts[i] );
    }
  }

  /**
   * Creates a new, absolute, {@link AsnOID} instance.
   * 
   * @param aSubIDs
   *          the sub identifiers of this OID, cannot be <code>null</code> or
   *          empty.
   * @throws IllegalArgumentException
   *           in case the given sub identifiers were <code>null</code> or
   *           empty.
   */
  public AsnOID( final int... aSubIDs )
  {
    this( false, aSubIDs );
  }

  /**
   * Creates a new, absolute, {@link AsnOID} instance.
   * 
   * @param aOID
   *          the string OID to parse, cannot be <code>null</code>.
   */
  public AsnOID( final String aOID )
  {
    this( false, aOID );
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
    if ( ( aObject == null ) || !( aObject instanceof AsnOID ) )
    {
      return false;
    }

    final AsnOID other = ( AsnOID )aObject;
    if ( this.relative != other.relative )
    {
      return false;
    }
    if ( !Arrays.equals( this.subIDs, other.subIDs ) )
    {
      return false;
    }

    return true;
  }

  /**
   * Returns the sub identifiers.
   * 
   * @return an array of sub identifiers, never <code>null</code>.
   */
  public int[] getSubIDs()
  {
    return Arrays.copyOf( this.subIDs, this.subIDs.length );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = ( prime * result ) + ( this.relative ? 1231 : 1237 );
    result = ( prime * result ) + Arrays.hashCode( this.subIDs );
    return result;
  }

  /**
   * Returns whether or not this OID is a relative identifier.
   * 
   * @return <code>true</code> if this OID is relative, <code>false</code> if it
   *         is absolute.
   */
  public boolean isRelative()
  {
    return this.relative;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    for ( int subID : this.subIDs )
    {
      if ( sb.length() > 0 )
      {
        sb.append( " " );
      }
      sb.append( subID );
    }
    return sb.toString();
  }
}
