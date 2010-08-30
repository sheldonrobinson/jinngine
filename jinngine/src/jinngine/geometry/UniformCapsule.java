/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.geometry;

import java.util.List;
import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.math.Transforms;
import jinngine.physics.Body;

public final class UniformCapsule implements Geometry, SupportMap3, Material {
	// data
	private final double radius;
	private final double length;
	private final InertiaMatrix inertia;
	private final double uniformmass;
	
	/**
	 * A uniform capsule aligned on the z-axis, centred in the origin.
	 * @param radius radius of capsule end spheres
	 * @param length distance between capsule end-points. Note that the total length of the 
	 *        capsule will be length + 2radius
	 */
	public UniformCapsule(double radius, double length) {
		this.radius = radius;
		this.length = length;
				
		// use mass of a cylinder for now
		this.uniformmass = Math.PI*radius*radius*length;
		
//		System.out.println("capsule mass="+uniformmass);

		// angular inertia (for a cylinder for now) TODO 
		final double Ixx = uniformmass*(1/12.0)*(3*radius*radius+length*length);
		this.inertia = new InertiaMatrix();
		this.inertia.assignScale( Ixx, Ixx, 0.5*uniformmass*radius*radius); 
	}
	
	/**
	 * Get the radius value for this capsule
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Get the length of this capsule
	 */
	public double getLength() {
		return length;
	}
	
	/*
	 *  Geometry methods and members
	 */
	
	private Object auxiliary;
	private Body body;
	private double envelope = 0.125;
	private final Matrix3 rotation = new Matrix3(Matrix3.identity());
	private final Vector3 translation = new Vector3();
	
	@Override
	public final Object getAuxiliary() {return this.auxiliary;}
	@Override
	public final void setAuxiliary(Object aux) {this.auxiliary = aux;}
	@Override
	public final Body getBody() {return body;}
	@Override
	public final void setBody(Body b) {this.body = b;}
	@Override
	public final double getEnvelope() {return envelope;}
	@Override
	public final InertiaMatrix getInertialMatrix() { return this.inertia;}
	@Override
	public final void getLocalTransform(Matrix3 R, Vector3 b) {
		R.assign(rotation);
		b.assign(translation);
	}
	@Override
	public final void getLocalTranslation(Vector3 t) { t.assign(translation);}

	@Override
	public final double getMass() { return uniformmass; }

	@Override
	public final Matrix4 getTransform() {
		Matrix4 T =  Transforms.transformAndTranslate4(rotation, translation);
		return body.getTransform().multiply(T);
	}
	@Override
	public final void setEnvelope(double envelope) { this.envelope = envelope; }

	@Override
	public final void setLocalScale(Vector3 s) {
		throw new UnsupportedOperationException("UniformCapsule: no support for scaling");
	}
	@Override
	public final void setLocalTransform(Matrix3 R, Vector3 b) { rotation.assign(R); translation.assign(b); }
	
	/*
	 *  BoundingBox methods
	 */

	@Override
	public Vector3 getMaxBounds() {
		Vector3 p1 = body.state.rotation.multiply(rotation.multiply(new Vector3(0, 0,  0.5*length)).add(translation)).add(body.state.position);
		Vector3 p2 = body.state.rotation.multiply(rotation.multiply(new Vector3(0, 0, -0.5*length)).add(translation)).add(body.state.position);
		return new Vector3((p1.x>p2.x?p1.x:p2.x) + envelope+radius, (p1.y>p2.y?p1.y:p2.y)+ envelope+radius, (p1.z>p2.z?p1.z:p2.z)+envelope+radius );
	}

	@Override
	public Vector3 getMinBounds() {
		Vector3 p1 = body.state.rotation.multiply(rotation.multiply(new Vector3(0, 0,  0.5*length)).add(translation)).add(body.state.position);
		Vector3 p2 = body.state.rotation.multiply(rotation.multiply(new Vector3(0, 0, -0.5*length)).add(translation)).add(body.state.position);
		return new Vector3((p1.x<p2.x?p1.x:p2.x)-envelope-radius, (p1.y<p2.y?p1.y:p2.y)-envelope-radius, (p1.z<p2.z?p1.z:p2.z)-envelope-radius );
	}

	/*  
	 *  SupportMap3 methods
	 */
	
	@Override
	public void supportFeature(Vector3 direction, List<Vector3> face) {
		// calculate a support point in world space
		Vector3 v = body.state.rotation.multiply(rotation).transpose().multiply(direction);
		
		if ( Math.abs(v.z) > 0.5 && false) {
			double sv3 = v.z<0?-0.5:0.5;
			face.add(body.state.rotation.multiply(rotation.multiply(new Vector3(0, 0, sv3*length)).add(translation)).add(body.state.position));
		} else {
			face.add(body.state.rotation.multiply(rotation.multiply(new Vector3(0, 0,  0.5*length)).add(translation)).add(body.state.position));
			face.add(body.state.rotation.multiply(rotation.multiply(new Vector3(0, 0, -0.5*length)).add(translation)).add(body.state.position));			
		}
	}

	@Override
	public Vector3 supportPoint(Vector3 direction) {
		// calculate a support point in world space
		Vector3 v = body.state.rotation.multiply(rotation).transpose().multiply(direction);
		double sv3 = v.z<0?-0.5:0.5;
		return body.state.rotation.multiply(rotation.multiply(new Vector3(0, 0, sv3*length)).add(translation)).add(body.state.position);
	}

	@Override
	public double sphereSweepRadius() {
		return radius;
	}
	
	/*
	 * Material methods
	 */
	
	private double friction = 0.5;
	private double restitution = 0.7;

	@Override
	public double getFrictionCoefficient() { return friction; }
	@Override
	public double getRestitution() { return restitution; }
	@Override
	public void setFrictionCoefficient(double f) { this.friction = f; }
	@Override
	public void setRestitution(double e) { this.restitution = e; }

}
