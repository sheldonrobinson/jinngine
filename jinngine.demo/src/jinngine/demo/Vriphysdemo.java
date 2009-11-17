package jinngine.demo;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import jinngine.demo.graphics.Graphics;
import jinngine.geometry.Box;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Model;
import jinngine.physics.solver.FischerNewtonConjugateGradients;
import jinngine.physics.solver.ProjectedGaussSeidel;
import jinngine.physics.solver.SubspaceMinimization;

public class Vriphysdemo implements KeyListener{
	Graphics g = new Graphics();
	Model model = g.getModel();

	public Vriphysdemo() {
		
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

//		model.setSolver(new FischerNewtonConjugateGradients());
		model.setSolver(new ProjectedGaussSeidel(1000, false));
		g.setWindowTitle("Projected Gauss Seidel, 1000 iterations, mass ratio 1/1e6");
		
		//wee need some power for this
		model.getSolver().setMaximumIterations(24);
		
		//build a wall
//		for (int i=0; i<1; i++) {
//			for (int j=0; j<2; j++) {
//				new Cube(g, new Vector3(3,3,3), new Vector3(-17+i*(3.1) +(j%2)*0.5,-18.8+j*2.1 ,-25), 10+j*1000 );
//
//			}
//		}
		int i=0; int j=0;
		new Cube(g, new Vector3(4,3,4), new Vector3(-17+0*(3.1) +(0%2)*0.5,-18.8+j*2.1 ,-25), 10);
		i=0; j=1;
		new Cube(g, new Vector3(2,2,2), new Vector3(-17+0*(3.1) +(0%2)*0.5,-18.8+j*2.1 ,-25), 10000 );
		
		
		//start animation
		g.addKeyListener(this);
		g.start();
	}
	
	public static void main( String args[]) {
		new Vriphysdemo();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (model == null) {
			return;
		}
		
		switch (arg0.getKeyChar()) {
		case '1':
			model.setSolver(new ProjectedGaussSeidel(1000, false));
			g.setWindowTitle("Projected Gauss Seidel, 1000 iterations,  mass ratio 1/1000");
			break;
		case '2':
			model.setSolver(new FischerNewtonConjugateGradients());
			g.setWindowTitle("Fischer-Newton-CG, (max 25 outer iteration, about the same as 1125 PGS iterations) mass ratio 1/1000");
			break;
		case '3':
			model.setSolver(new SubspaceMinimization(false));
			g.setWindowTitle("PGS-CG Subspace minimization, 3 outer iterations, 15 PGS+about 20 CG in each,  mass ratio 1/1000");
			break;
		case 'w':
			
		}
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}