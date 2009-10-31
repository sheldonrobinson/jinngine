package jinngine.physics.solver;

import java.util.List;
import jinngine.physics.Body;

/**
 * The PGS-Subspace Minimisation method. Method is based on using PGS to guess 
 * which variables is in the active set. Having that, it remains to solve a 
 * linear system of equations, which is done using Conjugate Gradients. 
 */
public class SubspaceMinimization implements Solver {

	@Override
	public void setMaximumIterations(int n) {
		// TODO Auto-generated method stub

	}

	@Override
	public double solve(List<constraint> constraints, List<Body> bodies) {
		return 0;
	}
}
