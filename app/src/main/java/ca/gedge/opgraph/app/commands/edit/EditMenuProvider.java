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
package ca.gedge.opgraph.app.commands.edit;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.components.PathAddressableMenu;
import ca.gedge.opgraph.app.components.canvas.GraphCanvasSelectionListener;

/**
 * Menu provider for core functions.
 */
public class EditMenuProvider implements MenuProvider {
	@Override
	public void installItems(final GraphEditorModel model, PathAddressableMenu menu) {
		final JMenu edit = menu.addMenu("edit", "Edit");

		final UndoCommand undo = new UndoCommand(model.getDocument().getUndoManager());
		final RedoCommand redo = new RedoCommand(model.getDocument().getUndoManager());

		final CopyCommand copy = new CopyCommand();
		final PasteCommand paste = new PasteCommand();
		final DuplicateCommand duplicate = new DuplicateCommand();

		menu.addMenuItem("edit/copy", copy);
		menu.addMenuItem("edit/paste", paste);
		menu.addMenuItem("edit/duplicate", duplicate);
		menu.addSeparator("edit");

		menu.addMenuItem("edit/undo", undo);
		menu.addMenuItem("edit/redo", redo);
		menu.addSeparator("edit");
		final JMenuItem delete = menu.addMenuItem("edit/delete", new DeleteCommand());
		menu.addMenuItem("edit/select all", new SelectAllCommand());

		// Setup backspace keybinding for delete
		final KeyStroke bsKs = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
		final InputMap inputMap = delete.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(bsKs, inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)));

		delete.setEnabled(false);

		// Listen to property changes and update menu items to reflect new state
		model.getDocument().addPropertyChangeListener(GraphDocument.UNDO_STATE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				undo.update();
				redo.update();
			}
		});

		model.getDocument().addPropertyChangeListener(GraphDocument.PROCESSING_CONTEXT, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				edit.setEnabled(evt.getNewValue() == null);
			}
		});

		model.getCanvas().getSelectionModel().addSelectionListener(new GraphCanvasSelectionListener() {
			@Override
			public void nodeSelectionChanged(Collection<OpNode> old, Collection<OpNode> selected) {
				delete.setEnabled(selected.size() > 0);
				duplicate.setEnabled(selected.size() > 0);
			}
		});
	}

	@Override
	public void installPopupItems(Object context, MouseEvent event, GraphEditorModel model, PathAddressableMenu menu) {
		// Add copy and paste commands for nodes
		if(context != null && (context instanceof OpNode || context instanceof OpGraph)) {
			// Add copy command if selection is available
			if(model.getDocument().getSelectionModel().getSelectedNodes().size() > 0)
				menu.addMenuItem("copy", new CopyCommand());

			// Check clipboard
			if(!GraphicsEnvironment.isHeadless()) {
				final Transferable clipboardContents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(model.getCanvas());
				if(clipboardContents != null && clipboardContents.isDataFlavorSupported(SubgraphClipboardContents.copyFlavor)) {
					// Add paste command
					menu.addMenuItem("paste", new PasteCommand());
				}
			}

			// Add duplicate command if selection is available
			if(model.getDocument().getSelectionModel().getSelectedNodes().size() > 0)
				menu.addMenuItem("duplicate", new DuplicateCommand());
		}
	}
}
