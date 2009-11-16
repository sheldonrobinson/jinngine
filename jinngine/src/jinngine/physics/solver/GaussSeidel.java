package jinngine.physics.solver;

import java.util.List;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.Solver.constraint;

public class GaussSeidel implements Solver {
	private int maximumIterations = 25;
	@Override
	public void setDamping(double damping) {
		// TODO Auto-generated method stub	
	}
	public GaussSeidel(int n) {
		this.maximumIterations = n;
	}
	
	@Override
	public void setMaximumIterations(int n) {
		this.maximumIterations = n;
	}

	@Override
	//solve linear system of equations 
	public final double solve(List<constraint> constraints, List<Body> bodies) {
		//perform iterations
		for (int m=0; m<maximumIterations; m++) {
			for (constraint ci: constraints) {
				//calculate (Ax+b)_i 
				double w =  ci.j1.dot(ci.body1.deltaVCm) 
				+ ci.j2.dot(ci.body1.deltaOmegaCm)
				+  ci.j3.dot(ci.body2.deltaVCm) 
				+ ci.j4.dot(ci.body2.deltaOmegaCm) + ci.lambda*ci.damper;

				double deltaLambda = (-ci.b - w)/(ci.diagonal + ci.damper );
				double lambda0 = ci.lambda;
				
				//update the V vector
				deltaLambda = ci.lambda - lambda0;
				
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
