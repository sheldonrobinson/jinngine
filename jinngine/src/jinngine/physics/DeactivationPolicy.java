/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.physics;

/**
 * A utility used by the simulator to classify inactive and active bodies. Note that both
 * functions may return false for the same body. This indicates that the current activation 
 * state for the body should not be changed, regardless of its current state.
 */
public interface DeactivationPolicy {
	
	/**
	 * Returns true if the Body b should be put into an deactivated state
	 */
	public boolean shouldBeDeactivated( Body b );
	
	/**
	 * Returns true if Body b should be activated
	 */
	public boolean shouldBeActivated( Body b );
	

}
