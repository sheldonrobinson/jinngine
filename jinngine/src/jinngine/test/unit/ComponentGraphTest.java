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
import jinngine.util.ComponentGraph.NodeClassifier;
import jinngine.util.ComponentGraphHashMapDataHandler;
import jinngine.util.HashMapComponentGraph;
import jinngine.util.Pair;
import junit.framework.TestCase;

@SuppressWarnings("unused")
public class ComponentGraphTest extends TestCase {

    /**
     * Minimal test creating a graph with two nodes. Nodes are inserted with an edge, where one component is expected.
     * Then the edge is removed, and we expect zero components, because free-floating nodes are removed.
     */
    public void testComponentGraph() {

        final NodeClassifier<Object> nc = new NodeClassifier<Object>() {
            @Override
            public boolean isDelimiter(final Object node) {
                return false;
            }
        };

        // DataHandler<Object,Object,Object> ch = new DataHandler<Object,Object,Object>() {
        // public Object createComponent() { return new Object(); }
        // public void mergeComponent(Object c1, Object c2) { }
        // public void addNodeToComponent(Object component, Object node) {};
        // public void removeNodeFromComponent(Object component, Object node) {}
        // public void removeComponent(Object leaving) {}
        // public void addEdgeToComponent(Object component, Object edge) {}
        // public void removeEdgeFromComponent(Object component, Object edge) {}
        // };

        // use default hash map data handler
        final ComponentGraphHashMapDataHandler<Object, Object, Object> data = new ComponentGraphHashMapDataHandler<Object, Object, Object>(
                new ComponentGraphHashMapDataHandler.ComponentFactory<Object>() {
                    @Override
                    public Object createComponent() {
                        return new Object();
                    }
                });

        final ComponentGraph<Object, Object, Object> graph = new HashMapComponentGraph<Object, Object, Object>(nc, data);

        // some dummy nodes and an edge element
        final Object n1 = new Object();
        final Object n2 = new Object();
        final Object e1 = new Object();

        // insert the nodes
        graph.addNode(n1);
        graph.addNode(n2);

        // we expect two components, one for n1 and one for n2
        assertTrue(data.getNumberOfComponents() == 2);

        // insert into graph egde (n1->n2)
        graph.addEdge(new Pair<Object>(n1, n2), e1);

        // we expect one component containing n1 and n2
        assertTrue(data.getNumberOfComponents() == 1);

        // get the component
        final Iterator<Object> components = data.getComponents();
        final Object c = components.next();

        // component should not be null
        assertTrue(c != null);

        // get nodes
        final Iterator<Object> nodes = data.getNodesInComponent(c);

        // poll out nodes
        final Object na = nodes.next();
        final Object nb = nodes.next();

        // elements as expected (we can't know their order)
        assertTrue(na == n1 && nb == n2 || na == n2 && nb == n1);

        // number of components as expected (1)
        assertTrue(data.getNumberOfComponents() == 1);

        // remove the edge
        graph.removeEdge(new Pair<Object>(n1, n2));

        // number of components should be 2, because we have two nodes
        // and no edges
        assertTrue(data.getNumberOfComponents() == 2);

        // remove the first node
        graph.removeNode(na);

        // number of components should be 1, since there is only one
        // node in the graph
        assertTrue(data.getNumberOfComponents() == 1);

        // remove the second node
        graph.removeNode(nb);

        // nothing in the graph, number of components should be 0.
        assertTrue(data.getNumberOfComponents() == 0);
    }

    /**
     * Various small tests using graph of 5 nodes
     * 
     * n1 -- n2 -- n3 -- n4 -- n5
     */
    public void testComponentGraph1() {

        final NodeClassifier<Object> nc = new NodeClassifier<Object>() {
            @Override
            public boolean isDelimiter(final Object node) {
                return false;
            }
        };

        // use default hash map data handler
        final ComponentGraphHashMapDataHandler<Object, Object, Object> data = new ComponentGraphHashMapDataHandler<Object, Object, Object>(
                new ComponentGraphHashMapDataHandler.ComponentFactory<Object>() {
                    @Override
                    public Object createComponent() {
                        return new Object();
                    }
                });

        final ComponentGraph<Object, Object, Object> graph = new HashMapComponentGraph<Object, Object, Object>(nc, data);

        // some dummy nodes
        final Object n1 = new Object();
        final Object n2 = new Object();
        final Object n3 = new Object();
        final Object n4 = new Object();
        final Object n5 = new Object();

        // some edges
        final Object e12 = new Object();
        final Object e23 = new Object();
        final Object e34 = new Object();
        final Object e45 = new Object();

        // insert into graph
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);
        graph.addNode(n4);
        graph.addNode(n5);

        graph.addEdge(new Pair<Object>(n1, n2), e12);
        graph.addEdge(new Pair<Object>(n2, n3), e23);
        graph.addEdge(new Pair<Object>(n3, n4), e34);
        graph.addEdge(new Pair<Object>(n4, n5), e45);

        // expect 1 component
        assertTrue(data.getNumberOfComponents() == 1);

        // remove an edge
        graph.removeEdge(new Pair<Object>(n3, n4));

        // expect 2 components
        assertTrue(data.getNumberOfComponents() == 2);

        // re-insert the same edge
        graph.addEdge(new Pair<Object>(n3, n4), e34);

        // expect 1 component
        assertTrue(data.getNumberOfComponents() == 1);

        // create a new edge so the graph becomes cyclic
        final Object e51 = new Object();
        graph.addEdge(new Pair<Object>(n5, n1), e51);

        // still, one component is expected
        assertTrue(data.getNumberOfComponents() == 1);

        // again, remove e34
        graph.removeEdge(new Pair<Object>(n3, n4));

        // one component is still expected
        assertTrue(data.getNumberOfComponents() == 1);

        // layout at this point
        // n4--n5--n1--n2--n3

        // remove all edges and expect finally five components
        graph.removeEdge(new Pair<Object>(n5, n1));

        // layout at this point
        // n4--n5 n1--n2--n3

        assertTrue(data.getNumberOfComponents() == 2);

        graph.removeEdge(new Pair<Object>(n3, n2));

        // layout at this point
        // n4--n5 n1--n2 n3

        assertTrue(data.getNumberOfComponents() == 3);

        graph.removeEdge(new Pair<Object>(n1, n2));

        // layout at this point
        // n4--n5 n1 n2 n3

        assertTrue(data.getNumberOfComponents() == 4);

        graph.removeEdge(new Pair<Object>(n4, n5));

        // layout at this point
        // n4 n5 n1 n2 n3

        assertTrue(data.getNumberOfComponents() == 5);
    }

    /**
     * Tests involving delimiter nodes
     */
    public void testComponentGraph3() {

        // some dummy nodes
        final Object n1 = new Object();
        final Object n2 = new Object();
        final Object n3 = new Object();
        final Object n4 = new Object();
        final Object n5 = new Object();
        final Object d6 = new Object();

        // create a node classifier, that recognises d6 as a delimiter node
        final NodeClassifier<Object> nc = new NodeClassifier<Object>() {
            @Override
            public boolean isDelimiter(final Object node) {
                return node == d6;
            }
        };

        // use default hash map data handler
        final ComponentGraphHashMapDataHandler<Object, Object, Object> data = new ComponentGraphHashMapDataHandler<Object, Object, Object>(
                new ComponentGraphHashMapDataHandler.ComponentFactory<Object>() {
                    @Override
                    public Object createComponent() {
                        return new Object();
                    }
                });

        final ComponentGraph<Object, Object, Object> graph = new HashMapComponentGraph<Object, Object, Object>(nc, data);

        graph.addNode(n1);
        graph.addNode(d6);
        graph.addEdge(new Pair<Object>(n1, d6), new Object());

        // expect 2 components, because d6 is a delimiter
        assertTrue(data.getNumberOfComponents() == 2);

        graph.addNode(n2);
        graph.addEdge(new Pair<Object>(n2, d6), new Object());

        // expect 3 components, because d6 is a delimiter
        assertTrue(data.getNumberOfComponents() == 3);

        graph.addNode(n3);
        graph.addEdge(new Pair<Object>(n3, d6), new Object());

        // expect 4 components, because d6 is a delimiter
        assertTrue(data.getNumberOfComponents() == 4);

        graph.addEdge(new Pair<Object>(n1, n2), new Object());
        // expect 3 components, because n1 and n2 is now connected
        assertTrue(data.getNumberOfComponents() == 3);

        graph.addEdge(new Pair<Object>(n2, n3), new Object());
        // expect 2 components, because n2 and n3 is now connected
        assertTrue(data.getNumberOfComponents() == 2);

        // remove connections to the delimiter node
        graph.removeEdge(new Pair<Object>(n1, d6));
        graph.removeEdge(new Pair<Object>(n2, d6));
        graph.removeEdge(new Pair<Object>(n3, d6));

        // still expect 2 components because d6 is a delimiter
        assertTrue(data.getNumberOfComponents() == 2);
    }

    /**
     * Test that adds and removes nodes
     */
    public void testComponentGraph4() {

        // some dummy nodes
        final Object n1 = new Object();
        final Object n2 = new Object();
        final Object n3 = new Object();

        final NodeClassifier<Object> nc = new NodeClassifier<Object>() {
            @Override
            public boolean isDelimiter(final Object node) {
                return false;
            }
        };

        // use default hash map data handler
        final ComponentGraphHashMapDataHandler<Object, Object, Object> data = new ComponentGraphHashMapDataHandler<Object, Object, Object>(
                new ComponentGraphHashMapDataHandler.ComponentFactory<Object>() {
                    @Override
                    public Object createComponent() {
                        return new Object();
                    }
                });

        final ComponentGraph<Object, Object, Object> graph = new HashMapComponentGraph<Object, Object, Object>(nc, data);

        // add some nodes
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);

        // expect 3 nodes in graph
        assertTrue(data.getNumberOfComponents() == 3);

        // remove nodes one at the time
        graph.removeNode(n1);

        // expect 2 nodes in graph
        assertTrue(data.getNumberOfComponents() == 2);

        // expect 1 node in graph
        graph.removeNode(n2);
        assertTrue(data.getNumberOfComponents() == 1);

        // expect 0 nodes in graph
        graph.removeNode(n3);
        assertTrue(data.getNumberOfComponents() == 0);

    }

    /**
     * Test that adds and removes nodes in a graph with edges
     */
    public void testComponentGraph5() {

        // some dummy nodes
        final Object n1 = new Object();
        final Object n2 = new Object();
        final Object n3 = new Object();

        // dummy edges
        final Object e12 = new Object();
        final Object e23 = new Object();

        final NodeClassifier<Object> nc = new NodeClassifier<Object>() {
            @Override
            public boolean isDelimiter(final Object node) {
                return false;
            }
        };

        // use default hash map data handler
        final ComponentGraphHashMapDataHandler<Object, Object, Object> data = new ComponentGraphHashMapDataHandler<Object, Object, Object>(
                new ComponentGraphHashMapDataHandler.ComponentFactory<Object>() {
                    @Override
                    public Object createComponent() {
                        return new Object();
                    }
                });

        final ComponentGraph<Object, Object, Object> graph = new HashMapComponentGraph<Object, Object, Object>(nc, data);

        // add some nodes
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);

        // expect 3 components in the graph
        assertTrue(data.getNumberOfComponents() == 3);

        // add an edge ( n1--n2 n3 )
        graph.addEdge(new Pair<Object>(n1, n2), e12);

        // expect 3 nodes, 1 component, and 1 free node
        assertTrue(data.getNumberOfComponents() == 2);

        // add another edge ( n1--n2--n3 )
        graph.addEdge(new Pair<Object>(n2, n3), e23);

        // expect 3 nodes, 1 component, and 0 free node
        assertTrue(data.getNumberOfComponents() == 1);

    }

    /**
     * Test that adds and removes edges in a graph, and tracks free nodes
     */
    public void testComponentGraph6() {

        // some dummy nodes
        final Object n1 = new Object();
        final Object n2 = new Object();
        final Object n3 = new Object();

        // dummy edges
        final Object e12 = new Object();
        final Object e13 = new Object();
        final Object e23 = new Object();

        final NodeClassifier<Object> nc = new NodeClassifier<Object>() {
            @Override
            public boolean isDelimiter(final Object node) {
                return false;
            }
        };

        // use default hash map data handler
        final ComponentGraphHashMapDataHandler<Object, Object, Object> data = new ComponentGraphHashMapDataHandler<Object, Object, Object>(
                new ComponentGraphHashMapDataHandler.ComponentFactory<Object>() {
                    @Override
                    public Object createComponent() {
                        return new Object();
                    }
                });

        final ComponentGraph<Object, Object, Object> graph = new HashMapComponentGraph<Object, Object, Object>(nc, data);

        // add the nodes
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);

        // add edges and check
        graph.addEdge(new Pair<Object>(n1, n2), e12);
        graph.addEdge(new Pair<Object>(n1, n3), e13);
        graph.addEdge(new Pair<Object>(n2, n3), e23);

        assertTrue(data.getNumberOfComponents() == 1);

        graph.removeEdge(new Pair<Object>(n1, n2));

        assertTrue(data.getNumberOfComponents() == 1);

        graph.removeEdge(new Pair<Object>(n1, n3));

        assertTrue(data.getNumberOfComponents() == 2);

        graph.removeEdge(new Pair<Object>(n2, n3));

        assertTrue(data.getNumberOfComponents() == 3);

        graph.addEdge(new Pair<Object>(n1, n3), e13);

        assertTrue(data.getNumberOfComponents() == 2);

    }

    /**
     * Tests the removeNode and addNode methods on a small graph
     */
    public void testComponentGraph7() {

        // some dummy nodes
        final Object n1 = new Object();
        final Object n2 = new Object();
        final Object n3 = new Object();

        // dummy edges
        final Object e12 = new Object();
        final Object e13 = new Object();
        final Object e23 = new Object();

        final NodeClassifier<Object> nc = new NodeClassifier<Object>() {
            @Override
            public boolean isDelimiter(final Object node) {
                return false;
            }
        };

        // use default hash map data handler
        final ComponentGraphHashMapDataHandler<Object, Object, Object> data = new ComponentGraphHashMapDataHandler<Object, Object, Object>(
                new ComponentGraphHashMapDataHandler.ComponentFactory<Object>() {
                    @Override
                    public Object createComponent() {
                        return new Object();
                    }
                });

        final ComponentGraph<Object, Object, Object> graph = new HashMapComponentGraph<Object, Object, Object>(nc, data);

        // add the nodes
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);

        // add edges and check
        graph.addEdge(new Pair<Object>(n1, n2), e12);
        graph.addEdge(new Pair<Object>(n1, n3), e13);
        graph.addEdge(new Pair<Object>(n2, n3), e23);

        assertTrue(data.getNumberOfComponents() == 1);

        graph.removeNode(n1);

        assertTrue(data.getNumberOfComponents() == 1);

        graph.addNode(n1);

        assertTrue(data.getNumberOfComponents() == 2);
    }

    /**
     * Tests merging of components
     */
    public void testComponentGraph8() {

        // some dummy nodes
        final Object n1 = new Object();
        final Object n2 = new Object();
        final Object n3 = new Object();
        final Object n4 = new Object();

        // dummy edges
        final Object e12 = new Object();
        final Object e34 = new Object();
        final Object e14 = new Object();

        final NodeClassifier<Object> nc = new NodeClassifier<Object>() {
            @Override
            public boolean isDelimiter(final Object node) {
                return false;
            }
        };

        // use default hash map data handler
        final ComponentGraphHashMapDataHandler<Object, Object, Object> data = new ComponentGraphHashMapDataHandler<Object, Object, Object>(
                new ComponentGraphHashMapDataHandler.ComponentFactory<Object>() {
                    @Override
                    public Object createComponent() {
                        return new Object();
                    }
                });

        final ComponentGraph<Object, Object, Object> graph = new HashMapComponentGraph<Object, Object, Object>(nc, data);

        // add the nodes
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);
        graph.addNode(n4);

        // add edges
        graph.addEdge(new Pair<Object>(n1, n2), e12);
        graph.addEdge(new Pair<Object>(n3, n4), e34);

        // layout
        //
        // n1 n3
        // | |
        // n2 n4

        // we expect two components
        assertTrue(data.getNumberOfComponents() == 2);

        // add an edge so the two components will be merged
        graph.addEdge(new Pair<Object>(n1, n4), e14);

        // layout
        //
        // n1 n3
        // | \ |
        // n2 n4

        // we expect one component
        assertTrue(data.getNumberOfComponents() == 1);

        // we expect the single component to contain all the
        // nodes and edges in the graph
        final Object g = data.getNodeComponent(n1);
        assertTrue(g.equals(data.getNodeComponent(n1)));
        assertTrue(g.equals(data.getNodeComponent(n2)));
        assertTrue(g.equals(data.getNodeComponent(n3)));
        assertTrue(g.equals(data.getNodeComponent(n4)));

    }

    // public void testComponentGraph9() {
    // /* this is focused on testing that the event handler is being
    // * called appropriately when the the graph is manipulated
    // */
    //
    // //some dummy nodes
    // final Object n1 = new Object();
    // final Object n2 = new Object();
    // final Object n3 = new Object();
    // final Object n4 = new Object();
    //
    // // dummy edges
    // final Object e12 = new Object();
    // final Object e34 = new Object();
    // final Object e14 = new Object();
    //
    // // default classifier, always returning false
    // final NodeClassifier<Object> classifier = new NodeClassifier<Object>() {
    // @Override
    // public boolean isDelimiter(Object node) {
    // return false;
    // }
    // };
    //
    // final class ObjectComponent {
    // public List<Object> nodes = new ArrayList<Object>();
    // public List<Object> edges = new ArrayList<Object>();
    // }
    //
    // // list of graph components
    // final List<ObjectComponent> components = new ArrayList<ObjectComponent>();
    //
    // // create an event handler that maintains lists of nodes and edges in each component
    // final DataHandler<Object,Object,ObjectComponent> event = new DataHandler<Object,Object,ObjectComponent>() {
    // public ObjectComponent createComponent() {
    // ObjectComponent c = new ObjectComponent();
    // components.add(c);
    // return c;
    // }
    // public void mergeComponent(ObjectComponent c1, ObjectComponent c2) { }
    // public void addNodeToComponent(ObjectComponent c, Object node) {
    // c.nodes.add(node);
    // };
    // public void removeNodeFromComponent(ObjectComponent c, Object node) {
    // c.nodes.remove(node);
    // }
    // public void removeComponent(ObjectComponent leaving) {
    // components.remove(leaving);
    // }
    // public void addEdgeToComponent(ObjectComponent c, Object edge) {
    // c.edges.add(edge);
    // }
    // public void removeEdgeFromComponent(ObjectComponent c, Object edge) {
    // c.edges.remove(edge);
    // }
    // };
    //
    //
    // // the graph
    // final ComponentGraph<Object,Object,ObjectComponent> graph = new
    // HashMapComponentGraph<Object,Object,ObjectComponent>(classifier,event);
    //
    // // add the nodes
    // graph.addNode(n1);
    // graph.addNode(n2);
    // graph.addNode(n3);
    // graph.addNode(n4);
    //
    // // add edges
    // graph.addEdge(new Pair<Object>(n1,n2), e12);
    // graph.addEdge(new Pair<Object>(n3,n4), e34);
    //
    // // layout
    // //
    // // n1 n3
    // // | |
    // // n2 n4
    //
    // // we expect two components
    // assertTrue( components.size() == 2);
    //
    // // in the first component, we expect n1 and n2
    // assertTrue( components.get(0).nodes.contains(n1) && components.get(0).nodes.contains(n2) );
    //
    // // and the first component should contain the edge e12
    // assertTrue( components.get(0).edges.contains(e12) );
    //
    // // in the second component, we expect n3 and n4
    // assertTrue( components.get(1).nodes.contains(n3) && components.get(1).nodes.contains(n4) );
    //
    // // the second component should contain e34
    // assertTrue( components.get(0).edges.contains(e34) );
    //
    // }

}