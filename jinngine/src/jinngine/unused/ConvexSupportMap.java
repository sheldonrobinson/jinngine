package jinngine.unused;

import java.util.List;

import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import jinngine.geometry.*;


/**
 * Constructs a support mapping of the convex hull of two different geometries (also given as support mappings)
 * @author mo
 *
 */
public class ConvexSupportMap implements SupportMap3, Geometry {

	public Object getAuxiliary() {
		return auxiliary;
	}

	public void setAuxiliary(Object auxiliary) {
		this.auxiliary = auxiliary;
	}

	private final SupportMap3 Sa;
	private final SupportMap3 Sb;
	private final Body a;
	private final Body b;
	private Object auxiliary;
	
	/**
	 * 
	 * @param Sa
	 * @param a
	 * @param Sb
	 * @param b
	 */
	public ConvexSupportMap(SupportMap3 Sa, Body a, SupportMap3 Sb, Body b) {
		this.Sa = Sa;
		this.Sb = Sb;
		this.a = a;
		this.b = b;
	}
	
	public Vector3 supportPoint(Vector3 direction) {
		Vector3 pa = a.toWorld(Sa.supportPoint(direction));
		Vector3 pb = b.toWorld(Sb.supportPoint(direction));
		
		if ( direction.dot(pa) > direction.dot(pb) ) 
			return pa;
		else
			return pb;
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
