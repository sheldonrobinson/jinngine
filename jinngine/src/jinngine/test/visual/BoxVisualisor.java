package jinngine.test.visual;

import jinngine.geometry.Box;
import jinngine.geometry.Geometry;
import jinngine.geometry.Sphere;
import jinngine.math.*;
import jinngine.physics.*;

import java.util.*;
import java.awt.Frame;
import java.awt.event.*;

// jogl imports
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.Animator;
//import com.sun.opengl.util.FPSAnimator;

public class BoxVisualisor extends Frame implements GLEventListener  {
	private static final long serialVersionUID = 1L;
	private final GLCanvas canvas;
	private final GLU glu = new GLU();	
	private double width;
	private double height;
	private double drawHeight;
	// Display lists
	private int box;
	private int sphere;

	// Camera coordinates
	private final Vector3 cameraFrom = new Vector3(-45,87,-185).multiply(0.3);
	private final Vector3 cameraTo = new Vector3(0,0,0);	
	
	// Physics
	private final Model model;
	private final List<Body> boxes;

	public BoxVisualisor( Model model, List<Body> boxes) {
		this.model = model;
		this.boxes = boxes;
	
		//Setup exit function
	    addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent e){ System.exit(0);}});
		setSize(800,600);
		canvas = new GLCanvas();
		canvas.setIgnoreRepaint( true );
		canvas.addGLEventListener(this);
		canvas.setVisible(true);
		add(canvas, java.awt.BorderLayout.CENTER);		
		setVisible(true);
	}
	
	public BoxVisualisor( Model model, List<Body> boxes, int ratio)  {
		this.model = model;
		this.boxes = boxes;
		//Setup exit function
	    addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent e){ System.exit(0);}});
		setSize(800,600);
		canvas = new GLCanvas();
		canvas.setIgnoreRepaint( true );
		canvas.addGLEventListener(this);
		canvas.setVisible(true);
		add(canvas, java.awt.BorderLayout.CENTER);		
		setVisible(true);
	}
	
	//public void addKeyListener()
	

	public void init(GLAutoDrawable drawable) {
		// Setup GL 
		GL gl = drawable.getGL();
		gl.glEnable (GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		//gl.glPolygonMode(GL.GL_FRONT, GL.GL_LINE );
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		
		// Generate a box display list
		box = gl.glGenLists(1);
		gl.glNewList(box,GL.GL_COMPILE);
		gl.glBegin(GL.GL_QUADS);
        gl.glNormal3f(0.0f, -0.5f, 0.0f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glColor3f(0.5f, 0.0f, 0.0f);
        gl.glVertex3f(-0.5f, -0.5f, -0.5f);  // Bottom Face
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(0.5f, -0.5f, -0.5f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(0.5f, -0.5f, 0.5f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(-0.5f, -0.5f, 0.5f);

        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glNormal3f(0.0f, 0.0f, 0.5f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-0.5f, -0.5f, 0.5f);  // Front Face
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(0.5f, -0.5f, 0.5f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(0.5f, 0.5f, 0.5f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-0.5f, 0.5f, 0.5f);

        gl.glColor3f(0.0f, 0.0f, 0.5f);
        gl.glNormal3f(0.0f, 0.0f, -0.5f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(-0.5f, -0.5f, -0.5f);  // Back Face
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-0.5f, 0.5f, -0.5f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(0.5f, 0.5f, -0.5f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(0.5f, -0.5f, -0.5f);

        gl.glColor3f(0.0f, 0.5f, 0.5f);
        gl.glNormal3f(0.5f, 0.0f, 0.0f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(0.5f, -0.5f, -0.5f);  // Right face
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(0.5f, 0.5f, -0.5f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(0.5f, 0.5f, 0.5f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(0.5f, -0.5f, 0.5f);

        gl.glColor3f(0.0f, 0.0f, 0.0f);
        gl.glNormal3f(-0.5f, 0.0f, 0.0f);
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-0.5f, -0.5f, -0.5f);  // Left Face
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glVertex3f(-0.5f, -0.5f, 0.5f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(-0.5f, 0.5f, 0.5f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-0.5f, 0.5f, -0.5f);

        gl.glColor3f(0.5f, 0.5f, 0.5f);
        gl.glNormal3f(0.0f, 0.5f, 0.0f);
        gl.glTexCoord2f(0.0f, 1.0f);
        gl.glVertex3f(-0.5f, 0.5f, -0.5f);// Top Face
        gl.glTexCoord2f(0.0f, 0.0f);
        gl.glVertex3f(-0.5f, 0.5f, 0.5f);
        gl.glTexCoord2f(1.0f, 0.0f);
        gl.glColor3f(0.7f, 0.7f, 0.7f);
        gl.glVertex3f(0.5f, 0.5f, 0.5f);
        gl.glTexCoord2f(1.0f, 1.0f);
        gl.glVertex3f(0.5f, 0.5f, -0.5f);
        gl.glEnd();      
        gl.glEndList();        
        
        //sphere
		// Generate a box display list
        GLUquadric q = glu.gluNewQuadric();
        glu.gluQuadricDrawStyle(q, GLU.GLU_LINE);
		sphere = gl.glGenLists(1);
		gl.glNewList(sphere,GL.GL_COMPILE);
		gl.glColor3f(0,0,0);
		glu.gluSphere(q, 1, 8, 8);
		gl.glEndList();
        
	}
	
	public void displayChanged(GLAutoDrawable glad,boolean a,boolean b) {
	}
	
	static double spend = 0;

	public void display(GLAutoDrawable drawable) {
		double total = 0.12;//model.getDt();
		// int ticks = (int)(1/model.getDt() *0.12);
 		// Perform ratio time-steps on the model
		do {
			spend += model.getDt();
			model.tick();
//			model.tick();
//			model.tick();			
		} while ( spend < total);

		spend = spend % total;
		
		// Clear buffer, etc.
		GL gl = drawable.getGL();
		gl.glClearColor(1.0f, 1.0f,1.0f, 1.0f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();

        // Set camera transform
        glu.gluLookAt(cameraFrom.x, cameraFrom.y, cameraFrom.z, 
        		cameraTo.x, cameraTo.y, cameraTo.z, 
        		0, 1, 0); 


        // Go through boxes and draw them
        for ( Body body: boxes) {
        	Iterator<Geometry> i = body.getGeometries();
        	while (i.hasNext()) {
        		Geometry g = i.next();
        		gl.glPushMatrix();
        		Matrix4 T = g.getTransform();   		
        		gl.glMultMatrixd(Matrix4.pack(T), 0);
        		
        		if (g instanceof Box)
        			gl.glCallList(box);
        		if (g instanceof Sphere)
        			gl.glCallList(sphere);
        		
        		gl.glPopMatrix();
        	}				
        }

        // Finish this frame
        gl.glFlush();
	}

	public void reshape(GLAutoDrawable drawable ,int x,int y, int w, int h) {
		// Setup wide screen view port
		GL gl = drawable.getGL();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustum (-1.77777, 1.777777, -1.0, 1.0, 1.5, 600.0); 	
		this.height = h; this.width = w;
		this.drawHeight = (int)((double)width/1.77777);
		gl.glViewport (0, (int)((height-drawHeight)/2.0), (int)width, (int)drawHeight);
	}	

	public void start() {
		// Ask an animator class to run simulation at 60 fps
		Animator animator = new Animator(this.canvas );
		animator.start();
	}

}
