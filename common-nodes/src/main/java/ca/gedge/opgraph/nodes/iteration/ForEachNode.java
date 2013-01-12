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
package ca.gedge.opgraph.nodes.iteration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.gedge.opgraph.validators.CollectionValidator;

/**
 * A special macro node that loops over {@link List} inputs. When a field is
 * published from an internal node, the published field will accept any
 * {@link List} that contains elements of types accepted by the internal field.
 */
@OpNodeInfo(
	name="For Each",
	description="A macro operation in which the macro is executed based on collections given as input.",
	category="Iteration"
)
public class ForEachNode extends MacroNode {
	/** {@link OpContext} key for the current iteration */
	public static final String CURRENT_ITERATION_KEY = "currentIteration";

	/** {@link OpContext} key for the max number of iterations */
	public static final String MAX_ITERATIONS_KEY = "maxIterations";

	/**
	 * Constructs a new macro with no source file and a default graph.
	 */
	public ForEachNode() {
		super(null, null);
	}

	/**
	 * Constructs a new macro with no source file and a specified graph.
	 * 
	 * @param graph  the graph
	 * 
	 * @throws NullPointerException  if the graph is <code>null</code>
	 */
	public ForEachNode(OpGraph graph) {
		super(null, graph);
	}

	/**
	 * Constructs a macro node from the given source file and DAG.
	 * 
	 * @param source  the source file (see {@link #getSource()}
	 * @param graph  the graph
	 */
	public ForEachNode(File source, OpGraph graph) {
		super(source, graph);
	}

	/**
	 * Constructs a context mapping for this macro's published inputs. Inputs contained
	 * in the given context will be mapped to their appropriate node/input field in the
	 * internal graph this macro is using. The returned mapping will have the given context
	 * as the global context (i.e., the context mapped to by the <code>null</code> key).
	 * 
	 * @param context  the macro's local context
	 */
	private void mapInputs(OpContext context, int iteration) {
		// Put in information about the iteration
		context.put(CURRENT_ITERATION_KEY, iteration);

		// Child contexts
		for(PublishedInput publishedInput : publishedInputs) {
			final OpContext local = context.getChildContext(publishedInput.destinationNode);
			final List<?> data = (List<?>)context.get(publishedInput);
			final Object value = (iteration < data.size() ? data.get(iteration) : null);
			local.put(publishedInput.nodeInputField, value);
		}
	}

	/**
	 * Maps published outputs from a given context mapping to a given context.
	 * 
	 * @param contextsMap  the context mapping to map outputs from
	 * @param context  the context to map outputs to
	 */
	private void mapOutputs(OpContext context, int iteration) {
		// Grab mapped outputs and put them in our context
		for(PublishedOutput publishedOutput : publishedOutputs) {
			final OpContext sourceContext = context.findChildContext(publishedOutput.sourceNode);
			if(sourceContext != null) {
				final Object result = sourceContext.get(publishedOutput.nodeOutputField);
				if(context.containsKey(publishedOutput)) {
					final ArrayList<Object> objects = new ArrayList<Object>((ArrayList<?>)context.get(publishedOutput));
					objects.add(result);
					context.put(publishedOutput, objects);
				} else {
					final ArrayList<Object> objects = new ArrayList<Object>();
					objects.add(result);
					context.put(publishedOutput, objects);
				}
			}
		}
	}

	//
	// Overrides
	//

	@Override
	public InputField publish(String key, OpNode destination, InputField field) {
		final InputField published = super.publish(key, destination, field);
		published.setValidator(new CollectionValidator(published.getValidator()));
		return published;
	}

	@Override
	public OutputField publish(String key, OpNode source, OutputField field) {
		final OutputField published = super.publish(key, source, field);
		published.setOutputType(Collection.class);
		return published;
	}

	@Override
	public void operate(OpContext context) throws ProcessingException {
		// First, find the biggest list we have
		int maxIterations = 0;
		for(PublishedInput field : getPublishedInputs()) {
			final Collection<?> data = (Collection<?>)context.get(field);
			maxIterations = Math.max(maxIterations, data.size());
		}

		// Process
		if(graph != null) {
			final Processor processor = new Processor(graph);

			context.put(MAX_ITERATIONS_KEY, maxIterations);
			for(int iteration = 0; iteration < maxIterations; ++iteration) {
				processor.reset(context);

				// The reset call above could clear out the context, so map after
				mapInputs(context, iteration);

				// Now run the graph
				processor.stepAll();
				if(processor.getError() != null)
					throw processor.getError();

				// Map the published outputs from the child nodes back into context
				mapOutputs(context, iteration);
			}
		}
	}

	//
	// CustomProcessing
	//

	@Override
	public CustomProcessor getCustomProcessor() {
		return new CustomProcessor() {
			private OpContext context;
			private OpNode nextNode;
			private Iterator<OpNode> nodeIter;
			private int iteration = 0;
			private int maxIterations = 0;

			@Override
			public void remove() {
				throw new UnsupportedOperationException("remove not supported");
			}

			@Override
			public OpNode next() {
				if(!hasNext())
					throw new NoSuchElementException();

				final OpNode node = nextNode;
				nextNode = null;
				return node;
			}

			@Override
			public boolean hasNext() {
				if(nextNode != null)
					return true;

				if(!nodeIter.hasNext() && iteration < maxIterations) {
					mapOutputs(context, iteration);

					++iteration;
					if(iteration < maxIterations) {
						nodeIter = graph.getVertices().iterator();
						mapInputs(context, iteration);
					}
				}

				if(nodeIter.hasNext())
					nextNode = nodeIter.next();

				return (nextNode != null);
			}

			@Override
			public void initialize(OpContext context) {
				this.maxIterations = 0;
				this.context = context;
				this.nodeIter = graph.getVertices().iterator();

				// First, find the biggest list we have
				for(PublishedInput field : getPublishedInputs()) {
					if(context.containsKey(field)) {
						final Collection<?> data = (Collection<?>)context.get(field);
						this.maxIterations = Math.max(this.maxIterations, data.size());
					}
				}

				context.put(MAX_ITERATIONS_KEY, maxIterations);

				mapInputs(context, 0);
			}

			@Override
			public void terminate(OpContext context) {}
		};
	}
}
