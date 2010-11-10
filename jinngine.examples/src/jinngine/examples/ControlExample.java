/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.examples;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import jinngine.collision.SAP2;
import jinngine.geometry.Box;
import jinngine.geometry.UniformCapsule;
import jinngine.math.Matrix3;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.*;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
import jinngine.physics.solver.Solver.NCPConstraint;
import jinngine.rendering.Rendering;
import jinngine.rendering.Rendering.EventCallback;
import jinngine.util.Pair;

public class ControlExample implements Rendering.Callback {	
	private final Scene scene;
	
	public ControlExample() {		
		// setup jinngine 
		scene = new DefaultScene(
				new SAP2(), 
				new NonsmoothNonlinearConjugateGradient(44), 
				new DisabledDeactivationPolicy());
		
		scene.setTimestep(0.09);
		
		// add a fixed floor to the scene 
		Box floor = new Box("floor",1500,20,1500);
		scene.addGeometry(Matrix3.identity(), new Vector3(0,-30,0), floor);
		scene.fixBody(floor.getBody(), true);
					
		// setup the rendering
		Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);

		// setup androidish figure using capsule shapes
		final Body figure = new Body("Figure");
		scene.addBody(figure);
		UniformCapsule head = new UniformCapsule("head", 1, 1);
		scene.addGeometry(figure, Quaternion.rotation(Math.PI/2, Vector3.i()).toRotationMatrix3(), new Vector3(-20,-10,-25), head);
		rendering.drawMe(head);
		UniformCapsule arm1 = new UniformCapsule("arm", 0.5, 0.5);
		scene.addGeometry(figure, Quaternion.rotation(Math.PI/2, Vector3.i()).toRotationMatrix3(), new Vector3(-21,-10,-25), arm1);
		rendering.drawMe(arm1);
		UniformCapsule arm2 = new UniformCapsule("arm", 0.5, 0.5);
		scene.addGeometry(figure, Quaternion.rotation(Math.PI/2, Vector3.i()).toRotationMatrix3(), new Vector3(-19,-10,-25), arm2);
		rendering.drawMe(arm2);
		UniformCapsule foot1 = new UniformCapsule("arm", 0.7, 0.5);
		scene.addGeometry(figure, Matrix3.identity(), new Vector3(-19.5,-11.2,-25), foot1);
		rendering.drawMe(foot1);
		UniformCapsule foot2 = new UniformCapsule("arm", 0.7, 0.5);
		scene.addGeometry(figure, Matrix3.identity(), new Vector3(-20.5,-11.2,-25), foot2);
		rendering.drawMe(foot2);
		scene.addForce( new GravityForce(figure));
		
		// remove restitution from feet
		foot1.setRestitution(0.1);
		foot2.setRestitution(0.1);
		
		// only allow Yaw motion for the figure. By doing this, the figure becomes
		// "Infinitely" hard to move along two of the angular axes. So it will stay 
		// locked on those. 
		Matrix3.multiply( Matrix3.identity().scale(new Vector3(0,1,0)), 
				figure.state.inverseinertia,
				figure.state.inverseinertia);

		// create a fixed body to move the figure around with. This body will just 
		// stay still all the time, so its just there to connect the constraint with it. 
		final Body majonet = new Body("Majonet");
		scene.addBody(majonet);
		scene.fixBody(majonet, true);

		// create a list of constraints, needed for the 
		// control constraint. They are only here because we
		// can't define a constructor in the control Constraint below
		final NCPConstraint linear1 = new NCPConstraint();
		final NCPConstraint linear2 = new NCPConstraint();
		final NCPConstraint linear3 = new NCPConstraint();
		final NCPConstraint angular1 = new NCPConstraint();
		
		// list needed for the constraint below
		final List<NCPConstraint> ncps = new ArrayList<NCPConstraint>();
		ncps.add(linear1);
		ncps.add(linear2);
		ncps.add(angular1);
		
		// the target figure velocity
		final Vector3 targetVelocity = new Vector3();

		// create constraint controller for figure
		Constraint control = new Constraint() {
			public Iterator<NCPConstraint> getNcpConstraints() { return ncps.iterator(); }			
			public Pair<Body> getBodies() { return new Pair<Body>(figure,majonet); }
			
			public void applyConstraints(ListIterator<NCPConstraint> iterator, double dt) {
				Vector3 u = figure.state.velocity.sub(targetVelocity);
				
				// the next two constraints takes care of the movement in the XZ plane. They
				// use the targetVelocity, but allow half the amount for force. Having the 
				// force limit set too high results in the figure "bulldozing" through the
				// world, possible creating blow up effects. However, the constraint ensures
				// that we use exactly the force needed to get to the desired velocity.
				linear1.assign(figure, majonet, 
        				new Vector3(1,0,0), new Vector3(), new Vector3(), new Vector3(),
        				new Vector3(1,0,0), new Vector3(), new Vector3(), new Vector3(), 
        				Math.min(targetVelocity.x*0.5,0), Math.max(targetVelocity.x*0.5,0), null, u.x, 0);
				iterator.add(linear1);
				
				linear2.assign(figure, majonet, 
        				new Vector3(0,0,1), new Vector3(), new Vector3(), new Vector3(),
        				new Vector3(0,0,1), new Vector3(), new Vector3(), new Vector3(), 
        				Math.min(targetVelocity.z*0.5,0), Math.max(targetVelocity.z*0.5,0), null, u.z, 0);
				iterator.add(linear2);

				// this constraint asks for a body velocity going upwards. The maximum 
				// force is the same as the velocity. The lower limit of the force is 0,
				// so it can never "pull" the figure down
				linear3.assign(figure, majonet, 
        				new Vector3(0,1,0), new Vector3(), new Vector3(), new Vector3(),
        				new Vector3(0,1,0), new Vector3(), new Vector3(), new Vector3(), 
        				0, Math.max(targetVelocity.y,0), null, u.y, 0);
				iterator.add(linear3);
				
				// remove the jump target velocity when "used"
				if (targetVelocity.y>0)
					targetVelocity.y=0;
												
				// prevent spinning. Since we completely removed angular freedom of the
				// body, some unintended artifacts can occur in relation to contacts. It can set 
				// a body "spinning" on a single contact point, like a base ball on a finger.  This 
				// is expected, and we counter it by applying torque that counters the movement. We limit
				// the allowed torque so that the figure is not completely locked in rotation.
				angular1.assign(figure, majonet, 
						new Vector3(), figure.state.inverseinertia.multiply(new Vector3(0,1,0)), new Vector3(), new Vector3(), 
						new Vector3(), new Vector3(0,1,0), new Vector3(), new Vector3(0,0,0),
						-0.9, 0.9,
						null,
						figure.state.omega.sub(majonet.state.omega).y, 0  );	
				iterator.add(angular1);
			}
		};
		
		// add the control constraint to the scene
		scene.addConstraint(control);
		
		// create a callback to receive input from view. We communicate 
		// with the physics through the targetVelocity vector. The control
		// constraints looks at this vector, ask the solver to give us the
		// desired motion. The controls are WASD, plus space for jumping
		rendering.addCallback( new EventCallback() {
			public void spaceReleased() {}			
			public void spacePressed() {}
			public void mousePressed(double x, double y, Vector3 point, Vector3 direction) {}
			public void mouseDragged(double x, double y, Vector3 point,Vector3 direction) {}
			public void mouseReleased() {}
			public void enterPressed() {}

			public void keyPressed(char key) {
//				System.out.println("got key="+key);
				switch (key) {
				case 'w': targetVelocity.z -=3; break;
				case 's': targetVelocity.z +=3; break;
				case 'a': targetVelocity.x -=3; break;
				case 'd': targetVelocity.x +=3; break;
				case ' ': targetVelocity.y +=9; break;
				}
			}
			@Override
			public void keyReleased(char key) {
				switch (key) {
				case 'w': targetVelocity.z +=3; break;
				case 's': targetVelocity.z -=3; break;
				case 'a': targetVelocity.x +=3; break;
				case 'd': targetVelocity.x -=3; break;
				}
			}
		});
		
		// create the static world grid
		Body grid = new Body("Grid");
		scene.addBody(grid);
		
		// make a grid of static boxes
		for (int i=0;i<7;i++) {
			for (int j=0;j<7;j++) {
				final double sigma = 0.5;
				int height = (int)(7*Math.exp( -((((i-4.0)*(i-4.0))/2*sigma*sigma)+(((j-4.0)*(j-4.0))/2*sigma*sigma)) ) );
				Box box = new Box("box", 3,3,3);
				scene.addGeometry(grid, Matrix3.identity(), new Vector3(3*i-15,-18.1+height,3*j-35), box);
				rendering.drawMe(box);
			}
		}

		// rotate the grid world a bit
		grid.state.orientation.assign(Quaternion.rotation(Math.PI*.25, Vector3.j()));
		
		// fix the grid
		scene.fixBody(grid, true);

		// create a box for androidish figure to play with
		Box box = new Box("box", 3,4,3);
		scene.addGeometry(Matrix3.identity(), new Vector3(-19,-9.1,-20), box);
		scene.addForce( new GravityForce(box.getBody()));		
		rendering.drawMe(box);
		
		// disable interaction due to a bug in it 
        // rendering.addCallback(new Interaction(scene));
		rendering.createWindow();
		
		// go go go :) 
		rendering.start();
	}

	@Override
	public void tick() {
		// each frame, to a time step on the Scene
		scene.tick();
	}
	
	public static void main( String[] args) {
		new ControlExample();
	}
}
