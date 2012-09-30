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
package ca.gedge.opgraph.app.edits.node;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import ca.gedge.opgraph.OpNode;

/**
 * Modifies the name of a node.
 */
public class ChangeNodeNameEdit extends AbstractUndoableEdit {
	/** The node whose name is changing */
	private final OpNode node;
	
	/** The new name of the node */
	private final String newName;
	
	/** The previous name of the node */
	private String oldName;
	
	/**
	 * Constructs an edit that changes a node's name. 
	 * 
	 * @param node  the node whose name will be changed
	 * @param name  the new name
	 */
	public ChangeNodeNameEdit(OpNode node, String name) {
		this.node = node;
		this.newName = name;
		this.oldName = node.getName();
		perform();
	}
	
	/**
	 * Performs this edit.
	 */
	private void perform() {
		node.setName(newName);
	}
	
	//
	// AbstractUndoableEdit
	//
	
	@Override
	public boolean isSignificant() {
		return !oldName.equals(newName);
	}
	
	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		boolean ret = false;
		if(anEdit instanceof ChangeNodeNameEdit) {
			final ChangeNodeNameEdit cnn = (ChangeNodeNameEdit)anEdit;
			ret = (cnn.node == node
			      && (newName != null && newName.equals(cnn.newName))
			      && (oldName != null && oldName.equals(cnn.oldName)) );
			
			if(ret)
				oldName = cnn.oldName;
		}
		return ret;
	}
	
	@Override
	public String getPresentationName() {
		return "Change Node Name";
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		node.setName(oldName);
	}
}
