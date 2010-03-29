package jinngine.geometry;

import java.util.List;
import jinngine.collision.GJK;
import jinngine.math.Vector3;

public class MassCalculation {
	
	private final GJK gjk = new GJK();
	private final int maxdepth;
	private final SupportMap3 Sa;
	private double mass = 0;
	
	public MassCalculation( SupportMap3 shape, int maxdepth ) {
		this.Sa = shape;
		this.maxdepth = maxdepth;
		
		// start subdivision using initial bounding box
		divide( Sa.supportPoint(new Vector3(1,0,0)).x, Sa.supportPoint(new Vector3(-1,0,0)).x,
				Sa.supportPoint(new Vector3(0,1,0)).y, Sa.supportPoint(new Vector3(0,-1,0)).y,     
				Sa.supportPoint(new Vector3(0,0,1)).z, Sa.supportPoint(new Vector3(0,0,-1)).z, 0 );
		System.out.println("calculated mass " + mass);
		
	}
	
	private final void divide( 
			final double xmax, final double xmin, 
			final double ymax, final double ymin, 
			final double zmax, final double zmin, 
			int depth ) {
		
		boolean separated = false;
		// create a support map for this box
		SupportMap3 Sb = new SupportMap3() {
			@Override
			public Vector3 supportPoint(Vector3 v) {
				double sv1 = v.x<0?xmin:xmax;
				double sv2 = v.y<0?ymin:ymax;
				double sv3 = v.z<0?zmin:xmax;
				return new Vector3(sv1, sv2, sv3);
			}
			public void supportFeature(Vector3 d, double epsilon,List<Vector3> face) {}
		};
		
		// find out if separated using gjk
		Vector3 va = new Vector3(), vb = new Vector3();
		gjk.run(Sa, Sb, va, vb, Double.POSITIVE_INFINITY, 1e-7, 32);
		separated = va.minus(vb).norm() > 1e-7; 
				
		// seperated?
		if (separated) {
//			System.out.println("seperated");
			return;
		} else if ( depth > maxdepth){
			double xl = Math.abs(xmax-xmin);
			double yl = Math.abs(ymax-ymin);
			double zl = Math.abs(zmax-zmin);
			
			mass = mass + xl*yl*zl;
//			System.out.println("added " +  xl*yl*zl );
			
			return;
		} else {
			//subdivide this cell 
			divide( (xmax+xmin)*0.5, xmin,           (ymax+ymin)*0.5,  ymin,            (zmax+zmin)*0.5, zmin,     depth+1);
			divide( xmax,           (xmax+xmin)*0.5, (ymax+ymin)*0.5,  ymin,            (zmax+zmin)*0.5, zmin,     depth+1);
			divide( (xmax+xmin)*0.5, xmin,            ymax,            (ymax+ymin)*0.5, (zmax+zmin)*0.5, zmin,     depth+1);
			divide( xmax,            (xmax+xmin)*0.5, ymax,            (ymax+ymin)*0.5, (zmax+zmin)*0.5, zmin,     depth+1);
			divide( (xmax+xmin)*0.5, xmin,            (ymax+ymin)*0.5, ymin,            zmax,            (zmax+zmin)*0.5, depth+1);
			divide( xmax,            (xmax+xmin)*0.5, (ymax+ymin)*0.5, ymin,            zmax,            (zmax+zmin)*0.5, depth+1);
			divide( (xmax+xmin)*0.5, xmin,            ymax,            (ymax+ymin)*0.5, zmax,            (zmax+zmin)*0.5, depth+1);
			divide( xmax,            (xmax+xmin)*0.5, ymax,            (ymax+ymin)*0.5, zmax,            (zmax+zmin)*0.5, depth+1);
		}
		
	}
			 

}
