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
package ca.gedge.opgraph.app.edits.node;

import java.util.Properties;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.gedge.opgraph.app.extensions.NodeSettings;

/**
 * Changes a node's settings.
 */
public class NodeSettingsEdit extends AbstractUndoableEdit {
	/** The settings for the node */
	private NodeSettings settings;
	
	/** The new settings for the node */
	private Properties newSettings;
	
	/** The old settings for the node */
	private Properties oldSettings;
	
	/**
	 * Constructs a node settings edit.
	 * 
	 * @param settings  the settings object to modify
	 * @param newSettings  the new settings to set
	 */
	public NodeSettingsEdit(NodeSettings settings, Properties newSettings) {
		this.settings = settings;
		this.oldSettings = settings.getSettings();
		this.newSettings = newSettings;
		perform();
	}

	/**
	 * Performs the edit
	 */
	private void perform() {
		settings.loadSettings(newSettings);
	}

	//
	// AbstractUndoableEdit
	//

	@Override
	public String getPresentationName() {
		return "Change Node Settings";
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		settings.loadSettings(oldSettings);
	}
}
