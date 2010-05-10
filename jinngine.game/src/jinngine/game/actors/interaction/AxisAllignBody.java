package jinngine.game.actors.interaction;

import com.ardor3d.math.Vector2;
import jinngine.game.actors.ActionActor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.Game;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class AxisAllignBody implements ActionActor {

	private final Body target;
	private final ActorOwner owner;

	public AxisAllignBody(ActorOwner owner, Body target, Vector3 pickpoint, Vector2 screenpos) {
		this.target = target;
		this.owner = owner;
	}
	
	@Override
	public void create(Game game) {
		// TODO Auto-generated method stub
		
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
		// we simply crank the orientation to axis allignment
		target.state.orientation.assign(Quaternion.rotation(0.0, new Vector3(0,1,0)));
		target.updateTransformations();
		
		owner.finished(game, this);
	}

	@Override
	public void mouseReleased(Game game) {
	}

}
