package jinngine.physics.solver;

import java.util.ArrayList;
import java.util.List;

import jinngine.physics.Body;
import jinngine.physics.solver.Solver.constraint;

public class QuadraticPrograming implements Solver {

	private final List<constraint> normals = new ArrayList<constraint>();
	private final Solver cg = new ConjugateGradients();
	
	@Override
	public void setMaximumIterations(int n) {
		// TODO Auto-generated method stub

	}

	@Override
	public double solve(List<constraint> constraints, List<Body> bodies) {
	   	normals.clear();
    	for (constraint ci: constraints) 
    		if (ci.coupling == null) {
    			normals.add(ci);
    			ci.b = ci.b;
    		}

	
    	cg.solve(normals, bodies);	
    	return 0;
	}

	@Override
	public void setDamping(double damping) {
		// TODO Auto-generated method stub
		
	}

}
