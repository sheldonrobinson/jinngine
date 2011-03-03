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
import java.util.ListIterator;

import jinngine.physics.constraint.Constraint;
import jinngine.physics.constraint.contact.ContactConstraint;
import jinngine.physics.solver.Solver.NCPConstraint;

/**
 * The contact trigger monitors a specific body for contact interactions. When
 * the total contact force of some contact interaction exceeds the user defined
 * value, an event is triggered. Likewise, when the total force drops below the
 * threshold, or the contact vanishes all together, another event is triggered.
 */
public class ContactTrigger implements Trigger {

    //    private final Body body;
    private final double impulsethreshold;
    private final Callback callback;

    // helper variables
    private double totalforce = 0;
    private int numberOfNcpConstraints = 0;

    // constraints that have not triggered an event
    private final List<ContactConstraint> monitoredconstraints = new ArrayList<ContactConstraint>();

    // constraints that have triggered an event
    private final List<ContactConstraint> triggeredconstraints = new ArrayList<ContactConstraint>();

    // interface for call-back from this trigger type
    public interface Callback {
        public void contactAboveThreshold(Body interactingBody, ContactConstraint constraint);

        public void contactBelowThreshold(Body interactingBody, ContactConstraint constraint);
    }

    /**
     * Create new contact trigger
     * 
     * @param body
     *            Body to monitor
     * @param impulsethreshold
     *            the total normal impulse exerted by the contact in last
     *            time-step
     */
    public ContactTrigger(final double impulsethreshold, final ContactTrigger.Callback callback) {
        //        this.body = body;
        this.impulsethreshold = impulsethreshold;
        this.callback = callback;
    }

    @Override
    public boolean update(final Body body, final double dt) {

        // see if the monitored constraints can trigger an event
        final ListIterator<ContactConstraint> monitored = this.monitoredconstraints.listIterator();
        while (monitored.hasNext()) {
            final ContactConstraint constraint = monitored.next();
            this.totalforce = 0;
            this.numberOfNcpConstraints = 0;

            // sum up the applied contact force
            for (final NCPConstraint ncp : constraint) {
                this.totalforce += ncp.lambda;
                this.numberOfNcpConstraints += 1;
            }

            // check condition
            if (this.totalforce > this.impulsethreshold) {

                // move constraint to triggered list
                monitored.remove();
                this.triggeredconstraints.add(constraint);

                // perform trigger event call-back
                // Pair<Body> bodies = constraint.getBodies();
                // Body interacting = bodies.getFirst()==body? bodies.getSecond(): bodies.getFirst();
                final Body interacting = constraint.getBody1() == body ? constraint.getBody2() : body;
                this.callback.contactAboveThreshold(interacting, constraint);

            } // if force > force threshold
        } // for monitored constraints

        // see if triggered constraints should be moved back to monitored
        final ListIterator<ContactConstraint> triggered = this.triggeredconstraints.listIterator();
        while (triggered.hasNext()) {

            final ContactConstraint constraint = triggered.next();
            this.totalforce = 0;
            this.numberOfNcpConstraints = 0;

            // sum up the applied contact force
            // Iterator<NCPConstraint> ncps = constraint.getNcpConstraints();
            // while(ncps.hasNext()) {
            // NCPConstraint ncp = ncps.next();
            for (final NCPConstraint ncp : constraint) {
                this.totalforce += ncp.lambda;
                this.numberOfNcpConstraints += 1;
            }

            // check condition
            if (this.totalforce < this.impulsethreshold) {

                // move constraint to monitored list
                triggered.remove();
                this.monitoredconstraints.add(constraint);

                // perform trigger event callback
                // Pair<Body> bodies = constraint.getBodies();
                // Body interacting = bodies.getFirst()==body? bodies.getSecond(): bodies.getFirst();
                final Body interacting = constraint.getBody1() == body ? constraint.getBody2() : body;
                this.callback.contactBelowThreshold(interacting, constraint);

            } // if force > force threshold
        } // for monitored constraints 

        // keep constraint
        return true;
    }

    @Override
    public void stop(final Body body) {
        // clear out the local lists
        this.monitoredconstraints.clear();
        this.triggeredconstraints.clear();
    }

    @Override
    public void start(final Body body) {

    }

    @Override
    public void constraintAttached(final Body body, final Constraint c) {
        // only react on contact constraints
        if (c instanceof ContactConstraint) {
            this.monitoredconstraints.add((ContactConstraint) c);
        }
    }

    @Override
    public void constraintDetached(final Body body, final Constraint c) {
        // only react on contact constraints
        final ContactConstraint contact;
        if (c instanceof ContactConstraint) {
            contact = (ContactConstraint) c;
        } else {
            return;
        }

        // if we had this constraint on our list of triggerd constraints, signal an event
        if (ContactTrigger.this.triggeredconstraints.contains(contact)) {

            // find the interacting body
            final Body interacting = contact.getBody1() == body ? contact.getBody2() : contact.getBody1();

            // perform the call back
            ContactTrigger.this.callback.contactBelowThreshold(interacting, contact);

            // remove from internal list
            ContactTrigger.this.triggeredconstraints.remove(contact);
        }

        // if the constraint is just on the monitor list, simply remove it
        if (ContactTrigger.this.monitoredconstraints.contains(contact)) {
            // remove from monitor list
            ContactTrigger.this.monitoredconstraints.remove(contact);
        }
    }

    @Override
    public void bodyAttached(final Body body) {
        // TODO Auto-generated method stub

    }

    @Override
    public void bodyDetached(final Body body) {
        // TODO Auto-generated method stub

    }
}
