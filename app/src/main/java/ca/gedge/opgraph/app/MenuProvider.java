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
package ca.gedge.opgraph.app;

import java.awt.event.MouseEvent;

import ca.gedge.opgraph.app.components.PathAddressableMenu;

/**
 * An interface for menu providers.    
 */
public interface MenuProvider {
	/**
	 * Installs the items associated with this provider to the orimary menu
	 * of the requesting GUI.
	 * 
	 * @param model  an application model that menu items can act upon
	 * @param menu  the menu to install things to
	 */
	public abstract void installItems(GraphEditorModel model, PathAddressableMenu menu);

	/**
	 * Installs the items associated with this provider to a popup menu.
	 * An object is given for context, and the provider should determine if
	 * it needs to install any items on that menu based on the context object. 
	 * 
	 * @param context  the object used as context
	 * @param event  the mouse event that created the popup
	 * @param model  an application model that menu items can act upon
	 * @param menu  the menu to install things to
	 */
	public abstract void installPopupItems(Object context, MouseEvent event, GraphEditorModel model, PathAddressableMenu menu);
}
