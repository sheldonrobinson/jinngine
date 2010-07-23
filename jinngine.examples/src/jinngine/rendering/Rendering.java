/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.rendering;

import jinngine.geometry.Geometry;
import jinngine.math.Vector3;

public interface Rendering {

	public interface Callback {
		public void tick();
	}
	
	public interface EventCallback {
		public void mousePressed(double x, double y, Vector3 point, Vector3 direction);
		public void mouseDragged(double x, double y, Vector3 point, Vector3 direction);
		public void mouseReleased();		
		public void spacePressed();
		public void spaceReleased();
	}
	
	public void drawMe( Geometry g);
	public void start();
	
}
