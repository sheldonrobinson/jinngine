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
import java.util.Iterator;
import jinngine.collision.GJK;
import jinngine.collision.RayCast;
import jinngine.geometry.Geometry;
import jinngine.geometry.util.ORourke;
import jinngine.geometry.SupportMap3;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.util.GramSchmidt;
import jinngine.util.Pool;

public final class SupportMapContactGenerator implements ContactGenerator {
	// data
	private final SupportMap3 Sa, Sb;
	private final Geometry ga, gb;
	private final Vector3 pa = new Vector3();
	private final Vector3 pb = new Vector3();

	// contact normal and tangent vectors for the contact space
	private final Vector3 contactNormal = new Vector3();
	private final Vector3 tangent1 = new Vector3();
	private final Vector3 tangent2 = new Vector3();
    // basis on matrix form
	private final Matrix3 basis = new Matrix3();
	private final Vector3 reference = new Vector3();


	
	
//	private final List<ContactPoint> contacts = new ArrayList<ContactPoint>();
//	private final List<Vector3> faceA = new ArrayList<Vector3>();
	private final Pool<Vector3> faceA = new Pool<Vector3>(
			new ArrayList<Vector3>(), 
			new Pool.Factory<Vector3>() {
				public final Vector3 getNewInstance() { 
					return new Vector3();
				}}
	);
	
//	private final List<Vector3> faceB = new ArrayList<Vector3>();
	private final Pool<Vector3> faceB = new Pool<Vector3>(
			new ArrayList<Vector3>(), 
			new Pool.Factory<Vector3>() {
				public final Vector3 getNewInstance() { 
					return new Vector3();
				}}
	);

//	private final List<Vector3> intersection = new ArrayList<Vector3>();
	private final Pool<Vector3> intersection = new Pool<Vector3>(
			new ArrayList<Vector3>(), 
			new Pool.Factory<Vector3>() {
				public final Vector3 getNewInstance() { 
					return new Vector3();
				}}
	);

	
	private final Vector3 gadisp = new Vector3();
	private final Vector3 gbdisp = new Vector3();
	

	// settings
	private final double epsilon = 1e-7;
	private final double envelope;
	private final double shell;
	private final double spa;
	private final double spb;
	private boolean active = false;

	// distance algorithms
	private final GJK gjk = new GJK();
	private final RayCast raycast = new RayCast();
	private final ORourke orourke = new ORourke();

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
			shell = envelope*0.75;
		} else {
			envelope = ga.getEnvelope();
			shell = envelope*0.75;			
		}
	}
	
	@Override
	public void run( Result result ) {				
		// first we run GJK (the same as setting t=0)
		// we must know is the distance is less than the envelope 
		// plus sphere sweep radius for both geometries
		gjk.run(Sa, Sb, pa, pb, envelope+spa+spb, epsilon, 32);
		
		// if objects are intersecting
		if (gjk.getState().intersection) {
			// mark contact generator 
			active = true;
			
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
			Vector3 sp = Sa.supportPoint(direction.negate(), new Vector3()).sub(Sb.supportPoint(direction, new Vector3()));
			double lambda = direction.dot(sp)/direction.dot(direction)-envelope/direction.norm();
			raycast.run(Sa, Sb, new Vector3(), direction, pa, pb, lambda, envelope, epsilon, false);

			
			// calculate contact normal
			contactNormal.assignDifference(pa, pb);
			contactNormal.assignNormalize();

			// calculate the contact space
//			final Matrix3 M = GramSchmidt.run(normal);				
			GramSchmidt.run(contactNormal,tangent1,tangent2);

			// make sure the normal direction is in the z-component
//			final Matrix3 B = new Matrix3(M.column(1),M.column(2),M.column(0));
			basis.assign(tangent1,tangent2,contactNormal);

			
			
			// generate contact points
			generate(result);
			
//			if (pa.isNaN() || pb.isNaN() ) {
//				System.out.println();
//			}
			
		// if separation
		} else {
			// A and B was initially separated. We determine the distance and taking into account
			// that A and/or B can be sphere swept 
			final double d = Vector3.normOfDifference(pa, pb) - spa - spb; 

			// determine the activity state
			if (active) {
				// if active, we only become inactive when leaving the envelope
				if (d>envelope)
					active = false;				
			} else {
				// if inactive, we activate when inside the shell, or inner envelope
				if (d<shell*1.01)
					active = true;
			}
			
			
			// if active, generate contact points			
			if (active) {
				// calculate contact normal
				contactNormal.assignDifference(pa, pb);
				contactNormal.assignNormalize();
				
				// calculate the contact space
//				final Matrix3 M = GramSchmidt.run(normal);				
				GramSchmidt.run(contactNormal,tangent1,tangent2);

				// make sure the normal direction is in the z-component
//				final Matrix3 B = new Matrix3(M.column(1),M.column(2),M.column(0));
				basis.assign(tangent1,tangent2,contactNormal);
				
				

				// generate contact points
				generate(result);				
			} else {
//				contacts.clear();					
			}
		} 	
	}

	private final void generate(final Result result ) {
		// obtain contact faces from support features, negating the direction for A
		contactNormal.assignNegate();
		Sa.supportFeature(contactNormal, faceA.insert()); faceA.done();
		contactNormal.assignNegate();		
		Sb.supportFeature(contactNormal, faceB.insert()); faceB.done();

		// use the principal midpoint as reference point for intersections, r = 0.5a+0.5b
		reference.assignSum(pa,pb);
		reference.assignMultiply(.5);
				
		// reverse the points in face A, so they will be in counter-clock-wise order
		// when relating to the normal direction
		faceA.reverse();
		
				
		// create a result handler for the intersection algorithm. the handler 
		// simply copies the intersection points into a local list of points
		final ORourke.ResultHandler handler = new ORourke.ResultHandler() {
			public final void intersection(final Vector3 p, final Vector3 q) {
				intersection.next().assign(p);
				intersection.next().assign(q);
			}
		};	
				
		// apply transform to all points
		for (Vector3 p: faceA) {
            // p = B^T(p-reference)
			p.assignSub(reference);
			Matrix3.multiplyTransposed(basis, p, p);			
		}
		for (Vector3 p: faceB) {
            // p = B^T(p-reference)
			p.assignSub(reference);
			Matrix3.multiplyTransposed(basis, p, p);			
		}
		
		// start the intersection pool in allocation mode
		// the pool will reuse available vectors, and allocate
		// new ones if needed. 
		intersection.insert();
		
		// run 2d intersection
		orourke.run(faceA.getList(), faceB.getList(), handler);
		
		// clear all remaining result points
		intersection.done();
				
		// report intersection points
		Iterator<Vector3> intersections = intersection.iterator();
		while (intersections.hasNext()) {
			// obtain next pair of intersection points
			Vector3 p = intersections.next();
			Vector3 q = intersections.next();

			// while points are still in contact space, 
			// find their distance along the normal 
			double signedDistance = (p.z-q.z)-spb-spa;
			
			// if distance is within the envelope, pass on
			// the contact point through the result handler
			if ( signedDistance < envelope ) {
				// transform p to world
				Matrix3.multiply( basis, p, p);
				Vector3.add( p, reference );
				Vector3.multiplyAndAdd(contactNormal, -spa, p);

				// transform q to world
				Matrix3.multiply( basis, q, q);
				Vector3.add( q, reference );
				Vector3.multiplyAndAdd(contactNormal, spb, q);										

				// report contact, passing on the desired correction distance
				// along the normal for this contact point
				result.contactPoint(contactNormal, p, q, signedDistance-shell );
			}
		}
	}
	
	@Override
	public void remove() {/* nothing to clean up */}

	
	@Override
	public Geometry getFirstGeoemtry() {
		return ga;
	}

	@Override
	public Geometry getSecondGeometry() {
		return gb;
	}

	@Override
	public double getEnvelope() {
		return envelope;
	}

	@Override
	public void getContactNormal(Vector3 normal) {
		normal.assign(contactNormal);
	}

}
