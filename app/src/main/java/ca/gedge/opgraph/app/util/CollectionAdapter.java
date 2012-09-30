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
 * An adapter class for {@link CollectionListener}. Extenders override only
 * the functions for which they want to handle.
 * 
 * @param <P> the type of the collection element
 * @param <E> the type of element stored in the collection
 */
public class CollectionAdapter<P, E> implements CollectionListener<P, E> {
	@Override
	public void elementAdded(P source, E element) {}

	@Override
	public void elementRemoved(P source, E element) {}
}
