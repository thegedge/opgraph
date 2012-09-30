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
package ca.gedge.opgraph.app.components;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

/**
 * A menu that provides path-based addressing, to make menu-building a simpler task.
 *
 * A menu path is a sequence of alphanumeric strings separated by forward slashes
 * (the '/' character). Examples:
 * <ul>
 *   <li><code>file/open recent</code></li>
 *   <li><code>edit/foo/bar/test</code></li>
 * </ul>
 */
public interface PathAddressableMenu {
	/**
	 * Gets the menu for a specified path.
	 * 
	 * @param path  the path
	 * 
	 * @return the menu associated with the given path, or <code>null</code>
	 *         if no menu exists for the given path
	 */
	public abstract JMenu getMenu(String path);
	
	/**
	 * Gets the menu item for a specified path.
	 * 
	 * @param path  the path
	 * 
	 * @return the menu item associated with the given path, or <code>null</code>
	 *         if no menu exists for the given path
	 */
	public abstract JMenuItem getMenuItem(String path);

	/**
	 * Gets the menu element for a specified path.
	 * 
	 * @param path  the path
	 * 
	 * @return the menu element associated with the given path, or <code>null</code>
	 *         if no menu exists for the given path
	 */
	public abstract MenuElement getMenuElement(String path);
	
	/**
	 * Adds a submenu to the menu at a specified path.
	 * 
	 * @param path  the path
	 * @param text  the text to use for the submenu 
	 * 
	 * @return the newly created submenu
	 */
	public abstract JMenu addMenu(String path, String text);
	
	/**
	 * Adds a menu item to the menu at a specified path.
	 * 
	 * @param path  the path
	 * @param action  the action to use for the menu item 
	 * 
	 * @return the newly created menu item
	 */
	public abstract JMenuItem addMenuItem(String path, Action action);
	
	/**
	 * Adds a component to the menu at a specified path.
	 * 
	 * @param path  the path
	 * @param component  the component to add 
	 * 
	 * @return the component
	 */
	public abstract Component addComponent(String path, Component component);
	
	/**
	 * Adds a separator to the menu at a specified path.
	 * 
	 * @param path  the path
	 */
	public abstract void addSeparator(String path);
}
