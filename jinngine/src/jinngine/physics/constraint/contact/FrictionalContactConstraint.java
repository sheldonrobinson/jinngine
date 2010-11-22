/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.physics.constraint.contact;

import java.util.*;

import jinngine.geometry.contact.*;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.Solver.NCPConstraint;
import jinngine.util.GramSchmidt;
import jinngine.util.Pair;


/**
 * A constraint the models a contact point between two bodies. A ContactConstraint acts 
 * like any other constraint/joint, for instance {@link BallInSocketJoint}. ContactConstraint uses one ore more
 * {@link ContactGenerator} instances to supply contact points and contact normals of the involved geometries. 
 * When two bodies are subject to a contact constraint, a ContactGenerator for each interacting geometry pair is required. 
 * Determining and instantiating these ContactGenerators should be handled by the simulator itself, however, one can create new 
 * and possibly optimised ContactGenerators for certain geometry pairs. A trivial example would be a ContactGenerator
 * for the Sphere-Sphere case, which is already implemented in Jinngine.
 *
 * @author mo
 *
 */
public final class FrictionalContactConstraint implements ContactConstraint {	
	private final Body b1, b2;                  //bodies in constraint
	private final List<ContactGenerator> generators = new ArrayList<ContactGenerator>();
	private final List<NCPConstraint>       ncpconstraints = new ArrayList<NCPConstraint>();
	private double frictionBoundMagnitude = Double.POSITIVE_INFINITY;
	
	// vectors spanning the contact space
	private final Vector3 t1 = new Vector3(), t2 = new Vector3(), t3 = new Vector3();

	
	private boolean enableCoupling = true;
	
	
	
	/**
	 * Create a new ContactConstraint, using one initial ContactGenerator
	 * @param b1
	 * @param b2
	 * @param generator
	 */
	public FrictionalContactConstraint(Body b1, Body b2, ContactGenerator generator) {
		super();
		this.b1 = b1;
		this.b2 = b2;
		this.generators.add(generator);
	}
	
	/**
	 * Add a new ContactGenerator for generating contact points and normal vectors
	 * @param g a new ContactGenerator
	 */
	public final void addGenerator(ContactGenerator g) {
		this.generators.add(g);
	}
	
	/**
	 * Remove a contact generator
	 * @param g Previously added contact generator to be removed from this contact constraint
	 */
	public final void removeGenerator(ContactGenerator g) {
		this.generators.remove(g);
	}
	
	/**
	 * Return the number of contact point generators
	 * @return
	 */
	public final double getNumberOfGenerators() {
		return generators.size();
	}
	
	private final NCPConstraint getNextNCPConstraint( ListIterator<NCPConstraint> internal, ListIterator<NCPConstraint> external) {
		if (internal.hasNext()) {
			NCPConstraint ci = internal.next();
			external.add(ci);
			return ci;
		} else {
			NCPConstraint ci = new NCPConstraint();
			internal.add(ci);
			external.add(ci);
			return ci;
		}
	}
	
	@Override
	public final void applyConstraints(ListIterator<NCPConstraint> constraintIterator, double dt) {
		// start iterator for the internal list of constraints
		ListIterator<NCPConstraint> internalConstraints = ncpconstraints.listIterator();
		
		// use ContactGenerators to create new contactpoints
		for ( ContactGenerator cg: generators) {
			//run contact generator
			cg.run();
			
			// generate contacts
			Iterator<ContactGenerator.ContactPoint> i = cg.getContacts();
			while (i.hasNext()) {
				ContactGenerator.ContactPoint cp = i.next();			
				createFrictionalContactConstraint(cp, b1, b2, cp.point, cp.normal, cp.depth, dt, internalConstraints, constraintIterator);				
			}
		}
		
		// clear remaining constraints from the internal list
		while (internalConstraints.hasNext()) {
			internalConstraints.next();
			internalConstraints.remove();
		}
		
	}

	// create a regular contact constraint including tangential friction
	public final void createFrictionalContactConstraint( 
			ContactGenerator.ContactPoint cp,
			Body b1, Body b2, Vector3 p, Vector3 n, double depth, double dt,
			ListIterator<NCPConstraint> internalConstraints, ListIterator<NCPConstraint> outConstraints 
	) {		

		// use a gram-schmidt process to create a orthonormal basis for the contact point ( normal and tangential directions)
		t1.assign(n); GramSchmidt.run(t1,t2,t3);

		// get the next ncp constraint in local constraint list
		NCPConstraint t1constraint = getNextNCPConstraint(internalConstraints,outConstraints);

		// assign jacobian and diagonal
		assignJacobian(b1, b2, t1, p, t1constraint);

		// first off, create the constraint in the normal direction
		final double e = cp.restitution; // coefficient of restitution
		final double uni = t1constraint.j1.dot(b1.state.velocity) 
		                 + t1constraint.j2.dot(b1.state.omega) 
		                 + t1constraint.j3.dot(b2.state.velocity) 
		                 + t1constraint.j4.dot(b2.state.omega);
		final double unf = uni<0 ? -e*uni: 0;		

		// external forces acing at contact (obsolete, external forces are modelled using the delta velocities)
		// double Fext = B1.dot(b1.state.force) + B2.dot(b1.state.torque) + B3.dot(b2.state.force) + B4.dot(b2.state.torque);
		double correction = depth*(1/dt); // the true correction velocity. This velocity corrects the contact in the next timestep.
		final double escape = (cp.envelope-cp.distance)*(1/dt);
		final double lowerNormalLimit = 0;
		final double limit = 2;
		
		// if the unf velocity will make the contact leave the envelope in the next timestep, 
		// we ignore corrections
		if (unf > escape) {
			//System.out.println("escape");
			correction = 0;
		} else {
			//even with unf, we stay inside the envelope
			//truncate correction velocity if already covered by repulsive velocity
			if (correction > 0) {
				if (unf > correction ) {
					correction = 0;
				} else {
					correction = correction - unf; // not sure this is smart TODO
				}
			}
		}
	
		// limit the correction velocity
		correction = correction< -limit? -limit:correction;  
		correction = correction>  limit?  limit:correction;
		
		// take a factor of real correction velocity
		correction = correction * 0.9;
		
		// assign constraint settings
		t1constraint.body1 = b1;
		t1constraint.body2 = b2;
		t1constraint.lower = lowerNormalLimit;
		t1constraint.upper = Double.POSITIVE_INFINITY;
		t1constraint.coupling = null;
		t1constraint.b = -(unf-uni)-correction;
		t1constraint.c = -correction;
		t1constraint.lambda = 0;
		
		// set distance (unused in simulator)
		t1constraint.distance = cp.distance;
		
		// normal-friction coupling 
		final NCPConstraint coupling = enableCoupling?t1constraint:null;
		
		// set the correct friction setting for this contact
		t1constraint.mu = cp.friction;
						
		// first tangent
		NCPConstraint t2constraint = getNextNCPConstraint(internalConstraints,outConstraints);

		// assign jacobian and diagonal
		assignJacobian( b1, b2, t2, p, t2constraint);
		
		// initial and final velocitu of the first tangent 
		final double ut1i = t2constraint.j1.dot(b1.state.velocity) + t2constraint.j2.dot(b1.state.omega) + t2constraint.j3.dot(b2.state.velocity) + t2constraint.j4.dot(b2.state.omega);
		final double ut1f = 0;
				
		// assign constraint settings
		t2constraint.body1 = b1;
		t2constraint.body2 = b2;
		t2constraint.lower = -frictionBoundMagnitude;
		t2constraint.upper = frictionBoundMagnitude;
		t2constraint.coupling = coupling;
		t2constraint.b = -(ut1f-ut1i);
		t2constraint.c = 0;
		t2constraint.lambda = 0;

		// get second tangent constraint
		NCPConstraint t3constraint = getNextNCPConstraint(internalConstraints,outConstraints);

		// assign jacobian and diagonal
		assignJacobian( b1, b2, t3, p, t3constraint);

		// initial and final velocity
		final double ut2i = t3constraint.j1.dot(b1.state.velocity) + t3constraint.j2.dot(b1.state.omega) + t3constraint.j3.dot(b2.state.velocity) + t3constraint.j4.dot(b2.state.omega); 
		final double ut2f = 0;
		
		// assign constraint settings
		t3constraint.body1 = b1;
		t3constraint.body2 = b2;
		t3constraint.lower = -frictionBoundMagnitude;
		t3constraint.upper =  frictionBoundMagnitude;
		t3constraint.coupling = coupling;
		t3constraint.b = -(ut2f-ut2i);
		t3constraint.c = 0;
		t3constraint.lambda = 0;
	}
	
	
	/**
	 * Assign the jacobian of the given constraint
	 */
	private final void assignJacobian( Body b1, Body b2, Vector3 normal, Vector3 point, NCPConstraint constraint ) {
		// shorthands for the data
		final Vector3 J1 = constraint.j1;
		final Vector3 J2 = constraint.j2;
		final Vector3 J3 = constraint.j3;
		final Vector3 J4 = constraint.j4;
		final Vector3 B1 = constraint.b1;
		final Vector3 B2 = constraint.b2;
		final Vector3 B3 = constraint.b3;
		final Vector3 B4 = constraint.b4;
		final Matrix3 I1 = b1.state.inverseinertia;
		final Matrix3 M1 = b1.state.inverseanisotropicmass;
		final Matrix3 I2 = b2.state.inverseinertia;
		final Matrix3 M2 = b2.state.inverseanisotropicmass;		

		// jacobians for normal direction
		// j1 = n
		J1.assign(normal);
		// j2 = r1 x n
		J2.assign(point);
		J2.assignSub(b1.state.position);
		J2.assignCross(normal);
		// j3 = -n
		J3.assign(normal);
		J3.assignNegate();
		// j4 = - r2 x n = n x r2
		J4.assign(b2.state.position);
		J4.assignSub(point);
		J4.assignCross(normal);

		// compute (M^-1)(J^T) vector
		// clear out B's if mass is "infinity"
		if (b1.isFixed()) { 
			B1.assignZero(); 
			B2.assignZero(); 
		} else {
			Matrix3.multiply( M1, J1, B1);
			Matrix3.multiply( I1, J2, B2);
		}

		if (b2.isFixed()) { 
			B3.assignZero(); 
			B4.assignZero(); 
		} else {
			Matrix3.multiply( M2, J3, B3);
			Matrix3.multiply( I2, J4, B4);			
		}
		
		// assign diagonal
		constraint.diagonal = constraint.j1.dot(constraint.b1) 
        + constraint.j2.dot(constraint.b2) 
        + constraint.j3.dot(constraint.b3) 
        + constraint.j4.dot(constraint.b4);
	}
	
	

	@Override
	public final Pair<Body> getBodies() {
		return new Pair<Body>(b1,b2);
	}

	/**
	 * Specify whether a normal force magnitude coupling should be used on the friction force bounds.
	 * If not enabled, the bounds will be fixed.
	 * @param coupling
	 */
	public final void setCouplingEnabled( boolean coupling ) {
		this.enableCoupling = coupling;
	}
	
	/**
	 * Set the limits for fixed bound friction
	 * @param magnitude
	 */
	public final void setFixedFrictionBoundsMagnitude( double magnitude) {
		this.frictionBoundMagnitude  = magnitude;
	}

	@Override
	public final Iterator<NCPConstraint> getNcpConstraints() {
		return ncpconstraints.iterator();
	}

	@Override
	public final Iterator<ContactGenerator> getGenerators() {
		return generators.iterator();
	}
	
}
