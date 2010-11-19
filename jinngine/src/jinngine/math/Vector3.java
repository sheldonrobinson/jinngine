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
import java.util.Collection;
/**
 * <code>Vector3d</code> defines a Vector for a three double value tuple.
 * <code>Vector3d</code> can represent any three dimensional value, such as a
 * vertex or normal.
 *
 * The functional methods like add, sub, multiply that returns new instances, and
 * left <code>this</code> unchanged.
 *
 * Static methods store the resulting vector on a existing reference, which avoid
 * allocation an can improve performances around 20% (profiling performed on vector
 * addition).
 *
 * Depreciated methods will be removed on October 2010
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


//	public transient final static double e = 1e-9f;

	/**
	 * Constructs and initialises a <code>Vector3</code> to [0., 0., 0.]
	 */
	public Vector3 () {
		x=0; y=0; z=0;
	}
	/**
	 * Constructs and initialises a <code>Vector3</code> from the specified
	 * xyz coordinates.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
	public Vector3( double x, double y, double z) {
		this.x=x; this.y=y; this.z=z;
	}

	/**
	 * Constructs and initialises a <code>Vector3</code> with the coordinates
	 * of the given <code>Vector3</code>.
	 * @param v the <code>Vector3</code> containing the initialisation x y z data
	 * @throws NullPointerException when v is null
	 */
	public Vector3( Vector3 v ) {
		x=v.x; y=v.y; z = v.z;
	}

	/**
	 * Create a new unit vector heading positive x
	 * @return a new unit vector heading positive x
	 */
	public static Vector3 i() {
		return new Vector3(1., 0., 0.);
	}
	/**
	 * Create a new unit vector heading positive y
	 * @return a new unit vector heading positive y
	 */
	public static Vector3 j() {
		return new Vector3(0., 1., 0.);
	}
	/**
	 * Create a new unit vector heading positive z
	 * @return a new unit vector heading positive z
	 */
	public static Vector3 k() {
		return new Vector3(0., 0., 1.);
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
	 * Get a coordinate from a dimension ordinal.
	 * @param i the dimension ordinal number. 1 is x, 2 is y 3 is z.
	 * @return <ul>
	 *<li>         x coordinate when i is 0</li>
	 *<li>         y coordinate when i is 1</li>
	 *<li>         z coordinate when i is 2</li>
	 * </ul>
	 */
	public double get( int i ) {
		return i>0?(i>1?z:y):x; 
	}
	/**
	 * Set a coordinate from a dimension ordinal.
	 * @param i the dimension ordinal number. 1 is x, 2 is y 3 is z.
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
	 * Add two vectors and place the result in v1.
	 * <code>v2</code> is not modified.
	 * @param v1 a not null reference, store the sum
	 * @param v2 a not null reference
	 * @throws NullPointerException if v1 or v2 is null
	 */
	public static void add( final Vector3 v1, final Vector3 v2 ) {
		v1.x += v2.x;
		v1.y += v2.y;
		v1.z += v2.z;
	}

	/**
	 * Subtract two vectors and place the result in v1.
	 * <code>v2</code> is not modified.
	 * @param v1 a not null reference, store the difference
	 * @param v2 a not null reference
	 * @throws NullPointerException if v1 or v2 is null
	 */
	public static void sub( final Vector3 v1, final Vector3 v2 ) {
		v1.x -= v2.x;
		v1.y -= v2.y;
		v1.z -= v2.z;
	}

	/**
	 * Subtracts a provided vector to this vector creating a resultant
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
	 * Subtracts a provided vector to this vector.
	 * <code>this</code> contains the result and <code>v</code> is not modified.
	 * @param v vector to substract
	 * @return <code>this</code>
	 */
	public final Vector3 assignSub(Vector3 v) {
		x-=v.x;
		y-=v.y;
		z-=v.z;
		return this;
	}

	/**
	 * Multiply this vector by the scalar s
	 * @param s scalar value
	 * @return this vector
	 */
	public final Vector3 assignMultiply( double s) {
		x *= s;
		y *= s;
		z *= s;
		return this;
	}

	/**
	 * Multiply this vector by a provided scalar creating a resultant
	 * vector which is returned.
	 * <code>this</code> vector is not modified.
	 *
	 * @param s multiplication coefficient
	 * @return resultant vector
	 */
	public final Vector3 multiply( double s ) {
		return new Vector3( x*s, y*s, z*s);
	}

	/**
	 * Scale vector by the scale matrix given by s.
	 * <code>this</code> vector is not modified.
	 * @param s scale direction and factor
	 * @return an new vector
	 */
	public final Vector3 scale( Vector3 s) {
		return new Vector3(x*s.x, y*s.y, z*s.z);
	}

	/**
	 * Return the norm of a-b
	 */
	public static final double normOfDifference( Vector3 a, Vector3 b) {
		return Math.sqrt( (a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y)+(a.z-b.z)*(a.z-b.z) );  
	}

	/**
	 * Return the squared norm of a-b
	 */
	public static final double squaredNormOfDifference( Vector3 a, Vector3 b) {
		return (a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y)+(a.z-b.z)*(a.z-b.z);  
	}

	/**
	 * Return the norm of a+b
	 */
	public static final double normOfSum( Vector3 a, Vector3 b) {
		return Math.sqrt( (a.x+b.x)*(a.x+b.x)+(a.y+b.y)*(a.y+b.y)+(a.z+b.z)*(a.z+b.z) );  
	}

	/**
	 * Return the squared norm of a+b
	 */
	public static final double squaredNormOfSum( Vector3 a, Vector3 b) {
		return (a.x+b.x)*(a.x+b.x)+(a.y+b.y)*(a.y+b.y)+(a.z+b.z)*(a.z+b.z);  
	}

	/**
	 * Multiply v by s and place the result in v, so v = v s
	 */
	public static void multiply( Vector3 v, double s) {
		v.x*=s; v.y*=s; v.z*=s;
	}

	/**
	 * Multiply v by s and add the result to r, such that r = r + v s
	 */
	public static void multiplyAndAdd( Vector3 v, double s, Vector3 r) {
		r.x += v.x*s; 
		r.y += v.y*s; 
		r.z += v.z*s;
	}

	/**
	 * Multiply v by s, and store result in v. Add v to result and store in result
	 * @param v
	 * @param s
	 * @param result
	 * @throws NullPointerException if v ot result is null
	 */
	public static void  multiplyStoreAndAdd( Vector3 v, double s, Vector3 result) {
		v.x *= s;
		v.y *= s;
		v.z *= s;		
		result.x += v.x; 
		result.y += v.y; 
		result.z += v.z;
	}

	/**
	 * Return the scalar value of (y1-y2)^Ty3
	 */
	public static final double subAndDot( final Vector3 y1, final Vector3 y2, final Vector3 y3 ) {
		return (y1.x-y2.x)*y3.x + (y1.y-y2.y)*y3.y + (y1.z-y2.z)*y3.z; 
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
	 * z coordinated if truncated
	 * @param v the other vector
	 * @return the dot product of this and v1
	 * @throws NullPointerException
	 */	
	public final double xydot( Vector3 v ) {
		return this.x*v.x+this.y*v.y;
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
	public static void crossProduct( final Vector3 v1, final Vector3 v2, final Vector3 result ) {
		final double tempa1 = v1.y*v2.z-v1.z*v2.y;
		final double tempa2 = v1.z*v2.x-v1.x*v2.z;
		final double tempa3 = v1.x*v2.y-v1.y*v2.x;

		result.x = tempa1;
		result.y = tempa2;
		result.z = tempa3;
	}
	
	/**
	 * Return a new vector set to the normalisation of vector v1.
	 * <code>this</code> vector is not modified.
	 * @return the normalised vector
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
     * Assign the zero vector to this vector 
	 */
	public final Vector3 assignZero() {
		x = 0;
		y = 0;
		z = 0;
		return this;
	}
	
	/**
	 * Assign the difference of a and b to this vector
	 * @param a
	 * @param b
	 * @return
	 */
	public final Vector3 assignDifference(final Vector3 a, final Vector3 b) {
		x = a.x-b.x;
		y = a.y-b.y;
		z = a.z-b.z;
		return this;
	}
	
	/**
	 * Negate the value of this vector
	 */
	public final Vector3 assignNegate() {
		x *= -1;
		y *= -1;
		z *= -1;
		return this;
	}
	
	/** 
	 * Normalise this vector
	 */
	public final Vector3 assignNormalize() {
		double s = 1.0 / this.norm();
		x *= s;
		y *= s;
		z *= s;
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
	 * Returns the length of this vector.
	 * <code>this</code> vector is not modified.
	 * @return the length of this vector
	 */
	public final double squaredNorm() {
		return x*x+y*y+z*z;
	}

	/**
	 * Return the infinity norm, absolute value of the largest entry.
	 * @return infinity norm of this vector
	 */
	public final double infnorm() {
		return x>y? (x>z? x:z) : (y>z? y:z);
	}

	/**
	 * Returns <tt>true</tt> if the absolute value of the three coordinates are
	 * smaller or equal to epsilon.
	 *
	 * @param epsilon positive tolerance around zero
	 * @return true when the coordinates are next to zero
	 *         false in the other cases
	 */
	public final boolean isEpsilon(double epsilon) {
		if (epsilon < 0.) {
			throw new IllegalArgumentException("epsilon must be positive");
		}
		return -epsilon <= x && x <= epsilon
		&& -epsilon <= y && y <= epsilon
		&& -epsilon <= z && z <= epsilon;
	}
	/**
	 * Pack the three coordinates into a new double array
	 * <code>this</code> vector is not modified.
	 * @return a array set with x, y and z
	 */
	public final double[] toArray() {
		return new double[]{x,y,z};
	}
	/**
	 * Build an array of {@link double} from a collection of {@link Vector3}.
	 * A new array is allocated with a length equal to <code>vectors.size()*3</code>
	 * The given collection is not modified.
	 * @param vectors a not null collection ov vector
	 * @return a array of packed vector coordinates
	 */
	public static double[] toArray(final Collection<Vector3> vectors) {
		// convert points
		double[] array = new double[3 * vectors.size()];
		int i = 0;
		for (Vector3 v : vectors) {
			array[i++] = v.x;
			array[i++] = v.y;
			array[i++] = v.z;
		}
		return array;
	}
	/**
	 * Returns a string representation of this vector.  The string
	 * representation consists of the three dimensions in the order x, y, z,
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

	/**
	 * Return this vector as a formated string, that can be 
	 * directly used in a MATLAB script.
	 */
	public final String toMatlabString() {
		return "["+x+"; "+y+"; "+z+ "]";
	}

}
