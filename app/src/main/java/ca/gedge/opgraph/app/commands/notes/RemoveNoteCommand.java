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
import ca.gedge.opgraph.app.edits.notes.RemoveNoteEdit;
import ca.gedge.opgraph.app.extensions.Note;
import ca.gedge.opgraph.app.extensions.Notes;

/**
 * A command for removing a note from the active model.
 */
public class RemoveNoteCommand extends AbstractAction {
	/** The note to remove */
	private final Note note;

	/**
	 * Constructs a command that removes a given note.
	 * 
	 * @param note  the note to remove
	 */
	public RemoveNoteCommand(Note note) {
		super("Remove Note");
		this.note = note;
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
				document.getUndoSupport().postEdit(new RemoveNoteEdit(notes, note));
		}
	}
}
