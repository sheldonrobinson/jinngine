package jinngine.game.actors.environment;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Box;

import jinngine.game.actors.Actor;
import jinngine.game.*;
import jinngine.physics.Body;
import jinngine.physics.Scene;

/**
 * Floors, background etc
 */
public class Environment extends Node implements Actor {	

	private Box floorbox;

	@Override
	public void create(Game game) {
		Node rootnode = game.getRendering().getRootNode();

		// make the floor box 
		floorbox = new Box("Box", new Vector3(0,-25-5,0), 120, 5, 120);
		floorbox.setSolidColor(new ColorRGBA(0.7f,0.7f,0.7f,1));
		floorbox.setModelBound(new BoundingBox());
		floorbox.setName("myfloorboxnode");
		this.attachChild(floorbox); //dont draw floor
		rootnode.attachChild(this);		
	}
	
	@Override
	public void act( Game game ) {
	}

	@Override
	public void start( Game game) {
		Scene physics = game.getPhysics();
		floorbox = (Box)getChild("myfloorboxnode");
		
		//shadowing
		game.getRendering().getPssmPass().add(floorbox);

		// make physics box
		Body floor = new Body("default", new jinngine.geometry.Box(120,10,120));
		floor.setPosition(new jinngine.math.Vector3(0,-25 -5,0));
		floor.setFixed(true);
		physics.addBody(floor);		
	}

	@Override
	public void stop( Game game) {

	}

}
