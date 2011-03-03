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

import java.util.ArrayList;
import java.util.List;

import jinngine.collision.BroadphaseCollisionDetection;
import jinngine.geometry.Geometry;
import jinngine.geometry.SupportMap3;
import jinngine.geometry.contact.ContactGenerator;
import jinngine.geometry.contact.ContactGeneratorClassifier;
import jinngine.geometry.contact.SupportMapContactGenerator;
import jinngine.physics.Body;
import jinngine.physics.Scene;
import jinngine.physics.constraint.Constraint;
import jinngine.util.Pair;

/**
 * Maintains constraints in a graph structure
 */
public class DefaultContactConstraintManager implements ContactConstraintManager, BroadphaseCollisionDetection.Handler {

    // the external broad-phase collision detection
    private final BroadphaseCollisionDetection broadphase;

    private final Scene scene;

    // list of geometry classifiers
    private final List<ContactGeneratorClassifier> geometryClassifiers = new ArrayList<ContactGeneratorClassifier>();

    // list of contact constraint creators
    private final List<ContactConstraintCreator> contactConstraintCreators = new ArrayList<ContactConstraintCreator>();

    // the default contact constraint creator
    private final ContactConstraintCreator defaultcreator = new ContactConstraintCreator() {
        @Override
        public final ContactConstraint createContactConstraint(final Body b1, final Body b2, final ContactGenerator g) {
            return new FrictionalContactConstraint(b1, b2, g);
            // return new SimplifiedContactConstraint(b1,b2,g);
            // return new StabilisationContactConstraint(b1,b2,g);
            // return new BaumgardeContactConstraint(b1,b2,g);
        }

        @Override
        public void removeContactConstraint(final ContactConstraint constraint) {}
    };

    public DefaultContactConstraintManager(final Scene scene) {

        // store the broad phase and the constraint graph references
        this.broadphase = scene.getBroadphase();
        this.scene = scene;
        this.broadphase.addHandler(this);

        // General convex support maps
        this.geometryClassifiers.add(new ContactGeneratorClassifier() {
            @Override
            public final ContactGenerator getGenerator(final Geometry a, final Geometry b) {
                if (a instanceof SupportMap3 && b instanceof SupportMap3) {
                    return new SupportMapContactGenerator((SupportMap3) a, a, (SupportMap3) b, b);
                }
                // not recognised
                return null;
            }
        });

    }

    /**
     * Add a new ContactConstraintCreator
     */
    public final void addContactConstraintCreator(final ContactConstraintCreator c) {
        this.contactConstraintCreators.add(c);
    }

    /**
     * Remove an existing contact constraint creator
     * 
     * @param c
     */
    public final void removeContactConstraintCreator(final ContactConstraintCreator c) {
        this.contactConstraintCreators.remove(c);
    }

    private ContactGenerator getContactGenerator(final Pair<Geometry> pair) {
        for (final ContactGeneratorClassifier gc : this.geometryClassifiers) {
            final ContactGenerator g = gc.getGenerator(pair.getFirst(), pair.getSecond());

            if (g != null) {
                return g;
            }
        }
        return null;
    }

    @Override
    public void cleanup(final Scene scene) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setup(final Scene scene) {
        // TODO Auto-generated method stub

    }

    @Override
    public void overlap(final Pair<Geometry> inputpair) {
        // System.out.println("overlap");
        // retrieve the bodies associated with overlapping geometries
        final Body b1 = inputpair.getFirst().getBody();
        final Body b2 = inputpair.getSecond().getBody();

        // ignore overlaps stemming from the same body
        if (b1 == b2) {
            return;
        }
        // ignore overlaps for non-body geometries
        if (b1 == null || b2 == null) {
            return;
        }
        // ignore overlaps of fixed bodies
        if (b1.isFixed() && b2.isFixed()) {
            return;
        }

        // always order bodies and geometries the same way, so that normals
        // will be pointing the right direction
        Pair<Body> bodypair;
        Pair<Geometry> geometrypair;
        bodypair = new Pair<Body>(b1, b2);

        // some constraint is present in the constraint graph
        if (this.scene.containsConstraint(b1, b2)) {
            final Constraint c = this.scene.getConstraint(b1, b2);

            // in case of a contact constraint
            if (c instanceof ContactConstraint) {
                final ContactConstraint contactConstraint = (ContactConstraint) c;

                // order the geometry pair to match the order of the contact constraint.
                // this is necessary to keep normals pointing in the right direction when
                // contact constraints have more than one contact generator
                final Pair<Body> orderedpair = new Pair<Body>(contactConstraint.getBody1(),
                        contactConstraint.getBody2());
                if (orderedpair.getFirst() == bodypair.getFirst()) {
                    // same order
                    geometrypair = inputpair;
                } else {
                    // swap
                    geometrypair = new Pair<Geometry>(inputpair.getSecond(), inputpair.getFirst());
                }

                // add a new contact generator to this contact constraint
                final ContactGenerator generator = getContactGenerator(geometrypair);
                contactConstraint.addGenerator(generator);

            } else {
                // there is some constraint present, but it is not a contact constraint
                // we should do nothing
                return;
            }
        } else {
            // no constraint is present at all. A new contact constraint must be 
            // created, and a new contact generator inserted into it
            final ContactGenerator generator = getContactGenerator(inputpair);

            ContactConstraint contactConstraint = null;

            // try custom contact constraint generators
            for (final ContactConstraintCreator c : DefaultContactConstraintManager.this.contactConstraintCreators) {
                contactConstraint = c.createContactConstraint(bodypair.getFirst(), bodypair.getSecond(), generator);
                if (contactConstraint != null) {
                    break;
                }
            }

            // if no contact constraint was obtained, use the default creator
            if (contactConstraint == null) {
                contactConstraint = DefaultContactConstraintManager.this.defaultcreator.createContactConstraint(
                        bodypair.getFirst(), bodypair.getSecond(), generator);
            }

            // add constraint to the scene
            this.scene.addConstraint(contactConstraint);
        }
    }

    @Override
    public void separation(final Pair<Geometry> geometrypair) {
        // retrieve the bodies associated with overlapping geometries
        final Body b1 = geometrypair.getFirst().getBody();
        final Body b2 = geometrypair.getSecond().getBody();
        //        final Pair<Body> bodypair = new Pair<Body>(b1, b2);

        // ignore overlaps stemming from the same body
        if (b1 == b2) {
            return;
        }
        // ignore overlaps for non-body geometries
        if (b1 == null || b2 == null) {
            return;
        }
        // ignore overlaps of fixed bodies
        if (b1.isFixed() && b2.isFixed()) {
            return;
        }

        // if this geometry pair has an acting contact constraint,
        // we must remove the contact generator
        // some constraint is present in the constraint graph
        if (this.scene.containsConstraint(b1, b2)) {
            final Constraint c = this.scene.getConstraint(b1, b2);

            // in case of a contact constraint
            if (c instanceof ContactConstraint) {
                final ContactConstraint contactConstraint = (ContactConstraint) c;

                // remove from contact constraint
                contactConstraint.removeGenerator(geometrypair.getFirst(), geometrypair.getSecond());

                // if the contact constraint has no more generators,
                // remove the contact constraint from the scene
                if (contactConstraint.getNumberOfGenerators() < 1) {
                    this.scene.removeConstraint(contactConstraint);
                }

            } // if contact constraint is present 
        } // if any constraint is present

    }

}
