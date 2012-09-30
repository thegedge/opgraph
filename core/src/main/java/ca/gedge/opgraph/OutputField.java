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
 * A descriptor for an output field of an {@link OpNode} in
 * an {@link OpGraph}.
 * 
 * TODO how about if output type is not known till runtime (e.g., PassThroughNode)?
 *      Perhaps use <code>null</code> to specify this?
 */
public class OutputField extends SimpleItem {
	/** If an output field, the type of object this field outputs. */
	private Class<?> outputType; // XXX Maybe create a type system that can alleviate type erasure
	
	/** Whether or not this field is a fixed field. Fixed fields cannot be removed. */
	private boolean fixed;
	
	/**
	 * Constructs an output descriptor with a key, output type, and description.
	 * 
	 * @param key  the reference key
	 * @param description  a description for the field
	 * @param isFixed  whether or not this field is fixed
	 * @param outputType  the type of object this field outputs 
	 */
	public OutputField(String key, String description, boolean isFixed, Class<?> outputType) {
		super(key, description);
		this.fixed = isFixed;
		this.outputType = outputType;
	}
	
	/**
	 * Gets the type of output for this field.
	 * 
	 * @return the type of output for this field
	 */
	public Class<?> getOutputType() {
		return outputType;
	}
	
	/**
	 * Sets the type of output for this field.
	 * 
	 * @param outputType  the type of output for this field
	 */
	public void setOutputType(Class<?> outputType) {
		this.outputType = outputType;
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
	
	@Override
	public String toString() {
		return getKey();
	}
}
