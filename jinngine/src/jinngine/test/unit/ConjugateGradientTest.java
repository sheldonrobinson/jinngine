/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */
package jinngine.test.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jinngine.math.*;
import jinngine.physics.Body;
import jinngine.physics.solver.ConjugateGradients;
import jinngine.physics.solver.Solver;
import jinngine.physics.solver.Solver.NCPConstraint;
import junit.framework.TestCase;

public class ConjugateGradientTest extends TestCase {
	/**
	 * A 1 by 1 problem
	 */
	public void testConjugateGradients1() {
		
		double epsilon = 1e-7;
		
		Solver s = new ConjugateGradients();
		
		Body b1 = new Body("default");
		Body b2 = new Body("default");
		
		Solver.NCPConstraint c1 = new NCPConstraint();
		Vector3 va =new Vector3(1,0,0);
		Vector3 vb =new Vector3(-1,0,0);
		Vector3 z = new Vector3(0,0,0);
		c1.assign(b1,b2,
				  va,z,vb,z,
				  va,z,vb,z,
				  -Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY,
				  null,
				  1,
				  0);
		
		//This is the system
		//
		// [ 1 0 0  0 0 0  -1 0 0  0 0 0] [1 0 0  0 0 0  -1 0 0  0 0 0 ]^T x + 1= 0  =>
		// 2 x + 1 = 0  
		//
		// with the solution x = -1/2 
		
		//list of bodies and constraints
		List<Body> bodies = new ArrayList<Body>();
		List<NCPConstraint> constraints = new ArrayList<NCPConstraint>();
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
		Body b1 = new Body("default");
		Body b2 = new Body("default");
		
		Solver.NCPConstraint c1 = new NCPConstraint();
		Solver.NCPConstraint c2 = new NCPConstraint();

		Vector3 va =new Vector3(1,0,0);
		Vector3 vb =new Vector3(-1,0,0);
		Vector3 z = new Vector3(0,0,0);
		c1.assign(b1,b2,
				va,z,vb,z,
				va,z,vb,z,
				0,0,null,1, 0);
		c2.assign(b1,b2,
				va,z,vb,z,
				va,z,vb,z,
				0,0,null,1, 0);

		
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
		List<NCPConstraint> constraints = new ArrayList<NCPConstraint>();
		bodies.add(b1); bodies.add(b2); constraints.add(c1); constraints.add(c2);

		//run the solver
		s.solve(constraints, bodies, 0.0);
		
		System.out.println(" lambda = ("+c1.lambda+","+c2.lambda+")");
		
		//expect solution (1/4,1/4)
		assertTrue( Math.abs(-0.25 - c1.lambda) < epsilon );	
		assertTrue( Math.abs(-0.25 - c2.lambda) < epsilon );	
	}
	
	/**
	 * 3 by 3 tests. This test create a series of random 3 by 3 linear systems. These are then solved 
	 * first by the conjugate gradients solver, next by computing the direct inverse. The two results 
	 * are compared and the squared error is required to be below the threshold. Only systems that are 
	 * significantly invertible are tested, otherwise they are left out of the test. 
	 * 
	 * This test uses a fixed random seed to make debugging possible.
	 */
	public void testConjugateGradients3() {
		final Random rand = new Random(879843275);
		
		final double epsilon = 1e-30;
		final double testEpsilon = 1e-26;
		
		
		// perform 2^10 tests with random 3x3 systems
		int i = 0;
		while (i<1024) {

			// create bodies and solver
			Solver s = new ConjugateGradients();
			Body b1 = new Body("b1");
			Body b2 = new Body("b2");

			// the constraints
			Solver.NCPConstraint c1 = new NCPConstraint();
			Solver.NCPConstraint c2 = new NCPConstraint();
			Solver.NCPConstraint c3 = new NCPConstraint();

			// create a PSD system based on 3 random jacobians, JJ^T lambda + B = 0
			Vector3 j11 = new Vector3(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			Vector3 j12 = new Vector3(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			Vector3 j13 = new Vector3(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			Vector3 j14 = new Vector3(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			double B1 = rand.nextDouble(); 
			c1.assign(b1,b2,
					j11,j12,j13,j14,
					j11,j12,j13,j14,
					0,0,null,B1, 0);

			Vector3 j21 = new Vector3(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			Vector3 j22 = new Vector3(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			Vector3 j23 = new Vector3(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			Vector3 j24 = new Vector3(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			double B2 = rand.nextDouble(); 
			c2.assign(b1,b2,
					j21,j22,j23,j24,
					j21,j22,j23,j24,
					0,0,null,B2, 0);

			Vector3 j31 = new Vector3(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			Vector3 j32 = new Vector3(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			Vector3 j33 = new Vector3(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			Vector3 j34 = new Vector3(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
			double B3 = rand.nextDouble(); 
			c3.assign(b1,b2,
					j31,j32,j33,j34,
					j31,j32,j33,j34,
					0,0,null,B3, 0);

			//list of bodies and constraints
			List<Body> bodies = new ArrayList<Body>();
			List<NCPConstraint> constraints = new ArrayList<NCPConstraint>();
			bodies.add(b1); bodies.add(b2); constraints.add(c1); constraints.add(c2); constraints.add(c3);

			//run the solver
			s.solve(constraints, bodies, epsilon);

			// calculate JJ' explicitly
			//[ j11 j12 j13 j14 ]  [ j11 j12 j13 j14 ]^T
			//[ j21 j22 j23 j24 ]  [ j21 j22 j23 j24 ]
			//[ j31 j32 j33 j34 ]  [ j31 j32 j33 j34 ]		
			Matrix3 JJ = new Matrix3( j11.dot(j11)+j12.dot(j12)+j13.dot(j13)+j14.dot(j14), 
					j11.dot(j21)+j12.dot(j22)+j13.dot(j23)+j14.dot(j24),
					j11.dot(j31)+j12.dot(j32)+j13.dot(j33)+j14.dot(j34),

					j21.dot(j11)+j22.dot(j12)+j23.dot(j13)+j24.dot(j14), 
					j21.dot(j21)+j22.dot(j22)+j23.dot(j23)+j24.dot(j24),
					j21.dot(j31)+j22.dot(j32)+j23.dot(j33)+j24.dot(j34),

					j31.dot(j11)+j32.dot(j12)+j33.dot(j13)+j34.dot(j14), 
					j31.dot(j21)+j32.dot(j22)+j33.dot(j23)+j34.dot(j24),
					j31.dot(j31)+j32.dot(j32)+j33.dot(j33)+j34.dot(j34));

			// calculate direct inverse
			Matrix3 JJinv = new Matrix3();		
			Matrix3.inverse(JJ, JJinv);

			// if JJ' is invertible, test solution against the direct inverse solution
			if (JJ.determinant() > 1e-12) {
				Vector3 directSolution = JJinv.multiply(new Vector3(-B1,-B2,-B3));
				Vector3 lambda = new Vector3(c1.lambda,c2.lambda,c3.lambda);

				// test squared error against testEpsilon
				assertTrue( directSolution.sub(lambda).squaredNorm() < testEpsilon );			
			}
			// count
			i = i+1;
		}
	}

}
