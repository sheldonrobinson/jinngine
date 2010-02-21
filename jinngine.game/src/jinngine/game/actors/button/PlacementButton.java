package jinngine.game.actors.button;

import com.ardor3d.math.Vector2;
import com.ardor3d.scenegraph.Node;

import jinngine.game.Game;
import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.actors.ConfigurableActor;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.actors.SelectableActor;
import jinngine.game.actors.interaction.BodyPlacement;
import jinngine.game.actors.interaction.ConfigureActor;
import jinngine.game.actors.platform1.Platform1;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class PlacementButton extends Platform1 implements SelectableActor {

	private boolean pressed = false;
	
	@Override
	public ActionActor provideActionActor(ActorOwner owner, Actor target, Node picknode,
			jinngine.math.Vector3 pickpoint, Vector2 screenpos ) {
				
		System.out.println("PlacementButton: got an actor "+ target);
		
		// spawn a BodyPlacement actor if possible
		if (target instanceof PhysicalActor) {
			PhysicalActor physactor = (PhysicalActor)target;
			Body body = physactor.getBodyFromNode(picknode);
		
			if (body != null)
				return new BodyPlacement(owner, body,pickpoint,screenpos);
		}
		
		
		return null;
	}

	@Override
	public void setSelected(Game game, boolean selected) {
		pressed = selected;
	}
	
}
