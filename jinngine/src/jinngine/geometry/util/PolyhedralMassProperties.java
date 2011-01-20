/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.geometry.util;

import jinngine.math.InertiaMatrix;
import jinngine.math.Vector3;
import quickhull3d.QuickHull3D;
/***
 * This class compute the mass, center of mass ans inertial matrix of a convex
 * hull at construction time. The result is retrieved by get* methods.
 *
 * The computation algorythm is described by Brian Mirtich article
 * "Fast and Accurate Computation of Polyhedral Mass Properties" published in
 * journal of graphics tools (http://jgt.akpeters.com/) volume 1, number 2, 1996.
 *
 * http://www.cs.berkeley.edu/~jfc/mirtich/massProps.html
 *
 * The algorithm is based on a three step reduction of the volume integrals to
 * successively simpler integrals. The algorithm is designed to minimize the
 * numerical errors that can result from poorly conditioned alignment of
 * polyhedral faces. It is also designed for efficiency. All required volume
 * integrals of a polyhedron are computed together during a single walk over the
 * boundary of the polyhedron; exploiting common subexpressions reduces floating
 * point operations
 *
 * http://www.cs.berkeley.edu/~jfc/mirtich/massProps.html
 *
 * Faces normals is conputed thanks to Newell’s method.
 * 
 * This class is immutable.
 * @author Pierre LABATUT
 */
public class PolyhedralMassProperties {

    private double mass;
    private final Vector3 centreofmass;
    private final InertiaMatrix inertia;

    private static double SQR(double s) {
        return s * s;
    }

    private static double CUBE(double s) {
        return s * s * s;
    }

    /**
     * This constructor computes mass properties of the given polyhedra
     * @param hull the volume fo which mass properties are computed.
     * @param density coefficient used to convert volume into mass
     */
    public PolyhedralMassProperties(final QuickHull3D hull, double density) {
        /**
         * Extract a vector array and face indices from QuickHull3D object.
         */
        final int[][] facesIndices = hull.getFaces();
        final Vector3[] vectices = new Vector3[hull.getNumVertices()];
        {
            final double[] vecticesArray = new double[hull.getNumVertices() * 3];
            hull.getVertices(vecticesArray);
            for (int i = 0; i < hull.getNumVertices(); i++) {
                final int i3 = i * 3;
                vectices[i] =
                        new Vector3(vecticesArray[i3], vecticesArray[i3 + 1], vecticesArray[i3 + 2]);
            }
        }

        /**
         * Compute faces normals using Newell's method. It is assumed that faces
         * are made of vertices in counter clock-wize order.
         * This algorythm is designed of planear faces convex & concave. It
         * retruns a normal and an offset.
         * offset = p0 dot n where n is the face normal and p0 is a vertex of
         * the face
         * It is robust to degenerated triangles (when to vertices are at the same place)
         * It returns  normal =(0,0,0) and  offset=0 for faces of area 0.
         */
        // Results
        final Vector3[] normals = new Vector3[facesIndices.length];
        final double[] offset = new double[facesIndices.length];

        for (int i = 0; i < facesIndices.length; i++) {
            final int[] faceIdx = facesIndices[i];
            /**
             * The components of the normal vector (a,b,c) is proportional to
             * the signed area of the projected polygon on yz, zx, and xy planes.
             * Newell’s method computes each one of these projected area s as
             * the sum of the “signed” areas of a trapezoid.
             */
            if (faceIdx.length < 3) {
                throw new RuntimeException("Degenerated face detected");
            }
            // The first vertex is used as the origin for computation
            final Vector3 p0 = vectices[faceIdx[0]];
            // u and v are two consecutive vectors of a fan
            final Vector3 u = new Vector3();
            final Vector3 v = new Vector3(vectices[faceIdx[1]]).assignSub(p0);
            final Vector3 n = normals[i] = new Vector3();
            for (int j = 2; j < faceIdx.length; j++) {

                u.assign(v);
                v.assign(vectices[faceIdx[j]]).assignSub(p0);
                //n += u^v
                n.x += u.y * v.z - u.z * v.y;
                n.y += u.z * v.x - u.x * v.z;
                n.z += u.x * v.y - u.y * v.x;
            }
            offset[i] = -p0.dot(n);
        }

        // 3 constants standing for dimentsions ordinal
        final int X = 0;
        final int Y = 1;
        final int Z = 2;

        double T0 = 0.; // Volume
        double T1[] = new double[3]; // Mass Center
        double T2[] = new double[3]; // Inertia diagonal
        double TP[] = new double[3]; // Inertia upper triangle

        for (int i = 0; i < facesIndices.length; i++) {
            // Get indices and normal of the face
            final int[] f = facesIndices[i];
            final Vector3 n = normals[i];


            /**
             * C is set to the largest coordinate ordinal for this vector
             * Maybe is this someting interesting to add to Vector3
             *
             * Green's theorem reduces an integral over a planear region to an
             * integral around it's one-dimentional boundary.
             *
             * The following code aims at finding A-B-C axes, a right handed
             * permutation of x-y-z that maximizes the area of the projected
             * face of A-B.
             *
             * Then integration is performed on faces projection over A-B
             */
            final int A, B, C;
            {
                // C is the main component of the vecore amon x-y-z
                double nx = Math.abs(n.x);
                double ny = Math.abs(n.y);
                double nz = Math.abs(n.z);
                if (nx > ny && nx > nz) {
                    C = X;
                } else {
                    C = (ny > nz) ? Y : Z;
                }
                // A & B are computed so that A,B,C is a right handed
                // permutation of x-y-z
                A = (C + 1) % 3;
                B = (A + 1) % 3;
            }


            /**
             * Integration over projected face perimeter
             */
            double P1 = 0, Pa = 0, Pb = 0, Paa = 0, Pab = 0, Pbb = 0, Paaa = 0;
            double Paab = 0, Pabb = 0, Pbbb = 0;
            {
                for (int j = 0; j < f.length; j++) {
                    //First vertex 2d coordinates
                    double a0 = vectices[f[j]].get(A);
                    double b0 = vectices[f[j]].get(B);
                    //Second vertex 2d coordinates
                    double a1 = vectices[f[(j + 1) % f.length]].get(A);
                    double b1 = vectices[f[(j + 1) % f.length]].get(B);

                    double da = a1 - a0;
                    double db = b1 - b0;
                    double a0_2 = a0 * a0;
                    double a0_3 = a0_2 * a0;
                    double a0_4 = a0_3 * a0;
                    double b0_2 = b0 * b0;
                    double b0_3 = b0_2 * b0;
                    double b0_4 = b0_3 * b0;
                    double a1_2 = a1 * a1;
                    double a1_3 = a1_2 * a1;
                    double b1_2 = b1 * b1;
                    double b1_3 = b1_2 * b1;

                    double C1 = a1 + a0;
                    double Ca = a1 * C1 + a0_2;
                    double Caa = a1 * Ca + a0_3;
                    double Caaa = a1 * Caa + a0_4;
                    double Cb = b1 * (b1 + b0) + b0_2;
                    double Cbb = b1 * Cb + b0_3;
                    double Cbbb = b1 * Cbb + b0_4;
                    double Cab = 3 * a1_2 + 2 * a1 * a0 + a0_2;
                    double Kab = a1_2 + 2 * a1 * a0 + 3 * a0_2;
                    double Caab = a0 * Cab + 4 * a1_3;
                    double Kaab = a1 * Kab + 4 * a0_3;
                    double Cabb =
                            4 * b1_3 + 3 * b1_2 * b0 + 2 * b1 * b0_2 + b0_3;
                    double Kabb =
                            b1_3 + 2 * b1_2 * b0 + 3 * b1 * b0_2 + 4 * b0_3;

                    P1 += db * C1;
                    Pa += db * Ca;
                    Paa += db * Caa;
                    Paaa += db * Caaa;
                    Pb += da * Cb;
                    Pbb += da * Cbb;
                    Pbbb += da * Cbbb;
                    Pab += db * (b1 * Cab + b0 * Kab);
                    Paab += db * (b1 * Caab + b0 * Kaab);
                    Pabb += da * (a1 * Cabb + a0 * Kabb);
                }

                P1 /= 2.0;
                Pa /= 6.0;
                Paa /= 12.0;
                Paaa /= 20.0;
                Pb /= -6.0;
                Pbb /= -12.0;
                Pbbb /= -20.0;
                Pab /= 24.0;
                Paab /= 60.0;
                Pabb /= -60.0;
            }
            /**
             * Compute faces integrals
             */
            {

                double w = offset[i];
                double k1 = 1 / n.get(C);
                double k2 = k1 * k1;
                double k3 = k2 * k1;
                double k4 = k3 * k1;

                double Fa = k1 * Pa;
                double Fb = k1 * Pb;
                double Fc = -k2 * (n.get(A) * Pa + n.get(B) * Pb + w * P1);

                double Faa = k1 * Paa;
                double Fbb = k1 * Pbb;
                double Fcc = k3 * (SQR(n.get(A)) * Paa + 2 * n.get(A) * n.get(B) * Pab + SQR(n.get(B)) * Pbb
                        + w * (2 * (n.get(A) * Pa + n.get(B) * Pb) + w * P1));

                double Faaa = k1 * Paaa;
                double Fbbb = k1 * Pbbb;
                double Fccc =
                        -k4 * (CUBE(n.get(A)) * Paaa + 3 * SQR(n.get(A)) * n.get(B) * Paab
                        + 3 * n.get(A) * SQR(n.get(B)) * Pabb + CUBE(n.get(B)) * Pbbb
                        + 3 * w * (SQR(n.get(A)) * Paa + 2 * n.get(A) * n.get(B) * Pab + SQR(n.get(B)) * Pbb)
                        + w * w * (3 * (n.get(A) * Pa + n.get(B) * Pb) + w * P1));

                double Faab = k1 * Paab;
                double Fbbc =
                        -k2 * (n.get(A) * Pabb + n.get(B) * Pbbb + w * Pbb);
                double Fcca = k3 * (SQR(n.get(A)) * Paaa + 2 * n.get(A) * n.get(B) * Paab + SQR(n.get(B)) * Pabb
                        + w * (2 * (n.get(A) * Paa + n.get(B) * Pab) + w * Pa));


                T0 += n.x * ((A == X) ? Fa : ((B == X) ? Fb : Fc));

                T1[A] += n.get(A) * Faa;
                T1[B] += n.get(B) * Fbb;
                T1[C] += n.get(C) * Fcc;
                T2[A] += n.get(A) * Faaa;
                T2[B] += n.get(B) * Fbbb;
                T2[C] += n.get(C) * Fccc;
                TP[A] += n.get(A) * Faab;
                TP[B] += n.get(B) * Fbbc;
                TP[C] += n.get(C) * Fcca;
            }
        }

        T1[X] /= 2;
        T1[Y] /= 2;
        T1[Z] /= 2;
        T2[X] /= 3;
        T2[Y] /= 3;
        T2[Z] /= 3;
        TP[X] /= 2;
        TP[Y] /= 2;
        TP[Z] /= 2;

        /**
         * Pack the result into data structures
         */
        centreofmass = new Vector3(T1[X] / T0, T1[Y] / T0, T1[Z] / T0);
        mass = T0 * density;
        inertia = new InertiaMatrix();
        inertia.a11 = density * (T2[Y] + T2[Z]);
        inertia.a22 = density * (T2[Z] + T2[X]);
        inertia.a33 = density * (T2[X] + T2[Y]);
        inertia.a12 = inertia.a21 = -density * TP[X];
        inertia.a23 = inertia.a32 = -density * TP[Y];
        inertia.a31 = inertia.a13 = -density * TP[Z];
    }

    /**
     * Return a copy of the computed center of mass. The returned object can be
     * modified by caller.
     * @return a new copy of the center of mass
     */
    public Vector3 getCentreOfMass() {
        return new Vector3(centreofmass);
    }

    /**
     * Return a copy of the computed inertia matrix.  The returned object can be
     * modified by caller.
     * @return a new copy of the inertia matrix
     */
    public InertiaMatrix getInertiaMatrix() {
        return new InertiaMatrix(inertia);
    }
    /**
     * Return the computed mass.
     * @return the mass of the convex hull
     */
    public double getMass() {
        return mass;
    }
}
