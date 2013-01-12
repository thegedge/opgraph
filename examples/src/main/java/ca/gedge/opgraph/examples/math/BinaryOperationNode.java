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
package ca.gedge.opgraph.examples.math;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.exceptions.ProcessingException;

/**
 * A node which performs a binary mathematical operation.
 */
@OpNodeInfo(
	name="Binary Operation",
	description="Performs a given binary operation"
)
public class BinaryOperationNode extends OpNode {
	/**
	 * Binary operation types.
	 */
	public static enum Op {
		/** Addition */
		ADD,

		/** Subtraction */
		SUBTRACT,

		/** Multiplication */
		MULTIPLY,

		/** Division */
		DIVIDE
	}

	/** Input field for one of the input values */
	public final InputField X = new InputField("x", "input value", false, true, Number.class);

	/** Input field for one of the input values */
	public final InputField Y = new InputField("y", "input value", false, true, Number.class);

	/** Output field for the result */
	public final OutputField RESULT = new OutputField("result", "result", true, Number.class);

	/** The operation this node performs */
	private Op op;

	/**
	 * Constructs a binary operation node that performs a given operation.
	 * 
	 * @param op  the operation
	 */
	public BinaryOperationNode(Op op) {
		// Since we've annotated this class with NodeData, we do
		// not need to call super(name, description)

		this.op = op;

		// Always remember to add the fields this node provides
		putField(X);
		putField(Y);
		putField(RESULT);
	}

	//
	// OpNode
	//

	@Override
	public void operate(OpContext context) throws ProcessingException {
	    final double x = ((Number)context.get(X)).doubleValue();
	    final double y = ((Number)context.get(Y)).doubleValue();

	    switch(op) {
		case DIVIDE:
			context.put(RESULT, x / y);
			break;
		case MULTIPLY:
			context.put(RESULT, x * y);
			break;
		case SUBTRACT:
			context.put(RESULT, x - y);
			break;
		case ADD:
		default: // add by default, but an error would be preferred
			context.put(RESULT, x + y);
			break;
	    }
	}
}
