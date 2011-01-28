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

import java.util.Iterator;

/**
 * An undirected graph that keeps track of connected components (groups). Each time an edge is added or removed 
 * from the graph, data structures are maintained, reflecting connected components in the graph. This means, 
 * that adding edges are roughly an O(k) operation, while removing an edge could i result in a total traversal of the graph, 
 * visiting all present edges, worst case O((n-1)^2) where n is the number of nodes. Usually this will be much 
 * cheaper, given that the graph has a low density, and is fragmented into separated components. 
 *
 * @param <T> Type that stores in nodes
 * @param <U> Type that stores in edges
 * @param <V> Type that stores in components
 */
public interface ComponentGraph<T,U,V> {

	/**
	 * Interface for supplying custom component objects. Components are used to reference the 
	 * independently connected components in the graph. It is therefore useful to be able to 
	 * store user information within the component references.
	 */
	public interface DataHandler<T,U,V>  {
		/**
		 * Called when a new component is created. This call MUST return a unique object of type 
		 * V, that has not been previously known to the ComponentGraph. Should be a O(k) operation. 
		 * @return a new unique component of type V
		 */
		public V createComponent();
		
		/**
		 * Add a new component to the graph
		 */
		public void addComponent( V component );
			
		/**
		 * Called when a component is entirely removed from the contact graph. The component that is 
		 * removed is guaranteed to have been previously returned by componentCreated(). Should be a O(k) 
		 * operation. 
		 */
		public void removeComponent( V leaving );
				
//		/**
//		 * The component remaining is merged with the component leaving. Prior to this
//		 * call, all nodes and edges will have been moved to the remaining component, using 
//		 * the usual methods. This call merely signals that the components have been merged 
//		 * together. Immediately after this call, {@link removeComponent()} will be called 
//		 * to remove the leaving component. 
//		 */
//		public void mergeComponent( V remaining, V leaving );
		
		/**
		 * Add the given node to the given component
		 */
		public void addNodeToComponent( V component, T node );
		
		/**
		 * Add the given edge to the given component
		 */
		public void addEdgeToComponent( V component, U edge );
		
		/**
		 * Remove the given node from the given component
		 */
		public void removeNodeFromComponent( V component, T node);
		
		/**
		 * Remove the given edge from the given component
		 */
		public void removeEdgeFromComponent( V component, U edge );
		
		/**
		 * Add the node to the graph data
		 */
		public void addNode( T node );
		
		/**
		 * Remove the given node from the graph data. By convention, a node will never be removed 
		 * before all its incident edges have been removed. Likewise, a node will never be removed 
		 * while it is present in any component.
		 * @param node node to be removed
		 */
		public void removeNode( T node );
		
		
		/**
		 * Return true if the given node exists in the graph
		 */
		public boolean containsNode( T node );
		
		/**
		 * Connect the node pair given by (node1,node2), and store the 
		 * given edge element
		 */
		public void addEdge( T node1, T node2, U edge );

		/**
		 * Remove the given edge between node1 and node2. Even though passing 
		 * both nodes and the edge is redundant, it may be practical for some
		 * implementations.
		 */
		public void removeEdge( T node1, T node2 );

		/**
		 * Return the edge defined between node1 and node2. If no edge is present, 
		 * null is returned.
		 */
		public U getEdge( T node1, T node2 );

		/**
		 * Return true if the edge (node1,node2) is present in the graph data
		 */
		public boolean containsEdge( T node1, T node2 );
		
		/**
		 * Return all nodes that is connected to the given node
		 */
		public Iterator<T> getConnectedNodes( T node );
		
		/**
		 * Return true if the given node has edges to other nodes
		 */
		public boolean hasConnectedNodes( T node );
		
		/**
		 * Return the component that contains the given node
		 */
		public V getNodeComponent( T node );
		
		/**
		 * Return the component that contains the given edge
		 */
		public V getEdgeComponent( U edge );
		
		/**
		 * Return all nodes in the given component
		 */
		public Iterator<T> getNodesInComponent( V component );
				
		/**
		 * Return all edges in the given component
		 */
		public Iterator<U> getEdgesInComponent( V component );
		
		/**
		 * Return all components
		 */
		public Iterator<V> getComponents();
		
		/**
		 * Return the number of components
		 */
		public int getNumberOfComponents();
		
		/**
		 * Return true if the given component contains any edges
		 */
		public boolean containsEdges( V component );
	}

	/**
	 * Node classifier for the ContactGraph
	 *
	 * @param <T> Type that stores in nodes
	 */
	public interface NodeClassifier<T> {
		/**
		 * @param node Node to classify
		 * @return true if the node is to be considered as a delimiting node, such that two
		 * components in some graph, would not be merged if connected through such a node. Returns false otherwise.
		 */
		public boolean isDelimiter(final T node);
	}
	
	/** 
	 * Add a node to the graph. If the node already exists in the graph, the call will have no effect. 
	 * @param node
	 */
	public void addNode( T node );
	
	/**
	 * Remove a node from the graph. All edges incident to this node will be removed as well. 
	 * @param node
	 */
	public void removeNode( T node );
	
	/**
	 * Add an edge to the graph, and implicitly add included end-nodes if not already present in the graph.
	 * This is roughly an O(k) and sometimes O(nodes) operation, depending on whether components are to be merged or not.
	 * @param pair A pair of nodes, where an edge is to be added between them.
	 * @param edgeelement An element of type U to store in the new edge
	 */
	public void addEdge( Pair<T> pair, U edgeelement);

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
	public boolean removeEdge( Pair<T> pair);
	
	/**
	 * Return true if the graph contains the node
	 */
	public boolean containsNode( T node1 );
	
	/**
	 * Return true if the graph contains the specified edge
	 */
	public boolean containsEdge(T node1, T node2);

	/**
	 * Return the edge defined by (node1,node2). If the edge does not exist, 
	 * then null is returned 
	 */
	public U getEdge( T node1, T node2 );
	
	
	
	
//	/**
//	 * Get the edge element of type U that is stored in the edge defined by
//	 * a pair of node types T. If no such edge exist, the return value is null.
//	 * @param pair A pair of T type objects defining an edge in the graph
//	 * @return The U type object stored in the edge. Return value is null if no such 
//	 * edge is present in the graph
//	 */
//	public U getEdge( Pair<T> pair);
//
//	/**
//	 * Return an iterator yielding the edges in the specified component. 
//	 * @param c Component to iterate
//	 * @return Iterator giving the edge elements in the component
//	 */
//	public Iterator<U> getEdgesInComponent(V c);
//
//	/**
//	 * Returns an iterator yielding the nodes present in the given component
//	 * @param c Any component of this graph
//	 * @return An iterator yielding the nodes present in the component c
//	 */
//	public Iterator<T> getNodesInComponent(V c);
//	
//	/**
//	 * Return an iterator that yields the components in the graph
//	 * @return 
//	 */
//	public Iterator<V> getComponents();
//	
////	/**
////	 * Return the number of components in this graph
////	 */
////	public int getNumberOfComponents();
//	
//	/**
//	 * Return the total number of nodes in this graph
//	 */
//	public int getNumberOfNodes();
//	
//	/**
//	 * Return the number of free nodes, which are nodes that are not a part of a graph component
//	 */
//	public int getNumberOfFreeNodes();
//	
//	
//	/**
//	 * Get all free nodes. A free node is not in any component.
//	 */
//	public Iterator<T> getFreeNodes();
//	
//	/**
//	 * Get all nodes that is connected to the given node. The constructible pairs
//	 * Pair<T> can then be used to obtain the edge type U using getEdge(Pair<T>)
//	 */
//	public Iterator<T> getConnectedNodes(T node);
//	
//	/**
//	 * Get all edges connected to the given node
//	 * @param node
//	 * @return An iterator over all edges connected to the given node
//	 */
//	public Iterator<U> getConnectedEdges(T node);

}
