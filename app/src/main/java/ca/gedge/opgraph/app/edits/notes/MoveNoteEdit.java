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
import javax.swing.undo.UndoableEdit;

import ca.gedge.opgraph.app.commands.graph.MoveNodeCommand;
import ca.gedge.opgraph.app.extensions.Note;

/**
 * Sets the location of a note.
 */
public class MoveNoteEdit extends AbstractUndoableEdit {
	/** The note's UI component */
	private JComponent noteComp;

	/** The distance along the x-axis to move the node */
	private int deltaX;

	/** The distance along the y-axis to move the node */
	private int deltaY;

	/**
	 * Constructs an edit that sets the location of a specified note.
	 * 
	 * @param note  the note whose location will be set
	 * @param deltaX  the x-axis delta
	 * @param deltaY  the y-axis delta
	 * 
	 * @throws NullPointerException  if <code>note</code> is <code>null</code>,
	 *                               or <code>note.getExtension(JComponent.class)</code> is
	 *                               <code>null</code>
	 */
	public MoveNoteEdit(Note note, int deltaX, int deltaY) {
		if(note == null)
			throw new NullPointerException();

		noteComp = note.getExtension(JComponent.class);
		if(noteComp == null)
			throw new NullPointerException();

		this.deltaX = deltaX;
		this.deltaY = deltaY;
		perform();
	}

	// XXX assumes noteComp doesn't change for the note

	/**
	 * Performs this edit.
	 */
	private void perform() {
		noteComp.setLocation(noteComp.getX() + deltaX, noteComp.getY() + deltaY);
	}

	//
	// AbstractUndoableEdit
	//

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		if(anEdit instanceof MoveNoteEdit) {
			final MoveNoteEdit moveEdit = (MoveNoteEdit)anEdit;
			if(noteComp.equals(moveEdit.noteComp)) {
				final boolean xDirSame = ((deltaX <= 0 && moveEdit.deltaX <= 0) || (deltaX >= 0 && moveEdit.deltaX >= 0));
				final boolean yDirSame = ((deltaY <= 0 && moveEdit.deltaY <= 0) || (deltaY >= 0 && moveEdit.deltaY >= 0));
				if(xDirSame && yDirSame) {
					deltaX += moveEdit.deltaX;
					deltaY += moveEdit.deltaY;
					moveEdit.die();
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public String getPresentationName() {
		final String prefix = "Move Note";
		final String suffix = MoveNodeCommand.getMoveString(deltaX, deltaY);
		if(suffix.length() == 0)
			return prefix;

		return prefix + " " + suffix;
	}

	@Override
	public boolean isSignificant() {
		return (deltaX != 0 || deltaY != 0);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		noteComp.setLocation(noteComp.getX() - deltaX, noteComp.getY() - deltaY);
	}
}
