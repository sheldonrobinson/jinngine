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

public final class Quaternion {
    /**
     * variables
     */
    public double w = 0.0;
    public double x = 0.0;
    public double y = 0.0;
    public double z = 0.0;

    public Quaternion() {}

    /**
     * Construct a new quaternion using the given scalar and vector parts
     */
    public Quaternion(final double w, final double x, final double y, final double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Return a quaternion representing a rotation of theta radians about the given n axis
     * 
     * @param theta
     * @param n
     * @return
     */
    public static Quaternion rotation(final double theta, final Vector3 n) {
        final double f = Math.sin(theta / 2.0);
        return new Quaternion(Math.cos(theta / 2.0), n.x * f, n.y * f, n.z * f);
    }

    /**
     * Multiply this quaternion by the quaternion q, and return the result in a new quaternion instance
     */
    public final Quaternion multiply(final Quaternion q) {
        // q*q' = [ ss'- v*v', sv' + s'v + v x v' ]
        final Quaternion qm = new Quaternion();
        qm.w = w * q.w - x * q.x - y * q.y - z * q.z;
        qm.x = q.x * w + x * q.w + y * q.z - z * q.y;
        qm.y = q.y * w + y * q.w + z * q.x - x * q.z;
        qm.z = q.z * w + z * q.w + x * q.y - y * q.x;
        return qm;
    }

    /**
     * Multiply this quaternion by the quaternion q
     */
    public final Quaternion assignMultiply(final Quaternion q) {
        final double wt = w * q.w - x * q.x - y * q.y - z * q.z;
        final double xt = q.x * w + x * q.w + y * q.z - z * q.y;
        final double yt = q.y * w + y * q.w + z * q.x - x * q.z;
        final double zt = q.z * w + z * q.w + x * q.y - y * q.x;
        w = wt;
        x = xt;
        y = yt;
        z = zt;
        return this;
    }

    /**
     * Multiply this quaternion by the scalar s
     */
    public final Quaternion assignMultiply(final double s) {
        w *= s;
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    /**
     * Assign this quaternion to [s,v]
     */
    public final void assign(final double s, final Vector3 v) {
        w = s;
        x = v.x;
        y = v.y;
        z = v.z;
    }

    /**
     * Assign this quaternion from q1
     */
    public final void assign(final Quaternion q1) {
        w = q1.w;
        x = q1.x;
        y = q1.y;
        z = q1.z;
    }

    /**
     * Assign this quaternion from rotation matrix
     */
    public final void assign(final Matrix3 m) {
        // TODO needs testing
        w = Math.sqrt(1.0 + m.a11 + m.a22 + m.a33) / 2.0;
        final double w4 = 4.0 * w;
        x = (m.a32 - m.a23) / w4;
        y = (m.a13 - m.a31) / w4;
        z = (m.a21 - m.a12) / w4;
    }

    /**
     * Apply this quaternion as a rotation to the vector v
     */
    public Vector3 rotate(final Vector3 v) {
        // PBA Theorem 18.42 p'= qpq*
        // p is quaternion [0,(x,y,z)]
        // p' is the rotatet result
        // q is the unit quaternion representing a rotation
        // q* is the conjugated q
        final Quaternion vq = new Quaternion(0.0f, v.x, v.y, v.z);
        final Quaternion rotatet = this.multiply(vq).multiply(conjugate());

        return new Vector3(rotatet.x, rotatet.y, rotatet.z);
    }

    /**
     * Add the quaternion q to this quaternion
     */
    public Quaternion add(final Quaternion q) {
        return new Quaternion(w + q.w, x + q.x, y + q.y, z + q.z);
    }

    /**
     * Add the quaternion q to this quaternion
     */
    public Quaternion assignAdd(final Quaternion q) {
        w += q.w;
        x += q.x;
        y += q.y;
        z += q.z;
        return this;
    }

    /**
     * Add the product q*s to this quaternion, so this += qs
     */
    public Quaternion assignAddProduct(final Quaternion q, final double s) {
        w += q.w * s;
        x += q.x * s;
        y += q.y * s;
        z += q.z * s;
        return this;
    }

    /**
     * Multiply this quaternion by the given scalar a
     */
    public Quaternion multiply(final double a) {
        return new Quaternion(w * a, x * a, y * a, z * a);
    }

    /**
     * Return the 2-norm of this quaternion
     */
    public double norm() {
        return Math.sqrt(w * w + x * x + y * y + z * z);
    }

    /**
     * Conjugate this quaternion, so q=(s,v) becomes (s,-v)
     */
    public Quaternion conjugate() {
        return new Quaternion(w, -x, -y, -z);
    }

    /**
     * Conjugate this quaternion
     */
    public final Quaternion assignConjugate() {
        x *= -1;
        y *= -1;
        z *= -1;
        return this;
    }

    /**
     * Convert the given quaternion q into the rotation matrix R. The result is placed in the given Matrix3 R, and the
     * reference for R is returned
     */
    public static Matrix3 rotationMatrix3(final Quaternion v, final Matrix3 R) {
        final double s = v.w;
        R.assign(1 - 2 * (v.y * v.y + v.z * v.z), 2 * v.x * v.y - 2 * s * v.z, 2 * s * v.y + 2 * v.x * v.z, 2 * v.x
                * v.y + 2 * s * v.z, 1 - 2 * (v.x * v.x + v.z * v.z), -2 * s * v.x + 2 * v.y * v.z, -2 * s * v.y + 2
                * v.x * v.z, 2 * s * v.x + 2 * v.y * v.z, 1 - 2 * (v.x * v.x + v.y * v.y));

        return R;
    }

    // /**
    // * Convert this quaternion into a new rotation matrix
    // * @return A new rotation matrix
    // */
    // public Matrix3 toRotationMatrix3() {
    // return Quaternion.rotationMatrix3(this, new Matrix3() );
    // }

    /**
     * Convert the given quaternion q into the rotation matrix R. The result is placed in the given Matrix3 R, and the
     * reference for R is returned
     */
    public final Matrix3 toRotationMatrix3(final Matrix3 R) {
        final Quaternion v = this;
        final double s = w;
        R.assign(1 - 2 * (v.y * v.y + v.z * v.z), 2 * v.x * v.y - 2 * s * v.z, 2 * s * v.y + 2 * v.x * v.z, 2 * v.x
                * v.y + 2 * s * v.z, 1 - 2 * (v.x * v.x + v.z * v.z), -2 * s * v.x + 2 * v.y * v.z, -2 * s * v.y + 2
                * v.x * v.z, 2 * s * v.x + 2 * v.y * v.z, 1 - 2 * (v.x * v.x + v.y * v.y));

        return R;
    }

    /**
     * Normalise this quaternion
     */
    public void assignNormalize() {
        final double l = Math.sqrt(w * w + x * x + y * y + z * z);
        w = w / l;
        x = x / l;
        y = y / l;
        z = z / l;
    }

    /**
     * The inner product of this quaternion and the given quaternion q
     */
    public final double dot(final Quaternion q) {
        return w * q.w + x * q.x + y * q.y + z * q.z;
    }

    /**
     * Calculate the XYZ Euler angles from the unit quaternion q.
     * 
     * The euler angles, [phi,theta,psi], will reflect the rotation obtained by the rotation matrix R(q) =
     * Rz(psi)Ry(theta)Rx(phi), where R(q) is the rotation matrix computed directly from the unit quaternion q, and
     * Rx(phi) is the rotation matrix that rotates phi radians about the x-axis, etc.
     */
    public final Vector3 toEuler(final Vector3 euler) {
        final Quaternion q = this;
        // partially calculate the rotation matrix
        final double m11 = 1 - 2 * (q.y * q.y + q.z * q.z); // final double m12 = 2*q1.v.x*q1.v.y-2*q1.s*q1.v.z; final
                                                            // double m13 = 2*q1.s*q1.v.y+2*q1.v.x*q1.v.z;
        final double m21 = 2 * q.x * q.y + 2 * q.w * q.z; // final double m22 = 1-2*(q1.v.x*q1.v.x+q1.v.z*q1.v.z); final
                                                          // double m23 = -2*q1.s*q1.v.x+2*q1.v.y*q1.v.z;
        final double m31 = -2 * q.w * q.y + 2 * q.x * q.z;
        final double m32 = 2 * q.w * q.x + 2 * q.y * q.z;
        final double m33 = 1 - 2 * (q.x * q.x + q.y * q.y);

        // calculate the euler angles using the atan2() function
        final double phi = Math.atan2(m32, m33);
        final double theta = Math.atan2(-m31, m32 * Math.sin(phi) + m33 * Math.cos(phi));
        final double psi = Math.atan2(m21, m11);

        // return result
        euler.assign(phi, theta, psi);
        return euler;
    }

    /**
     * Default toString() override
     */
    @Override
    public final String toString() {
        return "[ " + w + "," + x + "," + y + "," + z + "]";
    }
}
