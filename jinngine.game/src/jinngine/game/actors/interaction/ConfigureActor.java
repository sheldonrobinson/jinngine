package jinngine.game.actors.interaction;

import jinngine.game.Game;
import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.actors.ConfigurableActor;

public class ConfigureActor implements ActionActor {

	private final ConfigurableActor conf;
	private final ActorOwner owner;
	
	public ConfigureActor( ActorOwner owner, ConfigurableActor conf) {
		this.conf = conf;
		this.owner = owner;
	}
	
	@Override
	public void act(Game game) {
		// TODO Auto-generated method stub
	}

	@Override
	public void create(Game game) {
		// TODO Auto-generated method stub

	}

	@Override
	public void start(Game game) {
		// start the configuration
		conf.configure(game);
		
		// just finish immediately, should be changed
		owner.finished(game, this);
	}

	@Override
	public void stop(Game game) {
	}

	@Override
	public void mousePressed(Game game) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(Game game) {
		// TODO Auto-generated method stub
		
	}

}
