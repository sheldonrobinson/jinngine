package jinngine.test;

import java.util.ArrayList;
import java.util.List;

import jinngine.math.Vector3;
import jinngine.physics.*;
import jinngine.physics.constraint.HingeJoint;
import jinngine.physics.force.GravityForce;
import jinngine.util.Pair;


	public class Misc {
		public static void main(String arg[]) {
			Model model = new Engine();
			model.setDt(0.10);

			Box cube = new Box(5,5,15);
			cube.setMass(55);
			//cube.setAngularVelocity(new Vector3(0.1,0,0.1));
			cube.setPosition(new Vector3(0,-8,0));
			model.addBody(cube);
			model.addForce( new GravityForce(cube,1.0));
			
			Box seesaw = new Box(30,4,16);
			seesaw.setMass(15);
			seesaw.setPosition(new Vector3(0.199,4,0));
			model.addBody(seesaw);
			model.addForce( new GravityForce(seesaw,1.0));
			
			//model.addConstraint(new Pair(cube,seesaw), new HingeJoint(cube,seesaw, new Vector3(0,-6,0), new Vector3(0,0,1)) );
			
			Box weight = new Box(4,4,4);
			weight.setPosition(new Vector3(10,6,0));
			weight.setMass(1.5);			
			model.addBody(weight);
			model.addForce( new GravityForce(weight,1.0));
			
			Box weight3 = new Box(4,4,4);
			weight3.setPosition(new Vector3(7,11,0));
			model.addBody(weight3);
			model.addForce( new GravityForce(weight3,1.0));
			
			Box weight2 = new Box(4,4,4);
			weight2.setMass(1.5);
			weight2.setPosition(new Vector3(-10,1,0));
			model.addBody(weight2);
			model.addForce( new GravityForce(weight2,1.0));	

			Box weight5 = new Box(8,8,8);
			weight5.setMass(1);
			weight5.setPosition(new Vector3(-5,15,0));
			model.addBody(weight5);
			model.addForce( new GravityForce(weight5,1.0));	

			Box weight6 = new Box(8,8,8);
			weight6.setMass(32423);
			weight6.setPosition(new Vector3(5,-5,-190));
			model.addBody(weight6);
			weight6.setVelocity(new Vector3(0,0,1.5));
			//model.addForce( new GravityForce(weight5,1.0));	

			
			Box table = new Box(120,1+40,120);
			table.setPosition( new Vector3(0,-13-20,0));
			table.setMass(9e9);
			table.setFixed(true);
			table.advancePositions(1);
			model.addBody(table);
			
			
			
			// Use the visualiser to run the configuration
			List<Body> boxes = new ArrayList<Body>();
			boxes.add(cube);
			boxes.add(table);
			boxes.add(seesaw);
			boxes.add(weight);
			boxes.add(weight2);
			boxes.add(weight3);
			boxes.add(weight5);
			boxes.add(weight6);

			//build a wall
			for (int i=0; i<3; i++) {
				for (int j=0; j<2; j++) {
					Box b = new Box(8,4,(7-j));
					b.setPosition(new Vector3(i*9 +(j%2)*4,-9+j*5 ,-30));
					b.setMass(5);			
					model.addBody(b);
					model.addForce( new GravityForce(b,1.0));
					boxes.add(b);
				}
			}
			
			
			new BoxVisualisor(model, boxes,1).start();
			
		}
	}


