/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.physics.constraint.joint;

import java.util.*;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.solver.*;
import jinngine.util.Pair;

/**
 * The ball-in-socket joint
 */
public class BallInSocketJoint implements Constraint {	
	private final Body b1,b2;
	private final Vector3 p1, p2;// n1, n2;
	private final Solver.constraint c1 = new Solver.constraint();
	private final Solver.constraint c2 = new Solver.constraint();
	private final Solver.constraint c3 = new Solver.constraint();
	
	public BallInSocketJoint(Body b1, Body b2, Vector3 p, Vector3 n) {
		this.b1 = b1;
		this.b2 = b2;
		//anchor points on bodies
		p1 = b1.toModel(p);
		p2 = b2.toModel(p);	
	}
	
	public void applyConstraints( ListIterator<Solver.constraint> iterator, double dt ) {
		// Ball-In-Socket joint has a 3x12 jacobian matrix, since
		// it has 3 DOFs, thus removing 3, inducing 3 new constraints
		Vector3 ri = Matrix3.multiply(b1.state.rotation, p1, new Vector3());
		Vector3 rj = Matrix3.multiply(b2.state.rotation, p2, new Vector3());
		
		//jacobians on matrix form
		Matrix3 Ji = Matrix3.identity().multiply(1);
		Matrix3 Jangi =ri.crossProductMatrix3().multiply(-1);
		Matrix3 Jj = Matrix3.identity().multiply(-1);
		Matrix3 Jangj = rj.crossProductMatrix3().multiply(1);

		
		Matrix3 MiInv = Matrix3.identity().multiply(1/b1.state.mass);
		Matrix3 Bi = MiInv.multiply(Ji.transpose());
		Matrix3 Bangi = b1.state.inverseinertia.multiply(Jangi.transpose());

		if (b1.isFixed()) {
			Bi.assign(new Matrix3());
			Bangi.assign(new Matrix3());
		}
		
		Matrix3 MjInv = Matrix3.identity().multiply(1/b2.state.mass);
		Matrix3 Bj = MjInv.multiply(Jj.transpose());
		Matrix3 Bangj = b2.state.inverseinertia.multiply(Jangj.transpose());

		if (b2.isFixed()) {
			Bj.assign(new Matrix3());
			Bangj.assign(new Matrix3());
		}		

//		Vector3 u = b1.state.velocity.add( b1.state.omega.cross(ri)).minus(b2.state.velocity).add(b2.state.omega.cross(rj));
		Vector3 u = b1.state.velocity.add( b1.state.omega.cross(ri)).minus(b2.state.velocity.add(b2.state.omega.cross(rj)));

		double Kcor = 0.8;
//		Vector3 posError = b1.state.rCm.add(b1.state.q.rotate(p1)).minus(b2.state.rCm).minus(b2.state.q.rotate(p2)).multiply(Kcor);
		Vector3 posError = b1.state.position.add(ri).minus(b2.state.position).minus(rj).multiply(Kcor*(1.0/dt));
		u.assign( u.add(posError));
		
		
		//go through matrices and create rows in the final A matrix to be solved
		c1.assign( 
				b1, b2, 
				Bi.column(0), Bangi.column(0), Bj.column(0), Bangj.column(0), 
				Ji.row(0), Jangi.row(0), Jj.row(0), Jangj.row(0),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, 
				u.x );
		c2.assign( 
				b1, b2, 
				Bi.column(1), Bangi.column(1), Bj.column(1), Bangj.column(1),
				Ji.row(1), Jangi.row(1), Jj.row(1), Jangj.row(1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, 
				u.y );
		c3.assign( 
				b1, b2, 
				Bi.column(2), Bangi.column(2), Bj.column(2), Bangj.column(2), 
				Ji.row(2), Jangi.row(2), Jj.row(2), Jangj.row(2),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, 
				u.z );
		
		
		//apply constraints
		iterator.add(c1);
		iterator.add(c2);
		iterator.add(c3);
	}

	@Override
	public Pair<Body> getBodies() {
		return new Pair<Body>(b1,b2);
	}
}
