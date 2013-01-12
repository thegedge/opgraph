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

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.extensions.NodeMetadata;
import ca.gedge.opgraph.library.NodeData;

/**
 * A canvas edit that moves a collection of nodes a specified amount.
 */
public class AddNodeEdit extends AbstractUndoableEdit {
	/** The graph to which this edit was applied  */
	private OpGraph graph;

	/** The node information */
	private NodeData info;

	/** The node that was added */
	private OpNode node;

	/**
	 * Constructs an edit that constructs a node described by a specified
	 * {@link NodeData} and adds it to the given canvas.
	 * 
	 * @param graph  the graph to which this edit will be applied
	 * @param info  the info used for constructing the node
	 * 
	 * @throws InstantiationException  if the node could not be instantiated
	 *                                 from the instantiator in the node info
	 * @throws NullPointerException  if any argument is <code>null</code>
	 */
	public AddNodeEdit(OpGraph graph, NodeData info)
		throws InstantiationException
	{
		this(graph, info, 0, 0);
	}

	/**
	 * Constructs an edit that constructs a node described by a specified
	 * {@link NodeData} and adds it to the given canvas at the given
	 * initial location.
	 * 
	 * @param graph  the graph to which this edit will be applied
	 * @param info  the info used for constructing the node
	 * @param x  the initial x-coordinate for the node
	 * @param y  the initial y-coordinate for the node
	 * 
	 * @throws InstantiationException  if the node could not be instantiated
	 *                                 from the instantiator in the node info
	 * @throws NullPointerException  if either the canvas or info is <code>null</code>
	 */
	public AddNodeEdit(OpGraph graph, NodeData info, int x, int y)
		throws InstantiationException
	{
		this.graph = graph;
		this.info = info;
		this.node = this.info.instantiator.newInstance();
		this.node.putExtension(NodeMetadata.class, new NodeMetadata(x, y));
		perform();
	}

	/**
	 * Constructs an edit that adds a node to the given canvas at the given
	 * initial location.
	 * 
	 * @param graph  the graph to which this edit will be applied
	 * @param node  the node to add to the graph
	 */
	public AddNodeEdit(OpGraph graph, OpNode node) {
		this.graph = graph;
		this.node = node;
		perform();
	}

	/**
	 * Performs this edit.
	 */
	private void perform() {
		graph.add(node);
	}

	//
	// AbstractUndoableEdit
	//

	@Override
	public String getPresentationName() {
		return "Add Node";
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		graph.remove(node);
	}
}
