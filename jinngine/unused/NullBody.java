package jinngine.unused;

import jinngine.math.Vector3;
import jinngine.physics.Body;

public class NullBody extends Body {

	public NullBody() {
		super();
	}
	
	public Body setrcm(Vector3 r_cm) {
		this.state.rCm.assign(r_cm);
		updateTransformations();
		return this;
	}
	
//	@Override
	public void updateMomentOfInertia() {
		// TODO Auto-generated method stub

	}

//	@Override
	public void setupShape() {
		// TODO Auto-generated method stub

	}

}
