/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
package jinngine.test.visual;

import jinngine.physics.PhysicsScene;

public interface Testcase {
	
	public void initScene(PhysicsScene model);
	public void deleteScene(PhysicsScene model);

}
