package jinngine.physics.force;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Force;

public class SpringForce implements Force {
	private final Body a;
	private final Body b;
	private final Vector3 pa, pb;
	private double equilibrilium;
	private final double force;
	private final double damper;
	
	public SpringForce(Body a, Vector3 pa,  Body b, Vector3 pb) {		
		this.a = a;
		this.b = b;
		this.pa = pa.copy();
		this.pb = pb.copy();
		this.force = 10;
		this.damper = 1;

		//calculate the equilibrium length
		Vector3 paw = a.toWorld(pa);		
		Vector3 pbw = b.toWorld(pb);		
		Vector3 x = paw.minus(pbw);		
		this.equilibrilium = x.norm();		
	}
	
	public SpringForce(Body a, Vector3 pa, Body b, Vector3 pb, double force, double damper ) {
		this.a = a;
		this.b = b;
		this.pa = pa.copy();
		this.pb = pb.copy();
		this.force = force;
		this.damper = damper;

		//calculate the equilibrium length
		Vector3 paw = a.toWorld(pa);		
		Vector3 pbw = b.toWorld(pb);		
		Vector3 x = paw.minus(pbw);		
		this.equilibrilium = x.norm();		
		
	}
	
	public SpringForce(Body a, Vector3 pa,  Body b, Vector3 pb, double equilibrilium ) {		
		this.a = a;
		this.b = b;
		this.pa = pa;
		this.pb = pb;
		this.equilibrilium = equilibrilium;
		this.force = 10;
		this.damper = 1;
	}
	
	public void setPointOnB(Vector3 pb) {
		this.pb.assign(pb);
//		//calculate the equilibrium length
//		Vector3 paw = a.toWorld(this.pa);		
//		Vector3 pbw = b.toWorld(this.pb);		
//		Vector3 x = paw.minus(pbw);		
//		this.equilibrilium = x.norm();		
	}
	
	public void apply() {
		//point on a 
		Vector3 pra = a.toWorldNoTranslation(pa);
		
		//point on b
		Vector3 prb = b.toWorldNoTranslation(pb);
		
		Vector3 paw = pra.add(a.state.rCm);
		Vector3 pbw = prb.add(b.state.rCm);
		
		//Vector3 p2 = toWorldNoTranslation(new Vector3(0,-1,0));
		Vector3 x = pbw.minus(paw);

		Vector3 upa = a.state.vCm.add(a.state.omegaCm.cross(pra));
		Vector3 upb = b.state.vCm.add(b.state.omegaCm.cross(prb));
		
		
		Vector3 n;
		//normal of spring direction
		if ( x.abs().lessThan(Vector3.epsilon)) {
			n = x = Vector3.zero;
		} else {
			n = x.normalize();			
		}
		
		//clamp string length to avoid divergence
		if ( x.norm() > 1) {
			x.assign(x.normalize().multiply(1));
		}
		
		//relative velocities along spring
		double upax = (n.dot(upa));
		double upbx = (n.dot(upb));
		
		//Forces
		Vector3 Fspring = x.minus(n.multiply(equilibrilium)).multiply(this.force); //spring force
		Vector3 Fdamper = n.multiply( (-upax+upbx) * this.damper  ); //damping force

//		Vector3 Fspring = x.minus(n.multiply(equilibrilium)).multiply(0.5); //spring force
//		Vector3 Fdamper = n.multiply( (-upax+upbx) * 0   ); //damping force

		Vector3 Ftotal = Fspring.add( Fdamper );
		
		//System.out.println("damper relative velocity " + -(upax-upbx) ); 
		
		a.applyForce( pra, Ftotal.multiply(1) );
		b.applyForce( prb, Ftotal.multiply(-1));
		//a_cm = a_cm.add((new Vector3(0,-53,0)).multiply(1/this.mass));
	}

}
