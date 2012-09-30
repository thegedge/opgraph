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

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.exceptions.ProcessingException;

/**
 * An {@link OpNode} that takes a value and outputs the same value.
 * Although this sounds useless, it is useful in cases where we need to
 * reuse the same value, in some manner, without creating a cycle.
 */
@OpNodeInfo(
	name="Pass-Through",
	description="A node that takes a value and outputs the same value. If no value is " +
	            "given as input, a null value is output.",
	category="General"
)
public class PassThroughNode extends OpNode {
	/** Input field for the value */
	public final static InputField INPUT = new InputField("input", "input value", true, true);
	
	/** Output field for the value*/
	public final static OutputField OUTPUT = new OutputField("output", "output value", true, Object.class);

	/**
	 * Default constructor.
	 */
	public PassThroughNode() {
		putField(INPUT);
		putField(OUTPUT);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		context.put(OUTPUT, context.get(INPUT));
	}
}
