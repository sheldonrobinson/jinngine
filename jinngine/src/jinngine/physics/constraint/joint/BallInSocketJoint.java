/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.physics.constraint.joint;

import java.util.Iterator;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.solver.Solver;
import jinngine.physics.solver.Solver.NCPConstraint;

/**
 * The ball-in-socket joint
 */
public class BallInSocketJoint implements Constraint {
    private final Body b1, b2;
    private final Vector3 p1, p2;// n1, n2;
    private final Solver.NCPConstraint c1 = new Solver.NCPConstraint();
    private final Solver.NCPConstraint c2 = new Solver.NCPConstraint();
    private final Solver.NCPConstraint c3 = new Solver.NCPConstraint();
    private double forcelimit = Double.POSITIVE_INFINITY;
    private double velocitylimit = Double.POSITIVE_INFINITY;
    private boolean monitored = false;

    public BallInSocketJoint(final Body b1, final Body b2, final Vector3 p, final Vector3 n) {
        this.b1 = b1;
        this.b2 = b2;
        // anchor points on bodies
        p1 = b1.toModel(p);
        p2 = b2.toModel(p);
    }

    /**
     * Set the maximal (boxed) applied at the joint axis
     * 
     * @param forcelimit
     */
    public void setForceLimit(final double forcelimit) {
        this.forcelimit = forcelimit;
    }

    /**
     * Set the maximal velocity allowed when correcting displacement of anchor
     * points
     */
    public void setCorrectionVelocityLimit(final double limit) {
        velocitylimit = limit;
    }

    @Override
    public void update(final double dt) {
        // Ball-In-Socket joint has a 3x12 jacobian matrix, since
        // it has 3 DOFs, thus removing 3, inducing 3 new constraints
        final Vector3 ri = Matrix3.multiply(b1.state.rotation, p1, new Vector3());
        final Vector3 rj = Matrix3.multiply(b2.state.rotation, p2, new Vector3());

        // jacobians on matrix form
        final Matrix3 Ji = Matrix3.identity().multiply(1);
        final Matrix3 Jangi = Matrix3.cross(ri).multiply(-1);
        final Matrix3 Jj = Matrix3.identity().multiply(-1);
        final Matrix3 Jangj = Matrix3.cross(rj);

        // final Matrix3 MiInv = Matrix3.identity().multiply(1/b1.state.mass);
        final Matrix3 MiInv = b1.state.inverseanisotropicmass;
        final Matrix3 Bi = MiInv.multiply(Ji.transpose());
        final Matrix3 Bangi = b1.state.inverseinertia.multiply(Jangi.transpose());

        if (b1.isFixed()) {
            Bi.assign(new Matrix3());
            Bangi.assign(new Matrix3());
        }

        // final Matrix3 MjInv = Matrix3.identity().multiply(1/b2.state.mass);
        final Matrix3 MjInv = b2.state.inverseanisotropicmass;
        final Matrix3 Bj = MjInv.multiply(Jj.transpose());
        final Matrix3 Bangj = b2.state.inverseinertia.multiply(Jangj.transpose());

        if (b2.isFixed()) {
            Bj.assign(new Matrix3());
            Bangj.assign(new Matrix3());
        }

        // Vector3 u = b1.state.velocity.add(
        // b1.state.omega.cross(ri)).minus(b2.state.velocity).add(b2.state.omega.cross(rj));
        final Vector3 u = b1.state.velocity.add(b1.state.omega.cross(ri)).sub(
                b2.state.velocity.add(b2.state.omega.cross(rj)));

        // Vector3 posError =
        // b1.state.rCm.add(b1.state.q.rotate(p1)).minus(b2.state.rCm).minus(b2.state.q.rotate(p2)).multiply(Kcor);
        final Vector3 posError = b1.state.position.add(ri).sub(b2.state.position).sub(rj).multiply(1.0 / dt);

        // correction velocity limit
        if (posError.norm() > velocitylimit) {
            posError.assign(posError.normalize().multiply(velocitylimit));
        }

        u.assign(u.add(posError));

        // go through matrices and create rows in the final A matrix to be solved
        c1.assign(Ji.row(0), Jangi.row(0), Jj.row(0), Jangj.row(0), -forcelimit * dt, forcelimit * dt, null, u.x);
        c2.assign(Ji.row(1), Jangi.row(1), Jj.row(1), Jangj.row(1), -forcelimit * dt, forcelimit * dt, null, u.y);
        c3.assign(Ji.row(2), Jangi.row(2), Jj.row(2), Jangj.row(2), -forcelimit * dt, forcelimit * dt, null, u.z);

    }

    @Override
    public final Iterator<NCPConstraint> iterator() {
        // return iterator over the members c1, c2, and c3
        return new Iterator<NCPConstraint>() {
            private int i = 0;

            @Override
            public final boolean hasNext() {
                return i < 3;
            }

            @Override
            public final NCPConstraint next() {
                switch (i) {
                    case 0:
                        i = i + 1;
                        return c1;
                    case 1:
                        i = i + 1;
                        return c2;
                    case 2:
                        i = i + 1;
                        return c3;
                }
                return null;
            }

            @Override
            public final void remove() {
                throw new UnsupportedOperationException();
            }
        };
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
        return false;
    }

    @Override
    public boolean isMonitored() {
        return monitored;
    }

    @Override
    public void setMonitored(final boolean monitored) {
        this.monitored = monitored;
    }
}
