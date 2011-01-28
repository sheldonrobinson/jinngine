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

import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.solver.Solver.NCPConstraint;

/**
 * A gravity force is a force acting upon the centre of mass of a body, and is
 * proportional to the total mass of the body. The force is pointing in the
 * negative y direction, unless another direction is specified.
 */
public final class GravityForce implements Constraint, Iterator<NCPConstraint> {
    // private final Body a;
    private final Vector3 d = new Vector3();
    private final Vector3 F = new Vector3();
    private final NCPConstraint ncp = new NCPConstraint();
    private boolean hasNext = false;

    private final Body b1, b2;

    /**
     * Create a gravity force, accelerating the body 9.8m/s^2. The direction of
     * the force is along the negative y-axis.
     * 
     * @param a
     *            Body on which to act
     */
    public GravityForce(final Body b1, final Body b2) {
        this.b1 = b1;
        this.b2 = b2;
        d.assign(new Vector3(0, -1, 0));
    }

    /**
     * Create a gravity force, accelerating the body 9.8m/s^2 in the direction d
     * 
     * @param a
     *            Body on which to act
     * @param d
     *            Direction of gravity force.
     */
    public GravityForce(final Body b1, final Body b2, final Vector3 d) {
        this.b1 = b1;
        this.b2 = b2;
        this.d.assign(d);
    }

    @Override
    public Iterator<NCPConstraint> iterator() {
        hasNext = true;
        return this;
    }

    @Override
    public void update(final double dt) {
        ncp.j1.assign(d);
        ncp.j2.assignZero();
        ncp.j3.assign(d);
        ncp.j3.assignNegate();
        ncp.j4.assignZero();
        ncp.b = 0;

        F.assign(d);
        F.assignMultiply(9.8 * dt);
        Matrix3.multiply(b1.state.anisotropicmass, F, F);

        ncp.lower = F.norm();
        ncp.upper = F.norm();
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
        // cannot be monitored
        return false;
    }

    @Override
    public void setMonitored(final boolean monitored) {
        // cannot be monitored
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public NCPConstraint next() {
        if (hasNext) {
            hasNext = false;
            return ncp;
        } else {
            return null;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
