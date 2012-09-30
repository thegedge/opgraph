/*
 * Copyright (C) 2012 Jason Gedge <http://www.gedge.ca>
 *
 * This file is part of the OpGraph project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.gedge.opgraph.dag;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

/**
 * A generic implementation of a directed acyclic graph (DAG). Topological
 * ordering is enforced on the vertices of this graph (see
 * <a href="http://en.wikipedia.org/wiki/Topological_sorting">Wikipedia Entry</a>). 
 * 
 * @param <V>  the vertex type, which implements {@link Vertex}
 * @param <E>  the edge type, which implements {@link DirectedEdge}
 */
public class DirectedAcyclicGraph<V extends Vertex, E extends DirectedEdge<V>>
	implements Iterable<V>
{
	/** The vertices in this DAG */
	private ArrayList<V> vertices;
	
	/** The edges in this DAG */
	private TreeSet<E> edges;
	
	/**
	 * A mapping from vertex to its level.
	 * 
	 * @see #getLevel(Object)
	 */
	private WeakHashMap<V, Integer> vertexLevels;
	
	/** A cache of incoming edges */
	private WeakHashMap<V, SoftReference<Set<E>>> incomingEdgesCache;
	
	/** A cache of outgoing edges */
	private WeakHashMap<V, SoftReference<Set<E>>> outgoingEdgesCache;
	
	/** Whether or not the topological sorting needs to be performed */
	private boolean shouldSort;
	
	/**
	 * Default constructor.
	 */
	public DirectedAcyclicGraph() {
		this.vertices = new ArrayList<V>();
		this.edges = new TreeSet<E>();
		this.vertexLevels = new WeakHashMap<V, Integer>();
		this.incomingEdgesCache = new WeakHashMap<V, SoftReference<Set<E>>>();
		this.outgoingEdgesCache = new WeakHashMap<V, SoftReference<Set<E>>>();
		this.shouldSort = false;
	}
	
	/**
	 * Adds a vertex to this DAG.
	 * 
	 * @param vertex  the vertex to add
	 */
	public void add(V vertex) {
		if(!vertices.contains(vertex)) {
			vertices.add(vertex);
			shouldSort = true;
		}
	}
	
	/**
	 * Removes a vertex from this DAG. Any {@link DirectedEdge}s in this DAG that
	 * reference this vertex will also be removed.
	 * 
	 * @param vertex  the vertex to remove
	 * 
	 * @return <code>true</code> if this graph contained the given vertex,
	 *         <code>false</code> otherwise
	 */
	public boolean remove(V vertex) {
		final boolean removed = vertices.remove(vertex);
		if(removed) {
			shouldSort = true;
			
			// Remove edges which reference this vertex
			final ArrayList<E> edgesCopy = new ArrayList<E>(edges);
			for(E edge : edgesCopy) {
				if(edge.getSource() == vertex || edge.getDestination() == vertex)
					remove(edge);
			}
		}
		return removed;
	}
	
	/**
	 * Gets whether or not this graph contains a specified vertex.
	 * 
	 * @param vertex  the vertex
	 * 
	 * @return <code>true</code> if this graph contains the specified vertex,
	 *         <code>false</code> otherwise
	 */
	public boolean contains(V vertex) {
		return vertices.contains(vertex);
	}
	
	/**
	 * Gets whether or not this graph contains a specified edge.
	 * 
	 * @param edge  the edge
	 * 
	 * @return <code>true</code> if this graph contains the specified edge,
	 *         <code>false</code> otherwise
	 */
	public boolean contains(E edge) {
		return edges.contains(edge);
	}
	
	/**
	 * Adds an edge to this DAG.
	 * 
	 * @param edge  the edge to add
	 * 
	 * @throws VertexNotFoundException  if <code>edge</code> contains vertices
	 *                                  that are not contained within this graph.
	 *               
	 * @throws CycleDetectedException  if adding <code>edge</code> will induce a cycle 
	 */
	public void add(E edge) throws VertexNotFoundException, CycleDetectedException {
		if(!vertices.contains(edge.getSource()))
			throw new VertexNotFoundException(edge.getSource());
		
		if(!vertices.contains(edge.getDestination()))
			throw new VertexNotFoundException(edge.getDestination());
		
		edges.add(edge);
		
		// Clear out appropriate cache entries
		outgoingEdgesCache.remove(edge.getSource());
		incomingEdgesCache.remove(edge.getDestination());
		
		// Check if adding this edge created a cycle, and if so, remove it
		boolean oldShouldSort = shouldSort;
		shouldSort = true;
		if(!topologicalSort()) {
			edges.remove(edge);
			shouldSort = oldShouldSort;
			throw new CycleDetectedException("adding edge creates a cycle");
		}
	}
	
	/**
	 * Gets whether or not an edge can be added to this graph without raising
	 * any exception defined in {@link #add(DirectedEdge)}.
	 * 
	 * @param edge  the edge to check
	 * 
	 * @return <code>true</code> if the edge can be added without inducing a
	 *         cycle, <code>false</code> otherwise
	 */
	public boolean canAddEdge(E edge) {
		boolean canAdd = false;
		if(vertices.contains(edge.getSource()) && vertices.contains(edge.getDestination())) {
			try {
				edges.add(edge);
				
				// XXX It'd be nice to not have to do this, but instead either directly
				//     add this edge to the cached values of each, or maybe pass this
				//     edge to toplogicalSort directly
				outgoingEdgesCache.remove(edge.getSource());
				incomingEdgesCache.remove(edge.getDestination());
				
				// Check if adding this edge created a cycle, and if so, remove it
				boolean oldShouldSort = shouldSort;
				shouldSort = true;
				canAdd = topologicalSort();
				shouldSort = oldShouldSort;
			} finally {
				outgoingEdgesCache.remove(edge.getSource());
				incomingEdgesCache.remove(edge.getDestination());
				edges.remove(edge);
			}
		}
		return canAdd;
	}
	
	/**
	 * Removes an edge from this DAG.
	 * 
	 * @param edge  the edge to remove
	 * 
	 * @return <code>true</code> if this graph contained the given vertex,
	 *         <code>false</code> otherwise
	 */
	public boolean remove(E edge) {
		final int initalSize = edges.size();
		edges.remove(edge);
		final boolean removed = initalSize != edges.size();
		if(removed) {
			// Clear out appropriate cache entries
			outgoingEdgesCache.remove(edge.getSource());
			incomingEdgesCache.remove(edge.getDestination());
			
			shouldSort = true;
		}
		return removed;
	}
	
	/**
	 * Gets the set of vertices in this DAG. The list of vertices will be
	 * ordered according to their topological ordering.
	 * 
	 * @return An immutable {@link Set} of vertices.
	 */
	public List<V> getVertices() {
		topologicalSort();
		return Collections.unmodifiableList(vertices);
	}
	
	/**
	 * Gets the set of edges in this DAG.
	 * 
	 * @return An immutable {@link Set} of edges.
	 */
	public Set<E> getEdges() {
		return Collections.unmodifiableSet(edges);
	}
	
	/**
	 * Gets the level of a vertex. The level of a vertex <code>v</code> is
	 * defined as:
	 * <ul>
	 *   <li>0, if <code>getIncomingEdges(v) == 0</code></li>
	 *   <li><code>min(level of u) for u in getIncomingEdges(v).getSource()</code></li>
	 * </ul>
	 * 
	 * @param vertex  the vertex
	 * 
	 * @return the level of the vertex, or -1 if the vertex is not in this graph
	 */
	public int getLevel(V vertex) {
		if(!vertices.contains(vertex))
			return -1;
		
		topologicalSort();
		
		int ret = -1;
		if(vertexLevels.get(vertex) != null)
			ret = vertexLevels.get(vertex);
		return ret;
	}
	
	/**
	 * Gets the incoming {@link DirectedEdge}s for a {@link Vertex}.
	 * 
	 * @param vertex  the vertex
	 * 
	 * @return a {@link Set} of {@link DirectedEdge}s in this graph whose destination
	 *         is <code>vertex</code>
	 */
	public Set<E> getIncomingEdges(V vertex) {
		if(!vertices.contains(vertex))
			return new TreeSet<E>();

		// If not in cache, compute
		if(!incomingEdgesCache.containsKey(vertex)) {
			final TreeSet<E> cachedValue = new TreeSet<E>();
			for(E edge : edges) {
				if(edge.getDestination() == vertex)
					cachedValue.add(edge);
			}
			
			incomingEdgesCache.put(vertex, new SoftReference<Set<E>>(cachedValue));
		}
		
		return new TreeSet<E>(incomingEdgesCache.get(vertex).get());
	}

	/**
	 * Gets the outgoing {@link DirectedEdge}s for a {@link Vertex}.
	 * 
	 * @param vertex  the vertex
	 * 
	 * @return a {@link Set} of {@link DirectedEdge}s in this graph whose source is
	 *         the <code>vertex</code>  
	 */
	public Set<E> getOutgoingEdges(V vertex) {
		if(!vertices.contains(vertex))
			return new TreeSet<E>();

		// See if exists in cache
		if(!outgoingEdgesCache.containsKey(vertex)) {
			final TreeSet<E> cachedValue = new TreeSet<E>();
			for(E edge : edges) {
				if(edge.getSource() == vertex)
					cachedValue.add(edge);
			}
			
			outgoingEdgesCache.put(vertex, new SoftReference<Set<E>>(cachedValue));
		}
		
		return new TreeSet<E>(outgoingEdgesCache.get(vertex).get());
	}

	@Override
	public Iterator<V> iterator() {
		topologicalSort();
		
		return new Iterator<V>() {
			private Iterator<V> iter = vertices.iterator();
			
			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public V next() {
				return iter.next();
			}

			@Override
			public void remove() {
				// TODO perhaps allow removal?
				throw new UnsupportedOperationException("Removal via iterator not supported in DAGs");
			}
		};
	}
	
	/**
	 * Topologically orders the vertices in this DAG. A topological ordering
	 * is an ordering of a DAG's vertices such that for any edge
	 * <tt>{u, v}</tt>, the vertex <tt>u</tt> comes before the vertex
	 * <tt>v</tt> in the ordering.
	 *
	 * @return <code>true</code> if sorting was successful, <code>false</code>
	 *         otherwise (because a cycle exists).
	 *  
	 * @see <a href="http://en.wikipedia.org/wiki/Topological_sorting">Wikipedia Article</a>
	 */
	private boolean topologicalSort() {
		boolean ret = true;
		if(shouldSort && vertices.size() == 1) {
			vertexLevels.put(vertices.iterator().next(), 0);
		} else if(shouldSort && vertices.size() > 1) {
			final ArrayList<V> orderedVertices = new ArrayList<V>();
			final WeakHashMap<V, Integer> newLevels = new WeakHashMap<V, Integer>();
			final HashMap<V, Integer> incomingEdgeCount = new HashMap<V, Integer>();
			
			//
			for(V vertex : vertices)
				incomingEdgeCount.put(vertex, 0);
			
			// Gather initial incoming edge count
			for(E edge : edges) {
				int count = incomingEdgeCount.get(edge.getDestination());
				incomingEdgeCount.put(edge.getDestination(), count + 1);
			}
			
			// Ordering
			for(int level = 0; orderedVertices.size() < vertices.size(); ++level) {
				// Find a vertex with zero incoming edges
				ArrayList<V> verticesToProcess = new ArrayList<V>();
				for(Map.Entry<V, Integer> entry : incomingEdgeCount.entrySet()) {
					if(entry.getValue() == 0)
						verticesToProcess.add(entry.getKey());
				}
				
				if(verticesToProcess.size() == 0)
					break;
				
				for(V vertex : verticesToProcess) {
					// Prevent reuse of this vertex
					orderedVertices.add(vertex);
					newLevels.put(vertex, level);
					incomingEdgeCount.put(vertex, -1);
					
					// Reduce incoming edge count after removing vertex
					for(E edge : getOutgoingEdges(vertex)) {
						V out = edge.getDestination();
						incomingEdgeCount.put(out, incomingEdgeCount.get(out) - 1);
					}
				}
			}
			
			boolean cycleExists = false;
			for(Integer value : incomingEdgeCount.values()) {
				if(value > 0) {
					cycleExists = true;
					break;
				}
			}
			
			// If no cycle, we want to update the vertices to the new
			// ordered list and flag them as not needing sorting.
			if(cycleExists) {
				ret = false;
			} else {
				vertexLevels = newLevels;
				vertices = orderedVertices;
				shouldSort = false;
			}
		}
		
		return ret;
	}
}
