package jinngine.demo.graphics;


import jinngine.math.*;

import java.util.*;
import java.awt.Frame;
import java.awt.event.*;

// jogl imports
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;


public class RenderImpl2 extends Frame implements GLEventListener, Render  {

	private class DrawTask {
		public ShapeRender render;
		public Shape shape;
		public Matrix4 transform;
		public Entity entity;
	}
	
	List<DrawTask> tasks = new LinkedList<DrawTask>();

	
	@Override
	public void addShape(ShapeRender r, Shape s, Matrix4 transform, Entity e) {
		DrawTask dt = new DrawTask();
		dt.render = r;
		dt.shape = s;
		dt.transform = transform;
		dt.entity = e;	
		tasks.add(dt);	
	}


	private final GLCanvas canvas;
	public final GLU glu = new GLU();	
	private double width;
	private double height;
	private double drawHeight;
	private Graphics callback;

	// Display lists
	private int box;
	private int sphere;

	// Camera coordinates
	private final Vector3 cameraFrom = new Vector3(-45,287,-285).multiply(0.3);
	private final Vector3 cameraTo = new Vector3(0,0,0);	
	//camera transform
	public double[] proj = new double[16];
	public double[] camera = new double[16];
	
	Vector3 p1 = new Vector3();
	Vector3 p2 = new Vector3();
	


	public RenderImpl2( Graphics callback ) {
		this.callback = callback;
		
		//Setup exit function
		addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent e){ System.exit(0);}});
		setSize(800,600);
		canvas = new GLCanvas();
		canvas.setIgnoreRepaint( true );
		canvas.addGLEventListener(this);
		canvas.setVisible(true);
		add(canvas, java.awt.BorderLayout.CENTER);		
		setVisible(true);
		canvas.addMouseListener(callback);
		canvas.addMouseMotionListener(callback);
		canvas.addKeyListener(callback);
	}

	
	
	
	public void init(GLAutoDrawable drawable) {
		// Setup GL 
		GL gl = drawable.getGL();
		gl.glEnable (GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		//gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE );
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);


		//enable vsync
		gl.setSwapInterval(1);
		
		//run inits for all renders
		for (DrawTask dt: tasks) {
			dt.render.init(this, gl);
		}

		//sphere
		// Generate a box display list
//		GLUquadric q = glu.gluNewQuadric();
//		glu.gluQuadricDrawStyle(q, GLU.GLU_LINE);
//		sphere = gl.glGenLists(1);
//		gl.glNewList(sphere,GL.GL_COMPILE);
//		gl.glColor3f(0,0,0);
//		glu.gluSphere(q, 1, 8, 8);
//		gl.glEndList();
	}

	public void displayChanged(GLAutoDrawable glad,boolean a,boolean b) {
	}


	public void display(GLAutoDrawable drawable) {
		// Perform ratio time-steps on the model
		callback.callback();


		// Clear buffer, etc.
		GL gl = drawable.getGL();
		gl.glClearColor(1.0f, 1.0f,1.0f, 1.0f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();

//		gl.glMultMatrixd(shadowProjectionMatrix(new Vector3(0,115,0), new Vector3(0,-100,0), new Vector3(0,-1,0)), 0);

		
		// Set camera transform
		glu.gluLookAt(cameraFrom.a1, cameraFrom.a2, cameraFrom.a3, 
				cameraTo.a1, cameraTo.a2, cameraTo.a3, 
				0, 1, 0); 

		//copy camera transform
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, camera, 0);

		
		gl.glPushAttrib(GL.GL_LIGHTING_BIT);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(p1.a1, p1.a2, p1.a3);
		gl.glVertex3d(p2.a1, p2.a2, p2.a3);
		gl.glEnd();
		gl.glPopAttrib();
		

		// Go through boxes and draw them
//		for ( Body body: boxes) {
//			Iterator<Geometry> i = body.getGeometries();
//			while (i.hasNext()) {
//				Geometry g = i.next();
//				gl.glPushMatrix();
//				Matrix4 T = g.getTransform();   		
//				gl.glMultMatrixd(Matrix4.pack(T), 0);
//
//				if (body instanceof Box)
//					gl.glCallList(box);
//				if (body instanceof Sphere)
//					gl.glCallList(sphere);
//
//				gl.glPopMatrix();
//			}				
//		}


		for ( DrawTask dt: tasks) {
			gl.glPushAttrib(GL.GL_LIGHTING_BIT);
			gl.glPushMatrix();
			gl.glMultMatrixd(Matrix4.pack(dt.transform), 0);	
			dt.render.preRenderShape(this, dt.shape, dt.entity, gl);
			dt.render.renderShape(this, dt.shape, gl);
			gl.glPopMatrix();
			gl.glPopAttrib();
		}
		
		//draw shadows
		
		
		gl.glLoadIdentity();

		gl.glDisable(GL.GL_LIGHTING);
		// Set camera transform
		glu.gluLookAt(cameraFrom.a1, cameraFrom.a2, cameraFrom.a3, 
				cameraTo.a1, cameraTo.a2, cameraTo.a3, 
				0, 1, 0); 

		
		gl.glMultMatrixd(shadowProjectionMatrix(new Vector3(0,350,0), new Vector3(0,-19,0), new Vector3(0,-1,0)), 0);
		
		gl.glColor3d(0.85, 0.85, 0.85);
		
		for ( DrawTask dt: tasks) {
			gl.glPushMatrix();
			gl.glMultMatrixd(Matrix4.pack(dt.transform), 0);	
			gl.glMultMatrixd(Matrix4.pack(dt.shape.getLocalTransform()),0);


			gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
			Iterator<Vector3[]> i = dt.shape.getFaces(); 
			while (i.hasNext()) {
				gl.glBegin(GL.GL_POLYGON);
				Vector3[] face = i.next();
				//compute normal
				Vector3 n =face[1].minus(face[0]).cross(face[2].minus(face[1])).normalize();
				
				for ( Vector3 v: face) {
					gl.glVertex3d(v.a1, v.a2, v.a3);
				}
				gl.glEnd();
			}
			
			gl.glPopMatrix();
		}
		gl.glEnable(GL.GL_LIGHTING);
		

		// Finish this frame
		gl.glFlush();
	}

	public void reshape(GLAutoDrawable drawable ,int x,int y, int w, int h) {
		// Setup wide screen view port
		GL gl = drawable.getGL();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustum (-1.77777, 1.777777, -1.0, 1.0, 2.0, 600.0); 	
		this.height = h; this.width = w;
		this.drawHeight = (int)((double)width/1.77777);
		gl.glViewport (0, (int)((height-drawHeight)/2.0), (int)width, (int)drawHeight);

		//double[] proj = new double[16];
		gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, proj, 0);
	}	

	public void start() {
		// Ask an animator class to run simulation at 60 fps
		//Animator animator = new FPSAnimator(this.canvas,60,true);
		Animator animator = new Animator(this.canvas);
		animator.start();
	}

	@Override
	public Matrix4 getCameraMatrix() {
		Matrix4 M = new Matrix4();
		Matrix4.set(M, camera);
		return M;
	}

	@Override
	public Matrix4 getProjectionMatrix() {
		Matrix4 M = new Matrix4();
		Matrix4.set(M, proj);
		return M;
	}


	@Override
	public void getPointerRay(Vector3 p, Vector3 d) {
		double x = callback.mouse.a1;
		double y = callback.mouse.a2;
//		gl.glVertex3d(x/(double)width,-(y-((height-drawHeight)*0.5))/(double)drawHeight, 0);
		Vector3 near = new Vector3(2*x/(double)width-1,-2*(y-((height-drawHeight)*0.5))/(double)drawHeight+1, 0.7);
		Vector3 far = new Vector3(2*x/(double)width-1,-2*(y-((height-drawHeight)*0.5))/(double)drawHeight+1, 0.9);

		//inverse transform
		Matrix4 T = getProjectionMatrix().multiply(getCameraMatrix());
		Matrix4.invert(T);
		
		Vector3 p1 = new Vector3();
		Vector3 p2 = new Vector3();
		
		Matrix4.multiply(T,near,p1);
		Matrix4.multiply(T,far,p2);
		
		p.assign(p1);
		d.assign(p2.minus(p1).normalize());

//		this.p1.assign(p1);
//		this.p2.assign(p1.add(d.multiply(450)).add(new Vector3(0,0,0)));
				
		
//		p1.print();
//		p2.print();
		
//		p.print();
//		d.print();
	}
	
	/**
	 * This is where the "magic" is done:
	 *
	 * Multiply the current ModelView-Matrix with a shadow-projetion
	 * matrix.
	 *
	 * l is the position of the light source
	 * e is a point on within the plane on which the shadow is to be
	 *   projected.  
	 * n is the normal vector of the plane.
	 *
	 * Everything that is drawn after this call is "squashed" down
	 * to the plane. Hint: Gray or black color and no lighting 
	 * looks good for shadows *g*
	 */
	private double[] shadowProjectionMatrix(Vector3 l, Vector3 e, Vector3  n)
	{
	  double d, c;
	  double[] mat = new double[16];

	  // These are c and d (corresponding to the tutorial)
	  
	  d = n.a1*l.a1 + n.a2*l.a2 + n.a3*l.a3;
	  c = e.a1*n.a1 + e.a2*n.a2 + e.a3*n.a3 - d;

	  // Create the matrix. OpenGL uses column by column
	  // ordering

	  mat[0]  = l.a1*n.a1+c; 
	  mat[4]  = n.a2*l.a1; 
	  mat[8]  = n.a3*l.a1; 
	  mat[12] = -l.a1*c-l.a1*d;
	  
	  mat[1]  = n.a1*l.a2;        
	  mat[5]  = l.a2*n.a2+c;
	  mat[9]  = n.a3*l.a2; 
	  mat[13] = -l.a2*c-l.a2*d;
	  
	  mat[2]  = n.a1*l.a3;        
	  mat[6]  = n.a2*l.a3; 
	  mat[10] = l.a3*n.a3+c; 
	  mat[14] = -l.a3*c-l.a3*d;
	  
	  mat[3]  = n.a1;        
	  mat[7]  = n.a2; 
	  mat[11] = n.a3; 
	  mat[15] = -d;

	  return mat;
	}


	@Override
	public void getCamera(Vector3 from, Vector3 to) {
		from.assign(cameraFrom);
		to.assign(cameraTo);
	}
		

}
