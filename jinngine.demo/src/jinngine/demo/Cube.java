package jinngine.demo;

import java.util.LinkedList;
import java.util.List;

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

public class Cube implements Entity {
	private final Body body;
	private boolean alarmed = false;
	
	public Cube( Graphics m, Vector3 size, Vector3 position, double mass ) {
		Render render = m.getRender();
		Model model = m.getModel();		
		body = new Body();
			
		List<Vector3> points = new LinkedList<Vector3>();

		//just a box			
		points.add( new Vector3( 1, 1, 1 ).multiply(0.5));
		points.add( new Vector3( -1, 1, 1 ).multiply(0.5));
		points.add( new Vector3( 1, -1, 1 ).multiply(0.5));
		points.add( new Vector3( -1, -1, 1 ).multiply(0.5));
		points.add( new Vector3( 1, 1, -1 ).multiply(0.5));
		points.add( new Vector3( -1, 1, -1 ).multiply(0.5));
		points.add( new Vector3( 1, -1, -1 ).multiply(0.5));
		points.add( new Vector3( -1, -1, -1 ).multiply(0.5));

		Matrix4 transform = jinngine.math.Transforms.scale(size);
		for (Vector3 p: points)
			p.assign( transform.multiply(p));
		
		Hull shape = new Hull(points.iterator());
		shape.setAuxiliary(this);

		//ask render to draw this shape
		render.addShape( new FlatShade(), shape, body.state.transform, this);

		//setup the physics
		Box box = new Box(size.a1, size.a2, size.a3);
		box.setAuxiliary(this);
		
		body.addGeometry(box);		
		body.finalize();
		body.setPosition(position);
		body.sleepKinetic = 0.1;
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