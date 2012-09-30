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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.extensions.CompositeNode;
import ca.gedge.opgraph.extensions.CustomProcessing;
import ca.gedge.opgraph.extensions.Publishable;

/**
 * A node that contains a macro operation: a collection of nodes that behave
 * as a single {@link OpNode}.
 */
@OpNodeInfo(
	name="Macro",
	description="A set of nodes that behave as a single operation.",
	category="General"
)
public class MacroNode
	extends OpNode
	implements CompositeNode, CustomProcessing, Publishable
{
	/** The graph representing this macro */
	protected OpGraph graph;
	
	/** A list from published inputs */
	protected List<PublishedInput> publishedInputs;
	
	/** A list of published outputs */
	protected List<PublishedOutput> publishedOutputs;
	
	/**
	 * The source file for this macro.
	 * 
	 * @see #getSource()
	 */
	private final File source;
	
	/**
	 * Constructs a new macro with no source file and a default graph.
	 */
	public MacroNode() {
		this(null, null);
	}
	
	/**
	 * Constructs a new macro with no source file and a specified graph.
	 * 
	 * @param graph  the graph
	 * 
	 * @throws NullPointerException  if the graph is <code>null</code>
	 */
	public MacroNode(OpGraph graph) {
		this(null, graph);
	}
	
	/**
	 * Constructs a macro node from the given source file and DAG.
	 * 
	 * @param source  the source file (see {@link #getSource()}
	 * @param graph  the graph
	 */
	public MacroNode(File source, OpGraph graph) {
		this.source = source;
		this.graph = (graph == null ? new OpGraph() : graph);
		this.publishedInputs = new ArrayList<PublishedInput>();
		this.publishedOutputs = new ArrayList<PublishedOutput>();
		
		putExtension(CompositeNode.class, this);
		putExtension(CustomProcessing.class, this);
		putExtension(Publishable.class, this);
	}
	
	/**
	 * Gets the source file for this macro.
	 * 
	 * @return  the source file from which this macro was constructed, or
	 *          <code>null</code> if this macro was constructed from the
	 *          same file as the root graph which contains this node.
	 */
	public File getSource() {
		return source;
	}
	
	/**
	 * Constructs a context mapping for this macro's published inputs. Inputs contained
	 * in the given context will be mapped to their appropriate node/input field in the
	 * internal graph this macro is using. The returned mapping will have the given context
	 * as the global context (i.e., the context mapped to by the <code>null</code> key).
	 * 
	 * @param context  the macro's local context
	 */
	protected void mapInputs(OpContext context) {
		for(PublishedInput publishedInput : publishedInputs) {
			final OpContext local = context.getChildContext(publishedInput.destinationNode);
			local.put(publishedInput.nodeInputField, context.get(publishedInput));
		}
	}
	
	/**
	 * Maps published outputs from a given context mapping to a given context.
	 * 
	 * @param context  the context to map outputs to
	 */
	protected void mapOutputs(OpContext context) {
		// Grab mapped outputs and put them in our context
		for(PublishedOutput publishedOutput : publishedOutputs) {
			OpContext sourceContext = context.findChildContext(publishedOutput.sourceNode);
			if(sourceContext != null)
				context.put(publishedOutput, sourceContext.get(publishedOutput.nodeOutputField));
		}
	}
	
	//
	// Overrides
	//
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		if(graph != null) {
			// First set up processor
			final Processor processor = new Processor(graph);
			processor.reset(context);
			
			// The reset call above could clear out the context, so map after
			mapInputs(context);
			
			// Now run the graph
			processor.stepAll();
			if(processor.getError() != null)
				throw processor.getError();
			
			// Map the published outputs from the child nodes back into context
			mapOutputs(context);
		}
	}
	
	//
	// CompositeNode
	//

	@Override
	public OpGraph getGraph() {
		return graph;
	}

	@Override
	public void setGraph(OpGraph graph) {
		this.graph = graph;
	}

	//
	// CustomProcessing
	//

	@Override
	public CustomProcessor getCustomProcessor() {
		final Iterator<OpNode> nodeIter = graph.getVertices().iterator();
		return new CustomProcessor() {
			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove not supported");
			}
			
			@Override
			public OpNode next() {
				return nodeIter.next();
			}
			
			@Override
			public boolean hasNext() {
				return nodeIter.hasNext();
			}
			
			@Override
			public void initialize(OpContext context) {
				mapInputs(context);
			}

			@Override
			public void terminate(OpContext context) {
				mapOutputs(context);
			}
		};
	}
	
	//
	// Publishable
	//

	@Override
	public InputField publish(String key, OpNode destination, InputField field) {
		// First, check to see if the field isn't already published
		InputField publishedInput = getPublishedInput(destination, field);

		// If no existing published input field for the given, create a new one.
		// Otherwise, set the key of the old one to the newly specified key.
		if(publishedInput == null) {
			final PublishedInput newInputField = new PublishedInput(key, destination, field);
			publishedInputs.add(newInputField);
			putField(newInputField);
			publishedInput = newInputField;
		} else {
			publishedInput.setKey(key);
		}

		return publishedInput;
	}

	@Override
	public OutputField publish(String key, OpNode source, OutputField field) {
		// First, check to see if the field isn't already published
		OutputField publishedOutput = getPublishedOutput(source, field);
		
		// If no existing published output field for the given, create a new one.
		// Otherwise, set the key of the old one to the newly specified key.
		if(publishedOutput == null) {
			final PublishedOutput newOutputField = new PublishedOutput(key, source, field);
			publishedOutputs.add(newOutputField);
			putField(newOutputField);
			publishedOutput = newOutputField;
		} else {
			publishedOutput.setKey(key);
		}

		return publishedOutput;
	}

	@Override
	public void unpublish(OpNode destination, InputField field) {
		Iterator<PublishedInput> iter = publishedInputs.iterator();
		while(iter.hasNext()) {
			PublishedInput publishedInput = iter.next();
			if(publishedInput.destinationNode == destination
					&& publishedInput.nodeInputField == field)
			{
				removeField(publishedInput);
				iter.remove();
				break;
			}
		}
	}

	@Override
	public void unpublish(OpNode destination, OutputField field) {
		Iterator<PublishedOutput> iter = publishedOutputs.iterator();
		while(iter.hasNext()) {
			PublishedOutput publishedOutput = iter.next();
			if(publishedOutput.sourceNode == destination
					&& publishedOutput.nodeOutputField == field)
			{
				removeField(publishedOutput);
				iter.remove();
				break;
			}
		}
	}

	@Override
	public List<PublishedInput> getPublishedInputs() {
		return Collections.unmodifiableList(publishedInputs);
	}

	@Override
	public List<PublishedOutput> getPublishedOutputs() {
		return Collections.unmodifiableList(publishedOutputs);
	}

	@Override
	public PublishedInput getPublishedInput(OpNode destination, InputField field) {
		PublishedInput foundInput = null;
		for(PublishedInput publishedInput : publishedInputs) {
			if(publishedInput.destinationNode == destination
					&& publishedInput.nodeInputField == field)
			{
				foundInput = publishedInput;
				break;
			}
		}

		return foundInput;
	}

	@Override
	public PublishedOutput getPublishedOutput(OpNode source, OutputField field) {
		PublishedOutput foundOutput = null;
		for(PublishedOutput publishedOutput : publishedOutputs) {
			if(publishedOutput.sourceNode == source
					&& publishedOutput.nodeOutputField == field)
			{
				foundOutput = publishedOutput;
				break;
			}
		}

		return foundOutput;
	}
}
