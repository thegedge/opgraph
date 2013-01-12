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

import java.util.ArrayList;

/**
 * Support for collection-oriented classes that provide the listening
 * capabilities of {@link CollectionListener}.
 * 
 * @param <P> the type of the collection element
 * @param <E> the type of element stored in the collection
 */
public class CollectionListenerSupport<P, E> {
	private ArrayList<CollectionListener<P, E>> listeners = new ArrayList<CollectionListener<P, E>>();

	/**
	 * Adds a collection listener.
	 * 
	 * @param listener  the listener
	 */
	public void addCollectionListener(CollectionListener<P, E> listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes a collection listener.
	 * 
	 * @param listener  the listener
	 */
	public void removeCollectionListener(CollectionListener<P, E> listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Fires an {@link CollectionListener#elementAdded(Object, Object)} event to all listeners.
	 * 
	 * @param source  the source collection
	 * @param element  the element that was added
	 */
	public void fireElementAdded(P source, E element) {
		synchronized(listeners) {
			for(CollectionListener<P, E> listener : listeners)
				listener.elementAdded(source ,element);
		}
	}

	/**
	 * Fires an {@link CollectionListener#elementRemoved(Object, Object)} event to all listeners.
	 * 
	 * @param source  the source collection
	 * @param element  the element that was removed
	 */
	public void fireElementRemoved(P source, E element) {
		synchronized(listeners) {
			for(CollectionListener<P, E> listener : listeners)
				listener.elementRemoved(source, element);
		}
	}
}
