/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.test.visual;

import java.util.ArrayList;
import java.util.List;

import jinngine.geometry.Box;
import jinngine.geometry.Sphere;
import jinngine.math.Vector3;
import jinngine.physics.*;
import jinngine.physics.force.*;


	public class SphereStack implements Testcase {

		// Use the visualiser to run the configuration
		List<Body> boxes = new ArrayList<Body>();
		private int dimention = 7;
		private double dt;
		
		public SphereStack(int dimention, double dt) {
			this.dimention = dimention;
			this.dt = dt;
		}

		@Override
		public void deleteScene(PhysicsScene model) {
			for (Body b:boxes) {
				model.removeBody(b);
			}
			
			boxes.clear();
		}

		@Override
		public void initScene(PhysicsScene model) {
			//parameters
			//model.setDt(dt);

			Body table = new Body( new Box(220,1,120));
			table.setPosition( new Vector3(0,-13,0));
			table.setFixed(true);
			table.advancePositions(1);
			model.addBody(table);
			boxes.add(table);	

			//build a stack
			for (int i=0; i<dimention; i++) {
				for (int j=0; j<dimention; j++) {
					for ( int k=0; k<dimention; k++) {
						Body b = new Body( new Sphere(4) );
						b.setPosition(new Vector3(-40+i*9,j*9 ,k*9));
						//b.setMass(5);	
						//b.getGeometries().next().setEnvelope(1);
						model.addBody(b);
						model.addForce( new GravityForce(b));
						//model.addForce( new LinearDragForce(b,5.5));
						boxes.add(b);
					}
				}
			}
			
		}

		public static void main(String arg[]) {
			Engine model = new Engine();
			ThinWall test = new ThinWall(7, 0.05);
			test.initScene(model);			
			new BoxVisualisor(model, test.boxes, 1).start();
			
		}
	}


