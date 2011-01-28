
package jinngine.physics.solver;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.constraint.Constraint;

public class WarmStarter implements Solver {

    @Override
    public final double solve(final Iterable<Constraint> constraints, final Iterable<Body> bodies, final double epsilon) {

        // clear body space deltas
        for (final Body bi : bodies) {
            bi.deltavelocity.assignZero();
            bi.deltaomega.assignZero();
        }

        final Vector3 dv1 = new Vector3();
        final Vector3 do1 = new Vector3();
        final Vector3 dv2 = new Vector3();
        final Vector3 do2 = new Vector3();

        for (final Constraint ci : constraints) {
            final Body body1 = ci.getBody1();
            final Body body2 = ci.getBody2();
            final double b1mask = body1.isFixed() ? 0 : 1;
            final double b2mask = body2.isFixed() ? 0 : 1;

            dv1.assignZero();
            do1.assignZero();
            dv2.assignZero();
            do2.assignZero();

            // calculate the velocity contributions of this constraint
            for (final NCPConstraint cj : ci) {
                Vector3.multiplyAndAdd(body1.state.inverseanisotropicmass, cj.j1, cj.lambda * b1mask, dv1);
                Vector3.multiplyAndAdd(body1.state.inverseinertia, cj.j2, cj.lambda * b1mask, do1);
                Vector3.multiplyAndAdd(body2.state.inverseanisotropicmass, cj.j3, cj.lambda * b2mask, dv2);
                Vector3.multiplyAndAdd(body2.state.inverseinertia, cj.j4, cj.lambda * b2mask, do2);
            }

            // update this constraint
            ci.update(0.1);

            // calculate out the warm start b vector
            for (final NCPConstraint cj : ci) {
                // constraint velocity
                cj.Fext = -(Matrix3.transformAndDot(cj.j1, body1.state.inverseanisotropicmass, dv1) * b1mask
                        + Matrix3.transformAndDot(cj.j2, body1.state.inverseinertia, do1) * b1mask
                        + Matrix3.transformAndDot(cj.j3, body2.state.inverseanisotropicmass, dv2) * b2mask + Matrix3
                        .transformAndDot(cj.j4, body2.state.inverseinertia, do2) * b2mask);
            }

        }

        // calculate the warmstarts
        for (final Constraint ci : constraints) {
            final Body body1 = ci.getBody1();
            final Body body2 = ci.getBody2();
            final double b1mask = body1.isFixed() ? 0 : 1;
            final double b2mask = body2.isFixed() ? 0 : 1;
            dv1.assignZero();
            do1.assignZero();
            dv2.assignZero();
            do2.assignZero();

            // calculate out the warm start b vector
            final double bnorminv;
            double bnorm = 0;
            for (final NCPConstraint cj : ci) {
                // constraint velocity
                // cj.Fext = -(Matrix3.transformAndDot( cj.j1, body1.state.inverseanisotropicmass,
                // dv1)*(body1.isFixed()?0:1)
                // + Matrix3.transformAndDot( cj.j2, body1.state.inverseinertia, do1)*(body1.isFixed()?0:1)
                // + Matrix3.transformAndDot( cj.j3, body2.state.inverseanisotropicmass, dv2)*(body2.isFixed()?0:1)
                // + Matrix3.transformAndDot( cj.j4, body2.state.inverseinertia, do2)*(body2.isFixed()?0:1));

                // get contribution from current lambda into dv's
                Vector3.multiplyAndAdd(body1.state.inverseanisotropicmass, cj.j1, cj.lambda * b1mask, dv1);
                Vector3.multiplyAndAdd(body1.state.inverseinertia, cj.j2, cj.lambda * b1mask, do1);
                Vector3.multiplyAndAdd(body2.state.inverseanisotropicmass, cj.j3, cj.lambda * b2mask, dv2);
                Vector3.multiplyAndAdd(body2.state.inverseinertia, cj.j4, cj.lambda * b2mask, do2);

                // cj.lambda = 0;
                bnorm += cj.Fext * cj.Fext;
            }
            bnorm = Math.sqrt(bnorm);

            // below machine precision?
            if (bnorm > 1e-15) {
                bnorminv = 1.0 / bnorm;
            } else {
                continue;
                // bnorm =1;
                // bnorminv=1;
            }

            // scale system
            for (final NCPConstraint cj : ci) {
                cj.lambda *= bnorminv;
            }

            dv1.assignMultiply(bnorminv);
            do1.assignMultiply(bnorminv);
            dv2.assignMultiply(bnorminv);
            do2.assignMultiply(bnorminv);

            int iterations = 0;
            double residual = 0;
            while (true) {
                residual = 0;

                // use a pgs loop to calculate the warm started lambda values
                for (final NCPConstraint cj : ci) {
                    // constraint velocity
                    final double w = Matrix3.transformAndDot(cj.j1, body1.state.inverseanisotropicmass, dv1) * b1mask
                            + Matrix3.transformAndDot(cj.j2, body1.state.inverseinertia, do1) * b1mask
                            + Matrix3.transformAndDot(cj.j3, body2.state.inverseanisotropicmass, dv2) * b2mask
                            + Matrix3.transformAndDot(cj.j4, body2.state.inverseinertia, do2) * b2mask;

                    // diagonal element
                    final double diagonal = Matrix3.transformSquaredAndDot(cj.j1, body1.state.inverseanisotropicmass,
                            cj.j1)
                            * b1mask
                            + Matrix3.transformSquaredAndDot(cj.j2, body1.state.inverseinertia, cj.j2)
                            * b1mask
                            + Matrix3.transformSquaredAndDot(cj.j3, body2.state.inverseanisotropicmass, cj.j3)
                            * b2mask
                            + Matrix3.transformSquaredAndDot(cj.j4, body2.state.inverseinertia, cj.j4)
                            * b2mask;

                    // compute the gauss seidel update
                    double deltaLambda = -(cj.Fext * bnorminv + w) / diagonal;
                    final double lambda0 = cj.lambda;

                    // recompute friction limits
                    if (cj.coupling != null) {
                        // if the constraint is coupled, allow only lambda <= coupled lambda
                        cj.lower = -Math.abs(cj.coupling.lambda) * cj.coupling.mu;
                        cj.upper = Math.abs(cj.coupling.lambda) * cj.coupling.mu;
                    }

                    // do projection
                    final double newlambda = Math.max(cj.lower, Math.min(lambda0 + deltaLambda, cj.upper));

                    // update the V vector
                    deltaLambda = newlambda - lambda0;

                    // apply to delta velocities
                    Vector3.multiplyAndAdd(body1.state.inverseanisotropicmass, cj.j1, deltaLambda * b1mask, dv1);
                    Vector3.multiplyAndAdd(body1.state.inverseinertia, cj.j2, deltaLambda * b1mask, do1);
                    Vector3.multiplyAndAdd(body2.state.inverseanisotropicmass, cj.j3, deltaLambda * b2mask, dv2);
                    Vector3.multiplyAndAdd(body2.state.inverseinertia, cj.j4, deltaLambda * b2mask, do2);
                    cj.lambda += deltaLambda;

                    // residual
                    residual += deltaLambda * deltaLambda;

                }
                // System.out.println("residual="+residual);
                // count the iterations
                iterations = iterations + 1;

                // if (residual < 1e-1 || iterations>3)
                if (iterations > 3) {
                    break;
                }
            } // pgs iterations

            // apply body space effect
            Vector3.multiplyAndAdd(dv1, bnorm, body1.deltavelocity);
            Vector3.multiplyAndAdd(do1, bnorm, body1.deltaomega);
            Vector3.multiplyAndAdd(dv2, bnorm, body2.deltavelocity);
            Vector3.multiplyAndAdd(do2, bnorm, body2.deltaomega);

            // transform diagonal back
            for (final NCPConstraint cj : ci) {
                cj.lambda *= bnorm;
            }
        } // for each constraint

        return 0.0;
    }

    @Override
    public void setMaximumIterations(final int n) {
        // TODO Auto-generated method stub

    }

}
