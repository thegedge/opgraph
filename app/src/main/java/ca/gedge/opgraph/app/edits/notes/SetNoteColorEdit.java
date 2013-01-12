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

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.gedge.opgraph.app.extensions.Note;

/**
 * Sets the color of a note.
 */
public class SetNoteColorEdit extends AbstractUndoableEdit {
	/** The note's UI component */
	private JComponent noteComp;

	/** Old note color */
	private Color oldColor;

	/** New note color */
	private Color newColor;

	/**
	 * Constructs an edit that sets the color of a specified note.
	 * 
	 * @param note  the note whose color will be set
	 * @param color  the new color
	 * 
	 * @throws NullPointerException  if <code>note</code> is <code>null</code>,
	 *                               or <code>note.getExtension(JComponent.class)</code> is
	 *                               <code>null</code>
	 */
	public SetNoteColorEdit(Note note, Color color) {
		if(note == null)
			throw new NullPointerException();

		noteComp = note.getExtension(JComponent.class);
		if(noteComp == null)
			throw new NullPointerException();

		this.oldColor = noteComp.getBackground();
		this.newColor = color;
		perform();
	}

	// XXX assumes noteComp doesn't change for the note

	/**
	 * Performs this edit.
	 */
	private void perform() {
		noteComp.setBackground(newColor);
	}

	//
	// AbstractUndoableEdit
	//

	@Override
	public String getPresentationName() {
		return "Set Note Color";
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		noteComp.setBackground(oldColor);
	}
}
