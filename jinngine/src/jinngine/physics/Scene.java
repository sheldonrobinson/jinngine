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

import java.util.Iterator;

import jinngine.collision.BroadphaseCollisionDetection;
import jinngine.geometry.Geometry;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.constraint.Constraint;
import jinngine.physics.constraint.contact.ContactConstraintManager;
import jinngine.physics.constraint.joint.HingeJoint;
import jinngine.physics.constraint.joint.JointAxisController;

/**
 * Main interface for using Jinngine. Use Scene to insert and remove triggers,
 * bodies, geometries, forces, and constraints.
 */
public interface Scene {

    /**
     * Get an iterator for all bodies in the Scene
     * 
     * @return An iterator over bodies
     */
    public Iterator<Body> getBodies();

    /**
     * Get all constraints in the scene. This includes ContactConstraint
     * instances
     * 
     * @return An iterator over constraints
     */
    public Iterator<Constraint> getConstraints();

    /**
     * Get all constraints that are connected to the given body
     */
    public Iterator<Constraint> getConnectedConstraints(Body body);

    /**
     * Add a geometry to the scene, specifying the initial world transform. A
     * new body will be created automatically.
     * 
     * @param orientation
     *            A rotation matrix describing the orientation of the geometry
     * @param position
     *            world position of the geometry
     */
    public void addGeometry(Matrix3 orientation, Vector3 position, Geometry g);

    /**
     * Add a geometry to the scene, specifying the initial world transform,
     * using the specified body. The body must already exist in the scene.
     * 
     * @param body
     *            that this geometry should be attached to
     * @param orientation
     *            A rotation matrix describing the orientation of the geometry
     * @param position
     *            world position of the geometry
     */
    public void addGeometry(Body body, Matrix3 orientation, Vector3 position, Geometry g);

    /**
     * Add a body to the scene
     * 
     * @param b
     */
    public void addBody(Body b);

    /**
     * Add a constraint to the scene
     * 
     * @param c
     */
    public void addConstraint(Constraint c);

    /**
     * Remove a geometry from the scene
     */
    public void removeGeometry(Geometry g);

    /**
     * Remove a body from the scene
     * 
     * @param b
     */
    public void removeBody(Body b);

    /**
     * Remove a constraint from the scene
     * 
     * @param c
     */
    public void removeConstraint(Constraint c);

    /**
     * Query the existence of a constraint acting between b1 and b2
     */
    public boolean containsConstraint(Body b1, Body b2);

    /**
     * Obtain the constraint acting between b1 and b2
     */
    public Constraint getConstraint(Body b1, Body b2);

    /**
     * Perform a time step on this model
     * 
     * @param
     */
    public void tick();

    /**
     * Set the time-step size for this scene
     */
    public void setTimestep(double dt);

    /**
     * Get the time-step size
     */
    public double getTimestep();

    /**
     * Make a body become fixed, during animation
     */
    public void fixBody(Body b, boolean fixed);

    /**
     * A monitored constraint is a {@link Constraint} that is controlled by the
     * user or some other control mechanism, partly or completely independent of
     * the velocities and forces in the {@link Scene}. This could for instance
     * be the {@link HingeJoint} where a {@link JointAxisController} instance is
     * used by some automated or user controlled mechanism. This makes the
     * constraint unpredictable for the deactivation system, and once the two
     * {@link Body} objects that the constraint is acting upon is deactivated,
     * there is no way for the constraint to start working and reactivate the
     * bodies. By adding the constraint to the monitored constraints list, the
     * constraint will be monitored and reactivated appropriately. Remember to
     * remove unused constraints with {@link removeLiveConstraint} so they do
     * not unnecessarily take up resources.
     * 
     * @param c
     */
    public void monitorConstraint(Constraint c);

    /**
     * Remove a {@link Constraint} from the live constraints monitoring list.
     * See {@link addLiveConstraint} for details on live constraints.
     * 
     * @param c
     *            {@link Constraint} to be removed
     */
    public void unmonitorConstraint(Constraint c);

    /**
     * Add an event trigger to this scene
     */
    public void addTrigger(Body body, Trigger t);

    /**
     * Remove an event trigger from this scene
     */
    public void removeTrigger(Body body);

    /**
     * Get the {@link ContactConstraintManager} that is governing contact
     * constraints in this scene.
     */
    public ContactConstraintManager getContactConstraintManager();

    /**
     * Get the broadphase collision detection system
     */
    public BroadphaseCollisionDetection getBroadphase();
}
