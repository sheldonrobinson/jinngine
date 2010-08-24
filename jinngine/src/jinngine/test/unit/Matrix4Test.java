/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jinngine.test.unit;

import jinngine.math.Matrix4;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 *
 * @author PYW
 */
public class Matrix4Test {

    private static void assertMatrixEquals(double[] ref, Matrix4 val, double tolerance) {
        junit.framework.Assert.assertEquals(ref[0], val.a11, tolerance);
        junit.framework.Assert.assertEquals(ref[1], val.a12, tolerance);
        junit.framework.Assert.assertEquals(ref[2], val.a13, tolerance);
        junit.framework.Assert.assertEquals(ref[3], val.a14, tolerance);
        junit.framework.Assert.assertEquals(ref[4], val.a21, tolerance);
        junit.framework.Assert.assertEquals(ref[5], val.a22, tolerance);
        junit.framework.Assert.assertEquals(ref[6], val.a23, tolerance);
        junit.framework.Assert.assertEquals(ref[7], val.a24, tolerance);
        junit.framework.Assert.assertEquals(ref[8], val.a31, tolerance);
        junit.framework.Assert.assertEquals(ref[9], val.a32, tolerance);
        junit.framework.Assert.assertEquals(ref[10], val.a33, tolerance);
        junit.framework.Assert.assertEquals(ref[11], val.a34, tolerance);
        junit.framework.Assert.assertEquals(ref[12], val.a41, tolerance);
        junit.framework.Assert.assertEquals(ref[13], val.a42, tolerance);
        junit.framework.Assert.assertEquals(ref[14], val.a43, tolerance);
        junit.framework.Assert.assertEquals(ref[15], val.a44, tolerance);
    }

    private static void assertMatrixEquals(double[] ref, Matrix4 val) {
        assertMatrixEquals(ref, val, 0.);
    }

    @Test
    public void testCtorZero() {
        final Matrix4 m = new Matrix4();
        assertMatrixEquals(new double[]{
                    0., 0., 0., 0.,
                    0., 0., 0., 0.,
                    0., 0., 0., 0.,
                    0., 0., 0., 0.,}, m);
    }

    @Test
    public void testAssignZero() {
        final Matrix4 m = new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16);
        final Matrix4 r = m.assignZero();
        assertSame(r, m);//assert it return this
        //assert every value is 0.
        assertMatrixEquals(new double[]{
                    0., 0., 0., 0.,
                    0., 0., 0., 0.,
                    0., 0., 0., 0.,
                    0., 0., 0., 0.,}, m);
    }

    @Test
    public void testCtorD() {
        final Matrix4 m = new Matrix4(new double[]{
                    1., 2., 3., 4.,
                    5., 6., 7., 8.,
                    9., 10, 11, 12,
                    13, 14, 15, 16});
        assertMatrixEquals(new double[]{
                    1., 5., 9., 13,
                    2., 6., 10, 14,
                    3., 7., 11, 15,
                    4., 8., 12, 16}, m);
    }

    @Test
    public void testAssignD() {
        final Matrix4 m = new Matrix4();
        final Matrix4 r = m.assign(new double[]{
                    1., 2., 3., 4.,
                    5., 6., 7., 8.,
                    9., 10, 11, 12,
                    13, 14, 15, 16});
        assertSame(r, m);//assert it return this
        //assert every value is 0.
        assertMatrixEquals(new double[]{
                    1., 5., 9., 13,
                    2., 6., 10, 14,
                    3., 7., 11, 15,
                    4., 8., 12, 16}, m);
    }

    @Test
    public void testCtor() {
        final Matrix4 m = new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16);
        assertMatrixEquals(new double[]{
                    1., 2., 3., 4.,
                    5., 6., 7., 8.,
                    9., 10, 11, 12,
                    13, 14, 15, 16}, m);
    }

    @Test
    public void testAssign() {
        final Matrix4 m = new Matrix4();
        final Matrix4 r = m.assign(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16);
        assertSame(r, m);//assert it return this
        //assert every value is 0.
        assertMatrixEquals(new double[]{
                    1., 2., 3., 4.,
                    5., 6., 7., 8.,
                    9., 10, 11, 12,
                    13, 14, 15, 16}, m);
    }

    @Test
    public void testCtorMatrix() {
        final Matrix4 m = new Matrix4(new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16));

        assertMatrixEquals(new double[]{
                    1., 2., 3., 4.,
                    5., 6., 7., 8.,
                    9., 10, 11, 12,
                    13, 14, 15, 16}, m);
    }

    @Test
    public void testAssignMatrix() {
        final Matrix4 m = new Matrix4();
        final Matrix4 r = m.assign(new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16));
        assertSame(r, m);//assert it return this
        //assert every value is 0.
        assertMatrixEquals(new double[]{
                    1., 2., 3., 4.,
                    5., 6., 7., 8.,
                    9., 10, 11, 12,
                    13, 14, 15, 16}, m);

    }

    @Test
    public void testFtorIdentity() {
        final Matrix4 m = Matrix4.identity();
        final Matrix4 n = Matrix4.identity();
        assertNotSame(n, m);//create a new referene everytime like a ctor
        assertMatrixEquals(new double[]{
                    1., 0., 0., 0.,
                    0., 1., 0., 0.,
                    0., 0., 1., 0.,
                    0., 0., 0., 1.}, m);
    }

    @Test
    public void testAssignIdentity() {
        final Matrix4 m = new Matrix4(
                2., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16);
        final Matrix4 r = m.assignIdentity();
        assertSame(r, m);//assert it return this
        assertMatrixEquals(new double[]{
                    1., 0., 0., 0.,
                    0., 1., 0., 0.,
                    0., 0., 1., 0.,
                    0., 0., 0., 1.}, m);
    }

    @Test
    public void testFtorScaleDouble() {
        final Matrix4 m = Matrix4.scaleMatrix(3.);
        final Matrix4 n = Matrix4.scaleMatrix(3.);
        assertNotSame(n, m);//create a new referene everytime like a ctor
        assertMatrixEquals(new double[]{
                    3., 0., 0., 0.,
                    0., 3., 0., 0.,
                    0., 0., 3., 0.,
                    0., 0., 0., 1.}, m);
    }

    @Test
    public void testAssignScaleDouble() {
        final Matrix4 m = new Matrix4(
                1., 0., 0., 0.,
                0., 1., 0., 0.,
                0., 0., 1., 0.,
                0., 0., 0., 1.);
        final Matrix4 r = m.assignScale(3.);
        assertSame(r, m);//assert it return this
        assertMatrixEquals(new double[]{
                    3., 0., 0., 0.,
                    0., 3., 0., 0.,
                    0., 0., 3., 0.,
                    0., 0., 0., 1.}, m);
    }

    @Test
    public void testFtorScale3Double() {
        final Matrix4 m = Matrix4.scaleMatrix(2., 3., 4.);
        final Matrix4 n = Matrix4.scaleMatrix(2., 3., 4.);
        assertNotSame(n, m);//create a new referene everytime like a ctor
        assertMatrixEquals(new double[]{
                    2., 0., 0., 0.,
                    0., 3., 0., 0.,
                    0., 0., 4., 0.,
                    0., 0., 0., 1.}, m);
    }

    @Test
    public void testAssign3Double() {
        final Matrix4 m = new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16);

        final Matrix4 r = m.assignScale(2., 3., 4.);
        assertSame(r, m);//assert it return this
        assertMatrixEquals(new double[]{
                    2., 0., 0., 0.,
                    0., 3., 0., 0.,
                    0., 0., 4., 0.,
                    0., 0., 0., 1.}, m);
    }

    @Test
    public void testMultiply01() {
        final Matrix4 m = new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16);
        final Matrix4 n = new Matrix4(
                10., 20., 30., 40.,
                50., 60., 70., 80.,
                90., 100, 110, 120,
                130, 140, 150, 160);
        final Matrix4 r = m.multiply(n);
        assertNotSame(m, r); // R is new
        assertNotSame(n, r); // R is new

        //Input is not changed
        assertMatrixEquals(new double[]{
                    1., 2., 3., 4.,
                    5., 6., 7., 8.,
                    9., 10, 11, 12,
                    13, 14, 15, 16}, m);
        //Input is not changed
        assertMatrixEquals(new double[]{
                    10., 20., 30., 40.,
                    50., 60., 70., 80.,
                    90., 100, 110, 120,
                    130, 140, 150, 160}, n);
        //Result is ok        
        assertMatrixEquals(new double[]{
                    900.0, 1000.0, 1100.0, 1200.0,
                    2020.0, 2280.0, 2540.0, 2800.0,
                    3140.0, 3560.0, 3980.0, 4400.0,
                    4260.0, 4840.0, 5420.0, 6000.0}, r);
    }

    @Test(expected = NullPointerException.class)
    public void testMultiply02() {
        new Matrix4().multiply((Matrix4) null);
    }

    @Test
    public void testAssignMultiply01() {
        final Matrix4 m = new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16);
        final Matrix4 n = new Matrix4(
                10., 20., 30., 40.,
                50., 60., 70., 80.,
                90., 100, 110, 120,
                130, 140, 150, 160);
        final Matrix4 r = m.assignMultiply(n);
        assertSame(m, r); // r is m
        assertNotSame(n, r); // R is new        
        //Input is not changed
        assertMatrixEquals(new double[]{
                    10., 20., 30., 40.,
                    50., 60., 70., 80.,
                    90., 100, 110, 120,
                    130, 140, 150, 160}, n);
        //Result is ok        
        assertMatrixEquals(new double[]{
                    900.0, 1000.0, 1100.0, 1200.0,
                    2020.0, 2280.0, 2540.0, 2800.0,
                    3140.0, 3560.0, 3980.0, 4400.0,
                    4260.0, 4840.0, 5420.0, 6000.0,}, r);
    }

    @Test(expected = NullPointerException.class)
    public void testAssignMultiply02() {
        new Matrix4().assignMultiply((Matrix4) null);
    }

    @Test
    public void testMultiplyVector01() {
        final Matrix4 m = new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16);
        final Vector3 v = new Vector3(100., 10000., 1000000.);
        final Vector3 r = m.multiply(v);
        assertNotSame(r, v);//new Vector3 is returned
        //Input is not changed
        assertMatrixEquals(new double[]{
                    1., 2., 3., 4.,
                    5., 6., 7., 8.,
                    9., 10, 11, 12,
                    13, 14, 15, 16}, m);
        //Vector unmodified
        assertEquals(100., v.x);
        assertEquals(10000., v.y);
        assertEquals(1000000., v.z);

        //Vector unmodified        
        assertEquals(0.1994611300629351, r.x);
        assertEquals(0.46630742004195674, r.y);
        assertEquals(0.7331537100209784, r.z);

    }

    @Test(expected = NullPointerException.class)
    public void testAssignMultiplyVector02() {
        new Matrix4().multiply((Vector3) null);
    }

    @Test
    public void testIsNan() {
        final Matrix4 m = new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16);
        assertFalse(m.isNaN());
        assertMatrixEquals(new double[]{
                    1., 2., 3., 4.,
                    5., 6., 7., 8.,
                    9., 10, 11, 12,
                    13, 14, 15, 16}, m);

        assertTrue(new Matrix4(
                1., Double.NaN, 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., Double.NaN, 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., 3., Double.NaN,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., 3., 4.,
                Double.NaN, 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., 3., 4.,
                5., Double.NaN, 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., 3., 4.,
                5., 6., Double.NaN, 8.,
                9., 10, 11, 12,
                13, 14, 15, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., Double.NaN,
                9., 10, 11, 12,
                13, 14, 15, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                Double.NaN, 10, 11, 12,
                13, 14, 15, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., Double.NaN, 11, 12,
                13, 14, 15, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, Double.NaN, 12,
                13, 14, 15, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, Double.NaN,
                13, 14, 15, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                Double.NaN, 14, 15, 16).isNaN());

        assertTrue(new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, Double.NaN, 15, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, Double.NaN, 16).isNaN());
        assertTrue(new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, Double.NaN).isNaN());

    }

    @Test
    public void testInverse01() {
        Matrix4 m = Matrix4.scaleMatrix(2, 3, 4);
        Matrix4 r = m.inverse();
        assertNotSame(r, m);
        assertMatrixEquals(new double[]{
                    2.0, 0.0, 0.0, 0.0,
                    0.0, 3.0, 0.0, 0.0,
                    0.0, 0.0, 4.0, 0.0,
                    0.0, 0.0, 0.0, 1.0}, m);
        Matrix4 i = r.assignMultiply(m);
        assertMatrixEquals(new double[]{
                    1., 0., 0., 0.,
                    0., 1., 0., 0.,
                    0., 0., 1., 0.,
                    0., 0., 0., 1.}, i, 1E-15);
    }

    @Test
    public void testInverse02() {
        Matrix4 m = new Matrix4(
                1., 0., 0., 0.,
                0., 1., 0., 0.,
                0., 0., 1., 0.,
                0., 0., 0., 1.);
        Matrix4 r = m.inverse();
        assertMatrixEquals(new double[]{
                    1., 0., 0., 0.,
                    0., 1., 0., 0.,
                    0., 0., 1., 0.,
                    0., 0., 0., 1.}, r, 1E-15);
    }

    @Test
    public void testToArray() {

        final Matrix4 m = new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16);
        final double[] d = m.toArray();
        final double[] d2 = m.toArray();
        assertNotSame(
                d, d2);
        assertMatrixEquals(
                new double[]{
                    1., 2., 3., 4.,
                    5., 6., 7., 8.,
                    9., 10, 11, 12,
                    13, 14, 15, 16}, m);
        assertEquals(1., d[0]);
        assertEquals(5., d[1]);
        assertEquals(9., d[2]);
        assertEquals(13., d[3]);
        assertEquals(2., d[4]);
        assertEquals(6., d[5]);
        assertEquals(10., d[6]);
        assertEquals(14., d[7]);
        assertEquals(3., d[8]);
        assertEquals(7., d[9]);
        assertEquals(11., d[10]);
        assertEquals(15., d[11]);
        assertEquals(4., d[12]);
        assertEquals(8., d[13]);
        assertEquals(12., d[14]);
        assertEquals(16., d[15]);
    }

    @Test
    public void testToString() {
        assertEquals("[1.0, 2.0, 3.0, 4.0]\n"
                + "[5.0, 6.0, 7.0, 8.0]\n"
                + "[9.0, 10.0, 11.0, 12.0]\n"
                + "[13.0, 14.0, 15.0, 16.0]", new Matrix4(
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10, 11, 12,
                13, 14, 15, 16).toString());
    }
}
