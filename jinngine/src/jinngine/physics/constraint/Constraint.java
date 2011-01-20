/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */
package jinngine.physics.constraint;

import java.util.Iterator;
import java.util.ListIterator;
import jinngine.physics.Body;
import jinngine.physics.solver.Solver.NCPConstraint;
import jinngine.util.Pair;

public interface Constraint {
	
	/**
	 * Insert the ConstraintEntries of this Constraint into the list modelled by iterator
	 * @param iterator
	 * @param dt
	 */
	public void applyConstraints( ListIterator<NCPConstraint> iterator, double dt );

	/**
	 * Get the NCP constraints associated with this Constraint 
	 * @param constraints
	 */
	public Iterator<NCPConstraint> getNcpConstraints(); 
	
	/**
	 * Return the pair of bodies that this constraint is acting upon 
	 */
	public Pair<Body> getBodies();

}
