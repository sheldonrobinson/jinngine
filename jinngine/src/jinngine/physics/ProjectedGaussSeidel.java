package jinngine.physics;
import java.util.*;

import jinngine.math.Vector3;
import jinngine.util.Tuple;

/**
 * Implementation of the PGS solver. The PGS solver is derived from an iterative scheme.
 * @author mo
 *
 */
public class ProjectedGaussSeidel implements Solver {
	Map<Body,Tuple<Vector3,Vector3>> bodymap = new HashMap<Body,Tuple<Vector3,Vector3>>();

	private double epsilon = 0.00000001;
	private int maximumIterations = 5;
	
	
	@Override
	public void setErrorTolerance(double epsilon) {
		this.epsilon = epsilon;
	}

	@Override
	public void setMaximumIterations(int n) {
		this.maximumIterations = n;
	}

	@Override
	//solve J M^-1 J^T lambda = b
	public final double solve(List<ConstraintEntry> constraints, List<Body> bodies) {		
		//System.out.println("PGS: " + constraints.size() + " constraints");
		
		//clear impulses
//		Iterator<Map.Entry<Body,Tuple<Vector3,Vector3>>> iterator = bodymap.entrySet().iterator();
//		while (iterator.hasNext()) {
//			Map.Entry<Body,Tuple<Vector3,Vector3>> e = iterator.next();
//			e.getValue().first.assignZero();
//			e.getValue().second.assignZero();			
//		}

		//set limits
//		for (ConstraintEntry c: constraints) {
//			//clamb the lambda[i] value to the constraints
//			if (c.coupledMax != null) {
//				double mu = 0.5;				//if the constraint is coupled, allow only lambda <= coupled lambda
//				c.lambdaMin = -c.coupledMax.lambda*mu; 
//				c.lambdaMax = c.coupledMax.lambda*mu ;					
//			}
//
//		}
		
		
		//perform iterations
		for (int m=0; m<maximumIterations; m++) {
			//System.out.println("PGS: " + constraints.size() );
			boolean residualLow = true;
			for (ConstraintEntry constraint: constraints) {
				Vector3 b1deltaV;
				Vector3 b1deltaOmega;
				Vector3 b2deltaV;
				Vector3 b2deltaOmega;

				Body b1 = constraint.body1;
				Body b2 = constraint.body2;

				//check for bodies exist in the delta velocity tables
//				if (bodymap.containsKey(b1)) {
//					Tuple<Vector3,Vector3> t = bodymap.get(b1);
//					b1deltaV = t.first;
//					b1deltaOmega = t.second;
//				} else {
//					b1deltaV = new Vector3();
//					b1deltaOmega = new Vector3();
//					bodymap.put(b1, new Tuple<Vector3,Vector3>(b1deltaV,b1deltaOmega));
//				}
//
//				if (bodymap.containsKey(b2)) {
//					Tuple<Vector3,Vector3> t = bodymap.get(b2);
//					b2deltaV = t.first;
//					b2deltaOmega = t.second;
//				} else {
//					b2deltaV = new Vector3();
//					b2deltaOmega = new Vector3();
//					bodymap.put(b2, new Tuple<Vector3,Vector3>(b2deltaV,b2deltaOmega));
//				}

				double a =  constraint.j1.dot(constraint.body1.deltaVCm) 
				+ constraint.j2.dot(constraint.body1.deltaOmegaCm)
				+  constraint.j3.dot(constraint.body2.deltaVCm) 
				+ constraint.j4.dot(constraint.body2.deltaOmegaCm);

				double deltaLambda = (constraint.b - a)/(constraint.diagonal );
				double lambda0 = constraint.lambda;


				//clamb the lambda[i] value to the constraints
				if (constraint.coupledMax != null) {
					double mu = 0.5;
					constraint.lambdaMin = 
					//if the constraint is coupled, allow only lambda <= coupled lambda
					constraint.lambdaMin = -Math.abs(constraint.coupledMax.lambda)*mu;
					constraint.lambdaMax =  Math.abs(constraint.coupledMax.lambda)*mu;
				} 

				//do projection
				constraint.lambda =
					Math.max(constraint.lambdaMin, Math.min(lambda0 + deltaLambda,constraint.lambdaMax ));
					
				//update the V vector
				deltaLambda = constraint.lambda - lambda0;

				//System.out.println("residual :" + deltaLambda*constraint.diagonal);

				if (Math.abs(deltaLambda*constraint.diagonal) > epsilon )
					residualLow = false;

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

//		//copy impulses to bodies
//	    iterator = bodymap.entrySet().iterator();
//		while (iterator.hasNext()) {
//			Map.Entry<Body,Tuple<Vector3,Vector3>> e = iterator.next();
//			Body body = e.getKey();
//			Tuple<Vector3,Vector3> t = e.getValue();
//			//only apply impulse if any change
//			if ( !t.first.isZero() || !t.second.isZero() ) {
//				body.deltaVCm.assign(t.first);
//				body.deltaOmegaCm.assign(t.second);
//			}
//		}
		
		
		for ( ConstraintEntry c: constraints) {
			//System.out.println("lambda="+c.lambda);
		}
		
		return 0;
		
	}


}
