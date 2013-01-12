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
package ca.gedge.opgraph.app.commands.notes;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.edits.notes.AddNoteEdit;
import ca.gedge.opgraph.app.extensions.Notes;

/**
 * A command for adding a note to the active model.
 */
public class AddNoteCommand extends AbstractAction {
	/** The initial x-coordinate for the note */
	private int x;

	/** The initial y-coordinate for the note */
	private int y;

	/**
	 * Constructs an add note command that adds a note at the origin.
	 */
	public AddNoteCommand() {
		this(0, 0);
	}

	/**
	 * Constructs an add note command that adds a note at a given location.
	 * 
	 * @param x  the x-coordinate of the note
	 * @param y  the y-coordinate of the note
	 */
	public AddNoteCommand(int x, int y) {
		super("Add Note");
		this.x = x;
		this.y = y;
	}

	//
	// AbstractAction
	//

	@Override
	public void actionPerformed(ActionEvent e) {
		final GraphDocument document = GraphEditorModel.getActiveDocument();
		if(document != null && document.getGraph() != null) {
			final Notes notes = document.getGraph().getExtension(Notes.class);
			if(notes != null)
				document.getUndoSupport().postEdit(new AddNoteEdit(notes, "Note", "", x, y));
		}
	}
}
