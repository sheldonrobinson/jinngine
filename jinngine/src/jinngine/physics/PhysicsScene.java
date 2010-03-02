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

import jinngine.physics.Body;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.force.Force;

/**
 *  Interface for creating a new physics scene. One should implement this interface, and pass it onto some 
 *  class that is able to generate the scene. This could be a Collada loader.   
 */
public interface PhysicsScene {

	/**
	 * Get an iterator for all bodies in the Scene
	 * @return An iterator over bodies
	 */
	public Iterator<Body> getBodies(); 
	
	/**
	 * Get all constraints in the scene. This includes ContactConstraint instances
	 * @return An iterator over constraints
	 */ 
	public Iterator<Constraint> getConstraints();

	/**
	 * Add a body to the scene
	 * @param b
	 */
	public void addBody( Body b );
	
	/**
	 * Add a force to the scene
	 * @param f
	 */
	public void addForce( Force f );
	
	/**
	 * Add a constraint to the scene
	 * @param c
	 */
	public void addConstraint( Constraint c );
	
	/**
	 * Remove a body from the scene
	 * @param b
	 */
	public void removeBody( Body b );
	
	/** 
	 * Remove a force from the scene
	 * @param f
	 */
	public void removeForce( Force f );
	
	/**
	 * Remove a constraint from the scene
	 * @param c
	 */
	public void removeConstraint( Constraint c );
	
	/** 
	 * Make a body become fixed, during animation
	 */
	public void fixBody( Body b, boolean fixed );
	
}
