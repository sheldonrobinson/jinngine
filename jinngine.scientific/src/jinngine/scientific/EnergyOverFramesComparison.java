package jinngine.scientific;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import jinngine.collision.AllPairsTest;
import jinngine.collision.BroadfaseCollisionDetection;
import jinngine.geometry.Geometry;
import jinngine.geometry.SupportMap3;
import jinngine.geometry.contact.ContactGenerator;
import jinngine.geometry.contact.ContactGeneratorClassifier;
import jinngine.geometry.contact.FeatureSupportMapContactGenerator;
import jinngine.geometry.contact.SamplingSupportMapContacts;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.constraint.ContactConstraint;
import jinngine.physics.constraint.FrictionalContactConstraint;
import jinngine.physics.force.Force;
import jinngine.physics.solver.ProjectedGaussSeidel;
import jinngine.physics.solver.Solver.constraint;
import jinngine.physics.solver.experimental.FischerNewtonConjugateGradients;
import jinngine.util.Pair;

public class EnergyOverFramesComparison implements Test {

	private int frames = 2048;
	private double[] energy = new double[frames];
	private double[] error = new double[frames];
	private double[] contacts = new double[frames];
	private final ContactGeneratorClassifier classifier;
	
	public EnergyOverFramesComparison( ContactGeneratorClassifier classifier  ) {
		this.classifier = classifier;
	}
	
	@Override
	public void run(String testname, String[] configurations,
			PrintStream latexOut, PrintStream matlabOut) {
		
		for (String config: configurations) {
			System.out.println("*) EnergyOverFramesComparison: "+config);
			ConfigurationMetaInfo meta = new ConfigurationMetaInfo(config);

			//run the simulation and record results
			doTest(config);

			matlabOut.println("figure;");
			matlabOut.println("set(gca, 'FontSize', 29);");

			matlabOut.println("subplot(2,1,1);");

			matlabOut.println("grid on;");

			//get results on a matlab string form
			String energystr = "[";
			for (double r: energy) {
				energystr = energystr + r + " ";
			} energystr = energystr +"]";

			matlabOut.println("semilogy("+energystr+", 'LineWidth', 2, 'Color', 'Red' );");

			matlabOut.println("hold on;");

			//get results on a matlab string form
			String errorstr = "[";
			for (double r: error) {
				errorstr = errorstr + r + " ";
			} errorstr = errorstr +"]";

			matlabOut.println("semilogy("+errorstr+", 'LineWidth', 2, 'Color', 'Blue'  );");

			matlabOut.println("set(gca, 'FontSize', 29);");
			matlabOut.println("legend('kinetic', 'error');");
			matlabOut.println("title(\'"+meta.getTitle()+"\');");

			matlabOut.println("subplot(2,1,2);");

			//get results on a matlab string form
			String contactsstr = "[";
			for (double r: contacts) {
				contactsstr = contactsstr + r + " ";
			} contactsstr = contactsstr +"]";

			matlabOut.println("plot("+contactsstr+", 'LineWidth', 2, 'Color', 'Black'  );");

			matlabOut.println("set(gca, 'FontSize', 29);");
			matlabOut.println("legend('constraints');");
			matlabOut.println("xlabel('Frames');");
			//		matlabOut.println("ylabel('Kinetic energy');");
			matlabOut.println("print -dps2c -loose " + testname +".ps;");		
			matlabOut.println("print -depsc -loose " + testname +".eps;");
			matlabOut.println("print -dpdf " + testname +".pdf;");

			matlabOut.println("");

		}
	
	}
	
	/**
	 * Run a simulation for a fixed number of frames. Measure the kinnetic energy in each frame.
	 * @param configuration
	 * @return An array of the kinetic energy in each frame
	 */
	private void doTest( String configuration ) {

		//a list of bodies in animation
		final List<Body> bodies = new ArrayList<Body>();

		//a list of external forces in animation
		final List<Force> forces = new ArrayList<Force>();
		
		//a list of joint constraints animation
		final List<Constraint> joints = new ArrayList<Constraint>();
		
		
		//create a broadphase handler
		final BroadfaseCollisionDetection bf = new AllPairsTest();

		//create a handler for the scene
		PhysicsScene scene = new PhysicsScene() {
			@Override
			public void addBody(Body b) {
				b.updateTransformations();
				bodies.add(b);
				
				Iterator<Geometry> gi = b.getGeometries();
				while(gi.hasNext()) {
					bf.add(gi.next());
				}
			}

			//get forces
			@Override
			public void addForce(Force f) {
				forces.add(f);
			}
			
			//get non contact constraints
			@Override
			public void addConstraint(Constraint c) {
				joints.add(c);
			}
									
			public Iterator<Body> getBodies() {return null;}
			public Iterator<Constraint> getConstraints() {return null;}
			public void removeBody(Body b) {}
			public void removeConstraint(Constraint c) {}
			public void removeForce(Force f) {}
			
		};

		//load a scene 
		Scene.loadScene(configuration, scene);
			
		//do animation frames
		for (int i=0; i<frames; i++) {
			double timestep = 0.1;
			
			//clear delta velocities
			for (Body b: bodies) {
				b.deltaOmegaCm.assignZero();
				b.deltaVCm.assignZero();
				b.clearForces();
			}
			
	        // Apply all forces	
			for (Force f: forces) {
				f.apply(timestep);
			}
	
			//run broad phase to find pairs in proximity
			bf.run();
			
			//get the pairs
			Iterator<Pair<Geometry>> pairs = bf.getOverlappingPairs().iterator();
			
			//make a list of generators
			List<ContactConstraint> contactconstraints = new ArrayList<ContactConstraint>();
			
			//generate constact constraints for all pairs
			while(pairs.hasNext()) {
				Pair<Geometry> gp = pairs.next();
				final Body b1 = gp.getFirst().getBody();
				final Body b2 = gp.getSecond().getBody();
				
				
				//all pairs that we are testing
				if (gp.getFirst() instanceof SupportMap3 &&       // geometry is support maps
						gp.getSecond() instanceof SupportMap3 &&
						b1 != b2 &&                               // from different bodies
						!(b1.isFixed() && b2.isFixed())           // and both of them isn't fixed
						) {
//					ContactGeneratorClassifier classifier = new ContactGeneratorClassifier() {
//						@Override
//						public ContactGenerator createGeneratorFromGeometries(Geometry a,
//								Geometry b) {
//							
//							// *** insert contact determination method here ***
//							return new FeatureSupportMapContactGenerator((SupportMap3) a, a,(SupportMap3)b, b );
//							//return new SamplingSupportMapContacts(b1,b2,(SupportMap3)a, (SupportMap3)b);
//
//						}
//					};
										
					//create the contact constraint
					ContactConstraint cc = new FrictionalContactConstraint(b1,b2, classifier.createGeneratorFromGeometries(gp.getFirst(), gp.getSecond()) );
					
					contactconstraints.add(cc);
				}
			}

			//create NCP constraints
			List<constraint> ncpconstraints = new ArrayList<constraint>();
			ListIterator<constraint> ncps = ncpconstraints.listIterator();
 			for( Constraint c: contactconstraints) {
				c.applyConstraints(ncps, timestep);
			}

 			// ... and from joints
			for( Constraint c: joints) {
				c.applyConstraints(ncps, timestep);
			}

			//System.out.println("ncp constraints " + ncpconstraints.size());

			//use PGS solver on constraints
			new ProjectedGaussSeidel(25).solve(ncpconstraints, bodies, 0.0);
						
			//System.out.println("psi(x) = " +FischerNewtonConjugateGradients.fischerMerit(ncpconstraints, bodies) );
			
			double ekin = 0;
			
			//apply delta velocities to bodies and advance positions
			for (Body b: bodies) {				
				if (!b.isFixed()) {
					Vector3.add( b.state.vCm, b.deltaVCm );
					Vector3.add( b.state.omegaCm, b.deltaOmegaCm );
					//update momentums
					Matrix3.multiply(b.state.I, b.state.omegaCm, b.state.L);
					b.state.P.assign(b.state.vCm.multiply(b.state.M));


					b.advancePositions(timestep);

					ekin += b.totalKinetic();
				}
			}
			
			// store result data for this frame
			energy[i] = ekin;
			error[i] = FischerNewtonConjugateGradients.fischerMerit(ncpconstraints, bodies);
			contacts[i] = ncpconstraints.size();
			
		} //animation frames
		
	}

}
