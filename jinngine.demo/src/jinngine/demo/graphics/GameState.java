package jinngine.demo.graphics;

public interface GameState {

	/**
	 * When this game state is started
	 */
	public void start(Graphics m);
	
	
	/**
	 * Called every time frame when this game state is active
	 */
	public void tick(Graphics m);
	
	
	/**
	 * Must return true if the work of this game state is done. This
	 * should result in the method stop() being called eventually.
	 */
	public boolean done();
	
	/**
	 * Called when this game state is terminated
	 * @param m TODO
	 */
	public void stop(Graphics m);
}
