package jinngine.game;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.export.xml.XMLExporter;
import com.ardor3d.util.export.xml.XMLImporter;

import jinngine.game.actors.interaction.HUDActor;
import jinngine.game.actors.door.Door;
import jinngine.game.actors.environment.Environment;
import jinngine.game.actors.Actor;
import jinngine.game.actors.bear.Bear;
import jinngine.game.actors.platform1.Platform1;
import jinngine.physics.Engine;
import jinngine.physics.PhysicsScene;

public class Game {
	
	//list of actors
	private final List<Actor> startingactors = new ArrayList<Actor>();	
	private final List<Actor> runningactors = new ArrayList<Actor>();
	private final List<Actor> stoppingactors = new ArrayList<Actor>();	

	//setup rendering stuff
	private final Rendering rendering = new Rendering();
	private final Engine jinngine = new Engine();

	public Game() {
		jinngine.setTimestep(0.01);
		
		
		//setup some actors
//		Actor actor = new Environment();
//		actor.create(this);
//		actor.start(this);
//		runningactors.add(actor);
//		
//		Bear bear = new Bear();
//		bear.start(this);
//		runningactors.add(bear);
//		
//		Actor platformbox1 = new Platform1(new jinngine.math.Vector3(-3,-25+2,0), 0.7);
//		platformbox1.create(this);
//		platformbox1.start(this);
//		runningactors.add(platformbox1);
//
//		Platform1 platformbox2 = new Platform1(new jinngine.math.Vector3(-3,-25+3.5,0), 0.9);
//		platformbox2.create(this);
//		platformbox2.start(this);
//		runningactors.add(platformbox2);
//
//		Platform1 platformbox3 = new Platform1(new jinngine.math.Vector3(-3,-25+3.5,0), 0.7);
//		platformbox3.create(this);
//		platformbox3.start(this);
//		runningactors.add(platformbox3);
//
//		Actor p = new jinngine.game.actors.player.Player();
//		p.create(this);
//		addActor(p);
//
//		
//		Actor door = new Door();
//		door.create(this);
//		addActor(door);
//		
//		
//		addActor( new HUDActor() );

		

		
		//go through the scene graph depth first, and get the actors
		final ArrayList<Actor> foundactors = new ArrayList<Actor>();
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
//		 run traversal
		new traverse().find(getRendering().getRootNode());

		//add all actors
		for (Actor a: foundactors)  {
			addActor(a);
			System.out.println(""+a);
		}

		
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

			
			rendering.draw();
			Thread.yield();
		}
	}
	
	public final void addActor( Actor a) {
		startingactors.add(a);		
	}
	
	public final void removeActor( Actor a) {
		stoppingactors.add(a);
	}	
	
	public final Rendering getRendering() {
		return rendering;
	}
	
	public final Engine getPhysics() {
		return jinngine;
	}
	
	public static void main(String[] args) {
		Game game = new Game();			
	}
}
