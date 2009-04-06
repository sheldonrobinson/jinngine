package jinngine.test;

import java.util.ArrayList;
import java.util.List;

import jinngine.math.*;
import jinngine.physics.*;
import jinngine.physics.force.*;

public class Bounce {

	public static void main(String arg[]) {
		Model model = new Engine();
		model.setDt(0.02);

		Box cube = new Box(8,8,8);
		cube.setAngularVelocity(new Vector3(0.1,0,0.1));
		model.addBody(cube);
		model.addForce( new GravityForce(cube,1.0));

		Box table = new Box(50,1,50);
		table.setPosition( new Vector3(0,-17,0));
		table.setMass(9e9);
		model.addBody(table);
		
		// Use the visualiser to run the configuration
		List<Body> boxes = new ArrayList<Body>();
		boxes.add(cube);
		boxes.add(table);
		
		new BoxVisualisor(model, boxes).start();
		
	}
}
