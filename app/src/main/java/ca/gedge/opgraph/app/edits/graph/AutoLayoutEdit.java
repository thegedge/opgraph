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
package ca.gedge.opgraph.app.edits.graph;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.undo.CompoundEdit;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.components.canvas.CanvasNode;
import ca.gedge.opgraph.app.extensions.NodeMetadata;

/**
 * An edit which automatically lays out nodes in a graph.
 */
public class AutoLayoutEdit extends CompoundEdit {
	/**
	 * Constructs an edit that automatically lays out nodes in a graph. 
	 * 
	 * @param graph  the graph
	 * 
	 * @throws IllegalArgumentException  if at least one node in the graph does
	 *                                   not have a {@link JComponent} extension
	 */
	public AutoLayoutEdit(OpGraph graph) {
		// First make sure every node has a JComponent extension
		for(OpNode node : graph.getVertices()) {
			if(node.getExtension(JComponent.class) == null)
				throw new IllegalArgumentException("Not all npdes have JComponent extension");
		}

		// Perform automatic layout
		final List<OpNode> nodes = new ArrayList<OpNode>(graph.getVertices());
		int x = 15;
		for(int level = 0; !nodes.isEmpty(); ++level) {
			int maxWidth = 0;
			int y = 15;

			final Iterator<OpNode> iter = nodes.iterator();
			while(iter.hasNext()) {
				final OpNode v = iter.next();

				// Skip if node not at this level, otherwise remove it
				// because it shouldn't be processed at higher levels
				if(graph.getLevel(v) != level)
					continue;

				iter.remove();

				// Place those nodes
				final JComponent node = v.getExtension(JComponent.class);
				final Dimension pref = node.getPreferredSize();

				// If metadata null, add a metadata extension.
				NodeMetadata meta = v.getExtension(NodeMetadata.class);
				if(meta == null) {
					meta = new NodeMetadata(x, y);
					v.putExtension(NodeMetadata.class, meta);
				}

				// Post the undoable edit
				final ArrayList<OpNode> nodeToMove = new ArrayList<OpNode>();
				nodeToMove.add(v);

				final int deltaX = x - meta.getX();
				final int deltaY = y - meta.getY();
				if(deltaX != 0 || deltaY != 0)
					addEdit(new MoveNodesEdit(nodeToMove, deltaX, deltaY));

				// Move downwards, and extend the max width to this node, if it
				// is bigger than any previous node
				maxWidth = Math.max(maxWidth, pref.width);
				y += pref.height + 15;
			}

			x += maxWidth + 50;
		}

		super.end();
	}

	//
	// CompoundEdit
	//

	@Override
	public String getUndoPresentationName() {
	    return "Undo " + getPresentationName();
	}

	@Override
	public String getRedoPresentationName() {
	    return "Redo " + getPresentationName();
	}

	@Override
	public String getPresentationName() {
	    return "Auto Layout";
	}
}
