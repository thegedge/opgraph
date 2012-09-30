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
package ca.gedge.opgraph.app.components.canvas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import ca.gedge.opgraph.OpNode;

/**
 * Selection model used in a {@link GraphCanvas}.
 */
public class GraphCanvasSelectionModel {
	/** The currently selected nodes, or <code>null</code> if none selected */
	private ArrayList<OpNode> selectedNodes = new ArrayList<OpNode>();

	/**
	 * Gets the selected node.
	 * 
	 * @return the selected node, or <code>null</code> if
	 *         <code>{@link #getSelectedNodes()}.length != 1</code>
	 */
	public OpNode getSelectedNode() {
		return (selectedNodes.isEmpty() ? null : selectedNodes.get(0));
	}
	
	/**
	 * Sets the selected node.
	 * 
	 * @param node  the node to select 
	 */
	public void setSelectedNode(OpNode node) {
		final ArrayList<OpNode> old = new ArrayList<OpNode>(selectedNodes);
		selectedNodes.clear();
		if(node != null)
			selectedNodes.add(node);
		
		fireSelectionStateChanged(old);
	}

	/**
	 * Gets the list of selected nodes.
	 * 
	 * @return  the collection of selected nodes
	 */
	public Collection<OpNode> getSelectedNodes() {
		return Collections.unmodifiableCollection(selectedNodes);
	}
	
	/**
	 * Sets the nodes to select. If a specified node is not a member of
	 * the graph specified by this model, it is not selected.
	 * 
	 * @param newSelection  the new collection of nodes to select, or
	 *                      <code>null</code> to clear the selection
	 */
	public void setSelectedNodes(Collection<OpNode> newSelection) {
		// Create a set of all non-null nodes from given collection
		final ArrayList<OpNode> selected = new ArrayList<OpNode>();
		if(newSelection != null) {
			for(OpNode node : newSelection) {
				if(node != null)
					selected.add(node);
			}
		}
	
		// Only update selection if necessary
		//if(!newSelection.equals(selected)) {
			final Collection<OpNode> old = this.selectedNodes;
			selectedNodes = selected;
			fireSelectionStateChanged(old);
		//}
	}
	
	/**
	 * Adds a node to the selection, if it isn't already selected.
	 * 
	 * @param node  the node to add
	 */
	public void addNodeToSelection(OpNode node) {
		if(!selectedNodes.contains(node)) {
			final Collection<OpNode> old = new ArrayList<OpNode>(selectedNodes);
			selectedNodes.add(node);
			fireSelectionStateChanged(old);
		}
	}
	
	/**
	 * Remove a node from the selected nodes, if it is selected.
	 * 
	 * @param node  the node to remove
	 */
	public void removeNodeFromSelection(OpNode node) {
		if(selectedNodes.contains(node)) {
			final Collection<OpNode> old = new ArrayList<OpNode>(selectedNodes);
			selectedNodes.remove(node);
			fireSelectionStateChanged(old);
		}
	}
	
	/**
	 * Remove a collection of nodes from the selection.
	 * 
	 * @param nodes  the node to remove
	 */
	public void removeNodesFromSelection(Collection<OpNode> nodes) {
		if(nodes != null && !Collections.disjoint(nodes, selectedNodes)) {
			final Collection<OpNode> old = new ArrayList<OpNode>(selectedNodes);
			selectedNodes.removeAll(nodes);
			fireSelectionStateChanged(old);
		}
	}
	
	//
	// Listeners
	//
	
	private ArrayList<GraphCanvasSelectionListener> listeners = new ArrayList<GraphCanvasSelectionListener>();

	/**
	 * Adds a listener to this model.
	 * 
	 * @param listener  the listener to add
	 */
	public void addSelectionListener(GraphCanvasSelectionListener listener) {
		synchronized(listeners) {
			if(listener != null && !listeners.contains(listener))
				listeners.add(listener);
		}
	}
	
	/**
	 * Removes a listener from this model.
	 * 
	 * @param listener  the listener to remove
	 */
	public void removeSelectionListener(GraphCanvasSelectionListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}
	
	protected void fireSelectionStateChanged(Collection<OpNode> old) {
		synchronized(listeners) {
			for(GraphCanvasSelectionListener listener : listeners)
				listener.nodeSelectionChanged(old, selectedNodes);
		}
	}
}
