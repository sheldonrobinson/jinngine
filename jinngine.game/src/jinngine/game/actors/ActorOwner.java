package jinngine.game.actors;

import jinngine.game.Game;

public interface ActorOwner {
	
	/**
	 * An action actor should call this method on its owner when done
	 * @param actor
	 */
	public void finished(Game game,ActionActor actor);

}
