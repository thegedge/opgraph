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
package ca.gedge.opgraph.app.components.library;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.components.ErrorDialog;
import ca.gedge.opgraph.app.components.PathAddressableMenu;
import ca.gedge.opgraph.app.edits.graph.AddNodeEdit;
import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.library.NodeLibrary;

/**
 * Implementation of {@link MenuProvider} for {@link NodeLibrary}. Provides
 * menu items which allow the user to instantiate nodes known to the library  
 */
public class NodeLibraryMenuProvider implements MenuProvider {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(NodeLibraryMenuProvider.class.getName());

	/**
	 * Returns a menu which reflects a given {@link NodeLibrary}.
	 * 
	 * @param menu  the menu to add the items to
	 * @param document  the document upon which the returned menu items will act
	 * @param library  the library to construct the menu from 
	 * @param point  the point at which a node will be instantiated
	 */
	private void addMenuItems(JMenu menu, final GraphDocument document, NodeLibrary library, final Point point) {
		if(library != null) {
			final Map<String, List<NodeData>> categoryMap = library.getCategoryMap();
			for(Map.Entry<String, List<NodeData>> entry : categoryMap.entrySet()) {
				final JMenu categoryMenu = new JMenu(entry.getKey());

				// For each NodeData in this category, create a menu item that will
				// instantiate that node into the document
				for(final NodeData info : entry.getValue()) {
					categoryMenu.add(new AbstractAction(info.name) {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								final OpGraph graph = document.getGraph();
								document.getUndoSupport().postEdit(new AddNodeEdit(graph, info, point.x, point.y));
							} catch(InstantiationException exc) {
								final String message = "Unable to create '" + info.name + "'";
								LOGGER.severe(message);
								ErrorDialog.showError(exc, message);
							}
						}
					});
				}

				// Now add it to the parent menu
				menu.add(categoryMenu);
			}
		}
	}

	//
	// MenuProvider implementation
	//
	@Override
	public void installItems(GraphEditorModel model, PathAddressableMenu menu) {
		// TODO Auto-generated method stub
	}

	@Override
	public void installPopupItems(Object context, MouseEvent event, GraphEditorModel model, PathAddressableMenu menu) {
		if(model.getNodeLibrary().getLibrary().getNodeInfo().size() > 0) {
			final JMenu addNodeMenu = menu.addMenu("add_node", "Add");
			addMenuItems(addNodeMenu, model.getDocument(), model.getNodeLibrary().getLibrary(), event.getPoint());
		}
	}
}
