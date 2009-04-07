package jinngine.demo;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import jinngine.demo.graphics.Entity;
import jinngine.demo.graphics.FlatShade;
import jinngine.demo.graphics.Graphics;
import jinngine.demo.graphics.Hull;
import jinngine.demo.graphics.Render;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Box;
import jinngine.physics.CompositeBody;
import jinngine.physics.Model;
import jinngine.physics.force.GravityForce;

public class Cube implements Entity {
	private final CompositeBody body;
	private boolean alarmed = false;
	
	public Cube( Graphics m, Vector3 position, double mass, double radius ) {
		Render render = m.getRender();
		Model model = m.getModel();		
		body = new CompositeBody();
			
		List<Vector3> points = new LinkedList<Vector3>();

		//just a box			
		points.add( new Vector3( 1, 1, 1 ).multiply(radius).multiply(0.5));
		points.add( new Vector3( -1, 1, 1 ).multiply(radius).multiply(0.5));
		points.add( new Vector3( 1, -1, 1 ).multiply(radius).multiply(0.5));
		points.add( new Vector3( -1, -1, 1 ).multiply(radius).multiply(0.5));
		points.add( new Vector3( 1, 1, -1 ).multiply(radius).multiply(0.5));
		points.add( new Vector3( -1, 1, -1 ).multiply(radius).multiply(0.5));
		points.add( new Vector3( 1, -1, -1 ).multiply(radius).multiply(0.5));
		points.add( new Vector3( -1, -1, -1 ).multiply(radius).multiply(0.5));

		Hull shape = new Hull(points.iterator());
		shape.setAuxiliary(this);

		//ask render to draw this shape
		render.addShape( new FlatShade(), shape, body.state.transform, this);

		//setup the physics
		body.addGeometry(shape, new Matrix3().identity(),new Vector3(),mass);		
		body.finalize();
		body.setPosition(position);
		model.addForce(new GravityForce(body,1));
		model.addBody(body);
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