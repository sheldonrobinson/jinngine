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


/**
 * Material specifies physical properties for a geometric object, which are
 * restitution and friction coefficients. The restitution coefficient specifies
 * how much energy that dissipates during collisions. The value 0 means total
 * dissipation, and the value 1 means total preservation of energy. A good
 * standard value is around 0.7. The friction coefficient, often denoted mu, is
 * a linear coupling between the normal force magnitude at a contact point, and
 * the allowed magnitudes of the tangential forces. Obviously a value of 0
 * completely turns of friction.
 * 
 * @author mo
 * 
 */
public interface Material {
    /**
     * Restitution coefficient, 0 through 1
     */
    public double getRestitution();

    /**
     * Set the restitution coefficient
     */
    public void setRestitution(double e);

    /**
     * Friction coefficient, non-negative
     */
    public double getFrictionCoefficient();

    /**
     * Set the friction coefficient
     */
    public void setFrictionCoefficient(double f);

    /**
     * Get the correction velocity limit for this material
     */
    public double getCorrectionVelocityLimit();

    /**
     * Set the correction velocity limit for this material. This parameter can
     * be used to tweak the animation behaviour in case of penetrations. It
     * should not normally be necessary to adjust this value.
     */
    public void setCorrectionVelocityLimit(double limit);

}
