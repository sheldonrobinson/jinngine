package jinngine.demo.graphics;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Engine;
import jinngine.physics.Model;

public class Graphics implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	//create physics
	Model model = new Engine();

	//create a view
	Render render = new RenderImpl(this);
	
	//some mouse control stuff
	public final Vector3 mouse = new Vector3();
	public volatile int wheel = 0;
	public volatile boolean clicked = false;
	public volatile boolean pressed = false;
	public volatile boolean spacepressed = false;
	public volatile boolean spacereleased = false;
	
	//user interface 
	List<GameState> states = new ArrayList<GameState>();
	List<GameState> incommingStates = new ArrayList<GameState>();
	
	
	public Graphics() {}
	
	public void start() {
		model.setDt(0.08);
		
		//initial state
		addState(new Selection());
				
		//start visualisation thread
		render.start();	
	}
	
	/**
	 * This is intended to be called at each frame
	 */
	double spend = 0;
	public void callback()  {
		//do amount of ticks to meet 0.12 effective time-step per frame
		double step = 0.08;
		do {
			spend += model.getDt();
			model.tick();
		} while ( spend < step);
		spend = spend % step;

		//add incomming jinngine.demos.states
		states.addAll(incommingStates);
		incommingStates.clear();
		
		ListIterator<GameState> iter = states.listIterator();
		while(iter.hasNext()) {
			GameState s = iter.next();
			if (s.done()) {
				//clean up after the state
				s.stop(this);
				iter.remove();
				continue;
			}
			
			//give a "time slice"
			s.tick(this);
		}
		
		clicked = false;
	}

	/**
	 * Add a game state
	 * @param s
	 */
	public void addState( GameState s) {
		s.start(this);
		incommingStates.add(s);	
	}
	
	public Model getModel() {
		return model;
	}

	public Render getRender() {
		return render;
	}

	
	
	public static void main(String s[]) {
		new Graphics();
	}


	@Override
	public void mouseClicked(MouseEvent e) {
		clicked = true;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.pressed = true;
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.pressed = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mouse.assign(new Vector3(e.getX(), e.getY(), 0));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouse.assign(new Vector3(e.getX(), e.getY(), 0));
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		wheel = wheel + e.getWheelRotation();
		System.out.println("wheel");
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == ' ') {
			this.spacepressed = true;
			this.spacereleased = false;
			
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyChar() == ' ') {
			this.spacepressed = false;
			this.spacereleased = true;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
