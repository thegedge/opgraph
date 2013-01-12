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

/**
 * Sets the title and body of a note.
 */
public class SetNoteTextEdit extends AbstractUndoableEdit {
	/** The note whose color will be set */
	private Note note;

	/** Old note title */
	private String oldTitle;

	/** New note title */
	private String newTitle;

	/** Old note body */
	private String oldBody;

	/** New note body */
	private String newBody;

	/**
	 * Constructs an edit that sets the body and title of a note.
	 * 
	 * @param note  the note whose color will be set
	 * @param title  the title of the note
	 * @param body  the body text of the note
	 * 
	 * @throws NullPointerException  if <code>note</code> is <code>null</code>
	 */
	public SetNoteTextEdit(Note note, String title, String body) {
		if(note == null)
			throw new NullPointerException();

		this.note = note;
		this.oldTitle = note.getTitle();
		this.oldBody = note.getBody();
		this.newTitle = title;
		this.newBody = body;
		perform();
	}

	/**
	 * Performs this edit.
	 */
	private void perform() {
		note.setTitle(newTitle);
		note.setBody(newBody);
	}

	//
	// AbstractUndoableEdit
	//

	@Override
	public String getPresentationName() {
		return "Set Note Title/Body";
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		note.setTitle(oldTitle);
		note.setBody(oldBody);
	}
}
