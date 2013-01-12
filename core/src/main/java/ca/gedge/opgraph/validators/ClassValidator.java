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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A validator that uses {@link Class} instances for validation.
 */
public class ClassValidator implements TypeValidator {
	/** The accepted classes */
	private List<Class<?>> classes;

	/**
	 * Constructs a validator that accepts the given classes.
	 * 
	 * @param classes  the classes which will be accepted by this validator
	 */
	public ClassValidator(Class<?>... classes) {
		this.classes = new ArrayList<Class<?>>();
		for(Class<?> clz : classes) {
			if(clz != null)
				this.classes.add(clz);
		}
	}

	/**
	 * Gets the list of classes which this validator accepts.
	 * 
	 * @return  the list of accepted classes (immutable)
	 */
	public List<Class<?>> getClasses() {
		return Collections.unmodifiableList(classes);
	}

	//
	// TypeValidator
	//

	@Override
	public boolean isAcceptable(Object obj) {
		if(obj == null) return true;
		return isAcceptable(obj.getClass());
	}

	@Override
	public boolean isAcceptable(Class<?> cls) {
		if(cls == null)
			throw new NullPointerException("cls cannot be null");

		boolean ret = false;
		for(Class<?> acceptedClass : classes) {
			if(acceptedClass.isAssignableFrom(cls)) {
				ret = true;
				break;
			}
		}

		return ret;
	}
}
