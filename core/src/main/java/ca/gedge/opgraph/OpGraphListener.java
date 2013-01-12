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
package ca.gedge.opgraph;

/**
 *
 */
public interface OpGraphListener {
	/**
	 * Called when a nodenode was added to a graph.
	 *  
	 * @param graph  the source graph to which the node was added
	 * @param node  the node that was added
	 */
	public abstract void nodeAdded(OpGraph graph, OpNode node);

	/**
	 * Called when a node was removed from a graph.
	 * 
	 * @param graph  the source graph from which the node was removed
	 * @param node  the node that was removed
	 */
	public abstract void nodeRemoved(OpGraph graph, OpNode node);

	/**
	 * Called when an link was added to a graph.
	 *  
	 * @param graph  the source graph to which the link was added
	 * @param link  the link that was added
	 */
	public abstract void linkAdded(OpGraph graph, OpLink link);

	/**
	 * Called when an link was removed from a graph.
	 * 
	 * @param graph  the source graph from which the link was removed
	 * @param link  the link that was removed
	 */
	public abstract void linkRemoved(OpGraph graph, OpLink link);
}
