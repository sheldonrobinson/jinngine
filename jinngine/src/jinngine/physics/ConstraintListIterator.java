package jinngine.physics;

import java.util.ListIterator;
import jinngine.physics.solver.*;


public class ConstraintListIterator implements ListIterator<ConstraintEntry> {

	private final ListIterator<ConstraintEntry> iterator;
	private int lastIndex=0;
	
	public ConstraintListIterator( ListIterator<ConstraintEntry> iterator) {
		this.iterator = iterator;
	}
	
	@Override
	public void add(ConstraintEntry e) {
		if (iterator.hasNext())
			iterator.next().assign(e);
		else {
			//System.out.println("created new");
			iterator.add(e);
		}
		//iterator.add(e);
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public boolean hasPrevious() {
		return iterator.hasPrevious();
	}

	@Override
	public ConstraintEntry next() {		
		if (!iterator.hasNext()) {
			
			ConstraintEntry c = new ConstraintEntry();
			c.index = lastIndex;
			lastIndex = lastIndex + 1;
			iterator.add(c);
			return c;
		}
		return iterator.next();	
	}

	@Override
	public int nextIndex() {
		return iterator.nextIndex();
	}

	@Override
	public ConstraintEntry previous() {
		return iterator.previous();
	}

	@Override
	public int previousIndex() {
		return iterator.previousIndex();
	}

	@Override
	public void remove() {
		iterator.remove();
	}
	
	public void removeRemaining() {
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			lastIndex = lastIndex - 1;
		}
	}

	@Override
	public void set(ConstraintEntry e) {
		iterator.set(e);
	}

}
