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
import jinngine.geometry.Geometry;
import jinngine.geometry.Sphere;
import jinngine.geometry.UniformCapsule;
import jinngine.math.Matrix3;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.BreakageTrigger;
import jinngine.physics.DefaultDeactivationPolicy;
import jinngine.physics.DefaultScene;
import jinngine.physics.Scene;
import jinngine.physics.constraint.joint.UniversalJoint;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
import jinngine.rendering.Interaction;
import jinngine.rendering.Rendering;

public class RagdollExample implements Rendering.Callback {
    private final Scene scene;

    public RagdollExample() {

        // start jinngine
        scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(35),
                new DefaultDeactivationPolicy());
        scene.setTimestep(0.1);

        // add boxes to bound the world
        final Box floor = new Box("Floor", 1500, 20, 1500);
        scene.addGeometry(Matrix3.identity(), new Vector3(0, -30, 0), floor);
        scene.fixBody(floor.getBody(), true);

        final Box back = new Box("Back wall", 200, 200, 20);
        scene.addGeometry(Matrix3.identity(), new Vector3(0, 0, -55), back);
        scene.fixBody(back.getBody(), true);

        final Box front = new Box("Front wall", 200, 200, 20);
        scene.addGeometry(Matrix3.identity(), new Vector3(0, 0, -7), front);
        scene.fixBody(front.getBody(), true);

        final Box left = new Box("Left wall", 20, 200, 200);
        scene.addGeometry(Matrix3.identity(), new Vector3(-35, 0, 0), left);
        scene.fixBody(left.getBody(), true);

        final Box right = new Box("Right wall", 20, 200, 200);
        scene.addGeometry(Matrix3.identity(), new Vector3(10, 0, 0), right);
        scene.fixBody(right.getBody(), true);

        //        // fun box
        //        final Box fun = new Box("Fun", 4.3, 1.5, 4.8, 0.25);
        //        scene.addGeometry(Matrix3.identity(), new Vector3(-7, -19.0, -25), fun);
        //        scene.fixBody(fun.getBody(), true);
        //        // fun box
        //        final Box fun1 = new Box("Fun", 4.3, 2.5, 4.8, 0.25);
        //        scene.addGeometry(Matrix3.identity(), new Vector3(-12, -18.5, -25), fun1);
        //        scene.fixBody(fun1.getBody(), true);
        //        // fun box
        //        final Box fun2 = new Box("Fun", 4.3, 0.5, 4.8, 0.25);
        //        scene.addGeometry(Matrix3.identity(), new Vector3(-17, -19.5, -25), fun2);
        //        scene.fixBody(fun2.getBody(), true);
        // fun box
        final Box fun = new Box("Fun", 4.5, 1.5, 4.8, 0.25);
        scene.addGeometry(Matrix3.identity(), new Vector3(-7, -19.0, -25), fun);
        scene.fixBody(fun.getBody(), true);
        // fun box
        final Box fun1 = new Box("Fun", 4.5, 2.5, 4.8, 0.25);
        scene.addGeometry(Matrix3.identity(), new Vector3(-12, -18.5, -25), fun1);
        scene.fixBody(fun1.getBody(), true);
        // fun box
        final Box fun2 = new Box("Fun", 4.5, 0.5, 4.8, 0.25);
        scene.addGeometry(Matrix3.identity(), new Vector3(-17, -19.5, -25), fun2);
        scene.fixBody(fun2.getBody(), true);

        // handle drawing
        final Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);
        rendering.drawMe(fun);
        rendering.drawMe(fun1);
        rendering.drawMe(fun2);
        rendering.addCallback(new Interaction(scene));

        // make 3 dolls
        doDoll(scene, rendering, new Vector3(-2, 0, 0));
        doDoll(scene, rendering, new Vector3(-7, 2, 0));
        doDoll(scene, rendering, new Vector3(-12, 0, 0));

        rendering.createWindow();
        rendering.start();
    }

    public void doDoll(final Scene scene, final Rendering rendering, final Vector3 offset) {

        // head
        final Geometry head = new Sphere("head", 0.85);
        scene.addGeometry(Matrix3.identity(), new Vector3(-5, -9.5, -25).add(offset), head);

        // torso1
        final Geometry torso1 = new Box("torso1", 1.7, 0.75, 1.7, 0.20);
        scene.addGeometry(Matrix3.identity(), new Vector3(-5, -11, -25).add(offset), torso1);

        final UniversalJoint neck = new UniversalJoint(head.getBody(), torso1.getBody(),
                new Vector3(-5, -10, -25).add(offset), new Vector3(0, 0, 1), new Vector3(1, 0, 0));
        neck.getFirstAxisControler().setFrictionMagnitude(0.25);
        neck.getSecondAxisControler().setFrictionMagnitude(0.25);
        scene.addConstraint(neck);

        scene.addTrigger(new BreakageTrigger(neck, 2000));

        // torso2
        final Geometry torso2 = new Box("torso2", 1.5, 0.75, 1.5, 0.20);
        scene.addGeometry(Matrix3.identity(), new Vector3(-5, -12.5, -25).add(offset), torso2);

        final UniversalJoint spine = new UniversalJoint(torso1.getBody(), torso2.getBody(),
                new Vector3(-5, -12, -25).add(offset), new Vector3(0, 0, 1), new Vector3(1, 0, 0));
        spine.getFirstAxisControler().setLimits(-0.2, 0.2);
        spine.getSecondAxisControler().setLimits(-0.2, 0.2);
        spine.getFirstAxisControler().setFrictionMagnitude(0.25);
        spine.getSecondAxisControler().setFrictionMagnitude(0.25);

        scene.addConstraint(spine);

        scene.addTrigger(new BreakageTrigger(spine, 2000));

        // torso3
        final Geometry torso3 = new Box("torso3", 1.2, 0.75, 1.2, 0.20);
        scene.addGeometry(Matrix3.identity(), new Vector3(-5, -14, -25).add(offset), torso3);

        final UniversalJoint spine2 = new UniversalJoint(torso2.getBody(), torso3.getBody(),
                new Vector3(-5, -13, -25).add(offset), new Vector3(0, 0, 1), new Vector3(1, 0, 0));
        spine2.getFirstAxisControler().setLimits(-0.2, 0.2);
        spine2.getSecondAxisControler().setLimits(-0.2, 0.2);
        spine2.getFirstAxisControler().setFrictionMagnitude(0.25);
        spine2.getSecondAxisControler().setFrictionMagnitude(0.25);

        scene.addConstraint(spine2);
        scene.addTrigger(new BreakageTrigger(spine2, 2000));

        // upper left arm
        final Quaternion rotation = Quaternion.rotation(Math.PI * 0.5, Vector3.i());
        final Geometry upleftarm = new UniformCapsule("upleftarm", 0.5, 1.0);
        scene.addGeometry(rotation.toRotationMatrix3(new Matrix3()), new Vector3(-3.5, -12, -25).add(offset), upleftarm);

        final UniversalJoint leftshoulder = new UniversalJoint(torso1.getBody(), upleftarm.getBody(), new Vector3(-3.5,
                -11, -25).add(offset), new Vector3(0, 0, 1), new Vector3(0, 1, 0));
        leftshoulder.getFirstAxisControler().setLimits(-0.5, 0.5);
        leftshoulder.getSecondAxisControler().setLimits(-0.5, 0.5);
        leftshoulder.getFirstAxisControler().setFrictionMagnitude(0.25);
        leftshoulder.getSecondAxisControler().setFrictionMagnitude(0.25);
        scene.addConstraint(leftshoulder);

        scene.addTrigger(new BreakageTrigger(leftshoulder, 2000));

        // upper right arm
        final Geometry uprightarm = new UniformCapsule("uprightarm", 0.5, 1);
        scene.addGeometry(rotation.toRotationMatrix3(new Matrix3()), new Vector3(-6.5, -12, -25).add(offset),
                uprightarm);
        final UniversalJoint rightshoulder = new UniversalJoint(torso1.getBody(), uprightarm.getBody(), new Vector3(
                -6.5, -11, -25).add(offset), new Vector3(0, 0, 1), new Vector3(0, 1, 0));
        rightshoulder.getFirstAxisControler().setLimits(-1.5, 1.5);
        rightshoulder.getSecondAxisControler().setLimits(-1.5, 1.5);
        rightshoulder.getFirstAxisControler().setFrictionMagnitude(0.25);
        rightshoulder.getSecondAxisControler().setFrictionMagnitude(0.25);
        scene.addConstraint(rightshoulder);

        // lower left arm
        final Geometry lowerleftarm = new UniformCapsule("lowerleftarm", 0.4, 1.0);
        scene.addGeometry(rotation.toRotationMatrix3(new Matrix3()), new Vector3(-3.5, -14, -25).add(offset),
                lowerleftarm);

        final UniversalJoint leftelbow = new UniversalJoint(upleftarm.getBody(), lowerleftarm.getBody(), new Vector3(
                -3.5, -13.25, -25).add(offset), new Vector3(1, 0, 0), new Vector3(0, 1, 0));
        leftelbow.getFirstAxisControler().setLimits(-0.5, 0.5);
        leftelbow.getSecondAxisControler().setLimits(-1.5, 1.5);
        leftelbow.getFirstAxisControler().setFrictionMagnitude(0.25);
        leftelbow.getSecondAxisControler().setFrictionMagnitude(0.25);
        scene.addConstraint(leftelbow);

        // lower right arm
        final Geometry lowerrightarm = new UniformCapsule("lowerrightarm", 0.4, 1.0);
        scene.addGeometry(rotation.toRotationMatrix3(new Matrix3()), new Vector3(-6.5, -14, -25).add(offset),
                lowerrightarm);

        final UniversalJoint rightelbow = new UniversalJoint(uprightarm.getBody(), lowerrightarm.getBody(),
                new Vector3(-6.5, -13.25, -25).add(offset), new Vector3(1, 0, 0), new Vector3(0, 1, 0));
        rightelbow.getFirstAxisControler().setLimits(-0.5, 0.5);
        rightelbow.getSecondAxisControler().setLimits(-1.5, 1.5);
        rightelbow.getFirstAxisControler().setFrictionMagnitude(0.25);
        rightelbow.getSecondAxisControler().setFrictionMagnitude(0.25);
        scene.addConstraint(rightelbow);

        // left thigh
        final Geometry leftthigh = new UniformCapsule("leftthigh", 0.5, 1.3);
        scene.addGeometry(rotation.toRotationMatrix3(new Matrix3()), new Vector3(-5.5, -15.5, -25).add(offset),
                leftthigh);

        final UniversalJoint lefthip = new UniversalJoint(torso3.getBody(), leftthigh.getBody(), new Vector3(-5.5, -14,
                -25).add(offset), new Vector3(1, 0, 0), new Vector3(0, 0, 1));
        lefthip.getFirstAxisControler().setLimits(-0.5, 0.5);
        lefthip.getSecondAxisControler().setLimits(-0.5, 0.5);
        lefthip.getFirstAxisControler().setFrictionMagnitude(0.25);
        lefthip.getSecondAxisControler().setFrictionMagnitude(0.25);
        scene.addConstraint(lefthip);

        // left taiba
        final Geometry lefttaiba = new UniformCapsule("lefttaiba", 0.4, 1.3);
        scene.addGeometry(rotation.toRotationMatrix3(new Matrix3()), new Vector3(-5.5, -17.5, -25).add(offset),
                lefttaiba);

        final UniversalJoint lefttknee = new UniversalJoint(leftthigh.getBody(), lefttaiba.getBody(), new Vector3(-5.5,
                -16, -25).add(offset), new Vector3(1, 0, 0), new Vector3(0, 0, 1));
        lefttknee.getFirstAxisControler().setLimits(-0.5, 0.5);
        lefttknee.getSecondAxisControler().setLimits(-0.1, 0.1);
        lefttknee.getFirstAxisControler().setFrictionMagnitude(0.25);
        lefttknee.getSecondAxisControler().setFrictionMagnitude(0.25);
        scene.addConstraint(lefttknee);

        // right thigh
        final Geometry rightthigh = new UniformCapsule("rightthigh", 0.5, 1.3);
        scene.addGeometry(rotation.toRotationMatrix3(new Matrix3()), new Vector3(-4.0, -15.5, -25).add(offset),
                rightthigh);

        final UniversalJoint righthip = new UniversalJoint(torso3.getBody(), rightthigh.getBody(), new Vector3(-4.0,
                -14, -25).add(offset), new Vector3(1, 0, 0), new Vector3(0, 0, 1));
        righthip.getFirstAxisControler().setLimits(-0.5, 0.5);
        righthip.getSecondAxisControler().setLimits(-0.5, 0.5);
        righthip.getFirstAxisControler().setFrictionMagnitude(0.25);
        righthip.getSecondAxisControler().setFrictionMagnitude(0.25);
        scene.addConstraint(righthip);

        // right taiba
        final Geometry righttaiba = new UniformCapsule("righttaiba", 0.4, 1.3);
        scene.addGeometry(rotation.toRotationMatrix3(new Matrix3()), new Vector3(-4.0, -17.5, -25).add(offset),
                righttaiba);

        final UniversalJoint righttknee = new UniversalJoint(rightthigh.getBody(), righttaiba.getBody(), new Vector3(
                -4.0, -16, -25).add(offset), new Vector3(1, 0, 0), new Vector3(0, 0, 1));
        righttknee.getFirstAxisControler().setLimits(-0.5, 0.5);
        righttknee.getSecondAxisControler().setLimits(-0.1, 0.1);
        righttknee.getFirstAxisControler().setFrictionMagnitude(0.25);
        righttknee.getSecondAxisControler().setFrictionMagnitude(0.25);
        scene.addConstraint(righttknee);

        final Body ground = new Body("ground");
        scene.addBody(ground);
        scene.fixBody(ground, true);

        // put gravity on limbs
        scene.addConstraint(new GravityForce(head.getBody(), ground));
        scene.addConstraint(new GravityForce(torso1.getBody(), ground));
        scene.addConstraint(new GravityForce(torso2.getBody(), ground));
        scene.addConstraint(new GravityForce(torso3.getBody(), ground));
        scene.addConstraint(new GravityForce(upleftarm.getBody(), ground));
        scene.addConstraint(new GravityForce(lowerleftarm.getBody(), ground));
        scene.addConstraint(new GravityForce(uprightarm.getBody(), ground));
        scene.addConstraint(new GravityForce(lowerrightarm.getBody(), ground));
        scene.addConstraint(new GravityForce(leftthigh.getBody(), ground));
        scene.addConstraint(new GravityForce(lefttaiba.getBody(), ground));
        scene.addConstraint(new GravityForce(rightthigh.getBody(), ground));
        scene.addConstraint(new GravityForce(righttaiba.getBody(), ground));

        rendering.drawMe(head);
        rendering.drawMe(torso1);
        rendering.drawMe(torso2);
        rendering.drawMe(torso3);
        rendering.drawMe(upleftarm);
        rendering.drawMe(lowerleftarm);
        rendering.drawMe(uprightarm);
        rendering.drawMe(lowerrightarm);
        rendering.drawMe(leftthigh);
        rendering.drawMe(lefttaiba);
        rendering.drawMe(rightthigh);
        rendering.drawMe(righttaiba);
    }

    @Override
    public void tick() {
        // each frame, to a time step on the Scene
        scene.tick();
    }

    public static void main(final String[] args) {
        new RagdollExample();
    }

}
