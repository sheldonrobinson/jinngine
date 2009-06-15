package jinngine.physics;

import java.util.List;

import jinngine.math.Vector3;

public class NewtonConjugateGradientSolver implements Solver {

	@Override
	public void setErrorTolerance(double epsilon) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaximumIterations(int n) {
		// TODO Auto-generated method stub

	}

	@Override
	public double solve(List<ConstraintEntry> constraints) {
		double epsilon = 1e-7;
		int iterations = 0;
		//number of normal constraints
		int n = 0;
		for (ConstraintEntry c: constraints) {
			if (c.coupledMax == null) {
				n++;
			}
		}
		
		//System.out.println("solver: "+n+" constraints");
		
		//n = 2;
		
		double lambda0[] = new double[n];
		double lambda_best[] = new double[n];
		
		double J[][] = new double[n][n];
		double v[] = new double[n];
		double b[] = new double[n];

		int i = 0;
		for (ConstraintEntry c: constraints) {
			if (c.coupledMax == null) {
				lambda0[i] = c.lambda;
				i++;
			}
		}
		
//		J[0][0] = 1; J[0][1] = 0;  b[0] = 7;
//		J[1][0] = 0; J[1][1] = 1;  b[1] = 7;
//		double[] xm = CG(J, b, 1e-8, 0, 10);	
//		System.out.println("solution " + xm[0] + ", " + xm[1] );
		
		double error = 0;
		double error_inf = 0;
		double error_best = Double.POSITIVE_INFINITY;
		double error_inf_best = Double.POSITIVE_INFINITY;
		double stored_error = 0;
		
		//int s = 0;
		while (true) {
			if (iterations>15)
				break;
			
			
			//evaluate jacobian
			jacobian(constraints,J,v,n);
			
//			for (int i=0;i<n;i++)
//				System.out.print(", " + v[i] );
//			System.out.println();
			
			//evaluate b vector using fisher
			i=0;
			for (ConstraintEntry c: constraints) {
				//disregard friction constraints
				if (c.coupledMax == null) {

					//get the b vector and starting lambda values
					b[i] = -( Math.sqrt( v[i]*v[i]+ c.lambda*c.lambda )-v[i]-c.lambda);
	
					//?
//					if (Double.isNaN(v[i]))
//						b[i] = 0;

					i++;
				}
			}
						
			//norm of b
			error = 0;
			error_inf = 0;
			for (i=0;i<n;i++) {
				error += b[i]*b[i];
				if (error > error_inf)
					error_inf = Math.abs(b[i]);
			}
			error = Math.sqrt(error);
			//System.out.println("**** " + iterations);
			//System.out.println("error="+error);
			
			
			//record best solution
			if ( error < error_best) {
				error_best = error;
				//error_inf_best = error_inf;
				//System.out.println("Newton-CG: stop");
				stored_error = error;
				i=0;
				for (ConstraintEntry c: constraints) {
					if (c.coupledMax == null) {
						lambda_best[i] = c.lambda;
						i++;
					}
				}
				
				//break;
			}

			if ( error_inf < error_inf_best) {
				//error_best = error;
				error_inf_best = error_inf;
//				stored_error = error;
				//System.out.println("Newton-CG: stop");
//				i=0;
//				for (ConstraintEntry c: constraints) {
//					if (c.coupledMax == null) {
//						lambda_best[i] = c.lambda;
//						i++;
//					}
//				}
				//break;
			}

			
			//termination condition
			if (error<epsilon)
				break;
			
			
			//double tol = error*0.5;
			double tol = 1e-7;
			//solve newton equation
			double x[] = CG(J,b,tol,0,10);
	
			//do a newton step
			i=0;
			for (ConstraintEntry c: constraints) {
				if (c.coupledMax == null) {
					c.lambda += x[i];
					//System.out.println("lambda=" + c.lambda);
					i++;
				}
			}
			
//			for (double bi: b) {
//				System.out.println("bi="+bi);
//			}
//			
//			for (double xi: x) {
//				System.out.println("xi="+xi);
//			}
			
			
			iterations++;			
		}
		
		if (error_inf_best > epsilon) {
		//	System.out.printf("error %13.2e, error_inf, %13.2e, iter %d \n",stored_error, error_inf_best, iterations );
	
		}
		
		double norm=0;
		
		//if ( error < epsilon) {
			//compute delta velocities
			i=0;
			for (ConstraintEntry c: constraints) {
				if (c.coupledMax == null) {
					//do final projection
					//if (c.lambda<0) c.lambda = 0;
					c.lambda = lambda_best[i];
					
					norm += c.lambda*c.lambda;
					
					Vector3.add( c.body1.deltaVCm,     c.b1.multiply(c.lambda-lambda0[i]) );
					Vector3.add( c.body1.deltaOmegaCm, c.b2.multiply(c.lambda-lambda0[i]) );
					Vector3.add( c.body2.deltaVCm,     c.b3.multiply(c.lambda-lambda0[i]) );
					Vector3.add( c.body2.deltaOmegaCm, c.b4.multiply(c.lambda-lambda0[i]) );

					i++;
				}
			}
			
//			if (Math.sqrt(norm)  > 100000) {
//				for (ConstraintEntry c: constraints) {
//					if (c.coupledMax == null) {
//						System.out.println("lambda="+c.lambda);
//					}
//				}
//				System.out.println("Total error= " + error);
//			}
			
			

		//} //if error	
		
		return error_best;
	}
	
	public void jacobian(List<ConstraintEntry> constraints, double[][] J, double[] v, int n) {
		if (constraints.size()<1)
			return;
	
		// TODO Auto-generated method stub
		ConstraintEntry e = constraints.get(0);

		//int n = constraints.size();
		
		//double[][] J = new double[n][n];
		
		//for now, display the J*W*J' matrix
		int i = 0; int k =0;
		for (ConstraintEntry ci: constraints) {
			if (ci.coupledMax != null)
				continue;
			//System.out.println(""+ci.b);
			//System.out.println();
			
			v[i] = -ci.b;
			double ai[] = new double[n];

			//velocity pass
			int j=0;
			for (ConstraintEntry cj: constraints) {
				if (cj.coupledMax == null) {
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

					//System.out.print("j="+j+" "+ci);
					//System.out.printf("%13.2e  ",ai[j]);						
					//System.out.printf("%13.2e \n ",vi);						

					j++;
				} else {
					//contribution from non-normal constraints
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


					v[i] = v[i] + aij*cj.lambda;

					//System.out.print("j="+j+" "+ci);
					//System.out.printf("%13.2e  ",ai[j]);						
					//System.out.printf("%13.2e \n ",vi);						
					
				}
				
			}// for cj
			
			//System.out.println("\n"+k+" iterations");
			
			//jacobian pass
			j=0;
			for (ConstraintEntry cj: constraints) {
				//normal constraints
				if (cj.coupledMax == null) {

					J[i][j] = 0;

					if (ai[j] != 0) {
						if (Math.abs(v[i])+Math.abs(ci.lambda) < 1e-7) {
							ci.lambda += 1e-7;
						}

						//diagonal and off-diagonal
						if (ci==cj) {
							J[i][j] = (ai[j]*v[i]+ci.lambda)/Math.sqrt(v[i]*v[i]+ci.lambda*ci.lambda)-ai[j]-1;
						} else {
							J[i][j] = ai[j]*(v[i]/Math.sqrt(v[i]*v[i]+ci.lambda*ci.lambda)-1);
						}
					}
						
					j++;
				
					//frictions constraints
				} else if (false) {

					double limit = Math.abs(ci.coupledMax.lambda)*0.707106*0.85;
					ci.lambdaMin = -limit;
					ci.lambdaMax = limit;

					double n_u = Math.sqrt( Math.pow(ci.lambdaMax-ci.lambda, 2) + v[i]*v[i]);
		            double phi_u = fisher( ci.lambdaMax-ci.lambda, -v[i]);
		            double n_l = Math.sqrt( (ci.lambda-ci.lambdaMin)*(ci.lambda-ci.lambdaMin) + phi_u*phi_u );
		            if (n_u < 1e-14 || n_l < 1e-14) {
		                ci.lambda += 1e-12;
		            }

		            //the change with respect to the friction force
		            if (ci==cj) {
		                phi_u = fisher( ci.lambdaMax-ci.lambda, -v[i]);
		                double dxjphi_u = ai[j]*(v[i]/n_u+1)-((ci.lambdaMax-ci.lambda)/n_u) +1;
		                J[i][j] = (1/n_l)*(phi_u*dxjphi_u+(ci.lambda-ci.lambdaMin))-dxjphi_u-1;     

		            //off-diagonal j|=i
		            } else { 
		                
		                //how the constraint changes when we change the normal component
//		                if (j==normal_i && 0
//		                    si = sign(r(normal_i));
//		                    xn = abs(r(normal_i));
//		                    if si == 0
//		                        si =1;
//		                    end
//		                    %si=1;
//
//		                    phi_u = fisher( hi(i)-r(i), -f(i));
//		                    dxjphi_u = A(i,j)*(f(i)/n_u+1)+mu(i)*si*((mu(i)*xn-r(i))/n_u-1);
//		                    J(k,l) = (1/n_l)*( (r(i)-lo(i))*(-mu(i)*si) + phi_u*dxjphi_u) - dxjphi_u + mu(i)*si;
//		                else
		            	phi_u = fisher( ci.lambdaMax-ci.lambda, -v[i]);
		            	double dxjphi_u = ai[j]*((v[i]/n_u)+1);   
		            	J[i][j] = (phi_u/n_l-1)*(v[i]/n_u+1)*ai[j];
		            }
		            j++;
				}
			}
			
			i++;
		}//for ci

	}
	
	private double fisher( double a, double b) {
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
			if (Math.abs(tmp) < 1e-14 /*|| Math.abs(delta) < 1e-14*/ ) {
				//System.out.println("CG: break");
				break;
			}
			
			double alpha = delta / tmp;
			
		    //x = x + alpha*d;
			for (int i=0;i<n;i++) {
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

}
