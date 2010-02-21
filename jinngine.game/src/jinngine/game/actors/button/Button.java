package jinngine.game.actors.button;


import java.io.IOException;

import com.ardor3d.extension.ui.UICheckBox;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.UIRadioButton;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.layout.BorderLayout;
import com.ardor3d.extension.ui.layout.BorderLayoutData;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector2;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;

import jinngine.game.Game;
import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.actors.ConfigurableActor;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.actors.SelectableActor;
import jinngine.game.actors.interaction.ConfigureActor;
import jinngine.game.actors.platform1.Platform1;


public class Button extends Platform1 implements SelectableActor, PhysicalActor {

//	private enum Type {
//		Configure,
//		Placement
//	}
	
	private boolean pressed = false;
//	private Type buttontype = Type.Placement;
	
//	@Override
//	public void write(Ardor3DExporter e) throws IOException {
//		super.write(e);
//		e.getCapsule(this).write( buttontype, "buttontype", Type.Configure);
//	}
//
//	@Override
//	public void read(Ardor3DImporter e) throws IOException {
//		super.read(e);
//		buttontype = e.getCapsule(this).readEnum("buttontype", Type.class,  Type.Configure );
//	}

	
//	@Override
//	public void act(Game game) {
//		super.act(game);
//	}
//
//	@Override
//	public void create(Game game) {
//		super.create(game);
//	}
//
//	@Override
//	public void start(Game game) {
//		super.start(game);
//	}
//
//	@Override
//	public void stop(Game game) {
//		super.stop(game);
//	}

	@Override
	public ActionActor provideActionActor(ActorOwner owner, Actor target, Node picknode,
			jinngine.math.Vector3 pickpoint, Vector2 screenpos ) {
		
		System.out.println("Button: got an actor "+ target);
		
		// spawn a BodyPlacement actor if possible
		if (target instanceof ConfigurableActor) {
			ConfigurableActor confactor = (ConfigurableActor)target;

			return new ConfigureActor(owner, confactor);
		}
		
		// not possible
		return null;
	}

	@Override
	public void setSelected(Game game, boolean selected) {
		pressed = selected;
	}
	

}
