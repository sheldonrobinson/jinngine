package jinngine.test;

import java.util.ArrayList;
import java.util.List;

import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Box;
import jinngine.physics.Engine;
import jinngine.physics.Model;
import jinngine.physics.constraint.HingeJoint;
import jinngine.physics.force.GravityForce;
import jinngine.util.Pair;


public class Seesaw implements Testcase {
	
	private double dt;
	
	public Seesaw(double dt) {
		super();
		this.dt = dt;
	}

	// Use the visualiser to run the configuration
	List<Body> boxes = new ArrayList<Body>();
	
	
	@Override
	public void deleteScene(Model model) {
		for (Body b:boxes) {
			model.removeBody(b);
		}
		
		boxes.clear();
	}

	@Override
	public void initScene(Model model) {
		model.setDt(dt);

		Box cube = new Box(8,4,8);
		cube.setMass(5);
		cube.getBoxGeometry().setEnvelope(1);
		//cube.setAngularVelocity(new Vector3(0.1,0,0.1));
		cube.setPosition(new Vector3(0,-8,0));
		model.addBody(cube);
		model.addForce( new GravityForce(cube,1.0));
		
		Box seesaw = new Box(30,2,8);
		seesaw.getBoxGeometry().setEnvelope(1);
		seesaw.setMass(10);
		seesaw.setPosition(new Vector3(0,-2,0));
		model.addBody(seesaw);
		model.addForce( new GravityForce(seesaw,1.0));
		
		model.addConstraint(new Pair(cube,seesaw), new HingeJoint(cube,seesaw, new Vector3(0,-6,0), new Vector3(0,0,1)) );
		
		Box weight = new Box(4,4,4);
		weight.getBoxGeometry().setEnvelope(1);

		weight.setPosition(new Vector3(10,6,0));
		model.addBody(weight);
		model.addForce( new GravityForce(weight,1.0));
		
		Box weight3 = new Box(4,4,4);
		weight3.getBoxGeometry().setEnvelope(1);

		weight3.setPosition(new Vector3(7,11,0));
		model.addBody(weight3);
		model.addForce( new GravityForce(weight3,1.0));
		
		Box weight2 = new Box(4,4,4);
		weight2.getBoxGeometry().setEnvelope(1);
		weight2.setMass(1.5);
		weight2.setPosition(new Vector3(-10,10,0));
		model.addBody(weight2);
		model.addForce( new GravityForce(weight2,1.0));	

		Box table = new Box(50,1+40,50);
		table.getBoxGeometry().setEnvelope(2);
		table.setPosition( new Vector3(0,-13-20,0));
		table.setMass(9e9);
		model.addBody(table);
		
		// Use the visualiser to run the configuration
		boxes.add(cube);
		boxes.add(table);
		boxes.add(seesaw);
		boxes.add(weight);
		boxes.add(weight2);
		boxes.add(weight3);
				
	}

	public static void main(String arg[]) {
		Model model = new Engine();
		Seesaw test = new Seesaw(0.02);
		test.initScene(model);		
		new BoxVisualisor(model, test.boxes).start();
		
	}
}
