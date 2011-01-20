/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */
package jinngine.physics.force;

/**
 * Interface for modelling an external force. External forces are not
 * controlled by the solver, but are fixed in relation to for instance 
 * contact forces
 */
public interface Force {
	public void apply(double dt);
}
