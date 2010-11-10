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
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.*;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
import jinngine.rendering.Interaction;
import jinngine.rendering.Rendering;

public class BasicExample implements Rendering.Callback {	
	private final Scene scene;
	
	public BasicExample() {		
		// start jinngine 
		scene = new DefaultScene(
				new SAP2(), 
				new NonsmoothNonlinearConjugateGradient(44), 
				new DisabledDeactivationPolicy());
		
		scene.setTimestep(0.1);
		
		// add boxes to bound the world
		Box floor = new Box("floor",1500,20,1500);
		scene.addGeometry(Matrix3.identity(), new Vector3(0,-30,0), floor);
		scene.fixBody(floor.getBody(), true);
		
		Box back = new Box("back", 200, 200, 20);		
		scene.addGeometry(Matrix3.identity(), new Vector3(0,0,-55), back);
		scene.fixBody(back.getBody(), true);

		Box front = new Box("front", 200, 200, 20);		
		scene.addGeometry( Matrix3.identity(), new Vector3(0,0,-7), front);
		scene.fixBody(front.getBody(), true);

		Box left = new Box("left", 20, 200, 200);		
		scene.addGeometry(Matrix3.identity(), new Vector3(-35,0,0), left);
		scene.fixBody(left.getBody(), true);

		Box right = new Box("right", 20, 200, 200);		
		scene.addGeometry(Matrix3.identity(), new Vector3(10,0,0), right);
		scene.fixBody( right.getBody(), true);		
		
		Box box = new Box("box",5,5,5);
		scene.addGeometry(Matrix3.identity(), new Vector3(-10,-10, -20), box);
		

		// put gravity on box
		scene.addForce( new GravityForce(box.getBody()));		
		
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
		new BasicExample();
	}
}
