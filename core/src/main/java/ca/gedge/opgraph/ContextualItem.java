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

/**
 * A descriptor for a input/output field in an {@link OpNode}.
 */
public interface ContextualItem {
	/**
	 * Gets the reference key for the field that this descriptor describes.
	 * 
	 * @return the reference key
	 */
	public abstract String getKey();
	
	/**
	 * Sets the reference key for the field that this descriptor describes.
	 * 
	 * @param key  the reference key
	 */
	public abstract void setKey(String key);

	/**
	 * Gets the description for the field that this descriptor describes.
	 * 
	 * @return the description
	 */
	public abstract String getDescription();
	
	/**
	 * Sets the description for the field that this descriptor describes.
	 * 
	 * @param description  the description
	 */
	public abstract void setDescription(String description);

}
