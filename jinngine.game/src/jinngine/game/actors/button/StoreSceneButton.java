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

import jinngine.game.Game;
import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.actors.interaction.BodyPlacement;
import jinngine.game.actors.player.Player;

import jinngine.physics.Body;


public class StoreSceneButton extends Button {
	
	private Texture selectedtexture;
	private Texture deselectedtexture;	
	private TextureState texturestate;
	private Audio click;
	
	public StoreSceneButton() {
		
		try {
//			SoundStore.get().get
			click = SoundStore.get().getWAV("audiodump.wav");
			click.playAsSoundEffect(1, 0, false);
//			System.out.println(click.getPosition());
//			audiobufferid = click.getBufferID();

			System.out.println(click);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void act(Game game) {
		super.act(game);
		
		// do something if player is within range
		
	}
	
	@Override
	public void start(Game game) {
		super.start(game);
		
		// load textures
		selectedtexture = TextureManager.load("selectedstorescene.tga",
				Texture.MinificationFilter.Trilinear,
				TextureStoreFormat.GuessNoCompressedFormat, true);

		deselectedtexture = TextureManager.load("deselectedstorescene.tga",
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
				
		return null;
	}
	
	@Override
	public void setSelected(Game game, boolean selected) {
		super.setSelected(game, selected);
		
		if (selected) {
			// play click sound
			click.playAsSoundEffect(1, 100, false);
			
			game.storeCurrentLevel("storedlevel.xml");

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
