package jinngine.game.actors.interaction;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.KeyState;
import com.ardor3d.input.KeyboardState;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyHeldCondition;
import com.ardor3d.input.logical.MouseMovedCondition;
import com.ardor3d.input.logical.MouseWheelMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.google.common.base.Predicate;

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
//	private final Vector3 pickdisplacement = new Vector3();
	private InputTrigger tracktrigger;
	private InputTrigger modifiertrigger;
	private final ActorOwner owner;
	//stored angular properties
	private Matrix3 inertia;
	private Matrix3 inverse;
	private boolean modifier = false;
	private boolean updatepickpoint = false;
	private final Vector3 planeNormal;

	
	public BodyPlacement(ActorOwner owner, Body target, Vector3 pickpoint, Vector2 screenpos) {
		this.target = target;
		this.controller = new Body("default");
		this.controller.state.mass = 1; // hope to prevent bugs
		this.controller.setFixed(true);
		this.pickpoint.assign(pickpoint);
		this.screenpos.set(screenpos);
		this.owner = owner;
		this.planeNormal = new Vector3(0,1,0);
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
//		if (modifier)
//			planeNormal.assign(0,0,1);
//		else
//			planeNormal.assign(0,1,0);
//		
//		if (updatepickpoint)
//			//recalculate the pickpoint
//			pickpoint.assign(p1.add(d.multiply(u)));

			
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
//		pickdisplacement.assign(pickpoint.minus(target.getPosition()));

		// place a body into the world at the centre of mass
		// of target body
		controller.setVelocity(new Vector3());
		controller.setPosition(pickpoint);
		
//		this.force = new SpringForce(target, new Vector3(), controller, new Vector3(), 122, 16 ); 
		this.force = new BallInSocketJoint(target, controller, controller.getPosition(), new Vector3(0,1,0));
		this.force.setForceLimit(5*target.getMass());
		this.force.setCorrectionVelocityLimit(5);
		

		physics.addLiveConstraint(this.force);
		
		//copy angular mass properties
		inertia = target.state.inertia.copy();
		inverse = target.state.inverseinertia.copy();
		//remove angular movement
		//Matrix3.set( Matrix3.identity(new Matrix3()).multiply(9e9), target.state.inertia);
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

			}
		});

		
		// setup a listener for keyboard
		this.modifiertrigger = new InputTrigger(new Predicate<TwoInputStates>() {
			private boolean firsttime = true;
			public boolean apply(TwoInputStates state) {
				final KeyboardState current = state.getCurrent().getKeyboardState();
				final KeyboardState previous = state.getPrevious().getKeyboardState();
				// always run the first time around, so we wont miss
				// if LSHIFT is already held down when the trigger is created
				if (firsttime) {
					firsttime = false;
					return true;
				}
				// only monitor the LSHIFT key for presses and releases
				if ( current.getKeysPressedSince(previous).contains(Key.LSHIFT) 
						|| current.getKeysReleasedSince(previous).contains(Key.LSHIFT)) {
					return true;
				} else {
					return false;
				}
			}
		}, 
		new TriggerAction() {
			public void perform(Canvas source, TwoInputStates inputState, double tpf) {
				// we update pickpoint and plane normal when shift is pressed or released
				if (inputState.getCurrent().getKeyboardState().isDown(Key.LSHIFT)) {
					System.out.println("Shift is down");
					planeNormal.assign(0,0,1);
					pickpoint.assign(controller.getPosition());
					
				} else {
					System.out.println("Shift is up");
					planeNormal.assign(0,1,0);
					pickpoint.assign(controller.getPosition());
				}
			}
		});

                
		// install triggers
		game.getRendering().getLogicalLayer().registerTrigger(this.tracktrigger);
		game.getRendering().getLogicalLayer().registerTrigger(this.modifiertrigger);
	}

	@Override
	public final void stop( Game game) {
		System.out.println("BodyPlacement stopping");
		//remove controller body and force
		DefaultScene physics = game.getPhysics();
		physics.removeConstraint(force);
		physics.removeBody(controller);	
		physics.removeLiveConstraint(this.force);

		
		// remove angular movement by setting the inverse inertia to zero.
		// This basically means that the body cannot get into any angular movement,
		// because it would take an infinite amount of energy to do it
		Matrix3.set(inertia, target.state.inertia);
		Matrix3.set(inverse, target.state.inverseinertia );

		//remove velocity  (disabled)
//		entity.getPrimaryBody().setVelocity(new Vector3());
//		entity.getPrimaryBody().setAngularVelocity(new Vector3());

		
		//remove mouse tracker
		game.getRendering().getLogicalLayer().deregisterTrigger(this.tracktrigger);
		
		// remove keyboard trigger
		game.getRendering().getLogicalLayer().deregisterTrigger(this.modifiertrigger);
		
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
