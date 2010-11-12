package jinngine.physics.constraint.contact;

import java.util.Iterator;
import java.util.ListIterator;

import jinngine.geometry.contact.ContactGenerator;
import jinngine.geometry.contact.ContactGenerator.ContactPoint;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.constraint.contact.ContactConstraint;
import jinngine.physics.solver.Solver.NCPConstraint;
import jinngine.util.GramSchmidt;
import jinngine.util.Pair;

public class ApproximatedContactConstraint implements Constraint {

//	private final NCPConstraint linear1  = new NCPConstraint();
	private final NCPConstraint linear2  = new NCPConstraint();
	private final NCPConstraint linear3  = new NCPConstraint();
	private final NCPConstraint angular1 = new NCPConstraint();
//	private final NCPConstraint angular2 = new NCPConstraint();
//	private final NCPConstraint angular3 = new NCPConstraint();

	private final Body bi,bj;
	public final Vector3 externalNormal = new Vector3();

	public ApproximatedContactConstraint(Body bi, Body bj) {
		// store the normal
		
		// get the bodies
		this.bi = bi;
		this.bj = bj;
	}
	
	@Override
	public void applyConstraints(ListIterator<NCPConstraint> iterator, double dt) {	
	
		// get contact space
		final Vector3 normal = new Vector3(externalNormal);		
		final Matrix3 S = GramSchmidt.run(normal);
		final Vector3 t1 = S.row(1);
		final Vector3 t2 = S.row(2);
		
		// position of joint relative to body j mass centre (ri is defined to be zero)
		final Vector3 rj = bi.state.position.sub(bj.state.position);
		
		// jacobians
		Matrix3 Ji    = new Matrix3(S.transpose());
		Matrix3 Jangi = new Matrix3();
		Matrix3 Jj    = new Matrix3(S.transpose().negate());
		Matrix3 Jangj = new Matrix3((Matrix3.cross(rj).multiply(S)).transpose().negate());

		// B = M^{-1}J^T
		final Matrix3 MiInv = bi.state.inverseanisotropicmass;
		final Matrix3 MjInv = bj.state.inverseanisotropicmass;
		final Matrix3 Bi = bi.isFixed()? new Matrix3() : MiInv.multiply(Ji.transpose());
		final Matrix3 Bangi = bi.isFixed()? new Matrix3() : bi.state.inverseinertia.multiply(Jangi.transpose());
		final Matrix3 Bj = bj.isFixed()? new Matrix3() : MjInv.multiply(Jj.transpose());
		final Matrix3 Bangj = bj.isFixed()? new Matrix3() : bj.state.inverseinertia.multiply(Jangj.transpose());

		// relative velocity at centre of mass of bi
		final Vector3 u = bi.state.velocity.sub(bj.state.velocity.add(bj.state.omega.cross(rj)));	
		
//		linear1.assign( 
//				bi,	bj, 
//				Bi.column(0), Bangi.column(0), Bj.column(0), Bangj.column(0), 
//				Ji.row(0), Jangi.row(0), Jj.row(0), Jangj.row(0),
//				Double.NEGATIVE_INFINITY,
//				Double.POSITIVE_INFINITY,
//				null,
//				u.x, 0 );

		linear2.assign( 
				bi,	bj, 
				Bi.column(1), Bangi.column(1), Bj.column(1), Bangj.column(1), 
				Ji.row(1), Jangi.row(1), Jj.row(1), Jangj.row(1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null,
				u.y, 0 );

		linear3.assign( 
				bi,	bj, 
				Bi.column(2), Bangi.column(2), Bj.column(2), Bangj.column(2), 
				Ji.row(2), Jangi.row(2), Jj.row(2), Jangj.row(2),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null,
				u.z, 0 );	

		
		angular1.assign( 
				bi,	bj, 
				new Vector3(), bi.isFixed()? new Vector3():bi.state.inverseinertia.multiply(normal), new Vector3(), bj.isFixed()? new Vector3() : bj.state.inverseinertia.multiply(normal.negate()), 
				new Vector3(), normal, new Vector3(), normal.negate(), 
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null,
				normal.dot(bi.state.omega)-normal.dot(bj.state.omega), 0 );

		
//		angular2.assign( 
//				bi, bj, 
//				new Vector3(), bi.isFixed()? new Vector3() : bi.state.inverseinertia.multiply(t1), new Vector3(), bj.isFixed()? new Vector3() : bj.state.inverseinertia.multiply(t1.negate()), 
//				new Vector3(), t1, new Vector3(), t1.negate(),
//				Double.NEGATIVE_INFINITY,
//				Double.POSITIVE_INFINITY,
//				null,
//				t1.dot(bi.state.omega)-t1.dot(bj.state.omega) , 0  );	
//		
//		angular3.assign( 
//				bi,	bj, 
//				new Vector3(), bi.isFixed()? new Vector3() : bi.state.inverseinertia.multiply(t2), new Vector3(), bj.isFixed()? new Vector3() : bj.state.inverseinertia.multiply(t2.negate()), 
//				new Vector3(), t2, new Vector3(), t2.negate(),
//				Double.NEGATIVE_INFINITY,
//				Double.POSITIVE_INFINITY,
//				null,
//				t2.dot(bi.state.omega)-t2.dot(bj.state.omega), 0  );		


		// add constraints to return list
//		iterator.add(linear1);
		iterator.add(linear2);
		iterator.add(linear3);
		iterator.add(angular1);
//		iterator.add(angular2);
//		iterator.add(angular3);		
		
	}

	@Override
	public Pair<Body> getBodies() {
		return new Pair<Body>(bi,bj);
	}

	@Override
	public Iterator<NCPConstraint> getNcpConstraints() {
		throw new UnsupportedOperationException("lkajdlfkjdaf");
		
//		// return iterator over the members linear1, linear2, linear3. angular1, angular2, angular3
//		return new  Iterator<NCPConstraint>() {
//			private int i = 0;
//			@Override
//			public final boolean hasNext() {
//				return i<6;
//			}
//			@Override
//			public final NCPConstraint next() {
//				switch (i) {
//				case 0: i=i+1; return linear1; 
//				case 1: i=i+1; return linear2; 
//				case 2: i=i+1; return linear3; 
//				case 3: i=i+1; return angular1; 
//				case 4: i=i+1; return angular2; 
//				case 5: i=i+1; return angular3; 
//				}				
//				return null;
//			}
//			@Override
//			public final void remove() {
//				throw new UnsupportedOperationException();
//			}
//		};
	}
}
