/*
 * LibBER - Small BER transcoding library.
 * 
 * (C) Copyright 2012 - J.W. Janssen <j.w.janssen@lxtreme.nl>
 */
package nl.lxtreme.asn;


import java.util.*;


/**
 * Denotes an ordered sequence of (typed) items.
 */
public class AsnSequence implements Iterable<AsnSequence.AsnSequenceType>
{
  // INNER TYPES

  /**
   * A container for the individual sequence types.
   */
  public static class AsnSequenceType
  {
    // VARIABLES

    private final AsnIdentifier type;
    private final Object value;

    // CONSTRUCTORS

    /**
     * Creates a new {@link AsnSequenceType}.
     */
    public AsnSequenceType( final AsnIdentifier aType, final Object aValue )
    {
      this.type = aType;
      this.value = aValue;
    }

    // METHODS

    /**
     * @return the total length of this sequence type, >= 0.
     */
    public int getLength()
    {
      final int valueLength = determineValueLength();
      final int typeLength = 1; // XXX
      final int lengthLength = 1; // XXX

      return typeLength + lengthLength + valueLength;
    }

    /**
     * @return
     */
    public AsnIdentifier getType()
    {
      return this.type;
    }

    /**
     * @return
     */
    @SuppressWarnings( "unchecked" )
    public <T> T getValue()
    {
      return ( T )this.value;
    }

    /**
     * Determines the length of the value.
     * 
     * @return the length (in bytes) of the contained value, >= 0.
     */
    private int determineValueLength()
    {
      final int valueLength;
      if ( this.value == null )
      {
        valueLength = 0;
      }
      else if ( this.type.getClazz() == AsnClass.UNIVERSAL )
      {
        valueLength = this.type.getLength( this.value );
      }
      else if ( this.type.getClazz() == AsnClass.CONTEXT_SPECIFIC )
      {
        valueLength = AsnType.determineLength( this.value );
      }
      else
      {
        throw new RuntimeException( "Unable to determine length for class " + this.type.getClazz() );
      }
      return valueLength;
    }
  }

  // VARIABLES

  private final AsnIdentifier mainType;

  private final List<AsnSequenceType> seqTypes;

  // CONSTRUCTORS

  /**
   * Creates a new {@link AsnSequence}.
   * 
   * @param aIdentifier
   *          the identifier of this sequence, cannot be <code>null</code>.
   * @throws IllegalArgumentException
   *           in case the given identifier was <code>null</code>, or not of a
   *           constructed type.
   */
  public AsnSequence( final AsnIdentifier aIdentifier, final List<AsnSequenceType> aSeqTypes )
  {
    if ( aIdentifier == null )
    {
      throw new IllegalArgumentException( "Identifier cannot be null!" );
    }
    if ( !aIdentifier.isConstructed() )
    {
      throw new IllegalArgumentException( "Identifier cannot be primitive!" );
    }
    this.mainType = aIdentifier;

    this.seqTypes = new ArrayList<AsnSequenceType>( aSeqTypes );
  }

  /**
   * Creates a new {@link AsnSequence} instance as universal constructed
   * sequence.
   */
  public AsnSequence( final List<AsnSequenceType> aSeqTypes )
  {
    this( new AsnIdentifier( AsnClass.UNIVERSAL, true /* aConstructed */, AsnType.SEQUENCE ), aSeqTypes );
  }

  // METHODS

  /**
   * @return the total number of items in this sequence.
   */
  public int getItemCount()
  {
    return this.seqTypes.size();
  }

  /**
   * @return the total length (in bytes) of this sequence.
   */
  public int getLength()
  {
    int length = 0;
    for ( final AsnSequenceType seqType : this )
    {
      length += seqType.getLength();
    }
    return length;
  }

  /**
   * @return the main type of this sequence, never <code>null</code>.
   */
  public AsnIdentifier getMainType()
  {
    return this.mainType;
  }

  /**
   * @return the individual sequence types, never <code>null</code>
   */
  public List<AsnSequenceType> getSequenceTypes()
  {
    return Collections.unmodifiableList( this.seqTypes );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<AsnSequenceType> iterator()
  {
    final int size = this.seqTypes.size();

    return new Iterator<AsnSequence.AsnSequenceType>()
    {
      volatile int index = -1;

      @Override
      public boolean hasNext()
      {
        return ( this.index < ( size - 1 ) );
      }

      @Override
      public AsnSequenceType next()
      {
        return AsnSequence.this.seqTypes.get( ++this.index );
      }

      @Override
      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }
}
