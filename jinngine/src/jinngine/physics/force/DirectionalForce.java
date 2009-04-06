package jinngine.physics.force;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Force;

public class DirectionalForce implements Force {
	private final Body a;
	private final Vector3 direction = new Vector3();
	
	public DirectionalForce(Body a) {
		this.a = a;
	}

	public void setDirection(Vector3 direction) {
		this.direction.assign(direction);
	}
	
	public void apply() {
		a.applyForce(Vector3.zero, direction.multiply(1) );		
	}
}
