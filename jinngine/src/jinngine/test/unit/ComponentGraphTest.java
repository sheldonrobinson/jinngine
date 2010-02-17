/**
 * Copyright (c) 2008-2010  Morten Silcowitz.
 *
 * This file is part of the Jinngine physics library
 *
 * Jinngine is published under the GPL license, available 
 * at http://www.gnu.org/copyleft/gpl.html. 
 */
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
		
		//insert into graph egde (n1->n2)
		graph.addEdge(new Pair<Object>(n1,n2), e1);
		
		//we expect one component containing n1 and n2
		Iterator<Component> components = graph.getComponents();
		Component c = components.next();
		
		//get nodes
		Iterator<Object> nodes = graph.getNodesInComponent(c);
		
		//poll out nodes
		Object na = nodes.next();
		Object nb = nodes.next();
		
		//elements as expected (we can't know their order)
		assertTrue ( (na==n1&&nb==n2) || (na==n2&&nb==n1) );
		
		//number of components as expected (1) 
		assertTrue( graph.getNumberOfComponents() == 1);
		
		//remove the edge
		graph.removeEdge(new Pair<Object>(n1,n2));

		//number of components should be 0, because free nodes are removed
		assertTrue( graph.getNumberOfComponents() == 0);
		
	}

	/**
	 * Various small tests using graph of 5 nodes
	 * 
	 * n1 -- n2 -- n3 -- n4 -- n5
	 */
	public void testComponentGraph1() {
		
		NodeClassifier<Object> nc = new NodeClassifier<Object>() {
			@Override
			public boolean isDelimitor(Object node) {
				return false;
			}
		};
		
		ComponentGraph<Object,Object> graph = new HashMapComponentGraph<Object,Object>(nc);
		
		//some dummy nodes 
		Object n1 = new Object();
		Object n2 = new Object();
		Object n3 = new Object();
		Object n4 = new Object();
		Object n5 = new Object();
		
		//some edges
		Object e12 = new Object();
		Object e23 = new Object();
		Object e34 = new Object();
		Object e45 = new Object();
		
		//insert into graph
		graph.addEdge(new Pair<Object>(n1,n2), e12);
		graph.addEdge(new Pair<Object>(n2,n3), e23);
		graph.addEdge(new Pair<Object>(n3,n4), e34);
		graph.addEdge(new Pair<Object>(n4,n5), e45);
		
		//expect 1 component
		assertTrue( graph.getNumberOfComponents() == 1);
		
		//remove an edge
		graph.removeEdge(new Pair<Object>(n3,n4));
		
		//expect 2 components
		assertTrue( graph.getNumberOfComponents() == 2);
		
		//re-insert the same edge
		graph.addEdge(new Pair<Object>(n3,n4), e34);

		//expect 1 component
		assertTrue( graph.getNumberOfComponents() == 1);

		//create a new edge so the graph becomes cyclic
		Object e51 = new Object();
		graph.addEdge(new Pair<Object>(n5,n1), e51);

		//still, one component is expected
		assertTrue( graph.getNumberOfComponents() == 1);
		
		//again, remove e34
		graph.removeEdge(new Pair<Object>(n3,n4));

		//one component is still expected
		assertTrue( graph.getNumberOfComponents() == 1);
		
		//layout at this point
		// n4--n5--n1--n2--n3 
		
		//remove all edges and expect finally zero components
		graph.removeEdge(new Pair<Object>(n5,n1));
		assertTrue( graph.getNumberOfComponents() == 2);
				
		graph.removeEdge(new Pair<Object>(n3,n2));
		assertTrue( graph.getNumberOfComponents() == 2);

		graph.removeEdge(new Pair<Object>(n1,n2));
		assertTrue( graph.getNumberOfComponents() == 1);

		graph.removeEdge(new Pair<Object>(n4,n5));
		assertTrue( graph.getNumberOfComponents() == 0);	
	}

	/**
	 * Tests involving delimiter nodes
	 */
	public void testComponentGraph3() {

		//some dummy nodes 
		final Object n1 = new Object();
		final Object n2 = new Object();
		final Object n3 = new Object();
		final Object n4 = new Object();
		final Object n5 = new Object();
		final Object d6 = new Object();

		//create a node classifier, that recognises d6 as a delimiter node
		NodeClassifier<Object> nc = new NodeClassifier<Object>() {
			@Override
			public boolean isDelimitor(Object node) {
				return node==d6;
			}
		};
		
		ComponentGraph<Object,Object> graph = new HashMapComponentGraph<Object,Object>(nc);
		
		graph.addEdge(new Pair<Object>(n1,d6), new Object() );
		
		//expect 1 component, even though d6 is a delimiter
		assertTrue( graph.getNumberOfComponents() == 1);	

		graph.addEdge(new Pair<Object>(n2,d6), new Object() );

		//expect 2 components, because d6 is a delimiter
		assertTrue( graph.getNumberOfComponents() == 2);	

		graph.addEdge(new Pair<Object>(n3,d6), new Object() );
		
		//expect 3 components, because d6 is a delimiter
		assertTrue( graph.getNumberOfComponents() == 3);	

		
		graph.addEdge(new Pair<Object>(n1,n2), new Object() );
		//expect 2 components, because n1 and n2 is now connected
		assertTrue( graph.getNumberOfComponents() == 2);	

		graph.addEdge(new Pair<Object>(n2,n3), new Object() );
		//expect 2 components, because n2 and n3 is now connected
		assertTrue( graph.getNumberOfComponents() == 1);

		//remove connections to the delimiter node
		graph.removeEdge(new Pair<Object>(n1,d6));
		graph.removeEdge(new Pair<Object>(n2,d6));
		graph.removeEdge(new Pair<Object>(n3,d6));
		
		//still expect 1 component because n1, n2, and n3 is connected
		assertTrue( graph.getNumberOfComponents() == 1);
	}

}
