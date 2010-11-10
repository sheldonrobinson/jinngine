/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.test.unit;

import static org.junit.Assert.assertTrue;
import jinngine.geometry.Box;
import jinngine.geometry.Sphere;
import jinngine.math.Matrix3;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;

import org.junit.Test;

public class BodyTest {
	@Test
	public void testMassCalculation() {
		final double epsilon = 1e-14;
		
		Body body = new Body("test");
		
		Sphere u = new Sphere(1);
		
		// add a unit sphere
		body.addGeometry(Matrix3.identity(), new Vector3(0,1,0), u);
		
		// remove a unit sphere
		body.removeGeometry(u);
		
		// expect position to be at (0,1,0)
		assertTrue( body.getPosition().sub(new Vector3(0,1,0)).squaredNorm() < epsilon );				
	}

	@Test
	public void testMassCalculation2() {
		final double epsilon = 1e-14;
		
		Body body = new Body("test");
		
		Sphere u1 = new Sphere(1);
		Sphere u2 = new Sphere(1);
		
		// add a unit sphere at 1,1,1
		body.addGeometry(Matrix3.identity(), new Vector3(1,1,1), u1);

		// add a unit sphere at -1,-1,-1
		body.addGeometry(Matrix3.identity(), new Vector3(-1,-1,-1), u2);
				
		// expect position to be at (0,0,0)
		assertTrue( body.getPosition().sub(new Vector3(0,0,0)).squaredNorm() < epsilon );				
	}

	@Test
	public void testMassCalculation3() {
		/* Test adds 3 spheres one by one along the xaxis. The centre of mass position is 
		 * the tracked for each add. Afterwards the spheres are removed one by one, and 
		 * the centre of mass i likewise tested
		 */
		
		final double epsilon = 1e-14;
		
		Body body = new Body("test");
		
		Sphere u1 = new Sphere(1);
		Sphere u2 = new Sphere(1);
		Sphere u3 = new Sphere(1);
		
		/*
		 * Start adding spheres
		 */
		
		// add a unit sphere at 0,0,0
		body.addGeometry(Matrix3.identity(), new Vector3(0,0,0), u1);

		// mass centre should be unchanged at (0,0,0)
		assertTrue( body.getPosition().sub(new Vector3(0,0,0)).squaredNorm() < epsilon );				
		
		// add a unit sphere at 1,0,0
		body.addGeometry(Matrix3.identity(), new Vector3(1,0,0), u2);

		// mass centre should now be at (0.5,0,0)
		assertTrue( body.getPosition().sub(new Vector3(0.5,0,0)).squaredNorm() < epsilon );				
		
		// add a unit sphere at 1,0,0 
		body.addGeometry(Matrix3.identity(), new Vector3(2,0,0), u3);

		// mass centre should be at x coord 1
		assertTrue( body.getPosition().sub(new Vector3(1,0,0)).squaredNorm() < epsilon );				
		
		/*
		 * Start removing the spheres
		 */
		
		// remove the first two geometries 
		body.removeGeometry(u1);

		// mass centre should be at x coord 1.5
		assertTrue( body.getPosition().sub(new Vector3(1.5,0,0)).squaredNorm() < epsilon );				
				
		body.removeGeometry(u2);
				
		// we expect the positin of the body to be at the point where u3 started
		assertTrue( body.getPosition().sub(new Vector3(2,0,0)).squaredNorm() < epsilon );				
	}

	@Test
	public void testMassCalculation4() {
		final double epsilon = 1e-14;
		/* Test adds 2 spheres together. Afterwards one of the 
		 * spheres is removed, and we expect to end up with the same
		 * mass propperties as first sphere had on its own
		 */		
		
		Body body = new Body("test");
		
		Sphere u1 = new Sphere(1);
		Sphere u2 = new Sphere(1);
				
		// add a unit sphere at 0,0,0
		body.addGeometry(Matrix3.identity(), new Vector3(1,1,1),    u1);
		body.addGeometry(Matrix3.identity(), new Vector3(-1,-1,-1), u2);
		
		// mass centre should be  at (0,0,0)
		assertTrue( body.getPosition().sub(new Vector3(0,0,0)).squaredNorm() < epsilon );
		
		// remove the first sphere
		body.removeGeometry(u1);
		
		// mass centre should be  at (-1,-1,-1)
		assertTrue( body.getPosition().sub(new Vector3(-1,-1,-1)).squaredNorm() < epsilon );
				
//		System.out.println("MASS MATRICES:\n"+body.state.inertia);
//		System.out.println(""+u2.getInertiaMatrix().multiply(u2.getMass()));
		
		// we expect the inertia tensor of body to match the one in u2 (scaled in the u2 mass)
		assertTrue( body.state.inertia.subtract(u2.getInertiaMatrix().multiply(u2.getMass())).fnorm() < epsilon );
				
	}
	
	@Test
	public void testMassCalculation5() {
		final double epsilon = 1e-10; // the f-norm is a bit sensitive, so the epsilon needs
		// to be somewhat larger that machine 1e-15
		
		/* Test adds 4 spheres together. Afterwards one of the 
		 * spheres is removed, and we expect to end up with the same
		 * mass propperties as first sphere had on its own
		 */				
		Body body = new Body("test");
		
		Sphere u1 = new Sphere(1);
		Sphere u2 = new Sphere(2);
		Sphere u3 = new Sphere(3);
		Sphere u4 = new Sphere(4);
				
		// add a unit sphere at 0,0,0
		body.addGeometry(Matrix3.identity(), new Vector3(0,0,1), u1);
		body.addGeometry(Matrix3.identity(), new Vector3(0,1,0), u2);
		body.addGeometry(Matrix3.identity(), new Vector3(-1,0,0), u3);
		body.addGeometry(Matrix3.identity(), new Vector3(0,1,1), u4);
		
//		// mass centre should be  at (0,0,0)
//		assertTrue( body.getPosition().sub(new Vector3(0,0,0)).squaredNorm() < epsilon );
		
		// remove spheres in some random order
		body.removeGeometry(u2);
		body.removeGeometry(u3);
		body.removeGeometry(u1);
		
		// mass centre should be  at (0,1,1), the original position of u4
		assertTrue( body.getPosition().sub(new Vector3(0,1,1)).squaredNorm() < epsilon );
				
//		System.out.println("MASS MATRICES:\n"+body.state.inertia);
//		System.out.println(""+u4.getInertiaMatrix().multiply(u4.getMass()));
		
		// we expect the inertia tensor of body to match the one in u4 (scaled in the u4 mass)
		assertTrue( body.state.inertia.subtract(u4.getInertiaMatrix().multiply(u4.getMass())).fnorm() < epsilon );
		
	}
	
	@Test
	public void testMassCalculation6() {
		final double epsilon = 1e-5; // the f-norm is a bit sensitive, so the epsilon needs
		// to be somewhat larger that machine 1e-15
		
		/* By adding boxes together, we expect to end up with the same
		 * mass properties as if one big box filling the same space 
		 * would have.
		 */
		
		// composite body of 4 boxes, spanning a 4 by 4 by 2 area
		Body body1 = new Body("body1");				
		Box b1 = new Box("box1",2,2,2);
		Box b2 = new Box("box2",2,2,2);
		Box b3 = new Box("box3",2,2,2);
		Box b4 = new Box("box4",2,2,2);		
		body1.addGeometry(Matrix3.identity(), new Vector3(1,1,0), b1);
		body1.addGeometry(Matrix3.identity(), new Vector3(-1,1,0), b2);
		body1.addGeometry(Matrix3.identity(), new Vector3(1,-1,0), b3);
		body1.addGeometry(Matrix3.identity(), new Vector3(-1,-1,0), b4);
		
		// centre of mass should be 0,0,0
		assertTrue( body1.getPosition().squaredNorm() < 1e-14 ) ;
		
		// one solid 4x4x2 box
		Body body2 = new Body("body2");		
		Box b5 = new Box("box5",4,4,2);
		body2.addGeometry(Matrix3.identity(), new Vector3(0,0,0), b5);

//		System.out.println("MASS MATRICES:\n"+body1.state.inertia);
//		System.out.println(""+body2.state.inertia);
		
		// compare inertia matrices
		assertTrue( body1.state.inertia.subtract(body2.state.inertia).fnorm() < epsilon);
		
	}	
	
	@Test
	public void testMassCalculation7() {
		final double epsilon = 1e-5; // the f-norm is a bit sensitive, so the epsilon needs

		/*
		 * A test where two bodies are build of composite boxes. In the first case, this is
		 * done in a straight-forward way. The second time, it is done by rotating the body while
		 * attaching boxes. This must result in the same centre of mass, and the same inertia matrix.
		 */
		
		Body body1 = new Body("body1");				
		Box b1 = new Box("box1",1,1,1);
		Box b2 = new Box("box2",1,1,1);
		Box b3 = new Box("box3",1,1,1);

		body1.addGeometry(Matrix3.identity(), new Vector3(0, 1, 0), b1);
		body1.addGeometry(Matrix3.identity(), new Vector3(0,-1, 0), b2);
		body1.addGeometry(Matrix3.identity(), new Vector3(1, 0, 0), b3);

		// centre of mass should be 1/3,0,0
		assertTrue( body1.getPosition().sub(new Vector3(0.333333333333333,0,0)).squaredNorm() < 1e-14 ) ;
		
		Body body2 = new Body("body2");				
		Box b4 = new Box("box4",1,1,1);
		Box b5 = new Box("box5",1,1,1);
		Box b6 = new Box("box6",1,1,1);

		body2.addGeometry(Matrix3.identity(), new Vector3(0, 1, 0), b4);
		body2.addGeometry(Matrix3.identity(), new Vector3(0, -1, 0), b5);

		// rotate body2 around the Y axis, 180 degrees
		body2.state.orientation.assign( Quaternion.rotation(Math.PI, Vector3.j()));
		body2.updateTransformations();

		// add final body
		body2.addGeometry(Matrix3.identity(), new Vector3(1, 0, 0), b6);
	
		// centre of mass should be 1/3,0,0
		assertTrue( body1.getPosition().sub(new Vector3(0.333333333333333,0,0)).squaredNorm() < 1e-14 ) ;

		
//		System.out.println("MASS MATRICES:\n"+body1.state.inertia);
//		System.out.println(""+body2.state.inertia);

		
		// compare inertia matrices
		assertTrue( body1.state.inertia.subtract(body2.state.inertia).fnorm() < epsilon);
	
	}
}
