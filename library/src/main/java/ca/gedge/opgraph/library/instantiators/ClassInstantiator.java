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
package ca.gedge.opgraph.library.instantiators;

/**
 * An instantiator that creates instances from a given class.
 * 
 * @param <T>  the type of class
 */
public class ClassInstantiator<T> implements Instantiator<T> {
	/** The class to use for instantiation */
	private Class<? extends T> clz;

	/**
	 * Constructs an instantiator that uses a specified class to create
	 * new instances.
	 * 
	 * @param clz  the class to use for instantiation.
	 */
	public ClassInstantiator(Class<? extends T> clz) {
		this.clz = clz;
	}

	@Override
	public T newInstance(Object... params) throws InstantiationException {
		try {
			return clz.newInstance();
		} catch(InstantiationException exc) {
			throw exc;
		} catch(IllegalAccessException exc) {
			throw new InstantiationException(exc.getLocalizedMessage());
		}
	}
}
