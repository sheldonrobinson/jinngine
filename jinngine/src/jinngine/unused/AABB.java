package jinngine.unused;
import java.util.*;

import jinngine.geometry.Geometry;
import jinngine.math.Vector3;
import jinngine.physics.Body;

//axis aligned bounding box
//in object coordinates
public class AABB<G extends Geometry> {
	
	public G geometry;
	public Body body;


	public AABB() {
	}

	private Vector3 minBounds;
	private Vector3 maxBounds;

	public void setupAABB(Shape shape, double envelope) {
		//go through all vertices in the polyhedron 
		//object, and find the one thats most far from the 
		//origin. The AABB is then based on this distance. 
		//In other words, we find the bounding sphere, and create an AABB that
		//bounds the sphere
		double d=0;
		Shape.Vertex v=null;    

		Iterator<Shape.Vertex> iterator = shape.getVertexIterator();
		while (iterator.hasNext()) {
			v = iterator.next();
			double dv = v.getVector().norm();
			if (d<dv) { d=dv; }
		}

		if (v!=null) {
			//create the vertices representing bounds along the axis. We bind the 
			//new vertices to the same object as the given shape 
			minBounds = new Vector3();
			maxBounds = new Vector3();

//			//add small envelope
			//d += 1.7;
			d += envelope;


			//setup AABB bounds
			minBounds.assign(new Vector3(-d,-d,-d));
			maxBounds.assign(new Vector3(d,d,d));
			System.out.println("bounds are: " + d );
		} else {
			System.out.println("calculation of AABB faild");
			assert false;
		}

	}

	//Create the AABB from the current orientation of the body,
	//consequently if the body is rotated it could be violating the AABB's faces
	public void tightAABB( Body body, Shape shape, double envelope ) {
		minBounds.assign(Vector3.zero);
		maxBounds.assign(Vector3.zero);

		Iterator<Shape.Vertex> iterator = shape.getVertexIterator();
		while (iterator.hasNext()) {
			Vector3 v = iterator.next().getVector();
			//rotate
			//Quaternion.applyRotation(body.state.q, v);
			v = body.state.q.rotate(v);

			if (v.a1 > maxBounds.a1 ) maxBounds.a1 = v.a1;
			if (v.a2 > maxBounds.a2 ) maxBounds.a2 = v.a2;
			if (v.a3 > maxBounds.a3 ) maxBounds.a3 = v.a3;
			if (v.a1 < minBounds.a1 ) minBounds.a1 = v.a1;
			if (v.a2 < minBounds.a2 ) minBounds.a2 = v.a2;
			if (v.a3 < minBounds.a3 ) minBounds.a3 = v.a3; 
		}

		//extend the box by envelope amount
		maxBounds = maxBounds.add( new Vector3( envelope, envelope, envelope ) );
		minBounds = minBounds.minus( new Vector3( envelope, envelope, envelope ) );

		maxBounds.print();
		minBounds.print();
	}

	//in world coordinates
	public Vector3 getMinBoundsTranslated(Body body) {
		//System.out.println( body + " -- " + sourceFeature.object );
		//body.updateTransformations();
		return body.translate( minBounds );
		//return sourceFeature.translate( minBounds );
	}

	//in world coordinates
	public Vector3 getMaxBoundsTranslated(Body body) {
		return body.translate( maxBounds );
		//return sourceFeature.translate( maxBounds );
	}

	public static final boolean overlap( Body iBody, AABB i, Body jBody, AABB j) {
		Vector3 iminBoundsTranslated = iBody.translate( i.minBounds );
		Vector3 imaxBoundsTranslated = iBody.translate( i.maxBounds );
		Vector3 jminBoundsTranslated = jBody.translate( j.minBounds );
		Vector3 jmaxBoundsTranslated = jBody.translate( j.maxBounds );

		double ixMin = iminBoundsTranslated.a1;
		double iyMin = iminBoundsTranslated.a2;
		double izMin = iminBoundsTranslated.a3;
		double ixMax = imaxBoundsTranslated.a1;
		double iyMax = imaxBoundsTranslated.a2;
		double izMax = imaxBoundsTranslated.a3;

		double jxMin = jminBoundsTranslated.a1;
		double jyMin = jminBoundsTranslated.a2;
		double jzMin = jminBoundsTranslated.a3;
		double jxMax = jmaxBoundsTranslated.a1;
		double jyMax = jmaxBoundsTranslated.a2;
		double jzMax = jmaxBoundsTranslated.a3;

		//TODO test this 
		if( (((jxMin < ixMin) && (ixMin <= jxMax)) || ((ixMin <= jxMin) && (jxMin < ixMax ))) &&
				(((jyMin < iyMin) && (iyMin <= jyMax)) || ((iyMin <= jyMin) && (jyMin < iyMax ))) &&
				(((jzMin < izMin) && (izMin <= jzMax)) || ((izMin <= jzMin) && (jzMin < izMax )))) {
			return true;
		} else {
			return false;
		}
	}

}

