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
package ca.gedge.opgraph.nodes.menu;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.JMenuItem;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.components.PathAddressableMenu;
import ca.gedge.opgraph.app.components.canvas.GraphCanvasSelectionListener;
import ca.gedge.opgraph.app.components.canvas.GraphCanvasSelectionModel;
import ca.gedge.opgraph.nodes.general.MacroNode;

/**
 * 
 */
public class CommonNodesMenuProvider implements MenuProvider {
	@Override
	public void installItems(final GraphEditorModel model, PathAddressableMenu menu) {
		menu.addSeparator("graph");

		final JMenuItem create = menu.addMenuItem("graph/create macro", new CreateMacroCommand());
		final JMenuItem explode = menu.addMenuItem("graph/explode macro", new ExplodeMacroCommand());

		create.setEnabled(false);
		explode.setEnabled(false);

		model.getDocument().addPropertyChangeListener(GraphDocument.PROCESSING_CONTEXT, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if(e.getNewValue() == null) {
					final Collection<OpNode> selected = model.getCanvas().getSelectionModel().getSelectedNodes();

					boolean canExplode = false;
					if(selected.size() == 1)
						canExplode = (selected.iterator().next() instanceof MacroNode);

					create.setEnabled(selected.size() > 0);
					explode.setEnabled(canExplode);
				} else { 
					create.setEnabled(false);
					explode.setEnabled(false);
				}
			}
		});

		final GraphCanvasSelectionModel selectionModel = model.getCanvas().getSelectionModel();
		selectionModel.addSelectionListener(new GraphCanvasSelectionListener() {
			@Override
			public void nodeSelectionChanged(Collection<OpNode> old, Collection<OpNode> selected) {
				boolean canExplode = false;
				if(selected.size() == 1)
					canExplode = (selected.iterator().next() instanceof MacroNode);

				create.setEnabled(selected.size() > 0);
				explode.setEnabled(canExplode);
			}
		});
	}

	@Override
	public void installPopupItems(Object context, MouseEvent event, GraphEditorModel model, PathAddressableMenu menu) {
		// Nothing to do
	}
}
