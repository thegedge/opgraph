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

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.exceptions.ProcessingException;

/**
 * A node that outputs a random number.
 */
@OpNodeInfo(
	name = "Random Decimal",
	description = "Outputs random decimal numbers",
	category="Data Generation"
)
public class RandomDecimalNode extends OpNode {
	/** Input field for the minimum possible value of the random decimal */
	public final InputField MIN_INPUT = new InputField("min", "smallest possible value (inclusive)", true, true, Number.class);

	/** Input field for the maximum possible value of the random decimal */
	public final InputField MAX_INPUT = new InputField("max", "largest possible value (inclusive)", true, true, Number.class);

	/** Output field for the random decimal */
	public final OutputField VALUE_OUTPUT = new OutputField("value", "random decimal number", true, Double.class);

	/**
	 * Default constructor.
	 */
	public RandomDecimalNode() {
		putField(MIN_INPUT);
		putField(MAX_INPUT);
		putField(VALUE_OUTPUT);
	}

	//
	// OpNode
	//

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final double MIN = (context.containsKey(MIN_INPUT) ? ((Number)context.get(MIN_INPUT)).doubleValue() : -Double.MAX_VALUE);
		final double MAX = (context.containsKey(MAX_INPUT) ? ((Number)context.get(MAX_INPUT)).doubleValue() : Double.MAX_VALUE);
		final double t = Math.random();
		final double value = (1 - t)*MIN + t*MAX;
		context.put(VALUE_OUTPUT, value);
	}
}
