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

import static org.junit.Assert.*;

import org.junit.Test;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.nodes.logic.LogicalAndNode;

/**
 * Test nodes in {@link ca.gedge.opgraph.nodes.logic}.
 */
public class TestLogicNodes {
	final OpContext context = new OpContext();
	
	private void testUnary(OpNode node,
	                       InputField x, OutputField result,
	                       boolean xval, boolean expected)
	{
		testBinary(node, x, null, result, xval, true, expected);
	}
	
	private void testBinary(OpNode node,
	                        InputField x, InputField y, OutputField result,
	                        boolean xval, boolean yval, boolean expected)
	{
		context.clear();
		context.put(x, xval);
		
		if(y != null)
			context.put(y, yval);

		try {
			node.operate(context);
		} catch(ProcessingException exc) {
			fail(exc.getMessage());
		}
		
		assertTrue("result exists", context.containsKey(result));
		assertEquals(context.get(result), expected);
	}
	
	@Test
	public void testAnd() {
		final OpNode node = new LogicalAndNode();
		final InputField x = LogicalAndNode.X_INPUT_FIELD;
		final InputField y = LogicalAndNode.Y_INPUT_FIELD;
		final OutputField result = LogicalAndNode.RESULT_OUTPUT_FIELD;
		
		testBinary(node, x, y, result, true, true, true);
		testBinary(node, x, y, result, true, false, false);
		testBinary(node, x, y, result, false, true, false);
		testBinary(node, x, y, result, false, false, false);
	}
	
	@Test
	public void testOr() {
		final OpNode node = new LogicalOrNode();
		final InputField x = LogicalOrNode.X_INPUT_FIELD;
		final InputField y = LogicalOrNode.Y_INPUT_FIELD;
		final OutputField result = LogicalOrNode.RESULT_OUTPUT_FIELD;
		
		testBinary(node, x, y, result, true, true, true);
		testBinary(node, x, y, result, true, false, true);
		testBinary(node, x, y, result, false, true, true);
		testBinary(node, x, y, result, false, false, false);
	}
	
	@Test
	public void testXor() {
		final OpNode node = new LogicalXorNode();
		final InputField x = LogicalXorNode.X_INPUT_FIELD;
		final InputField y = LogicalXorNode.Y_INPUT_FIELD;
		final OutputField result = LogicalXorNode.RESULT_OUTPUT_FIELD;
		
		testBinary(node, x, y, result, true, true, false);
		testBinary(node, x, y, result, true, false, true);
		testBinary(node, x, y, result, false, true, true);
		testBinary(node, x, y, result, false, false, false);
	}
	
	@Test
	public void testNot() {
		final OpNode node = new LogicalNotNode();
		final InputField x = LogicalNotNode.X_INPUT_FIELD;
		final OutputField result = LogicalNotNode.RESULT_OUTPUT_FIELD;
		
		testUnary(node, x, result, true, false);
		testUnary(node, x, result, false, true);
	}
}
