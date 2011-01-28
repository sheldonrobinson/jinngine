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

import java.util.ArrayList;
import java.util.List;

import jinngine.collision.SAP2;
import jinngine.geometry.Box;
import jinngine.geometry.ConvexHull;
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
        scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(50),
                new DisabledDeactivationPolicy());
        scene.setTimestep(0.1);

        // add boxes to bound the world
        final Box floor = new Box("floor", 1500, 20, 1500);
        scene.addGeometry(Matrix3.identity(), new Vector3(0, -30, 0), floor);
        scene.fixBody(floor.getBody(), true);

        final Box back = new Box("back", 200, 200, 20);
        scene.addGeometry(Matrix3.identity(), new Vector3(0, 0, -55), back);
        scene.fixBody(back.getBody(), true);

        final Box front = new Box("front", 200, 200, 20);
        scene.addGeometry(Matrix3.identity(), new Vector3(0, 0, -7), front);
        scene.fixBody(front.getBody(), true);

        final Box left = new Box("left", 20, 200, 200);
        scene.addGeometry(Matrix3.identity(), new Vector3(-35, 0, 0), left);
        scene.fixBody(left.getBody(), true);

        final Box right = new Box("right", 20, 200, 200);
        scene.addGeometry(Matrix3.identity(), new Vector3(10, 0, 0), right);
        scene.fixBody(right.getBody(), true);

        // create capsules
        final UniformCapsule capgeo = new UniformCapsule("capgoe", 2, 6);
        scene.addGeometry(Matrix3.identity(), new Vector3(-10, -11, -25), capgeo);

        final UniformCapsule capgeo2 = new UniformCapsule("capgoe2", 1.8, 5);
        scene.addGeometry(Matrix3.identity(), new Vector3(-10, -11, -25), capgeo2);

        final UniformCapsule capgeo3 = new UniformCapsule("capgoe3", 1.0, 4);
        scene.addGeometry(Matrix3.identity(), new Vector3(-10, -11, -25), capgeo3);

        final UniformCapsule capgeo4 = new UniformCapsule("capgoe4", 1.6, 1.0);
        scene.addGeometry(Matrix3.identity(), new Vector3(-10, -11, -25), capgeo4);

        // create a box
        final Box boxgeometry = new Box("box", 3, 3, 3);
        scene.addGeometry(Matrix3.identity(), new Vector3(-4, -11, -25), boxgeometry);

        // create a box
        final Box boxgeometry2 = new Box("box2", 4, 4, 4);
        scene.addGeometry(Matrix3.identity(), new Vector3(-3, -11, -25), boxgeometry2);

        // create a box
        final Box boxgeometry3 = new Box("box3", 5, 5, 5);
        scene.addGeometry(Matrix3.identity(), new Vector3(-3, -11, -25), boxgeometry3);

        // create ico
        final List<Vector3> vertices = new ArrayList<Vector3>();
        final double t = (1.0 + Math.sqrt(5.0)) / 2.0;
        vertices.add(new Vector3(-1, t, 0).normalize());
        vertices.add(new Vector3(1, t, 0).normalize());
        vertices.add(new Vector3(-1, -t, 0).normalize());
        vertices.add(new Vector3(1, -t, 0).normalize());
        vertices.add(new Vector3(0, -1, t).normalize());
        vertices.add(new Vector3(0, 1, t).normalize());
        vertices.add(new Vector3(0, -1, -t).normalize());
        vertices.add(new Vector3(0, 1, -t).normalize());
        vertices.add(new Vector3(t, 0, -1).normalize());
        vertices.add(new Vector3(t, 0, 1).normalize());
        vertices.add(new Vector3(-t, 0, -1).normalize());
        vertices.add(new Vector3(-t, 0, 1).normalize());
        for (final Vector3 v : vertices) {
            v.assign(v.multiply(3));
        }
        final ConvexHull ico = new ConvexHull("ico", vertices);

        scene.addGeometry(Matrix3.identity(), new Vector3(0, -11, -25), ico);

        // gravity absorbing body
        final Body gravity = new Body("gravity");
        scene.addBody(gravity);
        scene.fixBody(gravity, true);

        // put gravity on stuff
        scene.addConstraint(new GravityForce(boxgeometry.getBody(), gravity));
        scene.addConstraint(new GravityForce(boxgeometry2.getBody(), gravity));
        scene.addConstraint(new GravityForce(boxgeometry3.getBody(), gravity));
        scene.addConstraint(new GravityForce(capgeo.getBody(), gravity));
        scene.addConstraint(new GravityForce(capgeo2.getBody(), gravity));
        scene.addConstraint(new GravityForce(capgeo3.getBody(), gravity));
        scene.addConstraint(new GravityForce(capgeo4.getBody(), gravity));
        scene.addConstraint(new GravityForce(ico.getBody(), gravity));

        // handle drawing
        final Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);
        rendering.addCallback(new Interaction(scene));
        rendering.drawMe(boxgeometry);
        rendering.drawMe(boxgeometry2);
        rendering.drawMe(boxgeometry3);
        rendering.drawMe(capgeo);
        rendering.drawMe(capgeo2);
        rendering.drawMe(capgeo3);
        rendering.drawMe(capgeo4);
        rendering.drawMe(ico);

        rendering.createWindow();
        rendering.start();
    }

    @Override
    public void tick() {
        // each frame, to a time step on the Scene
        scene.tick();
    }

    public static void main(final String[] args) {
        new CapsuleExample();
    }

}
