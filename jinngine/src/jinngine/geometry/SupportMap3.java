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

import java.util.*;

import jinngine.math.Vector3;

/**
 *  A support mapping for a convex object. Defined for a convex set A, as<p>
 *    
 *  S_A(v) = p, where v dot p = max { v dot x, x \in A } 
 *  
 *  In other words, the SupportMap is a function taking a vector v, and returns 
 *  the a point x on the boundary of the object, which has the greatest value v dot x. 
 *  Note that this point may not be a unique point on a given shape. The function 
 *  supportFeature gives the convex hull of all possible support points. 
 */
public interface SupportMap3  {
	/**
	 * Compute a support point of this geometry, in the given direction 
	 * @param result placeholder of the new support point
	 * @return the reference to result
	 */
	public Vector3 supportPoint( Vector3 direction, Vector3 result );

	/**
	 * Return the feature that supports the direction d. This could be either a point, 
	 * line segment, or a face. In case of a face, the points must appear in counter 
	 * clock-wise order with respect to the direction of d.
	 * 
	 * @return list of points that constitute either a point, line or a face
	 */
	public void supportFeature( Vector3 direction, Iterator<Vector3> face );
		
	/**
	 * Return the radius of the sweeping sphere for this support mapping. Sphere swept shapes
	 * are useful in modelling collision geometry, and they can be handled particularly efficient 
	 * by support mappings. 
	 */
	public double sphereSweepRadius();

}


