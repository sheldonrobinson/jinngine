/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.geometry;

import java.util.Iterator;
import java.util.ListIterator;

import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Transforms;
import jinngine.math.Vector3;
import jinngine.physics.Body;

/**
 * Sphere geometry implementation.
 */
public class Sphere implements SupportMap3, Geometry, Material {

	private Body body;
	private double radius;
	private final Vector3 displacement = new Vector3();
	private final Matrix4 transform4 = new Matrix4();
	private final Matrix4 localtransform4 = new Matrix4();
	private final Vector3 worldMaximumBounds = new Vector3();
	private final Vector3 worldMinimumBounds = new Vector3();
	private final Matrix4 worldTransform = new Matrix4();

	private double envelope = 1;
	private Object auxiliary;
	private double restitution = 0.7;
	private double friction = 0.5;
	private final double mass;
	private final String name;
	
	
	public Sphere(double radius) {
		this.radius = radius;		
		this.mass = (4.0/3.0)*Math.PI*radius*radius*radius;
		this.envelope = 1;
		this.name = new String("");
		//set the initial local transform
		setLocalTransform( Matrix3.identity(), new Vector3());		
	}

	public Sphere(String name, double radius) {
		this.radius = radius;		
		this.mass = (4.0/3.0)*Math.PI*radius*radius*radius;
		this.envelope = 1;
		this.name = new String(name);

		//set the initial local transform
		setLocalTransform( Matrix3.identity(), new Vector3());
	}
	
	public final double getRadius() { return this.radius; }

	@Override
	public Vector3 supportPoint(Vector3 direction, Vector3 result) {
		//sphere is invariant under rotation
		return result.assign(direction.normalize().multiply(radius).add(body.state.position).add(Matrix3.multiply(body.state.rotation, displacement, new Vector3()) ));
	}

	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public double getEnvelope() {
		return envelope;
	}
	
	@Override
	public final Vector3 getMaxBounds(Vector3 bounds) {
		return bounds.assign(worldMaximumBounds);
	}

	private Vector3 getMaxBoundsTmp(Vector3 bounds) {
		//return new Vector3(radius+envelope,radius+envelope,radius+envelope).add(Matrix3.multiply(body.state.rotation, displacement, new Vector3())).add(body.state.rCm);
		return bounds.assign(body.state.position.add( Matrix3.multiply(body.state.rotation, displacement, new Vector3())).add( new Vector3(radius+envelope,radius+envelope,radius+envelope)));
	}

	@Override
	public Vector3 getMinBounds(Vector3 bounds) {
		return bounds.assign(worldMinimumBounds);
	}
	
	private Vector3 getMinBoundsTmp(Vector3 bounds) {
		//return  new Vector3(-radius-envelope,-radius-envelope,-radius-envelope).add(Matrix3.multiply(body.state.rotation, displacement, new Vector3())).add(body.state.rCm);	
		return bounds.assign(body.state.position.add( Matrix3.multiply(body.state.rotation, displacement, new Vector3())).add( new Vector3(-radius-envelope,-radius-envelope,-radius-envelope)));
	}
	
	@Override
	public InertiaMatrix getInertiaMatrix() {
		// inertia tensor for the sphere.
		InertiaMatrix I = new InertiaMatrix();
                I.assignScale((2/5f)*mass*radius*radius);
		return I;
	}

	@Override
	public void setBody(Body b) { this.body = b; }

	@Override
	public void setLocalTransform(Matrix3 B, Vector3 b2) {
		// a sphere only supports translations as local transform
		displacement.assign(b2);
		localtransform4.assign(Transforms.transformAndTranslate4(Matrix3.scaleMatrix(radius), displacement));
	}
	
	@Override
	public void getLocalTranslation(Vector3 t) {
		t.assign(displacement);
	}
	

	@Override
	public Matrix4 getWorldTransform() {
		return new Matrix4(worldTransform);
	}

	@Override
	public void setEnvelope(double envelope) {
		//this.envelope = envelope;
	}

	@Override
	public void supportFeature(Vector3 d, Iterator<Vector3> ret) {
		// sphere is invariant under rotation
		ret.next().assign(d.normalize().multiply(radius).add(body.state.position).add(Matrix3.multiply(body.state.rotation, displacement, new Vector3()) ));
	}

	// material getters and setters
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

	@Override
	public void getLocalTransform(Matrix3 R, Vector3 b) {
		R.assign(Matrix3.identity());
		b.assign(this.displacement);	
	}

	@Override
	public double getMass() {
		return mass;
	}
	
	@Override
	public void setLocalScale(Vector3 s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double sphereSweepRadius() {
		return 0;
	}

	@Override
	public Vector3 getLocalCentreOfMass(Vector3 cm) { 
		return cm.assignZero();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getUserReference() { 
		return auxiliary; 
	}

	@Override
	public void setUserReference(Object auxiliary) {
		this.auxiliary = auxiliary;
	}

	@Override
	public void update() {
		// update world transform
		Matrix4.multiply(body.state.transform, localtransform4, worldTransform);	

        // update world bounding box
		getMaxBoundsTmp(worldMaximumBounds);
		getMinBoundsTmp(worldMinimumBounds);		
	}

}
