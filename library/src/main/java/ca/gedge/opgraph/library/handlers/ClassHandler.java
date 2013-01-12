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
import java.util.List;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.library.instantiators.ClassInstantiator;

/**
 * A {@link URIHandler} that loads node information from a given class. Handles
 * URIs of the form <code>class:&lt;classpath&gt;</code>
 */
public class ClassHandler implements URIHandler<List<NodeData>> {
	@Override
	public boolean handlesURI(URI uri) {
		return (uri != null && "class".equals(uri.getScheme()));
	}

	@Override
	public List<NodeData> load(URI uri) throws IOException {
		// Make sure we can handle URI
		if(!handlesURI(uri))
			throw new IllegalArgumentException("Cannot handle uri '" + uri + "'");

		// Load class
		ArrayList<NodeData> ret = new ArrayList<NodeData>();
		try {
			final String className = uri.getSchemeSpecificPart();
			final Class<?> clz = Class.forName(className, false, getClass().getClassLoader());
			final Class<? extends OpNode> ovClz = clz.asSubclass(OpNode.class);

			// If a node info annotation is present then we don't need to instantiate the class
			final OpNodeInfo info = ovClz.getAnnotation(OpNodeInfo.class);
			if(info != null) {
				ret.add(new NodeData(uri,
				                     info.name(),
				                     info.description(),
				                     info.category(),
				                     new ClassInstantiator<OpNode>(ovClz)));
			} else {
				// XXX should we create a new instance or require annotation?
				final OpNode node = ovClz.newInstance();
				ret.add(new NodeData(uri,
				                     node.getName(),
				                     node.getDescription(),
				                     node.getCategory(),
				                     new ClassInstantiator<OpNode>(ovClz)));
			}
		} catch(ClassCastException exc) {
			throw new IOException("Given class '" + uri.getPath() + "' does not extend OpNode", exc);
		} catch(ClassNotFoundException exc) {
			throw new IOException("Unknown class: " + uri.getPath(), exc);
		} catch(InstantiationException exc) {
			throw new IOException("Class could not be instantiated: " + uri.getPath(), exc);
		} catch(IllegalAccessException exc) {
			throw new IOException("Class could not be instantiated: " + uri.getPath(), exc);
		}

		return ret;
	}
}
