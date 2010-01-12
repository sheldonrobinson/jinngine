package jinngine.physics.constraint;

import java.util.*;

import jinngine.geometry.contact.*;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.solver.*;
import jinngine.physics.solver.Solver.constraint;
import jinngine.util.GramSchmidt;
import jinngine.util.Pair;


public final class CorrectionContact implements ContactConstraint {	
	private final Body b1, b2;                  //bodies in constraint
	private final List<ContactGenerator> generators = new ArrayList<ContactGenerator>();
	private final Map<ContactGenerator,List<contactpoint>> contacts = new HashMap<ContactGenerator,List<contactpoint>>();
	
	private final double nfactor = 1;	
	private final double cfactor = 1;
	
	private class contactpoint {
		boolean sticking = false;
		Vector3 point = new Vector3();
		Vector3 pa = new Vector3();
		Vector3 pb = new Vector3();		
		constraint n = new constraint();
		constraint t1 = new constraint();
		constraint t2 = new constraint();
	}
	
	

	public CorrectionContact(Body b1, Body b2, ContactGenerator generator) {
		super();
		this.b1 = b1;
		this.b2 = b2;
		addGenerator(generator);
	}

	public void addGenerator(ContactGenerator g) {
		this.generators.add(g);
		this.contacts.put(g,new ArrayList<contactpoint>());
	}

	public void removeGenerator(ContactGenerator g) {
		this.generators.remove(g);
		this.contacts.remove(g);
	}
	
	public double getNumberOfGenerators() {
		return generators.size();
	}
	
	@Override
	public final void applyConstraints(ListIterator<constraint> constraintIterator, double dt) {
//		System.out.println("------");

		//use ContactGenerators to create new contactpoints
		for ( ContactGenerator cg: generators) {
			//run contact generator
			cg.run(dt);

			List<contactpoint> pointlist = contacts.get(cg); 

			ListIterator<contactpoint> pointiter = pointlist.listIterator();
			Iterator<ContactGenerator.ContactPoint> i = cg.getContacts();
			
			//System.out.println("pointlist size="+pointlist.size());
			// ONLY apply one contact for each generator
			while (i.hasNext()) {
				ContactGenerator.ContactPoint cp = i.next();

				contactpoint co;

				if (pointiter.hasNext()) {
					co = pointiter.next();
				} else {
					co = new contactpoint();
					pointiter.add(co);
				}

				//investigate sticking
				if (co.t1.body1 != null) {
					double prev = co.t1.j1.dot(co.t1.body1.state.vCm) + co.t1.j2.dot(co.t1.body1.state.omegaCm)
					+ co.t1.j3.dot(co.t1.body2.state.vCm) + co.t1.j4.dot(co.t1.body2.state.omegaCm);
					
//					System.out.println("t1:prev v="+prev+ "  force="+co.t1.lambda+" ["+(co.t1.coupledMax.lambda*co.t1.coupledMax.mu)+"]");

					double lim = co.t1.coupling.lambda*co.t1.coupling.mu;
					double force = co.t1.lambda;
					
					if ( lim-Math.abs(force) < 1e-10 &&  Math.abs(prev) > 1e-3  ) {
						co.sticking = false;
					} else {
						if (!co.sticking) {
							co.sticking = true;
							co.point.assign(cp.midpoint);
							co.pa.assign(cp.pa);
							co.pb.assign(cp.pb);
						}
					}
					
				}

				
				createFrictionalContactConstraint(cp, b1, b2, cp.midpoint, cp.normal, cp.depth, dt, co, constraintIterator);				
			}
		}
	}

	/**
	 * Method that computes the relative velocity in the point p (in world coordinates), measured along the normal n. 
	 * @param b1 
	 * @param b2
	 * @param p
	 * @param n
	 * @return The relative velocity in the point p
	 */
	public final static double relativeVelocity(final Body b1, final Body b2, final Vector3 p, final Vector3 n ) 
	{
		// Vector rA = cp.Minus( A.r_cm );  
		//    Vector rB = cp.Minus( B.r_cm );

		// Relative contact velocity u, is
		// u = pdotA - pdotB
		//
		// where 
		// pdotX = omegaX x rX + v_cmX

		//  Vector pdotA = A.omega_cm.CrossProduct( rA ).Add(  A.v_cm );
		//  Vector pdotB = B.omega_cm.CrossProduct( rB ).Add(  B.v_cm );
		//  Vector u = pdotA.Minus( pdotB ); 

		//  double velocity = n.DotProduct(u);

		//   if ( u.DotProduct(n) > 0 ) {
		//Objects are not in collision in cp along n, RCV is negative
		//velocity = -velocity;
		//}

		//System.out.println("relative contact velocity (A-B) in cp " + velocity );
		Vector3 rb1 = new Vector3();
		Vector3 rb2 = new Vector3();
		Vector3 pdotb1 = new Vector3();
		Vector3 pdotb2 = new Vector3();
		Vector3 u = new Vector3();

		Vector3.sub( p, b1.state.rCm, rb1 );
		Vector3.sub( p, b2.state.rCm, rb2 );
		Vector3.crossProduct( b1.state.omegaCm, rb1, pdotb1 );
		Vector3.add( pdotb1, b1.state.vCm );
		Vector3.crossProduct( b2.state.omegaCm, rb2, pdotb2 );
		Vector3.add( pdotb2, b2.state.vCm );
		Vector3.sub( pdotb1, pdotb2, u );

		return Vector3.dot( n, u );
	}


	//Create a regular contact constraint including tangential friction
	public final void createFrictionalContactConstraint( 
			ContactGenerator.ContactPoint cp,
			Body b1, Body b2, Vector3 p, Vector3 n, double depth, double dt, contactpoint co,
			ListIterator<constraint> outConstraints 
	) {

		//Use a gram-schmidt process to create a orthonormal basis for the contact point ( normal and tangential directions)
		Vector3 t1 = new Vector3(), t2 = new Vector3(), t3 = new Vector3();
		Matrix3 B  = GramSchmidt.run(n);
		B.getColumnVectors(t1, t2, t3);
			
		//First off, create the constraint in the normal direction
		double e = cp.restitution; //coeficient of restitution
		double uni = relativeVelocity(b1,b2,p,n);
		double unf = uni<0 ? -e*uni: 0;
		
		//truncate small collision
		//unf = unf < 0.1? 0: unf;
		
		Vector3 r1 = p.minus(b1.state.rCm);
		Vector3 r2 = p.minus(b2.state.rCm);
		Vector3 J1 = n.multiply(-1);
		Vector3 J2 = r1.cross(n).multiply(-1);
		Vector3 J3 = n;
		Vector3 J4 = r2.cross(n).multiply(1);

		//compute B vector
		Matrix3 I1 = b1.state.Iinverse;
		double m1 = b1.state.M;
		Matrix3 I2 = b2.state.Iinverse;
		double m2 = b2.state.M;

		//		B = new Vector(n.multiply(-1/m1))
		//		.concatenateHorizontal( new Vector(I1.multiply(r1.cross(n).multiply(-1))) )
		//		.concatenateHorizontal(new Vector(n.multiply(1/m2)))
		//		.concatenateHorizontal(new Vector(I2.multiply(r2.cross(n).multiply(1))));

		Vector3 B1 = n.multiply(-1/m1);
		Vector3 B2 = I1.multiply(r1.cross(n).multiply(-1));
		Vector3 B3 = n.multiply(1/m2);
		Vector3 B4 = I2.multiply(r2.cross(n));

		if (b1.isFixed() ) { B1.assign( B2.assign(Vector3.zero)); }
		if (b2.isFixed() ) { B3.assign( B4.assign(Vector3.zero)); }

		//external forces acing at contact
		double Fext = B1.dot(b1.state.FCm) + B2.dot(b1.state.tauCm) + B3.dot(b2.state.FCm) + B4.dot(b2.state.tauCm);
		//double cv = cp.penetrating?0.5:0.50; //max. correction velocity
		//depth = depth > 0? 0:depth;
		double correction = 0;
		double lowerNormalLimit = 0;

		correction = depth*(1/dt)*0.8;
		
		double limit = 5.5;
		correction = correction< -limit? -limit:correction;
		correction = correction>  limit?  limit:correction;
		//correction = 0;
		
		//truncate correction if already covered by repulsive velocity
		if (correction > 0) {
			if (unf > correction ) {
				correction = 0;
			} else {
				correction = correction - unf;
			}
		}
		
		

		//then the tantential friction constraints (totaly sticking in all cases, since lambda is unbounded)
		double ut1i = relativeVelocity(b1,b2,p,t2);
		double ut2i = relativeVelocity(b1,b2,p,t3);
		double ut1f = 0; 
		double ut2f = 0;

		//send points to world space
		Vector3 paw = b1.toWorld( co.pa);
		Vector3 pbw = b2.toWorld( co.pb);
		
		//transform points to the contact space		
		Vector3 ta = B.transpose().multiply(paw.minus(cp.midpoint));
		Vector3 tb = B.transpose().multiply(pbw.minus(cp.midpoint));
		Vector3 disp = ta.minus(tb);
		
		double dt1 = 0;
		double dt2 = 0;
		
		if (co.pa.norm() > 1e-7 && co.sticking) {

			dt1 = -disp.y*(1/dt);
		    dt2 = -disp.z*(1/dt);
//			co.pa.print();
//			co.pb.print();
//			paw.minus(pbw).print();
//			disp.print();



		}
		//System.out.println(dt1+","+dt2);
		

		//normal
		constraint c = co.n;
		c.assign(b1,b2,B1,
				B2,B3,B4,J1,
				J2,J3,J4,lowerNormalLimit,
				Double.POSITIVE_INFINITY,null,
				unf-uni + Fext*dt + correction*nfactor );
		
		//set the correct friction setting for this contact
		c.mu = cp.friction;

		
		//first tangent
		Vector3 t2B1 = t2.multiply(-1/m1);
		Vector3 t2B2 = I1.multiply(r1.cross(t2).multiply(-1));
		Vector3 t2B3 = t2.multiply(1/m2);				
		Vector3 t2B4 = I2.multiply(r2.cross(t2));
		double t2Fext = t2B1.dot(b1.state.FCm) + t2B2.dot(b1.state.tauCm) + t2B3.dot(b2.state.FCm) + t2B4.dot(b2.state.tauCm);
		constraint c2 = co.t1;
		c2.assign(b1,b2,
				t2B1,
				t2B2,
				t2B3,
				t2B4,				
				t2.multiply(-1),
				r1.cross(t2).multiply(-1),
				t2,
				r2.cross(t2).multiply(1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				c,
				ut1f-ut1i + t2Fext*dt + dt1*cfactor

		);
		
		//book-keep constraints in each body
		//b1.constraints.add(c2);
		//b2.constraints.add(c2);


		//second tangent
		Vector3 t3B1 = t3.multiply(-1/m1);
		Vector3 t3B2 = I1.multiply(r1.cross(t3).multiply(-1));
		Vector3 t3B3 = t3.multiply(1/m2);				
		Vector3 t3B4 = I2.multiply(r2.cross(t3));
		double t3Fext = t3B1.dot(b1.state.FCm) + t3B2.dot(b1.state.tauCm) + t3B3.dot(b2.state.FCm) + t3B4.dot(b2.state.tauCm);
		constraint c3 = co.t2;
		c3.assign(b1,b2,
				t3B1,
				t3B2,
				t3B3,
				t3B4,
				t3.multiply(-1),
				r1.cross(t3).multiply(-1),
				t3,
				r2.cross(t3).multiply(1),
				Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY,
				c,
				ut2f-ut2i + t3Fext*dt +dt2 * cfactor
		);

		//book-keep constraints in each body
		//b1.constraints.add(c3);
		//b2.constraints.add(c3);
		outConstraints.add(c);
		outConstraints.add(c2);
		outConstraints.add(c3);
	}

	@Override
	public Pair<Body> getBodies() {
		return new Pair<Body>(b1,b2);
	}

}
