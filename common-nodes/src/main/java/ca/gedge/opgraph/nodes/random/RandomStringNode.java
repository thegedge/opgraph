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

import java.math.BigInteger;
import java.security.SecureRandom;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.exceptions.ProcessingException;

/**
 * A node that outputs a random string.
 */
@OpNodeInfo(
	name = "Random String",
	description = "Outputs random strings",
	category="Data Generation"
)
public class RandomStringNode extends OpNode {
	/** Input field for the output string's length */
	public final InputField LENGTH_INPUT = new InputField("length", "string length", false, true, Number.class);

	/** Output field for the random string */
	public final OutputField VALUE_OUTPUT = new OutputField("value", "random string", true, String.class);

	/** Random number generator to use */
	private SecureRandom random = new SecureRandom();

	/** 
	 * Default constructor 
	 */
	public RandomStringNode() {
		putField(LENGTH_INPUT);
		putField(VALUE_OUTPUT);
	}

	//
	// OpNode
	//

	@Override
	public void operate(OpContext context) throws ProcessingException {
		final int length = ((Number)context.get(LENGTH_INPUT)).intValue();
		context.put(VALUE_OUTPUT, (new BigInteger(6*length, random)).toString(32).substring(0, length));
	}
}
