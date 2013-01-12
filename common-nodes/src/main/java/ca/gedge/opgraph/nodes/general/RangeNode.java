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

import java.util.List;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;

/**
 * A node that outputs a constant value. 
 */
@OpNodeInfo(
	name="Range",
	description="Outputs a range of integers.",
	category="Data Generation"
)
public class RangeNode extends OpNode {
	/** Input field for the start of the output range */
	public final InputField START_INPUT_FIELD = new InputField("start", "Start value of range", false, true, Number.class);

	/** Input field for the end of the output range */
	public final InputField END_INPUT_FIELD = new InputField("end", "End value of range", false, true, Number.class);

	/** Output field for the range */
	public final OutputField RANGE_OUTPUT_FIELD = new OutputField("range", "Range list", true, List.class);

	/**
	 * Default constructor.
	 */
	public RangeNode() {
		putField(START_INPUT_FIELD);
		putField(END_INPUT_FIELD);
		putField(RANGE_OUTPUT_FIELD);
	}

	@Override
	public void operate(OpContext context) {
		final int start = ((Number)context.get(START_INPUT_FIELD)).intValue();
		final int end = ((Number)context.get(END_INPUT_FIELD)).intValue();
		context.put(RANGE_OUTPUT_FIELD, new IntRangeList(start, end));
	}
}
