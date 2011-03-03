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

import jinngine.collision.SAP2;
import jinngine.geometry.Box;
import jinngine.geometry.Sphere;
import jinngine.geometry.UniformCapsule;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.DefaultScene;
import jinngine.physics.DisabledDeactivationPolicy;
import jinngine.physics.Scene;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
import jinngine.rendering.Interaction;
import jinngine.rendering.Rendering;

public class CapsuleExample implements Rendering.Callback {
    private final Scene scene;

    public CapsuleExample() {
        // start jinngine
        this.scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(50),
                new DisabledDeactivationPolicy());
        this.scene.setTimestep(0.1);

        // add boxes to bound the world
        final Box floor = new Box("floor", 1500, 20, 1500);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, -30, 0), floor);
        this.scene.fixBody(floor.getBody(), true);

        final Box back = new Box("back", 200, 200, 20);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, 0, -55), back);
        this.scene.fixBody(back.getBody(), true);

        final Box front = new Box("front", 200, 200, 20);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, 0, -7), front);
        this.scene.fixBody(front.getBody(), true);

        final Box left = new Box("left", 20, 200, 200);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(-35, 0, 0), left);
        this.scene.fixBody(left.getBody(), true);

        final Box right = new Box("right", 20, 200, 200);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(10, 0, 0), right);
        this.scene.fixBody(right.getBody(), true);

        // create capsules
        final UniformCapsule capgeo = new UniformCapsule("capgoe", 2, 3);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(-10, -11, -25), capgeo);

        final UniformCapsule capgeo2 = new UniformCapsule("capgoe2", 1.8, 2.5);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(-10, -11, -25), capgeo2);

        final UniformCapsule capgeo3 = new UniformCapsule("capgoe3", 1.0, 2);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(-10, -11, -25), capgeo3);

        //        final UniformCapsule capgeo4 = new UniformCapsule("capgoe4", 1.6, 1.0);
        //        this.scene.addGeometry(Matrix3.identity(), new Vector3(-10, -11, -25), capgeo4);
        final Sphere capgeo4 = new Sphere("capgoe4", 3);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(-10, -11, -25), capgeo4);

        // gravity absorbing body
        final Body gravity = new Body("gravity");
        this.scene.addBody(gravity);
        this.scene.fixBody(gravity, true);

        // put gravity on stuff
        this.scene.addConstraint(new GravityForce(capgeo.getBody(), gravity));
        this.scene.addConstraint(new GravityForce(capgeo2.getBody(), gravity));
        this.scene.addConstraint(new GravityForce(capgeo3.getBody(), gravity));
        this.scene.addConstraint(new GravityForce(capgeo4.getBody(), gravity));

        // handle drawing
        final Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);
        rendering.addCallback(new Interaction(this.scene));
        rendering.drawMe(capgeo);
        rendering.drawMe(capgeo2);
        rendering.drawMe(capgeo3);
        rendering.drawMe(capgeo4);

        rendering.createWindow();
        rendering.start();
    }

    @Override
    public void tick() {
        // each frame, to a time step on the Scene
        this.scene.tick();
    }

    public static void main(final String[] args) {
        new CapsuleExample();
    }

}
