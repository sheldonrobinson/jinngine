/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.geometry;

import java.util.ArrayList;
import java.util.List;

import quickhull3d.Point3d;
import quickhull3d.QuickHull3D;

import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Transforms;
import jinngine.math.Vector3;
import jinngine.physics.Body;

public class ConvexHull implements SupportMap3, Geometry {

	private final List<Vector3[]> faces = new ArrayList<Vector3[]>();
	private final ArrayList<Vector3> vertices = new ArrayList<Vector3>();
	private final ArrayList<ArrayList<Integer>> adjacent;
	private final ArrayList<Vector3> dualvertices = new ArrayList<Vector3>();
	private final ArrayList<ArrayList<Integer>> dualadjacent;
	
	/**
	 * Computes vertex adjacency lists. Method simply runs through all faces, which are given as lists of vertex indices, and fills out 
	 * adjacency lists along the way. It also roots out duplicate adjacency entries, arising from the same pair of vertices being present
	 * two adjacent faces. The motivation for this approach is that the face index lists are the only adjacency information available from 
	 * the QuickHull3D implementation.
	 * @param faceindices
	 * @return
	 */
	private final ArrayList<ArrayList<Integer>> adjacencyList( int[][] faceindices, int numberOfVertices) {
		ArrayList<ArrayList<Integer>> adjacent = new ArrayList<ArrayList<Integer>>();
		
		//create array of arrays for adjacency lists 
		adjacent.ensureCapacity(numberOfVertices);
		for (int i=0;i<numberOfVertices; i++)
			adjacent.add(i, new ArrayList<Integer>());
		
		// for each face
		for ( int[] face : faceindices) {
			//for each vertex 
			int prevvertex = face[face.length-1];
			for (int i=0; i<face.length; i++) {
				//add both ways in adjacency list
				int vertex = face[i];
				
				// first vertex (if not already there)
				List<Integer> adj = adjacent.get(prevvertex);
				boolean found = false;
				for (int a : adj)
					if (a == vertex) 
						found = true;
				if (!found)
					adj.add(vertex);
				
				// second vertex
				adj = adjacent.get(vertex);
				found = false;
				for (int a : adj)
					if (a == prevvertex) 
						found = true;
				if (!found)
					adj.add(prevvertex);

				// set next previous vertex
				prevvertex = vertex;
			}			
		}
		
		return adjacent;
	}
	
	/**
	 * Create a convex hull geometry, based on the points given 
	 * @param input
	 */
	public ConvexHull(List<Vector3> input) {	
		final QuickHull3D dualhull = new QuickHull3D();
		final QuickHull3D hull = new QuickHull3D();

		// convert points 	
		int i=0; double[] vectors = new double[3*input.size()];
		for (Vector3 v: input) {
			vectors[i+0] = v.x;
			vectors[i+1] = v.y;
			vectors[i+2] = v.z;
			i = i+3;
		}
		
		// build the hull
		hull.build(vectors);
				
		// extract faces from the QuickHull3D implementation
		Point3d[] points = hull.getVertices();
		int[][] faceIndices = hull.getFaces();
		
		// get hull vertices 
		for ( Point3d p: points) 
			vertices.add(new Vector3( p.x, p.y, p.z));
	
		// adjacency lists for hull
		adjacent = adjacencyList(faceIndices, vertices.size() );
		
		// go thru all faces to make the dual hull points
		for (i=0; i< faceIndices.length; i++) { 	
			//convert to Vector3 array
			Vector3[] f = new Vector3[faceIndices[i].length];
			for (int j=0; j<faceIndices[i].length; j++) {
				Point3d p = points[faceIndices[i][j]];
				f[j] = new Vector3(p.x,p.y,p.z );
			}

			//append face to external representation
			faces.add(f);
			
			//get face vertices
			Vector3 v1 = f[0];
			Vector3 v2 = f[1];
			Vector3 v3 = f[2];

			// set normal
			Vector3 normal = v1.minus(v2).cross(v3.minus(v2)).normalize().multiply(-1);
			
			// add to the dual polygon vertices (index corresponds to a face)
			dualvertices.add(normal);
		}
		
		// create the dual hull
		i=0; double[] dualvectors = new double[3*dualvertices.size()];
		for (Vector3 v: dualvertices) {
			dualvectors[i+0] = v.x;
			dualvectors[i+1] = v.y;
			dualvectors[i+2] = v.z;
			i = i+3;
		}
		
		// build the hull
		dualhull.build(dualvectors);
		
		// create an adjacency list for the dual hull
		dualadjacent = adjacencyList(dualhull.getFaces(), dualvertices.size());	
		
		
		//search vertices to find bounds
		Vector3 extremal = new Vector3();
		for ( Vector3 v: vertices) {
			if (extremal.norm() < v.norm()) extremal.assign(v);
		}
		
		double max = extremal.norm()+5.0;
		minBounds.assign(new Vector3(-max,-max,-max));
		maxBounds.assign(new Vector3(max,max,max));
		//assume identity local transform for now
		minBoundsTransformed.assign(minBounds);
		maxBoundsTransformed.assign(maxBounds);
		
		System.out.println("Hull created");
		System.out.println("faces " + faces.size()); 
		body = new Body();
		
		new MassCalculation( this, 4);
			
	}
	

	// SupportMap3
	private Object auxiliary;
	private Body body;
	private double envelope =0;
	private Matrix3 localtransform = Matrix3.identity(new Matrix3());
	private Matrix4 localtransform4 = Matrix4.identity(new Matrix4());
	private final Vector3 localdisplacement = new Vector3();
	private final Vector3 displacement = new Vector3();
	private final Vector3 minBounds = new Vector3();
	private final Vector3 maxBounds = new Vector3();
	private final Vector3 minBoundsTransformed = new Vector3();
	private final Vector3 maxBoundsTransformed = new Vector3();

	@Override
	public Vector3 supportPoint(Vector3 direction) {
		Vector3 v = body.state.rotation.multiply(localtransform).transpose().multiply(direction);
		
		//hill climb along v
		int index = 0;
		double value = v.dot(vertices.get(index));
		boolean better = true;
		while (better) {
			better = false;
			//go through adjacency list and pick first improver (greedy)
			for ( int i: adjacent.get(index)) {
				double newvalue = v.dot(vertices.get(i));
				if ( newvalue > value) {
					value = newvalue;
					index = i;
					better = true;
					break;
				} 
			}
		}

		//return Matrix4.multiply(transform4, new Vector3(sv1, sv2, sv3), new Vector3());
		return body.state.rotation.multiply(localtransform.multiply(vertices.get(index)).add(localdisplacement)).add(body.state.position);
	}

	@Override
	public void supportFeature(Vector3 direction, double epsilon, List<Vector3> returnface) {
		Vector3 v = body.state.rotation.multiply(localtransform).transpose().multiply(direction);
		// hill climb the dual hull to find face 
		int index = 0;
		double value = v.dot(dualvertices.get(index));
		boolean better = true;
		while (better) {
			better = false;
			//go through adjacency list and pick first improver (greedy)
			for ( int i: dualadjacent.get(index)) {
				double newvalue = v.dot(dualvertices.get(i));
				if ( newvalue > value) {
					value = newvalue;
					index = i;
					better = true;
					break;
				} 
			}
		}
		
		// output the face according to the dual hull index
		for (Vector3 p: faces.get(index)) 
			returnface.add( body.state.rotation.multiply(localtransform.multiply(p).add(localdisplacement)).add(body.state.position) );
	}

	
	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public double getEnvelope(double dt) {
		return envelope;
	}

	@Override
	public InertiaMatrix getInertialMatrix() {
		InertiaMatrix I = new InertiaMatrix();
		//approximate the inertia matrix by the encapsulating box
		double a,b,c;
//		a=Math.abs(maxBounds.a1-minBounds.a1);
//		b=Math.abs(maxBounds.a2-minBounds.a2);
//		c=Math.abs(maxBounds.a3-minBounds.a3);
//		a=b=c=3;
		a=3;
		b=3;
		c=3;
		//I
		Matrix3.set( I,
				(1.0f/12.0f)*mass*(b*b+c*c), 0.0f, 0.0f,
				0.0f, (1.0f/12.0f)*mass*(a*a+c*c), 0.0f,
				0.0f, 0.0f, (1.0f/12.0f)*mass*(b*b+a*a) );

		return I;
	}

		
	@Override
	public Matrix4 getTransform() {
		return Matrix4.multiply(body.getTransform(), localtransform4, new Matrix4());
	}

	@Override
	public void setBody(Body b) {
		this.body = b;
	}

	@Override
	public void setEnvelope(double envelope) {
		this.envelope = envelope;
	}

	@Override
	public void setLocalTransform(Matrix3 B, Vector3 displacement) {
		this.localdisplacement.assign(displacement);
		Matrix3.set(B, this.localtransform); 
		Matrix4.set(Transforms.transformAndTranslate4(localtransform, localdisplacement), localtransform4);
		
		//extremal points transformed
		//double max = Matrix3.multiply(localtransform, new Vector3(0.5,0.5,0.5), new Vector3()).norm() +2.0;
		minBoundsTransformed.assign(Matrix3.multiply(localtransform, minBounds, new Vector3()));
		maxBoundsTransformed.assign(Matrix3.multiply(localtransform, maxBounds, new Vector3()));

		//add envelope
		minBoundsTransformed.assign(minBoundsTransformed.minus(new Vector3(envelope,envelope,envelope)));
		maxBoundsTransformed.assign(maxBoundsTransformed.add(new Vector3(envelope,envelope,envelope)));

	}
	

	@Override
	public Vector3 getMaxBounds() {
		//displacement.assign(Matrix3.multiply(body.state.rotation, localdisplacement, new Vector3()));
		return maxBoundsTransformed.add(displacement).add(body.state.position);

	}

	@Override
	public Vector3 getMinBounds() {
		//displacement.assign(Matrix3.multiply(body.state.rotation, localdisplacement, new Vector3()));
		return minBoundsTransformed.add(displacement).add(body.state.position);
	}

	
	@Override
	public void getLocalTransform(Matrix3 R, Vector3 b) {
		R.assign(this.localtransform);
		b.assign(this.localdisplacement);		
	}

	private double mass = 1;
	@Override
	public double getMass() {
		return mass;
	}

	public void setMass(double m) {
		this.mass = m;
	}

	@Override
	public void getLocalTranslation(Vector3 t) {
		t.assign(localdisplacement);
	}
	
	@Override
	public Object getAuxiliary() {
		return auxiliary;
	}

	@Override
	public void setAuxiliary(Object auxiliary) {
		this.auxiliary = auxiliary;
	}

}
