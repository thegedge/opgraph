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
package ca.gedge.opgraph.nodes.general.script;

import java.util.ArrayList;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;

/**
 * Wrapper for an {@link ArrayList} of {@link OutputField}s to simplify adding
 * output fields from a script.
 */
public class OutputFields extends ArrayList<OutputField> {
	/** The node which this class will add output fields to */
	private OpNode node;

	/**
	 * Constructs an output fields collection which adds output fields to a
	 * given node.
	 * 
	 * @param node  the node to add input fields to 
	 */
	public OutputFields(OpNode node) {
		this.node = node;
	}

	/**
	 * Adds an output descriptor with a key, output type, and description.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 * @param isFixed  whether or not this field is fixed
	 * @param outputType  the type of object this field outputs 
	 */
	public void add(String key, String description, boolean isFixed, Class<?> outputType) {
		node.putField(new OutputField(key, description, isFixed, outputType));
	}
}
