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
package ca.gedge.opgraph.app.edits.graph;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.commands.graph.MoveNodeCommand;
import ca.gedge.opgraph.app.extensions.NodeMetadata;

/**
 * Moves a collection of nodes a specified amount.
 */
public class MoveNodesEdit extends AbstractUndoableEdit {
	/** The nodes to move */
	private Collection<NodeMetadata> metas;
	
	/** The distance along the x-axis to move the node */
	private int deltaX;
	
	/** The distance along the y-axis to move the node */
	private int deltaY;
	
	/**
	 * Constructs a move edit that moves a collection of nodes a specified amount.
	 * 
	 * @param nodes  the nodes to move
	 * @param deltaX  the x-axis delta
	 * @param deltaY  the y-axis delta
	 */
	public MoveNodesEdit(Collection<OpNode> nodes, int deltaX, int deltaY) {
		this.deltaX = deltaX;
		this.deltaY = deltaY;

		this.metas = new ArrayList<NodeMetadata>();
		if(nodes != null) {
			for(OpNode node : nodes)
				this.metas.add(node.getExtension(NodeMetadata.class));
		}
		
		perform();
	}
	
	/**
	 * Performs this edit.
	 */
	private void perform() {
		for(NodeMetadata meta : metas)
			meta.setLocation(meta.getX() + deltaX, meta.getY() + deltaY);
	}
	
	//
	// AbstractUndoableEdit
	//
	
	@Override
	public String getPresentationName() {
		if(metas.size() == 0)
			return super.getPresentationName();
		
		String prefix = "Move Nodes";
		if(metas.size() == 1)
			prefix = "Move Node";
		
		final String suffix = MoveNodeCommand.getMoveString(deltaX, deltaY);
		if(suffix.length() == 0)
			return prefix;
		
		return prefix + " " + suffix;
	}

	@Override
	public boolean replaceEdit(UndoableEdit anEdit) {
		if(anEdit instanceof MoveNodesEdit) {
			final MoveNodesEdit moveEdit = (MoveNodesEdit)anEdit;
			if(metas.equals(moveEdit.metas)) {
				final boolean xDirSame = ((deltaX <= 0 && moveEdit.deltaX <= 0) || (deltaX >= 0 && moveEdit.deltaX >= 0));
				final boolean yDirSame = ((deltaY <= 0 && moveEdit.deltaY <= 0) || (deltaY >= 0 && moveEdit.deltaY >= 0));
				if(xDirSame && yDirSame) {
					deltaX += moveEdit.deltaX;
					deltaY += moveEdit.deltaY;
					moveEdit.die();
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public boolean isSignificant() {
		return ((metas.size() > 0) && (deltaX != 0 || deltaY != 0));
	}
	
	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}
	
	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for(NodeMetadata meta : metas)
			meta.setLocation(meta.getX() - deltaX, meta.getY() - deltaY);
	}
}
