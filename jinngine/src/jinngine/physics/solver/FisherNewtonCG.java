package jinngine.physics.solver;

import java.util.List;

import jinngine.math.Vector3;
import jinngine.physics.Body;

public class FisherNewtonCG implements Solver {

	@Override
	public void setErrorTolerance(double epsilon) {
		this.epsilon = epsilon;
	}

	@Override
	public void setMaximumIterations(int n) {
		this.imax = n;
	}

	public void setMaximumIterations2(int n) {
		this.imax2 = n;
	}

	
	public void setMaximumCGIterations(int n) {
		this.cgmax = n;
	}

	
	public void setLinesearchIterations(int n) {
		this.linemax =n;
	}

	public void setDamping(double d) {
		this.damper =d;
	}

	public void setFrictionDamping(double d) {
		this.frictiondamp =d;
	}

	
	private static double fisher(double a, double b) {
		return Math.sqrt(a*a+b*b)-(a+b);
	}

	private int linemax =6;
	private int imax = 10;
	private int imax2 = 10;
	private int cgmax = 15;
	private double damper =0;
	private double frictiondamp = 0;
	private double epsilon = 1e-7;
	
	
	@Override
	public double solve(List<ConstraintEntry> constraints, List<Body> bodies) {
		int n = constraints.size();
		double error = 0;
		double besterror = Double.POSITIVE_INFINITY;
		double squarederror = 0; 
		//double epsilon = 1e-7;
		int i = 0;
		double mu = 1;
		//double damper = 0.000001;
		
		//friction velocity trucation
		double truncatevelocity = 0;
		
		
		//compute fisher
		for (ConstraintEntry ci: constraints) {
			//velocity
			double w = ci.j1.dot(ci.body1.deltaVCm) + ci.j2.dot(ci.body1.deltaOmegaCm)
			+ ci.j3.dot(ci.body2.deltaVCm) + ci.j4.dot(ci.body2.deltaOmegaCm) + (-ci.b) + damper*ci.lambda;

			//fisher
			if (ci.coupledMax == null) ci.phixk =  Math.sqrt( w*w+ci.lambda*ci.lambda )-w-ci.lambda;
			else  {
				//apply friction damping
				w += frictiondamp*ci.lambda;
				
				//recompute friction limits
				double limit = Math.abs(ci.coupledMax.lambda)*mu; ci.lambdaMin = -limit; ci.lambdaMax = limit;
				if (Math.abs(w)<truncatevelocity ) {w=0;}
				
				ci.phixk =  fisher( ci.lambda-ci.lambdaMin, fisher(ci.lambdaMax-ci.lambda, -w));
			}
		
			squarederror += ci.phixk*ci.phixk;
		} error = Math.sqrt(squarederror);	
		
		
		// Newton loop
		while (true) {
			//System.out.println("error="+error);
			
			
			
			//termination condition
			if (error<epsilon || i>imax ) {
				
				if (besterror < error) {
					//copy best solution to delta velocities
					for (Body b: bodies) {
						Vector3.assign(b.deltaVCm, b.auxDeltav2);
						Vector3.assign(b.deltaOmegaCm,b.auxDeltaOmega2);				
					}
					
					return besterror;
				}
				
				return error;
			}
			


			
			//tolerance for sub-system
			double tol = 0;
			if ( error < 1) tol = 0.5*error*error;
			else tol = 0.5*Math.sqrt(error);
			
			//solve sub-system
			double[] r = new double[n];
			double[] d = new double[n];
			double[] z = new double[n];
			double[] z_low = new double[n];			
			double[] dk = new double[n];
			double[] q = new double[n];
			double delta_new = 0; 
			double h = 1e-5;
			int k = 0;
			
			//r= Phi(xk), d = r
			int j = 0; for (ConstraintEntry ci: constraints) {
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
				if (delta_new<tol) { for(j=0;j<n;j++) dk[j]=z[j]; break;}
				if (delta_new<delta_low) { delta_low = delta_new; for(j=0;j<n;j++) z_low[j] = z[j]; }
				if (k > cgmax) { for(j=0;j<n;j++) dk[j] = z_low[j]; break;}
				
				//conjugate gradients (internal)
				
				// compute Phi( xk + zk*h)
				// clear velocities
				for (Body b: bodies) {
					b.auxDeltaOmega.assignZero();
					b.auxDeltav.assignZero();
				}
				
				// compute change in velocity
				j=0; for (ConstraintEntry ci: constraints) {
					double delta = d[j]*h;
					Vector3.add( ci.body1.auxDeltav,     ci.b1.multiply(delta));
					Vector3.add( ci.body1.auxDeltaOmega, ci.b2.multiply(delta));
					Vector3.add( ci.body2.auxDeltav,     ci.b3.multiply(delta));
					Vector3.add( ci.body2.auxDeltaOmega, ci.b4.multiply(delta));
					j = j+1;
				}

				//compute q = (Phi(xk+ d*h)-Phi(xk))/h
				j=0; for (ConstraintEntry ci: constraints) {
					double lambda = ci.lambda + d[j]*h;

					//velocity
					double w = ci.j1.dot(ci.body1.deltaVCm.add(ci.body1.auxDeltav)) + ci.j2.dot(ci.body1.deltaOmegaCm.add(ci.body1.auxDeltaOmega))
					+ ci.j3.dot(ci.body2.deltaVCm.add(ci.body2.auxDeltav)) + ci.j4.dot(ci.body2.deltaOmegaCm.add(ci.body2.auxDeltaOmega)) + (-ci.b) + damper*lambda;

					//fisher
					if (ci.coupledMax == null) q[j] = ((Math.sqrt( w*w+lambda*lambda)-w-lambda) - ci.phixk)/h;
					else {
						//friction damping
						w += frictiondamp*ci.lambda;

						//recompute friction limits *)
						double limit = Math.abs(ci.coupledMax.lambda)*mu; ci.lambdaMin = -limit; ci.lambdaMax = limit;

						if (Math.abs(w)<truncatevelocity ) {w=0;}

						q[j] =   ( fisher( lambda-ci.lambdaMin, fisher(ci.lambdaMax-lambda, -w)) - ci.phixk )/h;
					}
					j=j+1;
				} 			

				//alpha = delta_new/dTq
				double dTq = 0;
				for (j=0;j<n;j++) dTq += d[j]*q[j];
				double alpha = delta_new/dTq;
				
				//z = z + alpha d
				for (j=0;j<n;j++) z[j] -= alpha*d[j];
				
				//r = r-alpha q
				for (j=0;j<n;j++) r[j] -= alpha*q[j];
				
				delta_old = delta_new;
				//delta_new = rTr
				delta_new = 0;
				for (j=0;j<n;j++) delta_new += r[j]*r[j];

				double beta = delta_new/delta_old;
				
				//d = r + beta d
				for (j=0;j<n;j++) d[j] = r[j] + beta*d[j];
				
				k = k+1;
				
				//evaluate
//				double rsum = 0;
//				int m=0; for (ConstraintEntry ci: constraints) {
//					rsum += r[m]*ci.phixk;
//					m++;
//				}

				//System.out.println(k+" :Q="+(rsum/squarederror));
				
			} // sub-system CG iterations


			// clear velocities
			for (Body b: bodies) {
				b.auxDeltaOmega.assignZero();
				b.auxDeltav.assignZero();
			}

			//compute velocity vector for step length 1, move velocity forward
			j=0; for (ConstraintEntry ci: constraints) {
				double delta = dk[j];
				ci.lambda += dk[j];
				//System.out.println(""+ dk[j]);
				Vector3.add( ci.body1.auxDeltav,     ci.b1.multiply(delta));
				Vector3.add( ci.body1.deltaVCm,      ci.b1.multiply(delta));
				
				Vector3.add( ci.body1.auxDeltaOmega, ci.b2.multiply(delta));
				Vector3.add( ci.body1.deltaOmegaCm,  ci.b2.multiply(delta));
				
				Vector3.add( ci.body2.auxDeltav,     ci.b3.multiply(delta));
				Vector3.add( ci.body2.deltaVCm,      ci.b3.multiply(delta));

				Vector3.add( ci.body2.auxDeltaOmega, ci.b4.multiply(delta));
				Vector3.add( ci.body2.deltaOmegaCm,  ci.b4.multiply(delta));

				j = j+1;
			}

			
			k=0; double step = 1;
			double oldsquarederror = squarederror;
			double olderror = error;
			while(true) {
				//compute fisher
				squarederror = 0;
				j=0; for (ConstraintEntry ci: constraints) {				
					//velocity
					double w = ci.j1.dot(ci.body1.deltaVCm) + ci.j2.dot(ci.body1.deltaOmegaCm)
					+ ci.j3.dot(ci.body2.deltaVCm) + ci.j4.dot(ci.body2.deltaOmegaCm) + (-ci.b) + damper*ci.lambda;

					if (k>0)
						ci.lambda += -step*dk[j];
					//fisher
					if (ci.coupledMax == null) ci.phixk = Math.sqrt( w*w+ci.lambda*ci.lambda )-w-ci.lambda;
					else {
						//friction damping
						w += frictiondamp*ci.lambda;

						
						//compute limits
						double limit = Math.abs(ci.coupledMax.lambda)*mu; ci.lambdaMin = -limit; ci.lambdaMax = limit;
						if (Math.abs(w)<truncatevelocity ) {
							w=0;
						}

						
						ci.phixk =  fisher( ci.lambda-ci.lambdaMin, fisher(ci.lambdaMax-ci.lambda, -w));
					}

					squarederror += ci.phixk*ci.phixk;
					j=j+1;
				} error = Math.sqrt(squarederror);	

				
				//termination
				if (0.5*squarederror <= 0.5*oldsquarederror + step*0.01*(-oldsquarederror) ) {
//					System.out.println("k="+k);
					break;
				}
				
				if (k>=linemax) {
//					System.out.println("k="+k);
					break;
				}
				
				k = k+1;
				step = Math.pow(2,-k);
				for (Body b: bodies) {
					Vector3.add(b.deltaVCm, b.auxDeltav.multiply(-step));
					Vector3.add(b.deltaOmegaCm, b.auxDeltaOmega.multiply(-step));				
				}
			} // linesearch


			if ( error < besterror) {
				besterror = error;
				//copy the best solution
				for (Body b: bodies) {
					Vector3.assign(b.auxDeltav2, b.deltaVCm);
					Vector3.assign(b.auxDeltaOmega2, b.deltaOmegaCm);				
				}
			}



			i=i+1;
		} // Newton iterations
	}

	
	
	private void computeVelocity2(List<ConstraintEntry> constraints, List<ConstraintEntry> off,  double[] lambda, double[] w, double damper) {	
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
	
	
	
	private void computeFisher( List<ConstraintEntry> constraints, double[] lambda, double w[], double[] b) {
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
	
	
	private void printA(List<ConstraintEntry> constraints) {
		System.out.println("A = [ ");

		int i = 0; int k =0;
		for (ConstraintEntry ci: constraints) {
			System.out.println("");

			//v[i] = -ci.b;
			double ai[] = new double[1000];

			//velocity pass
			int j=0;
			for (ConstraintEntry cj: constraints) {
				k++;
				//for each interacting constraint
				ai[j] = 0;
				if( ci.body1 == cj.body1) {
					ai[j] = ci.j1.dot(cj.b1) + ci.j2.dot(cj.b2);
				}

				if (ci.body2 == cj.body2) {
					ai[j] = ai[j] + ci.j3.dot(cj.b3) + ci.j4.dot(cj.b4);
				}

				if (ci.body1 == cj.body2) {
					ai[j] = ai[j] + ci.j1.dot(cj.b3) + ci.j2.dot(cj.b4);
				}

				if (ci.body2 == cj.body1) {
					ai[j] = ai[j] + ci.j3.dot(cj.b1) + ci.j4.dot(cj.b2);
				}

				//v[i] = v[i] + ai[j]*cj.lambda;

//				if (Math.abs(ai[j])<1e-10 ) {
//					ai[j] = 0;
//				}
				
				//System.out.printf("%F "/*"%18.8e "*/, ai[j]);
				System.out.print(ai[j] + " ");
				
				j++;
			}// for cj
			
			System.out.println("; ");
			
		}
		

		System.out.println("]\n b = [ ");
		for (ConstraintEntry ci: constraints) {
			System.out.print( (-ci.b) + " ");
		}		
		System.out.println("]");
			
	}

}
