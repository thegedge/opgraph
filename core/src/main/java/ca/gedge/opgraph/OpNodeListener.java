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
package ca.gedge.opgraph;

/**
 * A listener for {@link OpNode}.
 */
public interface OpNodeListener {
	/**
	 * Called when a basic property is changed on the node.
	 * 
	 * @param propertyName  the name of the property
	 * @param oldValue  the old value of the property
	 * @param newValue  the new value of the property
	 */
	public abstract void nodePropertyChanged(String propertyName, Object oldValue, Object newValue);

	/**
	 * Called when an input field was added to a node.
	 *  
	 * @param node  the source node to which the input field was added
	 * @param field  the input field that was added
	 */
	public abstract void fieldAdded(OpNode node, InputField field);

	/**
	 * Called when an input field was removed from a node.
	 * 
	 * @param node  the source node from which the input field was removed
	 * @param field  the input field that was removed
	 */
	public abstract void fieldRemoved(OpNode node, InputField field);

	/**
	 * Called when an output field was added to a node.
	 *  
	 * @param node  the source node to which the output field was added
	 * @param field  the output field that was added
	 */
	public abstract void fieldAdded(OpNode node, OutputField field);

	/**
	 * Called when an output field was removed from a node.
	 * 
	 * @param node  the source node from which the output field was removed
	 * @param field  the output field that was removed
	 */
	public abstract void fieldRemoved(OpNode node, OutputField field);
}
