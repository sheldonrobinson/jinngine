/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.geometry.contact;

import java.util.Iterator;
import java.util.List;

import jinngine.collision.GJK;
import jinngine.collision.RayCast;
import jinngine.geometry.Geometry;
import jinngine.geometry.SupportMap3;
import jinngine.math.Vector3;

public class ExperimentalContactGenerator implements ContactGenerator {
	
	private final double epsilon = 1e-6;
	private final double envelope = 0.125;
	private final SupportMap3 Sa, Sb, sco;
	private final GJK gjk = new GJK();
	private final RayCast raycast = new RayCast();
	private final Vector3 pa = new Vector3();
	private final Vector3 pb = new Vector3();
	private final Vector3 v = new Vector3();
	private final Vector3 n = new Vector3();
	
	
	public ExperimentalContactGenerator(SupportMap3 sa, Geometry a, SupportMap3 sb, Geometry b) {
		this.Sa = sa;
		this.Sb = sb;
		this.sco = new SupportMap3() {
			@Override
			public Vector3 supportPoint(Vector3 direction) {
				return Sa.supportPoint(direction).minus(Sb.supportPoint(direction).multiply(-1));
			}
			@Override
			public void supportFeature(Vector3 d, double epsilon, List<Vector3> face) {}
		};
	}
	
	@Override
	public Iterator<ContactPoint> getContacts() {
		return null;
	}

	@Override
	public boolean run(double dt) {
		// first we run gjk 
		gjk.run(Sa, Sb, pa, pb, envelope, epsilon, 32);
		v.assign(pa.minus(pb));
		n.assign(v.normalize());
		
		// if distance is below precision
		if (v.squaredNorm() < epsilon*epsilon) {
			// we perform a raycast that is equivalent to
			// finding the growth distance between Sa and Sb. 
			// by that we obtain a contact normal at the 
			// intersection point. 
//			raycast.
		}
		
		return true;
	}

}
