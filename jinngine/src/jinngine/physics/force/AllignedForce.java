package jinngine.physics.force;

import jinngine.math.Vector3;
import jinngine.physics.Body;

public class AllignedForce implements Force {
	private final Body body;
	private final Vector3 n;
	private final Vector3 p;
	private double magnitude;
	public AllignedForce(Body body, Vector3 p, Vector3 n, double magnitude) {
		this.body = body;
		this.n = n.copy();
		this.p = p.copy();
		this.magnitude = magnitude;
	}
	
	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}
	
	public void apply() {
		body.applyForce(body.toWorldNoTranslation(p), body.toWorldNoTranslation(n).multiply(magnitude) );		
	}


}
