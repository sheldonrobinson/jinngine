/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.geometry.contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jinngine.collision.GJK;
import jinngine.collision.RayCast;
import jinngine.geometry.Geometry;
import jinngine.geometry.Material;
import jinngine.geometry.UniformCapsule;
import jinngine.geometry.util.ORourke;
import jinngine.geometry.SupportMap3;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.util.GramSchmidt;

public final class SupportMapContactGenerator implements ContactGenerator {
	// data
	private final SupportMap3 Sa, Sb;
	private final Geometry ga, gb;
	private final Vector3 pa = new Vector3();
	private final Vector3 pb = new Vector3();
	private final List<ContactPoint> contacts = new ArrayList<ContactPoint>();
	private final List<Vector3> faceA = new ArrayList<Vector3>();
	private final List<Vector3> faceB = new ArrayList<Vector3>();
	private final Vector3 gadisp = new Vector3();
	private final Vector3 gbdisp = new Vector3();

	// settings
	private final double epsilon = 1e-7;
	private final double envelope;
	private final double shell;
	private double restitution;
	private double friction;
	private final double spa;
	private final double spb;

	// distance algorithms
	private final GJK gjk = new GJK();
	private final RayCast raycast = new RayCast();

	public SupportMapContactGenerator(SupportMap3 sa, Geometry ga, SupportMap3 sb, Geometry gb) {
		this.Sa = sa;
		this.Sb = sb;
		this.ga = ga;
		this.gb = gb;
		
		// sphere sweeping radius
		this.spa =  Sa.sphereSweepRadius();
		this.spb =  Sb.sphereSweepRadius();
		
		// select the largest envelope for contact generation
		if  ( gb.getEnvelope() > ga.getEnvelope() ) {
			envelope = gb.getEnvelope();
			shell = envelope*0.5;
		} else {
			envelope = ga.getEnvelope();
			shell = envelope*0.5;			
		}
	}
	
	@Override
	public Iterator<ContactPoint> getContacts() {
		return contacts.iterator();
	}

	@Override
	public void run() {		
		//select the smallest restitution and friction coefficients 
		if ( ga instanceof Material && gb instanceof Material) {
			double ea = ((Material)ga).getRestitution();
			double fa = ((Material)ga).getFrictionCoefficient();
			double eb = ((Material)gb).getRestitution();
			double fb = ((Material)gb).getFrictionCoefficient();
			//pick smallest values
			restitution = ea > eb ? eb : ea;
			friction    = fa > fb ? fb : fa;

		} else if ( ga instanceof Material ) {
			restitution = ((Material)ga).getRestitution();
			friction    = ((Material)ga).getFrictionCoefficient();
		} else if ( gb instanceof Material ) {
			restitution = ((Material)gb).getRestitution();
			friction    = ((Material)gb).getFrictionCoefficient();
		} else { // default values
			restitution = 0.7;
			friction = 0.5;
		}
		
		// first we run GJK (the same as setting t=0)
		// we must know is the distance is less than the envelope 
		// plus sphere sweep radius for both geometries
		gjk.run(Sa, Sb, pa, pb, envelope+spa+spb, epsilon, 32);
		
		// if objects are intersecting
		if (gjk.getState().intersection) {
			// we perform a ray-cast, that is equivalent to
			// finding the growth distance between Sa and Sb. 
			// by that we obtain a contact normal at the 
			// intersection point. 
			ga.getLocalTranslation(gadisp);	
			gb.getLocalTranslation(gbdisp);
			
			// add the local centre of mass displacement
			Vector3.add(gadisp, ga.getLocalCentreOfMass(new Vector3()));
			Vector3.add(gbdisp, gb.getLocalCentreOfMass(new Vector3()));
			
			// apply body rotation to centre of mass of objects
			Matrix3.multiply(ga.getBody().state.rotation, gadisp, gadisp);
			Matrix3.multiply(gb.getBody().state.rotation, gbdisp, gbdisp);
			Vector3 direction = ga.getBody().getPosition().add(gadisp).sub(gb.getBody().getPosition().add(gbdisp));
			
			// if direction is too small select a default one
			if (direction.norm() < epsilon)
				direction.assign(0,1,0);

			// compute the largest possible starting lambda, based on 
			// the support of A-B along the ray direction
			Vector3 sp = Sa.supportPoint(direction.negate()).sub(Sb.supportPoint(direction));
			double lambda = direction.dot(sp)/direction.dot(direction)-envelope/direction.norm();
			raycast.run(Sa, Sb, new Vector3(), direction, pa, pb, lambda, envelope, epsilon, false);
			
			// generate contact points
			generate(pa, pb, pa.sub(pb).normalize() );
			
			if (pa.isNaN() || pb.isNaN() ) {
				System.out.println();
			}
			
		// if separation
		} else {
			// A and B was initially separated. We determine the distance and taking into account
			// that A and/or B can be sphere swept 
			final double d = pa.sub(pb).norm() - spa - spb;

			// if distance is less that the envelope, generate contact points
			if (d<envelope) {
				generate(pa, pb, pa.sub(pb).normalize() );
			// or outside envelope
			} else {
				contacts.clear();	
			}
		} 	
	}

	private final void generate(final Vector3 a, final Vector3 b, final Vector3 v ) {
		contacts.clear(); faceA.clear(); faceB.clear();
		Sa.supportFeature(v.negate(), faceA);
		Sb.supportFeature(v, faceB);
		
		// reverse the points in face A, so they will be in counter-clock-wise order
		// when relating to the normal direction
		Collections.reverse(faceA);
		
		final Vector3 direction = v.normalize();
//		final Vector3 midpoint = a.add(b).multiply(0.5);
		final Vector3 midpoint = a.add(b).add(v.multiply(spb-spa)).multiply(0.5); // account for sphere sweeping
		
		// contact space basis 
		final Matrix3 M = GramSchmidt.run(direction);

		// make sure the normal direction is in the z-component
		final Matrix3 B = new Matrix3(M.column(1),M.column(2),M.column(0));

		// since B is orthogonal its inverse equals its transpose
		final Matrix3 Binv = B.transpose();
		
		// create a result handler for the intersection algorithm
		final ORourke.ResultHandler handler = new ORourke.ResultHandler() {
			public final void intersection(final Vector3 p, final Vector3 q) {				
				final ContactPoint cp = new ContactPoint();

				cp.b1 = ga.getBody();
				cp.b2 = gb.getBody();
				
				cp.normal.assign(direction);
				
				// optimised to avoid allocation
				// cp.point.assign( B.multiply(new Vector3(p.x,p.y,0)).add(midpoint) );				
				cp.point.assign(p.x,p.y,0);
				Matrix3.multiply( B, cp.point, cp.point);
				Vector3.add( cp.point, midpoint );
				
				// distance along the z axis in contact space
				cp.distance = (p.z-q.z)-spb-spa;  // take into account sphere sweeping

				// if contact is within the envelope size
				if (cp.distance < envelope ) {
					cp.depth = shell-cp.distance;
					cp.envelope = envelope;
					cp.restitution = restitution;
					cp.friction = friction;
					contacts.add(cp);
				}
			}
		};	
		
		// apply transform to all points
		for (Vector3 p: faceA) {
			p.assign( Binv.multiply(p.sub(midpoint)));
		}
		for (Vector3 p: faceB) {
			p.assign( Binv.multiply(p.sub(midpoint)));
		}
		
		// run 2d intersection
		ORourke.run(faceA, faceB, handler);

	}
	
	@Override
	public void remove() {/* nothing to clean up */}

	@Override
	public int getNumberOfContacts() {
		return contacts.size();
	}
}
