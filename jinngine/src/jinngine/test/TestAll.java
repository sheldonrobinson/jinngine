package jinngine.test;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import jinngine.physics.Engine;
import jinngine.physics.Model;

public class TestAll implements KeyListener {

	private Testcase currentTest = null;
	private Engine model = new Engine();
	private BoxVisualisor visual = new BoxVisualisor(model, model.bodies, 1);
	private Queue<Testcase> tests = new LinkedList<Testcase>();
	
	
	@Override
	public void keyPressed(KeyEvent e) {
	
		//remove current testcase and install the next
		currentTest.deleteScene(model);
		
		//poll test and start
		currentTest = tests.poll();
		currentTest.initScene(model);			
	}



	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public TestAll(double dt) {
		//add all testcases
		
		
		//bounce test
		tests.add(new Bounce(0.01));
		tests.add(new Bounce(0.02));
		tests.add(new Bounce(0.04));
		tests.add(new Bounce(0.08));
		tests.add(new Bounce(0.12));
		
		
		//seesaw test
		tests.add(new Seesaw(0.01));
		tests.add(new Seesaw(0.02));
		tests.add(new Seesaw(0.04));
		tests.add(new Seesaw(0.08));
		tests.add(new Seesaw(0.12));
		
		//round
		tests.add(new Oblong( 0.01));
		tests.add(new Oblong( 0.05));
		tests.add(new Oblong( 0.08));
		tests.add(new Oblong( 0.12));
		
		//thinwall test
		tests.add(new SphereStack(2, 0.01));
		tests.add(new SphereStack(3, 0.01));
		tests.add(new SphereStack(4, 0.01));

		tests.add(new SphereStack(2, 0.04));
		tests.add(new SphereStack(3, 0.04));
		tests.add(new SphereStack(4, 0.04));

		tests.add(new SphereStack(2, 0.12));
		tests.add(new SphereStack(3, 0.12));
		tests.add(new SphereStack(4, 0.12));

		

		//thinwall test
		tests.add(new ThinWall(2, 0.01));
		tests.add(new ThinWall(3, 0.01));
		tests.add(new ThinWall(6, 0.01));
		tests.add(new ThinWall(8, 0.01));

		tests.add(new ThinWall(2, 0.05));
		tests.add(new ThinWall(3, 0.05));
		tests.add(new ThinWall(6, 0.05));
		tests.add(new ThinWall(8, 0.05));

		tests.add(new ThinWall(2, 0.12));
		tests.add(new ThinWall(3, 0.12));
		tests.add(new ThinWall(6, 0.12));
		tests.add(new ThinWall(8, 0.12));

		//round wall 
		tests.add(new RoundWall(1, 0.01));
		tests.add(new RoundWall(2, 0.01));
		tests.add(new RoundWall(4, 0.01));
		tests.add(new RoundWall(7, 0.01));
		
		tests.add(new RoundWall(1, 0.05));
		tests.add(new RoundWall(2, 0.05));
		tests.add(new RoundWall(4, 0.05));
		tests.add(new RoundWall(7, 0.05));

		tests.add(new RoundWall(1, 0.12));
		tests.add(new RoundWall(2, 0.12));
		tests.add(new RoundWall(4, 0.12));
		tests.add(new RoundWall(7, 0.12));
		

		
		
		//install first test
		currentTest = tests.poll();
		currentTest.initScene(model);

		//let visualiser run the model
		visual.addKeyListener(this);
		visual.start();
	}
	
	
	
	public static void main(String[] s){
		new TestAll(0.01);
	}
	

}
