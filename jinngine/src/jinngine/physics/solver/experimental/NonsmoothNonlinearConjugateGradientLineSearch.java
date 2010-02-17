/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.physics.solver.experimental;

import java.util.ArrayList;
import java.util.List;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.ProjectedGaussSeidel;
import jinngine.physics.solver.Solver;
import jinngine.physics.solver.Solver.constraint;

public class NonsmoothNonlinearConjugateGradientLineSearch implements Solver {
	int max = 10000;
	
	public double[] pgsiters = new double[max];
	public double[] errors = new double[max];
	boolean usepolakribiere = false;
	
	@Override
	public void setMaximumIterations(int n) {
	//	max =n;

	}
	
	public NonsmoothNonlinearConjugateGradientLineSearch(int n, boolean polakribiere) {
		this.max = n;

		pgsiters = new double[max];
		errors = new double[max];
		this.usepolakribiere = polakribiere;
	}

	@Override
	public double solve(List<constraint> constraints, List<Body> bodies,
			double epsilon) {
		
		double polakribiere = 0;
		double restart = 0;
		double rnew = 0;
		double beta= 0;		
		int iter = 0;
		int pgslike = 0;

		while (iter<max) {

			pgsiters[iter] = pgslike;
//			errors[iter] = FischerNewton.fischerMerit(constraints, bodies);
			errors[iter] = merit(constraints, bodies);
//			errors[iter] = FischerNewton.fischerMerit(frictions, bodies);
			
//			System.out.println(""+errors[iter]);

			double rold = rnew; rnew = 0; polakribiere = 0; restart= 0;
			//use one PGS iteration to compute new residual 
			for (constraint ci: constraints) {
				//calculate (Ax+b)_i 				
				double w = ci.j1.dot(ci.body1.deltavelocity) 
				         + ci.j2.dot(ci.body1.deltaomega)
				         + ci.j3.dot(ci.body2.deltavelocity) 
				         + ci.j4.dot(ci.body2.deltaomega) 
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
				deltaLambda = (newlambda - lambda0);
				

				//Apply to delta velocities
				Vector3.add( ci.body1.deltavelocity,     ci.b1.multiply(deltaLambda));
				Vector3.add( ci.body1.deltaomega, ci.b2.multiply(deltaLambda));
				Vector3.add( ci.body2.deltavelocity,     ci.b3.multiply(deltaLambda));
				Vector3.add( ci.body2.deltaomega, ci.b4.multiply(deltaLambda));
				ci.lambda += deltaLambda;
				
				rnew += deltaLambda*deltaLambda;
				polakribiere += (-deltaLambda)*(-deltaLambda+ci.residual);
				restart += (-deltaLambda)*(-ci.residual);
				ci.residual = deltaLambda;
			} pgslike++; //for constraints	
	
//			errors[iter] = 0.5*rnew;

			
			//handle stagnation
			if (iter == 0) {
				rold = rnew;
				if (Math.abs(rnew) < 1e-20) {
					break;
				}
				
			} else {
				if (Math.abs(rold) < 1e-20) {
					break;
				}
				if ( Math.abs(rold-rnew) < 1e-6 ) {
					//break;
				}

			}
			
			//compute beta
			beta = rnew/rold;
//			double cond = Math.abs(restart)/rnew;
//			System.out.println("cond="+cond);

			if ( beta > 1 )  {
//			if (cond > 0.5) {
				beta = 0.0;

//				System.out.println("restart");
				//truncate direction
				for(constraint ci: constraints)
					ci.d = 0;//ci.residual;
				
			} else {
				//beta *=beta;
				//beta = Math.sqrt(beta);
				//if (usepolakribiere)
//					beta = polakribiere/rold;
				
				//PR+
				//beta=beta<0?0:beta;
//				beta=0;
				//move lambda forward with beta d
				double dfkTpk = 0;
				for (constraint ci: constraints) {
//				System.out.println("beta="+beta);

//					if (beta<0) {
//						System.out.println("negative beta value");
//					}

					//Apply to delta velocities
					Vector3.add( ci.body1.deltavelocity,     ci.b1.multiply(beta*ci.d));
					Vector3.add( ci.body1.deltaomega, ci.b2.multiply(beta*ci.d));
					Vector3.add( ci.body2.deltavelocity,     ci.b3.multiply(beta*ci.d));
					Vector3.add( ci.body2.deltaomega, ci.b4.multiply(beta*ci.d));
					ci.lambda += beta*ci.d;

//					Vector3.add( ci.body1.deltaVCm,     ci.b1.multiply(ci.d));
//					Vector3.add( ci.body1.deltaOmegaCm, ci.b2.multiply(ci.d));
//					Vector3.add( ci.body2.deltaVCm,     ci.b3.multiply(ci.d));
//					Vector3.add( ci.body2.deltaOmegaCm, ci.b4.multiply(ci.d));
//					ci.lambda += ci.d;
					
					
					//update the direction vector
					ci.d = beta*ci.d + ci.residual; // gradient is -r
					
					dfkTpk += -ci.residual * ci.d; 
				} 
				
				
				
				//use backtracking linesearch
				double alpha = 1;
				while (true) {
					double m = merit(constraints, bodies);
					if ( m <= rnew + 2*0.01*alpha*dfkTpk || alpha < 1e-21 ) {
						if (alpha <1)
							System.out.println("m="+m+", rnew="+rnew+", alpha="+alpha);
						

												
						break;
					}

					
					alpha = alpha*0.5; 
					
					
					for (constraint ci:constraints) {
						//Apply to delta velocities
						Vector3.add( ci.body1.deltavelocity,     ci.b1.multiply(-alpha*ci.d));
						Vector3.add( ci.body1.deltaomega, ci.b2.multiply(-alpha*ci.d));
						Vector3.add( ci.body2.deltavelocity,     ci.b3.multiply(-alpha*ci.d));
						Vector3.add( ci.body2.deltaomega, ci.b4.multiply(-alpha*ci.d));
						ci.lambda += -alpha*ci.d;
					}
					
				}

			}
			
			//iteration count
			iter = iter+1;
		}
		
//		System.out.println("iters="+iter);
		return 0;
	}
	

	public static final double merit(List<constraint> constraints, List<Body> bodies) {
		double value = 0;
		
		//copy to auxiliary
		for ( Body bi: bodies) {
			bi.auxDeltav.assign(bi.deltavelocity);
			bi.auxDeltaOmega.assign(bi.deltaomega);
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
			
			//value += Math.pow(w+ci.b,2);
			
//			if (onlyfrictions) {
//				if (ci.coupling!=null) {
//					value += deltaLambda*deltaLambda;
//				}
//			} else {
//				value += deltaLambda*deltaLambda;
//			}
			
//			value += Math.pow(w+ci.b,2);
			value += deltaLambda*deltaLambda;
			
		} //for constraints	
		
		//value = FischerNewton.fischerMerit(constraints, bodies);
		
		return value;
	}
	
}
