package jinngine.geometry;
import jinngine.math.*;
import jinngine.physics.*;

/**
 * A general encapsulation for geometries used in jinngine. All implementation of geometry classes 
 * should implement this interface.
 * @author mo
 *
 */
public interface Geometry extends AxisAlignedBoundingBox {
	/**
	 * Return the body that is associated with this Geometry instance
	 * @return 
	 */
	public Body getBody();
	
	
	/**
	 * Specify a body to be associated with this Geometry instance
	 * @param b
	 */
	public void setBody(Body b);
	
	/**
	 * Set the local transform for this geometry. The local transform is a transform that is applied before the transform 
	 * of the associated body is applied. Specifically, this is often used to rotate and translate geometries around in the
	 * object space. More generally, it can also be used to do scaling, shearing and other transforms. This is useful, however beware 
	 * that applying interesting transformation to geometries is not automatically reflected in the physical implementation of objects in jinngine. 
	 * For instance, the box implementations is only able to handle scaling correctly, not shearing and other transforms. To correctly use such 
	 * transforms, one must also implement the calculation of the inertia tensor reflecting the resulting geometry. 
	 * 
	 * @param B Transformation matrix
	 * @param b Displacement vector
	 */
	public void setLocalTransform( Matrix3 B, Vector3 b);
	
	/**
	 * Set the local translation
	 * @param b
	 */
	public void setLocalTranslation( Vector3 b);
	
	/**
	 * Compute the inertial tensor of this geometry, as a function of mass
	 * @param mass
	 * @return
	 */
	public InertiaMatrix getInertialMatrix(double mass);
	
	
	/**
	 * Get the envelope size for this geometry. The envelope is related to narrow-phase contact determination
	 * and describes a positive distance to a geometry surface. If another geometry is placed within this
	 * distance, the two geometries are considered to be in contact.
	 * @param dt TODO
	 * @return
	 */
	public double getEnvelope(double dt);
	
	/**
	 * Manually override the envelope size for this geometry
	 * 
	 */
	public void setEnvelope(double envelope);

	/**
	 * Get the final transform for this geometry, going from object space to world space.
	 * @return 4x4 affine transformation matrix
	 */
	public Matrix4 getTransform();
	
	
	/**
	 * Get the auxiliary reference 
	 * @return
	 */
	public Object getAuxiliary();
	
	
	/**
	 * Set the auxiliary reference. This reference is a way for the user to 
	 * link geometry objects to some user space object
	 */
	public void setAuxiliary(Object aux);
	
}

