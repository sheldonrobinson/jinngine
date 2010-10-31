package jinngine.examples;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import jinngine.collision.SAP2;
import jinngine.geometry.Box;
import jinngine.geometry.ConvexHull;
import jinngine.geometry.UniformCapsule;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.DefaultDeactivationPolicy;
import jinngine.physics.DefaultScene;
import jinngine.physics.Scene;
import jinngine.physics.force.GravityForce;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
import jinngine.rendering.Interaction;
import jinngine.rendering.Rendering;

public class AppletExample extends Applet implements Rendering.Callback {
	private static final long serialVersionUID = 1L;
	
	private Scene scene;
	
	@Override
	public void init() {
		super.init();
		
		// start jinngine 
		scene = new DefaultScene(new SAP2(), new NonsmoothNonlinearConjugateGradient(260), new DefaultDeactivationPolicy());
		scene.setTimestep(0.1);
		
		// add boxes to bound the world
		Body floor = new Body("floor", new Box(1500,20,1500));
		floor.setPosition(new Vector3(0,-30,0));
		floor.setFixed(true);
		
		Body back = new Body( "back", new Box(200,200,20));		
		back.setPosition(new Vector3(0,0,-55));
		back.setFixed(true);

		Body front = new Body( "front", new Box(200,200,20));		
		front.setPosition(new Vector3(0,0,-7));
		front.setFixed(true);

		Body left = new Body( "left", new Box(20,200,200));		
		left.setPosition(new Vector3(-35,0,0));
		left.setFixed(true);

		Body right = new Body( "right", new Box(20,200,200));		
		right.setPosition(new Vector3(10,0,0));
		right.setFixed(true);
		
		// create capsules
		UniformCapsule capgeo = new UniformCapsule(2,6);
		Body cap = new Body( "cap", capgeo );
		cap.setPosition(new Vector3(-10,-11,-25));

		UniformCapsule capgeo2 = new UniformCapsule(1.8,5);
		Body cap2 = new Body( "cap2", capgeo2 );
		cap2.setPosition(new Vector3(-10,-11,-25));

		UniformCapsule capgeo3 = new UniformCapsule(1.0,4);
		Body cap3 = new Body( "cap3", capgeo3 );
		cap3.setPosition(new Vector3(-10,-11,-25));

		UniformCapsule capgeo4 = new UniformCapsule(1.6,1.0);
		Body cap4 = new Body( "cap3", capgeo4 );
		cap4.setPosition(new Vector3(-10,-11,-25));

		// create a box
		Box boxgeometry = new Box(3,3,3);
		Body box = new Body( "box", boxgeometry );
		box.setPosition(new Vector3(-3,-11,-25));
		
		// create a box
		Box boxgeometry2 = new Box(4,4,4);
		Body box2 = new Body( "box2", boxgeometry2 );
		box2.setPosition(new Vector3(-3,-11,-25));

		// create a box
		Box boxgeometry3 = new Box(5,5,5);
		Body box3 = new Body( "box3", boxgeometry3);
		box3.setPosition(new Vector3(-3,-11,-25));

		
		// create ico
		List<Vector3> vertices = new ArrayList<Vector3>();
		final double t = (1.0 + Math.sqrt(5.0))/ 2.0;
		vertices.add(new Vector3(-1,  t,  0).normalize());
		vertices.add( new Vector3( 1,  t,  0).normalize());
		vertices.add( new Vector3(-1, -t,  0).normalize());
		vertices.add( new Vector3( 1, -t,  0).normalize());
		vertices.add( new Vector3( 0, -1,  t).normalize());
		vertices.add( new Vector3( 0,  1,  t).normalize());
		vertices.add( new Vector3( 0, -1, -t).normalize());
		vertices.add( new Vector3( 0,  1, -t).normalize());
		vertices.add( new Vector3( t,  0, -1).normalize());
		vertices.add( new Vector3( t,  0,  1).normalize());
		vertices.add( new Vector3(-t,  0, -1).normalize());
		vertices.add( new Vector3(-t,  0,  1).normalize());
		for (Vector3 v: vertices)
			v.assign(v.multiply(3));
		ConvexHull ico = new ConvexHull(vertices);
		
		Body icosphere = new Body("icosphere");
		icosphere.addGeometryIncremental(Matrix3.identity(), new Vector3(0,-11,-25), ico);
		
		// add all to scene
		scene.addBody(floor);
		scene.addBody(back);
		scene.addBody(front);		
		scene.addBody(left);
		scene.addBody(right);
		scene.addBody(box); 	
		scene.addBody(box2); 	
		scene.addBody(box3); 	
		scene.addBody(cap);
		scene.addBody(cap2);
		scene.addBody(cap3);
		scene.addBody(cap4);		
		scene.addBody(icosphere);

		// put gravity on stuff
		scene.addForce( new GravityForce(box));		
		scene.addForce( new GravityForce(box2));		
		scene.addForce( new GravityForce(box3));		
		scene.addForce( new GravityForce(cap));		
		scene.addForce( new GravityForce(cap2));
		scene.addForce( new GravityForce(cap3));		
		scene.addForce( new GravityForce(cap4));		
		scene.addForce( new GravityForce(icosphere));		
		
		// handle drawing
		Rendering rendering = new jinngine.rendering.jogl.JoglRendering(this);
		rendering.addCallback(new Interaction(scene));
		rendering.drawMe(boxgeometry);
		rendering.drawMe(boxgeometry2);
		rendering.drawMe(boxgeometry3);
		rendering.drawMe(capgeo);
		rendering.drawMe(capgeo2);
		rendering.drawMe(capgeo3);
		rendering.drawMe(capgeo4);
		rendering.drawMe(ico);

		// add to applet
		add( rendering.getCanvas(), BorderLayout.CENTER);
		
		rendering.start();
	}

	public void tick() {
		// each frame, to a time step on the Scene
		scene.tick();
	}

}
