package jinngine.geometry.contact;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jinngine.collision.ExpandingPolytope;
import jinngine.collision.GJK;
import jinngine.math.*;
import jinngine.physics.*;
import jinngine.geometry.*;

/**
 * A contact generator based on feature support mappings. This contact generator uses GJK and EPA algorithms to establish
 * a contact normal, penetrating or non-penetrating. In either case, the normal direction is used with the feature support
 * mappings two obtaining two faces that are subsequently intersected against each other in the contact plane, to form 
 * a contact region. See SupportMap3 for details on the feature support mapping. Currently the intersection is done in a
 * simple way that naively intersects all edges and point-face intersections, which can be inefficient for large features. 
 * Also note, that the implementation of these intersections is quite ugly...  
 * @author mo
 *
 */
public class FeatureSupportMapContactGenerator implements ContactGenerator {
	//final double envelopeMin = 2.75;
    private static double envelope = 0.125*0.5;
	private static double shell = envelope*0.75;
	private final SupportMap3 Sa;
	private final SupportMap3 Sb;
	private final GJK closest = new GJK();
	public final Body bodyA, bodyB;
	private final List<ContactPoint> contacts = new LinkedList<ContactPoint>();
	private final List<Vector3> faceA = new ArrayList<Vector3>();
	private final List<Vector3> faceB = new ArrayList<Vector3>();
	private final Vector3 principalNormal = new Vector3();
//	private final Vector3 principalPoint = new Vector3();
	private final double restitution;
	private final double friction;

	public FeatureSupportMapContactGenerator(SupportMap3 sa, Geometry a, SupportMap3 sb, Geometry b) {
		super();
		Sa = sa;
		Sb = sb;
		bodyA = a.getBody();
		bodyB = b.getBody();
		
		//select the smallest restitution and friction coefficients 
		if ( a instanceof Material && b instanceof Material) {
			double ea = ((Material)a).getRestitution();
			double fa = ((Material)a).getFrictionCoefficient();
			double eb = ((Material)b).getRestitution();
			double fb = ((Material)b).getFrictionCoefficient();

			//pick smallest values
			restitution = ea > eb ? eb : ea;
			friction    = fa > fb ? fb : fa;

		} else if ( a instanceof Material ) {
			restitution = ((Material)a).getRestitution();
			friction    = ((Material)a).getFrictionCoefficient();
		} else if ( b instanceof Material ) {
			restitution = ((Material)b).getRestitution();
			friction    = ((Material)b).getFrictionCoefficient();
		} else { //default values
			restitution = 0.7;
			friction = 0.5;
		}
	}

	@Override
	public Iterator<ContactPoint> getContacts() {
		return contacts.iterator();
	}

	@Override
	public boolean run(double dt) {
		//System.out.println("generator called");
		//get envelopes
		//envelope = Sa.getEnvelope(dt)> Sb.getEnvelope(dt)? Sa.getEnvelope(dt) : Sb.getEnvelope(dt);
		//shell = envelope*0.25;
		//run the closest points algorithm
		Vector3 a = new Vector3(); Vector3 b = new Vector3();
		closest.run(Sa, Sb, a, b, envelope); 
		Vector3 v = a.minus(b);
		principalNormal.assign(v.normalize());
		double  d = v.norm();
//		v.print();
		//Sa.supportPoint(Vector3.j).print();
		
		//penetration
		if (closest.getState().simplexSize > 3  ) {
//			System.out.println("penetrating");
			//run EPA
			ExpandingPolytope epa = new ExpandingPolytope();
			epa.run(Sa, Sb, a, b, closest.getState());
			v = a.minus(b);//.multiply(-1); //take opposite direction
			principalNormal.assign(v.normalize().multiply(-1));
			d = v.norm();
			
			//the depth is the penetration depth, plus the outer shell of the object
			double depth = d+shell;
			
			generate(a,b,principalNormal,depth, true);
			//principalNormal.print();
			
			//System.out.println("depth="+depth);
			
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
//			System.out.println("minimum distance is out of envelope, d="+d);
			//no contact
			contacts.clear();
			return false;	
		}		
	} 

	private void generate(Vector3 a, Vector3 b, Vector3 v, double depth, boolean penetrating ) {
		contacts.clear(); faceA.clear(); faceB.clear();
		Sa.supportFeature(v.multiply(-1), 0.09, faceA);
		Sb.supportFeature(v.multiply(1), 0.09, faceB);
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

		
		// *)face-point case
		if ( faceB.size()>2) {
			//determine face normal
			Vector3 facenormal = faceB.get(0).minus(faceB.get(1)).cross(faceB.get(2).minus(faceB.get(1))).normalize();
			
			//for all points in face A
			for (Vector3 paw: faceA)  {
				double firstsign = 0;

				//transform p1 into contact space, and project onto tangential plane
				Vector3 p1tp = Si.multiply(paw.minus(midpoint));
				p1tp.x = 0;

				boolean inside = true;
				Vector3 pp = faceB.get(faceB.size()-1).copy();
				
				//run thru edges of face B, and check if they form a closed
				//curve around the point in face A
				for (Vector3 p2: faceB) {

					//transform and project
					Vector3 pptp = Si.multiply(pp.minus(midpoint));
					pptp.x = 0;
					Vector3 p2tp = Si.multiply(p2.minus(midpoint));
					p2tp.x = 0;

					Vector3 cr = p1tp.minus(pptp).cross(p2tp.minus(pptp));

					//first sign
					if (firstsign == 0) firstsign = cr.x;
					
					if (Math.signum(cr.x) != Math.signum(firstsign) ) {
						inside = false; 
						//System.out.println("outside");
						break;
					}

					pp = p2;
				}

				if (inside) {
					//generate point
					ContactPoint cp = new ContactPoint();
					cp.restitution = restitution;
					cp.friction = friction;

					//determine the true distance to the other face along the contact normal
					// ((d t + paw) - pp) . facenormal = 0
					// d t fn + paw fn - pp fn =  0
					// d t fn = pp fn - paw fn
					// t = (pp-paw).fn / d.fn										 
					double t = -pp.minus(paw).dot(facenormal) / direction.dot(facenormal);
					cp.distance = t;
					
					//use t to calculate to intersection point on face B
					Vector3 pbw = direction.multiply(t).add(paw);
					
					//if within envelope, generate a contact point
					if (cp.distance < envelope) {
						cp.depth = shell-cp.distance;
						cp.paw.assign(paw);
						cp.pbw.assign(pbw);
						cp.pa.assign(bodyA.toModel(paw));
						cp.pb.assign(bodyB.toModel(pbw));
						cp.midpoint.assign(S.multiply(p1tp).add(midpoint));
						cp.normal.assign(direction);
						contacts.add(cp);
					} 
				} //inside
			}
		}


		//*) face - point intersection
		if (faceA.size()>2 ) {
			//determine face normal
			Vector3 facenormal = faceA.get(0).minus(faceA.get(1)).cross(faceA.get(2).minus(faceA.get(1))).normalize();

			for (Vector3 p1: faceB)  {
				double firstsign = 0;

				//transform and project
				Vector3 p1tp = Si.multiply(p1.minus(midpoint));
				p1tp.x = 0;
				
				//System.out.println("deviation="+deviation);

				boolean inside = true;
				Vector3 pp = faceA.get(faceA.size()-1).copy();
				for (Vector3 p2: faceA) {

					//transform and project
					Vector3 pptp = Si.multiply(pp.minus(midpoint));
					pptp.x = 0;
					Vector3 p2tp = Si.multiply(p2.minus(midpoint));
					p2tp.x = 0;

					Vector3 cr = p1tp.minus(pptp).cross(p2tp.minus(pptp));
					//cr.print();

					//first sign
					if (firstsign == 0) firstsign = cr.x;
					
					if (Math.signum(cr.x) != Math.signum(firstsign) ) {
						inside = false; break;
					}

					pp = p2;
				}

				if (inside) {
					//generate point
					ContactPoint cp = new ContactPoint();
					cp.restitution = restitution;
					cp.friction = friction;

					//determine the true distance to the other face along the contact normal
					// ((d t + paw) - pp) . facenormal = 0
					// d t fn + paw fn - pp fn =  0
					// d t fn = pp fn - paw fn
					// t = (pp-paw).fn / d.fn										 
					double t = pp.minus(p1).dot(facenormal) / direction.dot(facenormal);
					cp.distance = t;
					
					//use t to calculate to intersection point on face B
					Vector3 paw = direction.multiply(t).add(p1);
					
					//if within envelope, generate a contact point
					if (cp.distance < envelope) {
						cp.depth = shell-cp.distance;
						cp.paw.assign(paw);
						cp.pbw.assign(p1);
						cp.pa.assign(bodyA.toModel(paw));
						cp.pb.assign(bodyB.toModel(p1));
						cp.midpoint.assign(S.multiply(p1tp).add(midpoint));
						cp.normal.assign(direction);
						contacts.add(cp);
					} 
				
				} //inside
				
			}
		}//face - point case
		
		//edge edge intersecitons  
		if (faceA.size()>1 && faceB.size()>1 ) {
			Vector3 p1p = faceA.get(faceA.size()-1);
			for (Vector3 p1: faceA) {

				Vector3 d1 = p1.minus(p1p);
				Vector3 d1t = Si.multiply(d1);
				d1t.x = 0;
				//create and project starting point for line1
				Vector3 p1pt = Si.multiply(p1p.minus(midpoint));
				p1pt.x = 0;

				
				Vector3 p2p = faceB.get(faceB.size()-1);
				for (Vector3 p2: faceB) {

					Vector3 d2 = p2.minus(p2p);
					Vector3 d2t = Si.multiply(d2);
					d2t.x = 0;
					Vector3 point = p2p.minus(p1p);
					Vector3 pointt = Si.multiply(point);
					pointt.x = 0;
			
					
//					d1t = d1t;
//					d2t = d2t;
					
					
					double det =  d1t.y * (-d2t.z) - d1t.z * (-d2t.y);
					
					if (Math.abs(det) > 1e-7) {

						double alpha = (1/det)* ((-d2t.z) * pointt.y + d2t.y * pointt.z);
						double beta  = (1/det)* ((-d1t.z) * pointt.y + d1t.y * pointt.z); 

						if ( alpha>0 && alpha <1 && beta>0 && beta<1 ) {
							//generate point
							ContactPoint cp = new ContactPoint();
							cp.restitution = restitution;
							cp.friction = friction;
							
							//find points on bodies
							Vector3 paw = p1p.add(d1.multiply(alpha));
							Vector3 pbw = p2p.add(d2.multiply(beta));
							
//							cp.distance = p1p.add(d1.multiply(alpha)).minus( p2p.add(d2.multiply(beta))).dot(direction);
							cp.distance = paw.minus(pbw).dot(direction);
							
							double d = cp.distance;

							
							//find distance of projected points
							if (d<envelope) {
								cp.depth = shell-d;
								cp.paw.assign(paw);
								cp.pbw.assign(pbw);
								cp.pa.assign(bodyA.toModel(paw));
								cp.pb.assign(bodyB.toModel(pbw));
								
								cp.midpoint.assign(S.multiply(p1pt.add(d1t.multiply(alpha))).add(midpoint)  );
								cp.normal.assign(direction);
								contacts.add(cp);
							}
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

    
    public static double getEnvelope() {
		return envelope;
	}

	public static void setEnvelope(double envelope) {
		if (envelope>0) {
			FeatureSupportMapContactGenerator.envelope = envelope;
			FeatureSupportMapContactGenerator.shell = envelope*0.75;
		}
	}

	
}
