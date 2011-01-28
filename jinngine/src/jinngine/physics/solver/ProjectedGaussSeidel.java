/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */
package jinngine.physics.solver;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.util.Logger;
import jinngine.physics.constraint.*;

/**
 * Implementation of the Projected Gauss-Seidel solver.  
 */
public class ProjectedGaussSeidel implements Solver {
	private final int maximumIterations;
	private final double stagnation;
	
	public ProjectedGaussSeidel(int n, double stagnation ) {
		this.maximumIterations = n;
		this.stagnation = stagnation;
	}
	
	@Override
	public void setMaximumIterations(int n) {
		//this.maximumIterations = n;
	}

	@Override
	//solve NCP problem
	public final double solve(Iterable<Constraint> constraints, Iterable<Body> bodies, double epsilon) {
		double iterations = 0;
		double rnew = 0;
		double rold = 0;
		epsilon = 1e-3;
		
		// compute external force contribution, compute norm of the b-vector
		double bnorm = 0; 
		for (Constraint ci: constraints) {
			final Body body1 = ci.getBody1();
			final Body body2 = ci.getBody2();
			
			for (NCPConstraint ncpj: ci) {
				ncpj.Fext = ncpj.j1.dot(body1.externaldeltavelocity)
				+ ncpj.j2.dot(body1.externaldeltaomega)
				+ ncpj.j3.dot(body2.externaldeltavelocity) 
				+ ncpj.j4.dot(body2.externaldeltaomega)
				+ ncpj.b;

				bnorm += ncpj.Fext*ncpj.Fext;
			}
		} bnorm = Math.sqrt(bnorm);
		final double bnorminv;
		
		// avoid division by zero
		if (bnorm < 1e-15) {
			bnorm = 1.0;
			bnorminv= 1.0;
//			return 0.0;
		} else {
			bnorminv= 1.0/bnorm;			
		}
		
		// scale lambda values and b-vector
		for (Constraint ci: constraints) {
			for (NCPConstraint ncpj: ci) {
				ncpj.lambda *= bnorminv;
				ncpj.Fext *= bnorminv;
			}
		}
		
		// scale delta velocity in the bnorminv
		for (Body bi: bodies) {
			Vector3.multiply(bi.deltavelocity, bnorminv);
			Vector3.multiply(bi.deltaomega, bnorminv);
		}		
		
		// perform iterations
		for (int m=0; m<maximumIterations; m++) {
			rold = rnew;
			rnew = 0;
			for (final Constraint ci: constraints) {
				final Body body1 = ci.getBody1();				
				final Body body2 = ci.getBody2();
				final Matrix3 M1 = body1.state.inverseanisotropicmass;
				final Matrix3 I1 = body1.state.inverseinertia;
				final Matrix3 M2 = body2.state.inverseanisotropicmass;
				final Matrix3 I2 = body2.state.inverseinertia;
				final double b1mask = body1.isFixed()?0:1;
				final double b2mask = body2.isFixed()?0:1;
				

				for (final NCPConstraint ncpj: ci) {				
					// calculate (Ax+b)_i 
					final double w = ncpj.j1.dot(body1.deltavelocity) 
					+ ncpj.j2.dot(body1.deltaomega)
					+ ncpj.j3.dot(body2.deltavelocity) 
					+ ncpj.j4.dot(body2.deltaomega);
					
					final double diagonal = (Matrix3.transformAndDot(ncpj.j1, M1, ncpj.j1) 
			        + Matrix3.transformAndDot( ncpj.j2, I1, ncpj.j2))*b1mask
			        + (Matrix3.transformAndDot( ncpj.j3, M2, ncpj.j3) 
			        + Matrix3.transformAndDot( ncpj.j4, I2, ncpj.j4))*b2mask;


					// the change in lambda_i needed to obtain (Ax+b)_i = 0
					//				double deltaLambda = (-ci.b-w)/(ci.diagonal + ci.damper );
					double deltalambda = -(ncpj.Fext+w)/(diagonal);

					final double lambda0 = ncpj.lambda;

					//Clamp the lambda[i] value to the constraints
					if (ncpj.coupling != null) {
						//growing bounds
						//					double lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;					
						//					ci.lower = lower<ci.lower?lower:ci.lower;					
						//					double upper = Math.abs(ci.coupling.lambda)*ci.coupling.mu;
						//					ci.upper =  upper>ci.upper?upper:ci.upper;
						//if the constraint is coupled, allow only lambda <= coupled lambda
						ncpj.lower = -Math.abs(ncpj.coupling.lambda)*ncpj.coupling.mu;
						ncpj.upper =  Math.abs(ncpj.coupling.lambda)*ncpj.coupling.mu;
					} 

					//do projection
					ncpj.lambda =
						Math.max(ncpj.lower, Math.min(lambda0 + deltalambda,ncpj.upper ));

					// no projection
					//ci.lambda = lambda0 + deltaLambda;

					//update the V vector
					deltalambda = ncpj.lambda - lambda0;

					//update residual of change
					rnew += deltalambda*deltalambda;

					// apply to delta velocities
					Vector3.multiplyAndAdd(M1, ncpj.j1, deltalambda*b1mask, body1.deltavelocity);					
					Vector3.multiplyAndAdd(I1, ncpj.j2, deltalambda*b1mask, body1.deltaomega);
					Vector3.multiplyAndAdd(M2, ncpj.j3, deltalambda*b2mask, body2.deltavelocity);
					Vector3.multiplyAndAdd(I2, ncpj.j4, deltalambda*b2mask, body2.deltaomega);
					
				} // for ncp constraints	
			} // for constraints
			
			// termination criterion
			if (rnew < epsilon) {
				assert Logger.trace("ProjectedGaussSeidel: target epsilon reached, stopping");
				break;
			}
//			} else {
//				assert Logger.trace("ProjectedGaussSeidel: rnew="+rnew);				
//			}
			
			// stagnation test
			if ( iterations>0 && rnew/rold > stagnation) {
				assert Logger.trace("ProjectedGaussSeidel: stagnation="+(rnew/rold));
				break;
			}
 			
			iterations +=1;
		}
				
		// scale system back to original
		for (Constraint ci: constraints) {
			for (NCPConstraint ncpj: ci) {
				ncpj.lambda *= bnorm;
//				System.out.println("final lambda="+ncpj.lambda);
			}
		}
		
		// scale delta velocities back
		for (Body bi: bodies) {
			Vector3.multiply(bi.deltavelocity, bnorm);
			Vector3.multiply(bi.deltaomega, bnorm);
		}
		

		
		assert Logger.trace("ProjectedGaussSeidel: finshed at deltaResidual="+rnew+", iterations="+iterations);
		return iterations ;
	}
}
