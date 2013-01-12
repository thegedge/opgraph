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
package ca.gedge.opgraph.app.components.canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.app.util.GUIHelper;

/**
 * A full-canvas component to draw debug things on top of everything else.
 */
class DebugOverlay extends JComponent {
	/** Dark mask for shaded nodes */
	static final Paint DARK_MASK;

	/** Light mask for shaded nodes */
	static final Paint LIGHT_MASK;

	/** Mask for error node */
	static final Paint ERROR_MASK;

	static {
		final int W = 4;
		final int H = 4;

		// Creates a crosshatch texture
		final BufferedImage texture = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
		{
			final Graphics2D g = texture.createGraphics();
			g.setColor(new Color(0, 0, 0, 200));
			g.drawLine(0, 0, W - 1, H - 1);
			g.drawLine(0, H - 1, W - 1, 0);
		}

		DARK_MASK = new TexturePaint(texture, new Rectangle2D.Double(0, 0, W, H));
		LIGHT_MASK = new Color(0, 0, 0, 127);
		ERROR_MASK = new Color(255, 0, 0, 50);
	}

	/** The parent canvas */
	private final GraphCanvas canvas;

	/**
	 * Default constructor.
	 * 
	 * @param canvas  the parent canvas
	 */
	public DebugOverlay(GraphCanvas canvas) {
		this.canvas = canvas;

		setOpaque(false);
		setBackground(null);
	}

	@Override
	public Dimension getPreferredSize() {
		return null;
	}

	@Override
	protected void paintComponent(Graphics gfx) {
		final Processor context = canvas.getDocument().getProcessingContext();
		if(context != null) {
			// Get the context which is operating on the graph this
			// canvas is currently viewing
			Processor activeContext = context;
			while(activeContext.getMacroContext() != null && activeContext.getGraphOfContext() != canvas.getDocument().getGraph())
				activeContext = activeContext.getMacroContext();

			// If the current graph has an associated processing context
			if(activeContext != null) {
				// Find directly connected nodes
				final Graphics2D g = (Graphics2D)gfx;
				final IdentityHashMap<CanvasNode, Boolean> connectedNodes = new IdentityHashMap<CanvasNode, Boolean>();
				final OpNode currentNode = activeContext.getCurrentNodeOfContext();

				if(currentNode != null) {
					for(OpLink link : canvas.getDocument().getGraph().getIncomingEdges(currentNode))
						connectedNodes.put( canvas.getNode(link.getSource()), true );

					for(OpLink link : canvas.getDocument().getGraph().getOutgoingEdges(currentNode))
						connectedNodes.put( canvas.getNode(link.getDestination()), true );
				}

				// Draw masks over nodes
				final Paint oldPaint = g.getPaint();
				for(OpNode node : canvas.getDocument().getGraph().getVertices()) {
					final CanvasNode canvasNode = canvas.getNode(node);
					final Rectangle bounds = SwingUtilities.convertRectangle(canvasNode, GUIHelper.getInterior(canvasNode), this);

					--bounds.x;
					--bounds.y;
					bounds.width += 2;
					bounds.height += 2;

					if(node == currentNode) {
						if(context.getError() != null) {
							g.setPaint(ERROR_MASK);
							g.fill(bounds);
						}
					} else {
						g.setPaint(LIGHT_MASK);
						g.fill(bounds);
						if(!connectedNodes.containsKey(canvasNode)) {
							g.setPaint(DARK_MASK);
							g.fill(bounds);
						}
					}
				}

				g.setPaint(oldPaint);
			}
		}
	}
}
