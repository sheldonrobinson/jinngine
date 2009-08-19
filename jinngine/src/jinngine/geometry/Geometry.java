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
	 * Set the local rotation for this geometry and displacement. The local transform is applied before the transform 
	 * of the associated body is applied. This is used to rotate and translate geometries around in the
	 * object space. This transform must not include any scaling or other transformation types. That sort of 
	 * transformations are to be handled internally by the geometry instance. 
	 * 
	 * @param R An orthonormal rotation matrix
	 * @param b Displacement vector
	 */
	public void setLocalTransform( Matrix3 R, Vector3 b);
	
	/**
	 * Get the local transform for this geometry. The rotation and displacement is assigned to 
	 * the given matrix and vector. 
	 * @param R Matrix to contain the rotation matrix of the local transform
	 * @param b A vector to contain the local transform displacement
	 */
	public void getLocalTransform( Matrix3 R, Vector3 b);
	
	/** 
	 * Get the amount of mass for this geometry
	 */
	public double getMass();
	
	/**
	 * Compute the inertia tensor of this geometry. Note that this quantity is dependent on the mass of the geometry. 
	 * The inertia tensor returned from this call must assume no local rotation or translation of the geometry instance.
	 */
	public InertiaMatrix getInertialMatrix();
	
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
	 * Get the auxiliary reference. This reference is a way for the user to 
	 * link geometry objects to some user space object 
	 * @return
	 */
	public Object getAuxiliary();
		
	/**
	 * Set the auxiliary reference. This reference is a way for the user to 
	 * link geometry objects to some user space object
	 */
	public void setAuxiliary(Object aux);
	
}

