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
import java.util.*;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.util.Logger;

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
	public final double solve(List<NCPConstraint> constraints, List<Body> bodies, double epsilon) {
		double iterations = 0;
		double rnew = 0;
		double rold = 0;
		epsilon = 1e-31;
		
		// compute external force contribution, compute norm of the b-vector
		double bnorm = 0; 
		for (NCPConstraint ci: constraints) {
			ci.Fext = ci.j1.dot(ci.body1.externaldeltavelocity)
			        + ci.j2.dot(ci.body1.externaldeltaomega)
	                + ci.j3.dot(ci.body2.externaldeltavelocity) 
                    + ci.j4.dot(ci.body2.externaldeltaomega);
			
			bnorm += (ci.b+ci.Fext)*(ci.b+ci.Fext);			
		} bnorm = Math.sqrt(bnorm);
		final double bnorminv;
		
		// avoid division by zero
		if (bnorm < 1e-15) {
			bnorm = 1.0;
			bnorminv= 1.0;			
		} else {
			bnorminv= 1.0/bnorm;			
		}
		
		// scale system
		for (NCPConstraint ci: constraints) {
			ci.lambda *= bnorminv;
		}
		for (Body bi: bodies) {
			Vector3.multiply(bi.deltavelocity, bnorminv);
			Vector3.multiply(bi.deltaomega, bnorminv);
		}
		
		
		// perform iterations
		for (int m=0; m<maximumIterations; m++) {
			rold = rnew;
			rnew = 0;
			for (NCPConstraint ci: constraints) {				
				// calculate (Ax+b)_i 
				final double w = ci.j1.dot(ci.body1.deltavelocity) 
                               + ci.j2.dot(ci.body1.deltaomega)
                               + ci.j3.dot(ci.body2.deltavelocity) 
                               + ci.j4.dot(ci.body2.deltaomega);
				
				// the change in lambda_i needed to obtain (Ax+b)_i = 0
//				double deltaLambda = (-ci.b-w)/(ci.diagonal + ci.damper );
			    double deltaLambda = -((ci.b+ci.Fext)*bnorminv+w)/(ci.diagonal);

				final double lambda0 = ci.lambda;

				//Clamp the lambda[i] value to the constraints
				if (ci.coupling != null) {
					//growing bounds
//					double lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;					
//					ci.lower = lower<ci.lower?lower:ci.lower;					
//					double upper = Math.abs(ci.coupling.lambda)*ci.coupling.mu;
//					ci.upper =  upper>ci.upper?upper:ci.upper;
					//if the constraint is coupled, allow only lambda <= coupled lambda
					ci.lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;
					ci.upper =  Math.abs(ci.coupling.lambda)*ci.coupling.mu;
				} 

				//do projection
				ci.lambda =
					Math.max(ci.lower, Math.min(lambda0 + deltaLambda,ci.upper ));

				// no projection
				//ci.lambda = lambda0 + deltaLambda;
							
				//update the V vector
				deltaLambda = ci.lambda - lambda0;
				
				//update residual of change
				rnew += deltaLambda*deltaLambda;

				//Apply to delta velocities
				Vector3.add( ci.body1.deltavelocity,     ci.b1.multiply(deltaLambda) );
				Vector3.add( ci.body1.deltaomega, ci.b2.multiply(deltaLambda) );
				Vector3.add( ci.body2.deltavelocity,     ci.b3.multiply(deltaLambda));
				Vector3.add( ci.body2.deltaomega, ci.b4.multiply(deltaLambda));
				
			} //for constraints	
			
			// termination criterion
			if (rnew < epsilon) {
				assert Logger.trace("ProjectedGaussSeidel: target epsilon reached, stopping");
				break;
			}
			
			// stagnation test
			if ( iterations>0 && rnew/rold > stagnation) {
				assert Logger.trace("ProjectedGaussSeidel: stagnation="+(rnew/rold));
				break;
			}
 			
			iterations +=1;
		}
		
		
		// scale system back to original
		for (NCPConstraint ci: constraints) {
			ci.lambda *= bnorm;
		}
		for (Body bi: bodies) {
			Vector3.multiply(bi.deltavelocity, bnorm);
			Vector3.multiply(bi.deltaomega, bnorm);
		}
		
		assert Logger.trace("ProjectedGaussSeidel: finshed at deltaResidual="+rnew+", iterations="+iterations);
		return iterations ;
	}
}
