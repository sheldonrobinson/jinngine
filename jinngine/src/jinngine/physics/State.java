package jinngine.physics;

import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;

/**
 * Physical state structure for a rigid body. Encapsulates physical quantities such as linear and angular 
 * velocities, intertia tensor, and transformation matrices. 
 */
public final class State  {
	//generalized position
	/** Position relative to center of mass */
	final public Vector3             rCm       = new Vector3(0,0,0);
	/** Quaternion describing orientation */
	public final Quaternion          q         = Quaternion.rotation(0.0f, new Vector3(1,0,0));  

	//Angular inertia
	/** Moment of inertia */
	public final InertiaMatrix       I         = new InertiaMatrix();
	/** Moment of inertia inverse */
	public final Matrix3             Iinverse  = new Matrix3();
	/** Total mass */
	public double                    M         = 1;                                         

	/** Angular momentum */
	public final Vector3             L       = new Vector3(0,0,0);
	/** Linear momentum */
	public final Vector3             P         = new Vector3(0,0,0);                        

	//Time derivatives
	/** Center of mass velocity */
	public final Vector3             vCm       = new Vector3(0,0,0);
	/** Angular Velocity ( Radians per time unit around each axis) */
	public final Vector3             omegaCm   = new Vector3(0.0f,0.0f,0.0f);
	/** Quaternion first order time derivative  */
	public final Quaternion          Dq        = new Quaternion( 0.0f, new Vector3(0,0,0));   
	/** Quaternion second order time derivative  */
	public final Quaternion          D2q       = new Quaternion( 0.0f, new Vector3(0,0,0)); // Quaternion time double derivative dq/dt = (1/2)*omega_cm*q
	/** Linear acceleration */
	final public Vector3             aCm       = new Vector3(0,0,0);                          // Linear acceleration
	/** Angular acceleration */
	public final Vector3             alpha     = new Vector3(0,0,0);                          // Angular acceleration 
	/** total torque, dL/dt, change in angular momentum */
	public final Vector3             tauCm     = new Vector3(0,0,0);                          // total torque, dL/dt change in angular momentum
	/** total force, change in linear momentum dP/dt */
	public final Vector3             FCm       = new Vector3(0,0,0);                          // total force, change in linear momentum dP/dt

	//Auxiliary
	/**
	 * Auxiliary fields
	 */
	public double                    rMax;
	public final Vector3             deltaVCm     = new Vector3(0,0,0);
	public final Vector3             deltaOmegaCm = new Vector3(0,0,0);

	//transforms
	/** Transformation matrix, how to get from object space to world */
	public final Matrix4             transform       = new Matrix4();
	/** Rotation matrix, how to get from object orientation into world orientation */
	public final Matrix3             rotation        = new Matrix3().identity();
	/** Inverse rotation matrix */	
	public final Matrix3             rotationInverse = new Matrix3().identity();

	/**
	 * Assign this state the fields in the State t
	 * @param t State to be copied
	 */
	public void  assign( State t) {
		State s = this;
		s.rCm.assign(t.rCm);
		s.q.assign(t.q);
		s.omegaCm.assign(t.omegaCm);
		Matrix3.set(t.I, s.I);
		Matrix3.set(t.Iinverse, s.Iinverse);
		s.L.assign(t.L);
		s.P.assign(t.P);
		s.M = t.M;

		s.vCm.assign(t.vCm);
		s.aCm.assign(t.aCm);
		s.alpha.assign(t.alpha);
		s.tauCm.assign(t.tauCm);
		s.FCm.assign(t.FCm);
		
		Matrix4.set(t.transform, s.transform);
		Matrix3.set(t.rotation, s.rotation);
		Matrix3.set(t.rotationInverse, s.rotationInverse);
	}
}

