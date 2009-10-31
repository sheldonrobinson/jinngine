package jinngine.physics.solver;

import java.util.List;

import jinngine.math.Vector3;
import jinngine.physics.Body;

/**
 * Solves the given list of constraints, as if they described a plain symmetric 
 * linear system of equations, not a NCP problem. Phased in another way, CG can solve 
 * the NCP, if l=-infinity and u=infinity for all constraints
 */
public class ConjugateGradients implements Solver {

	int maxIterations = 100;
	
	@Override
	public void setMaximumIterations(int n) {
		// TODO Auto-generated method stub
	}

	@Override
	public double solve(List<constraint> constraints, List<Body> bodies) {
		
		//maxIterations = constraints.size();
		
		//Conjugate Gradients
		double delta_new=0, delta_old=0, delta_zero=0;
		double delta_low=Double.POSITIVE_INFINITY;
		double epsilon = 1e-5;
		int iterations=0;
		
		for (Body b: bodies) {
			b.auxDeltav.assignZero();
			b.auxDeltaOmega.assignZero();
		}
		
		//r = d = b-Ax 
		//delta_new = rTr
		for (constraint ci: constraints) {
			ci.residual = ci.d = 
				(-ci.b) - (ci.j1.dot(ci.body1.deltaVCm) + ci.j2.dot(ci.body1.deltaOmegaCm)
						+ ci.j3.dot(ci.body2.deltaVCm) + ci.j4.dot(ci.body2.deltaOmegaCm));
			
			//reflect d in the delta velocities
			Vector3.add( ci.body1.auxDeltav,     ci.b1.multiply(ci.d));
			Vector3.add( ci.body1.auxDeltaOmega, ci.b2.multiply(ci.d));
			Vector3.add( ci.body2.auxDeltav,     ci.b3.multiply(ci.d));
			Vector3.add( ci.body2.auxDeltaOmega, ci.b4.multiply(ci.d));

			delta_new += ci.residual * ci.residual;
		} 			
				
		delta_old = delta_new;
		delta_zero = delta_new;
		
		//CG iterations
		while (iterations < maxIterations && delta_new>epsilon*epsilon*delta_zero && delta_new > epsilon) {
			
			//System.out.println("iteration " + iterations +", delta_new=" + delta_new);
			//if (delta_new<delta_low) { delta_low = delta_new; for(j=0;j<n;j++) z_low[j] = z[j]; }
			//if (k > cgmax) { for(j=0;j<n;j++) dk[j] = z_low[j]; break;}
			
			//q = Ad
			//alpha = delta_new/dTq
			double dTq = 0;
			for (constraint ci: constraints) {
				ci.q = ci.j1.dot(ci.body1.auxDeltav) + ci.j2.dot(ci.body1.auxDeltaOmega)
				+ ci.j3.dot(ci.body2.auxDeltav) + ci.j4.dot(ci.body2.auxDeltaOmega);
				
				dTq += ci.d*ci.q;
			} 			

			if (dTq<epsilon) {
				//System.out.println("orthogonal search direction");
				System.out.println("iteration " + iterations );

				return 0;
			}
			
			double alpha = delta_new/dTq;
			delta_old = delta_new;
			delta_new = 0;

			for (constraint ci: constraints) {
				//x = x + alpha d
				ci.lambda -= alpha*ci.d;
				
				//reflect in delta velocities
				Vector3.add( ci.body1.deltaVCm,     ci.b1.multiply(-alpha*ci.d));
				Vector3.add( ci.body1.deltaOmegaCm, ci.b2.multiply(-alpha*ci.d));
				Vector3.add( ci.body2.deltaVCm,     ci.b3.multiply(-alpha*ci.d));
				Vector3.add( ci.body2.deltaOmegaCm, ci.b4.multiply(-alpha*ci.d));

				//r = r-alpha q
				ci.residual -= alpha*ci.q;

				//delta_new = rTr
				delta_new += ci.residual*ci.residual;
			}
			
			double beta = delta_new/delta_old;

			//d = r + beta d
			for (Body b: bodies) {
				Vector3.multiply( b.auxDeltav,     beta);
				Vector3.multiply( b.auxDeltaOmega, beta);				
			}			
			for (constraint ci: constraints) {
				ci.d = ci.residual + beta* ci.d;
				//reflect d in the delta velocities
				Vector3.add( ci.body1.auxDeltav,     ci.b1.multiply(ci.residual));
				Vector3.add( ci.body1.auxDeltaOmega, ci.b2.multiply(ci.residual));
				Vector3.add( ci.body2.auxDeltav,     ci.b3.multiply(ci.residual));
				Vector3.add( ci.body2.auxDeltaOmega, ci.b4.multiply(ci.residual));
			}	
			
			iterations += 1;
			
			System.out.println("iteration " + iterations +", delta_new=" + delta_new +", alpha=" +alpha);

		} //CG iterations

		System.out.println("iteration " + iterations );

		return 0;
	}

}
