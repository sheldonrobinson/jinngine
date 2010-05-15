package jinngine.game.actors.interaction;


import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.Game;
import jinngine.game.PostponedAction;
import jinngine.math.Vector3;

public class InsertActor implements ActionActor {

	private final Actor target;
	private final ActorOwner owner;
	private final Vector3 pickpoint = new Vector3();

	public InsertActor(ActorOwner owner, Actor target,  Vector3 pickpoint ) {
		this.target = target;
		this.owner = owner;
		this.pickpoint.assign(pickpoint);
	}
	
	@Override
	public void create(Game game) {
	}
	
	public final void act( Game game ) {
	}

	@Override
	public final void start( final Game game) {
		game.addPostponedAction(new PostponedAction() {
			@Override
			public void perform() {
				// create the new actor
				target.create(game);
				
				// insert it into game
				game.addActor(target);						
			}
		});
		
		// tell owner that were done
		owner.finished(game, this);

	}

	@Override
	public final void stop( Game game) {
		
	}

	@Override
	public void mousePressed(final Game game) {
		
	}

	@Override
	public void mouseReleased(Game game) {
	}

}
