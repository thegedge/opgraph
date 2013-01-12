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
 * A node that outputs a constant value. 
 */
@OpNodeInfo(
	name="Constant Value",
	description="Outputs a constant value.",
	category="Data Generation"
)
public class ConstantValueNode extends OpNode {
	/** Output field for the constant value */
	public final OutputField VALUE_OUTPUT_FIELD = new OutputField("value", "Constant value", true, Object.class);

	/** The value of this node */
	private Object value;

	/**
	 * Constructs a constant value node with a <code>null</code> value.
	 */
	public ConstantValueNode() {
		this(null);
	}

	/**
	 * Constructs a constant value node with the specified value.
	 * 
	 * @param value  the constant value
	 */
	public ConstantValueNode(Object value) {
		putField(VALUE_OUTPUT_FIELD);
		this.value = value;
	}

	/**
	 * Sets the value in this node.
	 * 
	 * @param value  the value
	 */
	public void setValue(Object value) {
		this.value = value;
		VALUE_OUTPUT_FIELD.setOutputType(value == null ? Object.class : value.getClass());
	}

	//
	// Overrides
	//

	@Override
	public void operate(OpContext context) {
		context.put(VALUE_OUTPUT_FIELD, value);
	}
}
