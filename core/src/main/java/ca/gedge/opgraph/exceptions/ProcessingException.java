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
package ca.gedge.opgraph.exceptions;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;

/**
 * A general exception that can be thrown during the processing of an
 * {@link OpGraph}. The specifics that generated this exception
 * can be found in {@link #getCause()}.
 */
public class ProcessingException extends Exception {
	/**
	 * Constructs a processing exception with a given detail message.
	 * 
	 * @param message  the detail message
	 */
	public ProcessingException(String message) {
		super(message);
	}
	
	/**
	 * Constructs a processing exception with a given cause.
	 * 
	 * @param cause  the cause
	 */
	public ProcessingException(Throwable cause) {
		super("An error occured during the processing of a graph", cause);
	}
	
	/**
	 * Constructs a processing exception with a given detail message and cause.
	 * 
	 * @param message  the detail message
	 * @param cause  the cause
	 */
	public ProcessingException(String message, Throwable cause) {
		super(message, cause);
	}
}
