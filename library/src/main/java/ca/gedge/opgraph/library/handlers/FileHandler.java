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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ca.gedge.opgraph.library.NodeData;

/**
 * A {@link URIHandler} that loads node information from a file. Handles
 * URIs of the form:
 * <ul>
 *   <li><code>file:&lt;path&gt;</code>, for loading all macros from a file</li>
 *   <li><code>file:&lt;path&gt;#&lt;macro_id&gt</code>, for loading a specific macro</li>
 * </ul>
 */
public class FileHandler implements URIHandler<List<NodeData>> {
	//
	// URIHandler<List<NodeData>>
	//

	@Override
	public boolean handlesURI(URI uri) {
		return (uri != null && "file".equals(uri.getScheme()));
	}

	@Override
	public List<NodeData> load(URI uri) throws IOException {
		// Make sure we can handle URI
		if(!handlesURI(uri))
			throw new IllegalArgumentException("Cannot handle uri '" + uri + "'");

		// Make sure file exists
		final File source = new File(uri.getPath());
		if(!source.exists())
			throw new IOException("File '" + source.getPath() + "' does not exist");

		// If no fragment, load all macros, otherwise load specific macro
		final ArrayList<NodeData> ret = new ArrayList<NodeData>();

		// FIXME since maven
//		final InputStream stream = new FileInputStream(source);
//		if(uri.getFragment() == null) {
//			final XMLGraphIO io = new XMLGraphIO();
//			for(NodeData info : io.loadMacros(stream)) {
//				final URI nodeURI = URI.create(uri.toString() + "#" + info.uri.getFragment());
//				ret.add(new NodeData(nodeURI, info.name, info.description, info.category, info.instantiator));
//			}
//		} else {
//			final XMLGraphIO io = new XMLGraphIO();
//			final NodeData info = io.loadMacro(stream, uri.getFragment());
//			ret.add(new NodeData(uri, info.name, info.description, info.category, info.instantiator));
//		}

		return ret;
	}
}
