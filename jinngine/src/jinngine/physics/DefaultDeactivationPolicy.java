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

import java.util.ArrayList;
import java.util.List;

import jinngine.math.Vector3;

/**
 * Default implementation of a de-activation policy. The policy uses a simple
 * threshold on the kinetic energy to reason about the activity of a body.
 */
public class DefaultDeactivationPolicy implements DeactivationPolicy {
    private final List<Body> forced = new ArrayList<Body>();

    @Override
    public boolean shouldBeDeactivated(final Body b, final double dt) {
        double accel = b.deltavelocity.add(b.externaldeltavelocity).squaredNorm();
        accel += b.deltaomega.add(b.externaldeltaomega).squaredNorm();
        // double accel = b.deltavelocity.dot(b.deltavelocity) + b.deltaomega.dot(b.deltaomega);
        // return b.totalKinetic()/b.state.mass + accel < 1e-3;
        return b.totalScaledKinetic() + accel < 1e-2 * dt;

        // return false;
    }

    @Override
    public boolean shouldBeActivated(final Body b, final double dt) {

        if (forced.size() > 0) {
            if (forced.contains(b)) {
                forced.remove(b);
                return true;
            }
        }

        // double accel = b.deltavelocity.add(b.externaldeltavelocity).squaredNorm();
        // accel += b.deltaomega.add(b.externaldeltaomega).squaredNorm();
        double accel = Vector3.squaredNormOfSum(b.deltavelocity, b.externaldeltavelocity);
        accel += Vector3.squaredNormOfSum(b.deltaomega, b.externaldeltaomega);
        return accel > 1e-0 * dt;
    }

    @Override
    public void forceActivate(final Body b) {
        forced.add(b);
    }

}
