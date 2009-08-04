package jinngine.geometry;

import java.util.ArrayList;
import java.util.List;

import jinngine.math.*;
import jinngine.physics.Body;

/**
 * A box geometry implementation. Represented using a simple support mapping. 
 * Uses a simple axis aligned bounding box implementation.
 * @author mo sdf
 *
 */
public class Box implements SupportMap3, Geometry {

	public Object getAuxiliary() {
		return auxiliary;
	}

	public void setAuxiliary(Object auxiliary) {
		this.auxiliary = auxiliary;
	}

	private  Body body;
//	private final Matrix4 transform4 = new Matrix4();
	private final Matrix3 transform = new Matrix3();
	private final Matrix4 localtransform4 = new Matrix4();
	private final Matrix3 localtransform = new Matrix3();
	private final Vector3 localdisplacement = new Vector3();
	private final Vector3 bounds = new Vector3();
//	private final Matrix4 transform;	
	private double envelope = 0;
	private Object auxiliary;
	
	
	public Box(Body body) {
		super();
		this.body = body;
		setLocalTransform(Matrix3.identity(new Matrix3()), new Vector3() );		
	}
	
	public Box(Body body, Vector3 displacement, Matrix3 localtransform) {
		super();
		this.body = body;
		setLocalTransform(localtransform, displacement);
	}

	@Override
	public Vector3 supportPoint(Vector3 direction) {
		Vector3 v = body.state.rotation.multiply(localtransform).transpose().multiply(direction);
		double sv1 = v.a1<0?-0.5:0.5;
		double sv2 = v.a2<0?-0.5:0.5;
		double sv3 = v.a3<0?-0.5:0.5;
		//return Matrix4.multiply(transform4, new Vector3(sv1, sv2, sv3), new Vector3());
		return body.state.rotation.multiply(localtransform.multiply(new Vector3(sv1, sv2, sv3)).add(localdisplacement)).add(body.state.rCm);

	}

	@Override
	public Body getBody() {
		return body;
	}
	
	public void setBody(Body b) {
		this.body = b;
	}

	@Override
	public InertiaMatrix getInertialMatrix(double mass) {
		InertiaMatrix I = new InertiaMatrix();
		double scale = 1;
		//assume that transform is only scaling
		double a = localtransform.a11*scale, b = localtransform.a22*scale, c = localtransform.a33*scale;
		
		Matrix3.set( I,
				(1.0f/12.0f)*mass*(b*b+c*c), 0.0f, 0.0f,
				0.0f, (1.0f/12.0f)*mass*(a*a+c*c), 0.0f,
				0.0f, 0.0f, (1.0f/12.0f)*mass*(b*b+a*a) );


		return I;
	}
	
	@Override
	public double getEnvelope(double dt) {
		//if manually override
		if (envelope > 0) {
			return envelope;
		}

		//assume that transform is only scaling
		double a = localtransform.a11, b = localtransform.a22, c = localtransform.a33;
		
		//TODO this is just a crazy guess :) a good heuristic is needed
		return (a+b+c) / 60 + dt*15 ;
	}

	@Override
	public void setEnvelope(double envelope) {
		this.envelope = envelope;
	}

	@Override
	public void setLocalTransform(Matrix3 localtransform, Vector3 displacement) {
		this.localdisplacement.assign(displacement);
		Matrix3.set(localtransform, this.localtransform); 
		Matrix4.set(Transforms.transformAndTranslate4(localtransform, localdisplacement), localtransform4);
		
		//extremal point on box
		double max = Matrix3.multiply(localtransform, new Vector3(0.5,0.5,0.5), new Vector3()).norm() +412.0;
		bounds.assign(new Vector3(max,max,max));
		//System.out.println("max="+max);
	}
	
	@Override
	public void setLocalTranslation(Vector3 b) {
		setLocalTransform(localtransform, b);		
	}

	@Override
	public Vector3 getMaxBounds() {
		Vector3 displacement = new Vector3();	
		displacement.assign(Matrix3.multiply(body.state.rotation, localdisplacement, new Vector3()));
		return bounds.add(displacement).add(body.state.rCm);
	}

	@Override
	public Vector3 getMinBounds() {
		Vector3 displacement = new Vector3();	
		displacement.assign(Matrix3.multiply(body.state.rotation, localdisplacement, new Vector3()));
		return bounds.multiply(-1).add(displacement).add(body.state.rCm);
	}

	@Override
	public Matrix4 getTransform() {
		return Matrix4.multiply(body.getTransform(), localtransform4, new Matrix4());
	}
	
	public Vector3 getExtends() {
		return new Vector3( localtransform.a11*0.5, localtransform.a22*0.5, localtransform.a33*0.5);

	}

	@Override
	public void supportFeature(Vector3 d, List<Vector3> featureList) {
		final double epsilon = 0.03;  //123+132  213 231 312+321   
		//get d into the canonical box space
		//Vector3 v = body.state.rotation.multiply(localtransform).transpose().multiply(d);
		Vector3 v = body.state.rotation.transpose().multiply(d);
		//Vector3 v = d.copy();
		//List<Vector3> featureList = new ArrayList<Vector3>();

		int numberOfZeroAxis = 0;
		int[] zeroAxisIndices = new int[3];

		int numberOfNonZeroAxis = 0;
		int[] nonZeroAxisIndices = new int[3];
		
		if (Math.abs(v.a1) < epsilon ) {
			zeroAxisIndices[numberOfZeroAxis++]=0;
		} else { nonZeroAxisIndices[ numberOfNonZeroAxis++] = 0; }
		if (Math.abs(v.a2) < epsilon ) {
			zeroAxisIndices[numberOfZeroAxis++]=1;
		} else { nonZeroAxisIndices[ numberOfNonZeroAxis++] = 1; }
		if (Math.abs(v.a3) < epsilon ) {
			zeroAxisIndices[numberOfZeroAxis++]=2;
		} else { nonZeroAxisIndices[ numberOfNonZeroAxis++] = 2; }
		
		if (numberOfZeroAxis == 0) {
			//eight possible points

			double sv1 = v.a1<0?-0.5:0.5;
			double sv2 = v.a2<0?-0.5:0.5;
			double sv3 = v.a3<0?-0.5:0.5;
			//return Matrix4.multiply(transform4, new Vector3(sv1, sv2, sv3), new Vector3());
			featureList.add( body.state.rotation.multiply(localtransform.multiply(new Vector3(sv1, sv2, sv3)).add(localdisplacement)).add(body.state.rCm) );
		
		}

		else if (numberOfZeroAxis == 1) {
			//System.out.println("edge case");

			//four possible edges
			Vector3 p1 = new Vector3(v.a1<0?-0.5:0.5, v.a2<0?-0.5:0.5, v.a3<0?-0.5:0.5 );
			Vector3 p2 = new Vector3(v.a1<0?-0.5:0.5, v.a2<0?-0.5:0.5, v.a3<0?-0.5:0.5 );
			p1.set( zeroAxisIndices[0], 0.5);
			p2.set( zeroAxisIndices[0], -0.5);
			
			featureList.add( body.state.rotation.multiply(localtransform.multiply(p1).add(localdisplacement)).add(body.state.rCm) );
			featureList.add( body.state.rotation.multiply(localtransform.multiply(p2).add(localdisplacement)).add(body.state.rCm) );
		}

		else if (numberOfZeroAxis == 2) {
			//System.out.println("face case");
			//two possible faces
			//four possible edges
			Vector3 p1 = new Vector3(v.a1<0?-0.5:0.5, v.a2<0?-0.5:0.5, v.a3<0?-0.5:0.5 );
			Vector3 p2 = new Vector3(v.a1<0?-0.5:0.5, v.a2<0?-0.5:0.5, v.a3<0?-0.5:0.5 );
			Vector3 p3 = new Vector3(v.a1<0?-0.5:0.5, v.a2<0?-0.5:0.5, v.a3<0?-0.5:0.5 );
			Vector3 p4 = new Vector3(v.a1<0?-0.5:0.5, v.a2<0?-0.5:0.5, v.a3<0?-0.5:0.5 );


			
			//make face turn clock wise
			if ( v.get(nonZeroAxisIndices[0]) > 0 ) { 
				p1.set( zeroAxisIndices[0], 0.5);
				p1.set( zeroAxisIndices[1], 0.5);

				p2.set( zeroAxisIndices[0], -0.5);
				p2.set( zeroAxisIndices[1],  0.5);

				p3.set( zeroAxisIndices[0], -0.5);
				p3.set( zeroAxisIndices[1], -0.5);     

				p4.set( zeroAxisIndices[0],  0.5);      
				p4.set( zeroAxisIndices[1], -0.5);
			} else {
				p1.set( zeroAxisIndices[0], 0.5);
				p1.set( zeroAxisIndices[1], 0.5);

				p2.set( zeroAxisIndices[0],  0.5);
				p2.set( zeroAxisIndices[1],  -0.5);

				p3.set( zeroAxisIndices[0], -0.5);
				p3.set( zeroAxisIndices[1], -0.5);

				p4.set( zeroAxisIndices[0],  -0.5);
				p4.set( zeroAxisIndices[1],   0.5);
				
			}
			
			
			
			featureList.add( body.state.rotation.multiply(localtransform.multiply(p1).add(localdisplacement)).add(body.state.rCm) );
			featureList.add( body.state.rotation.multiply(localtransform.multiply(p2).add(localdisplacement)).add(body.state.rCm) );
			featureList.add( body.state.rotation.multiply(localtransform.multiply(p3).add(localdisplacement)).add(body.state.rCm) );
			featureList.add( body.state.rotation.multiply(localtransform.multiply(p4).add(localdisplacement)).add(body.state.rCm) );			
		}

		else if (numberOfZeroAxis == 3) {
			//should never happen, undefinded result
		System.out.println("DOOOOOOOHHHHHHH");
			//return null;
		}
	}
}
