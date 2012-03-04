/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn;


import java.io.*;
import java.util.*;

import nl.lxtreme.asn.AsnSequence.AsnSequenceType;


/**
 * Provides a builder for {@link AsnSequence}.
 */
public final class AsnSequenceBuilder
{
  // VARIABLES

  private AsnIdentifier mainType = new AsnIdentifier( AsnClass.UNIVERSAL, true, AsnType.SEQUENCE );

  private final List<AsnSequenceType> types = new ArrayList<AsnSequenceType>();

  // METHODS

  /**
   * Adds a given boolean to the sequence.
   * 
   * @param aBoolean
   *          the boolean value to add.
   * @return this builder.
   */
  public AsnSequenceBuilder addBoolean( final boolean aBoolean )
  {
    this.types.add( new AsnSequenceType( new AsnIdentifier( AsnType.BOOLEAN ), aBoolean ) );
    return this;
  }

  /**
   * Adds a given boolean context value to the sequence.
   * 
   * @param aIndex
   *          the index of the context value;
   * @param aValues
   *          the boolean value to add.
   * @return this builder.
   */
  public AsnSequenceBuilder addContextValue( final int aIndex, final boolean aValue )
  {
    this.types.add( new AsnSequenceType( new AsnIdentifier( AsnClass.CONTEXT_SPECIFIC.getMask() | aIndex ), aValue ) );
    return this;
  }

  /**
   * Adds a given context value to the sequence.
   * 
   * @param aIndex
   *          the index of the context value;
   * @param aValues
   *          the value to add.
   * @return this builder.
   */
  public AsnSequenceBuilder addContextValue( final int aIndex, final byte[] aValue )
  {
    this.types.add( new AsnSequenceType( new AsnIdentifier( AsnClass.CONTEXT_SPECIFIC.getMask() | aIndex ), aValue ) );
    return this;
  }

  /**
   * Adds a given integer context value to the sequence.
   * 
   * @param aIndex
   *          the index of the context value;
   * @param aValues
   *          the integer value to add.
   * @return this builder.
   */
  public AsnSequenceBuilder addContextValue( final int aIndex, final int aValue )
  {
    this.types.add( new AsnSequenceType( new AsnIdentifier( AsnClass.CONTEXT_SPECIFIC.getMask() | aIndex ), aValue ) );
    return this;
  }

  /**
   * Adds a given integer to the sequence.
   * 
   * @param aValue
   *          the integer value to add.
   * @return this builder.
   */
  public AsnSequenceBuilder addInt( final int aValue )
  {
    this.types.add( new AsnSequenceType( new AsnIdentifier( AsnType.INTEGER ), aValue ) );
    return this;
  }

  /**
   * Adds a given string to the sequence.
   * 
   * @param aOctets
   *          the octets of the string to add, cannot be <code>null</code>.
   * @return this builder.
   */
  public AsnSequenceBuilder addString( final byte[] aOctets )
  {
    this.types.add( new AsnSequenceType( new AsnIdentifier( AsnType.OCTET_STRING ), aOctets ) );
    return this;
  }

  /**
   * Adds a given ISO8859-1 encoded string to the sequence.
   * 
   * @param aString
   *          the string to add, cannot be <code>null</code>.
   * @return this builder.
   * @throws UnsupportedEncodingException
   *           in case the running platform doesn't support ISO8859-1 encodings.
   */
  public AsnSequenceBuilder addString( final String aString ) throws UnsupportedEncodingException
  {
    addString( aString.getBytes( "8859_1" ) );
    return this;
  }

  /**
   * Adds a given UTF-8 encoded string to the sequence.
   * 
   * @param aString
   *          the string to add, cannot be <code>null</code>.
   * @return this builder.
   * @throws UnsupportedEncodingException
   *           in case the running platform doesn't support UTF-8 encodings.
   */
  public AsnSequenceBuilder addUTF8String( final String aString ) throws UnsupportedEncodingException
  {
    addString( aString.getBytes( "UTF8" ) );
    return this;
  }

  /**
   * Sets the main type of the sequence.
   * 
   * @param aMainType
   *          a main type, cannot be <code>null</code>.
   * @return this builder.
   * @throws IllegalArgumentException
   *           in case the given type is not a constructed type.
   */
  public AsnSequenceBuilder setMainType( final AsnIdentifier aMainType )
  {
    if ( !aMainType.isConstructed() )
    {
      throw new IllegalArgumentException( "Main type must be of a constructed type!" );
    }
    this.mainType = aMainType;
    return this;
  }

  /**
   * @return a new {@link AsnSequence} instance, never <code>null</code>.
   */
  public AsnSequence toSequence()
  {
    final AsnSequence result;
    synchronized ( this.types )
    {
      result = new AsnSequence( this.mainType, this.types );
      this.types.clear();
    }
    return result;
  }
}
