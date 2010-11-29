/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.math;

//3x3 matrix for optimized matrix ops


/**
 * A 3x3 matrix implementation
 */
public class Matrix3 {
  public double a11, a12, a13;
  public double a21, a22, a23;
  public double a31, a32, a33;
  
  public Matrix3() {
      a11=0; a12=0; a13=0;
      a21=0; a22=0; a23=0;
      a31=0; a32=0; a33=0;
  }

   /**
    * Assign the zero matrix to this matrix
    * @return <code>this</code>
    */
  public final Matrix3 assignZero() {
	  a11 = 0; a12 = 0; a13 = 0;
	  a21 = 0; a22 = 0; a23 = 0;
	  a31 = 0; a32 = 0; a33 = 0;
          return this;
  }
  
  /**
   * Construct a 3x3 matrix using specified fields
   * @param a11
   * @param a12
   * @param a13
   * @param a21
   * @param a22
   * @param a23
   * @param a31
   * @param a32
   * @param a33
   */
  public Matrix3(double a11, double a12, double a13, double a21, double a22,
		double a23, double a31, double a32, double a33) {	
	this.a11 = a11;
	this.a12 = a12;
	this.a13 = a13;
	this.a21 = a21;
	this.a22 = a22;
	this.a23 = a23;
	this.a31 = a31;
	this.a32 = a32;
	this.a33 = a33;
}

 /**
  * create a 3x3 matrix using a set of basis vectors
  * @param e1
  * @param e2
  * @param e3
  */
  public Matrix3( Vector3 e1, Vector3 e2, Vector3 e3) {
    a11 = e1.x;
    a21 = e1.y;
    a31 = e1.z;
    
    a12 = e2.x;
    a22 = e2.y;
    a32 = e2.z;
    
    a13 = e3.x;
    a23 = e3.y;
    a33 = e3.z;
  }
  
  /**
   * Construct a new 3x3 matrix as a copy of the given matrix B
   * @param B
   * @throws NullPointerException
   */
  public Matrix3( Matrix3 B) {
	 assign(B);
  }

   /**
   * assign the value of B to this Matrix3
   * @param B
   */
  public final Matrix3 assign(Matrix3 B) {
	  a11 = B.a11; a12 = B.a12; a13 = B.a13;
	  a21 = B.a21; a22 = B.a22; a23 = B.a23;
	  a31 = B.a31; a32 = B.a32; a33 = B.a33;
	  return this;
  }
  
  /**
   * Assign the matrix with the 3 column vectors b1, b2, and b3
   */
  public final Matrix3 assign(final Vector3 b1, final Vector3 b2, final Vector3 b3) {
	  a11 = b1.x; a12 = b2.x; a13 = b3.x;
	  a21 = b1.y; a22 = b2.y; a23 = b3.y;
	  a31 = b1.z; a32 = b2.z; a33 = b3.z;
	  return this;
  }

  public Matrix3( Matrix4 B) {
	  a11 = B.a11; a12 = B.a12; a13 = B.a13;
	  a21 = B.a21; a22 = B.a22; a23 = B.a23;
	  a31 = B.a31; a32 = B.a32; a33 = B.a33;
  } 
  
  /**
   * Assign the scale matrix given by s, to this matrix
   */
  public final Matrix3 assignScale(final double s) {
	  a11 = s; a12 = 0; a13 = 0;
	  a21 = 0; a22 = s; a23 = 0;
	  a31 = 0; a32 = 0; a33 = s;
          return this;
  }

  /**
   * Assign the non-uniform scale matrix given by s1, s2 and s3, to this matrix
   */
  public final Matrix3 assignScale(double sx, double sy, double sz) {
	  a11 = sx; a12 = 0.; a13 = 0.;
	  a21 = 0.; a22 = sy; a23 = 0.;
	  a31 = 0.; a32 = 0.; a33 = sz;
	  return this;
  }

  /**
   * Assign the non-uniform scale matrix given by s1, s2 and s3, to this matrix
   */
  public final Matrix3 assignScale(Vector3 s) {
	  a11 = s.x; a12 = 0.; a13 = 0.;
	  a21 = 0.; a22 = s.y; a23 = 0.;
	  a31 = 0.; a32 = 0.; a33 = s.z;
	  return this;
  }

  
  /**
   * Assign the identity matrix to this matrix
   */
  public final Matrix3 assignIdentity() {
	  a11 = 1; a12 = 0; a13 = 0;
	  a21 = 0; a22 = 1; a23 = 0;
	  a31 = 0; a32 = 0; a33 = 1;
          return this;
  }

    public Matrix3 assign(
            double a11, double a12, double a13,
            double a21, double a22, double a23,
            double a31, double a32, double a33) {
        this.a11 = a11;	this.a12 = a12;	this.a13 = a13;
	this.a21 = a21;	this.a22 = a22;	this.a23 = a23;
	this.a31 = a31;	this.a32 = a32;	this.a33 = a33;
        return this;
    }
  /**
   * Get the n'th column vector of this matrix
   * @param n
   * @return
   * @throws IllegalArgumentException
   */
  public final Vector3 column(int n) {
	  switch (n) {
	  case 0:
		  return new Vector3(a11,a21,a31);
	  case 1:
		  return new Vector3(a12,a22,a32);
	  case 2:
		  return new Vector3(a13,a23,a33);
              default:
                  throw new IllegalArgumentException();
	  }	
  }
  
  /**
   * Get the n'th row vector of this matrix
   * @param n
   * @return
   */
  public Vector3 row(int n) {
	  switch (n) {
	  case 0:
		  return new Vector3(a11,a12,a13);
	  case 1:
		  return new Vector3(a21,a22,a23);
	  case 2:
		  return new Vector3(a31,a32,a33);
              default:
                  throw new IllegalArgumentException();
	  }	
  }

  /** 
   * Get the diagonal of this matrix
   */
  public Vector3 diag() {
	  return new Vector3(a11,a22,a33);
  }
  
  /**
   * Get all column vectors of this matrix
   * @param c1
   * @param c2
   * @param c3
   */
  public void getColumnVectors( Vector3 c1, Vector3 c2, Vector3 c3) {
	  c1.x = a11;
	  c1.y = a21;
	  c1.z = a31;

	  c2.x = a12;
	  c2.y = a22;
	  c2.z = a32;
	  
	  c3.x = a13;
	  c3.y = a23;
	  c3.z = a33;
  }
  
  /**
   * Get all row vectors of this matrix
   * @param r1
   * @param r2
   * @param r3
   */
  public void getRowVectors( Vector3 r1, Vector3 r2, Vector3 r3) {
	  r1.x = a11;
	  r1.y = a12;
	  r1.z = a13;

	  r2.x = a21;
	  r2.y = a22;
	  r2.z = a23;
	  
	  r3.x = a31;
	  r3.y = a32;
	  r3.z = a33;
  }
    
  /**
   * Return a new identity Matrix3 instance
   * @return
   */
  public static Matrix3 identity() {
	  return new Matrix3().assignIdentity();
  }
  
  /**
   * Return a new matrix that is multiplied by the scalar -1
   */
  public final Matrix3 negate() {
	  return new Matrix3( -this.a11, -this.a12, -this.a13, 
			  -this.a21, -this.a22, -this.a23,
			  -this.a31, -this.a32, -this.a33 ); 
  }
  
  /**
   * Multiply this matrix by a scalar, return the resulting matrix
   * @param s
   * @return
   */
  public final Matrix3 multiply( double s) {
	  Matrix3 A = new Matrix3();
	  A.a11 = a11*s; A.a12 = a12*s; A.a13 = a13*s;
	  A.a21 = a21*s; A.a22 = a22*s; A.a23 = a23*s;
	  A.a31 = a31*s; A.a32 = a32*s; A.a33 = a33*s;	  
	  return A;
  }
  
  /**
   * Right-multiply by a scaling matrix given by s, so M.scale(s) = M S(s)
   * @param s
   * @return
   */
  public final Matrix3 scale( Vector3 s ) {
	  Matrix3 A = new Matrix3();
	  A.a11 = a11*s.x; A.a12 = a12*s.y; A.a13 = a13*s.z;
	  A.a21 = a21*s.x; A.a22 = a22*s.y; A.a23 = a23*s.z;
	  A.a31 = a31*s.x; A.a32 = a32*s.y; A.a33 = a33*s.z;	  
	  return A;
  }
  
  /**
   * Multiply this matrix by the matrix A and return the result
   * @param A
   * @return
   */
  public Matrix3 multiply(Matrix3 A) {
	  return multiply(this,A,new Matrix3());
  }

    /**
   * Multiply this matrix by the matrix A and return the result
   * @param A
   * @return
   */
  public Matrix3 assignMultiply(Matrix3 A) {
	  return multiply(this,A,this);
  }
  
  
  /**
   * Multiply by scalar s and store in this matrix.
   * @param s scalar
   * @return this matrix
   */
  public Matrix3 assignMultiply( double s) {
	  a11*=s; a12*=s; a13*=s;
	  a21*=s; a22*=s; a23*=s;
	  a31*=s; a32*=s; a33*=s;	  
	  return this;
  }
  
  //C = AxB 
  public static Matrix3 multiply( final Matrix3 A, final Matrix3 B, final Matrix3 C ) {
    //               B | b11 b12 b13
    //                 | b21 b22 b23
    //                 | b31 b32 b33
    //     -------------------------
    //  A  a11 a12 a13 | c11 c12 c13
    //     a21 a22 a23 | c21 c22 c23
    //     a31 a32 a33 | c31 c32 c33  C
    
    double t11 = A.a11*B.a11 + A.a12*B.a21 + A.a13*B.a31;
    double t12 = A.a11*B.a12 + A.a12*B.a22 + A.a13*B.a32;
    double t13 = A.a11*B.a13 + A.a12*B.a23 + A.a13*B.a33;
    
    double t21 = A.a21*B.a11 + A.a22*B.a21 + A.a23*B.a31;
    double t22 = A.a21*B.a12 + A.a22*B.a22 + A.a23*B.a32;
    double t23 = A.a21*B.a13 + A.a22*B.a23 + A.a23*B.a33;
    
    double t31 = A.a31*B.a11 + A.a32*B.a21 + A.a33*B.a31;
    double t32 = A.a31*B.a12 + A.a32*B.a22 + A.a33*B.a32;
    double t33 = A.a31*B.a13 + A.a32*B.a23 + A.a33*B.a33;

    //copy to C
    C.a11 = t11;
    C.a12 = t12;
    C.a13 = t13;

    C.a21 = t21;
    C.a22 = t22;
    C.a23 = t23;

    C.a31 = t31;
    C.a32 = t32;
    C.a33 = t33;

    return C;
  }
  
  //functional
  /**
   * Multiply a vector by this matrix, return the resulting vector
   */
  public final Vector3 multiply( final Vector3 v) {
	  Vector3 r = new Vector3();
	  Matrix3.multiply(this, v, r);
	  return r;
  }
  
  
  //A = A^T 
  public Matrix3 assignTranspose() {
    double t;
	t=a12; a12=a21; a21=t;
	t=a13; a13=a31; a31=t;
	t=a23; a23=a32; a32=t;
    return this;
  }
  
  /**
   * Functional method. Transpose this matrix and return the result
   * @return
   */
  public final Matrix3 transpose() {
	 return new Matrix3(this).assignTranspose();
  }


  //C = A-B
  public static Matrix3 subtract( final Matrix3 A, final Matrix3 B, final Matrix3 C ) {
    C.a11 = A.a11-B.a11; C.a12 = A.a12-B.a12; C.a13 = A.a13-B.a13;
    C.a21 = A.a21-B.a21; C.a22 = A.a22-B.a22; C.a23 = A.a23-B.a23;
    C.a31 = A.a31-B.a31; C.a32 = A.a32-B.a32; C.a33 = A.a33-B.a33;
    return C;
  }
 /**
   * Substract to this matrix the matrix B, return result in a new matrix instance
   * @param B
   * @return
   */
  public Matrix3 subtract( Matrix3 B ) {
	  return subtract(this,B,new Matrix3());
  }
  /**
   * Substract to this matrix the matrix B, return result in a new matrix instance
   * @param B
   * @return
   */
  public Matrix3 assignSubtract( Matrix3 B ) {
	  return subtract(this,B,this);
  }
  /**
   * Add to this matrix the matrix B, return result in a new matrix instance
   * @param B
   * @return
   */
  public Matrix3 add( Matrix3 B ) {
	  return add(this,B,new Matrix3());
  }
  /**
   * Add to this matrix the matrix B, return result in a new matrix instance
   * @param B
   * @return
   */
  public Matrix3 assignAdd( Matrix3 B ) {
	  return add(this,B,this);
  }
  
  //C = A+B
  public static Matrix3 add( final Matrix3 A, final Matrix3 B, final Matrix3 C ) {
    C.a11 = A.a11+B.a11; C.a12 = A.a12+B.a12; C.a13 = A.a13+B.a13;
    C.a21 = A.a21+B.a21; C.a22 = A.a22+B.a22; C.a23 = A.a23+B.a23;
    C.a31 = A.a31+B.a31; C.a32 = A.a32+B.a32; C.a33 = A.a33+B.a33;
    return C;
  }
  
  // rT = (vT)A   NOTE that the result of this is actually a transposed vector!! 
  public static Vector3 transposeVectorAndMultiply( final Vector3 v, final Matrix3 A, final Vector3 r ){
    //            A  | a11 a12 a13
    //               | a21 a22 a23
    //               | a31 a32 a33
    //      ----------------------
    // vT   v1 v2 v3 |  c1  c2  c3
    
    double t1 = v.x*A.a11+v.y*A.a21+v.z*A.a31;
    double t2 = v.x*A.a12+v.y*A.a22+v.z*A.a32;
    double t3 = v.x*A.a13+v.y*A.a23+v.z*A.a33;
    
    r.x = t1;
    r.y = t2;
    r.z = t3;

    return r;
  }

  /**
   * Multiply v by A, and place result in r, so r = Av
   * @param A 3 by 3 matrix
   * @param v Vector to be multiplied
   * @param r Vector to hold result of multiplication
   * @return Reference to the given Vector3 r instance
   */
  public static Vector3 multiply( final Matrix3 A, final Vector3 v, final Vector3 r ) {
    //                   
    //               V | v1
    //                 | v2
    //                 | v3                     
    //     -----------------
    //  A  a11 a12 a13 | c1
    //     a21 a22 a23 | c2
    //     a31 a32 a33 | c3   
    
    double t1 = v.x*A.a11+v.y*A.a12+v.z*A.a13;
    double t2 = v.x*A.a21+v.y*A.a22+v.z*A.a23;
    double t3 = v.x*A.a31+v.y*A.a32+v.z*A.a33;
    
    r.x = t1;
    r.y = t2;
    r.z = t3;
    
    return r;
  }  
  
  /**
   * Multiply v by A^T, and place result in r, so r = A^Tv
   * @param A 3 by 3 matrix
   * @param v Vector to be multiplied
   * @param r Vector to hold result of multiplication
   * @return Reference to the given Vector3 r instance
   */
  public static Vector3 multiplyTransposed( final Matrix3 A, final Vector3 v, final Vector3 r ) {
    //                   
    //               V | v1
    //                 | v2
    //                 | v3                     
    //     -----------------
    // A^T a11 a21 a31 | c1
    //     a12 a22 a32 | c2
    //     a13 a23 a33 | c3   
    
    double t1 = v.x*A.a11+v.y*A.a21+v.z*A.a31;
    double t2 = v.x*A.a12+v.y*A.a22+v.z*A.a32;
    double t3 = v.x*A.a13+v.y*A.a23+v.z*A.a33;
    
    r.x = t1;
    r.y = t2;
    r.z = t3;
    
    return r;
  } 

  /**
   * Compute the determinant of Matrix3 A
   * @param A
   * @return 
   */
  public double determinant() {
	  return a11*a22*a33- a11*a23*a32 + a21*a32*a13 - a21*a12*a33 + a31*a12*a23-a31*a22*a13;
  }
  
/**
 * Compute the inverse of the matrix A, place the result in C
 */
  public static Matrix3 inverse( final Matrix3 A, final Matrix3 C ) {
    double d = (A.a31*A.a12*A.a23-A.a31*A.a13*A.a22-A.a21*A.a12*A.a33+A.a21*A.a13*A.a32+A.a11*A.a22*A.a33-A.a11*A.a23*A.a32);
    double t11 =  (A.a22*A.a33-A.a23*A.a32)/d;
    double t12 = -(A.a12*A.a33-A.a13*A.a32)/d;
    double t13 =  (A.a12*A.a23-A.a13*A.a22)/d;
    double t21 = -(-A.a31*A.a23+A.a21*A.a33)/d;
    double t22 =  (-A.a31*A.a13+A.a11*A.a33)/d;
    double t23 = -(-A.a21*A.a13+A.a11*A.a23)/d;
    double t31 =  (-A.a31*A.a22+A.a21*A.a32)/d;
    double t32 = -(-A.a31*A.a12+A.a11*A.a32)/d;
    double t33 =  (-A.a21*A.a12+A.a11*A.a22)/d;

    C.a11 = t11; C.a12 = t12; C.a13 = t13;
    C.a21 = t21; C.a22 = t22; C.a23 = t23;
    C.a31 = t31; C.a32 = t32; C.a33 = t33;
    return C;
  }

  public final Matrix3 assignInverse() {
      return inverse(this,this);
  }
  public final Matrix3 inverse() {
        return inverse(this,new Matrix3());
  }

  public static Matrix3 scaleMatrix( double xs, double ys, double zs) {
      return new Matrix3().assignScale(xs,ys,zs);
  }
  
  public static Matrix3 scaleMatrix( double s ) {
      return new Matrix3().assignScale(s);      
  }
     
  @Override
  public String toString() {
    return "["+a11+", " + a12 + ", " + a13 + "]\n"
         + "["+a21+", " + a22 + ", " + a23 + "]\n"
         + "["+a31+", " + a32 + ", " + a33 + "]" ;
  }
  
  /**
   * Return this matrix as a formated string, that can be 
   * directly used in a MATLAB script.
   */
  public final String toMatlabString() {
	    return "[" +a11 +" "+a12+ " " +a13+ "; "
	    +a21+" " + a22 + " " + a23 + "; "
        + a31+" " + a32 + " " + a33 + "]" ;
  }
  
  /**
   * Check matrix for NaN values 
   */
  public final boolean isNaN() {
	  return Double.isNaN(a11)||Double.isNaN(a12)||Double.isNaN(a13)
	  || Double.isNaN(a21)||Double.isNaN(a22)||Double.isNaN(a23)
	  || Double.isNaN(a31)||Double.isNaN(a32)||Double.isNaN(a33);
  }
  
  public double[] toArray() {
	     return new double[]{
                  a11, a21, a31,
                  a12, a22, a32,
                  a13, a23, a33};
  }

  /**
   * Return the Frobenius norm of this Matrix3
   * @return
   */
  public final double fnorm() {
	  return Math.sqrt(	a11*a11 + a12*a12 + a13*a13  + a21*a21 + a22*a22  + a23*a23  + a31*a31 + a32*a32 + a33*a33 ); 
  }
    /**
     * Return the cross product matrix of the vector a, such that M b = a x b.
     */
    public static Matrix3 cross(Vector3 a) {
        return new Matrix3(
                0., -a.z, a.y,
                a.z, 0., -a.x,
                -a.y, a.x, 0.);
    }
}
