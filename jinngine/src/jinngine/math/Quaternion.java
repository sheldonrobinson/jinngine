package jinngine.math;


public final class Quaternion {

  public Vector3 v = new Vector3(1,0,0);
  public double s = 0.0f;

  public Quaternion() {}
  public Quaternion( double s, Vector3 v ) {
    this.s = s;
    this.v.assign(v);
  }
  
  public static Quaternion rotation( double theta, Vector3 n ) {
    return new Quaternion ( (double)Math.cos(theta/2.0f), n.multiply( (double)Math.sin(theta/2.0f) ) ); 
  }
  
  public Quaternion Multiply( Quaternion q ) {
    // q*q' = [ ss'- v*v', sv' + s'v + v x v' ] see 
    return new Quaternion( s*q.s-v.dot(q.v), 
                           q.v.multiply(s).add( v.multiply(q.s) ).add(  v.cross(q.v) ));  
  }

  public final void set( Quaternion qmark ) {
    //TODO, find some way of cleaning up the bad access to the Vector class, all over the code
    this.s = qmark.s;
    this.v.assign(qmark.v);
  }
  
  //q1 *= q2
  public static Quaternion sMultiply( Quaternion q1, Quaternion q2 ) {
    double new_s = q1.s*q2.s-q1.v.dot(q2.v);
    Vector3 new_v = q2.v.multiply(q1.s).add(q1.v.multiply(q2.s)).add(q1.v.cross(q2.v));  
    
    q1.s = new_s;
    q1.v = new_v;

    return q1;
  }

  //Same as constructor
  public final void assign( double s, Vector3 v) {
    this.s = s;
    this.v.assign(v);
  }

  public final void assign( Quaternion q1) {
	  this.s = q1.s;
	  this.v.assign(q1.v);
  }

  
  //returns a new instance representing the rotated Vector v
  public Vector3 rotate( Vector3 v ) {
    // PBA Theorem 18.42  p'= qpq* 
    // p is quaternion [0,(x,y,z)]
    // p' is the rotatet result
    // q  is the unit quaternion representing a rotation
    // q* is the conjugated q
    Quaternion vq = new Quaternion(0.0f, v);
    Quaternion rotatet = this.Multiply(vq).Multiply( this.conjugate() );
    
    return rotatet.v.copy();
  }
  
  //optimized static method, applying rotation to v
  public static final void applyRotation( Quaternion q, Vector3 v ) {
    Vector3 vtemp = new Vector3(0,0,0);
    
    double s = -Vector3.dot(q.v, v);  //scalar value of quaternion q*qv
    Vector3.multiply(v, q.s, vtemp);         //vector part of q*qv, stored in v
    Vector3.crossProduct(q.v, v, v );        
    Vector3.add(v,vtemp);
    
    //reset vtemp
    vtemp.assign(Vector3.zero);
    
    //conjugate q
    //Quaternion.conjugate(q);
    Vector3.multiply(q.v, -1);
    
    //calculate the vector part of (q*qv)*q'
    Vector3.multiplyAndAdd( q.v, s, vtemp);
    Vector3.multiplyAndAdd( v, q.s, vtemp);
    // v = v x q.v
    Vector3.crossProduct(v,q.v, v);
    
    Vector3.add( v, vtemp );  //v is now rotated    
    
    //conjugate again so q is restored
    //Quaternion.conjugate(q);
    Vector3.multiply(q.v, -1);
  }

  public Quaternion add( Quaternion q ) {
    return new Quaternion( s+q.s, v.add(q.v));
  }
  
  //q += a
  public static void add( Quaternion q, Quaternion a ) {
	  q.s += a.s;
	  Vector3.add( q.v, a.v );
  }

  public Quaternion multiply( double a ) {
    return new Quaternion( s*a, v.multiply(a) );
  }


  public double norm() {
    return (double)Math.sqrt( s*s + this.v.SquaredSum() );
  }

  public Quaternion conjugate() {
    return new Quaternion( s, v.multiply(-1) );
  }

  public static final void conjugate( Quaternion q ) {
    Vector3.multiply(q.v, -1);
  }

  //TODO change to the Matrix3 implementation
//  public void toRotationMatrix( Jama.Matrix r) {
//    r.set(0,0, 1-2*(v.a2*v.a2+v.a3*v.a3) ); r.set(1,0,2*v.a1*v.a2-2*s*v.a3);      r.set(2,0,2*s*v.a2+2*v.a1*v.a3);       r.set(3,0,0); 
//
//    r.set(0,1, 2*v.a1*v.a2+2*s*v.a3);       r.set(1,1,1-2*(v.a1*v.a1+v.a3*v.a3)); r.set(2,1,-2*s*v.a1+2*v.a2*v.a3);      r.set(3,1,0);
//
//    r.set(0,2, -2*s*v.a2+2*v.a1*v.a3);      r.set(1,2, 2*s*v.a1+2*v.a2*v.a3);     r.set(2,2, 1-2*(v.a1*v.a1+v.a2*v.a2)); r.set(3,2,0);
//
//    r.set(0,3,0);                           r.set(1,3,0);                         r.set(2,3,0);                          r.set(3,3, 1.0f );
//  }
  
  public static Matrix3 toRotationMatrix3( Quaternion q, Matrix3 R ) {
	  Vector3 v = q.v;
	  double s = q.s;
	  
	  Matrix3.set(R, 
			  1-2*(v.a2*v.a2+v.a3*v.a3), 2*v.a1*v.a2-2*s*v.a3,       2*s*v.a2+2*v.a1*v.a3, 
			  2*v.a1*v.a2+2*s*v.a3,      1-2*(v.a1*v.a1+v.a3*v.a3), -2*s*v.a1+2*v.a2*v.a3,
			  -2*s*v.a2+2*v.a1*v.a3,      2*s*v.a1+2*v.a2*v.a3,       1-2*(v.a1*v.a1+v.a2*v.a2));
	  
	  return R;
  }

  public Matrix3 rotationMatrix3() {
	  return Quaternion.toRotationMatrix3(this, new Matrix3() );
  }
  
  
  public Matrix4 rotationMatrix4() {
	  Matrix4 M = new Matrix4();
	  Vector3 v = this.v;
	  double s = this.s;
	  M.a11 = 1-2*(v.a2*v.a2+v.a3*v.a3); M.a12 =  2*v.a1*v.a2-2*s*v.a3;      M.a13 = 2*s*v.a2+2*v.a1*v.a3;  
	  M.a21 = 2*v.a1*v.a2+2*s*v.a3;      M.a22 =  1-2*(v.a1*v.a1+v.a3*v.a3); M.a23 = -2*s*v.a1+2*v.a2*v.a3;
	  M.a31 = -2*s*v.a2+2*v.a1*v.a3;     M.a32 =  2*s*v.a1+2*v.a2*v.a3;      M.a33 =  1-2*(v.a1*v.a1+v.a2*v.a2);	  
	  M.a44 = 1;	  
	  return M;
  }
  
  public void normalize() {
    double l = (double)Math.sqrt( s*s + v.a1*v.a1 + v.a2*v.a2 + v.a3*v.a3 );
    s = s/l;
    v.a1 = v.a1/l;
    v.a2 = v.a2/l;
    v.a3 = v.a3/l;
  }
  
  public Quaternion copy() {
	 return new Quaternion(this.s,this.v);
  }
  
  public final double dot(Quaternion q) {
	  return this.v.dot(q.v) + this.s * q.s;
  }
  
  public final Quaternion interpolate( Quaternion q2, double t) {
	  //seems to be slerp interpolation of quaterions [02 03 2008]
	  Quaternion qa = this;
	  Quaternion qb = q2;
	  //      qa sin((1-t) theta) + qb sin( t theta )
	  //qm = ---------------------------------------  0<t<1 
	  //                    sin theta
	  //  	  
	  //  theta = arccos( qa dot qb )
	  double theta = Math.acos(qa.dot(qb));

	  if (Math.abs(theta) < 1e-7 ) {
		  return this;
	  }
	  
	  return qa.multiply(Math.sin((1-t)*theta))
	  .add( qb.multiply( Math.sin( t*theta )))
	  .multiply( 1/Math.sin(theta));
  }

  public static final Vector3 anguarVelocity(Quaternion q0, Quaternion q1) {
	  Quaternion q0inv = new Quaternion(q0.s,q0.v.multiply(-1)).multiply(1/q0.dot(q0));
	  Quaternion r = q0inv.Multiply(q1);
	  
	  double sinomeganormhalf = r.v.norm();

	  //zero angular velocity
	  if (sinomeganormhalf < 1e-7 ) {
		  return new Vector3();
	  }
	  
	  Vector3 n = r.v.multiply(1/sinomeganormhalf);
	  
	  double omegaNorm = Math.asin(sinomeganormhalf)*2;
	  
	  return n.multiply(omegaNorm);
  }
  
  public static final Quaternion orientation( Vector3 unit) {
	  Vector3 i = Vector3.i;
	  
	  double theta = Math.acos(i.dot(unit));
	  Vector3 v = i.cross(unit);
	  
	  System.out.println("rotation axis is");
	  v.print();
	  System.out.println("and angle is " + theta );
	  
	  
	  return Quaternion.rotation(theta, v);
  }
  
  
  public void Print() {
    System.out.println( "[ "+ s );
    v.print();
    System.out.println("]");
  }
}
