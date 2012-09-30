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
 * An edge that only knows about its source and destination vertices.
 * 
 * @param <V>  the type of vertex used in the edge
 */
public class SimpleDirectedEdge<V> implements DirectedEdge<V> {
	/** The source vertex of this edge */
	protected final V source;
	
	/** The destination vertex of this edge */
	protected final V destination;
	
	/**
	 * Constructs an edge with a specified source/destination vertex.
	 * 
	 * @param source  source vertex
	 * @param destination  destination vertex
	 * 
	 * @throws NullPointerException  if either source/dest is <code>null</code> 
	 */
	public SimpleDirectedEdge(V source, V destination) {
		if(source == null || destination == null)
			throw new NullPointerException("source/destination cannot be null");
		
		this.source = source;
		this.destination = destination;
	}
	
	//
	// DirectedEdge
	//
	
	@Override
	public V getSource() {
		return source;
	}

	@Override
	public V getDestination() {
		return destination;
	}
	
	@Override
	public int compareTo(DirectedEdge<V> o) {
		if(o == null)
			return 1;
		return (equals(o) ? 0 : (new Integer(System.identityHashCode(this))).compareTo(System.identityHashCode(o)));
	}
}
