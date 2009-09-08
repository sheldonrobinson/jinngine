package jinngine.demo.graphics;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.force.*;

public class ObjectPlacement implements GameState {
	private Entity entity;
	private boolean done;
	double mousebiasx, mousebiasy, mousebiasz;
	private Vector3 initialPosition;
	private Force force;
	private Force damper;
	private Vector3 pickdisplacement;
	private Vector3 pickpoint;
	private boolean alternativePlaceMode = false;
		
	//stored angular properties
	private Matrix3 inertia;
	private Matrix3 inverse;
	
	//dummy body to connect movement forces
	private final Body dummy;
	
	public ObjectPlacement( Entity e, Vector3 pickpoint ) {
		entity = e;
		
		//create a dummy body without geometric meaning
		dummy = new Body();
		dummy.finalize();
		dummy.setFixed(true);
		
		initialPosition = entity.getPosition();
		dummy.setPosition(initialPosition);

		//pointing from the pick-point to the centre of mass
		pickdisplacement = dummy.state.rCm.minus(pickpoint);
		this.pickpoint = pickpoint.copy();
	}

	@Override
	public boolean done() {
		return done;
	}

	@Override
	public void start(Graphics m) {
		entity.setAlarmed(true);
		mousebiasx = m.mouse.x;
		mousebiasy = m.mouse.y;
		mousebiasz = m.mouse.z;
		
		initialPosition = entity.getPosition();
		dummy.setPosition(initialPosition);
		double mass = entity.getPrimaryBody().state.M;

		force = new SpringForce(dummy,new Vector3(), entity.getPrimaryBody(), new Vector3() ,125*mass,0);
		damper = new LinearDragForce(entity.getPrimaryBody(), 2.9625*mass);
		
		
		//copy angular mass properties
		inertia = entity.getPrimaryBody().state.I.copy();
		inverse = entity.getPrimaryBody().state.Iinverse.copy();
		
		//remove angular movement
		Matrix3.set( Matrix3.identity(new Matrix3()).multiply(9e9),entity.getPrimaryBody().state.I);
		Matrix3.set( Matrix3.zero, entity.getPrimaryBody().state.Iinverse );
		
		m.getModel().addForce(force);
		m.getModel().addForce(damper);
				
		System.out.println("object placement start");
	}

	@Override
	public void stop(Graphics m) {
		entity.setAlarmed(false);
		m.getModel().removeForce(force);
		m.getModel().removeForce(damper);
		
		//remove angular movement
		Matrix3.set(inertia, entity.getPrimaryBody().state.I);
		Matrix3.set(inverse, entity.getPrimaryBody().state.Iinverse );

		//remove velocity 
		entity.getPrimaryBody().setVelocity(new Vector3());
		
	}

	@Override
	public void tick(Graphics m) {
		//get rid of sleeping
		entity.getPrimaryBody().sleepy = false;
		entity.getPrimaryBody().sleepyness = 0;
		entity.getPrimaryBody().sleeping = false;
		
		
		//intersect in pick-plane
		// L , t1 t2 
		// y = b+ax  (p0-y)  
		Vector3 p1 = new Vector3(), d = new Vector3();
		m.getRender().getPointerRay(p1, d);
		//p2.assign(p2.add(p1));

		Vector3 planeNormal;
		if (alternativePlaceMode) {
			planeNormal = new Vector3(0,0,1).normalize();
		} else {
			planeNormal = new Vector3(0,1,0).normalize();			
		}
		
		double u = planeNormal.dot(pickpoint.minus(p1)) / planeNormal.dot(d);
		//System.out.println("object placement state");
		
		
//		dummy.setPosition(new Vector3(
//				-(m.mouse.a1-mousebiasx)/2.0+initialPosition.a1, 
//				initialPosition.a2 -(m.mouse.a3-mousebiasz)/2.0, 
//				-(m.mouse.a2-mousebiasy)/2.0+initialPosition.a3));

		dummy.setPosition(p1.add(d.multiply(u)).add(pickdisplacement));
		
		//m.mouse.print();
		//release object and respawn the selection state
		if (!m.pressed) {
			done = true;
			System.out.println("starting selevtion");
			m.addState(new Selection());
		}
		
		if (m.spacepressed) {
			if (!alternativePlaceMode) {
				alternativePlaceMode = true;
				
				//recalculate the pickpoint
				pickpoint.assign(p1.add(d.multiply(u)));
			}
		} 
		
		if ( alternativePlaceMode) {
			if (m.spacereleased) {
				alternativePlaceMode = false;
				
				//recalculate the pickpoint
				pickpoint.assign(p1.add(d.multiply(u)));
			}
		}
		
		
	}

}
