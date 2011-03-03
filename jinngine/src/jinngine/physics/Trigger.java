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

import jinngine.physics.constraint.Constraint;

/**
 * Triggers are inserted into {@link Scene} to monitor events in animation
 */
public interface Trigger {

    /**
     * Called by {@link Scene} when the trigger is added. The trigger should
     * setup event handlers etc during this call.
     */
    public void start(Body body);

    /**
     * Update this trigger. Called by {@link Scene}, should not be called by the
     * user directly.
     * 
     * @param dt
     *            the current time-step
     * @return false if this trigger wishes to remove it self. True otherwise.
     */
    public boolean update(Body body, double dt);

    /**
     * Called by {@link Scene} just before the trigger is removed from the
     * scene. The trigger should remove all installed event handlers etc.
     */
    public void stop(Body body);

    /**
     * Called when a body is added to the trigger
     * 
     * @param body
     */
    public void bodyAttached(Body body);

    /**
     * A body was removed from this trigger
     * 
     * @param body
     */
    public void bodyDetached(Body body);

    /**
     * Called when a constraint is attached to the trigger
     */
    public void constraintAttached(Body body, Constraint c);

    /**
     * Called when a constraint is detached from the trigger
     */
    public void constraintDetached(Body body, Constraint c);

}
