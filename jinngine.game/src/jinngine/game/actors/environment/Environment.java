package jinngine.game.actors.environment;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Plane;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.scenegraph.hint.TextureCombineMode;
import com.ardor3d.scenegraph.hint.TransparencyType;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.TextureManager;

import jinngine.game.actors.Actor;
import jinngine.game.*;
import jinngine.physics.Body;
import jinngine.physics.Scene;

/**
 * Floors, background etc
 */
public class Environment extends Node implements Actor {	

	private Box floorbox;
	private Quad sky;
	private Quad hills;
	private Body floor;

	@Override
	public void create(Game game) {
		Node rootnode = game.getRendering().getScene();

		// make the floor box 
		
		floorbox = new Box("Box", new Vector3(0,-25-5,0), 30, 5, 7);
		floorbox.setSolidColor(new ColorRGBA(0.86f,0.86f,0.86f,1.0f));
		floorbox.setModelBound(new BoundingBox());
		floorbox.setName("myfloorboxnode");
		this.attachChild(floorbox); //dont draw floor
//		rootnode.attachChild(this);		
		this.setName("Actor:environment");
		
        // default z-buffering
//        final ZBufferState buf = new ZBufferState();
//        buf.setEnabled(true);
//        buf.setFunction(ZBufferState.TestFunction.Always);
//        buf.setFunction(ZBufferState.TestFunction.Always);
		
        Camera cam = game.getRendering().getCamera();

//        hills = new Quad("HillsBackdrop", cam.getWidth() , cam.getHeight() );
//        hills.setTranslation(cam.getWidth() / 2, cam.getHeight() / 2, -0.1);
//        hills.setSolidColor(new ColorRGBA(0.0f,0.0f,0.0f,0.0f));
//        hills.getSceneHints().setCullHint(CullHint.Never);
////        hills.getSceneHints().setTextureCombineMode(TextureCombineMode.Replace);
//        hills.getSceneHints().setLightCombineMode(LightCombineMode.Off);
//        hills.getSceneHints().setRenderBucketType(RenderBucketType.Ortho);
//        hills.getSceneHints().setOrthoOrder(0);
//        hills.setModelBound(new BoundingBox());
////        final TextureState ts3 = new TextureState();
////        ts3.setEnabled(true);
////        ts3.setTexture(TextureManager.load("backdrophills.png", 
////        		MinificationFilter.BilinearNearestMipMap,
////        		TextureStoreFormat.GuessCompressedFormat, true));
////        hills.setRenderState(ts3);
//        hills.getSceneHints().setPickingHint(PickingHint.Pickable, false);
//        hills.getSceneHints().setPickingHint(PickingHint.Collidable, false);
//        this.attachChild(hills);

		
        sky = new Quad("SkyBackdrop", cam.getWidth() , cam.getHeight() );
        sky.setTranslation(cam.getWidth() / 2, cam.getHeight() / 2, -0.999);
        sky.getSceneHints().setCullHint(CullHint.Never);
        sky.getSceneHints().setTextureCombineMode(TextureCombineMode.Replace);
        sky.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        sky.getSceneHints().setRenderBucketType(RenderBucketType.Ortho);
        sky.getSceneHints().setOrthoOrder(1);

        sky.setModelBound(new BoundingBox());
        final TextureState ts2 = new TextureState();
        ts2.setEnabled(true);
        ts2.setTexture(TextureManager.load("backdrop.tga", 
        		MinificationFilter.BilinearNearestMipMap,
        		TextureStoreFormat.GuessCompressedFormat, true));
        sky.setRenderState(ts2);
//        sky.getSceneHints().setPickingHint(PickingHint.Pickable, false);
//        sky.getSceneHints().setPickingHint(PickingHint.Collidable, false);
       this.attachChild(sky);
       
//       this.setUserData(this);

      
       rootnode.attachChild(this);
	}
	
	@Override
	public void act( Game game ) {
//        Camera cam = game.getRendering().getCamera();
//		System.out.println("Environment runnign " + cam.getLocation());
//        hills.setTranslation(cam.getWidth() / 2 + cam.getLocation().getX()*25,  cam.getHeight() / 2, -0.99);
	}

	@Override
	public void start( Game game) {
		Scene physics = game.getPhysics();
		floorbox = (Box)getChild("myfloorboxnode");
		
		//shadowing
		game.getRendering().getPssmPass().add(floorbox);

		// make physics box
		floor = new Body("default", new jinngine.geometry.Box(120,10,120));
		floor.setPosition(new jinngine.math.Vector3(0,-25 -5,0));
		floor.setFixed(true);
		physics.addBody(floor);	

		// set scene hints again, as they seem to get lost over scene graph export/import
		Spatial sky = getChild("SkyBackdrop");
        sky.getSceneHints().setCullHint(CullHint.Never);
        sky.getSceneHints().setTextureCombineMode(TextureCombineMode.Replace);
        sky.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        sky.getSceneHints().setRenderBucketType(RenderBucketType.Ortho);	
        sky.getSceneHints().setOrthoOrder(1);
	}

	@Override
	public void stop( Game game) {
		// remove floor box
		game.getPhysics().removeBody(floor);
		
		// remove shadows
		game.getRendering().getPssmPass().remove(floorbox);
	}

}
