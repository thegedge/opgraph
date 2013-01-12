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

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;

/**
 * An extension meant for any {@link OpNode} that contains a graph (e.g., macro nodes).
 */
public interface CompositeNode {
	/**
	 * Gets the graph contained within this node.
	 * 
	 * @return the graph that composes this node
	 */
	public abstract OpGraph getGraph();

	/**
	 * Sets the graph contained within this node.
	 * 
	 * @param graph  the graph that composes this node
	 */
	public abstract void setGraph(OpGraph graph);
}
