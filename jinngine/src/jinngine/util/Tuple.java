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

public class Tuple<T,U> {
	public  T first;
	public  U second;
	public Tuple(T first, U second) {
		super();
		this.first = first;
		this.second = second;
	}
	
	  @SuppressWarnings("unchecked")
	public boolean equals( Object other ) {
		    return this.first.hashCode() == ((Tuple<T,U>)other).first.hashCode()
		    && this.second.hashCode() == ((Tuple<T,U>)other).second.hashCode();
	  }
}
