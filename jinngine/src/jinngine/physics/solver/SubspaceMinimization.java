package jinngine.physics.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.util.ListIterator;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.Solver.constraint;

import Jama.*;

/**
 * The PGS-Subspace Minimisation method. Method is based on using PGS to guess 
 * which variables is in the active set. Having that, it remains to solve a 
 * linear system of equations, which is done using Conjugate Gradients. 
 */
public class SubspaceMinimization implements Solver {
	private final Solver newton = new FischerNewtonConjugateGradients();
	private final Solver cg  = new ConjugateGradients();
	private final Solver projection = new Projection();
	//private final Solver cg  = new FischerNewtonConjugateGradients();	
	private final List<constraint> inactive = new ArrayList<constraint>();
	private final List<constraint> normals = new ArrayList<constraint>();
	
	private final double epsilon = 1e-7;
	private final int pgsmin = 15;
	private final ProjectedGaussSeidel pgs = new ProjectedGaussSeidel(pgsmin);
	private double phi;
	
	
	@Override
	public void setMaximumIterations(int n) {
		// TODO Auto-generated method stub

	}

	@Override
	public double solve(List<constraint> constraints, List<Body> bodies) {
	   	normals.clear();
    	for (constraint ci: constraints) 
    		if (ci.coupling == null)
    			normals.add(ci);
    	
		solveInternal(normals, bodies);
//		
//		//compute bounds
//		for (constraint ci: constraints) {
//			if (ci.coupling != null) {
//				//if the constraint is coupled, allow only lambda <= coupled lambda
//				ci.lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;
//				ci.upper =  Math.abs(ci.coupling.lambda)*ci.coupling.mu;
//			} 
//		}
		
		//solveInternal(constraints,bodies);

		solveInternal(normals, bodies);

		return 0;
	}

	
	public double solveInternal(List<constraint> constraints, List<Body> bodies) {
		boolean bounds = true;
		double damper = 0.000;
		pgs.setDamping(damper);
		pgs.setUpdateBounds(bounds);
		cg.setDamping(damper);
		//set damping
		for (constraint ci: constraints) {
			if (ci.coupling != null) 
				ci.damper = 0.0000;
			else
				ci.damper = 0.0;
		}
		

 
    	
    	//newton.solve(normals,bodies);

		//track best solution
    	double best_phi = Double.POSITIVE_INFINITY;
    	double[] best_lambda = new double[constraints.size()];
    	
    	//main iterations
		int i=-1;
		while (true) {
			i++;			
			//run PGS a random amount of iterations
			pgs.solve(constraints,bodies);
			
			//find inactive set
			inactive.clear();
			for (constraint ci: constraints) {
				if (ci.lower < ci.lambda && ci.lambda < ci.upper ) { 
					inactive.add(ci);
				}
			}

			//Subspace minimisation
			//boolean done = false;
			int k= 0;
			while (true) {
				k++;
				//System.out.println("sm stage: " + inactive.size() + " constraints");
				
				//solve the active set
				double residual = cg.solve( inactive, bodies);
				
//				if (residual > 0.01) {
//					//increase friction damping
//					for (constraint ci: constraints) {
//						if (ci.coupling != null) 
//							ci.damper += 0.100;
//					
//					}
//
//				}
				
				//use Jama to compute pseudoinverse
//				int n = inactive.size();
//				if(n>0 && false) {
//					double[][] am = new double[n][n];
//					FischerNewtonConjugateGradients.fillInA(inactive, am);
//					Matrix A = new Matrix(am);
//					SingularValueDecomposition svd = new SingularValueDecomposition(A);
//					Matrix sigma = svd.getS();
//					double[][] sigma_m =sigma.getArray();
//
//					//Reciprocal inverse
//					for (int x=0;x<n;x++)
//						if(Math.abs(sigma_m[x][x])>1e-4) 
//							sigma_m[x][x] = 1.0/sigma_m[x][x];
//						else
//							sigma_m[x][x] = 0;
//
//					
//					//compute b vector
//					List<constraint> outs = new ArrayList<constraint>();
//					outs.addAll(constraints);
//					outs.removeAll(inactive);
//					//System.out.println("outs="+outs.size());
//					
//					Matrix Afull = new Matrix(inactive.size(),outs.size());
//					FischerNewtonConjugateGradients.fillInA(inactive, outs, Afull.getArray());
//					Matrix lambdaFull = new Matrix(1,outs.size());
//					Matrix Bfull = new Matrix(1,inactive.size());
//					FischerNewtonConjugateGradients.fillInb(inactive, Bfull.getArray()[0]);
//					FischerNewtonConjugateGradients.fillInLambda(outs, lambdaFull.getArray()[0]);
//					Matrix Breduced = Afull.times(lambdaFull.transpose()).plus(Bfull.transpose().times(-1));
//
//
//					//M.print(1,1);
//
//					//get solution using pseudo inverse
//					Matrix solution = svd.getV().times(sigma).times(svd.getU().transpose().times(Breduced.times(-1)));
//
//					//solution.print(1,1);
//
//
//					int m=0;
//					for (constraint ci: inactive) {
//						double deltaLambda = solution.transpose().getArray()[0][m] - ci.lambda;
//
//						ci.lambda = solution.transpose().getArray()[0][m];
//						//reflect in delta velocities
//						Vector3.add( ci.body1.deltaVCm,     ci.b1.multiply(deltaLambda));
//						Vector3.add( ci.body1.deltaOmegaCm, ci.b2.multiply(deltaLambda));
//						Vector3.add( ci.body2.deltaVCm,     ci.b3.multiply(deltaLambda));
//						Vector3.add( ci.body2.deltaOmegaCm, ci.b4.multiply(deltaLambda));						
//						m++;
//					}
//				}
				
//				for (constraint ci:inactive)
//					System.out.println(""+ci.lambda);
				
				double p =projection.solve(inactive,bodies);

				if (p == 0) {
					if (k==1) {
						//System.out.println("first SM iteration had no change");
						//done = true;
					}
					//System.out.println("sm stage done");
					break;
				}
				
				//remove active constraints
				ListIterator<constraint> j = inactive.listIterator();
				while(j.hasNext()) {
					constraint ci = j.next();
					
					if (ci.coupling != null && bounds) {
						//if the constraint is coupled, allow only lambda <= coupled lambda
						ci.lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;
						ci.upper =  Math.abs(ci.coupling.lambda)*ci.coupling.mu;
					} 
					
					if (ci.lower < ci.lambda && ci.lambda < ci.upper) { 
						//do nothing
					} else {
						j.remove();
					}					
				}
			} //sub space
			
			if (bounds) 
				for (constraint ci: constraints) {
					if (ci.coupling != null) {
						//if the constraint is coupled, allow only lambda <= coupled lambda
						ci.lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;
						ci.upper =  Math.abs(ci.coupling.lambda)*ci.coupling.mu;
					} 
				}
			phi = FischerNewtonConjugateGradients.fischerMerit(constraints, bodies, damper);	
		    //System.out.println("*) pgs-sm iteration "+i+" error="+phi );	

			if (phi < best_phi) {
				best_phi = phi;
				for (int n=0;n<constraints.size(); n++) 
					best_lambda[n] = constraints.get(n).lambda;
			}
			
			
			if (phi < epsilon || i>3) {
				break;
			}
			
			//damper = damper*0.5;
		}
		
		
		// Set the best known solution
		for (Body b: bodies) {
			b.deltaVCm.assignZero();
			b.deltaOmegaCm.assignZero();
		}
		int n=0;
		for (constraint ci: constraints) {
			ci.lambda = best_lambda[n];
			Vector3.add( ci.body1.deltaVCm,     ci.b1.multiply(ci.lambda));
			Vector3.add( ci.body1.deltaOmegaCm, ci.b2.multiply(ci.lambda));
			Vector3.add( ci.body2.deltaVCm,     ci.b3.multiply(ci.lambda));
			Vector3.add( ci.body2.deltaOmegaCm, ci.b4.multiply(ci.lambda));						
			n++;
		}

	  //System.out.println("*) pgs-sm iteration "+i+" error="+phi );	

		
		return 0;
	}

	@Override
	public void setDamping(double damping) {
		// TODO Auto-generated method stub
		
	}
}
