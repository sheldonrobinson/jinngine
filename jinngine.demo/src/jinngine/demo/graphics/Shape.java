package jinngine.demo.graphics;

import java.util.Iterator;

import jinngine.math.*;

public interface Shape {
	public Matrix4 getLocalTransform();
	public Iterator<Vector3[]> getFaces();
}
