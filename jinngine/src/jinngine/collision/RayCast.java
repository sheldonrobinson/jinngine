/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.collision;

import java.util.Iterator;
import jinngine.geometry.SupportMap3;
import jinngine.math.Vector3;

/**
 * Performs a collision query between a ray in space and a convex shape, defined by a support mapping. 
 */
public final class RayCast {
	final GJK gjk = new GJK();
	final GJK.State gjkstate = gjk.getState();
	/** 
	 * Perform ray cast against the convex object defined by Sb. 
	 * @param Sb support mapping of a convex shape
	 * @param Sc optional support mapping. If this support map is given, it is "added" to the ray. The ray cast then 
	 * becomes a convex cast, where the convex shape given by Sc, is casted against Sb, starting at the given point and along
	 * the ray direction
	 * @param point point on ray 
	 * @param direction direction of ray
	 * @param pb upon termination, pb will contain the closest point on Sb
	 * @param pc upon termination, pc will contain the closest point on Sc. If Sc is not given, the pc will contain the end-point 
	 * of the ray
	 * @param lambda starting ray parameter. Defaults to zero
	 * @param envelope they ray is defined to hit the object if it is within this distance
	 * @param epsilon the desired accuracy (directly passed to GJK)
	 * @param sweep when true, the ray-cast will take sphere sweeping into account. 
	 * @return t such that c = direction t + point, where c is the point of collision. If the ray does not intersect the 
	 * convex shape for any positive t, then positive infinity is returned
	 */
	public final double run( 
			final SupportMap3 Sb, 
			final SupportMap3 Sc, 
			final Vector3 point, 
			final Vector3 direction, 
			Vector3 pb, 
			Vector3 pc, 
			double lambda, 
			final double envelope, 
			final double epsilon, 
			final boolean sweep) {
		
		int iterations = 0; 
		final Vector3 x = point.add(direction.multiply(lambda));
		final double sphere;
		
		// sphere swept volumes?
		if (sweep) {
			sphere = /*envelope*/ + Sb.sphereSweepRadius() + (Sc!=null? Sc.sphereSweepRadius():0);
		} else {
			sphere = 0;
		}
		
		
		// translated support mapping Sc+x
		final SupportMap3 Sa;		
		if (Sc == null) {
			Sa = new SupportMap3() {
				@Override
				public final Vector3 supportPoint(Vector3 direction, Vector3 result) { return result.assign(new Vector3(x)); }
				@Override
				public final void supportFeature(Vector3 d, Iterator<Vector3> returnList) {}
				@Override
				public final double sphereSweepRadius() {return 0;}
			};
		} else {
			// if Sc is given, add it to the second support map
			Sa = new SupportMap3() {
				@Override
				public final Vector3 supportPoint(Vector3 direction, Vector3 result) { return result.assign(x.add(Sc.supportPoint(direction, new Vector3()))); }
				@Override
				public final void supportFeature(Vector3 d, Iterator<Vector3> returnList) {}
				@Override
				public final double sphereSweepRadius() {return 0;}
				
			};			
		}
		
		// vectors from the GJK internal state (pretty ugly but it works) 
		final Vector3 v = gjkstate.v;
		final Vector3 w = gjkstate.w;
		
		while (true) {
			iterations++;
//			System.out.println("RayCast: iter=" + iterations +" lambda="+lambda);

			// run as many gjk iterations as necessary to get a separating axis. If the distance
			// is within the envelope, run until the error in v is below epsilon. 
			gjk.run(Sa, Sb, pc, pb, envelope+sphere, epsilon, 31);
			//termination
			if (v.norm() < envelope+sphere  || iterations > 31 )
				break;
			// ray miss?
			if ( v.dot(direction) >= 0) {
				return Double.POSITIVE_INFINITY;
			} else {
				// move forward as much as possible, half way into the envelope 
				Vector3 vs = v.sub(v.normalize().multiply(envelope*0.5+sphere));
				lambda = lambda - vs.dot(w) / v.dot(direction);
				x.assign(point.add(direction.multiply(lambda)));
			}			
		}
//		System.out.println("RayCast: Hitpoint lambda=" + lambda);
		return lambda;
	}
}
