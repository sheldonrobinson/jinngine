package jinngine.physics.constraint;
import java.util.Iterator;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Constraint;
import jinngine.physics.ConstraintEntry;

public class PlaneJoint implements Constraint {

	private final Body b1,b2;
	private final Vector3 p1;//, p2;
	private final Vector3 n;
	
	public PlaneJoint(Body b1, Body b2, Vector3 p, Vector3 n) {
		this.b1 = b1;
		this.b2 = b2;
		//anchor points on bodies
		p1 = b1.toModel(p);
		this.n = b1.toModelNoTranslation(n);
		//p2 = b2.toModel(p);	
		
//		p1.print();
//		p2.print();
	}
	
	public void applyConstraints( Iterator<ConstraintEntry> iterator, double dt ) {
		// Ball-In-Socket joint has a 3x12 jacobian matrix, since
		// it has 3 DOFs, thus removing 3, inducing 3 new constraints

		//b1.toWorld(p1).minus(b2.toWorld(p2)).print();
		
		
		Vector3 ri = b1.state.q.rotate(p1);
		Vector3 rj = new Vector3(); //b2.state.q.rotate(p2);
		Vector3 n = b1.toWorldNoTranslation(this.n);
		double error = ri.add(b1.state.rCm).minus(b2.state.rCm).dot(n);
		
		Vector3 J1 = n;
		Vector3 J2 = ri.cross(n);
		Vector3 J3 = n.multiply(-1);
		Vector3 J4 = rj.cross(n).multiply(-1);
		
		//compute B vector
		Matrix3 I1 = b1.state.Iinverse;
		double m1 = b1.state.M;
		Matrix3 I2 = b2.state.Iinverse;
		double m2 = b2.state.M;
				
//		B = new Vector(n.multiply(-1/m1))
//		.concatenateHorizontal( new Vector(I1.multiply(r1.cross(n).multiply(-1))) )
//		.concatenateHorizontal(new Vector(n.multiply(1/m2)))
//		.concatenateHorizontal(new Vector(I2.multiply(r2.cross(n).multiply(1))));
		
		Vector3 B1 = J1.multiply(1/m1);
		Vector3 B2 = I1.multiply(J2);
		Vector3 B3 = J3.multiply(1/m2);
		Vector3 B4 = I2.multiply(J4);
		
		double u = b1.state.vCm.minus( ri.cross(b1.state.omegaCm)).minus(b2.state.vCm).dot(n);
		
		if (error>0) {
			//constraint in normal direction
			iterator.next().assign(null,b1,b2,B1,B2,B3,B4,J1,J2,J3,J4,0,Double.POSITIVE_INFINITY, null, (u+0.7*u) + 0.8*error);
		}
	}
	


}
