package jinngine.demo;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import jinngine.demo.graphics.*;
import jinngine.geometry.Box;
import jinngine.geometry.Geometry;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Model;
import jinngine.physics.force.GravityForce;

public class Gear implements Entity {

	private final Body b;
	private boolean alarmed = false;
	private final Render render;
	private final Model model;
	
	
	public Gear( Graphics m, Vector3 position, double mass, double radius) {
		render = m.getRender();
		model = m.getModel();
		
		
		b = new Body();
			
		int k = 8; double r = 2.0;
		for ( int n=0; n<k; n++) {
			double theta = (n+1)*Math.PI*2/k;
			r = 2.0;
			//resize the drawing shape to the right dimensions
			Vector3 displacement = new Vector3(Math.cos(theta)*r, Math.sin(theta)*r,0);
			Matrix3 transform = Quaternion.toRotationMatrix3(Quaternion.rotation(-theta-Math.PI/4, Vector3.k), new Matrix3());

			Vector3 size = new Vector3(2,2,3);
			
			//Setup the physics
			Box box = new Box(size.x, size.y, size.z);
			box.setAuxiliary(this);			
			box.setLocalTransform(transform, displacement);
//			box.setMass(mass);
			b.addGeometry(box);
		}
		

		b.finalize();		
		b.setPosition(position);
		b.sleepKinetic = 0.1;
		model.addForce(new GravityForce(b));
		model.addBody(b);

		Iterator<Geometry> gi = b.getGeometries();
		while(gi.hasNext()) {
			Geometry geo = gi.next();

			//just a box for drawing
//			List<Vector3> points = new LinkedList<Vector3>();
//			points.add( new Vector3( 1, 1, 1 ).multiply(0.5));
//			points.add( new Vector3( -1, 0.333, 1 ).multiply(0.5));
//			points.add( new Vector3( 0.333, -1, 1 ).multiply(0.5));
//			points.add( new Vector3( -1, -1, 1 ).multiply(0.5));
//			points.add( new Vector3( 1, 1, -1 ).multiply(0.5));
//			points.add( new Vector3( -1, 0.3333, -1 ).multiply(0.5));
//			points.add( new Vector3( 0.333, -1, -1 ).multiply(0.5));
//			points.add( new Vector3( -1, -1, -1 ).multiply(0.5));
			List<Vector3> points = new LinkedList<Vector3>();
			points.add( new Vector3( 1, 0.333, 1 ).multiply(0.5));
			points.add( new Vector3( -1.5, 1.5, 1 ).multiply(0.5));
			points.add( new Vector3( 1, -1, 1 ).multiply(0.5));
			points.add( new Vector3( -0.333, -1, 1 ).multiply(0.5));
			points.add( new Vector3( 1, 0.333, -1 ).multiply(0.5));
			points.add( new Vector3( -1.5, 1.5, -1 ).multiply(0.5));
			points.add( new Vector3( 1, -1, -1 ).multiply(0.5));
			points.add( new Vector3( -0.333, -1, -1 ).multiply(0.5));

			//transform
			Vector3 size = ((Box)geo).getDimentions();
			Matrix3 scale = Matrix3.diagonal(size);
			Matrix3 transform = new Matrix3();
			Vector3 displacement = new Vector3();
			geo.getLocalTransform(transform, displacement);
			
			for (Vector3 p: points) {
				p.assign( transform.multiply(scale.multiply(p)).add(displacement) );
			}
			
			//create drawing shape
			Hull shape = new Hull(points.iterator());
			shape.setAuxiliary(this);
			
			render.addShape( new FlatShade(), shape, b.state.transform, this);			
		}
	}
	
	public void fix() {
		//model.addConstraint(new Pair<Body>(b,floor), new HingeJoint(b,floor, b.toWorld(new Vector3()), new Vector3(0,0,1)) );
	}
	
	@Override
	public void setPosition(Vector3 p) {
		b.setPosition(p);
	}

	@Override
	public Vector3 getPosition() {
		return b.state.rCm.copy();		
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
	public Body getPrimaryBody() {
		return b;
	}

}
