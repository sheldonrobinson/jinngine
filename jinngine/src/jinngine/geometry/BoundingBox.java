/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.geometry;
import jinngine.math.Vector3;

/**
 * A shape that encapsulates, or bounds a given, usually more complex geometry. All geometries
 * used in jinngine are required to supply an axis aligned bounding box (AABB), because it will be used when 
 * performing broad-phase collision detection 
 */
public interface BoundingBox {
	/**
	 * Return a vector that contains all minimum bounds on each axis. The bounds are in world coordinates in world space
	 * @param bounds Vector3 that will contain the updated bound values
	 * @return reference to the bounds vector
	 */
	public Vector3 getMinBounds(Vector3 bounds);

	/**
	 * Return a vector that contains all maximum bounds on each axis. The bounds are in world coordinates in world space
	 * @param bounds Vector3 that will contain the updated bound values
	 * @return reference to the bounds vector
	 */
	public Vector3 getMaxBounds(Vector3 bounds);
}
