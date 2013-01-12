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

import java.util.List;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;

/**
 * An extension that allows a field of a node to be "published". This
 * extension is intended for nodes with the {@link CompositeNode}
 * extension, so that they can expose fields of nodes in their composite
 * graph to be exposed to the graph in which the parent node is located.
 */
public interface Publishable {
	/**
	 * An input field of a child node that is published for input to
	 * this macro.
	 */
	public static class PublishedInput extends InputField {
		/** The node whose input field is published */
		public final OpNode destinationNode;

		/** The input field being published */
		public final InputField nodeInputField;

		/**
		 * Constructs a published input.
		 * 
		 * @param key  the key for the published input field
		 * @param destinationNode  the node having an input field published
		 * @param nodeInputField  the input field to publish
		 */
		public PublishedInput(String key, OpNode destinationNode, InputField nodeInputField) {
			super(key, nodeInputField.getDescription(), nodeInputField.isOptional(), true);

			this.destinationNode = destinationNode;
			this.nodeInputField = nodeInputField;

			setValidator(nodeInputField.getValidator());
		}
	}

	/**
	 * An output field of a child node that is published for output from
	 * this macro.
	 */
	public static class PublishedOutput extends OutputField {
		/** The node whose output field is published */
		public final OpNode sourceNode;

		/** The output field being published */
		public final OutputField nodeOutputField;

		/**
		 * Constructs a published input.
		 * 
		 * @param key  the key for the published output field
		 * @param sourceNode  the node having an output field published
		 * @param nodeOutputField  the output field to publish
		 */
		public PublishedOutput(String key, OpNode sourceNode, OutputField nodeOutputField) {
			super(key, nodeOutputField.getDescription(), true, nodeOutputField.getOutputType());
			this.sourceNode = sourceNode;
			this.nodeOutputField = nodeOutputField;
		}
	}

	/**
	 * Publishes an input field of a given node in this macro.
	 * 
	 * @param key  the key to give the published field, which can differ from the key
	 *             of the given {@link InputField}
	 * @param destination  the {@link OpNode} who shall have an input field published
	 * @param field  the {@link InputField} to publish
	 * 
	 * @return the {@link InputField} associated with the published field
	 */
	public abstract InputField publish(String key, OpNode destination, InputField field);

	/**
	 * Publishes an output field of a given node in this macro.
	 * 
	 * @param key  the key to give the published field, which can differ from the key
	 *             of the given {@link OutputField}
	 * @param source  the {@link OpNode} who shall have an output field published
	 * @param field  the {@link InputField} to publish
	 * 
	 * @return the {@link OutputField} associated with the published field
	 */
	public abstract OutputField publish(String key, OpNode source, OutputField field);

	/**
	 * Unpublish an input field of a given node.
	 * 
	 * @param destination  the {@link OpNode} which has an input field published
	 * @param field  the published {@link InputField}
	 */
	public abstract void unpublish(OpNode destination, InputField field);

	/**
	 * Unpublish an output field of a given node.
	 * 
	 * @param destination  the {@link OpNode} which has an output field published
	 * @param field  the published {@link OutputField}
	 */
	public abstract void unpublish(OpNode destination, OutputField field);

	/**
	 * Gets the list of published inputs.
	 * 
	 * @return  list of published inputs (immutable)
	 */
	public abstract List<PublishedInput> getPublishedInputs();

	/**
	 * Gets the list of published outputs.
	 * 
	 * @return  list of published outputs (immutable)
	 */
	public abstract List<PublishedOutput> getPublishedOutputs();

	/**
	 * Get a published input field for the given node and input field
	 * that it should have published.
	 * 
	 * @param destination  the {@link OpNode} which has an input field published
	 * @param field  the published {@link InputField}
	 * 
	 * @return the published {@link InputField}, or <code>null</code> if the
	 *         given node/field pair is not published
	 */
	public abstract PublishedInput getPublishedInput(OpNode destination, InputField field);

	/**
	 * Get a published output field for the given node and output field
	 * that it should have published.
	 * 
	 * @param source  the {@link OpNode} which has an output field published
	 * @param field  the published {@link OutputField}
	 * 
	 * @return the published {@link InputField}, or <code>null</code> if the
	 *         given node/field pair is not published
	 */
	public abstract PublishedOutput getPublishedOutput(OpNode source, OutputField field);
}
