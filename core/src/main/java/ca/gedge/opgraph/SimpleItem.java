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

import java.util.Collection;
import ca.gedge.opgraph.extensions.Extendable;
import ca.gedge.opgraph.extensions.ExtendableSupport;

/**
 * A {@link ContextualItem} with only a key and description.
 */
public class SimpleItem implements ContextualItem, Extendable {
	/** The key for this field */
	private String key;
	
	/** The description for this field */
	private String description;
	
	/**
	 * Constructs a field with a key and empty description.
	 * 
	 * @param key  the key
	 */
	public SimpleItem(String key) {
		this(key, "");
	}
	
	/**
	 * Constructs a field with a key and empty description.
	 * 
	 * @param key  the key
	 * @param description  the description
	 */
	public SimpleItem(String key, String description) {
		setKey(key);
		setDescription(description);
	}
	
	//
	// Overrides
	//
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		
		if(getClass() == obj.getClass())
			return key.equals( ((ContextualItem)obj).getKey() );
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	//
	// ContextualItem
	//
	
	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public void setKey(String key) {
		this.key = (key == null ? "" : key);
	}
	
	@Override
	public void setDescription(String description) {
		this.description = (description == null ? "" : description);
	}

	//
	// Extendable
	//
	
	private ExtendableSupport extendableSupport = new ExtendableSupport(SimpleItem.class);
	
	@Override
	public <T> T getExtension(Class<T> type) {
		return extendableSupport.getExtension(type);
	}
	
	@Override
	public Collection<Class<?>> getExtensionClasses() {
		return extendableSupport.getExtensionClasses();
	}
	
	@Override
	public <T> T putExtension(Class<T> type, T extension) {
		return extendableSupport.putExtension(type, extension);
	}
}
