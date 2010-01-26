package jinngine.game;

import java.util.*;

import jinngine.game.actors.interaction.HUDActor;
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
		jinngine.setTimestep(0.04);
		
		
		//setup some actors
		Actor actor = new Environment();
		actor.start(this);
		runningactors.add(actor);
		
		Bear bear = new Bear();
		bear.start(this);
		runningactors.add(bear);
		
		Actor platformbox1 = new Platform1(new jinngine.math.Vector3(-3,-25+2,0));
		platformbox1.start(this);
		runningactors.add(platformbox1);

		Platform1 platformbox2 = new Platform1(new jinngine.math.Vector3(-2.7,-25+3.5,0));
		platformbox2.start(this);
		runningactors.add(platformbox2);

//		Actor placer = new BodyPlacement( bear.bodyhead);
//		placer.start(this);
//		actors.add(placer);
//		
		addActor( new HUDActor() );

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
	
	public final PhysicsScene getPhysics() {
		return jinngine;
	}
	
	public static void main(String[] args) {
		Game game = new Game();			
	}
}
