/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html.
 *
 */
package jinngine.math;

import java.io.Serializable;
/**
 * <code>Vector3d</code> defines a Vector for a three double value tuple.
 * <code>Vector3d</code> can represent any three dimensional value, such as a
 * vertex or normal.
 *
 * The functional methods like add, sub, multiply that returns new instances, and
 * left <code>this</code> unchanged.
 *
 * Static methods store the resulting vector on a existing reference, which avoid
 * allowcation an can improove performances around 20% (profiling performend on vector
 * addition).
 *
 * Deprecated methods will be removed on October 2010
 *
 * @author Morten Silcowitz
 * @author Pierre Labatut 
 */
public final class Vector3 implements Serializable {
	private static final long serialVersionUID = 1L;

        /**
         * The x coordinate.
         */
	public double x;
        /**
         * The y coordinate.
         */
        public double y;
        /**
         * The z coordinate.
         */
        public double z;

        @Deprecated
	private transient final static double e = 1e-9f;
        @Deprecated
	public transient final static Vector3 epsilon = new Vector3(e,e,e) ;
        @Deprecated
        public transient final static Vector3 zero = new Vector3(0,0,0);
	@Deprecated
        public transient final static Vector3 i    = new Vector3(1,0,0);
	@Deprecated
        public transient final static Vector3 j    = new Vector3(0,1,0);
	@Deprecated
        public transient final static Vector3 k    = new Vector3(0,0,1);
        /**
         *
         * @deprecated
         */
        @Deprecated
        public transient final static Vector3 unit = new Vector3(1/Math.sqrt(3),1/Math.sqrt(3),1/Math.sqrt(3));

	/**
         * Constructs and initializes a <code>Vector3</code> to [0., 0., 0.]
         */
	public Vector3 () {
		x=0; y=0; z=0;
	}
        /**
         * Constructs and initializes a <code>Vector3</code> from the specified
         * xyz coordinates.
         * @param x the x coordinate
         * @param y the y coordinate
         * @param z the z coordinate
         */
	public Vector3( double x, double y, double z) {
		this.x=x; this.y=y; this.z=z;
	}

        /**
         * Constructs and initializes a <code>Vector3</code> with the coordinates
         * of the given <code>Vector3</code>.
         * @param v the <code>Vector3</code> containing the initialization x y z data
         * @throws NullPointerException when v is null
         */
	public Vector3( Vector3 v ) {
		x=v.x; y=v.y; z = v.z;
	}

	/**
         *
         * @param m
         * @deprecated reaplaced by public {@link Vector3#Vector3(double, double, double)}
         * @throws NullPointerException when m is null
         */
	public Vector3( double[] m ) {
		x = m[0];
		y = m[1];
		z = m[2];
	}
	/**
         * @return
         * @deprecated replaced by  {@link Vector3#Vector3(Vector3)}
         */
	public final Vector3 copy() {
		return new Vector3(this);
	}
        /**
         * Adds a provided vector to this vector creating a resultant
         * vector which is returned.
         * Neither <code>this</code> nor <code>v</code> is modified.
         *
         * @param v the vector to add to this.
         * @return resultant vector
         * @throws NullPointerException if v is null
         */
	public final Vector3 add( Vector3 v) {
		return new Vector3( x+v.x, y+v.y, z+v.z );
	}
        /**
         * @param s
         * @return
         * @deprecated not replaced
         */
	public final Vector3 add(double s) {
		return new Vector3( x+s, y+s, z+s );
	}
        /**
         * Multiply the vector coordinates by -1. creating a resultant vector
         * which is returned.
         * <code>this</code> vector is not modified.
         *
         * @return resultant vector
         * @throws NullPointerException if v is null
         */
	public final Vector3 negate() {
		return new Vector3(-x,-y,-z);
	}
	/**
         * Returns true if one of the coordinated is not a number
         * <code>this</code> vector is not modified.
         * @return true if one of the coordinated is not a number
         */
	public final boolean isNaN() {
		return Double.isNaN(x)||Double.isNaN(y)||Double.isNaN(z);
	}
        /**
         * Get a coordinate from a dimention ordinal.
         * @param i the dimention ordinal number. 1 is x, 2 is y 3 is z.
         * @return <ul>
         *<li>         x coordiante when i is 0</li>
         *<li>         y coordiante when i is 1</li>
         *<li>         z coordiante when i is 2</li>
         * </ul>
         */
	public double get( int i ) {
		return i>0?(i>1?z:y):x; 
	}
        /**
         * Set a coordinate from a dimention ordinal.
         * @param i the dimention ordinal number. 1 is x, 2 is y 3 is z.
         * @param v new coordinate value
         */
	public void set( int i, double v ) {
		if (i == 0) {
			x = v;
		} else {
			if ( i==1) {
				y=v;
			}else {
				z=v;
			}
		}
	}

	/**
         * @param v1
         * @param v2
         * @deprecated replaced by assign(Vector3)
         */
	public static final void assign( Vector3 v1, Vector3 v2) {
		v1.x = v2.x;
		v1.y = v2.y;
		v1.z = v2.z;
	} 
	
        /**
         * Add two vectors and place the result in v1.
         * <code>v2</code> is not modified.
         * @param v1 a not null reference, store the sum
         * @param v2 a not null reference
         * @throws NullPointerException if v1 or v2 is null
         */
	public static final void add( final Vector3 v1, final Vector3 v2 ) {
		v1.x += v2.x;
		v1.y += v2.y;
		v1.z += v2.z;
	}

        /**
         * Substract two vectors and place the result in v1.
         * <code>v2</code> is not modified.
         * @param v1 a not null reference, store the difference
         * @param v2 a not null reference
         * @throws NullPointerException if v1 or v2 is null
         */
	public static final void sub( final Vector3 v1, final Vector3 v2 ) {
		v1.x -= v2.x;
		v1.y -= v2.y;
		v1.z -= v2.z;
	}
	
        /**
         * Subtract two vectors, placing result in result
         * @param v1
         * @param v2
         * @param result
         * @deprecated an not replaced. See {@link Vector3#sub(Vector3, Vector3)}
         */
	public static final void sub( Vector3 v1, Vector3 v2, Vector3 result ) {
		result.x = v1.x - v2.x;
		result.y = v1.y - v2.y;
		result.z = v1.z - v2.z;
	}


        /**
         * @param v
         * @return
         * @deprecated replaces by {@link Vector3#sub( Vector3 )}
         */
	public final Vector3 minus( Vector3 v) {
		return new Vector3( x-v.x, y-v.y, z-v.z );	
        }

        /**
         * Substracts a provided vector to this vector creating a resultant
         * vector which is returned.
         * Neither <code>this</code> nor <code>v</code> is modified.
         *
         * @param v the vector to add to this.
         * @return resultant vector
         */
        public final Vector3 sub( Vector3 v ) {
		return new Vector3( x-v.x, y-v.y, z-v.z );
	}
        
        /**
         * @return
         * @deprecated replaced by v.x+v.y+x.z
         */
	public final double sum() {
		return x+y+z;
	}
	
	/**
         * @deprecated replaced by Vector3.sub(a,b)
	 * Subtract a from b and place result in a
         * @param a
         * @param b
	 */
	public static final void minus( Vector3 a, Vector3 b) {
		a.x -= b.x;
		a.y -= b.y;
		a.z -= b.z;		
	}
        /**
         * Multiply this vector by a provided scalar creating a resultant
         * vector which is returned.
         * <code>this</code> vector is not modified.
         *
         * @param s
         * @return resultant vector
         */
	public final Vector3 multiply( double s ) {
		return new Vector3( x*s, y*s, z*s);
	}
	
	/**
	 * Scale vector in scale matrix given by s
         * @deprecated replaced by Vector3.multiply
	 * @param s
	 * @return
	 */
	public final Vector3 scale( Vector3 s) {
		return new Vector3(x*s.x, y*s.y, z*s.z);
	}

        /**
         * Multiply a given vector by a scalar and place the result in v
         * @param v vector multipled
         * @param s scalar used to scale the vector
         * @throws NullPointerException if v is null
         */
	public static final void  multiply( Vector3 v, double s) {
		v.x*=s; v.y*=s; v.z*=s;
	}

        /**
         * @param v
         * @param s
         * @param result
         * @deprecated not replaced. See {@link Vector3#multiply(Vector3,double)}
         * This occurrence can be replaced by Vector3 multiply(double s)
         * Vector3 vtemp = new Vector3(0,0,0);
         * Vector3.multiply(v, q.s, vtemp);
         */
	public static final void  multiply( Vector3 v, double s, Vector3 result) {
		result.x = v.x*s; 
		result.y = v.y*s; 
		result.z = v.z*s;
	}
        /**
         *
         * @param v
         * @param s
         * @param result
         * @throws NullPointerException if v ot result is null
         */
	public static final void  multiplyAndAdd( Vector3 v, double s, Vector3 result) {
		result.x += v.x*s; 
		result.y += v.y*s; 
		result.z += v.z*s;
	}

	/**
	 * Multiply v by s, and store result in v. Add v to result and store in result
	 * @param v
	 * @param s
	 * @param result
         * @throws NullPointerException if v ot result is null
	 */
	public static final void  multiplyStoreAndAdd( Vector3 v, double s, Vector3 result) {
		v.x *= s;
		v.y *= s;
		v.z *= s;		
		result.x += v.x; 
		result.y += v.y; 
		result.z += v.z;
	}

        /**
         * Returns the dot product of this vector and vector v.
         * Neither <code>this</code> nor <code>v</code> is modified.
         * @param v the other vector
         * @return the dot product of this and v1
         * @throws NullPointerException if v is null
         */
	public final double dot( Vector3 v ) {
		return this.x*v.x+this.y*v.y+this.z*v.z;
	}
	 /**
         * Returns the dot product of this vector and vector v.
         * Neither <code>this</code> nor <code>v</code> is modified.
         * z coordinated if trucated
         * @param v the other vector
         * @return the dot product of this and v1
         * @throws NullPointerException
         */	
	public final double xydot( Vector3 v ) {
		return this.x*v.x+this.y*v.y;
	}


        /**
         * @param v1
         * @param v2
         * @return
         * @deprecated replaced by {@link Vector3#dot(Vector3) }
         */
	public static final double dot(Vector3 v1,Vector3 v2) {
		return v1.x*v2.x+v1.y*v2.y+v1.z*v2.z;
	}

	/**
         * Return a new new set to the cross product of this vectors and v
         * Neither <code>this</code> nor <code>v</code> is modified.
         * @param v a not null vector
         * @return the cross product
         * @throws NullPointerException when v is null
         */
	public final Vector3 cross( final Vector3 v ) {
		return new Vector3( y*v.z-z*v.y, z*v.x-x*v.z, x*v.y-y*v.x ); 
	}
       /**
        * Sets result vector to the vector cross product of vectors v1 and v2.
        * Neither <code>v1</code> nor <code>v2</code> is modified.
        * @param v1 the first vector
        * @param v2 the second vector
        * @param result
        */
	public static final void crossProduct( final Vector3 v1, final Vector3 v2, final Vector3 result ) {
		final double tempa1 = v1.y*v2.z-v1.z*v2.y;
		final double tempa2 = v1.z*v2.x-v1.x*v2.z;
		final double tempa3 = v1.x*v2.y-v1.y*v2.x;

		result.x = tempa1;
		result.y = tempa2;
		result.z = tempa3;
	}

        /**
         * Return a new vector set to the normalization of vector v1.
         * <code>this</code> vector is not modified.
         * @return the normalized vector
         */
	public final Vector3 normalize() {
		double l = Math.sqrt(x*x+y*y+z*z);
		if ( l == 0.0 ) {return new Vector3(1,0,0); }
                l=1./l;
		return new Vector3( x*l, y*l, z*l);
	}
        /**
         * Sets the value of this <code>Vector3</code> to the specified x, y and  coordinates.
         * @param x the x coordinate
         * @param y the y coordinate
         * @param z the z coordinate
         * @return return this
         */
        public final Vector3 assign( double x, double y, double z ) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
        /**
         * A this vector to the provided coordinates creating a new resultant vector.
         * <code>this</code> vector is not modified
         * @param x the x coordinate
         * @param y the y coordinate
         * @param z the z coordinate
         * @return the result vector
         */
	public final Vector3 add( double x, double y, double z ) {
		return new Vector3( this.x+x, this.y+y, this.z+z);
	}

	/**
         * Sets the value of this vector to the value of the xyz coordinates of the
         * given vector.
         * <code>v</code> is not modified
         * @param v the vector to be copied
         * @return <code>this</code>
         * @throws NullPointerException
         */
	public final Vector3 assign( Vector3 v ) {
		double t1 =v.x;
		double t2 =v.y;
		double t3 =v.z;
		x = t1;
		y = t2;
		z = t3;
		return this;
	}
        /**
         *
         * @return
         */
	public final Vector3 assignZero() {
		x = 0;
		y = 0;
		z = 0;
		return this;
	}
 
        /**
         * Returns the length of this vector.
         * <code>this</code> vector is not modified.
         * @return Returns the length of this vector.
         */
	public final double norm() {
		return Math.sqrt( x*x + y*y + z*z );
	}
	/**
         * Returns the length of this vector.
         * z coordinate is truncated.
         * <code>this</code> vector is not modified.
         * @return Double.NaN when Double.isNaN(x) || Double.isNaN(y)
         */
	public final double xynorm() {
		return Math.sqrt( x*x + y*y );
	}

        /**
         * @return
         * @deprecated not replaced
         */
	public final double infnorm() {
		return Math.abs( x>y?x>z?x:z:y>z?y:z); 
	}

        /**
         * Returns the length of this vector.
         * <code>this</code> vector is not modified.
         * @return the length of this vector
         */
	public final double squaredNorm() {
		return x*x+y*y+z*z;
	}

        /**
         * @return
         * @deprecated not replaced
         */
	public final double[][] crossProductMatrix() {
		double [][] m = new double[3][3];

		m[0][0] =   0;  m[0][1]= -z;  m[0][2]=  y;
		m[1][0] =  z;  m[1][1]=   0;  m[1][2]= -x;
		m[2][0] = -y;  m[2][1]=  x;  m[2][2]=   0;

		return m;
	}

        /**
         * @param A
         * @return
         * @deprecated not replaced
         */
	public final Matrix3 crossProductMatrix(Matrix3 A) {
		Matrix3.set( A,  0,-z,  y,
				z,  0, -x,
				-y, x,   0 );

		return A;
	}

        /**
         * @return
         * @deprecated replaced by Matrix3.crossProduct
         */
	public final Matrix3 crossProductMatrix3() {
		Matrix3 A = new Matrix3();

		Matrix3.set( A,  0,-z,  y,
				z,  0, -x,
				-y, x,   0 );

		return A;
	}

        /**
         * @return
         * @deprecated not repalced
         */
	public final double[][] transposeMatrix() {
		double [][] m = new double[3][1];

		m[0][0] = x;
		m[1][0] = y;
		m[2][0] = z;

		return m;
	}
        /**
         * @return
         * @deprecated not repalced
         */
	public final double[][] Matrix() {
		double [][] m = new double[1][3];

		m[0][0] = x;  m[0][1]= y;  m[0][2]= z;

		return m;
	}

        /**
         * @param v 
         * @return
         * @deprecated not replaced
         * the only occurence where it is used should migrate to isEpsilon(double)
         */
	public final boolean lessThan(Vector3 v) {
		return (x<v.x)&&(y<v.y)&&(z<v.z);
	}
        /**
         * @param v
         * @return
         * @deprecated not replaced
         */
	public final boolean weaklyLessThan(Vector3 v) {
		return (x<=v.x)&&(y<=v.y)&&(z<=v.z);
	}
        /**
         * @return
         * @deprecated not replaced. Try equals(new Vector())
         */
	public final boolean isZero() {
		return x==0&&y==0&&z==0;
	}
        /**
         * @deprecated not replaced
         * @return x>=0&&y>=0&&z>=0;
         */
        public final boolean isWeaklyGreaterThanZero() {
		return x>=0&&y>=0&&z>=0;
	}
        /**
         * @deprecated not replaced
         * @return new Vector3( Math.abs(x), Math.abs(y), Math.abs(z) )
         */
	public final Vector3 abs() {
		return new Vector3( Math.abs(x), Math.abs(y), Math.abs(z) );
	}
        /**
         * @deprecated replaced by System.out.println( vect );
         */
	public final void print() {
		System.out.println( "[" + x + "," +y+ "," +z + "]" );
	}

        /**
         * @param epsilon
         * @return
         * @deprecated not replaced
         */
	public final Vector3 cutOff(double epsilon) {
		double b1=Math.abs(x)<epsilon?0:x;
		double b2=Math.abs(y)<epsilon?0:y;
		double b3=Math.abs(z)<epsilon?0:z;
		return new Vector3(b1,b2,b3);
	}
        /**
         * Pack the three coorindates into a new double array
         * <code>this</code> vector is not modified.
         * @return a array set with x, y and z
         */
	public final double[] pack() {
		return new double[]{x,y,z};
	}

        /**
         * @param v
         * @return
         * @deprecated replaced by {@link Vector3#pack()}
         */
	public final static double[] pack(Vector3 v) {
		return new double[]{v.x,v.y,v.z};
	}

        /**
         * Returns a string representation of this vector.  The string
         * representation consists of the three dimentions in the order x, y, z,
         * enclosed in square brackets (<tt>"[]"</tt>). Adjacent elements are
         * separated by the characters <tt>", "</tt> (comma and space).
         * Elements are converted to strings as by {@link Double#toString(double)}.
         *
         * @return a string representation of this vector
         */
	@Override
	public final String toString() {
		return  "[" + x + ", " +y+ ", " +z + "]";
	}
}
