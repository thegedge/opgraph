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
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;

/**
 * An exception that is thrown during the processing of an {@link OpGraph}
 * when a required input field is given no input.
 */
public final class RequiredInputException extends ProcessingException {
	/** The node from which the field comes from */
	private OpNode node;

	/** The field with required input */
	private InputField field;

	/**
	 * Constructs exception with the input field that required
	 * 
	 * @param node  the node containing the input field
	 * @param field  the input field descriptor
	 */
	public RequiredInputException(OpNode node, InputField field) {
		super("Required field '" + field.getKey() + "' in node '" + node.getId() + "' has no input");
		this.node = node;
		this.field = field;
	}

	/**
	 * Gets the node which contains the field that received no input.
	 * 
	 * @return  the node
	 */
	public OpNode getnode() {
		return node;
	}

	/**
	 * Gets the field that requires input, but received none.
	 * 
	 * @return  the input field descriptor
	 */
	public InputField getField() {
		return field;
	}
}
