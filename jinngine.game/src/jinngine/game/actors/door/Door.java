package jinngine.game.actors.door;

import java.nio.FloatBuffer;
import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.framework.Scene;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.image.Texture.MagnificationFilter;
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.BlendState.DestinationFunction;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.renderer.state.FogState.DensityFunction;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

import jinngine.game.Game;
import jinngine.game.actors.PhysicalActor;
import jinngine.geometry.Geometry;
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;
import jinngine.physics.constraint.joint.HingeJoint;
import jinngine.physics.force.GravityForce;

public class Door implements PhysicalActor {

	public Body platformbox1body;
	private Node doornode;
	private static jinngine.math.Vector3 pos = new jinngine.math.Vector3();
	//private double scale = 0.5;
	private float shade ;
	
	public Door(jinngine.math.Vector3 pos, boolean lighting, double scale, double shade) {
		this.pos.assign(pos);
		//this.scale = scale;
		this.shade = (float)shade;
	}
	
	@Override
	public void act( Game game ) {
		// TODO Auto-generated method stub

	} 

	@Override
	public void start( Game game ) {
		PhysicsScene physics = game.getPhysics();
		Node rootnode = game.getRendering().getRootNode();

		


		// Make a box...
		doornode = new Node();
		rootnode.attachChild(doornode);
        
		// make the outline
		ColorRGBA[] colors = new ColorRGBA[24];
		for ( int i=0; i<colors.length; i++)
			colors[i] = new ColorRGBA(0f,0f,0f,1.0f);

		
		double xext=0.66666, yext=1, zext=0.1;
		// define outline lines for the box
		Vector3[] outline = new Vector3[]  { 
				new Vector3( xext, yext, zext), new Vector3(-xext, yext, zext),
				new Vector3( xext, yext, zext), new Vector3( xext,-yext, zext),
				new Vector3( xext, yext, zext), new Vector3( xext, yext,-zext),
				
				new Vector3(-xext, yext, zext), new Vector3(-xext,-yext, zext),
				new Vector3(-xext, yext, zext), new Vector3(-xext, yext,-zext),
				
				new Vector3( xext,-yext, zext), new Vector3(-xext,-yext, zext),
				new Vector3( xext,-yext, zext), new Vector3( xext,-yext,-zext),
				
				new Vector3(-xext,-yext, zext), new Vector3(-xext,-yext,-zext),

				new Vector3( xext, yext,-zext), new Vector3(-xext, yext,-zext),
				new Vector3( xext, yext,-zext), new Vector3( xext,-yext,-zext),

				new Vector3(-xext, yext,-zext), new Vector3(-xext,-yext,-zext),
				
				new Vector3( xext,-yext,-zext), new Vector3(-xext,-yext,-zext)
				
		};

		Line line = new Line("vector", outline, null, colors, null);
		line.setAntialiased(false);
        line.setModelBound(new BoundingBox());
        line.setLineWidth(4f);
        

        line.getSceneHints().setLightCombineMode(LightCombineMode.Off);
    
//        LightState _lightState = new LightState();
//        _lightState.setEnabled(false);
//        line.setRenderState(_lightState);
// 

        
        //doornode.attachChild(line);

		final ColladaImporter colladaImporter = new ColladaImporter();
        final ColladaStorage storage = colladaImporter.readColladaFile("doorframe.dae");
        Node doorscene = storage.getScene();

        
//		Box box = new Box("Door", new Vector3(), xext, yext, zext);
//        box.setModelBound(new BoundingBox());
//        box.setSolidColor(new ColorRGBA(shade,shade,shade,1));        
//		doornode.attachChild(box);

        Line dooroutline = (Line)doorscene.getChild("DoorOutline-Geometry_lines");
		dooroutline.setLineWidth(4);
		dooroutline.setDefaultColor(new ColorRGBA(0,0,0,1));
        doornode.attachChild(dooroutline);

        Spatial doorfaces = doorscene.getChild("Door-Geometry_triangles");
        doornode.attachChild(doorfaces);
        
        System.out.println("door = " + doorfaces);
        
        
//        Texture tex = TextureManager.load("platformbox1texlarge.tga", 
//        		Texture.MinificationFilter.Trilinear,
//                Format.Guess, true);
//        tex.setMagnificationFilter(MagnificationFilter.Bilinear);
//        tex.setAnisotropicFilterPercent(0);
//        TextureState headts = new TextureState();
//        headts.setEnabled(true);
//        headts.setTexture( tex, 0 );
//        platformbox1.setRenderState(headts);
		
        // define some light
        final DirectionalLight light = new DirectionalLight();
        light.setDirection(0.5, 1, 0);
        light.setDiffuse(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
        light.setAmbient(new ColorRGBA(1.9f, 1.9f, 1.9f, 1.0f));
        light.setSpecular(new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f));
        light.setAttenuate(false);
        light.setEnabled(true);
        
        final LightState ls = new LightState();
        ls.attach(light);
        ls.setEnabled(true);
        //platformbox1.setRenderState(ls);

        // connect the node with this actor
        doornode.setUserData(this);
        
        //setup shadowing
        game.getRendering().getPssmPass().add(doornode);
       // game.getRendering().getPssmPass().addOccluder(doorfaces);
        
        platformbox1body = new Body(new jinngine.geometry.Box(xext*2,yext*2,zext*2));
        physics.addBody(platformbox1body);
        physics.addForce(new GravityForce(platformbox1body));
        platformbox1body.setPosition(pos);
        platformbox1body.setAngularVelocity(new jinngine.math.Vector3(0,0,0));
                      
        final Body doorframe = new Body();
        doorframe.addGeometry(new jinngine.geometry.Box(1,2,1, -1,0,0));
        doorframe.addGeometry(new jinngine.geometry.Box(1,2,1,  1,0,0));
        doorframe.finalize();
        doorframe.setPosition(pos);
//        doorframe.setFixed(true);
        physics.addBody(doorframe);
        physics.addForce(new GravityForce(doorframe));
        
        HingeJoint j = new HingeJoint(doorframe, platformbox1body, pos.add(new jinngine.math.Vector3(-xext,0,0)), jinngine.math.Vector3.j);
        physics.addConstraint(j);
        //j.getHingeControler().setLimits(0, Math.PI);
        
        doornode.addController(new SpatialController<Spatial>() {
            public void update(final double time, final Spatial caller) {
            	Body body = platformbox1body;
            	caller.setTranslation(body.state.position.x, body.state.position.y, body.state.position.z);
            	ReadOnlyMatrix3 mat = new Matrix3(body.state.rotation.a11, body.state.rotation.a12, body.state.rotation.a13,
            			body.state.rotation.a21, body.state.rotation.a22, body.state.rotation.a23, 
            			body.state.rotation.a31, body.state.rotation.a32, body.state.rotation.a33);
            	
            	caller.setRotation(mat);
            }
        });
        
        Node visualframebox = new Node();
		rootnode.attachChild(visualframebox);
		
		// get the outline and do stuff
		Line l = (Line)doorscene.getChild("Outline_lines");		
		l.setLineWidth(4);
		l.setDefaultColor(new ColorRGBA(0,0,0,1));

		visualframebox.attachChild(l);
		
		Spatial faces = doorscene.getChild("Doorfaces_triangles");

		visualframebox.attachChild(faces);

        //setup shadowing
        game.getRendering().getPssmPass().add(faces);
        game.getRendering().getPssmPass().addOccluder(faces);

        visualframebox.addController(new SpatialController<Spatial>() {
            public void update(final double time, final Spatial caller) {
            	Body body = doorframe;
            	caller.setTranslation(body.state.position.x, body.state.position.y, body.state.position.z);
            	ReadOnlyMatrix3 mat = new Matrix3(body.state.rotation.a11, body.state.rotation.a12, body.state.rotation.a13,
            			body.state.rotation.a21, body.state.rotation.a22, body.state.rotation.a23, 
            			body.state.rotation.a31, body.state.rotation.a32, body.state.rotation.a33);
            	
            	caller.setRotation(mat);
            }
        });

	}

	@Override
	public void stop( Game game ) {
		// TODO Auto-generated method stub

	}

	@Override
	public Body getBodyFromNode(Node node) {
		if (node == doornode)
			return platformbox1body;
		else
			return null;
	}

}
