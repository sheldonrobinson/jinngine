package jinngine.physics;

import jinngine.geometry.SupportMap3;
import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;

/**
 * Ellipsoid implementation. Currently needs a new geometry implementation.
 * @author mo
 *
 */
public class Ellipsoid extends Body {
	public final double a,b,c;
	private final Body self = this;

	/**
	 * Construct a new ellipsoid with axis lengths a, b, and c. 
	 * @param a
	 * @param b
	 * @param c
	 */
	public Ellipsoid( double a, double b, double c  ) {
		super();		    
		this.a = a; this.b = b; this.c = c;
		final Matrix3 D = new Matrix3(new Vector3(a*a,0,0),new Vector3(0,b*b,0),new Vector3(0,0,c*c));
		this.state.M = 1;

		//compute the tensor
		updateMomentOfInertia();

		//Ellipsoid support map (this support mapping is derived in [Silcowitz 2008])
//		this.setSupportMap( new SupportMap3() {
//			@Override
//			public Vector3 supportPoint(Vector3 direction) {
//				Vector3 v = self.toModelNoTranslation(direction);
//				return D.multiply(v).multiply(1/Math.sqrt(v.dot(D.multiply(v)))); 
//			}
//
//
//			@Override
//			public Body getBody() {
//				return self;
//			}
//
//			
//			
//			@Override
//			public void setBody(Body b) {
//				// TODO Auto-generated method stub
//				
//			}
//
//
//			@Override
//			public Vector3 getMaxBounds() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			@Override
//			public Vector3 getMinBounds() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			@Override
//			public void setLocalTransform(Matrix3 B, Vector3 b2) {
//				// TODO Auto-generated method stub
//				
//			}
//			@Override
//			public void setLocalTranslation(Vector3 b) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public Matrix4 getTransform() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//
//			@Override
//			public double getEnvelope(double dt) {
//				// TODO Auto-generated method stub
//				return 0;
//			}
//
//
//			@Override
//			public InertiaMatrix getInertialMatrix(double mass) {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//
//			@Override
//			public void setEnvelope(double envelope) {
//				// TODO Auto-generated method stub
//				
//			}
//
//
//			@Override
//			public Object getAuxiliary() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//
//			@Override
//			public void setAuxiliary(Object aux) {
//				// TODO Auto-generated method stub
//				
//			}
//		});

	}


//	@Override
	public void updateMomentOfInertia() {

		//Inertia tensor for the Ellipsoid. 
		Matrix3.set( state.I,
				(1/5f)*state.M*(b*b+c*c), 0.0f, 0.0f,
				0.0f, (1/5f)*state.M*(a*a+c*c), 0.0f,
				0.0f, 0.0f, (1/5f)*state.M*(a*a+b*b) );


		//I inverse
		Matrix3.inverse( this.state.I, this.state.Iinverse );
	}
}
