package jinngine.geometry.contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import jinngine.collision.ExpandingPolytope;
import jinngine.collision.GJK;
import jinngine.geometry.Geometry;
import jinngine.geometry.SupportMap3;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class BulletPersistentManifold implements ContactGenerator {

	final Body b1,b2;
	final SupportMap3 s1,s2;
	final double envelope = 0.125;
	final double shell = envelope*0.5;

	
	final List<ContactPoint> points = new ArrayList<ContactPoint>(); 

	public BulletPersistentManifold( Geometry g1, Geometry g2) {
		b1 = g1.getBody();
		b2 = g2.getBody();
		s1 = (SupportMap3)g1;
		s2 = (SupportMap3)g2;
	}
	
	@Override
	public Iterator<ContactPoint> getContacts() {
		return points.iterator();
	}

	@Override
	public boolean run(double dt) {
		//b1.updateTransformations();
		//b2.updateTransformations();
		ContactPoint mp = new ContactPoint();
		GJK gjk = new GJK();
		Vector3 pa = new Vector3(); 
		Vector3 pb = new Vector3();
		
		gjk.run(s1,s2,pa,pb,1e-10);

		if (pa.minus(pb).norm()>envelope) {
			points.clear();
			return false;
		}
		
		//penetration
		if (gjk.getState().simplexSize>3) {
			ExpandingPolytope epa = new ExpandingPolytope();
			epa.run(s1, s1, pa, pb, gjk.getState());
		}

		mp.midpoint.assign( pa.add(pb).multiply(0.5));
		mp.pa.assign( b1.toModel(pa));
		mp.pb.assign( b2.toModel(pb));		
		mp.normal.assign(pa.minus(pb).normalize());
		mp.restitution = 0.7;
		mp.friction = 0.5;

		double d = pa.minus(pb).norm();
		mp.depth = shell-d;
		
		// invert normal for penetration
		if (gjk.getState().simplexSize > 3) {
			Vector3.multiply( mp.normal, -1 );
			d *= -1;
			mp.depth = shell - d;
		}
		
		//mp.depth = 0;
		mp.depth = mp.depth < 0 ? 0: mp.depth;

		points.add(mp);


		//clean out points
		ListIterator<ContactPoint> ci = points.listIterator();
		while(ci.hasNext()) {
			ContactPoint cp = ci.next();
			//overwrite normal
			cp.normal.assign(mp.normal);
			
			//project the midpoint onto the contact plane
			Vector3 midpoint =  b1.toWorld(cp.pa).add( b2.toWorld(cp.pb)).multiply(.5);
			cp.midpoint.assign(  midpoint.minus( mp.normal.multiply( mp.normal.dot( midpoint.minus(mp.midpoint))))  );

			Vector3 pbpa = b1.toWorld(cp.pa).minus( b2.toWorld(cp.pb));
			//recalculate depth relative to contact normal
			double dist = pbpa.dot(mp.normal);
			//cp.depth = shell - dist;
			System.out.println(""+dist+" norm="+ pbpa.norm());

			cp.depth = shell-dist < 0 ? 0: shell-dist;
			//cp.depth = 0;
			//throw away points outside contact region or normal is too off
			if ( dist > envelope || pbpa.normalize().cross(mp.normal).norm() > 0.01 ) {
				ci.remove();
			}
		}
		
		
		//points.clear();
		
		if (points.size()>4) {
			//we should find a replacer that maximises area
			Vector3 p0 = points.get(0).midpoint;
			Vector3 p1 = points.get(1).midpoint;
			Vector3 p2 = points.get(2).midpoint;
			Vector3 p3 = points.get(3).midpoint;
			Vector3 p4 = points.get(4).midpoint;

			int replace = 0;
			double bestd = 0;
			
//			double dn = areaSizeMetric(p0, p1, p2, p3);
//			if ( dn>bestd ) {
//				replace = 4;
//				bestd = dn;
//			}

			double dn = areaSizeMetric(p0, p1, p2, p4);
			if ( dn>bestd ) {
				replace = 3;
				bestd = dn;
			}

			dn = areaSizeMetric(p0, p1, p4, p3);
			if ( dn>bestd ) {
				replace = 2;
				bestd = dn;
			}

			dn = areaSizeMetric(p0, p4, p2, p3);
			if ( dn>bestd ) {
				replace = 1;
				bestd = dn;
			}

			dn = areaSizeMetric(p4, p1, p2, p3);
			if ( dn>bestd ) {
				replace = 0;
				bestd = dn;
			}


			//replace
			points.set(replace, points.get(4));
				
			//delete
			points.remove(4);			
		}

		return true;
	}
	
	private final double areaSizeMetric( Vector3 p0, Vector3 p1, Vector3 p2, Vector3 p3) {
		double a1 = p1.minus(p0).cross(p2.minus(p0)).squaredNorm();		
		double a2 = p1.minus(p0).cross(p3.minus(p0)).squaredNorm();		
		double a3 = p2.minus(p0).cross(p3.minus(p0)).squaredNorm();		
		double a4 = p2.minus(p1).cross(p3.minus(p1)).squaredNorm();
		return a1+a2+a3+a4;
	}

}
