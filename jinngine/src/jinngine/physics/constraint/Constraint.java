/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.physics.constraint;

import jinngine.physics.Body;
import jinngine.physics.solver.Solver.NCPConstraint;

public interface Constraint extends Iterable<NCPConstraint> {

    /**
     * Update this constraint to correspond to the current state of the system.
     * After this call, all NCPConstraint defined in this constraint must be
     * updated and ready for the constraint solver.
     * 
     * @param dt
     *            current time-step size.
     */
    public void update(double dt);

    /**
     * Get the first body in this constraint definition.
     */
    public Body getBody1();

    /**
     * Get the second body in this constraint definition.
     */
    public Body getBody2();

    /**
     * Return true if this constraint is to be treated as an external force
     * contribution. Constraints marked as external, will be given a single
     * solver iteration, where after they a treated as fixed by the solver.
     */
    public boolean isExternal();

    /**
     * Return true if this constraint is monitored. Monitored constraints have
     * the ability to activate deactivated bodies/components in the simulation.
     * In general, constraints should only be marked as monitored, if their
     * force contributions are dependent on some external factor, such as joint
     * motors that are dependent on user interactions.
     */
    public boolean isMonitored();

    /**
     * Set the monitored setting for this constraint. This setting should not be
     * set directly by the user, see {@link Scene.monitorConstraint()}
     */
    public void setMonitored(boolean monitored);

}
