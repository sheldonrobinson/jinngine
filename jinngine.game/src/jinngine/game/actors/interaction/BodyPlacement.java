package jinngine.game.actors.interaction;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.MouseMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;

import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.Game;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.DefaultScene;
import jinngine.physics.Scene;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.constraint.joint.BallInSocketJoint;

public class BodyPlacement implements ActionActor {

	private final Body target;
	private final Body controller;
//	private SpringForce force;
	private BallInSocketJoint force;
	private final com.ardor3d.math.Vector2 screenpos = new com.ardor3d.math.Vector2();
	private final Vector3 pickpoint = new Vector3();
	private final Vector3 pickdisplacement = new Vector3();
	private InputTrigger tracktrigger;
	private final ActorOwner owner;
	//stored angular properties
	private Matrix3 inertia;
	private Matrix3 inverse;

	
	public BodyPlacement(ActorOwner owner, Body target, Vector3 pickpoint, Vector2 screenpos) {
		this.target = target;
		this.controller = new Body("default");
		this.controller.state.mass = 1; // hope to prevent bugs
		this.controller.setFixed(true);
		this.pickpoint.assign(pickpoint);
		this.screenpos.set(screenpos);
		this.owner = owner;
	}
	
	@Override
	public void create(Game game) {
		// TODO Auto-generated method stub
		
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
		DefaultScene physics = game.getPhysics();
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
		this.force.setForceLimit(10);

		physics.liveconstraints.add(this.force);
		
		//copy angular mass properties
		inertia = target.state.inertia.copy();
		inverse = target.state.inverseinertia.copy();
		//remove angular movement
		Matrix3.set( Matrix3.identity(new Matrix3()).multiply(9e9), target.state.inertia);
		Matrix3.set( Matrix3.zero, target.state.inverseinertia );

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
//                    	System.out.println("mouse moved");
                    	
                    }
                });
		
                
		// install trigger
		game.getRendering().getLogicalLayer().registerTrigger(this.tracktrigger);
		
		
		
	}

	@Override
	public final void stop( Game game) {
		System.out.println("BodyPlacement stopping");
		//remove controller body and force
		DefaultScene physics = game.getPhysics();
		physics.removeConstraint(force);
		physics.removeBody(controller);	
		physics.liveconstraints.remove(this.force);

		
		//remove angular movement
		Matrix3.set(inertia, target.state.inertia);
		Matrix3.set(inverse, target.state.inverseinertia );

		//remove velocity 
//		entity.getPrimaryBody().setVelocity(new Vector3());
//		entity.getPrimaryBody().setAngularVelocity(new Vector3());

		
		//remove mouse tracker
		game.getRendering().getLogicalLayer().deregisterTrigger(this.tracktrigger);
	}

	@Override
	public void mousePressed(Game game) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(Game game) {
		// a release means that we are done
		owner.finished(game, this);
	}

}
