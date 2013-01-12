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
package ca.gedge.opgraph.app.commands.publish;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.extensions.Publishable;

/**
 * A menu that allows one to control the publishing of inputs and outputs
 * of a given node.
 */
public class PublishedFieldMenuHelper {
	/**
	 * Constructs a published field menu
	 * 
	 * @param menu  the menu to populate
	 * @param publishable  the {@link Publishable} node
	 * @param node  the node with fields to publish
	 * @param isInputs  <code>true</code> to show published inputs,
	 *                  <code>false</code> to show outputs
	 */
	public static void populate(JMenu menu, Publishable publishable, OpNode node, boolean isInputs) {
		final GraphDocument document = GraphEditorModel.getActiveDocument();
		if(document != null && document.getBreadcrumb().peekState(1) != null) {
			if(isInputs) {
				for(InputField field : node.getInputFields()) {
					final AbstractAction action = new PublishFieldCommand(publishable, node, field);
					final JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
					item.setSelected(publishable.getPublishedInput(node, field) != null);
					menu.add(item);
				}
			} else {
				for(OutputField field : node.getOutputFields()) {
					final AbstractAction action = new PublishFieldCommand(publishable, node, field);
					final JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
					item.setSelected(publishable.getPublishedOutput(node, field) != null);
					menu.add(item);
				}
			}
		}
	}
}
