/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.test.unit;
import jinngine.collision.ExhaustiveSearch;
import jinngine.collision.BroadphaseCollisionDetection;
import jinngine.collision.SweepAndPrune;
import jinngine.collision.BroadphaseCollisionDetection.Handler;
import jinngine.geometry.Box;
import jinngine.geometry.Geometry;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.util.Pair;
import junit.framework.TestCase;

public class SweepAndPruneTest extends TestCase {

	
	/**
	 * Primitive test using cubes and the {@link BroadphaseCollisionDetection} method getOverlappingPairs()
	 */
	public void testSweepAndPrune1() {
		
		double epsilon = 1e-15;
		
		//create two cubes
		Box box1 = new Box(1,1,1);
		Body b1 = new Body("Box 1", box1);
		Box box2 = new Box(1,1,1);
		Body b2 = new Body("Box 2", box2);
		
		// set the envelope size (or collision margin)
		final double env = 1.0;
		box1.setEnvelope(env);
		box2.setEnvelope(env);
		
		
		BroadphaseCollisionDetection.Handler handler = new Handler() {
			public void overlap(Pair<Geometry> pair) {
			}
			public void separation(Pair<Geometry> pair) {
			}
		};

		//create the detector with handler
		BroadphaseCollisionDetection sweep = new SweepAndPrune(handler);

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

		//move box2 1.0 along z axis
		b2.setPosition(0,0,1.0);
		
		// expect an overlap
		sweep.run();
		assertTrue( sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1,box2)));

		// move box2 by 1.0 + twice the envelope + epsilon 
		// this will make bounding boxes only just separated
		b2.setPosition(0,0,1.0+2*env+epsilon);
		
		// expect no overlap
		sweep.run();
		assertTrue( !sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1,box2)) );

		// move box2 by 1 + 2 env - epsilon
		// such that boxes will only just overlap by epsilon
		b2.setPosition(new Vector3(0,0,1.0+2*env-epsilon));

		// expect an overlap
		sweep.run();
		assertTrue( sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1,box2)));
	}
}
