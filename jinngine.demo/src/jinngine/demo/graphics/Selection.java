package jinngine.demo.graphics;

import jinngine.collision.BroadfaseCollisionDetection;
import jinngine.collision.GJK3;
import jinngine.collision.RayCast;
import jinngine.geometry.Geometry;
import jinngine.geometry.Line;
import jinngine.geometry.SupportMap3;
import jinngine.math.Vector3;
import jinngine.util.Pair;
import jinngine.util.Tuple;

public class Selection implements GameState {

	private boolean done = false;
	private boolean fired = false;

	@Override
	public boolean done() {
		return done;
	}

	@Override
	public void start(Graphics m) {}

	@Override
	public void stop(Graphics m) {}

	@Override
	public void tick(Graphics m) {
		//if mouse is pressed, do ray casting to find out if the user 
		//is pointing at an object
		if (m.pressed) {
			if (!fired ) {
				fired = true;

				//the best hit so far
				final Tuple<Entity,Double> hit = new Tuple<Entity,Double>(null,Double.POSITIVE_INFINITY);	
				final Render r = m.getRender();
				final Vector3 p1 = new Vector3(), d = new Vector3();
				r.getPointerRay(p1, d);

				//create a selection line
				final Line line = new Line(p1,p1.add(d.multiply(350)));

				//add the line as geometry
				m.getModel().addGeometry(line);

				//define a handler for the broad-fase
				BroadfaseCollisionDetection.Handler handler = new BroadfaseCollisionDetection.Handler() {				
					@Override
					public void overlap(Pair<Geometry> pair) {
						SupportMap3 a = (SupportMap3)pair.getFirst();
						SupportMap3 b = (SupportMap3)pair.getSecond();

						if ( a == line || b == line) {				
							GJK3 gjk = new GJK3();
							RayCast ray = new RayCast();

							//select non-line as shape
							SupportMap3 shape = a == line? b:a;

							//System.out.println(""+shape);
							double t = ray.run(shape, p1, d );

							//System.out.println("t="+t);

							if ( t!=Double.POSITIVE_INFINITY) {
								//select the geometry that isn't the line
								Geometry g =  a == line? pair.getSecond():pair.getFirst();

								//get the entity reference
								if (t < hit.second) {
									if (g.getAuxiliary() instanceof Entity) {
										Entity e = (Entity)g.getAuxiliary();
										hit.first = e;
										hit.second = t;
									}
								}
							}
						}
					}

					@Override
					public void separation(Pair<Geometry> pair) {
						// we are not interested in separated pairs
					}
				};


				//add the handler and run detection
				m.getModel().getBroadfase().addHandler(handler);
				m.getModel().getBroadfase().run();
				m.getModel().getBroadfase().removeHandler(handler);

				//remove the geometry
				m.getModel().removeGeometry(line);

				//spawn a ObjectPlacement state
				if (hit.first != null) {
					
					
					//pass the entity and the pickpoint
					m.addState(new ObjectPlacement(hit.first, p1.add(d.multiply(hit.second)) ));

					//signal done
					this.done = true;
				}

			}
		} else { // mouse not pressed
			fired = false;
		}



	}

}
