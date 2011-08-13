/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */

package jinngine.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jinngine.geometry.util.PolyhedralMassProperties;
import jinngine.math.InertiaMatrix;
import jinngine.math.Matrix3;
import jinngine.math.Matrix4;
import jinngine.math.Transforms;
import jinngine.math.Vector3;
import jinngine.physics.Body;
import quickhull3d.Point3d;
import quickhull3d.QuickHull3D;

/**
 * Convex hull geometry implementation, given by a user defined set of points
 */
public class ConvexHull implements SupportMap3, Geometry, Material {

    private final String name;	
	//Note to MO: Hi, here the intent is to 
    private final List<Vector3[]> faces;
    private final List<Vector3> vertices;
    private final ArrayList<ArrayList<Integer>> adjacent;
    private final ArrayList<Vector3> dualvertices = new ArrayList<Vector3>();
    private final ArrayList<ArrayList<Integer>> dualadjacent;
    private final Vector3 centreOfMass = new Vector3();
    private final double referenceMass;

    private final double sweepSphereRadius;
    // private int cachedVertex = 0;

    // bounding box
    public final Box boundingBox;
    private final Vector3 boundingBoxPosition = new Vector3();

    private final int[] originalVertexIndices;
    private final int[][] faceIndices;

    // Material
    private double friction = 0.5;
    private double restitution = 0.7;

    /**
     * Computes vertex adjacency lists. Method simply runs through all faces,
     * which are given as lists of vertex indices, and fills out adjacency lists
     * along the way. It also roots out duplicate adjacency entries, arising
     * from the same pair of vertices being present two adjacent faces. The
     * motivation for this approach is that the face index lists are the only
     * adjacency information available from the QuickHull3D implementation.
     */
    private final static ArrayList<ArrayList<Integer>> adjacencyList(final int[][] faceindices, final int numberOfVertices) {
        final ArrayList<ArrayList<Integer>> adjacent = new ArrayList<ArrayList<Integer>>();

        // create array of arrays for adjacency lists
        adjacent.ensureCapacity(numberOfVertices);
        for (int i = 0; i < numberOfVertices; i++) {
            adjacent.add(i, new ArrayList<Integer>());
        }

        // for each face
        for (final int[] face : faceindices) {
            // for each vertex
            int prevvertex = face[face.length - 1];
            for (int i = 0; i < face.length; i++) {
                // add both ways in adjacency list
                final int vertex = face[i];

                // first vertex (if not already there)
                List<Integer> adj = adjacent.get(prevvertex);
                boolean found = false;
                for (final int a : adj) {
                    if (a == vertex) {
                        found = true;
                    }
                }
                if (!found) {
                    adj.add(vertex);
                }

                // second vertex
                adj = adjacent.get(vertex);
                found = false;
                for (final int a : adj) {
                    if (a == prevvertex) {
                        found = true;
                    }
                }
                if (!found) {
                    adj.add(prevvertex);
                }

                // set next previous vertex
                prevvertex = vertex;
            }
        }

        return adjacent;
    }

    /**
     * Create a convex hull geometry, based on the points given
     * 
     * @param sweepSphereRadius positive radius of the sweepsphere. Use 0. for a normal hull.
     *
     */
    public ConvexHull(final String name, final List<Vector3> input, double sweepSphereRadius) {
        if(sweepSphereRadius<0.)
            throw new IllegalArgumentException("sweepSphereRadius shoud be positive or 0.");
        if(name==null)
            throw new NullPointerException();
        
        this.name = name; //String is immutable and can be shared safely.
        this.sweepSphereRadius = sweepSphereRadius;
        final QuickHull3D dualhull = new QuickHull3D();
        final QuickHull3D hull = new QuickHull3D();
        
        // build the hull
        hull.build(Vector3.toArray(input));

        // extract faces from the QuickHull3D implementation
        faceIndices = hull.getFaces();
        originalVertexIndices = hull.getVertexPointIndices();
        
        {// Build the vertex list from hull vertices
            final Point3d[] points = hull.getVertices();
            final List<Vector3> result = new ArrayList<Vector3>(points.length);
            for (final Point3d p : points) {
                result.add(new Vector3(p.x, p.y, p.z));
            }
            vertices=Collections.unmodifiableList(result);
        }    

        // adjacency lists for hull
        adjacent = adjacencyList(faceIndices, vertices.size());

        {// go thru all faces to make the dual hull points
            final List<Vector3[]> result = new ArrayList<Vector3[]>(faceIndices.length);
            for (int i = 0; i < faceIndices.length; i++) {
                // convert to Vector3 array
                final Vector3[] f = new Vector3[faceIndices[i].length];
                for (int j = 0; j < faceIndices[i].length; j++) {
                    // Point3d p = points[faceIndices[i][j]];
                    // f[j] = new Vector3(p.x,p.y,p.z );
                    // use the vectors in vertices insted of new vectors
                    f[j] = vertices.get(faceIndices[i][j]);
                }

                // append face to external representation
                result.add(f);

                // get face vertices
                final Vector3 v1 = f[0];
                final Vector3 v2 = f[1];
                final Vector3 v3 = f[2];

                // set normal
                final Vector3 normal = v1.sub(v2).cross(v3.sub(v2)).normalize().multiply(-1);

                // add to the dual polygon vertices (index corresponds to a face)
                dualvertices.add(normal);
            }
            faces = Collections.unmodifiableList(result);
        }
        // build the dual hull
        dualhull.build(Vector3.toArray(dualvertices));

        // create an adjacency list for the dual hull
        dualadjacent = adjacencyList(dualhull.getFaces(), dualvertices.size());

        // perform approximate mass calculation
        final PolyhedralMassProperties masscalculation = new PolyhedralMassProperties(hull, 1.);

        // set properties
        mass = referenceMass = masscalculation.getMass();

        // get the inertia matrix
        inertiamatrix=masscalculation.getInertiaMatrix();

        // translate inertia back into the centre of mass
        InertiaMatrix.inverseTranslate(inertiamatrix, mass, masscalculation.getCentreOfMass());

        // scale inertia matrix in the total mass
        inertiamatrix.assignMultiply(1.0 / mass);

        centreOfMass.assign(masscalculation.getCentreOfMass());

        // find extremal bounds to form a bounding box
        final Vector3 positiveBounds = new Vector3(supportPoint(new Vector3(1, 0, 0), new Vector3()).x, supportPoint(
                new Vector3(0, 1, 0), new Vector3()).y, supportPoint(new Vector3(0, 0, 1), new Vector3()).z);
        final Vector3 negativeBounds = new Vector3(supportPoint(new Vector3(-1, 0, 0), new Vector3()).x, supportPoint(
                new Vector3(0, -1, 0), new Vector3()).y, supportPoint(new Vector3(0, 0, -1), new Vector3()).z);

        boundingBoxPosition.assign(0.5 * (positiveBounds.x + negativeBounds.x),
                0.5 * (positiveBounds.y + negativeBounds.y), 0.5 * (positiveBounds.z + negativeBounds.z));

        final Vector3 boundingBoxSideLengths = new Vector3(Math.abs(positiveBounds.x - negativeBounds.x),
                Math.abs(positiveBounds.y - negativeBounds.y), Math.abs(positiveBounds.z - negativeBounds.z));

        // set the initial bounding box
        boundingBox = new Box("boundingBox", boundingBoxSideLengths, this.sweepSphereRadius);

        updateBoundingBoxTransform();
    }

    private final void updateBoundingBoxTransform() {
        boundingBox.setLocalTransform(localrotation, localrotation.multiply(boundingBoxPosition).add(localtranslation));
    }

   

    /**
     * Get the vertices of this convex hull in object space
     * 
     * @return an not null, unmodifiable list of mutable vectors
     */
    public final List<Vector3> getVertices() {
        return vertices;
    }


    /**
     * Get the faces of the convex hull in object space
     * 
     * @return an not null, unmodifiable list of mutable vector arrays
     */
    public final List<Vector3[]> getFaces() {
        return faces;
    }

    /**
     * Get the indices mapping the vertices to each face
     * 
     * @return
     */
    public final int[][] getFaceIndices() {
        return faceIndices;
    }

    /**
     * Return the vertex adjacency information for this convex hull
     * 
     * @return
     */
    public final ArrayList<ArrayList<Integer>> getVertexAdjacencyMatrix() {
        return adjacent;
    }

    /**
     * Mapping from each vertex index to the index of the original vertex, given
     * upon construction.
     * 
     * @return
     */
    public final int[] getOriginalVertexIndices() {
        return originalVertexIndices;
    }

    // Geometry
    private Object auxiliary;
    private Body body = new Body("default");
    private double envelope = 0.225;
    private final Matrix3 localrotation = Matrix3.identity();
    private final Matrix4 localtransform4 = Matrix4.identity();
    private final Vector3 localtranslation = new Vector3();
    private final Matrix4 worldTransform = new Matrix4();

    // Material
    private double mass = 1;
    private final InertiaMatrix inertiamatrix;
    private double correction = 2;

    @Override
    public Vector3 supportPoint(final Vector3 direction, final Vector3 result) {
        // normals are transformed (RS^-1)
        final Vector3 v = body.state.rotation.multiply(localrotation).transpose().multiply(direction);

        // do hill climbing if the hull has a considerable number of vertices
        // if (numberOfVertices > 32) {
        // hill climb along v
        int index = 0;// cachedVertex;
        double value = v.dot(vertices.get(index));
        boolean better = true;
        while (better) {
            better = false;
            // go through adjacency list and pick first improver (greedy)
            for (final int i : adjacent.get(index)) {
                final double newvalue = v.dot(vertices.get(i));
                if (newvalue > value) {
                    value = newvalue;
                    index = i;
                    better = true;
                    break;
                }
            }
        }

        // keep the vertex
        // cachedVertex = index;

        // return the final support point in world space
        return result.assign(body.state.rotation.multiply(
                localrotation./* scale(localscale). */multiply(vertices.get(index)).add(localtranslation)).add(
                body.state.position));

        // } else {
        // // if not, just check each vertex
        // double value = Double.NEGATIVE_INFINITY;
        // Vector3 best = null;
        // for (Vector3 p: vertices) {
        // if (v.dot(p) > value || best == null) {
        // best = p;
        // value = v.dot(p);
        // }
        // }
        //
        // // return final support point in world space
        // return
        // body.state.rotation.multiply(localrotation.multiply(best).add(localtranslation)).add(body.state.position);
        // }

    }

    @Override
    public void supportFeature(final Vector3 direction, final Iterator<Vector3> returnface) {
        // the support feature of CovexHull is the face with the face normal closest to the
        // given support direction. This is accomplished by hill-climbing the dual hull, that
        // maps a vertex onto a face in the original convex hull. Therefore, this method will
        // return a face at all times
        final Vector3 v = body.state.rotation.multiply(localrotation)./*
                                                                       * scale(
                                                                       * localscale
                                                                       * ).
                                                                       */transpose().multiply(direction);
        // hill climb the dual hull to find face
        int index = 0;
        double value = v.dot(dualvertices.get(index));
        boolean better = true;
        while (better) {
            better = false;
            // go through adjacency list and pick first improver (greedy)
            for (final int i : dualadjacent.get(index)) {
                final double newvalue = v.dot(dualvertices.get(i));
                if (newvalue > value) {
                    value = newvalue;
                    index = i;
                    better = true;
                    break;
                }
            } // for each adjacent vertex (normal)
        } // while better

        // output the face according to the dual hull index
        for (final Vector3 p : faces.get(index)) {
            returnface.next().assign(
                    body.state.rotation.multiply(localrotation./*
                                                                * scale(localscale
                                                                * ).
                                                                */multiply(p).add(localtranslation)).add(
                            body.state.position));
        }
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
        return new InertiaMatrix(inertiamatrix.multiply(mass / referenceMass));
    }

    @Override
    public final Matrix4 getWorldTransform() {
        return new Matrix4(worldTransform);
    }

    @Override
    public void setBody(final Body b) {
        body = b;
        // attach the bounding box to our body too
        boundingBox.setBody(b);
    }

    @Override
    public void setEnvelope(final double envelope) {
        this.envelope = envelope;
    }

    @Override
    public void setLocalTransform(final Matrix3 localRot, final Vector3 localTransl) {
        localtranslation.assign(localTransl);
        localrotation.assign(localRot);
        localtransform4.assign(Transforms.transformAndTranslate4(localrotation, localtranslation));
    }

    @Override
    public Vector3 getMaxBounds(final Vector3 bounds) {
        return boundingBox.getMaxBounds(bounds);
    }

    @Override
    public Vector3 getMinBounds(final Vector3 bounds) {
        return boundingBox.getMinBounds(bounds);
    }

    @Override
    public void getLocalTransform(final Matrix3 R, final Vector3 b) {
        R.assign(localrotation);
        b.assign(localtranslation);
    }

    @Override
    public double getMass() {
        return mass;
    }

    public void setMass(final double m) {
        mass = m;
    }

    @Override
    public void getLocalTranslation(final Vector3 t) {
        t.assign(localtranslation);
    }

    @Override
    public Object getUserReference() {
        return auxiliary;
    }

    @Override
    public void setUserReference(final Object auxiliary) {
        this.auxiliary = auxiliary;
    }

    public Vector3 getCentreOfMass() {
        return new Vector3(centreOfMass);
    }

    @Override
    public double getFrictionCoefficient() {
        return friction;
    }

    @Override
    public double getRestitution() {
        return restitution;
    }

    @Override
    public void setFrictionCoefficient(final double f) {
        friction = f;
    }

    @Override
    public void setRestitution(final double e) {
        restitution = e;
    }

    @Override
    public void setLocalScale(final Vector3 s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double sphereSweepRadius() {
        return sweepSphereRadius;
    }

    @Override
    public Vector3 getLocalCentreOfMass(final Vector3 cm) {
        return cm.assign(centreOfMass);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void update() {
        // update world transform
        Matrix4.multiply(body.getTransform(), localtransform4, worldTransform);

        // update the bounding box transform
        boundingBox.setLocalTransform(localrotation, localtranslation.add(boundingBoxPosition));
        boundingBox.update();
    }

    @Override
    public double getCorrectionVelocityLimit() {
        return correction;
    }

    @Override
    public void setCorrectionVelocityLimit(final double limit) {
        correction = limit;
    }

}
