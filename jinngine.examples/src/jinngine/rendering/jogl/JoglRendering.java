/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.rendering.jogl;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.Screenshot;

import jinngine.geometry.Box;
import jinngine.geometry.ConvexHull;
import jinngine.geometry.Geometry;
import jinngine.geometry.UniformCapsule;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.rendering.Rendering;

public class JoglRendering implements Rendering, 
GLEventListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	
	private static final long serialVersionUID = 1L;
	public List<DrawShape> toDraw = new ArrayList<DrawShape>();
	private final Callback callback;
	private final List<EventCallback> mouseCallbacks = new ArrayList<EventCallback>();
	private final GLCanvas canvas = new GLCanvas();
	private Animator animator = new Animator(this.canvas);
	private final GLU glu = new GLU();	
	private double width;
	private double height;
	private double drawHeight;
	private volatile boolean takeScreenShot = false;
	private volatile String screenShotFilename;
	private volatile boolean redoCamera;

	//camera transform
	public double[] proj = new double[16];
	public double[] camera = new double[16];
	public double zoom = 0.95;
	private final Vector3 cameraTo = new Vector3(-12,-3,0).multiply(1);	
	private final Vector3 cameraFrom = cameraTo.add(new Vector3(0,0.5,1).multiply(5));
	private int cameraClicks = 1;
	
	// global light0 position
	private final float position[] = { -5f, 20.0f, -25.0f, 1.0f };
//	private final float position[] = { 0f, 0.0f, -0.0f, 1.0f };
	
	// uniforms
	private int extrutionUniformLocation;
	private int colorUniformLocation;
	private int influenceUniformLocation;
	private double[] shadowProjMatrix;

	
	private boolean initialized = false;

	// interface for objects to be drawn
	private interface DrawShape {
		public void init(GL gl);
		public int getDisplayList();
		public int getShadowDisplayList();
		public Matrix4 getTransform();
		public Body getReferenceBody();
	}
	
	public JoglRendering(Callback callback ) {
		this.callback = callback;
		canvas.setSize(1024,(int)(1024/(1.77777)));
		canvas.setIgnoreRepaint( true );
		canvas.addGLEventListener(this);
		canvas.setVisible(true);
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addMouseWheelListener(this);
		canvas.addKeyListener(this);
		canvas.setVisible(true);
	}
	
	public void createWindow() {
		Frame frame = new Frame();
		frame.setTitle("jinngine.example");
		frame.setSize(1024,(int)(1024/(1.77777)));
		//Setup exit function
		frame.addWindowListener(new WindowAdapter() {public void windowClosing(java.awt.event.WindowEvent e) {			
			System.exit(0);} 
		} );

		frame.add(canvas, java.awt.BorderLayout.CENTER);
		frame.setVisible(true);
	}
	
	@Override
	public void drawMe(final Geometry g) {
		if (g instanceof ConvexHull) {
			final ConvexHull hull = (ConvexHull)g;
			toDraw.add( new DrawShape() {
				private int list = 0;
				private int shadowList = 0;
				@Override
				public Matrix4 getTransform() {
					return g.getWorldTransform();
				}
				@Override
				public Body getReferenceBody() {
					return g.getBody();
				}
				@Override
				public int getDisplayList() {
					return list;
				}
				@Override
				public void init(GL gl) {
					list = startDisplayList(gl);
					drawPolygonShape(hull.getVerticesList(), null, hull.getFaceIndices(), gl);
					endDisplayList(gl);

					shadowList = startDisplayList(gl);
					drawBackfaceShadowMesh(hull.getVerticesList(), null, hull.getFaceIndices(), gl);
					endDisplayList(gl);
				}
				@Override
				public int getShadowDisplayList() {
					return shadowList;
				}
			});
			
		}
		
		if ( g instanceof Box  ) {
			final List<Vector3> inputVertices = new ArrayList<Vector3>();
			final List<Vector3> hullVertices = new ArrayList<Vector3>();

			inputVertices.add( new Vector3(  0.5,  0.5,  0.5));
			inputVertices.add( new Vector3( -0.5,  0.5,  0.5));
			inputVertices.add( new Vector3(  0.5, -0.5,  0.5));
			inputVertices.add( new Vector3( -0.5, -0.5,  0.5));
			inputVertices.add( new Vector3(  0.5,  0.5, -0.5));
			inputVertices.add( new Vector3( -0.5,  0.5, -0.5));
			inputVertices.add( new Vector3(  0.5, -0.5, -0.5));
			inputVertices.add( new Vector3( -0.5, -0.5, -0.5));
			
			// apply scaling to the box vertices
			Matrix3 S = new Matrix3().assignScale(((Box)g).getDimentions());
			for (Vector3 v: inputVertices) {
				v.assign(S.multiply(v));
			}
			
			final ConvexHull hull = new ConvexHull(inputVertices);
			
			// get the vertices in the final hull
			Iterator<Vector3> i = hull.getVertices();
			while(i.hasNext()) {
				Vector3 point = i.next();
				hullVertices.add(point);				
			}
			
			toDraw.add( new DrawShape() {
				private int list = 0;
				private int shadowList = 0;

				@Override
				public Matrix4 getTransform() {
					return g.getWorldTransform();
				}
				@Override
				public Body getReferenceBody() {
					return g.getBody();
				}
				@Override
				public int getDisplayList() {
//					System.out.println(""+list);
					return list;
				}
				@Override
				public void init(GL gl) {
					list = startDisplayList(gl);
					drawPolygonShape(hullVertices, null, hull.getFaceIndices(), gl);
					endDisplayList(gl);					
					shadowList = startDisplayList(gl);
					drawBackfaceShadowMesh(hullVertices, null, hull.getFaceIndices(), gl);
					endDisplayList(gl);

				}
				@Override
				public int getShadowDisplayList() {
					return shadowList;
				}
			});
		}
		
		if ( g instanceof UniformCapsule  ) {
			UniformCapsule cap = (UniformCapsule)g;
			final List<Vector3> inputVertices = new ArrayList<Vector3>();
			final List<Vector3> inputNormals = new ArrayList<Vector3>();
			final List<Vector3> hullNormals = new ArrayList<Vector3>();
			final List<Vector3> hullVertices = new ArrayList<Vector3>();
			

			ConvexHull icosphere = buildIcosphere(1, 3);
			
			// add two ico-spheres to vertices
			Iterator<Vector3> iter = icosphere.getVertices();
			while(iter.hasNext()) {
				Vector3 v = iter.next();
				inputVertices.add( v.multiply(cap.getRadius()).add(0,0,cap.getLength()/2));
				inputNormals.add(v.normalize());
			}
			
			iter = icosphere.getVertices();
			while(iter.hasNext()) {
				Vector3 v = iter.next();
				inputVertices.add( v.multiply(cap.getRadius()).add(0,0,-cap.getLength()/2));
				inputNormals.add(v.normalize());
			}

			
			final ConvexHull hull = new ConvexHull(inputVertices);
			
			// build normal array
			for( int index :hull.getOriginalVertexIndices()) {
				hullNormals.add( inputNormals.get(index) );
			}
			
			// get the vertices in the final hull
			Iterator<Vector3> i = hull.getVertices();
			while(i.hasNext()) {
				hullVertices.add(i.next());
			}
			
			toDraw.add( new DrawShape() {
				private int list = 0;
				private int shadowList = 0;
				@Override
				public Matrix4 getTransform() {
					return g.getWorldTransform();
				}
				@Override
				public Body getReferenceBody() {
					return g.getBody();
				}
				@Override
				public int getDisplayList() {
					return list;
				}
				@Override
				public void init(GL gl) {
					list = startDisplayList(gl); 
					drawSmoothShape( hullVertices, hullNormals, hull.getFaceIndices(), gl);
					endDisplayList(gl);
					
					shadowList = startDisplayList(gl);
					drawBackfaceShadowMesh(hullVertices, null, hull.getFaceIndices(), gl);
					endDisplayList(gl);
				}
				@Override
				public int getShadowDisplayList() {
					return shadowList;
				}
			});
		}

	}
	
	private ConvexHull buildIcosphere(double r, int depth) {
		final List<Vector3> vertices = new ArrayList<Vector3>();
//		vertices.add(new Vector3( 1, 1, 1).normalize());
//		vertices.add(new Vector3(-1,-1, 1).normalize());
//		vertices.add(new Vector3(-1, 1,-1).normalize());
//		vertices.add(new Vector3( 1,-1,-1).normalize());
		// point on icosahedron
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

		int n = 0;
		while (true) {
			ConvexHull hull = new ConvexHull(vertices);

			if (n>=depth)
				return hull;

			// for each face, add a new sphere support 
			// point in direction of the face normal
			Iterator<Vector3[]> iter = hull.getFaces();
			while(iter.hasNext()) {
				Vector3[] face = iter.next();
				Vector3 normal =face[1].sub(face[0]).cross(face[2].sub(face[1])).normalize();
				vertices.add(new Vector3(normal));
			}
			
			// depth level done
			n++;
		}
	}

	@Override
	public void start() {
		animator.start();
	}

	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();

		// init all drawing objects
		if (!initialized) {
			// calculate shadow matrix
            shadowProjMatrix = shadowProjectionMatrix(new Vector3(0,350,0), new Vector3(0,-20 + 0.0,0), new Vector3(0,-1,0));

            // init all display lists
			for (DrawShape s: toDraw)
				s.init(gl);
			initialized = true;
		}

		if (redoCamera) {
			// setup camera
			gl.glMatrixMode(GL.GL_MODELVIEW);
			gl.glLoadIdentity();
						
			// Set camera transform
			glu.gluLookAt(cameraFrom.x, cameraFrom.y, cameraFrom.z, 
					cameraTo.x, cameraTo.y, cameraTo.z, 
					0, 1, 0); 

			//copy camera transform (needed for picking)
			gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, camera, 0);
			
			// set light position in world space
			gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, position,0);

			
			// camera done
			redoCamera = false;
		}
		
		// Perform ratio time-steps on the model
		callback.tick();

		// Clear buffer, etc.
		gl.glClearColor(1.0f, 1.0f,1.0f, 1.0f);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);

		for ( DrawShape shape: toDraw) {
			gl.glPushMatrix();
			final double[] transform= shape.getTransform().toArray();
			gl.glMultMatrixd(transform, 0);
			gl.glCallList(shape.getDisplayList());
			gl.glPopMatrix();
			
			// draw projected shadow
			gl.glPushMatrix();		
			gl.glMultMatrixd(shadowProjMatrix, 0);
			gl.glMultMatrixd(transform, 0);
			gl.glCallList(shape.getShadowDisplayList());
			gl.glPopMatrix();
		}

		// Finish this frame
		gl.glFlush();
		
		// take screenshot
		if (takeScreenShot) {
			gl.glFinish();
			
			takeScreenShot = false;
			try {
				Screenshot.writeToTargaFile(new File(screenShotFilename), (int)this.width, (int)this.height);
			} catch (GLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void drawEdgeMesh( ConvexHull hull, GL gl ) {		
		// build level 0 ico-sphere for edge endings
		ConvexHull icosphere = buildIcosphere(1, 0);
	
		// grap all edges in the hull
		List<Vector3> vertices = hull.getVerticesList();
		ArrayList<ArrayList<Integer>> adjacent = hull.getVertexAdjacencyMatrix();
		int i = 0;
		for ( ArrayList<Integer> indices : adjacent ) {
			for ( int j: indices) {
				// for each edge, create a thick line hull
				List<Vector3> inputEdgeVertices = new ArrayList<Vector3>();
				List<Vector3> inputEdgeNormals = new ArrayList<Vector3>();
			
//				System.out.println("edge " + vertices.get(i) +","+vertices.get(j));
				
				for (Vector3 p: icosphere.getVerticesList()) {
					// at i
					Vector3 normal = p.normalize();
					inputEdgeVertices.add(normal.add(vertices.get(i)));
					inputEdgeNormals.add(normal);
					
					// at j
					inputEdgeVertices.add(normal.add(vertices.get(j)));
					inputEdgeNormals.add(normal);

				}
				// build a hull for this edge
				ConvexHull edgehull = new ConvexHull(inputEdgeVertices);
				
				
				// build normal array
				List<Vector3> hullNormals = new ArrayList<Vector3>();
				for( int index : edgehull.getOriginalVertexIndices()) {
					hullNormals.add( inputEdgeNormals.get(index) );
				}
				
				// collapse edge mesh (extrudet later by vertex shader)
				int k=0; for ( Vector3 v: edgehull.getVerticesList() ) {
					v.assign(v.sub(hullNormals.get(k)));
					k++;
				}
				
				// draw this edge
				drawFaces(edgehull.getVerticesList(), hullNormals, edgehull.getFaceIndices(), gl);				
			}
			i = i+1;
		}
		
	}
	
	private void drawFaces( List<Vector3> vertices, List<Vector3> normals, int[][] faceIndices, GL gl) {
		for (int[] face :  faceIndices) {
			gl.glBegin(GL.GL_POLYGON);

			Vector3 n = new Vector3();
			
			//compute normal face
			if (normals==null) { 
				Vector3 v1 = vertices.get(face[0]);
				Vector3 v2 = vertices.get(face[1]);
				Vector3 v3 = vertices.get(face[2]);			
				n.assign((v2.sub(v1).cross(v3.sub(v2))).normalize());
			}
				
			for ( int index: face) {
				Vector3 v = vertices.get(index);
				
				// if normals are given, use them (if not, use face normal)
				if (normals!=null)
					n.assign(normals.get(index));
				
				gl.glNormal3d(n.x, n.y, n.z);
				gl.glVertex3d(v.x, v.y, v.z);
			}
			gl.glEnd();
		}
	}
	
	
	private int startDisplayList(GL gl) {
		int displayList = gl.glGenLists(1);
		gl.glNewList(displayList, GL.GL_COMPILE);
		return displayList;
	}
	
	private void endDisplayList(GL gl) {
		// end display list
		gl.glEndList();		
	}
	
	private void drawSmoothShape( List<Vector3> vertices, List<Vector3> normals, int[][] faceIndices, GL gl) {
		// draw shaded mesh
		gl.glUniform1f(extrutionUniformLocation, 0);
		gl.glUniform3f(colorUniformLocation, 1f, 0.95f, 1f);
		gl.glUniform1f(influenceUniformLocation, 0);
		gl.glCullFace(GL.GL_BACK);
		drawFaces( vertices, normals, faceIndices, gl);	
		
		// draw silhouette
		gl.glUniform1f(extrutionUniformLocation, 0.07f);
		gl.glUniform1f(influenceUniformLocation, 01f);
		gl.glUniform3f(colorUniformLocation, 0.30f,0.30f,0.30f);
		gl.glCullFace(GL.GL_FRONT);
		drawFaces( vertices, normals, faceIndices, gl);	
//		drawEdgeMesh( new ConvexHull(vertices), gl);
	}
	
	private void drawPolygonShape( List<Vector3> vertices, List<Vector3> normals, int[][] faceIndices, GL gl) {
		// draw shaded mesh
		gl.glUniform1f(extrutionUniformLocation, 0);
		gl.glUniform1f(influenceUniformLocation, 0);
		gl.glUniform3f(colorUniformLocation, 0.95f, 0.95f, 1f);
		gl.glCullFace(GL.GL_BACK);
		drawFaces( vertices, normals, faceIndices, gl);	
		
		// draw solid coloured edge mesh
		gl.glUniform1f(extrutionUniformLocation, 0.04f);
		gl.glUniform3f(colorUniformLocation, 0.30f,0.30f,0.30f);
		gl.glUniform1f(influenceUniformLocation, 1f);
		drawEdgeMesh( new ConvexHull(vertices), gl);
	}

	private void drawBackfaceShadowMesh( List<Vector3> vertices, List<Vector3> normals, int[][] faceIndices, GL gl) {
		gl.glUniform1f(extrutionUniformLocation, 0);
		gl.glUniform1f(influenceUniformLocation, 1);
		gl.glUniform3f(colorUniformLocation, 0.85f, 0.85f, 0.85f);
		gl.glCullFace(GL.GL_FRONT);
		drawFaces( vertices, normals, faceIndices, gl);	
	}


	@Override
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) { }

	@Override
	public void init(GLAutoDrawable drawable) {
		// Setup GL 
		GL gl = drawable.getGL();
		gl.glEnable (GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glEnable(GL.GL_LINE_SMOOTH);
	    gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		//enable vsync
		gl.setSwapInterval(1);
				
		// init some lighting
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_LIGHT0);

		// Create light components
		float ambientLight[] = { 2.0f, 2.0f, 2.0f, 1.0f };
		float diffuseLight[] = { 0.2f, 0.2f, 0.2f, 1.0f };
		float specularLight[] = { 0.5f, 0.5f, 0.5f, 1.0f };


		
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		// Set camera transform
		glu.gluLookAt(cameraFrom.x, cameraFrom.y, cameraFrom.z, 
				cameraTo.x, cameraTo.y, cameraTo.z, 
				0, 1, 0); 

		//copy camera transform
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, camera, 0);

		// Assign created components to GL_LIGHT0
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, ambientLight,0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, diffuseLight,0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, specularLight,0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, position,0);
		
		
		String[] vertexShader = {
				"uniform float extrution;\n ",
				"varying vec3 point;   \n",
				"varying vec3 normal;  \n",
				"void main(void) {     \n",
				"  normal = normalize(gl_NormalMatrix * gl_Normal); \n",
                "  point = vec3(gl_ModelViewMatrix * gl_Vertex); \n", 
                "  vec4 newpos = gl_ModelViewProjectionMatrix * gl_Vertex; \n",
                "  gl_Position = gl_ModelViewProjectionMatrix * vec4(newpos.w*extrution*0.04*gl_Normal,0) + newpos;\n",
                "}\n\n"	
		};
		
		String[] phongFragmentShader = {
				"uniform vec3 color;\n",
				"uniform float influence;\n",
				"varying vec3 point;\n",
				"varying vec3 normal;\n",
				"void main(void) { \n",
				" vec3 L = normalize(gl_LightSource[0].position.xyz-point);\n",
				" vec3 E = normalize(-point);\n",
				" vec3 R = normalize(reflect(-L,normal));\n",
				" float diff = 0.3 * max(dot(normal,L), 0.0);\n",
				" float spec = 0.1 * pow(max(dot(R,E),0.0), 35.0);\n",
				" gl_FragColor = vec4( (1.0-influence)*color*(0.5+diff+spec)+influence*color, 1.0);\n",
				"}\n\n"};
		
		
		int phongFragmentShaderIndex = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
		gl.glShaderSource(phongFragmentShaderIndex, phongFragmentShader.length, phongFragmentShader, (int[])null, 0);
		gl.glCompileShader(phongFragmentShaderIndex);
		
		int vertexShaderIndex = gl.glCreateShader(GL.GL_VERTEX_SHADER);
		gl.glShaderSource(vertexShaderIndex, vertexShader.length, vertexShader, (int[])null, 0);
		gl.glCompileShader(vertexShaderIndex);

		
		byte[] chars = new byte[1000];
		int[] ints = new int[1];
		ints[0]=1000;
		gl.glGetShaderInfoLog(phongFragmentShaderIndex, 1000, ints , 0, chars , 0);
		System.out.println(new String(chars,0,1000));

		ints[0]=1000;
		gl.glGetShaderInfoLog(vertexShaderIndex, 1000, ints , 0, chars , 0);
		System.out.println(new String(chars,0,1000));


		
		int shaderprogram = gl.glCreateProgram();
		gl.glAttachShader(shaderprogram, vertexShaderIndex);
		gl.glAttachShader(shaderprogram, phongFragmentShaderIndex);
		gl.glLinkProgram(shaderprogram);
		gl.glValidateProgram(shaderprogram);
		gl.glUseProgram(shaderprogram);

		extrutionUniformLocation = gl.glGetUniformLocation(shaderprogram, "extrution");
		colorUniformLocation = gl.glGetUniformLocation(shaderprogram, "color");
		influenceUniformLocation = gl.glGetUniformLocation(shaderprogram, "influence");


	}

	@Override
	public void reshape(GLAutoDrawable drawable ,int x,int y, int w, int h) {
		// Setup wide screen view port
		GL gl = drawable.getGL();
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustum (-1.77777*zoom, 1.777777*zoom, -1.0*zoom, 1.0*zoom, 4.0, 100.0); 	
		this.height = h; this.width = w;
		this.drawHeight = (int)((double)width/1.77777);
		gl.glViewport (0, (int)((height-drawHeight)/2.0), (int)width, (int)drawHeight);
		// copy projection matrix (needed for pick ray)
		gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, proj, 0);
		
		// setup camera
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		// Set camera transform
		glu.gluLookAt(cameraFrom.x, cameraFrom.y, cameraFrom.z, 
				cameraTo.x, cameraTo.y, cameraTo.z, 
				0, 1, 0); 

		//copy camera transform (needed for picking)
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, camera, 0);
	}
	
	
	private Matrix4 getCameraMatrix() {
		return new Matrix4(camera);
	}

	private Matrix4 getProjectionMatrix() {		
		return new Matrix4(proj);
	}
	
	public void getPointerRay(Vector3 p, Vector3 d, double x, double y) {
		// clipping planes
		Vector3 near = new Vector3(2*x/(double)width-1,-2*(y-((height-drawHeight)*0.5))/(double)drawHeight+1, 0.7);
		Vector3 far = new Vector3(2*x/(double)width-1,-2*(y-((height-drawHeight)*0.5))/(double)drawHeight+1, 0.9);

		//inverse transform
		Matrix4 T = getProjectionMatrix().multiply(getCameraMatrix()).inverse();
	
		Vector3 p1 = new Vector3();
		Vector3 p2 = new Vector3();
		
		Matrix4.multiply(T,near,p1);
		Matrix4.multiply(T,far,p2);
		
		p.assign(p1);
		d.assign(p2.sub(p1).normalize());
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


	public void getCamera(Vector3 from, Vector3 to) {
		from.assign(cameraFrom);
		to.assign(cameraTo);
	}


	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {} 

	@Override
	public void mousePressed(MouseEvent e) {
		Vector3 p = new Vector3();
		Vector3 d = new Vector3();
		getPointerRay(p, d, e.getX(), e.getY());
		
		for (EventCallback call: this.mouseCallbacks)
			call.mousePressed((double)e.getX(), (double)e.getY(), p, d );
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		for (EventCallback call: this.mouseCallbacks)
			call.mouseReleased();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Vector3 p = new Vector3();
		Vector3 d = new Vector3();
		getPointerRay(p, d, e.getX(), e.getY());
		
		for (EventCallback call: this.mouseCallbacks)
			call.mouseDragged((double)e.getX(), (double)e.getY(), p, d );
	}

	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyChar()==' ') {
			for (EventCallback call: this.mouseCallbacks)
				call.spacePressed();
		}
		
		if ( arg0.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER ) {
			for (EventCallback call: this.mouseCallbacks)
				call.enterPressed();			
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		if (arg0.getKeyChar()==' ') {
			for (EventCallback call: this.mouseCallbacks)
				call.spaceReleased();
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {}

	@Override
	public void addCallback(EventCallback c) {
		mouseCallbacks.add(c);
	}

	@Override
	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public void takeScreenShot(String filename) {
		screenShotFilename = filename;
		takeScreenShot = true;		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		cameraClicks += e.getWheelRotation();
		System.out.println(cameraClicks);
		
		Vector3 direction = new Vector3(0,0.5,1).normalize();
		cameraTo.assign(new Vector3(-12,-3,0).add(direction.multiply(cameraClicks)));	
		cameraFrom.assign(cameraTo.add(direction));

		redoCamera = true;
		
	}





}
