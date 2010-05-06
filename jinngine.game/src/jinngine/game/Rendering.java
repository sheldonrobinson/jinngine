package jinngine.game;

import java.net.URI;
import java.net.URISyntaxException;

import org.lwjgl.LWJGLException;



import com.ardor3d.extension.shadow.map.ParallelSplitShadowMapPass;
import com.ardor3d.extension.ui.UIComponent;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.framework.lwjgl.LwjglAwtCanvas;
import com.ardor3d.framework.lwjgl.LwjglCanvasRenderer;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
import com.ardor3d.input.logical.DummyControllerWrapper;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.lwjgl.LwjglTextureRendererProvider;
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.pass.RenderPass;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.renderer.state.CullState.Face;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.Timer;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;

/**
 * Ardor3d rengering with jogl
 */
public final class Rendering implements com.ardor3d.framework.Scene {

    // ardor3d members
    private LwjglAwtCanvas canvas;
//	private LwjglAwtCanvas canvas;
    private final Timer timer = new Timer();
    private Node root = new Node();
    private final BasicPassManager passes = new BasicPassManager();
    private boolean exit = false;
    private final LogicalLayer logicallayer = new LogicalLayer(); 
    private final ParallelSplitShadowMapPass pssm;
    private final PhysicalLayer physicallayer; 
    private final UIHud hud;
    private final Camera camera;
    
	public Rendering() {
        final DisplaySettings settings = new DisplaySettings((int)(600*(16.0/9.0)), 600, 16, 0, 0, 8, 0, 0, false, false);
//        final JoglCanvasRenderer canvasRenderer = new JoglCanvasRenderer(this);
//        TextureRendererFactory.INSTANCE.setProvider(new JoglTextureRendererProvider());
//        canvas =  new JoglCanvas(canvasRenderer, settings);

        final LwjglCanvasRenderer canvasRenderer = new LwjglCanvasRenderer(this);
        TextureRendererFactory.INSTANCE.setProvider(new LwjglTextureRendererProvider());
		canvas = null;

        try {
			canvas =  new LwjglAwtCanvas(settings, canvasRenderer);
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

        
        canvas.init();        
        //canvas.setTitle("Machinery");
        this.camera = canvas.getCanvasRenderer().getCamera();
        canvas.getCanvasRenderer().getCamera().setLocation(0, -25+7, -20);
        canvas.getCanvasRenderer().getCamera().setFrustumPerspective(25, 16.0/9.0, 1, 1500);
        canvas.getCanvasRenderer().getCamera().lookAt(0, -25, 0, Vector3.UNIT_Y);
        canvas.getCanvasRenderer().getRenderer().setBackgroundColor(new ColorRGBA(1,1,1,1));

        // do some ardor3d stuff to make the user interface stuff work
//        physicallayer = new PhysicalLayer(new AwtKeyboardWrapper(canvas), 
//    			 new AwtMouseWrapper(canvas, new AwtMouseManager(canvas)),
//                 new AwtFocusWrapper(canvas));
        
        
     
        AwtKeyboardWrapper keywrap = new AwtKeyboardWrapper(canvas);
//        KeyboardWrapper keywrap = new 
        
        
        physicallayer = new PhysicalLayer( keywrap,
        		new AwtMouseWrapper(canvas, new AwtMouseManager(canvas)), 
        		DummyControllerWrapper.INSTANCE, 
        		new AwtFocusWrapper(canvas));

       logicallayer.registerInput(canvas, physicallayer);
       
        
        // setup resource locator
        try {
            final SimpleResourceLocator srl = new SimpleResourceLocator(new URI("file:///home/mo/workspace/jinngine.game/"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, srl);
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, srl);
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_SHADER, srl);
        } catch (final URISyntaxException ex) {
            ex.printStackTrace(); 
        }
        
        AWTImageLoader.registerLoader();
//        
//        try {
//			root = (Node)XMLImporter.getInstance().load( new File("testgraph.xml"));
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
        
        
        // default back face culling
        CullState culling = new CullState();
        culling.setCullFace(Face.Back);
        culling.setEnabled(true);
        root.setRenderState(culling);
        
        // default z-buffering
        final ZBufferState buf = new ZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        root.setRenderState(buf);
        
        //blending
        BlendState bs = new BlendState();
        bs.setBlendEnabled(true);
        bs.setSourceFunction(SourceFunction.SourceAlpha);
        bs.setDestinationFunctionAlpha(DestinationFunction.OneMinusSourceAlpha);
        root.setRenderState(bs);
        
        // define some light
        final DirectionalLight light = new DirectionalLight();
        light.setDirection(-0.1, -0.1, 0.1);
        light.setDiffuse(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        light.setAmbient(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        light.setSpecular(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light.setAttenuate(true);
        
        // setup passes
        final RenderPass defaultpass = new RenderPass();       
        defaultpass.add(root);        
        passes.add(defaultpass);
        
        // shadow pass
        this.pssm = new ParallelSplitShadowMapPass(light, 1024, 4);
        //pssm.add(root);
        //pssm.addOccluder(root);
        this.pssm.setShadowColor(new ColorRGBA(0f,0f,0f,0.125f));
        passes.add(this.pssm);   
        
        // outline pass
//        final OutlinePass outline = new OutlinePass(true);
//        outline.add(root);
//        outline.setEnabled(true);
//        passes.add(outline);
        
        // setup for ui components
        UIComponent.setUseTransparency(true);
        hud = new UIHud();
        //hud.setupInput(canvas, physicallayer, logicallayer); 
	}
	
	/**
	 * Get the camera of the canvas renderer
	 */
	public final Camera getCamera() {
		return canvas.getCanvasRenderer().getCamera();
	}
	
	public final ParallelSplitShadowMapPass getPssmPass() {
		return pssm;
	}

	/**
	 * Draw a frame
	 */
	public final void draw() {
        // Update controllers/render states/transforms/bounds for rootNode.
        timer.update();
        
		//hud.getLogicalLayer().checkTriggers(timer.getTimePerFrame());
		//hud.updateGeometricState(timer.getTimePerFrame());
		root.updateGeometricState(timer.getTimePerFrame(), true);
		
		logicallayer.checkTriggers(timer.getTimePerFrame());

//		if ( timer.getTimeInSeconds()  )
//			System.out.println(""+timer.getFrameRate());
		
		canvas.draw(null);
	}
	
	/**
	 * Get the root node of the scene
	 */
	public final Node getRootNode() {
		return root;
	}
	
	public final LogicalLayer getLogicalLayer() {
		return logicallayer;
	}
	
	public final PhysicalLayer getPhysicalLayer() {
		return physicallayer;
	}
	
	public final Canvas getCanvas() {
		return canvas;
	}

	public final UIHud getHud() {
		return hud;
	}
	
	@Override
	public PickResults doPick(Ray3 pickRay) {
        final PrimitivePickResults pickResults = new PrimitivePickResults();
        pickResults.setCheckDistance(true);
        PickingUtil.findPick(root, pickRay, pickResults);
        //processPicks(pickResults);
        return pickResults;
	}

	@Override
	public boolean renderUnto(Renderer renderer) {
		passes.renderPasses(renderer);
		renderer.draw(hud);
		return true;
	}
	

	
}
