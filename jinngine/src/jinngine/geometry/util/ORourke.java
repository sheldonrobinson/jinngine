/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */
package jinngine.geometry.util;
import java.util.Iterator;
import java.util.List;
import jinngine.math.Vector3;

/**
 * 2D convex convex intersection. Algorithm due to O'Rourke et al. 1981 "A New Linear 
 * Algorithm for Intersecting Polygons". Intersection is done in the XY plane, the Z component
 * is completely ignored. However, the returned intersection will contain the appropriate Z values
 * for each vertex of the returned polygon, including vertices originating from edge-edge intersections. 
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
	
	private final static double epsilon = 1e-8;
	private final static double envelope = 0.1;
	
	/**
	 *  Internal algorithm state
	 */
	private enum State { 
		none,
		P,
		Q
	};
		
	/**
	 * Perform XY-plane intersection of poly1 and poly2. The given polygons must be in counter clockwise order.
	 * @param poly1 A closed polygon in the XY plane
	 * @param poly2 A closed polygon in the XY plane
	 * @param result Intersection of poly1 and poly2 in counter clock wise order
	 */
	public final void run( final List<Vector3> poly1, final List<Vector3> poly2, final List<Vector3> result ) {
		run( poly1, poly2, new ResultHandler() {
			public void intersection(Vector3 arg0, Vector3 arg1) {
				if (arg1 == null)
					result.add( new Vector3( arg0.x, arg0.y, arg0.z));
				else 
					result.add( new Vector3( arg1.x, arg1.y, arg1.z));
					
			}
		});
	}
	
	// data
	final Vector3 p = new Vector3(), pm = new Vector3(), pd = new Vector3();
	final Vector3 q = new Vector3(), qm = new Vector3(), qd = new Vector3();
	final Vector3 parameter = new Vector3();
	final Vector3 firstIntersection = new Vector3();
	final Vector3 firstAddedPoint = new Vector3();
	final Vector3 previouslyAddedPoint = new Vector3();
	final Vector3 ipp = new Vector3();
	final Vector3 ipq = new Vector3();
	final Vector3 poly1normal = new Vector3();
	final Vector3 poly2normal = new Vector3();
	final Vector3 projectedPoint = new Vector3();

	

	/**
	 * @param poly1
	 * @param poly2
	 * @param result
	 */
	public final void run( final List<Vector3> poly1, final List<Vector3> poly2, final ResultHandler result ) {
		final int N = poly1.size();
		final int M = poly2.size();
		
		// if any one polygon is empty, so is intersection
		if (N == 0 || M == 0)
			return;
		
		// point-point case
		if ( N==1 && M == 1) {
			p.assign(poly1.get(0));
			q.assign(poly2.get(0));
			// report intersection
			if ( p.sub(q).xynorm() < envelope)
				result.intersection(p, q);
			
			return;
		}

		// point-line case
		if ( N==1 && M == 2) {
			Vector3 x = poly1.get(0);
			Vector3 p1 = poly2.get(0);
			Vector3 p2 = poly2.get(1);

			if ( pointLineIntersection(x, p1, p2, projectedPoint, envelope))
				result.intersection(x, projectedPoint);
			
			return;
		}
		// line-point case
		if ( N==2 && M == 1) {
			Vector3 x = poly2.get(0);
			Vector3 p1 = poly1.get(0);
			Vector3 p2 = poly1.get(1);

			if ( pointLineIntersection(x, p1, p2, projectedPoint, envelope))
				result.intersection(projectedPoint, x);
			
			return;
		}
		
		// point-polygon case
		if (N==1 && M > 2) {
			Vector3 x = poly1.get(0);
			if (isContained(x, poly2)) {
				// polygon normal in 3d
				Vector3 polypoint = poly2.get(0);
				polyNormal(poly2, poly2normal);

				// report
				result.intersection(x, projectToPlane(x, poly2normal, polypoint, projectedPoint));				
			}
			
			return;
		}

		// polygon-point case
		if (N>2 && M==1) {
			Vector3 x = poly2.get(0);
			if (isContained(x, poly1)) {
				// polygon normal in 3d
				Vector3 polypoint = poly1.get(0);
				polyNormal(poly1, poly1normal);
				// report
				result.intersection(projectToPlane(x, poly1normal, polypoint, projectedPoint),x);	
			}
			return;
		}
		
		// line-line case
		if (N==2 && M==2) {
			// do line-line intersection
			final Vector3 p1 = poly1.get(0);
			final Vector3 p2 = poly1.get(1);
			final Vector3 p3 = poly2.get(0);
			final Vector3 p4 = poly2.get(1);
			lineLineIntersection(p1, p2, p3, p4, result);
			return;
		}
		
		// line-polygon case
		if (N==2 && M>2) {
			// compute the normal of polygon 1
			polyNormal(poly2, poly2normal);
			// run line polygon intersection (reversing input and output)
			linePolyIntersection(poly1.get(0), poly1.get(1), poly2, poly2normal, false, result);
			return;
		}
		// polygon-line case
		if (N>2 && M==2) {
			// compute the normal of polygon 1
			polyNormal(poly1, poly1normal);
			// run line polygon intersection (reversing input and output)
			linePolyIntersection(poly2.get(0), poly2.get(1), poly1, poly1normal, true, result);
			return;
		}
		
		// polygon-polygon case
		if (N>2 && M>2) {
			// run the o'rourke intersection algorithm for 2d polygons
			orourke(poly1, poly2, result);
			return;
		}
		
	}
	
	/**
	 * Special method for handling line-line intersections
	 */
	private final void lineLineIntersection( final Vector3 p1, final Vector3 p2, final Vector3 p3, final Vector3 p4, ResultHandler result) {
		// alternative version
		// the two lines are
		// l1(t) = p1 + (p2-p1)t
		// l2(s) = p3 + (p4-p3)s

		// if lines are orthogonal, use the unique intersection test (this is rare)
		final double p4p3Tp2p1 = p4.sub(p3).xydot(p2.sub(p1));		
		if ( Math.abs(p4p3Tp2p1) < epsilon) {
			lineLineIntersection(p1, p2, p3, p4, parameter, epsilon); 
			// intersection in internal part of lines?
			if (parameter.x>=-epsilon && parameter.x <=1+epsilon && parameter.y>=-epsilon && parameter.y<=1+epsilon) {
				// report intersection
				result.intersection(p1.add(p2.sub(p1).multiply(parameter.x)), p3.add(p4.sub(p3).multiply(parameter.y)));
				// done
				return;
			} else {
				// no intersection
				return;
			}	
		}
		// we require (p4-p3)T(p2-p1) > 0. if not, swap p3 and p4
		else if ( p4p3Tp2p1<0) {
			// swap
			Vector3 tmp = new Vector3(p3);
			p3.assign(p4);
			p4.assign(tmp);
		}			

		//			final double e = 0.1;
		final Vector3 p4p3 = p4.sub(p3);
		final Vector3 p2p1 = p2.sub(p1);
		final Vector3 p3p1 = p3.sub(p1);
		// d is the normalised tangent of l2
		final double p4p3n =p4p3.xynorm();
		final Vector3 d = new Vector3( -p4p3.y/p4p3n, p4p3.x/p4p3n, 0 );

		// if lines are not parallel, we can compute two points on l1 where
		// its distance to l2 is equal to envelope (in 2d)
		final double z = p2p1.dot(d);
		double tlow;
		double thigh;
		if (Math.abs(z)>epsilon) {
			// TODO include derivation
			tlow = (-envelope+p3p1.xydot(d))/z;
			thigh = (envelope+p3p1.xydot(d))/z;
			// enforce t1<t2
			if (thigh<tlow) {
				final double t = tlow;
				tlow=thigh;
				thigh=t;
			}
		} else {
			// if lines are parallel, we send tlow and thigh to their limits
			tlow = Double.NEGATIVE_INFINITY;
			thigh = Double.POSITIVE_INFINITY;
		}

		// transform t <-> s
		final double k1 = (1/(p4p3n*p4p3n))*p4p3.xydot(p1.sub(p3));
		final double k2 = (1/(p4p3n*p4p3n))*p4p3.xydot(p2p1);

		// all candidate points end-points
		final double slow = k1+k2*tlow;
		final double st0 = k1;
		final double s0 = 0;
		final double shigh = k1+k2*thigh;
		final double st1 = k1+k2;
		final double s1 = 1;

		// highest possible plow
		double plow = slow>st0? (slow>s0? slow:s0) : (st0>s0? st0:s0);
		// smallest possible phigh
		double phigh = shigh<st1? (shigh<s1? shigh:s1) : (st1<s1? st1:s1);

		// generate intersections
		if ( Math.abs(plow-phigh) < epsilon) {
			result.intersection(p1.add(p2p1.multiply((plow-k1)/k2)), p3.add(p4.sub(p3).multiply(plow)));					
		} else if ( plow < phigh) {
			result.intersection(p1.add(p2p1.multiply((plow-k1)/k2)), p3.add(p4.sub(p3).multiply(plow)));
			result.intersection(p1.add(p2p1.multiply((phigh-k1)/k2)), p3.add(p4.sub(p3).multiply(phigh)));
		}

		return;			
	}
		
	private final void orourke( final List<Vector3> poly1, final List<Vector3> poly2, final ResultHandler result ) {
		// transform polygons into projection space, leave for now
		int iter = 0;
		int intersections = 0;
		int addedpoints = 0;
		int firstIntersectionIter = 0;
		State inside = State.none; 
		final int N = poly1.size();
		final int M = poly2.size();
		//counters
		int n=0;
		int m=0;

		// get iterators
//		Iterator<Vector3> poly1points = poly1.iterator();
//		Iterator<Vector3> poly2points = poly2.iterator();
		
		
		// polygon normal in 3d
//		final Vector3 poly1normal = new Vector3();
		polyNormal(poly1, poly1normal);
//		final Vector3 poly2normal = new Vector3();
		polyNormal(poly2, poly2normal);
		
		
		// get end-vertices
		p.assign(poly1.get(poly1.size()-1));
		q.assign(poly2.get(poly2.size()-1));
		
		// first iteration
		pm.assign(p);
		qm.assign(q);

		// first vertices
		p.assign(poly1.get(n++));
		q.assign(poly2.get(m++));
		
		// deltas
//		pd.assign( p.sub(pm));
		pd.assignDifference(p, pm);
//		qd.assign( q.sub(qm));
		qd.assignDifference( q, qm);

		// run iterations
		while (true) {
			iter = iter+1;
			//System.out.println("iteration " + iter);

			// if intersection is in the interior of the lines
			if (lineLineIntersection(pm,p,qm,q,parameter,epsilon) ) {				
				if (parameter.x >= 0 && parameter.x <= 1 && parameter.y >= 0 && parameter.y <= 1 ) {
//					ipp.assign(pm.add( pd.multiply(parameter.x)));
					ipp.assign(pm); ipp.assignAddProduct(pd, parameter.x);
//					ipq.assign(qm.add( qd.multiply(parameter.y)));
					ipq.assign(qm); ipq.assignAddProduct(qd, parameter.y);

					intersections = intersections+1;
					// check if intersection is the same point
					// as first time an intersection was encountered. This
					// means that the algorithm should terminate
					if (intersections > 1) {
						// the firstIntersectionIter condition makes the algorithm able to handle some
						// degenerate cases, as treated in the O'Rourke paper
					    if (Vector3.xynormOfDifference(firstIntersection, ipp) < epsilon && firstIntersectionIter!=iter-1) {
							// termination 
							//System.out.println("intersection termination");
							return;
						}
					} else {
						// track the first discovered intersection
						firstIntersection.assign(ipp);
						firstIntersectionIter = iter;
					}
					
					// add intersection point
					if (testPoint(firstAddedPoint, previouslyAddedPoint, ipp, addedpoints)) {
						// report intersection
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
			// if (qd X pd)_z >= 0
		    if (qd.x*pd.y-qd.y*pd.x >= 0 ) {
				if (isInHalfplane(p, qm, q)) {
					// advance q
					if (inside == State.Q)
						// check point
						if ( testPoint(firstAddedPoint, previouslyAddedPoint, q, addedpoints)) {
							// report intersection
							result.intersection(projectToPlane(q, poly1normal, p, projectedPoint), q);
							addedpoints = addedpoints+1;
						}
					
					qm.assign(q);

					// rewind iterator
//					if (!poly2points.hasNext())
//						poly2points = poly2.iterator();
					if (!(m<M)) {m=0;}					
					q.assign(poly2.get(m++));
					
					// qd = q - qm
					qd.assignDifference( q, qm);
				} else {
					//advance p
					if (inside == State.P)
						if ( testPoint(firstAddedPoint, previouslyAddedPoint, p, addedpoints)) {
							result.intersection(p, projectToPlane(p, poly2normal, q, projectedPoint));
							addedpoints = addedpoints+1;
						}

					pm.assign(p);
					
					// rewind iterator
//					if (!poly1points.hasNext())
//						poly1points = poly1.iterator();
//
//					p.assign(poly1points.next());
					
					if (!(n<N)) {n=0;}
					p.assign(poly1.get(n++));
					
					// pd = p - pm
					pd.assignDifference(p, pm);
				}
			} else { // qd X pd < 0
				if (isInHalfplane(q, pm, p)) {
					// advance p
					if (inside == State.P)
						if ( testPoint(firstAddedPoint, previouslyAddedPoint, p, addedpoints)) {
							result.intersection(p, projectToPlane(p, poly2normal, q, projectedPoint));
							addedpoints = addedpoints+1;
						}

					pm.assign(p);
					
					// rewind iterator
//					if (!poly1points.hasNext())
//						poly1points = poly1.iterator();
//
//					p.assign(poly1points.next());
					if (!(n<N)) {n=0;}
					p.assign(poly1.get(n++));
					
					pd.assignDifference(p, pm);
				} else {
					// advance q
					if (inside == State.Q)
						if ( testPoint(firstAddedPoint, previouslyAddedPoint, q, addedpoints)) {
							result.intersection(projectToPlane(q, poly1normal, p, projectedPoint), q);
							addedpoints = addedpoints+1;
						}
					
					qm.assign(q);
					
					// rewind iterator
//					if (!poly2points.hasNext())
//						poly2points = poly2.iterator();
//
//					q.assign(poly2points.next());
					
					if (!(m<M)) {m=0;}
					q.assign(poly2.get(m++));
					
					// qd = q - qm
					qd.assignDifference(q, qm);
				}	
			}

			if (iter > 2*(N+M)) 
				break;
		} // while true
 		
		//System.out.println("separation or inclusion termination");
		
		// if we end up here, the polygons is either 
		// separated or contained inside each other
		if ( isContained(p, poly2) && M > 2) {
			// add all points from P as intersection
			for (Vector3 pi: poly1) {
				result.intersection(pi, projectToPlane(pi, poly2normal, q, projectedPoint));
			}
			return;
		} else if ( isContained(q, poly1) && N > 2) {
			// add all points from Q as intersection
			for (Vector3 qi: poly2) {
				result.intersection(projectToPlane(qi, poly1normal, p, projectedPoint), qi);
			}
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
//			if ( first.sub(point).xynorm()>epsilon) {
			if ( Vector3.xynormOfDifference(first, point) > epsilon) {
				prev.assign(point);
				return true;
			} else {
				return false;
			}
		} else {
//			if ( first.sub(point).xynorm()>epsilon && prev.sub(point).xynorm()>epsilon ) {
			if ( Vector3.xynormOfDifference(first, point)>epsilon && Vector3.xynormOfDifference(prev, point)>epsilon ) {
				prev.assign(point);
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Private method that determines if a point intersects a line, within the given envelope size
	 */
	private static final boolean pointLineIntersection( Vector3 x, Vector3 p1, Vector3 p2, Vector3 result, double envelope) {
		// check point line-intersection
		// l(t) = p1 + (p2-p1)t
		// (l(t)-x)T(p2-p1) = 0
		// l(t)T(p2-p1) - xT(p2-p1)  = 0
		// p1T(p2-p1) + (p2-p1)T(p2-p1) t - xT(p2-p1) = 0
		//  t =  xT(p2-p1) - p1T(p2-p1) / (p2-p1)T(p2-p1)
		//  t =  (x-p1)T(p2-p1) / |(p2-p1)|^2
		//		final double e = 0.1;
		//		final Vector3 p2p1 = p2.sub(p1);

		// use result as temporary vector to hold p2-p1
		final Vector3 p2p1 = result; 
		p2p1.assignDifference(p2, p1);

		// calculate parametrisation of closest point
		double t = x.xydot(p2p1)-p1.xydot(p2p1) / p2p1.squaredXYNorm();

		// clamp t in [0,1]
		t = Math.min( Math.max(0-epsilon, t), 1+epsilon);

		// calculate actual intersection point
		result.assignAddProduct(p2p1, t-1);
		result.assignAdd(p1);

		// report intersection
		if ( Vector3.xynormOfDifference(result, x) < envelope ) {
			// intersection within envelope
			return true;
		} else {
			// no intersection
			return false;
		}
	}
	
		
	private final void linePolyIntersection( Vector3 p1, Vector3 p2, List<Vector3> poly, Vector3 polynormal, boolean reverse, ResultHandler result ) {
		// we maintain two candidate points
//		final Vector3 out1 = new Vector3();
//		final Vector3 out2 = new Vector3();	
		boolean keepp1 = true;
		boolean keepp2 = true;
		int hits = 0;
		
		final int N = poly.size();
		int iter = 0;
		
//		final Vector3 qm = new Vector3();
//		final Vector3 q = new Vector3(poly.get(N-1));
		q.assign(poly.get(N-1));
//		final Vector3 polypoint = new Vector3(q);
//		final Vector3 param = new Vector3();

		while (iter<N) {
			qm.assign(q);
			q.assign(poly.get(iter));

			// discard end-points. If we find evidence that an end-point
			// is outside the polygon, it is discarded. End-points that survive 
			// these tests must be contained in the polygon.
			if (keepp1)
				if (!isInHalfplane(p1, qm, q)) 
					keepp1 = false;
			if (keepp2)
				if (!isInHalfplane(p2, qm, q)) 
					keepp2 = false;
			
			// intersect line with poly edge
			if (lineLineIntersection(p1, p2, qm, q, parameter, epsilon)) {
				// internal on lines
				if (-epsilon<=parameter.x && parameter.x<=1+epsilon && -epsilon<=parameter.y && parameter.y<=1+epsilon) {
					// calculate intersection points (including the right z-coordinate)
					final Vector3 ipp = p1.add(p2.sub(p1).multiply(parameter.x));
					final Vector3 ipq = qm.add( q.sub(qm).multiply(parameter.y));
	
					if (reverse) {
						result.intersection(ipq, ipp);
					} else {
						result.intersection(ipp, ipq);
					}
					
					//count intersections
					hits++;
					
					if (hits > 1) {
						// terminate if we found two intersections
						return;
					}
				} else {
					// intersection point is outside the [0,1] interval
				}
			} else {
				// poly edge is parallel with line. There is a special case where the line
				// is coincident with and edge of the poly
			}
			
			// go forward to next edge
			iter++;
		}
		
		if (keepp1) {
			// return first end-point
			if (reverse) {
				result.intersection(projectToPlane(p1, polynormal, q, projectedPoint), p1);				
			} else {
				result.intersection(p1, projectToPlane(p1, polynormal, q, projectedPoint));
			}
		}
		
		if (keepp2) {
			// return second end-point
			if (reverse) {
				result.intersection(projectToPlane(p2, polynormal, q, projectedPoint), p2);				
			} else {			
				result.intersection(p2, projectToPlane(p2, polynormal, q, projectedPoint));
			}
		}
		
	}

	
	/**
	 * assign to projected the point that results from projecting point onto the plane defined by normal and ref,
	 * along the positive z axis
	 */
	private static final Vector3 projectToPlane( Vector3 point, Vector3 normal, Vector3 ref, Vector3 result) {
//		return point.add(0,0,ref.sub(point).dot(normal)/normal.z);
		result.assignDifference(ref,point);		
		result.assign( point.x, point.y, point.z+result.dot(normal)/normal.z);
		return result;
	}
	
	/**
	 * Return true if a is in the positive half-plane define by the two points bs->bt. 
	 */
	public static final boolean isInHalfplane(final Vector3 a, final Vector3 bs, final Vector3 bt) {
		// optimised to avoid allocation
		// return (bt.sub(bs)).cross(a.sub(bs)).z >= 0;		
		return (bt.x-bs.x)*(a.y-bs.y)-(bt.y-bs.y)*(a.x-bs.x)>=0;
	}
	
	/**
	 * Return the signed distance from the line (bs,bt) to the point a, scaled by |bs-bt|
	 */
	public static final double inHalfplane(final Vector3 a, final Vector3 bs, final Vector3 bt) {
		// optimised to avoid allocation
		// return (bt.sub(bs)).cross(a.sub(bs)).z >= 0;		
		return (bt.x-bs.x)*(a.y-bs.y)-(bt.y-bs.y)*(a.x-bs.x);
	}

	/**
	 * Return the counter clock-wise normal of the given polygon
	 * @param poly
	 * @param normal
	 */
	private static final void polyNormal(List<Vector3> poly, Vector3 normal) {
		final Vector3 p3 = poly.get(0);
		final Vector3 p4 = poly.get(1);
		final Vector3 p5 = poly.get(2);
		// counter clock-wise normal
        // N = (p5-p3) X (p3-p4)
		normal.assign(
				(p5.y-p3.y)*(p3.z-p4.z)-(p5.z-p3.z)*(p3.y-p4.y), 
				(p5.z-p3.z)*(p3.x-p4.x)-(p5.x-p3.x)*(p3.z-p4.z),
				(p5.x-p3.x)*(p3.y-p4.y)-(p5.y-p3.y)*(p3.x-p4.x)
		);
		normal.assignNormalize();
	}
	
	/**
	 * Return true if p is contained inside poly. Poly is required to contain at least 3 affine independent points
	 * @param p
	 * @param poly
	 * @return
	 */
	public static final boolean isContained( final Vector3 p, final List<Vector3> poly ) {
		final int N = poly.size();
		Vector3 p0 = poly.get(0);
		Vector3 pm = p0;
		
		// test each edge
		for (int i=0; i<N; i++) {
			final Vector3 pi = poly.get(i);
			if (inHalfplane(p, pm, pi) < -envelope*Vector3.xynormOfDifference(pm, pi) ) {
				return false;
			}
			
			pm = pi;
		}
		
		// test last edge from final vertex to 
		// the first vertex, closing the polygon
		if (inHalfplane(p, pm, p0)< -envelope*Vector3.xynormOfDifference(pm, p0)) {
			return false;
		}
		
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
	public static final boolean lineLineIntersection( final Vector3 ps, final Vector3 pt, 
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
				
		// determinant calculation
		double det =  (pt.x-ps.x)*(qs.y-qt.y)-(pt.y-ps.y)*(qs.x-qt.x);		
		
		// ill-posed problem?
		if (Math.abs(det)<epsilon)
			return false;
		 
		// calculate parametrisation values at intersection
		double alpha = (1/det)* ((qs.y-qt.y)*(qs.x-ps.x) + (-(qs.x-qt.x))*(qs.y-ps.y));
		double beta  = (1/det)* ( (-(pt.y-ps.y))*(qs.x-ps.x) + (pt.x-ps.x)*(qs.y-ps.y) ); 
		
		// set return values
		st.x = alpha;
		st.y = beta;
		
		// all well
		return true;
	}
}
