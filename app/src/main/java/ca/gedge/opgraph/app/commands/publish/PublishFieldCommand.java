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
package ca.gedge.opgraph.app.commands.publish;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;

import ca.gedge.opgraph.ContextualItem;
import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.edits.graph.RemoveLinkEdit;
import ca.gedge.opgraph.app.edits.node.PublishFieldEdit;
import ca.gedge.opgraph.extensions.Publishable;

/**
 * A command to publish a field in a node.
 */
class PublishFieldCommand extends AbstractAction {
	/** The publishable object */
	private Publishable publishable;

	/** The node with a field to be published */
	private OpNode node;

	/** The field to publish */
	private ContextualItem field;

	/**
	 * Constructs a command that publishes a field of a given node.
	 * 
	 * @param publishable  the publishing extension
	 * @param node  the node with a field to publish
	 * @param field  the field to publish
	 */
	public PublishFieldCommand(Publishable publishable, OpNode node, ContextualItem field) {
		super(field.getKey());

		this.publishable = publishable;
		this.node = node;
		this.field = field;

		putValue(SHORT_DESCRIPTION, field.getDescription());
		putValue(LONG_DESCRIPTION, field.getDescription());
	}

	/**
	 * If currently editing , publish the given field of the selected node.
	 * 
	 * @param field  the field to publish
	 */
	public void publishFieldOfSelected(ContextualItem field) {
		if(publishable != null && field != null && node != null) {
			final GraphDocument document = GraphEditorModel.getActiveDocument();
			document.getUndoSupport().postEdit(new PublishFieldEdit(document.getGraph(), publishable, node, field.getKey(), field));
			document.getCanvas().updateAnchorFillStates(node);
		}
	}

	/**
	 * If currently editing a macro, unpublish the given field of the selected node.
	 * 
	 * @param field  the field to unpublish
	 */
	public void unpublishFieldOfSelected(ContextualItem field) {
		if(publishable != null && field != null && node != null) {
			final GraphDocument document = GraphEditorModel.getActiveDocument();
			final OpGraph graphOfMacroParent = document.getBreadcrumb().peekState(1);
			final OpNode publishNode = ((publishable instanceof OpNode) ? (OpNode)publishable : null);

			// If there is a parent graph, then check to see which links will
			// be removed if this field is unpublished
			final Collection<OpLink> linksToRemove = new ArrayList<OpLink>();
			if(graphOfMacroParent != null) {
				// For input fields, check incoming links, otherwise outgoing
				if(field instanceof InputField) {
					final InputField publishedField = publishable.getPublishedInput(node, (InputField)field);
					for(OpLink link : graphOfMacroParent.getIncomingEdges(publishNode)) {
						if(link.getDestinationField().equals(publishedField))
							linksToRemove.add(link);
					}
				} else if(field instanceof OutputField) {
					final OutputField publishedField = publishable.getPublishedOutput(node, (OutputField)field);
					for(OpLink link : graphOfMacroParent.getOutgoingEdges(publishNode)) {
						if(link.getSourceField().equals(publishedField))
							linksToRemove.add(link);
					}
				}
			}

			// Compound edit if there are links to remove
			if(linksToRemove.size() == 0) {
				document.getUndoSupport().postEdit(new PublishFieldEdit(document.getGraph(), publishable, node, null, field));
			} else {
				document.getUndoSupport().beginUpdate();
				for(OpLink link : linksToRemove)
					document.getUndoSupport().postEdit(new RemoveLinkEdit(graphOfMacroParent, link));
				document.getUndoSupport().postEdit(new PublishFieldEdit(document.getGraph(), publishable, node, null, field));
				document.getUndoSupport().endUpdate();
			}

			document.getCanvas().updateAnchorFillStates(node);
		}
	}

	//
	// Overrides
	//

	@Override
	public void actionPerformed(ActionEvent e) {
		boolean isPublishing = true;
		if((field instanceof InputField) && publishable.getPublishedInput(node, (InputField)field) != null)
			isPublishing = false;
		else if((field instanceof OutputField) && publishable.getPublishedOutput(node, (OutputField)field) != null)
			isPublishing = false;

		if(isPublishing)
			publishFieldOfSelected(field);
		else
			unpublishFieldOfSelected(field);
	}
}
