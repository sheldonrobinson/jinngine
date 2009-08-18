package jinngine.geometry;

import java.util.ArrayList;
import java.util.List;

import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Transforms;
import jinngine.math.Vector3;
import jinngine.physics.Body;

/**
 * Sphere geometry. This Geometry implementation is invariant to applied transforms. One can only affect
 * it by using the method setRadius() 
 * @author mo
 *
 */
public class Sphere implements SupportMap3, Geometry, Material {


	public Object getAuxiliary() {
		return auxiliary;
	}

	public void setAuxiliary(Object auxiliary) {
		this.auxiliary = auxiliary;
	}

	private Body body;
	private double radius;
	private final Matrix3 localtransform = new Matrix3();
	private final Vector3 displacement = new Vector3();
	private final Matrix4 transform4 = new Matrix4();
	private final Matrix4 localtransform4 = new Matrix4();
	private double envelope = 0;
	private Object auxiliary;
	private double restitution = 0.7;
	private double friction = 0.5;


	

	public Sphere(Body body, double radius, Vector3 displacement) {
		super();
		this.body = body;
		this.radius = radius;
		this.displacement.assign(displacement);
		
		//set the initial local transform
		setLocalTransform( Matrix3.identity(new Matrix3()), displacement);
	}
	
	/**
	 * Set sphere radius
	 * @param radius
	 */
	public void setRadius( double radius ) {
		this.radius = radius;
	}
	
	public double getRadius() {
		return this.radius;
	}

	@Override
	public Vector3 supportPoint(Vector3 direction) {
		//sphere is invariant under rotation
		return direction.normalize().multiply(radius).add(body.state.rCm).add(Matrix3.multiply(body.state.rotation, displacement, new Vector3()) );
	}

	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public double getEnvelope(double dt) {
		if (envelope > 0) {
			return envelope;
		}
		//TODO just a static value for now, should vary in radius
		return 0.5;
	}
	
	@Override
	public Vector3 getMaxBounds() {
		return new Vector3(radius+envelope,radius+envelope,radius+envelope).add(displacement).add(body.state.rCm);
	}

	@Override
	public Vector3 getMinBounds() {
		return  new Vector3(-radius-envelope,-radius-envelope,-radius-envelope).add(displacement).add(body.state.rCm);	
	}
	
	@Override
	public InertiaMatrix getInertialMatrix(double mass) {
		double r = radius;
		InertiaMatrix I = new InertiaMatrix();

		//Inertia tensor for the sphere. 
		Matrix3.set( I,
				(2/5f)*mass*r*r, 0.0f, 0.0f,
				0.0f, (2/5f)*mass*r*r, 0.0f,
				0.0f, 0.0f, (2/5f)*mass*r*r );

		return I;
	}

	@Override
	public void setBody(Body b) {
		this.body = b;
		
	}

	@Override
	public void setLocalTransform(Matrix3 B, Vector3 b2) {
		//A sphere only supports translations as local transform
		displacement.assign(b2);
		Matrix4.set(Transforms.transformAndTranslate4(Matrix3.identity(new Matrix3()).multiply(radius), displacement), localtransform4);

	}
	
	@Override
	public void setLocalTranslation(Vector3 b) {
		displacement.assign(b);
	}

	@Override
	public Matrix4 getTransform() {
		return Matrix4.multiply(body.state.transform, localtransform4, transform4);	
	}

	@Override
	public void setEnvelope(double envelope) {
		this.envelope = envelope;
	}

	@Override
	public void supportFeature(Vector3 d, double epsilon, List<Vector3> ret) {
		//sphere is invariant under rotation
		ret.add(d.normalize().multiply(radius).add(body.state.rCm).add(Matrix3.multiply(body.state.rotation, displacement, new Vector3()) ));
	}

	//Material getters and setters
	@Override
	public double getFrictionCoefficient() {
		return friction;
	}

	@Override
	public double getRestitution() {
		return restitution;
	}

	@Override
	public void setFrictionCoefficient(double f) {
		this.friction = f;
	}

	@Override
	public void setRestitution(double e) {
		this.restitution = e;
		
	}

}
