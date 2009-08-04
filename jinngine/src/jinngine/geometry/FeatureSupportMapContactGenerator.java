package jinngine.geometry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import jinngine.collision.ExpandingPolytope;
import jinngine.collision.GJK3;
import jinngine.math.*;
import jinngine.physics.*;


public class FeatureSupportMapContactGenerator implements ContactGenerator {

	//final double envelopeMin = 2.75;
    double envelope = 0.10;
    double shell = envelope*0.8;
	private final SupportMap3 Sa;
	private final SupportMap3 Sb;
	private final GJK3 closest = new GJK3();
	public final Body bodyA, bodyB;
	private final List<ContactPoint> contacts = new LinkedList<ContactPoint>();
	private final List<Vector3> face1 = new ArrayList<Vector3>();
	private final List<Vector3> face2 = new ArrayList<Vector3>();
	
	private final Vector3 principalNormal = new Vector3();
	private final Vector3 principalPoint = new Vector3();


	public FeatureSupportMapContactGenerator(SupportMap3 sa, Geometry a, SupportMap3 sb, Geometry b) {
		super();
		Sa = sa;
		Sb = sb;
		bodyA = a.getBody();
		bodyB = b.getBody();
	}

	@Override
	public Iterator<ContactPoint> getContacts() {
		return contacts.iterator();
	}

	@Override
	public boolean run(double dt) {
		//get envelopes
		//envelope = Sa.getEnvelope(dt)> Sb.getEnvelope(dt)? Sa.getEnvelope(dt) : Sb.getEnvelope(dt);
		//shell = envelope*0.25;
		
		
		//run the closest points algorithm
		Vector3 a = new Vector3(); Vector3 b = new Vector3();
		closest.run(Sa, Sb, a, b, envelope); 
		Vector3 v = a.minus(b);
		principalNormal.assign(v.normalize());
		double  d = v.norm();

		//penetration
		if (closest.getState().simplexSize > 3 ) {
			//System.out.println("penetrating");
			//run EPA
			ExpandingPolytope epa = new ExpandingPolytope();
			epa.run(Sa, Sb, a, b, closest.getState());
			v = a.minus(b);
			principalNormal.assign(v.normalize().multiply(-1));
			d = v.norm();
			//v.print();
			
			//principalNormal.print();
			generate(a,b,principalNormal,(d+shell), true);
			
			
			return true;
		}
		
		//distance is within envelope
		else if ( d > 1e-6  && d < envelope ) {
			//principalPenetration = false;
			
			//double depth = envelope-d;
			//depth = depth-shell > 0 ? depth-shell:0;
			double depth = -d+shell;
			//depth=0;

			depth = depth < 0 ? 0 :depth;
			
			
			//System.out.println("depth" + depth);
			//generate contact points
			generate(a, b, principalNormal, depth, false);
			
			return true;

		
			
			
		} else {
			//no contact
			contacts.clear();
			return false;	
		}		
	} 

	private void generate(Vector3 a, Vector3 b, Vector3 v, double depth, boolean penetrating) {
		contacts.clear(); face1.clear(); face2.clear();
		Sa.supportFeature(v.multiply(-1), face1);
		Sb.supportFeature(v.multiply(1), face2);


		Vector3 direction = v.normalize();
		Vector3 midpoint = a.add(b).multiply(0.5);

		//create basis
		//Use a gram-schmidt process to create a orthonormal basis for the impact space
		Vector3 v1 = direction.copy(); Vector3 v2 = Vector3.i; Vector3 v3 = Vector3.k;    
		Vector3 t1 = v1.normalize(); 
		Vector3 t2 = v2.minus( t1.multiply(t1.dot(v2)) );

		//in case v1 and v2 are parallel
		if ( t2.abs().lessThan( Vector3.epsilon ) ) {
			v2 = Vector3.j; v3 = Vector3.k;
			t2 = v2.minus( t1.multiply(t1.dot(v2)) ).normalize();    
		} else {
			t2 = t2.normalize();
		}
		//v1 paralell with v3
		if( v1.cross(v3).abs().lessThan( Vector3.epsilon ) ) {
			v3 = Vector3.j;
		}
		//finaly calculate t3
		Vector3 t3 = v3.minus( t1.multiply(t1.dot(v3)).minus( t2.multiply(t2.dot(v3)) )).normalize();

		
		Matrix3 S = new Matrix3(t1,t2,t3);
		Matrix3 Si = S.transpose();

		
		
		//System.out.println("face1" + face1 + "Sa " + Sa);
		
		if ( face2.size()>2) {
			double firstsign = 0;
			for (Vector3 p1: face1)  {
				//transform and project
				Vector3 p1tp = Si.multiply(p1.minus(midpoint));
				double deviation = p1tp.a1;
				p1tp.a1 = 0;

				boolean inside = true;
				Vector3 pp = face2.get(face2.size()-1).copy();
				for (Vector3 p2: face2) {

					//transform and project
					Vector3 pptp = Si.multiply(pp.minus(midpoint));
					pptp.a1 = 0;
					Vector3 p2tp = Si.multiply(p2.minus(midpoint));
					p2tp.a1 = 0;

					Vector3 cr = p1tp.minus(pptp).cross(p2tp.minus(pptp));

					//first sign
					if (firstsign == 0) firstsign = cr.a1;
					
					if (Math.signum(cr.a1) != Math.signum(firstsign) ) {
						inside = false; break;
					}

					pp = p2;
				}

				if (inside) {
					//generate point
					ContactPoint cp = new ContactPoint();
					cp.depth = depth + deviation*0;
					cp.midpoint.assign(S.multiply(p1tp).add(midpoint));
					cp.normal.assign(direction.multiply(1));
					cp.penetrating = penetrating;
					contacts.add(cp);
				}
			}
		}


		if (face1.size()>2) {
			double firstsign = 0;
			for (Vector3 p1: face2)  {
				//transform and project
				Vector3 p1tp = Si.multiply(p1.minus(midpoint));
				double deviation = p1tp.a1;
				p1tp.a1 = 0;
				
				//System.out.println("deviation="+deviation);

				boolean inside = true;
				Vector3 pp = face1.get(face1.size()-1).copy();
				for (Vector3 p2: face1) {

					//transform and project
					Vector3 pptp = Si.multiply(pp.minus(midpoint));
					pptp.a1 = 0;
					Vector3 p2tp = Si.multiply(p2.minus(midpoint));
					p2tp.a1 = 0;

					Vector3 cr = p1tp.minus(pptp).cross(p2tp.minus(pptp));
					//cr.print();

					//first sign
					if (firstsign == 0) firstsign = cr.a1;
					
					if (Math.signum(cr.a1) != Math.signum(firstsign) ) {
						inside = false; break;
					}

					pp = p2;
				}

				if (inside) {
					//generate point
					ContactPoint cp = new ContactPoint();
					cp.depth = depth + deviation*0;
					cp.midpoint.assign(S.multiply(p1tp).add(midpoint));
					cp.normal.assign(v.normalize().multiply(1));
					cp.penetrating = penetrating;
					contacts.add(cp);
				}
			}
		}
		
		//edge edge intersecitons
		if (face1.size()>1 && face2.size()>1) {
			Vector3 p1p = face1.get(face1.size()-1);
			for (Vector3 p1: face1) {

				Vector3 d1 = p1.minus(p1p);
				Vector3 d1t = Si.multiply(d1);
				d1t.a1 = 0;
				//create and project starting point for line1
				Vector3 p1pt = Si.multiply(p1p.minus(midpoint));
				p1pt.a1 = 0;

				
				Vector3 p2p = face2.get(face2.size()-1);
				for (Vector3 p2: face2) {

					Vector3 d2 = p2.minus(p2p);
					Vector3 d2t = Si.multiply(d2);
					d2t.a1 = 0;
					Vector3 point = p2p.minus(p1p);
					Vector3 pointt = Si.multiply(point);
					pointt.a1 = 0;
			
					
//					d1t = d1t;
//					d2t = d2t;
					
					
					double det =  d1t.a2 * (-d2t.a3) - d1t.a3 * (-d2t.a2);
					
					if (Math.abs(det) > 1e-7) {

						double alpha = (1/det)* ((-d2t.a3) * pointt.a2 + d2t.a2 * pointt.a3);
						double beta  = (1/det)* ((-d1t.a3) * pointt.a2 + d1t.a2 * pointt.a3); 

						if ( alpha>0 && alpha <1 && beta>0 && beta<1 ) {
							//System.out.println("intersection " + alpha);


							//generate point
							ContactPoint cp = new ContactPoint();
							cp.depth = depth;
							cp.midpoint.assign(S.multiply(p1pt.add(d1t.multiply(alpha))).add(midpoint)  );
							cp.normal.assign(v.normalize());
							cp.penetrating = penetrating;
							contacts.add(cp);

						 //contacts.get(contacts.size()-1).midpoint.print();


						}
					}
					
					p2p = p2;
				}
				
				
				p1p = p1;
			}
			
			
		}
		
		
//		if ( face1.size() > 1 && face2.size() > 1) {
//			Vector3 p1p = face1.get(face1.size()-1).copy();
//			for (Vector3 p1: face1) {
//				Vector3 d1 = p1.minus(p1p);
//				
//				Vector3 p2p = face2.get(face2.size()-1).copy();
//				for (Vector3 p2: face2) {
//
//					Vector3 d2 = p2.minus(p2p);
//					//Vector3 point = p2p.minus(p1p);
//					//Vector3 pointt = Si.multiply(point);
//					//pointt.a1 = 0;
//
//
//					//				d1t = d1t;
//					//				d2t = d2t;
//					
//					
//					//  p1 + d1 alpha
//					//  p2 + d2 beta
//					//
//					//  p1 + d1 alpha - p2 - d2 beta
//					//
//					//  p1.d1 +d1.d1 alpha - p2.d1 - d2.d1 beta = 0
//					//  p1.d2 +d1.d2 alpha - p2-d2 - d2.d2 beta = 0
//					//
//					//  (p1-p2).d1  [d1.d1  -d2.d1][alpha] = 0 
//					//  (p1-p2).d2  [d1.d2  -d2.d2][beta ] 
////					   
//					//    1/det * [ -d2.d2  -d1-d2 ] * [ -(p1-p2).d1 ]
//					//            [ d2.d1    d1.d1 ]   [ -(p1-p2).d2 ]
//					
//					Vector3 d1n = d1.normalize();
//					Vector3 d2n = d2.normalize();
//
//
//					//find closest point on lines
//					double det = (d1.dot(d1n)*(-d2.dot(d2n)))-(d1.dot(d2n)*(-d2.dot(d1n)));
//					Vector3 p1p2 = p1p.minus(p2p);
//
//					//System.out.println("det="+det);
//
//
//					//if not singular 
//					if (Math.abs(det)> 1e-10) {
//						double alpha = (1/det) * (  -d2.dot(d2n)*(-p1p2.dot(d1n)) + (-d1.dot(d2n))*(-p1p2.dot(d2n))); 
//						double beta =  (1/det) * (  d2.dot(d1n)*(-p1p2.dot(d1n))  +  d1.dot(d1n)*(-p1p2.dot(d2n))); 
//
//						//System.out.println("alpha="+alpha+" beta="+beta);
//						//compute midpoint
//						Vector3 lp1 = p1p.add(d1.multiply(alpha));
//						Vector3 lp2 = p2p.add(d2.multiply(beta));
//
//						//if at internal lines
//						if ( alpha>0 && alpha <1 && beta>0 && beta<1 ) {
//							//System.out.println("alpha="+alpha+" beta="+beta);
//
////							p2p.print();
////							p1p.print();
////							d1.print();
////							d2.print();
//
//							
//							if ( Math.abs(lp1.minus(lp2).normalize().dot(v)) > 0.95 ) {
//
//								Vector3 localmidpoint = (lp1.add(lp2)).multiply(0.5);
//								Vector3 localmidpointtrans = Si.multiply(localmidpoint.minus(midpoint));
//								double deviation = localmidpointtrans.a1;
//
//								System.out.println("deviation="+deviation);
//								//project onto contact plane;
//								localmidpointtrans.a1 = 0;
//
//								//generate point
//								ContactPoint cp = new ContactPoint();
//								cp.depth = depth + deviation;
//								cp.midpoint.assign(S.multiply(localmidpointtrans).add(midpoint)  );
//								//cp.midpoint.assign(localmidpoint);
//								cp.normal.assign(v.normalize());
//								contacts.add(cp);
//							}
//						}
//					}
//
//					p2p = p2;
//				}
//
//				p1p = p1;
//			}
//		}
		
	
		
		//contacts.addAll(face1);
	}
		
}
