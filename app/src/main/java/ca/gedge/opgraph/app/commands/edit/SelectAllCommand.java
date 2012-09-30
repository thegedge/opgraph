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
package ca.gedge.opgraph.app.commands.edit;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.components.canvas.GraphCanvas;

/**
 * Selects all nodes in the active document's {@link GraphCanvas}.
 */
public class SelectAllCommand extends AbstractAction {
	/**
	 * Default constructor.
	 */
	public SelectAllCommand() {
		super("Select All");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}
	
	//
	// AbstractAction
	//
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final GraphDocument document = GraphEditorModel.getActiveDocument();
		if(document != null)
			document.getSelectionModel().setSelectedNodes(document.getGraph().getVertices());
	}
}
