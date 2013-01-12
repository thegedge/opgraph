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
package ca.gedge.opgraph.nodes.menu.edits;

import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.extensions.Publishable.PublishedInput;
import ca.gedge.opgraph.extensions.Publishable.PublishedOutput;
import ca.gedge.opgraph.nodes.general.MacroNode;

/**
 * An edit that creates a macro from a given collection of nodes in a graph.
 */
public class ExplodeMacroEdit extends AbstractUndoableEdit {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(ExplodeMacroEdit.class.getName());

	/** The graph to which this edit was applied  */
	private OpGraph graph;

	/** The constructed macro node */
	private MacroNode macro;

	/** Links within the macro, and for published fields */
	private Set<OpLink> newLinks;

	/** Links attached to the macro */
	private Set<OpLink> oldLinks;

	/**
	 * Constructs a macro-explosion edit which acts upon a given macro node.
	 * 
	 * @param graph  the graph to which this edit will be applied
	 * @param macro  the macro node to explode 
	 */
	public ExplodeMacroEdit(OpGraph graph, MacroNode macro) {
		this.graph = graph;
		this.macro = macro;

		this.oldLinks = new TreeSet<OpLink>();
		this.oldLinks.addAll(graph.getIncomingEdges(macro));
		this.oldLinks.addAll(graph.getOutgoingEdges(macro));

		this.newLinks = new TreeSet<OpLink>();
		this.newLinks.addAll(macro.getGraph().getEdges());

		for(OpLink link : graph.getIncomingEdges(macro)) {
			// If an incoming link is linked to a node that isn't a published
			// input, we can't reliably explode this node 
			if(!(link.getDestinationField() instanceof PublishedInput))
				throw new IllegalArgumentException("Macro node contains an incoming link linked to a node that isn't a published input.");

			final PublishedInput input = (PublishedInput)link.getDestinationField();
			try {
				this.newLinks.add(new OpLink(link.getSource(),
				                             link.getSourceField(),
				                             input.destinationNode,
				                             input.nodeInputField));
			} catch(ItemMissingException exc) {
				throw new IllegalArgumentException("A link connected to this MacroNode is in an impossible state");
			}
		}

		for(OpLink link : graph.getOutgoingEdges(macro)) {
			// If an outgoing link is linked to a node that isn't a published
			// output, we can't reliably explode this node 
			if(!(link.getSourceField() instanceof PublishedOutput))
				throw new IllegalArgumentException("Macro node contains an outgoing link linked to a node that isn't a published output. Cannot reliably explode.");

			final PublishedOutput output = (PublishedOutput)link.getSourceField();
			try {
				this.newLinks.add(new OpLink(output.sourceNode,
				                             output.nodeOutputField,
				                             link.getDestination(),
				                             link.getDestinationField()));
			} catch(ItemMissingException exc) {
				throw new IllegalArgumentException("A link connected to this MacroNode is in an impossible state");
			}
		}

		perform();
	}

	/**
	 * Performs this edit.
	 */
	private void perform() {
		// Remove macro node
		graph.remove(macro);

		// Add all nodes from macro
		for(OpNode nodes : macro.getGraph().getVertices())
			graph.add(nodes);

		// Add new links
		for(OpLink link : newLinks) {
			try {
				graph.add(link);
			} catch(VertexNotFoundException exc) {
				LOGGER.severe("impossible exception");
			} catch(CycleDetectedException exc) {
				LOGGER.severe("impossible exception");
			}
		}
	}

	//
	// AbstractEdit overrides
	//

	@Override
	public String getPresentationName() {
		return "Create Macro";
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		perform();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();

		// Add all nodes from macro
		for(OpNode node : macro.getGraph().getVertices())
			graph.remove(node);

		// Add macro node
		graph.add(macro);

		for(OpLink link : oldLinks) {
			try {
				graph.add(link);
			} catch(VertexNotFoundException exc) {
				LOGGER.severe("impossible exception");
			} catch(CycleDetectedException exc) {
				LOGGER.severe("impossible exception");
			}
		}
	}
}
