package jinngine.geometry.contact;

import java.util.Iterator;
import java.util.List;

import jinngine.collision.ExpandingPolytope;
import jinngine.collision.GJK;
import jinngine.geometry.Sphere;
import jinngine.geometry.SupportMap3;
import jinngine.geometry.contact.ContactGenerator.ContactPoint;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;

/**
 * Contact generator for Sphere-SupportMap combinations. Insted of using a sphere support map,
 * we use just the sphere centre point as supprt map for the sphere. This makes GJK behave much 
 * more regular, because the continuous shape is avoided. 
 */
public final class SupportMapSphereContactGenerator implements ContactGenerator {

    private static double envelope = 0.125*0.5;
	private static double shell = envelope*0.75;
	private final SupportMap3 convex;
	private final SupportMap3 pointmap;
	private final Sphere sphere;
	private final Vector3 spherecentre = new Vector3();
	private final Body b1, b2;
	private final ContactPoint cp = new ContactPoint();
	private final GJK closest = new GJK();
	private boolean incontact = false;
	private boolean invertnormal = false;
	
	public SupportMapSphereContactGenerator(Body b1, SupportMap3 convex, Body b2, Sphere sphere) {
		this.convex = convex;
		this.sphere = sphere;
		this.b1 = b1;
		this.b2 = b2;

		// supportmap for the sphere centre
		this.pointmap = new SupportMap3() {
			@Override
			public final Vector3 supportPoint(Vector3 direction) {
				return spherecentre.copy();
			}
			@Override
			public final void supportFeature(Vector3 d, double epsilon,
					List<Vector3> face) {}
		};
		
		
		cp.restitution = 0.7;
		cp.friction = 0.5;
	}

	/**
	 * Alternative constructor for geoemtries in reversed order
	 * @param b2
	 * @param sphere
	 * @param b1
	 * @param convex
	 */
	public SupportMapSphereContactGenerator( Body b2, Sphere sphere, Body b1, SupportMap3 convex) {
		this.convex = convex;
		this.sphere = sphere;
		this.b1 = b1;
		this.b2 = b2;

		// supportmap for the sphere centre
		this.pointmap = new SupportMap3() {
			@Override
			public final Vector3 supportPoint(Vector3 direction) {
				return spherecentre.copy();
			}
			@Override
			public final void supportFeature(Vector3 d, double epsilon,
					List<Vector3> face) {}
		};
		
		
		cp.restitution = 0.7;
		cp.friction = 0.5;
		
		invertnormal = true;
	}

	
	
	
	@Override
	public final Iterator<ContactPoint> getContacts() {
		return new Iterator<ContactPoint>() {
			boolean done = false;
			@Override
			public boolean hasNext() {
				return (!done)&&incontact;
			}
			@Override
			public ContactPoint next() {
				done = true;
				return cp;
			}
			@Override
			public void remove() {
				// TODO Auto-generated method stub
			}			
		};
	}

	@Override
	public final boolean run(double dt) {
		boolean penetrating = false;
		// assign the centre of mass position of the sphere in world coords
		sphere.getLocalTransform(new Matrix3(), spherecentre);
		Vector3.add(spherecentre, b2.state.position);

		// run gjk
		Vector3 v = new Vector3();
		closest.run(convex, pointmap, cp.paw, cp.pbw, sphere.getRadius()+envelope, 1e-6, 31); //notice the envelope size
				
		// penetration
		if (closest.getState().simplexSize > 3  || cp.paw.minus(cp.pbw).norm() < 1e-10 ) {
			System.out.println("Support-Sphere: penetration");
			// run EPA
			ExpandingPolytope epa = new ExpandingPolytope();
			epa.run(convex, pointmap, cp.paw, cp.pbw, closest.getState());			
			penetrating = true;
		}	
		
		// find direction
		v.assign(cp.paw.minus(cp.pbw));
		double distance = v.norm();
		
		// if the norm is still zero at this point, the sphere has its centre on the 
		// boundary of the convex shape. A fall-back on a regular Sphere support mapping 
		// would be an option now, but it might be easier to simply skip the frame, since
		// this case would be rather rare :) 
		
		if (!penetrating) {
			if ( v.norm() < sphere.getRadius() ) {
				// regular case, move pbw in the direction of the normal
				cp.normal.assign( v.normalize());
				Vector3.add(cp.pbw, cp.normal.multiply( sphere.getRadius() ) ); 

				// world space interaction point and contact distance
				cp.midpoint.assign(cp.paw.add(cp.pbw).multiply(0.5));
				
				distance -= sphere.getRadius();
			} else {
				// pbw will pass paw, which is a penetrating case
				cp.normal.assign( v.normalize());
				Vector3.add(cp.pbw, cp.normal.multiply( sphere.getRadius() ) ); 

				// world space interaction point and contact distance
				cp.midpoint.assign(cp.paw.add(cp.pbw).multiply(0.5));

				// invert the normal
				//Vector3.multiply( cp.normal, -1);
				
				distance -= sphere.getRadius();
			}
		} else {
			// in the penetrating case,  we turn the normal around as usual, 
			// and move pbw further along it by the radius of the sphere
			cp.normal.assign( v.normalize().multiply(-1));
			Vector3.add(cp.pbw, cp.normal.multiply( sphere.getRadius() ) ); 

			// world space interaction point and contact distance
			cp.midpoint.assign(cp.paw.add(cp.pbw).multiply(0.5));
			
			distance =  -v.norm()-sphere.getRadius();					
		}
		
		//invert the normal if geoetries came in reverse order
		if (invertnormal)
			Vector3.multiply( cp.normal, -1);
		
		// contact within envelope
		if ( distance >= 0  && distance < envelope ) {
			cp.depth = shell-distance;
			//cp.depth = depth-(envelope/2.0) > 0 ? depth-(envelope/2.0):0;
			incontact = true;
			
			return true;
		// penetration
		} else if ( distance < 0){
			cp.depth = shell-distance;
			//cp.depth = 0;
			incontact = true;
			return true;
		// separation
		} else {
			cp.depth = 0;
			incontact = false;
			return false;
		}
		
		
		
		
	}

}
