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
 * Triggers are inserted into {@link Scene} to monitor events in animation
 */
public interface Trigger {

    /**
     * Called by {@link Scene} when the triller is added to it. The trigger should setup event handlers etc during this
     * call.
     */
    public void setup(Scene s);

    /**
     * Update this trigger. Called by {@link Scene}, should not be called by the user directly.
     */
    public void update(Scene s);

    /**
     * Called by {@link Scene} just before the trigger is removed from the scene. The trigger should remove all
     * installed event handlers etc.
     */
    public void cleanup(Scene s);

}
