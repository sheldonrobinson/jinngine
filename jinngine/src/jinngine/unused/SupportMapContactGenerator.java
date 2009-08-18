package jinngine.unused;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import jinngine.collision.ExpandingPolytope;
import jinngine.collision.GJK3;
import jinngine.math.*;
import jinngine.physics.*;
import jinngine.geometry.*;
import jinngine.geometry.contact.*;


/**
 * Implementation of a contact generator, handling pairs of convex objects, represented
 * by support mappings. The implementation is straight forward, and uses GJK for separated queries 
 * and EPA for penetrating ones. It maintains a contact manifold of 4 contact points at all times, 
 * meaning that every time a new point is obtained, an old one is discarded, using a simple heuristic, similar
 * to the one found in the Bullet Physics Library
 * @author mo
 *
 */
public class SupportMapContactGenerator implements ContactGenerator {

	private final double envelope;
	private final SupportMap3 Sa;
	private final SupportMap3 Sb;
	private final GJK3 closest = new GJK3();
	private final Vector3 normal = new Vector3();
	public final Body bodyA, bodyB;
	private final List<ContactPoint> manifold = new LinkedList<ContactPoint>();

	public SupportMapContactGenerator(SupportMap3 sa, Geometry a, SupportMap3 sb, Geometry b) {
		super();
		Sa = sa;
		Sb = sb;
		bodyA = a.getBody();
		bodyB = b.getBody();
		
		// pick the smallest envelope of each geometry
		envelope = Math.min(a.getEnvelope(0), b.getEnvelope(0));
	}

	@Override
	public Iterator<ContactPoint> getContacts() {
		return manifold.iterator();
	}

	@Override
	public boolean run(double dt) {

		//run the closest points algorithm (gjk)
		Vector3 a = new Vector3(); Vector3 b = new Vector3();
		closest.run(Sa, Sb, a, b, envelope);
		
		// compute normal and penetration depth (shallow inside envelope) 
		Vector3 v = a.minus(b);
		normal.assign(v);
		double  d = v.norm();

		// distance is within envelope
		if ( d > 1e-7  && d < envelope ) {
			double depth = envelope-d;
			depth = depth-(envelope/2.0) > 0 ? depth-(envelope/2.0):0; 
			
			// insert the new point into the contact region heuristic 
			add(a,b,v.normalize(), depth);

			return true;

		} else { //distance is non-positive or far away
			
			//check for penetration
			GJK3.State state = closest.getState();
			if ( state.simplexSize > 3 ) {
				// if penetration are present, invoke the EPA algorithm, to 
				// determine the penetration depth. This vector is then used 
				// as a contact normal
				new ExpandingPolytope().run(Sa, Sb, a, b, state);

				// compute normal
				v.assign(a.minus(b).multiply(-1));
				normal.assign(v);
				
				// ... and the penetration depth
				d = v.norm();
				double depth = d + envelope;

				// insert the new point into the contact region heuristic 
				add(a,b,v.normalize(), depth);
				
				return true;
			} else {
				// if no contact condition is detected, we clear the contact
				// region completely
				manifold.clear();
				
			 return false;	
			}
			
			
		}
	}

	private final void clean(Vector3 normal) {
		//System.out.println("Cleaning");
		//remove points with exceeding depths
		ListIterator<ContactPoint> i = manifold.listIterator();
		while( i.hasNext()){
			ContactPoint cp = i.next();

			Vector3 wpa = bodyA.toWorld(cp.pa);
			Vector3 wpb = bodyB.toWorld(cp.pb);
			Vector3 wmp = wpa.add(wpb).multiply(0.5);
			Vector3 wn = wpa.minus(wpb).normalize();
			double wd = wpa.minus(wpb).norm();

			double depth = envelope-wd;
			cp.depth = depth-(envelope/2.0) > 0 ? depth-(envelope/2.0):0; 

			//update midpoint
			cp.midpoint.assign(wmp);

			//if outside envelope or normal is divergent
			if ( Math.abs(wpa.minus(wpb).norm()) > 3 || wn.dot(normal)<0.98 ) {
				//System.out.println("removed!");
				i.remove();
			}		
		}
	}

	/**
	 * This is a contact region heuristic method. It maintains a contact region of 4 points at all times,
	 * and is updated each time a new contact point is added. The method is originally implemented in 
	 * the Bullet Physics Library, the one here is a bit more simple. -silcowitz
	 * @param pa
	 * @param pb
	 * @param n
	 * @param depth
	 */
	private final void add(Vector3 pa, Vector3 pb, Vector3 n, double depth  ) {
		clean(n);
		
		//TODO this method can be drasticly improved, e.g by limiting the region to 
		// less than 4 points whenever possible

		
		ContactPoint cp = new ContactPoint();
		cp.depth = depth;
		cp.midpoint.assign(pa.add(pb).multiply(0.5));
		cp.pa.assign(bodyA.toModel(pa));
		cp.pb.assign(bodyB.toModel(pb));
		cp.normal.assign(n);


		switch ( manifold.size()) {
		case 0:
			//add aways
			manifold.add( cp );
			break;
		case 1:
			//add if point it's far away
			//		if (manifold.get(0).midpoint.minus(midpoint).infnorm() < 1e-2 ) {
			//			break;
			//		} else {
			manifold.add( cp );
			//		}
			break;
		case 2:
			//if point is on the same line, expand area
			//
			//		double l0 = manifold.get(0).midpoint.minus(manifold.get(1).midpoint).norm();
			//		double l1 = manifold.get(0).midpoint.minus( midpoint).norm();
			//		double l2 = manifold.get(1).midpoint.minus( midpoint).norm();
			//
			//			
			//			if (l1 > l0) {
			//				
			//			}
			//
			//			
			manifold.add( cp );
			//}

			break;
		case 3:
			//only add if area gets bigger
			//		double area = triangleArea(manifold.get(0).midpoint, manifold.get(1).midpoint, manifold.get(2).midpoint);
			//		double newArea = triangleArea(manifold.get(0).midpoint, manifold.get(1).midpoint, midpoint) +
			//		triangleArea(manifold.get(1).midpoint, manifold.get(2).midpoint, midpoint);
			//		
			//		if ( newArea > area ) manifold.add( new ManifoldPoint(pa,pb,n) );
			//add always
			manifold.add( cp );
			break;
		case 4:
			//find replacer
			double area = 0;
			int index = 0;

			//0
			Vector3 a = cp.midpoint.minus( manifold.get(1).midpoint );
			Vector3 b = manifold.get(3).midpoint.minus(manifold.get(2).midpoint );
			double newArea = a.cross(b).norm();
			if ( newArea > area) {area= newArea; index = 0; }

			//1
			a = cp.midpoint.minus( manifold.get(0).midpoint );
			b = manifold.get(3).midpoint.minus(manifold.get(2).midpoint );
			newArea = a.cross(b).norm();
			if ( newArea > area) {area= newArea; index = 1; }

			//2
			a = cp.midpoint.minus( manifold.get(0).midpoint );
			b = manifold.get(3).midpoint.minus(manifold.get(1).midpoint );
			newArea = a.cross(b).norm();
			if ( newArea > area) {area= newArea; index = 2; }

			//3
			a = cp.midpoint.minus( manifold.get(0).midpoint );
			b = manifold.get(2).midpoint.minus(manifold.get(1).midpoint );
			newArea = a.cross(b).norm();
			if ( newArea > area) {area= newArea; index = 3; }

			//remove and add
			manifold.remove(index);
			manifold.add( cp );			
			break;
		}		
	}
}
