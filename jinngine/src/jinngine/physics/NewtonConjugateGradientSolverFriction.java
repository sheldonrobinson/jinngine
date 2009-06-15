package jinngine.physics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


import jinngine.math.Vector3;

public class NewtonConjugateGradientSolverFriction implements Solver {
	private boolean doLineSearch=true;
	int maxIter = 10;
	private double epsilon = 1e-1;
	private int maxInner = 1;
	public int internal = 0;
	
	public void doLineSearch(boolean state) {
		doLineSearch =state;
	}
	
	public void setMaximumInnerIterations(int n) {
		maxInner = n;
	}

	public static double fisherError(List<ConstraintEntry> constraints, List<ConstraintEntry> off) {
		int n=constraints.size();
		double[] w = new double[n];
		//clear auxiliary fields
		for (ConstraintEntry ci: constraints) {
			ci.body1.auxDeltav.assignZero();
			ci.body1.auxDeltaOmega.assignZero();
			ci.body2.auxDeltav.assignZero();
			ci.body2.auxDeltaOmega.assignZero();
		}

		//apply contributions from all constraints to bodies
		int i = 0;
		for (ConstraintEntry ci: constraints) {
			Vector3.add( ci.body1.auxDeltav,     ci.b1.multiply(ci.lambda) );
			Vector3.add( ci.body1.auxDeltaOmega, ci.b2.multiply(ci.lambda) );
			Vector3.add( ci.body2.auxDeltav,     ci.b3.multiply(ci.lambda) );
			Vector3.add( ci.body2.auxDeltaOmega, ci.b4.multiply(ci.lambda) );
			i = i+1;
		}
		
		for (ConstraintEntry ci: off) {
			Vector3.add( ci.body1.auxDeltav,     ci.b1.multiply(ci.lambda) );
			Vector3.add( ci.body1.auxDeltaOmega, ci.b2.multiply(ci.lambda) );
			Vector3.add( ci.body2.auxDeltav,     ci.b3.multiply(ci.lambda) );
			Vector3.add( ci.body2.auxDeltaOmega, ci.b4.multiply(ci.lambda) );
		}


		i = 0;
		for (ConstraintEntry ci: constraints) {
			w[i] = ci.j1.dot(ci.body1.auxDeltav) + ci.j2.dot(ci.body1.auxDeltaOmega)
			     + ci.j3.dot(ci.body2.auxDeltav) + ci.j4.dot(ci.body2.auxDeltaOmega) + (-ci.b);
			i = i+1;
		}
		
		//evaluate b vector using fisher
		i=0;
		double norm = 0;
		double[] f = new double[n];
		for (ConstraintEntry c: constraints) {
			//get the b vector and starting lambda values
			if (c.coupledMax == null) {
				
				f[i] = -( Math.sqrt( w[i]*w[i]+ c.lambda*c.lambda )-w[i]-c.lambda);
			} else {
				double limit = Math.abs(c.coupledMax.lambda)*friction;
				double lo = -limit;
				double hi = limit;
				f[i] = - ( fisher( c.lambda-c.lambdaMin, fisher(c.lambdaMax-c.lambda, -w[i])));
			}
			
			norm += f[i]*f[i];
			
			i++;
		}
		
		return Math.sqrt(norm);
		
	}
	
	public void printA(List<ConstraintEntry> constraints) {
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

				System.out.printf("%13.2e ", ai[j]);
				
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
	
	@Override
	public void setErrorTolerance(double epsilon) {
		this.epsilon = epsilon;
	}

	@Override
	public void setMaximumIterations(int n) {
		// TODO Auto-generated method stub
		maxIter = n;
	}
	
	public void computeVelocity(List<ConstraintEntry> constraints, double[] lambda, double[] w) {
		int i=0;
		for (ConstraintEntry ci: constraints) {

			w[i] = -ci.b;
			//double ai[] = new double[n];

			//velocity pass
			int j=0;
			for (ConstraintEntry cj: constraints) {
				//for each interacting constraint
				double aij = 0;
				if( ci.body1 == cj.body1) {
					aij = ci.j1.dot(cj.b1) + ci.j2.dot(cj.b2);
				}

				if (ci.body2 == cj.body2) {
					aij = aij + ci.j3.dot(cj.b3) + ci.j4.dot(cj.b4);
				}

				if (ci.body1 == cj.body2) {
					aij = aij + ci.j1.dot(cj.b3) + ci.j2.dot(cj.b4);
				}

				if (ci.body2 == cj.body1) {
					aij = aij + ci.j3.dot(cj.b1) + ci.j4.dot(cj.b2);
				}

				w[i] = w[i] + aij*lambda[j];
				
				j++;
			}// for cj
			i++;
		}	
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

	public void computeMinMap( List<ConstraintEntry> constraints, double[] lambda, double w[], double[] b) {
		//evaluate b vector using fisher
		int i=0;
		for (ConstraintEntry c: constraints) {
			//get the b vector and starting lambda values
			if (c.coupledMax == null) {
				
				b[i] = -Math.min(w[i], lambda[i]);
			} else {
//				double limit = Math.abs(c.coupledMax.lambda)*friction;
//				c.lambdaMin = -limit;
//				c.lambdaMax  = limit;
				

				b[i] = - ( Math.min( c.lambdaMax-lambda[i], -Math.min(lambda[i]-c.lambdaMin, w[i])));
			}
			i++;
		}
		
	}

	
	
	public void computeJacobianDiagonal( List<ConstraintEntry> constraints, double[] lambda, double w[], double[] diag, double damper ) {
		//evaluate b vector using fisher
		int i=0;
		for (ConstraintEntry c: constraints) {
			double aii = c.j1.dot(c.b1) 
			+ c.j2.dot(c.b2)
			+ c.j3.dot(c.b3)
			+ c.j4.dot(c.b4) + damper;

			//normal component constraints
			if (c.coupledMax == null) {
				double lambda_c = lambda[i];

				if (Math.abs(w[i])+Math.abs(lambda_c) < 1e-14) {
					lambda_c += 1e-13;
				}

				diag[i] = (aii*w[i]+lambda_c)/Math.sqrt(w[i]*w[i]+lambda_c*lambda_c)-aii-1;
			} else {
				//friction component constraints
				double lambda_c = lambda[i];

				double n_u = Math.sqrt( Math.pow(c.lambdaMax-lambda_c, 2) + w[i]*w[i]);
				double phi_u = fisher( c.lambdaMax-lambda_c, -w[i]);
				double n_l = Math.sqrt( (lambda_c-c.lambdaMin)*(lambda_c-c.lambdaMin) + phi_u*phi_u );

				if (n_u < 1e-14 || n_l < 1e-14) {
					lambda_c += 1e-13;
					n_u = Math.sqrt( Math.pow(c.lambdaMax-lambda_c, 2) + w[i]*w[i]);
					phi_u = fisher( c.lambdaMax-lambda_c, -w[i]);
					n_l = Math.sqrt( (lambda_c-c.lambdaMin)*(lambda_c-c.lambdaMin) + phi_u*phi_u );
				}

				phi_u = fisher( c.lambdaMax-lambda_c, -w[i]);
				double dxjphi_u = aii*(w[i]/n_u+1)-((c.lambdaMax-lambda_c)/n_u) +1;
				diag[i] = (1/n_l)*(phi_u*dxjphi_u+(lambda_c-c.lambdaMin))-dxjphi_u-1;     
			}

			i++;
		}
	}

	static double friction = 1.0;

	public double solve(List<ConstraintEntry> all) {
		return solve(all,new ArrayList<ConstraintEntry>());
	}

	//double epsilon = 1e-1;
	public double solve(List<ConstraintEntry> constraints, List<ConstraintEntry> off ) {
		boolean giveup = false;
		int iterations = 0;
		internal = 0;
		
		int n = constraints.size();
		//System.out.println("solver: "+n+" constraints");
		
		//n = 2;
		
		double lambda0[] = new double[n];
		double lambda[] = new double[n];
		double lambda_best[] = new double[n];
		
		double v[] = new double[n];
		double w[] = new double[n];
		double b[] = new double[n];
		double lo[] = new double[n];
		double hi[] = new double[n];
		
		
		double error = 0;
		double error_prev = Double.POSITIVE_INFINITY;
		double error_inf = 0;
		double error_best = Double.POSITIVE_INFINITY;
		double error_inf_best = Double.POSITIVE_INFINITY;
		double stored_error = 0;

		double damper =0;
		//int s = 0;

		
		//Set friction limits for this entire run
		int i=0;
		for (ConstraintEntry c: constraints) {
			if (c.coupledMax != null) {
				double limit = Math.abs(c.coupledMax.lambda)*friction;
				//System.out.println("limit " +limit);
				c.lambdaMin = -limit;
				c.lambdaMax = limit;

//				c.lambdaMin = -2;
//				c.lambdaMax = 2;

			}
			i++;
		}

		i = 0;
		for (ConstraintEntry c: constraints) {
			//c.lambda = 0;

			lambda0[i] = c.lambda;
			lambda[i] =  c.lambda;
			c.index = i;
			w[i] = -c.b;
			i++;
		}

		
		
		while (true) {
			//damper = 2/(iterations+1)*(iterations+1);


			//compute velocities and fisher
			if (iterations == 0) {
				computeVelocity2(constraints, off, lambda,  w, 0);
				computeFisher(constraints, lambda, w, b);
			}
			
			internal += 1;
			//computeMinMap(constraints, lambda, w, b);
			
						
			//norm of b
			error = 0;
			double sqared_error = 0;
			for (i=0;i<n;i++) {
				sqared_error += b[i]*b[i];
			}
			error = Math.sqrt(sqared_error);

			
			
			//record best solution
			if ( error < error_best) {
				error_best = error;
				//error_inf_best = error_inf;
				//System.out.println("Newton-CG: stop");
				stored_error = error;
				i=0;
				for (ConstraintEntry c: constraints) {
						lambda_best[i] = c.lambda;
						i++;
				}
			}

			if ( error_inf < error_inf_best) {
				error_inf_best = error_inf;
			}

			
			//termination condition
			if (error<epsilon /*|| Math.abs(error_prev-error) < epsilon*/) {
//				i=0;
//				for (ConstraintEntry c: constraints) {
//						System.out.printf("lambda %13.2e", c.lambda);
//						i++;
//				}
				
				break;
			}

			
			if (iterations>maxIter)
				break;

//			System.out.println("**** " + iterations);
//			System.out.println("error="+error);

			
			
			double tol=0;
			if (error < 1)
				tol = error*error*0.5;
			else
				tol = 0.5*Math.sqrt(error);
			
			//tol = 1e-7;

			
			//evaluate jacobian
			//jacobian(constraints,J,w,n);
			
			//List<Sparse> Jacobian = new ArrayList<Sparse>();
			//jacobian2(constraints, Jacobian, w, n);

//			double J[][] = new double[n][n];
//			for (Sparse e: Jacobian) {
//				J[e.i][e.j] = e.a;
//			}
			
			//double tol = 1e-7;
			//solve newton equation
			//double x[] = CG(J,b,tol,0,10);
			//double x[] = CG2(Jacobian,b,tol,0,10);

			double[] diag = new double[n];
			double[] x = new double[n];
			for (i=0;i<n;i++) diag[i] = 1;
			//computeJacobianDiagonal(constraints, lambda, w, diag, damper);
			//internal +=1;
			internal += 2*CGfiniteDifference(x, constraints, off, lambda, b, tol, 0, maxInner, diag, damper );

			
//			System.out.println("****iterate");
//			for (double xi:x) {
//				System.out.println("xi " + xi);
//			}
			
			error_prev = error;
			
			//boolean doLineSearch = true;
			
//			if (iterations>5) 
//				doLineSearch = true;

			
			
			if (doLineSearch) {

				//compute gradient of merit function
//				double lambda_small[] = new double[n];
//				double h = 1e-10;
//
//				i=0;
//				for (ConstraintEntry c: constraints) {
//					lambda_small[i] = c.lambda + x[i]*h; i++;
//				}
//				double[] wtemp = new double[n];
//				double[] btemp = new double[n];	
//				computeVelocity2(constraints, off, lambda_small, wtemp, 0);
//				computeFisher(constraints, lambda_small, wtemp, btemp);
//				double fval = 0;
//				for (i=0; i<n;i++) {
//					fval += btemp[i]*btemp[i];
//				}
//				fval *= 0.5;
//				double grad = (-0.5*error*error+fval)/h;
				
				//grad = 0;
				//System.out.println("grad = " + (grad + sqared_error));
				//internal +=2;
				
				double grad = -sqared_error;
				
				//line search
				int maxk = 6;
				for (int k=0; k<maxk; k++) {
					double lambda_new[] = new double[n];
					//double w[] = new double[n];
					double bvalue[] = new double[n];
					double alpha = Math.pow(2, -k);

					//if ()

					//do a newton step
					i=0;
					for (ConstraintEntry c: constraints) {
						lambda_new[i] = c.lambda + x[i]*(alpha);
						//lambda_small[i] = c.lambda + x[i]*h;
						
						//project the line-search
//						if (maxk > 0) {
//							if (lambda_new[i]>c.lambdaMax) lambda_new[i] = c.lambdaMax;
//							if (lambda_new[i]<c.lambdaMin) lambda_new[i] = c.lambdaMin;
//						}
						
						i++;
					}

					//computations
					computeVelocity2(constraints, off, lambda_new, w, damper);
					computeFisher(constraints, lambda_new, w, bvalue);
					//computeMinMap(constraints, lambda_new, w, bvalue);

					internal += 2;
					
					
					
					
					
					//norm of bvalue
					double norm = 0;
					for (int j=0;j<n;j++)
						norm += bvalue[j]*bvalue[j];
					double e = 0.5*norm; //Math.sqrt(norm);

					//linesearch condition
					if (e < 0.5*error*error + 0.1*alpha*grad || k == maxk-1 ) {
						//apply
						i=0;
						for (ConstraintEntry c: constraints) {
							c.lambda = lambda_new[i];
							lambda[i] = lambda_new[i];
							b[i] = bvalue[i];
							i++;
						}

						//System.out.println("k="+k);

						//error = Math.sqrt(norm);

						break;	
					}
				} // linesearch
			} else {
				double lambda_new[] = new double[n];
				i = 0;
				for (ConstraintEntry c: constraints) {
					lambda_new[i] = c.lambda + x[i];
					c.lambda = lambda_new[i];
					lambda[i] = lambda_new[i];
					//b[i] = bvalue[i];
					//System.out.println("k="+k);
					i++;
				}

				computeVelocity2(constraints, off, lambda_new, w, damper);
				computeFisher(constraints, lambda_new, w, b);
				internal += 2;

			} //linesearch
			
			
			
			iterations++;			
		}
		
		if (error_best > epsilon) {
			//System.out.printf("error %13.2e, error_inf, %13.2e, iter %d \n",error_best, error_inf_best, iterations );
			
		}
		
		double norm=0;

		//if ( error < epsilon) {
		//compute delta velocities
		i=0;
		for (ConstraintEntry c: constraints) {
			//do final projection
			//if (c.lambda<0) c.lambda = 0;

			c.lambda = lambda_best[i];

			//System.out.printf("lambda: %13.2e\n", c.lambda);

			

			norm += c.lambda*c.lambda;

			Vector3.add( c.body1.deltaVCm,     c.b1.multiply(c.lambda-lambda0[i]) );
			Vector3.add( c.body1.deltaOmegaCm, c.b2.multiply(c.lambda-lambda0[i]) );
			Vector3.add( c.body2.deltaVCm,     c.b3.multiply(c.lambda-lambda0[i]) );
			Vector3.add( c.body2.deltaOmegaCm, c.b4.multiply(c.lambda-lambda0[i]) );

			i++;
		}
		
		//System.out.println("newton iterations " + iterations);
//		System.out.println("internal " + internal);

		return error_best;
	}
	
	private class Sparse {
		public int i;
		public int j;
		public double a;
	}

	public void jacobian2(List<ConstraintEntry> constraints, List<Sparse> Jacobian, double[] w, int n) {
		//JWJ' matrix
		int i = 0;
		
		//System.out.println("pairs:");
		
		//double A[][] = new double[n][n];
		
		List<Sparse> A = new ArrayList<Sparse>();
		
//		for (ConstraintEntry ci: constraints) {
//			//System.out.println("constraint " +i);
//
//			ListIterator<ConstraintEntry> i1 = ci.body1.constraints.listIterator(); 
//			ListIterator<ConstraintEntry> i2 = ci.body2.constraints.listIterator(); 
//			ConstraintEntry cj = null;
//			ConstraintEntry ck = null;
//			int cjindex = 0;
//			int ckindex = 0;
//			Sparse element = null;
//			
//			//load the first two elements
//			if (i1.hasNext()) {
//				cj = i1.next();
//				cjindex = cj.index;
//			} else {
//				cjindex = Integer.MAX_VALUE;
//			}
//			if (i2.hasNext()) {
//				ck = i2.next();
//				ckindex = ck.index;
//			} else {
//				ckindex = Integer.MAX_VALUE;
//			}
//
//			//loop to merge lists
//			while (true) {
//				
//				//take the least index first
//				if ( cjindex < ckindex ) {
//
//					//System.out.println(""+cj.index);
//					//System.out.println(""+ck.index);
//					
//					double aij=0;
//					if ( ci.body1 == cj.body1)
//						aij = ci.b1.dot(cj.b1) + ci.b2.dot(cj.b2);
//
//					else if (ci.body1 == cj.body2)
//						aij = ci.b1.dot(cj.b3) + ci.b2.dot(cj.b4);
//
//					
//					if (element != null) {
//						//check for same j index
//						if (element.j == cj.index) {
//							element.a += aij;
//							A.add(element);
//							element = null;
//						} else {
//							//different index
//							A.add(element);
//							//add to list and create a new element
//							element = null;
//						}
//					//no element, create a new
//					} else {						
//						//add to list and create a new element
//						element = new Sparse();
//						element.i = i;
//						element.j = cj.index;
//						element.a = aij;
//					} 
//						
//					
//				
//					if (i1.hasNext()) {
//						cj = i1.next();
//						cjindex = cj.index;
//					} else {
//						cjindex = Integer.MAX_VALUE;
//					}
//
//					
//					
//				} else {
//
//					//System.out.println(""+ck.index);
//					//System.out.println(""+cj.index);
//
//
//					double aij = 0;
//					if ( ci.body2 == ck.body1)
//						aij += ci.b3.dot(ck.b1) + ci.b4.dot(ck.b2);
//
//					else if (ci.body2 == ck.body2)
//						aij += ci.b3.dot(ck.b3) + ci.b4.dot(ck.b4);
//
//
//					
//					if (element != null) {
//						//check for same j index
//						if (element.j == ck.index) {
//							element.a += aij;
//							A.add(element);
//							element = null;
//						} else {
//							//different index
//							A.add(element);
//							//add to list and create a new element
//							element = null;
//						}
//					//no element, create a new
//					} else {						
//						//add to list and create a new element
//						element = new Sparse();
//						element.i = i;
//						element.j = ck.index;
//						element.a = aij;
//					} 
//
//					
//					if (i2.hasNext()) {
//						ck = i2.next();
//						ckindex = ck.index;
//					} else {
//						ckindex = Integer.MAX_VALUE;
//					}
//
//
//				}
//				
//				
//				
//				if (cjindex == Integer.MAX_VALUE && ckindex == Integer.MAX_VALUE)
//					break;
//			}

			A.clear();
			i=0;
			for (ConstraintEntry cs: constraints) {
				int j=0;
				for (ConstraintEntry ct: constraints) {
					Sparse e = new Sparse();
					e.i=i; e.j = j; e.a = 0;
					
					if( cs.body1 == ct.body1) {
						e.a = cs.j1.dot(ct.b1) + cs.j2.dot(ct.b2);
					}

					if (cs.body2 == ct.body2) {
						e.a = e.a + cs.j3.dot(ct.b3) + cs.j4.dot(ct.b4);
					}

					if (cs.body1 == ct.body2) {
						e.a = e.a + cs.j1.dot(ct.b3) + cs.j2.dot(ct.b4);
					}

					if (cs.body2 == ct.body1) {
						e.a = e.a + cs.j3.dot(ct.b1) + cs.j4.dot(ct.b2);
					}
					
					if (Math.abs(e.a)> 1e-5)
						A.add(e);
					j++;
				}
				i++;
			}


//			for (ConstraintEntry cj: ci.body1.constraints) {
//				if ( ci.body1 == cj.body1)
//					A[i][cj.index] += ci.b1.dot(cj.b1) + ci.b2.dot(cj.b2);
//				
//				if (ci.body1 == cj.body2)
//					A[i][cj.index] += ci.b1.dot(cj.b3) + ci.b2.dot(cj.b4);
//			}
//			for (ConstraintEntry cj: ci.body2.constraints) {
//				
//				if ( ci.body2 == cj.body1)
//					A[i][cj.index] += ci.b3.dot(cj.b1) + ci.b4.dot(cj.b2);
//				
//				if (ci.body2 == cj.body2)
//					A[i][cj.index] += ci.b3.dot(cj.b3) + ci.b4.dot(cj.b4);
//			}
			
//			i = i+1;
//		}
		
		
		ListIterator<Sparse> iter = A.listIterator();
		while (iter.hasNext()) {
			Sparse s = iter.next();
//			if (Math.abs(s.a) < 1e-5) {
//				iter.remove();
//			} 
			
//			System.out.println("("+s.i+","+s.j+") " + s.a);
		}
		
		//System.out.println(""+A.size());
		
		
		//do jacobian
		//List<Sparse> Jacobian = new ArrayList<Sparse>();
		Jacobian.clear();
		for (Sparse e: A) {
			i = e.i;
			int j = e.j;
			double aij = e.a;

			ConstraintEntry ci = constraints.get(i);
			ConstraintEntry cj = constraints.get(j);

			Sparse Jij = new Sparse();
			Jij.i = e.i; Jij.j = e.j;
			Jacobian.add(Jij);

			//normal constraints
			if (ci.coupledMax == null) {

				//J[i][j] = 0;

				//if (ai[j] != 0) {
				if (Math.abs(w[i])+Math.abs(ci.lambda) < 1e-14) {
					ci.lambda += 1e-10;
				}

				//diagonal and off-diagonal
				if (ci==cj) {
					Jij.a = (aij*w[i]+ci.lambda)/Math.sqrt(w[i]*w[i]+ci.lambda*ci.lambda)-aij-1;
				} else {
					Jij.a = aij*(w[i]/Math.sqrt(w[i]*w[i]+ci.lambda*ci.lambda)-1);
				}
				//}

				j++;

				//frictions constraints
			} else if (true) {
				Jij.a = 0;



				//double limit = Math.abs(ci.coupledMax.lambda)*0.707106*friction;
				//ci.lambdaMin = -limit;
				//ci.lambdaMax = limit;


				double n_u = Math.sqrt( Math.pow(ci.lambdaMax-ci.lambda, 2) + w[i]*w[i]);
				double phi_u = fisher( ci.lambdaMax-ci.lambda, -w[i]);
				double n_l = Math.sqrt( (ci.lambda-ci.lambdaMin)*(ci.lambda-ci.lambdaMin) + phi_u*phi_u );
				if (n_u < 1e-14 || n_l < 1e-14) {
					ci.lambda += 1e-13;

					n_u = Math.sqrt( Math.pow(ci.lambdaMax-ci.lambda, 2) + w[i]*w[i]);
					phi_u = fisher( ci.lambdaMax-ci.lambda, -w[i]);
					n_l = Math.sqrt( (ci.lambda-ci.lambdaMin)*(ci.lambda-ci.lambdaMin) + phi_u*phi_u );
				}

				//the change with respect to the friction force
				if (ci==cj) {
					phi_u = fisher( ci.lambdaMax-ci.lambda, -w[i]);
					double dxjphi_u = aij*(w[i]/n_u+1)-((ci.lambdaMax-ci.lambda)/n_u) +1;
					Jij.a = (1/n_l)*(phi_u*dxjphi_u+(ci.lambda-ci.lambdaMin))-dxjphi_u-1;     
					//J[i][j] = 0;
					//off-diagonal j|=i
				} else { 

					if ( cj == ci.coupledMax && false) {
						double mu = friction;
						//how the constraint changes when we change the normal component
						//		                if (j==normal_i && 0
						double si = Math.signum(ci.coupledMax.lambda);
						double xn = Math.abs(ci.coupledMax.lambda);
						if (si == 0)
							si =1;

						phi_u = fisher( ci.lambdaMax-ci.lambda, -w[i]);
						double dxjphi_u = aij*(w[i]/n_u+1)+mu*si*((mu*xn-ci.lambda)/n_u-1);
						Jij.a = (1/n_l)*( (ci.lambda-ci.lambdaMin)*(-mu*si) + phi_u*dxjphi_u) - dxjphi_u + mu*si;
						//		                else
					} else {
						phi_u = fisher( ci.lambdaMax-ci.lambda, -w[i]);
						double dxjphi_u = aij*((w[i]/n_u)+1);   
						Jij.a = (phi_u/n_l-1)*(w[i]/n_u+1)*aij;
						//System.out.printf("%13.2e\n", J[i][j]);
						//J[i][j] = 0;

					}
				}
			}
		} // for elements in A

		
	}
	
	public void jacobian(List<ConstraintEntry> constraints, double[][] J, double[] v, int n) {
		if (constraints.size()<1)
			return;
	
		//jacobian2(constraints,J,v,n);
		
		// TODO Auto-generated method stub
		ConstraintEntry e = constraints.get(0);
		//System.out.println();
		
		//for now, display the J*W*J' matrix
		int i = 0; int k =0;
		for (ConstraintEntry ci: constraints) {
			//System.out.println();

			v[i] = -ci.b;
			double ai[] = new double[n];

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

				v[i] = v[i] + ai[j]*cj.lambda;

				//System.out.printf("%f,  ", ai[j]);
				
				j++;
			}// for cj

			
			//System.out.printf("%f,  ", v[i]);

			//System.out.println("\n"+k+" iterations");
			
			//jacobian pass
			j=0;
			for (ConstraintEntry cj: constraints) {
				//normal constraints
				if (ci.coupledMax == null) {

					J[i][j] = 0;

					//if (ai[j] != 0) {
					if (Math.abs(v[i])+Math.abs(ci.lambda) < 1e-14) {
						ci.lambda += 1e-10;
					}

					//diagonal and off-diagonal
					if (ci==cj) {
						J[i][j] = (ai[j]*v[i]+ci.lambda)/Math.sqrt(v[i]*v[i]+ci.lambda*ci.lambda)-ai[j]-1;
					} else {
						J[i][j] = ai[j]*(v[i]/Math.sqrt(v[i]*v[i]+ci.lambda*ci.lambda)-1);
					}
					//}

					j++;
				
					//frictions constraints
				} else if (true) {
					J[i][j] = 0;


					
					//double limit = Math.abs(ci.coupledMax.lambda)*0.707106*0.85;
					//ci.lambdaMin = -limit;
					//ci.lambdaMax = limit;


					double n_u = Math.sqrt( Math.pow(ci.lambdaMax-ci.lambda, 2) + v[i]*v[i]);
		            double phi_u = fisher( ci.lambdaMax-ci.lambda, -v[i]);
		            double n_l = Math.sqrt( (ci.lambda-ci.lambdaMin)*(ci.lambda-ci.lambdaMin) + phi_u*phi_u );
		            if (n_u < 1e-14 || n_l < 1e-14) {
		                ci.lambda += 1e-13;
		                
		                n_u = Math.sqrt( Math.pow(ci.lambdaMax-ci.lambda, 2) + v[i]*v[i]);
		                phi_u = fisher( ci.lambdaMax-ci.lambda, -v[i]);
		                n_l = Math.sqrt( (ci.lambda-ci.lambdaMin)*(ci.lambda-ci.lambdaMin) + phi_u*phi_u );
		            }

		            //the change with respect to the friction force
		            if (ci==cj) {
		                phi_u = fisher( ci.lambdaMax-ci.lambda, -v[i]);
		                double dxjphi_u = ai[j]*(v[i]/n_u+1)-((ci.lambdaMax-ci.lambda)/n_u) +1;
		                J[i][j] = (1/n_l)*(phi_u*dxjphi_u+(ci.lambda-ci.lambdaMin))-dxjphi_u-1;     
                        //J[i][j] = 0;
		            //off-diagonal j|=i
		            } else { 
		                
		            	if ( cj == ci.coupledMax && false) {
		            		double mu = friction;
		                //how the constraint changes when we change the normal component
//		                if (j==normal_i && 0
		                    double si = Math.signum(ci.coupledMax.lambda);
		                    double xn = Math.abs(ci.coupledMax.lambda);
		                    if (si == 0)
		                        si =1;

			            	phi_u = fisher( ci.lambdaMax-ci.lambda, -v[i]);
		                    double dxjphi_u = ai[j]*(v[i]/n_u+1)+mu*si*((mu*xn-ci.lambda)/n_u-1);
		                    J[i][j] = (1/n_l)*( (ci.lambda-ci.lambdaMin)*(-mu*si) + phi_u*dxjphi_u) - dxjphi_u + mu*si;
//		                else
		            	} else {
		            	phi_u = fisher( ci.lambdaMax-ci.lambda, -v[i]);
		            	double dxjphi_u = ai[j]*((v[i]/n_u)+1);   
		            	J[i][j] = (phi_u/n_l-1)*(v[i]/n_u+1)*ai[j];
		            	//System.out.printf("%13.2e\n", J[i][j]);
			            //J[i][j] = 0;

		            	}
		            }
		            j++;
				}
			}
			
			i++;
		}//for ci

	}
	
	private static double fisher( double a, double b) {
		return Math.sqrt( a*a+b*b)-a-b;
	}
	
	
	public double[] CG(double[][] A, double[] b, double epsilon, double min, double max) {
		int n = b.length;
		//System.out.println("CG:"+n);

//		Minv = eye(n);
//		Minv = inv(diag(diag(A)));
//		%Minv = Minv*A';

		//M inverse diagonal
		double[] M = new double[n];
		for (int i=0;i<n;i++) {
			M[i] = 1/A[i][i];
			//M[i] = 1;
		}
		
		//x = zeros(n,1);
		double x[] = new double[n];
		double x0[] = new double[n];
		double prev_norm = Double.POSITIVE_INFINITY;
		//r = b - A*x;
		double r[] = new double[n];
		for (int i=0;i<n;i++) {
			r[i] = b[i];
//			for (int j=0;i<n;i++) {
//				 r[i] += -A[i][j] * x[j]; 
//			}
		}
		
		//d = Minv*r;
		double d[] = new double[n];
		for (int i=0;i<n;i++) {
			d[i] = M[i] * r[i];
		}
		
		//delta = r'*d;
		double delta = 0;
		for (int i=0;i<n;i++) {
			delta += r[i] * d[i];
		}
		
		//delta0 = delta;
		double delta0 = delta;
		int		iter = 0;


//		while iter < min([maximum n+1]) || iter < minimum		
		while( iter < Math.min(max, n) ) {
			//System.out.println("CG: iteration " + iter);
		    //q = A*d;
			double q[] = new double[n];			
			for (int i=0;i<n;i++) {
				for (int j=0;j<n;j++) {
					 q[i] += A[i][j] * d[j]; 
				}
			}
			
//		   if abs(d'*q)< 1e-14 || abs(delta)< 1e-14
//		        xN=x;
//		        return;
//		    end
		    
//		    alpha = delta / (d'*q);
			double tmp = 0;
			for (int i=0;i<n;i++) {
				tmp += d[i] * q[i];
			}
			
			//break on sigular update
			if (Math.abs(tmp) < 1e-14 || Math.abs(delta) < 1e-14 ) {
				System.out.println("CG: break");
				break;
			}
			
			double alpha = delta / tmp;
			
		    //x = x + alpha*d;
			for (int i=0;i<n;i++) {
				x0[i] = x[i];
				x[i] += alpha * d[i];
			}
			
		    //r = r - alpha*q;
		    //%r = b - A*x;
			for (int i=0;i<n;i++) {
				r[i] = r[i] - alpha * q[i];
			}
			
		    //s = Minv*r;
			double s[] = new double[n];
			for (int i=0;i<n;i++) {
				s[i] =  M[i] * r[i];
			}
		  
		    iter = iter +1; 
		    
//		    if norm(r)<epsilon && iter >= minimum   
//		    %if norm(alpha*d)<epsilon
//		        break;
//		    end
		    double norm = 0;
			for (int i=0;i<n;i++) {
				norm += r[i]*r[i];
			}
			norm = Math.sqrt(norm);
			//System.out.println("CG: residual norm " + norm);
			if (norm<epsilon) {
				//System.out.println("CG: termination");
				break;
			}
			if ( prev_norm < norm) {
				//System.out.println("CG: abort because of rising residual");
				return x0;
				//break;
			} else {
				prev_norm = norm;
			}
		    
//		    delta_old = delta;
//		    delta = r'*s;
			double delta_old = delta;
			delta = 0;
			for (int i=0;i<n;i++) {
				delta +=  r[i]*s[i];
			}
		    
		    //beta = delta / delta_old;
			double beta = delta / delta_old;
			
		    //d = s + beta*d;
			for (int i=0;i<n;i++) {
				d[i] = s[i] + beta * d[i];
			}
		    
		}//while
		
		return x;
	}

	
	
	public double[] CG2(List<Sparse> A, double[] b, double epsilon, double min, double max) {
		int n = b.length;
		//System.out.println("CG:"+n);

//		Minv = eye(n);
//		Minv = inv(diag(diag(A)));
//		%Minv = Minv*A';

		//M inverse diagonal
//		double[] M = new double[n];
//		for (int i=0;i<n;i++) {
//			M[i] = 1/A[i][i];
//			//M[i] = 1;
//		}

		double[] M = new double[n];
		for (Sparse e: A) {
			if (e.i == e.j) {
				M[e.i] = 1/e.a;
				//System.out.println("("+e.i+","+e.j+")");
			}
		}
		
		//x = zeros(n,1);
		double x[] = new double[n];
		double x0[] = new double[n];
		double prev_norm = Double.POSITIVE_INFINITY;
		//r = b - A*x;
		double r[] = new double[n];
		for (int i=0;i<n;i++) {
			r[i] = b[i];
//			for (int j=0;i<n;i++) {
//				 r[i] += -A[i][j] * x[j]; 
//			}
		}
		
		//d = Minv*r;
		double d[] = new double[n];
		for (int i=0;i<n;i++) {
			d[i] = M[i] * r[i];
		}
		
		//delta = r'*d;
		double delta = 0;
		for (int i=0;i<n;i++) {
			delta += r[i] * d[i];
		}
		
		//delta0 = delta;
		double delta0 = delta;
		int		iter = 0;


//		while iter < min([maximum n+1]) || iter < minimum		
		while( iter < Math.min(max, n) ) {
			//System.out.println("CG: iteration " + iter);
		    //q = A*d;
//			double q[] = new double[n];			
//			for (int i=0;i<n;i++) {
//				for (int j=0;j<n;j++) {
//					 q[i] += A[i][j] * d[j]; 
//				}
//			}

			double q[] = new double[n];			
			for (Sparse e: A) {
				q[e.i] += e.a*d[e.j];
			}
			
//		   if abs(d'*q)< 1e-14 || abs(delta)< 1e-14
//		        xN=x;
//		        return;
//		    end
		    
//		    alpha = delta / (d'*q);
			double tmp = 0;
			for (int i=0;i<n;i++) {
				tmp += d[i] * q[i];
			}
			
			//break on sigular update
			if (Math.abs(tmp) < 1e-14 || Math.abs(delta) < 1e-14 ) {
				//System.out.println("CG: break");
				break;
			}
			
			double alpha = delta / tmp;
			
		    //x = x + alpha*d;
			for (int i=0;i<n;i++) {
				x0[i] = x[i];
				x[i] += alpha * d[i];
			}
			
		    //r = r - alpha*q;
		    //%r = b - A*x;
			for (int i=0;i<n;i++) {
				r[i] = r[i] - alpha * q[i];
			}
			
		    //s = Minv*r;
			double s[] = new double[n];
			for (int i=0;i<n;i++) {
				s[i] =  M[i] * r[i];
			}
		  
		    iter = iter +1; 
		    
//		    if norm(r)<epsilon && iter >= minimum   
//		    %if norm(alpha*d)<epsilon
//		        break;
//		    end
		    double norm = 0;
			for (int i=0;i<n;i++) {
				norm += r[i]*r[i];
			}
			norm = Math.sqrt(norm);
			//System.out.println("CG: residual norm " + norm);
			if (norm<epsilon) {
				//System.out.println("CG: termination");
				break;
			}
			if ( prev_norm < norm) {
				//System.out.println("CG: abort because of rising residual");
				return x0;
				//break;
			} else {
				prev_norm = norm;
			}
		    
//		    delta_old = delta;
//		    delta = r'*s;
			double delta_old = delta;
			delta = 0;
			for (int i=0;i<n;i++) {
				delta +=  r[i]*s[i];
			}
		    
		    //beta = delta / delta_old;
			double beta = delta / delta_old;
			
		    //d = s + beta*d;
			for (int i=0;i<n;i++) {
				d[i] = s[i] + beta * d[i];
			}
		    
		}//while
		
		return x;
	}

	
	public int CGfiniteDifference( double[] x, List<ConstraintEntry> constraints, List<ConstraintEntry> off, double[] lambda, double[] b, double epsilon, int min, int max, double[] diag, double damper) {
		int n = b.length;
		//System.out.println("CG:"+n);

//		Minv = eye(n);
//		Minv = inv(diag(diag(A)));
//		%Minv = Minv*A';

		//M inverse diagonal
//		double[] M = new double[n];
//		for (int i=0;i<n;i++) {
//			M[i] = 1/A[i][i];
//			//M[i] = 1;
//		}
		

		//identity preconditioner
		double[] M = new double[n];
		for (int i=0;i<n;i++) {
			M[i] = 1/diag[i];
			//M[i] = 1;
		}

		//x = zeros(n,1);
		double w[] = new double[n];
		//double x[] = new double[n];
		double x0[] = new double[n];
		double x_best[] = new double[n];
		//r = b - A*x;
		double r[] = new double[n];
		double bnorm = 0;
		for (int i=0;i<n;i++) {
			//x[i] = x0[i];,
			r[i] = b[i];
			bnorm += b[i]*b[i];
//			for (int j=0;i<n;i++) {
//				 r[i] += -A[i][j] * x[j]; 
//			}
		}
		bnorm = Math.sqrt(bnorm);

		double prev_norm = bnorm;
		prev_norm = Double.POSITIVE_INFINITY;
		
		//d = Minv*r;
		double d[] = new double[n];
		for (int i=0;i<n;i++) {
			d[i] = M[i] * r[i];
		}
		
		//delta = r'*d;
		double delta = 0;
		for (int i=0;i<n;i++) {
			delta += r[i] * d[i];
		}
		
		//delta0 = delta;
		double delta0 = delta;
		int		iter = 0;
		double delta_old = Double.POSITIVE_INFINITY;
		double best_delta = Double.POSITIVE_INFINITY;

//		while iter < min([maximum n+1]) || iter < minimum		
		while( iter < Math.min(max, n) ) {
			//System.out.println("CG: iteration " + iter);
		    //q = A*d;
//			double q[] = new double[n];			
//			for (int i=0;i<n;i++) {
//				for (int j=0;j<n;j++) {
//					 q[i] += A[i][j] * d[j]; 
//				}
//			}

			//break on sigular update
			if (/*Math.abs(tmp) < 1e-14 ||*/ Math.abs(delta) < epsilon ) {
				//System.out.println("CG: break");
				break;
			}
			
			if ( best_delta > delta && iter > 1) {
				best_delta = delta;
				
				for (int i=0;i<n;i++) {
					x_best[i] = x[i];
				}
			}
			
			
			
			if ( /*delta_old < delta &&*/ iter > max) {
				for (int i=0;i<n;i++) {
					x[i] = x_best[i];
				}
				//break;
				//System.out.println("CG:break" + delta_old +",    " + delta + "best=" + best_delta);
				return iter;
				//break;
			}

			
			double q[] = new double[n];			
//			for (Sparse e: A) {
//				q[e.i] += e.a*d[e.j];
//			}
			
			//compute finite diffecence Jd product
			double h = 1e-10;
			double[] tmpv = new double[n];
			double[] bdif = new double[n];
			for (int i=0;i<n;i++)
				tmpv[i] = lambda[i] + d[i]*h;
			computeVelocity2(constraints, off, tmpv , w, damper);
			computeFisher(constraints, tmpv, w, bdif);
			//computeMinMap(constraints, tmpv, w, bdif);

			for (int i=0;i<n;i++)
				q[i] = (-bdif[i] + b[i])/h;
			
		    iter = iter +1; 
			
			
//		   if abs(d'*q)< 1e-14 || abs(delta)< 1e-14
//		        xN=x;
//		        return;
//		    end
		    
//		    alpha = delta / (d'*q);
			double tmp = 0;
			for (int i=0;i<n;i++) {
				tmp += d[i] * q[i];
			}
			
			
			double alpha = delta / tmp;
			
		    //x = x + alpha*d;
			for (int i=0;i<n;i++) {
				x0[i] = x[i];
				x[i] += alpha * d[i];
			}
			
		    //r = r - alpha*q;
		    //%r = b - A*x;
			for (int i=0;i<n;i++) {
				r[i] = r[i] - alpha * q[i];
			}
			
		    //s = Minv*r;
			double s[] = new double[n];
			for (int i=0;i<n;i++) {
				s[i] =  M[i] * r[i];
			}
		  
		    
//		    if norm(r)<epsilon && iter >= minimum   
//		    %if norm(alpha*d)<epsilon
//		        break;
//		    end
		    double norm = 0;
			for (int i=0;i<n;i++) {
				norm += r[i]*r[i];
			}
			norm = Math.sqrt(norm);
			//System.out.println("CG: residual norm " + norm);
			//if (norm<epsilon) {
				//System.out.println("CG: termination");
			//	break;
			//}

//			if ( prev_norm < norm) {
//				//System.out.println("CG: abort because of rising residual " + iter);
//				//System.out.println("CG: " + iter);
//
//				//if not in the first iteration, return the 
//				//previous x vector (with the better norm)
//				//if (iter>1) {
//					for (int i=0;i<n;i++) {
//						x[i] = x0[i];
//					}
//					//break;
//					return iter;
//				//}
//				//prev_norm = norm;
//
//			} else {
//				prev_norm = norm;
//			}
		    
//		    delta_old = delta;
//		    delta = r'*s;
			delta_old = delta;
			delta = 0;
			for (int i=0;i<n;i++) {
				delta +=  r[i]*s[i];
			}
		    
		    //beta = delta / delta_old;
			double beta = delta / delta_old;
			
		    //d = s + beta*d;
			for (int i=0;i<n;i++) {
				d[i] = s[i] + beta * d[i];
			}
		    
		}//while
		
		//System.out.println("CG: " + iter);
		return iter;

	}


}


