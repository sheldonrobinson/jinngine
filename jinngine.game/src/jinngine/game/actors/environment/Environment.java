package jinngine.game.actors.environment;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Box;

import jinngine.game.actors.Actor;
import jinngine.game.*;
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;

/**
 * Floors, background etc
 */
public class Environment implements Actor {

	private Box floorbox;
	
	@Override
	public void act( Game game ) {
	}

	@Override
	public void start( Game game) {
		PhysicsScene physics = game.getPhysics();
		Node rootnode = game.getRendering().getRootNode();
		
		// make the floor box 
		floorbox = new Box("Box", new Vector3(0,-25-5,0), 120, 5, 120);
		floorbox.setSolidColor(new ColorRGBA(0.7f,0.7f,0.7f,1));
		floorbox.setModelBound(new BoundingBox());
		rootnode.attachChild(floorbox); //dont draw floor
		//shadowing
		game.getRendering().getPssmPass().add(floorbox);

		// make physics box
		Body floor = new Body(new jinngine.geometry.Box(120,10,120));
		floor.setPosition(new jinngine.math.Vector3(0,-25 -5,0));
		floor.setFixed(true);
		physics.addBody(floor);
		
	}

	@Override
	public void stop( Game game) {

	}

}
