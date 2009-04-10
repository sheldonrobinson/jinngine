package jinngine.stuff;
import java.util.*;

import jinngine.geometry.Geometry;
import jinngine.geometry.Shape;
import jinngine.geometry.SupportMap3;
import jinngine.geometry.Shape.*;
import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class HillClimbSupportMap 
implements SupportMap3, Geometry {

	@Override
	public Object getAuxiliary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAuxiliary(Object aux) {
		// TODO Auto-generated method stub
		
	}

	private final Shape shape;
	private Object auxiliary;

	//private SupportMap sphere = new SphereSupportMap(0.125);

	public HillClimbSupportMap( Shape shape ) {
		this.shape = shape;
	}

	//v is a unit vector in body space
	public Vector3 supportPoint( Vector3 v ) {
		//use a hill-climbing technique to locate the vertex with
		//the greatest v dot x

		//start out with a random vertex in the shape
		Vertex vertex = shape.getFirstVertex();

		//return the vector in object space
		return traverse(vertex, getBody().state.q.conjugate().rotate(v) ).getVector();
	}

	private final Vertex traverse( Vertex vertex, Vector3 v ) {
		double dotProduct = v.dot(vertex.getVector());

		//look at neighbor edges
		Iterator<Vertex> neighborIterator = vertex.getNeighborIterator();
		while (neighborIterator.hasNext()) {
			Vertex neighbor = neighborIterator.next();

			if ( dotProduct < v.dot(neighbor.getVector()) ) {
				//update greedily the the neighborVertex if it improves v dot x
				return traverse(neighbor,v);
			}
		}

		//if here, no updates could be found and the algorithm terminates
		return vertex;
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
	public void setLocalTranslation(Vector3 b) {
		// TODO Auto-generated method stub
		
	}
	
//	@Override
//	public void updateTransform() {
//		// TODO Auto-generated method stub
//		
//	}

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
	public InertiaMatrix getInertialMatrix(double mass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEnvelope(double envelope) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Vector3> supportFeature(Vector3 d) {
		// TODO Auto-generated method stub
		return null;
	}
}
