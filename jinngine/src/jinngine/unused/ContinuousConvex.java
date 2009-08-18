package jinngine.unused;

import jinngine.collision.ClosestPointsAlgorithm;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class ContinuousConvex implements ClosestPointsAlgorithm {
	private final ClosestPointsAlgorithm closest; 
	private double envelope;
	private double dt;
	
	public ContinuousConvex(ClosestPointsAlgorithm closest, double envelope, double dt) {
		this.closest = closest;
		this.envelope = envelope;
		this.dt = dt;
	}
	
	public void run(Body A, Body B, Vector3 a, Vector3 b, double envelope) {
		Vector3 pa = new Vector3(), pb = new Vector3();
		closest.run(A, B, pa, pb, Double.POSITIVE_INFINITY);
		Vector3 d = pa.minus(pb);
		
		Vector3 vA = A.state.vCm.copy();
		Vector3 vB = B.state.vCm.copy();
		Vector3 omegaA = A.state.omegaCm.copy();
		Vector3 omegaB = B.state.omegaCm.copy();
		
		Body bA = new NullBody();
		Body bB = new NullBody();
		Body bA1 = new NullBody();
		Body bB1 = new NullBody();
		
		//System.out.println("Convex - Convex ***");
		
		
		double tau = 0;
		double umax = 0;
		
		bA.setSupportMap(A.getSupportMap());
		bB.setSupportMap(B.getSupportMap());
		bA1.setSupportMap(A.getSupportMap());
		bB1.setSupportMap(B.getSupportMap());
		
		//copy jinngine.demos.states to bA1 and bB1
		bA1.state.assign(A.state);
		bB1.state.assign(B.state);
		bA1.advancePositions(dt);
		bB1.advancePositions(dt);
		
		
		//advance bodies to tau
		bA.state.rCm.assign(A.state.rCm.add(vA.multiply(tau)));
		bA.state.q.assign( A.state.q.interpolate(bA1.state.q, tau));
		bB.state.rCm.assign(B.state.rCm.add(vB.multiply(tau)));
		bB.state.q.assign( B.state.q.interpolate(bB1.state.q, tau));
		
		
		while ( d.norm() > envelope ) {
			//System.out.println("Convex:" + d.norm() + " tau = " +tau);
			
			Vector3 n = d.normalize();
			umax = vB.minus(vA).dot(n) + omegaA.norm()*dt*A.state.rMax + omegaB.norm()*dt*B.state.rMax;
			
			if (umax <= 0) {
				a.assign(new Vector3());
				b.assign(new Vector3());
				return;
			}
			
			double deltaTau = d.norm() / umax;
			
			tau = tau + deltaTau;
			
			if (tau>1) {
				a.assign(new Vector3());
				b.assign(new Vector3());
				return;
			}

			//advance bodies to tau
			bA.state.rCm.assign(A.state.rCm.add(vA.multiply(tau)));
			bA.state.q.assign( A.state.q.interpolate(bA1.state.q, tau));
			bB.state.rCm.assign(B.state.rCm.add(vB.multiply(tau)));
			bB.state.q.assign( B.state.q.interpolate(bB1.state.q, tau));

			
			closest.run(bA, bB, pa, pb, Double.POSITIVE_INFINITY);
			d = pa.minus(pb);
			n = d.normalize();

			umax = vB.minus(vA).dot(n) + omegaA.norm()*dt*A.state.rMax + omegaB.norm()*dt*B.state.rMax;
		}
		//System.out.println("Convex:" + d.norm() + " tau = " +tau);
		//System.out.println("hit!");
		a.assign(pa); b.assign(pb);
	}

	@Override
	public void run(Object geoa, Object geob, Vector3 pa, Vector3 pb,
			double envelope) {
		// TODO Auto-generated method stub
		
	}

}
