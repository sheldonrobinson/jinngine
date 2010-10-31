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
import java.util.Iterator;
import java.util.List;

import quickhull3d.Point3d;
import quickhull3d.QuickHull3D;

import jinngine.geometry.util.MassProperties;
import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Transforms;
import jinngine.math.Vector3;
import jinngine.physics.Body;

/**
 * Convex hull geometry implementation, given by a user defined set of points
 */
public class ConvexHull implements SupportMap3, Geometry, Material {

	private final List<Vector3[]> faces = new ArrayList<Vector3[]>();
	private final ArrayList<Vector3> vertices = new ArrayList<Vector3>();
	private final ArrayList<ArrayList<Integer>> adjacent;
	private final ArrayList<Vector3> dualvertices = new ArrayList<Vector3>();
	private final ArrayList<ArrayList<Integer>> dualadjacent;
	private final Vector3 centreOfMass = new Vector3();
	private final double referenceMass;
	private final int numberOfVertices;
//	private int cachedVertex = 0;

	// bounding box
	public final Box boundingBox;
	private final Vector3 boundingBoxPosition = new Vector3();
	
	private final int[]   originalVertexIndices;
	private final int[][] faceIndices;
	
	// Material
	private double friction = 0.5;
	private double restitution = 0.7;
	
	/**
	 * Computes vertex adjacency lists. Method simply runs through all faces, which are given as lists of vertex indices, and fills out 
	 * adjacency lists along the way. It also roots out duplicate adjacency entries, arising from the same pair of vertices being present
	 * two adjacent faces. The motivation for this approach is that the face index lists are the only adjacency information available from 
	 * the QuickHull3D implementation.
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
		faceIndices = hull.getFaces();
		originalVertexIndices = hull.getVertexPointIndices();
		
		// get hull vertices 
		for ( Point3d p: points) 
			vertices.add(new Vector3( p.x, p.y, p.z));
	
		// grab the number of vertices
		numberOfVertices = vertices.size();
		
		// adjacency lists for hull
		adjacent = adjacencyList(faceIndices, numberOfVertices );
		
		// go thru all faces to make the dual hull points
		for (i=0; i< faceIndices.length; i++) { 	
			//convert to Vector3 array
			Vector3[] f = new Vector3[faceIndices[i].length];
			for (int j=0; j<faceIndices[i].length; j++) {
				//Point3d p = points[faceIndices[i][j]];	
				//f[j] = new Vector3(p.x,p.y,p.z );
				// use the vectors in vertices insted of new vectors
				f[j] = vertices.get(faceIndices[i][j]);
			}

			//append face to external representation
			faces.add(f);
			
			//get face vertices
			Vector3 v1 = f[0];
			Vector3 v2 = f[1];
			Vector3 v3 = f[2];

			// set normal
			Vector3 normal = v1.sub(v2).cross(v3.sub(v2)).normalize().multiply(-1);
			
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
		
		// build the dual hull
		dualhull.build(dualvectors);
		
		// create an adjacency list for the dual hull
		dualadjacent = adjacencyList(dualhull.getFaces(), dualvertices.size());			
					
		// perform approximate mass calculation
		MassProperties masscalculation = new MassProperties( this, 0.05);
		
		// set properties
		mass = referenceMass = masscalculation.getMass();
		inertiamatrix.assign(masscalculation.getInertiaMatrix());
		centreOfMass.assign(masscalculation.getCentreOfMass());
				
		// find extremal bounds to form a bounding box
		final Vector3 positiveBounds = new Vector3( 
				this.supportPoint(new Vector3(1,0,0)).x, 
				this.supportPoint(new Vector3(0,1,0)).y, 
				this.supportPoint(new Vector3(0,0,1)).z ); 
		final Vector3 negativeBounds = new Vector3( 
				this.supportPoint(new Vector3(-1,0,0)).x, 
				this.supportPoint(new Vector3(0,-1,0)).y, 
				this.supportPoint(new Vector3(0,0,-1)).z ); 
		
		boundingBoxPosition.assign( 
				0.5*(positiveBounds.x+negativeBounds.x), 
				0.5*(positiveBounds.y+negativeBounds.y),
				0.5*(positiveBounds.z+negativeBounds.z));
	
		final Vector3 boundingBoxSideLengths = new Vector3(
				Math.abs(positiveBounds.x-negativeBounds.x),
				Math.abs(positiveBounds.y-negativeBounds.y),
				Math.abs(positiveBounds.z-negativeBounds.z));
		
		// set the initial bounding box
		this.boundingBox = new Box(boundingBoxSideLengths);
		updateBoundingBoxTransform();
	}
	
	private final void updateBoundingBoxTransform() {
		this.boundingBox.setBody(this.body);
		this.boundingBox.setLocalTransform(this.localrotation, this.localrotation.multiply(boundingBoxPosition).add(this.localtranslation));		
	}
	
	/**
	 * Get the number of vertices on the this convex hull
	 * @return
	 */
	public final int getNumberOfVertices() {
		return vertices.size();
	}
	
	/**
	 * Get the vertices of this convex hull in object space
	 * @return
	 */
	public final Iterator<Vector3> getVertices() {
		return vertices.iterator();
	}
	
	public final List<Vector3> getVerticesList() {
		return new ArrayList<Vector3>(vertices);
	}
	
	/**
	 * Get the faces of the convex hull in object space
	 * @return
	 */
	public final Iterator<Vector3[]> getFaces() {
		return faces.iterator();
	}
	
	/**
	 * Get the indices mapping the vertices to each face
	 * @return
	 */
	public final int[][] getFaceIndices() {
		return faceIndices;
	}
	
	/**
	 * Return the vertex adjacency information for this convex hull
	 * @return
	 */
	public final ArrayList<ArrayList<Integer>> getVertexAdjacencyMatrix() {
		return adjacent;
	}

	/**
	 * Mapping from each vertex index to the index of 
	 * the original vertex, given upon construction. 
	 * @return
	 */
	public final int[] getOriginalVertexIndices() {
		return originalVertexIndices;
	}
	
	// Geometry
	private Object auxiliary;
	private Body body = new Body("default");
	private double envelope = 0.225;
	private Matrix3 localrotation = Matrix3.identity();
	private Matrix4 localtransform4 = Matrix4.identity();
	private final Vector3 localtranslation = new Vector3();
	
	// Material
	private double mass = 1;
	private final InertiaMatrix inertiamatrix = new InertiaMatrix();
	
	@Override
	public Vector3 supportPoint(Vector3 direction) {
		// normals are transformed (RS^-1)
		Vector3 v = body.state.rotation.multiply(localrotation)./*scale(localscale).*/transpose().multiply(direction);
		
		// do hill climbing if the hull has a considerable number of vertices
//		if (numberOfVertices > 32) {
			//hill climb along v
			int index = 0;//cachedVertex;
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
			
			// keep the vertex
//			cachedVertex = index;
			
			// return the final support point in world space
			return body.state.rotation.multiply(localrotation./*scale(localscale).*/multiply(vertices.get(index)).add(localtranslation)).add(body.state.position);

//		} else {
//			// if not, just check each vertex
//			double value = Double.NEGATIVE_INFINITY;
//			Vector3 best = null;
//			for (Vector3 p: vertices) {
//				if (v.dot(p) > value || best == null) {
//					best = p;
//					value = v.dot(p);
//				}
//			}
//			
//			// return final support point in world space
//			return body.state.rotation.multiply(localrotation.multiply(best).add(localtranslation)).add(body.state.position);			
//		}

	}

	@Override
	public void supportFeature(Vector3 direction, List<Vector3> returnface) {
		// the support feature of CovexHull is the face with the face normal closest to the 
		// given support direction. This is accomplished by hill-climbing the dual hull, that
		// maps a vertex onto a face in the original convex hull. Therefore, this method will 
		// return a face at all times
		Vector3 v = body.state.rotation.multiply(localrotation)./*scale(localscale).*/transpose().multiply(direction);
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
			} // for each adjacent vertex (normal)
		} // while better
		
		// output the face according to the dual hull index
		for (Vector3 p: faces.get(index)) 
			returnface.add( body.state.rotation.multiply(localrotation./*scale(localscale).*/multiply(p).add(localtranslation)).add(body.state.position) );
	}

	
	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public double getEnvelope() {
		return envelope;
	}

	@Override
	public InertiaMatrix getInertiaMatrix() {
		// scale the inertia matrix in the specified mass and reference mass ratio
		return new InertiaMatrix(inertiamatrix.multiply( mass / referenceMass ));
	}

		
	@Override
	public Matrix4 getWorldTransform() {
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
	public void setLocalTransform(Matrix3 localRot, Vector3 localTransl) {
		this.localtranslation.assign(localTransl);
                this.localrotation.assign(localRot);
                localtransform4.assign( Transforms.transformAndTranslate4(localrotation, localtranslation) );
	}
	
	@Override
	public Vector3 getMaxBounds(Vector3 bounds) {
		updateBoundingBoxTransform();
		return boundingBox.getMaxBounds(bounds);
	}

	@Override
	public Vector3 getMinBounds(Vector3 bounds) {
		updateBoundingBoxTransform();
		return boundingBox.getMinBounds(bounds);
	}

	@Override
	public void getLocalTransform(Matrix3 R, Vector3 b) {
		R.assign(this.localrotation);
		b.assign(this.localtranslation);		
	}

	@Override
	public double getMass() {
		return mass;
	}

	public void setMass(double m) {
		this.mass = m;
	}

	@Override
	public void getLocalTranslation(Vector3 t) {
		t.assign(localtranslation);
	}
	
	@Override
	public Object getUserReference() {
		return auxiliary;
	}

	@Override
	public void setUserReference(Object auxiliary) {
		this.auxiliary = auxiliary;
	}
	
	public Vector3 getCentreOfMass() {
		return new Vector3(centreOfMass);
	}

	@Override
	public double getFrictionCoefficient() {
		return this.friction;
	}

	@Override
	public double getRestitution() {
		return this.restitution;
	}

	@Override
	public void setFrictionCoefficient(double f) {
		this.friction = f;
	}

	@Override
	public void setRestitution(double e) {
		this.restitution = e;
	}

	@Override
	public void setLocalScale(Vector3 s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double sphereSweepRadius() {
		return 0;
	}
	
	@Override
	public Vector3 getLocalCentreOfMass(Vector3 cm) { return cm.assign(centreOfMass); }
	
}
