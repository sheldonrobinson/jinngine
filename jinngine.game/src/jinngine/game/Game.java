package jinngine.game;

import java.util.*;

import jinngine.game.actors.BodyPlacer;
import jinngine.game.actors.Environment;
import jinngine.game.actors.bear.Bear;
import jinngine.game.actors.platform1.Platform1;
import jinngine.physics.Engine;
import jinngine.physics.PhysicsScene;

public class Game {
	
	//list of actors
	private final List<Actor> actors = new ArrayList<Actor>();

	//setup rendering stuff
	private final Rendering rendering = new Rendering();
	private final Engine jinngine = new Engine();

	public Game() {
		jinngine.setTimestep(0.04);
		
		
		//setup some actors
		Actor actor = new Environment();
		actor.start(this);
		actors.add(actor);
		
		Bear bear = new Bear();
		bear.start(this);
		actors.add(bear);
		
		Actor platformbox1 = new Platform1(new jinngine.math.Vector3(-3,-25+2,0));
		platformbox1.start(this);
		actors.add(platformbox1);

		Platform1 platformbox2 = new Platform1(new jinngine.math.Vector3(-2.7,-25+3.5,0));
		platformbox2.start(this);
		actors.add(platformbox2);

		Actor placer = new BodyPlacer( bear.bodyhead);
		placer.start(this);
		actors.add(placer);

		//run forever
		while(true) {
			
			jinngine.tick();
						
			//visit all actors
			for (Actor a: actors)
				a.act(this);			
			
			rendering.draw();
			Thread.yield();
		}
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
