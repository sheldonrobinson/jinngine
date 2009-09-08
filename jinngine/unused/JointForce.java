package jinngine.physics.force;

import jinngine.math.Vector3;
import jinngine.physics.Body;

public class JointForce implements Force {

	private final Force spring;
	private Force spring2;
	
	public JointForce(final Body a, final Body b, Vector3 pa ) {
		super();
//		this.pb = pb;
		
		//connect the point in A to the same place in B
		pa = new Vector3(-2,0,0);
		spring = new SpringForce(a, pa, b, b.toModel( a.toWorld(pa)) , 60, 6);
		Vector3 paa = new Vector3(2,0,0);
		spring2 = new SpringForce(a, paa, b, b.toModel( a.toWorld(paa)), 60, 6 );
		
	}

	public void apply() {
		spring.apply();
		spring2.apply();
	}

}
