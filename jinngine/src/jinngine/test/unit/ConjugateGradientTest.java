package jinngine.test.unit;

import java.util.ArrayList;
import java.util.List;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.ConjugateGradients;
import jinngine.physics.solver.Solver;
import jinngine.physics.solver.Solver.constraint;
import junit.framework.TestCase;

public class ConjugateGradientTest extends TestCase {
	/**
	 * A 1 by 1 problem
	 */
	public void testConjugateGradients1() {
		
		double epsilon = 1e-14;
		
		Solver s = new ConjugateGradients();
		Body b1 = new Body();
		Body b2 = new Body();
		
		Solver.constraint c1 = new constraint();
		Vector3 va =new Vector3(1,0,0);
		Vector3 vb =new Vector3(-1,0,0);
		Vector3 z = new Vector3(0,0,0);
		c1.assign(b1,b2,
				va,z,vb,z,
				va,z,vb,z,
				0,0,null,1);
		
		//This is the system
		//
		// [ 1 0 0  0 0 0  -1 0 0  0 0 0] [1 0 0  0 0 0  -1 0 0  0 0 0 ]^T x = 2  =>
		// 2 x = 1  
		//
		// with the solution x = 1/2 
		
		//list of bodies and constraints
		List<Body> bodies = new ArrayList<Body>();
		List<constraint> constraints = new ArrayList<constraint>();
		bodies.add(b1); bodies.add(b2); constraints.add(c1);

		//run the solver
		s.solve(constraints, bodies);
		
		//expect solution 1/2
		assertTrue( Math.abs(c1.lambda - 0.5) < epsilon );	
	}
}
