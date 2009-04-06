package jinngine.physics;
import java.util.Iterator;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;

/**
 * Interface to joint types in jinngine. 
 * @author mo
 *
 */
public class UniversalJoint implements Constraint {
	
	//involved bodies
	private Body i;
	private Body j;
	
	//joint anchor point in i and j space
	private final Vector3 ri;
	private final Vector3 rj;
	
	//joint axis in i and j space
	private final Vector3 ni;
	private final Vector3 t1i;
	private final Vector3 t2i;
	
	private final Vector3 nj;
	private final Vector3 t1j;
	private final Vector3 t2j;
		
	
	/**
	 * Construct a general joint with an anchor point. This point will converted into both i's and j's respective spaces.
	 * Tangential vectors of this normal are computed and used for generating constraints.
	 * @param i first body
	 * @param j second body
	 * @param r the joint's anchor point in world space
	 * @param normal The normal vector of the joint
	 */
	public UniversalJoint( Body i, Body j, Vector3 r, Vector3 normal ) {
		this.i = i;
		this.j = j;
		
		//anchor points
		ri = i.toModel(r);
		rj = j.toModel(r);

		
		//joint axis in current world space
		//Use a gram-schmidt process to create a orthonormal basis for the impact space
		//The well-known method takes a set of non co-linear vectors and turn them into an orthonomal basis
		Vector3 n = normal.normalize();
		Vector3 t1 = Vector3.i.copy(); 
		Vector3 t2 = Vector3.j.copy();

		
		//handle degenerate cases
		if ( Math.abs(n.dot(Vector3.i)-1) < 1e-7 ) {
			//n and i is co-linear
			t1.assign(Vector3.j); t2.assign(Vector3.k);
		} else if ( Math.abs(n.dot(Vector3.j)-1 )< 1e-7) {
			//n and j are co linear
			t1.assign(Vector3.i); t2.assign(Vector3.k);
		} 
//		else if ( n.dot(Vector3.k)-1 < 1e-7) {
//			n and k are co linear
//			t1.assign(Vector3.i); t2.assign(Vector3.j);
//		}

		
		//compute tangents
		t1.assign(t1.minus(n.multiply(t1.dot(n))).normalize());
		t2.assign( t2.minus(n.multiply(t2.dot(n))).minus( t2.multiply(t1.dot(t2))).normalize()  );
		
		
		//convert the new basis to body frames
		ni  = i.toModelNoTranslation(n);
		t1i = i.toModelNoTranslation(t1);
		t2i = i.toModelNoTranslation(t2);
		nj  = j.toModelNoTranslation(n);
		t1j = j.toModelNoTranslation(t1);
		t2j = j.toModelNoTranslation(t2);
		
		n.print();
		t1.print();
		t2.print();
	}
	
	@Override
	public void applyConstraints(Iterator<ConstraintEntry> iterator, double dt) {
		// TODO Auto-generated method stub
		
		// Ball-In-Socket joint has a 3x12 jacobian matrix, since
		// it has 3 DOFs, thus removing 3, inducing 3 new constraints

		//b1.toWorld(p1).minus(b2.toWorld(p2)).print();
		
//		Vector3 ri  = b1.state.q.rotate(p1);
//		Vector3 rj  = b2.state.q.rotate(p2);
		Vector3 p   = j.toWorld(rj);
		Vector3 pi  = p.minus(i.state.rCm);
		Vector3 pj  = p.minus(j.state.rCm);
		
		Vector3 riw = Matrix3.multiply(i.state.rotation, ri, new Vector3());
		Vector3 rjw = Matrix3.multiply(j.state.rotation, rj, new Vector3());
		
		Vector3 niw = Matrix3.multiply(i.state.rotation, ni, new Vector3());
		Vector3 njw = Matrix3.multiply(j.state.rotation, nj, new Vector3());
		
		Vector3 t1iw = Matrix3.multiply(i.state.rotation, t1i, new Vector3());
		Vector3 t1jw = Matrix3.multiply(j.state.rotation, t1j, new Vector3());		
		Vector3 t2iw = Matrix3.multiply(i.state.rotation, t2i, new Vector3());
		Vector3 t2jw = Matrix3.multiply(j.state.rotation, t2j, new Vector3());
		
		
		Matrix3 basis  = new Matrix3(niw,t1iw, t2iw);
		Matrix3 basisi = new Matrix3(niw,t1iw, t2iw);
		Matrix3 basisj = new Matrix3(njw,t1jw, t2jw);
		
		//Matrix3.identity(basis);
		
		// nT v1 +  nT omega x r1  - nT v2 - nT omega x r2
		//
		// nT 1/m1 J  + nT (I1(Jxr1) x r1) - nT 1/m2 -J  -nT (I2(-Jxr2) x r2)
		// nT 1/m1 (n lambda)  - nT r1 x ( I1((n lambda) xr1)) + nT 1/m2 n lambda - nT r2 x (I2((n lambda)xr2))
		// [nT -nxr1T nT -nxr2T]   [1/m1 I1 1/m2 I2 ] [ n -nxr1 n -nxr2 ]^T lambda
		// 
		// [ nT  (r1xn)T  -nT  -(r2xn)T ]  [ v1 w1 v2 w2 ] 
		//
		// nTv1 + -(n.r1xw1) -nTv2 +(n.r2xw2)
		
		//[ n t1 t2 ]
		  
		//  r1x (100) 
		//  r1xn T 
		//  r1xt1 T
		//  r1xt2 T
		
		//   0 -a3  a2  0 1 0 
		//  a3   0 -a1  1 0 0
		// -a2  a1   0  0 0 1
		
		//  -a3   0  a2   
		//    0  a3 -a1 
		//   a1 -a2   0
		
		//jacobians on matrix form
		Matrix3 Ji = basis.multiply(-1);
		Matrix3 Jangi = pi.crossProductMatrix3().multiply(basis).multiply(-1);
		Matrix3 Jangi2 = basis.multiply(-1);
		//Jangi = new Matrix3( riw.cross(basis.row(0)), riw.cross(basis.row(1)), riw.cross(basis.row(2))).multiply(-1);

		Matrix3 Jj = basis.multiply(1);
		Matrix3 Jangj = pj.crossProductMatrix3().multiply(basis).multiply(1);
		Matrix3 Jangj2 = basis.multiply(1);
		
		//Jangj = new Matrix3( rjw.cross(basis.row(0)), rjw.cross(basis.row(1)), rjw.cross(basis.row(2)));

		Matrix3 MiInv = new Matrix3().identity().multiply(1/i.state.M);
		Matrix3 MjInv = new Matrix3().identity().multiply(1/j.state.M);

		Matrix3 Bi = MiInv.multiply(Ji);
		Matrix3 Bj = MjInv.multiply(Jj);
		Matrix3 Bangi = i.state.Iinverse.multiply(Jangi);
		Matrix3 Bangj = j.state.Iinverse.multiply(Jangj); 
		
		Matrix3 Bangi2 = i.state.Iinverse.multiply(Jangi2);
		Matrix3 Bangj2 = j.state.Iinverse.multiply(Jangj2);
		
		
		if (i.isFixed()) {
			Bi = new Matrix3();
			Bangi = new Matrix3();
		}
		
		if (j.isFixed()) {
			Bj = new Matrix3();
			Bangj = new Matrix3();
		}
			

		//Matrix3.print(basis);
		//relative velocity in anchor point

		Vector3 u = i.state.vCm.add( i.state.omegaCm.cross(pi)).minus(j.state.vCm).minus(j.state.omegaCm.cross(pj));
		double Kcor = 0.9;
//		Vector3 posError = b1.state.rCm.add(b1.state.q.rotate(p1)).minus(b2.state.rCm).minus(b2.state.q.rotate(p2)).multiply(Kcor);
		Vector3 displacement = i.state.rCm.add(riw).minus(j.state.rCm).minus(rjw);
		u.assign(u.add(displacement.multiply(Kcor*1)));
		//u.print();

		// u = u + error * Kcor
		
		//Vector3 axis = njw;
		
		iterator.next().assign( 
				null, i, 
				j, Bi.column(0), Bangi.column(0), Bj.column(0),
				Bangj.column(0),    Ji.column(0),    Jangi.column(0),    Jj.column(0),
				Jangj.column(0),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, -basis.column(0).dot(u) );
		
		iterator.next().assign( 
				null, i, 
				j, Bi.column(1), Bangi.column(1), Bj.column(1),
				Bangj.column(1), Ji.column(1), Jangi.column(1), Jj.column(1),
				Jangj.column(1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, -basis.column(1).dot(u)   );
		
		iterator.next().assign( 
				null, i, 
				j, Bi.column(2), Bangi.column(2), Bj.column(2),
				Bangj.column(2), Ji.column(2), Jangi.column(2), Jj.column(2),
				Jangj.column(2),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, -basis.column(2).dot(u) );	
		
		//lock onto axis 0 (no rotation about basis(0), the normal)
		iterator.next().assign( 
				null, i, 
				j, new Vector3(), Bangi2.column(0), new Vector3(),
				Bangj2.column(0), new Vector3(), Jangi2.column(0), new Vector3(),
				Jangj2.column(0),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, -basis.column(0).dot(i.state.omegaCm)+basis.column(0).dot(j.state.omegaCm)*0.8 /*+ basis.column(0).dot(basisi.column(2).cross(basisj.column(2)))*0*/ + basisi.column(1).dot(basisj.column(2))  );	
		
//		iterator.next().assign( 
//				i, j, 
//				new Vector3(), Bangi2.column(1), new Vector3(), Bangj2.column(1),
//				new Vector3(), Jangi2.column(1), new Vector3(), Jangj2.column(1),
//				Double.NEGATIVE_INFINITY,
//				Double.POSITIVE_INFINITY,
//				-basis.column(1).dot(i.state.omegaCm)+basis.column(1).dot(j.state.omegaCm) 
//				) ;
		
//
//		iterator.next().assign( 
//				i, j, 
//				new Vector3(), Bangi2.column(2), new Vector3(), Bangj2.column(2),
//				new Vector3(), Jangi2.column(2), new Vector3(), Jangj2.column(2),
//				-170,
//				170,
//				1-basis.column(2).dot(i.state.omegaCm)+basis.column(2).dot(j.state.omegaCm)  /*basis.column(2).dot(basisi.column(0).cross(basisj.column(0)))*1 */ 
//				
//		);
				 
	
	
	}
	

	

}
