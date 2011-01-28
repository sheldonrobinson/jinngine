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

import java.util.ArrayList;
import java.util.Iterator;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.solver.Solver.NCPConstraint;

/**
 * Implementation of a Universal joint. This type of joint has two degrees of
 * freedom.
 */
public final class UniversalJoint implements Constraint, Iterator<NCPConstraint> {
    // members
    public final Body bi, bj;
    public final Vector3 pi, pj, n1i, n2i, n1j, n2j;

    private final class AngularJointAxis {
        public final Vector3 tiw = new Vector3();
        public final Vector3 tjw = new Vector3();
        public final Vector3 nijw = new Vector3();
        public final NCPConstraint angular = new NCPConstraint();
        public final NCPConstraint extra = new NCPConstraint();
        // settings for the joint axis
        public double upperLimit = Math.PI * 0.25;
        public double lowerLimit = -Math.PI * 0.25;
        public double motor = 0;
        public double motorTargetVelocity = 0;
        public double theta = 0;
        public double velocity = 0;
        public double friction = 0.0;
        public final double shell = 0.09;
        public boolean enableLimits = true;
        public boolean enableExtra = false;
    }

    private final AngularJointAxis axis1 = new AngularJointAxis();
    private final AngularJointAxis axis2 = new AngularJointAxis();
    private final JointAxisController controler1;
    private final JointAxisController controler2;

    // constraint entries
    private final NCPConstraint linear1 = new NCPConstraint();
    private final NCPConstraint linear2 = new NCPConstraint();
    private final NCPConstraint linear3 = new NCPConstraint();
    private final NCPConstraint angular1 = new NCPConstraint();

    // list for iterating
    private final ArrayList<NCPConstraint> list = new ArrayList<NCPConstraint>();

    /**
     * Get the axis controller for the first axis. Use this controller to adjust
     * joint limits, motor and friction
     * 
     * @return A controller for this hinge joint
     */
    public final JointAxisController getFirstAxisControler() {
        return controler1;
    }

    /**
     * Get the axis controller for the second axis. Use this controller to
     * adjust joint limits, motor and friction
     * 
     * @return A controller for this hinge joint
     */
    public final JointAxisController getSecondAxisControler() {
        return controler2;
    }

    /**
     * Universal joint implementation. Coordinates and normals are given in
     * world space. The given joint axis n1 and n2 must be orthogonal.
     */
    public UniversalJoint(final Body bi, final Body bj, final Vector3 p, final Vector3 n1, final Vector3 n2) {
        this.bi = bi;
        this.bj = bj;

        // transform points and vectors to body spaces
        pi = bi.toModel(p);
        n1i = bi.toModelNoTranslation(n1);
        n2i = bi.toModelNoTranslation(n2);
        pj = bj.toModel(p);
        n1j = bj.toModelNoTranslation(n1);
        n2j = bj.toModelNoTranslation(n2);

        // add initial ncps to the list
        list.add(linear1);
        list.add(linear2);
        list.add(linear3);
        list.add(angular1);
        list.add(axis1.angular);
        list.add(axis2.angular);

        // create the controllers
        controler1 = new JointAxisController() {
            @Override
            public double getPosition() {
                return axis1.theta;
            }

            @Override
            public void setLimits(final double thetaMin, final double thetaMax) {
                if (thetaMin < -Math.PI || thetaMin > 0) {
                    throw new IllegalArgumentException("HingeJoint: JointAxisController: thetaMin must be in [-PI,0]");
                }

                if (thetaMax < 0 || thetaMax > Math.PI) {
                    throw new IllegalArgumentException("HingeJoint: JointAxisController: thetaMax must be in [0,PI]");
                }

                axis1.upperLimit = thetaMax;
                axis1.lowerLimit = thetaMin;
            }

            @Override
            public double getVelocity() {
                return axis1.velocity;
            }

            @Override
            public void setFrictionMagnitude(final double magnitude) {
                axis1.friction = magnitude;
            }

            @Override
            public void setMotorForce(final double maxForceMagnitude, final double targetVelocity) {
                if (maxForceMagnitude < 0) {
                    throw new IllegalArgumentException(
                            "HingeJoint: JointAxisController: force magnitude must be positive");
                }

                axis1.motor = maxForceMagnitude;
                axis1.motorTargetVelocity = targetVelocity;
            }

            @Override
            public void enableLimits(final boolean enable) {
                axis1.enableLimits = enable;
            }
        };

        // create the controllers
        controler2 = new JointAxisController() {
            @Override
            public double getPosition() {
                return axis2.theta;
            }

            @Override
            public void setLimits(final double thetaMin, final double thetaMax) {
                if (thetaMin < -Math.PI || thetaMin > 0) {
                    throw new IllegalArgumentException("HingeJoint: JointAxisController: thetaMin must be in [-PI,0]");
                }

                if (thetaMax < 0 || thetaMax > Math.PI) {
                    throw new IllegalArgumentException("HingeJoint: JointAxisController: thetaMax must be in [0,PI]");
                }

                axis2.upperLimit = thetaMax;
                axis2.lowerLimit = thetaMin;
            }

            @Override
            public double getVelocity() {
                return axis2.velocity;
            }

            @Override
            public void setFrictionMagnitude(final double magnitude) {
                axis2.friction = magnitude;
            }

            @Override
            public void setMotorForce(final double maxForceMagnitude, final double targetVelocity) {
                if (maxForceMagnitude < 0) {
                    throw new IllegalArgumentException(
                            "HingeJoint: JointAxisController: force magnitude must be positive");
                }

                axis2.motor = maxForceMagnitude;
                axis2.motorTargetVelocity = targetVelocity;
            }

            @Override
            public void enableLimits(final boolean enable) {
                axis2.enableLimits = enable;
            }
        };

    }

    @Override
    public final void update(final double dt) {
        // transform vectors to world space
        final Vector3 riw = Matrix3.multiply(bi.state.rotation, pi, new Vector3());
        final Vector3 rjw = Matrix3.multiply(bj.state.rotation, pj, new Vector3());
        final Vector3 n1iw = Matrix3.multiply(bi.state.rotation, n1i, new Vector3());
        final Vector3 n2iw = Matrix3.multiply(bi.state.rotation, n2i, new Vector3());
        final Vector3 n1jw = Matrix3.multiply(bj.state.rotation, n1j, new Vector3());
        final Vector3 n2jw = Matrix3.multiply(bj.state.rotation, n2j, new Vector3());

        // at all times, n1iw and n2jw are orthogonal

        // jacobians on matrix form
        final Matrix3 Ji = Matrix3.identity().multiply(1);
        final Matrix3 Jangi = Matrix3.cross(riw).multiply(-1);
        final Matrix3 Jj = Matrix3.identity().multiply(-1);
        final Matrix3 Jangj = Matrix3.cross(rjw);

        // Matrix3 MiInv = Matrix3.identity().multiply(1/bi.state.mass);
        // Matrix3 MjInv = Matrix3.identity().multiply(1/bj.state.mass);
        //        final Matrix3 MiInv = bi.state.inverseanisotropicmass;
        //        final Matrix3 MjInv = bj.state.inverseanisotropicmass;

        //        final Matrix3 Bi = bi.isFixed() ? new Matrix3() : MiInv.multiply(Ji.transpose());
        //        final Matrix3 Bangi = bi.isFixed() ? new Matrix3() : bi.state.inverseinertia.multiply(Jangi.transpose());
        //        final Matrix3 Bj = bj.isFixed() ? new Matrix3() : MjInv.multiply(Jj.transpose());
        //        final Matrix3 Bangj = bj.isFixed() ? new Matrix3() : bj.state.inverseinertia.multiply(Jangj.transpose());

        final double Kcor = 0.2;

        // Vector3 u = b1.state.velocity.minus(
        // ri.cross(b1.state.omega)).minus(b2.state.velocity).add(rj.cross(b2.state.omega));
        final Vector3 u = bi.state.velocity.add(bi.state.omega.cross(riw)).sub(
                bj.state.velocity.add(bj.state.omega.cross(rjw)));

        final Vector3 posError = bi.state.position.add(riw).sub(bj.state.position).sub(rjw).multiply(1 / dt);
        // error in transformed normal
        // Vector3 nerror = n1iw.cross(n2jw);
        u.assign(u.add(posError.multiply(Kcor)));

        linear1.assign(Ji.row(0), Jangi.row(0), Jj.row(0), Jangj.row(0), Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, null, u.x);

        linear2.assign(Ji.row(1), Jangi.row(1), Jj.row(1), Jangj.row(1), Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, null, u.y);

        linear3.assign(Ji.row(2), Jangi.row(2), Jj.row(2), Jangj.row(2), Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, null, u.z);

        // angular axis where relative velocity must be zero
        final Vector3 q = n1iw.cross(n2jw);

        angular1.assign(new Vector3(), q, new Vector3(), q.multiply(-1), Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, null, q.dot(bi.state.omega) - q.dot(bj.state.omega) - Kcor * -n1iw.dot(n2jw)
                        * 1 / dt);

        axis1.tiw.assign(n1iw);
        axis1.tjw.assign(n1jw);
        axis1.nijw.assign(n2jw);
        applyJointAxisConstraint(axis1, Kcor, dt);

        axis2.tiw.assign(n2iw);
        axis2.tjw.assign(n2jw);
        axis2.nijw.assign(n1iw);
        applyJointAxisConstraint(axis2, Kcor, dt);
    }

    private final void applyJointAxisConstraint(final AngularJointAxis joint, final double Kcor, final double dt) {
        // handle the constraint modelling joint limits and motor
        double low = 0;
        double high = 0;
        double correction = 0;
        final Vector3 axis = joint.nijw;
        final double sign = joint.tiw.cross(joint.tjw).dot(joint.nijw) > 0 ? 1 : -1;
        double product = joint.tiw.dot(joint.tjw);
        // make sure product is exactly in [-1,1]
        product = Math.max(Math.min(product, 1), -1);
        joint.theta = -Math.acos(product) * sign;

        // angular velocity along axis
        joint.velocity = axis.dot(bi.state.omega) - axis.dot(bj.state.omega);
        double bvalue = 0;

        // if limits are clamped together
        if (Math.abs(joint.lowerLimit - joint.upperLimit) < joint.shell && joint.enableLimits) {
            correction = (joint.theta - joint.upperLimit) * 1 / dt * Kcor;
            high = Double.POSITIVE_INFINITY;
            low = Double.NEGATIVE_INFINITY;
            bvalue = joint.velocity + correction;
            // if joint is stretched upper
        } else if (joint.theta >= joint.upperLimit - joint.shell && joint.enableLimits) {
            correction = -(joint.theta - joint.upperLimit) * 1 / dt * Kcor;
            // correction = Math.min( correction, 0.9);
            high = joint.motorTargetVelocity >= 0 ? joint.motor : 0; // motor is pressing against limit?
            low = Double.NEGATIVE_INFINITY;// + motorLow;

            // if motor is working to leave the limit, we need an extra
            // velocity constraint to model the motors contribution at the limit
            if (joint.motorTargetVelocity < 0 && joint.motor > 0) {
                joint.extra.assign(new Vector3(), axis, new Vector3(), axis.multiply(-1), -joint.motor * dt, 0, null,
                        joint.velocity - joint.motorTargetVelocity);

                // add to list if it isn't there
                if (!joint.enableExtra) {
                    list.add(joint.extra);
                    joint.enableExtra = true;
                }

            } else {
                // clamp correction velocity to motor target when motor is pressing against limit
                if (correction > 0) {
                    correction = Math.min(correction, joint.motorTargetVelocity);
                }

                // remove from list if there
                if (joint.enableExtra) {
                    list.remove(joint.extra);
                    joint.enableExtra = false;
                }
            }

            bvalue = joint.velocity - correction;

            // if joint is stretched lower
        } else if (joint.theta <= joint.lowerLimit + joint.shell && joint.enableLimits) {
            correction = -(joint.theta - joint.lowerLimit) * 1 / dt * Kcor;
            // correction = Math.max( correction, -0.9);
            high = Double.POSITIVE_INFINITY;// + motorHigh;
            low = joint.motorTargetVelocity <= 0 ? -joint.motor : 0; // motor is pressing against limit?

            // if motor is working to leave the limit, we need an extra
            // velocity constraint to model the motors contribution at the limit
            if (joint.motorTargetVelocity > 0 && joint.motor > 0) {
                joint.extra.assign(new Vector3(), axis, new Vector3(), axis.multiply(-1), 0, joint.motor * dt, null,
                        joint.velocity - joint.motorTargetVelocity);

                // add to list if it isn't there
                if (!joint.enableExtra) {
                    list.add(joint.extra);
                    joint.enableExtra = true;
                }
            } else {
                // clamp correction velocity to motor target when motor is pressing against limit
                if (correction < 0) {
                    correction = Math.max(correction, joint.motorTargetVelocity);
                }

                // remove from list if there
                if (joint.enableExtra) {
                    list.remove(joint.extra);
                    joint.enableExtra = false;
                }

            }

            bvalue = joint.velocity - correction;

            // not at limits, motor working
        } else if (joint.motor != 0) {
            high = joint.motor;
            low = -joint.motor;
            // motor tries to achieve the target velocity using the motor force available
            bvalue = joint.velocity - joint.motorTargetVelocity;
            // not at limits, no motor. friction is working
        } else if (joint.friction != 0) {
            high = joint.friction;
            low = -joint.friction;
            // friction tries to prevent motion along the joint axis
            bvalue = joint.velocity;
        }
        // unlimited joint axis

        joint.angular.assign(new Vector3(), axis, new Vector3(), axis.multiply(-1), low * dt, high * dt, null, bvalue);

    }

    /*
     * Iterator implementation
     */

    private int N = 0;
    private int n = 0;

    @Override
    public final Iterator<NCPConstraint> iterator() {
        // reset the counter and return this as iterator
        n = 0;
        N = list.size();
        return this;
    }

    @Override
    public boolean hasNext() {
        return n < N;
    }

    @Override
    public NCPConstraint next() {
        if (n < N) {
            return list.get(n++);
        } else {
            return null;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Body getBody1() {
        return bi;
    }

    @Override
    public Body getBody2() {
        return bj;
    }

    @Override
    public boolean isExternal() {
        return false;
    }

    @Override
    public boolean isMonitored() {
        return false;
    }

    @Override
    public void setMonitored(final boolean monitored) {}
}
