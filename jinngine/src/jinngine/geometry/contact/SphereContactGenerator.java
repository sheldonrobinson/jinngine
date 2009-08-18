package jinngine.geometry.contact;
import java.util.Iterator;
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
	
	public SphereContactGenerator(Sphere s1, Sphere s2) {
		this.s1 = s1; 
		this.s2 = s2;
		this.b1 = s1.getBody(); 
		this.b2 = s2.getBody();
		//System.out.println("created");
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
