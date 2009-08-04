package jinngine.physics.force;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Force;

public class AngularDragForce implements Force {

	private final Body body;
	private double magnitude;
	private double power;
	
	public AngularDragForce(final Body body, double magnitude) {
		super();
		this.body = body;
		this.magnitude = magnitude;
		this.power =1;
	}

	public AngularDragForce(final Body body, double magnitude, double power) {
		super();
		this.body = body;
		this.magnitude = magnitude;
		this.power =power;
	}

	
	@Override
	public void apply() {
		double m = body.getAngularVelocity().norm();
		Vector3 d = body.getAngularVelocity().normalize();
		Vector3.add(body.state.tauCm, d.multiply(-magnitude* Math.pow(m, power)*body.state.M));
	}

}
