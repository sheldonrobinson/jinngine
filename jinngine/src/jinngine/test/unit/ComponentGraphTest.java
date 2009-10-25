package jinngine.test.unit;
import java.util.Iterator;

import jinngine.util.ComponentGraph;
import jinngine.util.ComponentGraph.Component;
import jinngine.util.HashMapComponentGraph;
import jinngine.util.Pair;
import jinngine.util.ComponentGraph.NodeClassifier;
import junit.framework.TestCase;

public class ComponentGraphTest extends TestCase {

	/**
	 * Minimal test creating a graph with two nodes. Nodes are inserted with an edge, where one 
	 * component is expected. Then the edge is removed, and we expect zero components, because free-floating
	 * nodes are removed. 
	 */
	public void testComponentGraph() {
		
		NodeClassifier<Object> nc = new NodeClassifier<Object>() {
			@Override
			public boolean isDelimitor(Object node) {
				return false;
			}
		};
		
		ComponentGraph<Object,Object> graph = new HashMapComponentGraph<Object,Object>(nc);
		
		//some dummy nodes and an edge element
		Object n1 = new Object();
		Object n2 = new Object();
		Object e1 = new Object();
		
		//insert into graph
		graph.addEdge(new Pair<Object>(n1,n2), e1);
		
		//we expect one component containing n1 and n2
		Iterator<Component> components = graph.getComponents();
		Component c = components.next();
		
		//get nodes
		Iterator<Object> nodes = graph.getNodesInComponent(c);
		
		//poll out nodes
		Object na = nodes.next();
		Object nb = nodes.next();
		
		//elements as expected
		assertTrue ( (na==n1&&nb==n2) || (na==n2&&nb==n1) );
		
		//number of components as expected
		assertTrue( graph.getNumberOfComponents() == 1);
		
		//remove the edge
		graph.removeEdge(new Pair<Object>(n1,n2));

		//number of components should be 0, because free nodes are removed
		assertTrue( graph.getNumberOfComponents() == 0);
		
	}


}
