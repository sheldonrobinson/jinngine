package jinngine.math;

public final class  Vector3 {
  public double a1,a2,a3;
  private final static double e = 1e-9f;
  private final static double[]  epsilonVector = {e,e,e};
  public final static Vector3 epsilon = new Vector3(epsilonVector) ;

  public final static Vector3 zero = new Vector3(0,0,0);
  public final static Vector3 i    = new Vector3(1,0,0);
  public final static Vector3 j    = new Vector3(0,1,0);
  public final static Vector3 k    = new Vector3(0,0,1);

  public Vector3 () {
    a1=0; a2=0; a3=0;
  }
  
  public Vector3( double a, double b, double c) {
    a1=a; a2=b; a3=c;
  }
  
  public Vector3( Vector3 v ) {
    a1=v.a1; a2=v.a2; a3 = v.a3;
  }
  
  //obtain vector from double array
  public Vector3( double[] m ) {
    a1 = m[0];
    a2 = m[1];
    a3 = m[2];
  }

  public final Vector3 copy() {
    return new Vector3(this);
  }
  
  public final Vector3 add( Vector3 v) {
    return new Vector3( a1+v.a1, a2+v.a2, a3+v.a3 );
  }
  
  public final Vector3 add(double s) {
    return new Vector3( a1+s, a2+s, a3+s );
  }
  
  public double get( int i ) {
    return i>0?(i>1?a3:a2):a1; 
  }

  public void set( int i, double v ) {
	    if (i == 0) {
	    	a1 = v;
	    } else {
	    	if ( i==1) {
	    		a2=v;
	    	}else {
	    		a3=v;
	    	}
	    }
  }

  //set
  public static final void assign( Vector3 v1, Vector3 v2) {
	  v1.a1 = v2.a1;
	  v1.a2 = v2.a2;
	  v1.a3 = v2.a3;
  } 
  
  //add two vectors, placing result in v1
  public static final void add( Vector3 v1, Vector3 v2 ) {
    v1.a1 += v2.a1;
    v1.a2 += v2.a2;
    v1.a3 += v2.a3;
  }

  //subtract two vectors, placing result in v1
  public static final void sub( Vector3 v1, Vector3 v2 ) {
    v1.a1 -= v2.a1;
    v1.a2 -= v2.a2;
    v1.a3 -= v2.a3;
  }

  //subtract two vectors, placing result in result
  public static final void sub( Vector3 v1, Vector3 v2, Vector3 result ) {
    result.a1 = v1.a1 - v2.a1;
    result.a2 = v1.a2 - v2.a2;
    result.a3 = v1.a3 - v2.a3;
  }
  
  public final Vector3 minus( Vector3 v) {
    return new Vector3( a1-v.a1, a2-v.a2, a3-v.a3 );
  }

  public final Vector3 multiply( double s ) {
    return new Vector3( a1*s, a2*s, a3*s);
  }
  
  public static final void  multiply( Vector3 v, double s) {
    v.a1*=s; v.a2*=s; v.a3*=s;
  }

  public static final void  multiply( Vector3 v, double s, Vector3 result) {
    result.a1 = v.a1*s; 
    result.a2 = v.a2*s; 
    result.a3 = v.a3*s;
  }

  public static final void  multiplyAndAdd( Vector3 v, double s, Vector3 result) {
    result.a1 += v.a1*s; 
    result.a2 += v.a2*s; 
    result.a3 += v.a3*s;
  }


  public final double dot( Vector3 v ) {
	  return this.a1*v.a1+this.a2*v.a2+this.a3*v.a3;
  }
  
  public static final double dot(Vector3 v1,Vector3 v2) {
    return v1.a1*v2.a1+v1.a2*v2.a2+v1.a3*v2.a3;
  }
  
  //Vector3    operator% ( Vector3 const & v ) const {  return Vector3(y*v(2)-v(1)*z, v(0)*z-x*v(2), x*v(1)-v(0)*y);  }
  public final Vector3 cross( Vector3 v ) {
    return new Vector3( a2*v.a3-a3*v.a2, a3*v.a1-a1*v.a3, a1*v.a2-a2*v.a1 ); 
  }
  
  public static final void crossProduct( Vector3 v1, Vector3 v2, Vector3 result ) {
    double tempa1 = v1.a2*v2.a3-v1.a3*v2.a2;
    double tempa2 = v1.a3*v2.a1-v1.a1*v2.a3;
    double tempa3 = v1.a1*v2.a2-v1.a2*v2.a1; 
    
    result.a1 = tempa1;
    result.a2 = tempa2;
    result.a3 = tempa3;
  }
  
  public final Vector3 normalize() {
    double l = (double)Math.sqrt(a1*a1+a2*a2+a3*a3);
    if ( l == 0.0 ) {/* System.err.println("Division by zero");*/ /*Thread.dumpStack(); System.exit(-1);*/ return new Vector3(1,0,0); } 
    return new Vector3( a1/l, a2/l, a3/l);
  }
  

  public Vector3 assign( Vector3 v ) {
    double t1 =v.a1;
    double t2 =v.a2;
    double t3 =v.a3;
    a1 = t1;
    a2 = t2;
    a3 = t3;
    return this;
  }
  
  public Vector3 assignZero() {
	    a1 = 0;
	    a2 = 0;
	    a3 = 0;
	    return this;
	  }

  
  public boolean equals( Vector3 v ) {
    return (a1==v.a1 && a2==v.a2 && a3==v.a3 );
  }

  public double norm() {
    return (double)Math.sqrt( a1*a1 + a2*a2 + a3*a3 );
  }

  public double infnorm() {
	    return Math.abs( a1>a2?a1>a3?a1:a3:a2>a3?a2:a3); 
  }

  
  public double SquaredSum() {
    return a1*a1+a2*a2+a3*a3;
  }

  public double[][] crossProductMatrix() {
    double [][] m = new double[3][3];
    
    m[0][0] =   0;  m[0][1]= -a3;  m[0][2]=  a2;
    m[1][0] =  a3;  m[1][1]=   0;  m[1][2]= -a1;
    m[2][0] = -a2;  m[2][1]=  a1;  m[2][2]=   0;
    
    return m;
  }

  public Matrix3 crossProductMatrix(Matrix3 A) {
    Matrix3.set( A,  0,-a3,  a2,
                    a3,  0, -a1,
                   -a2, a1,   0 );
    
    return A;
  }
  
  public Matrix3 crossProductMatrix3() {
	  Matrix3 A = new Matrix3();
	  
	    Matrix3.set( A,  0,-a3,  a2,
	                    a3,  0, -a1,
	                   -a2, a1,   0 );
	    
	    return A;
	  }

  public double[][] transposeMatrix() {
    double [][] m = new double[3][1];
    
    m[0][0] = a1;
    m[1][0] = a2;
    m[2][0] = a3;
    
    return m;
  }
  
  public double[][] Matrix() {
    double [][] m = new double[1][3];
    
    m[0][0] = a1;  m[0][1]= a2;  m[0][2]= a3;
    
    return m;
  }
  
  public boolean lessThan(Vector3 v) {
    return (a1<v.a1)&&(a2<v.a2)&&(a3<v.a3);
  }

  public boolean weaklyLessThan(Vector3 v) {
	    return (a1<=v.a1)&&(a2<=v.a2)&&(a3<=v.a3);
	  }

  
  public boolean isZero() {
	  return a1==0&&a2==0&&a3==0;
  }
  
  public final boolean isWeaklyGreaterThanZero() {
	  return a1>=0&&a2>=0&&a3>=0;
  }
  
  public Vector3 abs() {
    return new Vector3( (double)Math.abs(a1), (double)Math.abs(a2), (double)Math.abs(a3) );
  }
  
  public void print() {
    System.out.println( "[" + a1 + "," +a2+ "," +a3 + "]" );
  }
  
 public Vector3 cutOff() {
	 double b1=Math.abs(a1)<1e-6?0:a1;
	 double b2=Math.abs(a2)<1e-6?0:a2;
	 double b3=Math.abs(a3)<1e-6?0:a3;
	 return new Vector3(b1,b2,b3);
 }
 
 public double[] pack() {
	 return new double[]{a1,a2,a3};
 }
 
}
