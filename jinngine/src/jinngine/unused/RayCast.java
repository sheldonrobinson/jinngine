package jinngine.unused;

import jinngine.geometry.SupportMap3;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.physics.Sphere;

public class RayCast {
	public void run( 
			final SupportMap3 Sb, 
			final Vector3 s, 
			final Vector3 r) 
	{
		final GJKn gjk = new GJKn();
		final Body dummy = new Sphere(1);
		final double epsilon = 1e-10;		
		double lambda=0;
		final Vector3 x = s.copy();
		Vector3 n = new Vector3();
		Vector4 pa = new Vector4();
		Vector4 pb = new Vector4();
		
		System.out.println("(*) RayCast");
		
		SupportMap4 Sa4 = new SupportMap4() {
			public Vector4 supportPoint( Vector4 v ) {
				return new Vector4(x);
			}
		};
		
		SupportMap4 Sb4 = new SupportMap4() {
			public Vector4 supportPoint( Vector4 v ) {
				return new Vector4( Sb.supportPoint(new Vector3(v.a1,v.a2,v.a3)));
			}
		};

		
		gjk.run(Sa4,Sb4,pa,pb);
		Vector3 c = new Vector3();
		//c.assign(pb);
		
		while ( x.minus(c).norm() > epsilon ) {
			n.assign(x.minus(c));
			if ( n.dot(r) >= 0) {
				System.out.println("RayCast: miss, lambda="+lambda);
				return;
			} else {
				lambda = lambda - n.dot(n) / n.dot(r);
				x.assign(s.add(r.multiply(lambda)));
				System.out.println("lambda="+lambda);
				
				gjk.run(Sa4,Sb4,pa,pb);
				//c.assign(pb);
				

			}			
		}
		
		System.out.println("RayCast: Hitpoint lambda=" + lambda);
		n.print();
		
	}

}
