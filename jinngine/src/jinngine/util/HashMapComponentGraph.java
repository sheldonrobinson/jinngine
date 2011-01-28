/**
 * Copyright (c) 2010-2011 Morten Silcowitz
 *
 * This file is part of jinngine.
 *
 * jinngine is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://code.google.com/p/jinngine/>.
 */
package jinngine.util;
import java.util.*;



/**
 * An undirected graph that keeps track of connected components (groups). Each time an edge is added or removed 
 * from the graph, data structures are maintained, reflecting connected components in the graph. This means, 
 * that adding edges are roughly an O(k) operation, while removing an edge could i result in a total traversal of the graph, 
 * visiting all present edges, worst case O((n-1)^2) where n is the number of nodes. Usually this will be much 
 * cheaper, given that the graph has a low density, and is fragmented into separated components. 
 *
 * @param <T> Type that stores in nodes
 * @param <U> Type that stores in edges
 */
public class HashMapComponentGraph<T,U,V> implements ComponentGraph<T,U,V> {
	
	private final NodeClassifier<T> nodeClassifier;
	
	// the data handler for the component graph
	private final DataHandler<T,U,V> data;
	
	
	/**
	 * Create a new component graph 
	 * @param clasifier a classifier for the type T, used for the connected components analysis
	 * @param componetcreator a creator for new components that arrise inside the component graph
	 */
	public HashMapComponentGraph( NodeClassifier<T> clasifier, DataHandler<T,U,V> data ) {
		this.data = data;
		this.nodeClassifier = clasifier;		
	}
	
	/**
	 * Add an edge to the graph, and implicitly add included end-nodes if not already present in the graph.
	 * This is roughly an O(k) and sometimes O(nodes) operation, depending on whether components are to be merged or not.
	 * @param pair A pair of nodes, where an edge is to be added between them.
	 * @param edgeelement An element of type U to store in the new edge
	 */
	@Override
	public final void addEdge( Pair<T> pair, U edgeelement) {
		T a = pair.getFirst(); T b = pair.getSecond();
		
		// check if the edge is already present
		if ( data.containsEdge(a, b) )
			throw new IllegalArgumentException("HashMapComponentGraph: attempt to add an edge that already exists");

		// add the edge
		data.addEdge(a, b, edgeelement);
				
		//if b is fixed, interchange a and b ( now, if b is fixed, both a and b are fixed)
		if (nodeClassifier.isDelimiter(b)) {
			T t = a;
			a = b; b = t;
		}
				
		//Cases
		//   i. Both nodes are delimiters 
		//       a) do nothing
		//  ii. One node is delimiter:
		//       a) do nothing
		// iii. No node is delimiter:
		//       a). both nodes are in a component
		//             1. the same component
		//                 do nothing
		//             2. different components
		//                 merge the one component into the other
		
		// case i a) 
		// both a and b are delimiters
		if (nodeClassifier.isDelimiter(b)) {
			//do nothing
		
		// ii) one is delimiter
		} else if (nodeClassifier.isDelimiter(a)) {
			// a is a delimiter and is connected to b.
			// add the edge to the component of b (a remains in its own component)
			V g = data.getNodeComponent(b);
			data.addEdgeToComponent(g, edgeelement);				
		// non of the nodes are delimiters
		} else {
			V ca = data.getNodeComponent(a);
			V cb = data.getNodeComponent(b);
			// in the same component
			if (ca.equals(cb)) {				
				//add edge to this component
				data.addEdgeToComponent(ca, edgeelement);
		    // different components, merge the two components
			} else {
				// call the user handler to say we are merging gb into ga
				// data.mergeComponent(ca, cb);
				
				// create a new component
				V merged = data.createComponent();
				data.addComponent(merged);				
				
				// put nodes in component a into the new component 
				Iterator<T> i = data.getNodesInComponent(ca);
				while (i.hasNext())
					data.addNodeToComponent(merged, i.next());
				
				// put the edges in component a into the new component
				Iterator<U> ei = data.getEdgesInComponent(ca);
				while (ei.hasNext())
					data.addEdgeToComponent(merged, ei.next());

				
				// put nodes in component b into the new component
				i = data.getNodesInComponent(cb);
				while (i.hasNext())
					data.addNodeToComponent(merged, i.next());
				
				// put the edges in component b into the new component
				ei = data.getEdgesInComponent(cb);
				while (ei.hasNext())
					data.addEdgeToComponent(merged, ei.next());

				// add the new edge to the component of a
				data.addEdgeToComponent(merged, edgeelement);

				// remove the components of a and b from the component table
				data.removeComponent(ca);
				data.removeComponent(cb);
			}
		}
	}
	
	/**
	 * Remove an edge. If the removal results in one or more isolated nodes, these will be removed 
	 * from the graph implicitly. 
 	 * 
	 * For non-dense and relatively fragmented graphs, this operation will be cheap. Otherwise, for
	 * dense and strongly connected graphs, the operation could include a full traversal of the graph visiting all
	 * present edges, resulting in an O((n-1)^2) operation, where n is the number of nodes in the graph.
	 * @param pair edge to be removed
	 * @return true if the edge was actually removed, false if the edge did not exists before call.
	 */
	@Override
	public final boolean removeEdge( Pair<T> pair) {
		T a = pair.getFirst(); T b = pair.getSecond();
		
		// check if the edge is already present
		if ( !data.containsEdge(a, b) )
			throw new IllegalArgumentException("HashMapComponentGraph: attempt to remove an edge that do not exist");
		
		// remove the edge
		U edge = data.getEdge(a,b);
		data.removeEdge(a, b);
		

		//if b is delimiter, interchange a and b ( now, if b is a delimiter, both a and b are delimiters)
		if (nodeClassifier.isDelimiter(b)) {
			T t = a;
			a = b; b = t;
		}

		//Cases
		//   i. Both node are delimiters 
		//        do nothing
		//  ii. One node is delimiter:
		//       a). non-delimiter is in a component
		//             do nothing (node could now be alone in its component)
		//             if node contains no other edges, delete it from its component
		// iii. No node is delimiter:
		//       c). both nodes are in a component
		//             1. the same component
		//                 remove edge, traverse breadth-first from each node to determine 
		//                 if component should be split. 

		// both nodes are delimiters
		if ( nodeClassifier.isDelimiter(b)) {
			//do nothing
		} else if ( nodeClassifier.isDelimiter(a)) {
			// one node is delimiter	
			// b has edges left. remove the edge from the component of b
			V g = data.getNodeComponent(b);
			data.removeEdgeFromComponent(g, edge);			
		} else {
			// none is fixed
			// if b has edges, interchange a and b 
			// ( now, if b has edges, both a and b have edges)
			if (data.hasConnectedNodes(b)) {
				T t = a;
				a = b; b = t;
			}

			// both are in the same group (only possible option)
			V oldgroup = data.getNodeComponent(a);

			// both have edges
			if (data.hasConnectedNodes(b)) {

				// perform breadth-first traversal, 
				// to determine if group has become disjoint
				boolean disjoint = true;
				Queue<T> queue = new LinkedList<T>();
				Set<T> blueNodes = new LinkedHashSet<T>();
				Set<U> blueEdges = new LinkedHashSet<U>();
				Set<T> redNodes = new LinkedHashSet<T>();
				Set<U> redEdges = new LinkedHashSet<U>();
				
				blueNodes.add(a); redNodes.add(b);				
				queue.add(a); queue.add(b);

				// traverse
				while (!queue.isEmpty()) {
					T node = queue.poll();					
					Set<T> thisColorNodes;
					Set<U> thisColorEdges;
					Set<T> otherColorNodes;

					// assign sets according to colour of current node
					if ( blueNodes.contains(node)) {
						thisColorEdges = blueEdges;
						thisColorNodes = blueNodes;
						otherColorNodes = redNodes;
					} else {
						thisColorEdges = redEdges;
						thisColorNodes = redNodes;
						otherColorNodes = blueNodes;
					}
					
					// add node neighbours to queue
					Iterator<T> neighbors = data.getConnectedNodes(node);
					while (neighbors.hasNext()) { 
						T neighbor = neighbors.next();
					
						// store edges
						thisColorEdges.add( data.getEdge(node,neighbor));
						
						// ignore delimiter neighbours
						if (nodeClassifier.isDelimiter(neighbor)) {
							//ignore fixed nodes
							continue;
						} else if (otherColorNodes.contains(neighbor) ) {
							// component is still connected
							disjoint = false;
							break;
						} else if (thisColorNodes.contains(neighbor)) {
							// already visited 
							continue;							
						} else {
							// colour new node in this color, and add it to the queue
							thisColorNodes.add(neighbor);
							queue.add(neighbor);
							continue;
						}
					} // while neighbours
				} // while queue

				// handle result of traversal
				if (disjoint) {
					// after removing the edge, the component that contained 
					// a and b has become disjoint. We remove the old component,
					// and create two new components, one for each part 
					
					// remove old component
					data.removeComponent(oldgroup);
					
					// new components
					V blue = data.createComponent();
					V red =  data.createComponent();

					// add components
					data.addComponent(blue);
					data.addComponent(red);
					
					// add edges and nodes to the two new components,
					// starting with the blue nodes
					Iterator<T> ni = blueNodes.iterator();
					while (ni.hasNext())
						data.addNodeToComponent(blue, ni.next());

					// and the blue edges
					Iterator<U> ei = blueEdges.iterator();
					while (ei.hasNext())
						data.addEdgeToComponent(blue, ei.next());
					
					// the red nodes
					ni = redNodes.iterator();
					while (ni.hasNext())
						data.addNodeToComponent(red, ni.next());

					// and the red edges
					ei = redEdges.iterator();
					while (ei.hasNext())
						data.addEdgeToComponent(red, ei.next());
					

				} else {
					// component is still connected, simply
					// remove the edge
					data.removeEdgeFromComponent(oldgroup, edge);
				}
			} else if (data.hasConnectedNodes(a)){
				// a and b are in the same component. a has an edge and b do not
				// remove b and the edge from the common component					
				data.removeNodeFromComponent(oldgroup, b);
				data.removeEdgeFromComponent(oldgroup, edge);
				
				// b should now be placed in its own component
				V newcomp = data.createComponent();
				data.addComponent(newcomp);
				data.addNodeToComponent(newcomp, b);				
			} else {
				// a and b are in the same component, but after removing the edge between them
				// non of them have any edges left. They are now isolated in each their own component
				data.removeComponent(oldgroup);
				
				// new components
				V blue = data.createComponent();
				V red =  data.createComponent();
				
				// add the new components to the graph
				data.addComponent(blue);
				data.addComponent(red);
				
				// add each node to each new component
				data.addNodeToComponent(blue, a);
				data.addNodeToComponent(red, b);
			} // if non have edges
		} //if none is fixed

		// done
		return true;
	}
	
	public void addNode(T n) {
		// check that this node is not already in the graph
		if (data.containsNode(n) ) {
			throw new IllegalArgumentException("HashMapComponentGraph.addNode(): Node is already in graph");
		} else {
			// add new component
			V component = data.createComponent();
			data.addComponent(component);			
			
			// add the new node to the graph and add it to the new component
			data.addNode(n);
			data.addNodeToComponent(component, n);
		}
	}
	
	public void removeNode(T n) {		
		// remove each incident edges. store edges in intermediate list to avoid
		// concurrent modification errors
		List<T> edgesToRemove = new ArrayList<T>();
		Iterator<T> neighbors = data.getConnectedNodes(n);
		while (neighbors.hasNext()) {
			edgesToRemove.add(neighbors.next());
		}
		
		// remove each edge
		for (T incident: edgesToRemove ) 
			this.removeEdge(new Pair<T>(n,incident));
	
		// get the node component
		V c = data.getNodeComponent(n);

		// since we removed all edges to this node, 
		// it is now alone it its own component. We remove it
		data.removeComponent(c);
		
		// finally, remove the node
		data.removeNode(n);
	}

	@Override
	public boolean containsNode(T node1) {
		return data.containsNode(node1);
	}

	@Override
	public boolean containsEdge(T node1, T node2) {
		return data.containsEdge(node1, node2);
	}

	@Override
	public U getEdge(T node1, T node2) {
		return data.getEdge(node1, node2);
	}
	
}
