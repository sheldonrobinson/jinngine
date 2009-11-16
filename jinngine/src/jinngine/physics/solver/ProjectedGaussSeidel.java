package jinngine.physics.solver;
import java.util.*;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.Solver.constraint;

/**
 * Implementation of the PGS solver. 
 */
public class ProjectedGaussSeidel implements Solver {

	private int maximumIterations = 20;
	private double damper = 0.00;
	private double deltaResidual = 0;
	private boolean bounds = true;
	
	public void setUpdateBounds(boolean bounds) {
		this.bounds = bounds;
	}
	
	public void setDamping(double damping) {
		this.damper = damping;
	}

	public ProjectedGaussSeidel(int n) {
		this.maximumIterations = n;
	}
	
	@Override
	public void setMaximumIterations(int n) {
		this.maximumIterations = n;
	}

	@Override
	//solve NCP problem
	public final double solve(List<constraint> constraints, List<Body> bodies) {
//		for (constraint ci: constraints) {
//			//System.out.println(ci.lower+"-"+ci.upper);
//
//			if (ci.coupling != null) 
//				ci.damper = 0.1;
//			else
//				ci.damper = damper;
//		}
		//perform iterations
		for (int m=0; m<maximumIterations; m++) {
			deltaResidual = 0;
			for (constraint ci: constraints) {
				//calculate (Ax+b)_i 
				double w =  ci.j1.dot(ci.body1.deltaVCm) 
				+ ci.j2.dot(ci.body1.deltaOmegaCm)
				+  ci.j3.dot(ci.body2.deltaVCm) 
				+ ci.j4.dot(ci.body2.deltaOmegaCm) + ci.lambda*ci.damper;

				double deltaLambda = (-ci.b-w)/(ci.diagonal + ci.damper );
				double lambda0 = ci.lambda;

				//Clamp the lambda[i] value to the constraints
				if (ci.coupling != null && bounds) {
					//if the constraint is coupled, allow only lambda <= coupled lambda
					ci.lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;
					ci.upper =  Math.abs(ci.coupling.lambda)*ci.coupling.mu;
					
//					 double lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;					
//						ci.lower = lower<ci.lower?lower:ci.lower;					
//						double upper = Math.abs(ci.coupling.lambda)*ci.coupling.mu;
//						ci.upper =  upper>ci.upper?upper:ci.upper;

				} 

				//do projection
				ci.lambda =
					Math.max(ci.lower, Math.min(lambda0 + deltaLambda,ci.upper ));
//				constraint.lambda = lambda0 + deltaLambda;
				
				//update the V vector
				deltaLambda = ci.lambda - lambda0;
				
				//update residual of change
				deltaResidual += deltaLambda*deltaLambda;

				//Apply to delta velocities
				Vector3.add( ci.body1.deltaVCm,     ci.b1.multiply(deltaLambda) );
				Vector3.add( ci.body1.deltaOmegaCm, ci.b2.multiply(deltaLambda) );
				Vector3.add( ci.body2.deltaVCm,     ci.b3.multiply(deltaLambda));
				Vector3.add( ci.body2.deltaOmegaCm, ci.b4.multiply(deltaLambda));
			} //for constraints			
		}
		return 0;
	}
}
