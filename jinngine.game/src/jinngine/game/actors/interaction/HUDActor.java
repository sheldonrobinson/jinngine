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

import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.actors.SelectableActor;
import jinngine.game.Game;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class HUDActor implements Actor, ActorOwner {

	private SelectableActor selectedactor = null;
	private ActionActor actionactor = null;
	
	private final InputTrigger mousetrigger;
	private final InputTrigger mousetrigger2;
	private final InputTrigger clickedtrigger;
	private final com.ardor3d.math.Vector2 mouseposition = new Vector2();

	// kinda dump states, but no other solution 
	private boolean clicked = false;
	private boolean pressed = false;
	private boolean released = false;
		
	public HUDActor() {		
		// create the mouse triggers
		this.mousetrigger = new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT), new TriggerAction() {
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
            	// update the screen position
            	mouseposition.set(inputState.getCurrent().getMouseState().getX(),
            			inputState.getCurrent().getMouseState().getY());
            	pressed = true;
            }
        });
		
		this.mousetrigger2 = new InputTrigger(new MouseButtonReleasedCondition(MouseButton.LEFT), new TriggerAction() {
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
            	// update the screen position
            	mouseposition.set(inputState.getCurrent().getMouseState().getX(),
            			inputState.getCurrent().getMouseState().getY());
            	released = true;
            }
        });
		this.clickedtrigger = new InputTrigger(new MouseButtonClickedCondition(MouseButton.LEFT), new TriggerAction() {
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
            	// update the screen position
            	mouseposition.set(inputState.getCurrent().getMouseState().getX(),
            			inputState.getCurrent().getMouseState().getY());
            	clicked = true;
            }
        });

	}
	
	@Override
	public void create(Game game) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void act(Game game) {			
		// we need to see if we can find the stuff thats being clicked at
		Actor pressedactor = null;
		if ( pressed ) {
			pressed = false;

			System.out.println("pressed!");

			// get the pick ray from camera and perform pick
			final Ray3 ray = new Ray3();
			game.getRendering().getCamera().getPickRay(mouseposition, false, ray);
			PickResults results = game.getRendering().doPick(ray);

			// if any results
			Node node = null;
			if (results.getNumber()>0) {
				node = results.getPickData(0).getTargetMesh().getParent();
				System.out.println(""+node);
				// walk upwards in the scenegraph to find a SelectableActor
				while (node != null) {
//					if (node.getUserData() != null) {
//						if (node.getUserData() instanceof Actor ) {								
//							pressedactor = (Actor)node.getUserData();
//							// got what we wantet, done
//							break;
//						}						
//					} // if user data
					
					if (node instanceof Actor) {
						pressedactor = (Actor)node;
						break;
					}
					
					node = node.getParent();
				} // while node
			} // pick results			

			System.out.println("Pressed actor:" + pressedactor);
			
			// if found nothing, we're done
			if (pressedactor == null)
				return;
			
			// found selectable ?
			if (pressedactor instanceof SelectableActor ) {								
				// found. if we already have an selection, we should ignore this
				// selectable, unless it is the same. Then it should be deselected
				SelectableActor newselection = (SelectableActor)pressedactor;

				if (newselection == selectedactor) {
					// deselect
					selectedactor.setSelected(game,false);
					selectedactor = null;
					System.out.println("Deselected " + newselection);

					// done
					return;

				} else if ( selectedactor == null ) {
					// whole new selection
					
					// can it be selected? 
					if( newselection.canBeSelected() ) {
						newselection.setSelected(game,true);
						selectedactor = newselection;								
						System.out.println("Selected " + newselection);
					}
					
					// done
					return;
				}
			} // if SelectableActor

			// if we have an active selection, try to apply it to the pressed actor
			if ( selectedactor != null && actionactor == null) {
				
				System.out.println("polling action actor from selection");
				
				// ugly conversion
				Vector3 pickpoint = new Vector3(
						results.getPickData(0).getIntersectionRecord().getIntersectionPoint(0).getX(),
						results.getPickData(0).getIntersectionRecord().getIntersectionPoint(0).getY(),
						results.getPickData(0).getIntersectionRecord().getIntersectionPoint(0).getZ()
				);
				
				// get a new action actor from the selected actor
				ActionActor newactor = selectedactor.provideActionActor(this, pressedactor, node,  pickpoint.copy(), mouseposition);
								
				// if any, add the new actor
				if (newactor!=null) {
					actionactor = newactor;
					game.addActor(newactor);
				}
			}
			
			
			// if any action actor present, relay pressed event
			if (actionactor!=null) {
				actionactor.mousePressed(game);
			}
			
		} // if pressed ...
		
		// if released
		if (released) {
			released = false;			
			
			System.out.println("released");
			// if there is an action actor working, signal the release
			if (actionactor != null) {
				actionactor.mouseReleased(game);
			}
				
		}
		
		// if we have an active selection ...
		if ( selectedactor != null) {
			// make sure it still wants to be selected
			if( !selectedactor.canBeSelected() ) {
				// deselect the selected actor and stop the action actor
				if (actionactor != null) {
					finished(game, actionactor);
				}

				// deselect
				System.out.println("Deselected " + selectedactor);
				selectedactor.setSelected(game,false);
				selectedactor = null;
			}
		}
		
	}

	@Override
	public void start(Game game) {
		// install a mouse click trigger
		game.getRendering().getLogicalLayer().registerTrigger(this.mousetrigger);
		game.getRendering().getLogicalLayer().registerTrigger(this.mousetrigger2);
		game.getRendering().getLogicalLayer().registerTrigger(this.clickedtrigger);

	}

	@Override
	public void stop(Game game) {	
		// reset selection and action
		this.selectedactor = null;
		this.actionactor = null;
		
		// remove the mouse click trigger
		game.getRendering().getLogicalLayer().deregisterTrigger(this.mousetrigger);		
		game.getRendering().getLogicalLayer().deregisterTrigger(this.mousetrigger2);		
		game.getRendering().getLogicalLayer().deregisterTrigger(this.clickedtrigger);		

	}

	@Override
	public void finished(Game game, ActionActor actor) {
		// if there is an action actor working, remove it
		if (actionactor == actor) {
			game.removeActor(actionactor); 
			actionactor = null;
		}
		
	}
}
