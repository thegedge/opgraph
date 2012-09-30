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
package ca.gedge.opgraph.app.components.library;

import java.util.regex.Pattern;

import ca.gedge.opgraph.library.NodeData;

/**
 * A filter for {@link NodeData} instances.
 */
public abstract class NodeInfoFilter {
	/** The filter to use */
	protected String filter;

	/** The pattern for the filter */
	protected Pattern filterPattern;

	/**
	 * Sets the filter this renderer uses.
	 * 
	 * @param filter  the filter to set
	 */
	public void setFilter(String filter) {
		this.filter = filter;
		this.filterPattern = null;

		if(this.filter != null && this.filter.length() > 0)
			this.filterPattern = Pattern.compile(Pattern.quote(this.filter), Pattern.CASE_INSENSITIVE);
	}

	/**
	 * Gets whether or not this filter accepts a given {@link NodeData} instance.
	 * 
	 * @param info  the {@link NodeData} instance
	 * 
	 * @return <code>true</code> if this filter accepts the given instance,
	 *         <code>false</code> otherwise
	 */
	public abstract boolean isAccepted(NodeData info);
}
