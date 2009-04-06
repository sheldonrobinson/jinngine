package jinngine.physics;

import jinngine.math.Matrix3;
import jinngine.math.Vector3;

/** 
 * A Sphere implementation. Uses the geometry type {@link jinngine.geometry.Sphere}. 
 * @author mo
 *
 */
public class Sphere extends Body {
	public final double radius;

	public Sphere( double r  ) {
		super();
		this.radius = r;

		//compute the tensor
		updateMomentOfInertia();
	
		//since this sphere is its own geometry, we add it self to the geometry list
		//geometries.add(new jinngine.geometry.Sphere(this, radius, new Vector3(5,0,0)));
		geometries.add(new jinngine.geometry.Sphere(this, radius, new Vector3(0,0,0)));
		//geometries.add(new jinngine.geometry.Sphere(this, radius, new Vector3(55,-5,0)));

	}

	public void updateMomentOfInertia() {
		double r = radius;

		//Inertia tensor for the sphere. 
		Matrix3.set( state.I,
				(2/5f)*state.M*r*r, 0.0f, 0.0f,
				0.0f, (2/5f)*state.M*r*r, 0.0f,
				0.0f, 0.0f, (2/5f)*state.M*r*r );


		//I inverse
		Matrix3.inverse( this.state.I, this.state.Iinverse );
	}	
}