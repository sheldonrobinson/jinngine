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
import java.util.Iterator;
import java.util.List;

import jinngine.collision.SAP2;
import jinngine.geometry.Box;
import jinngine.math.Matrix3;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.DefaultDeactivationPolicy;
import jinngine.physics.DefaultScene;
import jinngine.physics.Scene;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.force.GeneralizedForce;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
import jinngine.physics.solver.Solver.NCPConstraint;
import jinngine.rendering.Interaction;
import jinngine.rendering.Rendering;
import jinngine.rendering.Rendering.EventCallback;

public class ControlExample implements Rendering.Callback {
    private final Scene scene;

    public ControlExample() {
        // setup jinngine
        scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(5),
                new DefaultDeactivationPolicy());

        scene.setTimestep(0.1);

        // add a fixed floor to the scene
        final Box floor = new Box("floor", 1500, 20, 1500);
        scene.addGeometry(Matrix3.identity(), new Vector3(0, -30, 0), floor);
        scene.fixBody(floor.getBody(), true);

        // setup the rendering
        final Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);

        // setup figure using sphere swept box shape
        final Body figure = new Body("Figure");
        scene.addBody(figure);
        //        final UniformCapsule head = new UniformCapsule("head", 1.5, 2);
        final Box head = new Box("head", 2, 4, 2, 1);

        // final Box head = new Box("head", 1, 1, 1);
        scene.addGeometry(figure, Matrix3.identity(), new Vector3(-20, -10, -25), head);
        // scene.addGeometry(figure, Quaternion.rotation(Math.PI / 2, Vector3.i()).toRotationMatrix3(), new Vector3(-20,
        // -10, -25), head);
        rendering.drawMe(head);
        // scene.addForce(new GravityForce(figure));

        final Body ground = new Body("ground");
        scene.addBody(ground);
        scene.fixBody(ground, true);

        scene.addConstraint(new GravityForce(figure, ground));

        // remove restitution from feet
        head.setFrictionCoefficient(0.0);
        head.setRestitution(0.0);
        head.setEnvelope(0); // force surroundings to determine the envelope size

        // mask out angular movement
        figure.state.inverseinertia.assignZero();

        // create a fixed body to move the figure around with. This body will just
        // stay still all the time, so its just there to connect the constraint with it.
        final Body majonet = new Body("Majonet");
        scene.addBody(majonet);
        scene.fixBody(majonet, true);

        // create a list of constraints, needed for the
        // control constraint. They are only here because we
        // can't define a constructor in the control Constraint below
        final NCPConstraint linear1 = new NCPConstraint();
        final NCPConstraint linear2 = new NCPConstraint();
        final NCPConstraint linear3 = new NCPConstraint();

        // list needed for the constraint below
        final List<NCPConstraint> ncps = new ArrayList<NCPConstraint>();
        ncps.add(linear1);
        ncps.add(linear2);
        ncps.add(linear3);

        // the target figure velocity
        final Vector3 targetVelocity = new Vector3();
        final Vector3 jumpDuration = new Vector3();

        // add a drag force in the XZ plane
        final Body pseude = new Body("Majonet");
        scene.addBody(pseude);
        scene.fixBody(pseude, true);

        scene.addConstraint(new GeneralizedForce(figure, pseude) {
            final jinngine.math.Vector3 direction = new jinngine.math.Vector3();
            double magnitude = 0;

            /*
             * I should change GeneralizedForce to just work directly with the
             * force vectors, insted of a direction and a magnitude. Its a real
             * pain having to keeping the direction normalized and figuring out
             * what the magnitude should be afterward etc..
             */
            @Override
            public void update(final double dt) {
                direction.assign(figure.state.velocity);
                direction.assignNegate();
                direction.y = 0;
                if (direction.norm() < 1e-7) {
                    direction.assign(1, 0, 0);
                    magnitude = 0;
                } else {
                    direction.assignNormalize();
                    magnitude = Math.max(-figure.state.velocity.dot(direction) * 20, 0);
                }

                setForce(magnitude, direction, 0, new jinngine.math.Vector3(1, 0, 0));

                super.update(dt);
            }
        });

        // create constraint controller for figure
        final Constraint control = new Constraint() {

            private boolean monitored = false;

            @Override
            public void update(final double dt) {
                final Vector3 u = figure.state.velocity.sub(targetVelocity);

                // if the target jump velocity is reached, set it back to zero
                //                if (Math.abs(u.y) < 1e-7) {
                //                    System.out.println("" + u.y);
                //
                //                    targetVelocity.y = 0;
                //                }

                if (jumpDuration.x <= 0) {
                    targetVelocity.y = 0;
                } else {
                    jumpDuration.x = jumpDuration.x - 1;
                }

                // the next two constraints takes care of the movement in the XZ plane. They
                // use the targetVelocity, but allow half the amount for force. Having the
                // force limit set too high results in the figure "bulldozing" through the
                // world, possible creating blow up effects. However, the constraint ensures
                // that we use exactly the force needed to get to the desired velocity.
                linear1.assign(new Vector3(1, 0, 0), new Vector3(), new Vector3(-1, 0, 0), new Vector3(),
                        Math.min(targetVelocity.x * figure.getMass() * 10, 0) * dt,
                        Math.max(targetVelocity.x * figure.getMass() * 10, 0) * dt, null, u.x);

                linear2.assign(new Vector3(0, 0, 1), new Vector3(), new Vector3(0, 0, -1), new Vector3(),
                        Math.min(targetVelocity.z * figure.getMass() * 10, 0) * dt,
                        Math.max(targetVelocity.z * figure.getMass() * 10, 0) * dt, null, u.z);

                // this constraint asks for a body velocity going upwards. The maximum
                // force is the same as the velocity. The lower limit of the force is 0,
                // so it can never "pull" the figure down
                linear3.assign(new Vector3(0, 1, 0), new Vector3(), new Vector3(0, -1, 0), new Vector3(), 0,
                        Math.max(targetVelocity.y * figure.getMass() * 87627 /*
                                                                              * something
                                                                              * big
                                                                              * so
                                                                              * we
                                                                              * are
                                                                              * sure
                                                                              * there
                                                                              * is
                                                                              * impulse
                                                                              * enough
                                                                              */, 0) * dt, null, u.y);

            }

            @Override
            public Iterator<NCPConstraint> iterator() {
                return ncps.iterator();
            }

            @Override
            public Body getBody1() {
                return figure;
            }

            @Override
            public Body getBody2() {
                return majonet;
            }

            @Override
            public boolean isExternal() {
                return false;
            }

            @Override
            public boolean isMonitored() {
                return monitored;
            }

            @Override
            public void setMonitored(final boolean monitored) {
                this.monitored = monitored;
            }
        };

        // add the control constraint to the scene
        scene.addConstraint(control);
        scene.monitorConstraint(control);

        // create a callback to receive input from view. We communicate
        // with the physics through the targetVelocity vector. The control
        // constraints looks at this vector, ask the solver to give us the
        // desired motion. The controls are WASD, plus space for jumping
        rendering.addCallback(new EventCallback() {
            @Override
            public void spaceReleased() {}

            @Override
            public void spacePressed() {}

            @Override
            public void mousePressed(final double x, final double y, final Vector3 point, final Vector3 direction) {}

            @Override
            public void mouseDragged(final double x, final double y, final Vector3 point, final Vector3 direction) {}

            @Override
            public void mouseReleased() {}

            @Override
            public void enterPressed() {}

            @Override
            public void keyPressed(final char key) {
                // System.out.println("got key="+key);
                switch (key) {
                    case 'w':
                        targetVelocity.z = -3;
                        break;
                    case 's':
                        targetVelocity.z = 3;
                        break;
                    case 'a':
                        targetVelocity.x = -3;
                        break;
                    case 'd':
                        targetVelocity.x = 3;
                        break;
                    case ' ':
                        jumpDuration.x = 5;
                        targetVelocity.y = 4;
                        break;
                }
            }

            @Override
            public void keyReleased(final char key) {
                switch (key) {
                    case 'w':
                        targetVelocity.z = 0;
                        break;
                    case 's':
                        targetVelocity.z = 0;
                        break;
                    case 'a':
                        targetVelocity.x = 0;
                        break;
                    case 'd':
                        targetVelocity.x = 0;
                        break;
                }
            }
        });

        // create the static world grid
        final Body grid = new Body("Grid");
        scene.addBody(grid);

        // make a grid of static boxes
        for (int i = 0; i < 17; i++) {
            for (int j = 0; j < 17; j++) {
                final double sigma = 0.3;
                final double height = 14 * Math.exp(-((i - 6.0) * (i - 6.0) / 2 * sigma * sigma + (j - 6.0) * (j - 6.0)
                        / 2 * sigma * sigma));
                final Box box = new Box("box", 2.8, 3, 2.8);
                box.setEnvelope(0.25);
                scene.addGeometry(grid, Matrix3.identity(), new Vector3(3 * i - 15, -18.1 + height, 3 * j - 35), box);
                rendering.drawMe(box);
            }
        }

        // rotate the grid world a bit
        grid.state.orientation.assign(Quaternion.rotation(Math.PI * .25, Vector3.j()));

        // fix the grid
        scene.fixBody(grid, true);

        // create a box for androidish figure to play with
        final Box box = new Box("box", 5, 4, 5);
        scene.addGeometry(Matrix3.identity(), new Vector3(-0, -12, -26), box);
        scene.addConstraint(new GravityForce(box.getBody(), ground));
        rendering.drawMe(box);

        // setup rendering
        rendering.addCallback(new Interaction(scene));
        rendering.createWindow();

        // go go go :)
        rendering.start();
    }

    @Override
    public void tick() {
        // each frame, to a time step on the Scene
        scene.tick();
    }

    public static void main(final String[] args) {
        new ControlExample();
    }
}