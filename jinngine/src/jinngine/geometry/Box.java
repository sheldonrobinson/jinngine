/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.geometry;

import java.util.Iterator;

import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Transforms;
import jinngine.math.Vector3;
import jinngine.physics.Body;

/**
 * A box geometry implementation
 */
public class Box implements SupportMap3, Geometry, Material {

    // transforms and body reference
    private Body body;
    private final Matrix3 localrotation = new Matrix3();
    private final Vector3 localdisplacement = new Vector3();
    private final Vector3 worldMaximumBounds = new Vector3();
    private final Vector3 worldMinimumBounds = new Vector3();
    private final Matrix4 worldTransform = new Matrix4();

    // box properties
    private final double xs, ys, zs;
    private double mass;
    private final String name;
    private double envelope = 0.125;
    private final double sphereSweepRadius;
    private double correction = 2;

    // auxiliary user reference
    private Object auxiliary;

    // material settings (defaults)
    private double restitution = 0.7;
    private double friction = 0.5;

    /**
     * Create a box with the given side lengths
     * 
     * @param x
     *            Box x-axis extend
     * @param y
     *            Box y-axis extend
     * @param z
     *            Box z-axis extend
     */
    public Box(final String name, final double x, final double y, final double z) {
        this.name = new String(name);
        xs = x;
        ys = y;
        zs = z;
        mass = xs * ys * zs;
        sphereSweepRadius = 0;

        // set the local transform
        setLocalTransform(Matrix3.identity(), new Vector3());
    }

    /**
     * Create a box with the given side lengths and a sphere sweeping radius
     * 
     * @param x
     *            Box x-axis extend
     * @param y
     *            Box y-axis extend
     * @param z
     *            Box z-axis extend
     */
    public Box(final String name, final double x, final double y, final double z, final double radius) {
        this.name = new String(name);
        xs = x;
        ys = y;
        zs = z;
        mass = xs * ys * zs;
        sphereSweepRadius = radius;

        // set the local transform
        setLocalTransform(Matrix3.identity(), new Vector3());
    }

    /**
     * Create a box with the given side lengths
     * 
     * @param x
     *            Box x-axis extend
     * @param y
     *            Box y-axis extend
     * @param z
     *            Box z-axis extend
     */
    public Box(final String name, final Vector3 sides) {
        this.name = new String(name);
        xs = sides.x;
        ys = sides.y;
        zs = sides.z;
        mass = xs * ys * zs;
        sphereSweepRadius = 0;

        // set the local transform
        setLocalTransform(Matrix3.identity(), new Vector3());
    }

    // /**
    // * Create a new box with the given side lengths and a local translation
    // * @param x Box x-axis extend
    // * @param y Box y-axis extend
    // * @param z Box z-axis extend
    // * @param posx Box local x-axis translation
    // * @param posy Box local y-axis translation
    // * @param posz Box local z-axis translation
    // */
    // public Box(double x, double y, double z, double posx, double posy, double posz) {
    // this.xs = x; this.ys = y; this.zs = z;
    // mass = xs*ys*zs;
    //
    // //set the local transform
    // setLocalTransform( Matrix3.identity(), new Vector3(posx,posy,posz) );
    // }

    // /**
    // * Set new side lengths for this box. Keep in mind that altering geometry changes mass and
    // * inertia properties of bodies. This method automatically re-finalises the attached body,
    // * should this box be attached to one. This operation is relatively expensive.
    // */
    // public final void setBoxSideLengths( double xl, double yl, double zl) {
    // this.xs = xl; this.ys = yl; this.zs = zl;
    // mass = xl*yl*zl;
    //
    // // re-finalise body if any present
    // if ( body != null)
    // body.finalize();
    // }

    // user auxiliary methods
    @Override
    public Object getUserReference() {
        return auxiliary;
    }

    @Override
    public void setUserReference(final Object auxiliary) {
        this.auxiliary = auxiliary;
    }

    @Override
    public Vector3 supportPoint(final Vector3 direction, final Vector3 result) {
        // transform direction into model space (use result as placeholder)
        result.assign(direction);
        Matrix3.multiplyTransposed(body.state.rotation, result, result);
        Matrix3.multiplyTransposed(localrotation, result, result);

        // support point in model space
        result.x = result.x < 0 ? -xs * 0.5 : xs * 0.5;
        result.y = result.y < 0 ? -ys * 0.5 : ys * 0.5;
        result.z = result.z < 0 ? -zs * 0.5 : zs * 0.5;

        // transform to world space
        Matrix3.multiply(localrotation, result, result);
        Vector3.add(result, localdisplacement);
        Matrix3.multiply(body.state.rotation, result, result);
        Vector3.add(result, body.state.position);
        return result;
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public void setBody(final Body b) {
        body = b;
    }

    @Override
    public InertiaMatrix getInertiaMatrix() {
        // standard inertia matrix for a box with variable side lengths
        final Matrix3 M = Matrix3.scaleMatrix(1.0f / 12.0f * 1 * (ys * ys + zs * zs), 1.0f / 12.0f * 1 * (xs * xs + zs
                * zs), 1.0f / 12.0f * 1 * (ys * ys + xs * xs));
        return new InertiaMatrix(M);
    }

    @Override
    public double getEnvelope() {
        return envelope;
    }

    @Override
    public void setEnvelope(final double envelope) {
        this.envelope = envelope;
    }

    @Override
    public void setLocalTransform(final Matrix3 rotation, final Vector3 displacement) {
        localdisplacement.assign(displacement);
        localrotation.assign(rotation);
    }

    @Override
    public void getLocalTransform(final Matrix3 R, final Vector3 b) {
        R.assign(localrotation);
        b.assign(localdisplacement);
    }

    @Override
    public void getLocalTranslation(final Vector3 t) {
        t.assign(localdisplacement);

    }

    @Override
    public final Vector3 getMaxBounds(final Vector3 bounds) {
        return bounds.assign(worldMaximumBounds);
    }

    private final Vector3 getMaxBoundsTmp2(final Vector3 bounds) {
        final Matrix4 T = worldTransform;

        // use the rotational part of the world transform to deduce the world space support
        // points for each axis
        bounds.assign(xs * (T.a11 < 0 ? -0.5 : 0.5) * T.a11 + ys * (T.a12 < 0 ? -0.5 : 0.5) * T.a12 + zs
                * (T.a13 < 0 ? -0.5 : 0.5) * T.a13, xs * (T.a21 < 0 ? -0.5 : 0.5) * T.a21 + ys
                * (T.a22 < 0 ? -0.5 : 0.5) * T.a22 + zs * (T.a23 < 0 ? -0.5 : 0.5) * T.a23, xs
                * (T.a31 < 0 ? -0.5 : 0.5) * T.a31 + ys * (T.a32 < 0 ? -0.5 : 0.5) * T.a32 + zs
                * (T.a33 < 0 ? -0.5 : 0.5) * T.a33);

        // account for translation in the body frame
        Vector3.transformAndAdd(body.state.rotation, localdisplacement, bounds);

        // translation of the body
        Vector3.add(bounds, body.state.position);

        // add sphere sweep radius and envelope
        bounds.x += sphereSweepRadius + envelope;
        bounds.y += sphereSweepRadius + envelope;
        bounds.z += sphereSweepRadius + envelope;

        return bounds;
    }

    // private final Vector3 getMaxBoundsTmp(Vector3 bounds) {
    // // find the pricipal axis of the box in world space
    // Matrix3 T = body.state.rotation.multiply(localrotation).transpose();
    // Vector3 vx = new Vector3(), vy = new Vector3(), vz = new Vector3();
    // T.getColumnVectors(vx, vy, vz);
    //
    // // support points in model space (with scaling)
    // Vector3 px = new Vector3( xs*(vx.x<0?-0.5:0.5), ys*(vx.y<0?-0.5:0.5), zs*(vx.z<0?-0.5:0.5) );
    // Vector3 py = new Vector3( xs*(vy.x<0?-0.5:0.5), ys*(vy.y<0?-0.5:0.5), zs*(vy.z<0?-0.5:0.5) );
    // Vector3 pz = new Vector3( xs*(vz.x<0?-0.5:0.5), ys*(vz.y<0?-0.5:0.5), zs*(vz.z<0?-0.5:0.5) );
    //
    // // local rotation
    // Matrix3.multiply( localrotation, px, px);
    // Matrix3.multiply( localrotation, py, py);
    // Matrix3.multiply( localrotation, pz, pz);
    //
    // // add local displacement
    // Vector3.add( px, localdisplacement);
    // Vector3.add( py, localdisplacement);
    // Vector3.add( pz, localdisplacement);
    //
    // // grab the row vectors of the body rotation (to save some matrix vector muls')
    // Matrix3 Tb = body.state.rotation;
    // Vector3 rx = new Vector3(), ry = new Vector3(), rz = new Vector3();
    // Tb.getRowVectors(rx, ry, rz);
    //
    // // return the final bounds, adding the envelope and sweep size
    // return bounds.assign(rx.dot(px)+sphereSweepRadius+envelope+body.state.position.x,
    // ry.dot(py)+sphereSweepRadius+envelope+body.state.position.y,
    // rz.dot(pz)+sphereSweepRadius+envelope+body.state.position.z);
    // }

    @Override
    public final Vector3 getMinBounds(final Vector3 bounds) {
        return bounds.assign(worldMinimumBounds);
    }

    private final Vector3 getMinBoundsTmp2(final Vector3 bounds) {
        final Matrix4 T = worldTransform;

        // use the rotational part of the world transform to deduce the world space support
        // points for each negative axis
        bounds.assign(xs * (-T.a11 < 0 ? -0.5 : 0.5) * T.a11 + ys * (-T.a12 < 0 ? -0.5 : 0.5) * T.a12 + zs
                * (-T.a13 < 0 ? -0.5 : 0.5) * T.a13, xs * (-T.a21 < 0 ? -0.5 : 0.5) * T.a21 + ys
                * (-T.a22 < 0 ? -0.5 : 0.5) * T.a22 + zs * (-T.a23 < 0 ? -0.5 : 0.5) * T.a23, xs
                * (-T.a31 < 0 ? -0.5 : 0.5) * T.a31 + ys * (-T.a32 < 0 ? -0.5 : 0.5) * T.a32 + zs
                * (-T.a33 < 0 ? -0.5 : 0.5) * T.a33);

        // account for translation in the body frame
        Vector3.transformAndAdd(body.state.rotation, localdisplacement, bounds);

        // translation of the body
        Vector3.add(bounds, body.state.position);

        // subtract sphere sweep radius and envelope
        bounds.x -= sphereSweepRadius + envelope;
        bounds.y -= sphereSweepRadius + envelope;
        bounds.z -= sphereSweepRadius + envelope;

        return bounds;
    }

    // private final Vector3 getMinBoundsTmp(Vector3 bounds) {
    // // get the column vectors of the transform
    // Matrix3 T = body.state.rotation.multiply(localrotation).transpose();
    // Vector3 vx = new Vector3(), vy = new Vector3(), vz = new Vector3();
    // T.getColumnVectors(vx, vy, vz);
    //
    // // invert vectors, because we are looking for minimum bounds
    // Vector3.multiply(vx, -1);
    // Vector3.multiply(vy, -1);
    // Vector3.multiply(vz, -1);
    //
    // // support points in body space (with scaling)
    // Vector3 px = new Vector3( xs*(vx.x<0?-0.5:0.5), ys*(vx.y<0?-0.5:0.5), zs*(vx.z<0?-0.5:0.5) );
    // Vector3 py = new Vector3( xs*(vy.x<0?-0.5:0.5), ys*(vy.y<0?-0.5:0.5), zs*(vy.z<0?-0.5:0.5) );
    // Vector3 pz = new Vector3( xs*(vz.x<0?-0.5:0.5), ys*(vz.y<0?-0.5:0.5), zs*(vz.z<0?-0.5:0.5) );
    //
    // // local rotation
    // Matrix3.multiply( localrotation, px, px);
    // Matrix3.multiply( localrotation, py, py);
    // Matrix3.multiply( localrotation, pz, pz);
    //
    // // add local displacement
    // Vector3.add( px, localdisplacement);
    // Vector3.add( py, localdisplacement);
    // Vector3.add( pz, localdisplacement);
    //
    // // grab the row vectors of the body rotation (to save some matrix vector muls')
    // Matrix3 Tb = body.state.rotation;
    // Vector3 rx = new Vector3(), ry = new Vector3(), rz = new Vector3();
    // Tb.getRowVectors(rx, ry, rz);
    //
    // // return final bounds, subtracting the envelope size
    // return bounds.assign(rx.dot(px)-sphereSweepRadius-envelope+body.state.position.x,
    // ry.dot(py)-sphereSweepRadius-envelope+body.state.position.y,
    // rz.dot(pz)-sphereSweepRadius-envelope+body.state.position.z);
    // }

    @Override
    public Matrix4 getWorldTransform() {
        // return Matrix4.multiply(body.getTransform(), Transforms.transformAndTranslate4(localrotation,
        // localdisplacement), new Matrix4());
        return worldTransform;
    }

    // @Override
    // public final void supportFeature(final Vector3 d, final ListIterator<Vector3> featureList) {
    // final double epsilon = 0.09;
    // //get d into the canonical box space
    // Vector3 v = body.state.rotation.multiply(localrotation).transpose().multiply(d);
    //
    // int numberOfZeroAxis = 0;
    // final int[] zeroAxisIndices = new int[3];
    // int numberOfNonZeroAxis = 0;
    // final int[] nonZeroAxisIndices = new int[3];
    //
    // if (Math.abs(v.x) < epsilon ) {
    // zeroAxisIndices[numberOfZeroAxis++]=0;
    // } else { nonZeroAxisIndices[ numberOfNonZeroAxis++] = 0; }
    // if (Math.abs(v.y) < epsilon ) {
    // zeroAxisIndices[numberOfZeroAxis++]=1;
    // } else { nonZeroAxisIndices[ numberOfNonZeroAxis++] = 1; }
    // if (Math.abs(v.z) < epsilon ) {
    // zeroAxisIndices[numberOfZeroAxis++]=2;
    // } else { nonZeroAxisIndices[ numberOfNonZeroAxis++] = 2; }
    //
    //
    // if (numberOfZeroAxis == 0) {
    // //eight possible points
    //
    // final double sv1 = v.x<0?-0.5:0.5;
    // final double sv2 = v.y<0?-0.5:0.5;
    // final double sv3 = v.z<0?-0.5:0.5;
    // //return Matrix4.multiply(transform4, new Vector3(sv1, sv2, sv3), new Vector3());
    // featureList.add( body.state.rotation.multiply(localrotation.multiply(new Vector3(xs*sv1, ys*sv2,
    // zs*sv3)).add(localdisplacement)).add(body.state.position) );
    // }
    //
    // else if (numberOfZeroAxis == 1) {
    // //System.out.println("edge case");
    //
    // //four possible edges
    // final Vector3 p1 = new Vector3(v.x<0?-0.5:0.5, v.y<0?-0.5:0.5, v.z<0?-0.5:0.5 );
    // final Vector3 p2 = new Vector3(v.x<0?-0.5:0.5, v.y<0?-0.5:0.5, v.z<0?-0.5:0.5 );
    // p1.set( zeroAxisIndices[0], 0.5);
    // p2.set( zeroAxisIndices[0], -0.5);
    //
    //
    //
    // featureList.add( body.state.rotation.multiply(localrotation.multiply(new Vector3(xs*p1.x, ys*p1.y, zs*p1.z)
    // ).add(localdisplacement)).add(body.state.position) );
    // featureList.add( body.state.rotation.multiply(localrotation.multiply(new Vector3(xs*p2.x, ys*p2.y, zs*p2.z)
    // ).add(localdisplacement)).add(body.state.position) );
    // }
    //
    // else if (numberOfZeroAxis == 2) {
    // //System.out.println("face case");
    // //two possible faces
    // //four possible edges
    // final Vector3 p1 = new Vector3(v.x<0?-0.5:0.5, v.y<0?-0.5:0.5, v.z<0?-0.5:0.5 );
    // final Vector3 p2 = new Vector3(v.x<0?-0.5:0.5, v.y<0?-0.5:0.5, v.z<0?-0.5:0.5 );
    // final Vector3 p3 = new Vector3(v.x<0?-0.5:0.5, v.y<0?-0.5:0.5, v.z<0?-0.5:0.5 );
    // final Vector3 p4 = new Vector3(v.x<0?-0.5:0.5, v.y<0?-0.5:0.5, v.z<0?-0.5:0.5 );
    //
    // // this makes sure that the returned set of points is always in counter
    // // clock-wise order wrt. the non zero axis direction.
    // switch( nonZeroAxisIndices[0]) {
    // case 0:
    // if (v.x > 0) {
    // p1.y = 0.5; p1.z = 0.5;
    // p2.y = -0.5; p2.z = 0.5;
    // p3.y = -0.5; p3.z = -0.5;
    // p4.y = 0.5; p4.z = -0.5;
    // } else {
    // p1.y = 0.5; p1.z = 0.5;
    // p2.y = 0.5; p2.z = -0.5;
    // p3.y = -0.5; p3.z = -0.5;
    // p4.y = -0.5; p4.z = 0.5;
    // }
    // break;
    // case 1:
    // if (v.y > 0) {
    // p1.z = 0.5; p1.x = 0.5;
    // p2.z = -0.5; p2.x = 0.5;
    // p3.z = -0.5; p3.x = -0.5;
    // p4.z = 0.5; p4.x = -0.5;
    // } else {
    // p1.z = 0.5; p1.x = 0.5;
    // p2.z = 0.5; p2.x = -0.5;
    // p3.z = -0.5; p3.x = -0.5;
    // p4.z = -0.5; p4.x = 0.5;
    // }
    // break;
    // case 2:
    // if (v.z > 0) {
    // p1.x = 0.5; p1.y = 0.5;
    // p2.x = -0.5; p2.y = 0.5;
    // p3.x = -0.5; p3.y = -0.5;
    // p4.x = 0.5; p4.y = -0.5;
    // } else {
    // p1.x = 0.5; p1.y = 0.5;
    // p2.x = 0.5; p2.y = -0.5;
    // p3.x = -0.5; p3.y = -0.5;
    // p4.x = -0.5; p4.y = 0.5;
    // }
    // break;
    // }
    //
    // // return transformed vertices
    // featureList.add( body.state.rotation.multiply(localrotation.multiply(new Vector3(xs*p1.x, ys*p1.y, zs*p1.z)
    // ).add(localdisplacement)).add(body.state.position) );
    // featureList.add( body.state.rotation.multiply(localrotation.multiply(new Vector3(xs*p2.x, ys*p2.y, zs*p2.z)
    // ).add(localdisplacement)).add(body.state.position) );
    // featureList.add( body.state.rotation.multiply(localrotation.multiply(new Vector3(xs*p3.x, ys*p3.y, zs*p3.z)
    // ).add(localdisplacement)).add(body.state.position) );
    // featureList.add( body.state.rotation.multiply(localrotation.multiply(new Vector3(xs*p4.x, ys*p4.y, zs*p4.z)
    // ).add(localdisplacement)).add(body.state.position) );
    // }
    //
    // else if (numberOfZeroAxis == 3) {
    // //should never happen, undefinded result
    // assert false;
    // }
    // }

    @Override
    public final void supportFeature(final Vector3 direction, final Iterator<Vector3> list) {
        final double epsilon = 0.25;
        boolean done = true;

        // we need at least one point
        final Vector3 p1 = list.next();

        // get d into model space. Use p1 as place holder for the new direction in
        // model space. When p1 is overwritten, v is no longer needed.
        final Vector3 v = p1;
        // v = (R_body R_geometry)^T d = R_g^T R_b^T d
        Matrix3.multiplyTransposed(body.state.rotation, direction, v);
        Matrix3.multiplyTransposed(localrotation, v, v);

        if (Math.abs(v.x) >= epsilon && Math.abs(v.y) >= epsilon && Math.abs(v.z) >= epsilon) {
            // support point
            p1.assign(v.x < 0 ? -0.5 : 0.5, v.y < 0 ? -0.5 : 0.5, v.z < 0 ? -0.5 : 0.5);
        } else {
            done = false;
        }

        // if done, transform the support point and return
        if (done) {
            // transform
            p1.assignScale(xs, ys, zs);
            Matrix3.multiply(localrotation, p1, p1);
            Vector3.add(p1, localdisplacement);
            Matrix3.multiply(body.state.rotation, p1, p1);
            Vector3.add(p1, body.state.position);
            return;
        } else {
            done = true;
        }

        // we need two points
        final Vector3 p2 = list.next();

        if (Math.abs(v.x) < epsilon && Math.abs(v.y) >= epsilon && Math.abs(v.z) >= epsilon) {
            // support points
            p1.assign(-0.5, v.y < 0 ? -0.5 : 0.5, v.z < 0 ? -0.5 : 0.5);
            p2.assign(0.5, v.y < 0 ? -0.5 : 0.5, v.z < 0 ? -0.5 : 0.5);
        } else if (Math.abs(v.x) >= epsilon && Math.abs(v.y) < epsilon && Math.abs(v.z) >= epsilon) {
            // support points
            p1.assign(v.x < 0 ? -0.5 : 0.5, -0.5, v.z < 0 ? -0.5 : 0.5);
            p2.assign(v.x < 0 ? -0.5 : 0.5, 0.5, v.z < 0 ? -0.5 : 0.5);
        } else if (Math.abs(v.x) >= epsilon && Math.abs(v.y) >= epsilon && Math.abs(v.z) < epsilon) {
            // support points
            p1.assign(v.x < 0 ? -0.5 : 0.5, v.y < 0 ? -0.5 : 0.5, -0.5);
            p2.assign(v.x < 0 ? -0.5 : 0.5, v.y < 0 ? -0.5 : 0.5, 0.5);
        } else {
            done = false;
        }

        // if done, transform points and return
        if (done) {
            // transform
            p1.assignScale(xs, ys, zs);
            Matrix3.multiply(localrotation, p1, p1);
            Vector3.add(p1, localdisplacement);
            Matrix3.multiply(body.state.rotation, p1, p1);
            Vector3.add(p1, body.state.position);

            // transform
            p2.assignScale(xs, ys, zs);
            Matrix3.multiply(localrotation, p2, p2);
            Vector3.add(p2, localdisplacement);
            Matrix3.multiply(body.state.rotation, p2, p2);
            Vector3.add(p2, body.state.position);
            return;
        } else {
            done = true;
        }

        // we need four points
        final Vector3 p3 = list.next();
        final Vector3 p4 = list.next();

        // for each case of supporting face, enforce counter
        // clock-wise order wrt. the non zero axis direction.
        if (Math.abs(v.y) < epsilon && Math.abs(v.z) < epsilon) {
            if (v.x >= 0) {
                p1.assign(.5, 0.5, 0.5);
                p2.assign(.5, -0.5, 0.5);
                p3.assign(.5, -0.5, -0.5);
                p4.assign(.5, 0.5, -0.5);
            } else {
                p1.assign(-.5, 0.5, 0.5);
                p2.assign(-.5, 0.5, -0.5);
                p3.assign(-.5, -0.5, -0.5);
                p4.assign(-.5, -0.5, 0.5);
            }
        } else if (Math.abs(v.z) < epsilon && Math.abs(v.x) < epsilon) {
            if (v.y >= 0) {
                p1.z = 0.5;
                p1.x = 0.5;
                p1.y = .5;
                p2.z = -0.5;
                p2.x = 0.5;
                p2.y = .5;
                p3.z = -0.5;
                p3.x = -0.5;
                p3.y = .5;
                p4.z = 0.5;
                p4.x = -0.5;
                p4.y = .5;
            } else {
                p1.z = 0.5;
                p1.x = 0.5;
                p1.y = -.5;
                p2.z = 0.5;
                p2.x = -0.5;
                p2.y = -.5;
                p3.z = -0.5;
                p3.x = -0.5;
                p3.y = -.5;
                p4.z = -0.5;
                p4.x = 0.5;
                p4.y = -.5;
            }
        } else if (Math.abs(v.x) < epsilon && Math.abs(v.y) < epsilon) {
            if (v.z >= 0) {
                p1.x = 0.5;
                p1.y = 0.5;
                p1.z = .5;
                p2.x = -0.5;
                p2.y = 0.5;
                p2.z = .5;
                p3.x = -0.5;
                p3.y = -0.5;
                p3.z = .5;
                p4.x = 0.5;
                p4.y = -0.5;
                p4.z = .5;
            } else {
                p1.x = 0.5;
                p1.y = 0.5;
                p1.z = -.5;
                p2.x = 0.5;
                p2.y = -0.5;
                p2.z = -.5;
                p3.x = -0.5;
                p3.y = -0.5;
                p3.z = -.5;
                p4.x = -0.5;
                p4.y = 0.5;
                p4.z = -.5;
            }
        } else {
            done = false;
        }

        // always done here
        if (done) {
            // transform p1
            p1.assignScale(xs, ys, zs);
            Matrix3.multiply(localrotation, p1, p1);
            Vector3.add(p1, localdisplacement);
            Matrix3.multiply(body.state.rotation, p1, p1);
            Vector3.add(p1, body.state.position);

            // transform p2
            p2.assignScale(xs, ys, zs);
            Matrix3.multiply(localrotation, p2, p2);
            Vector3.add(p2, localdisplacement);
            Matrix3.multiply(body.state.rotation, p2, p2);
            Vector3.add(p2, body.state.position);

            // transform p3
            p3.assignScale(xs, ys, zs);
            Matrix3.multiply(localrotation, p3, p3);
            Vector3.add(p3, localdisplacement);
            Matrix3.multiply(body.state.rotation, p3, p3);
            Vector3.add(p3, body.state.position);

            // transform p4
            p4.assignScale(xs, ys, zs);
            Matrix3.multiply(localrotation, p4, p4);
            Vector3.add(p4, localdisplacement);
            Matrix3.multiply(body.state.rotation, p4, p4);
            Vector3.add(p4, body.state.position);
            return;
        } else {
            done = true;
        }
    }

    // Material getters and setters
    @Override
    public double getFrictionCoefficient() {
        return friction;
    }

    @Override
    public double getRestitution() {
        return restitution;
    }

    @Override
    public void setFrictionCoefficient(final double f) {
        friction = f;
    }

    @Override
    public void setRestitution(final double e) {
        restitution = e;
    }

    @Override
    public double getMass() {
        return mass;
    }

    public void setMass(final double mass) {
        this.mass = mass;
    }

    /**
     * Return the side lengths of this box
     * 
     * @return
     */
    public Vector3 getDimentions() {
        return new Vector3(xs, ys, zs);
    }

    @Override
    public void setLocalScale(final Vector3 s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double sphereSweepRadius() {
        return sphereSweepRadius;
    }

    @Override
    public Vector3 getLocalCentreOfMass(final Vector3 cm) {
        return cm.assignZero();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void update() {
        // update world transform
        Matrix4.multiply(body.getTransform(), Transforms.transformAndTranslate4(localrotation, localdisplacement),
                worldTransform);

        // update world bounding box
        getMaxBoundsTmp2(worldMaximumBounds);
        getMinBoundsTmp2(worldMinimumBounds);
    }

    @Override
    public double getCorrectionVelocityLimit() {
        return correction;
    }

    @Override
    public void setCorrectionVelocityLimit(final double limit) {
        correction = limit;
    }

}
