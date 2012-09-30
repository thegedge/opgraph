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
package ca.gedge.opgraph.app.util;

/**
 * A listener interface for collection-oriented events.
 * 
 * @param <P> the type of the collection element
 * @param <E> the type of element stored in the collection
 */
public interface CollectionListener<P, E> {
	/**
	 * Called when an element was added to a collection.
	 *  
	 * @param source  the source object to which this element was added
	 * @param element  the element that was added
	 */
	public abstract void elementAdded(P source, E element);
	
	/**
	 * Called when an element was removed from a collection.
	 * 
	 * @param source  the source object from which this element was removed
	 * @param element  the element that was removed
	 */
	public abstract void elementRemoved(P source, E element);
}
