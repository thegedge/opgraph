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

import java.util.Collection;
import java.util.TreeSet;

import org.junit.Test;

import ca.gedge.opgraph.OpContext;

/**
 * Test nodes in {@link ca.gedge.opgraph.nodes.general}.
 */
public class TestGeneralNodes {
	@Test
	public void testRange() {
		final RangeNode node = new RangeNode();
		final OpContext context = new OpContext();

		for(int test = 0; test < 10; ++test) {
			final int start = (int)((2*Math.random() - 1)*1000);
			final int end = start + (int)(Math.random()*50);

			final TreeSet<Integer> expected = new TreeSet<Integer>();
			for(int value = start; value <= end; ++value)
				expected.add(value);

			context.put(node.START_INPUT_FIELD, start);
			context.put(node.END_INPUT_FIELD, end);
			node.operate(context);

			assertTrue("Result exists", context.containsKey(node.RANGE_OUTPUT_FIELD));
			assertEquals(expected, new TreeSet<Object>((Collection<?>)context.get(node.RANGE_OUTPUT_FIELD)));
		}
	}
}
