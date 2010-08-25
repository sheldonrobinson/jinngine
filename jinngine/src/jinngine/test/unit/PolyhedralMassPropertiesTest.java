/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jinngine.test.unit;

import java.util.ArrayList;
import java.util.List;
import jinngine.geometry.ConvexHull;
import jinngine.geometry.Geometry;
import jinngine.geometry.util.PolyhedralMassProperties;
import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import org.junit.Test;
import static junit.framework.Assert.*;
import quickhull3d.QuickHull3D;

/**
 *
 * @author Pierre LABATUT
 */
public class PolyhedralMassPropertiesTest {
 
    private static void assertMatrixEquals(Matrix3 ref, Matrix3 m, double tol) {
        assertEquals(ref.a11, m.a11, tol);
        assertEquals(ref.a12, m.a12, tol);
        assertEquals(ref.a13, m.a13, tol);
        assertEquals(ref.a21, m.a21, tol);
        assertEquals(ref.a22, m.a22, tol);
        assertEquals(ref.a23, m.a23, tol);
        assertEquals(ref.a31, m.a31, tol);
        assertEquals(ref.a32, m.a32, tol);
        assertEquals(ref.a33, m.a33, tol);
    }

    @Test
    public void testCube() {
        final Geometry g1 = new jinngine.geometry.Box(10, 10, 10);
        final InertiaMatrix im1 = g1.getInertialMatrix();
        final double m1 = g1.getMass();

        final List<Vector3> vectors = new ArrayList<Vector3>();
        vectors.add(new Vector3(-5, -5, -5));
        vectors.add(new Vector3(+5, -5, -5));
        vectors.add(new Vector3(-5, +5, -5));
        vectors.add(new Vector3(-5, -5, +5));
        vectors.add(new Vector3(+5, +5, -5));
        vectors.add(new Vector3(-5, +5, +5));
        vectors.add(new Vector3(+5, -5, +5));
        vectors.add(new Vector3(+5, +5, +5));
        final Geometry g2 = new ConvexHull(vectors);
        final InertiaMatrix im2 = g2.getInertialMatrix();
        final double m2 = g2.getMass();

        final QuickHull3D q = new QuickHull3D(Vector3.toArray(vectors));
        final PolyhedralMassProperties pmp = new PolyhedralMassProperties(q, 1.);
        final InertiaMatrix im3 = pmp.getInertiaMatrix();
        final double m3 = pmp.getMass();

        assertEquals(m1, m2, 1E-4);
        assertEquals(m1, m3, 1E-4);
        assertMatrixEquals(im1, im2, 1E-3);
        assertMatrixEquals(im1, im3, 1E-3);

    }

    @Test
    public void testSphere() {
        final Geometry g1 = new jinngine.geometry.Sphere(5);
        final InertiaMatrix im1 = g1.getInertialMatrix();
        final double m1 = g1.getMass();

        final List<Vector3> vectors = new ArrayList<Vector3>();
        double inc = Math.PI / 64.;
        for (double a = -Math.PI * .5 + inc; a < Math.PI * .5 - inc; a += inc) {
            for (double b = -Math.PI; b < Math.PI - inc; b += inc) {
                double ca = Math.cos(a) * 5.;
                double sa = Math.sin(a) * 5.;
                vectors.add(new Vector3(Math.cos(b) * ca, Math.sin(b) * ca, sa));
            }
        }
        final Geometry g2 = new ConvexHull(vectors);
        final InertiaMatrix im2 = g2.getInertialMatrix();
        final double m2 = g2.getMass();

        final QuickHull3D q = new QuickHull3D(Vector3.toArray(vectors));
        final PolyhedralMassProperties pmp = new PolyhedralMassProperties(q, 1.);
        final InertiaMatrix im3 = pmp.getInertiaMatrix();
        final double m3 = pmp.getMass();

        assertEquals(m1, m2, 300.);
        assertEquals(m1, m3, 1.);
        assertMatrixEquals(im1, im2, 6000.);
        assertMatrixEquals(im1, im3, 10.);

    }
}
