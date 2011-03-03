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

public class PlatonicSolidsExample implements Rendering.Callback {
    private final Scene scene;

    public PlatonicSolidsExample() {
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

        // create a box
        final Box boxgeometry = new Box("box", 3, 3, 3, 0.25);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(-4, -11, -25), boxgeometry);

        // create a box
        final Box boxgeometry2 = new Box("box2", 4, 4, 4, 0.25);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(-3, -11, -25), boxgeometry2);

        // create a box
        final Box boxgeometry3 = new Box("box3", 5, 5, 5, 0.25);
        this.scene.addGeometry(Matrix3.identity(), new Vector3(-3, -11, -25), boxgeometry3);

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
        final ConvexHull ico = new ConvexHull("ico", vertices, 0.25);

        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, -11, -25), ico);

        // create dodecahedron
        final List<Vector3> dodecahedron = new ArrayList<Vector3>();
        dodecahedron.add(new Vector3(1, 1, 1).normalize());
        dodecahedron.add(new Vector3(-1, 1, 1).normalize());
        dodecahedron.add(new Vector3(1, -1, 1).normalize());
        dodecahedron.add(new Vector3(-1, -1, 1).normalize());
        dodecahedron.add(new Vector3(1, 1, -1).normalize());
        dodecahedron.add(new Vector3(-1, 1, -1).normalize());
        dodecahedron.add(new Vector3(1, -1, -1).normalize());
        dodecahedron.add(new Vector3(-1, -1, -1).normalize());

        dodecahedron.add(new Vector3(0, -1 / t, t).normalize());
        dodecahedron.add(new Vector3(0, 1 / t, -t).normalize());
        dodecahedron.add(new Vector3(0, -1 / t, -t).normalize());
        dodecahedron.add(new Vector3(0, 1 / t, t).normalize());

        dodecahedron.add(new Vector3(-1 / t, t, 0).normalize());
        dodecahedron.add(new Vector3(1 / t, -t, 0).normalize());
        dodecahedron.add(new Vector3(-1 / t, -t, 0).normalize());
        dodecahedron.add(new Vector3(1 / t, t, 0).normalize());

        dodecahedron.add(new Vector3(t, 0, 1 / t).normalize());
        dodecahedron.add(new Vector3(-t, 0, 1 / t).normalize());
        dodecahedron.add(new Vector3(t, 0, -1 / t).normalize());
        dodecahedron.add(new Vector3(-t, 0, -1 / t).normalize());

        for (final Vector3 v : dodecahedron) {
            v.assign(v.multiply(3));
        }
        final ConvexHull dodeca = new ConvexHull("dodeca", dodecahedron, 0.25);

        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, -11, -25), dodeca);

        // create octa
        final List<Vector3> octahedron = new ArrayList<Vector3>();
        octahedron.add(new Vector3(-1, 0, 0).normalize());
        octahedron.add(new Vector3(1, 0, 0).normalize());
        octahedron.add(new Vector3(0, 1, 0).normalize());
        octahedron.add(new Vector3(0, -1, 0).normalize());
        octahedron.add(new Vector3(0, 0, 1).normalize());
        octahedron.add(new Vector3(0, 0, -1).normalize());
        for (final Vector3 v : octahedron) {
            v.assign(v.multiply(3));
        }
        final ConvexHull octa = new ConvexHull("ico", octahedron, 0.25);

        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, -11, -25), octa);

        // create octa
        final List<Vector3> tetrahedron = new ArrayList<Vector3>();
        tetrahedron.add(new Vector3(1, 1, 1).normalize());
        tetrahedron.add(new Vector3(-1, -1, 1).normalize());
        tetrahedron.add(new Vector3(-1, 1, -1).normalize());
        tetrahedron.add(new Vector3(1, -1, -1).normalize());
        for (final Vector3 v : tetrahedron) {
            v.assign(v.multiply(3));
        }
        final ConvexHull tetra = new ConvexHull("ico", tetrahedron, 0.25);

        this.scene.addGeometry(Matrix3.identity(), new Vector3(0, -11, -25), tetra);

        // gravity absorbing body
        final Body gravity = new Body("gravity");
        this.scene.addBody(gravity);
        this.scene.fixBody(gravity, true);

        // put gravity on stuff
        this.scene.addConstraint(new GravityForce(boxgeometry.getBody(), gravity));
        this.scene.addConstraint(new GravityForce(boxgeometry2.getBody(), gravity));
        this.scene.addConstraint(new GravityForce(boxgeometry3.getBody(), gravity));
        this.scene.addConstraint(new GravityForce(ico.getBody(), gravity));
        this.scene.addConstraint(new GravityForce(dodeca.getBody(), gravity));
        this.scene.addConstraint(new GravityForce(octa.getBody(), gravity));
        this.scene.addConstraint(new GravityForce(tetra.getBody(), gravity));

        // handle drawing
        final Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);
        rendering.addCallback(new Interaction(this.scene));
        rendering.drawMe(boxgeometry);
        rendering.drawMe(boxgeometry2);
        rendering.drawMe(boxgeometry3);
        rendering.drawMe(ico);
        rendering.drawMe(dodeca);
        rendering.drawMe(tetra);
        rendering.drawMe(octa);

        rendering.createWindow();
        rendering.start();
    }

    @Override
    public void tick() {
        // each frame, to a time step on the Scene
        this.scene.tick();
    }

    public static void main(final String[] args) {
        new PlatonicSolidsExample();
    }

}
