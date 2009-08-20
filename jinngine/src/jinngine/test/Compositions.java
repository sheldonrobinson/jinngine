package jinngine.test;

import java.util.ArrayList;
import java.util.List;

import jinngine.geometry.*;
import jinngine.math.*;
import jinngine.physics.*;
import jinngine.physics.force.*;

public class Compositions implements Testcase {
	// Use the visualiser to run the configuration
	List<Body> bodies = new ArrayList<Body>();
	private int dimention = 7;
	private double dt;
	
	public Compositions(int dimention, double dt) {
		this.dimention = dimention;
		this.dt = dt;
	}

	@Override
	public void deleteScene(Model model) {
		for (Body b:bodies) {
			model.removeBody(b);
		}
		
		bodies.clear();
	}

	@Override
	public void initScene(Model model) {
		//parameters
		model.setDt(dt);

		Body table = new Body( new Box(220,1,120));
		table.setPosition( new Vector3(0,-13,0));
		table.setFixed(true);
		table.advancePositions(1);
		model.addBody(table);
		bodies.add(table);	

		List<Geometry> spheres = new ArrayList<Geometry>();
		
		//build a stack
		for (int i=0; i<dimention; i++) {
			for (int j=0; j<dimention; j++) {
				for ( int k=0; k<j; k++) {
					Geometry b;
					if (i%2 == 0)
						 b = new Sphere(i*1.5+1);
					else {
						double sides = i*1.5+1;
						 b = new Box(sides,sides,sides);
					}
						
					b.setLocalTransform(Matrix3.identity(), new Vector3(-40+i*12,j*12 ,k*12));
					spheres.add(b);
				}
			}
		}
		
		Body b = new Body(spheres.iterator());
		b.setPosition(new Vector3(0,32,0));
		model.addBody(b);
		model.addForce( new GravityForce(b,1.0));
		//model.addForce( new LinearDragForce(b,5.5));
		bodies.add(b);

	}

	public static void main(String arg[]) {
		Model model = new Engine();
		ThinWall test = new ThinWall(7, 0.05);
		test.initScene(model);			
		new BoxVisualisor(model, test.boxes, 1).start();
		
	}

}
