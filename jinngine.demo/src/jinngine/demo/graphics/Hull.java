package jinngine.demo.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import jinngine.geometry.Geometry;
import jinngine.geometry.SupportMap3;
import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Transforms;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import quickhull3d.*;

public class Hull implements Shape, SupportMap3, Geometry {

	public Object getAuxiliary() {
		return auxiliary;
	}

	public void setAuxiliary(Object auxiliary) {
		this.auxiliary = auxiliary;
	}

	private final ArrayList<Point3d> points = new ArrayList<Point3d>();
	private final Point3d[] dummy  = new Point3d[1];
	private final QuickHull3D hull = new QuickHull3D();
	private final List<Vector3[]> faces = new ArrayList<Vector3[]>();
	private final ArrayList<Vector3> vertices = new ArrayList<Vector3>();
	
	private final ArrayList<ArrayList<Integer>> adjecent = new ArrayList<ArrayList<Integer>>();
	//private final ArrayList<ArrayList<Integer>> vertexToFace = new ArrayList<ArrayList<Integer>>();
	private final List<List<Integer>> vertexToFace;
	private Object auxiliary;
	
	public Hull( Iterator<Vector3> input ) {
		
		//convert vectors into internal qhull representation		
		while (input.hasNext()) {
			Vector3 v = input.next();
			points.add(new Point3d(v.x, v.y, v.z));
		}

		hull.build((Point3d[]) points.toArray(dummy));

//		System.out.println ("Vertices:");
//		Point3d[] vertices = hull.getVertices();
//		for (int i = 0; i < vertices.length; i++)
//		{ Point3d pnt = vertices[i];
//		System.out.println (pnt.x + " " + pnt.y + " " + pnt.z);
//		}

//		System.out.println ("Faces:");
//		int[][] faceIndices1 = hull.getFaces();
//		for (int i = 0; i < vertices.length; i++)
//		{ for (int k = 0; k < faceIndices1[i].length; k++)
//		{ System.out.print (faceIndices1[i][k] + " ");
//		}
//		System.out.println ("");
//		}

		
		//extract faces from the quickhull implementation
		Point3d[] points = hull.getVertices();
		int[][] faceIndices = hull.getFaces();
		
		//create array of arrays for adjacency lists 
		adjecent.ensureCapacity(points.length);
		for (int i=0;i<points.length; i++)
			adjecent.add(i, new ArrayList<Integer>());
		
		//add vertices to list
		for ( Point3d p: hull.getVertices()) {
			System.out.println("v added");
			vertices.add(new Vector3( p.x, p.y, p.z));
		}	

		//allocate vertex to face list
		vertexToFace = new ArrayList<List<Integer>>();
		for (Vector3 v: vertices) {
			v.multiply(1); //dummy
			vertexToFace.add(new ArrayList<Integer>());
		}
		
		
		//go thru all faces
		for (int i=0; i< faceIndices.length; i++) { 	
			//convert to Vector3 array
			Vector3[] f = new Vector3[faceIndices[i].length];
			for (int j=0; j<faceIndices[i].length; j++) {
				Point3d p = points[faceIndices[i][j]];
				f[j] = new Vector3(p.x,p.y,p.z );
			}

			//append face to external representation
			faces.add(f);

			
			//fill in adjacency lists
			int vertex = faceIndices[i][0];
			int first = vertex;
			//for each vertex in this case
			for (int j=1; j<faceIndices[i].length; j++) {
				//add both ways in adjacency list
				int nextVertex = faceIndices[i][j];
				adjecent.get(vertex).add(nextVertex);
				adjecent.get(nextVertex).add(vertex);
				vertex = nextVertex;
			}
			
			//connect end vertices in face
			adjecent.get(first).add(vertex);
			adjecent.get(vertex).add(first);
			

			
			//int incident = 0;
			//for each vertex in this face
			for (int j=1;j<faceIndices[i].length; j++) {
				
				//fill in vertexToFace map
				List<Integer> list = vertexToFace.get(faceIndices[i][j]);
//				if (list == null) {
//					list = new ArrayList<Integer>();
//					vertexToFace[j] = list;
//				}

				//associate this vertex with the i'th face
				list.add(i);
			}


			
		}
		
		//search vertices to find bounds
		Vector3 extremal = new Vector3();
		for ( Vector3 v: vertices) {
			if (extremal.norm() < v.norm()) extremal.assign(v);
//			if ( v.x < minBounds.x ) minBounds.x=v.x;
//			if ( v.y < minBounds.y ) minBounds.y=v.y;
//			if ( v.z < minBounds.z ) minBounds.z=v.z;
//			if ( v.x > maxBounds.x ) maxBounds.x=v.x;
//			if ( v.y > maxBounds.y ) maxBounds.y=v.y;
//			if ( v.z > maxBounds.z ) maxBounds.z=v.z;
		}
		
//		minBounds.print();
//		maxBounds.print();
		
		double max = extremal.norm()+5.0;
		minBounds.assign(new Vector3(-max,-max,-max));
		maxBounds.assign(new Vector3(max,max,max));

//		minBounds.print();
//		maxBounds.print();

		
		//assume identity local transform for now
		minBoundsTransformed.assign(minBounds);
		maxBoundsTransformed.assign(maxBounds);
		//add envelope
		//minBoundsTransformed.assign(minBoundsTransformed.minus(new Vector3(envelope,envelope,envelope)));
		//maxBoundsTransformed.assign(maxBoundsTransformed.add(new Vector3(envelope,envelope,envelope)));

		
	}
	
	@Override
	public Iterator<Vector3[]> getFaces() {
		return faces.iterator();
	}
	
	@Override
	public Matrix4 getLocalTransform() {	
		return localtransform4;
	}

	
	//jbounce.SupportMap3 overrides
	//private int chachedIndex = 0;
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
		//Matrix3.print(localtransform);
		Vector3 v = body.state.rotation.multiply(localtransform).transpose().multiply(direction);
		
		int chachedIndex = 0;
		
		//hill climb along v
		double value = v.dot(vertices.get(chachedIndex));
		boolean better = true;
		while (better) {
			better = false;
			//go through adjacency list and pick first improver (greedy)
			for ( int i: adjecent.get(chachedIndex)) {
				double newvalue = v.dot(vertices.get(i));
				if ( newvalue > value) {
					value = newvalue;
					chachedIndex = i;
					better = true;
					break;
				} 
			}
		}

		//return Matrix4.multiply(transform4, new Vector3(sv1, sv2, sv3), new Vector3());
		return body.state.rotation.multiply(localtransform.multiply(vertices.get(chachedIndex)).add(localdisplacement)).add(body.state.rCm);
	}

	@Override
	public void supportFeature(Vector3 d, double epsilon, List<Vector3> returnList) {
		//ArrayList<Integer> featureIndices = new ArrayList<Integer>();
		//List<Vector3> returnList = new ArrayList<Vector3>();
		Vector3 v = body.state.rotation.multiply(localtransform).transpose().multiply(d).normalize();

		//hill-climb to find optimal vertex
		boolean updated = true;
		double object = Double.NEGATIVE_INFINITY;
		int selectedIndex = 0;
		while (updated) {
			updated = false;
			for ( int i: adjecent.get(selectedIndex)) {
				double u = v.dot(vertices.get(i));
				if (u > object) {
					object = u;
					selectedIndex = i;
					updated = true;
					break;
				}
			}
		}
		
		//crawl along face edge to collect face vertices
//		featureIndices.add(selectedIndex);
//		returnList.add( body.state.rotation.multiply(localtransform.multiply(vertices.get(selectedIndex)).add(localdisplacement)).add(body.state.rCm) );
//		int startingIndex = selectedIndex;
//		int currentIndex = selectedIndex;
//		updated = true;
//		while (updated) {
//			updated = false;
//			for ( int i: adjecent.get(currentIndex)) {
//				double u = v.dot(vertices.get(i));
//				//if within tolerance, not the starting vertex, and not the current vertex 
//				if ( Math.abs(object - u) < 1e-3 && startingIndex !=i && currentIndex != i ) {
//					currentIndex = i;
//					//add transformed vertex to return list
//					returnList.add( body.state.rotation.multiply(localtransform.multiply(vertices.get(currentIndex)).add(localdisplacement)).add(body.state.rCm) );
//					updated = true;
//					break;
//				}
//			}
//			
//		}

		
		//*) face case
		//for each face that includes the vertex
		for (Integer faceIndex: vertexToFace.get(selectedIndex) ) {
			//compute face normal and pick if within tolerance of d
			//int faceIndex = vertexToFace[selectedIndex][i];
			
			//get face vertices
			Vector3 v1 = faces.get(faceIndex)[0];
			Vector3 v2 = faces.get(faceIndex)[1];
			Vector3 v3 = faces.get(faceIndex)[2];

			//set normal
			Vector3 faceNormal = v1.minus(v2).cross(v3.minus(v2)).normalize();
			
//			System.out.println("lala" +  faceNormal.dot(v.normalize()));
//			v.print();
			//if normal is within tolerance
			if ( Math.abs(faceNormal.dot(v.normalize())) > 0.8 ) {
				System.out.println("taken");
				//return face in WCS
				for (Vector3 vertex: faces.get(faceIndex))
					returnList.add( body.state.rotation.multiply(localtransform.multiply(vertex).add(localdisplacement)).add(body.state.rCm) );
				
				//return from face case
				return;
			}
		}

		//*) edge or point case
		returnList.add( body.state.rotation.multiply(localtransform.multiply(vertices.get(selectedIndex)).add(localdisplacement)).add(body.state.rCm) );

		//find the best possible neighbour point which can constitute an edge
		double best = Double.NEGATIVE_INFINITY;
		int neighbourIndex = 0;
		for ( int i: adjecent.get(selectedIndex)) {
			double u = v.dot(vertices.get(i));
			if ( u > best ) {
				best = u;
				neighbourIndex = i;
			}
		}
		
		//if neighbour is within threshhold, take the edge case
		if ( Math.abs(object-best) < 0.2 ){
			returnList.add( body.state.rotation.multiply(localtransform.multiply(vertices.get(neighbourIndex)).add(localdisplacement)).add(body.state.rCm) );			
		 System.out.println("edge case taken");
		}
			
		// return from edge or point case
		return;
	}

	
	public List<Vector3> supportFeature2(Vector3 d) {
		ArrayList<Integer> featureIndices = new ArrayList<Integer>();
		List<Vector3> returnList = new ArrayList<Vector3>();

		//Matrix3.print(localtransform);
		Vector3 v = body.state.rotation.multiply(localtransform).transpose().multiply(d);
		
		int chachedIndex = 0;
		
		//hill climb along v
		double value = v.dot(vertices.get(chachedIndex));
		boolean better = true;
		
		while (better) {
			better = false;
			//go through adjacency list and pick first improver (greedy)
			for ( int i: adjecent.get(chachedIndex)) {
				
				double newvalue = v.dot(vertices.get(i));
				if ( Math.abs(newvalue-value) < 1e-2) {
					
					//elements on the list
					if (featureIndices.size() > 1) {
						//check if element is same as previous
						if ( featureIndices.get(featureIndices.size()-2) != i && featureIndices.get(0) != i ) {
							featureIndices.add(i);
							chachedIndex = i;
							better = true;
							break;							
						} else {
							//don't pick this element
							continue;
						}
					}
					
					//if no elements or the last element isn't the same as this one, add a new
					featureIndices.add(i);
					chachedIndex = i;
					better = true;
					break;

				}
				//better value found, dump what we have and use the new index
				else if ( newvalue > value ) {
					featureIndices.clear();
					featureIndices.add(i);					
					value = newvalue;
					chachedIndex = i;
					better = true;
					break;
				} 
			}
		}

		//return Matrix4.multiply(transform4, new Vector3(sv1, sv2, sv3), new Vector3());
		
		for (Integer i : featureIndices) {
			returnList.add( body.state.rotation.multiply(localtransform.multiply(vertices.get(i)).add(localdisplacement)).add(body.state.rCm) );
		}
		
		//System.out.println("vertices= "+ returnList.size());

		return returnList;
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
		a=b=c=3;
		//I
		Matrix3.set( I,
				(1.0f/12.0f)*1*(b*b+c*c), 0.0f, 0.0f,
				0.0f, (1.0f/12.0f)*1*(a*a+c*c), 0.0f,
				0.0f, 0.0f, (1.0f/12.0f)*1*(b*b+a*a) );

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
		return maxBoundsTransformed.add(displacement).add(body.state.rCm);

	}

	@Override
	public Vector3 getMinBounds() {
		//displacement.assign(Matrix3.multiply(body.state.rotation, localdisplacement, new Vector3()));
		return minBoundsTransformed.add(displacement).add(body.state.rCm);
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


}

