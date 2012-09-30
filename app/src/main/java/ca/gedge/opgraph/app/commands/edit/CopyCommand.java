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

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;

/**
 * Copy selected nodes to system clipboard.
 */
public class CopyCommand extends AbstractAction {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(CopyCommand.class.getName());
	
	/**
	 * Default constructor.
	 */
	public CopyCommand() {
		super("Copy");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(GraphicsEnvironment.isHeadless())
			return;
		
		final GraphDocument document = GraphEditorModel.getActiveDocument();
		if(document != null) {
			// If a selection exists, create a copy of the selection
			final Collection<OpNode> selectedNodes = document.getSelectionModel().getSelectedNodes();
			if(selectedNodes.size() > 0) {
				final OpGraph graph = document.getGraph();
				
				// Copy selected nodes
				final OpGraph selectedGraph = new OpGraph();
				for(OpNode node : selectedNodes)
					selectedGraph.add(node);
				
				// For each selected node, copy outgoing links if they are fully contained in the selection
				for(OpNode selectedNode : selectedNodes) {
					final Collection<OpLink> outgoingLinks = graph.getOutgoingEdges(selectedNode);
					for(OpLink link : outgoingLinks) {
						if(selectedNodes.contains(link.getDestination())) {
							try {
								selectedGraph.add(link);
							} catch(VertexNotFoundException exc) {
								LOGGER.severe(exc.getMessage());
							} catch(CycleDetectedException exc) {
								LOGGER.severe(exc.getMessage());
							}
						}
					}
				}
				
				// Add to system clipboard
				final SubgraphClipboardContents clipboardContents = new SubgraphClipboardContents(document, selectedGraph);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(clipboardContents, document.getCanvas());
			}
		}
	}
}
