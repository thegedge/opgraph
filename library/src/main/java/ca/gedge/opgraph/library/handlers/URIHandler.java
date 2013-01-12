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
package ca.gedge.opgraph.library.handlers;

import java.io.IOException;
import java.net.URI;

/**
 * An interface for classes that want to load information from a given URI.
 * 
 * @param <T>  the type of information that this handler loads
 * 
 * @see URI
 */
public interface URIHandler<T> {
	/**
	 * Checks whether or not this handler can load data from a given uri.
	 * 
	 * @param uri  the uri to check
	 * 
	 * @return <code>true</code> if this handler can handle the given uri,
	 *         <code>false</code> otherwise
	 */
	public abstract boolean handlesURI(URI uri);

	/**
	 * Loads node information from a given {@link URI}.
	 * 
	 * @param uri  the {@link URI} to load NodeData from
	 * 
	 * @return the node info
	 * 
	 * @throws IllegalArgumentException  if the scheme of the specified uri is unsupported
	 * @throws IOException  if there were any errors loading data from the specified uri
	 */
	public abstract T load(URI uri) throws IOException; 
}
