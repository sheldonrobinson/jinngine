/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */
package jinngine.test.unit;
import java.util.Iterator;

import jinngine.util.ComponentGraph;
import jinngine.util.HashMapComponentGraph;
import jinngine.util.Pair;
import jinngine.util.ComponentGraph.ComponentHandler;
import jinngine.util.ComponentGraph.NodeClassifier;
import junit.framework.TestCase;

@SuppressWarnings("unused")
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
		
		ComponentHandler<Object,Object> ch = new ComponentHandler<Object,Object>() {
			public Object newComponent() { return new Object(); }
			public void mergeComponent(Object c1, Object c2) {  }
			public void nodeAddedToComponent(Object component, Object node) {};
			public void nodeRemovedFromComponent(Object component, Object node) {}
		};
		
		ComponentGraph<Object,Object,Object> graph = new HashMapComponentGraph<Object,Object,Object>(nc,ch);
		
		//some dummy nodes and an edge element
		Object n1 = new Object();
		Object n2 = new Object();
		Object e1 = new Object();
		
		//insert into graph egde (n1->n2)
		graph.addEdge(new Pair<Object>(n1,n2), e1);
		
		//we expect one component containing n1 and n2		
		assertTrue( graph.getNumberOfComponents() == 1);
	
		// get the component
		Iterator<Object> components = graph.getComponents();
		Object c = components.next();
		
		// component should not be null
		assertTrue( c != null );
		
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
		
		ComponentHandler<Object,Object> ch = new ComponentHandler<Object,Object>() {
			public Object newComponent() { return new Object(); }
			public void mergeComponent(Object c1, Object c2) {  }
			public void nodeAddedToComponent(Object component, Object node) {};
			public void nodeRemovedFromComponent(Object component, Object node) {}
		};

		
		ComponentGraph<Object,Object,Object> graph = new HashMapComponentGraph<Object,Object,Object>(nc,ch);
		
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
		
		ComponentHandler<Object,Object> ch = new ComponentHandler<Object,Object>() {
			public Object newComponent() { return new Object(); }
			public void mergeComponent(Object c1, Object c2) {  }
			public void nodeAddedToComponent(Object component, Object node) {};
			public void nodeRemovedFromComponent(Object component, Object node) {}
		};

		
		ComponentGraph<Object,Object,Object> graph = new HashMapComponentGraph<Object,Object,Object>(nc,ch);
		
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
	
	/**
	 * Test that adds and removes nodes
	 */
	public void testComponentGraph4() {

		//some dummy nodes 
		final Object n1 = new Object();
		final Object n2 = new Object();
		final Object n3 = new Object();

		NodeClassifier<Object> nc = new NodeClassifier<Object>() {
			@Override
			public boolean isDelimitor(Object node) {
				return false;
			}
		};
		
		ComponentHandler<Object,Object> ch = new ComponentHandler<Object,Object>() {
			public Object newComponent() { return new Object(); }
			public void mergeComponent(Object c1, Object c2) {  }
			public void nodeAddedToComponent(Object component, Object node) {};
			public void nodeRemovedFromComponent(Object component, Object node) {}
		};

		
		// the graph
		ComponentGraph<Object,Object,Object> graph = new HashMapComponentGraph<Object,Object,Object>(nc,ch);

		// add some nodes
		graph.addNode( n1 );
		graph.addNode( n2 );
		graph.addNode( n3 );
				
		//expect 3 nodes in graph
		assertTrue( graph.getNumberOfNodes() == 3);
		
		// remove nodes one at the time
		graph.removeNode( n1 );
		
		//expect 2 nodes in graph
		assertTrue( graph.getNumberOfNodes() == 2);
		
		//expect 1 node in graph
		graph.removeNode( n2 );
		assertTrue( graph.getNumberOfNodes() == 1);

		//expect 0 nodes in graph
		graph.removeNode( n3 );
		assertTrue( graph.getNumberOfNodes() == 0);
		
	}
	
	/**
	 * Test that adds and removes nodes in a graph with edges
	 */
	public void testComponentGraph5() {

		//some dummy nodes 
		final Object n1 = new Object();
		final Object n2 = new Object();
		final Object n3 = new Object();
		
		// dummy edges
		final Object e12 = new Object();
		final Object e23 = new Object();

		NodeClassifier<Object> nc = new NodeClassifier<Object>() {
			@Override
			public boolean isDelimitor(Object node) {
				return false;
			}
		};
		
		ComponentHandler<Object,Object> ch = new ComponentHandler<Object,Object>() {
			public Object newComponent() { return new Object(); }
			public void mergeComponent(Object c1, Object c2) {  }
			public void nodeAddedToComponent(Object component, Object node) {};
			public void nodeRemovedFromComponent(Object component, Object node) {}
		};

		
		// the graph
		ComponentGraph<Object,Object,Object> graph = new HashMapComponentGraph<Object,Object,Object>(nc,ch);

		// add some nodes
		graph.addNode( n1 );
		graph.addNode( n2 );
		graph.addNode( n3 );
				
		// expect 3 nodes in graph
		assertTrue( graph.getNumberOfNodes() == 3);
		
		// add an edge ( n1--n2   n3 ) 
		graph.addEdge( new Pair<Object>(n1,n2), e12 );
		
		// expect 3 nodes, 1 component, and 1 free node
		assertTrue( graph.getNumberOfNodes() == 3);
		assertTrue( graph.getNumberOfComponents() == 1);
		assertTrue( graph.getNumberOfFreeNodes() == 1);
		
		// add another edge ( n1--n2--n3 )
		graph.addEdge( new Pair<Object>(n2,n3), e23);

		// expect 3 nodes, 1 component, and 0 free node
		assertTrue( graph.getNumberOfNodes() == 3);
		assertTrue( graph.getNumberOfComponents() == 1);
		assertTrue( graph.getNumberOfFreeNodes() == 0);
		
	}



	/**
	 * Test that adds and removes edges in a graph, and tracks free nodes
	 */
	public void testComponentGraph6() {

		//some dummy nodes 
		final Object n1 = new Object();
		final Object n2 = new Object();
		final Object n3 = new Object();

		// dummy edges
		final Object e12 = new Object();
		final Object e13 = new Object();
		final Object e23 = new Object();

		NodeClassifier<Object> nc = new NodeClassifier<Object>() {
			@Override
			public boolean isDelimitor(Object node) {
				return false;
			}
		};
		
		ComponentHandler<Object,Object> ch = new ComponentHandler<Object,Object>() {
			public Object newComponent() { return new Object(); }
			public void mergeComponent(Object c1, Object c2) {  }
			public void nodeAddedToComponent(Object component, Object node) {};
			public void nodeRemovedFromComponent(Object component, Object node) {}
		};


		// the graph
		ComponentGraph<Object,Object,Object> graph = new HashMapComponentGraph<Object,Object,Object>(nc,ch);

		// add the nodes
		graph.addNode(n1);
		graph.addNode(n2);
		graph.addNode(n3);
				
		// add edges and check
		graph.addEdge( new Pair<Object>(n1,n2), e12);
		graph.addEdge( new Pair<Object>(n1,n3), e12);
		graph.addEdge( new Pair<Object>(n2,n3), e12);
		
		assertTrue( graph.getNumberOfFreeNodes() == 0);

		graph.removeEdge( new Pair<Object>(n1,n2) );

		assertTrue( graph.getNumberOfFreeNodes() == 0);

		graph.removeEdge( new Pair<Object>(n1,n3) );

		assertTrue( graph.getNumberOfFreeNodes() == 1);

		graph.removeEdge( new Pair<Object>(n2,n3) );

		assertTrue( graph.getNumberOfFreeNodes() == 3);
		
		graph.addEdge(new Pair<Object>(n1,n3), e13 );

		assertTrue( graph.getNumberOfFreeNodes() == 1);

	}

	
	/**
	 * Tests the removeNode and addNode methods on a small graph
	 */
	public void testComponentGraph7() {

		//some dummy nodes 
		final Object n1 = new Object();
		final Object n2 = new Object();
		final Object n3 = new Object();

		// dummy edges
		final Object e12 = new Object();
		final Object e13 = new Object();
		final Object e23 = new Object();

		NodeClassifier<Object> nc = new NodeClassifier<Object>() {
			@Override
			public boolean isDelimitor(Object node) {
				return false;
			}
		};
		
		ComponentHandler<Object,Object> ch = new ComponentHandler<Object,Object>() {
			public Object newComponent() { return new Object(); }
			public void mergeComponent(Object c1, Object c2) {  }
			public void nodeAddedToComponent(Object component, Object node) {};
			public void nodeRemovedFromComponent(Object component, Object node) {}
		};


		// the graph
		ComponentGraph<Object,Object,Object> graph = new HashMapComponentGraph<Object,Object,Object>(nc,ch);

		// add the nodes
		graph.addNode(n1);
		graph.addNode(n2);
		graph.addNode(n3);
				
		// add edges and check
		graph.addEdge( new Pair<Object>(n1,n2), e12);
		graph.addEdge( new Pair<Object>(n1,n3), e12);
		graph.addEdge( new Pair<Object>(n2,n3), e12);
		
		assertTrue( graph.getNumberOfNodes() == 3 );
		assertTrue( graph.getNumberOfComponents() == 1 );
		assertTrue( graph.getNumberOfFreeNodes() == 0 );
		
		graph.removeNode(n1);
		
		assertTrue( graph.getNumberOfNodes() == 2 );
		assertTrue( graph.getNumberOfComponents() == 1 );
		assertTrue( graph.getNumberOfFreeNodes() == 0 );
		
		graph.addNode(n1);

		assertTrue( graph.getNumberOfNodes() == 3 );
		assertTrue( graph.getNumberOfComponents() == 1 );
		assertTrue( graph.getNumberOfFreeNodes() == 1 );
	}
	
	
	/**
	 * Tests merging of components
	 */
	public void testComponentGraph8() {

		//some dummy nodes 
		final Object n1 = new Object();
		final Object n2 = new Object();
		final Object n3 = new Object();
		final Object n4 = new Object();

		// dummy edges
		final Object e12 = new Object();
		final Object e34 = new Object();
		final Object e14 = new Object();

		NodeClassifier<Object> nc = new NodeClassifier<Object>() {
			@Override
			public boolean isDelimitor(Object node) {
				return false;
			}
		};
		
		final class dummy {
			public int data = 0;
		}
		
		// make the merge component handler return a specific object
		ComponentHandler<Object,dummy> ch = new ComponentHandler<Object,dummy>() {
			public dummy newComponent() { return new dummy(); }
			public void mergeComponent(dummy c1, dummy c2) { c1.data = 1;  }
			public void nodeAddedToComponent(dummy component, Object node) {};
			public void nodeRemovedFromComponent(dummy component, Object node) {};
		};


		// the graph
		ComponentGraph<Object,Object,dummy> graph = new HashMapComponentGraph<Object,Object,dummy>(nc,ch);

		// add the nodes
		graph.addNode(n1);
		graph.addNode(n2);
		graph.addNode(n3);
		graph.addNode(n4);
		
		// add edges
		graph.addEdge(new Pair<Object>(n1,n2), e12);
		graph.addEdge(new Pair<Object>(n3,n4), e34);
		
		// layout
		// 
		// n1   n3
		// |    |
		// n2   n4
		
		// we expect two components 
		assertTrue( graph.getNumberOfComponents() == 2);
		
		// add an edge so the two components will be merged
		graph.addEdge(new Pair<Object>(n1,n4), e14);

		// layout
		// 
		// n1   n3
		// |  \  |
		// n2   n4
		
		// we expect one component
		assertTrue( graph.getNumberOfComponents() == 1);
		
		// we expect the component to contain the data that we assigned 
		// during the merge
		Iterator<dummy> i = graph.getComponents();	
		assertTrue( i.next().data == 1 );
		
	}

}