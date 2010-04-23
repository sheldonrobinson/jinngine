/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package jinngine.game;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import sun.java2d.pipe.GlyphListLoopPipe;

import jinngine.physics.Body;
import jinngine.physics.DefaultScene;
import jinngine.physics.force.GravityForce;
import jinngine.geometry.*;

import com.ardor3d.bounding.BoundingBox;
//import com.ardor3d.example.ExampleBase;
import com.ardor3d.extension.effect.bloom.BloomRenderPass;
import com.ardor3d.extension.effect.water.WaterNode;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.extension.shadow.map.ParallelSplitShadowMapPass;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.Updater;
import com.ardor3d.framework.jogl.JoglCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.Image.Format;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.light.PointLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextCapabilities;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRenderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.jogl.JoglTextureRendererProvider;
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.pass.OutlinePass;
import com.ardor3d.renderer.pass.Pass;
import com.ardor3d.renderer.pass.RenderPass;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.GLSLShaderDataLogic;
import com.ardor3d.renderer.state.GLSLShaderObjectsState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.ShadingState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.renderer.state.CullState.Face;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Renderable;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.shape.GeoSphere.TextureMode;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.Timer;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;
import com.ardor3d.util.scenegraph.DisplayListDelegate;
import com.ardor3d.util.shader.ShaderVariable;

/**
 * <p>
 * This jogl-based example is meant to show how to use Ardor3D at the most primitive level, forsaking the use of Guice
 * and most of our framework classes and interfaces.
 * </p>
 * 
 * <p>
 * Also of note, this example does not allow choosing of properties on launch. It also does not handle input or show any
 * special debugging. This is to simplify the example to the basic essentials.
 * </p>
 */
public class JoglNoGuiceBoxExample implements Scene {

    // Our native window, not the gl surface itself.
    private final JoglCanvas _canvas;

    // Our timer.
    private final Timer _timer = new Timer();

    // A boolean allowing us to "pull the plug" from anywhere.
    private boolean _exit = false;

    // The root of our scene
    private final Node _root = new Node();
    
    private Node colladaNode;
    private Box _box3;
    private Node _box2;
    private CullState culling;
    private TextureState ts; 
    
    private GLSLShaderObjectsState shader;
    
    private  Box floornode ;
    
   private  TextureRenderer tRenderer;
   private Texture2D mytexture = new Texture2D();
   private Texture2D mytexture2 = new Texture2D();
   
   private OutlinePass outline;

    
    //jinngine
    DefaultScene engine = new DefaultScene();
    
    BasicPassManager _passManager = new BasicPassManager();

    ParallelSplitShadowMapPass _pssmPass;

    public static void main(final String[] args) {
        final JoglNoGuiceBoxExample example = new JoglNoGuiceBoxExample();
        example.start();
    }

    /**
     * Constructs the example class, also creating the native window and GL surface.
     */
    public JoglNoGuiceBoxExample() {
        _canvas = initJogl();
        _canvas.init();
        engine.setTimestep(0.08);
    }

    /**
     * Kicks off the example logic, first setting up the scene, then continuously updating and rendering it until exit
     * is flagged. Afterwards, the scene and gl surface are cleaned up.
     */
    private void start() {

        initExample();

        // Run in this same thread.
        while (!_exit) {
            updateExample();
            _canvas.draw(null);
            Thread.yield();
        }
    
        _canvas.getCanvasRenderer().setCurrentContext();

        // Done, do cleanup
        ContextGarbageCollector.doFinalCleanup(_canvas.getCanvasRenderer().getRenderer());
        _canvas.close();
    }

    /**
     * Setup a jogl canvas and canvas renderer.
     * 
     * @return the canvas.
     */
    
    
    private JoglCanvas initJogl() {
        final JoglCanvasRenderer canvasRenderer = new JoglCanvasRenderer(this);
        final DisplaySettings settings = new DisplaySettings((int)(600*(16.0/9.0)), 600, 16, 0, 0, 8, 0, 0, false, false);
//        final DisplaySettings settings = new DisplaySettings(640, 480, 16, 0, 0, 8, 0, 0, true, false);

        //ContextManager.addContext(contextKey, RenderContext)
        TextureRendererFactory.INSTANCE.setProvider(new JoglTextureRendererProvider());
        

        return new JoglCanvas(canvasRenderer, settings);
    }

    /**
     * Initialize our scene.
     */
    private void initExample() {
    	
        // Set the location of our example resources.
        try {
            final SimpleResourceLocator srl = new SimpleResourceLocator(new URI("file:///home/mo/workspace/jinngine.game/"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, srl);
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, srl);
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_SHADER, srl);
            

        } catch (final URISyntaxException ex) {
            ex.printStackTrace();
        }


    	
    	//jinngine
		Body floor = new Body("default", new jinngine.geometry.Box(1500,10,1500));
		floor.setPosition(new jinngine.math.Vector3(0,-25,0));
		//floor.state.q.assign(jinngine.math.Quaternion.rotation(-0.04, jinngine.math.Vector3.k));
		floor.setFixed(true);
		
//		Body back = new Body( new jinngine.geometry.Box(200,200,2));		
//		back.setPosition(new jinngine.math.Vector3(0,0,-45));
//		back.setFixed(true);
//
//		Body front = new Body( new jinngine.geometry.Box(200,200,2));		
//		front.setPosition(new jinngine.math.Vector3(0,0,-15));
//		front.setFixed(true);
//
//		Body left = new Body( new jinngine.geometry.Box(2,200,200));		
//		left.setPosition(new jinngine.math.Vector3(-25,0,0));
//		left.setFixed(true);
//
//		Body right = new Body( new jinngine.geometry.Box(2,200,200));		
//		right.setPosition(new jinngine.math.Vector3(0,0,0));
//		right.setFixed(true);
//
//		engine.addBody(left);
//		engine.addBody(right);
//		engine.addBody(front);
		engine.addBody(floor);
//		engine.addBody(back);
    	
    	
//		final Body box = new Body(new jinngine.geometry.Sphere(6));
		final Body box = new Body("default", new jinngine.geometry.Box(6,6,6));
    	engine.addBody(box);
    	//engine.addForce(new GravityForce(box));
    	box.setPosition(new jinngine.math.Vector3(-1,0,-19));

		final Body box2 = new Body("default", new jinngine.geometry.Box(2,2,2));
    	engine.addBody(box2);
    	box2.setAngularVelocity(new jinngine.math.Vector3(0.0,1.4,-1));
    	box2.setVelocity(new jinngine.math.Vector3(0,-0.1,0));
    	engine.addForce(new GravityForce(box2));
    	box2.setPosition(new jinngine.math.Vector3(0,-14,-2));


		final Body boxmonkey = new Body("default", new jinngine.geometry.Sphere(1));
//		final Body boxmonkey = new Body(new jinngine.geometry.Sphere(1.5));

		//boxmonkey.setMass(0.05);
    	//boxmonkey.setAngularVelocity(new jinngine.math.Vector3(0.0,0.0, 0));

		boxmonkey.finalize();
    	engine.addBody(boxmonkey);
    	engine.addForce(new GravityForce(boxmonkey));
    	boxmonkey.setPosition(new jinngine.math.Vector3(-5,-14, 0));
    	boxmonkey.setVelocity(new jinngine.math.Vector3(0,1,0));
    	boxmonkey.setAngularVelocity(new jinngine.math.Vector3(-1.0,1.4,-0));

    	
        _canvas.setTitle("JoglNoGuiceBoxExample - close window to exit");
        _canvas.getCanvasRenderer().getCamera().setLocation(5, -8, 12);
      
        _canvas.getCanvasRenderer().getCamera().setFrustumPerspective(25, 16.0/9.0, 1, 1500);
        _canvas.getCanvasRenderer().getCamera().lookAt(0, -18, 0, Vector3.UNIT_Y);
    //    _canvas.getCanvasRenderer().getCamera().set
        
        _canvas.getCanvasRenderer().getRenderer().setBackgroundColor(new ColorRGBA(1,1,1,1));

        
        
        // Make a box...
         _box3 = new Box("Box", Vector3.ZERO, 1, 1, 1);
   //  final com.ardor3d.scenegraph.shape.Sphere _box3 = new com.ardor3d.scenegraph.shape.Sphere("Sphere", 10, 10, 20);

       //  _box3 = new ColladaImporter().readColladaFile("box.dae").getScene();
        // Make it a bit more colorful.
        //_box.setRandomColors();

         
        // Setup a bounding box for it.
        _box3.setModelBound(new BoundingBox()); 
        
        MeshData _meshData= _box3.getMeshData();
 
        if (_meshData.getTangentBuffer() == null) {
            _meshData.setTangentBuffer(BufferUtils.createVector3Buffer(24));

            // back
            for (int i = 0; i < 4; i++) {
//                _meshData.getNormalBuffer().put(0).put(0).put(-1);
                _meshData.getTangentBuffer().put(0).put(1).put(0);

            }

            // right
            for (int i = 0; i < 4; i++) {
               // _meshData.getNormalBuffer().put(1).put(0).put(0);
                _meshData.getTangentBuffer().put(0).put(1).put(0);

            }

            // front
            for (int i = 0; i < 4; i++) {
//                _meshData.getNormalBuffer().put(0).put(0).put(1);
                _meshData.getTangentBuffer().put(0).put(1).put(0);

            }

            // left
            for (int i = 0; i < 4; i++) {
//                _meshData.getNormalBuffer().put(-1).put(0).put(0);
                _meshData.getTangentBuffer().put(0).put(1).put(0);

            }

            // top
            for (int i = 0; i < 4; i++) {
//                _meshData.getNormalBuffer().put(0).put(1).put(0);
                _meshData.getTangentBuffer().put(1).put(0).put(0);

            }

            // bottom
            for (int i = 0; i < 4; i++) {
//                _meshData.getNormalBuffer().put(0).put(-1).put(0);
              _meshData.getTangentBuffer().put(-1).put(0).put(0);

            }
        }


        // Set its location in space.
        _box3.setTranslation(new Vector3(4, -5, 0));
        _root.attachChild(_box3);
        
        
        final ColladaImporter colladaImporter = new ColladaImporter();
        
        final ColladaStorage storage2 = colladaImporter.readColladaFile("platformbox1.dae");
        _box2 = storage2.getScene();
        
        // Make a box...
        //_box2 = new Box("Box", Vector3.ZERO, 1, 1, 1);

        // Make it a bit more colorful.
        //_box.setRandomColors();

        // Setup a bounding box for it.
        //_box2.setModelBound(new BoundingBox());

        // Set its location in space.
        _box2.setTranslation(new Vector3(0, -19, -2));
        
        // Add to root.
        _root.attachChild(_box2);
        
        
        
        
        
        // Make a box...
       final Box _box = new Box("Box", Vector3.ZERO, 3, 3, 3);
//        final com.ardor3d.scenegraph.shape.Sphere _box = new com.ardor3d.scenegraph.shape.Sphere("sphere", Vector3.ZERO, 50, 50, 6);

        
        // Make it a bit more colorful.
        //_box.setRandomColors();

        // Setup a bounding box for it.
        _box.setModelBound(new BoundingBox());

        
        // Set its location in space.
        _box.setTranslation(new Vector3(0, 2, -15));

        // Add to root.
        _root.attachChild(_box);
        

        floornode = new Box("box", new Vector3(0,-25,0), 10,5,10);
        floornode.setModelBound(new BoundingBox());
        _root.attachChild(floornode);
        
        //duck
        final ColladaStorage storage = colladaImporter.readColladaFile("bearface.dae");
         colladaNode = storage.getScene();

         

         
        // Set dynamic node culling
        //colladaNode.getSceneHints().setCullHint(showMesh ? CullHint.Dynamic : CullHint.Always);

        // Uncomment for debugging the scenegraph
        // final ScenegraphTree tree = new ScenegraphTree();
        // tree.show(colladaNode);

        //System.out.println("Importing: " + file);
        //System.out.println("Took " + (System.currentTimeMillis() - time) + " ms");
//         final MaterialState ms = new MaterialState();
//         ms.setColorMaterial(ColorMaterial.Specular);
//         ms.setDiffuse(new ColorRGBA(0.0f,1.0f,1.0f,1.0f));
//         colladaNode.setRenderState(ms);
         for (RenderState rs: colladaNode.getLocalRenderStates().values()) {
        	 System.out.println( ""+rs);
         }
         
        // colladaNode.setRenderState(ts);
        // Add colladaNode to root
        _root.attachChild(colladaNode);
        //colladaNode.setScale(7);
//        colladaNode.setre

        
        colladaNode.addController(new SpatialController<Node>() {
			@Override
			public void update(double time, Node caller) {
            	caller.setTranslation(boxmonkey.state.position.x, boxmonkey.state.position.y, boxmonkey.state.position.z);
            	ReadOnlyMatrix3 mat = new Matrix3(boxmonkey.state.rotation.a11, boxmonkey.state.rotation.a12, boxmonkey.state.rotation.a13,
            			boxmonkey.state.rotation.a21, boxmonkey.state.rotation.a22, boxmonkey.state.rotation.a23, 
            			boxmonkey.state.rotation.a31, boxmonkey.state.rotation.a32, boxmonkey.state.rotation.a33);
            	
                caller.setRotation(mat);
                caller.setScale(2);
			} 
        	
        });
        
        
        
        //try to make a texture renderer
        //final ContextCapabilities caps = ContextManager.getCurrentContext().getCapabilities();
        final ContextCapabilities caps =  _canvas.getCanvasRenderer().getRenderContext().getCapabilities();
        Camera cam = _canvas.getCanvasRenderer().getCamera();
       tRenderer = TextureRendererFactory.INSTANCE.createTextureRenderer( //
                    (int)(600*(16.0/9.0)), // width
                    (int)(600), // height
                    8, // Depth bits... TODO: Make configurable?
                    0, // Samples... TODO: Make configurable?
                    _canvas.getCanvasRenderer().getRenderer(), caps);

       
//        tRenderer.getCamera().setFrustum(cam.getFrustumNear(), cam.getFrustumFar(), cam.getFrustumLeft(),
//                cam.getFrustumRight(), cam.getFrustumTop(), cam.getFrustumBottom());
        
       
        tRenderer.getCamera().set(cam);
        
       // tRenderer.getCamera().lookAt(colladaNode.getTranslation(), Vector3.UNIT_Y);
        
  
        // Create a texture from the Ardor3D logo.
        ts = new TextureState();
        ts.setEnabled(true);
//        ts.setTexture(TextureManager.load("balllowtex.tga", Texture.MinificationFilter.Trilinear,
//                Format.GuessNoCompression, true),0);
//
//        ts.setTexture(TextureManager.load("balllownormalmap.tga", Texture.MinificationFilter.BilinearNoMipMaps,
//                Format.GuessNoCompression, true),1);

        ts.setTexture(TextureManager.load("bearlowtex.tga", Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression, true),0);

        ts.setTexture(TextureManager.load("bearlowpolynormalmap.tga", Texture.MinificationFilter.BilinearNoMipMaps,
                Format.GuessNoCompression, true),1);

//        ts.setTexture(TextureManager.load("facepaintlow.tga", Texture.MinificationFilter.Trilinear,
//                Format.GuessNoCompression, true),0);
//
//        ts.setTexture(TextureManager.load("lowpolynormalmap.tga", Texture.MinificationFilter.BilinearNoMipMaps,
//                Format.GuessNoCompression, true),1);

        
        ts.getTexture(0).setWrap(Texture.WrapMode.Repeat);
        ts.getTexture(0).setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        
        ts.getTexture(1).setWrap(Texture.WrapMode.Repeat);
        ts.getTexture(1).setMagnificationFilter(Texture.MagnificationFilter.Bilinear);

        
        //mytexture.setEnvironmentalMapMode(Texture.EnvironmentalMapMode.ObjectLinear);
      //  ts.getTexture().setEnvironmentalMapMode(Texture.EnvironmentalMapMode.ReflectionMap);

//        ts.setTexture(mytexture,0); 
        
   //     _box3.setRenderState(ts);
        

//        final TextureState ts2 = new TextureState();
//        ts2.setTexture(TextureManager.load("ardor3d_white_256.tga", Texture.MinificationFilter.Trilinear,
//                Format.Guess, true),1);
//
//        ts2.setEnabled(true);
//
//        _box3.setRenderState(ts2);

        
        //ts.getTexture().
       // ts.s
        
        shader = new GLSLShaderObjectsState();
        shader.setEnabled(true);
        
        String vertex = 
        	"\n"+
        	"attribute vec3 Mytangent;\n"+
        	"varying vec3 T;\n" +
        	"varying vec3 N;\n"+
        	"varying vec3 v;\n\n"+
        	"void main(void) \n" +
        	"{\n" +
        	"  v = vec3(gl_ModelViewMatrix * gl_Vertex); \n"+
        	"  N = normalize(gl_NormalMatrix * gl_Normal); \n" +
        	"  T = normalize(gl_NormalMatrix * Mytangent);\n" +
        	//        " gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;\n" +
        	//        "gl_TexCoord[1] = gl_MultiTexCoord1;\n" +
        	"  gl_TexCoord[0] = gl_MultiTexCoord0;\n" +

        	"  gl_Position = ftransform(); \n" +
        	"}\n";

        String simplefragment = " void main(void) { gl_FragColor= vec4(1.0,0.0,0.0,0.0); }\n";
        String fragment = 
        	//"attribute vec3 tangent;\n"+
        	"uniform bool doreflection;\n"+
        	"varying vec3 N;\n"+
        	"varying vec3 T;\n"+
            "varying vec3 v;\n"+
            "\n"+
        	"uniform sampler2D myTexture;\n" +
//                "uniform sampler2D overlay;\n" +
        		"void main (void) \n" +
        		"{\n"+
        		" float distortx = -N.x*0.05;\n"+
        		" float distorty = -N.y*0.05;\n"+
        		
        		" vec2 base     = vec2( gl_FragCoord.x*0.00093750000000585943 + distortx , gl_FragCoord.y*0.0016666 + distorty);\n" +
//        		" vec2 base     = gl_FragCoord.xy;\n" +
        		" vec2 point   = vec2(0.0,0.0);\n"  +
        		" vec2 point1   = vec2(0.003,0.003);\n"  +
        		" vec2 point2   = vec2(-0.003,0.003);\n"  +
        		" vec2 point3   = vec2(0.003,-0.003);\n"  +
           		" vec2 point4   = vec2(-0.003,-0.003);\n"  +       		
           		" vec2 displace  = vec2( cos(gl_TexCoord[0].s*2*3.14159*8)*0.0059, sin(gl_TexCoord[0].t*2*3.14159*8)*0.0059*0); \n"+

  //         		" vec2 displace  = vec2( cos(gl_TexCoord[0].p*0.01)*0.0059, sin(gl_TexCoord[0].q)*0.0059*0); \n"+
 //       		" vec4 light = vec4( displace.x, displace.y )  "
//        		" vec2 displace  = vec2(0.0,0.0); \n"+

           		//        		"  gl_FragColor  = texture2D(myTexture, gl_TexCoord[0] + displace ); \n" +
           	
        		"  vec4 sum  = texture2D(myTexture, base + point +  displace  ); \n" +        
        		"  sum  += texture2D(myTexture, base + point1+  displace  ); \n" +        
        		"  sum  += texture2D(myTexture, base + point2+  displace  ); \n" +        
        		"  sum  += texture2D(myTexture, base + point3+  displace  ); \n" + 
        		"  sum  += texture2D(myTexture, base + point4+  displace  ); \n" +        
//         		
        		
//        		"  sum  += texture2D(overlay, gl_TexCoord[0].st );\n"+
//        		"  gl_FragColor = sum*0.1666666*0.999; \n" +   

        		// compute the surface normal (rotate the normal around the tangent)
        		// do quaternion to rotation matrix conversion
        		"float s=cos(gl_TexCoord[0].s*4*3.14159);\n"+
        		"vec3 q = sin(gl_TexCoord[0].s*4*3.14159)*T;\n"+
        		"mat3 Ro;\n" +
        		"Ro[0][0]=1.0-2.0*(q.y*q.y+q.z*q.z); Ro[0][1]=2.0*q.x*q.y-2.0*s*q.z;      Ro[0][2]=2.0*s*q.y+2.0*q.x*q.z;\n"+ 
        		"Ro[1][0]=2.0*q.x*q.y+2.0*s*q.z;     Ro[1][1]= 1.0-2.0*(q.x*q.x+q.z*q.z); Ro[1][2]=-2.0*s*q.x+2.0*q.y*q.z;\n"+
        		"Ro[2][0]=-2.0*s*q.y+2.0*q.x*q.z;    Ro[2][1]= 2.0*s*q.x+2.0*q.y*q.z;     Ro[2][2]=1.0-2.0*(q.x*q.x+q.y*q.y);\n"+
        		"vec3 Nt = Ro*N;\n" +
        		
        		
        		"vec3 L = normalize(gl_LightSource[0].position.xyz - v);\n" +
        		"vec3 E = normalize(-v);\n"+
        		"vec3 R = normalize(-reflect(L,Nt));\n"+

        		"vec4 Idiff = gl_FrontLightProduct[0].diffuse*0.1 * max(dot(Nt,L), 0.0);\n"+

        		// calculate Specular Term:
//        		"vec4 Ispec = gl_FrontLightProduct[0].specular\n"+
//        		"             * pow(max(dot(R,E),0.0), gl_FrontMaterial.shininess);\n"+
        		"vec4 Ispec = 1.0\n"+
        		"             * pow(max(dot(R,E),0.0), 3.0);\n"+

        		
        		"  if (doreflection) sum=vec4(1.0,1.0,1.0,1.0)*5;\n" +
        		"  gl_FragColor = sum*0.2*0.98 +Idiff*0 +Ispec*0.5; \n" +   
//        		"  gl_FragColor = sum*1.0*0.95 + max(displace.x*displace.x*14000,0.0)*0; \n" +   

        		"Idiff = gl_FrontLightProduct[0].diffuse*0.1 * max(dot(N,L), 0.0);\n"+

        		
//        		" if (gl_TexCoord[0].s < 0.02) \n"+
//        		" gl_FragColor = sum*0.2*0.95+Idiff ; \n"+
//        		" if (gl_TexCoord[0].s > 0.98) \n"+
//        		" gl_FragColor = sum*0.2*0.95+Idiff ; \n"+
//        		" if (gl_TexCoord[0].t < 0.02) \n"+
//        		" gl_FragColor = sum*0.2*0.95+Idiff ; \n"+
//        		" if (gl_TexCoord[0].t > 0.98) \n"+
//        		" gl_FragColor = sum*0.2*0.95+Idiff ; \n"+

        		"} \n";     

        //shader.setAttributePointer("mytangent", 3, false, 0, _box3.getMeshData().getTangentBuffer());

        
        try {
//            logger.info("loading " + currentShaderStr);
            shader.setVertexShader(WaterNode.class.getClassLoader()
                    .getResourceAsStream("jinngine/game/resources/bumbshader.vert"));
            shader.setFragmentShader(WaterNode.class.getClassLoader().getResourceAsStream(
                    "jinngine/game/resources/bumbshader.frag"));
        } catch (final IOException e) {
            //logger.log(Level.WARNING, "Error loading shader", e);
        	e.printStackTrace();
            return;
        }

        
//        shader.setVertexShader(vertex);
//        shader.setFragmentShader(fragment);  
//        shader.setUniform("doreflection", false);
       // shader.setUniform("gl_FrontLightProduct[0].specular", 1.0f);
       // System.out.println("attribs = "+shader.getShaderUniforms().size()) ;
        //shader.setEnabled(true);
//        shader.setUniform("overlay", 1);
        
//        shader.setShaderDataLogic(new GLSLShaderDataLogic() {
//        	@Override
//        	public void applyData(GLSLShaderObjectsState shader, Mesh mesh,
//        			Renderer renderer) {
//                shader.setAttributePointer("mytangent", 3, false, 0, mesh.getMeshData().getTangentBuffer());
//        		
//        	}
//        });

        

      //_box3.setRenderState(shader);
      shader.setUniform("texture0", 0);
      shader.setUniform("normalmap", 1);
      
      colladaNode.setRenderState(shader);
      colladaNode.setRenderState(ts);
     // ((Mesh)colladaNode.getChild(0)).set

      
      
      
      TextureState ts2 = new TextureState();
      ts2.setEnabled(true);
      ts2.setTexture(TextureManager.load("concrete.tga", Texture.MinificationFilter.Trilinear,
              Format.Guess, true),0);

      ts2.setTexture(TextureManager.load("bakedpaltform1normals.tga", Texture.MinificationFilter.BilinearNoMipMaps,
              Format.Guess, true),1);

      ts2.getTexture(0).setWrap(Texture.WrapMode.Repeat);
      ts2.getTexture(0).setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
      
      ts2.getTexture(1).setWrap(Texture.WrapMode.Repeat);
      ts2.getTexture(1).setMagnificationFilter(Texture.MagnificationFilter.Bilinear);

      shader.setUniform("texture0", 0);
      shader.setUniform("normalmap", 1);

      _box2.setRenderState(ts2);
      _box2.setRenderState(shader);

      
      
      
      
      
      
      
        
        
        final DirectionalLight light = new DirectionalLight();
        light.setDiffuse(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
       light.setAmbient(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        light.setSpecular(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light.setAttenuate(false);
        
        //light.setDiffuse(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        //light.setAmbient(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
       // light.setSpecular(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        final PointLight light2 = new PointLight();
        light2.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light2.setAmbient(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        light2.setSpecular(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        light2.setLocation(new Vector3(0, -10, 0));
        light2.setEnabled(true);
        light2.setAttenuate(false);
        

        
        
     //  light.setLocation(new Vector3(-10, -5, 0));
       light.setDirection(new Vector3(0.25,1,0.25));
       light.setEnabled(true);
        
        LightState _lightState = new LightState();
        _lightState.setEnabled(true);
        _lightState.attach(light);
        _lightState.attach(light2);
        _root.setRenderState(_lightState);

        culling = new CullState();
        //culling.setCullFace(Face.Front);
        culling.setCullFace(Face.Back);
        
        _root.setRenderState(culling);
        
        ShadingState shading = new ShadingState();
        shading.setShadingMode(ShadingState.ShadingMode.Smooth);
        _root.setRenderState(shading);

        
        // Create pssm pass
        _pssmPass = new ParallelSplitShadowMapPass(light, 1024, 4);
        _pssmPass.setUseSceneTexturing(true);
        
//         _pssmPass.add(floornode);
//        _pssmPass.add(_box);
//        _pssmPass.add(_box2);
//        _pssmPass.add(floornode);
        _pssmPass.add(_root);
        
//      _pssmPass.addOccluder(colladaNode);
      _pssmPass.addOccluder(_root);

        _pssmPass.setShadowColor(new ColorRGBA(0f,0f,0f,0.25f));
       // colladaNode.
       // _pssmPass.add(_box3);

        BloomRenderPass bloomRenderPass = new BloomRenderPass(_canvas.getCanvasRenderer().getCamera(),1);
        bloomRenderPass.setUseCurrentScene(false);
        //bloomRenderPass.setExposurePow(1000.0f);
      //  bloomRenderPass.setBlurIntensityMultiplier(10.0f);
//       bloomRenderPass.setExposureCutoff(8.0f);
          bloomRenderPass.add(colladaNode);

        // Populate passmanager with passes.
        final RenderPass rootPass = new RenderPass();       
        rootPass.add(_root);

        final RenderPass nonocclusionpass = new RenderPass();       
        nonocclusionpass.add(colladaNode);


        //DisplayListDelegate delegate = new DisplayListDelegate(1,);
        
        _passManager.add(rootPass);
        
        _passManager.add(_pssmPass);          

//        BlendState blend = new BlendState();
//        blend.setBlendEnabled(true);
//        blend.setEnabled(true);

        
        outline = new OutlinePass(true);
        outline.add(_root);
        //outline.setBlendState(blend);
        //outline.setOutlineColor(new ColorRGBA(1,1,0,0.0f));
        outline.setOutlineWidth(1.5f);
        
        //_passManager.add(outline);
        
        
        

//        _box.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
              // _passManager.add(bloomRenderPass);

        //_passManager.add(renderPass);

        //_pssmPass.setUseSceneTexturing(false);


        // set it to rotate:
        _box.addController(new SpatialController<Box>() {
            private static final long serialVersionUID = 1L;
            private final Vector3 _axis = new Vector3(1, 1, 0.5f).normalizeLocal();
            private final Matrix3 _rotate = new Matrix3();
            private double _angle = 0;

            public void update(final double time, final Box caller) {
                // update our rotation
  
            	
            	_box.setTranslation(box.state.position.x, box.state.position.y, box.state.position.z);
            	ReadOnlyMatrix3 mat = new Matrix3(box.state.rotation.a11, box.state.rotation.a12, box.state.rotation.a13,
            			box.state.rotation.a21, box.state.rotation.a22, box.state.rotation.a23, 
            			box.state.rotation.a31, box.state.rotation.a32, box.state.rotation.a33);
            	
                _box.setRotation(mat);
            }
        });
        
        
        _box3.addController(new SpatialController<Spatial>() {
            private static final long serialVersionUID = 1L;
            private final Vector3 _axis = new Vector3(1, 1, 0.5f).normalizeLocal();
            private final Matrix3 _rotate = new Matrix3();
            private double _angle = 0;

            public void update(final double time, final Spatial caller) {
                // update our rotation
  
            	
            	_box2.setTranslation(box2.state.position.x, box2.state.position.y, box2.state.position.z);
            	ReadOnlyMatrix3 mat = new Matrix3(box2.state.rotation.a11, box2.state.rotation.a12, box2.state.rotation.a13,
            			box2.state.rotation.a21, box2.state.rotation.a22, box2.state.rotation.a23, 
            			box2.state.rotation.a31, box2.state.rotation.a32, box2.state.rotation.a33);
            	
                _box2.setRotation(mat);
            }
        });

        // Add our awt based image loader.
        AWTImageLoader.registerLoader();


        // Create a ZBuffer to display pixels closest to the camera above farther ones.
        final ZBufferState buf = new ZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        _root.setRenderState(buf);

        
        
    }

    /**
     * Update our scene... Check if the window is closing. Then update our timer and finally update the geometric state
     * of the root and its children.
     */
    private void updateExample() {
        if (_canvas.isClosing()) {
            _exit = true;
            return;
        }
        
        engine.tick();

        _timer.update();

        _passManager.updatePasses(_timer.getTimePerFrame());

        
        // Update controllers/render states/transforms/bounds for rootNode.
        _root.updateGeometricState(_timer.getTimePerFrame(), true);
        floornode.updateGeometricState(_timer.getTimePerFrame(), true);


        
    }

    // ------ Scene methods ------

    public boolean renderUnto(final Renderer renderer) {
        if (!_pssmPass.isInitialised()) {
            _pssmPass.init(renderer);
            
            

           tRenderer.setupTexture(mytexture);
           tRenderer.setupTexture(mytexture2);
           
           

           //tRenderer.getCamera().lookAt(colladaNode.getTranslation(), Vector3.UNIT_Y);
           shader.setAttributePointer("Mytangent", 3, false, 0, _box3.getMeshData().getTangentBuffer() );

            
        }
        if (!_canvas.isClosing()) {

        	
           //tRenderer.getCamera().lookAt(colladaNode.getTranslation(), Vector3.UNIT_Y);

        	culling.setCullFace(Face.Back);
//        	shader.setUniform("doreflection", true);

        //	tRenderer.render(colladaNode, mytexture2, Renderer.BUFFER_COLOR_AND_DEPTH);
        //	tRenderer.render(_box2, mytexture2, Renderer.BUFFER_NONE);

        //	tRenderer.render(_box2, mytexture, Renderer.BUFFER_COLOR_AND_DEPTH);

        	
        	
        	
        	
//        	culling.setCullFace(Face.Front);
//
//        	ts.setTexture(mytexture2,0);
//        	tRenderer.render(_box3, mytexture, Renderer.BUFFER_COLOR_AND_DEPTH);
//        	shader.setEnabled(false);
//            culling.setCullFace(Face.Back);
//        	shader.setEnabled(true);
//
//        	ts.setTexture(mytexture,0);
//        	
        	
            _passManager.renderPasses(renderer);
            //_passManager.renderPasses(renderer);
            //outline.renderPass(renderer);


            
            return true;
        }
        return false;
    }

    public PickResults doPick(final Ray3 pickRay) {
        // Ignore
        return null;
    }


}
