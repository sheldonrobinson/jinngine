package jinngine.stuff;
import java.util.*;

import jinngine.geometry.Shape;
import jinngine.math.Vector3;

public interface ConvexHullAlgorithm { 
  public void run( List<Vector3> vectorList, Shape shape );
}

