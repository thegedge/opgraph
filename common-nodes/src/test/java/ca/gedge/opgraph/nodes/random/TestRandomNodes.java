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
package ca.gedge.opgraph.nodes.random;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.exceptions.ProcessingException;

/**
 * Test nodes in {@link ca.gedge.opgraph.nodes.random}.
 */
public class TestRandomNodes {
	@Test
	public void testRandomInteger() throws ProcessingException {
		final RandomIntegerNode node = new RandomIntegerNode();
		final OpContext context = new OpContext();
		
		node.operate(context);
		assertTrue("Output exists", context.containsKey(node.VALUE_OUTPUT));
		assertTrue("Output value is an integer", Integer.class.isInstance(context.get(node.VALUE_OUTPUT)));
		
		context.put(node.MIN_INPUT, 50);
		node.operate(context);
		assertTrue("Output exists", context.containsKey(node.VALUE_OUTPUT));
		assertTrue("Output value is an integer", Integer.class.isInstance(context.get(node.VALUE_OUTPUT)));
		assertTrue("Output greater than min", ((Integer)context.get(node.VALUE_OUTPUT)) >= 50);
		
		context.put(node.MAX_INPUT, 80);
		node.operate(context);
		assertTrue("Output exists", context.containsKey(node.VALUE_OUTPUT));
		assertTrue("Output value is an integer", Integer.class.isInstance(context.get(node.VALUE_OUTPUT)));
		
		final int value = (Integer)context.get(node.VALUE_OUTPUT);
		assertTrue("Output in range", (value >= 50) && (value <= 80));
	}
	
	@Test
	public void testRandomDecimal() throws ProcessingException {
		final RandomDecimalNode node = new RandomDecimalNode();
		final OpContext context = new OpContext();
		
		node.operate(context);
		assertTrue("Output exists", context.containsKey(node.VALUE_OUTPUT));
		assertTrue("Output value is a double", Double.class.isInstance(context.get(node.VALUE_OUTPUT)));
		
		context.put(node.MIN_INPUT, 50);
		node.operate(context);
		assertTrue("Output exists", context.containsKey(node.VALUE_OUTPUT));
		assertTrue("Output value is a double", Double.class.isInstance(context.get(node.VALUE_OUTPUT)));
		assertTrue("Output greater than min", ((Double)context.get(node.VALUE_OUTPUT)) >= 50);
		
		context.put(node.MAX_INPUT, 80);
		node.operate(context);
		assertTrue("Output exists", context.containsKey(node.VALUE_OUTPUT));
		assertTrue("Output value is a double", Double.class.isInstance(context.get(node.VALUE_OUTPUT)));
		
		final double value = (Double)context.get(node.VALUE_OUTPUT);
		assertTrue("Output in range", (value + 1e-10 >= 50) && (value <= 80 + 1e-10));
	}
	
	@Test
	public void testRandomBoolean() throws ProcessingException {
		final RandomBooleanNode node = new RandomBooleanNode();
		final OpContext context = new OpContext();
		
		node.operate(context);
		assertTrue("Output exists", context.containsKey(node.VALUE_OUTPUT));
		assertTrue("Output value is a boolean", Boolean.class.isInstance(context.get(node.VALUE_OUTPUT)));
	}
	
	@Test
	public void testRandomString() throws ProcessingException {
		final RandomStringNode node = new RandomStringNode();
		final OpContext context = new OpContext();
		
		for(int test = 0; test < 50; ++test) {
			final int LENGTH = (int)(Math.random() * 500) + 10;
			context.put(node.LENGTH_INPUT, LENGTH);
			node.operate(context);
			assertTrue("Output exists", context.containsKey(node.VALUE_OUTPUT));
			assertTrue("Output value is a string", String.class.isInstance(context.get(node.VALUE_OUTPUT)));
			assertEquals("Output value is correct length", LENGTH, context.get(node.VALUE_OUTPUT).toString().length());
		}
	}
}
