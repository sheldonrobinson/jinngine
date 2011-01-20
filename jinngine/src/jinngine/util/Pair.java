/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */
package jinngine.util;

/** 
 * Small pair class, for the purpose of indexing unordered pairs in a hash table
 */
public final class Pair<T>  {
  private final T o1;
  private final T o2;

  public Pair(T o1, T o2) {
	  this.o1 = o1;
	  this.o2 = o2;
  }
    
  //since this is an unordered pair, we use
  //the same hash code for interchanced objects
  @Override
  public final int hashCode() {
	  return o1.hashCode()*o2.hashCode();
  }

  @SuppressWarnings("unchecked")
  @Override
  public final boolean equals( Object other ) {
	  if (this == other) return true;
	  if (other == null ) return false;
	  if ( other instanceof Pair) {
		  final Pair<T> otherpair = (Pair<T>)other;
		  return ( ( o1.equals( otherpair.o1 ) && o2.equals( otherpair.o2 ) )
				  || ( o1.equals( otherpair.o2 ) && o2.equals( otherpair.o1 ) ) );
	  } else {
		  return false;
	  }
  }
  
  public final T getFirst()  { return o1;}
  public final T getSecond() { return o2;}
  
  public final boolean contains(T o) {
    return (o==o1||o==o2);
  }

}
