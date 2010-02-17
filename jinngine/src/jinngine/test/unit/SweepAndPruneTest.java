/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.test.unit;
import jinngine.collision.AllPairsTest;
import jinngine.collision.BroadfaseCollisionDetection;
import jinngine.collision.SweepAndPrune;
import jinngine.collision.BroadfaseCollisionDetection.Handler;
import jinngine.geometry.Box;
import jinngine.geometry.Geometry;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.util.Pair;
import junit.framework.TestCase;

public class SweepAndPruneTest extends TestCase {

	
	/**
	 * Primitive test using cubes and the {@link BroadfaseCollisionDetection} method getOverlappingPairs()
	 */
	public void testSweepAndPrune1() {
		
		//create two cubes
		Box box1 = new Box(1,1,1);
		Body b1 = new Body(box1);
		Box box2 = new Box(1,1,1);
		Body b2 = new Body(box2);

		
		BroadfaseCollisionDetection.Handler handler = new Handler() {
			public void overlap(Pair<Geometry> pair) {
			}
			public void separation(Pair<Geometry> pair) {
			}
		};

		//create the detector with handler
		BroadfaseCollisionDetection sweep = new SweepAndPrune(handler);

		//add both boxes
		sweep.add(box1);
		sweep.add(box2);
		
		// expect an overlap
		sweep.run();
		assertTrue( sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1,box2)));
		
		// remove box2
		sweep.remove(box2);
		
		// expect no overlaps
		sweep.run();
		assertTrue( sweep.getOverlappingPairs().size() == 0);

		// add box2 again
		sweep.add(box2);
		
		// expect an overlap
		sweep.run();
		assertTrue( sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1,box2)));

		//move box2 0.5 along z axis 
		b2.setPosition(new Vector3(0,0,0.5));
		
		// expect an overlap
		sweep.run();
		assertTrue( sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1,box2)));

		//move box2 by the extend of the bounding box + epsilon 
		b2.setPosition(new Vector3(0,0,Math.sqrt(3)+1e-14));
		
		// expect no overlap
		sweep.run();
		assertTrue( !sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1,box2)));

		//move box2 by the extend of the bounding box MINUS epsilon 
		b2.setPosition(new Vector3(0,0,Math.sqrt(3)-1e-14));

		// expect an overlap
		sweep.run();
		assertTrue( sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1,box2)));
	}
}
