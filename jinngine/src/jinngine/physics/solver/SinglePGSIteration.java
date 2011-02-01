/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.physics.solver;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.solver.Solver.NCPConstraint;

/**
 * A single PGS iteration, to handle monitoring of deactivated constraints
 */
public class SinglePGSIteration {
    public static final void run(final Constraint constraint, final Body body1, final Body body2) {

        // compute external force contribution, compute norm of the b-vector
        for (final NCPConstraint ncpj : constraint) {
            ncpj.Fext = ncpj.j1.dot(body1.externaldeltavelocity) + ncpj.j2.dot(body1.externaldeltaomega)
                    + ncpj.j3.dot(body2.externaldeltavelocity) + ncpj.j4.dot(body2.externaldeltaomega) + ncpj.b;
        }

        final Matrix3 M1 = body1.state.inverseanisotropicmass;
        final Matrix3 I1 = body1.state.inverseinertia;
        final Matrix3 M2 = body2.state.inverseanisotropicmass;
        final Matrix3 I2 = body2.state.inverseinertia;
        final double b1mask = body1.isFixed() ? 0 : 1;
        final double b2mask = body2.isFixed() ? 0 : 1;

        // do the PGS iteration
        for (final NCPConstraint ncpj : constraint) {
            // calculate (Ax+b)_i
            final double w = ncpj.j1.dot(body1.deltavelocity) + ncpj.j2.dot(body1.deltaomega)
                    + ncpj.j3.dot(body2.deltavelocity) + ncpj.j4.dot(body2.deltaomega);

            final double diagonal = (Matrix3.transformAndDot(ncpj.j1, M1, ncpj.j1) + Matrix3.transformAndDot(ncpj.j2,
                    I1, ncpj.j2))
                    * b1mask
                    + (Matrix3.transformAndDot(ncpj.j3, M2, ncpj.j3) + Matrix3.transformAndDot(ncpj.j4, I2, ncpj.j4))
                    * b2mask;

            // the change in lambda_i needed to obtain (Ax+b)_i = 0
            // double deltaLambda = (-ci.b-w)/(ci.diagonal + ci.damper );
            double deltalambda = Math.abs(diagonal) > 1e-14 ? -(ncpj.Fext + w) / diagonal : 0;

            final double lambda0 = ncpj.lambda;

            // Clamp the lambda[i] value to the constraints
            if (ncpj.coupling != null) {
                // growing bounds
                // double lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;
                // ci.lower = lower<ci.lower?lower:ci.lower;
                // double upper = Math.abs(ci.coupling.lambda)*ci.coupling.mu;
                // ci.upper = upper>ci.upper?upper:ci.upper;
                // if the constraint is coupled, allow only lambda <= coupled lambda
                ncpj.lower = -Math.abs(ncpj.coupling.lambda) * ncpj.coupling.mu;
                ncpj.upper = Math.abs(ncpj.coupling.lambda) * ncpj.coupling.mu;
            }

            // do projection
            ncpj.lambda = Math.max(ncpj.lower, Math.min(lambda0 + deltalambda, ncpj.upper));

            // update the V vector
            deltalambda = ncpj.lambda - lambda0;

            // apply to delta velocities
            Vector3.multiplyAndAdd(M1, ncpj.j1, deltalambda * b1mask, body1.deltavelocity);
            Vector3.multiplyAndAdd(I1, ncpj.j2, deltalambda * b1mask, body1.deltaomega);
            Vector3.multiplyAndAdd(M2, ncpj.j3, deltalambda * b2mask, body2.deltavelocity);
            Vector3.multiplyAndAdd(I2, ncpj.j4, deltalambda * b2mask, body2.deltaomega);

        } // for ncp constraints
    } // for constraints
}
