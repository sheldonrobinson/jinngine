///**
// * Copyright (c) 2008-2010  Morten Silcowitz.
// *
// * This file is part of the Jinngine physics library
// *
// * Jinngine is published under the GPL license, available 
// * at http://www.gnu.org/copyleft/gpl.html. 
// */
//package jinngine.physics.solver;
//
//import jinngine.math.Vector3;
//import jinngine.physics.Body;
//import jinngine.physics.constraint.Constraint;
//import jinngine.util.Logger;
//
///**
// * Preconditioned Conjugate Gradients solver. Solves the given list of constraints, 
// * as if they described a plain symmetric linear system of equations, not a NCP problem. 
// * Phased in another way, CG can solve the NCP, if l=-infinity and u=infinity for all constraints.
// */
//public class ConjugateGradients implements Solver {
//	int maxIterations = 0;
//
//	@Override
//	public void setMaximumIterations(int n) {
//		maxIterations = n;
//	}
//
//	@Override
//	public double solve(Iterable<Constraint> constraints, Iterable<Body> bodies, double epsilon) {
//		maxIterations = constraints.size();
//				
//		assert Logger.trace("ConjugateGradients: "+ maxIterations+"x" + maxIterations+" system");
//
//		//Conjugate Gradients
//		double delta_new=0, delta_old=0, delta_zero=0;
//		double delta_low=Double.POSITIVE_INFINITY;
//		double delta_best=delta_low;
//		//double epsilon = 1e-6;
//		final double division_epsilon = 1e-31;
//		int iterations=0;
//				
//		//reset auxiliary deltas
//		for (Body b: bodies) {
//			b.deltavelocity1.assignZero();
//			b.deltaomega1.assignZero();
//		}
//		
//		// compute external force contribution, compute b vector norm
//		double bnorm = 0;
//		for (NCPConstraint ci: constraints) {
//			ci.Fext = ci.j1.dot(ci.body1.externaldeltavelocity)
//			+ ci.j2.dot(ci.body1.externaldeltaomega)
//			+ ci.j3.dot(ci.body2.externaldeltavelocity) 
//			+ ci.j4.dot(ci.body2.externaldeltaomega); 			
//			bnorm += Math.pow(ci.b+ci.Fext,2);			
//		}
//		bnorm = Math.sqrt(bnorm);
//		final double bnorminv = 1.0/bnorm;
//		
//		//We solve Ax+b = 0 => r = -b-Ax
//		//r  = b-Ax
//		//d  = M^(-1)r
//		//delta_new = rTr
//		for (NCPConstraint ci: constraints) {
//			// find residual (scaled) 
//			ci.residual = -(
//				(ci.b + ci.Fext)*bnorminv  + (ci.j1.dot(ci.body1.deltavelocity) + ci.j2.dot(ci.body1.deltaomega)
//						+ ci.j3.dot(ci.body2.deltavelocity) + ci.j4.dot(ci.body2.deltaomega)) );
//							
//			//d = M^-1 r
//			ci.d = ci.residual / (ci.diagonal);
//
//			//reflect d in the deltavelocity1
//			Vector3.add( ci.body1.deltavelocity1, ci.b1.multiply(ci.d));
//			Vector3.add( ci.body1.deltaomega1,    ci.b2.multiply(ci.d));
//			Vector3.add( ci.body2.deltavelocity1, ci.b3.multiply(ci.d));
//			Vector3.add( ci.body2.deltaomega1,    ci.b4.multiply(ci.d));
//			
//			delta_new += ci.residual * ci.d;
//		} 		
//		
//				
//		delta_old = delta_new;
//		delta_zero = delta_new;
//		delta_best = delta_new;
//				
//		//begin conjugate gradients iterations
//		while (iterations < maxIterations &&  delta_new>epsilon*epsilon*delta_zero && delta_new > epsilon ) {			
//			//q = Ad
//			//alpha = delta_new/dTq
//			double dTq = 0;
//			for (NCPConstraint ci: constraints) {
//				ci.q = ci.j1.dot(ci.body1.deltavelocity1) + ci.j2.dot(ci.body1.deltaomega1)
//				+ ci.j3.dot(ci.body2.deltavelocity1) + ci.j4.dot(ci.body2.deltaomega1); 
//				
//				dTq += ci.d*ci.q;
//			} 			
//
//			if (Math.abs(dTq)< division_epsilon) {
//				assert Logger.trace("ConjugateGradients: at solution, delta_new " + delta_new +", iteration="+iterations );
//				if (iterations==0){
//					delta_best = Double.POSITIVE_INFINITY;
//				}
//				break;
//			} 
//			
//			double alpha = delta_new/dTq;
//			delta_old = delta_new;
//			delta_new = 0;
//
//			for (NCPConstraint ci: constraints) {
//				//x = x + alpha d
//				ci.dlambda += alpha*ci.d;
//
//				//r = r-alpha q
//				ci.residual -= alpha*ci.q;
//				
//				//s = M^(-1) r
//				ci.s = ci.residual / (ci.diagonal);
//								
//				//delta_new = rTs
//				delta_new += ci.residual*ci.s;
//			}
//			
//			assert Logger.trace("ConjugateGradients: ("+iterations+") delta_new="+delta_new);
//			
//			//keep track of best solution
//			boolean best_updated = false;
//			if (delta_new < delta_best) {
//				delta_best = delta_new;
//				best_updated = true;
//			}
//
//			if (Math.abs(delta_old)<division_epsilon) {
//				assert Logger.trace("ConjugateGradients: old delta too low, stopping");
//				break;
//			}
//			
////			if (delta_new > delta_old && iterations > 25) {
////				System.out.println("rising error");
////				break;
////			}
//			
//			if (delta_new > 100*delta_zero) {
//				assert Logger.trace("ConjugateGradients: solution divergence, stopping");
//				break;
//			}
//			
//			double beta = delta_new/delta_old;
//
//			//d = s + beta d
//			for (Body b: bodies) { // d = beta d
//				Vector3.multiply( b.deltavelocity1, beta);
//				Vector3.multiply( b.deltaomega1,    beta);				
//			}			
//			for (NCPConstraint ci: constraints) { 
//				// d = d + r
//				//ci.d = ci.residual + beta* ci.d;
//				ci.d = ci.s + beta* ci.d;
//
//				//reflect d in the delta velocities
//				Vector3.add( ci.body1.deltavelocity1, ci.b1.multiply(ci.s));
//				Vector3.add( ci.body1.deltaomega1,    ci.b2.multiply(ci.s));
//				Vector3.add( ci.body2.deltavelocity1, ci.b3.multiply(ci.s));
//				Vector3.add( ci.body2.deltaomega1,    ci.b4.multiply(ci.s));
//				
//				//if best updated, set the best vector
//				if (best_updated)
//					ci.bestdlambda = ci.dlambda;
//			}	
//			
//			iterations += 1;
//		} // conjugate gradients iterations
//
//		assert Logger.trace("ConjugateGradients: finshed at delta_new="+delta_new+", iterations="+iterations);
//
//		//apply the lambda values to the final velocities, scaled in the b vector norm
//		for (NCPConstraint ci: constraints) {
//			ci.lambda += ci.bestdlambda*bnorm;
//
//			//reflect in delta velocities
//			Vector3.add( ci.body1.deltavelocity, ci.b1.multiply(ci.bestdlambda*bnorm));
//			Vector3.add( ci.body1.deltaomega,    ci.b2.multiply(ci.bestdlambda*bnorm));
//			Vector3.add( ci.body2.deltavelocity, ci.b3.multiply(ci.bestdlambda*bnorm));
//			Vector3.add( ci.body2.deltaomega,    ci.b4.multiply(ci.bestdlambda*bnorm));
//
//			//reset
//			ci.dlambda = 0;
//			ci.bestdlambda = 0;
//		}
//
////		// write out solution
////		for (NCPConstraint ci: constraints) {			
////				System.out.println(((ci.b + ci.Fext) + (ci.j1.dot(ci.body1.deltavelocity) + ci.j2.dot(ci.body1.deltaomega)
////						+ ci.j3.dot(ci.body2.deltavelocity) + ci.j4.dot(ci.body2.deltaomega)
////						+ ci.lambda*ci.damper))+"    " + ci.lambda );
////							
////		} 	
//		
//		return (iterations+1);
//	}
//
//}
