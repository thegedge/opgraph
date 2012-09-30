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
package ca.gedge.opgraph.nodes.math;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.util.Pair;

/**
 * Test nodes in {@link ca.gedge.opgraph.nodes.math}.
 */
public class TestMathNodes {
	@Test
	public void testMathExpression() throws ProcessingException {
		final MathExpressionNode node = new MathExpressionNode();
		final OpContext context = new OpContext();
		
		final double x = 5.00123124122;
		final double y = 10.98877841241;
		final double z = 37.2478231289379;
		
		context.put("x", x);
		context.put("y", y);
		context.put("z", z);
		
		final ArrayList<Pair<String, Double>> expressions = new ArrayList<Pair<String, Double>>();
		
		// Hide logging
		Logger.getLogger(MathExpressionNode.class.getName()).setLevel(Level.WARNING);
		
		// Some basic identities
		expressions.add(new Pair<String, Double>( "x + 0", x ));
		expressions.add(new Pair<String, Double>( "x - x", 0.0 ));
		expressions.add(new Pair<String, Double>( "x + -x", 0.0 ));
		expressions.add(new Pair<String, Double>( "-(-(x))", x ));
		expressions.add(new Pair<String, Double>( "((x))", x ));
		expressions.add(new Pair<String, Double>( "-x", -x ));
		expressions.add(new Pair<String, Double>( "x*1", x ));
		expressions.add(new Pair<String, Double>( "1*x", x ));
		expressions.add(new Pair<String, Double>( "x", x ));
		
		// Operations
		expressions.add(new Pair<String, Double>( "x+y", x + y ));
		expressions.add(new Pair<String, Double>( "x  - y", x - y ));
		expressions.add(new Pair<String, Double>( "x   *y", x * y ));
		expressions.add(new Pair<String, Double>( "   z%    y   ", z % y ));
		expressions.add(new Pair<String, Double>( "x +(y -   z)", x + (y - z) ));
		expressions.add(new Pair<String, Double>( "2*x+ -5*(-y + 5.102*z)", 2*x + -5*(-y + 5.102*z) ));
		expressions.add(new Pair<String, Double>( "1.0258*(x + 5*(y - 6124.293))", 1.0258*(x + 5*(y - 6124.293)) ));
		
		for(Pair<String, Double> expression : expressions) {
			node.setExpression(expression.getFirst());
			node.operate(context);
			
			assertTrue("Output exists", context.containsKey(node.RESULT_OUTPUT_FIELD));
			
			final double result = ((Number)context.get(node.RESULT_OUTPUT_FIELD)).doubleValue();
			assertEquals(expression.getFirst(), expression.getSecond(), result, 1e-10);
		}
		
		// Significant decimal places
		node.setExpression("x+y");
		{
			node.setSignificantDigits(5);
			node.operate(context);
			
			final double value = ((Number)context.get(node.RESULT_OUTPUT_FIELD)).doubleValue();
			final double expected = x + y;
			assertTrue("ensure within range of significance", Math.abs(value - expected) < 1e-5);
		}
		{
			node.setSignificantDigits(0);
			node.operate(context);
			
			final double value = ((Number)context.get(node.RESULT_OUTPUT_FIELD)).doubleValue();
			final double expected = x + y;
			assertTrue("loss of data expected", Math.abs(value - expected) < 1);
		}
	}
}
