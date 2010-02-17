/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */

package jinngine.collision;

import jinngine.math.Vector3;

/**
 * An algorithm capable of determening the closest points between two given geometry template types
 * @author mo
 *
 * @param <T> 
 * @param <U>
 */
public interface ClosestPointsAlgorithm<T,U> {
  
	/**
	 * Run the closest points algorithm. Upon return, the vectors pa and pb now contains the closest points
	 * of both geometry A and geometry B, respectively, in world coordinates.  
	 * @param geoa Geometry A
	 * @param geob Geometry B
	 * @param pa The returning point on A
	 * @param pb The returning point on B
	 * @param envelope A hinting value, saying that the algorithm should not bother computing points that
	 * are further from each other than the envelope value
	 * @param epsilon TODO
	 * @param maxiter TODO
	 */
  public void run( T geoa, U geob, Vector3 pa, Vector3 pb, double envelope, double epsilon, int maxiter );
  
}

