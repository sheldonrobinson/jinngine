package jinngine.game.actors.platform1;

import java.nio.FloatBuffer;

import javax.tools.Tool;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.image.Texture.MagnificationFilter;
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.renderer.state.FogState.DensityFunction;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

import jinngine.game.Game;
import jinngine.game.Toolbox;
import jinngine.game.actors.PhysicalActor;
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;
import jinngine.physics.force.GravityForce;

/**
 * A primitive box platform
 */
public class Platform1 extends Node implements PhysicalActor {

	public Body platformbox1body;
	private Node platform;
	private static jinngine.math.Vector3 pos = new jinngine.math.Vector3();
	private float shade ;
	
	public Platform1() {
	}
	
	public Platform1(jinngine.math.Vector3 pos, double shade) {
		this.pos.assign(pos);
		this.shade = (float)shade;
	}
	
	@Override
	public void create(Game game) {
		Node rootnode = game.getRendering().getRootNode();
		this.setName("Actor:Platform");
		
		platform = new Node();

		 double scale = 0.5;
		
		// make the outline
		ColorRGBA[] colors = new ColorRGBA[24];
		for ( int i=0; i<colors.length; i++)
			colors[i] = new ColorRGBA(0f,0f,0f,1.0f);
		
		
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

		Line line = new Line("vector", outline, null, colors, null);
		line.setAntialiased(false);
        line.setModelBound(new BoundingBox());
        line.setLineWidth(4f);
        line.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        line.setName("myplatformboxlines");

		Box box = new Box("myplatformbox", new Vector3(), scale, scale, scale);
        box.setModelBound(new BoundingBox());        
        box.setSolidColor(new ColorRGBA(shade,shade,shade,1));
        
		platform.attachChild(box);
        platform.attachChild(line);
        platform.setName("myplatform");
		this.attachChild(platform);
		platform.setTranslation(pos.x, pos.y, pos.z);
		
		platform.setScale(2,2,3);
		// connect the node with this actor
        platform.setUserData(this);

        
        
        // add this to root node
        rootnode.attachChild(this);
	}
	
	@Override
	public void act( Game game ) { } 

	@Override
	public void start( Game game ) {
		PhysicsScene physics = game.getPhysics();
		platform = (Node)this.getChild("myplatform");
		final Box platformbox = (Box)this.getChild("myplatformbox");

		//setup shadowing
        game.getRendering().getPssmPass().add(platform);
        game.getRendering().getPssmPass().addOccluder(platformbox);
        
        ReadOnlyVector3 s = platform.getScale();
        
        platformbox1body = new Body(new jinngine.geometry.Box(s.getX(),s.getY(),s.getZ()));
        physics.addBody(platformbox1body);
        physics.addForce(new GravityForce(platformbox1body));
        platformbox1body.setPosition(pos);
        platformbox1body.setAngularVelocity(new jinngine.math.Vector3(0,0,0));
        
        Toolbox.setTransformFromNode(platform, platformbox1body);
        platform.addController(Toolbox.createSpatialControllerForBody(platformbox1body));
	}

	@Override
	public void stop( Game game ) {}
	
	@Override
	public Body getBodyFromNode(Node node) {
		if (node == platform)
			return platformbox1body;
		else
			return null;
	}

}
