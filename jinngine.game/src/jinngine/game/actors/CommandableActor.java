package jinngine.game.actors;

import jinngine.game.actors.Actor;
import jinngine.math.Vector3;

public interface CommandableActor extends Actor, PhysicalActor {
	
	/**
	 * Set a target position
	 */
	public void moveToPosition( Vector3 pos);

}
