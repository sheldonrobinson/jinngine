package jinngine.physics.force;
import jinngine.math.Vector3;
import jinngine.physics.Body;


/**
 * A gravity force is a force acting upon the center of mass of a body, and is proportional to the total mass 
 * of the body. The force is pointing in the negative y direction. 
 * @author mo
 */
public class GravityForce implements Force {
	private final Body a;
	private final double factor;
	private final Vector3 d = new Vector3();

	public GravityForce(Body a) {
		this.a = a;
		this.factor = 1;
		this.d.assign(new Vector3(0,-1,0));
	}

	public GravityForce(Body a, double factor) {
		this.a = a;
		this.factor = factor;
		this.d.assign(new Vector3(0,-1,0));
	}

	public GravityForce(Body a, Vector3 d, double factor)  {
		this.a = a;
		this.factor = factor;
		this.d.assign(d);
	}

	
	public void apply() {
		a.applyForce(Vector3.zero, d.multiply(9.8*factor*a.state.M) );		
	}

}
