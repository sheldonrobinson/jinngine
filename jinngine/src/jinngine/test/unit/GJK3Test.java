/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.test.unit;

import jinngine.collision.GJK;
import jinngine.geometry.Box;
import jinngine.geometry.Sphere;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import junit.framework.TestCase;

public class GJK3Test extends TestCase {
	
	//error tolerance
	double epsilon = 1e-12;

	
	/*
	 * Two spheres of radius 1 having a distance of 1.
	 * Sphere 1 is at (0,3,0), Sphere 2 is at (0,0,0)
	 */
	public void testSphereSphere1() {
		GJK gjk = new GJK();
		
		//set up geometries
		Sphere s1 = new Sphere(1);
		Body b1 = new Body("default");
		b1.addGeometry(Matrix3.identity(), new Vector3(), s1);
		b1.setPosition(new Vector3(0,3,0));
		
		Sphere s2 = new Sphere(1);
		Body b2 = new Body("default");
		b2.addGeometry(Matrix3.identity(), new Vector3(), s2);
		b2.setPosition(new Vector3(0,0,0));
		
		//closest point vectors
		Vector3 p1 = new Vector3();
		Vector3 p2 = new Vector3();
				
		gjk.run(s1,s2,p1,p2,Double.POSITIVE_INFINITY, epsilon, 31);
		
		double d = p1.sub(p2).norm();
		
		System.out.println( "d="+d);
		
		// expect distance to be one ( take into account sphere sweeping)
		assertTrue( Math.abs(d-1-2) < epsilon );
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
		double a = 1e-3;
				
		//set up geometries
		Sphere s1 = new Sphere(1);
		Body b1 = new Body("default");
		b1.addGeometry(Matrix3.identity(), new Vector3(), s1);
		b1.setPosition(new Vector3(0,1+a,0));
		
		Sphere s2 = new Sphere(1);
		Body b2 = new Body("default");
		b2.addGeometry(Matrix3.identity(), new Vector3(), s2);
		b2.setPosition(new Vector3(0,-1-a,0));
		
		//closest point vectors
		Vector3 p1 = new Vector3();
		Vector3 p2 = new Vector3();
				
		gjk.run(s1,s2,p1,p2,Double.POSITIVE_INFINITY, epsilon, 31);
		
		double d = p1.sub(p2).norm();
		System.out.println("d="+d);
		
		// distance should be 2a within precision
		assertTrue( (Math.abs(d-2*a-2) < epsilon) );
	}
	
	/**
	 * Two spheres excactly overlapping each other
	 */
	public void testOverlapSphere1() {
		GJK gjk = new GJK();
		
		//set up two spheres, occupying the exact same space
		Sphere s1 = new Sphere(1);
		Body b1 = new Body("default");
		b1.addGeometry(Matrix3.identity(), new Vector3(), s1);
		b1.setPosition(new Vector3(0,0,0));

		Sphere s2 = new Sphere(1);
		Body b2 = new Body("default");
		b2.addGeometry(Matrix3.identity(), new Vector3(), s2);
		b2.setPosition(new Vector3(0,0,0));
		
		//closest point vectors
		Vector3 p1 = new Vector3();
		Vector3 p2 = new Vector3();
				
		gjk.run(s1,s2,p1,p2,Double.POSITIVE_INFINITY, epsilon, 31);
		
		// we expect closest points to be the same
		assertTrue( p1.sub(p2).norm() < epsilon );
	}
	
	/*
	 * Two spheres initially overlapping. One of the spheres is gradually moved
	 * out of the other sphere. At all times, we expect the calculated distance to 
	 * be within epsilon precision.
	 */
	public void testOverlapSphere2() {
		
		//set up two spheres, occupying the exact same space
		Sphere s1 = new Sphere(1);
		Body b1 = new Body("default");
		b1.addGeometry(Matrix3.identity(), new Vector3(), s1);
		b1.setPosition(new Vector3(0,0,0));

		Sphere s2 = new Sphere(1);
		Body b2 = new Body("default");
		b2.addGeometry(Matrix3.identity(), new Vector3(), s2);
		b2.setPosition(new Vector3(0,0,0));
		
		int N = 1024*2;
		Vector3 displacement = new Vector3(-4,1,-2);
		for (int i=0; i<N; i++) {
			// displace geometry and find the expected distance
			b1.setPosition(displacement.multiply((double)i/N));
			double length = displacement.multiply((double)i/N).norm();
			double expected = length-2;

			//closest point vectors
			Vector3 p1 = new Vector3();
			Vector3 p2 = new Vector3();

			// create a new gjk every time, to avoid the frame coherence 
			// heuristic inside GJK
			GJK gjk = new GJK();
			
			// when the distance between objects becomes close to zero, 
			// the gjk algorithm gegenerates and produces less acurate results.
			// therefore we use more iterations here
			gjk.run(s1,s2,p1,p2,Double.POSITIVE_INFINITY, epsilon, 2256);
			
			// we want the expected distance ( with a high error tolerance )
			assertTrue( Math.abs( p1.sub(p2).norm() - expected -2 ) < 1e-6 );
		}
	}
	
	public void testBoxBox1() {
		Body body = new Body("");
		Box box1 = new Box("", 1, 1, 1);
		Box box2 = new Box("", 1, 1, 1);
		body.addGeometry(Matrix3.identity(), new Vector3(0,0,0), box1);
		body.addGeometry(Matrix3.identity(), new Vector3(2,2,2), box2);

		// expected distance
		double expected = Math.sqrt(3);
		
		//closest point vectors
		Vector3 p1 = new Vector3();
		Vector3 p2 = new Vector3();

		// create a new gjk every time, to avoid the frame coherence 
		// heuristic inside GJK
		GJK gjk = new GJK();
		
		// when the distance between objects becomes close to zero, 
		// the gjk algorithm gegenerates and produces less acurate results.
		// therefore we use more iterations here
		gjk.run(box1,box2,p1,p2,Double.POSITIVE_INFINITY, epsilon, 2256);
		
		// we want the expected distance 
		assertTrue( Math.abs( p1.sub(p2).norm() - expected ) < epsilon );
		
	}


}
