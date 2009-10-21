package jinngine.geometry.contact;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import jinngine.geometry.SupportMap3;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public final class SamplingSupportMapContacts implements ContactGenerator {

	private final Body A,B;
	private final SupportMap3 Sa, Sb;
	private final Random random= new Random();
	private final Vector3 ta = new Vector3();
	private final Vector3 tb = new Vector3();
	private final Vector3 tnormal = new Vector3();
	private final Vector3 axis = new Vector3();
	private double tdist = Double.POSITIVE_INFINITY;
	
	private final ArrayList<ContactPoint> points = new ArrayList<ContactPoint>();
	
	
	public SamplingSupportMapContacts(Body a, Body b, SupportMap3 sa,
			SupportMap3 sb) {
		super();
		this.A = a;
		this.B = b;
		Sa = sa;
		Sb = sb;
	}

	@Override
	public Iterator<ContactPoint> getContacts() {
		return points.iterator();
	}

	@Override
	public boolean run(double dt) {
		points.clear();
		
		//Create sample direction and sample points
		Vector3 v = (new Vector3(random.nextGaussian(),random.nextGaussian(), random.nextGaussian())).normalize();
		Vector3 a = Sa.supportPoint(v.multiply(-1));
		Vector3 b = Sb.supportPoint(v.multiply(1));
		Vector3 w = a.minus(b);

		//check for separation axis
//		if (v.dot(w)>0) {
//			ta.assign(A.toModel(a));
//			tb.assign(B.toModel(b));
//			axis.assign(v);
//			tdist = Double.POSITIVE_INFINITY;
//			System.out.println("seperation");
//			points.clear();
//			return true;
//		}
		
		//better point
		double d = w.norm();	
		if ( d < tdist) {
			ta.assign(A.toModel(a));
			tb.assign(B.toModel(b));
			axis.assign(v);
			tnormal.assign(w.normalize());
			tdist = d;
		}

		
		
		ContactPoint cp = new ContactPoint();
		Vector3 pa = A.toWorld(ta);
		Vector3 pb = B.toWorld(tb);	
		tdist = pa.minus(pb).norm();
		
		if (axis.dot(pa.minus(pb))>0 ) {
			System.out.println("seperation");
		}

		
		cp.normal.assign(pa.minus(pb).normalize().multiply(-1));
		cp.midpoint.assign(pa.minus(pb).multiply(0.5));
		cp.depth = tdist;
		cp.normal.print();
		//points.add(cp);
		System.out.println(""+tdist);

		return true;
	}

}
