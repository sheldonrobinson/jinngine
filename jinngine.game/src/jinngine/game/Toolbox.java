package jinngine.game;

import java.util.Iterator;

import jinngine.geometry.Geometry;
import jinngine.math.Quaternion;
import jinngine.physics.Body;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyMatrix3;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.hint.LightCombineMode;

public final class Toolbox {

	/**
	 * Set the transform of a Ardor3d node to a jinngine body
	 * @param node
	 * @param body
	 */
	public static final void setTransformFromNode( Node node, Body body) {
		ReadOnlyTransform transform = node.getTransform();
		ReadOnlyVector3 dis = transform.getTranslation();
		ReadOnlyMatrix3 or = transform.getMatrix();
		body.setPosition(dis.getX(), dis.getY(), dis.getZ());
		body.setOrientation( new jinngine.math.Matrix3(or.getValue(0,0), or.getValue(0,1), or.getValue(0,2),  
				or.getValue(1,0), or.getValue(1,1), or.getValue(1,2), 
				or.getValue(2,0), or.getValue(2,1), or.getValue(2,2)) );
	}
	
	public static final void getNodeTransform( Node node, jinngine.math.Vector3 translation, Quaternion orientation) {
		ReadOnlyTransform transform = node.getTransform();
		ReadOnlyVector3 dis = transform.getTranslation();
		ReadOnlyMatrix3 or = transform.getMatrix();
		translation.assign(dis.getX(), dis.getY(), dis.getZ());
		orientation.assign( new jinngine.math.Matrix3(or.getValue(0,0), or.getValue(0,1), or.getValue(0,2),  
				or.getValue(1,0), or.getValue(1,1), or.getValue(1,2), 
				or.getValue(2,0), or.getValue(2,1), or.getValue(2,2)) );
	}
	
	
	public static void addJinngineDebugMesh( String name, Node node, Body body) {
		//add jinngine debug geometry
		Iterator<Geometry> gj = body.getGeometries();
		while(gj.hasNext()) {
			jinngine.geometry.Geometry g = gj.next();
			if (g instanceof jinngine.geometry.Box) {
			jinngine.geometry.Box b = (jinngine.geometry.Box)g;
			node.attachChild(getJinngineDebugBox(name, b));		
			}
		}
	}
	
	
	private static Mesh getJinngineDebugBox( String name, jinngine.geometry.Box box) {
		// make the outline
		ColorRGBA[] colors = new ColorRGBA[24];
		for ( int i=0; i<colors.length; i++)
			colors[i] = new ColorRGBA(1f,0f,0f,1.0f);

		double xext=0.5, yext=0.5, zext=0.5;		
		// define outline lines for the box
		Vector3[] outline = new Vector3[]  { 
				new Vector3( xext, yext, zext), new Vector3(-xext, yext, zext),
				new Vector3( xext, yext, zext), new Vector3( xext,-yext, zext),
				new Vector3( xext, yext, zext), new Vector3( xext, yext,-zext),
				
				new Vector3(-xext, yext, zext), new Vector3(-xext,-yext, zext),
				new Vector3(-xext, yext, zext), new Vector3(-xext, yext,-zext),
				
				new Vector3( xext,-yext, zext), new Vector3(-xext,-yext, zext),
				new Vector3( xext,-yext, zext), new Vector3( xext,-yext,-zext),
				
				new Vector3(-xext,-yext, zext), new Vector3(-xext,-yext,-zext),

				new Vector3( xext, yext,-zext), new Vector3(-xext, yext,-zext),
				new Vector3( xext, yext,-zext), new Vector3( xext,-yext,-zext),

				new Vector3(-xext, yext,-zext), new Vector3(-xext,-yext,-zext),
				
				new Vector3( xext,-yext,-zext), new Vector3(-xext,-yext,-zext)	
		};

		Line line = new Line(name, outline, null, colors, null);
		line.setAntialiased(false);
        line.setModelBound(new BoundingBox());
        line.setLineWidth(1f);
        line.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        
        jinngine.math.Matrix3 R = new jinngine.math.Matrix3();
        jinngine.math.Vector3 b = new jinngine.math.Vector3();
        box.getLocalTransform(R, b);
        jinngine.math.Vector3 d =box.getDimentions();
        
		line.setTranslation(b.x, b.y, b.z);
		line.setScale(d.x, d.y, d.z);        
		return line;
	}

	
	public static SpatialController<Spatial> createSpatialControllerForBody( final Body body) {
		return new SpatialController<Spatial>() {
			public void update(final double time, final Spatial caller) {
				caller.setTranslation(body.state.position.x, body.state.position.y, body.state.position.z);
				ReadOnlyMatrix3 mat = new Matrix3(body.state.rotation.a11, body.state.rotation.a12, body.state.rotation.a13,
						body.state.rotation.a21, body.state.rotation.a22, body.state.rotation.a23, 
						body.state.rotation.a31, body.state.rotation.a32, body.state.rotation.a33);

				caller.setRotation(mat);
			}
		};
	}
	
	
}
