package jinngine.physics.solver;
import java.util.*;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.Solver.constraint;
import jinngine.physics.solver.experimental.FischerNewton;

/**
 * Implementation of the PGS solver. 
 */
public class ProjectedGaussSeidel implements Solver {
	private int maximumIterations = 35;
	private double deltaResidual = 0;	
		
	public ProjectedGaussSeidel() {}
	
	public ProjectedGaussSeidel(int n) {
		maximumIterations = n;
	}
	
	@Override
	public void setMaximumIterations(int n) {
		//this.maximumIterations = n;
	}

	@Override
	//solve NCP problem
	public final double solve(List<constraint> constraints, List<Body> bodies, double epsilon) {
		double iterations = 0;
		//perform iterations
		for (int m=0; m<maximumIterations; m++) {
			deltaResidual = 0;
			for (constraint ci: constraints) {
				//calculate (Ax+b)_i 
				double w =  ci.j1.dot(ci.body1.deltavelocity) 
				+ ci.j2.dot(ci.body1.deltaomega)
				+  ci.j3.dot(ci.body2.deltavelocity) 
				+ ci.j4.dot(ci.body2.deltaomega) + ci.lambda*ci.damper;

				double deltaLambda = (-ci.b-w)/(ci.diagonal + ci.damper );
				double lambda0 = ci.lambda;

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
				
				//update the V vector
				deltaLambda = ci.lambda - lambda0;
				
				//update residual of change
				deltaResidual += deltaLambda*deltaLambda;

				//Apply to delta velocities
				Vector3.add( ci.body1.deltavelocity,     ci.b1.multiply(deltaLambda) );
				Vector3.add( ci.body1.deltaomega, ci.b2.multiply(deltaLambda) );
				Vector3.add( ci.body2.deltavelocity,     ci.b3.multiply(deltaLambda));
				Vector3.add( ci.body2.deltaomega, ci.b4.multiply(deltaLambda));
				
			} //for constraints	
			iterations +=1;
		}
		return iterations ;
	}
}
