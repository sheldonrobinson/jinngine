package jinngine.geometry;

import java.util.Iterator;
import java.util.List;

import jinngine.math.Vector3;

/**
 * 2D convex convex intersection. Algorithm due to O'Rourke et al. 1981 "A New Linear 
 * Algorithm for Intersecting Polygons". 
 * @author mo 
 *
 */
public class ORourke {
	
	private final static double epsilon = 1e-10;
	
	private enum State { 
		none,
		P,
		Q
	};
	/**
	 * @param poly1
	 * @param poly2
	 * @param intersection
	 */
	public static void run( List<Vector3> input1, List<Vector3> input2, List<Vector3> intersection ) {
		// transform polygons into projection space, leave for now
		int iter = 0;
		final List<Vector3> poly1;
		final List<Vector3> poly2;
		final Vector3 p = new Vector3(), pm = new Vector3(), pd = new Vector3();
		final Vector3 q = new Vector3(), qm = new Vector3(), qd = new Vector3();
		final Vector3 parameter = new Vector3();
		Vector3 firstIntersection = null;
		int firstIntersectionIter = 0;
		State inside = State.none; 
		
		
		// sort polygons by number of vertices
		if (input2.size() > input1.size()) {
			poly1 = input1;
			poly2 = input2;
		} else {
			poly2 = input1;
			poly1 = input2;
		}
		
		final int N = poly1.size();
		final int M = poly2.size();
		
		// trival cases
		// if any one polygon is empty, so is intersection
		if (poly1.size() == 0 )
			return;
		
		
		if (poly1.size() < 2 ) {
			throw new IllegalArgumentException("Polygons must contain at least 3 vertices");
		}
		
		// get iterators
		Iterator<Vector3> poly1points = poly1.iterator();
		Iterator<Vector3> poly2points = poly2.iterator();
				
		// get end-vertices
		p.assign(poly1.get(poly1.size()-1));
		q.assign(poly2.get(poly2.size()-1));
		
		// first iteration
		pm.assign(p);
		qm.assign(q);

		// next vertices
		p.assign(poly1points.next());
		q.assign(poly2points.next());
		
		// deltas
		pd.assign( p.minus(pm));
		qd.assign( q.minus(qm));

		// run iterations
		while (true) {
			iter = iter+1;
			System.out.println("iteration " + iter);

			// if intersection is in the interior of the lines
			if (intersect(pm,p,qm,q,parameter,epsilon) ) {				
				if (parameter.x >= 0 && parameter.x <= 1 && parameter.y >= 0 && parameter.y <= 1 ) {
					final Vector3 ip = pm.add( pd.multiply(parameter.x));
					System.out.println("computed intersection="+ip);

					// check if intersection is the same point
					// as first time an intersection was encountered. This
					// means that the algorithm should terminate
					if (firstIntersection!=null) {
						// the firstIntersectionIter condition makes the algorithm able to handle some
						// degenerate cases, as treated in the O'Rourke paper
						if (firstIntersection.minus(ip).norm() < epsilon && firstIntersectionIter!=iter-1) {
							return;
						}
					} else {
						firstIntersection = ip.copy();
						firstIntersectionIter = iter;
					}
					
					// add intersection point
					intersection.add(ip);
					

					// determine inside setting
					if ( isInHalfplane( p, qm, q)) {
						inside = State.P;
					} else {
						inside = State.Q;
					}
				}
			} 
			
			// advance q or p
			if (qd.cross(pd).z >= 0) {
				if (isInHalfplane(p, qm, q)) {
					//advance q
					if (inside == State.Q)
						intersection.add(q.copy());
					
					qm.assign(q);

					// rewind iterator
					if (!poly2points.hasNext())
						poly2points = poly2.iterator();

					q.assign(poly2points.next());
					qd.assign( q.minus(qm));
				} else {
					//advance p
					if (inside == State.P)
						intersection.add(p.copy());
					pm.assign(p);
					
					// rewind iterator
					if (!poly1points.hasNext())
						poly1points = poly1.iterator();

					p.assign(poly1points.next());
					pd.assign( p.minus(pm));
					

				}
			} else { // qd X pd < 0
				if (isInHalfplane(q, pm, p)) {
					//advance p
					if (inside == State.P)
						intersection.add(p.copy());
					pm.assign(p);
					
					// rewind iterator
					if (!poly1points.hasNext())
						poly1points = poly1.iterator();

					p.assign(poly1points.next());
					pd.assign( p.minus(pm));
				} else {
					//advance q
					if (inside == State.Q)
						intersection.add(q.copy());
					
					qm.assign(q);
					
					// rewind iterator
					if (!poly2points.hasNext())
						poly2points = poly2.iterator();

					q.assign(poly2points.next());
					qd.assign( q.minus(qm));

				}	
			}

			if (iter > 2*(N+M)) 
				break;
		} // while true
 		
		System.out.println("degenerate case");
		
	}
	
	/**
	 * Return true if a is in the positive half-plane define by the two points bs->bt. 
	 * @param a
	 * @param bs
	 * @param bt
	 * @return
	 */
	public static final boolean isInHalfplane(final Vector3 a, final Vector3 bs, final Vector3 bt) {
		return (bt.minus(bs)).cross(a.minus(bs)).z >= 0;
	}
	
	/**
	 * Intersect lines in 2d. Lines are given by 
	 *  p(a) = ps + (pt-ps)a
	 *  q(b) = qs + (qt-qs)b
	 *  
	 *  Returns the values of a and b at the intersection point
	 *  in the (x,y) values in given vector st. If lines p and q are 
	 *  parallel or overlapping along a line, the function returns false.
	 *  Otherwise the return value is true
	 *   
	 * @param ps Starting point for line p
	 * @param pt End-point for line p
	 * @param qs Starting point for line q
	 * @param qt End-point for line q
	 * @param st Return vector for parameter values at intersection
	 * @param epsilon Precision value. 
	 * @return True if result is well defined. False solution is non-existent or
	 * not unique
	 */
	public static final boolean intersect( final Vector3 ps, final Vector3 pt, 
			final Vector3 qs, final Vector3 qt, 
			final Vector3 st,
			final double epsilon ) {
		// intersect edges in 2d
		//
		// p(a) = pm + a (p-pm)
		// q(b) = qm + b (q-qm)
			// we want to know a and b so p(a) = q(b)
		// rewriting
		// pm + a(p-pm) = qm + b(q-qm)
		// a(p-pm) - b(q-qm) = qm - pm
		// [ (p-pm) (qm-q) ] [a,b] = [qm-pm]
		// take inverse and obtain a and b.
		// inverse of 2d matrix is
		// [c d]^-1              [ f -d]
		// [e f]    = (cf-ed)^-1 [-e  c]
		// so
		// [a]    [ pd.x qd.x ]^-1 
		// [b] =  [ pd.y qd.y ]   (qm-pm)
		// and then
		// a = 1/(pd.x*qd.y-pd.y*qd.x)( qd.y*b.x + (-qd.x)*b.y );
		// b = 1/(pd.x*qd.y-pd.y*qd.x)( (-pd.y)*b.x + pd.x*b.y );
		
		// line deltas
		Vector3 pd = pt.minus(ps);			
		Vector3 qd = qs.minus(qt); // turned around, see derivation 
		
		// the b-side in the equation in the comments above
		Vector3 b = qs.minus(ps);
		
		// determinant calculation
		double det =  pd.x*qd.y-pd.y*qd.x;		
		
		// ill posed problem?
		if (Math.abs(det)<epsilon)
			return false;
		 
		// calculate parametrisation values at intersection
		double alpha = (1/det)* (qd.y*b.x + (-qd.x)*b.y);
		double beta  = (1/det)* ( (-pd.y)*b.x + pd.x*b.y ); 
		
		// set return values
		st.x = alpha;
		st.y = beta;
		
		// all well
		return true;
	}

}
