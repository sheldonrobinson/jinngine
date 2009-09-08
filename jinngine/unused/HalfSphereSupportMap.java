package jinngine.unused;

import java.util.List;

import jinngine.geometry.Geometry;
import jinngine.geometry.SupportMap3;
import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class HalfSphereSupportMap implements SupportMap3, Geometry {
	@Override
	public Object getAuxiliary() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setAuxiliary(Object aux) {
		// TODO Auto-generated method stub
		
	}
	private double xt,yu,zv;
	//private Object auxiliary;

	
	public HalfSphereSupportMap( double xt, double yu, double zv) {
		this.xt = xt;
		this.yu = yu;
		this.zv = zv;
	}
	public Vector3 supportPoint( Vector3 v) {
		Vector3 w = getBody().state.q.conjugate().rotate(v.normalize()); 
		//return w.multiply(radius*(0.5f +Math.abs(w.dot(Vector3.k)))    );
		double wx = w.dot(Vector3.i);
		double wy = w.dot(Vector3.j);
		double wz = w.dot(Vector3.k); wz=wz<0?0:wz; //no negative z axis 
		
		
		Vector3 xd = (new Vector3(wx,wy,wz));
		
		//Check 
		Vector3 x;
		if (xd.abs().lessThan(Vector3.epsilon) ) {
			x = Vector3.i;
		} else {
			x = xd.normalize();
		}
		
		return Vector3.i.multiply(x.a1*xt)
		.add(Vector3.j.multiply(x.a2*yu))
		.add(Vector3.k.multiply(x.a3*zv - (3.0/8.0)*zv ) );
		
	}
	@Override
	public Body getBody() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	@Override
	public void setBody(Body b) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setLocalTransform(Matrix3 B, Vector3 b2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vector3 getMaxBounds() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Vector3 getMinBounds() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Matrix4 getTransform() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public double getEnvelope(double dt) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public InertiaMatrix getInertialMatrix() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setEnvelope(double envelope) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void supportFeature(Vector3 d, double epsilon, List<Vector3> face) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void getLocalTransform(Matrix3 R, Vector3 b) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public double getMass() {
		// TODO Auto-generated method stub
		return 0;
	}
}
