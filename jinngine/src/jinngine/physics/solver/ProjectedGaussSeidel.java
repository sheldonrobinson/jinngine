package jinngine.physics.solver;
import java.util.*;
import jinngine.math.Vector3;
import jinngine.physics.Body;

/**
 * Implementation of the PGS solver. The PGS solver is derived from an iterative scheme.
 * @author mo
 *
 */
public class ProjectedGaussSeidel implements Solver {

	private int maximumIterations = 15;
	
	
	@Override
	public void setErrorTolerance(double epsilon) {
		//this.epsilon = epsilon;
	}

	@Override
	public void setMaximumIterations(int n) {
		System.out.println("PGS iterations = " + n);
		this.maximumIterations = n;
	}

	@Override
	//solve NCP problem
	public final double solve(List<ConstraintEntry> constraints, List<Body> bodies) {		
		//System.out.println("PGS: " + constraints.size() + " constraints");
		
		//perform iterations
		for (int m=0; m<maximumIterations; m++) {
			//System.out.println("PGS: " + constraints.size() );
//			boolean residualLow = true;
			for (ConstraintEntry constraint: constraints) {

				double a =  constraint.j1.dot(constraint.body1.deltaVCm) 
				+ constraint.j2.dot(constraint.body1.deltaOmegaCm)
				+  constraint.j3.dot(constraint.body2.deltaVCm) 
				+ constraint.j4.dot(constraint.body2.deltaOmegaCm);

				double deltaLambda = (constraint.b - a)/(constraint.diagonal );
				double lambda0 = constraint.lambda;

				//clamb the lambda[i] value to the constraints
				if (constraint.coupledMax != null) {
					//double mu = Math.sqrt(2)/2.0;
					constraint.lambdaMin = 
					//if the constraint is coupled, allow only lambda <= coupled lambda
					constraint.lambdaMin = -Math.abs(constraint.coupledMax.lambda)*constraint.coupledMax.mu;
					constraint.lambdaMax =  Math.abs(constraint.coupledMax.lambda)*constraint.coupledMax.mu;
				} 

				//do projection
				constraint.lambda =
					Math.max(constraint.lambdaMin, Math.min(lambda0 + deltaLambda,constraint.lambdaMax ));
					
				//update the V vector
				deltaLambda = constraint.lambda - lambda0;

				//System.out.println("residual :" + deltaLambda*constraint.diagonal);

//				if (Math.abs(deltaLambda*constraint.diagonal) > epsilon )
//					residualLow = false;

				//Apply to delta velocities
				Vector3.add( constraint.body1.deltaVCm,     constraint.b1.multiply(deltaLambda) );
				Vector3.add( constraint.body1.deltaOmegaCm, constraint.b2.multiply(deltaLambda) );
				Vector3.add( constraint.body2.deltaVCm,     constraint.b3.multiply(deltaLambda));
				Vector3.add( constraint.body2.deltaOmegaCm, constraint.b4.multiply(deltaLambda));
			} //for constraints

			//exit on low error
			//if (residualLow) {
			//	break;
			//}
			
		}
		
		return 0;
	}
}
