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
package ca.gedge.opgraph.app.components.canvas;

import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.UndoableEditListener;

import ca.gedge.opgraph.ContextualItem;
import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeListener;
import ca.gedge.opgraph.OutputField;

/**
 * A component that visualizes an {@link OpNode}.
 */
public class CanvasNode extends JPanel {
	/** The node being displayed */
	private OpNode node;

	/** A label for the node name */
	private CanvasNodeName name;

	/** A panel containing all the input fields */
	private JPanel inputs;

	/** A panel containing all the output fields */
	private JPanel outputs;

	/** The style used for this component */
	private NodeStyle style;

	/** Padding used in component */
	private final static int PADDING = 4;

	/** The selected state */
	private boolean selected;

	/** A mapping from field to the field component */
	private Map<ContextualItem, CanvasNodeField> fields;

	/**
	 * Constructs a component that displays the specified node using a default style.
	 * 
	 * @param node  the node to display
	 * 
	 * @throws NullPointerException  if <code>node</code> is <code>null</code>
	 */
	public CanvasNode(OpNode node) {
		this(node, new NodeStyle());
	}

	/**
	 * Constructs a component that displays a node using a particular style.
	 * 
	 * @param node  the node to display
	 * @param style  the style to display this node in
	 * 
	 * @throws NullPointerException  if <code>node</code> is <code>null</code>
	 */
	public CanvasNode(OpNode node, NodeStyle style) {
		super(new GridBagLayout());

		// Create components
		this.selected = false;
		this.name = new CanvasNodeName(node, style);
		this.inputs = new JPanel();
		this.outputs = new JPanel();
		this.fields = new HashMap<ContextualItem, CanvasNodeField>();

		// Initialize components
		setOpaque(false);
		setBorder(null);

		inputs.setOpaque(false);
		inputs.setLayout(new BoxLayout(inputs, BoxLayout.Y_AXIS));

		outputs.setOpaque(false);
		outputs.setLayout(new BoxLayout(outputs, BoxLayout.Y_AXIS));

		setNode(node);

		// Add components to layout
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(PADDING, PADDING, PADDING / 2, PADDING);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		add(name, gbc);

		gbc.insets = new Insets(PADDING, PADDING, PADDING, PADDING);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		add(inputs, gbc);

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridx = 1;
		add(outputs, gbc);
	}

	/**
	 * Gets the canvas field component at the specified point.
	 * 
	 * @param p  the point, in this component's coordinate system
	 * 
	 * @return the canvas node field component, or <code>null</code> if no
	 *         field component exists at the specified point
	 */
	public CanvasNodeField getFieldAt(Point p) {
		CanvasNodeField field = null;
		if(inputs.getBounds().contains(p)) {
			p = SwingUtilities.convertPoint(this, p, inputs);
			Component comp = inputs.getComponentAt(p);
			if(comp instanceof CanvasNodeField)
				field = (CanvasNodeField)comp;
		} else if(outputs.getBounds().contains(p)) {
			p = SwingUtilities.convertPoint(this, p, outputs);
			Component comp = outputs.getComponentAt(p);
			if(comp instanceof CanvasNodeField)
				field = (CanvasNodeField)comp;
		}
		return field;
	}

	/**
	 * Gets a mapping from {@link OpNode} to the respective node
	 * component that displays that node.
	 * 
	 * @return the mapping
	 */
	public Map<ContextualItem, CanvasNodeField> getFieldsMap() {
		return fields;
	}

	/**
	 * Gets the style used for this node.
	 * 
	 * @return the node style
	 */
	public NodeStyle getStyle() {
		return style;
	}

	/**
	 * Sets the style used for this node.
	 * 
	 * @param style  the node style
	 */
	public void setStyle(NodeStyle style) {
		this.style = (style == null ? new NodeStyle() : style);

		setBorder(style.NodeBorder);
		setBackground(this.style.NodeBackgroundColor);
		setForeground(this.style.NodeBorderColor);

		// Update children
		name.setStyle(this.style);
		for(CanvasNodeField fieldComp : fields.values())
			fieldComp.setStyle(this.style);

		revalidate();
		repaint();
	}

	/**
	 * Gets the node being displayed by this component.
	 * 
	 * @return the node
	 */
	public OpNode getNode() {
		return node;
	}

	/**
	 * Sets the node being displayed by this component.
	 * 
	 * @param node  the node
	 * 
	 * @throws NullPointerException  if <code>node</code> is <code>null</code>
	 */
	public void setNode(OpNode node) {
		if(node == null) throw new NullPointerException("node cannot be null");
		if(node != this.node) {
			if(this.node != null)
				this.node.removeNodeListener(nodeListener);

			this.node = node;
			this.node.addNodeListener(nodeListener);

			setStyle(NodeStyle.getStyleForNode(node));
			super.setToolTipText(node.getDescription());
			name.setText(node.getName());
			updateFields();
		}
	}

	/**
	 * Updates the listing of fields in this component.
	 */
	public void updateFields() {
		// Remove old fields, and add new ones
		fields.clear();
		inputs.removeAll();
		outputs.removeAll();

		for(InputField field : node.getInputFields())
			nodeListener.fieldAdded(node, field);

		for(OutputField field : node.getOutputFields())
			nodeListener.fieldAdded(node, field);
	}

	/**
	 * Set whether or not this node is selected.
	 * 
	 * @param selected  the selected state
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
		revalidate();
		repaint();
	}

	/**
	 * Gets the selected state of this node.
	 * 
	 * @return the selected state
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Get the y-coordinate of the separator line that separates the node
	 * name from the fields.
	 * 
	 * @return y-coordinate for separator
	 */
	public int getSeparatorY() {
		final Insets insets = getInsets();
		if(inputs.getComponentCount() == 0 && outputs.getComponentCount() == 0)
			return (getHeight() - insets.bottom - insets.top);
		return (name.getHeight() + PADDING / 2 + insets.top);
	}

	//
	// Overrides
	//

	@Override
	public void setBorder(Border border) {
		if(border == null)
			border = new DefaultNodeBorder();
		super.setBorder(border);
	}

	// TODO export drawing and shape information to NodeStyle
	@Override
	public void paintComponent(Graphics gfx) {
		super.paintComponent(gfx);

		Graphics2D g = (Graphics2D)gfx;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final Insets insets = getInsets();
		final int w = getWidth() - insets.left - insets.right;
		final int h = getHeight() - insets.top - insets.bottom;

		g.translate(insets.left, insets.top);

		// Name area background
		final int separatorY = getSeparatorY();
		Paint namePaint = new GradientPaint(0, 0, style.NodeNameTopColor, 0, separatorY, style.NodeNameBottomColor);
		g.setPaint(namePaint);
		g.fillRect(0, 0, w, separatorY);

		g.setColor(style.NodeBackgroundColor);
		g.fillRect(0, separatorY, w, h - separatorY);

		// Node name separator
		g.setColor(style.NodeBorderColor);
		g.drawLine(0, separatorY, w - 1, separatorY);

		// Translate back
		g.translate(-insets.left, -insets.top);
	}

	//
	// OpNpdeListener
	//

	private final OpNodeListener nodeListener = new OpNodeListener() {
		@Override
		public void fieldRemoved(OpNode node, OutputField field) {
			final CanvasNodeField fieldComp = fields.get(field);
			outputs.remove(fieldComp);
			fields.remove(field);

			revalidate();
			repaint();
		}

		@Override
		public void fieldRemoved(OpNode node, InputField field) {
			final CanvasNodeField fieldComp = fields.get(field);
			inputs.remove(fieldComp);
			fields.remove(field);

			revalidate();
			repaint();
		}

		@Override
		public void fieldAdded(OpNode node, OutputField field) {
			final CanvasNodeField fieldComp = new CanvasNodeField(field);
			fieldComp.setStyle(style);
			outputs.add(fieldComp);
			fields.put(field, fieldComp);

			revalidate();
			repaint();
		}

		@Override
		public void fieldAdded(OpNode node, InputField field) {
			if(field != OpNode.ENABLED_FIELD || style.ShowEnabledField) {
				final CanvasNodeField fieldComp = new CanvasNodeField(field);
				fieldComp.setStyle(style);
				inputs.add(fieldComp);
				fields.put(field, fieldComp);

				revalidate();
				repaint();
			}
		}

		@Override
		public void nodePropertyChanged(String propertyName, Object oldValue, Object newValue) {
			if(propertyName.equals(OpNode.NAME_PROPERTY)) {
				revalidate();
				repaint();
			}
		}
	};

	//
	// UndoableEdit support
	//

	/**
	 * Adds an undoable edit listener to this component.
	 * 
	 * @param listener  the listener to add
	 */
	public void addUndoableEditListener(UndoableEditListener listener) {
		name.addUndoableEditListener(listener);
	}

	/**
	 * Removes an undoable edit listener from this component.
	 * 
	 * @param listener  the listener to remove
	 */
	public void removeUndoableEditListener(UndoableEditListener listener) {
		name.removeUndoableEditListener(listener);
	}
}
