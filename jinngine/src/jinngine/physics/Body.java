package jinngine.physics;

import java.util.*;

import jinngine.geometry.*;
import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Quaternion;
import jinngine.math.Transforms;
import jinngine.math.Vector3;
import jinngine.physics.solver.*;

/**
 * Abstract class representing a rigid body. This class implements most functionality needed to handle bodies 
 * in a simulation. It is required that new body types inherit from this Body class.  
 * @author mo
 *
 */
public class Body {
	//auxiliary
	public final Vector3               deltaVCm = new Vector3(0,0,0);
	public final Vector3               deltaOmegaCm = new Vector3(0,0,0);
	public final List<ConstraintEntry> constraints = new ArrayList<ConstraintEntry>();
	public final Vector3               auxDeltav = new Vector3();
	public final Vector3               auxDeltaOmega = new Vector3();
	public final Vector3               auxDeltav2 = new Vector3();
	public final Vector3               auxDeltaOmega2 = new Vector3();

	public final State state = new State();
	public double  sleepKinetic = 0;
	public double  sleepyness = 0;
	public boolean sleeping = false;
	public boolean ignore = false;

	private final List<Geometry> geometries = new ArrayList<Geometry>();
	
	//fixed setting
	private boolean isFixed = false;

	//Sleepy
	public boolean sleepy = false;
	private SupportMap3 supportMap = null;
	
	//abstract methods
    //public abstract void updateMomentOfInertia();
	
	//constructors
	public Body() {
		Matrix4.identity(state.transform);
		Matrix3.identity(state.rotation);
		updateTransformations();
		
		this.sleepKinetic = 0;
		this.state.M=0;
		Matrix3.set(new Matrix3(), state.I);
	}
	
	public Body( Geometry g  ) {
		Matrix4.identity(state.transform);
		Matrix3.identity(state.rotation);
		updateTransformations();
		
		this.sleepKinetic = 0;
		
		//some default properties
		this.state.M=1;
		Matrix3.set(new Matrix3(), state.I);
		Matrix3.set(new Matrix3(), state.Iinverse);
				
		addGeometry(g);
		
		//complete
		finalize();
	}
	
	public Body( Iterator<Geometry> i) {
		Matrix4.identity(state.transform);
		Matrix3.identity(state.rotation);
		updateTransformations();
		
		this.sleepKinetic = 0;
		
		//some default properties
		this.state.M=1;
		Matrix3.set(new Matrix3(), state.I);
		Matrix3.set(new Matrix3(), state.Iinverse);
				
		while (i.hasNext()) {
			addGeometry(i.next());
		}
		
		//complete
		finalize();
	}
		
	/**
	 * Add a geometry to this body
	 * @param g
	 * @param R
	 * @param r
	 * @param mass
	 */
	public void addGeometry( Geometry g ) {
		geometries.add(g);
	}

	public void finalize() {
		final Vector3 cm = new Vector3();
		
		//reset body properties
		this.state.M=0;
		Matrix3.set(new Matrix3(), state.I);
		Matrix3.set(new Matrix3(), state.Iinverse);

		if ( geometries.size() > 0 ) {

			//find center of mass
			cm.assignZero();
			double totalMass = 0;

			for (Geometry g: geometries) {
				g.setBody(this);
				
				totalMass += g.getMass();
				
				//get the transformation
				Matrix3 R = new Matrix3();
				Vector3 b = new Vector3();
				g.getLocalTransform(R, b);
				
				// cm = cm + b*M
				cm.assign( cm.add( b.multiply(g.getMass())));
			}
			
			// cm = cm / total mass
			cm.assign( cm.multiply(1/totalMass));
			this.state.M = totalMass;

			//translate all geometries so centre of mass will become the origin
			for (Geometry g: geometries) {

				//get the transformation
				Matrix3 R = new Matrix3();
				Vector3 b = new Vector3();
				g.getLocalTransform(R, b);
				
				//align to centre of mass
				b.assign( b.minus(cm));

				//rotate the inertia matrix into this frame and add it to the inertia tensor of this body
				Matrix3 Im = InertiaMatrix.rotate(g.getInertialMatrix(), R).translate(g.getMass(), b);
				Matrix3.add(this.state.I, Im, this.state.I);

				//set the final transform
				g.setLocalTransform(R, b);

			}

			//fill out the invers tensor
			Matrix3.inverse(this.state.I, this.state.Iinverse);

		} else {
			//fall-back on something, in case no geometries were given
			this.state.M = 1;
			//this.state.I.assign(InertiaMatrix.identity());
			//this.state.Iinverse.identity();
		}
	}

	

	public Iterator<Geometry> getGeometries() {
		return geometries.iterator();
	}
	
	public final void applyImpulseToMomentums(Vector3 r, Vector3 J) {
		//                P
		// P = Mv => v = ---
		//                M

		//I-1  Lcm = I-1(r x F) = omega

		if (!isFixed()) {
			Vector3.add(this.state.P, J);
			Vector3.add(this.state.L, r.cross(J));

			//recalculate linear and angular velocities
			Matrix3.multiply(this.state.Iinverse, state.L, this.state.omegaCm);
			this.state.vCm.assign(state.P.multiply(1/this.state.M));
		}
	}
	
	public void updateMomentums() {
		Matrix3.multiply(state.I, state.omegaCm, state.L);
		state.P.assign(state.vCm.multiply(state.M));
	}

	public final boolean isFixed() {
		return isFixed;
	}

	public void setFixed( boolean value){
		isFixed = value;
	}

	public final void setVelocity( Vector3 v ) {
		state.vCm.assign(v);

		//Recalculate linear momentum
		state.P.assign( this.state.vCm.multiply(this.state.M));
	}

	public final Vector3 getVelocity() {
		return new Vector3(state.vCm);
	}


	public final void setPosition( Vector3 r ) {
		state.rCm.assign(r);
		updateTransformations();
	}

	public final void updateTransformations() {
		//set identity transforms
		Matrix3.identity(state.rotation);
		Matrix4.identity(state.transform);

		Matrix3.multiply(state.q.rotationMatrix3(), state.rotation, state.rotation);

		//affine transform
		Matrix4.multiply(Transforms.rotateAndTranslate4( state.q, state.rCm), state.transform, state.transform);

		//inverse rotations (for normals)
		Matrix3.inverse(state.rotation, state.rotationInverse);
	}
	
	public final Matrix4 getTransform() {
		return state.transform;
	}

	public final void setAngularVelocity( Vector3 omega ) {
		state.omegaCm.assign(omega);
		//recalculate the angular momentum
		Matrix3.multiply(state.I, state.omegaCm, state.L);

	}

	public final Vector3 getAngularVelocity() {
		return new Vector3(state.omegaCm);
	}

//	public final void setMass( double mass ) {
//		this.state.M = mass;
//
//		//Redefine I, as it is dependent on the mass
//		updateMomentOfInertia();
//	}

	public final double getMass() {
		return this.state.M;
	}

	public final void clearForces() {
		this.state.FCm.assign(new Vector3(0,0,0));
		this.state.tauCm.assign(new Vector3(0,0,0));	  
	}

	public final void applyForce( Vector3 delta_ri, Vector3 F ) {
		Vector3.add(this.state.tauCm, delta_ri.cross(F));
		//Vector3.add(this.a_cm, F.multiply(1/this.mass));

		Vector3.add(this.state.FCm, F );	  
		Vector3.multiply(this.state.FCm, 1.0/this.state.M, this.state.aCm );
	}

	public final double totalKinetic() {
		double eKin;
		Vector3 res = new Vector3();

		//Calculate the rotational kinetic energy
		// T = (1/2) omega * I * omega,
		Matrix3.multiply(state.Iinverse, state.L, state.omegaCm);
		res  = Matrix3.transposeVectorAndMultiply( state.omegaCm, state.I , res);
		eKin = res.dot( state.omegaCm )*0.5f;

		//Translational energy E = (1/2)*v*m^2
		Vector3.multiply(this.state.P, 1.0/this.state.M, this.state.vCm );
		eKin += state.vCm.dot(state.vCm)*state.M*0.5f;

		return Math.abs(eKin);
	}

	public final double totalPotential() {
		return 0;	  
	}


	public final void advanceVelocities( double dt ) {
		//take one explicit euler-step, and integrate forward on the linear momentum
		Vector3.add(this.state.P, this.state.FCm.multiply(dt));
		Vector3.multiply(this.state.P, 1.0/this.state.M, this.state.vCm );

		//Integrate the angular momentum forward taking an explicit euler step
		//and calculate the angular velocity from the angular momentum
		Vector3.add(state.L, state.tauCm.multiply(dt));    
		Matrix3.multiply(state.Iinverse, state.L, state.omegaCm);
	}

	public final void advancePositions( double dt) {
		//forward in time
		//timestamp++;

		// Runge-Kutta midpoint method
		//
		//                             h^2
		// r(ti+1) = r(ti) + hr'(ti) + --- r''(ti) + O(h^3), where h is the step-size   (1)
		//                              2

		// h( v+ (h/2)a )
		
		//if ( v_cm.Length() * dt  > 1e-9  ) 
		//Vector3.add(state.r_cm, state.v_cm.multiply(dt).add( state.a_cm.multiply(dt*dt/2.0)) );

//		Explicit euler 
		Vector3.add(state.rCm, state.vCm.multiply(dt)  );

		//single explicit euler step
		//       //rotation, q'= 0.5*[0,omega_cm]*q
		//       dq_dt.Assign(0.0, omega_cm.Multiply(0.5));

		//       //dq_dt = dq_dt.Multiply( q ); 
		//       Quaternion.sMultiply( dq_dt, q );

		//       q = q.Add( dq_dt.Multiply(dt)  );
		//       q.Normalize();
		//       q.toRotationMatrix(R);

		// Runge-Kutta midpoint method
		//
		//                             h^2
		// r(ti+1) = r(ti) + hr'(ti) + --- r''(ti) + O(h^3), where h is the step-size   (1)
		//                              2
		//Quaternion d2q_dt2 = new Quaternion(); 
		state.Dq.assign(0.0f, state.omegaCm.multiply(0.5f));
		Quaternion.sMultiply( state.Dq, state.q );

		//  dq     1
		//  -- =   - [0,omega]*q
		//  dt     2
		//
		//  d2q    1          1            1                1         1
		//  --- =  -[0,a]*q + -[0,omega] ( - [0,omega]q ) = -[0,a]q + -[0,omega]^2
		//  dt2    4          2            2                2         4

		//For now, we set the change in angular velocity to zero, TODO
		state.D2q.assign( 0.0f, state.omegaCm.multiply(0.5f) );
		Quaternion.sMultiply( state.D2q, state.Dq );

		//inserting into (1)
		//Quaternion change  = state.Dq.multiply(dt).add(state.D2q.multiply((dt*dt)/2.0f));
		//Quaternion change  = dq_dt.Multiply(dt);

		//state.q = state.q.add(change);
		//Quaternion.add( state.q, (state.Dq.multiply(dt).add(state.D2q.multiply((dt*dt)/2.0f))));

		Quaternion.add( state.q, (state.Dq.multiply(dt) ));

		//apply to body
		state.q.normalize();    
		// q.toRotationMatrix(R);  
		
		updateTransformations();
	}
	
	//Go from world to model
	public final Vector3 toModel( final Vector3 v) {
		Vector3 vmark = v.copy();
		Vector3.sub( vmark, state.rCm );
		Quaternion cq = state.q.conjugate();
		Quaternion.applyRotation( cq, vmark );
		return vmark;    
	}

	//Go from world to model without translating
	public final Vector3 toModelNoTranslation( final Vector3 v) {
		return Matrix3.multiply(state.rotationInverse, v, new Vector3() );
	}
	
	//Go to world coordinates from model coordinates
	public final Vector3 toWorld( final Vector3 v) {
		//apply complete transform
		return  Matrix4.multiply(state.transform, v, new Vector3());	
	}

	//Go from model to rotated model
	public final Vector3 toWorldNoTranslation(final Vector3 v) {
		return Matrix3.multiply(this.state.rotation, v, new Vector3());
	}

	//translate (no local rotation) 
	public final Vector3 translate( final Vector3 v) {
		return v.add(state.rCm);
	}

	public SupportMap3 getSupportMap() {
		return supportMap;
	}
	public void setSupportMap(SupportMap3 supportMap) {
		this.supportMap = supportMap;
	}
}  

