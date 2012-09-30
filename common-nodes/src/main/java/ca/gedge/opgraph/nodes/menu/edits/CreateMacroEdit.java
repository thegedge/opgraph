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
package ca.gedge.opgraph.nodes.menu.edits;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.extensions.NodeMetadata;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.nodes.general.MacroNode;

/**
 * An edit that creates a macro from a given collection of nodes in a graph.
 */
public class CreateMacroEdit extends AbstractUndoableEdit {
	private static final Logger LOGGER = Logger.getLogger(CreateMacroEdit.class.getName());
	
	/** The graph to which this edit was applied  */
	private OpGraph graph;
	
	/** The constructed macro node */
	private MacroNode macro;
	
	/** Metadata for the newly created macro */
	private NodeMetadata macroMeta;
	
	/** Links that existed before the macro */
	private Set<OpLink> oldLinks;
	
	/** Links attached to the newly created macro */
	private Set<OpLink> newLinks;
	
	
	/**
	 * Constructs a macro-creation edit which will automatically create a
	 * macro from a given collection of nodes.
	 * 
	 * @param graph  the graph to which this edit will be applied
	 * @param nodes  the nodes from which the macro will be created 
	 */
	public CreateMacroEdit(OpGraph graph, Collection<OpNode> nodes) {
		this.graph = graph;
		this.oldLinks = new TreeSet<OpLink>();
		this.newLinks = new TreeSet<OpLink>();
		
		// Construct the macro node
		final OpGraph macroGraph = new OpGraph();
		final Set<OpLink> links = new TreeSet<OpLink>();
		
		this.macroMeta = new NodeMetadata();
		this.macro = new MacroNode(macroGraph);
		this.macro.putExtension(NodeMetadata.class, this.macroMeta);
		
		macroGraph.setId("macro" + macro.getId());
		
		// First add all the nodes
		int numNodes = 0;
		for(OpNode node : nodes) {
			// Sanity check...
			if(!graph.contains(node)) {
				LOGGER.warning("node not in graph modeled by the given graph canvas");
				continue;
			}
			
			// Add the node
			macroGraph.add(node);
			
			final NodeMetadata nodeMeta = node.getExtension(NodeMetadata.class);
			if(nodeMeta != null) {
				macroMeta.setX(macroMeta.getX() + nodeMeta.getX());
				macroMeta.setY(macroMeta.getY() + nodeMeta.getY());
				++numNodes;
			}
			
			// Extend the link set
			links.addAll(graph.getIncomingEdges(node));
			links.addAll(graph.getOutgoingEdges(node));
		}
		
		// Macro's initial location is the centroid
		if(numNodes > 0) {
			macroMeta.setX(macroMeta.getX() / numNodes);
			macroMeta.setY(macroMeta.getY() / numNodes);
		}

		// These maps keep track of input/output keys that have already been 
		// used, so that we don't publish two with the same key
		//
		final Map<String, Integer> publishedInputsMap = new HashMap<String, Integer>();
		final Map<String, Integer> publishedOutputsMap = new HashMap<String, Integer>();
		
		// Given all of the incoming/outgoing links, find which are internal
		// and which are external. If an links is external, publish the
		// appropriate field
		//
		for(OpLink link : links) {
			if(!nodes.contains(link.getSource())) {
				oldLinks.add(link);
				
				// Make sure no duplicate keys
				String name = link.getDestinationField().getKey();
				if(publishedInputsMap.containsKey(name)) {
					int val = publishedInputsMap.get(name);
					publishedInputsMap.put(name, val + 1);
					name += val;
				} else {
					publishedInputsMap.put(name, 1);
				}
				
				// Publish input
				final InputField input = macro.publish(name, link.getDestination(), link.getDestinationField());
				try {
					newLinks.add(new OpLink(link.getSource(), link.getSourceField(), macro, input));
				} catch(ItemMissingException exc) {
					LOGGER.severe("impossible exception");
				}
			} else if(!nodes.contains(link.getDestination())) {
				oldLinks.add(link);
				
				// Make sure no duplicate keys
				String name = link.getSourceField().getKey();
				if(publishedOutputsMap.containsKey(name)) {
					int val = publishedOutputsMap.get(name);
					publishedOutputsMap.put(name, val + 1);
					name += val;
				} else {
					publishedOutputsMap.put(name, 1);
				}
				
				// Publish output
				final OutputField output = macro.publish(name, link.getSource(), link.getSourceField());
				try {
					newLinks.add(new OpLink(macro, output, link.getDestination(), link.getDestinationField()));
				} catch(ItemMissingException exc) {
					LOGGER.severe("impossible exception");
				}
			} else {
				oldLinks.add(link);
				try {
					macroGraph.add(link);
				} catch(VertexNotFoundException exc) {
					LOGGER.severe("impossible exception");
				} catch(CycleDetectedException exc) {
					LOGGER.severe("impossible exception");
				}
			}
		}
		
		perform();
	}
	

	/**
	 * Performs this edit.
	 */
	private void perform() {
		// Remove all nodes, in turn removing all associated links
		for(OpNode node : macro.getGraph().getVertices())
			graph.remove(node);
		
		// Add macro node, and new external links attached to this macro node
		graph.add(macro);
		for(OpLink link : newLinks) {
			try {
				graph.add(link);
			} catch(VertexNotFoundException exc) {
				LOGGER.severe("impossible exception");
			} catch(CycleDetectedException exc) {
				LOGGER.severe("impossible exception");
			}
		}
	}
	
	//
	// AbstractEdit overrides
	//
	
	@Override
	public String getPresentationName() {
		return "Create Macro";
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		// Remove macro 
		graph.remove(macro);
		
		// Add back original nodes and links
		for(OpNode node : macro.getGraph().getVertices())
			graph.add(node);
		
		for(OpLink link : oldLinks) {
			try {
				graph.add(link);
			} catch(VertexNotFoundException exc) {
				LOGGER.severe("impossible exception");
			} catch(CycleDetectedException exc) {
				LOGGER.severe("impossible exception");
			}
		}
	}
}
