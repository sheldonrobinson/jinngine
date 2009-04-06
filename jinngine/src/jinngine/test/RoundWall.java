package jinngine.test;

import java.util.ArrayList;
import java.util.List;

import jinngine.math.Vector3;
import jinngine.physics.*;
import jinngine.physics.force.GravityForce;


	public class RoundWall implements Testcase {

		public RoundWall(int layers, double dt) {
			super();
			this.layers = layers;
			this.dt = dt;
		}

		// Use the visualiser to run the configuration
		List<Body> boxes = new ArrayList<Body>();
		private int layers = 5;
		private double dt = 0.05;
		
		
		
		
		
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

			
			Box table = new Box(120,1+40,120);
			table.setPosition( new Vector3(0,-13-20,0));
			table.setMass(9e9);
			table.setFixed(true);
			table.advancePositions(1);
			model.addBody(table);
			boxes.add(table);
			
	
			int stack =layers; double height = 4f; double diameter = 21.0f;
			for (int i=0;i<(stack-0) ;i++) {
				int j = 0; double delta_theta = ((double)(2*Math.PI)/12.0f);
				for (double theta=0; theta<2*Math.PI; theta+=delta_theta ) {
					j++;
	
					for ( int k=0;k<1;k++) {
						Box cu = new Box(8, height, 7);
						
						//Tetrahedron cu = new Tetrahedron(model,0.5);
						//c.setOmegaCm( new Vector( 0.00, 1.202*(-(i%2)), 0.000) );
						cu.setPosition( new Vector3( /*0.60*j+(0.3*(i%2))*/ diameter*(double)Math.sin(theta + (i%2)*0.5f*delta_theta ),  (height*1.05f)*i-9.4500f, -5.3f-0.65f*k+diameter*(double)Math.cos(theta + (i%2)*delta_theta*0.5f ) ));
						cu.setVelocity( new Vector3( 0.000f, 0.000f, 0.0000f ));
						//cu.setCubeSides( 1.2f, height, 0.62f );
						//cu.actingForce.assign(new Vector3(0,-50.5f*1.0f,0));
						model.addForce(new GravityForce(cu,1));
						cu.setMass(7-i );
						cu.state.q.assign(cu.state.q.rotation( theta+(i%2)*0.5f*delta_theta, new Vector3(0,1,0) ));
						//c.q = c.q.Rotation( Math.PI/(32.0f), new Vector(0,0,1) );
						model.addBody(cu);
						boxes.add(cu);
	
					}
				}

			}
			
		}

		public static void main(String arg[]) {
			Model model = new Engine();
			RoundWall test = new RoundWall(7, 0.05);
			test.initScene(model);
			
			new BoxVisualisor(model, test.boxes,2).start();
			
		}
	}


