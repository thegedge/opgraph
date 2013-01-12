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
package ca.gedge.opgraph.nodes.menu;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.components.canvas.GraphCanvasSelectionModel;
import ca.gedge.opgraph.nodes.menu.edits.CreateMacroEdit;

/**
 * A command for creating a macro from the selected nodes in the active editor's canvas.
 */
public class CreateMacroCommand extends AbstractAction {
	/**
	 * Constructs a create macro command that automatically creates a macro
	 * from the selected nodes in the active editor's canvas.
	 */
	public CreateMacroCommand() {
		super("Create Macro From Selected Nodes");

		final int CTRL = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, CTRL));
	}

	//
	// AbstractAction
	//

	@Override
	public void actionPerformed(ActionEvent e) {
		final GraphDocument document = GraphEditorModel.getActiveDocument();
		if(document != null) {
			final GraphCanvasSelectionModel selectionModel = document.getSelectionModel();
			final Collection<OpNode> selectedNodes = selectionModel.getSelectedNodes();
			document.getUndoSupport().postEdit(new CreateMacroEdit(document.getGraph(), selectedNodes));
		}
	}
}
