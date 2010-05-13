package jinngine.game.actors.platform1;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;


import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.extension.ui.UIFrame;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;

import jinngine.game.Game;
import jinngine.game.Toolbox;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.actors.ScalableActor;
import jinngine.geometry.ConvexHull;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Scene;
import jinngine.physics.force.GravityForce;

/**
 * A primitive convex platform
 */
public class ConvexPlatform extends Node implements PhysicalActor, ScalableActor {

	// jinngine
	private Body platformconvexbody;
	private jinngine.geometry.Box boxgeometry;
	private jinngine.geometry.ConvexHull hull;
	
	// ardor3d
	private Node platform;
	private Spatial convexplatformfaces;
	
	// properties
	private double shade = 0.7;
	private String daefilename;
	private boolean isFixed = false;
	private UIFrame frame;

	public ConvexPlatform() {
		daefilename = "convexplatform.dae";
	}

	@Override
	public void read(final InputCapsule ic) throws IOException {
		super.read(ic);
		// read some settings
		isFixed = ic.readBoolean("isFixed", false);
		shade = ic.readDouble("shade", 0.7);
		daefilename = (String)ic.readString("daefilename", "convexplatform.dae");
	}

	@Override
	public void write(final OutputCapsule oc) throws IOException {
		super.write(oc);		
		// write some settings
		oc.write( platformconvexbody.isFixed(), "isFixed", false);
		oc.write( shade, "shade", 0.7);
		oc.write( daefilename, "daefilename", "convexplatform.dae");
	}

	public ConvexPlatform(double shade) {
		this.shade = (float)shade;
		this.daefilename = "convexplatform.dae";
	}

	@Override
	public void create(Game game) {
		Node scene = game.getRendering().getScene();
		this.setName("Actor:ConvexPlatform");

		platform = new Node();

		// load the geometry
        final ColladaImporter colladaImporter = new ColladaImporter();
        final ColladaStorage storage = colladaImporter.load(daefilename);
        Node convexplatformscene = storage.getScene();

        
        // set the transform of the collada file to the new platform node
        platform.setTransform(convexplatformscene.getChild("ConvexPlatformSolid").getTransform());
        
        // load the outline
        Line outline = (Line)convexplatformscene.getChild("ConvexPlatformOut_lines");
		outline.setLineWidth(4);
		outline.setDefaultColor(new ColorRGBA(0,0,0,1));
		outline.setScale(1.01);
        platform.attachChild(outline);
        
        // load faces
        Spatial convexplatformfaces = convexplatformscene.getChild("ConvexPlatformSolid-Geometry_triangles");
        platform.attachChild(convexplatformfaces);
	
        // set some color
        ((Mesh)convexplatformfaces).setSolidColor(new ColorRGBA((float)shade,(float)shade,(float)shade,1));        
       
        
		platform.attachChild(convexplatformfaces);
		platform.attachChild(outline);
		platform.setName("myconvexplatform");
		this.attachChild(platform);
//		platform.setTranslation(pos.x, pos.y, pos.z);


		this.setTranslation(3,-20,0);
		
		// add this to root node
		scene.attachChild(this);
	}

	@Override
	public void act( Game game ) {
		
		
	} 

	@Override
	public void start( Game game ) {
		Scene physics = game.getPhysics();
		platform = (Node)this.getChild("myconvexplatform");
//		final Spatial platformbox = this.getChild("myplatformbox");


		ReadOnlyVector3 s = platform.getScale();
		
		// get the mesh
		convexplatformfaces = platform.getChild("ConvexPlatformSolid-Geometry_triangles");
		MeshData meshdata  = ((Mesh)convexplatformfaces).getMeshData();
		FloatBuffer verticedata = meshdata.getVertexBuffer();

		// set color
        ((Mesh)convexplatformfaces).setSolidColor(new ColorRGBA((float)shade,(float)shade,(float)shade,1));
		
		//setup shadowing
		game.getRendering().getPssmPass().add(convexplatformfaces);
		game.getRendering().getPssmPass().addOccluder(convexplatformfaces);

		
		// load vertices
		// points for the convex hull
		List<jinngine.math.Vector3> points = new ArrayList<jinngine.math.Vector3>();
		float[] p = new float[3];
		verticedata.rewind();
		while ( verticedata.hasRemaining()) {
			verticedata.get(p, 0, 3);
			points.add(new jinngine.math.Vector3(p[0],p[1],p[2]));
		}
		
		
		System.out.println("ConvexPlatform: Vertices for hull " + points.size());

		//	        System.out.println(verticedata);

		// do the convex hull and setup mesh 
		hull = new ConvexHull(points);	
		hull.setLocalScale(new Vector3(this.getScale().getX(), this.getScale().getY(), this.getScale().getZ()));
		
		System.out.println("ConvexPlatform: final hull vertices " + hull.getNumberOfVertices());
		

		
		platformconvexbody = new Body("convexplatform", hull);
		//boxgeometry = new jinngine.geometry.Box(s.getX(),s.getY(),s.getZ());
		//platformconvexbody.addGeometry(boxgeometry);
		//platformconvexbody.finalize();
		physics.addBody(platformconvexbody);
		physics.addForce(new GravityForce(platformconvexbody));
		platformconvexbody.setAngularVelocity(new jinngine.math.Vector3(0,0,0));
		platformconvexbody.setFixed(isFixed);

		Toolbox.setTransformFromNode(this, platformconvexbody);
		this.addController(Toolbox.createSpatialControllerForBody(platformconvexbody));
	}
	
	public void update( Game game) {
		// reflect the change in jinngine geometry
		ReadOnlyVector3 s = platform.getScale();
//		boxgeometry.setBoxSideLengths(Math.max(0.5,s.getX()),Math.max(0.5,s.getY()),Math.max(0.5,s.getZ()));
	}

	@Override
	public void stop( Game game ) {
		//clean shadowing
		game.getRendering().getPssmPass().remove(convexplatformfaces);
		game.getRendering().getPssmPass().removeOccluder(convexplatformfaces);
		
		//clean physics
		game.getPhysics().removeBody(platformconvexbody);
	
		// remove from scene
		game.getRendering().getScene().detachChild(this);
	}

	@Override
	public Body getBodyFromNode(Node node) {
//		System.out.println("Given node=" + node);
		if (node == this)
			return platformconvexbody;
		else
			return null;
	}

	@Override
	public void getScale(jinngine.math.Vector3 scale) {
		scale.assign(this.getScale().getX(),this.getScale().getY(),this.getScale().getZ());
	}

	@Override
	public void setScale(jinngine.math.Vector3 scale) {
		this.setScale(Math.max(0.5,scale.x),Math.max(0.5,scale.y),Math.max(0.5,scale.z));
		
		// reflect the change in jinngine geometry
		ReadOnlyVector3 s = this.getScale();

		// scale jinngine geometry
		hull.setLocalScale(new Vector3(s.getX(),s.getY(),s.getZ()));
	}
}
