/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.test.unit;

import java.util.ArrayList;
import java.util.List;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.ConjugateGradients;
import jinngine.physics.solver.experimental.FischerNewton;
import jinngine.physics.solver.ProjectedGaussSeidel;
import jinngine.physics.solver.Solver;
import jinngine.physics.solver.Solver.constraint;
import junit.framework.TestCase;

public class ConjugateGradientTest extends TestCase {
	/**
	 * A 1 by 1 problem
	 */
	public void testConjugateGradients1() {
		
		double epsilon = 1e-7;
		
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
				-Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,null,1);
		
		//This is the system
		//
		// [ 1 0 0  0 0 0  -1 0 0  0 0 0] [1 0 0  0 0 0  -1 0 0  0 0 0 ]^T x + 1= 0  =>
		// 2 x + 1 = 0  
		//
		// with the solution x = -1/2 
		
		//list of bodies and constraints
		List<Body> bodies = new ArrayList<Body>();
		List<constraint> constraints = new ArrayList<constraint>();
		bodies.add(b1); bodies.add(b2); constraints.add(c1);

		//run the solver
		s.solve(constraints, bodies, 0.0);
		
		System.out.println("cg test="+c1.lambda);
		
		//expect solution 1/2
		assertTrue( Math.abs(-0.5 - c1.lambda) < epsilon );	
	}
	
	/**
	 * 2 by 2 problem
	 */
	public void testConjugateGradients2() {
		
		double epsilon = 1e-7;
		
		Solver s = new ConjugateGradients();
		Body b1 = new Body();
		Body b2 = new Body();
		
		Solver.constraint c1 = new constraint();
		Solver.constraint c2 = new constraint();

		Vector3 va =new Vector3(1,0,0);
		Vector3 vb =new Vector3(-1,0,0);
		Vector3 z = new Vector3(0,0,0);
		c1.assign(b1,b2,
				va,z,vb,z,
				va,z,vb,z,
				0,0,null,1);
		c2.assign(b1,b2,
				va,z,vb,z,
				va,z,vb,z,
				0,0,null,1);

		
		//This is the system
		//
		// [ 1 0 0  0 0 0  -1 0 0  0 0 0] [1 0 0  0 0 0  -1 0 0  0 0 0 ]^T x + 1 = 0  
		// [ 1 0 0  0 0 0  -1 0 0  0 0 0] [1 0 0  0 0 0  -1 0 0  0 0 0 ]           =>
		//
		// 2  2 x1 + 1 = 0  
		// 2  2 x2 + 1 = 0
		//
		// with a least squares solution solution x1 = -1/4 and x2 = -1/4 
		
		//list of bodies and constraints
		List<Body> bodies = new ArrayList<Body>();
		List<constraint> constraints = new ArrayList<constraint>();
		bodies.add(b1); bodies.add(b2); constraints.add(c1); constraints.add(c2);

		//run the solver
		s.solve(constraints, bodies, 0.0);
		
		System.out.println("("+c1.lambda+","+c2.lambda+")");
		
		//expect solution (1/4,1/4)
		assertTrue( Math.abs(-0.25 - c1.lambda) < epsilon );	
		assertTrue( Math.abs(-0.25 - c2.lambda) < epsilon );	
	}

}
