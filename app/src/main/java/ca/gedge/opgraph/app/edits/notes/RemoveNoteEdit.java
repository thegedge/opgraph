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
/**
 * 
 */
package ca.gedge.opgraph.app.edits.notes;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.gedge.opgraph.app.extensions.Note;
import ca.gedge.opgraph.app.extensions.Notes;
import ca.gedge.opgraph.library.NodeData;

/**
 * Removes a note from a note collection.
 */
public class RemoveNoteEdit extends AbstractUndoableEdit {
	/** The graph to which this edit was applied */
	private Notes notes;
	
	/** The note that was removed */
	private Note note;
	
	/**
	 * Constructs an edit that constructs a node described by a specified
	 * {@link NodeData} and adds at the given initial location.
	 * 
	 * @param notes  the notes to which this edit will be applied
	 * @param note  the note to remove
	 * 
	 * @throws NullPointerException  if <code>notes<code> is null
	 */
	public RemoveNoteEdit(Notes notes, Note note) {
		if(notes == null)
			throw new NullPointerException();
		
		this.notes = notes;
		this.note = note;
		perform();
	}
	
	/**
	 * Performs this edit.
	 */
	private void perform() {
		notes.remove(note);
	}
	
	//
	// AbstractUndoableEdit
	//
	
	@Override
	public String getPresentationName() {
		return "Remove Note";
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		notes.add(note);
	}
}
