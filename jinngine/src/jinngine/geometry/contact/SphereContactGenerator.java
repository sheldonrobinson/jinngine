package jinngine.geometry.contact;
import java.util.Iterator;

import jinngine.geometry.Material;
import jinngine.geometry.Sphere;
import jinngine.math.*;
import jinngine.physics.Body;

//import jinngine.physics.Body;

/**
 * Simple, fast, sphere-sphere contact generator.
 * @author mo
 *
 */
public class SphereContactGenerator implements ContactGenerator {
	private final ContactPoint cp = new ContactPoint();
	private final Sphere s1;
	private final Sphere s2;
	private final Body b1;
	private final Body b2;
	private boolean incontact = false;
	private final double envelope = 3.0;
	private final double restitution;
	private final double friction;

	
	public SphereContactGenerator(Sphere a, Sphere b) {
		this.s1 = a; 
		this.s2 = b;
		this.b1 = a.getBody(); 
		this.b2 = b.getBody();
		//System.out.println("created");
		
		//select the smallest restitution and friction coefficients 
		if ( a instanceof Material && b instanceof Material) {
			double ea = ((Material)a).getRestitution();
			double fa = ((Material)a).getFrictionCoefficient();
			double eb = ((Material)b).getRestitution();
			double fb = ((Material)b).getFrictionCoefficient();

			//pick smallest values
			restitution = ea > eb ? eb : ea;
			friction    = fa > fb ? fb : fa;

		} else if ( a instanceof Material ) {
			restitution = ((Material)a).getRestitution();
			friction    = ((Material)a).getFrictionCoefficient();
		} else if ( b instanceof Material ) {
			restitution = ((Material)b).getRestitution();
			friction    = ((Material)b).getFrictionCoefficient();
		} else { //default values
			restitution = 0.7;
			friction = 0.5;
		}
		
		//copy material properties to the contactpoint
		cp.restitution = restitution;
		cp.friction = friction;
	}
	
	@Override
	public Iterator<ContactPoint> getContacts() {
		//System.out.println("lksdf");
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
	public boolean run(double dt) {
		//System.out.println(""+s1.getRadius()+","+s2.getRadius());
		Vector3 normal = b1.state.rCm.minus(b2.state.rCm).normalize();
		cp.pa.assign( normal.multiply(-s1.getRadius()).add(b1.state.rCm));
		cp.pb.assign(normal.multiply(s2.getRadius()).add(b2.state.rCm));		
		cp.midpoint.assign(cp.pa.add(cp.pb).multiply(0.5));
		cp.normal.assign(normal.multiply(1));
		
		//cp.normal.assign(Vector3.j.multiply(-1));
		//double d = cp.pa.minus(cp.pb).norm();
		//normal.print();
		//signed distance between spheres
		double d = b1.state.rCm.minus(b2.state.rCm).norm() - (s1.getRadius()+s2.getRadius());  

		//contact within envelope
		if ( d >= 0  && d < envelope ) {
			double depth = envelope-d;
			//cp.depth = depth-(envelope/2.0) > 0 ? depth-(envelope/2.0):0;
			incontact = true;
			return true;
		//penetration
		} else if ( d < 0){
			cp.depth = -d+(envelope/2.0);
			//cp.depth = 0;
			incontact = true;
			return true;
		//Separation
		} else {
			cp.depth = 0;
			incontact = false;
			return false;
		}
	}

}
