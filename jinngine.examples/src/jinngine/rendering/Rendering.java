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

public interface Rendering {

	public interface Callback {
		public void callback();
	}

	public void drawMe( Geometry g);
	public void start();
	
}
