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
package ca.gedge.opgraph.app.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JComponent;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.extensions.NodeMetadata;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.app.extensions.Note;
import ca.gedge.opgraph.app.extensions.NoteComponent;
import ca.gedge.opgraph.app.extensions.Notes;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.extensions.CompositeNode;
import ca.gedge.opgraph.extensions.Publishable;
import ca.gedge.opgraph.extensions.Publishable.PublishedInput;
import ca.gedge.opgraph.extensions.Publishable.PublishedOutput;

/**
 * Helper methods for graphs.
 * 
 * TODO Need to generalize cloning of extensions. This would be best done through implementing
 *      Cloneable and Object#clone() for everything, and throwing up a warning dialog whenever
 *      an extension is encountered which doesn't implement Cloneable.
 */
public class GraphUtils {
	/** Logger */
	private final static Logger LOGGER = Logger.getLogger(GraphUtils.class.getName());
	
	/**
	 * Clone a node along with {@link NodeSettings}, {@link NodeMetadata},
	 * {@link CompositeNode}, and {@link Publishable} extensions cloned.
	 * 
	 * @param node  the node to clone
	 * 
	 * @return the cloned node, or <code>null</code> if the node could not be cloned
	 * 
	 * @throws NullPointerException  if node is <code>null</code>
	 */
	public static OpNode cloneNode(OpNode node) {
		if(node == null)
			throw new NullPointerException();
		
		final Class<? extends OpNode> nodeClass = node.getClass();
		try {
			final OpNode newNode = nodeClass.newInstance();
			newNode.setName(node.getName());
			
			// copy node settings (if available)
			final NodeSettings nodeSettings = node.getExtension(NodeSettings.class);
			final NodeSettings newNodeSettings = newNode.getExtension(NodeSettings.class);
			if(nodeSettings != null && newNodeSettings != null) {
				newNodeSettings.loadSettings(nodeSettings.getSettings());
			}
			
			// copy meta data (if available)
			final NodeMetadata metaData = node.getExtension(NodeMetadata.class);
			if(metaData != null) {
				final NodeMetadata newMetaData = new NodeMetadata(metaData.getX(), metaData.getY());
				newNode.putExtension(NodeMetadata.class, newMetaData);
			}
			
			// if a composite node, clone graph
			final CompositeNode compositeNode = node.getExtension(CompositeNode.class);
			final CompositeNode newCompositeNode = newNode.getExtension(CompositeNode.class);
			if(compositeNode != null && newCompositeNode != null) {
				final Map<String, String> nodeMap = new HashMap<String, String>();
				final OpGraph graph = compositeNode.getGraph();
				final OpGraph newGraph = cloneGraph(graph, null, nodeMap);
				newCompositeNode.setGraph(newGraph);
				
				// setup published fields (if available)
				final Publishable publishable = node.getExtension(Publishable.class);
				final Publishable newPublishable = newNode.getExtension(Publishable.class);
				if(publishable != null && newPublishable != null) {
					for(PublishedInput pubInput:publishable.getPublishedInputs()) {
						final OpNode destNode = newGraph.getNodeById(nodeMap.get(pubInput.destinationNode.getId()), false);
						if(destNode != null) {
							final InputField destField = destNode.getInputFieldWithKey(pubInput.nodeInputField.getKey());
							newPublishable.publish(pubInput.getKey(), destNode, destField);
						}
					}
					
					for(PublishedOutput pubOutput:publishable.getPublishedOutputs()) {
						final OpNode srcNode = newGraph.getNodeById(nodeMap.get(pubOutput.sourceNode.getId()), false);
						if(srcNode != null) {
							final OutputField srcField = srcNode.getOutputFieldWithKey(pubOutput.nodeOutputField.getKey());
							newPublishable.publish(pubOutput.getKey(), srcNode, srcField);
						}
					}
				}
			}

			// XXX Other extensions. See note attached to class javadoc.
			
			return newNode;
		} catch (InstantiationException e) {
			LOGGER.severe(e.getMessage());
		} catch (IllegalAccessException e) {
			LOGGER.severe(e.getMessage());
		}
		
		return null;
	}

	/**
	 * Clone the given graph.
	 * 
	 * TODO Deal with cloning custom extensions
	 * 
	 * @param graph  the graph to clone
	 * @param newGraph  graph to modify. If <code>null</code> a new graph will be
	 *                  created and returned. If not <code>null</code>, the return
	 *                  value will be the same object as this variable.
	 * @param nodeMap  mapping from node id to cloned node id
	 *
	 * @return the cloned graph
	 */
	public static OpGraph cloneGraph(OpGraph graph, OpGraph newGraph, Map<String, String> nodeMap) {
		final OpGraph retVal = (newGraph != null ? newGraph : new OpGraph());
		
		// Clone nodes
		for(OpNode node : graph.getVertices()) {
			final OpNode clonedNode = cloneNode(node);
			nodeMap.put(node.getId(), clonedNode.getId());
			retVal.add(clonedNode);
		}
		
		// Clone links
		for(OpLink link : graph.getEdges()) {
			final OpNode origSource = link.getSource();
			final OpNode newSource = retVal.getNodeById(nodeMap.get(origSource.getId()), false);
			final OutputField sourceField = newSource.getOutputFieldWithKey(link.getSourceField().getKey());
			final OpNode origDest = link.getDestination();
			final OpNode newDest = retVal.getNodeById(nodeMap.get(origDest.getId()), false);
			final InputField destField = newDest.getInputFieldWithKey(link.getDestinationField().getKey());
			
			try {
				final OpLink newLink = new OpLink(newSource, sourceField.getKey(), newDest, destField.getKey());
				retVal.add(newLink);
			} catch (ItemMissingException e) {
				LOGGER.severe(e.getMessage());
			} catch (VertexNotFoundException e) {
				LOGGER.severe(e.getMessage());
			} catch (CycleDetectedException e) {
				LOGGER.severe(e.getMessage());
			}
		}
		
		// Clone notes
		final Notes notes = graph.getExtension(Notes.class);
		if(notes != null) {
			final Notes newNotes = new Notes();
			
			for(Note note:notes) {
				final Note newNote = cloneNote(note);
				newNotes.add(newNote);
			}
			
			retVal.putExtension(Notes.class, newNotes);
		}
		
		// XXX Other extensions. See note attached to class javadoc.
		
		return retVal;
	}
	
	/**
	 * Clones the given graph.
	 * 
	 * @param graph  the graph to clone
	 * 
	 * @return the cloned graph
	 */
	public static OpGraph cloneGraph(OpGraph graph) {
		return cloneGraph(graph, null, new HashMap<String, String>());
	}
	
	/**
	 * Clone a graph note.
	 * 
	 * @param note  the note to clone
	 * 
	 * @return the cloned note
	 */
	public static Note cloneNote(Note note) {
		final Note retVal = new Note(note.getTitle(), note.getBody());
		final JComponent oldComp = note.getExtension(JComponent.class);
		final JComponent newComp = retVal.getExtension(JComponent.class);
		if(newComp != null && oldComp != null) {
			newComp.setBackground(oldComp.getBackground());
			newComp.setLocation(oldComp.getLocation());
			newComp.setPreferredSize(newComp.getPreferredSize());
		}
		
		return retVal;
	}
}
