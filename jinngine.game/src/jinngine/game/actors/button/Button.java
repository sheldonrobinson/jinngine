package jinngine.game.actors.button;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.image.Texture;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;

import jinngine.game.Game;
import jinngine.game.Toolbox;
import jinngine.game.actors.ActionActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ActorOwner;
import jinngine.game.actors.ConfigurableActor;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.actors.ScalableActor;
import jinngine.game.actors.SelectableActor;
import jinngine.game.actors.interaction.ConfigureActor;
import jinngine.geometry.Geometry;
import jinngine.physics.Body;
import jinngine.physics.Scene;
import jinngine.physics.force.GravityForce;


public class Button extends Node implements SelectableActor, PhysicalActor, ScalableActor {

	protected Body buttonbody;
	protected Node buttonnode;
	protected jinngine.geometry.Box buttongeometry;
	protected TextureState buttontexture;
	protected Vector3 initialposition;
	private boolean pressed = false;
		
	@Override
	public void create(Game game) {
		Node rootnode = game.getRendering().getScene();
		this.setName("Actor:Button");

        // create the outline for this button box
		double scale = 0.5;

//		final ColladaImporter colladaImporter = new ColladaImporter();
//      final ColladaStorage storage = colladaImporter.readColladaFile("handbox.dae");
//      final Node body = storage.getScene();
		final Node body = new Node();
		final Box box = new Box("mybuttonbox", new Vector3(), scale, scale, scale);
		box.setModelBound(new BoundingBox());        
		box.setSolidColor(new ColorRGBA((float)1,(float)1,(float)1,1));
        body.setName("mybutton");
        body.attachChild(box);
        
		// make the outline
		ColorRGBA[] colors = new ColorRGBA[24];
		for ( int i=0; i<colors.length; i++)
			colors[i] = new ColorRGBA(0.20f,0.20f,0.20f,1.0f);

		// define outline lines for the box
		Vector3[] outline = new Vector3[]  { 
				new Vector3( scale, scale, scale), new Vector3(-scale, scale, scale),
				new Vector3( scale, scale, scale), new Vector3( scale,-scale, scale),
				new Vector3( scale, scale, scale), new Vector3( scale, scale,-scale),				
				new Vector3(-scale, scale, scale), new Vector3(-scale,-scale, scale),
				new Vector3(-scale, scale, scale), new Vector3(-scale, scale,-scale),				
				new Vector3( scale,-scale, scale), new Vector3(-scale,-scale, scale),
				new Vector3( scale,-scale, scale), new Vector3( scale,-scale,-scale),				
				new Vector3(-scale,-scale, scale), new Vector3(-scale,-scale,-scale),
				new Vector3( scale, scale,-scale), new Vector3(-scale, scale,-scale),
				new Vector3( scale, scale,-scale), new Vector3( scale,-scale,-scale),
				new Vector3(-scale, scale,-scale), new Vector3(-scale,-scale,-scale),				
				new Vector3( scale,-scale,-scale), new Vector3(-scale,-scale,-scale)				
		};

		// Outlines
		Line line = new Line("vector", outline, null, colors, null);
		line.setAntialiased(false);
		line.setModelBound(new BoundingBox()); 
		line.setLineWidth(3f);
		line.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		line.setName("mybuttonoutline"); 
        
		// Setup the texturing
        buttontexture = new TextureState();  
        buttontexture.setEnabled(true);
        box.setRenderState(buttontexture);
		
        line.setScale(0.7070 *1.01);
        body.setScale(0.7070);		
		attachChild(line);
        attachChild(body);
        
        this.setTranslation(0, -25, 0);
        
        rootnode.attachChild(this);
	}
	
	@Override
	public void start(Game game) {
		System.out.println("Button started");																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																					
		Scene physics = game.getPhysics();
		buttonnode = (Node)this.getChild("mybutton");
        buttonnode.setUserData(this);

		//setup shadowing
		game.getRendering().getPssmPass().add(buttonnode);
		game.getRendering().getPssmPass().addOccluder(buttonnode);

		ReadOnlyVector3 s = buttonnode.getScale();
		System.out.println("s="+s);
		
		buttonbody = new Body("default");
		buttongeometry = new jinngine.geometry.Box(s.getX(),s.getY(),s.getZ());
		buttonbody.addGeometry(buttongeometry);
		buttonbody.finalize();
		physics.addBody(buttonbody);
		physics.addForce(new GravityForce(buttonbody));
		buttonbody.setAngularVelocity(new jinngine.math.Vector3(0,0,0));

		Toolbox.setTransformFromNode(this, buttonbody);
		this.addController(Toolbox.createSpatialControllerForBody(buttonbody));
	}

	@Override
	public void stop(Game game) {
		// remove body
		game.getPhysics().removeBody(buttonbody);
		
		//clean shadowing
		game.getRendering().getPssmPass().remove(buttonnode);
		game.getRendering().getPssmPass().removeOccluder(buttonnode);

		// remove from scene
		game.getRendering().getScene().detachChild(this);
	}


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
		System.out.println("Button selected");
		pressed = selected;
		
	}
	@Override
	public void act(Game game) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Body getBodyFromNode(Node node) {
		return node==this?buttonbody:null;
	}

	@Override
	public boolean canBeSelected() {
		return true;
	}

	@Override
	public void getScale(jinngine.math.Vector3 scale) {
		scale.assign(this.getScale().getX(),this.getScale().getY(),this.getScale().getZ());
	}

	@Override
	public void setScale(jinngine.math.Vector3 scale) {
//		System.out.println("setScale()" + scale);

		this.setScale(Math.max(0.5,scale.x),Math.max(0.5,scale.y),Math.max(0.5,scale.z));
		
		// reflect the change in jinngine geometry
		ReadOnlyVector3 s = this.getScale();
//		System.out.println("setting scale " + s);
		buttongeometry.setBoxSideLengths(Math.max(0.5,s.getX()),Math.max(0.5,s.getY()),Math.max(0.5,s.getZ()));

	}
}
