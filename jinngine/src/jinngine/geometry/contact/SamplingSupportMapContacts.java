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
		//assume objects are overlapping
		
		
		points.clear();
		
		tdist = Double.POSITIVE_INFINITY;
		
		
		
		for (int n=0;n<25;n++) {
			//Create sample direction and sample points
			Vector3 v = (new Vector3(random.nextGaussian(),random.nextGaussian(), random.nextGaussian())).normalize();
			Vector3 a = Sa.supportPoint(v.multiply(-1));
			Vector3 b = Sb.supportPoint(v.multiply(1));
			Vector3 w = a.minus(b);

			
			if (w.norm() < tdist ) {
				tdist = w.norm();
				ta.assign(a);
				tb.assign(b);
			}
			
			if ( v.dot(w) > 0) {
				//System.out.println("separation");
				return true;
			}
			
		}		
		ContactPoint cp = new ContactPoint();


		
		cp.normal.assign(ta.minus(tb).normalize().multiply(-1));
		cp.midpoint.assign(ta.add(tb).multiply(0.5));
		cp.depth = tdist>0.1?tdist-0.1:0;
		//cp.normal.print();
		
		points.add(cp);
		System.out.println(""+tdist);

		return true;
	}

}
