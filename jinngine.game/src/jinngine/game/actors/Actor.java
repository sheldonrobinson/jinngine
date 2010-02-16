package jinngine.game.actors;

import jinngine.game.Game;


public interface Actor {	
	
	/**
	 * Create an actor from scratch. This means loading textures and creating nodes etc 
	 * @param game
	 */
	public void create( Game game );	
	
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
