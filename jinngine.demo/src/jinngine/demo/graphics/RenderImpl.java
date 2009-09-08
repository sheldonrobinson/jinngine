package jinngine.demo.graphics;

import jinngine.math.*;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.util.*;

// jogl imports
import javax.media.opengl.*;
import javax.media.opengl.glu.*;


import com.sun.opengl.util.Animator;
//import com.sun.opengl.util.FPSAnimator;
//import com.sun.opengl.util.Screenshot;


public class RenderImpl extends Frame implements GLEventListener, Render  {
	private static final long serialVersionUID = 1L;

	private class DrawTask {
		public ShapeRender render;
		public Shape shape;
		public Matrix4 transform;
		public Entity entity;
	}
	
	List<DrawTask> tasks = new LinkedList<DrawTask>();

	
	public double zoom = 0.95;

	
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
//	private final GLJPanel canvas;
	public final GLU glu = new GLU();	
	private double width;
	private double height;
	private double drawHeight;
	private Graphics callback;
	private int frame = 0;

	// Display lists
//	private int box;
//	private int sphere;

	// Camera coordinates
//	private final Vector3 cameraFrom = new Vector3(-45,287,-285).multiply(0.3);
//	private final Vector3 cameraTo = new Vector3(0,0,-0);	

//	private final Vector3 cameraFrom = new Vector3(-12,5,-45).multiply(1);

	private final Vector3 cameraTo = new Vector3(-12,-4,0).multiply(1);	
	private final Vector3 cameraFrom = cameraTo.add(new Vector3(0,0.5,1).multiply(1));

	//camera transform
	public double[] proj = new double[16];
	public double[] camera = new double[16];
	
	Vector3 p1 = new Vector3();
	Vector3 p2 = new Vector3();
	


	public RenderImpl(final Graphics callback ) {
		this.callback = callback;
		

		
		setTitle("Jinngine");
		setSize(1024,(int)(1024/(1.77777)));

		canvas = new GLCanvas();
		//canvas = GLDrawableFactory.getFactory().
		canvas.setIgnoreRepaint( true );
		canvas.addGLEventListener(this);
		canvas.setVisible(true);
		
		//setLayout(getLayout());

		//Setup exit function
       // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter() {public void windowClosing(java.awt.event.WindowEvent e) {System.exit(0);} } );

		add(canvas, java.awt.BorderLayout.CENTER);

        
        //Display the window.
        //pack();

		
		//add(canvas);	
		//add(new JButton("lala"));
		setVisible(true);
		canvas.addMouseListener(callback);
		canvas.addMouseMotionListener(callback);
		canvas.addMouseWheelListener(callback);
		canvas.addKeyListener(callback);


	}

	
	
	
	public void init(GLAutoDrawable drawable) {
		// Setup GL 
		GL gl = drawable.getGL();
		gl.glEnable (GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glEnable(GL.GL_LINE_SMOOTH);
	//	gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
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
		frame=frame+1;


		// Clear buffer, etc.
		GL gl = drawable.getGL();
		gl.glClearColor(1.0f, 1.0f,1.0f, 1.0f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		
		gl.glLoadIdentity();
		
//		gl.glMultMatrixd(shadowProjectionMatrix(new Vector3(0,115,0), new Vector3(0,-100,0), new Vector3(0,-1,0)), 0);
		//gl.glScaled(5, 5,2.1);

		
		// Set camera transform
		glu.gluLookAt(cameraFrom.x, cameraFrom.y, cameraFrom.z, 
				cameraTo.x, cameraTo.y, cameraTo.z, 
				0, 1, 0); 

		//copy camera transform
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, camera, 0);

		
		gl.glPushAttrib(GL.GL_LIGHTING_BIT);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(p1.x, p1.y, p1.z);
		gl.glVertex3d(p2.x, p2.y, p2.z);
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
		glu.gluLookAt(cameraFrom.x, cameraFrom.y, cameraFrom.z, 
				cameraTo.x, cameraTo.y, cameraTo.z, 
				0, 1, 0); 

		
		gl.glMultMatrixd(shadowProjectionMatrix(new Vector3(75,350,-75), new Vector3(0,-20 + 0.0,0), new Vector3(0,-1,0)), 0);
		
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
				//Vector3 n =face[1].minus(face[0]).cross(face[2].minus(face[1])).normalize();
				
				for ( Vector3 v: face) {
					gl.glVertex3d(v.x, v.y, v.z);
				}
				gl.glEnd();
			}
			
			gl.glPopMatrix();
		}
		gl.glEnable(GL.GL_LIGHTING);
		

		// Finish this frame
		gl.glFlush();

//		if (frame % 30 == 0 || frame == 2) {
//			try {
//				Screenshot.writeToFile(new File("demo2" + frame + ".png"), (int)(this.width/4.0), 0, (int)(this.width/4.0)*2, (int)this.height,  false);
//			} catch (GLException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//		}
	}

	public void reshape(GLAutoDrawable drawable ,int x,int y, int w, int h) {
		// Setup wide screen view port
		GL gl = drawable.getGL();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustum (-1.77777*zoom, 1.777777*zoom, -1.0*zoom, 1.0*zoom, 4.0, 100.0); 	
		this.height = h; this.width = w;
		this.drawHeight = (int)((double)width/1.77777);
		gl.glViewport (0, (int)((height-drawHeight)/2.0), (int)width, (int)drawHeight);

		//double[] proj = new double[16];
		gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, proj, 0);
	}	

	public void start() {
		// Ask an animator class to run simulation at 60 fps
		//FPSAnimator animator = new FPSAnimator(this.canvas,60);
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
		double x = callback.mouse.x;
		double y = callback.mouse.y;
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
	  
	  d = n.x*l.x + n.y*l.y + n.z*l.z;
	  c = e.x*n.x + e.y*n.y + e.z*n.z - d;

	  // Create the matrix. OpenGL uses column by column
	  // ordering

	  mat[0]  = l.x*n.x+c; 
	  mat[4]  = n.y*l.x; 
	  mat[8]  = n.z*l.x; 
	  mat[12] = -l.x*c-l.x*d;
	  
	  mat[1]  = n.x*l.y;        
	  mat[5]  = l.y*n.y+c;
	  mat[9]  = n.z*l.y; 
	  mat[13] = -l.y*c-l.y*d;
	  
	  mat[2]  = n.x*l.z;        
	  mat[6]  = n.y*l.z; 
	  mat[10] = l.z*n.z+c; 
	  mat[14] = -l.z*c-l.z*d;
	  
	  mat[3]  = n.x;        
	  mat[7]  = n.y; 
	  mat[11] = n.z; 
	  mat[15] = -d;

	  return mat;
	}


	@Override
	public void getCamera(Vector3 from, Vector3 to) {
		from.assign(cameraFrom);
		to.assign(cameraTo);
	}



		

}
