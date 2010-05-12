package jinngine.game.actors.interaction;

import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.actors.ScalableActor;
import jinngine.game.Game;
import jinngine.math.Vector3;

public class DeleteActor implements ActionActor {

	private final Actor target;
	private final ActorOwner owner;

	public DeleteActor(ActorOwner owner, Actor target ) {
		this.target = target;
		this.owner = owner;
	}
	
	@Override
	public void create(Game game) {
	}
	
	public final void act( Game game ) {
	}

	@Override
	public final void start( Game game) {
	}

	@Override
	public final void stop( Game game) {
		
	}

	@Override
	public void mousePressed(Game game) {
		// set the target actor for removal
		game.removeActor(target);		
		owner.finished(game, this);
	}

	@Override
	public void mouseReleased(Game game) {
	}

}
