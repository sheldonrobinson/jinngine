package jinngine.unused;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.force.*;

public class MotorForce implements Force {
	private final Body A;
	private final Vector3 force;
	private final Vector3 r;
	private double factor = 0;
	

	public MotorForce(Body a, Vector3 force, Vector3 r) {
		super();
		A = a;
		this.force = force;
		this.r = r;
	}


	public void apply() {
		if (factor!=0)
			 	Vector3.add(A.state.tauCm, A.toWorldNoTranslation(r.cross(force.multiply(factor))));

//			A.setAngularVelocity(A.omega_cm.add(force.multiply(0.01)));
//			A.omega_cm.assign(A.omega_cm.add(force.multiply(0.01))); 
			//A.applyForce( r, force );
	}


	public void setFactor(double factor) {
		this.factor = factor;
	}

}
