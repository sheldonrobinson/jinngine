package jinngine.game.actors;

import com.ardor3d.scenegraph.Node;
import jinngine.game.actors.Actor;
import jinngine.physics.Body;

public interface PhysicalActor extends Actor {
	
	/**
	 * PhysicalActor must provide a mapping between some ardor3d Node and a jinngine Body. 
	 * For instance, this is used together with picking, so that the user can interact with 
	 * the physical actor. 
	 * @param node
	 * @return
	 */
	public Body getBodyFromNode(Node node);
	
}
