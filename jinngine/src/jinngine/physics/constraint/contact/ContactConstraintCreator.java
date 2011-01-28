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

import jinngine.geometry.contact.ContactGenerator;
import jinngine.physics.Body;

/**
 * A contact constraint creator is used to supply new ContactConstraint instances when new pairs of bodies starts to
 * interact. By implementing this interface, one can customise which types or instances of contact constraints will be
 * used to generate the final NCP constraints
 */
public interface ContactConstraintCreator {

    /**
     * Create a new ContactConstraint instance between b1 and b2, using the initial ContactGenerator g
     * 
     * @param b1
     *            Frist body
     * @param b2
     *            Second body
     * @param g
     *            A new contact generator
     * @return A new contact constraint
     */
    public ContactConstraint createContactConstraint(Body b1, Body b2, ContactGenerator g);

    /**
     * Called when a contact constraint that was instansiated by this generator, is removed from the animation. This
     * makes it possible for an application to react to the event, by for instance removing this contact constraint from
     * its internal book-keeping
     * 
     * @param constraint
     */
    public void removeContactConstraint(ContactConstraint constraint);
}
