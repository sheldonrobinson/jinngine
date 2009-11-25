package jinngine.physics.solver.experimental;

import java.util.ArrayList;
import java.util.List;
import jinngine.physics.Body;
import jinngine.physics.solver.*;

public class QuadraticPrograming implements Solver {

	private final List<constraint> normals = new ArrayList<constraint>();
	private final Solver cg = new ConjugateGradients();
	
	@Override
	public void setMaximumIterations(int n) {
		// TODO Auto-generated method stub

	}

	@Override
	public double solve(List<constraint> constraints, List<Body> bodies, double epsilon) {
	   	normals.clear();
    	for (constraint ci: constraints) 
    		if (ci.coupling == null) {
    			normals.add(ci);
    			ci.b = ci.b;
    		}

	
    	cg.solve(normals, bodies, 0.0);	
    	return 0;
	}
}
