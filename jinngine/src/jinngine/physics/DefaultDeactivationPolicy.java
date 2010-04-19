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
 * Default implementation of a de-activation policy. The policy uses a simple threshold on the kinetic 
 * energy to reason about the activity of a body. 
 */
public class DefaultDeactivationPolicy implements DeactivationPolicy {
	@Override
	public boolean shouldBeDeactivated(Body b) {		
		double accel = b.deltavelocity.dot(b.deltavelocity) + b.deltaomega.dot(b.deltaomega);
		return b.totalKinetic() + accel < 1e-14 ;
	}
	
	@Override
	public boolean shouldBeActivated(Body b) {
		double accel = b.deltavelocity.dot(b.deltavelocity) + b.deltaomega.dot(b.deltaomega);
		return b.totalKinetic() + 0*accel > 1e-3;
	}

}
