package jinngine.physics.constraint;

import java.util.*;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.*;


public final class HingeJoint implements Constraint {

	private final Body b1,b2;
	private final Vector3 pi,pj,ni,nj,t2i,t2j, t3i;
	private final JointAxisController controler;
	
	private double upperLimit = Double.POSITIVE_INFINITY;
	private double lowerLimit = Double.NEGATIVE_INFINITY;
	private double motor  = 0;
	private double theta = 0;
	private double velocity = 0;
	private double desiredVelocity = 0;
	private double forceMagnitude = 0;
	
	boolean velocityMode = false;
	
	
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
		
		//Use a gram-schmidt process to create a orthonormal basis for the impact space
		Vector3 v1 = n.normalize(); Vector3 v2 = Vector3.i; Vector3 v3 = Vector3.k;    
		Vector3 t1 = v1.normalize(); 
		t2i = v2.minus( t1.multiply(t1.dot(v2)) );
		
		//in case v1 and v2 are parallel
		if ( t2i.abs().lessThan( Vector3.epsilon ) ) {
			v2 = Vector3.j; v3 = Vector3.k;
			t2i.assign(v2.minus( t1.multiply(t1.dot(v2)) ).normalize());    
		} else {
			t2i.assign(t2i.normalize());
		}
		
		//tangent 2 in j body space
		t2j = b2.toModelNoTranslation(b1.toWorldNoTranslation(t2i));
		
		//v1 paralell with v3
		if( v1.cross(v3).abs().lessThan( Vector3.epsilon ) ) {
			v3 = Vector3.j;
		}
		//finaly calculate t3
		t3i = v3.minus( t1.multiply(t1.dot(v3)).minus( t2i.multiply(t2i.dot(v3)) )).normalize();
		
		
		//create the controler
		this.controler = new JointAxisController() {
			@Override
			public double getAngularPosition() {
				return theta;
			}

			@Override
			public void setAngularLimits(double thetaMin, double thetaMax) {
				upperLimit = thetaMax;
				lowerLimit = thetaMin;
			}

			@Override
			public void setAngularMotorForce(double magnitude) {
				velocityMode = false;
				motor = magnitude;
			}

			@Override
			public double getAngularVelocity() {
				return velocity;
			}

			@Override
			public void setDesiredAngularVelocity(double velocity, double force) {
				desiredVelocity = velocity;	
				forceMagnitude = force;
				velocityMode = true;
			}
			
		};
		
	}

	public final void applyConstraints(Iterator<ConstraintEntry> iterator, double dt) {
		// TODO Auto-generated method stub
//		Vector3 ri = b1.state.q.rotate(p1);
//		Vector3 rj = b2.state.q.rotate(p2);
//		//tangents in b1 space, transform to world
//		Vector3 ti = b1.state.q.rotate(t2);
//		Vector3 tj = b1.state.q.rotate(t3);
		
		Vector3 ri = Matrix3.multiply(b1.state.rotation, pi, new Vector3());
		Vector3 rj = Matrix3.multiply(b2.state.rotation, pj, new Vector3());
		Vector3 tt2i = Matrix3.multiply(b1.state.rotation, t2i, new Vector3());
		Vector3 tt2j = Matrix3.multiply(b2.state.rotation, t2j, new Vector3());		
		Vector3 tt3i = Matrix3.multiply(b1.state.rotation, t3i, new Vector3());
		Vector3 tn1 = Matrix3.multiply(b1.state.rotation, ni, new Vector3());
		Vector3 tn2 = Matrix3.multiply(b2.state.rotation, nj, new Vector3());
		
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

		double Kcor = 0.9;
		
		Vector3 u = b1.state.vCm.minus( ri.cross(b1.state.omegaCm)).minus(b2.state.vCm).add(rj.cross(b2.state.omegaCm));
//		Vector3 posError = b1.state.rCm.add(b1.state.q.rotate(p1)).minus(b2.state.rCm).minus(b2.state.q.rotate(p2)).multiply(Kcor);
		Vector3 posError = b1.state.rCm.add(ri).minus(b2.state.rCm).minus(rj).multiply(Kcor);
//		Vector3 u = b1.state.v_cm.minus( ri.cross(b1.state.omega_cm)).add(b2.state.v_cm).minus(rj.cross(b2.state.omega_cm)).multiply(1);
		//error in transformed normal
		Vector3 nerror = tn1.cross(tn2);

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

	
		//handle the constraint modeling joint limits and motor
		double low = 0;
		double high = 0;
		//Vector3 wcor = new Vector3();
		double correction = 0;
		Vector3 axis = tn1;		
		double sign = tt2i.cross(tt2j).dot(tn1)>0?1:-1;
		double product = tt2i.dot(tt2j);
		//avoid values slightly greater then one
		theta = Math.acos( product>1?1:product )*sign;
		//set the motor limits
		double motorHigh = motor>0?motor:0;
		double motorLow = motor>0?0:motor;
		velocity = axis.dot(b1.state.omegaCm)-axis.dot(b2.state.omegaCm);
		double bvalue = 0;
		
		//if joint is stretched upper
		//double limit = Math.PI*0.3333;
		if ( theta > upperLimit  ) {
			correction = -(theta - upperLimit);
			high = motorHigh;
			low = Double.NEGATIVE_INFINITY + motorLow;
			bvalue = velocity + correction ;
		} 
		
		//if joint is stretched lower
		else if ( theta < lowerLimit ) {
			correction = -(theta - lowerLimit);
			high = Double.POSITIVE_INFINITY + motorHigh;
			low = motorLow;
			bvalue = velocity + correction ;

		}
		
		//not at limits (motor is working)
		else {
			if (velocityMode) {
				high = forceMagnitude;
				low = -forceMagnitude;

				//motor tries to accelerate joint to the desired velocity using available force
				bvalue = velocity -desiredVelocity;
				
				
			} else {
				high = motorHigh;
				low = motorLow;

				//motor tries to accelerate joint to the maximum velocity possible
				bvalue = Math.signum(motor)>0? Double.POSITIVE_INFINITY: Double.NEGATIVE_INFINITY;
			}
		}
		

		

//		if (tt2i.dot(tt2j) < Math.cos(Math.PI/32.0)) {
//			axis.assign(tt2i.cross(tt2j).normalize());
//			//rotate tt2i along axis to the maximum allowed displacement angle
//			Matrix3 Rlimit = Quaternion.rotation(Math.PI/3.0, axis).rotationMatrix3();
//			Vector3 tt2ilimit = Matrix3.multiply(Rlimit, tt2i, new Vector3());
//			
//			low = Double.NEGATIVE_INFINITY;
//			//high = 122;
//			
//			//with low error, and approximation will do (theta aproximates sin(theta) for small theta)
//			wcor.assign(tt2j.cross(tt2ilimit));
//		}

		iterator.next().assign( 
				null, b1, 
				b2, new Vector3(), b1.state.Iinverse.multiply(axis), new Vector3(),
				b2.state.Iinverse.multiply(axis.multiply(-1)), new Vector3(), axis, new Vector3(),
				axis.multiply(-1),
				low,
				high,
				null, bvalue );

		
		//keep bodies aligned to the axis
		iterator.next().assign( 
				null, b1, 
				b2, new Vector3(), b1.state.Iinverse.multiply(tt2i), new Vector3(),
				b2.state.Iinverse.multiply(tt2i.multiply(-1)), new Vector3(), tt2i, new Vector3(),
				tt2i.multiply(-1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, tt2i.dot(b1.state.omegaCm)-tt2i.dot(b2.state.omegaCm) - Kcor*tt2i.dot(nerror) );	
		
		iterator.next().assign( 
				null, b1, 
				b2, new Vector3(), b1.state.Iinverse.multiply(tt3i), new Vector3(),
				b2.state.Iinverse.multiply(tt3i.multiply(-1)), new Vector3(), tt3i, new Vector3(),
				tt3i.multiply(-1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				null, tt3i.dot(b1.state.omegaCm)-tt3i.dot(b2.state.omegaCm) - Kcor*tt3i.dot(nerror) );		



	}


}
