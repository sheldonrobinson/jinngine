package jinngine.test.unit;

import jinngine.collision.GJK;
import jinngine.geometry.Sphere;
import jinngine.geometry.SupportMap3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import junit.framework.TestCase;

public class GJK3Test extends TestCase {
	/*
	 * Two spheres of radius 1 having a distance of 1.
	 * Sphere 1 is at (0,3,0), Sphere 2 is at (0,0,0)
	 */
	public void testSphereSphere1() {
		GJK gjk = new GJK();
		
		//set up geometries
		Sphere s1 = new Sphere(1);
		Body b1 = new Body(s1);
		b1.setPosition(new Vector3(0,3,0));
		Sphere s2 = new Sphere(1);
		Body b2 = new Body(s2);
		b2.setPosition(new Vector3(0,0,0));
		
		//closest point vectors
		Vector3 p1 = new Vector3();
		Vector3 p2 = new Vector3();
		
		//error tolerance
		double epsilon = 1e-14;
		
		gjk.run(s1,s2,p1,p2,epsilon);
		
		double d = p1.minus(p2).norm();
		
		assertTrue( Math.abs(d-1) < epsilon );
	}

	/*
	 * Two spheres of radius 1 having a very small distance
	 * Sphere 1 is at (0,1+a,0), Sphere 2 is at (0,-1-a,0), 
	 * where a is a small number. We expect the distance to 
	 * be 2a, within the error tolerance
	 */
	public void testSphereSphere2() {
		GJK gjk = new GJK();

		//small number
		double a = 1e-10;
		
		//error tolerance
		double epsilon = 1e-14;
		
		//set up geometries
		Sphere s1 = new Sphere(1);
		Body b1 = new Body(s1);
		b1.setPosition(new Vector3(0,1+a,0));
		Sphere s2 = new Sphere(1);
		Body b2 = new Body(s2);
		b2.setPosition(new Vector3(0,-1-a,0));
		
		//closest point vectors
		Vector3 p1 = new Vector3();
		Vector3 p2 = new Vector3();
				
		gjk.run(s1,s2,p1,p2,epsilon);
		
		double d = p1.minus(p2).norm();
		
		assertTrue( (Math.abs(d-2*a) < epsilon) );
	}

}
