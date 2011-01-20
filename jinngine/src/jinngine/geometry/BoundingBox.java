/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
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
