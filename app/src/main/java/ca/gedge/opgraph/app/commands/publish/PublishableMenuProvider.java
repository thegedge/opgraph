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
package ca.gedge.opgraph.app.commands.publish;

import java.awt.event.MouseEvent;
import javax.swing.JMenu;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.commands.publish.PublishedFieldMenuHelper;
import ca.gedge.opgraph.app.components.PathAddressableMenu;
import ca.gedge.opgraph.extensions.Publishable;

/**
 * Menu provider for publishing input/output fields.
 */
public class PublishableMenuProvider implements MenuProvider {
	@Override
	public void installItems(final GraphEditorModel model, PathAddressableMenu menu) {
		// Nothing to do, unless the publish menus should be available beyond
		// the popup level
	}

	@Override
	public void installPopupItems(Object context, MouseEvent event, GraphEditorModel model, PathAddressableMenu menu) {
		if(context != null && (context instanceof OpNode)) {
			final OpNode node = (OpNode)context;
			final Publishable publishable = model.getDocument().getGraph().getExtension(Publishable.class);

			if(publishable != null) {
				menu.addSeparator("");

				final JMenu inputs = menu.addMenu("published_inputs", "Publish Inputs");
				final JMenu outputs = menu.addMenu("published_outputs", "Publish Outputs");

				PublishedFieldMenuHelper.populate(inputs, publishable, node, true);
				PublishedFieldMenuHelper.populate(outputs, publishable, node, false);
			}
		}
	}
}
