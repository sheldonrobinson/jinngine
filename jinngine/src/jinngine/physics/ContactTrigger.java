/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.physics;

import java.util.Iterator;
import java.util.ListIterator;

import jinngine.math.Vector3;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.solver.Solver;
import jinngine.physics.solver.Solver.constraint;

public class ContactTrigger implements Trigger {

	private final Body body;
	private final double forcethreshold;
	private final TriggerCallback<ContactTrigger> callback;
	private boolean isAbove = false;
	private boolean fireEvent = false;
	
	/**
	 * Create new contact trigger
	 * @param body Body to monitor
	 * @param forcethreshold the total normal force excerted by the contact in last time-step
	 */
	public ContactTrigger( Body body, double forcethreshold, TriggerCallback<ContactTrigger> callback ) {
		this.body = body;
		this.forcethreshold = forcethreshold;
		this.callback = callback;
	}
	
	@Override
	public void update(Scene s) {
		final double timestep = s.getTimestep();
		Iterator<Constraint> iter = s.getConstraints(body);
		while (iter.hasNext()) {
			iter.next().getNcpConstraints(new ListIterator<Solver.constraint>() {
				double totalforce = 0;		

				// this is called for each ncp constraint definition
				public final void add(constraint arg0) {	
					// add up force
					totalforce += Math.abs(arg0.lambda)/timestep;
					
					// check condition
					if (totalforce > forcethreshold) {
						isAbove = true; fireEvent = false;
					}
				}
				
				// unused methods
				public final boolean hasNext() {return false;}
				public final boolean hasPrevious() {return false;}
				public final constraint next() {return null;}
				public final int nextIndex() {return 0;}
				public final constraint previous() {return null;}
				public final int previousIndex() {return 0;}
				public final void remove() {}
				public void set(constraint e) {}		
			});
		}
	}
	
	public boolean isAbloveThreshold() {
		return isAbove;		
	}

}
