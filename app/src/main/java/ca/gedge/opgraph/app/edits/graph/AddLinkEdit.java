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

import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;

/**
 * Adds a link between two fields.
 */
public class AddLinkEdit extends AbstractUndoableEdit {
	/** The graph to which this edit was applied  */
	private OpGraph graph;

	/** The node whose name is changing */
	private final OpLink link;

	/**
	 * Constructs an edit that creates a link between two fields.
	 * 
	 * @param graph  the graph to which this edit will be applied
	 * @param link  the link to add 
	 * 
	 * @throws CycleDetectedException  if creation of the link creates a cycle
	 * @throws VertexNotFoundException  if either/both of the source/destination of
	 *                                  the link is not a member of the graph 
	 */
	public AddLinkEdit(OpGraph graph, OpLink link)
		throws VertexNotFoundException, CycleDetectedException
	{
		this.graph = graph;
		this.link = link;
		perform();
	}

	/**
	 * Performs this edit.
	 */
	private void perform() throws VertexNotFoundException, CycleDetectedException {
		graph.add(link);
	}

	//
	// AbstractUndoableEdit
	//

	@Override
	public String getPresentationName() {
		return "Add Link";
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		try {
			perform();
		} catch(VertexNotFoundException exc) {
			throw new CannotRedoException();
		} catch(CycleDetectedException exc) {
			throw new CannotRedoException();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		graph.remove(link);
	}
}
