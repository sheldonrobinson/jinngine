package jinngine.game.actors;

import jinngine.game.Game;


public interface Actor {	
	
	/**
	 * Do stuff when the actor comes into play
	 */
	public void start( Game game );
	
	/**
	 * Do the stuff that should be done each frame
	 */
	public void act( Game game );
	
	/**
	 * Do clean up before actor leaves
	 * @param game
	 */
	public void stop( Game game);
}
