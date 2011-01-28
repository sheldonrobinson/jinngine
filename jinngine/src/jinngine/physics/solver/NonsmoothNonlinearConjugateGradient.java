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

/**
 * Implementation of the NNCG solver, or nonsmooth nonlinear Conjugate Gradient
 * solver, as described in the Visual Computer paper "A nonsmooth nonlinear
 * conjugate gradient method for interactive contact force problems". This
 * method can be seen as a simple extension of the PGS method, that "reuses"
 * some of the previous solutions in each iteration, to achieve a better
 * convergence rate.
 * 
 * http://www.springerlink.com/content/e83432544t772126/
 * 
 * BibTex:
 * 
 * @article{1821968, author = {Silcowitz-Hansen, Morten and Niebe, Sarah and
 *                   Erleben, Kenny}, title = {A nonsmooth nonlinear conjugate
 *                   gradient method for interactive contact force problems},
 *                   journal = {Visual Computer}, volume = {26}, number = {6-8},
 *                   year = {2010}, issn = {0178-2789}, pages = {893--901}, doi
 *                   = {http://dx.doi.org/10.1007/s00371-010-0502-6}, publisher
 *                   = {Springer-Verlag New York, Inc.}, address = {Secaucus,
 *                   NJ, USA}, }
 */
public class NonsmoothNonlinearConjugateGradient implements Solver {
    private int max = 10000;

    //	private final double eps = 1e-7;
    //	public double[] pgsiters = new double[max];
    //	public double[] errors = new double[max];

    @Override
    public void setMaximumIterations(final int n) {
        //	max =n;
    }

    public NonsmoothNonlinearConjugateGradient(final int n) {
        max = n;
        //		pgsiters = new double[max];
        //		errors = new double[max];
    }

    @Override
    public double solve(final Iterable<Constraint> constraints, final Iterable<Body> bodies, final double epsilon) {

        double rnew = 0;
        double rold = 0;
        double beta = 0;
        int iter = 0;
        int restarts = 0;
        //		epsilon = 1e-5;

        // compute external force contribution, clear direction and residual, compute b vector norm
        double bnorm = 0;
        for (final Constraint ci : constraints) {
            final Body body1 = ci.getBody1();
            final Body body2 = ci.getBody2();

            for (final NCPConstraint cj : ci) {
                cj.Fext = cj.j1.dot(body1.externaldeltavelocity) + cj.j2.dot(body1.externaldeltaomega)
                        + cj.j3.dot(body2.externaldeltavelocity) + cj.j4.dot(body2.externaldeltaomega);
                cj.d = 0;
                cj.residual = 0;
                bnorm += Math.pow(cj.b + cj.Fext, 2);
            }
        }
        bnorm = Math.sqrt(bnorm);
        final double bnorminv;
        // avoid division by zero
        if (bnorm < 1e-15) {
            bnorm = 1.0;
            bnorminv = 1.0;
            return 0.0;
        } else {
            bnorminv = 1.0 / bnorm;
        }

        // scale lambda values and b-vector
        for (final Constraint ci : constraints) {
            for (final NCPConstraint ncpj : ci) {
                ncpj.lambda *= bnorminv;
                //				ncpj.Fext *= bnorminv;
            }
        }

        // scale delta velocity in the bnorminv
        for (final Body bi : bodies) {
            Vector3.multiply(bi.deltavelocity, bnorminv);
            Vector3.multiply(bi.deltaomega, bnorminv);
        }

        // reset search direction
        for (final Body b : bodies) {
            b.deltavelocity1.assignZero();
            b.deltaomega1.assignZero();
        }

        while (true) {
            // copy body velocity
            for (final Body bi : bodies) {
                bi.deltavelocity2.assign(bi.deltavelocity);
                bi.deltaomega2.assign(bi.deltaomega);
            }

            rold = rnew;
            rnew = 0;

            // use one PGS iteration to compute new residual 
            for (final Constraint ci : constraints) {
                final Body body1 = ci.getBody1();
                final Body body2 = ci.getBody2();
                final Matrix3 M1 = body1.state.inverseanisotropicmass;
                final Matrix3 I1 = body1.state.inverseinertia;
                final Matrix3 M2 = body2.state.inverseanisotropicmass;
                final Matrix3 I2 = body2.state.inverseinertia;
                final double b1mask = body1.isFixed() ? 0 : 1;
                final double b2mask = body2.isFixed() ? 0 : 1;

                for (final NCPConstraint cj : ci) {
                    // update lambda and d
                    final double alpha = beta * cj.d;
                    cj.lambda += alpha;
                    cj.d = alpha + cj.residual; // gradient is -r

                    // constraint velocity contribution 				
                    final double w = cj.j1.dot(body1.deltavelocity) + cj.j2.dot(body1.deltaomega)
                            + cj.j3.dot(body2.deltavelocity) + cj.j4.dot(body2.deltaomega);

                    // compute diagonal element
                    final double diagonal = (Matrix3.transformAndDot(cj.j1, M1, cj.j1) + Matrix3.transformAndDot(cj.j2,
                            I1, cj.j2))
                            * b1mask
                            + (Matrix3.transformAndDot(cj.j3, M2, cj.j3) + Matrix3.transformAndDot(cj.j4, I2, cj.j4))
                            * b2mask;

                    // the gauss-seidel update
                    double deltalambda = -((cj.b + cj.Fext) * bnorminv + w) / diagonal;
                    final double lambda0 = cj.lambda;

                    //Clamp the lambda[i] value to the constraints
                    if (cj.coupling != null) {
                        //if the constraint is coupled, allow only lambda <= coupled lambda
                        cj.lower = -Math.abs(cj.coupling.lambda) * cj.coupling.mu;
                        cj.upper = Math.abs(cj.coupling.lambda) * cj.coupling.mu;

                        //use growing bounds only (disabled)
                        //if the constraint is coupled, allow only lambda <= coupled lambda
                        //					 double lower = -Math.abs(ci.coupling.lambda)*ci.coupling.mu;					
                        //					ci.lower = lower<ci.lower?lower:ci.lower;					
                        //					double upper = Math.abs(ci.coupling.lambda)*ci.coupling.mu;
                        //					ci.upper =  upper>ci.upper?upper:ci.upper;

                    }

                    // do projection
                    final double newlambda = Math.max(cj.lower, Math.min(lambda0 + deltalambda, cj.upper));

                    // update the V vector
                    deltalambda = newlambda - lambda0;

                    // apply to delta velocities
                    Vector3.multiplyAndAdd(M1, cj.j1, deltalambda * b1mask, body1.deltavelocity);
                    Vector3.multiplyAndAdd(I1, cj.j2, deltalambda * b1mask, body1.deltaomega);
                    Vector3.multiplyAndAdd(M2, cj.j3, deltalambda * b2mask, body2.deltavelocity);
                    Vector3.multiplyAndAdd(I2, cj.j4, deltalambda * b2mask, body2.deltaomega);

                    // update solution variable
                    cj.lambda += deltalambda;

                    // update residual and squared gradient
                    rnew += deltalambda * deltalambda;

                    cj.residual = deltalambda;
                } // for variables 
            } // for constraints

            // termination condition
            if (Math.abs(rnew) < epsilon) {
                break;
            }

            //			System.out.println("rnew="+rnew);

            // iteration limit
            if (iter > max || restarts > max) {
                break;
            }

            //compute beta
            beta = rnew / rold;

            if (beta > 1.0 || iter == 0) {
                beta = 0;
                restarts = restarts + 1;
                //				System.out.println("restart");
            }

            for (final Body bi : bodies) {
                // compute residual in body space
                Vector3.sub(bi.deltavelocity2, bi.deltavelocity);
                Vector3.sub(bi.deltaomega2, bi.deltaomega);
                Vector3.multiply(bi.deltavelocity2, -1);
                Vector3.multiply(bi.deltaomega2, -1);

                // apply to delta velocities
                Vector3.multiplyStoreAndAdd(bi.deltavelocity1, beta, bi.deltavelocity);
                Vector3.multiplyStoreAndAdd(bi.deltaomega1, beta, bi.deltaomega);

                // add gradient from this iteration
                Vector3.add(bi.deltavelocity1, bi.deltavelocity2);
                Vector3.add(bi.deltaomega1, bi.deltaomega2);
            }

            //iteration count
            iter = iter + 1;
        } // while true

        //		System.out.println("rnew="+rnew);

        // scale system back to original
        for (final Constraint ci : constraints) {
            for (final NCPConstraint ncpj : ci) {
                ncpj.lambda *= bnorm;
                //				System.out.println("final lambda="+ncpj.lambda);
            }
        }

        // scale delta velocities back
        for (final Body bi : bodies) {
            Vector3.multiply(bi.deltavelocity, bnorm);
            Vector3.multiply(bi.deltaomega, bnorm);
        }

        //		// scale lambda in the bnorm. This is unnecessary if bnorm is set to 1
        //		for (NCPConstraint ci: constraints) {
        //			final double factor = (bnorm-1)*ci.lambda;
        ////			Vector3.add( ci.body1.deltavelocity, ci.b1.multiply(factor));
        ////			Vector3.add( ci.body1.deltaomega, ci.b2.multiply(factor));
        ////			Vector3.add( ci.body2.deltavelocity, ci.b3.multiply(factor));
        ////			Vector3.add( ci.body2.deltaomega, ci.b4.multiply(factor));
        //			Vector3.multiplyAndAdd( ci.b1, factor, ci.body1.deltavelocity );
        //			Vector3.multiplyAndAdd( ci.b2, factor, ci.body1.deltaomega );
        //			Vector3.multiplyAndAdd( ci.b3, factor, ci.body2.deltavelocity );
        //			Vector3.multiplyAndAdd( ci.b4, factor, ci.body2.deltaomega );
        //
        //		}

        return 0;
    }
}
