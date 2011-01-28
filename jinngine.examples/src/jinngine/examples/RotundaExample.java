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
import jinngine.math.Matrix3;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.DefaultDeactivationPolicy;
import jinngine.physics.DefaultScene;
import jinngine.physics.Scene;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
import jinngine.rendering.Interaction;
import jinngine.rendering.Rendering;

public class RotundaExample implements Rendering.Callback {
    private final Scene scene;

    public RotundaExample() {
        // start jinngine
        // scene = new DefaultScene(new SAP2(), new ProjectedGaussSeidel(57,Double.POSITIVE_INFINITY), new
        // DisabledDeactivationPolicy());
        scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(15),
                new DefaultDeactivationPolicy());
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

        // handle drawing
        final Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);
        rendering.addCallback(new Interaction(scene));

        final Body ground = new Body("ground");
        scene.addBody(ground);
        scene.fixBody(ground, true);

        // add some boxes (rotunda)
        for (int j = 0; j < 4; j++) {
            double theta = j % 2 * Math.PI * 2 / 12 / 2;
            for (int i = 0; i < 12; i++) {
                final double x = Math.sin(theta) * 9;
                final double y = Math.cos(theta) * 9;

                final Box box = new Box("box", 4, 2, 2);
                box.setCorrectionVelocityLimit(2);
                scene.addGeometry(Quaternion.rotation(theta, Vector3.j()).toRotationMatrix3(new Matrix3()),
                        new Vector3(-12 + x, -19 + 0.09375 + j * 2.09375, -27 + y), box);
                scene.addConstraint(new GravityForce(box.getBody(), ground));

                rendering.drawMe(box);

                theta = theta + 2 * Math.PI / 12;
            }

        }

        rendering.createWindow();
        rendering.start();
    }

    @Override
    public void tick() {
        // each frame, to a time step on the Scene
        scene.tick();
    }

    public static void main(final String[] args) {
        new RotundaExample();
    }

}
