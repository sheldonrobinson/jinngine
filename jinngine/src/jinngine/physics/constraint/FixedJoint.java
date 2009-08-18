package jinngine.physics.constraint;

import java.util.Iterator;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.*;

public class FixedJoint implements Constraint {
	private final Body b1,b2;
	@SuppressWarnings("unused")
	private final Vector3 p1,p2,n1,n2,t2i,t2j,t3i;
	
	public FixedJoint(Body b1, Body b2, Vector3 p, Vector3 n) {
		this.b1 = b1;
		this.b2 = b2;		
		//anchor points on bodies
		p1 = b1.toModel(p);
		n1 = b1.toModelNoTranslation(n);

		p2 = b2.toModel(p);
		n2 = b2.toModelNoTranslation(n);
		
		//Use a gram-schmidt process to create a orthonormal basis for the impact space
		Vector3 v1 = n.normalize(); Vector3 v2 = Vector3.i; Vector3 v3 = Vector3.k;    
		Vector3 t1 = v1.normalize(); 
		t2i = v2.minus( t1.multiply(t1.dot(v2)) );
		t2j = b2.toModelNoTranslation(b1.toWorldNoTranslation(t2i));
		
		//in case v1 and v2 are parallel
		if ( t2i.abs().lessThan( Vector3.epsilon ) ) {
			v2 = Vector3.j; v3 = Vector3.k;
			t2i.assign(v2.minus( t1.multiply(t1.dot(v2)) ).normalize());    
		} else {
			t2i.assign(t2i.normalize());
		}
		//v1 paralell with v3
		if( v1.cross(v3).abs().lessThan( Vector3.epsilon ) ) {
			v3 = Vector3.j;
		}
		//finaly calculate t3
		t3i = v3.minus( t1.multiply(t1.dot(v3)).minus( t2i.multiply(t2i.dot(v3)) )).normalize();
	}

	public final void applyConstraints(Iterator<ConstraintEntry> iterator, double dt) {
		// TODO Auto-generated method stub
//		Vector3 ri = b1.state.q.rotate(p1);
//		Vector3 rj = b2.state.q.rotate(p2);
//		//tangents in b1 space, transform to world
//		Vector3 ti = b1.state.q.rotate(t2);
//		Vector3 tj = b1.state.q.rotate(t3);
		
		Vector3 ri = Matrix3.multiply(b1.state.rotation, p1, new Vector3());
		Vector3 rj = Matrix3.multiply(b2.state.rotation, p2, new Vector3());
		
		Vector3 tt2i = Matrix3.multiply(b1.state.rotation, t2i, new Vector3());
		Vector3 tt2j = Matrix3.multiply(b2.state.rotation, t2i, new Vector3());		
		Vector3 tt3i = Matrix3.multiply(b1.state.rotation, t3i, new Vector3());
		Vector3 tni = Matrix3.multiply(b1.state.rotation, n1, new Vector3());
		Vector3 tnj = Matrix3.multiply(b2.state.rotation, n2, new Vector3());
		
		//jacobians on matrix form
		Matrix3 Ji = new Matrix3().identity().multiply(1);
		Matrix3 Jangi =ri.crossProductMatrix3().multiply(-1);
		Matrix3 Jj = new Matrix3().identity().multiply(-1);
		Matrix3 Jangj = rj.crossProductMatrix3().multiply(1);

		Matrix3 MiInv = new Matrix3().identity().multiply(1/b1.state.M);
		Matrix3 MjInv = new Matrix3().identity().multiply(1/b2.state.M);

		Matrix3 Bi = MiInv.multiply(Ji.transpose());
		Matrix3 Bj = MjInv.multiply(Jj.transpose());
		Matrix3 Bangi = b1.state.Iinverse.multiply(Jangi.transpose());
		Matrix3 Bangj = b2.state.Iinverse.multiply(Jangj.transpose());

		double Kcor = 0.9;
		
		Vector3 u = b1.state.vCm.minus( ri.cross(b1.state.omegaCm)).minus(b2.state.vCm).add(rj.cross(b2.state.omegaCm));
//		Vector3 posError = b1.state.rCm.add(b1.state.q.rotate(p1)).minus(b2.state.rCm).minus(b2.state.q.rotate(p2)).multiply(Kcor);
		Vector3 posError = b1.state.rCm.add(ri).minus(b2.state.rCm).minus(rj).multiply(Kcor);
//		Vector3 u = b1.state.v_cm.minus( ri.cross(b1.state.omega_cm)).add(b2.state.v_cm).minus(rj.cross(b2.state.omega_cm)).multiply(1);
		//error in transformed normal

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

		
		Vector3 tangenterror = tni.cross(tnj);
		Vector3 normalerror = tt2i.cross(tt2j);

		iterator.next().assign( 
				null, b1, 
				b2, new Vector3(), b1.state.Iinverse.multiply(tni), new Vector3(),
				b2.state.Iinverse.multiply(tni.multiply(-1)), new Vector3(), tni, new Vector3(),
				tni.multiply(-1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, tni.dot(b1.state.omegaCm)-tni.dot(b2.state.omegaCm) - Kcor*tni.dot(normalerror) );	

		
		iterator.next().assign( 
				null, b1, 
				b2, new Vector3(), b1.state.Iinverse.multiply(tt2i), new Vector3(),
				b2.state.Iinverse.multiply(tt2i.multiply(-1)), new Vector3(), tt2i, new Vector3(),
				tt2i.multiply(-1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, tt2i.dot(b1.state.omegaCm)-tt2i.dot(b2.state.omegaCm) - Kcor*tt2i.dot(tangenterror) );	
		
		iterator.next().assign( 
				null, b1, 
				b2, new Vector3(), b1.state.Iinverse.multiply(tt3i), new Vector3(),
				b2.state.Iinverse.multiply(tt3i.multiply(-1)), new Vector3(), tt3i, new Vector3(),
				tt3i.multiply(-1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, tt3i.dot(b1.state.omegaCm)-tt3i.dot(b2.state.omegaCm) - Kcor*tt3i.dot(tangenterror) );		



	}
	

}
