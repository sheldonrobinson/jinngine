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
    public void forceActivate(final Body b) {
        // ignore
    }

    @Override
    public boolean shouldBeActivated(final Body b, double dt) {
        // always activate
        return true;
    }

    @Override
    public boolean shouldBeDeactivated(final Body b, double dt) {
        // never deactivate
        return false;
    }

}
