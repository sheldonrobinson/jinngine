package jinngine.collision;

import jinngine.geometry.*;
import jinngine.util.Pair;

/**
 * Broad-phase collision detection is the process of finding objects in a simulation,
 * that are in close proximity of each other. Often, implementations are using axis aligned bounding boxes (AABB's), or some
 * other simple geometric bounding volume, to simplify the problem. The main motivation of broad-phase collision detection, 
 * is to cheaply cull away object pairs, that is of no interest to any later collision detection and/or contact determination. 
 */
public interface BroadfaseCollisionDetection {
	/**
	 * A handler for broad-phase collision detection events. An implementation of the {@link BroadfaseCollisionDetection}
	 * interface, is required to allow the user to register his or her own handler. 
	 */
	public interface Handler {
		
		/**
		 * A geometry pair is now overlapping
		 * @param pair
		 */
		public void overlap(Pair<Geometry> pair);
		
		/**
		 * A geometry pair is now seperated
		 * @param pair
		 */
		public void separation(Pair<Geometry> pair);
	}
	
	/**
	 * Add a broad-fase handler
	 * @param h
	 */
	public void addHandler( Handler h);
	
	/**
	 * Remove a broad-fase handler
	 * @param h
	 */
	public void removeHandler( Handler h);
	
	/**
	 * Add a new body to be handled by this broad-phase collision detection implementation.
	 * @param a Body to be added
	 */
	public void add(Geometry a);

	/**
	 * Remove a body
	 * @param a Body to be removed
	 */
	public void remove(Geometry a);
	
	/**
	 * Run the broad-phase collision detection. This call will invoke calls to any registered event handler, se {@link Handler}.
	 * The method overlap() in the {@link Handler} interface, is called when ever a new overlap between two objects arises. separation() is 
	 * invoked when an overlap vanishes.
	 */
	public void run();
}
