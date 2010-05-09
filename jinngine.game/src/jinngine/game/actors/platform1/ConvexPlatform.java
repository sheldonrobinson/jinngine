package jinngine.game.actors.platform1;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.tools.Tool;

import com.ardor3d.bounding.BoundingBox;

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
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.geom.BufferUtils;

import jinngine.game.Game;
import jinngine.game.Toolbox;
import jinngine.game.actors.Actor;
import jinngine.game.actors.ConfigurableActor;
import jinngine.game.actors.PhysicalActor;
import jinngine.game.actors.SelectableActor;
import jinngine.geometry.ConvexHull;
import jinngine.physics.Body;
import jinngine.physics.Scene;
import jinngine.physics.force.GravityForce;

/**
 * A primitive convex platform
 */
public class ConvexPlatform extends Node implements PhysicalActor, ConfigurableActor {

	// jinngine
	private Body platformconvexbody;
	private jinngine.geometry.Box boxgeometry;
	private jinngine.geometry.ConvexHull platformhullgeometry;
	
	// ardor3d
	private Node platform;
	
	// properties
	private final jinngine.math.Vector3 pos = new jinngine.math.Vector3();	
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
		oc.write( isFixed, "isFixed", false);
		oc.write( shade, "shade", 0.7);
		oc.write( daefilename, "daefilename", "convexplatform.dae");
	}

	public ConvexPlatform(jinngine.math.Vector3 pos, double shade) {
		this.pos.assign(pos);
		this.shade = (float)shade;
		this.daefilename = "convexplatform.dae";
		

	}

	@Override
	public void create(Game game) {
		Node rootnode = game.getRendering().getScene();
		this.setName("Actor:ConvexPlatform");

		platform = new Node();

		// load door asset
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
        
        // load vertices
        //Mesh convexplatformvertices = (Mesh)convexplatformscene.getChild("ConvexPlatform_001_vertex");
        

//		Line line = new Line("vector", outline, null, colors, null);
//		line.setAntialiased(false);
//		line.setModelBound(new BoundingBox()); 
//		line.setLineWidth(2.8f);
//		line.getSceneHints().setLightCombineMode(LightCombineMode.Off);
//		line.setName("myplatformboxlines");
		

//        MeshData meshdata       = ((Mesh)convexplatformfaces).getMeshData();
//        FloatBuffer verticedata = meshdata.getVertexBuffer();

//       // load vertices
//       // points for the convex hull
//       List<jinngine.math.Vector3> points = new ArrayList<jinngine.math.Vector3>();
//       float[] p = new float[3];
//       verticedata.rewind();
//       while ( verticedata.hasRemaining()) {
//    	   verticedata.get(p, 0, 3);
//    	   points.add(new jinngine.math.Vector3(p[0],p[1],p[2]));
//       }
//       
////        System.out.println(verticedata);
//        
//        // do the convex hull and setup mesh 
//		ConvexHull hull = new ConvexHull(points);

//		Box box = new Box("myplatformbox", new Vector3(), scale, scale, scale);
//		box.setModelBound(new BoundingBox());        
//		box.setSolidColor(new ColorRGBA((float)shade,(float)shade,(float)shade,1));

		platform.attachChild(convexplatformfaces);
		platform.attachChild(outline);
		platform.setName("myconvexplatform");
		this.attachChild(platform);
		platform.setTranslation(pos.x, pos.y, pos.z);

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
		platform = (Node)this.getChild("myconvexplatform");
//		final Spatial platformbox = this.getChild("myplatformbox");


		ReadOnlyVector3 s = platform.getScale();
		
		// get the mesh
		Spatial convexplatformfaces = platform.getChild("ConvexPlatformSolid-Geometry_triangles");
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
		ConvexHull hull = new ConvexHull(points);
		
		System.out.println("ConvexPlatform: final hull vertices " + hull.getNumberOfVertices());
		

		
		platformconvexbody = new Body("convexplatform", hull);
		//boxgeometry = new jinngine.geometry.Box(s.getX(),s.getY(),s.getZ());
		//platformconvexbody.addGeometry(boxgeometry);
		//platformconvexbody.finalize();
		physics.addBody(platformconvexbody);
		physics.addForce(new GravityForce(platformconvexbody));
		platformconvexbody.setPosition(pos);
		platformconvexbody.setAngularVelocity(new jinngine.math.Vector3(0,0,0));
		platformconvexbody.setFixed(isFixed);

		Toolbox.setTransformFromNode(platform, platformconvexbody);
		platform.addController(Toolbox.createSpatialControllerForBody(platformconvexbody));
	}
	
	public void update( Game game) {
		// reflect the change in jinngine geometry
		ReadOnlyVector3 s = platform.getScale();
//		boxgeometry.setBoxSideLengths(Math.max(0.5,s.getX()),Math.max(0.5,s.getY()),Math.max(0.5,s.getZ()));
	}

	@Override
	public void stop( Game game ) {}

	@Override
	public Body getBodyFromNode(Node node) {
		if (node == platform)
			return platformconvexbody;
		else
			return null;
	}

	@Override
	public void configure(final Game game) {
			final UIPanel panel = new UIPanel();
			panel.setForegroundColor(ColorRGBA.DARK_GRAY);
			panel.setLayout(new BorderLayout());

			final RowLayout rowLay = new RowLayout(false, false, false);
			final UIPanel centerPanel = new UIPanel(rowLay);
			centerPanel.setLayoutData(BorderLayoutData.CENTER);
			panel.add(centerPanel);
			
			final double dv = 0.5;
			
			final UIButton plusx = new UIButton("+X");
			plusx.setEnabled(true);
			centerPanel.add(plusx); 
			plusx.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					platform.setScale(platform.getScale().add(dv, 0, 0, null));

					System.out.println("pressed state " + event);
				}
			});

			final UIButton minusx = new UIButton("-X");
			minusx.setEnabled(true);
			centerPanel.add(minusx);
			minusx.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					platform.setScale(platform.getScale().add(-dv, 0, 0, null));
					update(game);
					System.out.println("pressed state " + event);
				}
			});

			final UIButton plusy = new UIButton("+Y");
			plusy.setEnabled(true);
			centerPanel.add(plusy); 
			plusy.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					platform.setScale(platform.getScale().add(0, dv, 0, null));
					update(game);
					System.out.println("pressed state " + event);
				}
			});

			final UIButton minusy = new UIButton("-Y");
			minusy.setEnabled(true);
			centerPanel.add(minusy);
			minusy.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					platform.setScale(platform.getScale().add(0, -dv, 0, null));
					update(game);
					System.out.println("pressed state " + event);
				}
			});
			
			final UIButton plusz = new UIButton("+Z");
			plusz.setEnabled(true);
			centerPanel.add(plusz); 
			plusz.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					platform.setScale(platform.getScale().add(0, 0, dv, null));
					update(game);
					System.out.println("pressed state " + event);
				}
			});

			final UIButton minusz = new UIButton("-Z");
			minusz.setEnabled(true);
			centerPanel.add(minusz);
			minusz.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					platform.setScale(platform.getScale().add(0, 0, -dv, null));
					update(game);
					System.out.println("pressed state " + event);
				}
			});
			
			
			final UIButton shadebuttonplus = new UIButton("+shade");
			shadebuttonplus.setEnabled(true);
			centerPanel.add(shadebuttonplus); 
			shadebuttonplus.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					shade = shade + 0.1;
					((Box)getChild("myplatformbox")).setSolidColor(new ColorRGBA((float)shade,(float)shade,(float)shade,1.0f));
				}
			});

			final UIButton shadebuttonminus = new UIButton("-shade");
			shadebuttonminus.setEnabled(true);
			centerPanel.add(shadebuttonminus);
			shadebuttonminus.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					shade = shade - 0.1;
					((Box)getChild("myplatformbox")).setSolidColor(new ColorRGBA((float)shade,(float)shade,(float)shade,1.0f));
				}
			});
			
			final UICheckBox fixedsetting = new UICheckBox("fixed");
			fixedsetting.setSelected(isFixed);
			fixedsetting.setEnabled(true);
			centerPanel.add(fixedsetting);
			fixedsetting.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent event) {
					boolean state = ((UICheckBox)event.getSource()).isSelected();
					//remove velocity from body
					platformconvexbody.setVelocity(0,0,0);
					platformconvexbody.setAngularVelocity(0, 0, 0);
					game.getPhysics().fixBody(platformconvexbody, state);
					isFixed = state;				
					System.out.println("setting state " + state);
				}
			});

			frame = new UIFrame("Platform1");
			frame.setContentPanel(panel);
			frame.updateMinimumSizeFromContents();
			frame.layout();
			frame.pack();
			frame.setUseStandin(false);
			frame.setOpacity(0.75f);
			frame.setLocationRelativeTo(game.getRendering().getCamera());
			frame.setName("sample");

			game.getRendering().getHud().add(frame);
			
//			game.getRendering().getHud().remove(frame);
	}





}
