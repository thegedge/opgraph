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
package ca.gedge.opgraph.app.edits.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.components.ErrorDialog;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;

/**
 * Deletes a collection of nodes.
 */
public class DeleteNodesEdit extends AbstractUndoableEdit {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(DeleteNodesEdit.class.getName());
	
	/** The graph to which this edit was applied  */
	private OpGraph graph;
	
	/** The nodes to remove */
	private List<OpNode> nodes;
	
	/** The links that get removed when the nodes get removed */
	private Set<OpLink> links;
	
	/**
	 * Constructs a delete edit that removes a collection of nodes from a
	 * specified canvas model.
	 * 
	 * @param graph  the graph to which this edit will be applied
	 * @param nodes  the nodes to remove
	 */
	public DeleteNodesEdit(OpGraph graph, Collection<OpNode> nodes) {
		this.graph = graph;
		this.nodes = new ArrayList<OpNode>();
		this.links = new TreeSet<OpLink>();
		
		if(nodes != null)
			this.nodes.addAll(nodes);
		
		for(OpNode node : this.nodes) {
			if(graph.contains(node)) {
				links.addAll(graph.getIncomingEdges(node));
				links.addAll(graph.getOutgoingEdges(node));
			}
		}
		
		perform();
	}
	
	/**
	 * Performs this edit.
	 */
	private void perform() {
		for(OpNode link : nodes)
			graph.remove(link);
	}
	
	//
	// AbstractUndoableEdit
	//

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();

		// Add nodes
		for(OpNode node : nodes)
			graph.add(node);
		
		// Add old links
		for(OpLink link : links) {
			try {
				graph.add(link);
			} catch(VertexNotFoundException exc) {
				LOGGER.severe("Erroneous state that should never happen");
				ErrorDialog.showError(exc);
			} catch(CycleDetectedException exc) {
				LOGGER.severe("Erroneous state that should never happen");
				ErrorDialog.showError(exc);
			}
		}
	}
	
	@Override
	public String getPresentationName() {
		if(nodes.size() == 0)
			return "Delete";
		else if(nodes.size() == 1)
			return "Delete Node";
		else
			return "Delete Nodes";
	}
	
	@Override
	public boolean isSignificant() {
		return (nodes.size() > 0);
	}
}
