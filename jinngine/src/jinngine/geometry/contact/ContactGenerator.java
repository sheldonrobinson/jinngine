/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.geometry.contact;

import jinngine.geometry.Geometry;
import jinngine.math.Vector3;

/**
 * A contact generator is used by the {@link ContactConstraint} to generate its contact information. ContactGenerator 
 * encapsulates all geometry and algorithm specific details. Therefore, it is the responsibility 
 * of the simulator (Model) to instantiate correct implementations of ContactGenerators, when various types of geometries 
 * in proximity are encountered during simulation. A ContactConstraint can handle one or more ContactGenerators. This allows for bodies in the simulation, which have more 
 * than one geometry instance attached.
 */
public interface ContactGenerator {
	
	/** 
	 * Interface for obtaining contact points
	 */
	public interface Result {
		public void contactPoint( final Vector3 normal, final Vector3 pa, final Vector3 pb, final double error );
	}
	
	/**
	 * Run/update contact generation. This method will be invoked by the ContactConstraint,
	 * to update the contact information, to reflect the present configuration. An implementation
	 * should run all necessary computations during this call, and generate all contact points, as
	 * to be returned by getContacts().
	 */
	public void run( Result result );
			
	
	/**
	 * Called when this ContactGenerator is being removed from the {@link ContactConstraint} (or some other user). 
	 * This method can be used to perform clean-up if needed.
	 */
	public void remove();

	
	/**
	 * Return the first geometry
	 */
	public Geometry getFirstGeoemtry();

	
	/**
	 * Return the second geometry
	 */
	public Geometry getSecondGeometry();

	
	/**
	 * Return the envelope
	 */
	public double getEnvelope();
	
	/**
	 * Return the principal contact normal
	 */
	public void getContactNormal( Vector3 normal );
}
