/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
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
 * Implementation of a hinge joint. This type of joint leaves only one degree of freedom left for the involved bodies, 
 * where they can have angular motion along some axis.
 */
public final class HingeJoint implements Constraint {
	// members
	public final Body b1,b2;
	public final Vector3 pi,pj,ni,nj,t2i,t2j, t3i;
	private final JointAxisController controler;
		
	// settings for the joint axis
	public double upperLimit = Double.POSITIVE_INFINITY;
	public double lowerLimit = Double.NEGATIVE_INFINITY;
	private double motor  = 0;
	private double motorTargetVelocity = 0;
	private double theta = 0;
	private double velocity = 0;
	private double friction = 0.0;
	private final double shell = 0.09;
	private boolean enableLimits = true;
	
	// constraint entries
	private NCPConstraint linear1 = new NCPConstraint();
	private NCPConstraint linear2 = new NCPConstraint();
	private NCPConstraint linear3 = new NCPConstraint();
	private NCPConstraint angular1 = new NCPConstraint();
	private NCPConstraint angular2 = new NCPConstraint();
	private NCPConstraint angular3 = new NCPConstraint();
	private NCPConstraint extra = new NCPConstraint();

	
	/**
	 * Get the axis controller for the hinge joint. Use this controller to adjust joint limits, motor and friction
	 * @return A controller for this hinge joint
	 */
	public JointAxisController getHingeControler() {
		return controler;
	}
	
	public HingeJoint(Body b1, Body b2, Vector3 p, Vector3 n) {
		this.b1 = b1;
		this.b2 = b2;		
		//anchor points on bodies
		pi = b1.toModel(p);
		ni = b1.toModelNoTranslation(n);
		pj = b2.toModel(p);
		nj = b2.toModelNoTranslation(n);
				
		//Use a Gram-Schmidt process to create a orthonormal basis for the impact space
		Vector3 v1 = n.normalize(); Vector3 v2 = Vector3.i(); Vector3 v3 = Vector3.k();
		Vector3 t1 = v1.normalize(); 
		t2i = v2.sub( t1.multiply(t1.dot(v2)) );
		
		System.out.println(t2i);
		
		//in case v1 and v2 are parallel
		if ( t2i.isEpsilon(1e-9) ) {
			v2 = Vector3.j(); v3 = Vector3.k();
			t2i.assign(v2.sub( t1.multiply(t1.dot(v2)) ).normalize());
		} else {
			t2i.assign(t2i.normalize());
		}
		
		//tangent 2 in j body space
		t2j = b2.toModelNoTranslation(b1.toWorldNoTranslation(t2i));
		
		System.out.println(t2j);
		
		//v1 parallel with v3
		if( v1.cross(v3).isEpsilon(1e-9) ) {
			v3 = Vector3.j();
		}
		//finally calculate t3
		t3i = v3.sub( t1.multiply(t1.dot(v3)).sub( t2i.multiply(t2i.dot(v3)) )).normalize();
		
		
		System.out.println(t3i);
		
		// create the controller
		this.controler = new JointAxisController() {
			@Override
			public double getPosition() {
				return theta;
			}

			@Override
			public void setLimits(double thetaMin, double thetaMax) {
				if (thetaMin < -Math.PI || thetaMin > 0)
					throw new IllegalArgumentException("HingeJoint: JointAxisController: thetaMin must be in [-PI,0]");
				
				if ( thetaMax < 0 || thetaMax > Math.PI  )
					throw new IllegalArgumentException("HingeJoint: JointAxisController: thetaMax must be in [0,PI]");

				upperLimit = thetaMax;
				lowerLimit = thetaMin;
			}

			@Override
			public double getVelocity() {
				return velocity;
			}

			@Override
			public void setFrictionMagnitude(double magnitude) {
				friction = magnitude;
				
			}

			@Override
			public void setMotorForce(double maxForceMagnitude, double targetVelocity) {
				if (maxForceMagnitude<0) 
					throw new IllegalArgumentException("HingeJoint: JointAxisController: force magnitude must be positive");
				
				motor = maxForceMagnitude;
				motorTargetVelocity = targetVelocity;
			}

			@Override
			public void enableLimits(boolean enable) {
				enableLimits = enable;	
			}		
		};
		
	}

	
	public final void applyConstraints(ListIterator<NCPConstraint> iterator, double dt) {
		//transform points
		Vector3 ri = Matrix3.multiply(b1.state.rotation, pi, new Vector3());
		Vector3 rj = Matrix3.multiply(b2.state.rotation, pj, new Vector3());
		Vector3 tt2i = Matrix3.multiply(b1.state.rotation, t2i, new Vector3());
		Vector3 tt2j = Matrix3.multiply(b2.state.rotation, t2j, new Vector3());		
		Vector3 tt3i = Matrix3.multiply(b1.state.rotation, t3i, new Vector3());
		Vector3 tn1 = Matrix3.multiply(b1.state.rotation, ni, new Vector3());
		Vector3 tn2 = Matrix3.multiply(b2.state.rotation, nj, new Vector3());
		
		//jacobians on matrix form
		final Matrix3 Ji = Matrix3.identity().multiply(1);
		final Matrix3 Jangi = Matrix3.cross(ri).multiply(-1);
		final Matrix3 Jj = Matrix3.identity().multiply(-1);
		final Matrix3 Jangj = Matrix3.cross(rj);

//		Matrix3 MiInv = Matrix3.identity().multiply(1/b1.state.mass);
//		Matrix3 MjInv = Matrix3.identity().multiply(1/b2.state.mass);
		final Matrix3 MiInv = b1.state.inverseanisotropicmass;
		final Matrix3 MjInv = b2.state.inverseanisotropicmass;
		

		final Matrix3 Bi = b1.isFixed()? new Matrix3() : MiInv.multiply(Ji.transpose());
		final Matrix3 Bangi = b1.isFixed()? new Matrix3() : b1.state.inverseinertia.multiply(Jangi.transpose());
		final Matrix3 Bj = b2.isFixed()? new Matrix3() : MjInv.multiply(Jj.transpose());
		final Matrix3 Bangj = b2.isFixed()? new Matrix3() : b2.state.inverseinertia.multiply(Jangj.transpose());

		double Kcor = 0.9;
		
//		Vector3 u = b1.state.velocity.minus( ri.cross(b1.state.omega)).minus(b2.state.velocity).add(rj.cross(b2.state.omega));
		Vector3 u = b1.state.velocity.add( b1.state.omega.cross(ri)).sub(b2.state.velocity.add(b2.state.omega.cross(rj)));

		Vector3 posError = b1.state.position.add(ri).sub(b2.state.position).sub(rj).multiply(1/dt);
		//error in transformed normal
		Vector3 nerror = tn1.cross(tn2);
		u.assign( u.add(posError.multiply(Kcor)));
		
		linear1.assign( 
				b1,	b2, 
				Bi.column(0), Bangi.column(0), Bj.column(0), Bangj.column(0), 
				Ji.row(0), Jangi.row(0), Jj.row(0), Jangj.row(0),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null,
				u.x, 0 );

		linear2.assign( 
				b1,	b2, 
				Bi.column(1), Bangi.column(1), Bj.column(1), Bangj.column(1), 
				Ji.row(1), Jangi.row(1), Jj.row(1), Jangj.row(1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null,
				u.y, 0 );

		linear3.assign( 
				b1,	b2, 
				Bi.column(2), Bangi.column(2), Bj.column(2), Bangj.column(2), 
				Ji.row(2), Jangi.row(2), Jj.row(2), Jangj.row(2),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null,
				u.z, 0 );	

	
		// handle the constraint modelling joint limits and motor
		double low = 0;
		double high = 0;
		double correction = 0;
		Vector3 axis = tn1;		
		double sign = tt2i.cross(tt2j).dot(tn1)>0?1:-1;
		double product = tt2i.dot(tt2j);
		// make sure product is excatly in [-1,1]
		product = Math.max( Math.min( product, 1), -1);		
		theta = -Math.acos( product )*sign;
		
		// angular velocity along axis 
		this.velocity = axis.dot(b1.state.omega)-axis.dot(b2.state.omega);
		double bvalue = 0;
		
		// if limits are clamped together
		if ( Math.abs( lowerLimit - upperLimit) < shell && enableLimits) {
			correction = (theta - (upperLimit) )*(1/dt)*Kcor;
			high = Double.POSITIVE_INFINITY;
			low = Double.NEGATIVE_INFINITY;
			bvalue = velocity + correction ;					
		// if joint is stretched upper
		} else if ( theta >= upperLimit-shell && enableLimits ) {
			correction = (theta - (upperLimit) )*(1/dt)*Kcor;
//			correction = Math.min( correction, 0.9);
			high = motorTargetVelocity>=0?motor:0; // motor is pressing against limit?
			low = Double.NEGATIVE_INFINITY;// + motorLow;
			bvalue = velocity + correction;
			
			// if motor is working to leave the limit, we need an extra 
			// velocity constraint to model the motors contribution at the limit
			if ( motorTargetVelocity<0 && motor>0) {
				extra.assign( 
						b1,	b2, 
						new Vector3(), b1.isFixed()? new Vector3():b1.state.inverseinertia.multiply(axis), new Vector3(), b2.isFixed()? new Vector3() : b2.state.inverseinertia.multiply(axis.multiply(-1)), 
						new Vector3(), axis, new Vector3(), axis.multiply(-1), 
						-this.motor,
						0,
						null,
						this.velocity-this.motorTargetVelocity, 0 );
				// add the motor constraint
				iterator.add(extra);
			}
		// if joint is stretched lower
		} else if ( theta <= lowerLimit+shell && enableLimits ) {
			correction = (theta - (lowerLimit) )*(1/dt)*Kcor;
//			correction = Math.max( correction, -0.9);
			high = Double.POSITIVE_INFINITY;// + motorHigh;
			low = motorTargetVelocity<=0?-motor:0; // motor is pressing against limit?
			bvalue = (velocity + correction) ;
			
			// if motor is working to leave the limit, we need an extra 
			// velocity constraint to model the motors contribution at the limit
			if ( motorTargetVelocity>0 && motor>0) {
				extra.assign( 
						b1,	b2, 
						new Vector3(), b1.isFixed()? new Vector3():b1.state.inverseinertia.multiply(axis), new Vector3(), b2.isFixed()? new Vector3() : b2.state.inverseinertia.multiply(axis.multiply(-1)), 
						new Vector3(), axis, new Vector3(), axis.multiply(-1), 
						0,
						this.motor,
						null,
						this.velocity-this.motorTargetVelocity, 0 );

				// add the motor constraint
				iterator.add(extra);
			}
		// not at limits, motor working 
		} else if (motor!=0 ){
			high = motor;
			low = -motor;
			// motor tries to achieve the target velocity using the motor force available
			bvalue = velocity-this.motorTargetVelocity;
		// not at limits, no motor. friction is working
		} else if ( friction!=0) {
			high = friction;
			low = -friction;
			//friction tries to prevent motion along the joint axis
			bvalue = velocity;			
		}
		// unlimited joint axis
		
		
		
		
		angular1.assign( 
				b1,	b2, 
				new Vector3(), b1.isFixed()? new Vector3():b1.state.inverseinertia.multiply(axis), new Vector3(), b2.isFixed()? new Vector3() : b2.state.inverseinertia.multiply(axis.multiply(-1)), 
				new Vector3(), axis, new Vector3(), axis.multiply(-1), 
				low,
				high,
				null,
				bvalue, 0 );

		
		//keep bodies aligned to the axis
		angular2.assign( 
				b1, b2, 
				new Vector3(), b1.isFixed()? new Vector3() : b1.state.inverseinertia.multiply(tt2i), new Vector3(), b2.isFixed()? new Vector3() : b2.state.inverseinertia.multiply(tt2i.multiply(-1)), 
				new Vector3(), tt2i, new Vector3(), tt2i.multiply(-1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null,
				tt2i.dot(b1.state.omega)-tt2i.dot(b2.state.omega) - Kcor*tt2i.dot(nerror)*(1/dt), 0  );	

		
		angular3.assign( 
				b1,	b2, 
				new Vector3(), b1.isFixed()? new Vector3() : b1.state.inverseinertia.multiply(tt3i), new Vector3(), b2.isFixed()? new Vector3() : b2.state.inverseinertia.multiply(tt3i.multiply(-1)), 
				new Vector3(), tt3i, new Vector3(), tt3i.multiply(-1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null,
				tt3i.dot(b1.state.omega)-tt3i.dot(b2.state.omega) - Kcor*tt3i.dot(nerror)*(1/dt), 0  );		


		// add constraints to return list
		iterator.add(linear1);
		iterator.add(linear2);
		iterator.add(linear3);
		iterator.add(angular1);
		iterator.add(angular2);
		iterator.add(angular3);
		


	}

	@Override
	public Pair<Body> getBodies() {
		return new Pair<Body>(b1,b2);
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
				case 3: i=i+1; return angular1; 
				case 4: i=i+1; return angular2; 
				case 5: i=i+1; return angular3; 
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
