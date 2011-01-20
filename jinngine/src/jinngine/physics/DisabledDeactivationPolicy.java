/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.physics;

public class DisabledDeactivationPolicy implements DeactivationPolicy {

	@Override
	public void activate(Body b) {
		b.deactivated = false;
	}

	@Override
	public void deactivate(Body b) {
		b.deactivated = true;
	}

	@Override
	public void forceActivate(Body b) {
		// ignore
	}

	@Override
	public boolean shouldBeActivated(Body b) {
		// if b is not active, activate it
		if ( b.deactivated ) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean shouldBeDeactivated(Body b) {
		// never deactivate
		return false;
	}

}
