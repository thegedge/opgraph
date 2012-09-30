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
package ca.gedge.opgraph.nodes.general;

import java.util.AbstractList;

/**
 * An integer range, as a list.
 */
class IntRangeList extends AbstractList<Integer> {
	/** Start of the range, inclusive */
	private int start;

	/** End of the range, inclusive */
	private int end;

	/**
	 * Constructs an integer range.
	 * 
	 * @param start  start value of the range, inclusive
	 * @param end  end value of the range, inclusive
	 */
	public IntRangeList(int start, int end) {
		if(start <= end) {
			this.start = start;
			this.end = end;
		} else {
			this.start = end;
			this.end = start;
		}
	}

	//
	// AbstractList<Integer>
	//

	@Override
	public Integer get(int index) {
		return (start + index);
	}

	@Override
	public int size() {
		return (end - start + 1);
	}
}
