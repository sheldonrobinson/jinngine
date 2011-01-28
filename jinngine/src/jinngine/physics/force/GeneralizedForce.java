/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.physics.force;

import java.util.Iterator;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.solver.Solver.NCPConstraint;

/**
 * A generalized force that acts between the two given bodies
 */
public class GeneralizedForce implements Constraint, Iterator<NCPConstraint> {
    // private final Body a;
    private final Vector3 linearDirection = new Vector3();
    private final Vector3 angularDirection = new Vector3();
    private double linearMagnitude = 0;
    private double angularMagnitude = 0;
    private final NCPConstraint linear = new NCPConstraint();
    private final NCPConstraint angular = new NCPConstraint();
    private int iter = 0;
    private boolean monitored = false;
    private final Body b1, b2;

    /**
     * Create a generalized force acting between b1 and b2
     * 
     * @param b1
     *            first Body in interaction
     * @param b2
     *            second body in interaction
     */
    public GeneralizedForce(final Body b1, final Body b2) {
        this.b1 = b1;
        this.b2 = b2;
        linearDirection.assign(1, 0, 0);
        angularDirection.assign(1, 0, 0);
        linearMagnitude = 0;
        angularMagnitude = 0;
    }

    /**
     * Set the direction of this generalized force. Directions must not be zero.
     * 
     * @param linearMagnitude
     *            the force magnitude of the linear part of the force
     * @param linear
     *            linear direction
     * @param angular
     *            angular direction
     * @param angularMagnitude
     *            the force magnitude of the angular part of the force
     */
    public final void setForce(final double linearMagnitude, final Vector3 linear, final double angularMagnitude,
            final Vector3 angular) {
        linearDirection.assign(linear);
        linearDirection.assignNormalize();
        angularDirection.assign(linear);
        angularDirection.assignNormalize();
        this.linearMagnitude = linearMagnitude;
        this.angularMagnitude = angularMagnitude;
    }

    @Override
    public Iterator<NCPConstraint> iterator() {
        iter = 0;
        return this;
    }

    @Override
    public void update(final double dt) {
        // assign linear constraint
        linear.j1.assign(linearDirection);
        linear.j2.assignZero();
        linear.j3.assign(linearDirection);
        linear.j3.assignNegate();
        linear.j4.assignZero();
        linear.b = 0;
        // fixed magnitude
        linear.lower = linearMagnitude * dt;
        linear.upper = linearMagnitude * dt;

        // assign angular constraint
        angular.j1.assignZero();
        angular.j2.assign(angularDirection);
        angular.j3.assignZero();
        angular.j4.assign(angularDirection);
        angular.j4.assignZero();
        angular.b = 0;
        // fixed magnitude
        angular.lower = angularMagnitude * dt;
        angular.upper = angularMagnitude * dt;
    }

    @Override
    public Body getBody1() {
        return b1;
    }

    @Override
    public Body getBody2() {
        return b2;
    }

    @Override
    public boolean isExternal() {
        // always external
        return true;
    }

    @Override
    public boolean isMonitored() {
        return monitored;
    }

    @Override
    public void setMonitored(final boolean monitored) {
        this.monitored = monitored;
    }

    @Override
    public boolean hasNext() {
        return iter < 2;
    }

    @Override
    public NCPConstraint next() {
        switch (iter) {
            case 0:
                return linear;
            case 1:
                return angular;
            default:
                return null;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
