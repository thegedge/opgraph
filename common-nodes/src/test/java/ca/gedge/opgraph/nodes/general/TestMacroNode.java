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

import static org.junit.Assert.*;

import org.junit.Test;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.nodes.general.ConstantValueNode;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.gedge.opgraph.nodes.general.PassThroughNode;
import ca.gedge.opgraph.nodes.logic.LogicalNotNode;

/**
 * Tests {@link MacroNode}.
 */
public class TestMacroNode {
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
	 * Constructs an operable graph that computes the minimum of two values.
	 * 
	 * @param inputs  an array for returning the two nodes for inputs
	 * @param outputs  an array for returning the single node containing the output
	 * 
	 * @return the graph
	 */
	private static OpGraph createMinDAG(PassThroughNode[] inputs, PassThroughNode[] outputs) {
		//
		// Constructs a dag that computes the minimum of two values, making
		// use of the ENABLED_FIELD feature of OpNode 
		//
		final OpGraph minDAG = new OpGraph();
		minDAG.setId("min");

		final PassThroughNode pt1_1 = new PassThroughNode();
		final PassThroughNode pt2_1 = new PassThroughNode();		
		final PassThroughNode pt1_2 = new PassThroughNode();
		final PassThroughNode pt2_2 = new PassThroughNode();
		final PassThroughNode ov1 = new PassThroughNode();

		final LessThanNode lv1 = new LessThanNode();
		final LogicalNotNode nv1 = new LogicalNotNode();

		// Return values
		inputs[0] = pt1_1;
		inputs[1] = pt2_1;
		outputs[0] = ov1;

		// Add nodes
		minDAG.add(pt1_1);
		minDAG.add(pt1_2);
		minDAG.add(pt2_1);
		minDAG.add(pt2_2);
		minDAG.add(lv1);
		minDAG.add(nv1);
		minDAG.add(ov1);

		// Add link
		assertNotNull(minDAG.connect(pt1_1, PassThroughNode.OUTPUT, lv1, LessThanNode.X_FIELD));
		assertNotNull(minDAG.connect(pt2_1, PassThroughNode.OUTPUT, lv1, LessThanNode.Y_FIELD));

		assertNotNull(minDAG.connect(pt1_1, PassThroughNode.OUTPUT, pt1_2, PassThroughNode.INPUT));
		assertNotNull(minDAG.connect(pt2_1, PassThroughNode.OUTPUT, pt2_2, PassThroughNode.INPUT));

		assertNotNull(minDAG.connect(lv1, LessThanNode.RESULT_FIELD, pt1_2, OpNode.ENABLED_FIELD));
		assertNotNull(minDAG.connect(lv1, LessThanNode.RESULT_FIELD, nv1, LogicalNotNode.X_INPUT_FIELD));
		assertNotNull(minDAG.connect(nv1, LogicalNotNode.RESULT_OUTPUT_FIELD, pt2_2, OpNode.ENABLED_FIELD));

		assertNotNull(minDAG.connect(pt1_2, PassThroughNode.OUTPUT, ov1, PassThroughNode.INPUT));
		assertNotNull(minDAG.connect(pt2_2, PassThroughNode.OUTPUT, ov1, PassThroughNode.INPUT));

		return minDAG;
	}

	/** Tests the correctness of a macro */
	@Test
	public void testMacro() {
		PassThroughNode [] inputs1 = new PassThroughNode[2];
		PassThroughNode [] outputs1 = new PassThroughNode[1];

		PassThroughNode [] inputs2 = new PassThroughNode[2];
		PassThroughNode [] outputs2 = new PassThroughNode[1];

		OpGraph dag = new OpGraph();
		OpGraph minDAG1 = createMinDAG(inputs1, outputs1);
		OpGraph minDAG2 = createMinDAG(inputs2, outputs2);

		ConstantValueNode cv1 = new ConstantValueNode(1.0);
		ConstantValueNode cv2 = new ConstantValueNode(2.0);
		ConstantValueNode cv3 = new ConstantValueNode(3.0);

		MacroNode min1 = new MacroNode(minDAG1);
		MacroNode min2 = new MacroNode(minDAG2);

		dag.add(cv1);
		dag.add(cv2);
		dag.add(cv3);
		dag.add(min1);
		dag.add(min2);

		// Publish inputs/outputs from macros
		InputField min1_in1 = min1.publish("x", inputs1[0], PassThroughNode.INPUT);
		InputField min1_in2 = min1.publish("y", inputs1[1], PassThroughNode.INPUT);
		OutputField min1_out1 = min1.publish("result", outputs1[0], PassThroughNode.OUTPUT);

		InputField min2_in1 = min2.publish("x", inputs2[0], PassThroughNode.INPUT);
		InputField min2_in2 = min2.publish("y", inputs2[1], PassThroughNode.INPUT);
		OutputField min2_out1 = min2.publish("result", outputs2[0], PassThroughNode.OUTPUT);

		try {			
			assertNotNull(dag.connect(cv1, cv1.VALUE_OUTPUT_FIELD, min1, min1_in1));
			assertNotNull(dag.connect(cv2, cv2.VALUE_OUTPUT_FIELD, min1, min1_in2));
			assertNotNull(dag.connect(min1, min1_out1, min2, min2_in1));
			assertNotNull(dag.connect(cv3, cv3.VALUE_OUTPUT_FIELD, min2, min2_in2));

			for(int i = 0; i < 5; ++i) {
				for(int j = 0; j < 5; ++j) {
					for(int k = 0; k < 5; ++k) {
						double minVal = Math.min(Math.min(i, j), k);
						cv1.setValue(1.0*i);
						cv2.setValue(1.0*j);
						cv3.setValue(1.0*k);

						double result = getResult(Double.class, dag, null, min2, min2_out1);
						assertEquals(minVal, result, 1e-10);
					}
				}
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
