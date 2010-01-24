package jinngine.game.actors;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.KeyboardState;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.MouseButtonClickedCondition;
import com.ardor3d.input.logical.MouseMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Ray3;
import com.ardor3d.scenegraph.Node;
import jinngine.game.Actor;
import jinngine.game.Game;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;
import jinngine.physics.force.SpringForce;

public class BodyPlacer implements Actor {

	private final Body target;
	private final Body controller;
	private SpringForce force;
	private final com.ardor3d.math.Vector2 screenpos = new com.ardor3d.math.Vector2();
	private final Vector3 pickpoint = new Vector3();
	
	public BodyPlacer(Body target) {
		this.target = target;
		this.controller = new Body();
		this.controller.state.mass = 1; // hope to prevent bugs
		this.controller.setFixed(true);
	}
	
	public final void act( Game game ) {
		// intersect in pick-plane
		// L , t1 t2 
		// y = b+ax  (p0-y)  
		Vector3 p1 = new Vector3(), d = new Vector3();
		final Ray3 ray = new Ray3();		
		game.getRendering().getCamera().getPickRay(screenpos, false, ray);
		p1.x = ray.getOrigin().getX(); p1.y = ray.getOrigin().getY(); p1.z = ray.getOrigin().getZ();
		d.x = ray.getDirection().getX(); d.y = ray.getDirection().getY(); d.z = ray.getDirection().getZ();

		
		Vector3 planeNormal;
		planeNormal = new Vector3(0,1,0).normalize();
		double u = planeNormal.dot(pickpoint.minus(p1)) / planeNormal.dot(d);
//
		controller.setPosition(p1.add(d.multiply(u)));//.add(pickdisplacement));
//		System.out.println(""+screenpos);
	}

	@Override
	public final void start( Game game) {
		// get out the physics
		PhysicsScene physics = game.getPhysics();
		
		// place a body into the world at the centre of mass
		// of target body
		controller.setVelocity(new Vector3());
		controller.setPosition(target.getPosition());
		
		target.updateTransformations();
		controller.updateTransformations();
		this.force = new SpringForce(target, new Vector3(), controller, new Vector3(), 122, 16 ); 

		//controller.getPosition().print();
		physics.addBody(controller);
		
		//add a spring force between the two bodies
		physics.addForce(this.force);
		
		pickpoint.assign(target.getPosition());
		pickpoint.assign(new Vector3(0,-25+1,0));
		
		//setup a listender for mouse
		game.getRendering().getLogicalLayer().registerTrigger(new InputTrigger(new MouseMovedCondition(),
                new TriggerAction() {
                    @Override
                    public void perform(Canvas source,
                    		TwoInputStates inputState, double tpf) {
                    	// update the screen position
                    	screenpos.set(inputState.getCurrent().getMouseState().getX(),
                    			inputState.getCurrent().getMouseState().getY() );
                    	
                    }
                }
		));
		
	}

	@Override
	public final void stop( Game game) {
		//remove controller body and force
		PhysicsScene physics = game.getPhysics();
		physics.removeForce(force);
		physics.removeBody(target);		
	}

}
