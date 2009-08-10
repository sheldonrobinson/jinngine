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
    double envelope = 0.25;
    double shell = envelope*0.5;
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
			v = a.minus(b);//.multiply(-1); //take opposite direction
			principalNormal.assign(v.normalize().multiply(-1));
			d = v.norm();
			
			//the depth is the penetration depth, plus the outer shell of the object
			double depth = d+shell;
			
			generate(a,b,principalNormal,depth, true);
			
			
			return true;
		}
		
		//distance is within envelope
		else if ( d > 1e-6  && d < envelope ) {
			//if the distance is larger than the shell, the depth is zero, if it is less than the shell,
			//then the depth is the difference (d is always positive since it is a norm)
			double depth = shell-d < 0 ? 0: shell-d;

			//generate contact points
			generate(a, b, principalNormal, depth, false);

			//System.out.println("contact: d="+d);

			
			return true;
			
		} else {
			//System.out.println("minimum distance is out of envelope, d="+d);
			//no contact
			contacts.clear();
			return false;	
		}		
	} 

	private void generate(Vector3 a, Vector3 b, Vector3 v, double depth, boolean penetrating) {
		contacts.clear(); face1.clear(); face2.clear();
		Sa.supportFeature(v.multiply(-1), 0.06, face1);
		Sb.supportFeature(v.multiply(1), 0.06, face2);
		Vector3 direction = v.normalize();
		Vector3 midpoint = a.add(b).multiply(0.5);

		//create basis
		//Use a gram-schmidt process to create a orthonormal basis for the contact space
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

					//determine the true distance to the other face along the contact normal
					// (p1 + nt - pp ) n = 0 => p1 n + nt n - pp n = 0 => nt n = pp n - p1 n => t = (pp-p1)n/ n.n
					double d= Math.abs(pp.minus(p1).dot(direction));
					cp.distance = d;
					
					if (penetrating) {
						cp.depth = (shell+d);
						//System.out.println("penetrating depth=" + cp.depth);
						cp.midpoint.assign(S.multiply(p1tp).add(midpoint));
						cp.normal.assign(v.normalize().multiply(1));
						cp.penetrating = penetrating;
						contacts.add(cp);
						
						//cp.normal.print();

					} else {
						cp.depth = shell-d;

						if (cp.distance < envelope) {
							cp.midpoint.assign(S.multiply(p1tp).add(midpoint));
							cp.normal.assign(v.normalize().multiply(1));
							cp.penetrating = penetrating;
							contacts.add(cp);
						} 
					}
				} //inside
			}
		}


		//face - point intersection
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

					//determine the true distance to the other face along the contact normal
					// (p1 + nt - pp ) n = 0 => p1 n + nt n - pp n = 0 => nt n = pp n - p1 n => t = (pp-p1)n/ n.n
					double d= Math.abs(pp.minus(p1).dot(direction));
					cp.distance = d;
					
					if (penetrating) {
						cp.depth = (shell+d);
						//System.out.println("penetrating depth=" + cp.depth);

						cp.midpoint.assign(S.multiply(p1tp).add(midpoint));
						cp.normal.assign(v.normalize().multiply(1));
						cp.penetrating = penetrating;
						contacts.add(cp);
					} else {
						cp.depth = shell-d;

						if (cp.distance < envelope) {
							cp.midpoint.assign(S.multiply(p1tp).add(midpoint));
							cp.normal.assign(v.normalize().multiply(1));
							cp.penetrating = penetrating;
							contacts.add(cp);
						} 
					}
				} //inside
				
			}
		}//face - point case
		
		//edge edge intersecitons 
		if (face1.size()>1 && face2.size()>1 ) {
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
							
							//find distance of projected points
							if (penetrating) {
								cp.depth = p1p.add(d1.multiply(alpha)).minus( p2p.add(d2.multiply(beta))).norm() + shell;
							} else {
								cp.depth = shell - p1p.add(d1.multiply(alpha)).minus( p2p.add(d2.multiply(beta))).norm(); 
							}
								
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
			
			
		}//edge-edge case
		

		
		//System.out.println("contacts="+contacts.size());
		//direction.print();
		
	}//generate()
		
}
