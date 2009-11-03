package jinngine.physics.solver;

import java.util.ArrayList;
import java.util.List;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.Solver.constraint;

/**
 * The PGS-Subspace Minimisation method. Method is based on using PGS to guess 
 * which variables is in the active set. Having that, it remains to solve a 
 * linear system of equations, which is done using Conjugate Gradients. 
 */
public class SubspaceMinimization implements Solver {
	
	private final Solver newton = new FischerNewtonConjugateGradients();
	private final Solver pgs = new ProjectedGaussSeidel(25);
	private final Solver cg  = new ConjugateGradients();
	//private final Solver cg  = new FischerNewtonConjugateGradients();
	
	private final List<constraint> active = new ArrayList<constraint>();
	private final List<constraint> normals = new ArrayList<constraint>();

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
 
    	//newton.solve(normals,bodies);
    	
		pgs.setMaximumIterations(5);

	//	while (true) {
		for (int i=0;i<3;i=i+1) {
			pgs.solve(constraints,bodies);
			
			//find active set
			active.clear();
			for (constraint ci: constraints) {

				
				if ((ci.lambda > ci.lower && ci.lambda < ci.upper)   ) { 
					active.add(ci);
				}
			}
			
			//solve the active set
			cg.solve( active, bodies);
			
		}

		
		//System.out.println("constraints " + constraints.size() + ", active "+ active.size() );
			
		return 0;
	}
}
