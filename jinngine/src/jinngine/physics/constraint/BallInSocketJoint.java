package jinngine.physics.constraint;

import java.util.*;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.*;

public class BallInSocketJoint implements Constraint {	


	private final Body b1,b2;
	private final Vector3 p1, p2;// n1, n2;
	
	public BallInSocketJoint(Body b1, Body b2, Vector3 p, Vector3 n) {
		this.b1 = b1;
		this.b2 = b2;
		//anchor points on bodies
		p1 = b1.toModel(p);
		p2 = b2.toModel(p);	
		
		//Vector3 n = new Vector3(1,0,0);
		//n1 = b1.toModelNoTranslation(n);
		//n2 = b2.toModelNoTranslation(n);
		
		p1.print();
		p2.print();
	}
	
	public void applyConstraints( Iterator<ConstraintEntry> iterator, double dt ) {
		// Ball-In-Socket joint has a 3x12 jacobian matrix, since
		// it has 3 DOFs, thus removing 3, inducing 3 new constraints

		//b1.toWorld(p1).minus(b2.toWorld(p2)).print();
		
//		Vector3 ri = b1.state.q.rotate(p1);
//		Vector3 rj = b2.state.q.rotate(p2);
		Vector3 ri = Matrix3.multiply(b1.state.rotation, p1, new Vector3());
		Vector3 rj = Matrix3.multiply(b2.state.rotation, p2, new Vector3());
		
		//jacobians on matrix form
		Matrix3 Ji = Matrix3.identity().multiply(1);
		Matrix3 Jangi =ri.crossProductMatrix3().multiply(-1);
		Matrix3 Jj = Matrix3.identity().multiply(-1);
		Matrix3 Jangj = rj.crossProductMatrix3().multiply(1);

		Matrix3 MiInv = Matrix3.identity().multiply(1/b1.state.M);
		Matrix3 MjInv = Matrix3.identity().multiply(1/b2.state.M);

		Matrix3 Bi = MiInv.multiply(Ji.transpose());
		Matrix3 Bj = MjInv.multiply(Jj.transpose());
		Matrix3 Bangi = b1.state.Iinverse.multiply(Jangi.transpose());
		Matrix3 Bangj = b2.state.Iinverse.multiply(Jangj.transpose());

		Vector3 u = b1.state.vCm.minus( ri.cross(b1.state.omegaCm)).minus(b2.state.vCm).add(rj.cross(b2.state.omegaCm));
		double Kcor = 0.9;
//		Vector3 posError = b1.state.rCm.add(b1.state.q.rotate(p1)).minus(b2.state.rCm).minus(b2.state.q.rotate(p2)).multiply(Kcor);
		Vector3 posError = b1.state.rCm.add(ri).minus(b2.state.rCm).minus(rj).multiply(Kcor);
		

		
		u.assign( u.add(posError));
		
		//go through matrices and create rows in the final A matrix to be solved
		iterator.next().assign( 
				null, b1, 
				b2, Bi.column(0), Bangi.column(0), Bj.column(0),
				Bangj.column(0), Ji.row(0), Jangi.row(0), Jj.row(0),
				Jangj.row(0),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, u.a1 );
		iterator.next().assign( 
				null, b1, 
				b2, Bi.column(1), Bangi.column(1), Bj.column(1),
				Bangj.column(1), Ji.row(1), Jangi.row(1), Jj.row(1),
				Jangj.row(1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, u.a2 );
		iterator.next().assign( 
				null, b1, 
				b2, Bi.column(2), Bangi.column(2), Bj.column(2),
				Bangj.column(2), Ji.row(2), Jangi.row(2), Jj.row(2),
				Jangj.row(2),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, u.a3 );		
		
//		Vector3 tn1 = Matrix3.multiply(b1.state.rotation, n1, new Vector3());
//		Vector3 tn2 = Matrix3.multiply(b2.state.rotation, n2, new Vector3());
//		Vector3 t = tn1.cross(tn2).normalize();
//		//if angle is over limit
//		if (tn1.dot(tn2) < 0.87) {
//			//Vector3 t = nerror.copy();
//			iterator.next().assign( 
//					b1, b2, 
//					new Vector3(), b1.state.Iinverse.multiply(t), new Vector3(), b2.state.Iinverse.multiply(t.multiply(-1)),
//					new Vector3(), t, new Vector3(), t.multiply(-1),
//					Double.NEGATIVE_INFINITY,
//					/*Double.POSITIVE_INFINITY*/ 0,
//					t.dot(b1.state.omegaCm)-t.dot(b2.state.omegaCm)  );		
//		}
		
		//tn1.dot(tn2) = 0.87
	}
	
	public Body getBody1() {
		return b1;
	}
	
	

}
