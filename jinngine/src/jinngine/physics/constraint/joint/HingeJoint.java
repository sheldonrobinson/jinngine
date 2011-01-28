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
import jinngine.physics.solver.Solver.NCPConstraint;

/**
 * Implementation of a hinge joint. This type of joint leaves only one degree of
 * freedom left for the involved bodies, where they can have angular motion
 * along some axis.
 */
public final class HingeJoint implements Constraint, Iterator<NCPConstraint> {
    // members
    public final Body b1, b2;
    public final Vector3 pi, pj, ni, nj, t2i, t2j, t3i;
    private final JointAxisController controler;

    // settings for the joint axis
    public double upperLimit = Double.POSITIVE_INFINITY;
    public double lowerLimit = Double.NEGATIVE_INFINITY;
    private double motor = 0;
    private double motorTargetVelocity = 0;
    private double theta = 0;
    private double velocity = 0;
    private double friction = 0.0;
    private final double shell = 0.09;
    private boolean enableLimits = true;
    private boolean enableExtra = false;

    // constraint entries
    private final NCPConstraint linear1 = new NCPConstraint();
    private final NCPConstraint linear2 = new NCPConstraint();
    private final NCPConstraint linear3 = new NCPConstraint();
    private final NCPConstraint angular1 = new NCPConstraint();
    private final NCPConstraint angular2 = new NCPConstraint();
    private final NCPConstraint angular3 = new NCPConstraint();
    private final NCPConstraint extra = new NCPConstraint();

    /**
     * Get the axis controller for the hinge joint. Use this controller to
     * adjust joint limits, motor and friction
     * 
     * @return A controller for this hinge joint
     */
    public JointAxisController getHingeControler() {
        return controler;
    }

    public HingeJoint(final Body b1, final Body b2, final Vector3 p, final Vector3 n) {
        this.b1 = b1;
        this.b2 = b2;
        // anchor points on bodies
        pi = b1.toModel(p);
        ni = b1.toModelNoTranslation(n);
        pj = b2.toModel(p);
        nj = b2.toModelNoTranslation(n);

        // Use a Gram-Schmidt process to create a orthonormal basis for the impact space
        final Vector3 v1 = n.normalize();
        Vector3 v2 = Vector3.i();
        Vector3 v3 = Vector3.k();
        final Vector3 t1 = v1.normalize();
        t2i = v2.sub(t1.multiply(t1.dot(v2)));

        // in case v1 and v2 are parallel
        if (t2i.isEpsilon(1e-9)) {
            v2 = Vector3.j();
            v3 = Vector3.k();
            t2i.assign(v2.sub(t1.multiply(t1.dot(v2))).normalize());
        } else {
            t2i.assign(t2i.normalize());
        }

        // tangent 2 in j body space
        t2j = b2.toModelNoTranslation(b1.toWorldNoTranslation(t2i));

        // v1 parallel with v3
        if (v1.cross(v3).isEpsilon(1e-9)) {
            v3 = Vector3.j();
        }
        // finally calculate t3
        t3i = v3.sub(t1.multiply(t1.dot(v3)).sub(t2i.multiply(t2i.dot(v3)))).normalize();

        // create the controller
        controler = new JointAxisController() {
            @Override
            public double getPosition() {
                return theta;
            }

            @Override
            public void setLimits(final double thetaMin, final double thetaMax) {
                if (thetaMin < -Math.PI || thetaMin > 0) {
                    throw new IllegalArgumentException("HingeJoint: JointAxisController: thetaMin must be in [-PI,0]");
                }

                if (thetaMax < 0 || thetaMax > Math.PI) {
                    throw new IllegalArgumentException("HingeJoint: JointAxisController: thetaMax must be in [0,PI]");
                }

                upperLimit = thetaMax;
                lowerLimit = thetaMin;
            }

            @Override
            public double getVelocity() {
                return velocity;
            }

            @Override
            public void setFrictionMagnitude(final double magnitude) {
                friction = magnitude;

            }

            @Override
            public void setMotorForce(final double maxForceMagnitude, final double targetVelocity) {
                if (maxForceMagnitude < 0) {
                    throw new IllegalArgumentException(
                            "HingeJoint: JointAxisController: force magnitude must be positive");
                }

                motor = maxForceMagnitude;
                motorTargetVelocity = targetVelocity;
            }

            @Override
            public void enableLimits(final boolean enable) {
                enableLimits = enable;
            }
        };

    }

    @Override
    public final void update(final double dt) {
        // transform points
        final Vector3 ri = Matrix3.multiply(b1.state.rotation, pi, new Vector3());
        final Vector3 rj = Matrix3.multiply(b2.state.rotation, pj, new Vector3());
        final Vector3 tt2i = Matrix3.multiply(b1.state.rotation, t2i, new Vector3());
        final Vector3 tt2j = Matrix3.multiply(b2.state.rotation, t2j, new Vector3());
        final Vector3 tt3i = Matrix3.multiply(b1.state.rotation, t3i, new Vector3());
        final Vector3 tn1 = Matrix3.multiply(b1.state.rotation, ni, new Vector3());
        final Vector3 tn2 = Matrix3.multiply(b2.state.rotation, nj, new Vector3());

        // jacobians on matrix form
        final Matrix3 Ji = Matrix3.identity().multiply(1);
        final Matrix3 Jangi = Matrix3.cross(ri).multiply(-1);
        final Matrix3 Jj = Matrix3.identity().multiply(-1);
        final Matrix3 Jangj = Matrix3.cross(rj);

        // Matrix3 MiInv = Matrix3.identity().multiply(1/b1.state.mass);
        // Matrix3 MjInv = Matrix3.identity().multiply(1/b2.state.mass);
        final Matrix3 MiInv = b1.state.inverseanisotropicmass;
        final Matrix3 MjInv = b2.state.inverseanisotropicmass;

        final Matrix3 Bi = b1.isFixed() ? new Matrix3() : MiInv.multiply(Ji.transpose());
        final Matrix3 Bangi = b1.isFixed() ? new Matrix3() : b1.state.inverseinertia.multiply(Jangi.transpose());
        final Matrix3 Bj = b2.isFixed() ? new Matrix3() : MjInv.multiply(Jj.transpose());
        final Matrix3 Bangj = b2.isFixed() ? new Matrix3() : b2.state.inverseinertia.multiply(Jangj.transpose());

        final double Kcor = 0.9;

        // Vector3 u = b1.state.velocity.minus(
        // ri.cross(b1.state.omega)).minus(b2.state.velocity).add(rj.cross(b2.state.omega));
        final Vector3 u = b1.state.velocity.add(b1.state.omega.cross(ri)).sub(
                b2.state.velocity.add(b2.state.omega.cross(rj)));

        final Vector3 posError = b1.state.position.add(ri).sub(b2.state.position).sub(rj).multiply(1 / dt);
        // error in transformed normal
        final Vector3 nerror = tn1.cross(tn2);
        u.assign(u.add(posError.multiply(Kcor)));

        linear1.assign(Ji.row(0), Jangi.row(0), Jj.row(0), Jangj.row(0), Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, null, u.x);

        linear2.assign(Ji.row(1), Jangi.row(1), Jj.row(1), Jangj.row(1), Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, null, u.y);

        linear3.assign(Ji.row(2), Jangi.row(2), Jj.row(2), Jangj.row(2), Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, null, u.z);

        // handle the constraint modelling joint limits and motor
        double low = 0;
        double high = 0;
        double correction = 0;
        final Vector3 axis = tn1;
        final double sign = tt2i.cross(tt2j).dot(tn1) > 0 ? 1 : -1;
        double product = tt2i.dot(tt2j);
        // make sure product is excatly in [-1,1]
        product = Math.max(Math.min(product, 1), -1);
        theta = -Math.acos(product) * sign;

        // angular velocity along axis
        velocity = axis.dot(b1.state.omega) - axis.dot(b2.state.omega);
        double bvalue = 0;

        // if limits are clamped together
        if (Math.abs(lowerLimit - upperLimit) < shell && enableLimits) {
            correction = (theta - upperLimit) * 1 / dt * Kcor;
            high = Double.POSITIVE_INFINITY;
            low = Double.NEGATIVE_INFINITY;
            bvalue = velocity + correction;
            // if joint is stretched upper
        } else if (theta >= upperLimit - shell && enableLimits) {
            correction = (theta - upperLimit) * 1 / dt * Kcor;
            // correction = Math.min( correction, 0.9);
            high = motorTargetVelocity >= 0 ? motor : 0; // motor is pressing against limit?
            low = Double.NEGATIVE_INFINITY;// + motorLow;
            bvalue = velocity + correction;

            // if motor is working to leave the limit, we need an extra
            // velocity constraint to model the motors contribution at the limit
            if (motorTargetVelocity < 0 && motor > 0) {
                extra.assign(new Vector3(), axis, new Vector3(), axis.multiply(-1), -motor * dt, 0, null, velocity
                        - motorTargetVelocity);
            }
            // if joint is stretched lower
        } else if (theta <= lowerLimit + shell && enableLimits) {
            correction = (theta - lowerLimit) * 1 / dt * Kcor;
            // correction = Math.max( correction, -0.9);
            high = Double.POSITIVE_INFINITY;// + motorHigh;
            low = motorTargetVelocity <= 0 ? -motor : 0; // motor is pressing against limit?
            bvalue = velocity + correction;

            // if motor is working to leave the limit, we need an extra
            // velocity constraint to model the motors contribution at the limit
            if (motorTargetVelocity > 0 && motor > 0) {
                extra.assign(new Vector3(), axis, new Vector3(), axis.multiply(-1), 0, motor * dt, null, velocity
                        - motorTargetVelocity);

                enableExtra = true;
            } else {
                enableExtra = false;
            }
            // not at limits, motor working
        } else if (motor != 0) {
            high = motor;
            low = -motor;
            // motor tries to achieve the target velocity using the motor force available
            bvalue = velocity - motorTargetVelocity;
            // not at limits, no motor. friction is working
        } else if (friction != 0) {
            high = friction;
            low = -friction;
            // friction tries to prevent motion along the joint axis
            bvalue = velocity;
        }
        // unlimited joint axis

        angular1.assign(new Vector3(), axis, new Vector3(), axis.multiply(-1), low * dt, high * dt, null, bvalue);

        // keep bodies aligned to the axis
        angular2.assign(new Vector3(), tt2i, new Vector3(), tt2i.multiply(-1), Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, null,
                tt2i.dot(b1.state.omega) - tt2i.dot(b2.state.omega) - Kcor * tt2i.dot(nerror) * 1 / dt);

        angular3.assign(new Vector3(), tt3i, new Vector3(), tt3i.multiply(-1), Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, null,
                tt3i.dot(b1.state.omega) - tt3i.dot(b2.state.omega) - Kcor * tt3i.dot(nerror) * 1 / dt);

    }

    @Override
    public final Body getBody1() {
        return b1;
    }

    @Override
    public final Body getBody2() {
        return b2;
    }

    @Override
    public final boolean isExternal() {
        return false;
    }

    @Override
    public boolean isMonitored() {
        return false;
    }

    @Override
    public final void setMonitored(final boolean monitored) {
        return;
    }

    /*
     * Iterator implementation
     */

    // private int N=0;
    private int n = 0;

    @Override
    public final Iterator<NCPConstraint> iterator() {
        // reset the counter and return this as iterator
        n = 0;
        return this;
    }

    @Override
    public boolean hasNext() {
        // true if n is below 6 or below 7 if the extra constraint is enabled
        return n < 6 || n < 7 && enableExtra;
    }

    @Override
    public NCPConstraint next() {
        switch (n) {
            case 0:
                n = n + 1;
                return linear1;
            case 1:
                n = n + 1;
                return linear2;
            case 2:
                n = n + 1;
                return linear3;
            case 3:
                n = n + 1;
                return angular1;
            case 4:
                n = n + 1;
                return angular2;
            case 5:
                n = n + 1;
                return angular3;
        }

        if (n == 6 && enableExtra) {
            n = n + 1;
            return extra;
        }

        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
