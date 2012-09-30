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
 * creates a cycle.
 */
public class CycleDetectedException extends Exception {
	/**
	 * Constructs this exception with a default detail message.
	 */
	public CycleDetectedException() {
		this("A cycle was detected");
	}
	
	/**
	 * Constructs this exception with a given detail message.
	 * 
	 * @param message  the detail message
	 */
	public CycleDetectedException(String message) {
		super(message);
	}
}
