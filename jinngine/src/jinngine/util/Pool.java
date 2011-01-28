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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class Pool<T> implements Iterator<T>, Iterable<T> {

	private final Factory<T> factory;
	private final List<T> list;
	private boolean allocate = false;
	
	private int position = 0;
	private int size = 0;
	
	public interface Factory<T> {
		public T getNewInstance();
	}
		
	public Pool( List<T> list, Factory<T> factory) {
		this.list = list;
		this.factory = factory;
	}
	
	@Override
	public final boolean hasNext() {
		if (allocate) {
			return true;
		} else {
			return position<size;//iter.hasNext();
		}
	}
	
	@Override
	public final T next() {
		if (allocate) {
			if (position<size) {
				T e = list.get(position);
				position++;
				return e;
			} else {
				T instance = factory.getNewInstance();
				list.add(instance);
				position++;
				size++;
				return instance;
			}
		} else {
			if (position<size) {
				T e = list.get(position);
				position++;
				return e;				
			} else {
				return null;
			}
		}
	}

	@Override
	public final void remove() {
		// not allowed
		throw new UnsupportedOperationException();
	}

		
	public final void reverse() {
		Collections.reverse(list);
	}
	
	public final List<T> getList() {
		return list;
	}
	
	public final Iterator<T> insert() {
		this.allocate = true;
//		this.iter = list.listIterator();
		position=0;
		return this;
	}
	
	/**
	 * Clear the remaining elements after a call to {@link insert()} 
	 */
	public final void done() {
		// clear remaining entries (from last call to fill)
		if (this.allocate) {
			if (position<size) {
				for (int i=size-1; i>=position; i--)
					list.remove(i);
				size = list.size();
			}
		} else {
			throw new IllegalAccessError("Pool: call to clearRemaining() without previous call to overwrite()");
		}		
	}
	
	public final Iterator<T> iterator() {
		this.allocate = false;
//		this.iter = list.listIterator();
		position=0;
		return this;
	}
	
	public final int size() {
		return size;
	}
}
