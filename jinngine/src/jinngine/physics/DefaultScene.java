/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.physics;
import java.util.*;

import jinngine.physics.constraint.*;
import jinngine.physics.constraint.contact.ContactConstraintManager;
import jinngine.physics.solver.*;
import jinngine.physics.solver.Solver.constraint;
import jinngine.collision.*;
import jinngine.geometry.*;
import jinngine.math.*;
import jinngine.physics.force.*;
import jinngine.util.*;
import jinngine.util.ComponentGraph.Component;

/**
 * A basic fixed time stepping rigid body simulator. It uses a contact graph to organise constraints, and generates
 * contact constraints upon intersecting/touching geometries. The engine is limited to having one constraint per. 
 * body pair. This means that if a joint constraint is present, there can be no contact constraints simultaneously. 
 * Such behaviour should be modelled using joint limits. 
 */
public final class DefaultScene implements Scene {
	// bodies in model
	public final List<Body> bodies = new ArrayList<Body>();
	
	// constraints, joints and forces
	public final List<constraint> ncpconstraints = new LinkedList<constraint>();  
	private final List<Force> forces = new LinkedList<Force>(); 
	
	// create a contact graph classifier, used by the contact graph for determining
	// fixed bodies, i.e. bodies considered to have infinite mass. 
	private final ComponentGraph.NodeClassifier<Body> classifier = 
		new ComponentGraph.NodeClassifier<Body>() {
			public boolean isDelimitor(Body node) {
				return node.isFixed();
			}
	};
	
	// create the contact graph using the classifier above
	private final ComponentGraph<Body,Constraint,Boolean> constraintGraph = new HashMapComponentGraph<Body,Constraint,Boolean>(classifier);

	// use sweep and prune as broadphase collision detection
	private final BroadphaseCollisionDetection broadphase;

	// create a ncp solver
	private final Solver solver;
	
	// deactivation policy
	private final DeactivationPolicy policy;
	
	// time-step size
	private double timestep = 0.08; 

	/** 
	 * Create a new fixed time-stepping simulator 
	 * @param broadphase Broadphase collision detection method
	 * @param solver Solver to be used
	 * @param policy TODO
	 */
	public DefaultScene( BroadphaseCollisionDetection broadphase,  Solver solver, DeactivationPolicy policy ) {	
		
		this.broadphase = broadphase;
		this.solver = solver;
		this.policy = policy;
		
		// start the new contact constraint manager
		new ContactConstraintManager( broadphase, constraintGraph);
	}
	
	/**
	 * Create a new fixed time-stepping simulator. 
	 */
	public DefaultScene() {	
		
		// some default choises
		this.policy = new DefaultDeactivationPolicy();
		this.broadphase = new SweepAndPrune();
		this.solver = new ProjectedGaussSeidel(55);
		
		// start the new contact constraint manager
		new ContactConstraintManager( broadphase, constraintGraph);
	}


	@Override
	public final void tick() {
		// clear acting forces and delta velocities
		for (Body c:bodies) {
			c.clearForces();
			c.deltavelocity.assign(Vector3.zero);
			c.deltaomega.assign(Vector3.zero);
		}

        // apply all forces	to delta velocities
		for (Force f: forces) {
			f.apply(timestep);
		}

		// Run the broad-phase collision detection (this automatically updates the contactGraph,
		// through the BroadfaseCollisionDetection.Handler type)
		broadphase.run();
		
		// create a special iterator to be used with constraints. Each constraint will
		// insert its ncp-constraints into this list
		ncpconstraints.clear();
		ListIterator<constraint> constraintIterator = ncpconstraints.listIterator();
				
		// iterate through groups/components in the contact graph
		Iterator<ComponentGraph.Component<Boolean>> components = constraintGraph.getComponents();		
		while (components.hasNext()) {
			// the component 
			ComponentGraph.Component<Boolean> g = components.next();
			
			// check if whole group is inactive
			Iterator<Body> bodyiter =constraintGraph.getNodesInComponent(g);
			boolean activefound = false;
			while (bodyiter.hasNext()) {
				if ( !bodyiter.next().deactivated ) { //!policy.isInactive(bodyiter.next()) ) {
					activefound = true;
					break;
				}
			}
			
			// if there are active bodies in the group, apply constraints
			if (activefound) {
				// apply all constraints in interaction component
				Iterator<Constraint> constraints = constraintGraph.getEdgesInComponent(g);
				while (constraints.hasNext()) {
					Constraint c = constraints.next();
					c.applyConstraints(constraintIterator, timestep);
				} // while
			} // if active found
		} //while groups

		
		// run the solver (compute delta velocities) for all 
		// components in the constraint graph
		solver.solve( ncpconstraints, bodies, 0.0 );
		
		// apply delta velocities and integrate positions forward 
		for (Body body : bodies ) {						
			// apply computed forces to bodies
			if ( !body.isFixed() && !body.deactivated ) {
				// apply delta velocities
				body.state.velocity.assign( body.state.velocity.add( body.deltavelocity));
				body.state.omega.assign( body.state.omega.add( body.deltaomega));
				// update angular and linear momentums
				Matrix3.multiply(body.state.inertia, body.state.omega, body.state.L);
				body.state.P.assign(body.state.velocity.multiply(body.state.mass));
			}

			//integrate forward on positions
			body.advancePositions(timestep);
			
			// change activation state
			if ( policy.shouldBeDeactivated(body)) {
				body.deactivated = true;
			} 
			else if (policy.shouldBeActivated(body)) {
				body.deactivated = false;
			}		
		} // for bodies
	} //time-step


	@Override
	public void addForce( Force f ) {
		forces.add(f);
	}

	@Override
	public void removeForce(Force f) {
		forces.remove(f);
	}

	@Override
	public void addBody( Body c) {
		bodies.add(c);
		c.updateTransformations();
		
		//install geometries into the broad-phase collision detection
		Iterator<Geometry> i = c.getGeometries();
		while (i.hasNext()) {
			Geometry g = i.next();
			broadphase.add(g);
		}
	}
	
	@Override
	public void addConstraint(Constraint joint) {
		constraintGraph.addEdge(joint.getBodies(), joint);
	}
	
	@Override
	public Iterator<Constraint> getConstraints() {
		List<Constraint> list = new ArrayList<Constraint>();
		Iterator<Component<Boolean>> ci = constraintGraph.getComponents();
		while(ci.hasNext()) {
			Iterator<Constraint> ei = constraintGraph.getEdgesInComponent(ci.next());
			while(ei.hasNext())
				list.add(ei.next());
		}
			
		return list.iterator();
	}
	
	public final void removeConstraint(Constraint c) {
		if (c!=null) {
			constraintGraph.removeEdge(c.getBodies());
		} else {
			System.out.println("Engine: attempt to remove null constraint");
		}
	}
	
	@Override
	public final void removeBody(Body body) {
		//remove associated geometries from collision detection
		Iterator<Geometry> i = body.getGeometries();
		while( i.hasNext()) {
			broadphase.remove(i.next());			
		}
		
		//finally remove from body list
		bodies.remove(body);
		
	}

	@Override
	public Iterator<Body> getBodies() {
		return bodies.iterator();
	}
	
	@Override
	public void setTimestep(double dt) {
		this.timestep = dt;
	}

	@Override
	public void fixBody(Body b, boolean fixed) {
		// this may seem a bit drastic, but it is necessary. If one
		// just changes the fixed setting directly on bodies during animation,
		// really bad thing will happen, because the contact graph will become
		// corrupted and will eventually crash jinngine
		
		//check if the body is in the animation
		if (!bodies.contains(b))
			return;
		
		// check if body is already the at the correct 
		// fixed setting, in which case do nothing
		if (b.isFixed() == fixed) 
			return;
		
		// remove the body from simulation 
		removeBody(b);

		//change the fixed setting
		b.setFixed(fixed);

		// reinsert body
		addBody(b);
		
	}

}
