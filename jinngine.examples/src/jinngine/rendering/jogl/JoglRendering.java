/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
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
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import jinngine.geometry.Box;
import jinngine.geometry.ConvexHull;
import jinngine.geometry.Geometry;
import jinngine.geometry.Sphere;
import jinngine.geometry.UniformCapsule;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.rendering.Rendering;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.Screenshot;

public class JoglRendering implements Rendering, GLEventListener, MouseListener, MouseMotionListener,
        MouseWheelListener, KeyListener {

    private static final long serialVersionUID = 1L;
    public List<DrawShape> toDraw = new ArrayList<DrawShape>();
    private final Callback callback;
    private final List<EventCallback> mouseCallbacks = new ArrayList<EventCallback>();
    private final GLCanvas canvas = new GLCanvas();
    private final Animator animator = new Animator(this.canvas);
    private final GLU glu = new GLU();
    private double width;
    private double height;
    private double drawHeight;
    private volatile boolean takeScreenShot = false;
    private volatile String screenShotFilename;
    private volatile boolean redoCamera;
    private static final Random rand = new Random();

    // camera transform
    public double[] proj = new double[16];
    public double[] camera = new double[16];
    public double zoom = 0.95;
    private final Vector3 cameraTo = new Vector3(-12, -3, 0).multiply(1);
    private final Vector3 cameraFrom = this.cameraTo.add(new Vector3(0, 0.5, 1).multiply(5));
    private int cameraClicks = 1;

    // global light0 position
    private final float position[] = { -5f, 20.0f, -25.0f, 1.0f };
    // private final float position[] = { 0f, 0.0f, -0.0f, 1.0f };

    // uniforms
    private int extrutionUniformLocation;
    private int colorUniformLocation;
    private int influenceUniformLocation;
    private double[] shadowProjMatrix;

    private final ConvexHull icosphere = buildIcosphere(1, 3);

    private boolean initialized = false;

    public JoglRendering(final Callback callback) {
        this.callback = callback;
        this.canvas.setSize(1024, (int) (1024 / 1.77777));
        this.canvas.setIgnoreRepaint(true);
        this.canvas.addGLEventListener(this);
        this.canvas.setVisible(true);
        this.canvas.addMouseListener(this);
        this.canvas.addMouseMotionListener(this);
        this.canvas.addMouseWheelListener(this);
        this.canvas.addKeyListener(this);
        this.canvas.setVisible(true);
    }

    public JoglRendering(final Callback callback, final int w, final int h) {
        this.callback = callback;
        this.canvas.setSize(w, h);
        this.canvas.setIgnoreRepaint(true);
        this.canvas.addGLEventListener(this);
        this.canvas.setVisible(true);
        this.canvas.addMouseListener(this);
        this.canvas.addMouseMotionListener(this);
        this.canvas.addMouseWheelListener(this);
        this.canvas.addKeyListener(this);
        this.canvas.setVisible(true);
    }

    @Override
    public void createWindow() {
        final Frame frame = new Frame();
        frame.setTitle("jinngine.example");
        frame.setSize(1024, (int) (1024 / 1.77777));
        // Setup exit function
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });

        frame.add(this.canvas, java.awt.BorderLayout.CENTER);
        frame.setVisible(true);
    }

    @Override
    public void drawMe(final DrawShape shape, final Geometry g) {
        final DrawShape newshape = new DrawShape() {
            @Override
            public void getTransform(final Matrix4 T) {
                T.assign(g.getWorldTransform());
            }

            @Override
            public Body getReferenceBody() {
                return g.getBody();
            }

            @Override
            public int getDisplayList() {
                return shape.getDisplayList();
            }

            @Override
            public void init(final GL gl) {
                // init is not needed
            }

            @Override
            public int getShadowDisplayList() {
                return shape.getShadowDisplayList();
            }

            @Override
            public void preDisplay(final GL gl) {
                // TODO Auto-generated method stub

            }
        };

        this.toDraw.add(newshape);
    }

    public DrawShape doShape(final Geometry g, final List<Vector3> vertices, final double radius) {
        if (radius > 0) {
            final List<Vector3> inputVertices = new ArrayList<Vector3>();
            final List<Vector3> inputNormals = new ArrayList<Vector3>();
            final List<Vector3> hullNormals = new ArrayList<Vector3>();
            final List<Vector3> hullVertices = new ArrayList<Vector3>();

            // add ico-spheres for each vertex in hull
            for (final Vector3 vertex : vertices) {
                final Iterator<Vector3> iter = this.icosphere.getVertices();
                while (iter.hasNext()) {
                    final Vector3 v = iter.next();
                    inputVertices.add(v.multiply(radius).add(vertex));
                    inputNormals.add(v.normalize());
                }
            }

            final ConvexHull hull = new ConvexHull("hull", inputVertices, 0.0);

            // build normal array
            for (final int index : hull.getOriginalVertexIndices()) {
                hullNormals.add(inputNormals.get(index));
            }

            // get the vertices in the final hull
            final Iterator<Vector3> i = hull.getVertices();
            while (i.hasNext()) {
                hullVertices.add(i.next());
            }

            return new DrawShape() {
                private boolean initable = true;
                private int list = 0;
                private int shadowList = 0;

                @Override
                public void getTransform(final Matrix4 T) {
                    T.assign(g.getWorldTransform());
                }

                @Override
                public Body getReferenceBody() {
                    return g.getBody();
                }

                @Override
                public int getDisplayList() {
                    return this.list;
                }

                @Override
                public void init(final GL gl) {
                    if (this.initable) {
                        this.list = startDisplayList(gl);
                        drawSmoothShape(hullVertices, hullNormals, hull.getFaceIndices(), gl);
                        endDisplayList(gl);

                        this.shadowList = startDisplayList(gl);
                        drawBackfaceShadowMesh(hullVertices, null, hull.getFaceIndices(), gl);
                        endDisplayList(gl);

                        this.initable = false;
                    }
                }

                @Override
                public int getShadowDisplayList() {
                    return this.shadowList;
                }

                @Override
                public void preDisplay(final GL gl) {
                    gl.glUniform1f(JoglRendering.this.extrutionUniformLocation,
                            -(float) (32f / JoglRendering.this.height));
                }
            };
        } else {

            return new DrawShape() {
                private int list = 0;
                private int shadowList = 0;

                @Override
                public void getTransform(final Matrix4 T) {
                    T.assign(g.getWorldTransform());
                }

                @Override
                public Body getReferenceBody() {
                    return g.getBody();
                }

                @Override
                public int getDisplayList() {
                    return this.list;
                }

                @Override
                public void init(final GL gl) {
                    final ConvexHull hull = new ConvexHull("hull", vertices, 0.0);

                    this.list = startDisplayList(gl);
                    drawPolygonShape(hull.getVerticesList(), null, hull.getFaceIndices(), gl);
                    endDisplayList(gl);

                    this.shadowList = startDisplayList(gl);
                    drawBackfaceShadowMesh(hull.getVerticesList(), null, hull.getFaceIndices(), gl);
                    endDisplayList(gl);
                }

                @Override
                public int getShadowDisplayList() {
                    return this.shadowList;
                }

                @Override
                public void preDisplay(final GL gl) {
                    // TODO Auto-generated method stub

                }
            };
        }
    }

    @Override
    public DrawShape drawMe(final Geometry g) {
        DrawShape shape = null;

        if (g instanceof ConvexHull) {
            final ConvexHull orghull = (ConvexHull) g;
            shape = doShape(orghull, orghull.getVerticesList(), orghull.sphereSweepRadius());
        }

        if (g instanceof Box) {
            final List<Vector3> inputVertices = new ArrayList<Vector3>();
            new ArrayList<Vector3>();
            inputVertices.add(new Vector3(0.5, 0.5, 0.5));
            inputVertices.add(new Vector3(-0.5, 0.5, 0.5));
            inputVertices.add(new Vector3(0.5, -0.5, 0.5));
            inputVertices.add(new Vector3(-0.5, -0.5, 0.5));
            inputVertices.add(new Vector3(0.5, 0.5, -0.5));
            inputVertices.add(new Vector3(-0.5, 0.5, -0.5));
            inputVertices.add(new Vector3(0.5, -0.5, -0.5));
            inputVertices.add(new Vector3(-0.5, -0.5, -0.5));

            // apply scaling to the box vertices
            final Matrix3 S = new Matrix3().assignScale(((Box) g).getDimentions());
            for (final Vector3 v : inputVertices) {
                v.assign(S.multiply(v));
            }

            // do the box shape
            shape = doShape(g, inputVertices, ((Box) g).sphereSweepRadius());

        }

        if (g instanceof UniformCapsule) {
            final UniformCapsule cap = (UniformCapsule) g;
            final List<Vector3> inputVertices = new ArrayList<Vector3>();

            inputVertices.add(new Vector3(0, 0, cap.getLength() / 2));
            inputVertices.add(new Vector3(0, 0, -cap.getLength() / 2));

            shape = doShape(g, inputVertices, ((UniformCapsule) g).sphereSweepRadius());
        }

        if (g instanceof Sphere) {
            final Sphere sphere = (Sphere) g;
            final List<Vector3> inputVertices = new ArrayList<Vector3>();
            inputVertices.add(new Vector3());
            shape = doShape(g, inputVertices, sphere.getRadius());

        }

        if (shape != null) {
            this.toDraw.add(shape);
            return shape;
        } else {
            throw new IllegalArgumentException("Unknown Geometry type");
        }
    }

    private ConvexHull buildIcosphere(final double r, final int depth) {
        final List<Vector3> vertices = new ArrayList<Vector3>();
        // vertices.add(new Vector3( 1, 1, 1).normalize());
        // vertices.add(new Vector3(-1,-1, 1).normalize());
        // vertices.add(new Vector3(-1, 1,-1).normalize());
        // vertices.add(new Vector3( 1,-1,-1).normalize());
        // point on icosahedron
        final double t = (1.0 + Math.sqrt(5.0)) / 2.0;
        vertices.add(new Vector3(-1, t, 0).normalize());
        vertices.add(new Vector3(1, t, 0).normalize());
        vertices.add(new Vector3(-1, -t, 0).normalize());
        vertices.add(new Vector3(1, -t, 0).normalize());
        vertices.add(new Vector3(0, -1, t).normalize());
        vertices.add(new Vector3(0, 1, t).normalize());
        vertices.add(new Vector3(0, -1, -t).normalize());
        vertices.add(new Vector3(0, 1, -t).normalize());
        vertices.add(new Vector3(t, 0, -1).normalize());
        vertices.add(new Vector3(t, 0, 1).normalize());
        vertices.add(new Vector3(-t, 0, -1).normalize());
        vertices.add(new Vector3(-t, 0, 1).normalize());

        int n = 0;
        while (true) {
            final ConvexHull hull = new ConvexHull("hull", vertices, 0.0);

            if (n >= depth) {
                return hull;
            }

            // for each face, add a new sphere support
            // point in direction of the face normal
            final Iterator<Vector3[]> iter = hull.getFaces();
            while (iter.hasNext()) {
                final Vector3[] face = iter.next();
                final Vector3 normal = face[1].sub(face[0]).cross(face[2].sub(face[1])).normalize();
                vertices.add(new Vector3(normal));
            }

            // depth level done
            n++;
        }
    }

    @Override
    public void start() {
        this.animator.start();
    }

    // transformation matrix
    final double[] transform = new double[4 * 4];
    final Matrix4 jMatrix4 = new Matrix4();

    @Override
    public void display(final GLAutoDrawable drawable) {
        final GL gl = drawable.getGL();

        // init all drawing objects
        if (!this.initialized) {
            // calculate shadow matrix
            this.shadowProjMatrix = shadowProjectionMatrix(new Vector3(0, 350, 0), new Vector3(0, -20 + 0.0, 0),
                    new Vector3(0, -1, 0));

            // init all display lists
            for (final DrawShape s : this.toDraw) {
                s.init(gl);
            }
            this.initialized = true;
        }

        if (this.redoCamera) {
            // setup camera
            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glLoadIdentity();

            // Set camera transform
            this.glu.gluLookAt(this.cameraFrom.x, this.cameraFrom.y, this.cameraFrom.z, this.cameraTo.x,
                    this.cameraTo.y, this.cameraTo.z, 0, 1, 0);

            // copy camera transform (needed for picking)
            gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, this.camera, 0);

            // set light position in world space
            gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, this.position, 0);

            // camera done
            this.redoCamera = false;
        }

        // Perform ratio time-steps on the model
        this.callback.tick();
        // callback.tick();
        // callback.tick();
        // callback.tick();

        // Clear buffer, etc.
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);

        for (final DrawShape shape : this.toDraw) {
            gl.glPushMatrix();
            shape.getTransform(this.jMatrix4);
            this.jMatrix4.toArray(this.transform);
            gl.glMultMatrixd(this.transform, 0);
            shape.preDisplay(gl);
            gl.glCallList(shape.getDisplayList());
            gl.glPopMatrix();

            // draw projected shadow
            gl.glPushMatrix();
            gl.glMultMatrixd(this.shadowProjMatrix, 0);
            gl.glMultMatrixd(this.transform, 0);
            gl.glCallList(shape.getShadowDisplayList());
            gl.glPopMatrix();
        }

        // Finish this frame
        gl.glFlush();

        // take screenshot
        if (this.takeScreenShot) {
            gl.glFinish();

            this.takeScreenShot = false;
            try {
                Screenshot.writeToTargaFile(new File(this.screenShotFilename), (int) this.width, (int) this.height);
            } catch (final GLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private void drawEdgeMesh(final ConvexHull hull, final GL gl) {
        // build level 0 ico-sphere for edge endings
        final ConvexHull icosphere = buildIcosphere(1, 0);

        // grap all edges in the hull
        final List<Vector3> vertices = hull.getVerticesList();
        final ArrayList<ArrayList<Integer>> adjacent = hull.getVertexAdjacencyMatrix();
        int i = 0;
        for (final ArrayList<Integer> indices : adjacent) {
            for (final int j : indices) {
                // for each edge, create a thick line hull
                final List<Vector3> inputEdgeVertices = new ArrayList<Vector3>();
                final List<Vector3> inputEdgeNormals = new ArrayList<Vector3>();

                // System.out.println("edge " + vertices.get(i) +","+vertices.get(j));

                for (final Vector3 p : icosphere.getVerticesList()) {
                    // at i
                    final Vector3 normal = p.normalize();
                    inputEdgeVertices.add(normal.add(vertices.get(i)));
                    inputEdgeNormals.add(normal);

                    // at j
                    inputEdgeVertices.add(normal.add(vertices.get(j)));
                    inputEdgeNormals.add(normal);

                }
                // build a hull for this edge
                final ConvexHull edgehull = new ConvexHull("hull", inputEdgeVertices, 0.0);

                // build normal array
                final List<Vector3> hullNormals = new ArrayList<Vector3>();
                for (final int index : edgehull.getOriginalVertexIndices()) {
                    hullNormals.add(inputEdgeNormals.get(index));
                }

                // collapse edge mesh (extrudet later by vertex shader)
                int k = 0;
                for (final Vector3 v : edgehull.getVerticesList()) {
                    v.assign(v.sub(hullNormals.get(k)));
                    k++;
                }

                // draw this edge
                drawFaces(edgehull.getVerticesList(), hullNormals, edgehull.getFaceIndices(), gl);
            }
            i = i + 1;
        }

    }

    private void drawFaces(final List<Vector3> vertices, final List<Vector3> normals, final int[][] faceIndices,
            final GL gl) {
        for (final int[] face : faceIndices) {
            gl.glBegin(GL.GL_POLYGON);

            final Vector3 n = new Vector3();

            // compute normal face
            if (normals == null) {
                final Vector3 v1 = vertices.get(face[0]);
                final Vector3 v2 = vertices.get(face[1]);
                final Vector3 v3 = vertices.get(face[2]);
                n.assign(v2.sub(v1).cross(v3.sub(v2)).normalize());
            }

            for (final int index : face) {
                final Vector3 v = vertices.get(index);

                // if normals are given, use them (if not, use face normal)
                if (normals != null) {
                    n.assign(normals.get(index));
                }

                gl.glNormal3d(n.x, n.y, n.z);
                gl.glVertex3d(v.x, v.y, v.z);
            }
            gl.glEnd();
        }
    }

    private int startDisplayList(final GL gl) {
        final int displayList = gl.glGenLists(1);
        gl.glNewList(displayList, GL.GL_COMPILE);
        return displayList;
    }

    private void endDisplayList(final GL gl) {
        // end display list
        gl.glEndList();
    }

    int colorCount = 0;

    private void drawSmoothShape(final List<Vector3> vertices, final List<Vector3> normals, final int[][] faceIndices,
            final GL gl) {
        // draw shaded mesh
        //        gl.glUniform1f(this.extrutionUniformLocation, -0.045f);
        //        gl.glUniform1f(this.extrutionUniformLocation, -(float) (32f / this.height));
        //        gl.glUniform3f(colorUniformLocation, 1f, 0.95f, 1f);

        final float[][] color = { { 256 / 256f, 26 / 256f, 171 / 256f }, { 155 / 256f, 256 / 256f, 26 / 256f },
                { 0, 0.5f, 1 }, { 66 / 256f, 229 / 256f, 227 / 256f }, { 169 / 256f, 55 / 256f, 222 / 256f },
                { 63 / 256f, 189 / 256f, 225 / 256f }, { 255 / 256f, 123 / 256f, 131 / 256f } };
        final float shade = 1 - rand.nextFloat() * 0.0f;
        final int index = this.colorCount++ % color.length;
        gl.glUniform3f(this.colorUniformLocation, shade * color[index][0], shade * color[index][1], shade
                * color[index][2]);
        //        gl.glUniform3f(this.colorUniformLocation, 1f - shade, 1f - shade2, 1f - shade3);

        gl.glUniform1f(this.influenceUniformLocation, 0);
        gl.glCullFace(GL.GL_BACK);
        drawFaces(vertices, normals, faceIndices, gl);

        // draw silhouette
        gl.glUniform1f(this.extrutionUniformLocation, -0.00f);
        gl.glUniform1f(this.influenceUniformLocation, 01f);
        gl.glUniform3f(this.colorUniformLocation, 0.15f, 0.15f, 0.15f);
        gl.glCullFace(GL.GL_FRONT);
        drawFaces(vertices, normals, faceIndices, gl);
        // drawEdgeMesh( new ConvexHull(vertices), gl);
    }

    private void drawPolygonShape(final List<Vector3> vertices, final List<Vector3> normals, final int[][] faceIndices,
            final GL gl) {
        // draw shaded mesh
        gl.glUniform1f(this.extrutionUniformLocation, 0);
        gl.glUniform1f(this.influenceUniformLocation, 0);
        final float shade = rand.nextFloat() * 0.2f;
        gl.glUniform3f(this.colorUniformLocation, 1f - shade, 1f - shade, 1f - shade);
        //        gl.glUniform3f(colorUniformLocation, 0.95f, 0.95f, 1f);

        gl.glCullFace(GL.GL_BACK);
        drawFaces(vertices, normals, faceIndices, gl);

        // draw solid coloured edge mesh
        gl.glUniform1f(this.extrutionUniformLocation, 0.03f);
        gl.glUniform3f(this.colorUniformLocation, 0.30f, 0.30f, 0.30f);
        gl.glUniform1f(this.influenceUniformLocation, 1f);
        drawEdgeMesh(new ConvexHull("edge hull", vertices, 0.0), gl);
    }

    private void drawBackfaceShadowMesh(final List<Vector3> vertices, final List<Vector3> normals,
            final int[][] faceIndices, final GL gl) {
        gl.glUniform1f(this.extrutionUniformLocation, 0);
        gl.glUniform1f(this.influenceUniformLocation, 1);
        gl.glUniform3f(this.colorUniformLocation, 0.85f, 0.85f, 0.85f);
        gl.glCullFace(GL.GL_FRONT);
        drawFaces(vertices, normals, faceIndices, gl);
    }

    @Override
    public void displayChanged(final GLAutoDrawable arg0, final boolean arg1, final boolean arg2) {}

    @Override
    public void init(final GLAutoDrawable drawable) {
        // Setup GL
        final GL gl = drawable.getGL();
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        // enable vsync
        gl.setSwapInterval(1);

        // init some lighting
        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_LIGHT0);

        // Create light components
        final float ambientLight[] = { 2.0f, 2.0f, 2.0f, 1.0f };
        final float diffuseLight[] = { 0.2f, 0.2f, 0.2f, 1.0f };
        final float specularLight[] = { 0.5f, 0.5f, 0.5f, 1.0f };

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        // Set camera transform
        this.glu.gluLookAt(this.cameraFrom.x, this.cameraFrom.y, this.cameraFrom.z, this.cameraTo.x, this.cameraTo.y,
                this.cameraTo.z, 0, 1, 0);

        // copy camera transform
        gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, this.camera, 0);

        // Assign created components to GL_LIGHT0
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, ambientLight, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, diffuseLight, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, specularLight, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, this.position, 0);

        final String[] vertexShader = { "uniform float extrution;\n ", "varying vec3 point;   \n",
                "varying vec3 normal;  \n", "void main(void) {     \n",
                "  normal = normalize(gl_NormalMatrix * gl_Normal); \n",
                "  point = vec3(gl_ModelViewMatrix * gl_Vertex); \n",
                "  vec4 newpos = gl_ModelViewProjectionMatrix * gl_Vertex; \n",
                "  gl_Position = gl_ModelViewProjectionMatrix * vec4(newpos.w*extrution*0.04*gl_Normal,0) + newpos;\n",
                "}\n\n" };

        final String[] phongFragmentShader = { "uniform vec3 color;\n", "uniform float influence;\n",
                "varying vec3 point;\n", "varying vec3 normal;\n", "void main(void) { \n",
                " vec3 L = normalize(gl_LightSource[0].position.xyz-point);\n", " vec3 E = normalize(-point);\n",
                " vec3 R = normalize(reflect(-L,normal));\n", " float diff = 0.3 * max(dot(normal,L), 0.0);\n",
                //                " float spec = 0.2 * pow(max(dot(R,E),0.0), 35.0);\n",
                " float spec = 0.27 * pow(max(dot(R,E),0.0), 55.0);\n",
                " gl_FragColor = vec4( (1.0-influence)*color*(0.5+diff+spec)+influence*color, 1.0);\n", "}\n\n" };

        final int phongFragmentShaderIndex = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
        gl.glShaderSource(phongFragmentShaderIndex, phongFragmentShader.length, phongFragmentShader, (int[]) null, 0);
        gl.glCompileShader(phongFragmentShaderIndex);

        final int vertexShaderIndex = gl.glCreateShader(GL.GL_VERTEX_SHADER);
        gl.glShaderSource(vertexShaderIndex, vertexShader.length, vertexShader, (int[]) null, 0);
        gl.glCompileShader(vertexShaderIndex);

        final byte[] chars = new byte[1000];
        final int[] ints = new int[1];
        ints[0] = 1000;
        gl.glGetShaderInfoLog(phongFragmentShaderIndex, 1000, ints, 0, chars, 0);
        System.out.println(new String(chars, 0, 1000));

        ints[0] = 1000;
        gl.glGetShaderInfoLog(vertexShaderIndex, 1000, ints, 0, chars, 0);
        System.out.println(new String(chars, 0, 1000));

        final int shaderprogram = gl.glCreateProgram();
        gl.glAttachShader(shaderprogram, vertexShaderIndex);
        gl.glAttachShader(shaderprogram, phongFragmentShaderIndex);
        gl.glLinkProgram(shaderprogram);
        gl.glValidateProgram(shaderprogram);
        gl.glUseProgram(shaderprogram);

        this.extrutionUniformLocation = gl.glGetUniformLocation(shaderprogram, "extrution");
        this.colorUniformLocation = gl.glGetUniformLocation(shaderprogram, "color");
        this.influenceUniformLocation = gl.glGetUniformLocation(shaderprogram, "influence");

    }

    @Override
    public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int w, final int h) {
        // Setup wide screen view port
        final GL gl = drawable.getGL();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        this.height = h;
        this.width = w;
        final double ratio = (double) w / (double) h;

        gl.glFrustum(-ratio * this.zoom, ratio * this.zoom, -1.0 * this.zoom, 1.0 * this.zoom, 4.0, 200.0);
        //        this.drawHeight = (int) (this.width / ratio);
        //        System.out.println("drawHeight=" + this.drawHeight + " ratio=" + ratio);
        this.drawHeight = (int) this.height;

        gl.glViewport(0, (int) ((this.height - this.drawHeight) / 2.0), (int) this.width, (int) this.drawHeight);
        // copy projection matrix (needed for pick ray)
        gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, this.proj, 0);

        // setup camera
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        // Set camera transform
        this.glu.gluLookAt(this.cameraFrom.x, this.cameraFrom.y, this.cameraFrom.z, this.cameraTo.x, this.cameraTo.y,
                this.cameraTo.z, 0, 1, 0);

        // copy camera transform (needed for picking)
        gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, this.camera, 0);
    }

    private Matrix4 getCameraMatrix() {
        return new Matrix4(this.camera);
    }

    private Matrix4 getProjectionMatrix() {
        return new Matrix4(this.proj);
    }

    public void getPointerRay(final Vector3 p, final Vector3 d, final double x, final double y) {
        // clipping planes
        final Vector3 near = new Vector3(2 * x / this.width - 1, -2 * (y - (this.height - this.drawHeight) * 0.5)
                / this.drawHeight + 1, 0.7);
        final Vector3 far = new Vector3(2 * x / this.width - 1, -2 * (y - (this.height - this.drawHeight) * 0.5)
                / this.drawHeight + 1, 0.9);

        // inverse transform
        final Matrix4 T = getProjectionMatrix().multiply(getCameraMatrix()).inverse();

        final Vector3 p1 = new Vector3();
        final Vector3 p2 = new Vector3();

        Matrix4.multiply(T, near, p1);
        Matrix4.multiply(T, far, p2);

        p.assign(p1);
        d.assign(p2.sub(p1).normalize());
    }

    /**
     * This is where the "magic" is done:
     * 
     * Multiply the current ModelView-Matrix with a shadow-projetion matrix.
     * 
     * l is the position of the light source e is a point on within the plane on
     * which the shadow is to be projected. n is the normal vector of the plane.
     * 
     * Everything that is drawn after this call is "squashed" down to the plane.
     * Hint: Gray or black color and no lighting looks good for shadows *g*
     */
    private double[] shadowProjectionMatrix(final Vector3 l, final Vector3 e, final Vector3 n) {
        double d, c;
        final double[] mat = new double[16];

        // These are c and d (corresponding to the tutorial)

        d = n.x * l.x + n.y * l.y + n.z * l.z;
        c = e.x * n.x + e.y * n.y + e.z * n.z - d;

        // Create the matrix. OpenGL uses column by column
        // ordering

        mat[0] = l.x * n.x + c;
        mat[4] = n.y * l.x;
        mat[8] = n.z * l.x;
        mat[12] = -l.x * c - l.x * d;

        mat[1] = n.x * l.y;
        mat[5] = l.y * n.y + c;
        mat[9] = n.z * l.y;
        mat[13] = -l.y * c - l.y * d;

        mat[2] = n.x * l.z;
        mat[6] = n.y * l.z;
        mat[10] = l.z * n.z + c;
        mat[14] = -l.z * c - l.z * d;

        mat[3] = n.x;
        mat[7] = n.y;
        mat[11] = n.z;
        mat[15] = -d;

        return mat;
    }

    public void getCamera(final Vector3 from, final Vector3 to) {
        from.assign(this.cameraFrom);
        to.assign(this.cameraTo);
    }

    @Override
    public void mouseClicked(final MouseEvent e) {}

    @Override
    public void mouseEntered(final MouseEvent e) {}

    @Override
    public void mouseExited(final MouseEvent e) {}

    @Override
    public void mousePressed(final MouseEvent e) {
        final Vector3 p = new Vector3();
        final Vector3 d = new Vector3();
        getPointerRay(p, d, e.getX(), e.getY());

        for (final EventCallback call : this.mouseCallbacks) {
            call.mousePressed(e.getX(), e.getY(), p, d);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        for (final EventCallback call : this.mouseCallbacks) {
            call.mouseReleased();
        }
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        final Vector3 p = new Vector3();
        final Vector3 d = new Vector3();
        getPointerRay(p, d, e.getX(), e.getY());

        for (final EventCallback call : this.mouseCallbacks) {
            call.mouseDragged(e.getX(), e.getY(), p, d);
        }
    }

    @Override
    public void mouseMoved(final MouseEvent e) {}

    @Override
    public void keyPressed(final KeyEvent arg0) {
        if (arg0.getKeyChar() == ' ') {
            for (final EventCallback call : this.mouseCallbacks) {
                call.spacePressed();
            }
        }

        if (arg0.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            for (final EventCallback call : this.mouseCallbacks) {
                call.enterPressed();
            }
        }

        for (final EventCallback call : this.mouseCallbacks) {
            call.keyPressed(arg0.getKeyChar());
        }

    }

    @Override
    public void keyReleased(final KeyEvent arg0) {
        if (arg0.getKeyChar() == ' ') {
            for (final EventCallback call : this.mouseCallbacks) {
                call.spaceReleased();
            }
        }

        for (final EventCallback call : this.mouseCallbacks) {
            call.keyReleased(arg0.getKeyChar());
        }

    }

    @Override
    public void keyTyped(final KeyEvent arg0) {}

    @Override
    public void addCallback(final EventCallback c) {
        this.mouseCallbacks.add(c);
    }

    @Override
    public Canvas getCanvas() {
        return this.canvas;
    }

    @Override
    public void takeScreenShot(final String filename) {
        this.screenShotFilename = filename;
        this.takeScreenShot = true;
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        this.cameraClicks += e.getWheelRotation();

        final Vector3 direction = new Vector3(0, 0.5, 1).normalize();
        this.cameraTo.assign(new Vector3(-12, -3, 0).add(direction.multiply(this.cameraClicks * 5)));
        this.cameraFrom.assign(this.cameraTo.add(direction));

        this.redoCamera = true;

    }

}
