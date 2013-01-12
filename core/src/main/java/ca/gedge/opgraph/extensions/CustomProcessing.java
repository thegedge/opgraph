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
package ca.gedge.opgraph.extensions;

import java.util.Iterator;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.Processor;

/**
 * An extension meant for any {@link OpNode} that requires custom initialization,
 * operation, and termination when being stepped into by a {@link Processor}.
 * {@link Processor} will only step into a node with a {@link CompositeNode} extension.
 * 
 * For example, a macro node may need to map inputs from the macro node's
 * {@link OpContext} to appropriate nodes  on initialization, and map outputs
 * from nodes to its {@link OpContext} on termination.
 */
public interface CustomProcessing {
	/**
	 * Interface for custom processors.
	 */
	public interface CustomProcessor extends Iterator<OpNode> {
		/**
		 * Initializes the given context for processing.
		 * 
		 * @param context  the context to initialize
		 */
		public abstract void initialize(OpContext context);

		/**
		 * Terminates processing, updating the given context as necessary.
		 * 
		 * @param context  the context to update, if necessary
		 */
		public abstract void terminate(OpContext context);
	}

	/**
	 * Gets a custom processor for a node.
	 * 
	 * @return a custom processor
	 */
	public abstract CustomProcessor getCustomProcessor();
}
