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
package ca.gedge.opgraph.validators;

/**
 * A validator that will check if an {@link Iterable} object contains objects
 * that adhere to a given {@link TypeValidator}.
 */
public class IterableValidator implements TypeValidator {
	/** The validator used for iterated elements */
	private TypeValidator elementValidator;

	/**
	 * Constructs this validator with an element validator which accepts
	 * the given list of classes.
	 * 
	 * @param acceptedTypes  the classes to accept for iterated elements
	 * 
	 * @see #IterableValidator(TypeValidator)
	 */
	public IterableValidator(Class<?>... acceptedTypes) {
		this(new ClassValidator(acceptedTypes));
	}

	/**
	 * Constructs this validator with the given validator to use against
	 * iterated elements.
	 * 
	 * @param elementValidator  the {@link TypeValidator} to use on iterated elements
	 */
	public IterableValidator(TypeValidator elementValidator) {
		this.elementValidator = elementValidator;
	}

	//
	// TypeValidator
	//

	@Override
	public boolean isAcceptable(Object obj) {
		boolean ret = true;
		if(obj instanceof Iterable) {
			for(Object o : (Iterable<?>)obj) {
				if(!elementValidator.isAcceptable(o)) {
					ret = false;
					break;
				}
			}
		}
		return ret;
	}

	@Override
	public boolean isAcceptable(Class<?> cls) {
		if(cls == null)
			throw new NullPointerException("cls cannot be null");
		return Iterable.class.isAssignableFrom(cls);
	}
}
