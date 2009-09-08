package jinngine.physics.force;

import jinngine.math.Vector3;
import jinngine.physics.Body;

public class TorqueForce implements Force {
	private final Body body;
	private final Vector3 omega;
	private double magnitude;
	public TorqueForce(Body body, Vector3 omega, double magnitude) {
		this.body = body;
		this.omega = omega;
		this.magnitude = magnitude;
	}
	
	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}
	
	public void apply() {
		//apply torque to the body
		Vector3.add(body.state.tauCm, body.toWorldNoTranslation(omega).multiply(magnitude));
	}


}
