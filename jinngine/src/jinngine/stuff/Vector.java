package jinngine.stuff;

import jinngine.math.Vector3;

public class Vector extends Matrix {
		
	//Basic column vector, a 1xn matrix
	public Vector(int m) {
		super(m,1);		
	}
	
	//Create a Vector from the Vector3 type 
	public Vector(Vector3 v) {
		super(3,1);
		this.elements[0][0] = v.a1;
		this.elements[1][0] = v.a2;
		this.elements[2][0] = v.a3;
	}

	//basic dot product opp
	public double dot( Vector a ) {
		double sum = 0;
		for ( int i=0; i<this.elements.length; i++ ) {
			sum += this.elements[i][0] * a.elements[i][0];
		}		
		return sum;
	}
	
//	public double dot( Vector a) {
//		// mTa
//		return (this.transpose().multiply(a)).elements[0][0];
//	}
	
}
