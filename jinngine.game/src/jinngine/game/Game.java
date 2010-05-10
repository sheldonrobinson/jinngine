package jinngine.game;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.newdawn.slick.openal.SoundStore;

import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.export.binary.BinaryExporter;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.export.xml.XMLExporter;
import com.ardor3d.util.export.xml.XMLImporter;

import jinngine.game.actors.button.AxisAllignBodyButton;
import jinngine.game.actors.button.Button;
import jinngine.game.actors.button.DeleteActorButton;
import jinngine.game.actors.button.FixBodyButton;
import jinngine.game.actors.button.PlacementButton;
import jinngine.game.actors.button.StoreSceneButton;
import jinngine.game.actors.door.Door;
import jinngine.game.actors.door.SimpleDoor;
import jinngine.game.actors.environment.Environment;
import jinngine.game.actors.interaction.HUDActor;
import jinngine.game.actors.Actor;
import jinngine.game.actors.platform1.BoxPlatform;
import jinngine.game.actors.platform1.ConvexPlatform;
import jinngine.game.actors.player.Player;
import jinngine.physics.DefaultScene;

public class Game {
	
	//list of actors
	private final List<Actor> startingactors = new ArrayList<Actor>();	
	private final List<Actor> runningactors = new ArrayList<Actor>();
	private final List<Actor> stoppingactors = new ArrayList<Actor>();	
	
	private final List<PostponedAction> actions = new ArrayList<PostponedAction>();

	//setup rendering stuff
	private final Rendering rendering = new Rendering();
	private final DefaultScene jinngine = new DefaultScene();
	
	private final HUDActor hudactor;
	


	public Game() {
		jinngine.setTimestep(0.065);

		// always create the HUD actor
		this.hudactor = new HUDActor();
		addActor(this.hudactor);

//		setupDemoLevel();
		loadLevel("storedlevel.xml");
		
		//run forever
		while(true) { 				
			jinngine.tick();
	
			// start actors
			for (Actor a: startingactors) {
				a.start(this);
				runningactors.add(a);
			} startingactors.clear();

			// stop actors
			for (Actor a: stoppingactors) {
				a.stop(this);
				runningactors.remove(a);
			} stoppingactors.clear();
			
			// visit all running actors
			for (Actor a: runningactors)
				a.act(this);	
			
			//perform postponed actions
			ListIterator<PostponedAction> iter = actions.listIterator();
			while(iter.hasNext()) {
				iter.next().perform();
				iter.remove();
			}
		
			rendering.draw();
			Thread.yield();
		}
	}
	
	public final void restartHudActor() {
		this.hudactor.stop(this);
		this.hudactor.start(this);
	}
	
	public final void setupDemoLevel() {
		//new scene
		Node scene = new Node();
		
		scene.setName("Game:CurrentLevel");

		
		// set the scene node
		getRendering().setScene(scene);

		// attach the scene to the root node
		getRendering().getRootNode().attachChild(scene);
		
		
		//setup some actors
		Actor actor = new Environment();
		actor.create(this);
//		actor.start(this);
//		runningactors.add(actor);

		
//		Actor platformbox1 = new Platform1(new jinngine.math.Vector3(-3,-25+2,0), 0.7);
//		platformbox1.create(this);

//		BoxPlatform platformbox2 = new BoxPlatform(new jinngine.math.Vector3(-3,-25+3.5,0), 0.9);
//		platformbox2.create(this);
//		//
//		BoxPlatform platformbox3 = new BoxPlatform(new jinngine.math.Vector3(-3,-25+3.5,0), 0.7);
//		platformbox3.create(this);
//
//		
//		
//		ConvexPlatform platform3 = new ConvexPlatform(new jinngine.math.Vector3(0,-25+3.5,0), 0.7);
//		platform3.create(this);
//		
//		ConvexPlatform platform4 = new ConvexPlatform(new jinngine.math.Vector3(0,-25+3.5,0), 0.7);
//		platform4.create(this);
//
//		ConvexPlatform platform5 = new ConvexPlatform(new jinngine.math.Vector3(0,-25+3.5,0), 0.7);
//		platform5.create(this);
//
//		ConvexPlatform platform6 = new ConvexPlatform(new jinngine.math.Vector3(0,-25+3.5,0), 0.7);
//		platform6.create(this);
//
		ConvexPlatform platform7 = new ConvexPlatform(new jinngine.math.Vector3(0,-25+3.5,0), 0.7);
		platform7.create(this);
		
		Player p = new jinngine.game.actors.player.Player();
		p.create(this);
		
		Actor simpledoor = new SimpleDoor();
		simpledoor.create(this);

		FixBodyButton fixbutton = new FixBodyButton();
		fixbutton.create(this);
		
		PlacementButton button = new PlacementButton();
		button.create(this);
		
		StoreSceneButton button2 = new StoreSceneButton();
		button2.create(this);

		Button axisbutton = new AxisAllignBodyButton();
		axisbutton.create(this);

		Button deletebutton = new DeleteActorButton();
		deletebutton.create(this);


		// add actors in scene
		addActorsInScene(scene);		
	}
	
	public final void storeCurrentLevel(String name) {
		Node scene = (Node)getRendering().getRootNode().getChild("Game:CurrentLevel");

		if ( scene == null) {
			System.err.println("No level loaded!");
			return;
		}
		
		try {
			BinaryExporter.getInstance().save(scene, new File(name));
//			XMLExporter.getInstance().save(scene, new File(name));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}	
	}
	
	public final void loadLevel(String name) {
		Node scene = null;

		if (getRendering().getRootNode().getChild("Game:CurrentLevel") != null) {
			System.err.println("A level already loaded!");
			return;
		}
		
		try {
			
//			scene = (Node)XMLImporter.getInstance().load( new File(name));
			scene = (Node)BinaryImporter.getInstance().load( new File(name));

			scene.setName("Game:CurrentLevel");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}	

		// add actors in scene
		addActorsInScene(scene);

		// attach the scene to the root node
		getRendering().getRootNode().attachChild(scene);
		
		// set the scene node
		getRendering().setScene(scene);
	}
	
	
	public final void unloadCurrentLevel() {
		Node scene = (Node)getRendering().getRootNode().getChild("Game:CurrentLevel");
		
		if (scene == null) {
			System.err.println("unloadCurrentLevel: No level loaded?");
			return;
		}
		
		// remove actors
		removeAllActorsInScene(scene);
				
		// remove scene
		getRendering().getRootNode().detachChild(scene);
		
		// remove scene node
		getRendering().setScene(null);
		
		// restart hud actor
		restartHudActor();

	}
	

	public final void removeAllActorsInScene( Node scene ) {
		// get actors in scene
		List<Actor> foundactors = getActorsInScene(scene);

		//remove all actors in list
		for (Actor a: foundactors)  {
			removeActor(a);
			System.out.println("Removing: "+a);
		}

	}	
	
	public List<Actor> getActorsInScene( Node scene) {
		//go through the scene graph depth first, and get the actors
		final ArrayList<Actor> foundactors = new ArrayList<Actor>();
		
		// recursive traversal class
		final class traverse {
			public void find(Node node) {
				if (node instanceof Actor)
					foundactors.add((Actor)node);
				for (Spatial sub : node.getChildren()) {
					if (sub instanceof Node) {
						find((Node)sub);
					}
				}
			}
		}
		
		 // run traversal
		new traverse().find(scene);
		
		// return the list
		return foundactors;
	}
	
	public final void addActorsInScene(Node scene) {
		// get actors in scene
		List<Actor> foundactors = getActorsInScene(scene);

		//add all actors
		for (Actor a: foundactors)  {
			addActor(a);
			System.out.println("Adding: "+a);
		}
	}
	
	public final void addActor( Actor a) {
		startingactors.add(a);		
	}
	
	public final void addPostponedAction( PostponedAction action ) {
		actions.add(action);
	}
	
	public final void removeActor( Actor a) {
		stoppingactors.add(a);
	}	
	
	public final Rendering getRendering() {
		return rendering;
	}
	
	public final DefaultScene getPhysics() {
		return jinngine;
	}
	
	public static void main(String[] args) {
		Game game = new Game();			
	}
}
