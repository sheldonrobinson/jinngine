package jinngine.physics;

import jinngine.geometry.Geometry;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;

/**
 * A box implementation
 * @author mo
 *
 */
public class Box extends Body {
	public final double a;                                     // lengths of the box' sides
	public final double b;                                     
	public final double c;                                     

	/**
	 * Construct a box with given side lengths. The inertial momentum is computed assuming uniform mass distribution.
	 * @param x
	 * @param y
	 * @param z
	 */
	public Box(double x,double y, double z) {
		super();

		a = x; b = y; c = z;
		
		this.sleepKinetic = 0.2;

		//compute the tensor
		updateMomentOfInertia();

		//calculate maximum extend of this box
		this.state.rMax = new Vector3(a*0.5,b*0.5,c*0.5).norm();

		//new box geometry with no local transform
		this.geometries.add(
				new jinngine.geometry.Box(this, new Vector3(), Matrix3.scale(new Matrix3(), new Vector3(a,b,c))));

	}
	
	//temp fix
	public Geometry getBoxGeometry() {
		return geometries.get(0);
	}

	public void updateMomentOfInertia() {
		//I
		Matrix3.set( state.I,
				(1.0f/12.0f)*state.M*(b*b+c*c), 0.0f, 0.0f,
				0.0f, (1.0f/12.0f)*state.M*(a*a+c*c), 0.0f,
				0.0f, 0.0f, (1.0f/12.0f)*state.M*(b*b+a*a) );

		//I inverse
		Matrix3.inverse(state.I, state.Iinverse);
	}

	//  public void setupShape() {
	//    //Setup a trivial cube shape with side length s
	//    //the very sick form of trivial that is...
	//    double hx = a/2.0f;
	//    double hy = bb/2.0f;
	//    double hz = c/2.0f;
	//    
	//    //Cube consists of 8 vertices
	//    Vector3 p000 =  new Vector3( -hx, -hy, -hz );
	//    Vector3 p001 =  new Vector3( -hx, -hy, +hz );
	//    Vector3 p010 =  new Vector3( -hx, +hy, -hz );
	//    Vector3 p011 =  new Vector3( -hx, +hy, +hz );
	//    Vector3 p100 =  new Vector3( +hx, -hy, -hz );
	//    Vector3 p101 =  new Vector3( +hx, -hy, +hz );
	//    Vector3 p110 =  new Vector3( +hx, +hy, -hz );
	//    Vector3 p111 =  new Vector3( +hx, +hy, +hz  );
	//
	//    // A cube made of unit vectors with 
	//    // 0,0 as origin
	//    //
	//    // -11  ->     +--------+  <-  111
	//    //            /|       /|
	//    //           / |      / |
	//    // -1-  ->  +--------+  |  <-  11-
	//    // --1  ->  |  +-----|--+  <-  1-1 
	//    //          | /      | /  
	//    //          |/       |/
	//    // ---  ->  +--------+     <-  1--
	//
	//    //add the vertices 
	//    shape.addVertex( p000 );
	//    shape.addVertex( p001 );
	//    shape.addVertex( p010 );
	//    shape.addVertex( p011 );
	//    shape.addVertex( p100 );
	//    shape.addVertex( p101 );
	//    shape.addVertex( p110 );
	//    shape.addVertex( p111 );
	//
	//  }
}
