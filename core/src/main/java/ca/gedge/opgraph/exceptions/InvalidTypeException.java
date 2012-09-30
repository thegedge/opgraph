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

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpGraph;

/**
 * An exception that is thrown during the processing of an {@link OpGraph}
 * when a given input value isn't accepted by an {@link InputField}. 
 */
public final class InvalidTypeException extends ProcessingException {
	/** The field given bad input*/
	private InputField field;

	/** The given input */
	private Object value;
	
	/**
	 * Constructs an exception with the input field that did not accept
	 * the given object.
	 * 
	 * @param field  the input field descriptor
	 * @param value  the value that was not accepted by the given field
	 */
	public InvalidTypeException(InputField field, Object value) {
		super("Field '" + field.getKey() + "' doesn't accepte value '" + value + "'");
		this.field = field;
		this.value = value;
	}

	/**
	 * Gets the field that requires input, but received none.
	 * 
	 * @return  the input field descriptor
	 */
	public InputField getField() {
		return field;
	}
	
	/**
	 * Gets the value the input field was given
	 * 
	 * @return  the value
	 */
	public Object getValue() {
		return value;
	}
}
