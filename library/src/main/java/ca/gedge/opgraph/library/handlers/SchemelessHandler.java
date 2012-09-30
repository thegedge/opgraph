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
package ca.gedge.opgraph.library.handlers;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.gedge.opgraph.library.NodeData;

/**
 * A handler that handles URIs with everything <code>null</code> except for
 * the fragment part. This is achieved by storing a fixed set of node info
 * instances.
 * 
 * @see URI
 */
public class SchemelessHandler implements URIHandler<List<NodeData>> {
	/** The fixed set of items */
	private HashMap<String, NodeData> items = new HashMap<String, NodeData>();

	/**
	 * Associate the fragment component of a given info's URI with the info.
	 * 
	 * @param info  the info
	 * 
	 * @throws NullPointerException  if the given info instance is <code>null</code>
	 * @throws NullPointerException  if the given info's uri has a <code>null</code> fragment
	 */
	public void put(NodeData info) {
		if(info == null)
			throw new NullPointerException("Given info cannot be null");
		
		if(info.uri == null || info.uri.getFragment() == null)
			throw new NullPointerException("Given uri has a null fragment component");
		
		items.put(info.uri.getFragment(), info);
	}
	
	//
	// URIHandler<List<NodeData>>
	//
	
	@Override
	public boolean handlesURI(URI uri) {
		if(uri != null && (uri.getScheme() == null || uri.getScheme().trim().length() == 0))
			return items.containsKey(uri.getFragment());
		return false;
	}

	@Override
	public List<NodeData> load(URI uri) throws IOException {
		// Make sure we can handle URI
		if(!handlesURI(uri))
			throw new IllegalArgumentException("Cannot handle uri '" + uri + "'");
		
		ArrayList<NodeData> ret = new ArrayList<NodeData>();
		ret.add(items.get(uri.getFragment()));
		return ret;
	}
}
