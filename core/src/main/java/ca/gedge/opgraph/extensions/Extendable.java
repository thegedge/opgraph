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
package ca.gedge.opgraph.extensions;

import java.util.Collection;

/**
 * An interface for any class which would like to provide extensions to
 * their functionality, even if the class is defined as <code>final</code>. 
 */
public interface Extendable {
	/**
	 * Gets the extension of a given type.
	 * 
	 * @param type  the type of extension to get
	 * 
	 * @return an instance of that extension type, or <code>null</code> if
	 *         the specified extension type is not supported by the class.
	 *         
	 * @throws NullPointerException  if <code>type</code> is <code>null</code>
	 */
	public abstract <T> T getExtension(Class<T> type);

	/**
	 * Gets an iterable copy of the extensions.
	 * 
	 * @return an iterable copy of the extensions
	 */
	public abstract Collection<Class<?>> getExtensionClasses();

	/**
	 * Adds or removes an extension of a specified type to an extendable.
	 * 
	 * @param type  the type of extension to add
	 * @param extension  the extension instance to add, or <code>null</code>
	 *                   if the extension of the specified type should be
	 *                   removed from this extendable
	 *                   
	 * @return the old extension instance of the specified type, or
	 *         <code>null</code> if one did not previously exist
	 *         
	 * @throws NullPointerException  if <code>type</code> is <code>null</code>
	 */
	public abstract <T> T putExtension(Class<T> type, T extension);
}
