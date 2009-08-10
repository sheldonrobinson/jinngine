package jinngine.geometry;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import jinngine.collision.ExpandingPolytope;
import jinngine.collision.GJK3;
import jinngine.math.*;
import jinngine.physics.*;

/**
 * Implementation of a contact generator, handling pairs of convex objects, represented
 * by support mappings. The implementation is straight forward, and uses GJK for separated queries 
 * and EPA for penetrating ones. It maintains a contact manifold of 4 contact points at all times, 
 * meaning that every time a new point is obtained, an old one is discarded, using a simple heuristic, similar
 * to the one found in the Bullet Physics Library
 * @author mo
 *
 */
public class SupportMapContactGenerator2 implements ContactGenerator {

	//final double envelopeMin = 2.75;
    double envelope = 0.6;
    double shell = envelope*0.5;
	private final SupportMap3 Sa;
	private final SupportMap3 Sb;
	private final GJK3 closest = new GJK3();
	public final Body bodyA, bodyB;
	private final List<ContactPoint> manifold = new LinkedList<ContactPoint>();
	private final Vector3 principalNormal = new Vector3();
	private final Vector3 principalPoint = new Vector3();


	public SupportMapContactGenerator2(SupportMap3 sa, Geometry a, SupportMap3 sb, Geometry b) {
		super();
		Sa = sa;
		Sb = sb;
		bodyA = a.getBody();
		bodyB = b.getBody();
	}

	@Override
	public Iterator<ContactPoint> getContacts() {
		return manifold.iterator();
	}

	@Override
	public boolean run(double dt) {
		//get envelopes
		//envelope = Sa.getEnvelope(dt)> Sb.getEnvelope(dt)? Sa.getEnvelope(dt) : Sb.getEnvelope(dt);
		//shell = envelope*0.25;
		
		
		//run the closest points algorithm
		Vector3 a = new Vector3(); Vector3 b = new Vector3();
		closest.run(Sa, Sb, a, b, envelope); 
		Vector3 v = a.minus(b);
		principalNormal.assign(v.normalize());
		double  d = v.norm();

		//distance is within envelope
		if ( d > 1e-6  && d < envelope ) {
			//principalPenetration = false;
			
			double depth = 0;;
			//depth = depth-shell > 0 ? depth-shell:0;
			depth = d-shell;
			depth = depth > 0 ? 0:depth;
			//depth=0;
			add(a,b,v.normalize(), depth);

			
			//System.out.println("depth="+depth);
			return true;

//		} else { //distance is non-positive or far away
//			
//			//check for penetration
//			GJK3.State state = closest.getState();
//			if ( state.simplexSize > 3  && false) {
//				principalPenetration = true;
//				new ExpandingPolytope().run(Sa, Sb, a, b, state);				
//				//reverse normal direction for penetrating points
//				 v.assign(a.minus(b).multiply(-1));
//				principalNormal.assign(v.normalize());
//				d = v.norm();
//				double depth = d + envelope*0.5;
//				
			//				add(a,b,principalNormal, depth);
			//				return true;


		
		} else {
			//no contact
			manifold.clear();
			return false;	
		}		
	} 
	

	private final void clean() {
		//System.out.println("Cleaning");
		//remove points with exceeding depths
		ListIterator<ContactPoint> i = manifold.listIterator();
		while( i.hasNext()){
			ContactPoint cp = i.next();

			Vector3 wpa = bodyA.toWorld(cp.pa);
			Vector3 wpb = bodyB.toWorld(cp.pb);
			Vector3 wab = wpa.minus(wpb);
			Vector3 wmp = wpa.add(wpb).multiply(0.5);
			Vector3 wn = wpa.minus(wpb).normalize();
			double  wd = wpa.minus(wpb).norm();

			//penetrating or non-penetrating
//			boolean penetrating = wab.normalize().dot(principalNormal) < 0; 

			//different type of depth and interaction point for
			//penetrating and non-penetrating points
//			if ( !penetrating ) {
				double depth = envelope-wd;
				//System.out.println(""+depth);
				cp.depth = depth-shell > 0 ? depth-shell:0; 

				//cp.depth = 0;
				//if (wd > envelope) depth = 0;
				
				//update midpoint
				cp.midpoint.assign(wmp);
//			} else {
//				//in the penetrating state, we use another way of measuring depth
//				double depth = envelope*0.5+wd;
//				cp.depth = depth;
//				
//				//update midpoint
//				cp.midpoint.assign(wmp);				
//			}
			
			//all points use the same normal
			cp.normal.assign(principalNormal);
			
			
			//when a point's normal is tilting away from the true normal,
			//remove the point regardless of a penetrating or non-penetrating point 
			if ( wn.dot(principalNormal) < 0.91) {
				i.remove();
				continue;
			} 
			
			if ( Math.abs(wmp.minus(principalPoint).dot(principalNormal)) > envelope ) {
				i.remove();
				continue;
			}
			
			//if outside envelope, remove
			if ( wd > envelope ) {
				i.remove();
			}	
			
		}
	}

	private final void add(Vector3 pa, Vector3 pb, Vector3 n, double depth ) {
		clean();
		ContactPoint cp = new ContactPoint();
		cp.depth = depth;

		//compute midpoint or select one point
		cp.midpoint.assign(pa.add(pb).multiply(0.5));
		
		cp.pa.assign(bodyA.toModel(pa));
		cp.pb.assign(bodyB.toModel(pb));
		cp.normal.assign(principalNormal);
		principalPoint.assign(cp.midpoint);

		
		double tol = 1e-1;
		switch ( manifold.size()) {
		case 0:
			//add aways
			manifold.add( cp );
			break;
		case 1:
			//			add if point it's far away
			if (manifold.get(0).midpoint.minus(cp.midpoint).infnorm() < tol ) {
				break;
			} else {
				manifold.add( cp );
			}
			break;
		case 2:
			//if point is on the same line, expand area
			Matrix3 M = new Matrix3(manifold.get(0).midpoint, manifold.get(1).midpoint, cp.midpoint);
			double d = Matrix3.determinant(M);

			if ( Math.abs(d) < tol) {
				double l0 = manifold.get(0).midpoint.minus(manifold.get(1).midpoint).norm();
				double l1 = manifold.get(0).midpoint.minus( cp.midpoint).norm();
				double l2 = manifold.get(1).midpoint.minus( cp.midpoint).norm();
				
				if (l1 > l2) {
					if ( l1 > l0) {
						manifold.remove(1);
						manifold.add(1, cp);
						break;
					} else {
						break;
					}
				} else {
					if ( l2 > l0 ) {
						manifold.remove(0);
						manifold.add(0, cp);
						break;
					} else {
						break;
					}
				}
			}					
				
			//add
			manifold.add( cp );
			break;
		case 3:
			// this case is assumed to rarely require point reduction,
			// so we just add a new point each time
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
			ContactPoint old = manifold.get(index);
			manifold.remove(index);
			//cp.cachedNormalForce = old.cachedNormalForce;
			manifold.add( index, cp );			
			break;
		}
		
//		//update depths
//		for (ContactPoint c: manifold){
//			c.depth = 	bodyA.toWorld(c.pa).minus(bodyB.toWorld(c.pb)).norm();
//		}
		
	}
}
