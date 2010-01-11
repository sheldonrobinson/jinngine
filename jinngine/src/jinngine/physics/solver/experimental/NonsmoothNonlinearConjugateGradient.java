package jinngine.physics.solver.experimental;

import java.util.List;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.ProjectedGaussSeidel;
import jinngine.physics.solver.Solver;
import jinngine.physics.solver.Solver.constraint;

public class NonsmoothNonlinearConjugateGradient implements Solver {
	int max = 10000;
	
	public double[] pgsiters = new double[max];
	public double[] errors = new double[max];
	
	@Override
	public void setMaximumIterations(int n) {
	//	max =n;

	}
	
	public NonsmoothNonlinearConjugateGradient(int n) {
		this.max = n;

		pgsiters = new double[max];
		errors = new double[max];
	}

	@Override
	public double solve(List<constraint> constraints, List<Body> bodies,
			double epsilon) {
		
		double rnew = 0;

//		double new_merit = FischerNewton.fischerMerit(constraints, bodies);
		double new_merit = merit(constraints, bodies);

		System.out.println("initial merit = " + new_merit);
		
		//set friction bounds 2to zero
//		for (constraint ci: constraints) {
//			ci.mu = 0;
//		}
		
		//one pgs iteration, computes initial residual and seach direction
		for (constraint ci: constraints) {
			//calculate (Ax+b)_i 
			double w = ci.j1.dot(ci.body1.deltaVCm) 
			         + ci.j2.dot(ci.body1.deltaOmegaCm)
			         + ci.j3.dot(ci.body2.deltaVCm) 
		             + ci.j4.dot(ci.body2.deltaOmegaCm) 
			         + ci.lambda*ci.damper;

			double deltaLambda = (-ci.b-w)/(ci.diagonal + ci.damper );
			double lambda0 = ci.lambda;

			//Clamp the lambda[i] value to the constraints
			if (ci.coupling != null) {
				//if the constraint is coupled, allow only lambda <= coupled lambda
				ci.lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;
				ci.upper =  Math.abs(ci.coupling.lambda)*ci.coupling.mu;
			} 

			//do projection
			double newlambda =
				Math.max(ci.lower, Math.min(lambda0 + deltaLambda,ci.upper ));

			//update the V vector
			deltaLambda = newlambda - lambda0;
			
			//beta=0 in first iteration, so residual and d is equal
			ci.residual = ci.d = deltaLambda;
			
			rnew += ci.residual*ci.residual;
			
			//Apply to delta velocities
			Vector3.add( ci.body1.deltaVCm,     ci.b1.multiply(deltaLambda) );
			Vector3.add( ci.body1.deltaOmegaCm, ci.b2.multiply(deltaLambda) );
			Vector3.add( ci.body2.deltaVCm,     ci.b3.multiply(deltaLambda));
			Vector3.add( ci.body2.deltaOmegaCm, ci.b4.multiply(deltaLambda));
			ci.lambda += deltaLambda;

		} //for constraints	

		
		

		double old_merit = new_merit;
		
		int iter = 0;
		int pgslike = 1;
		while (iter<max) {
		
			//line search
			double tau = 0;

			//copy to auxiliary
			for ( Body bi: bodies) {
				bi.auxDeltav2.assign(bi.deltaVCm);
				bi.auxDeltaOmega2.assign(bi.deltaOmegaCm);
			}

			double best_merit = Double.POSITIVE_INFINITY;
			old_merit = new_merit = merit(constraints, bodies);
			System.out.println("linesearch merit START="+new_merit);
//			for (int k=0;k<0;k++)  {
			int res = 1000;
			tau = 1.0 / res;				
			int best_k = 0;
			for (int k=0;k<res;k++)  {
				
				//update friction bounds
				for (constraint ci: constraints) 
					if (ci.coupling != null) {
						//if the constraint is coupled, allow only lambda <= coupled lambda
						ci.lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;
						ci.upper =  Math.abs(ci.coupling.lambda)*ci.coupling.mu;
						//ci.coupling.mu = 0.0;
					} 
				
				
				//evaluate fischer, check backtracking condition
//				new_merit = FischerNewton.fischerMerit(constraints, bodies); pgslike++;
				new_merit = merit(constraints, bodies); //pgslike++;
				double gradienttimesx = 0;//meritgradient(constraints, bodies);
				
				//System.out.println("linesearch merit="+new_merit);
				if ( new_merit <= old_merit + 2*tau*0.01*(gradienttimesx) ) {
					//System.out.println("linesearch terminated k="+k);
					//break;
				}
				
				if ( new_merit < best_merit) {
					best_merit = new_merit;
					best_k = k;
				 //   System.out.println("linesearch best merit="+new_merit+" tau =" + tau*k);

					//copy to auxiliary
					for ( Body bi: bodies) {
						bi.auxDeltav2.assign(bi.deltaVCm);
						bi.auxDeltaOmega2.assign(bi.deltaOmegaCm);
					}
					//copy lambda vector
					for (constraint cj: constraints) {
						cj.z = cj.lambda;
					}

				}
								
				//go backwards
				for (constraint ci: constraints) {
					Vector3.add( ci.body1.deltaVCm,     ci.b1.multiply(ci.d * -tau) );
					Vector3.add( ci.body1.deltaOmegaCm, ci.b2.multiply(ci.d * -tau) );
					Vector3.add( ci.body2.deltaVCm,     ci.b3.multiply(ci.d * -tau) );
					Vector3.add( ci.body2.deltaOmegaCm, ci.b4.multiply(ci.d * -tau) );
					ci.lambda += ci.d * -tau;
				} //	pgslike++;

			}
			
			//go to best linesearch position
			for ( Body bi: bodies) {
				bi.deltaVCm.assign(bi.auxDeltav2);
				bi.deltaOmegaCm.assign(bi.auxDeltaOmega2);
			} //set lambda values
			for (constraint ci : constraints) {
				ci.lambda = ci.z;
			}
			
			//reflect linesearch in d
			for (constraint ci: constraints) {
				//ci.d = ci.d - (ci.d * tau*best_k);
				if(best_k>0) {
					ci.d = 0;
				}
			}
			System.out.println("search tau="+tau*best_k);
			


			pgsiters[iter] = iter;
			errors[iter] = FischerNewton.fischerMerit(constraints, bodies);
//			errors[iter] = merit(constraints, bodies);

			//set to machine presicion
			if (errors[iter] < 1e-20) 
				errors[iter] = 1e-20;

			System.out.println(" Iteration " + iter +", pgslike="+pgslike+" error="+errors[iter]);
			//System.out.print(pgslike+" "+new_merit+"; ");
			System.out.println("fischer merit ="+ FischerNewton.fischerMerit(constraints, bodies) );


			
			double rold = rnew;
			rnew = 0;
			//use one PGS iteration to compute new residual 
			for (constraint ci: constraints) {
				//calculate (Ax+b)_i 
				double w =  ci.j1.dot(ci.body1.deltaVCm) 
				+ ci.j2.dot(ci.body1.deltaOmegaCm)
				+  ci.j3.dot(ci.body2.deltaVCm) 
				+ ci.j4.dot(ci.body2.deltaOmegaCm) + ci.lambda*ci.damper;

				double deltaLambda = (-ci.b-w)/(ci.diagonal + ci.damper );
				double lambda0 = ci.lambda;

				//Clamp the lambda[i] value to the constraints
				if (ci.coupling != null) {
					//if the constraint is coupled, allow only lambda <= coupled lambda
					ci.lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;
					ci.upper =  Math.abs(ci.coupling.lambda)*ci.coupling.mu;
				} 

				//do projection
				double newlambda =
					Math.max(ci.lower, Math.min(lambda0 + deltaLambda,ci.upper ));

				//update the V vector
				deltaLambda = newlambda - lambda0;
				
				ci.residual = deltaLambda;

				//Apply to delta velocities
				Vector3.add( ci.body1.deltaVCm,     ci.b1.multiply(deltaLambda));
				Vector3.add( ci.body1.deltaOmegaCm, ci.b2.multiply(deltaLambda));
				Vector3.add( ci.body2.deltaVCm,     ci.b3.multiply(deltaLambda));
				Vector3.add( ci.body2.deltaOmegaCm, ci.b4.multiply(deltaLambda));
				ci.lambda += deltaLambda;
				
				rnew += ci.residual*ci.residual;
			} //for constraints	
			pgslike++;

			if (Math.abs(rold) < 1e-20) {
				iter = iter + 1;
				//solution reached
				pgsiters[iter] = iter;
				errors[iter] = FischerNewton.fischerMerit(constraints, bodies);

				//set to machine presicion
				if (errors[iter] < 1e-20) 
					errors[iter] = 1e-20;
				
				System.out.println(" Iteration " + iter +", pgslike="+pgslike+" fischer="+errors[iter]);
				//System.out.print(pgslike+" "+new_merit+"; ");
				//new_merit = FischerNewton.fischerMerit(constraints, bodies);
				
				break;
			}
			
			//compute new d
			double beta = rnew/rold;
			System.out.println("beta="+beta +", rnew="+rnew+", rold="+rold);
			//beta = 0;
			//beta *= 10;
		 // if (beta>1) beta =1;
			
			for (constraint ci: constraints) {
				//move lambda forward with beta d 
				//Apply to delta velocities
				Vector3.add( ci.body1.deltaVCm,     ci.b1.multiply(beta*ci.d));
				Vector3.add( ci.body1.deltaOmegaCm, ci.b2.multiply(beta*ci.d));
				Vector3.add( ci.body2.deltaVCm,     ci.b3.multiply(beta*ci.d));
				Vector3.add( ci.body2.deltaOmegaCm, ci.b4.multiply(beta*ci.d));
				ci.lambda += beta*ci.d;
				
				//update the direction vector
				ci.d = beta*ci.d + ci.residual; // gradient is -r
			}// pgslike++;
			
			iter = iter+1;
		}
		
		for (constraint ci: constraints) {
			System.out.println(""+ci.lambda);
		}
		
		return 0;
	}
	
	private final double meritold(List<constraint> constraints, List<Body> bodies) {
		double value = 0;
		
		//copy to auxiliary
		for ( Body bi: bodies) {
			bi.auxDeltav.assign(bi.deltaVCm);
			bi.auxDeltaOmega.assign(bi.deltaOmegaCm);
		}
		
		//use one PGS iteration to compute new residual 
		for (constraint ci: constraints) {
			//calculate (Ax+b)_i 
			double w =  ci.j1.dot(ci.body1.auxDeltav) 
			+ ci.j2.dot(ci.body1.auxDeltaOmega)
			+ ci.j3.dot(ci.body2.auxDeltav) 
			+ ci.j4.dot(ci.body2.auxDeltaOmega) + ci.lambda*ci.damper;

			double deltaLambda = (-ci.b-w)/(ci.diagonal + ci.damper );
			double lambda0 = ci.lambda;

			//Clamp the lambda[i] value to the constraints
			if (ci.coupling != null) {
				//if the constraint is coupled, allow only lambda <= coupled lambda
				ci.lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;
				ci.upper =  Math.abs(ci.coupling.lambda)*ci.coupling.mu;
			} 

			//do projection
			double newlambda =
				Math.max(ci.lower, Math.min(lambda0 + deltaLambda,ci.upper ));

			//update the V vector
			deltaLambda = newlambda - lambda0;
			
			//ci.residual = deltaLambda;
			Vector3.add( ci.body1.auxDeltav,     ci.b1.multiply(deltaLambda) );
			Vector3.add( ci.body1.auxDeltaOmega, ci.b2.multiply(deltaLambda) );
			Vector3.add( ci.body2.auxDeltav,     ci.b3.multiply(deltaLambda));
			Vector3.add( ci.body2.auxDeltaOmega, ci.b4.multiply(deltaLambda));

			
			value += deltaLambda*deltaLambda;
		} //for constraints	
		
		return value;
	}

	private final double merit(List<constraint> constraints, List<Body> bodies) {
		double value = 0;
		
		//copy to auxiliary
		for ( Body bi: bodies) {
			bi.auxDeltav.assign(bi.deltaVCm);
			bi.auxDeltaOmega.assign(bi.deltaOmegaCm);
		}
		//copy lambda value
		for (constraint ci: constraints) {
			ci.s = ci.lambda;
		}
		
		//use one PGS iteration to compute new residual 
		for (constraint ci: constraints) {
			//calculate (Ax+b)_i 
			double w =  ci.j1.dot(ci.body1.auxDeltav) 
			+ ci.j2.dot(ci.body1.auxDeltaOmega)
			+ ci.j3.dot(ci.body2.auxDeltav) 
			+ ci.j4.dot(ci.body2.auxDeltaOmega) + ci.s*ci.damper;

			double deltaLambda = (-ci.b-w)/(ci.diagonal + ci.damper );
			double lambda0 = ci.s;

			//Clamp the lambda[i] value to the constraints
			if (ci.coupling != null) {
				//if the constraint is coupled, allow only lambda <= coupled lambda
				ci.l = -Math.abs(ci.coupling.s)*ci.coupling.mu;
				ci.u =  Math.abs(ci.coupling.s)*ci.coupling.mu;
			} else {
				ci.l = ci.lower;
				ci.u = ci.upper;
			}

			//do projection
			double newlambda =
				Math.max(ci.l, Math.min(lambda0 + deltaLambda,ci.u ));

			//update the V vector
			deltaLambda = newlambda - lambda0;
			
			//ci.residual = deltaLambda;
			Vector3.add( ci.body1.auxDeltav,     ci.b1.multiply(deltaLambda) );
			Vector3.add( ci.body1.auxDeltaOmega, ci.b2.multiply(deltaLambda) );
			Vector3.add( ci.body2.auxDeltav,     ci.b3.multiply(deltaLambda));
			Vector3.add( ci.body2.auxDeltaOmega, ci.b4.multiply(deltaLambda));

			ci.s += deltaLambda;
			
			value += deltaLambda*deltaLambda;
		} //for constraints	
		
		return value;
	}

	
}
