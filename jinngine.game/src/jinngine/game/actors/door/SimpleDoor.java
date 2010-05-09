package jinngine.game.actors.door;

import java.io.IOException;

import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.SoundStore;

import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UICheckBox;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.layout.BorderLayout;
import com.ardor3d.extension.ui.layout.BorderLayoutData;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector2;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.export.InputCapsule;

import jinngine.game.Game;
import jinngine.game.PostponedAction;
import jinngine.game.Toolbox;
import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.actors.ConfigurableActor;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.actors.SelectableActor;
import jinngine.game.actors.logic.FadeOutAndIn;

import jinngine.math.Matrix3;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Scene;
import jinngine.physics.constraint.joint.HingeJoint;
import jinngine.physics.force.GravityForce;


public class SimpleDoor extends Node implements PhysicalActor, SelectableActor {

	private Body doorbody;
	private Node doornode;
	private Audio click;
	private boolean fixedframe = false;
	
	public SimpleDoor() {
		
		try {
//			SoundStore.get().get
			click = SoundStore.get().getWAV("door_creak_closing.wav");
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
	public void write(final OutputCapsule oc) throws IOException {
		super.write(oc);
		oc.write( fixedframe,"fixedframe", false);
	}
	
	@Override
	public void read(final InputCapsule ic) throws IOException {
		super.read(ic);
		fixedframe = ic.readBoolean( "fixedframe", false);
	}
	
	@Override
	public void create(Game game) {
		this.setName("Actor:SimpleDoor");
		
		// make the door
		doornode = new Node();
		doornode.setName("mydoornode");
		this.attachChild(doornode);
		
		// load door asset
        final ColladaImporter colladaImporter = new ColladaImporter();
        final ColladaStorage storage = colladaImporter.load("simpledoor.dae");
        Node doorscene = storage.getScene();

        Toolbox.writeOutScene(doorscene);
        
        // modify line so they are what we like
        Line dooroutline = (Line)doorscene.getChild("DoorOutline_001_lines");
		dooroutline.setLineWidth(3);
		dooroutline.setDefaultColor(new ColorRGBA(0,0,0,1));   
        dooroutline = (Line)doorscene.getChild("DoorframeOutline_001_lines");
		dooroutline.setLineWidth(3);
		dooroutline.setDefaultColor(new ColorRGBA(0,0,0,1));

        Spatial faces = doorscene.getChild("DoorOutline");
        doornode.attachChild(faces);
        faces = doorscene.getChild("DoorframeOutline");
        doornode.attachChild(faces);
        
        faces = doorscene.getChild("DoorKnop");
        doornode.attachChild(faces);
        faces = doorscene.getChild("Door");
        doornode.attachChild(faces);
        faces = doorscene.getChild("DoorframeFaces");
        doornode.attachChild(faces);
        
        doornode.setTranslation(0,-20,0);
        
//        doornode.attachChild(doorscene);
        
//      Line dooroutline = (Line)doorscene.getChild("DoorOutline-Geometry_lines");
//		dooroutline.setLineWidth(4);
//		dooroutline.setDefaultColor(new ColorRGBA(0,0,0,1));
//        doornode.attachChild(dooroutline);
//        Spatial doorfaces = doorscene.getChild("Door-Geometry_triangles");
//        doornode.attachChild(doorfaces);

 		
		// get the outline and do stuff
//		Line l = (Line)doorscene.getChild("Outline_lines");		
//		l.setLineWidth(4);
//		l.setDefaultColor(new ColorRGBA(0,0,0,1));
//		doorframenode.attachChild(l);
		

		// translate initially
//		doorframenode.setTranslation(doorframenode.getTranslation().add(0, -10, 0, null));
//		doornode.setTranslation(doornode.getTranslation().add(0, -10, 0, null));
		
		// attach to root
		game.getRendering().getScene().attachChild(this);
	}
	
	@Override
	public void act( Game game ) {
		// TODO Auto-generated method stub

	} 

	@Override
	public void start( Game game ) {
		Scene physics = game.getPhysics();
		Node rootnode = game.getRendering().getScene();

		// obtain the transform
		Vector3 translation = new Vector3();
		Quaternion orientation = new Quaternion();
		Toolbox.getNodeTransform(this, translation, orientation);
		
		

		doornode = (Node)getChild("mydoornode");

//        doornode.getChild("DoorframeFaces-Geometry").setUserData(this);
//        doornode.getChild("DoorframeFaces-Geometry").setUserData(this);

        
        //setup shadowing
//        game.getRendering().getPssmPass().add(doornode);
        //game.getRendering().getPssmPass().addOccluder(doorfaces);
       
        // door
        jinngine.geometry.Box doorbox = new jinngine.geometry.Box(2,2,1);
        doorbox.setFrictionCoefficient(0.5);
        doorbody = new Body("SimpleDoorBox", doorbox);
        physics.addBody(doorbody);
        physics.addForce(new GravityForce(doorbody));
        //doorbody.setPosition(doorbody.getPosition().add(new jinngine.math.Vector3(0,-0.125,0)));
        doorbody.setAngularVelocity(new jinngine.math.Vector3(0,0,0));                      
        //set position
        Toolbox.setTransformFromNode(doornode, doorbody);
        // attach body to node
        doornode.addController( Toolbox.createSpatialControllerForBody(doorbody));
//		// add jinngine debug geometry        
//        Toolbox.addJinngineDebugMesh("jinnginedebugmesh1", doornode, doorbody);

        		
        //setup shadowing
//        game.getRendering().getPssmPass().add(doornode);
        game.getRendering().getPssmPass().addOccluder(doornode);
	}
	
	@Override
	public void stop( Game game ) {
		// clean up physics
		game.getPhysics().removeBody(doorbody);
		
		// clean up shadows
		game.getRendering().getPssmPass().removeOccluder(doornode);

	}

	@Override
	public Body getBodyFromNode(Node node) {
		return doorbody;
//		if (node == doornode)
//			return doorbody;
//		else
//			return null;
	}

	@Override
	public boolean canBeSelected() {
		return true;
	}

	@Override
	public ActionActor provideActionActor(ActorOwner owner, Actor target,
			Node picknode, Vector3 pickpoint, Vector2 screenpos) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSelected(final Game game, boolean selected) {
		if (selected) {
			// play click sound
			click.playAsSoundEffect(1, 100, false);

//			texturestate.setTexture(selectedtexture);
			
			// create action that loads a new scene
			PostponedAction event = new PostponedAction() {
				@Override
				public void perform() {
					game.unloadCurrentLevel();
					game.loadLevel("storedlevel.xml");
				}
			};
			
			// wrap the load scene action in fade-out and in
			Actor a = new FadeOutAndIn(event);
			a.create(game);
			game.addActor(a);
			
		} else {
			click.playAsSoundEffect(1, 100, false);

			// play click sound
//			texturestate.setTexture(deselectedtexture);	
		}

	}
	
	
	
}
