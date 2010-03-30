package jinngine.geometry;

import java.util.List;
import jinngine.collision.GJK;
import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;

public class MassProperties {
	
	private final GJK gjk = new GJK();
	private final int maxdepth;
	private final SupportMap3 Sa;
	private double totalmass = 0;
	private final Vector3 centreofmass = new Vector3();
	private final InertiaMatrix inertia = new InertiaMatrix();
	
	public MassProperties( SupportMap3 shape, int maxdepth ) {
		this.Sa = shape;
		this.maxdepth = maxdepth;
		
		// start subdivision using initial bounding box
		divide( Sa.supportPoint(new Vector3(1,0,0)).x, Sa.supportPoint(new Vector3(-1,0,0)).x,
				Sa.supportPoint(new Vector3(0,1,0)).y, Sa.supportPoint(new Vector3(0,-1,0)).y,     
				Sa.supportPoint(new Vector3(0,0,1)).z, Sa.supportPoint(new Vector3(0,0,-1)).z, 0 );
		System.out.println("calculated mass " + totalmass);
		
		// finalise calculation of centre of mass
		Vector3.multiply( centreofmass, (1/totalmass));
		
		// align inertia tensor to centre of mass
		InertiaMatrix.translate( inertia, totalmass, centreofmass.multiply(-1));		
	}
	
	private final void divide( 
			final double xmax, final double xmin, 
			final double ymax, final double ymin, 
			final double zmax, final double zmin, 
			int depth ) {
		
		
		// create a support map for this box
		SupportMap3 Sb = new SupportMap3() {
			@Override
			public Vector3 supportPoint(Vector3 v) {
				double sv1 = v.x<0?xmin:xmax;
				double sv2 = v.y<0?ymin:ymax;
				double sv3 = v.z<0?zmin:zmax;
				return new Vector3(sv1, sv2, sv3);
			}
			public void supportFeature(Vector3 d, double epsilon,List<Vector3> face) {}
		};

		
		// side lengths and local mass of partition
		double xl = Math.abs(xmax-xmin);
		double yl = Math.abs(ymax-ymin);
		double zl = Math.abs(zmax-zmin);	
		double localmass = xl*yl*zl;
		
		System.out.println("localmass = " +localmass);
		System.out.println(""+xmin+","+xmax+"\n"+
				""+ymin+","+ymax+"\n"+
				""+zmin+","+zmax+"\n");

		
		// find out if separated using gjk
		Vector3 va = new Vector3(), vb = new Vector3(); boolean separated = false;
		gjk.run(Sa, Sb, va, vb, 0/*Double.POSITIVE_INFINITY*/, 1e-7, 32);
		separated = va.minus(vb).norm() > 1e-7; 
//		va.minus(vb).print();

		
		// seperated?
		if (separated) {
//			System.out.println("seperated");
			return;
		} else if ( localmass < 0.1 ){
			
			totalmass = totalmass + localmass;
			
			// inertia matrix for this local box
			InertiaMatrix localinertia = new InertiaMatrix();
			Matrix3.set( localinertia,
					(1.0f/12.0f)*localmass*(yl*yl+zl*zl), 0.0f, 0.0f,
					0.0f, (1.0f/12.0f)*localmass*(xl*xl+zl*zl), 0.0f,
					0.0f, 0.0f, (1.0f/12.0f)*localmass*(yl*yl+xl*xl) );
			
			// translate inertia matrix
			Vector3 localcentre = new Vector3((xmax+xmin)*0.5, (ymax+ymin)*0.5, (zmax+zmin)*0.5);			
			InertiaMatrix.translate(localinertia, localmass, localcentre);
			
			// add to final inertia matrix
			Matrix3.add(inertia, localinertia, inertia);
			
			// update centre of mass vector
			Vector3.add(centreofmass, localcentre.multiply(localmass));
			
			return;
		} else {
			//subdivide this cell into 8 partitions
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
			
	public Vector3 getCentreOfMass() {
		return centreofmass.copy();
	}
	
	public double getMass() {
		return totalmass;
	}
	
	public Matrix3 getInertiaMatrix() {
		return inertia.copy();
	}
	

}
