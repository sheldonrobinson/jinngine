package jinngine.game.actors.button;


import java.io.IOException;
import org.newdawn.slick.openal.*;

import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Vector2;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.CopyLogic;
import com.ardor3d.util.geom.SceneCopier;
import com.ardor3d.util.geom.SharedCopyLogic;
import com.ardor3d.util.resource.ResourceLocatorTool;

import jinngine.game.Game;
import jinngine.game.PostponedAction;
import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.actors.ClonableActor;
import jinngine.game.actors.ScalableActor;
import jinngine.game.actors.interaction.DeleteActor;
import jinngine.game.actors.interaction.ScaleActor;


public class CopyActorButton extends Button {
	
	private Texture selectedtexture;
	private Texture deselectedtexture;	
	private TextureState texturestate;
	private Audio click;
	
	public CopyActorButton() {
		try {
			click = SoundStore.get().getWAV(
					ResourceLocatorTool.locateResource(
							ResourceLocatorTool.TYPE_AUDIO, "audiodump.wav").openStream());

			System.out.println(click);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void act(Game game) {
		super.act(game);
	}
	
	@Override
	public void start(Game game) {
		super.start(game);
		
		// load textures
		selectedtexture = TextureManager.load("selectedcopyactor.tga",
				Texture.MinificationFilter.Trilinear,
				TextureStoreFormat.GuessNoCompressedFormat, true);

		deselectedtexture = TextureManager.load("deselectedcopyactor.tga",
				Texture.MinificationFilter.Trilinear,
				TextureStoreFormat.GuessNoCompressedFormat, true);
		
		// get the texture state on the button box
		texturestate = (TextureState)buttonnode.getChild("mybuttonbox").getLocalRenderState(StateType.Texture);

		// set the deselected texture by default
		texturestate.setTexture(deselectedtexture);	
	}
	
	@Override
	public ActionActor provideActionActor(final ActorOwner owner, final Actor target, final Node picknode,
			jinngine.math.Vector3 pickpoint, Vector2 screenpos ) {
		
		if (target instanceof ClonableActor ) {
		// copy actor actor
			return new ActionActor() {
				@Override
				public void act(Game game) {}

				public void mousePressed(Game game) { }

				@Override
				public void mouseReleased(Game game) { }

				@Override
				public void create(Game game) {
				}

				@Override
				public void start(final Game game) {
					System.out.println("Creating a copy");

					game.addPostponedAction(new PostponedAction() {
						public void perform() {
							game.addActor( ((ClonableActor)target).getCopy(game));
						}
					});
					
					owner.finished(game, this);					
				}

				@Override
				public void stop(Game game) { }
			};
		} else {
			return null;
		}
	}
	
	@Override
	public void setSelected(Game game, boolean selected) {
		super.setSelected(game, selected);
		
		if (selected) {
			// play click sound
			click.playAsSoundEffect(1, 100, false);

			texturestate.setTexture(selectedtexture);
						
			
		} else {
			click.playAsSoundEffect(1, 100, false);

			// play click sound
			texturestate.setTexture(deselectedtexture);	
		}
	}
	
	@Override
	public boolean canBeSelected() {
		return true;
	}
	
}
