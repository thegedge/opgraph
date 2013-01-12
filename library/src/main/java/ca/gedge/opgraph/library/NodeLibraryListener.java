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
package ca.gedge.opgraph.library;

/**
 * An interface for people that want to listen to node library events.
 */
public interface NodeLibraryListener {
	/**
	 * Called whenever a node is first registered with a node library.
	 * 
	 * @param info  the info for the registered node
	 */
	public abstract void nodeRegistered(NodeData info);

	/**
	 * Called whenever a node is first registered with a node library.
	 * 
	 * @param info  the info for the unregistered node
	 */
	public abstract void nodeUnregistered(NodeData info);
}
