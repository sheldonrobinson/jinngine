package jinngine.test.visual;

import java.util.ArrayList;
import java.util.List;

import jinngine.geometry.Box;
import jinngine.math.*;
import jinngine.physics.*;
import jinngine.physics.force.*;

public class Bounce implements Testcase {

	private double dt;
	
	public Bounce(double dt) {
		super();
		this.dt = dt;
	}
	
	// Use the visualiser to run the configuration
	List<Body> boxes = new ArrayList<Body>();

	
	public static void main(String arg[]) {
		Model model = new Engine();
		model.setDt(0.02);

		Body cube = new Body(new Box(6,6,6));
		cube.setAngularVelocity(new Vector3(0.1,0,0.1));
		model.addBody(cube);
		model.addForce( new GravityForce(cube));

		Body table = new Body(new Box(50,1,50));
		table.setPosition( new Vector3(0,-17,0));
		table.setFixed(true);
		model.addBody(table);
		
		// Use the visualiser to run the configuration
		List<Body> boxes = new ArrayList<Body>();
		boxes.add(cube);
		boxes.add(table);
		
		new BoxVisualisor(model, boxes).start();
		
	}

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

		Body cube = new Body(new Box(24,24,5));
		cube.setAngularVelocity(new Vector3(0.1,0,0.1));
		model.addBody(cube);
		model.addForce( new GravityForce(cube));

		Body table = new Body(new Box(50,1,50));
		table.setPosition( new Vector3(0,-27,0));
		table.setFixed(true);
		model.addBody(table);

		boxes.add(cube);
		boxes.add(table);

		
	}
}
