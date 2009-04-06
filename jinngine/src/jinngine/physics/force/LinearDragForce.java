package jinngine.physics.force;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Force;

public class LinearDragForce implements Force {
	private final Body body;
	private double magnitude;
	
	public LinearDragForce(final Body body, double magnitude) {
		super();
		this.body = body;
		this.magnitude = magnitude;
	}


	public void apply() {
		double m = body.getVelocity().norm();
		Vector3 d = body.getVelocity().normalize();

		body.applyForce(Vector3.zero, d.multiply(-magnitude* m) );		
	}

}
