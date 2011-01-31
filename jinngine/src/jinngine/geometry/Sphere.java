/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.geometry;

import java.util.Iterator;

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
    private final double radius;
    private final Vector3 translation = new Vector3();
    private final Matrix3 rotation = new Matrix3();
    private final Matrix4 transform4 = new Matrix4();
    private final Vector3 worldposition = new Vector3();

    private final double envelope = 0.125;
    private Object auxiliary;
    private double restitution = 0.7;
    private double friction = 0.5;
    private final double mass;
    private final String name;
    private double correction = 2;

    public Sphere(final double radius) {
        this.radius = radius;
        mass = 4.0 / 3.0 * Math.PI * radius * radius * radius;
        name = new String("Give me a name!");
        // set the initial local transform
        setLocalTransform(Matrix3.identity(), new Vector3());
    }

    public Sphere(final String name, final double radius) {
        this.radius = radius;
        mass = 4.0 / 3.0 * Math.PI * radius * radius * radius;
        this.name = new String(name);

        // set the initial local transform
        setLocalTransform(Matrix3.identity(), new Vector3());
    }

    public final double getRadius() {
        return radius;
    }

    @Override
    public Vector3 supportPoint(final Vector3 direction, final Vector3 result) {
        // sphere is invariant under rotation
        // return
        // result.assign(direction.normalize().multiply(radius).add(body.state.position).add(Matrix3.multiply(body.state.rotation,
        // displacement, new Vector3()) ));

        // result = Rp + position
        result.assign(translation);
        Matrix3.multiply(body.state.rotation, result, result);
        return result.assignAdd(body.state.position);
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
    public final Vector3 getMaxBounds(final Vector3 bounds) {
        bounds.assign(worldposition);
        return bounds.assignAdd(radius + envelope, radius + envelope, radius + envelope);
    }

    @Override
    public Vector3 getMinBounds(final Vector3 bounds) {
        bounds.assign(worldposition);
        return bounds.assignAdd(-radius - envelope, -radius - envelope, -radius - envelope);
    }

    @Override
    public InertiaMatrix getInertiaMatrix() {
        // inertia tensor for the sphere.
        final InertiaMatrix I = new InertiaMatrix();
        I.assignScale(2 / 5f * radius * radius);
        return I;
    }

    @Override
    public void setBody(final Body b) {
        body = b;
    }

    @Override
    public void setLocalTransform(final Matrix3 B, final Vector3 b2) {
        // a sphere only supports translations as local transform
        translation.assign(b2);
        rotation.assign(B);
    }

    @Override
    public void getLocalTranslation(final Vector3 t) {
        t.assign(translation);
    }

    @Override
    public Matrix4 getWorldTransform() {
        return transform4;
    }

    @Override
    public void setEnvelope(final double envelope) {
        // this.envelope = envelope;
    }

    @Override
    public void supportFeature(final Vector3 d, final Iterator<Vector3> ret) {
        // sphere is invariant under rotation
        final Vector3 result = ret.next();

        // result = Rp + position
        result.assign(translation);
        Matrix3.multiply(body.state.rotation, result, result);
        result.assignAdd(body.state.position);
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
    public void setFrictionCoefficient(final double f) {
        friction = f;
    }

    @Override
    public void setRestitution(final double e) {
        restitution = e;

    }

    @Override
    public void getLocalTransform(final Matrix3 R, final Vector3 b) {
        R.assign(rotation);
        b.assign(translation);
    }

    @Override
    public double getMass() {
        return mass;
    }

    @Override
    public void setLocalScale(final Vector3 s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double sphereSweepRadius() {
        return radius;
    }

    @Override
    public Vector3 getLocalCentreOfMass(final Vector3 cm) {
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
    public void setUserReference(final Object auxiliary) {
        this.auxiliary = auxiliary;
    }

    @Override
    public void update() {
        // update world transform
        Matrix4.multiply(body.getTransform(), Transforms.transformAndTranslate4(rotation, translation), transform4);

        // update world position point
        Matrix3.multiply(body.state.rotation, translation, worldposition);
        worldposition.assignAdd(body.state.position);
    }

    @Override
    public double getCorrectionVelocityLimit() {
        return correction;
    }

    @Override
    public void setCorrectionVelocityLimit(final double limit) {
        correction = limit;
    }

}
