/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.test.unit;

import java.util.Vector;
import jinngine.math.Matrix4;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 *
 * @author PYW
 */
public class Matrix3Test {

    private static void assertMatrixEquals(double[] ref, Matrix3 val, double tolerance) {
        junit.framework.Assert.assertEquals(ref[0], val.a11, tolerance);
        junit.framework.Assert.assertEquals(ref[1], val.a12, tolerance);
        junit.framework.Assert.assertEquals(ref[2], val.a13, tolerance);
        junit.framework.Assert.assertEquals(ref[3], val.a21, tolerance);
        junit.framework.Assert.assertEquals(ref[4], val.a22, tolerance);
        junit.framework.Assert.assertEquals(ref[5], val.a23, tolerance);
        junit.framework.Assert.assertEquals(ref[6], val.a31, tolerance);
        junit.framework.Assert.assertEquals(ref[7], val.a32, tolerance);
        junit.framework.Assert.assertEquals(ref[8], val.a33, tolerance);
    }

    private static void assertMatrixEquals(double[] ref, Matrix3 val) {
        assertMatrixEquals(ref, val, 0.);
    }

    @Test
    public void testCtorZero() {
        final Matrix3 m = new Matrix3();
        assertMatrixEquals(new double[]{
                    0., 0., 0.,
                    0., 0., 0.,
                    0., 0., 0.}, m);
    }

    @Test
    public void testAssignZero() {
        final Matrix3 m = new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Matrix3 r = m.assignZero();
        assertSame(r, m);//assert it return this
        //assert every value is 0.
        assertMatrixEquals(new double[]{
                    0., 0., 0.,
                    0., 0., 0.,
                    0., 0., 0.}, m);
    }

    @Test
    public void testCtor() {
        final Matrix3 m = new Matrix3(1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);
    }

    @Test
    public void testAssign() {
        final Matrix3 m = new Matrix3();
        final Matrix3 r = m.assign(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        assertSame(r, m);//assert it return this
        //assert every value is 0.
        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);
    }

    @Test
    public void testCtor02() {
        final Matrix3 m = new Matrix3(
                new Vector3(1, 4, 7),
                new Vector3(2, 5, 8),
                new Vector3(3, 6, 9));

        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);
    }

    @Test(expected = NullPointerException.class)
    public void testCtor03() {
        final Matrix3 m = new Matrix3(null,
                new Vector3(),
                new Vector3());
    }

    @Test(expected = NullPointerException.class)
    public void testCtor04() {
        final Matrix3 m = new Matrix3(
                new Vector3(),
                null,
                new Vector3());
    }

    @Test(expected = NullPointerException.class)
    public void testCtor05() {
        final Matrix3 m = new Matrix3(
                new Vector3(),
                new Vector3(),
                null);
    }

    @Test
    public void testCtorMatrix() {
        final Matrix3 m = new Matrix3(new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.));

        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);
    }

    @Test
    public void testAssignMatrix() {
        final Matrix3 m = new Matrix3();
        final Matrix3 r = m.assign(new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.));
        assertSame(r, m);//assert it return this
        //assert every value is 0.
        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);

    }

    @Test
    public void testCtorMatrix4() {
        final Matrix3 m = new Matrix3(new Matrix4(
                1., 2., 3., -1.,
                4., 5., 6., -1.,
                7., 8., 9., -1.,
                -1., -1., -1., -1.));

        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);
    }

    @Test
    public void testFtorIdentity() {
        final Matrix3 m = Matrix3.identity();
        final Matrix3 n = Matrix3.identity();
        assertNotSame(n, m);//create a new referene everytime like a ctor
        assertMatrixEquals(new double[]{
                    1., 0., 0.,
                    0., 1., 0.,
                    0., 0., 1.}, m);
    }

    @Test
    public void testAssignIdentity() {
        final Matrix3 m = new Matrix3(
                7., 2., 3.,
                4., 5., 6.,
                1., 8., 9.);
        final Matrix3 r = m.assignIdentity();
        assertSame(r, m);//assert it return this
        assertMatrixEquals(new double[]{
                    1., 0., 0.,
                    0., 1., 0.,
                    0., 0., 1.}, m);
    }

    @Test
    public void testFtorScaleDouble() {
        final Matrix3 m = Matrix3.scaleMatrix(3.);
        final Matrix3 n = Matrix3.scaleMatrix(3.);
        assertNotSame(n, m);//create a new referene everytime like a ctor
        assertMatrixEquals(new double[]{
                    3., 0., 0.,
                    0., 3., 0.,
                    0., 0., 3.}, m);
    }

    @Test
    public void testAssignScaleDouble() {
        final Matrix3 m = new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Matrix3 r = m.assignScale(3.);
        assertSame(r, m);//assert it return this
        assertMatrixEquals(new double[]{
                    3., 0., 0.,
                    0., 3., 0.,
                    0., 0., 3.}, m);
    }

    @Test
    public void testFtorScale3Double() {
        final Matrix3 m = Matrix3.scaleMatrix(2., 3., 4.);
        final Matrix3 n = Matrix3.scaleMatrix(2., 3., 4.);
        assertNotSame(n, m);//create a new referene everytime like a ctor
        assertMatrixEquals(new double[]{
                    2., 0., 0.,
                    0., 3., 0.,
                    0., 0., 4.}, m);
    }

    @Test
    public void testAssign3Double() {
        final Matrix3 m = new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Matrix3 r = m.assignScale(2., 3., 4.);
        assertSame(r, m);//assert it return this
        assertMatrixEquals(new double[]{
                    2., 0., 0.,
                    0., 3., 0.,
                    0., 0., 4.}, m);
    }

    @Test
    public void testColumn01() {
        final Matrix3 m = new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Vector3 c1 = m.column(0);
        assertEquals(1., c1.x);
        assertEquals(4., c1.y);
        assertEquals(7., c1.z);
        final Vector3 c2 = m.column(1);
        assertNotSame(c1, c2); // Vector is not recycled
        assertEquals(2., c2.x);
        assertEquals(5., c2.y);
        assertEquals(8., c2.z);
        final Vector3 c3 = m.column(2);
        assertEquals(3., c3.x);
        assertEquals(6., c3.y);
        assertEquals(9., c3.z);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testColumn02() {
        new Matrix3().column(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testColumn03() {
        new Matrix3().column(3);
    }

    @Test
    public void testRow() {
        final Matrix3 m = new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Vector3 r1 = m.row(0);
        assertEquals(1., r1.x);
        assertEquals(2., r1.y);
        assertEquals(3., r1.z);
        final Vector3 r2 = m.row(1);
        assertNotSame(r1, r2); // Vector is not recycled
        assertEquals(4., r2.x);
        assertEquals(5., r2.y);
        assertEquals(6., r2.z);
        final Vector3 r3 = m.row(2);
        assertEquals(7., r3.x);
        assertEquals(8., r3.y);
        assertEquals(9., r3.z);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRow02() {
        new Matrix3().row(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRow03() {
        new Matrix3().row(3);
    }

    @Test
    public void testColumnVectors() {
        final Vector3 c1 = new Vector3();
        final Vector3 c2 = new Vector3();
        final Vector3 c3 = new Vector3();
        new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.).getColumnVectors(c1, c2, c3);

        assertEquals(1., c1.x);
        assertEquals(4., c1.y);
        assertEquals(7., c1.z);
        assertEquals(2., c2.x);
        assertEquals(5., c2.y);
        assertEquals(8., c2.z);
        assertEquals(3., c3.x);
        assertEquals(6., c3.y);
        assertEquals(9., c3.z);
    }

    @Test(expected = NullPointerException.class)
    public void testColumnVectors02() {
        new Matrix3().getColumnVectors(null, new Vector3(), new Vector3());
    }

    @Test(expected = NullPointerException.class)
    public void testColumnVectors03() {
        new Matrix3().getColumnVectors(new Vector3(), null, new Vector3());
    }

    @Test(expected = NullPointerException.class)
    public void testColumnVectors04() {
        new Matrix3().getColumnVectors(new Vector3(), new Vector3(), null);
    }

    @Test
    public void testRowVectors() {
        final Vector3 r1 = new Vector3();
        final Vector3 r2 = new Vector3();
        final Vector3 r3 = new Vector3();
        new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.).getRowVectors(r1, r2, r3);
        assertEquals(1., r1.x);
        assertEquals(2., r1.y);
        assertEquals(3., r1.z);
        assertEquals(4., r2.x);
        assertEquals(5., r2.y);
        assertEquals(6., r2.z);
        assertEquals(7., r3.x);
        assertEquals(8., r3.y);
        assertEquals(9., r3.z);
    }

    @Test(expected = NullPointerException.class)
    public void testRowVectors02() {
        new Matrix3().getRowVectors(null, new Vector3(), new Vector3());
    }

    @Test(expected = NullPointerException.class)
    public void testRowVectors03() {
        new Matrix3().getRowVectors(new Vector3(), null, new Vector3());
    }

    @Test(expected = NullPointerException.class)
    public void testRowVectors04() {
        new Matrix3().getRowVectors(new Vector3(), new Vector3(), null);
    }

    @Test
    public void testMultiplyDouble() {
        final Matrix3 m = new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Matrix3 r = m.multiply(2);
        assertNotSame(r, m);
        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);
        assertMatrixEquals(new double[]{
                    2., 4., 6.,
                    8., 10., 12.,
                    14., 16., 18.}, r);

    }

    @Test
    public void testScaleVector() {
        final Matrix3 m = new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Vector3 s = new Vector3(2., 3., 4.);
        final Matrix3 r = m.scale(s);
        assertNotSame(r, m);
        //Vector unmodified
        assertEquals(2., s.x);
        assertEquals(3., s.y);
        assertEquals(4., s.z);
        //MAtrix unchanged
        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);
        assertMatrixEquals(new double[]{
                    2., 6., 12,
                    8., 15., 24.,
                    14., 24., 36.}, r);

    }

    @Test(expected = NullPointerException.class)
    public void testScaleVector02() {
        new Matrix3().scale(null);
    }

    @Test
    public void testMultiply01() {
        final Matrix3 m = new Matrix3(1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Matrix3 n = new Matrix3(10., 11., 12.,
                13., 14., 15.,
                16., 17., 18.);
        final Matrix3 r = m.multiply(n);
        assertNotSame(m, r); // R is new
        assertNotSame(n, r); // R is new

        //Input is not changed
        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);
        //Input is not changed
        assertMatrixEquals(new double[]{
                    10., 11., 12.,
                    13., 14., 15.,
                    16., 17., 18.}, n);
        //Result is ok
        assertMatrixEquals(new double[]{
                    84., 90., 96.,
                    201., 216., 231.,
                    318., 342., 366.}, r);
    }

    @Test(expected = NullPointerException.class)
    public void testMultiply02() {
        new Matrix3().multiply((Matrix3) null);
    }

    @Test
    public void testAssignMultiply01() {
        final Matrix3 m = new Matrix3(1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Matrix3 n = new Matrix3(10., 11., 12.,
                13., 14., 15.,
                16., 17., 18.);
        final Matrix3 r = m.assignMultiply(n);
        assertSame(m, r); // r is m
        assertNotSame(n, r); // R is new        
        //Input is not changed
        assertMatrixEquals(new double[]{
                    10., 11., 12.,
                    13., 14., 15.,
                    16., 17., 18.}, n);
        //Result is ok
        assertMatrixEquals(new double[]{
                    84., 90., 96.,
                    201., 216., 231.,
                    318., 342., 366.}, r);
    }

    @Test(expected = NullPointerException.class)
    public void testAssignMultiply02() {
        new Matrix3().assignMultiply((Matrix3) null);
    }

    @Test
    public void testMultiplyVector01() {
        final Matrix3 m = new Matrix3(1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Vector3 v = new Vector3(10., 100., 1000.);
        final Vector3 r = m.multiply(v);
        assertNotSame(r, v);//new Vector3 is returned
        //Input is not changed
        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);
        //Vector unmodified
        assertEquals(10., v.x);
        assertEquals(100., v.y);
        assertEquals(1000., v.z);

        //Vector unmodified
        assertEquals(3210., r.x);
        assertEquals(6540., r.y);
        assertEquals(9870., r.z);

    }

    @Test(expected = NullPointerException.class)
    public void testAssignMultiplyVector02() {
        new Matrix3().multiply((Vector3) null);
    }

    @Test
    public void testAssignTranspose() {
        final Matrix3 m = new Matrix3(1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Matrix3 r = m.assignTranspose();
        assertSame(r, m);
        assertMatrixEquals(new double[]{
                    1., 4., 7.,
                    2., 5., 8.,
                    3., 6., 9.}, m);

    }

    @Test
    public void testTranspose() {
        final Matrix3 m = new Matrix3(1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Matrix3 r = m.transpose();
        assertNotSame(r, m);
        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);
        assertMatrixEquals(new double[]{
                    1., 4., 7.,
                    2., 5., 8.,
                    3., 6., 9.}, r);
    }

    @Test
    public void testAdd01() {
        final Matrix3 m = new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Matrix3 n = new Matrix3(
                10., 20., 30.,
                40., 50., 60.,
                70., 80., 90.);
        final Matrix3 r = m.add(n);
        assertNotSame(r, m);
        assertNotSame(r, n);
        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);
        assertMatrixEquals(new double[]{
                    10., 20., 30.,
                    40., 50., 60.,
                    70., 80., 90.}, n);
        assertMatrixEquals(new double[]{
                    11., 22., 33.,
                    44., 55., 66.,
                    77., 88., 99.}, r);
    }

    @Test(expected = NullPointerException.class)
    public void testAdd02() {
        new Matrix3().add(null);
    }

    @Test
    public void testAssignAdd01() {
        final Matrix3 m = new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final Matrix3 n = new Matrix3(
                10., 20., 30.,
                40., 50., 60.,
                70., 80., 90.);
        final Matrix3 r = m.assignAdd(n);
        assertSame(r, m);
        assertNotSame(r, n);
        assertMatrixEquals(new double[]{
                    10., 20., 30.,
                    40., 50., 60.,
                    70., 80., 90.}, n);
        assertMatrixEquals(new double[]{
                    11., 22., 33.,
                    44., 55., 66.,
                    77., 88., 99.}, r);
    }

    @Test(expected = NullPointerException.class)
    public void testAssignAdd02() {
        new Matrix3().assignAdd(null);
    }

    @Test
    public void testSub01() {
        final Matrix3 m = new Matrix3(
                -1., -2., -3.,
                -4., -5., -6.,
                -7., -8., -9.);
        final Matrix3 n = new Matrix3(
                10., 20., 30.,
                40., 50., 60.,
                70., 80., 90.);
        final Matrix3 r = m.subtract(n);
        assertNotSame(r, m);
        assertNotSame(r, n);
        assertMatrixEquals(new double[]{
                    -1., -2., -3.,
                    -4., -5., -6.,
                    -7., -8., -9.}, m);
        assertMatrixEquals(new double[]{
                    10., 20., 30.,
                    40., 50., 60.,
                    70., 80., 90.}, n);
        assertMatrixEquals(new double[]{
                    -11., -22., -33.,
                    -44., -55., -66.,
                    -77., -88., -99.}, r);
    }

    @Test(expected = NullPointerException.class)
    public void testSub02() {
        new Matrix3().subtract(null);
    }

    @Test
    public void testAssignSub01() {
        final Matrix3 m = new Matrix3(
                -1., -2., -3.,
                -4., -5., -6.,
                -7., -8., -9.);
        final Matrix3 n = new Matrix3(
                10., 20., 30.,
                40., 50., 60.,
                70., 80., 90.);
        final Matrix3 r = m.assignSubtract(n);
        assertSame(r, m);
        assertNotSame(r, n);
        assertMatrixEquals(new double[]{
                    10., 20., 30.,
                    40., 50., 60.,
                    70., 80., 90.}, n);
        assertMatrixEquals(new double[]{
                    -11., -22., -33.,
                    -44., -55., -66.,
                    -77., -88., -99.}, r);
    }

    @Test(expected = NullPointerException.class)
    public void testAssignSub02() {
        new Matrix3().assignSubtract(null);
    }

    @Test
    public void testIsNan() {
        final Matrix3 m = new Matrix3(
                -1., -2., -3.,
                -4., -5., -6.,
                -7., -8., -9.);
        assertFalse(m.isNaN());
        assertMatrixEquals(new double[]{
                    -1., -2., -3.,
                    -4., -5., -6.,
                    -7., -8., -9.}, m);

        assertTrue(new Matrix3(
                Double.NaN, -2., -3.,
                -4., -5., -6.,
                -7., -8., -9.).isNaN());
        assertTrue(new Matrix3(
                -1., Double.NaN, -3.,
                -4., -5., -6.,
                -7., -8., -9.).isNaN());
        assertTrue(new Matrix3(
                -1., -2., Double.NaN,
                -4., -5., -6.,
                -7., -8., -9.).isNaN());
        assertTrue(new Matrix3(
                -1., -2., -3.,
                Double.NaN, -5., -6.,
                -7., -8., -9.).isNaN());
        assertTrue(new Matrix3(
                -1., -2., -3.,
                -4., Double.NaN, -6.,
                -7., -8., -9.).isNaN());
        assertTrue(new Matrix3(
                -1., -2., -3.,
                -4., -5., Double.NaN,
                -7., -8., -9.).isNaN());
        assertTrue(new Matrix3(
                -1., -2., -3.,
                -4., -5., -6.,
                Double.NaN, -8., -9.).isNaN());
        assertTrue(new Matrix3(
                -1., -2., -3.,
                -4., -5., -6.,
                -8., Double.NaN, -9.).isNaN());
        assertTrue(new Matrix3(
                -1., -2., -3.,
                -4., -5., -6.,
                -8., -9., Double.NaN).isNaN());
    }

    @Test
    public void testDeterminan() {
        Matrix3 m = new Matrix3(
                1., 4., -7.,
                2., -5., 8.,
                3., 6., 9.);
        assertEquals(-258., m.determinant());
        assertMatrixEquals(new double[]{
                    1., 4., -7.,
                    2., -5., 8.,
                    3., 6., 9.}, m);
        assertEquals(1., Matrix3.identity().determinant());
        assertEquals(8., Matrix3.scaleMatrix(2.).determinant());
        assertEquals(0., new Matrix3().determinant());
    }

    @Test
    public void testFNorm() {
        Matrix3 m = new Matrix3(
                1., 4., -7.,
                2., -5., 8.,
                3., 6., 9.);
        assertEquals(16.881943016134134, m.fnorm());
        assertMatrixEquals(new double[]{
                    1., 4., -7.,
                    2., -5., 8.,
                    3., 6., 9.}, m);
        assertEquals(1.7320508075688772, Matrix3.identity().fnorm());
        assertEquals(3.4641016151377544, Matrix3.scaleMatrix(2.).fnorm());
        assertEquals(0., new Matrix3().fnorm());
    }

    @Test
    public void testInverse01() {
        Matrix3 m = new Matrix3(
                1., 4., -7.,
                2., -5., 8.,
                3., 6., 9.);
        Matrix3 r = m.inverse();
        assertNotSame(r, m);
        assertMatrixEquals(new double[]{
                    1., 4., -7.,
                    2., -5., 8.,
                    3., 6., 9.}, m);
        Matrix3 i = r.assignMultiply(m);
        assertMatrixEquals(new double[]{
                    1., 0., 0.,
                    0., 1., 0.,
                    0., 0., 1.}, i, 1E-15);
    }

    @Test
    public void testInverse02() {
        Matrix3 m = new Matrix3(
                1., 0., 0.,
                0., 1., 0.,
                0., 0., 1.);
        Matrix3 r = m.inverse();
        assertMatrixEquals(new double[]{
                    1., 0., 0.,
                    0., 1., 0.,
                    0., 0., 1.}, r, 1E-15);
    }

    @Test
    public void testAssignInverse02() {
        Matrix3 m = new Matrix3(
                1., 4., -7.,
                2., -5., 8.,
                3., 6., 9.);
        Matrix3 r = m.assignInverse();
        assertSame(r, m);
        Matrix3 i = r.assignMultiply(new Matrix3(
                1., 4., -7.,
                2., -5., 8.,
                3., 6., 9.));
        assertMatrixEquals(new double[]{
                    1., 0., 0.,
                    0., 1., 0.,
                    0., 0., 1.}, i, 1E-15);
    }

    @Test
    public void testToArray() {

        final Matrix3 m = new Matrix3(
                1., 2., 3.,
                4., 5., 6.,
                7., 8., 9.);
        final double[] d = m.toArray();
        final double[] d2 = m.toArray();
        assertNotSame(d, d2);
        assertMatrixEquals(new double[]{
                    1., 2., 3.,
                    4., 5., 6.,
                    7., 8., 9.}, m);
        assertEquals(1., d[0]);
        assertEquals(4., d[1]);
        assertEquals(7., d[2]);
        assertEquals(2., d[3]);
        assertEquals(5., d[4]);
        assertEquals(8., d[5]);
        assertEquals(3., d[6]);
        assertEquals(6., d[7]);
        assertEquals(9., d[8]);

    }

    @Test
    public void testToString() {
        assertEquals("[1.0, 2.0, 3.0]\n"
                + "[5.0, 6.0, 7.0]\n"
                + "[9.0, 10.0, 11.0]", new Matrix3(
                1., 2., 3.,
                5., 6., 7.,
                9., 10, 11).toString());
    }
}
