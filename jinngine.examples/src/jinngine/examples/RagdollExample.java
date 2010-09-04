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
		Body floor = new Body("floor", new Box(1500,20,1500));
		floor.setPosition(new Vector3(0,-30,0));
		floor.setFixed(true);
		
		Body back = new Body( "back", new Box(200,200,20));		
		back.setPosition(new Vector3(0,0,-55));
		back.setFixed(true);

		Body front = new Body( "front", new Box(200,200,20));		
		front.setPosition(new Vector3(0,0,-7));
		front.setFixed(true);

		Body left = new Body( "left", new Box(20,200,200));		
		left.setPosition(new Vector3(-35,0,0));
		left.setFixed(true);

		Body right = new Body( "right", new Box(20,200,200));		
		right.setPosition(new Vector3(10,0,0));
		right.setFixed(true);
		
		// head
		Geometry headgeometry = new Box(1,1,1);
		Body head = new Body( "head", headgeometry );
		head.setPosition(new Vector3(-5,-9.5,-25));

		// torso1
		Geometry torso1geometry = new Box(1.7,1,1.7);
		Body torso1 = new Body( "torso1", torso1geometry );
		torso1.setPosition(new Vector3(-5,-11,-25));

		Constraint neck = new UniversalJoint(head,torso1,new Vector3(-5,-10,-25), new Vector3(0,0,1), new Vector3(1,0,0));
		scene.addConstraint(neck);

		// torso2
		Geometry torso2geometry = new Box(1.5,1,1.5);
		Body torso2 = new Body( "torso2", torso2geometry );
		torso2.setPosition(new Vector3(-5,-12.5,-25));

		UniversalJoint spine = new UniversalJoint(torso1,torso2,new Vector3(-5,-12,-25), new Vector3(0,0,1), new Vector3(1,0,0));
		spine.getFirstAxisControler().setLimits(-0.2, 0.2);
		spine.getSecondAxisControler().setLimits(-0.2, 0.2);
		scene.addConstraint(spine);

		// torso3
		Geometry torso3geometry = new Box(1.2,1,1.2);
		Body torso3 = new Body( "torso3", torso3geometry );
		torso3.setPosition(new Vector3(-5,-14,-25));

		UniversalJoint spine2 = new UniversalJoint(torso2,torso3,new Vector3(-5,-13,-25), new Vector3(0,0,1), new Vector3(1,0,0));
		spine2.getFirstAxisControler().setLimits(-0.2, 0.2);
		spine2.getSecondAxisControler().setLimits(-0.2, 0.2);
		scene.addConstraint(spine2);

		// upper left arm
		Geometry upleftarmgeometry = new Box(0.5,2,0.5);
		Body upleftarm = new Body( "upleftarm", upleftarmgeometry );
		upleftarm.setPosition(new Vector3(-3.5,-12,-25));
		
		UniversalJoint leftshoulder = new UniversalJoint(torso1,upleftarm, new Vector3(-3.5,-11,-25), new Vector3(0,0,1), new Vector3(0,1,0));
		leftshoulder.getFirstAxisControler().setLimits(-0.5, 0.5);
		leftshoulder.getSecondAxisControler().setLimits(-0.5, 0.5);
		leftshoulder.getFirstAxisControler().setFrictionMagnitude(0.01);
		leftshoulder.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(leftshoulder);

		// upper right arm
		Geometry uprightarmgeometry = new Box(0.5,2,0.5);
		Body uprightarm = new Body( "uprightarm", uprightarmgeometry );
		uprightarm.setPosition(new Vector3(-6.5,-12,-25));
		
		UniversalJoint rightshoulder = new UniversalJoint(torso1,uprightarm, new Vector3(-6.5,-11,-25), new Vector3(0,0,1), new Vector3(0,1,0));
		rightshoulder.getFirstAxisControler().setLimits(-1.5, 1.5);
		rightshoulder.getSecondAxisControler().setLimits(-1.5, 1.5);
		rightshoulder.getFirstAxisControler().setFrictionMagnitude(0.01);
		rightshoulder.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(rightshoulder);
		
		// lower left arm
		Geometry lowerleftarmgeometry = new Box(0.5,1.5,0.5);
		Body lowerleftarm = new Body( "lowerleftarm", lowerleftarmgeometry );
		lowerleftarm.setPosition(new Vector3(-3.5,-14,-25));
		
		UniversalJoint leftelbow = new UniversalJoint(upleftarm, lowerleftarm, new Vector3(-3.5,-13.25,-25), new Vector3(1,0,0), new Vector3(0,1,0));
		leftelbow.getFirstAxisControler().setLimits(-0.0, 0.0);
		leftelbow.getSecondAxisControler().setLimits(-1.5, 1.5);
		leftelbow.getFirstAxisControler().setFrictionMagnitude(0.01);
		leftelbow.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(leftelbow);

		// lower right arm
		Geometry lowerrightarmgeometry = new Box(0.5,1.5,0.5);
		Body lowerrightarm = new Body( "lowerrightarm", lowerrightarmgeometry );
		lowerrightarm.setPosition(new Vector3(-6.5,-14,-25));
		
		UniversalJoint rightelbow = new UniversalJoint(uprightarm, lowerrightarm, new Vector3(-6.5,-13.25,-25), new Vector3(1,0,0), new Vector3(0,1,0));
		rightelbow.getFirstAxisControler().setLimits(-0.0, 0.0);
		rightelbow.getSecondAxisControler().setLimits(-1.5, 1.5);
		rightelbow.getFirstAxisControler().setFrictionMagnitude(0.01);
		rightelbow.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(rightelbow);

		// left thigh
		Geometry leftthighgeometry = new Box(0.5,1.5,0.5);
		Body leftthigh = new Body( "leftthigh", leftthighgeometry );
		leftthigh.setPosition(new Vector3(-5.5,-15.5,-25));

		UniversalJoint lefthip = new UniversalJoint(torso3, leftthigh, new Vector3(-5.5,-14,-25), new Vector3(1,0,0), new Vector3(0,0,1));
		lefthip.getFirstAxisControler().setLimits(-0.5, 0.5);
		lefthip.getSecondAxisControler().setLimits(-0.5, 0.5);
		lefthip.getFirstAxisControler().setFrictionMagnitude(0.01);
		lefthip.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(lefthip);

		// left taiba
		Geometry lefttaibageometry = new Box(0.5,1.5,0.5);
		Body lefttaiba = new Body( "lefttaiba", lefttaibageometry );
		lefttaiba.setPosition(new Vector3(-5.5,-17.5,-25));

		UniversalJoint lefttknee = new UniversalJoint(leftthigh, lefttaiba, new Vector3(-5.5,-16,-25), new Vector3(1,0,0), new Vector3(0,0,1));
		lefttknee.getFirstAxisControler().setLimits(-0.5, 0.5);
		lefttknee.getSecondAxisControler().setLimits(0, 0);
		lefttknee.getFirstAxisControler().setFrictionMagnitude(0.01);
		lefttknee.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(lefttknee);

		
		// right thigh
		Geometry rightthighgeometry = new Box(0.5,1.5,0.5);
		Body rightthigh = new Body( "rightthigh", rightthighgeometry );
		rightthigh.setPosition(new Vector3(-4.0,-15.5,-25));

		UniversalJoint righthip = new UniversalJoint(torso3, rightthigh, new Vector3(-4.0,-14,-25), new Vector3(1,0,0), new Vector3(0,0,1));
		righthip.getFirstAxisControler().setLimits(-0.5, 0.5);
		righthip.getSecondAxisControler().setLimits(-0.5, 0.5);
		righthip.getFirstAxisControler().setFrictionMagnitude(0.01);
		righthip.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(righthip);

		// right taiba
		Geometry righttaibageometry = new Box(0.5,1.5,0.5);
		Body righttaiba = new Body( "righttaiba", righttaibageometry );
		righttaiba.setPosition(new Vector3(-4.0,-17.5,-25));

		UniversalJoint righttknee = new UniversalJoint(rightthigh, righttaiba, new Vector3(-4.0,-16,-25), new Vector3(1,0,0), new Vector3(0,0,1));
		righttknee.getFirstAxisControler().setLimits(-0.5, 0.5);
		righttknee.getSecondAxisControler().setLimits(0, 0);
		righttknee.getFirstAxisControler().setFrictionMagnitude(0.01);
		righttknee.getSecondAxisControler().setFrictionMagnitude(0.01);
		scene.addConstraint(righttknee);

	    
		
		
		// add all to scene
		scene.addBody(floor);
		scene.addBody(back);
		scene.addBody(front);		
		scene.addBody(left);
		scene.addBody(right);
		
		scene.addBody(head);
		scene.addBody(torso1);
		scene.addBody(torso2);
		scene.addBody(torso3);
		scene.addBody(upleftarm);
		scene.addBody(lowerleftarm);
		scene.addBody(lowerrightarm);
		scene.addBody(uprightarm);
		scene.addBody(leftthigh);
		scene.addBody(lefttaiba);
		scene.addBody(rightthigh);
		scene.addBody(righttaiba);
		
		
//		 put gravity on limbs
		scene.addForce( new GravityForce(head));		
		scene.addForce( new GravityForce(torso1));		
		scene.addForce( new GravityForce(torso2));		
		scene.addForce( new GravityForce(torso3));		
		scene.addForce( new GravityForce(upleftarm));		
		scene.addForce( new GravityForce(lowerleftarm));		
		scene.addForce( new GravityForce(uprightarm));		
		scene.addForce( new GravityForce(lowerrightarm));		
		scene.addForce( new GravityForce(leftthigh));		
		scene.addForce( new GravityForce(lefttaiba));		
		scene.addForce( new GravityForce(rightthigh));		
		scene.addForce( new GravityForce(righttaiba));		

		
		// handle drawing
		Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this, new Interaction(scene));
		rendering.drawMe(headgeometry);
		rendering.drawMe(torso1geometry);
		rendering.drawMe(torso2geometry);
		rendering.drawMe(torso3geometry);
		rendering.drawMe(upleftarmgeometry);
		rendering.drawMe(lowerleftarmgeometry);
		rendering.drawMe(uprightarmgeometry);
		rendering.drawMe(lowerrightarmgeometry);
		rendering.drawMe(leftthighgeometry);
		rendering.drawMe(lefttaibageometry);
		rendering.drawMe(rightthighgeometry);
		rendering.drawMe(righttaibageometry);


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
