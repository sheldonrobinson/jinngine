package jinngine.demo.graphics;

import jinngine.math.Matrix4;
import jinngine.math.Vector3;

public interface Render {
	
	/**
	 * Final call that starts the game loop thread
	 */
	public void start();
	
	/**
	 * Stop game loop thread
	 */
	public void stop();	
	

	/**
	 * Add a shape with an associated render
	 * @param render
	 * @param s
	 * @return display list id
	 */
	public void addShape(ShapeRender r, Shape s, Matrix4 transform, Entity entity);
	
	/**
	 * Get the OpenGL projection matrix
	 * @return
	 */
	public Matrix4 getProjectionMatrix();
	
	/**
	 * Get the OpenGL camera transform matrix
	 * @return
	 */
	public Matrix4 getCameraMatrix();
	
	/**
	 * Get pointer ray. This ray is the line that goes
	 * from the camera eye-point, and through the mouse
	 * position at the near-clipping plane. This line
	 * can be though of as the "direction" at which the 
	 * user is pointing with the mouse in 3d space. 
	 * @return 
	 * 
	 */
	public void getPointerRay(Vector3 p, Vector3 d);
	
	
	/**
	 * Get the camera from-to points
	 * @param from
	 * @param to
	 */
	public void getCamera( Vector3 from, Vector3 to);
}
