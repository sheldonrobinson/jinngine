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
import java.util.ListIterator;

public final class Pool<T> implements Iterator<T>, Iterable<T> {

	private final Factory<T> factory;
	private final List<T> list;
	private ListIterator<T> iter;
	private boolean allocate = false;
	
	public interface Factory<T> {
		public T getNewInstance();
	}
		
	public Pool( List<T> list, Factory<T> factory) {
		this.list = list;
		this.factory = factory;
		// initially create the iterator
		this.iter = list.listIterator();
	}
	
//	public final Iterator<T> start() {		
//		// rewind iterator
//		this.iter = list.listIterator();
//		return this;
//	}

	@Override
	public final boolean hasNext() {
		if (allocate) {
			return true;
		} else {
			return iter.hasNext();
		}
	}
	
	@Override
	public final T next() {
		if (allocate) {
			if (iter.hasNext()) {
				return iter.next();
			} else {
				T instance = factory.getNewInstance();
				iter.add(instance);
				return instance;
			}
		} else {
			return iter.next();
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
		this.iter = list.listIterator();
		return this;
	}
	
	/**
	 * Clear the remaining elements after a call to {@link insert()} 
	 */
	public final void done() {
		// clear remaining entries (from last call to fill)
		if (this.allocate) {
			while (iter.hasNext()) {
				iter.next();
				iter.remove();
			}
		} else {
			throw new IllegalAccessError("Pool: call to clearRemaining() without previous call to overwrite()");
		}		
	}
	
	public final Iterator<T> iterator() {
		this.allocate = false;
		this.iter = list.listIterator();
		return this;
	}
	
	public final int size() {
		return this.list.size();
	}
}
