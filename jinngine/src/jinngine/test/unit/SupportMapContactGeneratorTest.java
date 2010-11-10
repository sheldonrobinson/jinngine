package jinngine.test.unit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import jinngine.geometry.Box;
import jinngine.geometry.contact.ContactGenerator;
import jinngine.geometry.contact.SupportMapContactGenerator;
import jinngine.geometry.contact.ContactGenerator.ContactPoint;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import junit.framework.TestCase;

@SuppressWarnings("unused")
public class SupportMapContactGeneratorTest extends TestCase {
	
	private static final double epsilon = 1e-10;
	
	public void testBoxBox1() {
		// Two 1-cubes, one placed with centre in the origin, the other displaced 1.5 along
		// one of the Cartesian axes. We expect four contact points, one for each contacting corner of the 
		// boxes. The tests will displace the second box in different directions, and test for the expected
		// points to be returned. We always expect contact points to be ordered in counter clock wise order
		// wrt. the contact normal.
		//
		//      -0.0----0.75---1.5--->
		//     -------   x   ------- 
		//    |       |     |       |
		//    |   o   |     |   o   |
		//    |       |     |       |
		//     -------   x   ------- 
		
		// vertex lists
		final List<Vector3> result = new ArrayList<Vector3>();
		final List<Vector3> expect = new ArrayList<Vector3>();
		
		// setup box geometries
		final Box box1 = new Box("box",1,1,1);		
		box1.setEnvelope(1);
		final Box box2 = new Box("box",1,1,1);
		box2.setEnvelope(1);

		// attach geometries to bodies (to we can change the transform of the
		// boxes by changing the transform of the bodies)
		final Body body1 = new Body("box1");
		body1.addGeometry(Matrix3.identity(), new Vector3(), box1);
		final Body body2 = new Body("box2");
		body2.addGeometry(Matrix3.identity(), new Vector3(), box2);
				
		// contact generator
		ContactGenerator g = new SupportMapContactGenerator(box1,box1,box2,box2);
		

		/*
		 * Test 1, displace along positive y-axis 
		 */
		
		// displace box2
		body2.setPosition(0, 1.5, 0);
		
		// run contact point generation
		g.run();
		
		// extract contact points
		result.clear();
		Iterator<ContactPoint> i = g.getContacts();
		while (i.hasNext()) {
			ContactPoint cp = i.next();
			System.out.println(cp.point);
			System.out.println("dist="+cp.distance);
			result.add(new Vector3(cp.point));
		}
		
		// expected contact points, in counter clock-wise order 
		expect.clear();
		expect.add(new Vector3(0.5,0.75,0.5));
		expect.add(new Vector3(-0.5,0.75,0.5));
		expect.add(new Vector3(-0.5,0.75,-0.5));
		expect.add(new Vector3(0.5,0.75,-0.5));

		// check
		assertTrue(verifyPolygon(expect, result));

	
		/*
		 * Test 2, displace along negative y-axis 
		 */
		
		// displace box2
		body2.setPosition(0, -1.5, 0);
		
		// run contact point generation
		g.run();
		
		// extract contact points
		result.clear();
		i = g.getContacts();
		while (i.hasNext()) {
			ContactPoint cp = i.next();
			System.out.println(cp.point);
			System.out.println("dist="+cp.distance);
			result.add(new Vector3(cp.point));
		}
		
		// expected contact points, in counter clock-wise order 
		expect.clear();
		expect.add(new Vector3( 0.5,-0.75,-0.5));
		expect.add(new Vector3(-0.5,-0.75,-0.5));
		expect.add(new Vector3(-0.5,-0.75, 0.5));
		expect.add(new Vector3( 0.5,-0.75, 0.5));

		// check
		assertTrue(verifyPolygon(expect, result));

	
		/*
		 * Test 3, displace along positive x-axis 
		 */
		
		// displace box2
		body2.setPosition(1.5, 0, 0);
		
		// run contact point generation
		g.run();
		
		// extract contact points
		result.clear();
		i = g.getContacts();
		while (i.hasNext()) {
			ContactPoint cp = i.next();
			System.out.println(cp.point);
			System.out.println("dist="+cp.distance);
			result.add(new Vector3(cp.point));
		}
		
		// expected contact points, in counter clock-wise order 
		expect.clear();
		expect.add(new Vector3( 0.75, 0.5, 0.5));
		expect.add(new Vector3( 0.75, 0.5,-0.5));
		expect.add(new Vector3( 0.75,-0.5,-0.5));
		expect.add(new Vector3( 0.75,-0.5, 0.5));

		// check
		assertTrue(verifyPolygon(expect, result));
		
	}
	
	
	
	private boolean verifyPolygon( List<Vector3> poly1, List<Vector3> poly2) {
		// this is an auxiliary method that poly1 contains the same sequence of points 
		// as poly2, and vice versa. 
		if (poly1.size() == 0 && poly2.size() == 0)
			return true;
		
		if (poly1.size() != poly2.size())
			return false;
		
		// insert first polygon into the temporary list two times
		final List<Vector3> templist = new ArrayList<Vector3>();		
		templist.addAll(poly1);
		templist.addAll(poly1);

		// iterator for second polygon
		ListIterator<Vector3> poly2iter = poly2.listIterator();

		
		boolean traversalStarted = false;
		Vector3 p2 = poly2iter.next(); // poly2iter will always have a next at this point
		for (Vector3 p1: templist) {
			if (traversalStarted) {		
				// check the next vertex from poly2
				if (poly2iter.hasNext()) {
					p2 = poly2iter.next();
					
					// distance less that epsilon
					if (p2.sub(p1).norm() >= epsilon)
						return false;
				} else {
					// no more vertices
					return true;
				}		
			} else {
				if (p2.sub(p1).norm() < epsilon) {
					traversalStarted = true;
				}
			}
		}

		// if here we never found a starting vertex, return false
		return false;	
	}

}
