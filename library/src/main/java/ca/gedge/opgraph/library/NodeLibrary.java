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
package ca.gedge.opgraph.library;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.library.handlers.URIHandler;
import ca.gedge.opgraph.library.instantiators.ClassInstantiator;

/**
 * A class for managing available node classes.
 * 
 * Nodes have a corresponding {@link URI}, defined as per the following specification:
 * <ul>
 *   <li>
 *     If a node is a {@link Class} then its URI will be <code>class://classname</code>,
 *     where <code>classname</code> is equal to {@link Class#getName()}.
 *   </li>
 *   <li>
 *     If a node is a macro, then its URI will be <code>file://parentFile#macroName</code>,
 *     where <code>parentFile</code> is equal to {@link File#getName()} and 
 *     <code>macroName</code> is the id of any macro contained in that file. 
 *   </li>
 * </ul>
 * This type can be used in the {@link #register(URI)} and {@link #get(URI)}
 * functions of this class.
 */
public class NodeLibrary implements Iterable<NodeData> {
	/** Logger **/
	private static final Logger LOGGER = Logger.getLogger(NodeLibrary.class.getName());
	
	/** The registered map of nodes */
	private Map<URI, NodeData> nodeMap;
	
	/** The list of uri handlers this library uses */
	private List< URIHandler<List<NodeData>> > uriHandlers;
	
	/**
	 * Default constructor. 
	 */
	public NodeLibrary() {
		this.nodeMap = new TreeMap<URI, NodeData>();
		this.uriHandlers = new ArrayList<URIHandler<List<NodeData>>>();
	}
	
	/**
	 * Attempts to load the node specified by the given type. Type is parsed
	 * as per the class' specification. If the type is a macro, and its source
	 * file contains multiple macros, all macros in that file will be registered.
	 * 
	 * @param uri  the {@link URI} to register
	 * 
	 * @return the node info associated with the newly registered type
	 *         
	 * @throws IllegalArgumentException  if this library cannot handle the given URI
	 * @throws IOException  if a handler handles the specified URI, but cannot load
	 *                      data from that URI
	 */
	public NodeData register(URI uri) throws IOException {
		if(!nodeMap.containsKey(uri)) {
			NodeData nodeInfo = null;
			for(URIHandler<List<NodeData>> handler : uriHandlers) {
				if(handler.handlesURI(uri)) {
					// We catch the IOException within the loop because maybe
					// another handler can deal with the URI
					try {
						// Load all the node data from the given URI
						for(NodeData info : handler.load(uri)) {
							nodeInfo = info;
							put(info);
						}
					} catch(IOException exc) {
						LOGGER.warning(handler.getClass() + " says it handles URI '" + uri + "', but threw an IOException");
					}
				}
			}
			
			if(nodeInfo == null)
				throw new IllegalArgumentException("The URI '" + uri + "' is not handled by this library");
		}
		
		return get(uri);
	}

	/**
	 * Registers a specified {@link OpNode} class to this library.
	 * 
	 * @param clz  the class
	 * 
	 * @throws URISyntaxException  if a URI could not be created for the given class
	 */
	public void register(Class<? extends OpNode> clz) throws URISyntaxException {
		if(clz != null) {
			final OpNodeInfo nodeInfo = clz.getAnnotation(OpNodeInfo.class);
			if(nodeInfo != null) {
				final String type = clz.getName();
				final String name = (nodeInfo == null ? "no name" : nodeInfo.name());
				final String desc = (nodeInfo == null ? "" : nodeInfo.description());
				final String cat = (nodeInfo == null ? "" : nodeInfo.category());
				final URI uri = new URI("class", type, null);
				put(new NodeData(uri, name, desc, cat, new ClassInstantiator<OpNode>(clz)));
			}
		}
	}
	
	/**
	 * Unregisters a URI from this library.
	 * 
	 * @param uri  the uri to unregister
	 *  
	 * @return the node info associated with the given uri, or <code>null</code>
	 *         if the given uri was not registered with this library
	 */
	public NodeData unregister(URI uri) {
		final NodeData info = nodeMap.remove(uri);
		if(info != null)
			fireNodeUnregistered(info);
		return info;
	}
	
	/**
	 * Adds a specific node type to this library.
	 * 
	 * @param info  the node info
	 * 
	 * @throws NullPointerException  if given info is <code>null</code>, or any
	 *                               of its members are <code>null</code>
	 * 
	 * @throws IllegalArgumentException  if no validator exists to handle the
	 *                                   URI in the given info 
	 */
	public void put(NodeData info) {
		if(info == null)
			throw new NullPointerException("Node info cannot be null");
		
		if(info.uri == null || info.name == null || info.description == null || info.instantiator == null)
			throw new NullPointerException("Members of node info cannot be null");
		
		// Check to make sure we have a handler for the given URI
		boolean handled = false;
		for(URIHandler<?> handler : uriHandlers) {
			if(handler.handlesURI(info.uri)) {
				handled = true;
				break;
			}
		}
		
		if(!handled)
			throw new IllegalArgumentException("No handler exists for the uri: " + info.uri);
		
		nodeMap.put(info.uri, info);
		fireNodeRegistered(info);
	}

	/**
	 * Gets the node info for every node type registered with this library.
	 * 
	 * @return a collection of node info
	 */
	public List<NodeData> getNodeInfo() {
		List<NodeData> list = new ArrayList<NodeData>(nodeMap.values());
		return Collections.unmodifiableList(list);
	}
	
	/**
	 * Gets node information associated with a specified type.
	 * 
	 * @param uri  the uri of the node
	 * 
	 * @return  the information associated with the given type, or <code>null</code>
	 *          if the given uri is not registered with this library.
	 *          
	 * @see #register(URI) 
	 */
	public NodeData get(URI uri) {
		return nodeMap.get(uri);
	}
	
	/**
	 * Get's the URI associated with a node.
	 * 
	 * @param node  the node
	 * 
	 * @return the URI for the specified node
	 * 
	 * @throws URISyntaxException  if the given node could not be encoded into a URI
	 */
	public static URI getNodeURI(OpNode node)
		throws URISyntaxException
	{
		// FIXME since maven
//		if(node instanceof MacroNode) {
//			final MacroNode macro = (MacroNode)node;
//			if(macro.getSource() == null)
//				return new URI(null, null, macro.getGraph().getId());
//			return new URI("file", macro.getSource().getPath(), macro.getGraph().getId());
//		} else {
			return new URI("class", node.getClass().getName(), null);
//		}
	}
	
	/**
	 * Adds the given handler to the list of handlers this library uses.
	 * 
	 * @param handler  the handler
	 */
	public void addURIHandler(URIHandler<List<NodeData>> handler) {
		if(handler != null)
			uriHandlers.add(handler);
	}
	
	/**
	 * Removes the given handler from the list of handlers this library uses.
	 * 
	 * @param handler  the handler
	 */
	public void removeURIHandler(URIHandler<List<NodeData>> handler) {
		uriHandlers.remove(handler);
	}
	
	/**
	 * Gets a mapping from category name to all the nodes having that category.
	 * 
	 * @return a {@link SortedMap} from category name to a {@link List} of
	 *         {@link NodeData} instances having that category.
	 */
	public SortedMap<String, List<NodeData>> getCategoryMap() {
		final SortedMap<String, List<NodeData>> categoryMap = new TreeMap<String, List<NodeData>>();
		for(NodeData nodeInfo : nodeMap.values()) {
			// Grab the list of NodeData instances for this NodeData's category,
			// but if this is the first time hitting this category, create a new
			// list and put it into the map
			List<NodeData> nodes = categoryMap.get(nodeInfo.category);
			if(nodes == null) {
				nodes = new ArrayList<NodeData>();
				categoryMap.put(nodeInfo.category, nodes);
			}

			nodes.add(nodeInfo);
		}

		return categoryMap;
	}
	
	//
	// Listeners
	//
	
	private ArrayList<NodeLibraryListener> listeners = new ArrayList<NodeLibraryListener>();

	/**
	 * Adds a listener to this library.
	 * 
	 * @param listener  the listener to add
	 */
	public void addNodeLibraryListener(NodeLibraryListener listener) {
		synchronized(listeners) {
			if(listener != null && !listeners.contains(listener))
				listeners.add(listener);
		}
	}

	/**
	 * Removes a listener from this library.
	 * 
	 * @param listener  the listener to remove
	 */
	public void removeNodeLibraryListener(NodeLibraryListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}

	protected void fireNodeRegistered(NodeData info) {
		synchronized(listeners) {
			for(NodeLibraryListener listener : listeners)
				listener.nodeRegistered(info);
		}
	}
	
	protected void fireNodeUnregistered(NodeData info) {
		synchronized(listeners) {
			for(NodeLibraryListener listener : listeners)
				listener.nodeUnregistered(info);
		}
	}
	
	//
	// Iterable
	//
	
	@Override
	public Iterator<NodeData> iterator() {
		return Collections.unmodifiableCollection(nodeMap.values()).iterator();
	}
}
