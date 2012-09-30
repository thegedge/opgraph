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

import ca.gedge.opgraph.library.NodeData;

/**
 * A {@link NodeInfoFilter} that filters on {@link NodeData#name}.
 */
public class NodeInfoNameFilter extends NodeInfoFilter {
	@Override
	public boolean isAccepted(NodeData info) {
		if(filterPattern == null)
			return true;
		
		return (info != null && info.name != null && filterPattern.matcher(info.name).find());
	}
}
