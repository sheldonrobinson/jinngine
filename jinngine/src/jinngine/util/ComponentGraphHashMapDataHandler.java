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

public class ComponentGraphHashMapDataHandler<T,U,V> implements ComponentGraph.DataHandler<T, U, V>{
	
	public interface ComponentFactory<V> {
		public V createComponent();
	}

	// data
	private final ComponentFactory<V>  factory;
	private final Set<V>               components      = new LinkedHashSet<V>();    
	private final Map<V,Set<T>>        componentNodes  = new LinkedHashMap<V,Set<T>>();
	private final Map<V,Set<U>>        componentEdges  = new LinkedHashMap<V,Set<U>>();	
	private final Set<T>               nodes           = new LinkedHashSet<T>();
	private final Map<T,Set<T>>        adjacency       = new LinkedHashMap<T,Set<T>>();
	private final Map<Pair<T>,U>       edges           = new LinkedHashMap<Pair<T>,U>();//


	/**
	 * Create a new ComponentGraph data handler, based on hash maps
	 * @param factory factory for creating new component instances
	 */
	public ComponentGraphHashMapDataHandler( final ComponentFactory<V> factory) {
		this.factory = factory;
	}
		
	@Override
	public V createComponent() {
		return factory.createComponent();
	}
	
	@Override
	public void addComponent(V c) {
		if (!components.contains(c)) {
			// create the new component in the data structure
			components.add(c);
			componentNodes.put(c, new HashSet<T>());
			componentEdges.put(c, new HashSet<U>());
		} else {
			throw new IllegalArgumentException("HashMapDataHandler: attempt to add a component that is already present in the graph");			
		}
	}

	@Override
	public void removeComponent(V c) {
		if (components.contains(c)) {
			// remove component from the data structure
			components.remove(c);
			componentNodes.remove(c);
			componentEdges.remove(c);
			return;
		} else {
			throw new IllegalArgumentException("HashMapDataHandler: removal of non-existing component");
		}
	}

//	@Override
//	public void mergeComponent(V remaining, V leaving) {
//		// check that components exists, otherwise do nothing		
//		if ( components.contains(remaining) && components.contains(leaving) ) {
//			return;
//		} else {
//			throw new IllegalArgumentException("HashMapDataHandler: attempt to merge one or more components that does not exist");
//		}
//	}

	@Override
	public void addNodeToComponent(V c, T node) {
		if (components.contains(c)) {
			final Set<T> nodes = componentNodes.get(c);
			
			if (!nodes.contains(node)) {
				nodes.add(node);
				return;
			} else {
				throw new IllegalArgumentException("HashMapDataHandler: attempt to add a node to a component, where the node is already present");
			}
			
		} else {
			throw new IllegalArgumentException("HashMapDataHandler: attempt to add a node to a component that do not exist");
		}
		
	}

	
	@Override
	public void removeNodeFromComponent(V c, T node) {
		if (components.contains(c)) {
			// this set always exists if c is contained in components
			final Set<T> nodes = componentNodes.get(c);
			
			if (nodes.contains(node)) {
				nodes.remove(node);
				return;
			} else {
				throw new IllegalArgumentException("HashMapDataHandler: attempt to remove a node in a component that do not contain the node");
			}
			
		} else {
			throw new IllegalArgumentException("HashMapDataHandler: attempt to remove a node from a component that do not exist");
		}
	}

	
	@Override
	public void addEdgeToComponent(V c, U e) {
		if (components.contains(c)) {
			final Set<U> edges = componentEdges.get(c);
			
			if (!edges.contains(e)) {
				edges.add(e);
				return;
			} else {
				throw new IllegalArgumentException("HashMapDataHandler: attempt to add an edge to a component, where the edge is already present");
			}
			
		} else {
			throw new IllegalArgumentException("HashMapDataHandler: attempt to add an edge to a component that do not exist");
		}
		
	}


	@Override
	public void removeEdgeFromComponent(V c, U e) {
		if (components.contains(c)) {
			// this set always exists if c is contained in components
			Set<U> edges = componentEdges.get(c);
			
			if (edges.contains(e)) {
				edges.remove(e);
				return;
			} else {
				throw new IllegalArgumentException("HashMapDataHandler: attempt to remove an edge in a component that do not contain the edge");
			}
			
		} else {
			throw new IllegalArgumentException("HashMapDataHandler: attempt to remove an edge from a component that do not exist");
		}
	}

	@Override
	public void addNode(T n) {
		if (!nodes.contains(n)) {
			nodes.add(n);
			return;
		} else {
			throw new IllegalArgumentException("HashMapDataHandler: adding a node that is already in the graph");
		}
	}

	@Override
	public void removeNode(T n) {
		if (nodes.contains(n)) {
			if (!adjacency.containsKey(n)) {
				nodes.remove(n);
			} else {
				throw new IllegalArgumentException("HashMapDataHandler.removeNodeFromGraph(): cannot remove a node that still has edges connected");			
			}
		} else {
			throw new IllegalArgumentException("HashMapDataHandler.removeNodeFromGraph(): cannot remove a node that is not in the graph");			
		}
	}

	@Override
	public void addEdge(T node1, T node2, U e) {
		final Pair<T> pair = new Pair<T>(node1, node2);
		
		if (!edges.containsKey(pair)) {
			if (nodes.contains(node1)) {
				if (nodes.contains(node2)) {
					// store the edge data
					edges.put(pair, e);

					// create neighbour map in node1
					if (adjacency.containsKey(node1)) {
						final Set<T> set1 = adjacency.get(node1);
						set1.add(node2);

					} else {
						final Set<T> set1 = new HashSet<T>();
						set1.add(node2);
						adjacency.put(node1, set1 );
					}
										
					// create neighbour map in node2
					if (adjacency.containsKey(node2)) {
						final Set<T> set2 = adjacency.get(node2);
						set2.add(node1);
					} else {
						final Set<T> set2 = new HashSet<T>();
						set2.add(node1);
						adjacency.put(node2, set2 );
					}
					
					return;
				} else {
					throw new IllegalArgumentException("HashMapDataHandler.addEdgeToGraph(): node2 is missing from the graph data");															
				}
			} else {
				throw new IllegalArgumentException("HashMapDataHandler.addEdgeToGraph(): node1 is missing from the graph data");										
			}
		} else {
			throw new IllegalArgumentException("HashMapDataHandler.addEdgeToGraph(): cannot add an edge that is already present");						
		}
		
	}

	@Override
	public void removeEdge(T node1, T node2 ) {
		final Pair<T> pair = new Pair<T>(node1, node2);
		
		if (edges.containsKey(pair)) {
			// remove the edge data
			edges.remove(pair);
			
			// remove node2 from the adjacency list of node1 
			final Set<T> set1 = adjacency.get(node1);
			set1.remove(node2);
			if (set1.isEmpty()) {
				adjacency.remove(node1);
			}
			
			// remove node1 from the adjacency list of node2
			final Set<T> set2 = adjacency.get(node2);
			set2.remove(node1);
			if (set2.isEmpty()) {
				adjacency.remove(node2);
			}

			
		} else {
			throw new IllegalArgumentException("HashMapDataHandler.addEdgeToGraph(): cannot add an edge that is already present");						
		}
		
	}

	@Override
	public Iterator<T> getConnectedNodes(T n) {
		if (adjacency.containsKey(n)) {
			return adjacency.get(n).iterator();
		} else {
			// return empty iterator
			return new Iterator<T>() {
				public boolean hasNext() {return false;}
				public T next() {return null;}
				public void remove() {}
			};
		}
	}

	@Override
	public U getEdge(T node1, T node2) {
		final Pair<T> pair = new Pair<T>(node1,node2);
		if (edges.containsKey(pair)) {
			return edges.get(pair);			
		} else {
			throw new IllegalArgumentException("HashMapDataHandler.getEdge(): the requested edge do not exist");						
		}
	}

	@Override
	public V getNodeComponent(T node) {
		// search components for the node
		for (V c: components) 
			for (T n: componentNodes.get(c))
				if (n.equals(node))
					return c;

		
		//not found, return null
		return null;
	}
	
	@Override
	public V getEdgeComponent(U edge) {
		// search components for the node
		for (V c: components) 
			for (U e: componentEdges.get(c))
				if (e.equals(edge))
					return c;

		
		//not found, return null
		return null;
	};

	@Override
	public boolean containsNode(T node) {
		return nodes.contains(node);
	}

	@Override
	public boolean containsEdge(T node1, T node2) {
		return edges.containsKey(new Pair<T>(node1,node2));
	}

	@Override
	public Iterator<T> getNodesInComponent(V c) {
		return componentNodes.get(c).iterator();
	}
	
    // specific methods
	
	@Override
	public int getNumberOfComponents() {
		//return the number of keys in the component-Nodes map
		return components.size();
	}

	@Override
	public Iterator<U> getEdgesInComponent(V c) {
		return componentEdges.get(c).iterator();
	}

	@Override
	public Iterator<V> getComponents() {
		return components.iterator();
	}

	@Override
	public boolean hasConnectedNodes(T node) {
		// if there is anything in the adjacency table, there is at least 
		// one edge
		return adjacency.containsKey(node);
	}

	@Override
	public boolean containsEdges(V component) {
		// only if componentEdges contains component as a key, can the 
		// component have any edges
		return componentEdges.containsKey(component);
	}

}
