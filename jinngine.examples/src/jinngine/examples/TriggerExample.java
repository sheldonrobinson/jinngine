/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.examples;


import jinngine.geometry.Box;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.*;
import jinngine.physics.ContactTrigger.Callback;
import jinngine.physics.constraint.contact.ContactConstraint;
import jinngine.physics.force.GravityForce;
import jinngine.rendering.Interaction;
import jinngine.rendering.Rendering;

public class TriggerExample implements Rendering.Callback {	
	private final Scene scene;
	
	public TriggerExample() {
		
		// start jinngine 
		scene = new DefaultScene();
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
		
		// create a box
		Box box = new Box("box",2,2,2);
		scene.addGeometry(Matrix3.identity(), new Vector3(-10,-11,-25), box );
				
		// put gravity on box
		scene.addForce( new GravityForce(box.getBody()));	
		
		// create a trigger to detect contact forces with some threshold
		scene.addTrigger(new ContactTrigger(box.getBody(), 2.0, new Callback(){
			@Override
			public void contactAboveThreshold(Body interactingBody,
					ContactConstraint constraint) {
				System.out.println("In contact with " + interactingBody  );
			}
			@Override
			public void contactBelowThreshold(Body interactingBody,
					ContactConstraint constraint) {
				System.out.println("No longer in contact with " + interactingBody );
			}
		}));
		
		// handle drawing
		Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);
		rendering.addCallback(new Interaction(scene));
		rendering.drawMe(box);
		rendering.createWindow();
		rendering.start();
	}

	@Override
	public void tick() {
		// each frame, to a time step on the Scene
		scene.tick();
	}
	
	public static void main( String[] args) {
		new TriggerExample();
	}

}
