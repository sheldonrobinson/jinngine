package jinngine.game.actors.button;

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
import jinngine.game.actors.ConfigurableActor;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.actors.SelectableActor;
import jinngine.game.actors.interaction.BodyPlacement;
import jinngine.game.actors.interaction.ConfigureActor;
import jinngine.game.actors.platform1.BoxPlatform;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class PlacementButton extends Button {
	
	private Texture selectedtexture;
	private Texture deselectedtexture;	
	private TextureState texturestate;
	
	@Override
	public void start(Game game) {
		// TODO Auto-generated method stub
		super.start(game);
		
		// load textures
		selectedtexture = TextureManager.load("selectedhand.tga",
				Texture.MinificationFilter.Trilinear,
				TextureStoreFormat.GuessNoCompressedFormat, true);

		deselectedtexture = TextureManager.load("deselectedhand.tga",
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
				
		System.out.println("PlacementButton: got an actor "+ target);
		
		// spawn a BodyPlacement actor if possible
		if (target instanceof PhysicalActor) {
			PhysicalActor physactor = (PhysicalActor)target;
			Body body = physactor.getBodyFromNode(picknode);
		
			if (body != null)
				return new BodyPlacement(owner, body,pickpoint,screenpos);
		}
				
		return null;
	}
	
	@Override
	public void setSelected(Game game, boolean selected) {
		super.setSelected(game, selected);
		
		if (selected) {
			texturestate.setTexture(selectedtexture);
		} else {
			texturestate.setTexture(deselectedtexture);	
		}
	}
	
}
