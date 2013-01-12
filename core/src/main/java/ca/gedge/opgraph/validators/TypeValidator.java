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
 * An interface for classes who want to provide validation on objects.
 */
public interface TypeValidator {
	/**
	 * Gets whether or not this validator accepts a given object.
	 *  
	 * @param obj  the object to check
	 * 
	 * @return  <code>true</code> if this validator accepts the given object,
	 *          <code>false</code> otherwise
	 */
	public abstract boolean isAcceptable(Object obj);

	/**
	 * Gets whether or not this validator can potentially accept instances
	 * of a given class.
	 *  
	 * @param cls  the class to check
	 * 
	 * @return  <code>true</code> if this validator <em>map</em> accept the
	 *          given class, <code>false</code> otherwise
	 *          
	 * @throws NullPointerException  if the specified class is <code>null</code>
	 */
	public abstract boolean isAcceptable(Class<?> cls);
}
