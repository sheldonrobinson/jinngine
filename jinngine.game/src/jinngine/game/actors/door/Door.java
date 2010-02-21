package jinngine.game.actors.door;

import java.io.IOException;

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
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;

import jinngine.game.Game;
import jinngine.game.Toolbox;
import jinngine.game.actors.ConfigurableActor;
import jinngine.game.actors.PhysicalActor;

import jinngine.math.Matrix3;
import jinngine.math.Quaternion;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.PhysicsScene;
import jinngine.physics.constraint.joint.HingeJoint;
import jinngine.physics.force.GravityForce;


public class Door extends Node implements PhysicalActor, ConfigurableActor {

	private Body doorbody;
	private Body doorframebody;
	private Node doornode;
	private Node doorframenode;

	private boolean fixedframe = false;
	
	public Door(){
		
	}
	
	@Override
	public void write(Ardor3DExporter e) throws IOException {
		super.write(e);
		
		e.getCapsule(this).write( fixedframe,"fixedframe", false);
	}
	
	@Override
	public void read(Ardor3DImporter e) throws IOException {
		super.read(e);
		fixedframe = e.getCapsule(this).readBoolean( "fixedframe", false);
	}
	
	@Override
	public void create(Game game) {
		this.setName("Actor:Door");
		
		// make the door
		doornode = new Node();
		doornode.setName("mydoornode");
		this.attachChild(doornode);
		
		// load door asset
        final ColladaImporter colladaImporter = new ColladaImporter();
        final ColladaStorage storage = colladaImporter.readColladaFile("doorframe.dae");
        Node doorscene = storage.getScene();

        // set the door transform of the collada file to the new door node
        doornode.setTransform(doorscene.getChild("Door").getTransform());
        
        //System.out.println(""+doornode.getTransform());
        
        Line dooroutline = (Line)doorscene.getChild("DoorOutline-Geometry_lines");
		dooroutline.setLineWidth(4);
		dooroutline.setDefaultColor(new ColorRGBA(0,0,0,1));
        doornode.attachChild(dooroutline);
        Spatial doorfaces = doorscene.getChild("Door-Geometry_triangles");
        doornode.attachChild(doorfaces);

        // setup the door frame 
        doorframenode = new Node();
        doorframenode.setName("mydoorframenode");
		this.attachChild(doorframenode);
		
		// get the outline and do stuff
		Line l = (Line)doorscene.getChild("Outline_lines");		
		l.setLineWidth(4);
		l.setDefaultColor(new ColorRGBA(0,0,0,1));
		doorframenode.attachChild(l);
		
		// faces 
		Spatial faces = doorscene.getChild("Doorfaces_triangles");
		doorframenode.attachChild(faces);

		// translate initially
		doorframenode.setTranslation(doorframenode.getTranslation().add(0, -10, 0, null));
		doornode.setTranslation(doornode.getTranslation().add(0, -10, 0, null));
		
		// attach to root
		game.getRendering().getRootNode().attachChild(this);
	}
	
	@Override
	public void act( Game game ) {
		// TODO Auto-generated method stub

	} 

	@Override
	public void start( Game game ) {
		PhysicsScene physics = game.getPhysics();
		Node rootnode = game.getRendering().getRootNode();

		// obtain the transform
		Vector3 translation = new Vector3();
		Quaternion orientation = new Quaternion();
		Toolbox.getNodeTransform(this, translation, orientation);
		
		//set old
		double xext=0.66666, yext=0.9, zext=0.2;
		

		doornode = (Node)getChild("mydoornode");
		doorframenode = (Node)getChild("mydoorframenode");
				
        
        //setup shadowing
        game.getRendering().getPssmPass().add(doornode);
        //game.getRendering().getPssmPass().addOccluder(doorfaces);
       
        // door
        jinngine.geometry.Box doorbox = new jinngine.geometry.Box(xext*2,yext*2,zext*2);
        doorbox.setFrictionCoefficient(0);
        doorbody = new Body(doorbox);
        physics.addBody(doorbody);
        physics.addForce(new GravityForce(doorbody));
        //doorbody.setPosition(doorbody.getPosition().add(new jinngine.math.Vector3(0,-0.125,0)));
        doorbody.setAngularVelocity(new jinngine.math.Vector3(0,0,0));                      
        //set position
        Toolbox.setTransformFromNode(doornode, doorbody);
        // attach body to node
        doornode.addController( Toolbox.createSpatialControllerForBody(doorbody));
		// add jinngine debug geometry        
        Toolbox.addJinngineDebugMesh("jinnginedebugmesh1", doornode, doorbody);

        // doorframe
        doorframebody = new Body();
        jinngine.geometry.Box box1 = new jinngine.geometry.Box(0.35,2.3,0.35, -0.85,0,0);
        doorframebody.addGeometry(box1);
        jinngine.geometry.Box box2 = new jinngine.geometry.Box(0.35,2.3,0.35, 0.85,0,0);
        doorframebody.addGeometry(box2);        
        doorframebody.finalize();
        doorframebody.setFixed(fixedframe);
        physics.addBody(doorframebody);
        physics.addForce(new GravityForce(doorframebody));
        Toolbox.setTransformFromNode(doorframenode, doorframebody);
        doorframenode.addController(Toolbox.createSpatialControllerForBody(doorframebody));
        //debug
		Toolbox.addJinngineDebugMesh("jinnginedebugmesh2", doorframenode, doorframebody);
      
        // door hinge (alternative constructor, should be changed)
		//create a basis
		Matrix3 basis = jinngine.util.GramSchmidt.run(jinngine.math.Vector3.j);
		//Matrix3 basis = Matrix3.identity();
		HingeJoint j = new HingeJoint(doorframebody, doorbody, new Vector3(-0.6666666666,0,0), basis.column(0), basis.column(1), basis.column(2), new Vector3(-0.66666666, 0.12263,0), basis.column(0), basis.column(1), 0, 0);
        //HingeJoint j = new HingeJoint(doorframebody, doorbody, translation.add(new jinngine.math.Vector3(-xext,0,0)), jinngine.math.Vector3.j);
        physics.addConstraint(j);
        j.getHingeControler().setLimits(0.1, Math.PI*0.5);
        j.getHingeControler().setFrictionMagnitude(0.25);

        // connect the node with this actor
        doornode.setUserData(this);
		doorframenode.setUserData(this);
        		
        //setup shadowing
        game.getRendering().getPssmPass().add(doorframenode);
        game.getRendering().getPssmPass().addOccluder(doorframenode);

	}
	
	@Override
	public void stop( Game game ) {
		// TODO Auto-generated method stub

	}

	@Override
	public Body getBodyFromNode(Node node) {
		if (node == doornode)
			return doorbody;
		else if ( node == doorframenode)
			return doorframebody;
		else
			return null;
	}
	
	@Override
	public void configure(final Game game) {
		final UIFrame frame;
		final UIPanel panel = new UIPanel();
		panel.setForegroundColor(ColorRGBA.DARK_GRAY);
		panel.setLayout(new BorderLayout());

		final RowLayout rowLay = new RowLayout(false, false, false);
		final UIPanel centerPanel = new UIPanel(rowLay);
		centerPanel.setLayoutData(BorderLayoutData.CENTER);
		panel.add(centerPanel);
		

		final UICheckBox fixedsetting = new UICheckBox("fixed door frame");
		fixedsetting.setSelected(fixedframe);
		fixedsetting.setEnabled(true);
		centerPanel.add(fixedsetting);
		fixedsetting.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				boolean state = ((UICheckBox)event.getSource()).isSelected();
				//remove velocity from body
				doorframebody.setVelocity(0,0,0);
				doorframebody.setAngularVelocity(0, 0, 0);
				game.getPhysics().fixBody(doorframebody, state);
				fixedframe = state;				
				System.out.println("setting state " + state);
			}
		});

		frame = new UIFrame("Door properties");
		frame.setContentPanel(panel);
		frame.updateMinimumSizeFromContents();
		frame.layout();
		frame.pack();
		frame.setUseStandin(false);
		frame.setOpacity(0.75f);
		frame.setLocationRelativeTo(game.getRendering().getCamera());
		frame.setName("sample");

		game.getRendering().getHud().add(frame);
		
//		game.getRendering().getHud().remove(frame);
	
	}

}
