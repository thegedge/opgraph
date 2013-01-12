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
package ca.gedge.opgraph.app.components;

import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.Processor;

/**
 * A component to show inputs and output values from a node's {@link OpContext}.
 */
public class ContextViewerPanel extends JEditorPane {
	/** The processing context being debugged */
	private Processor processor;

	/** The node currently being viewed */
	private OpNode node;

	/**
	 * Default constructor.
	 */
	public ContextViewerPanel() {
		this.processor = null;
		this.node = null;

		// Set up editor kit
		final HTMLEditorKit kit = new HTMLEditorKit();
		final StyleSheet style = kit.getStyleSheet();
		style.addRule("body { padding: 5px; }");
		style.addRule("ul { margin: 5px 15px; }");
		style.addRule(".error { font-family: Courier,Courier New,Console,System; color: #ff0000; white-space: pre; }");

		// Initialize
		setEditorKit(kit);
		setEditable(false);

		updateDebugInfo();
	}

	/**
	 * Gets the processing context this component is using for displaying
	 * debug information on a node.
	 * 
	 * @return the context, or <code>null</code> if no context is being used
	 */
	public Processor getProcessingContext() {
		return processor;
	}

	/**
	 * Sets the processing context this component should use for displaying
	 * debug information on a node.
	 * 
	 * @param context  the context, or <code>null</code> if no context should be used
	 */
	public void setProcessingContext(Processor context) {
		if(this.processor != context) {
			this.processor = context;
			updateDebugInfo();
		}
	}

	/**
	 * Gets the node whose debug information this component is currently displaying.
	 * 
	 * @return the node
	 */
	public OpNode getNode() {
		return node;
	}

	/**
	 * Sets the node whose debug information this component should display.
	 * 
	 * @param node the node to set
	 */
	public void setNode(OpNode node) {
		if(this.node != node) {
			this.node = node;
			updateDebugInfo();
		}
	}

	/**
	 * Updates the debug info for the current node/processing context.
	 */
	public void updateDebugInfo() {
		String text = "<html><body><i>No debug info available</i></body></html>";
		if(processor != null) {
			final OpContext context = processor.getContext().findChildContext(node);
			if(context != null) {
				final StringBuilder sb = new StringBuilder();
				sb.append("<html><body>");
				sb.append("Inputs:<ul>");
				for(InputField field : node.getInputFields()) {
					sb.append("<li><b>");
					sb.append(field.getKey());
					sb.append("</b>: ");

					final Object value = context.get(field);
					sb.append(value == null ? "undefined" : value);

					sb.append("</li>");
				}
				sb.append("</ul>Outputs:<ul>");
				for(OutputField field : node.getOutputFields()) {
					sb.append("<li><b>");
					sb.append(field.getKey());
					sb.append("</b>: ");

					final Object value = context.get(field);
					sb.append(value == null ? "undefined" : value);

					sb.append("</li>");
				}
				sb.append("</ul>");
				sb.append("</body></html>");

				text = sb.toString();
			}
		}

		setText(text);
	}
}
