/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.test.unit;

import jinngine.collision.BroadphaseCollisionDetection;
import jinngine.collision.BroadphaseCollisionDetection.Handler;
import jinngine.collision.SAP2;
import jinngine.geometry.Box;
import jinngine.geometry.Geometry;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.util.Pair;
import junit.framework.TestCase;

public class SweepAndPruneTest extends TestCase {

    /**
     * Primitive test using cubes and the {@link BroadphaseCollisionDetection}
     * method getOverlappingPairs()
     */
    public void testSweepAndPrune1() {

        final double epsilon = 1e-15;

        //create two cubes
        final Box box1 = new Box("box1", 1, 1, 1);
        final Body b1 = new Body("Box 1");
        b1.addGeometry(Matrix3.identity(), new Vector3(), box1);

        final Box box2 = new Box("box2", 1, 1, 1);
        final Body b2 = new Body("Box 2");
        b2.addGeometry(Matrix3.identity(), new Vector3(), box2);

        // set the envelope size (or collision margin)
        final double env = 1.0;
        box1.setEnvelope(env);
        box2.setEnvelope(env);

        final BroadphaseCollisionDetection.Handler handler = new Handler() {
            @Override
            public void overlap(final Pair<Geometry> pair) {}

            @Override
            public void separation(final Pair<Geometry> pair) {}
        };

        //create the detector with handler 
        final BroadphaseCollisionDetection sweep = new SAP2();
        sweep.addHandler(handler);

        //add both boxes
        sweep.add(box1);
        sweep.add(box2);

        // update transforms
        box1.update();
        box2.update();

        // expect an overlap
        sweep.run();
        assertTrue(sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1, box2)));

        // remove box2
        sweep.remove(box2);

        // expect no overlaps
        sweep.run();
        assertTrue(sweep.getOverlappingPairs().size() == 0);

        // add box2 again
        sweep.add(box2);

        // expect an overlap
        sweep.run();
        assertTrue(sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1, box2)));

        //move box2 1.0 along z axis
        b2.setPosition(0, 0, 1.0);

        // update transforms
        box1.update();
        box2.update();

        // expect an overlap
        sweep.run();
        assertTrue(sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1, box2)));

        // move box2 by 1.0 + twice the envelope + epsilon 
        // this will make bounding boxes only just separated
        b2.setPosition(0, 0, 1.0 + 2 * env + epsilon);

        // update transforms
        box1.update();
        box2.update();

        // expect no overlap
        sweep.run();
        assertTrue(!sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1, box2)));

        // move box2 by 1 + 2 env - epsilon
        // such that boxes will only just overlap by epsilon
        b2.setPosition(new Vector3(0, 0, 1.0 + 2 * env - epsilon));

        // update transforms
        box1.update();
        box2.update();

        // expect an overlap
        sweep.run();
        assertTrue(sweep.getOverlappingPairs().contains(new Pair<Geometry>(box1, box2)));
    }
}
