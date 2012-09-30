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
package ca.gedge.opgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.DirectedAcyclicGraph;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.extensions.CompositeNode;
import ca.gedge.opgraph.extensions.Extendable;
import ca.gedge.opgraph.extensions.ExtendableSupport;
import ca.gedge.opgraph.util.Pair;

/**
 * A DAG that supports a general flow of "operation". One implements various
 * operation nodes. These nodes take input from incoming links, and produce
 * output for outgoing links. The flow of operation obeys the topological
 * ordering of the DAG.
 */
public final class OpGraph
	extends DirectedAcyclicGraph<OpNode, OpLink>
	implements Extendable
{
	/** An id for the graph */
	private String id;
	
	/** A mapping from node id to node */
	private Map<String, OpNode> nodeMap;

	/**
	 * Default constructor.
	 */
	public OpGraph() {
		this.nodeMap = new HashMap<String, OpNode>();
		setId(null);
	}
	
	/**
	 * Gets the id for this graph.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id for this graph.
	 * 
	 * @param id  the id
	 */
	public void setId(String id) {
		this.id = (id == null ? Integer.toHexString(super.hashCode()) : id);
	}
	
	//
	// Helper methods
	//
	
	/**
	 * Gets a node by its id.
	 * 
	 * @param id  the id of the node
	 * @param deep  if <code>true</code>, performs a deep search where composite
	 *              nodes will also be searched for a node with the given id
	 * 
	 * @return  the node with the given id, or <code>null</code> if no such
	 *          node exists in this graph
	 */
	public OpNode getNodeById(String id, boolean deep) {
		if(deep) {
			final Pair<OpGraph, OpNode> nodeGraphPair = findNodeById(id);
			return (nodeGraphPair == null ? null : nodeGraphPair.getSecond());
		} else {
			return nodeMap.get(id);
		}
	}
	
	/**
	 * Finds a node and its parent graph by id. This is a deep operation,
	 * and hence will recursively search through macro nodes to find the
	 * node with the given id. 
	 * 
	 * @param id  the id of the node
	 * 
	 * @return  the graph/node pair, or <code>null</code> if no such node
	 *          exists anywhere in this graph or its macros
	 */
	protected Pair<OpGraph, OpNode> findNodeById(String id) {
		Pair<OpGraph, OpNode> ret = null;
		if(nodeMap.containsKey(id)) {
			final OpNode node = nodeMap.get(id);
			if(node != null) {
				ret = new Pair<OpGraph, OpNode>(this, node);
			} else {
				nodeMap.remove(id);
			}
		} else {
			// Try searching through composite nodes
			for(OpNode node : getVertices()) {
				final CompositeNode composite = node.getExtension(CompositeNode.class);
				if(composite != null) {
					ret = composite.getGraph().findNodeById(id);
					if(ret != null)
						break;
				}
			}
		}
		return ret;
	}
	
	/**
	 * Connects the given source node/field pair to a given destination
	 * node/field pair. This is a convenience method which catches
	 * exceptions from the {@link OpLink} constructor, and from
	 * {@link #add(OpLink)}.
	 * 
	 * @param source  source node
	 * @param sourceFieldKey  the key of the field connected at the source
	 * @param destination  destination node
	 * @param destinationFieldKey  the key of the field connected at the destination
	 * 
	 * @return the link that was created, or <code>null</code> if the link
	 *         could not be created
	 */
	public OpLink connect(OpNode source,
	                      String sourceFieldKey,
	                      OpNode destination,
	                      String destinationFieldKey)
	{
		OpLink link = null;
		try {
			link = new OpLink(source, sourceFieldKey, destination, destinationFieldKey);
			add(link);
		} catch(ItemMissingException exc) {
			link = null;
		} catch(VertexNotFoundException exc) {
			link = null;
		} catch(CycleDetectedException exc) {
			link = null;
		}

		return link;
	}
	
	/**
	 * Connects the given source node/field pair to a given destination node/field
	 * pair. This is a convenience method which catches exceptions from the
	 * {@link OpLink} constructor, and from {@link #add(OpLink)}.
	 * 
	 * @param source  source node
	 * @param sourceField  the field connected at the source
	 * @param destination  destination node
	 * @param destinationField  the field connected at the destination
	 * 
	 * @return the link that was created, or <code>null</code> if the link
	 *         could not be created
	 */
	public OpLink connect(OpNode source,
	                            OutputField sourceField,
	                            OpNode destination,
	                            InputField destinationField)
	{
		OpLink link = null;
		try {
			link = new OpLink(source, sourceField, destination, destinationField);
			add(link);
		} catch(ItemMissingException exc) {
			link = null;
		} catch(VertexNotFoundException exc) {
			link = null;
		} catch(CycleDetectedException exc) {
			link = null;
		}
		
		return link;
	}
	
	//
	// Overrides
	//
	
	@Override
	public void add(OpNode node) {
		if(node != null && node.getId() != null) {
			if(nodeMap.containsKey(node)) {
				// XXX What to do if node with that id already exists? 
			} else {
				super.add(node);
				node.addNodeListener(nodeListener);
				nodeMap.put(node.getId(), node);
				fireNodeAdded(node);
			}
		}
	}
	
	@Override
	public boolean remove(OpNode node) {
		final boolean removed = super.remove(node);
		if(removed) {
			node.removeNodeListener(nodeListener);
			nodeMap.remove(node.getId());
			fireNodeRemoved(node);
		}
		return removed;
	}

	@Override
	public void add(OpLink link) throws VertexNotFoundException, CycleDetectedException {
		super.add(link);
		if(link != null)
			fireLinkAdded(link);
	}

	@Override
	public boolean remove(OpLink link) {
		final boolean removed = super.remove(link);
		if(removed)
			fireLinkRemoved(link);
		return removed;
	}
	
	//
	// Extendable
	//

	private ExtendableSupport extendableSupport = new ExtendableSupport(OpLink.class);
	
	@Override
	public <T> T getExtension(Class<T> type) {
		return extendableSupport.getExtension(type);
	}
	
	@Override
	public Collection<Class<?>> getExtensionClasses() {
		return extendableSupport.getExtensionClasses();
	}
	
	@Override
	public <T> T putExtension(Class<T> type, T extension) {
		return extendableSupport.putExtension(type, extension);
	}
	
	//
	// OpNodeListener
	//
	
	final OpNodeListener nodeListener = new OpNodeListener() {
		@Override
		public void fieldRemoved(OpNode node, OutputField field) {
			for(OpLink link : getOutgoingEdges(node)) {
				if(link.getSourceField().equals(field)) {
					remove(link);
					break;
				}
			}
		}
		
		@Override
		public void fieldRemoved(OpNode node, InputField field) {
			for(OpLink link : getIncomingEdges(node)) {
				if(link.getDestinationField().equals(field)) {
					remove(link);
					break;
				}
			}
		}
		
		@Override
		public void nodePropertyChanged(String propertyName, Object oldValue, Object newValue) {}
		
		@Override
		public void fieldAdded(OpNode node, OutputField field) {}
		
		@Override
		public void fieldAdded(OpNode node, InputField field) {}
	};
	
	//
	// Listeners
	//

	private final ArrayList<OpGraphListener> listeners = new ArrayList<OpGraphListener>();

	/**
	 * Adds a listener to this graph.
	 * 
	 * @param listener  the listener to add
	 */
	public void addGraphListener(OpGraphListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes a listener from this graph.
	 * 
	 * @param listener  the listener to remove
	 */
	public void removeGraphListener(OpGraphListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}

	private void fireNodeAdded(OpNode node) {
		synchronized(listeners) {
			for(OpGraphListener listener : listeners)
				listener.nodeAdded(this, node);
		}
	}

	private void fireNodeRemoved(OpNode node) {
		synchronized(listeners) {
			for(OpGraphListener listener : listeners)
				listener.nodeRemoved(this, node);
		}
	}

	private void fireLinkAdded(OpLink link) {
		synchronized(listeners) {
			for(OpGraphListener listener : listeners)
				listener.linkAdded(this, link);
		}
	}

	private void fireLinkRemoved(OpLink link) {
		synchronized(listeners) {
			for(OpGraphListener listener : listeners)
				listener.linkRemoved(this, link);
		}
	}
}
