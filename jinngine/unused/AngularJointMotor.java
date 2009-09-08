package jinngine.physics.constraint;
import java.util.ListIterator;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.*;

public class AngularJointMotor implements Constraint {



	private final Body b1,b2;
	private final Vector3 axe1;
	private double lambda = 0;
	
	public AngularJointMotor(Body b1, Body b2, Vector3 n) {
		this.b1 = b1; this.b2 = b2;
		axe1 = b1.toModelNoTranslation(n);
	}
	
	public void setLambda(double lambda) {
		this.lambda = lambda;
	}


	public void applyConstraints(ListIterator<ConstraintEntry> iterator, double dt) {
		// TODO Auto-generated method stub
		Vector3 axe = b1.toWorldNoTranslation(axe1);
		
		iterator.next().assign( 
				null, b1, 
				b2, new Vector3(), b1.state.Iinverse.multiply(axe.multiply(-1)), new Vector3(),
				b2.state.Iinverse.multiply(axe.multiply(1)), new Vector3(), axe.multiply(-1), new Vector3(),
				axe.multiply(1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, lambda-axe.dot(b1.state.omegaCm)+axe.dot(b2.state.omegaCm) );		


	}

	
}
