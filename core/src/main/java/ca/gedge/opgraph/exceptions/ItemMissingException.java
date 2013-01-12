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
package ca.gedge.opgraph.exceptions;

import ca.gedge.opgraph.ContextualItem;

/**
 * An exception thrown whenever a given {@link ContextualItem} does not exist, but
 * is required in some context.
 */
public final class ItemMissingException extends Exception {
	/** The item that was missing */
	private ContextualItem item;

	/**
	 * Constructs exception with the given item that was missing.
	 * 
	 * @param item  the item that was missing
	 */
	public ItemMissingException(ContextualItem item) {
		this(item, "Contextual item with key '" + item.getKey() + "' missing");
	}

	/**
	 * Constructs exception with the given item that was missing, and a
	 * custom detail message for the exception.
	 * 
	 * @param item  the item that was missing
	 * @param message  the detail message
	 */
	public ItemMissingException(ContextualItem item, String message) {
		super(message);
		this.item = item;
	}

	/**
	 * Gets the {@link ContextualItem} that was missing.
	 * 
	 * @return  the contextual item
	 */
	public ContextualItem getItem() {
		return item;
	}
}
