/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.geometry;

import java.util.Iterator;
import java.util.List;
import jinngine.math.Vector3;

/**
 * 2D convex convex intersection. Algorithm due to O'Rourke et al. 1981 "A New Linear 
 * Algorithm for Intersecting Polygons". Intersection is done in the XY plane, the Z component
 * is completely ignored. However, the returned intersection will contain the appropriate Z values
 * for each vertex of the returned polygon. 
 * @author mo 
 *
 */
public class ORourke {
	
	/**
	 * Handler for obtaining the result from a 2d intersection. Intersections will be reported in
	 * counter clock-wise order, wrt. the positive Z axis
	 */
	public interface ResultHandler {
		/**
		 * Called for each point in the intersection result. p will be the point on P and 
		 * like wise q will be the point on Q. The xy coordinates obviously be the same for 
		 * p and q, but the z-value may differ. 
		 */
		public void intersection(Vector3 p, Vector3 q);
	}
	
	private final static double epsilon = 1e-10;
	
	private enum State { 
		none,
		P,
		Q
	};
	
	
	
	public static void run( final List<Vector3> input1, final List<Vector3> input2, final List<Vector3> result ) {
		run( input1, input2, new ResultHandler() {
			public void intersection(Vector3 arg0, Vector3 arg1) {
				// here, we don't care about the z value
				result.add( new Vector3( arg0.x, arg0.y,0));
			}
		});
	}
	
	/**
	 * @param poly1
	 * @param poly2
	 * @param result
	 */
	public static void run( final List<Vector3> input1, final List<Vector3> input2, final ResultHandler result ) {
		// transform polygons into projection space, leave for now
		int iter = 0;
		int intersections = 0;
		int addedpoints = 0;
		int firstIntersectionIter = 0;
		State inside = State.none; 
		boolean switched = false;
		final List<Vector3> poly1;
		final List<Vector3> poly2;
		final Vector3 p = new Vector3(), pm = new Vector3(), pd = new Vector3();
		final Vector3 q = new Vector3(), qm = new Vector3(), qd = new Vector3();
		final Vector3 parameter = new Vector3();
		final Vector3 firstIntersection = new Vector3();
		final Vector3 firstAddedPoint = new Vector3();
		final Vector3 previouslyAddedPoint = new Vector3();
		final Vector3 ipp = new Vector3();
		final Vector3 ipq = new Vector3();
		final Vector3 pnormal = new Vector3();
		final Vector3 qnormal = new Vector3();
		
		// calculate poly normals
		pnormal.assign
		// sort polygons by number of vertices
//		if (input2.size() > input1.size()) {
//			poly1 = input1;
//			poly2 = input2;
//		} else {
//			switched = true;
//			poly2 = input1;
//			poly1 = input2;
//		}
		
		final int N = poly1.size();
		final int M = poly2.size();
		
		// trival cases
		// if any one polygon is empty, so is intersection
		if (poly1.size() == 0 )
			return;
		
		
		if (poly1.size() < 2 || poly2.size() < 2 ) {
			throw new IllegalArgumentException("Polygons must contain at least 2 vertices");
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
					ipp.assign(pm.add( pd.multiply(parameter.x)));
					ipq.assign(qm.add( qd.multiply(parameter.y)));

					intersections = intersections+1;
					//System.out.println("computed intersection="+ip);
					// check if intersection is the same point
					// as first time an intersection was encountered. This
					// means that the algorithm should terminate
					if (intersections > 1) {
						// the firstIntersectionIter condition makes the algorithm able to handle some
						// degenerate cases, as treated in the O'Rourke paper
						if (firstIntersection.minus(ipp).xynorm() < epsilon && firstIntersectionIter!=iter-1) {
							// termination. Before exiting, we check that the first output point is 
							// not equal to the final output point. This can happen in some degenerate cases. If
							// so, we simply remove the final point

//							final int n = result.size();
//							if (n>1) {
//								if( result.get(0).minus( result.get(n-1)).xynorm() < epsilon) {
//									result.remove(n-1);
//								}
//							}
							
							return;
						}
					} else {
						// track the first discovered intersection
						firstIntersection.assign(ipp);
						firstIntersectionIter = iter;
					}
					
					// add intersection point
					if (testPoint(firstAddedPoint, previouslyAddedPoint, ipp, addedpoints)) {
						result.intersection(ipp, ipq);
						addedpoints = addedpoints+1;
					}
					

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
						//intersection.add(q.copy());
						//addPoint(previouslyAddedPoint, q, result);
						if ( testPoint(firstAddedPoint, previouslyAddedPoint, q, addedpoints)) {
							result.intersection(p, projectToPlane(p, qnormal, q));
							addedpoints = addedpoints+1;
						}
					
					qm.assign(q);

					// rewind iterator
					if (!poly2points.hasNext())
						poly2points = poly2.iterator();

					q.assign(poly2points.next());
					qd.assign( q.minus(qm));
				} else {
					//advance p
					if (inside == State.P)
						//intersection.add(p.copy());
						addPoint(previouslyAddedPoint, p, result);

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
						//intersection.add(p.copy());
						addPoint(previouslyAddedPoint, p, result);

					pm.assign(p);
					
					// rewind iterator
					if (!poly1points.hasNext())
						poly1points = poly1.iterator();

					p.assign(poly1points.next());
					pd.assign( p.minus(pm));
				} else {
					//advance q
					if (inside == State.Q)
						//intersection.add(q.copy());
						addPoint(previouslyAddedPoint, q, result);
					
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
 		
		
		// if we end up here, the polygons is either 
		// separated or contained inside each other
		if ( isContained(p, poly2.iterator())) {
//			System.out.println("p is contained in Q");
			// add all points from P as intersection
			result.addAll(poly1);
			return;
		} else if ( isContained(q, poly1.iterator())) {
//			System.out.println("q is contained in P");	
			// add all points from Q as intersection
			result.addAll(poly2);
			return;
		}
		
		// P and Q are separated
		return;
	}
	
	/**
	 * Private method that checks if a new point is equal to the previous point or the first point. The
	 * method also updates the first and previous point vectors. It returns true if the point can be accepted
	 * and false otherwise
	 */
	private static final boolean testPoint( Vector3 first, Vector3 prev, Vector3 point, int addedPoints ) {
		if (addedPoints < 1) {
			first.assign(point);
			prev.assign(point);
			return true;
		} else if ( addedPoints < 2 ) {
			if ( first.minus(point).xynorm()>epsilon) {
				prev.assign(point);
				return true;
			} else {
				return false;
			}
		} else {
			if ( first.minus(point).xynorm()>epsilon && prev.minus(point).xynorm()>epsilon ) {
				prev.assign(point);
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * assign to projected the point that results from projecting point onto the plane defined by normal and ref,
	 * along the positive z axis
	 */
	private static final Vector3 projectToPlane( Vector3 point, Vector3 normal, Vector3 ref) {
		return point.add(0,0,ref.minus(point).dot(normal)/normal.z);
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
	 * Return true if p is contained inside poly. Poly is required to contain at least 3 affine independent points
	 * @param p
	 * @param poly
	 * @return
	 */
	public static final boolean isContained( final Vector3 p, final Iterator<Vector3> poly ) {
		Vector3 p0 = poly.next();
		Vector3 pm = p0;
		
		// test each edge
		while(poly.hasNext()) {
			Vector3 pi = poly.next();
			if (!isInHalfplane(p, pm, pi))
				return false;
			
			pm = pi;
		}
		
		// test last edge from final vertex to 
		// the first vertex, closing the polygon
		if (!isInHalfplane(p, pm, p0))
			return false;
		
		// all tests passed
		return true;
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
		
		// ill-posed problem?
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
