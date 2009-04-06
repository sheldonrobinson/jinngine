package jinngine.physics.force;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Force;

public class GravityForce implements Force {
	private final Body a;
	private final double factor;
	public GravityForce(Body a, double factor) {
		this.a = a;
		this.factor = factor;
	}
	
	public void apply() {
		a.applyForce(Vector3.zero, new Vector3(0,-9.8*factor*a.state.M ,0) );		
	}

}
