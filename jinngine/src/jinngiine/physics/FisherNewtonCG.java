package jinngiine.physics;

import java.util.List;

import jinngine.math.Vector3;
import jinngine.physics.ConstraintEntry;
import jinngine.physics.Solver;

public class FisherNewtonCG implements Solver {

	@Override
	public void setErrorTolerance(double epsilon) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaximumIterations(int n) {
		// TODO Auto-generated method stub

	}

	
	private static double fisher(double a, double b) {
		return Math.sqrt(a*a+b*b)-(a+b);
	}
	
	@Override
	public double solve(List<ConstraintEntry> constraints) {
		int n = constraints.size();
		double error = 0;
		double epsilon = 1e-7;
		int i = 0;
		int imax = 10;

		//compute fisher
		for (ConstraintEntry ci: constraints) {
			//velocity
			double w = ci.j1.dot(ci.body1.deltaVCm) + ci.j2.dot(ci.body1.deltaOmegaCm)
			+ ci.j3.dot(ci.body2.deltaVCm) + ci.j4.dot(ci.body2.deltaOmegaCm) + (-ci.b);

			//fisher
			if (ci.coupledMax == null) ci.phixk = -( Math.sqrt( w*w+ci.lambda*ci.lambda )-w-ci.lambda);
			else  ci.phixk = - ( fisher( ci.lambda-ci.lambdaMin, fisher(ci.lambdaMax-ci.lambda, -w)));
		
			error += ci.phixk*ci.phixk;
		} error = Math.sqrt(error);	
		
		
		// Newton loop
		while (true) {
			
			//termination condition
			if (error<epsilon || i>imax) {
				return error;
			}

			//tolerance for sub-system
			double tol = 0;
			if ( error < 1) tol = 0.5*error*error;
			else tol = 0.5*Math.sqrt(error);
			
			//conjugate gradients
			double[] r = new double[n];
			double[] d = new double[n];
			double[] z = new double[n];
			double[] z_low = new double[n];			
			double[] dk = new double[n];
			double delta_new = 0; 
			double h = 1e-5;
			int cgmax = 15;
			int k = 0;
			//r= Phi(xk)
			int j = 0;
			for (ConstraintEntry ci: constraints) {
				r[j] = ci.phixk;
				d[j] = r[j];
				j++;
			}	
			// delta_new = rTr
			for (j=0;j<n;j++)
				delta_new += r[j]*r[j];			
			double delta_old = delta_new, delta_low = Double.POSITIVE_INFINITY;
			
			while (true) {
				//termination
				if (delta_new<tol) {break;}
				if (delta_new<delta_low) { delta_low = delta_new; for(j=0;j<n;j++) z_low[j] = z[j]; }
				if (k > cgmax) { for(j=0;j<n;j++) dk[j] = z_low[j];
				
				//conjugate gradient
				// compute Phi( xk + zk*h)
				j=0;
				for (ConstraintEntry ci: constraints) {
					double delta = d[j]*h;
					Vector3.add( ci.body1.auxDeltav,     ci.b1.multiply(delta));
					Vector3.add( ci.body1.auxDeltaOmega, ci.b2.multiply(delta));
					Vector3.add( ci.body2.auxDeltav,     ci.b3.multiply(delta));
					Vector3.add( ci.body2.auxDeltaOmega, ci.b4.multiply(delta));
					i = i+1;
				}

				}
			}
			
			
			
		}
		
		
		
		return 0;
	}

	
	
	public void computeVelocity2(List<ConstraintEntry> constraints, List<ConstraintEntry> off,  double[] lambda, double[] w, double damper) {	
		//clear auxiliary fields
		for (ConstraintEntry ci: constraints) {
			ci.body1.auxDeltav.assignZero();
			ci.body1.auxDeltaOmega.assignZero();
			ci.body2.auxDeltav.assignZero();
			ci.body2.auxDeltaOmega.assignZero();
		}

		// Jh M-1J^t x+ Ic x + b = 0
		
		//apply contributions from all constraints to bodies
		int i = 0;
		for (ConstraintEntry ci: constraints) {
			Vector3.add( ci.body1.auxDeltav,     ci.b1.multiply(lambda[i] )  );
			Vector3.add( ci.body1.auxDeltaOmega, ci.b2.multiply(lambda[i] ) );
			Vector3.add( ci.body2.auxDeltav,     ci.b3.multiply(lambda[i] ) );
			Vector3.add( ci.body2.auxDeltaOmega, ci.b4.multiply(lambda[i] ) );
			i = i+1;
		}

		for (ConstraintEntry ci: off) {
			Vector3.add( ci.body1.auxDeltav,     ci.b1.multiply(ci.lambda )  );
			Vector3.add( ci.body1.auxDeltaOmega, ci.b2.multiply(ci.lambda ) );
			Vector3.add( ci.body2.auxDeltav,     ci.b3.multiply(ci.lambda ) );
			Vector3.add( ci.body2.auxDeltaOmega, ci.b4.multiply(ci.lambda ) );
			i = i+1;
		}

		
		i = 0;
		for (ConstraintEntry ci: constraints) {
			w[i] = ci.j1.dot(ci.body1.auxDeltav) + ci.j2.dot(ci.body1.auxDeltaOmega)
			     + ci.j3.dot(ci.body2.auxDeltav) + ci.j4.dot(ci.body2.auxDeltaOmega) + (-ci.b) + damper*lambda[i];
			i = i+1;
		}			
	}
	
	
	
	public void computeFisher( List<ConstraintEntry> constraints, double[] lambda, double w[], double[] b) {
		//evaluate b vector using fisher
		int i=0;
		for (ConstraintEntry c: constraints) {
			//get the b vector and starting lambda values
			if (c.coupledMax == null) {
				
				b[i] = -( Math.sqrt( w[i]*w[i]+ lambda[i]*lambda[i] )-w[i]-lambda[i]);
			} else {
//				double limit = Math.abs(c.coupledMax.lambda)*friction;
//				c.lambdaMin = -limit;
//				c.lambdaMax  = limit;
				

				b[i] = - ( fisher( lambda[i]-c.lambdaMin, fisher(c.lambdaMax-lambda[i], -w[i])));
			}
			i++;
		}
		
	}

}
