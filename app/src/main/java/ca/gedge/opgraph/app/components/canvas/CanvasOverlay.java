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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * A full-canvas component to draw things on top of everything else. This
 * currently includes:
 * <ul>
 *   <li>the link currently being edited, and<li>
 *   <li>the selection rectangle</li>
 * </ul>
 */
public class CanvasOverlay extends JComponent {
	/** The parent canvas */
	private final GraphCanvas canvas;

	/**
	 * Default constructor.
	 * 
	 * @param canvas  the parent canvas.
	 */
	public CanvasOverlay(GraphCanvas canvas) {
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
		Graphics2D g = (Graphics2D)gfx;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		// The drag link
		if(canvas.getCurrentlyDraggedLinkInputField() != null) {
			Ellipse2D anchor = canvas.getCurrentlyDraggedLinkInputField().getAnchor();
			Point p = new Point((int)anchor.getCenterX(), (int)anchor.getCenterY());
			p = SwingUtilities.convertPoint(canvas.getCurrentlyDraggedLinkInputField(), p, canvas);
			
			final Shape link = LinksLayer.createSmoothLink(p, canvas.getCurrentDragLinkLocation());
			final Stroke oldStroke = g.getStroke();
			
			if(link != null) {
				g.setColor(canvas.isDragLinkValid() ? Color.WHITE : Color.RED);
				g.setStroke(LinksLayer.THIN);
				g.draw(link);
				
				g.setColor(Color.BLACK);
				g.setStroke(oldStroke);
				g.draw(LinksLayer.THICK.createStrokedShape(link));
			}
		}
		
		// the selection rect
		final Rectangle selectionRect = canvas.getSelectionRect();
		if(selectionRect != null) {
			int x = selectionRect.x;
			int y = selectionRect.y;
			int w = selectionRect.width;
			int h = selectionRect.height;
			
			if(w < 0) {
				x += w;
				w = -w;
			}
			
			if(h < 0) {
				y += h;
				h = -h;
			}
			
			g.setColor(new Color(255, 255, 255, 50));
			g.fillRect(x, y, w, h);
			g.setColor(Color.WHITE);
			g.drawRect(x, y, w, h);
		}
	}
}
