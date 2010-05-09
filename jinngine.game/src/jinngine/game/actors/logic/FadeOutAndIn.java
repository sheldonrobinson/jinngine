package jinngine.game.actors.logic;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.scenegraph.shape.Quad;

import jinngine.game.Game;
import jinngine.game.PostponedAction;
import jinngine.game.actors.Actor;

public class FadeOutAndIn implements Actor {

	private PostponedAction action;
	private Quad fadeoutscreen;
	private double fade = 0.0;
	private boolean fadingout = true;
	private double dt = 0.05;
	
	public FadeOutAndIn( PostponedAction action) {
		System.out.println("FadeOutAndIn");
		this.action = action;
		
	}
	
	@Override
	public void act(Game game) {	
		if (fadingout) {
			if (fade < 1.0) {
				fade = fade + dt;
				fadeoutscreen.setSolidColor(new ColorRGBA(0.0f,0.0f,0.0f,(float)fade));
			} else {
				// if done, send the perform action and swith to fade-in state
				game.addPostponedAction(action);
				fadingout = false;
			}
		} else {
			if (fade > 0.0) {
				fade = fade - dt;
				fadeoutscreen.setSolidColor(new ColorRGBA(0.0f,0.0f,0.0f,(float)fade));
			} else {
				// if done, kill this actor
				game.removeActor(this);
			}
		}
	}

	@Override
	public void create(Game game) {
        Camera cam = game.getRendering().getCamera();
        fadeoutscreen = new Quad("FadeOutScreen", cam.getWidth() , cam.getHeight() );
        fadeoutscreen.setTranslation(cam.getWidth() / 2, cam.getHeight() / 2, -0.1);
        fadeoutscreen.setSolidColor(new ColorRGBA(0.0f,0.0f,0.0f,0.0f));
        fadeoutscreen.getSceneHints().setCullHint(CullHint.Never);
        fadeoutscreen.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        fadeoutscreen.getSceneHints().setRenderBucketType(RenderBucketType.Ortho);
        fadeoutscreen.getSceneHints().setOrthoOrder(0);
        fadeoutscreen.setModelBound(new BoundingBox());
        fadeoutscreen.getSceneHints().setPickingHint(PickingHint.Pickable, false);
        fadeoutscreen.getSceneHints().setPickingHint(PickingHint.Collidable, false);
        
        game.getRendering().getRootNode().attachChild(fadeoutscreen);		
	}

	@Override
	public void start(Game game) {
		System.out.println("FadeOutAndIn.start");
	}

	@Override
	public void stop(Game game) {
		// clean out the fullscreen quad
		 game.getRendering().getRootNode().detachChild(fadeoutscreen);		
	}

}
