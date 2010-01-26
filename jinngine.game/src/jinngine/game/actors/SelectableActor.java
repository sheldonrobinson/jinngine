package jinngine.game.actors;

import com.ardor3d.math.Vector2;
import com.ardor3d.scenegraph.Node;
import jinngine.math.Vector3;

public interface SelectableActor extends Actor {
	
	/**
	 * Tell this actor its selected state
	 * @param selected
	 */
	public void setSelected( boolean selected);
	
	/**
	 * A selectable actor can provide an action actor
	 * @param target
	 * @param pickpoint
	 * @return
	 */
	public Actor provideActionActor( Actor target, Node picknode, Vector3 pickpoint, Vector2 screenpos );

}
