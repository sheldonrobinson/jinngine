package jinngine.geometry;

import java.util.List;

import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class Line implements SupportMap3, Geometry {

	public Object getAuxiliary() {
		return auxiliary;
	}

	public void setAuxiliary(Object auxiliary) {
		this.auxiliary = auxiliary;
	}

	//world vectors
	private final Vector3 p1, p2;
	private Object auxiliary;
	
	public Line(Vector3 p1, Vector3 p2) {
		this.p1 = p1.copy();
		this.p2 = p2.copy();
	}
	
	@Override
	public Vector3 supportPoint(Vector3 direction) {
		return direction.dot(p1) > direction.dot(p2) ? p1.copy():p2.copy();
	}

	@Override
	public Body getBody() {
		return null;
	}

	@Override
	public double getEnvelope(double dt) {
		return 0;
	}

	@Override
	public InertiaMatrix getInertialMatrix(double mass) {
		//lines have no inertia
		return null;
	}

	@Override
	public Matrix4 getTransform() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBody(Body b) {
		// ignore

	}

	@Override
	public void setEnvelope(double envelope) {
		// do nothing
	}

	@Override
	public void setLocalTransform(Matrix3 B, Vector3 b2) {
		//do nothing
	}

	@Override
	public void setLocalTranslation(Vector3 b) {
		// do nothing
	}

	@Override
	public Vector3 getMaxBounds() {
		return new Vector3( p1.a1 > p2.a1? p1.a1 : p2.a1, 
				            p1.a2 > p2.a2? p1.a2 : p2.a2,
					        p1.a3 > p2.a3? p1.a3 : p2.a3);
	}

	@Override
	public Vector3 getMinBounds() {
		return new Vector3( p1.a1 < p2.a1? p1.a1 : p2.a1, 
	                        p1.a2 < p2.a2? p1.a2 : p2.a2,
	         	            p1.a3 < p2.a3? p1.a3 : p2.a3);
	}


	@Override
	public void supportFeature(Vector3 d, double epsilon, List<Vector3> face) {
		// TODO Auto-generated method stub
		
	}

}
