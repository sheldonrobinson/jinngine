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
	private Object auxiliary;
	
	public Hull( Iterator<Vector3> input ) {
		
		//convert vectors into internal qhull representation		
		while (input.hasNext()) {
			Vector3 v = input.next();
			points.add(new Point3d(v.a1, v.a2, v.a3));
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
			
		}
		
		//search vertices to find bounds
		Vector3 extremal = new Vector3();
		for ( Vector3 v: vertices) {
			if (extremal.norm() < v.norm()) extremal.assign(v);
			if ( v.a1 < minBounds.a1 ) minBounds.a1=v.a1;
			if ( v.a2 < minBounds.a2 ) minBounds.a2=v.a2;
			if ( v.a3 < minBounds.a3 ) minBounds.a3=v.a3;
			if ( v.a1 > maxBounds.a1 ) maxBounds.a1=v.a1;
			if ( v.a2 > maxBounds.a2 ) maxBounds.a2=v.a2;
			if ( v.a3 > maxBounds.a3 ) maxBounds.a3=v.a3;
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
	private int chachedIndex = 0;
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
		
		chachedIndex = 0;
		
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
	public Body getBody() {
		return body;
	}

	@Override
	public double getEnvelope(double dt) {
		return envelope;
	}

	@Override
	public InertiaMatrix getInertialMatrix(double mass) {
		InertiaMatrix I = new InertiaMatrix();
		//approximate the inertia matrix by the encapsulating box
		double a,b,c;
		a=Math.abs(maxBounds.a1-minBounds.a1);
		b=Math.abs(maxBounds.a2-minBounds.a2);
		c=Math.abs(maxBounds.a3-minBounds.a3);
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
	public void setLocalTranslation(Vector3 b) {
		setLocalTransform(localtransform,b);
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


}
