/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.collision;
import jinngine.geometry.SupportMap3;
import jinngine.math.Vector3;


/**
 * Implementation of the Gilbert-Johnsson-Keerthi algorithm, for computing the distance 
 * between convex objects and/or closest points. This implementation is based on the theory presented in 
 * [Bergen, 2003, Continuous Collision Detection in Interactive 3D environments, ISBN-13: 978-1558608016].
 * This describes an approach that is based on a recursive simplex reduction technique. This implementation
 * us "unrolled", so that recursive calls are avoided. 
 */
public final class GJK {
	/**
	 * A state structure for the GJK3 implementation. Used internally in the GJK3 implementation,
	 * and also used to simplify caching of GJK results.
	 */
	public final class State {
		public final Vector3 v = new Vector3(10,10,10);
		public final Vector3 w = new Vector3();
		public final Vector3 p = new Vector3();
		public final Vector3 q = new Vector3();
		public final Vector3 sa = new Vector3();
		public final Vector3 sb = new Vector3();
		public final Vector3[][] simplices = new Vector3[4][4]; //contains 3 simplices for A-B, Sa, Sb
		public final double[] lambda = new double[] {1,0,0,0};
		public final int[] permutation = new int[] {0,1,2,3};
		public int simplexSize;	
		public int iterations = 0;
		public boolean intersection = false;
		//public boolean initialised = false;
		public State() {
			// fill out the simplices table with vectors
			for (int i=0;i<4;i++)
				for (int j=0;j<4;j++)
					simplices[i][j] = new Vector3();
		}
	}

	private final State state = new State();
	
	/**
	 * Get the internal state of the GJK algorithm
	 * @return A GJK state
	 */
	public State getState() {
		return state;
	}
	
	/**
	 * Find the closest pair of points on the convex objects A and B, given by the support mappings Sa and Sb. This 
	 * method does not take into account the value of {@link SupportMap3.sphereSweepRadius()}. This is so, because 
	 * the closest points of two sphere swept volumes are trivially known if the closest points of the original shapes
	 * are known. 
	 * 
	 * @param Sa support mapping of a convex object A
	 * @param Sb support mapping of a convex object B
	 * @param va on return, va will contain the closest point on A 
	 * @param vb on return, vb will contain the closest point on B
	 * 
	 * @param envelope the maximum distance that GJK should compute. If the algorithm reaches a state where the 
	 * true distance between A and B will be greater the given value, it terminates. Setting this value Inf will 
	 * result in the algorithm computing the distance between A and B in all cases. Setting it to zero will reduce 
	 * the algorithm to computing a separating axis, because it will terminate as soon as evidence that A and B are 
	 * non-intersecting is found.
	 * @param epsilon error precision, used in termination criterion. In general, a smaller error tolerance results 
	 * in more iterations before termination. 
	 * 
	 * @param maxiter maximum number of iterations before termination is forced.
	 */
	public void run( SupportMap3 Sa, SupportMap3 Sb, Vector3 va, Vector3 vb, double envelope, double epsilon, int maxiter ) {		
		// if v has become too small, reset it TODO check this
		if (state.v.norm()<epsilon) {
			state.v.assign(10,10,10);
		}

		state.iterations = 0;
		state.intersection = false;
    	final Vector3 v = state.v;
		final Vector3 w = state.w;
		final Vector3 sa = state.sa;
		final Vector3 sb = state.sb;
    			
    	// initially update the simplex (often results in a quick termination)
    	if (state.simplexSize>0)
    		updateSimplex(state, Sa, Sb);

    	// main loop
		while ( true  ) {
			state.iterations++;		
//		    System.out.println("gjk iteration" + " " + v.norm()+ "  : " + state.simplexSize);
			
			// store points of convex objects a and b, and A-B 
//			sa.assign( Sa.supportPoint(state.v.negate(), new Vector3()));
//			sb.assign( Sb.supportPoint(state.v, new Vector3()));	    							
//			w.assign( sa.sub(sb) );
			state.v.assignNegate();
			Sa.supportPoint(state.v, sa);
			state.v.assignNegate();
			Sb.supportPoint(state.v, sb);	    							
			w.assignDifference(sa,sb);

			
			// termination condition
			// ||v||2 -v.w is an upper bound for ||vk-v(A-B)||2 which converges towards zero as k goes large
			if (  Math.abs(v.dot(v)-v.dot(w)) < epsilon*epsilon  || state.iterations>maxiter || state.simplexSize > 3 ) 
				break;
						
			//add w to the simplices
			Vector3[] row = state.simplices[state.permutation[state.simplexSize]];
			row[0].assign(w);
			row[1].assign(sa);
			row[2].assign(sb);
			row[3].assign(v);
			state.simplexSize++;
						
			// reduce the simplex to smallest subset containing the closest point
			if ( !reduceSimplex( state ) ) {
				//latest w vector was rejected, we consequently terminate (simplex cannot chance from here on)
				break;
			}
			
			// separating axis test (distance is at least more than the envelope)
			if ( v.dot(w) > envelope*v.norm() ) {
				state.intersection = false;
				break;
			} 
			
			//Calculate the vector v using lambda values
			v.assignZero();
			for (int i=0; i<state.simplexSize;i++) {
//				Vector3.add(v, state.simplices[state.permutation[i]][0].multiply( state.lambda[state.permutation[i]]));
				Vector3.multiplyAndAdd(state.simplices[state.permutation[i]][0], state.lambda[state.permutation[i]], v);
			}

			//Check for a penetrating state
			if ( v.norm() < epsilon || state.simplexSize > 3 ) {
				break;				
			}
		} //while true
		
		//Computing v, p, and q, closest points of A and B
//		state.v.assignZero(); 
		state.p.assignZero(); state.q.assignZero(); 
		for (int i=0; i<state.simplexSize;i++) {
//			Vector3.add(state.p, state.simplices[state.permutation[i]][1].multiply(state.lambda[state.permutation[i]]));
//			Vector3.add(state.q, state.simplices[state.permutation[i]][2].multiply(state.lambda[state.permutation[i]]));				
			Vector3.multiplyAndAdd(state.simplices[state.permutation[i]][1], state.lambda[state.permutation[i]], state.p );
			Vector3.multiplyAndAdd(state.simplices[state.permutation[i]][2], state.lambda[state.permutation[i]], state.q );				
		}

		// return closest points in return arguments
		va.assign(state.p);
		vb.assign(state.q);
		
		// check for intersection
		//if ( va.sub(vb).norm() < epsilon || state.simplexSize > 3)
		if ( Vector3.normOfDifference(va, vb) < epsilon || state.simplexSize > 3)
			state.intersection = true;
	}
	
	
	/**
	 *  updateSimplex() uses the cached information in GJK3.State to update the current simplex. More specifically,
	 *  the cached v vectors, that was previously used to obtain each simplex point, is reused to recompute all simplex points.
	 *  After this, the method reduceSimplex is invoked, such that the new updated simplex is indeed a
	 *  minimal one. The relevance in this lies in exploiting frame coherence. When GJK is run on a given configuration,
	 *  the v vectors used to obtain the current simplex will often be sufficient to obtain a new simplex, adequate 
	 *  to meet the termination condition for the next time frame. For example, this is would often be the case 
	 *  for resting contacts. Thus all subsequent GJK iterations is avoided.
	 */
	private final void updateSimplex(final GJK.State state, final SupportMap3 Sa, final SupportMap3 Sb) {

		for (int i = 0; i<state.simplexSize; i++) {
			//add w to the simplices
			final Vector3[] row = state.simplices[state.permutation[i]];

			//store points of convex objects a and b, and A-B (in A space)
//			row[1].assign(Sa.supportPoint(state.simplices[i][3].negate(), new Vector3()));
//			row[2].assign(Sb.supportPoint(state.simplices[i][3], new Vector3()));	    							
//			row[0].assign(row[1].sub(row[2]));			
			Sa.supportPoint(state.simplices[i][3].assignNegate(), row[1]);
			Sb.supportPoint(state.simplices[i][3].assignNegate(), row[2]);
			row[0].assignDifference(row[1],row[2]); // v = a-b
		}

		//recompute the simplex and lambda values
		reduceSimplex( state );
	
		//Calculate the vector v
		state.v.assignZero();
		for (int i=0; i<state.simplexSize;i++) 
			//Vector3.add(state.v, state.simplices[state.permutation[i]][0].multiply( state.lambda[state.permutation[i]]));
			Vector3.multiplyAndAdd( state.simplices[state.permutation[i]][0], state.lambda[state.permutation[i]], state.v);

	}

//	/**
//	 * Return a support point on the CSO of A and B, (A-B), in world space
//	 * @param Sa support mapping for object A
//	 * @param a body for object A
//	 * @param Sb support mapping for object B
//	 * @param b Body for object B
//	 * @param v direction vector 
//	 * @return a new support point on the configuration space obstacle of A and B
//	 */
//	@SuppressWarnings("unused")
//	private final Vector3 support( final SupportMap3 Sa, final SupportMap3 Sb, final Vector3 v) {
//		final Vector3 sva = Sa.supportPoint(v, new Vector3()) ;
//		//We need rotate the vector reverse in B space
//		final Vector3 svb = Sb.supportPoint(v.multiply(-1), new Vector3());   		
//		return sva.sub(svb);
//	}
	/**
	 * Auxiliary function for swapping two elements in a permutation array.
	 * 
	 * @param i 
	 * @param j
	 * @param permutation 
	 */
	private final void swap( final int i, final int j, final int[] permutation ) {
		final int temp = permutation[i];
		permutation[i] = permutation[j];
		permutation[j] = temp;
	}

	/**
	 * Method is a hard-coded method that reduces the given simplex to the smallest possible simplex,
	 * that contains the closest point to the origin. This approach is described in detail in [Bergen 2003],
	 * and can be quite cumbersome to grasp. However, it is sufficient to know that the method finds the 
	 * smallest subset of simplex points, which contains the closest point to the origin of the whole simplex.
	 * In addition, the method also computes a parametrisation of the closest point, v, expressed as a convex
	 * combination of the new simplex points, given by the lambda coefficients.
	 * @param state State containing the simplex that is to be reduced
	 * @return returns false if the latest added simplex point was rejected, true otherwise. Upon return, the state
	 * class will be updated containing new simplex points, lambda values and permutation vector. 
	 */
	private final boolean reduceSimplex(final State state) {
		// Matrix is a 4x3 matrix like:| y1 a1 b1 |
		// permutation is              | ...      |
		//  {size,p1,p2,p3,p4}         | y4 a4 b4 |
		//		
		final Vector3[][] matrix = state.simplices;
		final double[] lambda = state.lambda;
		final int[] perm = state.permutation;
		final double epsilon = 1e-13;
		boolean modified = true;

		// reference the working vectors
		final Vector3[] row0 = matrix[perm[0]];
		final Vector3[] row1 = matrix[perm[1]];
		final Vector3[] row2 = matrix[perm[2]];			
		final Vector3[] row3 = matrix[perm[3]];			
		final Vector3 y1 = row0[0]; 
		final Vector3 y2 = row1[0];
		final Vector3 y3 = row2[0];
		final Vector3 y4 = row3[0];

		// scale in the norm of the latest vector
		final double scale;
		final double norm = state.simplices[state.permutation[state.simplexSize-1]][0].norm();

		if (norm < epsilon) {
			scale = 1;
		} else {
			scale = 1.0/norm;
		}

		// scale vectors
		for (int i=0; i<state.simplexSize; i++) {
			Vector3.multiply( state.simplices[perm[i]][0], scale );
		}
		
		
		// trivial case
		switch (state.simplexSize) {
		case 1:
			lambda[perm[0]]=1;
			state.simplexSize=1;
			break; // end of case 1			
		case 2: {
			// 1 12
			// 2
			//			final Vector3[] row0 = matrix[perm[0]];
			//			final Vector3[] row1 = matrix[perm[1]];
			//			final Vector3 y1 = row0[0]; 
			//			final Vector3 y2 = row1[0]; 

			
			//final double d12_1 = y2.sub(y1).dot(y2);
			final double d12_1 = Vector3.subAndDot(y2,y1,y2);		
			//final double d12_2 = y1.sub(y2).dot(y1);
			final double d12_2 = Vector3.subAndDot(y1,y2,y1);

			//y1 (no permutation needed)
			if ( d12_2 < epsilon ) {                  
				lambda[perm[0]] = 1;  
				state.simplexSize = 1; 
				modified = false;
				break;
			}
			//y2 ( (2,1)
			if ( d12_1 < epsilon ) { 
				swap(1,0, perm); 
				lambda[perm[0]] = 1;  
				state.simplexSize = 1; 
				break; 
			}

			final double d12 = d12_1 + d12_2;
			// terminate on affinely dependent points in the set (if d12 is zero, we can never use point y2)
			if ( Math.abs(d12) <= epsilon ) {
				// System.out.println("Affinely dependent set in case d12");
				state.simplexSize = 1;
				modified = false;
				break;
			}

			if ( d12_1 > epsilon && d12_2 > epsilon ) {	
				//y1, y2 (no permutation)			
				lambda[perm[0]] = d12_1/d12; lambda[perm[1]] = d12_2/d12;
				break;
			} else {
				//				System.out.println("Unable to determine smallest set, use y1");
				state.simplexSize = 1;
				modified = false;
				break;
			}			
			//break; // end of case 2
		}
		case 3: {
			// 1 12 123
			// 2 13
			// 3 23

			//			final Vector3[] row0 = matrix[perm[0]];
			//			final Vector3[] row1 = matrix[perm[1]];
			//			final Vector3[] row2 = matrix[perm[2]];			
			//			final Vector3 y1 = row0[0]; 
			//			final Vector3 y2 = row1[0];
			//			final Vector3 y3 = row2[0];

			//y1, (no permutation)
			//			final double d13_3 = y1.sub(y3).dot(y1);
			final double d13_3 = Vector3.subAndDot(y1,y3,y1);
			//			final double d12_2 = y1.sub(y2).dot(y1);
			final double d12_2 = Vector3.subAndDot(y1,y2,y1);

			if ( d12_2 < epsilon && d13_3 < epsilon ) {  
				lambda[perm[0]]=1; state.simplexSize=1; 
				break; 
			}

			//y2 (2,1)
			//			final double d12_1 = y2.sub(y1).dot(y2); 
			final double d12_1 = Vector3.subAndDot(y2,y1,y2); 
			//			final double d23_3 = y2.sub(y3).dot(y2); 
			final double d23_3 = Vector3.subAndDot(y2,y3,y2); 

			if ( d12_1 < epsilon && d23_3 < epsilon ) { 
				swap(1,0,perm); 
				lambda[perm[0]]=1; state.simplexSize=1; 
				break; 
			}

			//y3 (3,1)
			//			final double d13_1 = y3.sub(y1).dot(y3); 
			final double d13_1 = Vector3.subAndDot(y3,y1,y3); 
			//			final double d23_2 = y3.sub(y2).dot(y3); 
			final double d23_2 = Vector3.subAndDot(y3,y2,y3); 

			if ( d23_2 < epsilon && d13_1 < epsilon ) { 
				swap(2,0,perm); 
				lambda[perm[0]]=1; 
				state.simplexSize=1; 
				break; 
			}

			// calculate determinants 
			final double d23 = d23_2 + d23_3;
			final double d13 = d13_1 + d13_3;
			final double d12 = d12_1 + d12_2;

			//y2,y3 (2,1) (3,2)
			//			final double d123_1 = d23_2 * y2.sub(y1).dot(y2) + d23_3 * y2.sub(y1).dot(y3); 
			final double d123_1 = d23_2 * Vector3.subAndDot(y2,y1,y2) + d23_3 * Vector3.subAndDot(y2,y1,y3); 
			if (d123_1 < epsilon && d23_2 > epsilon && d23_3 > epsilon) { 
				swap(1,0,perm); 
				swap(2,1,perm); 
				lambda[perm[0]]=d23_2/d23; 
				lambda[perm[1]]=d23_3/d23; 
				state.simplexSize=2; 
				break; 
			}

			//y1,y3 (3,2)
			//			final double d123_2 = d13_1 * y1.sub(y2).dot(y1) + d13_3 * y1.sub(y2).dot(y3); 
			final double d123_2 = d13_1 * Vector3.subAndDot(y1,y2,y1) + d13_3 * Vector3.subAndDot(y1,y2,y3); 
			if (d123_2 < epsilon && d13_1 > epsilon && d13_3 > epsilon) {
				swap(2,1,perm); 
				lambda[perm[0]]=d13_1/d13; 
				lambda[perm[1]]=d13_3/d13; 
				state.simplexSize=2; 
				break; 
			}

			//y1,y2 (no permutation)
			//			final double d123_3 = d12_1 * y1.sub(y3).dot(y1) + d12_2 * y1.sub(y3).dot(y2); //d123_3 = Math.abs(d123_3)<epsilon?0:d123_3;
			final double d123_3 = d12_1 * Vector3.subAndDot(y1,y3,y1) + d12_2 * Vector3.subAndDot(y1,y3,y2); //d123_3 = Math.abs(d123_3)<epsilon?0:d123_3;
			if (d123_3 < epsilon && d12_1 > epsilon && d12_2 > epsilon) {
				lambda[perm[0]]=d12_1/d12; 
				lambda[perm[1]]=d12_2/d12; 
				state.simplexSize=2; 
				modified = false;
				break; 
			}

			//y1, y2, y3 (no permutation)	
			final double d123 = d123_1 + d123_2 + d123_3;
			if ( d123_1 > epsilon && d123_2 > epsilon && d123_3 > epsilon)
			{  		
				lambda[perm[0]]=d123_1/d123; lambda[perm[1]]=d123_2/d123; lambda[perm[2]]=d123_3/d123; state.simplexSize=3; 
				break;
			} else {
				System.out.println("GJK: Unable to determine smallest set, use y1,y2");
				//double d123_3 = d12_1 * y1.minus(y3).dot(y1) + d12_2 * y1.minus(y3).dot(y2); d123_3 = Math.abs(d123_3)<epsilon?0:d123_3;
				lambda[perm[0]]=d12_1/d12; lambda[perm[1]]=d12_2/d12; state.simplexSize=2; 
				modified = false;
				break;
				//System.exit(-1);
			}
			//break; // end of case 3
		}
		case 4: {
			// 1 12 123 1234
			// 2 13 124
			// 3 14 134
			// 4 23 234
			//	 24
			//   34
			//y1 (no permutation)
			//			final double d13_3 = y1.sub(y3).dot(y1); 
			final double d13_3 = Vector3.subAndDot(y1,y3,y1); 
			//			final double d12_2 = y1.sub(y2).dot(y1); 
			final double d12_2 = Vector3.subAndDot(y1,y2,y1); 
			//			final double d14_4 = y1.sub(y4).dot(y1); 
			final double d14_4 = Vector3.subAndDot(y1,y4,y1); 
			if ( d12_2 < epsilon && d13_3 < epsilon && d14_4 < epsilon ) { 
				lambda[perm[0]] = 1; 
				state.simplexSize=1; 
				break; 
			}

			//y2 (2,1)
			//			final double d12_1 = y2.sub(y1).dot(y2); 
			final double d12_1 = Vector3.subAndDot(y2,y1,y2); 
			//			final double d23_3 = y2.sub(y3).dot(y2); 
			final double d23_3 = Vector3.subAndDot(y2,y3,y2); 
			//			final double d24_4 = y2.sub(y4).dot(y2);
			final double d24_4 = Vector3.subAndDot(y2,y4,y2); 
			if ( d12_1 < epsilon && d23_3 < epsilon  && d24_4 < epsilon) { 
				swap(1,0,perm); 
				lambda[perm[0]] = 1; 
				state.simplexSize=1; 
				break; 
			}

			//y3 (3,1)
			//			final double d13_1 = y3.sub(y1).dot(y3); 
			final double d13_1 = Vector3.subAndDot(y3,y1,y3); 
			//			final double d23_2 = y3.sub(y2).dot(y3); 
			final double d23_2 = Vector3.subAndDot(y3,y2,y3); 
			//			final double d34_4 = y3.sub(y4).dot(y3); 
			final double d34_4 = Vector3.subAndDot(y3,y4,y3); 
			if ( d23_2 < epsilon && d13_1 < epsilon && d34_4 < epsilon ) { 
				swap(2,0,perm); 
				lambda[perm[0]] = 1; 
				state.simplexSize=1; 
				break; 
			}

			//y4 (4,1)
			//			final double d14_1 = y4.sub(y1).dot(y4); 
			final double d14_1 = Vector3.subAndDot(y4,y1,y4); 
			//			final double d24_2 = y4.sub(y2).dot(y4); 
			final double d24_2 = Vector3.subAndDot(y4,y2,y4); 			
			//			final double d34_3 = y4.sub(y3).dot(y4); 
			final double d34_3 = Vector3.subAndDot(y4,y3,y4); 
			if ( d14_1 < epsilon && d24_2 < epsilon && d34_3 < epsilon ) { 
				swap(3,0,perm); 
				lambda[perm[0]] = 1; 
				state.simplexSize=1; 
				break; 
			}

			// calculate the determinants
			final double d12 = d12_1 + d12_2;
			final double d13 = d13_1 + d13_3;
			final double d14 = d14_1 + d14_4;
			final double d23 = d23_2 + d23_3;
			final double d24 = d24_2 + d24_4;
			final double d34 = d34_3 + d34_4;

			//y1,y2 (no permutation)
			//			final double d123_3 = d12_1 * y1.sub(y3).dot(y1) + d12_2 * y1.sub(y3).dot(y2); 
			final double d123_3 = d12_1 * Vector3.subAndDot(y1,y3,y1) + d12_2 * Vector3.subAndDot(y1,y3,y2); 
			//			final double d124_4 = d12_1 * y1.sub(y4).dot(y1) + d12_2 * y1.sub(y4).dot(y2); 
			final double d124_4 = d12_1 * Vector3.subAndDot(y1,y4,y1) + d12_2 * Vector3.subAndDot(y1,y4,y2); 
			if( d12_1 > epsilon && d12_2 > epsilon && d123_3 < epsilon && d124_4 < epsilon) {
				lambda[perm[0]] = d12_1 / d12; 
				lambda[perm[1]] = d12_2 / d12;
				state.simplexSize = 2;
				break;				
			}

			//y1, y3 (3,2)
			//			final double d123_2 = d13_1 * y1.sub(y2).dot(y1) + d13_3 * y1.sub(y2).dot(y3); 
			final double d123_2 = d13_1 * Vector3.subAndDot(y1,y2,y1) + d13_3 * Vector3.subAndDot(y1,y2,y3); 
			//			final double d134_4 = d13_1 * y1.sub(y4).dot(y1) + d13_3 * y1.sub(y4).dot(y3); 
			final double d134_4 = d13_1 * Vector3.subAndDot(y1,y4,y1) + d13_3 * Vector3.subAndDot(y1,y4,y3); 
			if( d13_1 > epsilon && d13_3 > epsilon && d123_2 < epsilon && d134_4 < epsilon) {				
				swap(2,1,perm);
				lambda[perm[0]] = d13_1 / d13; lambda[perm[1]] = d13_3/d13;
				state.simplexSize = 2;
				break;
			}

			//y1, y4 (4,2)
			//			final double d124_2 = d14_1 * y1.sub(y2).dot(y1) + d14_4 * y1.sub(y2).dot(y4); 
			final double d124_2 = d14_1 * Vector3.subAndDot(y1,y2,y1) + d14_4 * Vector3.subAndDot(y1,y2,y4); 
			//			final double d134_3 = d14_1 * y1.sub(y3).dot(y1) + d14_4 * y1.sub(y3).dot(y4); 
			final double d134_3 = d14_1 * Vector3.subAndDot(y1,y3,y1) + d14_4 * Vector3.subAndDot(y1,y3,y4); 
			if( d14_1 > epsilon && d14_4 > epsilon && d124_2 < epsilon && d134_3 < epsilon) {
				swap(3,1,perm);
				lambda[perm[0]] = d14_1 / d14; lambda[perm[1]] = d14_4/d14;
				state.simplexSize = 2;
				break;
			}

			//y2,y3 (2,1) (3,2)
			//			final double d123_1 = d23_2 * y2.sub(y1).dot(y2) + d23_3 * y2.sub(y1).dot(y3); 
			final double d123_1 = d23_2 * Vector3.subAndDot(y2,y1,y2) + d23_3 * Vector3.subAndDot(y2,y1,y3); 
			//			final double d234_4 = d23_2 * y2.sub(y4).dot(y2) + d23_3 * y2.sub(y4).dot(y3); 
			final double d234_4 = d23_2 * Vector3.subAndDot(y2,y4,y2) + d23_3 * Vector3.subAndDot(y2,y4,y3); 
			if( d23_2 > epsilon && d23_3 > epsilon && d123_1 < epsilon && d234_4 < epsilon) {
				swap(1,0,perm); swap(3,1,perm);
				lambda[perm[0]] = d23_2 / d23; lambda[perm[1]] = d23_3/d23;
				state.simplexSize = 2;
				break;
			}

			//y2,y4 (2,1) (4,2)
			//			final double d124_1 = d24_2 * y2.sub(y1).dot(y2) + d24_4 * y2.sub(y1).dot(y4); 
			final double d124_1 = d24_2 * Vector3.subAndDot(y2,y1,y2) + d24_4 * Vector3.subAndDot(y2,y1,y4); 
			//			final double d234_3 = d24_2 * y2.sub(y3).dot(y2) + d24_4 * y2.sub(y3).dot(y4); 
			final double d234_3 = d24_2 * Vector3.subAndDot(y2,y3,y2) + d24_4 * Vector3.subAndDot(y2,y3,y4); 
			if( d24_2 > epsilon && d24_4 > epsilon && d124_1 < epsilon && d234_3 < epsilon) {
				swap(1,0,perm); swap(3,1,perm);
				lambda[perm[0]] = d24_2 / d24; lambda[perm[1]] = d24_4/d24;
				state.simplexSize = 2;
				break;
			}

			//y3,y4 (3,1) (2,4)
			//			final double d134_1 = d34_3 * y3.sub(y1).dot(y3) + d34_4 * y3.sub(y1).dot(y4); 
			final double d134_1 = d34_3 * Vector3.subAndDot(y3,y1,y3) + d34_4 * Vector3.subAndDot(y3,y1,y4); 
			//			final double d234_2 = d34_3 * y3.sub(y2).dot(y3) + d34_4 * y3.sub(y2).dot(y4); 
			final double d234_2 = d34_3 * Vector3.subAndDot(y3,y2,y3) + d34_4 * Vector3.subAndDot(y3,y2,y4); 
			if( d34_3 > epsilon && d34_4 > epsilon && d134_1 < epsilon && d234_2 < epsilon) {
				swap(2,0,perm); swap(1,3,perm);
				lambda[perm[0]] = d34_3 / d34; lambda[perm[1]] = d34_4/d34;
				state.simplexSize = 2;
				break;
			}

			//y1,y2,y3 (no permutation)
			//			final double d1234_4 = d123_1 * (y1.sub(y4).dot(y1)) + d123_2 * (y1.sub(y4).dot(y2) ) + d123_3 * (y1.sub(y4).dot(y3));
			final double d1234_4 = d123_1 * Vector3.subAndDot(y1,y4,y1) + d123_2 * Vector3.subAndDot(y1,y4,y2) + d123_3 * Vector3.subAndDot(y1,y4,y3);
			if ( d123_1 > epsilon && d123_2 > epsilon && d123_3 > epsilon && d1234_4 < epsilon) {
				final double d123 = d123_1 + d123_2 + d123_3;
				lambda[perm[0]]= d123_1/d123; lambda[perm[1]]= d123_2/d123; lambda[perm[2]] = d123_3/d123;
				state.simplexSize = 3;
				modified = false;
				break;
			}

			//y1,y2,y4 (4,3)
			//			final double d1234_3 = d124_1 * (y1.sub(y3).dot(y1)) + d124_2 * (y1.sub(y3).dot(y2) ) + d124_4 * (y1.sub(y3).dot(y4));
			final double d1234_3 = d124_1 * Vector3.subAndDot(y1,y3,y1) + d124_2 * Vector3.subAndDot(y1,y3,y2) + d124_4 * Vector3.subAndDot(y1,y3,y4);
			if ( d124_1 > epsilon && d124_2 > epsilon && d124_4 > epsilon && d1234_3 < epsilon) { 
				final double d124 = d124_1 + d124_2 + d124_4;
				swap(3,2,perm);
				lambda[perm[0]]= d124_1/d124; lambda[perm[1]]= d124_2/d124; lambda[perm[2]] = d124_4/d124;
				state.simplexSize = 3;
				break;
			}

			//y1,y3,y4 (3,2) (4,3)
			//			final double d1234_2 = d134_1 * (y1.sub(y2).dot(y1)) + d134_3 * (y1.sub(y2).dot(y3) ) + d134_4 * (y1.sub(y2).dot(y4));
			final double d1234_2 = d134_1 * Vector3.subAndDot(y1,y2,y1) + d134_3 * Vector3.subAndDot(y1,y2,y3) + d134_4 * Vector3.subAndDot(y1,y2,y4);
			if ( d134_1 > epsilon && d134_3 > epsilon && d134_4 > epsilon && d1234_2 < epsilon) { 
				final double d134 = d134_1 + d134_3 + d134_4;
				swap(2,1,perm); swap(3,2,perm);
				lambda[perm[0]]= d134_1/d134; lambda[perm[1]]= d134_3/d134; lambda[perm[2]] = d134_4/d134;
				state.simplexSize = 3;
				break;
			}

			//y2,y3,y4 (2,1)(3,2)(4,3)
			//			final double d1234_1 = d234_2 * (y2.sub(y1).dot(y2)) + d234_3 * (y2.sub(y1).dot(y3) ) + d234_4 * (y2.sub(y1).dot(y4));
			final double d1234_1 = d234_2 * Vector3.subAndDot(y2,y1,y2) + d234_3 * Vector3.subAndDot(y2,y1,y3) + d234_4 * Vector3.subAndDot(y2,y1,y4);
			if ( d234_2 > epsilon && d234_3 > epsilon && d234_4 > epsilon && d1234_1 < epsilon) {
				final double d234 = d234_2 + d234_3 + d234_4;
				swap(1,0,perm); swap(2,1,perm); swap(3,2,perm);
				lambda[perm[0]]= d234_2/d234; lambda[perm[1]]= d234_3/d234; lambda[perm[2]] = d234_4/d234;
				state.simplexSize = 3;
				break;
			}

			//y1,y2,y3,y4 (no permute)
			final double d1234 = d1234_1 + d1234_2 + d1234_3 + d1234_4;
			if ( d1234_1 > epsilon && d1234_2 > epsilon && d1234_3 > epsilon && d1234_4 > epsilon) {
				// origin is contained in the simplex

				//check for the accuracy, v should be the zero vector
//				if(  y1.multiply(d1234_1/d1234).add(y2.multiply(d1234_2/d1234)).add(y3.multiply(d1234_3/d1234)).add(y4.multiply(d1234_4/d1234)).norm() > 0.001 ) {
//
//					System.out.println("d1234="+d1234+"  wrong penetration: "+ y1.multiply(d1234_1/d1234).add(y2.multiply(d1234_2/d1234)).add(y3.multiply(d1234_3/d1234)).add(y4.multiply(d1234_4/d1234)).norm());
//					state.simplexSize = 3;
//					modified = false;
//					break;
//				}

				lambda[perm[0]] = d1234_1/d1234; lambda[perm[1]] = d1234_2/d1234; lambda[perm[2]]=d1234_3/d1234; lambda[perm[3]] = d1234_4/d1234;
				state.simplexSize=4;
				break;
			} else {
				//The algorithm was unable to determine the subset. return the last best known subset:
				final double d123 = d123_1 + d123_2 + d123_3;
				lambda[perm[0]]= d123_1/d123; lambda[perm[1]]= d123_2/d123; lambda[perm[2]] = d123_3/d123;
				state.simplexSize = 3;
				modified = false;
				break;
			}
		}} // switch
		
		// scale vectors back to original scale
		for (int i=0; i<state.simplexSize; i++) {
			Vector3.multiply( state.simplices[perm[i]][0], norm );
		}
		
		// return true if the simplex was modified, false if the latest
		// point was rejected
		return modified;
	}

}
