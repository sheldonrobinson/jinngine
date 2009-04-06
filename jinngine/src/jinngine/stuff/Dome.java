package jinngine.stuff;
import java.util.*;

import jinngine.geometry.Shape;
import jinngine.geometry.SupportMap3;
import jinngine.math.Matrix3;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class Dome extends Body {
	private double t,u,v;

	public Dome( double r ) {
		super();
		this.t = r;
		this.u = r;
		this.v = r;

		//compute the tensor
		updateMomentOfInertia();

		SupportMap3 one = new HalfSphereSupportMap(r,r,r);

	}

	public void updateMomentOfInertia() {

		//Inertia tensor for the half sphere. 
		Matrix3.set( state.I,
				(1/5f)*state.M*(u*u)+(19*state.M*v*v)/320.0, 0.0f, 0.0f,
				0.0f, (1/5f)*state.M*(t*t)+(19*state.M*v*v)/320.0, 0.0f,
				0.0f, 0.0f, (1/5f)*state.M*(u*u+t*t) );

		//I inverse
		Matrix3.inverse( this.state.I, this.state.Iinverse );
	}

	public void setupShape() {
		List<Vector3> list = new ArrayList<Vector3>();

		//create som points on the convex hull of the half-sphere
		for (float theta=0.00f; theta<2*Math.PI; theta+=Math.PI/8) {
			for (float phi=0.01f; phi<Math.PI*0.6; phi+=Math.PI/8) {
				Vector3 p = (new Vector3( t*((double)(Math.sin(phi)*Math.cos(theta))), 
						u*(double)((Math.sin(phi)*Math.sin(theta))), 
						v*((double)Math.cos(phi)))).add(new Vector3(0,0,-(3.0/8.0)*v));          

				list.add( p );

			}
		}

		//fire off the convex hull
//		ConvexHullAlgorithm algorithm = new RandomizedConvexHull();
//		algorithm.run(list,shape);

	}




}
