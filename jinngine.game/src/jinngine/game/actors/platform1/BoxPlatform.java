package jinngine.game.actors.platform1;

import java.io.IOException;
import java.nio.FloatBuffer;

import javax.tools.Tool;

import com.ardor3d.bounding.BoundingBox;

import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UICheckBox;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIPanel;

import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.layout.BorderLayout;
import com.ardor3d.extension.ui.layout.BorderLayoutData;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;

import jinngine.game.Game;
import jinngine.game.Toolbox;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ConfigurableActor;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.actors.ScalableActor;
import jinngine.game.actors.SelectableActor;
import jinngine.physics.Body;
import jinngine.physics.Scene;
import jinngine.physics.force.GravityForce;

/**
 * A primitive box platform
 */
public class BoxPlatform extends Node implements PhysicalActor, ScalableActor  {

	// jinngine
	private Body boxplatformbody;
	private jinngine.geometry.Box boxgeometry;
	
	// ardor3d
	private Node platform;
	
	// properties
	private final jinngine.math.Vector3 pos = new jinngine.math.Vector3();	
	private double shade = 0.7;	
	private boolean isFixed = false;


	public BoxPlatform() {
	}

	@Override
	public void read(final InputCapsule ic) throws IOException {
		super.read(ic);
		// read some settings
		isFixed = ic.readBoolean("isFixed", false);
		shade = (float)ic.readDouble("shade", 1.0);

	}

	@Override
	public void write(final OutputCapsule oc) throws IOException {
		super.write(oc);		
		// write some settings
		oc.write(boxplatformbody.isFixed(), "isFixed", false);
		oc.write(shade, "shade", 1.0);
	}

	public BoxPlatform(jinngine.math.Vector3 pos, double shade) {
		this.pos.assign(pos);
		this.shade = (float)shade;
	}

	@Override
	public void create(Game game) {
		Node rootnode = game.getRendering().getScene();
		this.setName("Actor:Platform");

		platform = new Node();


		// make the outline
		ColorRGBA[] colors = new ColorRGBA[24];
		for ( int i=0; i<colors.length; i++)
			colors[i] = new ColorRGBA(0f,0f,0f,1.0f);

		// define outline lines for the box
		final double scale=0.5;
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

		Line line = new Line("vector", outline, null, colors, null);
		line.setAntialiased(false);
		line.setModelBound(new BoundingBox()); 
		line.setLineWidth(2.8f);
		line.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		line.setName("myplatformboxlines");
		line.setScale(1.01);

		Box box = new Box("myplatformbox", new Vector3(), scale, scale, scale);
		box.setModelBound(new BoundingBox());        
		box.setSolidColor(new ColorRGBA((float)shade,(float)shade,(float)shade,1));

		platform.attachChild(box);
		platform.attachChild(line);
		platform.setName("myplatform");
		this.attachChild(platform);
		platform.setTranslation(pos.x, pos.y, pos.z);

//		platform.setScale(2,2,3);
		// connect the node with this actor
		platform.setUserData(this);

		// add this to root node
		rootnode.attachChild(this);
	}

	@Override
	public void act( Game game ) { } 

	@Override
	public void start( Game game ) {
		Scene physics = game.getPhysics();
		platform = (Node)this.getChild("myplatform");
		final Spatial platformbox = this.getChild("myplatformbox");

		//setup shadowing
		game.getRendering().getPssmPass().add(platform);
		game.getRendering().getPssmPass().addOccluder(platformbox);

		// setup physics with jinngine
		boxplatformbody = new Body("default");
		// use the node scale as dimentions for the box
		boxgeometry = new jinngine.geometry.Box(this.getScale().getX(),this.getScale().getY(),this.getScale().getZ());
		boxplatformbody.addGeometry(boxgeometry);
		boxplatformbody.finalize();
		physics.addBody(boxplatformbody);
		physics.addForce(new GravityForce(boxplatformbody));
		boxplatformbody.setPosition(pos);
		boxplatformbody.setAngularVelocity(new jinngine.math.Vector3(0,0,0));
		boxplatformbody.setFixed(isFixed);

		// create spatial controller
		Toolbox.setTransformFromNode(this, boxplatformbody);
		this.addController(Toolbox.createSpatialControllerForBody(boxplatformbody));
	}
	
	public void update( Game game) {
	}

	@Override
	public void stop( Game game ) {
		// clean out physics
		game.getPhysics().removeBody(boxplatformbody);
		
		// clean out shadowing
		final Spatial platformbox = this.getChild("myplatformbox");
		game.getRendering().getPssmPass().remove(platform);
		game.getRendering().getPssmPass().removeOccluder(platformbox);

		// remove our selves 
		game.getRendering().getScene().detachChild(this);
	}

	@Override
	public Body getBodyFromNode(Node node) {
		return boxplatformbody;
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
		boxgeometry.setBoxSideLengths(Math.max(0.5,s.getX()),Math.max(0.5,s.getY()),Math.max(0.5,s.getZ()));

	}
}
