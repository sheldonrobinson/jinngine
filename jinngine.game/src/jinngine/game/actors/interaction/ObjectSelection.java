package jinngine.game.actors.interaction;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.MouseButtonClickedCondition;
import com.ardor3d.input.logical.MouseButtonPressedCondition;
import com.ardor3d.input.logical.MouseButtonReleasedCondition;
import com.ardor3d.input.logical.MouseMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

import jinngine.game.actors.Actor;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.Game;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class ObjectSelection implements Actor {

	private Actor workingactor = null;
	private final InputTrigger mousetrigger;
	private final InputTrigger mousetrigger2;	
	private final com.ardor3d.math.Vector2 pos = new Vector2();
	private boolean pressed = false;
	private boolean released = false;
	
	
	public ObjectSelection() {		
		// create the mouse triggers
		this.mousetrigger = new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT), new TriggerAction() {
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
            	// update the screen position
            	pos.set(inputState.getCurrent().getMouseState().getX(),
            			inputState.getCurrent().getMouseState().getY());
            	pressed = true;
            }
        });
		
		this.mousetrigger2 = new InputTrigger(new MouseButtonReleasedCondition(MouseButton.LEFT), new TriggerAction() {
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
            	// update the screen position
            	pos.set(inputState.getCurrent().getMouseState().getX(),
            			inputState.getCurrent().getMouseState().getY());
            	released = true;
            }
        });
	}
	
	@Override
	public void act(Game game) {
		// if pressed and nothing is going on, do pick
		if (pressed && workingactor == null) {
			pressed = false;
			
			// get the pick ray from camera and perform pick
			final Ray3 ray = new Ray3();
			game.getRendering().getCamera().getPickRay(pos, false, ray);
			PickResults results = game.getRendering().doPick(ray);
			
			// if any results
			if (results.getNumber()>0) {
				System.out.println(""+results.getPickData(0).getTargetMesh());
				System.out.println(""+results.getPickData(0).getIntersectionRecord().getIntersectionPoint(0));
				
				// walk upwards in the scenegraph to find a PhysicalActor
				Node node = results.getPickData(0).getTargetMesh().getParent();
				while (node != null) {
					if (node.getUserData() != null) {
						if (node.getUserData() instanceof PhysicalActor ) {
							// found
							PhysicalActor actor = (PhysicalActor)node.getUserData();
							
							// get the body that we should be moving around (if any)
							Body body = actor.getBodyFromNode(node);
							
							// start a placement actor
							if (body != null) {
								Vector3 pickpoint = new Vector3(
										results.getPickData(0).getIntersectionRecord().getIntersectionPoint(0).getX(),
										results.getPickData(0).getIntersectionRecord().getIntersectionPoint(0).getY(),
										results.getPickData(0).getIntersectionRecord().getIntersectionPoint(0).getZ()
								);
								workingactor = new BodyPlacement(body, pickpoint, pos);
								game.addActor(workingactor);								
							}
						} // if PhysicalActor
					} // if user data
					
					node = node.getParent();
				} // while node
			} // pick results			
		} // if pressed ...
		
		// check if actor should be removed
		if ( released ) {
			released = false;			
			if (workingactor!= null) {
				game.removeActor(workingactor);
				workingactor = null;
			}
		}
	}

	@Override
	public void start(Game game) {
		// install a mouse click trigger
		game.getRendering().getLogicalLayer().registerTrigger(this.mousetrigger);
		game.getRendering().getLogicalLayer().registerTrigger(this.mousetrigger2);

	}

	@Override
	public void stop(Game game) {
		// remove the mouse click trigger
		game.getRendering().getLogicalLayer().deregisterTrigger(this.mousetrigger);		
		game.getRendering().getLogicalLayer().deregisterTrigger(this.mousetrigger2);		

	}
}
