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
package ca.gedge.opgraph;

import java.util.List;

import ca.gedge.opgraph.validators.ClassValidator;
import ca.gedge.opgraph.validators.CompositeValidator;
import ca.gedge.opgraph.validators.TypeValidator;

/**
 * A descriptor for an input field of an {@link OpNode} in an {@link OpGraph}.
 */
public class InputField extends SimpleItem {
	/** An instance of a type validator, or <code>null</code> if this field accepts anything */
	private TypeValidator validator;
	
	/** Whether or not this field is optional */
	private boolean optional;
	
	/** Whether or not this field is a fixed field. Fixed fields cannot be removed. */
	private boolean fixed;
	
	/**
	 * Constructs a non-optional, fixed input field with a key and
	 * description that accepts all types.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 */
	public InputField(String key, String description) {
		this(key, description, false, true);
	}
	
	/**
	 * Constructs an input field with a key, description, and optionality. The
	 * field that accepts all incoming types.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 * @param isOptional  whether or not this field is optional
	 * @param isFixed  whether or not this field is fixed
	 */
	public InputField(
			String key,
			String description,
			boolean isOptional,
			boolean isFixed)
	{
		this(key, description, isOptional, isFixed, (TypeValidator)null);
	}
	
	/**
	 * Constructs a non-optional, fixed input field with a key and description
	 * that accepts a specified list of classes.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 * @param acceptedTypes  the classes that are accepted
	 */
	public InputField(
			String key,
			String description,
			Class<?>... acceptedTypes)
	{
		this(key, description, false, true, acceptedTypes);
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
	public InputField(
			String key,
			String description,
			TypeValidator... validators)
	{
		this(key, description, false, false, validators);
	}

	/**
	 * Constructs an input descriptor that accepts a specified list of classes.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 * @param isOptional  whether or not this field is optional
	 * @param isFixed  whether or not this field is fixed
	 * @param acceptedTypes  the classes that are accepted
	 */
	public InputField(
			String key, 
			String description, 
			boolean isOptional, 
			boolean isFixed, 
			Class<?>... acceptedTypes)
	{
		this(key, description, isOptional, isFixed, new ClassValidator(acceptedTypes));
	}
	
	/**
	 * Constructs an input descriptor with a specified list of validators.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 * @param isOptional  whether or not this field is optional
	 * @param isFixed  whether or not this field is fixed
	 * @param validators  a {@link List} of {@link TypeValidator}s to use
	 *                    for validating incoming values
	 */
	public InputField(
			String key,
			String description,
			boolean isOptional,
			boolean isFixed,
			TypeValidator... validators)
	{
		super(key, description);
		this.optional = isOptional;
		this.fixed = isFixed;
		if(validators.length > 0)
			this.validator = (validators.length == 1 ? validators[0] : new CompositeValidator(validators));
	}
	
	/**
	 * Gets the type validator for this field.
	 * 
	 * @return the type validator. If <code>null</code>, this field will
	 *         accept any object. 
	 */
	public TypeValidator getValidator() {
		return validator;
	}
	
	/**
	 * Sets the type validator for this field.
	 * 
	 * @param validator  the type validator. If <code>null</code>, this field
	 *                   will accept any object.
	 */
	public void setValidator(TypeValidator validator) {
		this.validator = validator;
	}
	
	/**
	 * Gets whether or not this is a fixed field. Fixed fields cannot be removed.
	 * 
	 * @return <code>true</code> if a fixed field, <code>false</code> otherwise
	 */
	public boolean isFixed() {
		return fixed;
	}

	/**
	 * Sets whether or not this is a fixed field. Fixed fields cannot be removed.
	 * 
	 * @param fixed  <code>true</code> if a fixed field, <code>false</code> otherwise
	 */
	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	/**
	 * Gets whether or not this field is an optional input field.
	 * 
	 * @return <code>true</code> if this is an optional field, <code>false</code> otherwise
	 */
	public boolean isOptional() {
		return optional;
	}
	
	/**
	 * Sets whether or not this field is optional.
	 * 
	 * @param optional  <code>true</code> if an optional field, <code>false</code> otherwise
	 */
	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	
	//
	// Overrides
	//
	
	@Override
	public String toString() {
		return getKey();
	}
}
