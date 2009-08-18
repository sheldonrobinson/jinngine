package jinngine.physics;
import java.util.*;

import jinngine.geometry.Geometry;
import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Transforms;
import jinngine.math.Vector3;
import jinngine.util.Tuple;

/**
 * Composite body is a way of constructing a rigid body as a composition of a number of other bodies. 
 * This is equivalent to inserting all bodies into the simulator, and afterwards link them together using
 * the required number of fixed joints. However, using body composition is much more effective, as it reduces 
 * the number of required new constraints and variables to zero. On the other hand, bodies cannot be dynamically released 
 * from each other at a later point in time.
 * @author mo
 *
 */
public class CompositeBody extends Body {
	//private final List<Tuple<Vector3,Double>> massList = new ArrayList<Tuple<Vector3,Double>>();
	private final Vector3 cm = new Vector3();
	private final List<Double> masses = new ArrayList<Double>();
	private final List<Matrix4> transforms = new ArrayList<Matrix4>();
	
	//Give two objects in the CompositeBody object space, construct a 
	//new rigid body composed of these. 
	public CompositeBody() {
		super();
		this.sleepKinetic = 0;
		this.state.M=0;
		Matrix3.set(new Matrix3(), state.I);
	}
	
	/**
	 * Add a geometry to this body
	 * @param g
	 * @param R
	 * @param r
	 * @param mass
	 */
	public void addGeometry( Geometry g, Matrix3 R, Vector3 r, double mass) {
		//keep track on center of mass
		this.cm.assign( cm.add(r.multiply(mass)));
		this.state.M += mass;

		//set local transform in geometry and add to list
		g.setBody(this);
		//g.setLocalTransform(R, r);
		masses.add(mass);		
		transforms.add(Transforms.transformAndTranslate4(R, r));
		geometries.add(g);
	}

	public void finalize() {
		
		if ( geometries.size() > 0 ) {
			//find center of mass
			cm.assign( cm.multiply(1/this.state.M));

			//translate all geometries so center of mass will become the origin
			int i = 0;
			for (Geometry g: geometries) {
				double mass = masses.get(i);
				Matrix4 transform = transforms.get(i);

				Vector3 r = Transforms.translation(transform).minus(cm);
				Matrix3 R = new Matrix3(transform);

				//rotate the inertia matrix into this frame and add it to the inertia tensor of this body
				Matrix3 Im = InertiaMatrix.rotate(g.getInertialMatrix(mass), R).translate(mass, r);
				Matrix3.add(this.state.I, Im, this.state.I);

				//set the final transform
				g.setLocalTransform(R, r);

				//next
				i++;
			}

			//fill out the invers tensor
			Matrix3.inverse(this.state.I, this.state.Iinverse);

			Matrix3.print(state.Iinverse.multiply(state.I));
			System.out.println("mass="+state.M);
		} else {
			//fall-back on something, in case no geometries were given
			this.state.M = 1;
			this.state.I.identity();
			this.state.Iinverse.identity();
		}
	}

	public void updateCenterOfMass() {
		//       1  i=1
		// cm = --- sum m_i * r_i  where M is the total mass
		//       M  i<N		

//		this.state.M = sum;
//		cm.assign( rcm.multiply(1/this.state.M));
	}
	
	
	public void updateMomentOfInertia() {
//		double a,b,c;
//		a=16;
//		b=16;
//		c=16;
//		//I
//		Matrix3.set( state.I,
//				(1.0f/12.0f)*state.M*(b*b+c*c), 0.0f, 0.0f,
//				0.0f, (1.0f/12.0f)*state.M*(a*a+c*c), 0.0f,
//				0.0f, 0.0f, (1.0f/12.0f)*state.M*(b*b+a*a) );
//
//		//I inverse
//		Matrix3.inverse(state.I, state.Iinverse);
	}
	
//	@Override
//	public void updateMomentOfInertia() {
//		//clear matrix
//		Matrix3.set(new Matrix3(), this.state.I);
//
//		//go through list, rotate and translate inertia matrices, sum together
//		for (Body b: list) {
//			InertiaMatrix Ib = b.state.I.rotate(b.state.q).translate(b.state.M, b.state.rCm.minus(this.cm));
//			Matrix3.add(this.state.I, Ib, this.state.I);
//		}
//		
//		//inverse
//		Matrix3.inverse(this.state.I, this.state.Iinverse);
//	}

}
