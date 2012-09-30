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
import javax.swing.undo.UndoManager;

/**
 * Sends an undo command to a given {@link UndoManager}.
 */
public class UndoCommand extends AbstractAction {
	/** The undo manager to send undo commands to */
	private UndoManager manager;
	
	/**
	 * Constructs an undo command for a given undo manager.
	 * 
	 * @param manager  the undo manager
	 */
	public UndoCommand(UndoManager manager) {
		this.manager = manager;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		update();
	}
	
	/**
	 * Updates the text and enabled state of this command.
	 */
	public void update() {
		setEnabled(manager.canUndo());
		putValue(NAME, manager.getUndoPresentationName());
	}
	
	//
	// AbstractAction
	//
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(manager.canUndo())
			manager.undo();
	}
}
