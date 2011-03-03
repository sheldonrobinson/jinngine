/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.examples;

import jinngine.geometry.Box;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.ContactTrigger;
import jinngine.physics.ContactTrigger.Callback;
import jinngine.physics.DefaultScene;
import jinngine.physics.Scene;
import jinngine.physics.constraint.contact.ContactConstraint;
import jinngine.physics.force.GravityForce;
import jinngine.rendering.Interaction;
import jinngine.rendering.Rendering;

public class TriggerExample implements Rendering.Callback {
    private final Scene scene;

    public TriggerExample() {

        // start jinngine
        this.scene = new DefaultScene();
        this.scene.setTimestep(0.1);

        // add boxes to bound the world
        final Box floor = new Box("Floor", 1500, 20, 1500);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, -30, 0), floor);
        this.scene.fixBody(floor.getBody(), true);

        final Box back = new Box("Back wall", 200, 200, 20);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, 0, -55), back);
        this.scene.fixBody(back.getBody(), true);

        final Box front = new Box("Front wall", 200, 200, 20);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, 0, -7), front);
        this.scene.fixBody(front.getBody(), true);

        final Box left = new Box("Left wall", 20, 200, 200);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(-35, 0, 0), left);
        this.scene.fixBody(left.getBody(), true);

        final Box right = new Box("Right wall", 20, 200, 200);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(10, 0, 0), right);
        this.scene.fixBody(right.getBody(), true);

        // body for attracting gravity
        final Body gravity = new Body("Gravity");
        this.scene.addBody(gravity);
        this.scene.fixBody(gravity, true);

        // create a box
        final Box box = new Box("box", 2, 2, 2);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(-10, -11, -25), box);

        // put gravity on box
        this.scene.addConstraint(new GravityForce(box.getBody(), gravity));

        // create a trigger to detect contact forces with some threshold
        this.scene.addTrigger(box.getBody(), new ContactTrigger(2.0, new Callback() {
            @Override
            public void contactAboveThreshold(final Body interactingBody, final ContactConstraint constraint) {
                System.out.println("In contact with " + interactingBody);
            }

            @Override
            public void contactBelowThreshold(final Body interactingBody, final ContactConstraint constraint) {
                System.out.println("No longer in contact with " + interactingBody);
            }
        }));

        // handle drawing
        final Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);
        rendering.addCallback(new Interaction(this.scene));
        rendering.drawMe(box);
        rendering.createWindow();
        rendering.start();
    }

    @Override
    public void tick() {
        // each frame, to a time step on the Scene
        this.scene.tick();
    }

    public static void main(final String[] args) {
        new TriggerExample();
    }

}
