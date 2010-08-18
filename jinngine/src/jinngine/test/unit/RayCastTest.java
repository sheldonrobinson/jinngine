/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */

package jinngine.test.unit;

import jinngine.collision.RayCast;
import jinngine.geometry.Box;
import jinngine.geometry.Sphere;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import junit.framework.TestCase;

@SuppressWarnings("unused")
public class RayCastTest extends TestCase {
	
	// epsilon is the allowed error in the 
	// computed distances. envelope is the shell 
	// around objects that is considered as contact area
	double epsilon = 1e-10;
	double envelope = 1e-7;
	
	/**
	 * create a sphere and cast a ray against it
	 */
	public void testRay1() {
		RayCast raycast = new RayCast();
		
		// setup cube geometry
		Sphere s1 = new Sphere(1);
		Body b1 = new Body("default", s1);
		
		// pick a point outside the sphere, and let the direction point towards 
		// the centre of the sphere.
		Vector3 point = new Vector3(-4, 6, 9);
		Vector3 direction = point.multiply(-1);
		
		// do the raycast
		double lambda = raycast.run(s1, null, point, direction, new Vector3(), new Vector3(), 0, envelope, epsilon, false );
		
		// we know the exact intersection point
		Vector3 expected = point.normalize();
		
		// calculate the deviation of the returned point and the reference point
		double error = point.add(direction.multiply(lambda)).sub(expected).norm();
		
		// deviation from expected hitpoint should be lower than envelope+epsilon
		assertTrue(error < envelope+epsilon);
	}
	
	/**
	 * A similar ray test where the sphere is moved off the origo
	 */
	public void testRay2() {
		RayCast raycast = new RayCast();
		
		// setup cube geometry
		Sphere s1 = new Sphere(1);
		Body b1 = new Body("default", s1);
		b1.setPosition(2,-3,-1);
		
		// pick a point outside the sphere, and let the direction point towards 
		// the centre of the sphere.
		Vector3 point = new Vector3(-4, 6, 9);
		Vector3 direction = b1.getPosition().sub(point);
		
		// do the raycast
		double lambda = raycast.run(s1, null, point, direction, new Vector3(), new Vector3(), 0, envelope, epsilon, false );
		
		// we know the exact intersection point ( go from the centre of the sphere
		// to the boundary along the oposite ray direction )
		Vector3 expected = b1.getPosition().sub(direction.normalize());
		
		// calculate the deviation of the returned point and the reference point
		double error = point.add(direction.multiply(lambda)).sub(expected).norm();
		
		System.out.println("error" + error);
		
		// deviation from expected hitpoint should be lower than envelope+epsilon
		assertTrue(error < envelope+epsilon);
		
	}
	
	
	/**
	 * A sphere is placed at the origo, and a ray is shot in such a way, that is 
	 * exactly misses the sphere. Due to the envelope, we still expect a hit, 
	 * and this hitpoint should be within the envelope around the sphere. 
	 * Next, we move the sphere a bit, such that we expect a miss.
	 */
	public void testRay3() {
		RayCast raycast = new RayCast();
		
		// setup sphere geometry
		Sphere s1 = new Sphere(1);
		Body b1 = new Body("default", s1);

		// select a point (5,1,0) and the raydirection (-1,0,0)
		Vector3 point = new Vector3(5, 1, 0);
		Vector3 direction = new Vector3(-1,0,0);
		
		// do the raycast
		double lambda = raycast.run(s1, null, point, direction, new Vector3(), new Vector3(), 0, envelope, epsilon, false );
		
		// calculate the  point 
		Vector3 p = point.add(direction.multiply(lambda));
		
		// the hitpoint must be within the envelope
		assertTrue( Math.abs(p.norm()-1) < envelope+epsilon);
		
		b1.setPosition(0,-envelope-epsilon, 0);
		
		// do the raycast
		lambda = raycast.run(s1, null, point, direction, new Vector3(), new Vector3(), 0, envelope, epsilon, false );
		
		System.out.println("returned lambda="+lambda);
		
		assertTrue( lambda == Double.POSITIVE_INFINITY);
		
	}
	
	
	/**
	 * A ray against box test
	 */
	public void testRay4() {
		RayCast raycast = new RayCast();
		
		// setup cube geometry
		Box box = new Box(1,1,1);
		Body b1 = new Body("default", box);

		// select a point (5,1,0) and the raydirection (-1,0,0)
		Vector3 point = new Vector3(0, 5, 0);
		Vector3 direction = new Vector3(0,-1,0);
		
		// do the raycast
		double lambda = raycast.run(box, null, point, direction, new Vector3(), new Vector3(), 0, envelope, epsilon, false );
		
		// calculate the  point 
		Vector3 p = point.add(direction.multiply(lambda));
		
		// expected point
		Vector3 e = new Vector3(0,0.5,0);
		
		// the hitpoint must be within the envelope
		assertTrue( p.sub(e).norm() < envelope+epsilon);
	}

	/**
	 * A ray against box corner test
	 */
	public void testRay5() {
		RayCast raycast = new RayCast();
		
		// setup cube geometry
		Box box = new Box(1,1,1);
		Body b1 = new Body("default", box);

		// select a point (5,1,0) and the raydirection (-1,0,0)
		Vector3 point = new Vector3(2, 5, 9);
		Vector3 direction = new Vector3(0.5,0.5,0.5).sub(point);
		
		// do the raycast
		double lambda = raycast.run(box, null, point, direction, new Vector3(), new Vector3(), 0, envelope, epsilon, false );
		
		// calculate the  point 
		Vector3 p = point.add(direction.multiply(lambda));
		
		// expected point
		Vector3 e = new Vector3(0.5,0.5,0.5);
		
		// the hitpoint must be within the envelope
		assertTrue( p.sub(e).norm() < envelope+epsilon);
	}

	


}
