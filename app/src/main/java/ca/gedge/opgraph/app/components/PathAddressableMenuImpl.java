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
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;

import ca.gedge.opgraph.util.Pair;

/**
 * An implementation of {@link PathAddressableMenu} that operates on a given
 * menu element.
 */
public class PathAddressableMenuImpl implements PathAddressableMenu {
	/** The root element to base paths off of */
	private MenuElement root;

	/**
	 * Constructs a path-adressable menu with a given menu element as its root.
	 * 
	 * @param root  the menu element to use as root
	 */
	public PathAddressableMenuImpl(MenuElement root) {
		this.root = root;
	}

	//
	// PathAddressableMenu
	//

	@Override
	public JMenu getMenu(String path) {
		final MenuElement elem = getMenuElement(path);
		return (elem != null && (elem instanceof JMenu) ? (JMenu)elem : null);
	}

	@Override
	public JMenuItem getMenuItem(String path) {
		final MenuElement elem = getMenuElement(path);
		return (elem != null && (elem instanceof JMenuItem) ? (JMenuItem)elem : null);
	}

	@Override
	public MenuElement getMenuElement(String path) {
		final Pair<String, MenuElement> deepest = getDeepestMenuElement(root, path);
		return ((deepest.getFirst().length() == path.length()) ? deepest.getSecond() : null);
	}

	@Override
	public JMenu addMenu(String path, String text) {
		final Pair<String, MenuElement> deepest = getDeepestMenuElement(root, path);
		final String name = path.substring(deepest.getFirst().length());

		JMenu ret = null;
		if(name.indexOf('/') == -1) {
			if(deepest.getSecond() instanceof JMenu) {
				ret = new JMenu(text);
				ret.setName(name);
				((JMenu)deepest.getSecond()).add(ret);
			} else if(deepest.getSecond() instanceof JPopupMenu) {
				ret = new JMenu(text);
				ret.setName(name);
				ret.setIcon(null);
				((JPopupMenu)deepest.getSecond()).add(ret);
			} else if(deepest.getSecond() instanceof JMenuBar) {
				ret = new JMenu(text);
				ret.setName(name);
				((JMenuBar)deepest.getSecond()).add(ret);
			}
		}
		return ret;
	}

	@Override
	public JMenuItem addMenuItem(String path, Action action) {
		final Pair<String, MenuElement> deepest = getDeepestMenuElement(root, path);
		final String name = path.substring(deepest.getFirst().length());

		JMenuItem ret = null;
		if(name.indexOf('/') == -1) {
			if(deepest.getSecond() instanceof JMenu) {
				ret = new JMenuItem(action);
				ret.setName(name);
				ret.setIcon(null);
				((JMenu)deepest.getSecond()).add(ret);
			} else if(deepest.getSecond() instanceof JPopupMenu) {
				ret = new JMenuItem(action);
				ret.setName(name);
				ret.setIcon(null);
				((JPopupMenu)deepest.getSecond()).add(ret);
			} else if(deepest.getSecond() instanceof JMenuBar) {
				ret = new JMenuItem(action);
				ret.setName(name);
				ret.setIcon(null);
				((JMenuBar)deepest.getSecond()).add(ret);
			}
		}
		return ret;
	}

	@Override
	public Component addComponent(String path, Component component) {
		final Pair<String, MenuElement> deepest = getDeepestMenuElement(root, path);
		final String name = path.substring(deepest.getFirst().length());

		if(name.indexOf('/') == -1) {
			component.setName(name);
			if(deepest.getSecond() instanceof JMenu) {
				((JMenu)deepest.getSecond()).add(component);
			} else if(deepest.getSecond() instanceof JPopupMenu) {
				((JPopupMenu)deepest.getSecond()).add(component);
			} else if(deepest.getSecond() instanceof JMenuBar) {
				((JMenuBar)deepest.getSecond()).add(component);
			}
		} else component = null;

		return component;
	}

	@Override
	public void addSeparator(String path) {
		final Pair<String, MenuElement> deepest = getDeepestMenuElement(root, path);
		if(deepest.getFirst().length() == path.length()) {
			if(deepest.getSecond().getSubElements().length > 0) {
				if(deepest.getSecond() instanceof JMenu) {
					((JMenu)deepest.getSecond()).addSeparator();
				} else if(deepest.getSecond() instanceof JPopupMenu) {
					((JPopupMenu)deepest.getSecond()).addSeparator();
				}
			}
		}
	}

	private Pair<String, MenuElement> getDeepestMenuElement(MenuElement elem, String path) {
		int position = 0;
		if(elem != null && path != null) {
			final String [] components = path.split("/");
			int index = 0;

			// Go as deep as we can go
			while(index < components.length) {
				final int oldIndex = index;
				for(MenuElement subelem : elem.getSubElements()) {
					if(components[index].equals(subelem.getComponent().getName())) {
						position += components[index].length() + 1;
						++index;
						elem = subelem;
						break;
					}
				}

				// If we didn't move, stop
				if(index == oldIndex)
					break;
			}

			if(index == components.length)
				--position;
		}

		return new Pair<String, MenuElement>(path.substring(0, position), elem);
	}
}
