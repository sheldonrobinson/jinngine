package jinngine.util;
import java.util.*;


/**
 * An undirected graph that keeps track of connected components (groups). Each time an edge is added or removed 
 * from the graph, data structures are maintained, reflecting connected components in the graph. This means, 
 * that adding edges are roughly an O(k) operation, while removing an edge could i result in a total traversal of the graph, 
 * visiting all present edges, worst case O((n-1)^2) where n is the number of nodes. Usually this will be much 
 * cheaper, given that the graph has a low density, and is fragmented into separated components. 
 * 
 * @author Silcowitz
 *
 * @param <T> Type that stores in nodes
 * @param <U> Type that stores in edges
 */
public class ComponentGraph<T,U> {
	
	public final class Group {}
//	private final class Edge {
//		public Edge(U element) {
//		public final U element;
//	}
	public final class Node {
		public Node(T element) {this.element = element;}
		public final T element;
		public int color;
		
		public final int hashCode() {
			return element.hashCode();
		}
		public final boolean equals( Object other ) {
//			if (this == other) return true;
//			if (other == null ) return false;
			return element == ((Node)other).element;
		}

	}
	
	/**
	 * Node classifier for the ContactGraph
	 * @author Silcowitz
	 *
	 * @param <T> Type that stores in nodes
	 */
	public interface NodeClassifier<T> {
		/**
		 * @param node Node to classify
		 * @return true if the node is to be considered as a delimiting node, such that two
		 * components in some graph, would not be merged if connected through such a node. Returns false otherwise.
		 */
		public boolean isDelimitor(final T node);
	}
		
	public final Map<Group,Set<Node>>            groups = new HashMap<Group,Set<Node>>();
	public  final Map<Group,Set<Pair<T>>>  groupPairs = new HashMap<Group,Set<Pair<T>>>();
	private final Map<Node,Set<Node>>                 edges = new HashMap<Node,Set<Node>>();
	private final Map<Node,Group>                  group = new HashMap<Node,Group>();
	private final Map<Node,Node>                        nodes = new HashMap<Node,Node>();
	public final Map<Pair<T>,U>                        alledges = new HashMap<Pair<T>,U>();
	
	private final NodeClassifier<T> nodeClassifier;

	/**
	 * 
	 * @param nodeClassifier a classifier for the type T, used for the connected components analysis
	 */
	public ComponentGraph( NodeClassifier<T> nodeClassifier ) {
		this.nodeClassifier = nodeClassifier;
	}
	
	/**
	 * Add an edge to the graph, and implicitly add included end-nodes if not already present in the graph.
	 * This is roughly an O(k) and sometimes O(nodes) operation, depending on whether components are to be merged or not.
	 * @param pair A pair of nodes, where an edge is to be added between them.
	 * @param edgeelement An element of type U to store in the new edge
	 */
	public void addEdge( Pair<T> pair, U edgeelement) { 
		
		//do not act if edge is already present
		if (alledges.containsKey(pair)) {
			alledges.put(pair,edgeelement);
			return;
		}
		alledges.put(pair,edgeelement);
		
		
		//get nodes from the node tables
		Node a = new Node(pair.getFirst());
		if ( nodes.containsKey(a)) 
			a = nodes.get(a); else nodes.put(a,a);
		
		Node b = new Node(pair.getSecond());
		if ( nodes.containsKey(b)) 
			b = nodes.get(b); else nodes.put(b,b);
		
		//if b is fixed, interchange a and b ( now, if b is fixed, both a and b are fixed)
		if (nodeClassifier.isDelimitor(b.element)) {
			Node t = a;
			a = b; b = t;
		}
		
		//add edge to nodes
		if (!edges.containsKey(a))
			edges.put(a, new HashSet<Node>());
		if (!edges.containsKey(b))
			edges.put(b, new HashSet<Node>());
		edges.get(b).add(a);
		edges.get(a).add(b);
		
		//Cases
		//   i. Both bodies are fixed 
		//        do nothing
		//  ii. One body is fixed:
		//       a). non-fixed is in a group
		//             do nothing
		//       b). non-fixed is not in a group
		//             create a new group for the non-fixed body
		// iii. No body is fixed:
		//       a). no body is in a group
		//             create a new group for both bodies
		//       b). one body is in a group
		//             add the new body to this group
		//       c). both bodies are in a group
		//             1. the same group
		//                 do nothing
		//             2. different groups
		//                 merge the one group into the other
		
		//both a and b are delimitors
		if (nodeClassifier.isDelimitor(b.element)) {
			//do nothing
			
		//one is fixed
		} else if (nodeClassifier.isDelimitor(a.element)) {
			//if b is not in a group, create a new one for b
			if (!group.containsKey(b)) {
				Group g = new Group();
				group.put(b, g);
				groups.put(g, new HashSet<Node>());
				groups.get(g).add(b);
				
				//add to pairs
				groupPairs.put(g, new HashSet<Pair<T>>());
				groupPairs.get(g).add(pair);
				//return;
			} else {
				//add to pairs
				Group g = group.get(b);
				groupPairs.get(g).add(pair);
			}
		//non of the bodies are fixed
		} else {

			//if b is in a group, interchange a and b 
			//( now, if b is in a group, both a and b are grouped)
			if (group.containsKey(b)) {
				Node t = a;
				a = b; b = t;
			}

			//both grouped
			if (group.containsKey(b)) {
				//same group
				if (group.get(a) == group.get(b)) {
					//do nothing
					//add pair to this group
					groupPairs.get(group.get(a)).add(pair);
				//different groups
				} else {
					//System.out.println("Merging");
					
					//merge groups (remove the gb group)  
					Group ga = group.get(a);
					Group gb = group.get(b);
					
					//update the group table, i.e. update bodies that was tied to group gb, to ga
					Iterator<Node> i = groups.get(gb).iterator();
					while (i.hasNext()) {
						Node body = i.next();
						group.put(body, ga);
					}
					
					//put nodes in group b into group a
					groups.get(ga).addAll(groups.get(gb));
					groupPairs.get(ga).addAll(groupPairs.get(gb));
					
					//also, add the new edge (pair) 
					groupPairs.get(ga).add(pair);
					
					//remove the gb group from the groups table
					groups.remove(gb);
					groupPairs.remove(gb);
					//return;
				}
			//one group
			} else if (group.containsKey(a)) {
				//assign b to the group of a
				Group g = group.get(a);
				group.put(b, g);
				groups.get(g).add(b);
				groupPairs.get(g).add(pair);
				//return;
			//no groups
			} else {
				//create a new group for both bodies
				Group newGroup = new Group();
				group.put(a, newGroup); 
				group.put(b, newGroup);
				groups.put(newGroup, new HashSet<Node>());
				groups.get(newGroup).add(a);
				groups.get(newGroup).add(b);
				
				groupPairs.put(newGroup, new HashSet<Pair<T>>());
				groupPairs.get(newGroup).add(pair);
				//return;
			}
						
		}
		
//		System.out.println("After add: " + 		groups.keySet().size() + " groups with " + group.size() + " bodies"  );
		Iterator<Group> groupiter = groups.keySet().iterator();

		Set<Pair<T>> allpairs = new HashSet<Pair<T>>();
		Set<Node> allnodes = new HashSet<Node>();
		while(groupiter.hasNext()){
			Group g = groupiter.next();
			//System.out.println( "Group " + g + " : " + groupPairs.get(g).size() + " pairs " );
			
			Iterator<Pair<T>> pairiter = groupPairs.get(g).iterator(); 
			while (pairiter.hasNext()) {
				Pair<T> thispair = pairiter.next();
				//System.out.println( "    pair:"+thispair.hashCode());
				if (allpairs.contains(thispair)) {
					System.out.println("Duplicates!!!!");
					System.exit(0);
				}
				allpairs.add(thispair);	

			}

			
			Iterator<Node> nodeiter = groups.get(g).iterator(); 
			while (nodeiter.hasNext()) {
				Node node = nodeiter.next();
				//System.out.println( "     Node:"+node);
				if (allnodes.contains(node)) {
					System.out.println("Duplicates!!!!");
					System.exit(0);
				}
				allnodes.add(node);	

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
	public boolean removeEdge( Pair<T> pair) {
		//don't act if edge is not  present
		if (!alledges.containsKey(pair))
			return false; else alledges.remove(pair);

		Node a = new Node(pair.getFirst());
		if ( nodes.containsKey(a)) 
			a = nodes.get(a); else nodes.put(a,a);		
		Node b = new Node(pair.getSecond());
		if ( nodes.containsKey(b)) 
			b = nodes.get(b); else nodes.put(b,b);


		//if b is fixed, interchange a and b ( now, if b is fixed, both a and b are fixed)
		if (nodeClassifier.isDelimitor(b.element)) {
			Node t = a;
			a = b; b = t;
		}

		//remove edge from nodes O(k)
		edges.get(a).remove(b);
		edges.get(b).remove(a);

		if (edges.get(a).isEmpty())
			edges.remove(a);

		if (edges.get(b).isEmpty())
			edges.remove(b);


		//Cases
		//   i. Both bodies are fixed 
		//        do nothing
		//  ii. One body is fixed:
		//       a). non-fixed is in a group
		//             do nothing (body could now be alone in its group)
		//             if body contains no other edges, delete it from its group
		//       b). non-fixed is not in a group (not possible)
		//             do nothing
		// iii. No body is fixed:
		//       a). no body is in a group (not possible)
		//             do nothing
		//       b). one body is in a group (not possible)
		//             do nothing
		//       c). both bodies are in a group
		//             1. the same group
		//                 remove edge, traverse breadth-first from each body to determine 
		//                 if group should be split. 
		//             2. different groups (not possible)
		//                 do nothing

		//both nodes are fixed
		if ( nodeClassifier.isDelimitor(b.element)) {
			//do nothing
			//return;
			//one is fixed
		} else if ( nodeClassifier.isDelimitor(a.element)) {
			if (group.containsKey(b)) { //(only possible option)
				//System.out.println("One fixed node");
				Group g = group.get(b);

				//check for another edge on this node
				if (!edges.containsKey(b)) {
					//System.out.println("b did not have any edges");
					//remove the body from group
					group.remove(b);
					Set<Node> s = groups.get(g);
					if (!s.remove(b)) {
						System.out.println("ALARM");
						System.exit(0);
					}
					//remove group if empty
					if (s.isEmpty()) {
						//System.out.println("groups entry removed");
						groups.remove(g);
					} else {
						System.out.println("Group isn't empty, why??");
						//System.exit(0);

					}
				} else {
					//System.out.println("b has edges left, and is part of a group");

				}



				//remove pair from group (even if b was not removed from the group)
				Set<Pair<T>> sp = groupPairs.get(g);
				sp.remove(pair);
				//remove group if empty
				if (sp.isEmpty()) {
					//System.out.println("grouppair entry removed " + g );
					groupPairs.remove(g);
				}

			} else {
				System.out.println("What?");
				System.exit(0);
			}
			//return;
			//none is fixed
		} else {

			//if b has edges, interchange a and b 
			//( now, if b has edges, both a and b have edges)
			if (edges.containsKey(b)) {
				Node t = a;
				a = b; b = t;
			}

			//both are in the same group (only possible option)
			Group oldgroup = group.get(a);

			if (oldgroup != group.get(b)) {
				System.out.println("Different groups??!");
				System.exit(0);
			}
			//both have edges
			if (edges.containsKey(b)) {
				final int NONE = 0;
				final int RED  = 1;
				final int BLUE = 2;

				//clear node colors in entire group
				Iterator<Node> i = groups.get(oldgroup).iterator();
				while (i.hasNext()) {
					i.next().color = NONE;
				}


				// perform breadth-first traversal, 
				// to determine if group has become disjoint
				boolean disjoint = true;
				Queue<Node> queue = new LinkedList<Node>();
				Set<Pair<T>> blueEdges = new HashSet<Pair<T>>();
				a.color = RED;
				b.color = BLUE;
				queue.add(a); queue.add(b);

				//traverse
				while (!queue.isEmpty()) {
					Node node = queue.poll();

					//add nodes neighbors to queue
					Iterator<Node> neighbors = edges.get(node).iterator();
					while (neighbors.hasNext()) { 
						Node neighbor = neighbors.next();

						//remember visited edges
						if (node.color == BLUE)
							blueEdges.add(new Pair<T>(node.element,neighbor.element));

						if (nodeClassifier.isDelimitor(neighbor.element)) {
							//ignore fixed nodes
							continue;
						} else if (neighbor.color == NONE ) {
							neighbor.color = node.color;
							queue.add(neighbor);
							continue;
						} else if (neighbor.color != node.color ) {
							//group is connected
							disjoint = false;
							break;
						} else {
							//already visited 
							continue;
						}
					} //while neighbors
				} // while queue

				//handle result of traversal
				if (disjoint) {
					//System.out.println("Splitting group");

					//new group
					Group newgroup = new Group();

					Set<Node> blues = new HashSet<Node>();

					//find all blue nodes
					Iterator<Node> iter = groups.get(oldgroup).iterator();
					while (iter.hasNext()) {
						Node node = iter.next();
						if (node.color == BLUE ) { 
							blues.add(node);
							group.put(node, newgroup);
						}
					} 

					// impossible
					if (blues.isEmpty()) {
						System.out.println("Why was no blue nodes found?");
						System.exit(0);
					}

					//remove bodies from old groups and add the new group
					groups.get(oldgroup).removeAll(blues);
					groups.put(newgroup, blues);

					//remove blue edges from the red group and create a new group with pairs (ng)
					groupPairs.get(oldgroup).removeAll(blueEdges);
					groupPairs.get(oldgroup).remove(pair);  //the edge that was to be removed
					groupPairs.put(newgroup, blueEdges);
					//return;

				} else {
					//System.out.println("Group still connected");
					//we keep group as it is, but remove the pair (edge)
					Set<Pair<T>> sp = groupPairs.get(oldgroup);
					sp.remove(pair);
					
					//remove group if empty
					if (sp.isEmpty()) {
						//System.out.println("grouppair entry removed " + oldgroup );
						groupPairs.remove(oldgroup);
					}

					//return;
				}


				//a has an edge and b do not
			} else if (edges.containsKey(a)){
				//keep group as it is, but wipe out b
				group.remove(b);
				groups.get(oldgroup).remove(b);

				if (groups.get(oldgroup).isEmpty()) { // never happens?
					System.out.println("How can group be empty?");
					groups.remove(oldgroup);
				}

				//remove from pairs
				//System.out.println("removing " + pair +" from group pairs " + oldgroup);
				Set<Pair<T>> sp = groupPairs.get(oldgroup);
				sp.remove(pair);
				//remove group if empty
				if (sp.isEmpty()) {
					//System.out.println("grouppair entry removed " + oldgroup );
					groupPairs.remove(oldgroup);
				}


				//non have edges
			} else {
				//clear out group entirely
				group.remove(a);
				group.remove(b);

				//assume that the group is only containing a and b?
				groups.get(oldgroup).remove(b);
				groups.get(oldgroup).remove(a);


				if (groups.get(oldgroup).isEmpty()) {
					groups.remove(oldgroup);
				} else { //impossible
					System.out.println("Hmm still stuff in group but no outgoing edges?" + groups.get(oldgroup) + " a and b is " +a +",    " + b);
					System.exit(0);
				}

				//remove from pairs
				Set<Pair<T>> sp = groupPairs.get(oldgroup);
				sp.remove(pair);
				//remove group if empty
				if (sp.isEmpty()) {
					//System.out.println("grouppair entry removed " + oldgroup );
					groupPairs.remove(oldgroup);
				}

			}//non have edges
		} //none is fixed


		//System.out.println("After remove: " + 		groups.keySet().size() + " groups with " + group.size() + " bodies" );
		Iterator<Group> groupiter = groups.keySet().iterator();

		Set<Pair<T>> allpairs = new HashSet<Pair<T>>();
		Set<Node> allnodes = new HashSet<Node>();
		while(groupiter.hasNext()){
			Group g = groupiter.next();
			//System.out.println( "Group " + g + " : " + groupPairs.get(g).size() + " pairs " );

			Iterator<Pair<T>> pairiter = groupPairs.get(g).iterator(); 
			while (pairiter.hasNext()) {
				Pair<T> thispair = pairiter.next();
				//System.out.println( "    pair:"+thispair.hashCode());
				if (allpairs.contains(thispair)) {
					System.out.println("Duplicates!!!!");
					System.exit(0);
				}
				allpairs.add(thispair);	

			}


			Iterator<Node> nodeiter = groups.get(g).iterator(); 
			while (nodeiter.hasNext()) {
				Node node = nodeiter.next();
				//System.out.println( "    Node:"+node);
				if (allnodes.contains(node)) {
					System.out.println("Duplicates!!!!");
					System.exit(0);
				}
				allnodes.add(node);	

			}

		}

		return true;
	}


	public void print() {
		System.out.println("Status: " + 		groups.keySet().size() + " groups with " + group.size() + " bodies" );
		Iterator<Group> groupiter = groups.keySet().iterator();

		Set<Pair<T>> allpairs = new HashSet<Pair<T>>();
		Set<Node> allnodes = new HashSet<Node>();
		while(groupiter.hasNext()){
			Group g = groupiter.next();
			System.out.println( "Group " + g + " : " + groupPairs.get(g).size() + " pairs " );

			Iterator<Pair<T>> pairiter = groupPairs.get(g).iterator(); 
			while (pairiter.hasNext()) {
				Pair<T> thispair = pairiter.next();
				//System.out.println( "    pair:"+thispair.hashCode());
				if (allpairs.contains(thispair)) {
					System.out.println("Duplicates!!!!");
					System.exit(0);
				}
				allpairs.add(thispair);	

			}


			Iterator<Node> nodeiter = groups.get(g).iterator(); 
			while (nodeiter.hasNext()) {
				Node node = nodeiter.next();
				//System.out.println( "    Node:"+node);
				if (allnodes.contains(node)) {
					System.out.println("Duplicates!!!!");
					System.exit(0);
				}
				allnodes.add(node);	

			}
		}
		
		
	}





}
