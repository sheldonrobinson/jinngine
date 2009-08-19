package jinngine.test;

import java.util.ArrayList;
import java.util.List;

import jinngine.geometry.Box;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Engine;
import jinngine.physics.Model;
import jinngine.physics.force.GravityForce;


public class Oblong implements Testcase {
	
	private double dt;
	
	public Oblong(double dt) {
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

		
		Body seesaw =  new Body( new Box(30,2,8) );
//		seesaw.getBoxGeometry().setEnvelope(1);
//		seesaw.setMass(10);
		seesaw.setPosition(new Vector3(0,-2,0));
		model.addBody(seesaw);
		model.addForce( new GravityForce(seesaw,1.0));
		
		Body table = new Body(new Box(50,1+40,50));
//		table.getBoxGeometry().setEnvelope(2);
		table.setPosition( new Vector3(0,-13-20,0));
//		table.setMass(9e9);
		table.setFixed(true);
		model.addBody(table);
		
		// Use the visualiser to run the configuration
		boxes.add(table);
		boxes.add(seesaw);
				
	}

	public static void main(String arg[]) {
		Model model = new Engine();
		Seesaw test = new Seesaw(0.02);
		test.initScene(model);		
		new BoxVisualisor(model, test.boxes).start();
		
	}
}