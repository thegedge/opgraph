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
package ca.gedge.opgraph.dag;

/**
 * An edge in a {@link DirectedAcyclicGraph} instance.
 * 
 * @param <V> the type of {@link Vertex} this edge references
 */
public interface DirectedEdge<V> extends Comparable<DirectedEdge<V>> {
	/**
	 * Gets the vertex at the source end of this edge.
	 * 
	 * @return a {@link Vertex} reference.  
	 */
	public abstract V getSource();

	/**
	 * Gets the vertex at the destination end of this edge.
	 * 
	 * @return a {@link Vertex} reference.
	 */
	public abstract V getDestination();
}
