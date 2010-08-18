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

import jinngine.physics.constraint.*;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.Solver.NCPConstraint;
import jinngine.util.Pair;

/**
 * Implementation of a Universal joint. This type of joint has two degrees of freedom.
 */
public final class UniversalJoint implements Constraint {
	// members
	public final Body bi,bj;
	public final Vector3 pi,pj,n1i,n2i,n1j,n2j;
		
	private final class AngularJointAxis {
		public final Vector3 tiw = new Vector3();
		public final Vector3 tjw = new Vector3();
		public final Vector3 nijw = new Vector3();
		public final NCPConstraint angular = new NCPConstraint();
		public final NCPConstraint extra = new NCPConstraint();
		// settings for the joint axis
		public double upperLimit = Math.PI*0.25;
		public double lowerLimit = -Math.PI*0.25;
		public double motor  = 0;
		public double motorTargetVelocity = 0;
		public double theta = 0;
		public double velocity = 0;
		public double friction =0.0;
		public final double shell = 0.09;
		public boolean enableLimits = true;
	}
	
	private final AngularJointAxis axis1 = new AngularJointAxis();
	private final AngularJointAxis axis2 = new AngularJointAxis();
	private final JointAxisController controler1;
	private final JointAxisController controler2;
	
	// constraint entries
	private NCPConstraint linear1 = new NCPConstraint();
	private NCPConstraint linear2 = new NCPConstraint();
	private NCPConstraint linear3 = new NCPConstraint();
	private NCPConstraint angular1 = new NCPConstraint();
	
	/**
	 * Get the axis controller for the first axis. Use this controller to adjust joint limits, motor and friction
	 * @return A controller for this hinge joint
	 */
	public final JointAxisController getFirstAxisControler() {
		return controler1;
	}

	/**
	 * Get the axis controller for the second axis. Use this controller to adjust joint limits, motor and friction
	 * @return A controller for this hinge joint
	 */
	public final JointAxisController getSecondAxisControler() {
		return controler2;
	}

	/**
	 * Universal joint implementation. Coordinates and normals are given in world space. The given 
	 * joint axis n1 and n2 must be orthogonal. 
	 */
	public UniversalJoint(Body bi, Body bj, Vector3 p, Vector3 n1, Vector3 n2) {
		this.bi = bi;
		this.bj = bj;
		
		// transform points and vectors to body spaces
		pi = bi.toModel(p);
		n1i = bi.toModelNoTranslation(n1);
		n2i = bi.toModelNoTranslation(n2);
		pj = bj.toModel(p);
		n1j = bj.toModelNoTranslation(n1);
		n2j = bj.toModelNoTranslation(n2);
		
		// create the controllers
		this.controler1 = new JointAxisController() {
			@Override
			public double getPosition() {
				return axis1.theta;
			}

			@Override
			public void setLimits(double thetaMin, double thetaMax) {
				if (thetaMin < -Math.PI || thetaMin > 0)
					throw new IllegalArgumentException("HingeJoint: JointAxisController: thetaMin must be in [-PI,0]");
				
				if ( thetaMax < 0 || thetaMax > Math.PI  )
					throw new IllegalArgumentException("HingeJoint: JointAxisController: thetaMax must be in [0,PI]");

				axis1.upperLimit = thetaMax;
				axis1.lowerLimit = thetaMin;
			}

			@Override
			public double getVelocity() { return axis1.velocity; }

			@Override
			public void setFrictionMagnitude(double magnitude) { axis1.friction = magnitude; }

			@Override
			public void setMotorForce(double maxForceMagnitude, double targetVelocity) {
				if (maxForceMagnitude<0) 
					throw new IllegalArgumentException("HingeJoint: JointAxisController: force magnitude must be positive");
				
				axis1.motor = maxForceMagnitude;
				axis1.motorTargetVelocity = targetVelocity;
			}

			@Override
			public void enableLimits(boolean enable) {
				axis1.enableLimits = enable;	
			}		
		};

		// create the controllers
		this.controler2 = new JointAxisController() {
			@Override
			public double getPosition() {
				return axis2.theta;
			}

			@Override
			public void setLimits(double thetaMin, double thetaMax) {
				if (thetaMin < -Math.PI || thetaMin > 0)
					throw new IllegalArgumentException("HingeJoint: JointAxisController: thetaMin must be in [-PI,0]");
				
				if ( thetaMax < 0 || thetaMax > Math.PI  )
					throw new IllegalArgumentException("HingeJoint: JointAxisController: thetaMax must be in [0,PI]");

				axis2.upperLimit = thetaMax;
				axis2.lowerLimit = thetaMin;
			}

			@Override
			public double getVelocity() { return axis2.velocity; }

			@Override
			public void setFrictionMagnitude(double magnitude) { axis2.friction = magnitude; }

			@Override
			public void setMotorForce(double maxForceMagnitude, double targetVelocity) {
				if (maxForceMagnitude<0) 
					throw new IllegalArgumentException("HingeJoint: JointAxisController: force magnitude must be positive");
				
				axis2.motor = maxForceMagnitude;
				axis2.motorTargetVelocity = targetVelocity;
			}

			@Override
			public void enableLimits(boolean enable) {
				axis2.enableLimits = enable;	
			}		
		};

		
	}

	
	public final void applyConstraints(ListIterator<NCPConstraint> iterator, double dt) {
		//transform vectors to world space
		final Vector3 riw = Matrix3.multiply(bi.state.rotation, pi, new Vector3());
		final Vector3 rjw = Matrix3.multiply(bj.state.rotation, pj, new Vector3());
		final Vector3 n1iw = Matrix3.multiply(bi.state.rotation, n1i, new Vector3());
		final Vector3 n2iw = Matrix3.multiply(bi.state.rotation, n2i, new Vector3());
		final Vector3 n1jw = Matrix3.multiply(bj.state.rotation, n1j, new Vector3());
		final Vector3 n2jw = Matrix3.multiply(bj.state.rotation, n2j, new Vector3());
		
		
		// at all times, n1iw and n2jw are orthogonal 
		
		//jacobians on matrix form
		Matrix3 Ji = Matrix3.identity().multiply(1);
		Matrix3 Jangi = Matrix3.crossProductMatrix(riw).multiply(-1);
		Matrix3 Jj = Matrix3.identity().multiply(-1);
		Matrix3 Jangj = Matrix3.crossProductMatrix(rjw);

//		Matrix3 MiInv = Matrix3.identity().multiply(1/bi.state.mass);
//		Matrix3 MjInv = Matrix3.identity().multiply(1/bj.state.mass);
		final Matrix3 MiInv = bi.state.inverseanisotropicmass;
		final Matrix3 MjInv = bj.state.inverseanisotropicmass;


		Matrix3 Bi = bi.isFixed()? new Matrix3() : MiInv.multiply(Ji.transpose());
		Matrix3 Bangi = bi.isFixed()? new Matrix3() : bi.state.inverseinertia.multiply(Jangi.transpose());
		Matrix3 Bj = bj.isFixed()? new Matrix3() : MjInv.multiply(Jj.transpose());
		Matrix3 Bangj = bj.isFixed()? new Matrix3() : bj.state.inverseinertia.multiply(Jangj.transpose());

		double Kcor = 0.8;
		
//		Vector3 u = b1.state.velocity.minus( ri.cross(b1.state.omega)).minus(b2.state.velocity).add(rj.cross(b2.state.omega));
		Vector3 u = bi.state.velocity.add( bi.state.omega.cross(riw)).sub(bj.state.velocity.add(bj.state.omega.cross(rjw)));

		Vector3 posError = bi.state.position.add(riw).sub(bj.state.position).sub(rjw).multiply(1/dt);
		//error in transformed normal
		Vector3 nerror = n1iw.cross(n2jw);
		u.assign( u.add(posError.multiply(Kcor)));
		
		linear1.assign( 
				bi,	bj, 
				Bi.column(0), Bangi.column(0), Bj.column(0), Bangj.column(0), 
				Ji.row(0), Jangi.row(0), Jj.row(0), Jangj.row(0),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null,
				u.x, 0 );

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

	


		// angular axis where relative velocity must be zero
		Vector3 q = n1iw.cross(n2jw);
		
		angular1.assign( 
				bi,	bj, 
				new Vector3(), bi.isFixed()? new Vector3() : bi.state.inverseinertia.multiply(q), new Vector3(), bj.isFixed()? new Vector3() : bj.state.inverseinertia.multiply(q.multiply(-1)), 
				new Vector3(), q, new Vector3(), q.multiply(-1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null,
				q.dot(bi.state.omega)-q.dot(bj.state.omega) - Kcor*(-n1iw.dot(n2jw))*(1/dt), 0  );		


		// add constraints to return list
		iterator.add(linear1);
		iterator.add(linear2);
		iterator.add(linear3);
		iterator.add(angular1);
		

		axis1.tiw.assign(n1iw);
		axis1.tjw.assign(n1jw);
		axis1.nijw.assign(n2jw);
		applyJointAxisConstraint( axis1 , Kcor, dt, iterator);
		
		axis2.tiw.assign(n2iw);
		axis2.tjw.assign(n2jw);
		axis2.nijw.assign(n1iw);
		applyJointAxisConstraint( axis2 , Kcor, dt, iterator);

	}
	
	
	private final void applyJointAxisConstraint( final AngularJointAxis joint, double Kcor, double dt, ListIterator<NCPConstraint> iterator) {
		// handle the constraint modelling joint limits and motor
		double low = 0;
		double high = 0;
		double correction = 0;
		Vector3 axis = joint.nijw;		
		double sign = joint.tiw.cross(joint.tjw).dot(joint.nijw)>0?1:-1;
		double product = joint.tiw.dot(joint.tjw);
		// make sure product is excatly in [-1,1]
		product = Math.max( Math.min( product, 1), -1);		
		joint.theta = -Math.acos( product )*sign;
		
		// angular velocity along axis 
		joint.velocity = axis.dot(bi.state.omega)-axis.dot(bj.state.omega);
		double bvalue = 0;
		
		// if limits are clamped together
		if ( Math.abs( joint.lowerLimit - joint.upperLimit) < joint.shell && joint.enableLimits) {
			correction = (joint.theta - (joint.upperLimit) )*(1/dt)*Kcor;
			high = Double.POSITIVE_INFINITY;
			low = Double.NEGATIVE_INFINITY;
			bvalue = joint.velocity + correction ;					
		// if joint is stretched upper
		} else if ( joint.theta >= joint.upperLimit-joint.shell && joint.enableLimits ) {
			correction = -(joint.theta - (joint.upperLimit) )*(1/dt)*Kcor;
//			correction = Math.min( correction, 0.9);
			high = joint.motorTargetVelocity>=0?joint.motor:0; // motor is pressing against limit?
			low = Double.NEGATIVE_INFINITY;// + motorLow;
			

			
			// if motor is working to leave the limit, we need an extra 
			// velocity constraint to model the motors contribution at the limit
			if ( joint.motorTargetVelocity<0 && joint.motor>0) {
				joint.extra.assign( 
						bi,	bj, 
						new Vector3(), bi.isFixed()? new Vector3():bi.state.inverseinertia.multiply(axis), new Vector3(), bj.isFixed()? new Vector3() : bj.state.inverseinertia.multiply(axis.multiply(-1)), 
						new Vector3(), axis, new Vector3(), axis.multiply(-1), 
						-joint.motor,
						0,
						null,
						joint.velocity-joint.motorTargetVelocity, 0 );
				// add the motor constraint
				iterator.add(joint.extra);
			} else {
				// clamp correction velocity to motor target when motor is pressing against limit
				if (correction > 0)
					correction = Math.min(correction, joint.motorTargetVelocity);
			}

			
			bvalue = joint.velocity - correction;


			// if joint is stretched lower
		} else if ( joint.theta <= joint.lowerLimit+joint.shell && joint.enableLimits ) {
			correction = -(joint.theta - (joint.lowerLimit) )*(1/dt)*Kcor;
			//			correction = Math.max( correction, -0.9);
			high = Double.POSITIVE_INFINITY;// + motorHigh;
			low = joint.motorTargetVelocity<=0?-joint.motor:0; // motor is pressing against limit?



			// if motor is working to leave the limit, we need an extra 
			// velocity constraint to model the motors contribution at the limit
			if ( joint.motorTargetVelocity>0 && joint.motor>0) {
				joint.extra.assign( 
						bi,	bj, 
						new Vector3(), bi.isFixed()? new Vector3():bi.state.inverseinertia.multiply(axis), new Vector3(), bj.isFixed()? new Vector3() : bj.state.inverseinertia.multiply(axis.multiply(-1)), 
								new Vector3(), axis, new Vector3(), axis.multiply(-1), 
								0,
								joint.motor,
								null,
								joint.velocity-joint.motorTargetVelocity, 0 );

				// add the motor constraint
				iterator.add(joint.extra);
			} else {
				// clamp correction velocity to motor target when motor is pressing against limit
				if (correction < 0)
					correction = Math.max(correction, joint.motorTargetVelocity);
			}

			bvalue = joint.velocity-correction;
		
		// not at limits, motor working 
		} else if (joint.motor!=0 ){
			high = joint.motor;
			low = -joint.motor;
			// motor tries to achieve the target velocity using the motor force available
			bvalue = joint.velocity-joint.motorTargetVelocity;
		// not at limits, no motor. friction is working
		} else if ( joint.friction!=0) {
			high = joint.friction;
			low = -joint.friction;
			//friction tries to prevent motion along the joint axis
			bvalue = joint.velocity;			
		}
		// unlimited joint axis
		
		
		joint.angular.assign( 
				bi,	bj, 
				new Vector3(), bi.isFixed()? new Vector3():bi.state.inverseinertia.multiply(axis), new Vector3(), bj.isFixed()? new Vector3() : bj.state.inverseinertia.multiply(axis.multiply(-1)), 
				new Vector3(), axis, new Vector3(), axis.multiply(-1), 
				low,
				high,
				null,
				bvalue, 0 );
		
		iterator.add(joint.angular);

	}

	@Override
	public Pair<Body> getBodies() {
		return new Pair<Body>(bi,bj);
	}

	@Override
	public final Iterator<NCPConstraint> getNcpConstraints() {
		// return iterator over the members linear1, linear2, linear3. angular1, angular2, angular3
		return new  Iterator<NCPConstraint>() {
			private int i = 0;
			@Override
			public final boolean hasNext() {
				return i<6;
			}
			@Override
			public final NCPConstraint next() {
				switch (i) {
				case 0: i=i+1; return linear1; 
				case 1: i=i+1; return linear2; 
				case 2: i=i+1; return linear3; 
//				case 3: i=i+1; return angular1; 
//				case 4: i=i+1; return angular2; 
				case 3: i=i+1; return angular1; 
				}				
				return null;
			}
			@Override
			public final void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
