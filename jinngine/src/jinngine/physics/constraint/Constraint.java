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
     * Insert the ConstraintEntries of this Constraint into the list modelled by iterator
     * 
     * @param dt
     */
    public void update(double dt);

    /**
     * Get the first body in this constraint definition
     * 
     * @return
     */
    public Body getBody1();

    /**
     * Get the second body in this constraint definition
     * 
     * @return
     */
    public Body getBody2();

    /**
     * Return true if this constraint is to be treated as an external force contribution
     */
    public boolean isExternal();

    public boolean isMonitored();

    public void setMonitored(boolean monitored);

}
