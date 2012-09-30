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

/**
 * Removes a link between two fields.
 */
public class RemoveLinkEdit extends AbstractUndoableEdit {
	/** The graph to which this edit was applied  */
	private OpGraph graph;
	
	/** The link that was removed */
	private final OpLink link;
	
	/**
	 * Constructs an edit that removes a given link from a canvas model.
	 * 
	 * @param graph  the graph to which this edit will be applied
	 * @param link  the link to add
	 */
	public RemoveLinkEdit(OpGraph graph, OpLink link) {
		this.graph = graph;
		this.link = link;
		perform();
	}
	
	/**
	 * Performs this edit.
	 */
	private void perform() {
		graph.remove(link);
	}
	
	//
	// AbstractUndoableEdit
	//
	
	@Override
	public String getPresentationName() {
		return "Remove Link";
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		try {
			graph.add(link);
		} catch(VertexNotFoundException exc) {
			throw new CannotUndoException();
		} catch(CycleDetectedException exc) {
			throw new CannotUndoException();
		}
	}
}
