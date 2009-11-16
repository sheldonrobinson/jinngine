package jinngine.demo;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import jinngine.demo.graphics.Entity;
import jinngine.demo.graphics.FlatShade;
import jinngine.demo.graphics.Graphics;
import jinngine.demo.graphics.Hull;
import jinngine.demo.graphics.Render;
import jinngine.geometry.Box;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Model;
import jinngine.physics.force.GravityForce;

public class Sphere implements Entity {
	private final Body body;
	private boolean alarmed = false;
	
	public Sphere( Graphics m, Vector3 size, Vector3 position, double mass ) {
		Render render = m.getRender();
		Model model = m.getModel();		
			
		//just a sphere for drawing
		Random random = new Random();
		List<Vector3> points = new LinkedList<Vector3>();
		for (int n=0;n<75; n++) {
			points.add((new Vector3(random.nextGaussian(),random.nextGaussian(),random.nextGaussian())).normalize().multiply(2));
		}

		//resize the drawing shape to the right dimensions
//		Matrix4 transform = jinngine.math.Transforms.scale(size);
//		for (Vector3 p: points)
//			p.assign( transform.multiply(p));
		
		//create drawing shape
		Hull shape = new Hull(points.iterator());
		shape.setAuxiliary(this);

		//Setup the physics
		jinngine.geometry.Sphere box = new jinngine.geometry.Sphere(2);
		box.setMass(mass);
		box.setAuxiliary(this); 
		
		//set material properties
		box.setFrictionCoefficient(0.55);
		box.setRestitution(0.3);
		
		//create a body, and add the geometry to it
		body = new Body();		
		body.addGeometry(box);		
		body.finalize();                 // called when all geometry is added
		body.setPosition(position);
		body.sleepKinetic = 0.00;
		
		//Tell the model about our new box and attach a gravity force to it
		model.addForce(new GravityForce(body));
		//model.addForce(new GravityForce(body,new Vector3(1,0,0),0.4));

		model.addBody(body);
	
		//finally, ask render to draw this shape
		render.addShape( new FlatShade(), shape, body.state.transform, this);

	}
	
	public void setPosition(Vector3 p) {
		body.setPosition(p);
	}

	@Override
	public void setSelected(boolean selected) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean getAlarmed() {
		return alarmed;
	}

	@Override
	public void setAlarmed(boolean alarmed) {
		this.alarmed = alarmed;
	}

	@Override
	public Vector3 getPosition() {
		return body.state.rCm.copy();
	}

	@Override
	public Body getPrimaryBody() {
		return body;
	}

}