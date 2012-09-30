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
package ca.gedge.opgraph.nodes.general;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;

/**
 * A node that fetches a value from the local context.
 */
@OpNodeInfo(
	name="Get Context Value",
	description="Gets a value from the local context."
)
public abstract class GetContextValueNode extends OpNode {
	/** The value output field for this node */
	private OutputField outputField;
	
	/** The key of the context value that will be fetched */
	private String key;
	
	/**
	 * Constructs a node that will grab a context value with a specified key.
	 * 
	 * @param key  the key of the item we'll be fetching
	 * @param valueType  the type of value this key references
	 */
	public GetContextValueNode(String key, Class<?> valueType) {
		this.key = key;
		putField(outputField = new OutputField("value", "context value", true, valueType));
	}
	
	//
	// Overrides
	//
	
	@Override
	public void operate(OpContext context) {
		context.put(outputField, context.get(key));
	}
}
