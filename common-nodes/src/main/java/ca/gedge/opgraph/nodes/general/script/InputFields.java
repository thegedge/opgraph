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
import java.util.List;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.validators.ClassValidator;
import ca.gedge.opgraph.validators.CompositeValidator;
import ca.gedge.opgraph.validators.TypeValidator;

/**
 * Wrapper for an {@link ArrayList} of {@link InputField}s to simplify adding
 * input fields from a script.
 */
public class InputFields extends ArrayList<InputField> {
	/** The node which this class will add input fields to */
	private OpNode node;

	/**
	 * Constructs an input fields collection which adds input fields to a
	 * given node.
	 * 
	 * @param node  the node to add input fields to 
	 */
	public InputFields(OpNode node) {
		this.node = node;
	}

	/**
	 * Adds a non-optional, fixed input field with a key and
	 * description that accepts all types.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 */
	public void add(String key, String description) {
		add(key, description, false, true);
	}
	
	/**
	 * Adds an input field with a key, description, and optionality that
	 * accepts all types.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 * @param isOptional  whether or not this field is optional
	 * @param isFixed  whether or not this field is fixed
	 */
	public void add(
			String key,
			String description,
			boolean isOptional,
			boolean isFixed)
	{
		add(key, description, isOptional, isFixed, (TypeValidator)null);
	}
	
	/**
	 * Adds a non-optional, fixed input field with a key and description
	 * that accepts a specified list of classes.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 * @param acceptedTypes  the classes that are accepted
	 */
	public void add(
			String key,
			String description,
			Class<?>... acceptedTypes)
	{
		add(key, description, false, true, acceptedTypes);
	}
	
	/**
	 * Constructs a non-optional, fixed input field with a key, description
	 * and validator list.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 * @param validators  a {@link List} of {@link TypeValidator}s to use
	 *                    for validating incoming values
	 */
	public void add(
			String key,
			String description,
			TypeValidator... validators)
	{
		add(key, description, false, false, validators.length == 1 ? validators[0] : new CompositeValidator(validators));
	}

	/**
	 * Adds an input descriptor that accepts a specified list of classes.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 * @param isOptional  whether or not this field is optional
	 * @param isFixed  whether or not this field is fixed
	 * @param acceptedTypes  the classes that are accepted
	 */
	public void add(
			String key, 
			String description, 
			boolean isOptional, 
			boolean isFixed, 
			Class<?>... acceptedTypes)
	{
		add(key, description, isOptional, isFixed, new ClassValidator(acceptedTypes));
	}
	
	/**
	 * Adds an input descriptor with a specified list of validators.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 * @param isOptional  whether or not this field is optional
	 * @param isFixed  whether or not this field is fixed
	 * @param validators  a {@link List} of {@link TypeValidator}s to use
	 *                    for validating incoming values
	 */
	public void add(
			String key,
			String description,
			boolean isOptional,
			boolean isFixed,
			TypeValidator validators)
	{
		node.putField(new InputField(key, description, isOptional, isFixed, validators));
	}
}
