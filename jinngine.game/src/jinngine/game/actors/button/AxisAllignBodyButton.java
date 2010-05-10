package jinngine.game.actors.button;


import java.io.IOException;
import org.newdawn.slick.openal.*;

import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Vector2;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.resource.ResourceLocatorTool;

import jinngine.game.Game;
import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.actors.interaction.AxisAllignBody;
import jinngine.game.actors.interaction.BodyPlacement;
import jinngine.game.actors.interaction.FixUnfixBody;
import jinngine.game.actors.player.Player;

import jinngine.physics.Body;


public class AxisAllignBodyButton extends Button {
	
	private Texture selectedtexture;
	private Texture deselectedtexture;	
	private TextureState texturestate;
	private final double distancelimit = 2.5;
	private Audio click;
	private int audiobufferid = 0;
	
	public AxisAllignBodyButton() {
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
		selectedtexture = TextureManager.load("selectedaxisallignbody.tga",
				Texture.MinificationFilter.Trilinear,
				TextureStoreFormat.GuessNoCompressedFormat, true);

		deselectedtexture = TextureManager.load("deselectedaxisallignbody.tga",
				Texture.MinificationFilter.Trilinear,
				TextureStoreFormat.GuessNoCompressedFormat, true);
		
		// get the texture state on the button box
		texturestate = (TextureState)buttonnode.getChild("mybuttonbox").getLocalRenderState(StateType.Texture);

		// set the deselected texture by default
		texturestate.setTexture(deselectedtexture);	
	}
	
	@Override
	public ActionActor provideActionActor(ActorOwner owner, Actor target, Node picknode,
			jinngine.math.Vector3 pickpoint, Vector2 screenpos ) {

		System.out.println(""+this);
		
		// spawn a AxisAllignBody actor if possible
		if (target instanceof PhysicalActor) {
			PhysicalActor physactor = (PhysicalActor)target;
			Body body = physactor.getBodyFromNode(picknode);
		
			if (body != null)
				return new AxisAllignBody(owner, body,pickpoint,screenpos);
			
			else
				System.out.println("No body recieved from actor");
		}
				
		return null;
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
