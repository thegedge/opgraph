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
package ca.gedge.opgraph.app.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.extensions.NodeSettings;

/**
 * A panel for displaying and editing settings for a node. Settings are
 * available whenever the node has the {@link NodeSettings} extension. 
 * 
 * TODO undoable edits for settings
 */
public class NodeSettingsPanel extends JPanel {
	/** The node currently being viewed */
	private OpNode node;
	
	/**
	 * Default constructor.
	 */
	public NodeSettingsPanel() {
		super(new BorderLayout());
		setNode(null);
		setBorder(new EmptyBorder(5, 5, 5, 5));
	}

	/**
	 * Gets the node this info panel is currently viewing.
	 * 
	 * @return the node
	 */
	public OpNode getNode() {
		return node;
	}

	/**
	 * Sets the node this panel is currently viewing.
	 * 
	 * @param node  the node to display
	 */
	public void setNode(OpNode node) {
		if(this.node != node || getComponentCount() == 0) {
			this.node = node;
			
			// Clear all current components and add in new ones
			removeAll();
			
			// Get the settings component
			Component settingsComp = null;
			if(node == null) {
				final JLabel label = new JLabel("No node selected", SwingConstants.CENTER);
				label.setFont(label.getFont().deriveFont(Font.ITALIC));
				settingsComp = label;
			} else {
				final NodeSettings settings = node.getExtension(NodeSettings.class);
				if(settings != null)
					settingsComp = settings.getComponent(GraphEditorModel.getActiveDocument());
				
				if(settingsComp == null) {
					final JLabel label = new JLabel("No settings available", SwingConstants.CENTER);
					label.setFont(label.getFont().deriveFont(Font.ITALIC));
					settingsComp = label;
				}
			}
			
			add(settingsComp, BorderLayout.CENTER);
			
			revalidate();
			repaint();
		}
	}
}
