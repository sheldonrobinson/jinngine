package jinngine.demo;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;


import jinngine.demo.graphics.*;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Box;
import jinngine.physics.CompositeBody;
import jinngine.physics.Model;
import jinngine.physics.constraint.HingeJoint;
import jinngine.physics.force.GravityForce;
import jinngine.util.Pair;

public class Gear implements Entity {

	private final CompositeBody b;
	private boolean alarmed = false;
	private final Render render;
	private final Model model;
	
	
	public Gear( Graphics m, Vector3 position, double mass, double radius) {
		render = m.getRender();
		model = m.getModel();
		
		double n = 7;
		double r = radius*1;
		double r2 = radius*(10/13.0);
		double r3 = radius*(8.5/13.0);
		double d = radius*(6/13.0);
		
		b = new CompositeBody();
			
		double delta = 2*Math.PI*(1/n);
		for ( double theta=0; theta<1.9*Math.PI; theta+=delta) {
			List<Vector3> points = new LinkedList<Vector3>();

			//gear tooth			
			points.add( new Vector3( r*Math.sin(theta-delta*0.20), r*Math.cos(theta-delta*0.20), -d ));
			points.add( new Vector3( r*Math.sin(theta-delta*0.20), r*Math.cos(theta-delta*0.20), d ));
			points.add( new Vector3( r*Math.sin(theta+delta*0.20), r*Math.cos(theta+delta*0.20), -d ));
			points.add( new Vector3( r*Math.sin(theta+delta*0.20), r*Math.cos(theta+delta*0.20), d ));
			
			points.add( new Vector3( r2*Math.sin(theta-delta*0.5), r2*Math.cos(theta-delta*0.5), -d ));
			points.add( new Vector3( r2*Math.sin(theta-delta*0.5), r2*Math.cos(theta-delta*0.5), d ));
			points.add( new Vector3( r2*Math.sin(theta+delta*0.5), r2*Math.cos(theta+delta*0.5), -d ));
			points.add( new Vector3( r2*Math.sin(theta+delta*0.5), r2*Math.cos(theta+delta*0.5), d ));
//			points.add( new Vector3( r3*Math.sin(theta-delta*0.25), r3*Math.cos(theta-delta*0.25), -d ));
//			points.add( new Vector3( r3*Math.sin(theta-delta*0.25), r3*Math.cos(theta-delta*0.25), d ));
//			points.add( new Vector3( r3*Math.sin(theta+delta*0.25), r3*Math.cos(theta+delta*0.25), -d ));
//			points.add( new Vector3( r3*Math.sin(theta+delta*0.25), r3*Math.cos(theta+delta*0.25), d ));

			
			//find translation
			Vector3 cm = new Vector3();
			for (Vector3 p: points) {
				cm.assign(cm.add(p.multiply(1/(double)points.size())));
			}
			
			//translate points
			for (Vector3 p: points) {
				p.assign(p.minus(cm));
			}
			
			Hull shape = new Hull(points.iterator());
			shape.setAuxiliary(this);
			b.addGeometry(shape, new Matrix3().identity(),cm,mass/n);
			
			render.addShape( new FlatShade(), shape, b.state.transform, this);
			
		}
		
		b.finalize();		
		b.setPosition(position);
		model.addForce(new GravityForce(b,1));
		model.addBody(b);

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
