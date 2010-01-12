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
	/** 
	 * Perform ray cast against the convex object defined by Sb. 
	 * @param Sb support mapping of a convex shape
	 * @param point point on ray 
	 * @param direction direction of ray
	 * @return t such that c = direction t + point, where c is the point of collision. If the ray does not intersect the 
	 * convex shape for any positive t, then positive infinity is returned
	 */
	public double run( 
			final SupportMap3 Sb, 
			final Vector3 point, 
			final Vector3 direction) 
	{
		final GJK gjk = new GJK();
		final double epsilon = 1e-7;		
		double lambda=0;
		final Vector3 x = point.copy();
		Vector3 n = new Vector3();
		Vector3 pb = new Vector3(), pa = new Vector3();
		
		//System.out.println("(*) RayCast");
		
		SupportMap3 Sa = new SupportMap3() {
			@Override
			public Vector3 supportPoint(Vector3 direction) {
				return x.copy();
			}

			@Override
			public void supportFeature(Vector3 d, double epsilon, List<Vector3> returnList) {
				// TODO Auto-generated method stub
				return;
			}
		};

		gjk.run(Sa,Sb,pa,pb,Double.POSITIVE_INFINITY);
		Vector3 c = new Vector3();
		c.assign(pb);
		pb.print();
		
		while ( x.minus(c).norm() > epsilon ) {
			//System.out.println("iteration");			
			
			n.assign(x.minus(c));
			if ( n.normalize().dot(direction) >= 0) {
				//System.out.println("RayCast: miss, lambda="+lambda);
				return Double.POSITIVE_INFINITY;
			} else {
				lambda = lambda - n.dot(n) / n.dot(direction);
				x.assign(point.add(direction.multiply(lambda)));
				//System.out.println("lambda="+lambda);
				
				gjk.run(Sa,Sb,pa,pb,Double.POSITIVE_INFINITY);
				c.assign(pb);
			}			
		}
		
		//System.out.println("RayCast: Hitpoint lambda=" + lambda);
		//n.print();
		return lambda;
	}
}
