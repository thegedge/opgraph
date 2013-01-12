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
 * An {@link Exception} thrown when an operation on {@link DirectedAcyclicGraph}
 * requires a {@link Vertex} to be contained in the graph, but is not.
 */
public class VertexNotFoundException extends Exception {
	/** The {@link Vertex} that wasn't in the DAG */
	private Vertex vertex;

	/**
	 * Construct exception with the given vertex that could not be found and
	 * a default detail message.
	 * 
	 * @param vertex  the vertex that could not be found
	 */
	public VertexNotFoundException(Vertex vertex) {
		this(vertex, "Vertex not found");
	}

	/**
	 * Construct exception with the given vertex that could not be found and
	 * a custom detail message.
	 * 
	 * @param vertex  the vertex that could not be found
	 * @param message the detail message
	 */
	public VertexNotFoundException(Vertex vertex, String message) {
		super(message);
		this.vertex = vertex;
	}

	/**
	 * Gets the {@link Vertex} that was not found in the {@link DirectedAcyclicGraph}.
	 * 
	 * @return the {@link Vertex}
	 */
	public Vertex getVertex() {
		return this.vertex;
	}
}
