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

/**
 * A utility used by {@link Scene} to classify inactive and active bodies. Note that both functions may return false for
 * the same body. This indicates that the current activation state for the body should not be changed, regardless of its
 * current state. In general, the end-user should only care about this interface if one wants to implement a custom
 * deactivation policy. Usually, the DefaultDeactivationPolicy will be adequate for most purposes.
 */
public interface DeactivationPolicy {

    /**
     * Returns true if the Body b should be put into an deactivated state
     * @param dt TODO
     */
    public boolean shouldBeDeactivated(Body b, double dt);

    /**
     * Returns true if Body b should be activated
     * @param dt TODO
     */
    public boolean shouldBeActivated(Body b, double dt);

    /**
     * Force the next call to shouldBeActivated() to return true for the given body
     * 
     * @param b
     */
    public void forceActivate(Body b);
}
