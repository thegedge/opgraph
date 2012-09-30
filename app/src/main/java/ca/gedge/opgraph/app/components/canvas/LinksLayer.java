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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpNode;

/**
 * A full-canvas component to draw links between node fields
 */
public class LinksLayer extends JComponent {
	/** Thin stroke for drawing links */
	static final Stroke THIN = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
	
	/** Thick stroke for drawing links */
	static final Stroke THICK = new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
	
	/** The mapping of links to link shapes */
	private TreeMap<OpLink, Shape> links;
	
	/** The canvas parent */
	private GraphCanvas canvas;
	
	/**
	 * Default constructor.
	 * 
	 * @param canvas  the parent canvas
	 */
	public LinksLayer(GraphCanvas canvas) {
		this.canvas = canvas;
		this.links = new TreeMap<OpLink, Shape>();
		
		setOpaque(false);
		setBackground(null);
	}

	/**
	 * Creates a smooth curve between two points.
	 * 
	 * @param p1  a point
	 * @param p2  a point
	 * 
	 * @return the path representing the smooth curve
	 */
	public static Path2D createSmoothLink(Point p1, Point p2) {
		final double cx1 = (p1.x + p2.x) * 0.5;
		final double cy1 = p1.y;
		final double cx2 = (p1.x + p2.x) * 0.5;
		final double cy2 = p2.y;
		
		final Path2D link = new Path2D.Double();
		link.moveTo(p1.x, p1.y);
		link.curveTo(cx1, cy1, cx2, cy2, p2.x, p2.y);
		return link;
	}
	
	/**
	 * Updates the path shape for a given link.
	 * 
	 * @param link  the link whose path should be updated
	 */
	void updateLink(OpLink link) {
		final CanvasNode source = canvas.getNode(link.getSource());
		final CanvasNode dest = canvas.getNode(link.getDestination());
		if(source != null && dest != null) {
			final CanvasNodeField sourceField = source.getFieldsMap().get(link.getSourceField());
			final CanvasNodeField destField = dest.getFieldsMap().get(link.getDestinationField());
			if(sourceField != null && destField != null) {
				// Get the anchoring points
				final Ellipse2D sourceAnchor = sourceField.getAnchor();
				final Ellipse2D destAnchor = destField.getAnchor();
				
				// Convert to our coordinate system
				Point sourceLoc = new Point((int)sourceAnchor.getCenterX(), (int)sourceAnchor.getCenterY());
				Point destLoc = new Point((int)destAnchor.getCenterX(), (int)destAnchor.getCenterY());
				sourceLoc = SwingUtilities.convertPoint(sourceField, sourceLoc, canvas);
				destLoc = SwingUtilities.convertPoint(destField, destLoc, canvas);
				
				links.put(link, createSmoothLink(sourceLoc, destLoc));
				
				repaint();
			}
		}
	}

	/**
	 * Removes the path shape from a given link.
	 * 
	 * @param link  the link whose path should be removed
	 */
	void removeLink(OpLink link) {
		if(links.containsKey(link)) {
			links.remove(link);
			repaint();
		}
	}

	/**
	 * Removes path shapes for all links.
	 * 
	 * @param link  the link whose path should be removed
	 */
	void removeAllLinks() {
		links.clear();
		repaint();
	}

	//
	// Overrides
	//
	
	@Override
	public Dimension getPreferredSize() {
		return null;
	}
	
	@Override
	protected void paintComponent(Graphics gfx) {
		Graphics2D g = (Graphics2D)gfx;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		// Draw links between nodes
		final Stroke oldStroke = g.getStroke();
		if(canvas.getDocument().getProcessingContext() != null) {
			// Find links connected to the current node
			final IdentityHashMap<OpLink, Boolean> connectedLinks = new IdentityHashMap<OpLink, Boolean>();
			final OpNode currentNode = canvas.getDocument().getProcessingContext().getCurrentNode();
			
			for(OpLink link : canvas.getDocument().getGraph().getIncomingEdges(currentNode))
				connectedLinks.put(link, true);
			
			for(OpLink link : canvas.getDocument().getGraph().getOutgoingEdges(currentNode))
				connectedLinks.put(link, true);
			
			// We're debugging, so draw things faded out
			final Color SELECTED_FILL = Color.ORANGE;
			final Color REGULAR_FILL = Color.ORANGE.darker().darker();

			// Draw links
			for(Map.Entry<OpLink, Shape> link : links.entrySet()) {
				Color strokeColor = Color.BLACK;
				Color fillColor = REGULAR_FILL;
				if(connectedLinks.containsKey(link.getKey()))
					fillColor = SELECTED_FILL;
				
				// Link fill
				g.setColor(fillColor);
				g.setStroke(THIN);
				g.draw(link.getValue());
				
				// Link outline
				g.setColor(strokeColor);
				g.setStroke(oldStroke);
				g.draw(THICK.createStrokedShape(link.getValue()));
			}
		} else {
			// Get incoming/outgoing links of selected nodes
			final TreeSet<OpLink> selectedLinks = new TreeSet<OpLink>();
			for(OpNode node : canvas.getSelectionModel().getSelectedNodes()) {
				for(OpLink link : canvas.getDocument().getGraph().getIncomingEdges(node))
					selectedLinks.add(link);
				
				for(OpLink link : canvas.getDocument().getGraph().getOutgoingEdges(node))
					selectedLinks.add(link);
			}
			
			// Draw links
			for(Map.Entry<OpLink, Shape> link : links.entrySet()) {
				Color strokeColor = Color.BLACK;
				Color fillColor = Color.ORANGE;
				if(link.getKey() == canvas.getCurrentlyDraggedLink()) {
					strokeColor = new Color(0, 0, 0, 50);
					fillColor = new Color(255, 165, 0, 50);
				} else if(selectedLinks.contains(link.getKey())) {
					fillColor = Color.GREEN;
				}
				
				// Link fill
				g.setColor(fillColor);
				g.setStroke(THIN);
				g.draw(link.getValue());
				
				// Link outline
				g.setColor(strokeColor);
				g.setStroke(oldStroke);
				g.draw(THICK.createStrokedShape(link.getValue()));
			}
		}
		
		g.setStroke(oldStroke);
	}
}
