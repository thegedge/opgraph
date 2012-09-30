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
package ca.gedge.opgraph.extensions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Support class for the {@link Extendable} interface.
 */
public class ExtendableSupport implements Extendable {
	/** The mapping of extension type to extension */
	private HashMap<Class<?>, Object> extensions;
	
	/** The parent class that this class supports */
	@SuppressWarnings("unused")
	private Class<?> parentClass;

	/**
	 * Constructs a support object that targets a given parent class.
	 * 
	 * @param parentClass  the class that is making use of this one
	 */
	public ExtendableSupport(Class<?> parentClass) {
		this.extensions = new LinkedHashMap<Class<?>, Object>();
		this.parentClass = parentClass;
	}
	
	//
	// Extendable
	//
	
	@Override
	public <T> T getExtension(Class<T> type) {
		if(type == null)
			throw new NullPointerException("Extension type cannot be null");
		
		return type.cast(extensions.get(type));
	}
	
	@Override
	public Collection<Class<?>> getExtensionClasses() {
		return Collections.unmodifiableCollection(extensions.keySet());
	}
	
	@Override
	public <T> T putExtension(Class<T> type, T extension) {
		if(type == null)
			throw new NullPointerException("Extension type cannot be null");
		return type.cast(extension == null ? extensions.remove(type) : extensions.put(type, extension));
	}
}
