/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.vriphys2010;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jinngine.collision.SAP2;
import jinngine.geometry.Box;
import jinngine.geometry.ConvexHull;
import jinngine.geometry.Geometry;
import jinngine.geometry.UniformCapsule;
import jinngine.math.Matrix3;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.*;
import jinngine.physics.constraint.contact.FrictionalContactConstraint;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
import jinngine.physics.solver.ProjectedGaussSeidel;
import jinngine.rendering.Interaction;
import jinngine.rendering.Rendering;
import jinngine.rendering.Rendering.EventCallback;
import jinngine.rendering.Rendering.TaskCallback;
import jinngine.rendering.jogl.JoglRendering;

public class Vriphys2010demo implements Rendering.Callback {	
	private final DefaultScene scene;
	
	public Vriphys2010demo() {
		// start jinngine 
		scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(60), new DisabledDeactivationPolicy());
		scene.setTimestep(0.1);
		
		
//		Box back = new Box("back", 200, 200, 20);		
//		scene.addGeometry(Matrix3.identity(), new Vector3(0,0,-55), back);
//		scene.fixBody(back.getBody(), true);
//
//		Box front = new Box("front", 200, 200, 20);		
//		scene.addGeometry( Matrix3.identity(), new Vector3(0,0,-7), front);
//		scene.fixBody(front.getBody(), true);
//
//		Box left = new Box("left", 20, 200, 200);		
//		scene.addGeometry(Matrix3.identity(), new Vector3(-35,0,0), left);
//		scene.fixBody(left.getBody(), true);
//
//		Box right = new Box("right", 20, 200, 200);		
//		scene.addGeometry(Matrix3.identity(), new Vector3(10,0,0), right);
//		scene.fixBody( right.getBody(), true);

		
		// handle drawing
		final Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);
		rendering.addCallback(new Interaction(scene));

		JoglRendering.Solver = "NNCG60";
		JoglRendering.Normals = "normal stabl.";
		JoglRendering.Friction = "friction stabl.";
		JoglRendering.Timestep = "dt 0.1";


		final TaskCallback setBigSolver = new TaskCallback() {			
			@Override
			public void doTask() {
				scene.solver = new NonsmoothNonlinearConjugateGradient(60);
				JoglRendering.Solver = "NNCG60";
			}
		};
		
		final TaskCallback setMediumSolver = new TaskCallback() {			
			@Override
			public void doTask() {
				scene.solver = new NonsmoothNonlinearConjugateGradient(30);
				JoglRendering.Solver = "NNCG30";
			}
		};

		final TaskCallback setSmallSolver = new TaskCallback() {			
			@Override
			public void doTask() {
				scene.solver = new ProjectedGaussSeidel(15,Double.POSITIVE_INFINITY);
				JoglRendering.Solver = "PGS15";
			}
		};

		
		final TaskCallback reduceTimestep = new TaskCallback() {			
			@Override
			public void doTask() {
				double dt = scene.getTimestep();
				scene.setTimestep(dt/10);
				System.out.println("dt="+scene.getTimestep());
				JoglRendering.Timestep = "dt "+scene.getTimestep();

			}
		};

		final TaskCallback increaseTimestep = new TaskCallback() {			
			@Override
			public void doTask() {
				double dt = scene.getTimestep();
				scene.setTimestep(dt*10>0.1?0.1:dt*10);
				System.out.println("dt="+scene.getTimestep());
				
				JoglRendering.Timestep = "dt "+scene.getTimestep();

			}
		};

		// create clean up task
		final TaskCallback cleanScene = new TaskCallback() {			
			@Override
			public void doTask() {
				Iterator<Body> iter = scene.getBodies();
				
				// move bodies to an intermediate list
				List<Body> bodies = new ArrayList<Body>();
				while (iter.hasNext()) 
					bodies.add(iter.next());
				
				// go through list and remove all bodies
				for (Body bi: bodies) {
				
					// remove geoemtry from visualisation
					Iterator<Geometry> geos = bi.getGeometries();
					while(geos.hasNext()) {
						rendering.dontDrawMe(geos.next());
					}
					
					// remove body (and geometries) from scene
					scene.removeBody(bi);
				}				
			}
		};
		
		
		final TaskCallback makeAndroidPile = new TaskCallback() {
			@Override
			public void doTask() {

				// add boxes to bound the world
				Box floor = new Box("floor",1500,20,1500);
				scene.addGeometry(Matrix3.identity(), new Vector3(0,-30,0), floor);
				scene.fixBody(floor.getBody(), true);

				for (int j=0; j<2; j++) 
					for (int i=0; i<2; i++) {
						// setup androidish figure using capsule shapes
						final Body figure = new Body("Figure");
						scene.addBody(figure);
						UniformCapsule head = new UniformCapsule("head", 1, 1);
						scene.addGeometry(figure, Quaternion.rotation(Math.PI/2, Vector3.i()).toRotationMatrix3(), new Vector3(-20,-10,-25), head);
						rendering.drawMe(head);
						UniformCapsule arm1 = new UniformCapsule("arm", 0.5, 0.5);
						scene.addGeometry(figure, Quaternion.rotation(Math.PI/2.5, Vector3.i()).toRotationMatrix3(), new Vector3(-21,-10,-25), arm1);
						rendering.drawMe(arm1);
						UniformCapsule arm2 = new UniformCapsule("arm", 0.5, 0.5);
						scene.addGeometry(figure, Quaternion.rotation(Math.PI/2.5, Vector3.i()).toRotationMatrix3(), new Vector3(-19,-10,-25), arm2);
						rendering.drawMe(arm2);
						UniformCapsule foot1 = new UniformCapsule("arm", 0.5, 0.5);
						scene.addGeometry(figure, Matrix3.identity(), new Vector3(-19.5,-11.2,-25), foot1);
						rendering.drawMe(foot1);
						UniformCapsule foot2 = new UniformCapsule("arm", 0.5, 0.5);
						scene.addGeometry(figure, Matrix3.identity(), new Vector3(-20.5,-11.2,-25), foot2);
						rendering.drawMe(foot2);
						scene.addForce( new GravityForce(figure));

						figure.setPosition(-25+4*i, -19, -21+4*j);
					}
				
				// add big heavy box
				Box bigbox = new Box("lkj", 7,7,7);
				scene.addGeometry(Matrix3.identity(), new Vector3(-20.5,-11.2,-27), bigbox );
				scene.addForce( new GravityForce(bigbox.getBody()));

				rendering.drawMe(bigbox);

			}
		};
		
		
		// create setup rotunda task
		final TaskCallback makeRotunda = new TaskCallback() {
			@Override
			public void doTask() {
				
				// add boxes to bound the world
				Box floor = new Box("floor",1500,20,1500);
				scene.addGeometry(Matrix3.identity(), new Vector3(0,-30,0), floor);
				scene.fixBody(floor.getBody(), true);
			
				//	 add some boxes (rotunda)
				for (int j=0;j<4; j++) {
					double theta = (j%2 )*Math.PI*2/12/2;
					//			Body boxn = new Body( "box"+j );
					//			scene.addBody(boxn);

					for (int i=0;i<12;i++){
						double x = Math.sin(theta) * 10;
						double y = Math.cos(theta) * 10;
						//				double x = theta * 15;
						//				double y = theta * 15;

						Box boxgeometryn = new Box("box",4,2,2);
						boxgeometryn.setFrictionCoefficient(0.5);
						scene.addGeometry( Quaternion.rotation(theta,Vector3.j()).toRotationMatrix3(),  new Vector3(-15+x,-19+j*2.05,-31+y+10), boxgeometryn);
						scene.addForce( new GravityForce(boxgeometryn.getBody()));

						rendering.drawMe(boxgeometryn);		

						theta = theta+2*Math.PI/12;
					}
				}				
			}
		}; 
		
		
		// create setup rotunda task
		final TaskCallback makeStack = new TaskCallback() {
			@Override
			public void doTask() {

				// add boxes to bound the world
				Box floor = new Box("floor",1500,20,1500);
				scene.addGeometry(Matrix3.identity(), new Vector3(0,-30,0), floor);
				scene.fixBody(floor.getBody(), true);

				for (int i=0;i<6;i++){
					Box boxgeometryn = new Box("box",4,4,4);
					boxgeometryn.setFrictionCoefficient(0.5);
					scene.addGeometry( Quaternion.rotation(0.2*i,Vector3.j()).toRotationMatrix3(),  new Vector3(-15,-18+i*4.1,-21), boxgeometryn);
					scene.addForce( new GravityForce(boxgeometryn.getBody()));

					rendering.drawMe(boxgeometryn);		
				}
			}
		}; 
		
		
		// create arch setup task
		final TaskCallback makeArch = new TaskCallback() {
			@Override
			public void doTask() {
				
				// add boxes to bound the world
				Box floor = new Box("floor",1500,20,1500);
				scene.addGeometry(Matrix3.identity(), new Vector3(0,-30,0), floor);
				scene.fixBody(floor.getBody(), true);

				
				// make arch
//				int N = 25;
//				int M = 25;
//				double theta = -Math.PI/2;
//				double dtheta = Math.PI/N;
//				double dthetaOffset1 = dtheta*0.855;
//				double dthetaOffset2 = dtheta*0.912;

				// second config
				int N = 15;
				int M = 15;
				double theta = -Math.PI/2;
				double dtheta = Math.PI/N;
				double dthetaOffset1 = dtheta*0.94;
				double dthetaOffset2 = dtheta*0.97;

				for (int j=0;j<M; j++) {
					final double xp = -12;
					final double yp = -19.97;
					final double r1 = 6.5;
					final double r2 = 11.0;
					final double zoff = (j%2)*0.5*0;

					double x11 = Math.sin(theta) * r1 +xp;
					double y11 = Math.cos(theta) * r1 +yp;
					double x12 = Math.sin(theta) * r2 +xp;
					double y12 = Math.cos(theta) * r2 +yp;

					double x21 = Math.sin(theta+dthetaOffset1) * r1 +xp;
					double y21 = Math.cos(theta+dthetaOffset1) * r1 +yp;
					double x22 = Math.sin(theta+dthetaOffset2) * r2 +xp;
					double y22 = Math.cos(theta+dthetaOffset2) * r2 +yp;

					List<Vector3> list = new ArrayList<Vector3>();
					//front side
					list.add(new Vector3(x11,y11,-24+zoff));
					list.add(new Vector3(x12,y12,-24+zoff));
					list.add(new Vector3(x21,y21,-24+zoff));
					list.add(new Vector3(x22,y22,-24+zoff));

					list.add(new Vector3(x11,y11,-21+zoff));
					list.add(new Vector3(x12,y12,-21+zoff));
					list.add(new Vector3(x21,y21,-21+zoff));
					list.add(new Vector3(x22,y22,-21+zoff));


					ConvexHull g = new ConvexHull("brick",list);
					g.setEnvelope(0.170);
					g.setRestitution(0.5);

					scene.addGeometry(Matrix3.identity(), new Vector3(), g);
					scene.addForce( new GravityForce(g.getBody()));
					rendering.drawMe(g);

					theta += dtheta;

				}	
			}
		};
		
		final TaskCallback toggleFrictionStabilisation = new TaskCallback() {
			private boolean friction = true;
			@Override
			public void doTask() {
				
				// turn parameter
				friction=friction?false:true;
				
				System.out.println("friction " + friction);
				
				if (friction) {
					JoglRendering.Friction = "friction stabl.";
					FrictionalContactConstraint.frictionStabilisation = 1.0;
				} else {
					JoglRendering.Friction = "";
					FrictionalContactConstraint.frictionStabilisation = 0.0;					
				}
			}
		};

		final TaskCallback toggleNormalStabilisation = new TaskCallback() {
			private boolean normalCorrection = true;
			@Override
			public void doTask() {
				
				// turn parameter
				normalCorrection=normalCorrection?false:true;
				
				System.out.println("friction " + normalCorrection);
				
				if (normalCorrection) {
					JoglRendering.Normals = "normal stabl.";
					FrictionalContactConstraint.normalStabilisation = 1.0;
				} else {
					JoglRendering.Normals = "";
					FrictionalContactConstraint.normalStabilisation = 0.0;					
				}
			}
		};

		
		// create arch setup task
		final TaskCallback makeArch2 = new TaskCallback() {
			@Override
			public void doTask() {
				
				// add boxes to bound the world
				Box floor = new Box("floor",1500,20,1500);
				scene.addGeometry(Matrix3.identity(), new Vector3(0,-30,0), floor);
				scene.fixBody(floor.getBody(), true);

				
				// make arch
				int N = 25;
				int M = 25;
				double theta = -Math.PI/2;
				double dtheta = Math.PI/N;
				double dthetaOffset1 = dtheta*0.855;
				double dthetaOffset2 = dtheta*0.912;

				// second config
//				int N = 12;
//				int M = 12;
//				double theta = -Math.PI/2;
//				double dtheta = Math.PI/N;
//				double dthetaOffset1 = dtheta*0.94;
//				double dthetaOffset2 = dtheta*0.97;

				for (int j=0;j<M; j++) {
					final double xp = -12;
					final double yp = -19.97;
					final double r1 = 6.5;
					final double r2 = 11.0;
					final double zoff = (j%2)*0.5*0;

					double x11 = Math.sin(theta) * r1 +xp;
					double y11 = Math.cos(theta) * r1 +yp;
					double x12 = Math.sin(theta) * r2 +xp;
					double y12 = Math.cos(theta) * r2 +yp;

					double x21 = Math.sin(theta+dthetaOffset1) * r1 +xp;
					double y21 = Math.cos(theta+dthetaOffset1) * r1 +yp;
					double x22 = Math.sin(theta+dthetaOffset2) * r2 +xp;
					double y22 = Math.cos(theta+dthetaOffset2) * r2 +yp;

					List<Vector3> list = new ArrayList<Vector3>();
					//front side
					list.add(new Vector3(x11,y11,-24+zoff));
					list.add(new Vector3(x12,y12,-24+zoff));
					list.add(new Vector3(x21,y21,-24+zoff));
					list.add(new Vector3(x22,y22,-24+zoff));

					list.add(new Vector3(x11,y11,-21+zoff));
					list.add(new Vector3(x12,y12,-21+zoff));
					list.add(new Vector3(x21,y21,-21+zoff));
					list.add(new Vector3(x22,y22,-21+zoff));


					ConvexHull g = new ConvexHull("brick",list);
					g.setEnvelope(0.170);
					g.setRestitution(0.5);

					scene.addGeometry(Matrix3.identity(), new Vector3(), g);
					scene.addForce( new GravityForce(g.getBody()));
					rendering.drawMe(g);

					theta += dtheta;

				}	
			}
		};

		

		// setup the keyhandler
		rendering.addCallback(new EventCallback() {			
			@Override
			public void spaceReleased() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void spacePressed() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseReleased() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(double x, double y, Vector3 point,
					Vector3 direction) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDragged(double x, double y, Vector3 point,
					Vector3 direction) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(char key) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(char key) {
				
				if (key=='1') {
					rendering.addTask(cleanScene);
					rendering.addTask(makeArch);
				}

				if (key=='2') {
					rendering.addTask(cleanScene);
					rendering.addTask(makeArch2);
				}
				
				if (key=='3') {
					rendering.addTask(cleanScene);
					rendering.addTask(makeRotunda);
				}

				if (key=='4') {
					rendering.addTask(cleanScene);
					rendering.addTask(makeStack);
				}

				if (key=='5') {
					rendering.addTask(cleanScene);
					rendering.addTask(makeAndroidPile);
				}

				if (key =='f') {
					rendering.addTask(toggleFrictionStabilisation);
				}

				if (key =='n') {
					rendering.addTask(toggleNormalStabilisation);
				}

				if (key == 'a') {
					rendering.addTask(setBigSolver);
				}

				if (key == 'x') {
					rendering.addTask(setMediumSolver);
				}

				if (key == 'z') {
					rendering.addTask(setSmallSolver);
				}

				
				if (key =='q')
					rendering.addTask(reduceTimestep);
				if (key == 'w')
					rendering.addTask(increaseTimestep);
					
			}
			
			@Override
			public void enterPressed() {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		rendering.createWindow();
		rendering.start();
	}

	@Override
	
	public void tick() {
		// each frame, to a time step on the Scene
		scene.tick();
	}
	
	public static void main( String[] args) {
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for(String font:e.getAvailableFontFamilyNames()){
			System.out.println(font);
		}
		
		new Vriphys2010demo();
	}

}
