package jinngine.game.actors.interaction;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.MouseMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Vector2;
import jinngine.game.actors.ActionActor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.actors.ScalableActor;
import jinngine.game.Game;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class ScaleActor implements ActionActor {

	private final ScalableActor target;
	private final ActorOwner owner;
	private InputTrigger tracktrigger;
	private final Vector2 drag = new Vector2();
	private final Vector2 reference = new Vector2();
	
	private boolean firstevent = true;
	

	public ScaleActor(ActorOwner owner, ScalableActor target ) {
		this.target = target;
		this.owner = owner;
	}
	
	@Override
	public void create(Game game) {
		// TODO Auto-generated method stub
		
	}
	
	public final void act( Game game ) {
		target.setScale(new Vector3((drag.getY()-reference.getY())/10,1,(drag.getX()-reference.getX())/10));
	}

	@Override
	public final void start( Game game) {
		
		
		// setup a listener for mouse
		this.tracktrigger = new InputTrigger(new MouseMovedCondition(),
				new TriggerAction() {
			@Override
			public void perform(Canvas source,
					TwoInputStates inputState, double tpf) {
				
				if (firstevent) {
					reference.set(inputState.getCurrent().getMouseState().getX(),
							inputState.getCurrent().getMouseState().getY());
					firstevent = false;
				}
				
				// update the screen position
				drag.set(inputState.getCurrent().getMouseState().getX(),
						inputState.getCurrent().getMouseState().getY() );

			}
		});

		
		// install triggers
		game.getRendering().getLogicalLayer().registerTrigger(this.tracktrigger);
	}

	@Override
	public final void stop( Game game) {
		// deregister triggers
		game.getRendering().getLogicalLayer().deregisterTrigger(this.tracktrigger);
	}

	@Override
	public void mousePressed(Game game) {
		
	}

	@Override
	public void mouseReleased(Game game) {
		owner.finished(game, this);
	}

}
