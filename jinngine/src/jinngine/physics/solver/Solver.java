package jinngine.physics.solver;
import java.util.*;
import jinngine.physics.*;

/**
 * Solver for a non-linear complimentarity problem (NCP).
 * 
 * Given a list of constraint definitions, approximately solve the resulting velocity based linear complimentary problem (VBLCP).
 * See [Erleben 2004] or [Cato 2005]. The NCP is on the form<p>
 * <p>
 *    Find lambda such that
 *   <p>
 *   for normal force constraints:<br>
 *    w = (J M^-1 J^T) lambda + b >= 0,
 *    lambda >= 0, 
 *    w^T lambda = 0
 *    <p>
 *    and for friction force constraints:<br>
 *    w = (J M^-1 J^T) lambda + b, and one of<p>
 *     
 *            lambda_i  = h_i:   w_i > 0
 *     l_i >= lambda_i >= h_i:   w_i = 0      for all i
 *            lambda_i  = l_i:   w_i < 0
 *            <p>
 *            where limit = mu * N(i) and l=-limit and u=limit
 *    <p>
 *    where J is the jacobian, M is the mass matrix, lambda is the solution vector
 *    l and h are vectors, containing lower and upper limits on the lambda solution entries.
 *    <p>
 *    An entry in the lambda vector is equivalent to an impulse magnitude, which is to be applied
 *    to the pair of bodies described by a {@link ConstraintEntry} object given by the list 
 *    of constraints. Such a single impulse is given by an entry in the vector (J lambda). 
 *    <p>
 *    Upon return, the auxiliary delta velocity fields in all bodies referenced by all given constraints, are to 
 *    be updated with the correct delta velocities, reflecting the impulses found in the solution.
 */
public interface Solver {
	
	/**
	 * Given a list of constraints, solve the corresponding VBLCP 
	 * @param constraints List of constraints
	 * @return error
	 */
	public double solve(List<ConstraintEntry> constraints, List<Body> bodies );

	/**
	 * In each iteration of the solver, the lambda solution entries are changed. Any such a change corresponds to
	 * a change in velocity in the corresponding constraint. If all changes (involving all lambda entries) are below magnitude
	 * of epsilon, the current solution is defined acceptable and the solver is to terminate.
	 *  
	 * @param epsilon minimum change in solution velocity
	 */
	public void setErrorTolerance( double epsilon );
	
	/**
	 * Set an upper limit on the number of iterations, that this solver is allowed to perform. 
	 * @param n
	 */
	public void setMaximumIterations( int n );

}
