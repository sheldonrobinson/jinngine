package jinngine.physics;
import java.util.*;

import jinngine.physics.solver.*;
import jinngine.geometry.contact.*;
import jinngine.collision.*;
import jinngine.geometry.*;
import jinngine.math.*;
import jinngine.physics.force.*;
import jinngine.physics.constraint.*;
import jinngine.util.*;

/**
 * A fixed time stepping rigid body simulator. It uses a contact graph to organise constraints, and generates
 * contact constraints upon intersecting/touching geometries. The engine is limited to having one constraint per. 
 * body pair. This means that if a joint constraint is present, there can be no contact constraints simultaneously. Such behaviour 
 * should be modelled using joint limits. 
 * 
 * @author mo
 */
public final class Engine implements Model {
	// Bodies in model
	public final List<Body> bodies = new ArrayList<Body>();
	
	// List of geometry classifiers
	private final List<ContactGeneratorClassifier> geometryClassifiers = new ArrayList<ContactGeneratorClassifier>();

	// Constraints, joints and forces
	private final List<ConstraintEntry> constraintList = new LinkedList<ConstraintEntry>();  
	private final List<Force> forces = new LinkedList<Force>(); 

	
	private final List<ConstraintEntry> normals = new ArrayList<ConstraintEntry>();
	private final List<ConstraintEntry> frictions = new ArrayList<ConstraintEntry>();


	// Pairs not to be considered by contact constraints
	private final Map<Pair<Body>,Boolean> mutedBodyPairs = new HashMap<Pair<Body>,Boolean>();

	public int method = 1;
	int tick = 0;
	double accumen;
	double accumef;
	int totalinner = 0;
	public static double energy;
	
	// Create a contact graph classifier, used by the contact graph for determining
	// fixed bodies, i.e. bodies considered to have infinite mass. 
	private final ComponentGraph.NodeClassifier<Body> classifier = 
		new ComponentGraph.NodeClassifier<Body>() {
			public boolean isDelimitor(Body node) {
				return node.isFixed();
			}
	};

	// Create the contact graph using the classifier above
	private final ComponentGraph<Body,Constraint> contactGraph = new ComponentGraph<Body,Constraint>(classifier);

	// Set of maintained contact constraints
	private final Map<Pair<Body>,ContactConstraint> contactConstraints = new HashMap<Pair<Body>,ContactConstraint>();
	
	// Setup a broad-phase handler. The handler ensures that ContactConstraints are properly inserted and 
	// removed from the contact graph, whenever the broad-phase collision detection detects overlaps and separations
	private final BroadfaseCollisionDetection.Handler handler = 
		new BroadfaseCollisionDetection.Handler() {
			@Override
			public final void overlap(Pair<Geometry> pair) {
				//retrieve the bodies associated with overlapping geometries
				Body a = pair.getFirst().getBody();
				Body b = pair.getSecond().getBody();
				Pair<Body> bpair = new Pair<Body>(a,b);
				//ignore overlaps stemming from the same body				
				if ( a == b) return;
				
				//ignore overlaps for non-body geometries
				if ( a == null || b == null) return;
				
				//ignore overlaps of fixed bodies
				if ( a.isFixed() && b.isFixed() ) return;

				ContactConstraint constraint;

				//a contact constraint already exists 
				if (contactConstraints.containsKey(bpair)) {
					constraint = contactConstraints.get(bpair);
					
					//add a new contact generator to this contact constraint
					constraint.addGenerator(getContactGenerator(pair));
					
				//no contact constraint is present
				} else {
					//System.out.println("contact");
					
					//do not act if some other constraint(joint) is already present
					if (!contactGraph.alledges.containsKey(bpair))  {

						//create a new contact constraint
						constraint = new ContactConstraint(a,b,getContactGenerator(pair));
						
						//insert into data structures
						contactConstraints.put(bpair, constraint);
						contactGraph.addEdge( bpair, constraint);
					}
				}
			}
			@Override
			public final void separation(Pair<Geometry> pair) {
				//System.out.println("Contact removed");
				
				//retrieve the bodies associated with overlapping geometries
				Body a = pair.getFirst().getBody();
				Body b = pair.getSecond().getBody();
				Pair<Body> bpair = new Pair<Body>(a,b);

				//ignore overlaps stemming from the same body				
				if ( a == b) return;
				//ignore overlaps for non-body geometries
				if ( a == null || b == null) return;

				
				//if this geometry pair had an acting contact constraint,
				//remove it
				if (contactConstraints.containsKey(bpair)) {
					contactConstraints.remove(bpair);
					contactGraph.removeEdge(bpair);					
				}

				
				
			}
	};
	
	// Broad phase collision detection implementation 
	private BroadfaseCollisionDetection broadfase = new SweepAndPrune(handler);
//	private BroadfaseCollisionDetection broadfase = new AllPairsTest(handler);

	//Create a linear complementarity problem solver
	private Solver solver = new ProjectedGaussSeidel();
	
	//time-step size
	public double dt = 0.01f; 

	/**
	 * 
	 */
	public Engine() {
		//create some initial ContactGeneratorClassifiers

		//The sphere-sphere classifier
		geometryClassifiers.add(new ContactGeneratorClassifier() {
			@Override
			public ContactGenerator createGeneratorFromGeometries(Geometry a,
					Geometry b) {
				if ( a instanceof jinngine.geometry.Sphere && b instanceof jinngine.geometry.Sphere) {
					return new SphereContactGenerator((jinngine.geometry.Sphere)a, (jinngine.geometry.Sphere)b);
				}
				
				//not recognised
				return null;	
			}
		});

		
		//Box-Box support maps
		geometryClassifiers.add(new ContactGeneratorClassifier() {
			@Override
			public ContactGenerator createGeneratorFromGeometries(Geometry a,
					Geometry b) {
				if ( a instanceof jinngine.geometry.Box && b instanceof jinngine.geometry.Box) {
					//return new SupportMapContactGenerator2((SupportMap3)a, a,  (SupportMap3)b, b);
					return new FeatureSupportMapContactGenerator((SupportMap3)a, a,  (SupportMap3)b, b);
					//return new BoxBoxContactGenerator((jinngine.geometry.Box)a,(jinngine.geometry.Box)b);					
				}
				
				//not recognised
				return null;	
			}
		});

		
		//General convex support maps
		geometryClassifiers.add(new ContactGeneratorClassifier() {
			@Override
			public ContactGenerator createGeneratorFromGeometries(Geometry a,
					Geometry b) {
				if ( a instanceof SupportMap3 && b instanceof SupportMap3) {
					//return new SupportMapContactGenerator2((SupportMap3)a, a,  (SupportMap3)b, b);
					//return new SupportMapContactGenerator2((SupportMap3)a, a,  (SupportMap3)b, b);
					return new FeatureSupportMapContactGenerator((SupportMap3)a, a,  (SupportMap3)b, b);

					
				}
				
				//not recognised
				return null;	
			}
		});
		
		
		
	}

	/**
	 *  Core method in jinngine is the Model.timeStep() method. The method, 
	 *  in order, does the following:
	 *  
	 *  1. clear out all auxiliary parameters
	 *  a. Apply forces to bodies
	 *  b. Integrate forward on velocities
	 *  2. run broad-phase collision detection
	 *  3. if overlaps, run narrow-phase collision detection, and possibly 
	 *     generating contact constraints using specified parameters (e.g. with 
	 *     or without friction)
	 *  4. Apply constraints generated by modeled joints (e.g a fixed joint, generating 6 constraints)
	 *  5. Solve the velocity based LCP problem using the solver
	 *  6. Apply impulses (delta velocities) to bodies
	 *  7. Integrate forward on positions
	 *  
	 */
	private final void timeStep() {
		// Clear acting forces and auxillary variables
		for (Body c:bodies) {
			// force
			c.clearForces();

			// clear out auxillary delta velocity fields and constraint lists
			c.deltaVCm.assign(Vector3.zero);
			c.deltaOmegaCm.assign(Vector3.zero);
			c.constraints.clear();
		}

		
        // Apply all forces	
		for (Force f: forces) {
			f.apply();
		}

		
		// Run the broad-phase collision detection (this automatically updates the contactGraph,
		// through the BroadfaseCollisionDetection.Handler type)
		broadfase.run();

		
		// Create a special iterator to be used with joints. All joints use this iterator to 
		// insert constraint entries into the constraintList
		ConstraintListIterator constraintIterator = new ConstraintListIterator(constraintList.listIterator());

		
		// Iterate through groups/components in the contact graph
		Iterator<ComponentGraph<Body,Constraint>.Group> groups = contactGraph.groupPairs.keySet().iterator();		
		while (groups.hasNext()) {
			//constraintList.clear();			
			
			//The group 
			ComponentGraph<Body,Constraint>.Group g = groups.next();

			//check if group is all sleepy, then ignore its constraints
			//get nodes (bodies)
			boolean ignoreGroup = true;
			Iterator<ComponentGraph<Body,Constraint>.Node> nodeiter = contactGraph.groups.get(g).iterator();
			while (nodeiter.hasNext()) {
				if ( !nodeiter.next().element.sleeping ) {
					ignoreGroup = false;
					break;
				} 		
			}

			//mark bodies
			//if (ignoreGroup) System.out.println("Sleeping group");
			nodeiter = contactGraph.groups.get(g).iterator();
			while (nodeiter.hasNext()) {
				nodeiter.next().element.ignore = ignoreGroup;
			}
			
			//apply constraints off group
			if (!ignoreGroup) {
				Iterator<Pair<Body>> overlapping = contactGraph.groupPairs.get(g).iterator();
				// Iterate through all edges in this contact graph component
				while( overlapping.hasNext()) {
					Pair<Body> pair = overlapping.next();

					// Apply joint constraints
					Constraint joint = contactGraph.alledges.get(pair);
					joint.applyConstraints(constraintIterator, dt);

				} //while overlapping

			}


			
		} //while groups

		
		// Remove any remaining constraint entries, inserted in previous iteration 
		constraintIterator.removeRemaining();

		
//		Comparator<ConstraintEntry> comp = new Comparator<ConstraintEntry>() {
//			@Override
//			public int compare(ConstraintEntry o1, ConstraintEntry o2) {
//				if ( o1.coupledMax != null && o2.coupledMax != null ) 
//					return 0;
//				if ( o1.coupledMax == null && o2.coupledMax != null ) 
//					return -1;
//				if ( o1.coupledMax != null && o2.coupledMax == null ) 
//					return 1;
//				
//				return 0;
//			}
//		};
//		Collections.sort(constraintList, comp);
		
		//System.out.println(" constraints = "+constraintList.size());
		
		if (constraintList.size()>0) 
			//reset lambda values
			for (ConstraintEntry e: constraintList) {
				e.lambda = 0;
			}


		normals.clear();
		frictions.clear();
		
		for (ConstraintEntry e: constraintList) {
			if (e.coupledMax == null)
				normals.add(e);
			else
				frictions.add(e);
		}

		FisherNewtonCG newton = new FisherNewtonCG();


		if (method == 2) {

			//System.out.println("N="+constraintList.size());

			long t = System.currentTimeMillis();

			newton.setLinesearchIterations(0);
			newton.setMaximumCGIterations(8);
			newton.setMaximumIterations(10);
			newton.setErrorTolerance(0);
			newton.setDamping(0);
			newton.setFrictionDamping(0);
			double errNormals = newton.solve(normals, bodies);
//			double errAll4 = newton.solve(constraintList, bodies);
		   //System.out.println("error ="+errAll4);

//			solver.setMaximumIterations(35);			
//			solver.solve( constraintList, bodies );				

			
			//recompute limits
			double mu=0.7;
			for (ConstraintEntry ci: constraintList) {
				if (ci.coupledMax != null) {
					double limit = Math.abs(ci.coupledMax.lambda)*mu; ci.lambdaMin = -limit; ci.lambdaMax = limit;
					//ci.lambdaMin=-100; ci.lambdaMax = 100;
				}
			}

			newton.setLinesearchIterations(5);
			newton.setMaximumCGIterations(15);
			newton.setMaximumIterations(10);
			newton.setErrorTolerance(0);
			newton.setDamping(0.000000);
			newton.setFrictionDamping(0.0000000);

			double errAll4 = newton.solve(constraintList, bodies);
//
//			long delta = System.currentTimeMillis() -t;
//			//System.out.println("delta="+delta);
			 System.out.println("error final="+errAll4);
//
//			
//			if (errAll4>0.1) {
//
//
//				
//		 System.out.println("error final="+errAll4);

				//newton.printA(constraintList);
			//}


		} else {
			//				solver.setMaximumIterations(30);			
			//				solver.solve( normals );
			//				totalinner += 30/3;


			//long t = System.currentTimeMillis();

			solver.setMaximumIterations(17);			
			solver.solve( constraintList, bodies );				
			//totalinner +=15;

			//long delta = System.currentTimeMillis() -t;
			//System.out.println("delta="+delta);

		}


		
		//double en = NewtonConjugateGradientSolverFriction.fisherError(normals, frictions);
		//double ef = NewtonConjugateGradientSolverFriction.fisherError(frictions, normals);
		
		tick ++;
		//accumen += en;
		//accumef += ef;
		
		
		if (tick % 200 == 0) {
			
			//newton.printA(constraintList);
			
			//for (ConstraintEntry entry: constraintList)
			//	System.out.println(" " + entry.diagonal);
			
			//System.out.println("N="+constraintList.size()+"  Last "+en + " -- " + ef );

			//System.out.println(""+accumen + " -- " + accumef + " = " +totalinner);
		    accumen =0; accumef = 0;
		    totalinner = 0;
		    
		    energy = 0;
		    for (Body b: bodies) {
		    	energy += b.totalKinetic();
		    }
		    //System.out.println("energy="+energy);
		}
		


	

		// Solve the velocity based LCP problem for all constraints
		// TODO (experimenting could reveal a speed up if constraints were solved per group)
		//solver.solve( constraintList );

		//hack, clear delta velocities 
//		for (Body b: bodies) {
//			b.deltaOmegaCm.assignZero();
//			b.deltaVCm.assignZero();
//		}
		
		

		


		
		// Apply delta velocities and integrate positions forward
		for (Body c : bodies ) {
			
			//dont process inactive bodies
			if (c.ignore)
				continue;

			double prekinetic = c.totalKinetic();
			
			//apply external forces
			c.advanceVelocities(dt);
			
			// Apply computed forces to bodies
			if ( !c.isFixed() ) {
				c.state.vCm.assign( c.state.vCm.minus( c.deltaVCm));
				c.state.omegaCm.assign( c.state.omegaCm.minus( c.deltaOmegaCm));	
				Matrix3.multiply(c.state.I, c.state.omegaCm, c.state.L);
				c.state.P.assign(c.state.vCm.multiply(c.state.M));
			}
			
			
			
			//integrate forward on positions 
			if (!c.sleepy) 
				c.advancePositions(dt);
			else {
				c.advancePositions(dt*c.sleepyness);
				c.sleepyness *= 0.75;
				
				if (c.sleepyness < 1e-3) {
					c.sleeping = true;
				}
			}
//				double value = Math.pow(c.totalKinetic() / c.sleepKinetic, 0.125); 
//				c.advancePositions(dt * value );
//			}
			
//			fall asleep or awake
			if (c.totalKinetic() < c.sleepKinetic ) {
				
				if (c.sleepy == false ) {
					c.sleepyness=1;
				}
				
				c.sleepy = true;
				
//				c.state.vCm.assignZero();
//				c.state.omegaCm.assignZero();
//				c.state.P.assignZero();
//				c.state.L.assignZero();
			} else {
				c.sleepy = false;
				c.sleeping = false;
				c.sleepyness = 1;
			}	
		}
	} //time-step


	@Override
	public void setDt(double dt) {
		this.dt = dt;
	}

	private Iterator<Body> getBodyIterator() {
		return bodies.iterator();
	}

	private void muteBodyPair(Pair<Body> pair) {
		mutedBodyPairs.put(pair, true);
	}

	private void unmuteBodyPair(Pair<Body> pair) {
		mutedBodyPairs.remove(pair);
	}

	public void addForce( Force f ) {
		forces.add(f);
	}

	public void removeForce(Force f) {
		forces.remove(f);
	}


	public void addBody( Body c) {
		bodies.add(c);
		
		//install geometries into the broad-phase collision detection
		Iterator<Geometry> i = c.getGeometries();
		while (i.hasNext()) 
			broadfase.add(i.next()); 
	}

	
	public void addConstraint(Pair<Body> pair, Constraint joint) {
		mutedBodyPairs.put(pair,true);		
		contactGraph.addEdge(pair, joint);
	}
	
	public void removeConstraint(Pair<Body> pair) {
		mutedBodyPairs.remove(pair);
		contactGraph.removeEdge(pair);
	}

	@Override
	public BroadfaseCollisionDetection getBroadfase() {
		return broadfase;
	}  
	
	private ContactGenerator getContactGenerator(Pair<Geometry> pair) {
//		if (pair.getFirst() instanceof Sphere && pair.getSecond() instanceof Sphere  ){
//			System.out.println("Sphere pair");
//			return new SphereContactGenerator((Sphere)pair.getFirst(),(Sphere)pair.getSecond());
//
//		} else {
//			SupportMap3 Sa = (SupportMap3)pair.getFirst();
//			SupportMap3 Sb = (SupportMap3)pair.getSecond();	
//			return new SupportMapContactGenerator(Sa,Sb);
//		}
		
		for ( ContactGeneratorClassifier gc: geometryClassifiers) {
			ContactGenerator g = gc.createGeneratorFromGeometries(pair.getFirst(), pair.getSecond());
			
			if (g!=null) {
				return g;
			}
		}
		
		return null;

	}

	@Override
	public void addContactGeneratorClasifier(
			ContactGeneratorClassifier classifier) {
		geometryClassifiers.add(classifier);
		
	}

	@Override
	public void removeBody(Body body) {
		//remove associated geometries from collision detection
		Iterator<Geometry> i = body.getGeometries();
		while( i.hasNext()) {
			broadfase.remove(i.next());			
		}
		
		//finally remove from body list
		bodies.remove(body);
		
	}

	@Override
	public void tick() {
		timeStep();
	}

	@Override
	public double getDt() {
		return dt;
	}

	@Override
	public void addGeometry(Geometry g) {
		broadfase.add(g);
		
	}

	@Override
	public void removeGeometry(Geometry g) {
		broadfase.remove(g);
		
	}

	@Override
	public Solver getSolver() {
		return solver;
	}
	

	
}
