/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.physics.constraint.contact;

import jinngine.physics.Body;
import jinngine.physics.Scene;
import jinngine.util.Pair;

public interface ContactConstraintManager {

    public interface Handler {
        public void contactConstraintCreated(Pair<Body> bodies, ContactConstraint contact);

        public void contactConstraintRemoved(Pair<Body> bodies, ContactConstraint constac);
    }

    public void setup(Scene scene);

    public void cleanup(Scene scene);

    public void addHandler(Handler handler);

    public void removeHandler(Handler handler);
}
