package jinngine.demo.graphics;

import javax.media.opengl.GL;


public interface ShapeRender {

	/**
	 * Setup initial render calls, such as GL textures
	 * @param render
	 */
	public void init(Render render, GL gl);
	
	
	/**
	 * This method will be called in every frame, prior to executing the 
	 * GL display list from renderShape()
	 * @param render
	 * @param shape
	 * @param gl
	 */
	public void preRenderShape(Render render, Shape shape, Entity entity, GL gl);
	
	/**
	 * Render a shape. The gl calls done here will be compiled into 
	 * a GL display list, and will only be called once
	 * @param render
	 * @return display list index
	 */
	public void renderShape(Render render, Shape shape, GL gl);

}
