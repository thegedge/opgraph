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
package ca.gedge.opgraph.tools;

import java.net.URL;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.stringtemplate.v4.*;

import ca.gedge.opgraph.OpNode;

/**
 * A tool that generates DITA files for all known {@link OpNode}
 * classes. Generated DITA files are formed from a template.
 */
public class GenerateDITAFiles {
	/** Logger **/
	private static final Logger LOGGER = Logger.getLogger(GenerateDITAFiles.class.getName());
	
	/**
	 * Program entry point.
	 * 
	 * @param args  program arguments
	 */
	public static void main(String[] args) {
		// Load up the template
		final URL templateURL = GenerateDITAFiles.class.getResource("node_template.st");
		final STGroup group = new STGroupFile(templateURL, "UTF-8", '$', '$');
		ST template = group.getInstanceOf("base");

		// Run through all known implementations of OpNode and output
		// the rendered template based on each
		Iterator<OpNode> iter = ServiceLoader.load(OpNode.class).iterator();
		while(iter.hasNext()) {
			try {
				OpNode v = iter.next();
				template.add("node", v);
				System.out.println( template.render() );
				template.remove("node");
			} catch(ServiceConfigurationError exc) {
				LOGGER.warning("Could not instantiate class as OpNode: " + exc.getLocalizedMessage());
			}
		}
	}
}
