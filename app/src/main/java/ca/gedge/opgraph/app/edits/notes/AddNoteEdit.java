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

import javax.swing.JComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.gedge.opgraph.app.extensions.Note;
import ca.gedge.opgraph.app.extensions.Notes;

/**
 * Adds a note to a note collection.
 */
public class AddNoteEdit extends AbstractUndoableEdit {
	/** The notes to which this edit was applied */
	private Notes notes;
	
	/** The note that was added */
	private Note note;

	/**
	 * Constructs an edit that adds a note to a graph at the origin.
	 * 
	 * @param notes  the notes to which this edit will be applied
	 * @param title  the initial title
	 * @param body  the initial body text
	 * 
	 * @throws NullPointerException  if <code>notes<code> is <code>null</code>
	 */
	public AddNoteEdit(Notes notes, String title, String body) {
		this(notes, title, body, 0, 0);
	}
	
	/**
	 * Constructs an edit that adds a note to a graph.
	 * 
	 * @param notes  the notes to which this edit will be applied
	 * @param title  the initial title
	 * @param body  the initial body text
	 * @param x  the initial x-coordinate of the note
	 * @param y  the initial y-coordinate of the note
	 * 
	 * @throws NullPointerException  if <code>notes<code> is <code>null</code>
	 */
	public AddNoteEdit(Notes notes, String title, String body, int x, int y) {
		if(notes == null)
			throw new NullPointerException();
		
		this.notes = notes;
		this.note = new Note(title, body);
		
		final JComponent noteComp = this.note.getExtension(JComponent.class);
		if(noteComp != null)
			noteComp.setLocation(x, y);
		
		perform();
	}
	
	/**
	 * Performs this edit.
	 */
	private void perform() {
		notes.add(note);
	}
	
	//
	// AbstractUndoableEdit
	//
	
	@Override
	public String getPresentationName() {
		return "Add Note";
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		notes.remove(note);
	}
}
