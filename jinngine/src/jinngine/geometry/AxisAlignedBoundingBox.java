package jinngine.geometry;
import jinngine.math.Vector3;

/**
 * A shape that encapsulates, or geometricaly bounds a given, usually more complex geometry. All geometries
 * used in jinngine are required to supply an axis aligned bounding box (AABB), because it will be used when 
 * performing broad-phase collision detection 
 * @author mo
 *
 */
public interface AxisAlignedBoundingBox {
	/**
	 * Return a vector that contains all minimum bounds on each axis. The bounds are in world coordinates in world space
	 */
	public Vector3 getMinBounds();

	/**
	 * Return a vector that contains all maximum bounds on each axis. The bounds are in world coordinates in world space
	 */
	public Vector3 getMaxBounds();
}
