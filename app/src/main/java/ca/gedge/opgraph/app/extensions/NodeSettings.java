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
package ca.gedge.opgraph.app.extensions;

import java.awt.Component;
import java.util.Properties;

import ca.gedge.opgraph.app.GraphDocument;

/**
 * A node extension that supplies a component for modifying settings
 * in a node.
 */
public interface NodeSettings {
	/**
	 * Gets a component for editing the node's settings.
	 * 
	 * @param document  the document which the component can use (e.g., for
	 *                  posting undoable edits)
	 * 
	 * @return the component
	 */
	public abstract Component getComponent(GraphDocument document);

	/**
	 * Gets the node settings as a properties object.
	 * 
	 * @return the properties
	 */
	public abstract Properties getSettings();

	/**
	 * Loads node settings from a properties object. Implementations should
	 * not assume certain keys exist.
	 * 
	 * @param properties  the properties to load settings from
	 */
	public abstract void loadSettings(Properties properties);
}
