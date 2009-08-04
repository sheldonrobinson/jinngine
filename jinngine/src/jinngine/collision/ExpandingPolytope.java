package jinngine.collision;

import java.util.Comparator;

import jinngine.geometry.SupportMap3;
import jinngine.math.*;
import jinngine.util.Heap;

/**
 * Naive implementation of the expanding polytope algorithm. The algorithm is capable of computing
 * the penetration depth of two intersecting convex objects, represented by support mappings. 
 * @author mo
 *
 */
public class ExpandingPolytope {
	
	Heap<Triangle> heap = new Heap<Triangle>(new Comparator<Triangle>() {
		@Override
		public int compare(Triangle arg0, Triangle arg1) {
			return arg0.d<arg1.d?-1:1;
		}});
	
	private final class Triangle {
		public Triangle(Vector3 p1, Vector3 a1, Vector3 b1, Vector3 p2,
				Vector3 a2, Vector3 b2, Vector3 p3, Vector3 a3, Vector3 b3) {
			super();
			this.a1 = a1;
			this.a2 = a2;
			this.a3 = a3;
			this.b1 = b1;
			this.b2 = b2;
			this.b3 = b3;
			this.p1 = p1;
			this.p2 = p2;
			this.p3 = p3;

			//setup linear system of equations to find closest point to the origin
			Matrix3 M = new Matrix3(   1,                    1,                    1,
					p1.dot(p2.minus(p1)), p2.dot(p2.minus(p1)), p3.dot(p2.minus(p1)),
					p1.dot(p3.minus(p1)), p2.dot(p3.minus(p1)), p3.dot(p3.minus(p1)) );
			
			
			//calculate inverse and find a point 
			Matrix3 Mi = new Matrix3();			
			Vector3 lambda = Matrix3.inverse(M, Mi).column(0);
			
			//if all lambda values are positive, compute true distance to origin
			if (lambda.isWeaklyGreaterThanZero()) {
				p.assign(new Matrix3(p1,p2,p3).multiply(lambda));
				a.assign(new Matrix3(a1,a2,a3).multiply(lambda));
				b.assign(new Matrix3(b1,b2,b3).multiply(lambda));
				
				d = p.norm();
				
				// in case of non-positive lambda values, closest point is not contained inside the triangle
			} else {d = Double.POSITIVE_INFINITY; }
			
			//System.out.println("d=" + d);
			//lambda.print();
		}
		public final Vector3 p1;
		public final Vector3 p2;
		public final Vector3 p3;
		public final Vector3 a1;
		public final Vector3 a2;
		public final Vector3 a3;
		public final Vector3 b1;
		public final Vector3 b2;
		public final Vector3 b3;

		
		public final Vector3 p = new Vector3();
		public final Vector3 a = new Vector3();
		public final Vector3 b = new Vector3();

		public final double d;
	}
	
	public void run( SupportMap3 Sa, SupportMap3 Sb, Vector3 pa, Vector3 pb, GJK3.State tetrahedron) {
		//System.out.println("*) EPA Run");
		Vector3 s1 = tetrahedron.simplices[0][0].copy();
		Vector3 s2 = tetrahedron.simplices[1][0].copy();
		Vector3 s3 = tetrahedron.simplices[2][0].copy();
		Vector3 s4 = tetrahedron.simplices[3][0].copy();

		Vector3 a1 = tetrahedron.simplices[0][1].copy();
		Vector3 a2 = tetrahedron.simplices[1][1].copy();
		Vector3 a3 = tetrahedron.simplices[2][1].copy();
		Vector3 a4 = tetrahedron.simplices[3][1].copy();

		Vector3 b1 = tetrahedron.simplices[0][2].copy();
		Vector3 b2 = tetrahedron.simplices[1][2].copy();
		Vector3 b3 = tetrahedron.simplices[2][2].copy();
		Vector3 b4 = tetrahedron.simplices[3][2].copy();

		heap.clear();
		
		
		//extract 4 triangles from the tetrahedron
		heap.insert(new Triangle(s1,a1,b1, s2,a2,b2, s3,a3,b3));
		heap.insert(new Triangle(s1,a1,b1, s2,a2,b2, s4,a4,b4));
		heap.insert(new Triangle(s1,a1,b1, s3,a3,b3, s4,a4,b4));
		heap.insert(new Triangle(s2,a2,b2, s3,a3,b3, s4,a4,b4));

		
		double d = 0;
		while( heap.size() > 0) {
			Triangle t = heap.pop();
			
			//System.out.println("  size " + t.d + " point is " + t +" in heap " + heap.size() );
			//t.p.print();
			
			if ( t.d == Double.POSITIVE_INFINITY) {
				//no more triangles are usable, we terminate
				break;
			}
			
			//stop if no improvement
			if (Math.abs(t.d-d) < 1e-7) {
				pa.assign(t.a);
				pb.assign(t.b);
				break;
			}
			
			d= t.d;
			
			//split this triangle into 3 new and insert the into the heap			
			Vector3 a = Sa.supportPoint(t.p);
			Vector3 b = Sb.supportPoint(t.p.multiply(-1));
			Vector3 w = a.minus(b);
			
			heap.insert(new Triangle(t.p1,t.a1,t.b1, t.p2,t.a2,t.b2, w,a,b) );
			heap.insert(new Triangle(t.p1,t.a1,t.b1, t.p3,t.a3,t.b3, w,a,b) );
			heap.insert(new Triangle(t.p2,t.a2,t.b2, t.p3,t.a3,t.b3, w,a,b) );

		}
		
		

			
		
	}

}
