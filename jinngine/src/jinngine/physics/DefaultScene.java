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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jinngine.collision.BroadphaseCollisionDetection;
import jinngine.collision.SAP2;
import jinngine.geometry.Geometry;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.constraint.contact.ContactConstraintManager;
import jinngine.physics.constraint.contact.DefaultContactConstraintManager;
import jinngine.physics.solver.NonsmoothNonlinearConjugateGradient;
import jinngine.physics.solver.SinglePGSIteration;
import jinngine.physics.solver.Solver;
import jinngine.physics.solver.Solver.NCPConstraint;
import jinngine.util.ComponentGraph;
import jinngine.util.ComponentGraphHashMapDataHandler;
import jinngine.util.HashMapComponentGraph;
import jinngine.util.Pair;

/**
 * A basic fixed time stepping rigid body simulator. It uses a contact graph to
 * organise constraints, and generates contact constraints upon
 * intersecting/touching geometries. The engine is limited to having one
 * constraint per. body pair. This means that if a joint constraint is present,
 * there can be no contact constraints simultaneously. Such behaviour should be
 * modelled using joint limits.
 */
public final class DefaultScene implements Scene {

    // triggers
    public final List<Trigger> triggers = new LinkedList<Trigger>();

    // create a contact graph classifier, used by the contact graph for determining
    // fixed bodies, i.e. bodies considered to have infinite mass.
    private final ComponentGraph.NodeClassifier<Body> classifier = new ComponentGraph.NodeClassifier<Body>() {
        @Override
        public boolean isDelimiter(final Body node) {
            return node.isFixed();
        }
    };

    // inner class for storing data in components in constraint graph
    public final class ConstraintGroup {

        // maintain constraints and bodies in groups
        public final List<Constraint> constraints = new ArrayList<Constraint>();
        public final List<Constraint> externalconstraints = new ArrayList<Constraint>();
        public final List<Constraint> monitoredconstraints = new ArrayList<Constraint>();

        // all bodies in group
        public final List<Body> bodies = new ArrayList<Body>();

        // we track monitored constraints in each component
        // public final Set<Body> monitoredbodies = new HashSet<Body>();

        // components can be deactivated
        public boolean deactivated = false;
    }

    // setup the data handler for the constraint graph. Here, some methods are overridden
    // to handle the way we like to order stuff in each constraint component
    final ComponentGraphHashMapDataHandler<Body, Constraint, ConstraintGroup> data = new ComponentGraphHashMapDataHandler<Body, Constraint, ConstraintGroup>(
            new ComponentGraphHashMapDataHandler.ComponentFactory<ConstraintGroup>() {
                // just a simple factory for new components
                @Override
                public ConstraintGroup createComponent() {
                    return new ConstraintGroup();
                }
            }) {

        @Override
        public void addNodeToComponent(final ConstraintGroup c, final Body node) {
            super.addNodeToComponent(c, node);

            // add body to the component
            c.bodies.add(node);
        }

        @Override
        public void removeNodeFromComponent(final ConstraintGroup c, final Body node) {
            super.removeNodeFromComponent(c, node);

            // remove body from the component
            c.bodies.remove(node);
        }

        @Override
        public void addEdgeToComponent(final ConstraintGroup c, final Constraint e) {
            super.addEdgeToComponent(c, e);

            // add constraint to component
            if (e.isExternal()) {
                c.externalconstraints.add(e);
            } else {
                c.constraints.add(e);
            }

            // handle monitored constraints
            if (e.isMonitored()) {
                c.monitoredconstraints.add(e);
            }

            // activate the component whenever
            // topological changes occur
            c.deactivated = false;
        }

        @Override
        public void removeEdgeFromComponent(final ConstraintGroup c, final Constraint e) {
            super.removeEdgeFromComponent(c, e);

            // remove constraint from component
            if (e.isExternal()) {
                c.externalconstraints.remove(e);
            } else {
                c.constraints.remove(e);
            }

            // handle monitored constraints
            if (e.isMonitored()) {
                c.monitoredconstraints.remove(e);
            }

            // activate the component whenever
            // topological changes occur
            c.deactivated = false;
        }
    };

    // create the contact graph using the classifier above
    private final ComponentGraph<Body, Constraint, ConstraintGroup> constraintGraph = new HashMapComponentGraph<Body, Constraint, ConstraintGroup>(
            classifier, data);

    // broadphase collision detection
    private final BroadphaseCollisionDetection broadphase;

    // contact constraints
    private final ContactConstraintManager contactmanager;

    // constraint solver
    private final Solver solver;

    // deactivation policy
    private final DeactivationPolicy policy;

    // time-step size
    private double timestep = 0.08;

    /**
     * Create a new fixed time-stepping simulator
     * 
     * @param broadphase
     *            Broadphase collision detection method
     * @param solver
     *            Solver to be used
     * @param policy
     *            the deactivation policy to be used
     */
    public DefaultScene(final BroadphaseCollisionDetection broadphase, final Solver solver,
            final DeactivationPolicy policy) {

        this.broadphase = broadphase;
        this.solver = solver;
        this.policy = policy;

        // start the new contact constraint manager
        contactmanager = new DefaultContactConstraintManager(broadphase, constraintGraph);
    }

    /**
     * Create a new fixed time-stepping simulator with general purpose settings.
     */
    public DefaultScene() {

        // some default choices
        policy = new DefaultDeactivationPolicy();
        // this.broadphase = new SweepAndPrune();
        // this.broadphase = new ExhaustiveSearch();
        broadphase = new SAP2();

        // this.solver = new ProjectedGaussSeidel(55);
        // this.solver = new NonsmoothNonlinearConjugateGradient(55);
        solver = new NonsmoothNonlinearConjugateGradient(45);

        // start the new contact constraint manager
        contactmanager = new DefaultContactConstraintManager(broadphase, constraintGraph);
    }

    // private final void clearExternal( ConstraintGroup g) {
    // // hack
    // List<Body> bodies = new ArrayList<Body>();
    //
    // // active constraints
    // List<Constraint> constraints = new ArrayList<Constraint>();
    //
    // {
    // Iterator<Body> bi = data.getNodesInComponent(g);
    // while(bi.hasNext()) bodies.add(bi.next());
    //
    // Iterator<Constraint> ci = data.getEdgesInComponent(g);
    // while(ci.hasNext()) {
    // Constraint c = ci.next();
    // // if (c.isPassive()) {
    // // external.add(c);
    // // } else {
    // constraints.add(c);
    // // }
    // }
    //
    // }
    //
    // // clear the delta velocities in the group
    // for (Body bi: bodies) {
    // bi.externaldeltaomega.assignZero();
    // bi.externaldeltavelocity.assignZero();
    // }
    // }

    /**
     * Perform a step on a constraint group, or a equivalently, a portion of the
     * constraint graph. This portion will consist of constraints (edges) and
     * bodies (nodes).
     */
    private final void step(final ConstraintGroup group) {
        // any constraints to be handled?
        if (group.constraints.size() > 0 || group.externalconstraints.size() > 0) {
            // update with hard warm-start
            // clear the delta velocities in the group
            for (final Body bi : group.bodies) {
                bi.deltavelocity.assignZero();
                bi.deltaomega.assignZero();
                bi.externaldeltavelocity.assignZero();
                bi.externaldeltaomega.assignZero();
            }

            // update external constraints
            for (final Constraint ci : group.externalconstraints) {
                ci.update(timestep);

                // clear contribution
                for (final NCPConstraint cj : ci) {
                    cj.lambda = 0;
                }

                // run single pgs on external constraint
                SinglePGSIteration.run(ci, ci.getBody1(), ci.getBody2());
            }

            // clear the delta velocities in the group
            for (final Body bi : group.bodies) {
                bi.externaldeltavelocity.assign(bi.deltavelocity);
                bi.externaldeltaomega.assign(bi.deltaomega);

                bi.deltavelocity.assignZero();
                bi.deltaomega.assignZero();
            }

            // update constraints
            for (final Constraint ci : group.constraints) {
                final Body body1 = ci.getBody1();
                final Body body2 = ci.getBody2();
                final double b1mask = body1.isFixed() ? 0 : 1;
                final double b2mask = body2.isFixed() ? 0 : 1;

                ci.update(timestep);

                // calculate current contribution
                for (final NCPConstraint cj : ci) {
                    // cj.lambda=0;
                    // get contribution from current lambda into the delta velocities
                    Vector3.multiplyAndAdd(body1.state.inverseanisotropicmass, cj.j1, cj.lambda * b1mask,
                            body1.deltavelocity);
                    Vector3.multiplyAndAdd(body1.state.inverseinertia, cj.j2, cj.lambda * b1mask, body1.deltaomega);
                    Vector3.multiplyAndAdd(body2.state.inverseanisotropicmass, cj.j3, cj.lambda * b2mask,
                            body2.deltavelocity);
                    Vector3.multiplyAndAdd(body2.state.inverseinertia, cj.j4, cj.lambda * b2mask, body2.deltaomega);
                }
            }

            // solve constraints
            solver.solve(group.constraints, group.bodies, 1e-15);
        } else {
            // clear the delta velocities in the group
            for (final Body bi : group.bodies) {
                bi.deltavelocity.assignZero();
                bi.deltaomega.assignZero();
            }

        }

        // advance positions
        for (final Body body : group.bodies) {
            // check for corrupted velocities
            if (body.deltavelocity.isNaN() || body.deltaomega.isNaN()) {
                throw new IllegalStateException("DefaultScene: delta velocities containes NaN");
            }
            if (body.externaldeltavelocity.isNaN() || body.externaldeltaomega.isNaN()) {
                throw new IllegalStateException("DefaultScene: external delta velocities containes NaN");
            }

            // apply delta velocities
            // body.state.velocity.assign(body.state.velocity.add(body.deltavelocity).add(body.externaldeltavelocity));
            body.state.velocity.assignAdd(body.deltavelocity);
            body.state.velocity.assignAdd(body.externaldeltavelocity);
            // body.state.omega.assign(body.state.omega.add(body.deltaomega).add(body.externaldeltaomega));
            body.state.omega.assignAdd(body.deltaomega);
            body.state.omega.assignAdd(body.externaldeltaomega);

            // integrate forward on positions
            body.advancePositions(timestep);
        }

        // check for possible deactivation
        boolean deactivate = true;
        // update bodies after position updates
        for (final Body bi : group.bodies) {
            if (!policy.shouldBeDeactivated(bi, timestep)) {
                deactivate = false;
            }

            // update transformations
            bi.update();
        }

        // deactivate this contact component
        if (deactivate) {
            group.deactivated = true;
        }
    } // step()

    @Override
    public final void tick() {
        // run the broad-phase collision detection (this automatically updates the contactGraph,
        // through the BroadfaseCollisionDetection.Handler type)
        broadphase.run();

        // iterate through groups/components in the constraint graph
        final Iterator<ConstraintGroup> components = data.getComponents();
        while (components.hasNext()) {
            // get the component
            final ConstraintGroup group = components.next();

            if (!group.deactivated) {
                // if active, take a simulation step
                step(group);

            } else {
                // if not active, then handle monitored constraints
                if (group.monitoredconstraints.size() > 0) {

                    // update the monitored constraints
                    for (final Constraint ci : group.monitoredconstraints) {
                        ci.update(timestep);

                        // get bodies
                        final Body body1 = ci.getBody1();
                        final Body body2 = ci.getBody2();

                        if (ci.isExternal()) {
                            // if external, swap the delta velocities
                            body1.deltavelocity1.assign(body1.deltavelocity);
                            body1.deltaomega1.assign(body1.deltaomega);
                            body1.deltavelocity.assign(body1.externaldeltavelocity);
                            body1.deltaomega.assign(body1.externaldeltaomega);
                            body1.externaldeltavelocity.assign(body1.deltavelocity1);
                            body1.externaldeltaomega.assign(body1.deltaomega1);

                            body2.deltavelocity1.assign(body2.deltavelocity);
                            body2.deltaomega1.assign(body2.deltaomega);
                            body2.deltavelocity.assign(body2.externaldeltavelocity);
                            body2.deltaomega.assign(body2.externaldeltaomega);
                            body2.externaldeltavelocity.assign(body2.deltavelocity1);
                            body2.externaldeltaomega.assign(body2.deltaomega1);
                        }

                        // perform a pgs step on the constraint, to check for any changes
                        // in force contributions
                        SinglePGSIteration.run(ci, body1, body2);

                        if (ci.isExternal()) {
                            // if external, swap the delta velocities
                            body1.deltavelocity1.assign(body1.deltavelocity);
                            body1.deltaomega1.assign(body1.deltaomega);
                            body1.deltavelocity.assign(body1.externaldeltavelocity);
                            body1.deltaomega.assign(body1.externaldeltaomega);
                            body1.externaldeltavelocity.assign(body1.deltavelocity1);
                            body1.externaldeltaomega.assign(body1.deltaomega1);

                            body2.deltavelocity1.assign(body2.deltavelocity);
                            body2.deltaomega1.assign(body2.deltaomega);
                            body2.deltavelocity.assign(body2.externaldeltavelocity);
                            body2.deltaomega.assign(body2.externaldeltaomega);
                            body2.externaldeltavelocity.assign(body2.deltavelocity1);
                            body2.externaldeltaomega.assign(body2.deltaomega1);
                        }

                    }
                    // a single activating body will activate the whole group
                    boolean activate = false;
                    // for (final Body bi: group.monitoredbodies) {
                    for (final Constraint ci : group.monitoredconstraints) {
                        if (policy.shouldBeActivated(ci.getBody1(), timestep)
                                || policy.shouldBeActivated(ci.getBody2(), timestep)) {
                            activate = true;
                            break;
                        }
                    }

                    // activate component?
                    if (activate) {
                        group.deactivated = false;
                    }
                } // if monitored constraints
            } // if deactivated
        } // for each group

        // update triggers
        final Iterator<Trigger> iter = triggers.iterator();
        while (iter.hasNext()) {
            final Trigger trigger = iter.next();

            // run trigger, and remove if requested 
            if (!trigger.update(this, timestep)) {
                iter.remove();
                trigger.cleanup(this);
            }
        }
    } // time-step

    //    @Override
    //    public void addForce(final Force f) {
    //        forces.add(f);
    //    }
    //
    //    @Override
    //    public void removeForce(final Force f) {
    //        forces.remove(f);
    //    }

    @Override
    public void addBody(final Body c) {
        c.updateTransformations();

        // install geometries into the broad-phase collision detection
        final Iterator<Geometry> i = c.getGeometries();
        while (i.hasNext()) {
            final Geometry g = i.next();
            broadphase.add(g);
        }

        // add node to graph
        constraintGraph.addNode(c);
    }

    @Override
    public void addConstraint(final Constraint joint) {
        constraintGraph.addEdge(new Pair<Body>(joint.getBody1(), joint.getBody2()), joint);
    }

    @Override
    public Iterator<Constraint> getConstraints() {
        final List<Constraint> list = new ArrayList<Constraint>();
        final Iterator<ConstraintGroup> ci = data.getComponents();
        while (ci.hasNext()) {
            final Iterator<Constraint> ei = data.getEdgesInComponent(ci.next());
            while (ei.hasNext()) {
                list.add(ei.next());
            }
        }

        return list.iterator();
    }

    @Override
    public final void removeConstraint(final Constraint c) {
        if (c != null) {
            constraintGraph.removeEdge(new Pair<Body>(c.getBody1(), c.getBody2()));
        } else {
            throw new IllegalArgumentException("DefaultScene: attempt to remove null constraint");
        }
    }

    @Override
    public final void removeBody(final Body body) {
        // remove associated geometries from collision detection
        final Iterator<Geometry> i = body.getGeometries();
        while (i.hasNext()) {
            broadphase.remove(i.next());
        }

        // remove node from graph
        constraintGraph.removeNode(body);
    }

    @Override
    public Iterator<Body> getBodies() {
        // gather all bodies up in a new list
        final List<Body> bodies = new ArrayList<Body>();

        // go thru all components and add all bodies in them to the list
        final Iterator<ConstraintGroup> i = data.getComponents();
        while (i.hasNext()) {
            final Iterator<Body> j = data.getNodesInComponent(i.next());
            while (j.hasNext()) {
                bodies.add(j.next());
            }
        }

        // pass back an iterator on this list
        return bodies.iterator();

    }

    @Override
    public void setTimestep(final double dt) {
        timestep = dt;
    }

    @Override
    public void fixBody(final Body b, final boolean fixed) {
        // check if the body is in the animation
        if (!data.containsNode(b)) {
            throw new IllegalArgumentException("Attempt to fix a body that is not in the scene");
        }
        // check if body is already the at the correct
        // fixed setting, in which case do nothing
        if (b.isFixed() == fixed) {
            return;
        }

        // remove the body from simulation
        removeBody(b);

        // change the fixed setting
        b.setFixed(fixed);

        // reinsert body
        addBody(b);

        // make sure transforms are in order, because the fixed
        // body will no longer be updated
        b.update();
    }

    @Override
    public void monitorConstraint(final Constraint constraint) {
        // check if the constraint is in the graph
        if (!data.containsEdge(constraint.getBody1(), constraint.getBody2())) {
            throw new IllegalArgumentException("Scene.monitorConstraint: given constraint is not in the scene");
        }

        // mark the constraint monitored
        if (!constraint.isMonitored()) {
            constraint.setMonitored(true);
        } else {
            throw new IllegalArgumentException("Constraint is already monitored");
        }

        // remove constraint
        removeConstraint(constraint);

        // add constraint
        addConstraint(constraint);
    }

    @Override
    public void unmonitorConstraint(final Constraint constraint) {
        // check if the constraint is in the graph
        if (!data.containsEdge(constraint.getBody1(), constraint.getBody2())) {
            throw new IllegalArgumentException("Scene: given constraint is not in the scene");
        }

        // mark the constraint monitored
        if (constraint.isMonitored()) {
            constraint.setMonitored(false);
        } else {
            throw new IllegalArgumentException("Constraint was not monitored");
        }

        // remove constraint
        removeConstraint(constraint);

        // add constraint
        addConstraint(constraint);
    }

    @Override
    public void addTrigger(final Trigger t) {
        t.setup(this);
        triggers.add(t);
    }

    @Override
    public void removeTrigger(final Trigger t) {
        triggers.remove(t);
        t.cleanup(this);
    }

    @Override
    public double getTimestep() {
        return timestep;
    }

    @Override
    public ContactConstraintManager getContactConstraintManager() {
        return contactmanager;
    }

    @Override
    public BroadphaseCollisionDetection getBroadphase() {
        return broadphase;
    }

    @Override
    public void addGeometry(final Matrix3 orientation, final Vector3 position, final Geometry g) {
        // create a new body
        final Body body = new Body(g.getName());
        body.addGeometry(orientation, position, g);

        // add the geometry to broadphase
        broadphase.add(g);

        // add the new body to the graph
        constraintGraph.addNode(body);
    }

    @Override
    public void addGeometry(final Body body, final Matrix3 orientation, final Vector3 position, final Geometry g) {
        // check if the body is in the animation
        if (!data.containsNode(body)) {
            throw new IllegalArgumentException("The given body does not exist in this scene");
        }

        // add geometry to the body
        body.addGeometry(orientation, position, g);

        // add the geometry to broadphase
        broadphase.add(g);
    }

    @Override
    public void removeGeometry(final Geometry g) {
        // if the geometry has no body assigned, it
        // means that it was never added to the scene
        final Body body = g.getBody();
        if (body == null) {
            throw new IllegalStateException("Attempt to remove a geometry that does not exist in the scene");
        }

        // remove from body
        body.removeGeometry(g);

        if (body.getNumberOfGeometries() < 1) {
            // body has no geometries remove it
            removeBody(body);
        }
    }

    @Override
    public Iterator<Constraint> getConnectedConstraints(final Body body) {
        final List<Constraint> returnList = new ArrayList<Constraint>();
        final Iterator<Body> nodes = data.getConnectedNodes(body);
        while (nodes.hasNext()) {
            returnList.add(data.getEdge(body, nodes.next()));
        }

        // return iterator with constraints
        return returnList.iterator();
    }

}
