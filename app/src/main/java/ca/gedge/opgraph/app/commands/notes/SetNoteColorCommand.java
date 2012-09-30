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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.edits.notes.SetNoteColorEdit;
import ca.gedge.opgraph.app.extensions.Note;

/**
 * Sets the color of a note.
 */
public class SetNoteColorCommand extends AbstractAction {
	/** The initial x-coordinate for the note */
	private Note note;
	
	/** The initial y-coordinate for the note */
	private Color color;
	
	/**
	 * Constructs a command to set the color of a specified note.
	 * 
	 * @param note  the note whose color shall be set
	 * @param color  the color
	 * @param name  the name of this command
	 */
	public SetNoteColorCommand(Note note, Color color, String name) {
		super(name);
		
		this.note = note;
		this.color = color;
		
		// Create an icon
		final int PAD = 2;
		final int SZ = 12;
		final BufferedImage iconImage = new BufferedImage(SZ + 2*PAD, SZ + 2*PAD, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = iconImage.createGraphics();
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		g.setColor(color);
		g.fillRoundRect(PAD, PAD, SZ - 1, SZ - 1, SZ / 2, SZ / 2);
		g.setColor(Color.BLACK);
		g.drawRoundRect(PAD, PAD, SZ - 1, SZ - 1, SZ / 2, SZ / 2);
		
		putValue(SMALL_ICON, new ImageIcon(iconImage));
	}
	
	//
	// AbstractAction
	//
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final GraphDocument document = GraphEditorModel.getActiveDocument();
		if(document != null)
			document.getUndoSupport().postEdit(new SetNoteColorEdit(note, color));
	}
}
