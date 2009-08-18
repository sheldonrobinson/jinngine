package jinngine.physics.force;

import jinngine.math.Vector3;
import jinngine.physics.Body;

public class LinearDragForce implements Force {
	private final Body body;
	private double magnitude;
	private double power;
	
	public LinearDragForce(final Body body, double magnitude) {
		super();
		this.body = body;
		this.magnitude = magnitude;
		this.power =1;
	}

	public LinearDragForce(final Body body, double magnitude, double power) {
		super();
		this.body = body;
		this.magnitude = magnitude;
		this.power = power;
	}


	public void apply() {
		double m = body.getVelocity().norm();
		Vector3 d = body.getVelocity().normalize();

		body.applyForce(Vector3.zero, d.multiply(-magnitude* Math.pow(m, power)  ) );		
	}

}
