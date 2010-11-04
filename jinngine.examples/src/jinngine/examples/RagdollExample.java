/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.examples;

import jinngine.collision.SAP2;
import jinngine.geometry.Box;
import jinngine.geometry.Geometry;
import jinngine.geometry.UniformCapsule;
import jinngine.math.Matrix3;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.*;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.constraint.joint.UniversalJoint;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
import jinngine.rendering.Interaction;
import jinngine.rendering.Rendering;

public class RagdollExample implements Rendering.Callback {	
	private final Scene scene;
	
	public RagdollExample() {
		
		// start jinngine 
		scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(75), new DefaultDeactivationPolicy());
		scene.setTimestep(0.1);
		
		// add boxes to bound the world
		Box floor = new Box("Floor",1500,20,1500);
		scene.addGeometry(Matrix3.identity(), new Vector3(0,-30,0), floor);
		scene.fixBody(floor.getBody(), true);
		
		Box back = new Box("Back wall", 200, 200, 20);		
		scene.addGeometry(Matrix3.identity(), new Vector3(0,0,-55), back);
		scene.fixBody(back.getBody(), true);

		Box front = new Box("Front wall", 200, 200, 20);		
		scene.addGeometry( Matrix3.identity(), new Vector3(0,0,-7), front);
		scene.fixBody(front.getBody(), true);

		Box left = new Box("Left wall", 20, 200, 200);		
		scene.addGeometry(Matrix3.identity(), new Vector3(-35,0,0), left);
		scene.fixBody(left.getBody(), true);

		Box right = new Box("Right wall", 20, 200, 200);		
		scene.addGeometry(Matrix3.identity(), new Vector3(10,0,0), right);
		scene.fixBody( right.getBody(), true);	
		
		// head
		Geometry head = new Box("head",1,1,1);
		scene.addGeometry(Matrix3.identity(), new Vector3(-5,-9.5,-25),  head );

		// torso1
		Geometry torso1 = new Box("torso1",1.7,1,1.7);
		scene.addGeometry(Matrix3.identity(), new Vector3(-5,-11,-25), torso1 );

		Constraint neck = new UniversalJoint(head.getBody(),torso1.getBody(),new Vector3(-5,-10,-25), new Vector3(0,0,1), new Vector3(1,0,0));
		scene.addConstraint(neck);

		// torso2
		Geometry torso2 = new Box("torso2",1.5,1,1.5);
		scene.addGeometry(Matrix3.identity(), new Vector3(-5,-12.5,-25), torso2);

		UniversalJoint spine = new UniversalJoint(torso1.getBody(),torso2.getBody(),new Vector3(-5,-12,-25), new Vector3(0,0,1), new Vector3(1,0,0));
		spine.getFirstAxisControler().setLimits(-0.2, 0.2);
		spine.getSecondAxisControler().setLimits(-0.2, 0.2);
		scene.addConstraint(spine);

		// torso3
		Geometry torso3 = new Box("torso3",1.2,1,1.2);
		scene.addGeometry(Matrix3.identity(), new Vector3(-5,-14,-25), torso3 );

		UniversalJoint spine2 = new UniversalJoint(torso2.getBody(),torso3.getBody(),new Vector3(-5,-13,-25), new Vector3(0,0,1), new Vector3(1,0,0));
		spine2.getFirstAxisControler().setLimits(-0.2, 0.2);
		spine2.getSecondAxisControler().setLimits(-0.2, 0.2);
		scene.addConstraint(spine2);

		// upper left arm
		Geometry upleftarm = new UniformCapsule("upleftarm",0.5,1.0);
		scene.addGeometry(Quaternion.rotation(Math.PI*0.5, Vector3.i()).toRotationMatrix3(), new Vector3(-3.5,-12,-25), upleftarm);
		
		UniversalJoint leftshoulder = new UniversalJoint(torso1.getBody(),upleftarm.getBody(), new Vector3(-3.5,-11,-25), new Vector3(0,0,1), new Vector3(0,1,0));
		leftshoulder.getFirstAxisControler().setLimits(-0.5, 0.5);
		leftshoulder.getSecondAxisControler().setLimits(-0.5, 0.5);
		leftshoulder.getFirstAxisControler().setFrictionMagnitude(0.01);
		leftshoulder.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(leftshoulder);

		// upper right arm
		Geometry uprightarm = new Box("uprightarm",0.5,2,0.5);
		scene.addGeometry(Matrix3.identity(), new Vector3(-6.5,-12,-25), uprightarm);
		UniversalJoint rightshoulder = new UniversalJoint(torso1.getBody(),uprightarm.getBody(), new Vector3(-6.5,-11,-25), new Vector3(0,0,1), new Vector3(0,1,0));
		rightshoulder.getFirstAxisControler().setLimits(-1.5, 1.5);
		rightshoulder.getSecondAxisControler().setLimits(-1.5, 1.5);
		rightshoulder.getFirstAxisControler().setFrictionMagnitude(0.01);
		rightshoulder.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(rightshoulder);
		
		// lower left arm
		Geometry lowerleftarm = new Box("lowerleftarm",0.5,1.5,0.5);
		scene.addGeometry(Matrix3.identity(), new Vector3(-3.5,-14,-25), lowerleftarm);
		
		UniversalJoint leftelbow = new UniversalJoint(upleftarm.getBody(), lowerleftarm.getBody(), new Vector3(-3.5,-13.25,-25), new Vector3(1,0,0), new Vector3(0,1,0));
		leftelbow.getFirstAxisControler().setLimits(-0.0, 0.0);
		leftelbow.getSecondAxisControler().setLimits(-1.5, 1.5);
		leftelbow.getFirstAxisControler().setFrictionMagnitude(0.01);
		leftelbow.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(leftelbow);

		// lower right arm
		Geometry lowerrightarm = new Box("lowerrightarm",0.5,1.5,0.5);
		scene.addGeometry(Matrix3.identity(), new Vector3(-6.5,-14,-25), lowerrightarm);
		
		UniversalJoint rightelbow = new UniversalJoint(uprightarm.getBody(), lowerrightarm.getBody(), new Vector3(-6.5,-13.25,-25), new Vector3(1,0,0), new Vector3(0,1,0));
		rightelbow.getFirstAxisControler().setLimits(-0.0, 0.0);
		rightelbow.getSecondAxisControler().setLimits(-1.5, 1.5);
		rightelbow.getFirstAxisControler().setFrictionMagnitude(0.01);
		rightelbow.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(rightelbow);

		// left thigh
		Geometry leftthigh = new Box("leftthigh",0.5,1.5,0.5);
		scene.addGeometry(Matrix3.identity(), new Vector3(-5.5,-15.5,-25), leftthigh);

		UniversalJoint lefthip = new UniversalJoint(torso3.getBody(), leftthigh.getBody(), new Vector3(-5.5,-14,-25), new Vector3(1,0,0), new Vector3(0,0,1));
		lefthip.getFirstAxisControler().setLimits(-0.5, 0.5);
		lefthip.getSecondAxisControler().setLimits(-0.5, 0.5);
		lefthip.getFirstAxisControler().setFrictionMagnitude(0.01);
		lefthip.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(lefthip);

		// left taiba
		Geometry lefttaiba = new Box("lefttaiba",0.5,1.5,0.5);
		scene.addGeometry(Matrix3.identity(), new Vector3(-5.5,-17.5,-25), lefttaiba);

		UniversalJoint lefttknee = new UniversalJoint(leftthigh.getBody(), lefttaiba.getBody(), new Vector3(-5.5,-16,-25), new Vector3(1,0,0), new Vector3(0,0,1));
		lefttknee.getFirstAxisControler().setLimits(-0.5, 0.5);
		lefttknee.getSecondAxisControler().setLimits(0, 0);
		lefttknee.getFirstAxisControler().setFrictionMagnitude(0.01);
		lefttknee.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(lefttknee);

		
		// right thigh
		Geometry rightthigh = new Box("rightthigh",0.5,1.5,0.5);
		scene.addGeometry(Matrix3.identity(), new Vector3(-4.0,-15.5,-25), rightthigh);

		UniversalJoint righthip = new UniversalJoint(torso3.getBody(), rightthigh.getBody(), new Vector3(-4.0,-14,-25), new Vector3(1,0,0), new Vector3(0,0,1));
		righthip.getFirstAxisControler().setLimits(-0.5, 0.5);
		righthip.getSecondAxisControler().setLimits(-0.5, 0.5);
		righthip.getFirstAxisControler().setFrictionMagnitude(0.01);
		righthip.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(righthip);

		// right taiba
		Geometry righttaiba = new Box("righttaiba",0.5,1.5,0.5);
		scene.addGeometry(Matrix3.identity(), new Vector3(-4.0,-17.5,-25), righttaiba);

		UniversalJoint righttknee = new UniversalJoint(rightthigh.getBody(), righttaiba.getBody(), new Vector3(-4.0,-16,-25), new Vector3(1,0,0), new Vector3(0,0,1));
		righttknee.getFirstAxisControler().setLimits(-0.5, 0.5);
		righttknee.getSecondAxisControler().setLimits(0, 0);
		righttknee.getFirstAxisControler().setFrictionMagnitude(0.01);
		righttknee.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(righttknee);
				
        // put gravity on limbs
		scene.addForce( new GravityForce(head.getBody()));		
		scene.addForce( new GravityForce(torso1.getBody()));		
		scene.addForce( new GravityForce(torso2.getBody()));		
		scene.addForce( new GravityForce(torso3.getBody()));		
		scene.addForce( new GravityForce(upleftarm.getBody()));		
		scene.addForce( new GravityForce(lowerleftarm.getBody()));		
		scene.addForce( new GravityForce(uprightarm.getBody()));		
		scene.addForce( new GravityForce(lowerrightarm.getBody()));		
		scene.addForce( new GravityForce(leftthigh.getBody()));		
		scene.addForce( new GravityForce(lefttaiba.getBody()));		
		scene.addForce( new GravityForce(rightthigh.getBody()));		
		scene.addForce( new GravityForce(righttaiba.getBody()));		

		
		// handle drawing
		Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);
		rendering.addCallback(new Interaction(scene));
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

		rendering.createWindow();
		rendering.start();
	}

	@Override
	public void tick() {
		// each frame, to a time step on the Scene
		scene.tick();
	}
	
	public static void main( String[] args) {
		new RagdollExample();
	}

}
