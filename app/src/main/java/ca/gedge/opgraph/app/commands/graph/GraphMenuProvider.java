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
package ca.gedge.opgraph.app.commands.graph;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.components.PathAddressableMenu;
import ca.gedge.opgraph.app.components.canvas.GraphCanvasSelectionListener;
import ca.gedge.opgraph.app.components.canvas.GridLayer;

/**
 * Menu provider for core functions.
 */
public class GraphMenuProvider implements MenuProvider {
	@Override
	public void installItems(final GraphEditorModel model, PathAddressableMenu menu) {
		final JMenu graph = menu.addMenu("graph", "Graph");

		final JMenuItem move1 = menu.addMenuItem("graph/move down", new MoveNodeCommand(0, GridLayer.DEFAULT_GRID_SPACING / 2));
		final JMenuItem move2 = menu.addMenuItem("graph/move up", new MoveNodeCommand(0, -GridLayer.DEFAULT_GRID_SPACING / 2));
		final JMenuItem move3 = menu.addMenuItem("graph/move right", new MoveNodeCommand(GridLayer.DEFAULT_GRID_SPACING / 2, 0));
		final JMenuItem move4 = menu.addMenuItem("graph/move left", new MoveNodeCommand(-GridLayer.DEFAULT_GRID_SPACING / 2, 0));
		menu.addSeparator("graph");
		menu.addMenuItem("graph/auto layout", new AutoLayoutCommand());

		move1.setEnabled(false);
		move2.setEnabled(false);
		move3.setEnabled(false);
		move4.setEnabled(false);

		model.getDocument().addPropertyChangeListener(GraphDocument.PROCESSING_CONTEXT, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				graph.setEnabled(evt.getNewValue() == null);
			}
		});

		model.getCanvas().getSelectionModel().addSelectionListener(new GraphCanvasSelectionListener() {
			@Override
			public void nodeSelectionChanged(Collection<OpNode> old, Collection<OpNode> selected) {
				move1.setEnabled(selected.size() > 0);
				move2.setEnabled(selected.size() > 0);
				move3.setEnabled(selected.size() > 0);
				move4.setEnabled(selected.size() > 0);
			}
		});
	}

	@Override
	public void installPopupItems(Object context, MouseEvent event, GraphEditorModel model, PathAddressableMenu menu) {
		//
	}
}
