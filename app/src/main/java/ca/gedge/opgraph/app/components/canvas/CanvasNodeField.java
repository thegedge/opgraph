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

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import ca.gedge.opgraph.ContextualItem;
import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.components.DoubleClickableTextField;

/**
 * A component that displays either an {@link InputField} or an {@link OutputField}.
 */
public class CanvasNodeField extends JComponent {
	/**
	 * State for link anchor points on this field.
	 */
	public static enum AnchorFillState {
		/** No fill state */
		NONE,

		/** Link is attached to this field */
		LINK,

		/** A default value is attached to this field */
		DEFAULT,

		/** This field is published */
		PUBLISHED,
	}

	/** Stroke we use to show an optional input field */
	private final static BasicStroke optionalFieldStroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{1}, 0);

	/** The input/output field being displayed */
	private ContextualItem field;

	/** The field's anchoring point for an link */
	private Ellipse2D anchor;

	/** The style used for this component */
	private NodeStyle style;

	/** The color used to fill this anchor */
	private AnchorFillState anchorFillState;

	/** The field's name */
	private FieldName name;

	/**
	 * Extension of {@link DoubleClickableTextField} that modifies the current
	 * field's key whenever the text changes.
	 */
	private class FieldName extends JTextField {
		/** Double-click support */
		private DoubleClickableTextField doubleClickSupport;

		/** The field this text field displays */
		private ContextualItem field;

		/** The default font */
		private final Font defaultFont;

		public FieldName() {
			this.doubleClickSupport = new DoubleClickableTextField(this);
			this.defaultFont = getFont();

			setBorder(new EmptyBorder(2, 5, 2, 0));

			this.doubleClickSupport.addPropertyChangeListener(DoubleClickableTextField.TEXT_PROPERTY, textListener);
		}

		public void setField(ContextualItem field) {
			this.field = field;
			super.setText(field == null ? "" : field.getKey());
			super.setToolTipText(field == null ? "" : field.getDescription());

			if(field instanceof InputField) {
				setHorizontalAlignment(SwingConstants.LEFT);
				setEditable( !((InputField)field).isFixed() );
				setFont(((InputField)field).isOptional() ? defaultFont.deriveFont(Font.ITALIC) : defaultFont);
			} else if(field instanceof OutputField) {
				setHorizontalAlignment(SwingConstants.RIGHT);
				setEditable( !((OutputField)field).isFixed() );
				setFont(defaultFont);
			} else {
				setEditable(true);
				setFont(defaultFont);
			}
		}

		private PropertyChangeListener textListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if(field != null)
					field.setKey((String)e.getNewValue());
			}
		};
	}

	/**
	 * Constructs a component that displays the given field.
	 * 
	 * @param field  the field
	 * 
	 * @throws NullPointerException  if specified field is <code>null</code>
	 */
	public CanvasNodeField(ContextualItem field) {
		this.name = new FieldName();
		this.anchor = new Ellipse2D.Double();
		this.anchorFillState = AnchorFillState.NONE;

		setLayout(null);
		setFont(UIManager.getLookAndFeelDefaults().getFont("Label.font"));
		setField(field);
		setOpaque(false);

		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseMotionAdapter);

		add(name);
	}

	/**
	 * Get the link anchoring area for this field.
	 * 
	 * @return the anchor
	 */
	public Ellipse2D getAnchor() {
		return (Ellipse2D)anchor.clone();
	}

	/**
	 * Sets the state used for the anchor fill.
	 * 
	 * If the given state is {@link AnchorFillState#DEFAULT}, and the current
	 * state is {@link AnchorFillState#LINK}, then the change does not occur.
	 * 
	 * @param anchorFillState  the fill state
	 */
	public void setAnchorFillState(AnchorFillState anchorFillState) {
		if(this.anchorFillState != anchorFillState) {
			this.anchorFillState = anchorFillState;
			repaint();
		}
	}

	/**
	 * Sets the anchor fill state to a specified state, but only if the
	 * current state is not {@link AnchorFillState#LINK}.
	 * 
	 * @param anchorFillState  the fill state
	 */
	public void updateAnchorFillState(AnchorFillState anchorFillState) {
		if(this.anchorFillState != AnchorFillState.LINK)
			setAnchorFillState(anchorFillState);
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
		if(!style.ShowEnabledField && field == OpNode.ENABLED_FIELD) {
			if(getParent() != null)
				getParent().remove(this);
		} else {
			revalidate();
		}
	}

	/**
	 * Gets the field being displayed by this component.
	 * 
	 * @return the field
	 */
	public ContextualItem getField() {
		return field;
	}

	/**
	 * Sets the field being displayed by this component.
	 * 
	 * @param field  the field
	 * 
	 * @throws NullPointerException  if specified field is <code>null</code>
	 */
	public void setField(ContextualItem field) {
		if(field == null) throw new NullPointerException("field cannot be null");
		if(field != this.field) {
			this.field = field;

			name.setField(field);
			setToolTipText(field.getDescription());

			revalidate();
		}
	}

	//
	// Overrides
	//

	@Override
	public void setBounds(int newX, int newY, int newW, int newH) {
		super.setBounds(newX, newY, newW, newH);

		// Everything else based off of insets
		final Insets insets = getInsets();
		newW -= insets.left + insets.right + 1;
		newH -= insets.top + insets.bottom + 1;

		// Update anchor based on whether or not this is an input/output field
		final int pad = newH - 1;
		double x = insets.left;
		double y = insets.top + (pad + 5.0) / 6;
		double w = (2.0*pad) / 3;
		double h = (2.0*pad) / 3;

		if(field instanceof InputField) {
			name.setBounds((int)(x + w), insets.top, (int)(newW - 2*x - w), newH);
		} else if(field instanceof OutputField) {
			x += newW - w;
			name.setBounds(insets.left, insets.right, (int)(newW - w - 3), newH);
		}

		anchor.setFrame(x, y, w, h);
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension textPref = name.getPreferredSize();
		final int anchorSize = textPref.height;
		return new Dimension(textPref.width + anchorSize + 2, textPref.height);
	}

	@Override
	protected void paintComponent(Graphics gfx) {
		super.paintComponent(gfx);

		Graphics2D g = (Graphics2D)gfx;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

		switch(anchorFillState) {
		case LINK:
			g.setColor(style.AnchorLinkFillColor);
			g.fill(anchor);
			break;
		case DEFAULT:
			g.setColor(style.AnchorDefaultFillColor);
			g.fill(anchor);
			break;
		case PUBLISHED:
			g.setColor(style.AnchorPublishedFillColor);
			g.fill(anchor);
			break;
		case NONE:
			break;
		}

		g.setColor(style.FieldsTextColor);
		if((field instanceof InputField) && ((InputField)field).isOptional()) {
			final Stroke oldStroke = g.getStroke();
			g.setStroke(optionalFieldStroke);
			g.draw(anchor);
			g.setStroke(oldStroke);
		} else {
			g.draw(anchor);
		}
	}

	//
	// MouseAdapter
	//

	private final MouseAdapter mouseAdapter = new MouseAdapter() {
		@Override
		public void mouseExited(MouseEvent e) {
			Component parentCanvas = SwingUtilities.getAncestorOfClass(GraphCanvas.class, CanvasNodeField.this);
			if(parentCanvas != null)
				parentCanvas.setCursor(null);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			GraphCanvas parentCanvas = (GraphCanvas)SwingUtilities.getAncestorOfClass(GraphCanvas.class, CanvasNodeField.this);
			if(parentCanvas != null) {
				if(anchor.contains(e.getPoint()))
					parentCanvas.startLinkDrag(CanvasNodeField.this);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			GraphCanvas parentCanvas = (GraphCanvas)SwingUtilities.getAncestorOfClass(GraphCanvas.class, CanvasNodeField.this);
			if(parentCanvas != null)
				parentCanvas.endLinkDrag(SwingUtilities.convertPoint(CanvasNodeField.this, e.getPoint(), parentCanvas));
		}
	};

	//
	// MouseMotionAdapter
	//

	private final MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter() {
		@Override
		public void mouseDragged(MouseEvent e) {
			GraphCanvas parentCanvas = (GraphCanvas)SwingUtilities.getAncestorOfClass(GraphCanvas.class, CanvasNodeField.this);
			if(parentCanvas != null)
				parentCanvas.updateLinkDrag(SwingUtilities.convertPoint(CanvasNodeField.this, e.getPoint(), parentCanvas));
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			Component parentCanvas = SwingUtilities.getAncestorOfClass(GraphCanvas.class, CanvasNodeField.this);
			if(parentCanvas != null) {
				if(anchor.contains(e.getPoint())) {
					parentCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else {
					parentCanvas.setCursor(null);
				}
			}
		}
	};
}
