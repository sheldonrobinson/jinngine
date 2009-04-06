package jinngine.physics.force;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Force;

public class AllignedTorqueForce implements Force {
	private final Body body;
	private final Body target;
	private final Vector3 omega;
	private double magnitude;
	public AllignedTorqueForce(Body body, Body target, Vector3 omega, double magnitude) {
		this.body = body;
		this.target = target;
		this.omega = omega;
		this.magnitude = magnitude;
	}
	
	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}
	
	public void apply() {
		//apply torque to the body
		Vector3.add(target.state.tauCm, body.toWorldNoTranslation(omega).multiply(magnitude));
	}


}
