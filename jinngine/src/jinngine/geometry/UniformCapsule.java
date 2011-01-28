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

public final class UniformCapsule implements Geometry, SupportMap3, Material {
    // data
    private final double radius;
    private final double length;
    private final InertiaMatrix inertia;
    private final double uniformmass;
    private final String name;

    /**
     * A uniform capsule aligned on the z-axis, centred in the origin.
     * 
     * @param radius
     *            radius of capsule end spheres
     * @param length
     *            distance between capsule end-points. Note that the total
     *            length of the capsule will be length + 2radius
     */
    public UniformCapsule(final String name, final double radius, final double length) {
        this.name = new String(name);
        this.radius = radius;
        this.length = length;

        // use mass of a cylinder plus mass of the sphere
        final double cylinderMass = Math.PI * radius * radius * length;
        final double sphereMass = 4.0 / 3.0 * Math.PI * radius * radius * radius;
        uniformmass = cylinderMass + sphereMass;

        // angular inertia for cylinder part (scale in cylinders part of total mass)
        final double Ixx = cylinderMass / uniformmass * 1.0 / 12.0 * (3 * radius * radius + length * length);
        inertia = new InertiaMatrix();
        inertia.assignScale(Ixx, Ixx, 0.5 * radius * radius);

        // add the contribution from the two half-sphere at the ends
        // inertia for half-sphere in (0,0,0) frame (not in centre of mass frame)
        final double IHalfSphere = 1.0 / 5.0 * radius * radius;
        // the following is equivalent to translating two half-sphere tensors to
        // each side along the z-axis, there after adding them to the inertia of the cylinder
        inertia.a11 += 2 * sphereMass / uniformmass * (IHalfSphere + 0.25 * length * length);
        inertia.a22 += 2 * sphereMass / uniformmass * (IHalfSphere + 0.25 * length * length);

        // centre of mass for half-sphere in positive z space
        // final double halfSphereCmZ = (3.0/16.0)*radius;
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
     * Geometry methods and members
     */

    private Object auxiliary;
    private Body body;
    private double envelope = 0.225;
    private final Matrix3 rotation = new Matrix3(Matrix3.identity());
    private final Vector3 translation = new Vector3();
    private final Vector3 worldMaximumBounds = new Vector3();
    private final Vector3 worldMinimumBounds = new Vector3();
    private final Matrix4 worldTransform = new Matrix4();

    @Override
    public final Object getUserReference() {
        return auxiliary;
    }

    @Override
    public final void setUserReference(final Object aux) {
        auxiliary = aux;
    }

    @Override
    public final Body getBody() {
        return body;
    }

    @Override
    public final void setBody(final Body b) {
        body = b;
    }

    @Override
    public final double getEnvelope() {
        return envelope;
    }

    @Override
    public final InertiaMatrix getInertiaMatrix() {
        return inertia;
    }

    @Override
    public final void getLocalTransform(final Matrix3 R, final Vector3 b) {
        R.assign(rotation);
        b.assign(translation);
    }

    @Override
    public final void getLocalTranslation(final Vector3 t) {
        t.assign(translation);
    }

    @Override
    public final double getMass() {
        return uniformmass;
    }

    @Override
    public final Matrix4 getWorldTransform() {
        final Matrix4 T = Transforms.transformAndTranslate4(rotation, translation);
        return body.getTransform().multiply(T);
        // return new Matrix4(worldTransform);
    }

    @Override
    public final void setEnvelope(final double envelope) {
        this.envelope = envelope;
    }

    @Override
    public final void setLocalScale(final Vector3 s) {
        throw new UnsupportedOperationException("UniformCapsule: no support for scaling");
    }

    @Override
    public final void setLocalTransform(final Matrix3 R, final Vector3 b) {
        rotation.assign(R);
        translation.assign(b);
    }

    /*
     * BoundingBox methods
     */
    @Override
    public final Vector3 getMaxBounds(final Vector3 bounds) {
        return bounds.assign(worldMaximumBounds);
    }

    private final Vector3 getMaxBoundsTmp(final Vector3 bounds) {
        final Vector3 p1 = body.state.rotation.multiply(
                rotation.multiply(new Vector3(0, 0, 0.5 * length)).add(translation)).add(body.state.position);
        final Vector3 p2 = body.state.rotation.multiply(
                rotation.multiply(new Vector3(0, 0, -0.5 * length)).add(translation)).add(body.state.position);
        return bounds.assign((p1.x > p2.x ? p1.x : p2.x) + envelope + radius, (p1.y > p2.y ? p1.y : p2.y) + envelope
                + radius, (p1.z > p2.z ? p1.z : p2.z) + envelope + radius);
    }

    @Override
    public Vector3 getMinBounds(final Vector3 bounds) {
        return bounds.assign(worldMinimumBounds);
    }

    private final Vector3 getMinBoundsTmp(final Vector3 bounds) {
        final Vector3 p1 = body.state.rotation.multiply(
                rotation.multiply(new Vector3(0, 0, 0.5 * length)).add(translation)).add(body.state.position);
        final Vector3 p2 = body.state.rotation.multiply(
                rotation.multiply(new Vector3(0, 0, -0.5 * length)).add(translation)).add(body.state.position);
        return bounds.assign((p1.x < p2.x ? p1.x : p2.x) - envelope - radius, (p1.y < p2.y ? p1.y : p2.y) - envelope
                - radius, (p1.z < p2.z ? p1.z : p2.z) - envelope - radius);
    }

    /*
     * SupportMap3 methods
     */

    @Override
    public void supportFeature(final Vector3 direction, final Iterator<Vector3> face) {
        // calculate a support point in world space
        final Vector3 v = body.state.rotation.multiply(rotation).transpose().multiply(direction);

        if (Math.abs(v.z) > 0.5 && false) {
            final double sv3 = v.z < 0 ? -0.5 : 0.5;
            face.next().assign(
                    body.state.rotation.multiply(rotation.multiply(new Vector3(0, 0, sv3 * length)).add(translation))
                            .add(body.state.position));
        } else {
            face.next().assign(
                    body.state.rotation.multiply(rotation.multiply(new Vector3(0, 0, 0.5 * length)).add(translation))
                            .add(body.state.position));
            face.next().assign(
                    body.state.rotation.multiply(rotation.multiply(new Vector3(0, 0, -0.5 * length)).add(translation))
                            .add(body.state.position));
        }
    }

    @Override
    public Vector3 supportPoint(final Vector3 direction, final Vector3 result) {
        // calculate a support point in world space
        final Vector3 v = body.state.rotation.multiply(rotation).transpose().multiply(direction);
        final double sv3 = v.z < 0 ? -0.5 : 0.5;
        return result.assign(body.state.rotation.multiply(
                rotation.multiply(new Vector3(0, 0, sv3 * length)).add(translation)).add(body.state.position));
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
    private double correction = 2;

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
    public Vector3 getLocalCentreOfMass(final Vector3 cm) {
        return cm.assignZero();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void update() {
        // System.out.println("Capsule update");

        // update world transform
        Matrix4.multiply(body.getTransform(), Transforms.transformAndTranslate4(rotation, translation), worldTransform);

        // update world bounding box
        getMaxBoundsTmp(worldMaximumBounds);
        getMinBoundsTmp(worldMinimumBounds);
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
