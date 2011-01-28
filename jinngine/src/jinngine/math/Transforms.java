/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.math;

/**
 * Various geometric transformations on matrix form
 */
public final class Transforms {
    /**
     * Return a 4x4 translation matrix for homogeneus coordinates
     * 
     * @param r
     *            Vector representing a translation
     * @return A 4x4 translation matrix
     */
    public final static Matrix4 translate4(final Vector3 r) {
        final Matrix4 T = Matrix4.identity();
        T.a14 = r.x;
        T.a24 = r.y;
        T.a34 = r.z;
        return T;
    }

    /**
     * Given homogeneous transformation matrix, return translation vector
     * 
     * @param M
     *            transformation matrix
     * @return translation vector
     */
    public final static Vector3 translation(final Matrix4 M) {
        return new Vector3(M.a14, M.a24, M.a34);
    }

    /**
     * Create a 4x4 rotation matrix for homogeneous coordinates
     * 
     * @param q
     *            Quaternion representing a rotation i R3 space
     * @return A 4x4 rotation matrix
     */
    public final static Matrix4 rotate4(final Quaternion q) {
        final Matrix4 M = new Matrix4();
        // Vector3 v = q.v;
        final double s = q.w;
        M.a11 = 1 - 2 * (q.y * q.y + q.z * q.z);
        M.a12 = 2 * q.x * q.y - 2 * s * q.z;
        M.a13 = 2 * s * q.y + 2 * q.x * q.z;
        M.a21 = 2 * q.x * q.y + 2 * s * q.z;
        M.a22 = 1 - 2 * (q.x * q.x + q.z * q.z);
        M.a23 = -2 * s * q.x + 2 * q.y * q.z;
        M.a31 = -2 * s * q.y + 2 * q.x * q.z;
        M.a32 = 2 * s * q.x + 2 * q.y * q.z;
        M.a33 = 1 - 2 * (q.x * q.x + q.y * q.y);
        M.a44 = 1;
        return M;
    }

    // 1 0 0 x r r r 0 r r r x
    // 0 1 0 y r r r 0 = r r r y
    // 0 0 1 z r r r 0 r r r z
    // 0 0 0 1 0 0 0 1 0 0 0 1

    /**
     * Create a combined rotation and translation matrix, in described order, T(r)R(q)
     * 
     * @param q
     *            Quaternion representing a rotation
     * @param r
     *            Vector representing translation
     * @return Combined rotation and translation matrix
     */
    public final static Matrix4 rotateAndTranslate4(final Quaternion q, final Vector3 r) {
        final Matrix4 M = new Matrix4();
        // Vector3 v = q.v;
        final double s = q.w;
        M.a11 = 1 - 2 * (q.y * q.y + q.z * q.z);
        M.a12 = 2 * q.x * q.y - 2 * s * q.z;
        M.a13 = 2 * s * q.y + 2 * q.x * q.z;
        M.a14 = r.x;
        M.a21 = 2 * q.x * q.y + 2 * s * q.z;
        M.a22 = 1 - 2 * (q.x * q.x + q.z * q.z);
        M.a23 = -2 * s * q.x + 2 * q.y * q.z;
        M.a24 = r.y;
        M.a31 = -2 * s * q.y + 2 * q.x * q.z;
        M.a32 = 2 * s * q.x + 2 * q.y * q.z;
        M.a33 = 1 - 2 * (q.x * q.x + q.y * q.y);
        M.a34 = r.z;
        M.a44 = 1;
        return M;

    }

    /**
     * Create a combined transform and translation matrix, in described order, T(r)B
     * 
     * @param B
     *            Matrix representing a transform
     * @param r
     *            Vector representing translation
     * @return Combined transform and translation matrix
     */
    public final static Matrix4 transformAndTranslate4(final Matrix3 B, final Vector3 r) {
        final Matrix4 M = new Matrix4();
        M.a11 = B.a11;
        M.a12 = B.a12;
        M.a13 = B.a13;
        M.a14 = r.x;
        M.a21 = B.a21;
        M.a22 = B.a22;
        M.a23 = B.a23;
        M.a24 = r.y;
        M.a31 = B.a31;
        M.a32 = B.a32;
        M.a33 = B.a33;
        M.a34 = r.z;
        M.a44 = 1;
        return M;
    }

    /**
     * Create a scaling transform scaled in the entries of vector s
     * 
     * @param s
     * @return
     */
    public final static Matrix4 scale(final Vector3 s) {
        final Matrix4 M = new Matrix4();
        M.a11 = s.x;
        M.a12 = 0;
        M.a13 = 0;
        M.a14 = 0;
        M.a21 = 0;
        M.a22 = s.y;
        M.a23 = 0;
        M.a24 = 0;
        M.a31 = 0;
        M.a32 = 0;
        M.a33 = s.z;
        M.a34 = 0;
        M.a44 = 1;
        return M;

    }

}
