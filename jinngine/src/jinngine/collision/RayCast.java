/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.collision;

import java.util.List;

import jinngine.geometry.SupportMap3;
import jinngine.math.Vector3;

/**
 * Performs a collision query between a ray in space and a convex shape, defined by a support mapping. This implementation 
 * is rather naive, it can be done more efficiently by integration into the GJK algorithm. However, the implementation at hand 
 * is still usable in practice. 
 * @author mo
 *
 */
public class RayCast {
	final GJK gjk = new GJK();
	GJK.State gjkstate = gjk.getState();
	
	/** 
	 * Perform ray cast against the convex object defined by Sb. 
	 * @param Sb support mapping of a convex shape
	 * @param point point on ray 
	 * @param direction direction of ray
	 * @param envelope they ray is defined to hit the object if it is within this distance
	 * @param epsilon the desired accuracy ( directly passed to gjk )
	 * @return t such that c = direction t + point, where c is the point of collision. If the ray does not intersect the 
	 * convex shape for any positive t, then positive infinity is returned
	 */
	public double run( 
			final SupportMap3 Sb, 
			final Vector3 point, 
			final Vector3 direction, 
			double envelope, 
			double epsilon) 
	{
		double lambda=0;
		int iterations = 0;
		final Vector3 x = point.copy();
		final Vector3 n = new Vector3();
		final Vector3 pb = new Vector3(), pa = new Vector3();
		
//		System.out.println("(*) RayCast");
		
		// dummy support map consisting only of the point x
		final SupportMap3 Sa = new SupportMap3() {
			@Override
			public final Vector3 supportPoint(Vector3 direction) { return x.copy(); }
			@Override
			public final void supportFeature(Vector3 d, double epsilon, List<Vector3> returnList) {}
		};

		// vectors from the GJK internal state (pretty ugly but it works) 
		final Vector3 v = gjkstate.v;
		final Vector3 w = gjkstate.w;
		
		while (true) {
			iterations++;
			// run as many gjk iterations as necessary to get a separating axis. If the distance
			// is within the envelope, run until the error in v is below epsilon
			gjk.run(Sa, Sb, pa, pb, envelope, epsilon, 31);
			//termination
			if (v.norm() < envelope  || iterations > 31 )
				break;
			// ray miss?
			if ( v.normalize().dot(direction) >= 0) {
				return Double.POSITIVE_INFINITY;
			} else {
				// move forward as much as possible 
				// TODO in theory we could hit the boundary and go degenerate. Maybe we could
				// limit the advance so it can never go through the envelope?
				lambda = lambda - v.dot(w) / v.dot(direction);
				x.assign(point.add(direction.multiply(lambda)));
			}			
		}
		//System.out.println("RayCast: Hitpoint lambda=" + lambda);
		//n.print();
		return lambda;
	}
}
