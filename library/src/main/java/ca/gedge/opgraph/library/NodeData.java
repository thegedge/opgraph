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
package ca.gedge.opgraph.library;

import java.net.URI;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.library.instantiators.Instantiator;

/**
 * Information about nodes registered with a node library.
 * 
 * @see NodeLibrary
 */
public class NodeData {
	/** The {@link URI} for this node descriptor */
	public final URI uri;
	
	/** The name of this node */
	public final String name;
	
	/** The description of this node */
	public final String description;
	
	/** The category of this node */
	public final String category;
	
	/** An instantiator for this node */
	public final Instantiator<? extends OpNode> instantiator;
	
	/**
	 * Constructs a node info.
	 * 
	 * @param uri  the URI for the node
	 * @param name  the name of the node
	 * @param description  the description of the node
	 * @param category  the category of the node
	 * @param instantiator  an instantiator for the node
	 */
	public NodeData(URI uri,
	                String name,
	                String description,
	                String category,
	                Instantiator<? extends OpNode> instantiator) 
	{
		this.uri = uri;
		this.name = name;
		this.description = description;
		this.category = category;
		this.instantiator = instantiator;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
