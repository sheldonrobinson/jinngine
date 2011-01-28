/**
 * Copyright (c) 2008-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.collision;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jinngine.geometry.BoundingBox;
import jinngine.geometry.Geometry;
import jinngine.math.Vector3;
import jinngine.util.Pair;

/**
 * A naive broad-phase collision detection algorithm. ExhaustiveSearch obviously just performs an overlap test on each
 * possible pair in the considered configuration. Despite its O(n^2) complexity, the method sometimes out-performs more
 * advanced implementations on configurations with few objects. In addition, all pairs test is often useful for test
 * purposes.
 */
public class ExhaustiveSearch implements BroadphaseCollisionDetection {

    private final Set<Pair<Geometry>> existingPairs = new LinkedHashSet<Pair<Geometry>>();
    private final Set<Pair<Geometry>> leavingPairs = new LinkedHashSet<Pair<Geometry>>();
    private final List<Geometry> geometries = new ArrayList<Geometry>();
    private final List<BroadphaseCollisionDetection.Handler> handlers = new ArrayList<Handler>();

    public ExhaustiveSearch() {}

    public ExhaustiveSearch(final BroadphaseCollisionDetection.Handler handler) {
        handlers.add(handler);
    }

    @Override
    public void run() {
        leavingPairs.addAll(existingPairs);

        // O(N^2) broad-phase collision detection
        final int size = geometries.size();
        for (int i = 0; i < size; i++) {
            final Geometry c1 = geometries.get(i);
            for (int j = i + 1; j < size; j++) {
                final Geometry c2 = geometries.get(j);
                if (c1 != c2) {
                    if (overlap(c1, c2)) {
                        final Pair<Geometry> pair = new Pair<Geometry>(c1, c2);

                        // if we discover a new pair, report it and add to table
                        if (!existingPairs.contains(pair)) {
                            existingPairs.add(pair);

                            // notify handlers
                            for (final Handler handler : handlers) {
                                handler.overlap(pair);
                            }
                        }

                        // any pair we observe is not leaving
                        leavingPairs.remove(pair);
                    }
                }
            }
        }

        // handle disappearing pairs
        final Iterator<Pair<Geometry>> leaving = leavingPairs.iterator();
        while (leaving.hasNext()) {
            final Pair<Geometry> pair = leaving.next();

            for (final Handler handler : handlers) {
                handler.separation(pair);
            }

            existingPairs.remove(pair);
        }

        leavingPairs.clear();
    }

    private static final boolean overlap(final BoundingBox i, final BoundingBox j) {

        final Vector3 bi = i.getMinBounds(new Vector3());
        final Vector3 ei = i.getMaxBounds(new Vector3());
        final Vector3 bj = j.getMinBounds(new Vector3());
        final Vector3 ej = j.getMaxBounds(new Vector3());

        final double bix = bi.x;
        final double biy = bi.y;
        final double biz = bi.z;
        final double eix = ei.x;
        final double eiy = ei.y;
        final double eiz = ei.z;
        final double bjx = bj.x;
        final double bjy = bj.y;
        final double bjz = bj.z;
        final double ejx = ej.x;
        final double ejy = ej.y;
        final double ejz = ej.z;

        // TODO test this
        if ((bjx < bix && bix <= ejx || bix <= bjx && bjx < eix)
                && (bjy < biy && biy <= ejy || biy <= bjy && bjy < eiy)
                && (bjz < biz && biz <= ejz || biz <= bjz && bjz < eiz)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void add(final Geometry a) {
        geometries.add(a);
    }

    @Override
    public void remove(final Geometry a) {
        geometries.remove(a);
    }

    @Override
    public void addHandler(final Handler h) {
        handlers.add(h);
    }

    @Override
    public void removeHandler(final Handler h) {
        handlers.remove(h);
    }

    @Override
    public Set<Pair<Geometry>> getOverlappingPairs() {
        return new LinkedHashSet<Pair<Geometry>>(existingPairs);
    }

}
