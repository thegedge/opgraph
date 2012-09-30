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
package ca.gedge.opgraph.app.commands.notes;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.components.PathAddressableMenu;
import ca.gedge.opgraph.app.extensions.Note;

/**
 * Menu provider for core functions.
 */
public class NotesMenuProvider implements MenuProvider {
	@Override
	public void installItems(final GraphEditorModel model, PathAddressableMenu menu) {
		// 
	}

	@Override
	public void installPopupItems(Object context, MouseEvent event, GraphEditorModel model, PathAddressableMenu menu) {
		final boolean isGraph = (context instanceof OpGraph);
		final boolean isNote = (context instanceof Note);
		
		if(isGraph || isNote) {
			final Point loc = model.getCanvas().getMousePosition();
			menu.addSeparator("");
			menu.addMenuItem("add_note", new AddNoteCommand(loc.x, loc.y));
		} 

		if(isNote) {
			final Note note = (Note)context;

			final Object [] colors = new Object[] {
				new Color(255, 150, 150), "Red",
				new Color(150, 255, 150), "Green",
				new Color(150, 150, 255), "Blue",
				new Color(255, 255, 150), "Yellow",
				new Color(255, 150, 255), "Magenta",
				new Color(255, 200, 100), "Orange",
				new Color(200, 200, 200), "Gray"
			};
			
			menu.addMenuItem("remove_note", new RemoveNoteCommand(note));
			menu.addSeparator("");
			
			final JMenu colorsMenu = menu.addMenu("colors", "Colors");
			for(int index = 0; index < colors.length; index += 2) {
				final Color color = (Color)colors[index];
				final String name = (String)colors[index + 1];
				colorsMenu.add(new JMenuItem(new SetNoteColorCommand(note, color, name)));  
			}
		}
	}
}
