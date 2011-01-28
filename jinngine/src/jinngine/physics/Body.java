/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.physics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jinngine.geometry.Geometry;
import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Transforms;
import jinngine.math.Vector3;

/**
 * A body is a rigid body entity, that is animated according to the laws of Newton. A body can have one or more geometry
 * instances attached, and these define its final mass and inertia properties.
 */
public final class Body {
    // global public name
    public final String identifier;

    // delta velocities. these vectors contains the time integrated force
    // contributions for the current time-step. The first two is the internal
    // forces computed by the constraint solver. The later two is the contributions
    // from external forces.
    public final Vector3 deltavelocity = new Vector3();
    public final Vector3 deltaomega = new Vector3();
    public final Vector3 externaldeltavelocity = new Vector3();
    public final Vector3 externaldeltaomega = new Vector3();

    // more auxiliary members
    public final Vector3 deltavelocity1 = new Vector3();
    public final Vector3 deltaomega1 = new Vector3();
    public final Vector3 deltavelocity2 = new Vector3();
    public final Vector3 deltaomega2 = new Vector3();

    // physical state of body
    public final State state = new State();

    // list of attached geometries
    private final List<Geometry> geometries = new ArrayList<Geometry>();

    // fixed setting
    private boolean fixed = false;

    /**
     * Create a now body with no geometry
     * 
     * @param identifier
     *            Unique identifier for this body
     */
    public Body(final String identifier) {
        this.identifier = identifier;
        state.transform.assignIdentity();
        state.rotation.assignIdentity();
        updateTransformations();
        state.anisotropicmass.assignZero();
        state.inverseanisotropicmass.assignZero();
        state.inertia.assignZero();
        state.inverseinertia.assignZero();
    }

    /**
     * Add a geometry to this body. The mass properties of the body is updated, and the body space transformations for
     * the new geometry, as well as existing ones, are updated. After the geometry is added, it will be in the world
     * space transform specified, but the world position of the body may have changed.
     * 
     * @param rotation
     *            rotation of geometry in world space
     * @param translation
     *            translation of geometry in world space
     * @param g
     *            geometry to be added
     */
    public void addGeometry(final Matrix3 rotation, final Vector3 translation, final Geometry g) {
        // get the previous mass
        final double previousTotalMass = state.anisotropicmass.diag().infnorm(); // mass at (0,0,0)

        // System.out.println("local centre of mass: "+g.getLocalCentreOfMass(new Vector3()));

        // the local centre of mass displacement in the geometry, transformed to body space
        final Vector3 localCentreOfMassDisplacementBody = state.inverserotation.multiply(rotation).multiply(
                g.getLocalCentreOfMass(new Vector3()));

        // the centre of mass point for the new geometry in body space
        final Vector3 localTranslationBody = state.inverserotation.multiply(translation.sub(state.position));
        final Vector3 localCentreOfMassPositionBody = localTranslationBody.add(localCentreOfMassDisplacementBody);

        // find the new centre of mass displacement for the resulting body
        // point = p1*m1/(m1+m2) + p2*m2/(m1+m2)
        final double localMass = g.getMass();
        final double totalMass = previousTotalMass + localMass;
        final Vector3 totalCentreOfMassDisplacementBody = localCentreOfMassPositionBody.multiply(localMass / totalMass);

        // move body to the new centre of mass point in world space
        Vector3.add(state.position, state.rotation.multiply(totalCentreOfMassDisplacementBody));

        // System.out.println("new body position=" +state.position);
        // System.out.println("cm displacement=" +state.rotation.multiply(totalCentreOfMassDisplacementBody));

        // translate the current inertia tensor the reverse direction
        // of the new centre of mass displacement (inertia tensor is already scaled to the right mass)
        InertiaMatrix.translate(state.inertia, previousTotalMass, totalCentreOfMassDisplacementBody.negate());

        // add the new contribution from the new inertia tensor
        final InertiaMatrix Inew = g.getInertiaMatrix();

        // System.out.println("tenser from body = \n" + Inew);

        // scale inertia to mass
        Inew.assignMultiply(localMass);
        // rotate the new tensor into body frame
        InertiaMatrix.rotate(Inew, state.inverserotation.multiply(rotation));
        // translate to new position relative to cm
        InertiaMatrix.translate(Inew, localMass, localCentreOfMassPositionBody.sub(totalCentreOfMassDisplacementBody));

        // System.out.println("amount of tensor translation= "+localCentreOfMassPositionBody.sub(totalCentreOfMassDisplacementBody)
        // );
        // add to body tensor
        state.inertia.assignAdd(Inew);

        // System.out.println(""+state.inertia);

        // add the new mass to the total mass
        state.anisotropicmass.assignScale(previousTotalMass + localMass);

        // compute mass inverses
        state.inverseanisotropicmass.assignScale(1 / (previousTotalMass + localMass));
        Matrix3.inverse(state.inertia, state.inverseinertia);

        // go through existing geometries and translate them
        for (final Geometry gi : geometries) {
            final Matrix3 R = new Matrix3();
            final Vector3 b = new Vector3();
            gi.getLocalTransform(R, b);
            gi.setLocalTransform(R, b.sub(totalCentreOfMassDisplacementBody));
        }

        // assign local transform to new geometry
        g.setLocalTransform(
                state.inverserotation.multiply(rotation),
                localCentreOfMassPositionBody.sub(totalCentreOfMassDisplacementBody).sub(
                        localCentreOfMassDisplacementBody));

        // System.out.println("local translation = "+localCentreOfMassPositionBody.sub(totalCentreOfMassDisplacementBody).add(localCentreOfMassDisplacementBody));
        // System.out.println("local centre of mass displacement = "+localCentreOfMassDisplacementBody);

        // assign this body to g
        g.setBody(this);

        // attach the new geometry
        geometries.add(g);
    }

    public void removeGeometry(final Geometry g) {
        // make sure this geometry exist here
        if (!geometries.remove(g)) {
            throw new IllegalArgumentException("Attempt to delete geometry from Body that does not exist");
        }

        // trivial case if there are no more geometries left
        if (geometries.isEmpty()) {
            // reset physical properties
            state.anisotropicmass.assignZero();
            state.inverseanisotropicmass.assignZero();
            state.inertia.assignZero();
            state.inverseinertia.assignZero();
            return;
        }

        // we need to remove this geometry from the mass properties
        final Matrix3 localRotation = new Matrix3();
        final Vector3 localTranslation = new Vector3();
        final Vector3 localCm = new Vector3();
        final double localMass = g.getMass();
        g.getLocalTransform(localRotation, localTranslation);
        g.getLocalCentreOfMass(localCm);

        // calculate the the geometry centre of mass position in
        // body space
        final Vector3 geometryCmBody = localTranslation.add(localRotation.multiply(localCm));

        // get the local mass of geometry
        final double geometryMass = g.getMass();

        // get the total current mass
        final double currentTotalMass = state.anisotropicmass.diag().infnorm(); // mass at (0,0,0)

        // calculate the old mass centre, which would have been
        // before this geometry was added

        final Vector3 displ = geometryCmBody.multiply(-geometryMass / (currentTotalMass - geometryMass));

        // go through existing geometries and translate them
        for (final Geometry gi : geometries) {
            final Matrix3 R = new Matrix3();
            final Vector3 b = new Vector3();
            gi.getLocalTransform(R, b);
            gi.setLocalTransform(R, b.sub(displ));
        }

        // translate body to compensate the change
        Vector3.add(state.position, state.rotation.multiply(displ));

        // update the mass
        state.anisotropicmass.assign(Matrix3.identity().multiply(currentTotalMass - geometryMass));
        state.inverseanisotropicmass.assignScale(1 / (currentTotalMass - geometryMass));

        // now, we need to update the inertia tensor.
        // first remove the contribution of the removed geometry
        final InertiaMatrix Ig = g.getInertiaMatrix();
        // scale inertia to mass
        Ig.assignMultiply(localMass);
        // rotate the new tensor into body frame
        InertiaMatrix.rotate(Ig, localRotation);
        // translate to new position relative to cm
        InertiaMatrix.translate(Ig, localMass, localTranslation);

        // remove the contribution of Ig
        Matrix3.subtract(state.inertia, Ig, state.inertia);

        // now, the contribution from moving the rest of the body back to
        // the new centre of mass must be removed
        InertiaMatrix.inverseTranslate(state.inertia, (currentTotalMass - geometryMass), displ.negate());

        // finally update the inverse inertia
        Matrix3.inverse(state.inertia, state.inverseinertia);

    }

    /**
     * Get geometry instances attached to this Body.
     * 
     * @return iterator containing geometry instances attached to this body
     */
    public Iterator<Geometry> getGeometries() {
        return geometries.iterator();
    }

    public final boolean isFixed() {
        return fixed;
    }

    // package private
    void setFixed(final boolean value) {
        fixed = value;
    }

    /**
     * Set the linear velocity of this body
     */
    public final void setVelocity(final Vector3 v) {
        state.velocity.assign(v);
    }

    /**
     * Set the linear velocity of this body
     */
    public final void setVelocity(final double x, final double y, final double z) {
        state.velocity.assign(x, y, z);
    }

    /**
     * Get the linear velocity of this body
     */
    public final Vector3 getVelocity() {
        return new Vector3(state.velocity);
    }

    /**
     * Set position of this body
     */
    public final void setPosition(final Vector3 r) {
        state.position.assign(r);
        updateTransformations();
    }

    /**
     * Set position of this body
     */
    public final void setPosition(final double x, final double y, final double z) {
        state.position.x = x;
        state.position.y = y;
        state.position.z = z;
        updateTransformations();
    }

    /**
     * Set orientation matrix
     */
    public final void setOrientation(final Matrix3 orientation) {
        state.orientation.assign(orientation);
        updateTransformations();
    }

    /**
     * Return a copy of the rotation matrix
     */
    public final Matrix3 getOrientation() {
        return new Matrix3(state.rotation);
    }

    /**
     * Get reference point of this body. This will be the centre of mass of the body, unless manual modifications has
     * been made.
     * 
     * @return reference position
     */
    public final Vector3 getPosition() {
        return new Vector3(state.position);
    }

    /**
     * Recalculate the transformation matrices rotation (3 by 3) and transform (4 by 4) from the position and
     * orientation state
     */
    public final void updateTransformations() {
        // set identity transforms
        state.transform.assignIdentity();

        // quaternion to rotation matrix
        state.orientation.toRotationMatrix3(state.rotation);

        // inverse rotations (for normals)
        Matrix3.inverse(state.rotation, state.inverserotation);

        // affine transform
        Matrix4.multiply(Transforms.rotateAndTranslate4(state.orientation, state.position), state.transform,
                state.transform);
    }

    /**
     * Return the internal 4 by 4 transformation matrix of this body
     */
    public final Matrix4 getTransform() {
        return state.transform;
    }

    /**
     * Set the angular velocity of this body
     */
    public final void setAngularVelocity(final Vector3 omega) {
        state.omega.assign(omega);
    }

    /**
     * Set the angular velocity of this body
     */
    public final void setAngularVelocity(final double x, final double y, final double z) {
        state.omega.assign(x, y, z);
    }

    /**
     * Get the angular velocity of this body
     */
    public final Vector3 getAngularVelocity() {
        return new Vector3(state.omega);
    }

    /**
     * Get the mass of this body.
     */
    public final double getMass() {
        // return the length of the unit axis vector scaled by the anisotropic mass matrix
        final double prjLength = 1. / Math.sqrt(3.);
        final Vector3 unit = new Vector3(prjLength, prjLength, prjLength);
        return state.anisotropicmass.multiply(unit).norm();
    }

    /**
     * Get a copy of the anisotropic mass matrix of this body
     */
    public final Matrix3 getAnisotopicMass() {
        return new Matrix3(state.anisotropicmass);
    }

    /**
     * Get a copy of the inverse anisotropic mass matrix of this body
     */
    public final Matrix3 getInverseAnisotropicMass() {
        return new Matrix3(state.inverseanisotropicmass);
    }

    /**
     * Apply external force to delta velocities
     * 
     * @param interaction
     *            point relative to centre of mass
     * @param f
     *            force
     * @param dt
     *            time-step size
     */
    public final void applyForce(final Vector3 point, final Vector3 f, final double dt) {
        // fixed bodies are unaffected by external forces
        if (!isFixed()) {
            // apply directly to delta velocities
            Vector3.add(externaldeltavelocity, state.inverseanisotropicmass.multiply(f.multiply(dt)));
            Vector3.add(externaldeltaomega, state.inverseinertia.multiply(point.cross(f)).multiply(dt));
        }
    }

    /**
     * Apply external force and torque to delta velocities
     * 
     * @param f
     *            linear force
     * @param tau
     *            angular force
     * @param dt
     *            time-step size
     */
    public final void applyGeneralizedForce(final Vector3 f, final Vector3 tau, final double dt) {
        // fixed bodies are unaffected by external forces
        if (!isFixed()) {
            // apply directly to delta velocities
            // Vector3.add(this.externaldeltavelocity, state.inverseanisotropicmass.multiply(f.multiply(dt)));
            // Vector3.add(this.externaldeltaomega, state.inverseinertia.multiply(tau.multiply(dt)));
            Vector3.multiplyAndAdd(state.inverseanisotropicmass, f, dt, externaldeltavelocity);
            Vector3.multiplyAndAdd(state.inverseinertia, tau, dt, externaldeltaomega);

        }
    }

    /**
     * Calculate the total kinetic energy of this body. This is the some of both translational and angular kinetic
     * energy
     */
    public final double totalKinetic() {
        double eKin;
        Vector3 res = new Vector3();

        // calculate the rotational kinetic energy
        // T = (1/2) omega * I * omega,
        res = Matrix3.transposeVectorAndMultiply(state.omega, state.inertia, res);
        eKin = res.dot(state.omega) * 0.5f;

        // translational energy E = m*(1/2)*v^2
        eKin += state.velocity.dot(state.anisotropicmass.multiply(state.velocity)) * 0.5f;

        return Math.abs(eKin);
    }

    /**
     * Calculate the kinetic energy, not scaling in linear and angular mass
     * 
     * @return
     */
    public final double totalScaledKinetic() {
        double eKin;

        // Calculate the rotational kinetic energy
        eKin = state.omega.dot(state.omega) * 0.5f;

        // Translational energy E = m*(1/2)*v^2
        eKin += state.velocity.dot(state.velocity) * 0.5f;

        return Math.abs(eKin);
    }

    /**
     * Integrate forward on position using an explicit Euler step of time-step size
     * 
     * @param dt
     */
    public final void advancePositions(final double dt) {
        // explicit euler step on position
        state.position.assignAddProduct(state.velocity, dt);

        // explicit euler step on orientation
        state.orientationderivative.assign(0.0f, state.omega);
        state.orientationderivative.assignMultiply(0.5);
        state.orientationderivative.assignMultiply(state.orientation);
        state.orientation.assignAddProduct(state.orientationderivative, dt);

        // keep orientation normalised
        state.orientation.assignNormalize();
    }

    // go from world to model
    public final Vector3 toModel(final Vector3 v) {
        // apply inverse rotation and translate backwards
        return state.rotation.transpose().multiply(v.sub(state.position));
    }

    // go from world to model without translating
    public final Vector3 toModelNoTranslation(final Vector3 v) {
        // apply inverse rotation
        return Matrix3.multiply(state.inverserotation, v, new Vector3());
    }

    // go to world coordinates from model coordinates
    public final Vector3 toWorld(final Vector3 v) {
        // apply complete transform
        return state.rotation.multiply(v).add(state.position);
    }

    // go from model to rotated model
    public final Vector3 toWorldNoTranslation(final Vector3 v) {
        return Matrix3.multiply(state.rotation, v, new Vector3());
    }

    // translate (no local rotation)
    public final Vector3 translate(final Vector3 v) {
        return v.add(state.position);
    }

    @Override
    public String toString() {
        return identifier;
    }

    public int getNumberOfGeometries() {
        return geometries.size();
    }

    /**
     * Update transforms, including attached geometries
     */
    public final void update() {
        updateTransformations();
        // update geometries
        for (final Geometry gi : geometries) {
            gi.update();
        }
    }
}
