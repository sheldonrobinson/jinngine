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
import jinngine.game.actors.SelectableActor;
import jinngine.physics.Body;
import jinngine.physics.Scene;
import jinngine.physics.force.GravityForce;

/**
 * A primitive box platform
 */
public class BoxPlatform extends Node implements PhysicalActor, ConfigurableActor {

	// jinngine
	private Body platformbox1body;
	private jinngine.geometry.Box boxgeometry;
	//private jinngine.geometry.ConvexHull platformhullgeometry;
	
	// ardor3d
	private Node platform;
	
	// properties
	private final jinngine.math.Vector3 pos = new jinngine.math.Vector3();	
	private double shade;	
	private boolean isFixed = false;
	private UIFrame frame;
	private double scale = 1.0;



	public BoxPlatform() {
	}

	@Override
	public void read(final InputCapsule ic) throws IOException {
		super.read(ic);
		// read some settings
		isFixed = ic.readBoolean("isFixed", false);
		shade = (float)ic.readDouble("shade", 0);
		scale = (double)ic.readDouble("scale", 1.0);

	}

	@Override
	public void write(final OutputCapsule oc) throws IOException {
		super.write(oc);		
		// write some settings
		oc.write(isFixed, "isFixed", false);
		oc.write(shade, "shade", 0.0);
		oc.write(scale, "scale", 1.0);

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

		ReadOnlyVector3 s = platform.getScale();

		
		platformbox1body = new Body("default");
		boxgeometry = new jinngine.geometry.Box(scale*2,scale*2,scale*2);
		platformbox1body.addGeometry(boxgeometry);
		platformbox1body.finalize();
		physics.addBody(platformbox1body);
		physics.addForce(new GravityForce(platformbox1body));
		platformbox1body.setPosition(pos);
		platformbox1body.setAngularVelocity(new jinngine.math.Vector3(0,0,0));
		platformbox1body.setFixed(isFixed);

		Toolbox.setTransformFromNode(platform, platformbox1body);
		platform.addController(Toolbox.createSpatialControllerForBody(platformbox1body));
	}
	
	public void update( Game game) {
		// reflect the change in jinngine geometry
		ReadOnlyVector3 s = platform.getScale();
		boxgeometry.setBoxSideLengths(Math.max(0.5,s.getX()),Math.max(0.5,s.getY()),Math.max(0.5,s.getZ()));
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
					platformbox1body.setVelocity(0,0,0);
					platformbox1body.setAngularVelocity(0, 0, 0);
					game.getPhysics().fixBody(platformbox1body, state);
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
