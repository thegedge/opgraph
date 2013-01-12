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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;

/**
 * Sends a redo command to a given {@link UndoManager}.
 */
public class RedoCommand extends AbstractAction {
	/** The undo manager to send undo commands to */
	private UndoManager manager;

	/**
	 * Constructs a redo command for a given undo manager.
	 * 
	 * @param manager  the undo manager
	 */
	public RedoCommand(UndoManager manager) {
		this.manager = manager;

		final int CTRL = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, CTRL | InputEvent.SHIFT_MASK));

		update();
	}

	/**
	 * Updates the text and enabled state of this command.
	 */
	public void update() {
		setEnabled(manager.canRedo());
		putValue(NAME, manager.getRedoPresentationName());
	}

	//
	// AbstractAction
	//

	@Override
	public void actionPerformed(ActionEvent e) {
		if(manager.canRedo())
			manager.redo();
	}
}
