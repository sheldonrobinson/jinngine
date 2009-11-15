package jinngine.physics.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import jinngine.physics.Body;
import jinngine.physics.solver.Solver.constraint;

public class SubspaceMinPaper implements Solver {

	private final Solver pgs = new ProjectedGaussSeidel(25);
	private final Solver cg  = new ConjugateGradients();
	private final Solver projection = new Projection();
	
	private final List<constraint> inactive = new ArrayList<constraint>();

	@Override
	public void setMaximumIterations(int n) {
		// TODO Auto-generated method stub
	}

	@Override
	public double solve(List<constraint> constraints, List<Body> bodies) {
    	

		
	//	while (true) {
		for (int i=0;i<4;i=i+1) {	
			//run the PGS solver
			pgs.setMaximumIterations(5);
			pgs.solve(constraints,bodies);
			
			
			//find inactive set
			inactive.clear();
			for (constraint ci: constraints) {
				if (ci.lower < ci.lambda && ci.lambda < ci.upper /*&& Math.abs(ci.lambda) > 1e0)*/   ) { 
					inactive.add(ci);
				}
			}
			
			
			//Subspace minimisation
			while (true) {
				//solve the active set
				cg.solve( inactive, bodies);

				double p =projection.solve(inactive,bodies);

				if (p == 0)
					break;
				
				//remove active constraints
				ListIterator<constraint> j = inactive.listIterator();
				while(j.hasNext()) {
					constraint ci = j.next();
					if (ci.lower < ci.lambda && ci.lambda < ci.upper) { 
						//do nothing
					} else {
						j.remove();
					}					
				}
				
				
			} //sub space
			
			
		}

		//re-adjust friction limits
		for (constraint ci: constraints) {
			if (ci.coupling != null) {
				//if the constraint is coupled, allow only lambda <= coupled lambda
				ci.lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;
				ci.upper =  Math.abs(ci.coupling.lambda)*ci.coupling.mu;
			} 
		}

		//System.out.println("constraints " + constraints.size() + ", inactive "+ inactive.size() );
		double phi = FischerNewtonConjugateGradients.fischerMerit(constraints, bodies);	

		if(phi>0.1) 
			System.out.println("fischer="+phi);	
		
//		for (constraint ci: constraints) {
//			System.out.println(""+ci.lambda);
//		}

		return 0;
	}

	@Override
	public void setDamping(double damping) {
		// TODO Auto-generated method stub
		
	}

}
