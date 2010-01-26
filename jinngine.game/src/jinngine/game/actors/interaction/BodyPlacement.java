package jinngine.game.actors.interaction;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.MouseMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import jinngine.game.actors.Actor;
import jinngine.game.Game;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.constraint.joint.BallInSocketJoint;

public class BodyPlacement implements Actor {

	private final Body target;
	private final Body controller;
//	private SpringForce force;
	private Constraint force;
	private final com.ardor3d.math.Vector2 screenpos = new com.ardor3d.math.Vector2();
	private final Vector3 pickpoint = new Vector3();
	private final Vector3 pickdisplacement = new Vector3();
	private InputTrigger tracktrigger;
	
	public BodyPlacement(Body target, Vector3 pickpoint, Vector2 screenpos) {
		this.target = target;
		this.controller = new Body();
		this.controller.state.mass = 1; // hope to prevent bugs
		this.controller.setFixed(true);
		this.pickpoint.assign(pickpoint);
		this.screenpos.set(screenpos);
	}
	
	public final void act( Game game ) {
		// intersect in pick-plane
		// L , t1 t2 
		// y = b+ax  (p0-y)  
		Vector3 p1 = new Vector3(), d = new Vector3();
		final Ray3 ray = new Ray3();
		
		// get the pick ray from camera
		game.getRendering().getCamera().getPickRay(screenpos, false, ray);
		
		// ugly copy from one vector format to another
		p1.x = ray.getOrigin().getX(); p1.y = ray.getOrigin().getY(); p1.z = ray.getOrigin().getZ();
		d.x = ray.getDirection().getX(); d.y = ray.getDirection().getY(); d.z = ray.getDirection().getZ();
		
		// pick-plane is defined by the pickpoint and a normal. Current position
		// is the intersection of the pick-ray against the pick-plane
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
		target.updateTransformations();
		controller.updateTransformations();

		// set the initial pick-point displacement
		pickdisplacement.assign(pickpoint.minus(target.getPosition()));

		// place a body into the world at the centre of mass
		// of target body
		controller.setVelocity(new Vector3());
		controller.setPosition(pickpoint);
		
//		this.force = new SpringForce(target, new Vector3(), controller, new Vector3(), 122, 16 ); 
		this.force = new BallInSocketJoint(target, controller, controller.getPosition(), new Vector3(0,1,0));

		// insert the acting stuff into the physics world
		physics.addBody(controller);
		physics.addConstraint(force);
		
		
		// setup a listener for mouse
		this.tracktrigger = new InputTrigger(new MouseMovedCondition(),
                new TriggerAction() {
                    @Override
                    public void perform(Canvas source,
                    		TwoInputStates inputState, double tpf) {
                    	// update the screen position
                    	screenpos.set(inputState.getCurrent().getMouseState().getX(),
                    			inputState.getCurrent().getMouseState().getY() );
                    	
                    }
                });
		
                
		// install trigger
		game.getRendering().getLogicalLayer().registerTrigger(this.tracktrigger);
	}

	@Override
	public final void stop( Game game) {
		System.out.println("BodyPlacement stopping");
		//remove controller body and force
		PhysicsScene physics = game.getPhysics();
		physics.removeConstraint(force);
		physics.removeBody(controller);	
		
		//remove mouse tracker
		game.getRendering().getLogicalLayer().deregisterTrigger(this.tracktrigger);
	}

}
