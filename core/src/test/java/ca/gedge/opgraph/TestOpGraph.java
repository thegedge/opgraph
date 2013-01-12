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

import static org.junit.Assert.*;

import org.junit.Test;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.exceptions.RequiredInputException;

/**
 * Tests {@link OpGraph}.
 */
public class TestOpGraph {
	/**
	 * Test node that outputs a constant [double] value.
	 */
	static class ConstantNode extends OpNode {
		private final double value;
		public final static OutputField VALUE_FIELD = new OutputField("value", "", true, Double.class);

		public ConstantNode(double value) {
			putField(VALUE_FIELD);
			this.value = value;
		}

		@Override
		public void operate(OpContext context) {
			context.put(VALUE_FIELD, value);
		}
	}

	/**
	 * Test node that adds its two input values.
	 */
	static class AddNode extends OpNode {
		public final static InputField X_FIELD = new InputField("x", "", false, true, Double.class);
		public final static InputField Y_FIELD = new InputField("y", "", false, true, Double.class);
		public final static OutputField RESULT_FIELD = new OutputField("result", "", true, Double.class);

		public AddNode() {
			super("Add", "Computes x + y");
			putField(X_FIELD);
			putField(Y_FIELD);
			putField(RESULT_FIELD);
		}

		@Override
		public void operate(OpContext context) {
			double x = (Double)context.get(X_FIELD);
			double y = (Double)context.get(Y_FIELD);
			context.put(RESULT_FIELD, x + y);
		}
	}

	/**
	 * Test node that multiplies its two input values.
	 */
	static class MultiplyNode extends OpNode {
		public final static InputField X_FIELD = new InputField("x", "", false, true, Double.class);
		public final static InputField Y_FIELD = new InputField("y", "", true, true, Double.class);
		public final static OutputField RESULT_FIELD = new OutputField("result", "", true, Double.class);

		public MultiplyNode() {
			super("Multiply", "Computes x*y");
			putField(X_FIELD);
			putField(Y_FIELD);
			putField(RESULT_FIELD);
		}

		@Override
		public void operate(OpContext context) {
			double x = (Double)context.get(X_FIELD);
			double y = 1.0;
			if(context.containsKey(Y_FIELD))
				y = (Double)context.get(Y_FIELD);

			context.put(RESULT_FIELD, x * y);
		}
	}

	/**
	 * Test node that compares its two inputs values and outputs if input
	 * X is less than input Y.
	 */
	static class LessThanNode extends OpNode {
		public final static InputField X_FIELD = new InputField("x", "", false, true, Double.class);
		public final static InputField Y_FIELD = new InputField("y", "", false, true, Double.class);
		public final static OutputField RESULT_FIELD = new OutputField("result", "", true, Boolean.class);

		public LessThanNode() {
			super("Less Than", "Computes x < y");
			putField(X_FIELD);
			putField(Y_FIELD);
			putField(RESULT_FIELD);
		}

		@Override
		public void operate(OpContext context) {
			double x = (Double)context.get(X_FIELD);
			double y = (Double)context.get(Y_FIELD);
			context.put(RESULT_FIELD, x < y);
		}
	}

	/**
	 * Test node that outputs the logical not of its boolean input.
	 */
	static class LogicalNotNode extends OpNode {
		public final static InputField X_INPUT_FIELD = new InputField("x", "boolean input", false, true, Boolean.class);
		public final static OutputField RESULT_OUTPUT_FIELD =  new OutputField("result", "not x", true, Boolean.class);

		public LogicalNotNode() {
			super("Logical Not", "Computes NOT x");
			putField(X_INPUT_FIELD);
			putField(RESULT_OUTPUT_FIELD);
		}

		@Override
		public void operate(OpContext context) throws ProcessingException {
			boolean x = (Boolean)context.get(X_INPUT_FIELD);
			context.put(RESULT_OUTPUT_FIELD, !x);
		}
	}

	/**
	 * Processes an operable graph.
	 * 
	 * @param graph  the graph to process
	 * @param context  the operating context, or <code>null</code> to use a default one
	 * 
	 * @return  the operating context used for processing
	 * 
	 * @throws ProcessingException  if any errors occurred during processing
	 */
	public static OpContext process(OpGraph graph, OpContext context)
		throws ProcessingException
	{
		final Processor processor = new Processor(graph);
		processor.reset(context);
		processor.stepAll();
		if(processor.getError() != null)
			throw processor.getError();

		return processor.getContext();
	}

	/**
	 * Gets a result from the execution of a graph.
	 * 
	 * @param cls  the type of result
	 * @param graph  the graph to execute
	 * @param context  the operating context, or <code>null</code> to use a default one
	 * @param resultNode  the node containing the result
	 * @param resultField  the field containing the result
	 * 
	 * @return the result
	 * 
	 * @throws ProcessingException  if any errors occurred during processing
	 */
	public static <T> T getResult(Class<T> cls,
	                              OpGraph graph,
	                              OpContext context,
	                              OpNode resultNode,
	                              OutputField resultField)
		throws ProcessingException
	{
		return cls.cast(process(graph, context).findChildContext(resultNode).get(resultField));
	}

	/**
	 * Tests basic operation of operable graph
	 */
	@Test
	public void testBasicOperation() {
		final OpGraph dag = new OpGraph();
		final ConstantNode cv1 = new ConstantNode(1.0);
		final ConstantNode cv2 = new ConstantNode(2.0);
		final ConstantNode cv10 = new ConstantNode(10.0);
		final AddNode av1 = new AddNode();
		final MultiplyNode mv1 = new MultiplyNode();

		// Add nodes
		dag.add(cv1);
		dag.add(cv2);
		dag.add(cv10);
		dag.add(av1);
		dag.add(mv1);

		// Add links to create the expression (1 + 2)*10
		assertNotNull(dag.connect(cv1, ConstantNode.VALUE_FIELD, av1, AddNode.X_FIELD));
		assertNotNull(dag.connect(cv2, ConstantNode.VALUE_FIELD, av1, AddNode.Y_FIELD));
		assertNotNull(dag.connect(av1, AddNode.RESULT_FIELD, mv1, MultiplyNode.X_FIELD));
		assertNotNull(dag.connect(cv10, ConstantNode.VALUE_FIELD, mv1, MultiplyNode.Y_FIELD));

		try {
			double result = getResult(Double.class, dag, null, mv1, MultiplyNode.RESULT_FIELD);
			assertEquals(30.0, result, 1e-10);
		} catch(ProcessingException exc) {
			if(exc.getCause() != null)
				exc.getCause().printStackTrace();
			else
				exc.printStackTrace();

			fail("Should be no errors when processing");
		}
	}

	/**
	 * Tests optional and required inputs
	 */
	@Test(expected=RequiredInputException.class)
	public void testOptionalInput() throws RequiredInputException {
		final OpGraph dag = new OpGraph();
		final ConstantNode cv1 = new ConstantNode(1.0);
		final ConstantNode cv2 = new ConstantNode(2.0);
		final ConstantNode cv10 = new ConstantNode(10.0);
		final AddNode av1 = new AddNode();
		final MultiplyNode mv1 = new MultiplyNode();

		// Add nodes
		dag.add(cv1);
		dag.add(cv2);
		dag.add(cv10);
		dag.add(av1);
		dag.add(mv1);

		// Add links to create expression (1 + 2)*10
		final OpLink requiredLink = dag.connect(av1, AddNode.RESULT_FIELD, mv1, MultiplyNode.X_FIELD);
		assertNotNull(requiredLink);

		final OpLink optionalLink = dag.connect(cv10, ConstantNode.VALUE_FIELD, mv1, MultiplyNode.Y_FIELD);
		assertNotNull(optionalLink);

		assertNotNull(dag.connect(cv1, ConstantNode.VALUE_FIELD, av1, AddNode.X_FIELD));
		assertNotNull(dag.connect(cv2, ConstantNode.VALUE_FIELD, av1, AddNode.Y_FIELD));

		// Everything should be good here
		try {
			double result = getResult(Double.class, dag, null, mv1, MultiplyNode.RESULT_FIELD);
			assertEquals(30.0, result, 1e-10);
		} catch(ProcessingException exc) {
			if(exc.getCause() != null)
				exc.getCause().printStackTrace();
			else
				exc.printStackTrace();

			fail("Should be no errors when processing");
		}

		// Now remove an link on an optional input field
		dag.remove(optionalLink);
		try {
			assertFalse(dag.contains(optionalLink));
			double result = getResult(Double.class, dag, null, mv1, MultiplyNode.RESULT_FIELD);
			assertEquals(3.0, result, 1e-10);
		} catch(ProcessingException exc) {
			if(exc.getCause() != null)
				exc.getCause().printStackTrace();
			else
				exc.printStackTrace();

			fail("Should be no errors when processing");
		}

		// Now remove an link on a required input field
		dag.remove(requiredLink);
		try {
			assertFalse(dag.contains(requiredLink));
			getResult(Double.class, dag, null, mv1, MultiplyNode.RESULT_FIELD);
		} catch(RequiredInputException exc) {
			throw exc;
		} catch(ProcessingException exc) {
			if(exc.getCause() != null)
				exc.getCause().printStackTrace();
			else
				exc.printStackTrace();

			fail("Should be no errors when processing");
		}
	}

	/**
	 * Tests data injection through a default context 
	 */
	@Test
	public void testContextInjection() {
		final OpGraph dag = new OpGraph();
		final AddNode av1 = new AddNode();
		final MultiplyNode mv1 = new MultiplyNode();

		// Add nodes
		dag.add(av1);
		dag.add(mv1);

		// Default context
		final OpContext defaults = new OpContext();
		defaults.getChildContext(av1).put(AddNode.X_FIELD, 1.0);
		defaults.getChildContext(av1).put(AddNode.Y_FIELD, 2.0);
		defaults.getChildContext(mv1).put(MultiplyNode.Y_FIELD, 10.0);

		// Add links to create the expression (1 + 2)*10
		assertNotNull(dag.connect(av1, AddNode.RESULT_FIELD, mv1, MultiplyNode.X_FIELD));

		// Everything should be good here
		try {
			double result = getResult(Double.class, dag, defaults, mv1, MultiplyNode.RESULT_FIELD);
			assertEquals(30.0, result, 1e-10);
		} catch(ProcessingException exc) {
			if(exc.getCause() != null)
				exc.getCause().printStackTrace();
			else
				exc.printStackTrace();

			fail("Should be no errors when processing");
		}
	}

	/**
	 * Tests the &quot;enabled&quot; field of nodes. 
	 */
	@Test
	public void testEnabled() {
		final OpGraph dag = new OpGraph();
		final AddNode av1 = new AddNode();
		final MultiplyNode mv1 = new MultiplyNode();

		// Add nodes
		dag.add(av1);
		dag.add(mv1);

		// Add links to create the expression (1 + 2)*10
		assertNotNull(dag.connect(av1, AddNode.RESULT_FIELD, mv1, MultiplyNode.X_FIELD));

		try {
			// First have everything enabled
			{
				final OpContext defaults = new OpContext();
				defaults.getChildContext(av1).put(AddNode.X_FIELD, 1.0);
				defaults.getChildContext(av1).put(AddNode.Y_FIELD, 2.0);
				defaults.getChildContext(mv1).put(MultiplyNode.Y_FIELD, 10.0);

				final OpContext context = process(dag, defaults);
				assertTrue("Multiply result not available when it should be",
				           context.getChildContext(mv1).containsKey(MultiplyNode.RESULT_FIELD));
			}

			// Now disable multiply node
			{
				final OpContext defaults = new OpContext();
				defaults.getChildContext(av1).put(AddNode.X_FIELD, 1.0);
				defaults.getChildContext(av1).put(AddNode.Y_FIELD, 2.0);
				defaults.getChildContext(mv1).put(MultiplyNode.Y_FIELD, 10.0);
				defaults.getChildContext(mv1).put(OpNode.ENABLED_FIELD, false);

				final OpContext context = process(dag, defaults);
				assertFalse("Multiply result available when it should not be",
				           context.getChildContext(mv1).containsKey(MultiplyNode.RESULT_FIELD));
			}
		} catch(ProcessingException exc) {
			if(exc.getCause() != null)
				exc.getCause().printStackTrace();
			else
				exc.printStackTrace();

			fail("Should be no errors when processing");
		}
	}
}
