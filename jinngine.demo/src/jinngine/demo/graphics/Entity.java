package jinngine.demo.graphics;
import jinngine.math.*;
import jinngine.physics.Body;

public interface Entity {
	
	/**
	 * Set world coordinates for this entity (typically a visual game object)
	 * @param p
	 */
	public void setPosition( Vector3 p); 
	
	/**
	 * Get world position of entity
	 */
	public Vector3 getPosition();
	
	
	/**
	 * Get primary physics body
	 *  
	 */
	public Body getPrimaryBody();
	
	/**
	 * Set the selection property for this entity
	 * @param sh
	 */
	public void setSelected( boolean selected );
	
	/**
	 * Set the alarmed setting
	 */
	public void setAlarmed(boolean alarmed);
	
	/**
	 * Get the alarmed setting
	 * @return
	 */
	public boolean getAlarmed();


}
