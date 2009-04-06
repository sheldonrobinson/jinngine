package jinngine.stuff;

import jinngine.math.Vector3;

public class Vector4 {
	public double a1,a2,a3,a4;
	
	public Vector4() {
		
	}
	
	public Vector4(double a1, double a2, double a3, double a4) {
		this.a1 = a1;
		this.a2 = a2;
		this.a3 = a3;
		this.a4 = a4;		
	}

	public Vector4( Vector3 v, double a4) {
		this.a1 = v.a1;
		this.a2 = v.a2;
		this.a3 = v.a3;
		this.a4 = a4;		
	}

	public Vector4( Vector3 v ) {
		this.a1 = v.a1;
		this.a2 = v.a2;
		this.a3 = v.a3;
		this.a4 = 0;		
	}
	
	public final double dot(Vector4 v) {
		return a1*v.a1+a2*v.a2+a3*v.a3+a4*v.a4;
	}
	
	public final Vector4 assign( Vector3 v ) {
		double t1=v.a1;
		double t2=v.a2;
		double t3=v.a3;
		a1=t1;
		a2=t2;
		a3=t3;
		a4=0;
		return this;		
	}
	
	public final Vector4 assign( Vector4 v) {
		double t1=v.a1;
		double t2=v.a2;
		double t3=v.a3;
		double t4=v.a4;
		a1=t1;
		a2=t2;
		a3=t3;
		a4=t4;
		return this;
	}

	public final Vector4 add( Vector4 v) {
		return new Vector4( a1+v.a1, a2+v.a2, a3+v.a3, a4+v.a4);
	}

	public final Vector4 minus( Vector4 v) {
		return new Vector4( a1-v.a1, a2-v.a2, a3-v.a3, a4-v.a4);
	}
	
	public final Vector4 multiply( double s ) {
		return new Vector4( a1*s, a2*s, a3*s, a4*s);
	}
	
	public final Vector4 copy() {
		return new Vector4( a1, a2, a3, a4);
	}
	
	public final double norm() {
		return Math.sqrt(a1*a1+a2*a2+a3*a3+a4*a4);
	}
	
	public final void print() {
		System.out.println("["+a1+","+a2+","+a3+","+a4+"]");
	}
	
}
