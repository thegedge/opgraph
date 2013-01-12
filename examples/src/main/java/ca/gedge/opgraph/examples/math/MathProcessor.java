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

import static ca.gedge.opgraph.examples.math.BinaryOperationNode.Op.*;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;

/**
 * An example program showing the construction and execution of an operable graph.
 * This example provides a binary operation class {@link BinaryOperationNode}
 * which is put together to perform the math operation (x + y) / (x*y + z)
 */
public class MathProcessor {
	/**
	 * Program entry point.
	 * 
	 * @param args  args[1] = x, args[2] = y, args[3] = z
	 */
	public static void main(String [] args) {
		if(args.length != 3) {
			System.out.println("usage: java MathProcessor x y z");
			System.exit(1);
		}

		// Parse input arguments
		final double x = Double.parseDouble(args[0]);
		final double y = Double.parseDouble(args[1]);
		final double z = Double.parseDouble(args[2]);

		// Construct the nodes
		final BinaryOperationNode xPlusY = new BinaryOperationNode(ADD);
		final BinaryOperationNode xTimesY = new BinaryOperationNode(MULTIPLY);
		final BinaryOperationNode addZ = new BinaryOperationNode(ADD);
		final BinaryOperationNode divide = new BinaryOperationNode(DIVIDE);

		// Construct the graph
		final OpGraph expression = new OpGraph();
		expression.add(xPlusY);
		expression.add(xTimesY);
		expression.add(addZ);
		expression.add(divide);

		// Connect nodes
		try {
			expression.add(new OpLink(xTimesY, xTimesY.RESULT, addZ, addZ.X));
			expression.add(new OpLink(xPlusY, xPlusY.RESULT, divide, divide.X));
			expression.add(new OpLink(addZ, addZ.RESULT, divide, divide.Y));
		} catch(CycleDetectedException exc) {
		} catch(VertexNotFoundException exc) {
		} catch(ItemMissingException exc) {}

		// Inject constant values into appropriate contexts
		final OpContext context = new OpContext();
		context.getChildContext(xPlusY).put(xPlusY.X, x);
		context.getChildContext(xPlusY).put(xPlusY.Y, y);
		context.getChildContext(xTimesY).put(xPlusY.X, x);
		context.getChildContext(xTimesY).put(xPlusY.Y, y);
		context.getChildContext(addZ).put(xPlusY.Y, z);

		// Execute 
		(new Processor(expression, context)).stepAll();

		// Output the result (note, we assume no errors during processing)
		final double result = ((Number)context.getChildContext(divide).get(divide.RESULT)).doubleValue();
		System.out.format("(x + y) / (x*y + z) = %.2f where {x, y, z} = {%.2f, %.2f, %.2f}", result, x, y, z);
	}
}
