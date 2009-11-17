package jinngine.demo;

import jinngine.demo.graphics.Graphics;
import jinngine.geometry.Box;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Model;
import jinngine.physics.solver.SubspaceMinimization;

public class Demo5 {
	public Demo5() {
		Graphics g = new Graphics();
		Model model = g.getModel();
		
		Body floor = new Body(new Box(1500,10,1500));
		floor.setPosition(new Vector3(0,-25,0));
		floor.setFixed(true);
		
		Body back = new Body( new Box(200,200,2));		
		back.setPosition(new Vector3(0,0,-45));
		back.setFixed(true);

		Body front = new Body( new Box(200,200,2));		
		front.setPosition(new Vector3(0,0,-15));
		front.setFixed(true);

		Body left = new Body( new Box(2,200,200));		
		left.setPosition(new Vector3(-25,0,0));
		left.setFixed(true);

		Body right = new Body( new Box(2,200,200));		
		right.setPosition(new Vector3(0,0,0));
		right.setFixed(true);

		model.addBody(left);
		model.addBody(right);
		model.addBody(front);
		model.addBody(floor);
		model.addBody(back);

		//wee need some power for this
		model.getSolver().setMaximumIterations(24);
		model.setSolver(new SubspaceMinimization(false));
		
		//build a wall
		for (int i=0; i<2; i++) {
			for (int j=0; j<1; j++) {
				new Sphere(g, new Vector3(0.5,2,2), new Vector3(-17+i*(3.1) +(j%2)*1.5,-18.8+j*2.1 ,-25), 1000000*i+1 );
//				new Cube(g, new Vector3(3,2,2), new Vector3(-17+i*3.1 +(j%2)*1.5*.2,-18.8+j*2.1 ,-25), 10 );

			}
		}

		//start animation
		g.start();
	}
	
	public static void main( String args[]) {
		new Demo5();
	}
}
