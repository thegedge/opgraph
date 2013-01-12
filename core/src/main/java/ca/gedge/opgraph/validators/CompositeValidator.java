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
 * A validator which accepts any object in which at least one of its composed
 * validators accepts.
 */
public class CompositeValidator implements TypeValidator {
	/** The list of validators this validator uses */
	private TypeValidator[] validators;

	/**
	 * Constructs a composite validator from the given validators. Note
	 * that a <code>null</code> validator accepts all objects, so if such
	 * a validator is contained in <code>validators</code> then this
	 * validator will always return <code>true</code> from {@link #isAcceptable(Object)}.
	 * Also, if no validators are given, this validator will always return
	 * <code>false</code> from {@link #isAcceptable(Object)}. 
	 * 
	 * @param validators  {@link TypeValidator}s to use
	 */
	public CompositeValidator(TypeValidator... validators) {
		this.validators = validators;
	}

	//
	// TypeValidator
	//

	@Override
	public boolean isAcceptable(Object obj) {
		boolean ret = false;
		for(TypeValidator validator : validators) {
			if(validator == null || validator.isAcceptable(obj)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	@Override
	public boolean isAcceptable(Class<?> cls) {
		if(cls == null)
			throw new NullPointerException("cls cannot be null");

		boolean ret = false;
		for(TypeValidator validator : validators) {
			if(validator == null || validator.isAcceptable(cls)) {
				ret = true;
				break;
			}
		}
		return ret;
	}
}
