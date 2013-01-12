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
package ca.gedge.opgraph.nodes.logic;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.exceptions.ProcessingException;

/**
 * An {@link OpNode} that computes the logical negation of its input.
 */
@OpNodeInfo(
	name="Logical NOT",
	description="Computes the logical negation of a boolean input.",
	category="Logic"
)
public class LogicalNotNode extends OpNode {
	/** Input field for one of the two boolean values */
	public final static InputField X_INPUT_FIELD = new InputField("x", "boolean input", false, true, Boolean.class);

	/** Output field for the logical NOT of the input */
	public final static OutputField RESULT_OUTPUT_FIELD =  new OutputField("result", "not x", true, Boolean.class);

	/**
	 * Default constructor.
	 */
	public LogicalNotNode() {
		putField(X_INPUT_FIELD);
		putField(RESULT_OUTPUT_FIELD);
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		boolean x = (Boolean)context.get(X_INPUT_FIELD);
		context.put(RESULT_OUTPUT_FIELD, !x);
	}
}
