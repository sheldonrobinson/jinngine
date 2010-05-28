package jinngine.test.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import jinngine.geometry.util.ORourke;
import jinngine.math.Vector3;

import junit.framework.TestCase;

public class ORourkeTest extends TestCase {

	private double epsilon = 1e-12;
	
	
	public void testIntersection() {
		// test for the 2d intersection of lines
		// setup two lines	
		Vector3 p1 = new Vector3(0,0,0);
		Vector3 p2 = new Vector3(1,1,0);
		Vector3 p3 = new Vector3(0,1,0);
		Vector3 p4 = new Vector3(1,0,0);
		Vector3 result = new Vector3();

		ORourke.lineLineIntersection(p1, p2, p3, p4, result, epsilon);
		
		// we expect lines to intersect at their centre points
		assertTrue( Math.abs(result.x-0.5) < epsilon );
		assertTrue( Math.abs(result.y-0.5) < epsilon );
	}

	public void testIntersection2() {
		// setup two co-incident lines 
		Vector3 p1 = new Vector3(0,0,0);
		Vector3 p2 = new Vector3(1,1,0);
		Vector3 p3 = new Vector3(1,1,0);
		Vector3 p4 = new Vector3(2,2,0);
		Vector3 result = new Vector3();

		// Expect false, because result is non-unique
		assertFalse(ORourke.lineLineIntersection(p1, p2, p3, p4, result, 1e-7) );		
	}
	
	public void testIntersection3() {
		// test for the 2d intersection of lines setup
		// two lines that does not have the same length	
		Vector3 p1 = new Vector3(0,0,0);
		Vector3 p2 = new Vector3(2,2,0);
		Vector3 p3 = new Vector3(0,1,0);
		Vector3 p4 = new Vector3(1,0,0);
		Vector3 result = new Vector3();

		// compute 2d intersection
		ORourke.lineLineIntersection(p1, p2, p3, p4, result, epsilon);
		
		// we expect lines to intersect t=0.25 and s= 0.5
		assertTrue( Math.abs(result.x-0.25) < epsilon );
		assertTrue( Math.abs(result.y-0.5) < epsilon );
		
		// calculate the points given by the returned parameter values
		Vector3 point1 = p1.add( p2.minus(p1).multiply(result.x));
		Vector3 point2 = p3.add( p4.minus(p3).multiply(result.y));
		
		// we expect the intersection point to be (0.5,0.5) for both lines		
		assertTrue( point1.minus(new Vector3(0.5,0.5,0)).xynorm() < epsilon );
		assertTrue( point2.minus(new Vector3(0.5,0.5,0)).xynorm() < epsilon );
	}


	public void testIsInHalfPlane() {
		// test for the half-plane test. The O'Rourke algorithm
		// determines if points are placed in half-planes around line segments
		// of polygons
		Vector3 p = new Vector3(1,1,0);
		Vector3 p1 = new Vector3(0,0,0);
		Vector3 p2 = new Vector3(1,0,0);
		
		assertTrue( ORourke.isInHalfplane(p, p1, p2));
	}
	
	public void testIsContained() {
		// one 1 by 1 box and a point x
	    //                    
		//       ------z      
		//       |     |   y   
	    //       |  x  |      
        // (0,0) -------      
		
		final List<Vector3> box1 = new ArrayList<Vector3>();
		
		// build each box, counter clock wise		
		box1.add( new Vector3(0,0,0));
		box1.add( new Vector3(1,0,0));
		box1.add( new Vector3(1,1,0));
		box1.add( new Vector3(0,1,0));
		
		// a point that's inside the box
		final Vector3 x = new Vector3(0.5,0.5,0);
		
		// a point that's outside the box
		final Vector3 y = new Vector3(2,0.5,0);
		
		// point that's on the cornor of the box ( should be classified as inside)
		final Vector3 z = new Vector3(1,1,0);
		
		// perform tests
		assertTrue( ORourke.isContained(x, box1.iterator()));		
		assertFalse( ORourke.isContained(y, box1.iterator()));
		assertTrue( ORourke.isContained(z, box1.iterator()));
	}
	
	
	public void testBoxBox() {
		// build a two simple 1 by 1 boxes
	    //          -------
		//       ---x--x  |   
		//       |  |  |  |   
	    //       |  x--x---   
        // (0,0) -------      
		
		List<Vector3> box1 = new ArrayList<Vector3>();
		List<Vector3> box2 = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build each box, counter clock wise		
		box1.add( new Vector3(0,0,0));
		box1.add( new Vector3(1,0,0));
		box1.add( new Vector3(1,1,0));
		box1.add( new Vector3(0,1,0));

		box2.add( new Vector3(0.5,0.5,0));
		box2.add( new Vector3(1.5,0.5,0));
		box2.add( new Vector3(1.5,1.5,0));
		box2.add( new Vector3(0.5,1.5,0));

		// we expect the an overlap box with coordinates
		expected.add( new Vector3(0.5,0.5,0) ); 
		expected.add( new Vector3(1,0.5,0) ); 
		expected.add( new Vector3(1,1,0) ); 
		expected.add( new Vector3(0.5,1,0) ); 

		// run intersection
		ORourke.run(box1, box2, result);
		
		// write out the returned polygon
//		for (Vector3 p: result)
//			System.out.println(""+p);
		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}
	
	public void testBoxBox2() {
		// two coinciding 1 by 1 boxes
	    //             -------
		//       ------x     |
		//       |     |     |
	    //       |     x------
        // (0,0) -------      
		
		List<Vector3> box1 = new ArrayList<Vector3>();
		List<Vector3> box2 = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build each box, counter clock wise		
		box1.add( new Vector3(0,0,0));
		box1.add( new Vector3(1,0,0));
		box1.add( new Vector3(1,1,0));
		box1.add( new Vector3(0,1,0));

		box2.add( new Vector3(1.0,0.5,0));
		box2.add( new Vector3(2.0,0.5,0));
		box2.add( new Vector3(2.0,1.5,0));
		box2.add( new Vector3(1.0,1.5,0));

		// we expect the an overlap line with coordinates
		expected.add( new Vector3(1.0,0.5,0) ); 
		expected.add( new Vector3(1.0,1.0,0) ); 

		// run intersection
		ORourke.run(box1, box2, result);
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);
		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}
	
	public void testBoxBox3() {
		// two 1 by 1 boxes coinciding in a single vertex
		//
		//             -------
		//             |     |
		//             |     |
		//       ------x------
		//       |     |      
	    //       |     |     -
        // (0,0) -------      
		
		List<Vector3> box1 = new ArrayList<Vector3>();
		List<Vector3> box2 = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build each box, counter clock wise		
		box1.add( new Vector3(0,0,0));
		box1.add( new Vector3(1,0,0));
		box1.add( new Vector3(1,1,0));
		box1.add( new Vector3(0,1,0));

		box2.add( new Vector3(1.0,1.0,0));
		box2.add( new Vector3(2.0,1.0,0));
		box2.add( new Vector3(2.0,2.0,0));
		box2.add( new Vector3(1.0,2.0,0));

		// we expect the an overlap point with coordinates
		expected.add( new Vector3(1.0,1.0,0) ); 

		// run intersection
		ORourke.run(box1, box2, result);
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);
		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}
	
	public void testBoxBox35() {
		// two alike 1 by 1 boxes
		//
		//       x-----x
		//       |     |      
	    //       |     |     -
        // (0,0) x-----x      
		
		List<Vector3> box1 = new ArrayList<Vector3>();
		List<Vector3> box2 = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build each box, counter clock wise		
		box1.add( new Vector3(0,0,0));
		box1.add( new Vector3(1,0,0));
		box1.add( new Vector3(1,1,0));
		box1.add( new Vector3(0,1,0));

		box2.add( new Vector3(0,0,0));
		box2.add( new Vector3(1,0,0));
		box2.add( new Vector3(1,1,0));
		box2.add( new Vector3(0,1,0));


		// we expect the an overlap at vertices of either box
		expected.addAll( box2 ); 

		// run intersection
		ORourke.run(box1, box2, result);
		
		System.out.println("overlap box case");
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);
		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}

	public void testBoxBox4() {
		// two 1 by 1 boxes completely separated
		//               -------
		//               |     |
		//               |     |
		//               -------
		//       ------(1,1)    
		//       |     |      
	    //       |     |     -
        // (0,0) -------      
		
		List<Vector3> box1 = new ArrayList<Vector3>();
		List<Vector3> box2 = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build each box, counter clock wise		
		box1.add( new Vector3(0,0,0));
		box1.add( new Vector3(1,0,0));
		box1.add( new Vector3(1,1,0));
		box1.add( new Vector3(0,1,0));

		box2.add( new Vector3(1.5,1.5,0));
		box2.add( new Vector3(2.5,1.5,0));
		box2.add( new Vector3(2.5,2.5,0));
		box2.add( new Vector3(1.5,2.5,0));

		// we expect an empty list

		// run intersection
		ORourke.run(box1, box2, result);
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);
		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}
	
	public void testBoxBox5() {
		// two 1 by 1 boxes, the second contained in the first
		//       -----------
		//       |  -----  |
		//       |  |   |  |
		//       |  |   |  |
	    //       |  -----  |
        // (0,0) -----------      
		
		List<Vector3> box1 = new ArrayList<Vector3>();
		List<Vector3> box2 = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build each box, counter clock wise		
		box1.add( new Vector3(-1,-1,0));
		box1.add( new Vector3(1,-1,0));
		box1.add( new Vector3(1,1,0));
		box1.add( new Vector3(-1,1,0));

		box2.add( new Vector3(-0.5,-0.5,0));
		box2.add( new Vector3(0.5,-0.5,0));
		box2.add( new Vector3(0.5,0.5,0));
		box2.add( new Vector3(-0.5,0.5,0));

		// we expect box2 to be returned
		expected.addAll(box2);

		// run intersection
		ORourke.run(box1, box2, result);
		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}
	
	public void testLineBox6() {
		// two 1 by 1 box intersected by a line (a polygon of 2 vertices)
		//
		//       -------
		//       |     |      
	    //       |  x--x----
        // (0,0) -------      
		
		List<Vector3> box1 = new ArrayList<Vector3>();
		List<Vector3> line = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build each box, counter clock wise		
		box1.add( new Vector3(0,0,0));
		box1.add( new Vector3(1,0,0));
		box1.add( new Vector3(1,1,0));
		box1.add( new Vector3(0,1,0));

		line.add( new Vector3(0.5,0.5,0));
		line.add( new Vector3(1.5,0.5,0));

		// we expect box2 to be returned
		expected.add(new Vector3(0.5,0.5,0));
		expected.add(new Vector3(1.0,0.5,0));
		
		// run intersection
		ORourke.run(line, box1, result);	

		System.out.println("line box case");
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);

		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}
	
	public void testLineBox7() {
		// two 1 by 1 box intersected by a line (a polygon of 2 vertices)
		//
		//       -------
		//       |     |      
	    //     --x-----x----
        // (0,0) -------      
		
		List<Vector3> box1 = new ArrayList<Vector3>();
		List<Vector3> line = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build each box, counter clock wise		
		box1.add( new Vector3(0,0,0));
		box1.add( new Vector3(1,0,0));
		box1.add( new Vector3(1,1,0));
		box1.add( new Vector3(0,1,0));

		line.add( new Vector3(-1.5,0.5,0));
		line.add( new Vector3(1.5,0.5,0));

		// we expect box2 to be returned
		expected.add(new Vector3(0.0,0.5,0));
		expected.add(new Vector3(1.0,0.5,0));
		
		// run intersection
		ORourke.run(line, box1, result);	

		System.out.println("line box case");
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);

		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}
	
	public void testLineDiamond8() {
		// A line intersecting a diamond in a single point
		//
		//    /\
		//   /  \
		//  <    x----o
		//   \  /
		//    \/

		List<Vector3> diamond1 = new ArrayList<Vector3>();
		List<Vector3> line = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build a diamond, counter clock wise		
		diamond1.add( new Vector3(0,1,0));
		diamond1.add( new Vector3(-1,0,0));
		diamond1.add( new Vector3(0,-1,0));
		diamond1.add( new Vector3(1,0,0));

		// a line intersecting only in (0,1)
		line.add( new Vector3(1,0,0));
		line.add( new Vector3(2,0,0));

		// we intersection in a single point (1,0)
		expected.add(new Vector3(1,0,0));
		
		// run intersection
		ORourke.run(line, diamond1, result);	

		System.out.println("line diamond case");
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);

		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}
	
	public void testLineDiamond9() {
		// A line intersecting a diamond in a single point
		//
		//    /\ |
		//   /  \|
		//  <    x
		//   \  /|
		//    \/ |

		List<Vector3> diamond1 = new ArrayList<Vector3>();
		List<Vector3> line = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build a diamond, counter clock wise		
		diamond1.add( new Vector3(0,1,0));
		diamond1.add( new Vector3(-1,0,0));
		diamond1.add( new Vector3(0,-1,0));
		diamond1.add( new Vector3(1,0,0));

		// a line intersecting only in (0,1)
		line.add( new Vector3(1, 1,0));
		line.add( new Vector3(1,-1,0));

		// we intersection in a single point
		expected.add(new Vector3(1,0,0));
		
		// run intersection
		ORourke.run(line, diamond1, result);	

		System.out.println("line diamond case");
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);

		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}

	public void testLineDiamond10() {
		// A line intersecting a diamond along one of its edges
		//    \ 
		//    /x  
		//   /  \ 
		//  <    x 
		//   \  / \
		//    \/   \

		List<Vector3> diamond = new ArrayList<Vector3>();
		List<Vector3> line = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build a diamond, counter clock wise		
		diamond.add( new Vector3(0,1,0));
		diamond.add( new Vector3(-1,0,0));
		diamond.add( new Vector3(0,-1,0));
		diamond.add( new Vector3(1,0,0));

		//f(x) = -1 x +1 
		
		// a line coinciding with the edge (0,1)->(1,0)
//		line.add( new Vector3( 2, -1, 0));
//		line.add( new Vector3(-1, 2, 0));
		line.add( new Vector3(-1, 2, 0));
		line.add( new Vector3( 2, -1, 0));

		// we intersection in two points
		expected.add(new Vector3(1,0,0));
		expected.add(new Vector3(0,1,0));		
		
		// run intersection
		ORourke.run(line, diamond, result);	

		System.out.println("line diamond case");
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);

		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}
	
	public void testLineLine11() {
		// Two lines crossing at (0,0)
        //  (-1,1) (1,1)
		//     o    o
		//      \  /
		//       \/
		//       /\  
		//      /  \
		//     o    o
		// (-1,-1)  (1,-1)

		List<Vector3> line1 = new ArrayList<Vector3>();
		List<Vector3> line2 = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build lines		
		line1.add( new Vector3(1,1,0));
		line1.add( new Vector3(-1,-1,0));

		line2.add( new Vector3( 1, -1, 0));
		line2.add( new Vector3(-1,  1, 0));

		// we intersection in two points
		expected.add(new Vector3(0,0,0));
		
		// run intersection
		ORourke.run(line1, line2, result);	

		System.out.println("line line case");
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);

		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}

	public void testLineLine12() {
		// coincident lines. 2. line is contained in the 1. line
        //               
		//        (-0.5,0)       
		//        
		//(-1,0) o----x----x----0  (0,1)      
		//           
		//              (0,0.5)  
		//           
		//                  

		List<Vector3> line1 = new ArrayList<Vector3>();
		List<Vector3> line2 = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build lines		
		line1.add( new Vector3(-1,0,0));
		line1.add( new Vector3( 1, 0,0));

		line2.add( new Vector3( -0.5, 0, 0));
		line2.add( new Vector3(  0.5, 0, 0));

		// we intersection in two points
		expected.add(new Vector3(-0.5,0,0));
		expected.add(new Vector3( 0.5,0,0));
		
		// run intersection
		ORourke.run(line1, line2, result);	

		System.out.println("coincident line line case");
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);

		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}
	
	public void testLineLine13() {
		// co-incident lines. Lines are overlapping
        //               
		//        (-0.5,0)       
		//        
		//(-1,0) o----x----o----x  (0,1)      
		//           
		//              (0,0.5)  
		//           
		//                  

		List<Vector3> line1 = new ArrayList<Vector3>();
		List<Vector3> line2 = new ArrayList<Vector3>();
		List<Vector3> result = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build lines		
		line1.add( new Vector3(-1,0,0));
		line1.add( new Vector3( 0.5, 0,0));

		line2.add( new Vector3( -0.5, 0, 0));
		line2.add( new Vector3(  1, 0, 0));

		// we intersection in two points
		expected.add(new Vector3(-0.5,0,0));
		expected.add(new Vector3( 0.5,0,0));
		
		// run intersection
		ORourke.run(line1, line2, result);	

		System.out.println("coincident line line case");
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);

		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
	}
	
	public void testLineLine14() {
		//  Equivalent lines
        //               
		//              
		//       x--------------x 
		//(-1,5) o--------------o  (-5,1)      
		//           
		//                
		//           
		//                  
		List<Vector3> line1    = new ArrayList<Vector3>();
		List<Vector3> line2    = new ArrayList<Vector3>();
		List<Vector3> result   = new ArrayList<Vector3>();
		List<Vector3> expected = new ArrayList<Vector3>();
		
		// build lines		
		line1.add( new Vector3( -1, 5,0));
		line1.add( new Vector3( -5, 1,0));
		line2.add( new Vector3( -1, 5,0));
		line2.add( new Vector3( -5, 1,0));

		// we intersection in two points
		expected.add(new Vector3(-1, 5, 0));
		expected.add(new Vector3(-5, 1, 0));
		
		// run intersection
		ORourke.run(line1, line2, result);	

		System.out.println("coincident line line case");
		
		// write out the returned polygon
		for (Vector3 p: result)
			System.out.println(""+p);

		
		// check result by first searching for the first vertex.
		// When found, we traverse the result again to verify the 
		// rest of the vertices, in order.
		assertTrue( verifyPolygon(result, expected));
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
					if (p2.minus(p1).xynorm() >= epsilon) 
						return false;
				} else {
					// no more vertices
					return true;
				}		
			} else {
				if (p2.minus(p1).xynorm() < epsilon) { 
					traversalStarted = true;
				}
			}
		}

		// if here we never found a starting vertex, return false
		return false;	
	}
		
}
	
