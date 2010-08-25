/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jinngine.test.unit;

import java.util.ArrayList;
import java.util.List;
import jinngine.math.Vector3;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 *
 * @author PYW
 */
public class Vector3Test {

    @Test
    public void testCtor01() {
        final Vector3 v = new Vector3();
        assertEquals(0., v.x);
        assertEquals(0., v.y);
        assertEquals(0., v.z);
    }

    @Test(expected = NullPointerException.class)
    public void testCtor02() {
        new Vector3((Vector3) null);
    }

    @Test
    public void testCtor03() {
        final Vector3 v =
                new Vector3(new Vector3(Double.MIN_VALUE, Double.MAX_VALUE, 0.));
        assertEquals(Double.MIN_VALUE, v.x);
        assertEquals(Double.MAX_VALUE, v.y);
        assertEquals(0., v.z);

        final Vector3 w =
                new Vector3(new Vector3(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
        assertEquals(Double.NaN, w.x);
        assertEquals(Double.POSITIVE_INFINITY, w.y);
        assertEquals(Double.NEGATIVE_INFINITY, w.z);
    }

    @Test
    public void testAssign() {
        final Vector3 v = new Vector3();
        final Vector3 r = v.assign(1., 2., 3.);
        assertTrue(r == v);
        assertEquals(1., v.x);
        assertEquals(2., v.y);
        assertEquals(3., v.z);
    }

    @Test
    public void testToArray01() {
        final Vector3 v = new Vector3(1., 2., 3.);
        final double[] d = v.toArray();
        assertEquals(1., v.x);
        assertEquals(2., v.y);
        assertEquals(3., v.z);
        assertEquals(3, d.length);
        assertEquals(1., d[0]);
        assertEquals(2., d[1]);
        assertEquals(3., d[2]);
    }

    @Test
    public void testToArray02() {
        final List<Vector3> vertices = new ArrayList<Vector3>();
        vertices.add(new Vector3(1, 2, 3));
        vertices.add(new Vector3(4, 5, 6));
        vertices.add(new Vector3(7, 8, 9));

        final Vector3 v = new Vector3(1., 2., 3.);
        final double[] d = v.toArray();
        assertEquals(1., v.x);
        assertEquals(2., v.y);
        assertEquals(3., v.z);
        assertEquals(3, d.length);
        assertEquals(1., d[0]);
        assertEquals(2., d[1]);
        assertEquals(3., d[2]);
    }

    @Test
    public void testToString() {
        assertEquals(new Vector3(1., 2., 3.).toString(), "[1.0, 2.0, 3.0]");
        assertEquals(new Vector3(Double.MIN_VALUE, Double.POSITIVE_INFINITY, Double.NaN).toString(),
                "[4.9E-324, Infinity, NaN]");
    }

    @Test
    public void testCopy() {
        final Vector3 v1 = new Vector3(1., 2., 3.);
        final Vector3 v2 = new Vector3(v1);
        assertTrue(v1 != v2); //It is a new ref
        assertEquals(1., v1.x);
        assertEquals(2., v1.y);
        assertEquals(3., v1.z);
        assertEquals(1., v2.x);
        assertEquals(2., v2.y);
        assertEquals(3., v2.z);

    }

    @Test
    public void testAdd01() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = new Vector3(10., 20., 30.);
        final Vector3 r = a.add(b);
        assertTrue(r != a); //It is a new ref
        assertTrue(r != b); //It is a new ref
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(10., b.x);
        assertEquals(20., b.y);
        assertEquals(30., b.z);
        assertEquals(11., r.x);
        assertEquals(22., r.y);
        assertEquals(33., r.z);
    }

    @Test
    public void testAdd02() {
        final Vector3 a = new Vector3(1., 4., 16.);
        final Vector3 r = a.add(a); // Just to be sure that add a ref to itself works
        assertTrue(r != a);
        assertEquals(1., a.x);
        assertEquals(4., a.y);
        assertEquals(16., a.z);
        assertEquals(2., r.x);
        assertEquals(8., r.y);
        assertEquals(32., r.z);
    }

    @Test(expected = NullPointerException.class)
    public void testAdd03() {
        new Vector3(1., 4., 16.).add(null);
    }

    @Test
    public void testAdd04() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = new Vector3(10., 20., 30.);
        Vector3.add(a, b);
        assertEquals(11., a.x);
        assertEquals(22., a.y);
        assertEquals(33., a.z);
        assertEquals(10., b.x);
        assertEquals(20., b.y);
        assertEquals(30., b.z);
    }

    @Test
    public void testAdd05() {
        final Vector3 a = new Vector3(1., 4., 16.);
        Vector3.add(a, a);
        assertEquals(2., a.x);
        assertEquals(8., a.y);
        assertEquals(32., a.z);
    }

    @Test(expected = NullPointerException.class)
    public void testAdd06() {
        Vector3.add(new Vector3(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testAdd07() {
        Vector3.add(null, new Vector3());
    }

    @Test
    public void testAdd08() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 r = a.add(10., 20., 30.);
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(11., r.x);
        assertEquals(22., r.y);
        assertEquals(33., r.z);
    }

    @Test
    public void testSub01() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = new Vector3(10., 20., 30.);
        final Vector3 r = a.sub(b);
        assertTrue(r != a); //It is a new ref
        assertTrue(r != b); //It is a new ref
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(10., b.x);
        assertEquals(20., b.y);
        assertEquals(30., b.z);
        assertEquals(-9., r.x);
        assertEquals(-18., r.y);
        assertEquals(-27., r.z);
    }

    @Test
    public void testSub02() {
        final Vector3 a = new Vector3(1., 4., 16.);
        final Vector3 r = a.sub(a); // Just to be sure that substract a ref to itself works
        assertTrue(r != a);
        assertEquals(1., a.x);
        assertEquals(4., a.y);
        assertEquals(16., a.z);
        assertEquals(0., r.x);
        assertEquals(0., r.y);
        assertEquals(0., r.z);
    }

    @Test(expected = NullPointerException.class)
    public void testSub03() {
        new Vector3(1., 4., 16.).sub(null);
    }

    @Test
    public void testSub04() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = new Vector3(10., 20., 30.);
        Vector3.sub(a, b);
        assertEquals(-9., a.x);
        assertEquals(-18., a.y);
        assertEquals(-27., a.z);
        assertEquals(10., b.x);
        assertEquals(20., b.y);
        assertEquals(30., b.z);
    }

    @Test
    public void testSub05() {
        final Vector3 a = new Vector3(1., 4., 16.);
        Vector3.sub(a, a);
        assertEquals(0., a.x);
        assertEquals(0., a.y);
        assertEquals(0., a.z);
    }

    @Test(expected = NullPointerException.class)
    public void testSub06() {
        Vector3.sub(new Vector3(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testSub07() {
        Vector3.sub(null, new Vector3());
    }

    @Test
    public void testMultiply01() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 r = a.multiply(2.);
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(2., r.x);
        assertEquals(4., r.y);
        assertEquals(6., r.z);
    }

    @Test
    public void testMultiply02() {
        final Vector3 r = new Vector3(1., 2., 3.);
        Vector3.multiply(r, 2.);
        assertEquals(2., r.x);
        assertEquals(4., r.y);
        assertEquals(6., r.z);
    }

    @Test
    public void testMultiplyAndAdd() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = new Vector3(10., 20., 30.);
        Vector3.multiplyAndAdd(a, 2., b);
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(12., b.x);
        assertEquals(24., b.y);
        assertEquals(36., b.z);
    }

    @Test
    public void testMultiplyStoreAndAdd() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = new Vector3(10., 20., 30.);
        Vector3.multiplyStoreAndAdd(a, 2., b);
        assertEquals(2., a.x);
        assertEquals(4., a.y);
        assertEquals(6., a.z);
        assertEquals(12., b.x);
        assertEquals(24., b.y);
        assertEquals(36., b.z);
    }

    @Test
    public void testNegate() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 r = a.negate();
        assertTrue(r != a); //It is a new ref
        assertEquals(-1., r.x);
        assertEquals(-2., r.y);
        assertEquals(-3., r.z);
    }

    @Test
    public void testIsNan() {
        assertFalse(new Vector3(1., 2., 3.).isNaN());
        assertTrue(new Vector3(Double.NaN, 2., 3.).isNaN());
        assertTrue(new Vector3(1., Double.NaN, 3.).isNaN());
        assertTrue(new Vector3(1., 2., Double.NaN).isNaN());
    }

    @Test
    public void testGet() {
        final Vector3 a = new Vector3(1., 2., 3.);
        assertEquals(1., a.get(0));
        assertEquals(2., a.get(1));
        assertEquals(3., a.get(2));
    }

    @Test
    public void testSet() {
        final Vector3 a = new Vector3(1., 2., 3.);
        a.set(0, 0.);
        assertEquals(0., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        a.set(1, 0.);
        assertEquals(0., a.x);
        assertEquals(0., a.y);
        assertEquals(3., a.z);
        a.set(2, 0.);
        assertEquals(0., a.x);
        assertEquals(0., a.y);
        assertEquals(0., a.z);
    }

    @Test
    public void testAssign01() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = new Vector3(10., 20., 30.);
        a.assign(b);
        assertEquals(10., a.x);
        assertEquals(20., a.y);
        assertEquals(30., a.z);
        assertEquals(10., b.x);
        assertEquals(20., b.y);
        assertEquals(30., b.z);
    }

    @Test(expected = NullPointerException.class)
    public void testAssign02() {
        new Vector3().assign(null);
    }

    @Test
    public void testAssignZero() {
        final Vector3 a = new Vector3(1., 2., 3.);
        a.assignZero();
        assertEquals(0., a.x);
        assertEquals(0., a.y);
        assertEquals(0., a.z);
    }

    @Test
    public void testNorm() {
        final Vector3 a = new Vector3(1., 2., 3.);
        a.norm();
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(Math.sqrt(1029.), new Vector3(32., 1., 2.).norm());
        assertEquals(Math.sqrt(1029.), new Vector3(2., 32., 1.).norm());
        assertEquals(Math.sqrt(1029.), new Vector3(1., 2., 32.).norm());
        assertTrue(Double.isNaN(new Vector3(Double.NaN, 0., 0.).norm()));
        assertTrue(Double.isNaN(new Vector3(0., Double.NaN, 0.).norm()));
        assertTrue(Double.isNaN(new Vector3(0., 0., Double.NaN).norm()));
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(Double.POSITIVE_INFINITY, 0., 0.).norm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(0., Double.POSITIVE_INFINITY, 0.).norm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(0., 0., Double.POSITIVE_INFINITY).norm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(Double.NEGATIVE_INFINITY, 0., 0.).norm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(0., Double.NEGATIVE_INFINITY, 0.).norm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(0., 0., Double.NEGATIVE_INFINITY).norm());

    }

    @Test
    public void testNormXY() {
        final Vector3 a = new Vector3(1., 2., 3.);
        a.xynorm();
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(Math.sqrt(5), new Vector3(1., 2., Double.NaN).xynorm());
        assertEquals(Math.sqrt(5), new Vector3(2., 1., Double.NaN).xynorm());
        // NaN gives NaN
        assertTrue(Double.isNaN(new Vector3(Double.NaN, 0., 0.).xynorm()));
        assertTrue(Double.isNaN(new Vector3(0., Double.NaN, 0.).xynorm()));
        // Infiny gives +Infiny
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(Double.POSITIVE_INFINITY, 0., 0.).xynorm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(0., Double.POSITIVE_INFINITY, 0.).xynorm());
        assertEquals(0., new Vector3(0., 0., Double.POSITIVE_INFINITY).xynorm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(Double.NEGATIVE_INFINITY, 0., 0.).xynorm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(0., Double.NEGATIVE_INFINITY, 0.).xynorm());
        assertEquals(0., new Vector3(0., 0., Double.NEGATIVE_INFINITY).xynorm());
    }

    @Test
    public void testSquareNorm() {
        final Vector3 a = new Vector3(1., 2., 3.);
        a.squaredNorm();
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(1029., new Vector3(32., 1., 2.).squaredNorm());
        assertEquals(1029., new Vector3(2., 32., 1.).squaredNorm());
        assertEquals(1029., new Vector3(1., 2., 32.).squaredNorm());
        assertTrue(Double.isNaN(new Vector3(Double.NaN, 0., 0.).squaredNorm()));
        assertTrue(Double.isNaN(new Vector3(0., Double.NaN, 0.).squaredNorm()));
        assertTrue(Double.isNaN(new Vector3(0., 0., Double.NaN).squaredNorm()));
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(Double.POSITIVE_INFINITY, 0., 0.).squaredNorm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(0., Double.POSITIVE_INFINITY, 0.).squaredNorm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(0., 0., Double.POSITIVE_INFINITY).squaredNorm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(Double.NEGATIVE_INFINITY, 0., 0.).squaredNorm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(0., Double.NEGATIVE_INFINITY, 0.).squaredNorm());
        assertEquals(Double.POSITIVE_INFINITY, new Vector3(0., 0., Double.NEGATIVE_INFINITY).squaredNorm());
    }

    @Test
    public void testDot01() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = new Vector3(10., 20., 30.);
        assertEquals(10. + 40. + 90., a.dot(b));
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(10., b.x);
        assertEquals(20., b.y);
        assertEquals(30., b.z);
        assertEquals(10. + 40. + 90., b.dot(a));
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(10., b.x);
        assertEquals(20., b.y);
        assertEquals(30., b.z);
    }

    @Test(expected = NullPointerException.class)
    public void testDot02() {
        new Vector3().dot(null);
    }

    @Test
    public void testXYDot01() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = new Vector3(10., 20., 30.);
        assertEquals(10. + 40., a.xydot(b));
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(10., b.x);
        assertEquals(20., b.y);
        assertEquals(30., b.z);
        assertEquals(10. + 40., b.xydot(a));
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(10., b.x);
        assertEquals(20., b.y);
        assertEquals(30., b.z);
    }

    @Test(expected = NullPointerException.class)
    public void testXYDot02() {
        new Vector3().xydot(null);
    }

    @Test
    public void testNormalize01() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = a.normalize();
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(1. / Math.sqrt(14), b.x);
        assertEquals(2. / Math.sqrt(14), b.y);
        assertEquals(3. / Math.sqrt(14), b.z);
    }

    @Test
    public void testNormalize02() {
        final Vector3 a = new Vector3();
        final Vector3 b = a.normalize();
        assertEquals(0., a.x);
        assertEquals(0., a.y);
        assertEquals(0., a.z);
        assertEquals(1., b.x);
        assertEquals(0., b.y);
        assertEquals(0., b.z);
    }

    @Test
    public void testCross01() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = new Vector3(4., 5., 6.);
        final Vector3 r = a.cross(b);
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(4., b.x);
        assertEquals(5., b.y);
        assertEquals(6., b.z);
        assertEquals(-3., r.x);
        assertEquals(6., r.y);
        assertEquals(-3., r.z);
    }

    @Test(expected = NullPointerException.class)
    public void testCross02() {
        new Vector3().cross(null);
    }

    @Test
    public void testCross03() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = new Vector3(4., 5., 6.);
        final Vector3 r = new Vector3();
        Vector3.crossProduct(a, b, r);
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(4., b.x);
        assertEquals(5., b.y);
        assertEquals(6., b.z);
        assertEquals(-3., r.x);
        assertEquals(6., r.y);
        assertEquals(-3., r.z);
    }

    @Test(expected = NullPointerException.class)
    public void testCross04() {
        Vector3.crossProduct(null, new Vector3(), new Vector3());
    }

    @Test(expected = NullPointerException.class)
    public void testCross05() {
        Vector3.crossProduct(new Vector3(), null, new Vector3());
    }

    @Test(expected = NullPointerException.class)
    public void testCross06() {
        Vector3.crossProduct(new Vector3(), new Vector3(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsEpsilon01() {
        new Vector3().isEpsilon(-1.);
    }

    @Test
    public void testIsEpsilon02() {
        {//The vector is not changed
            final Vector3 a = new Vector3(-3., 0., 3.);
            a.isEpsilon(4.);
            a.isEpsilon(1.);
            assertEquals(-3., a.x);
            assertEquals(0., a.y);
            assertEquals(3., a.z);
        }
        assertTrue(new Vector3().isEpsilon(-0.));
        assertTrue(new Vector3(-3., -3., -3.).isEpsilon(3.));
        assertTrue(new Vector3(3., 3., 3.).isEpsilon(3.));
        assertFalse(new Vector3(-4., 0., 0.).isEpsilon(3.));
        assertFalse(new Vector3(4., 0., 0.).isEpsilon(3.));
        assertFalse(new Vector3(0., -4., 0.).isEpsilon(3.));
        assertFalse(new Vector3(0., 4., 0.).isEpsilon(3.));
        assertFalse(new Vector3(0., 0., -4.).isEpsilon(3.));
        assertFalse(new Vector3(0., 0., 4.).isEpsilon(3.));
    }

    @Test
    public void testI() {
        Vector3 a = Vector3.i();
        assertEquals(1., a.x);
        assertEquals(0., a.y);
        assertEquals(0., a.z);
        assertFalse(a == Vector3.i());
    }

    @Test
    public void testJ() {
        Vector3 a = Vector3.j();
        assertEquals(0., a.x);
        assertEquals(1., a.y);
        assertEquals(0., a.z);
        assertFalse(a == Vector3.j());
    }

    @Test
    public void testK() {
        Vector3 a = Vector3.k();
        assertEquals(0., a.x);
        assertEquals(0., a.y);
        assertEquals(1., a.z);
        assertFalse(a == Vector3.k());
    }

    @Test
    public void testScale01() {
        final Vector3 a = new Vector3(1., 2., 3.);
        final Vector3 b = new Vector3(10., 20., 30.);
        final Vector3 r = a.scale(b);
        assertTrue(r != a);
        assertTrue(r != b);
        assertEquals(1., a.x);
        assertEquals(2., a.y);
        assertEquals(3., a.z);
        assertEquals(10., b.x);
        assertEquals(20., b.y);
        assertEquals(30., b.z);
        assertEquals(10., r.x);
        assertEquals(40., r.y);
        assertEquals(90., r.z);
    }

    @Test(expected = NullPointerException.class)
    public void testScale02() {
        new Vector3().scale(null);
    }
}
