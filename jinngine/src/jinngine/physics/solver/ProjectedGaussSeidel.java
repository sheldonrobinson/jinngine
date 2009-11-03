package jinngine.physics.solver;
import java.util.*;
import jinngine.math.Vector3;
import jinngine.physics.Body;

/**
 * Implementation of the PGS solver. 
 */
public class ProjectedGaussSeidel implements Solver {

	private int maximumIterations = 20;
	private double damper = 0.0;
	
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
		//perform iterations
		for (int m=0; m<maximumIterations; m++) {
			for (constraint constraint: constraints) {
				//calculate (Ax+b)_i 
				double w =  constraint.j1.dot(constraint.body1.deltaVCm) 
				+ constraint.j2.dot(constraint.body1.deltaOmegaCm)
				+  constraint.j3.dot(constraint.body2.deltaVCm) 
				+ constraint.j4.dot(constraint.body2.deltaOmegaCm);

				double deltaLambda = (constraint.b - w)/(constraint.diagonal + damper );
				double lambda0 = constraint.lambda;

				//Clamp the lambda[i] value to the constraints
				if (constraint.coupling != null) {
					//if the constraint is coupled, allow only lambda <= coupled lambda
					constraint.lower = -Math.abs(constraint.coupling.lambda)*constraint.coupling.mu;
					constraint.upper =  Math.abs(constraint.coupling.lambda)*constraint.coupling.mu;
				} 

				//do projection
				constraint.lambda =
					Math.max(constraint.lower, Math.min(lambda0 + deltaLambda,constraint.upper ));
//				constraint.lambda = lambda0 + deltaLambda;
				
				//update the V vector
				deltaLambda = constraint.lambda - lambda0;

				//Apply to delta velocities
				Vector3.add( constraint.body1.deltaVCm,     constraint.b1.multiply(deltaLambda) );
				Vector3.add( constraint.body1.deltaOmegaCm, constraint.b2.multiply(deltaLambda) );
				Vector3.add( constraint.body2.deltaVCm,     constraint.b3.multiply(deltaLambda));
				Vector3.add( constraint.body2.deltaOmegaCm, constraint.b4.multiply(deltaLambda));
			} //for constraints
		}
		
		return 0;
	}
}
