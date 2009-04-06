package jinngine.geometry;
import java.util.*;

import jinngine.math.Vector3;

/**
 * A shape is an explicit geometric representation. It consists of a set of vertices, all maintaining 
 * an adjacent vertex lists. This is can be used to perform hill-climbing on a shape, for instance. 
 * @author mo
 *
 */
public class Shape {
	
	
	public class Vertex {
		  private final Vector3 vertex = new Vector3(); 
		  private final List<Vertex> neighborList = new ArrayList<Vertex>();
		  
		  public Vertex(Vector3 v) {
			  vertex.assign(v);
		  }
		  
		  public Vector3 getVector() {
			  return vertex.copy();
		  }
		  
		  public void addNeighbor(Vertex vertex) {
			  neighborList.add(vertex);
		  }
		  
		  public Iterator<Vertex> getNeighborIterator() {
			  return neighborList.iterator();
		  }
		}

	
	
  private final List<Vertex> vertices = new ArrayList<Vertex>();
  
  public Shape() {
  }
  
  public void addVertex(Vertex vertex) {
	  vertices.add(vertex);
  }
  
  public void addVertex(Vector3 vertex) {
	  vertices.add(new Vertex(vertex));
  }

  
  public Vertex getFirstVertex() {
    //TODO bounds check
    return vertices.get(0);
  }
  
  public final Iterator<Vertex> getVertexIterator() {
    return vertices.iterator();
  }
  
};




