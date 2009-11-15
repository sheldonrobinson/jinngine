package jinngine.physics.solver;

import java.util.List;
import jinngine.math.Vector3;
import jinngine.physics.Body;

/**
 * This is not a real solver, it can simply project the given constraints into their proper limits,
 * which is equivalent to PGS using only the projection part of it. 
 */
public class Projection implements Solver {
	@Override
	public final double solve(List<constraint> constraints, List<Body> bodies) {
		boolean projected = false;
		
		for (constraint ci: constraints) {
			double deltaLambda = 0;
			double lambda0 = ci.lambda;

//			//Clamp the lambda[i] value to the constraints
//			if (ci.coupling != null) {
//				//if the constraint is coupled, allow only lambda <= coupled lambda
//				ci.lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;
//				ci.upper =  Math.abs(ci.coupling.lambda)*ci.coupling.mu;
//			} 

			//do projection
			ci.lambda =
				Math.max(ci.lower, Math.min(lambda0 + deltaLambda,ci.upper ));
			
			//friction 
//			if (ci.coupling !=null) {
//				double w = 
//					 (ci.j1.dot(ci.body1.deltaVCm) + ci.j2.dot(ci.body1.deltaOmegaCm)
//							+ ci.j3.dot(ci.body2.deltaVCm) + ci.j4.dot(ci.body2.deltaOmegaCm));
//
//				if (w>100 ) {
//					ci.lambda = ci.lower;
//				} else if (w<-100) {
//					ci.lambda = ci.upper;
//				}
//			}

			//update the V vector
			deltaLambda = ci.lambda - lambda0;
			
			if (deltaLambda!=0)
				projected = true;

			//Apply to delta velocities
			Vector3.add( ci.body1.deltaVCm,     ci.b1.multiply(deltaLambda) );
			Vector3.add( ci.body1.deltaOmegaCm, ci.b2.multiply(deltaLambda) );
			Vector3.add( ci.body2.deltaVCm,     ci.b3.multiply(deltaLambda));
			Vector3.add( ci.body2.deltaOmegaCm, ci.b4.multiply(deltaLambda));
		} //for constraints
		return projected?1:0;
	}

	@Override
	public void setMaximumIterations(int n) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDamping(double damping) {
		// TODO Auto-generated method stub
		
	}

}
