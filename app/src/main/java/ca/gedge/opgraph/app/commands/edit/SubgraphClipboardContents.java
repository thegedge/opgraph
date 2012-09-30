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
package ca.gedge.opgraph.app.commands.edit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.io.OpGraphSerializer;
import ca.gedge.opgraph.io.OpGraphSerializerFactory;
import ca.gedge.opgraph.util.Pair;

/**
 * Inner class for handling OpGraph clipboard content.
 */
class SubgraphClipboardContents implements Transferable {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(SubgraphClipboardContents.class.getName());
	
	/** Clipboard data flavor */
	public static final DataFlavor copyFlavor = new DataFlavor(SubgraphClipboardContents.class, "SubgraphClipboardContents");
	
	/** The document containing the graph to copy from */
	public final GraphDocument document;
	
	/** The copied sub-graph */
	public final OpGraph subGraph;
	
	/** Mapping from graph to the number of times it has been pasted into each graph */
	public final Map<OpGraph, Integer> graphDuplicates = new HashMap<OpGraph, Integer>();
	
	/** Cached data values */
	private final Map<DataFlavor, Object> cachedData = new HashMap<DataFlavor, Object>();

	/**
	 * Constructs a clipboard contents for a given document and the subgraph which is being copied. 
	 * 
	 * @param document  the document containing the graph 
	 * @param selectedGraph  the subgraph which is being copied
	 */
	public SubgraphClipboardContents(GraphDocument document, OpGraph selectedGraph) {
		this.document = document;
		this.subGraph = selectedGraph;
		this.graphDuplicates.put(document.getGraph(), new Integer(0));
	}
	
	/**
	 * Gets the bounding rectangle of a given set of nodes.
	 * 
	 * @return the bounding rectangle of the given collection of nodes
	 */
	private Rectangle getBoundingRect(Collection<OpNode> nodes) {
		int xmin = Integer.MAX_VALUE;
		int xmax = Integer.MIN_VALUE;
		int ymin = Integer.MAX_VALUE;
		int ymax = Integer.MIN_VALUE;

		for(OpNode node : nodes) {
			final JComponent comp = node.getExtension(JComponent.class);
			if(comp != null) {
				final Dimension pref = comp.getPreferredSize();
				xmin = Math.min(xmin, comp.getX());
				xmax = Math.max(xmax, comp.getX() + pref.width);
				ymin = Math.min(ymin, comp.getY());
				ymax = Math.max(ymax, comp.getY() + pref.height);
			}
		}
		
		return new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
	}
	
	//
	// Transferable overrides
	//
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		// First, check to see if we've cached a value for this data flavor 
		Object retVal = cachedData.get(flavor);
		
		if(retVal == null) {
			if(flavor == copyFlavor) {
				retVal = this;
			} else if(flavor == DataFlavor.imageFlavor) {
				// XXX create a temp graph of the selected nodes and links?
				final Rectangle boundRect = getBoundingRect(subGraph.getVertices());
				if(boundRect.width > 0 && boundRect.height > 0) {
					final Dimension fullSize = new Dimension(boundRect.x + boundRect.width, boundRect.y + boundRect.height);
					final BufferedImage img = new BufferedImage(boundRect.width, boundRect.height, BufferedImage.TYPE_INT_ARGB);
					final Graphics2D g = (Graphics2D)img.getGraphics();
					final Collection<OpNode> currentSelection = document.getSelectionModel().getSelectedNodes();
					
					// Set clip and paint into temp buffer
					final AffineTransform transform = AffineTransform.getTranslateInstance(-boundRect.getX(), -boundRect.getY());
					g.setColor(new Color(255,255,255,0));
					g.fill(new Rectangle(0, 0, fullSize.width, fullSize.height));
					g.setTransform(transform);
					g.setClip(boundRect);
					
					// Paint to graphics (without selection)
					document.getSelectionModel().setSelectedNodes(null);
					document.getCanvas().paint(g);
					document.getSelectionModel().setSelectedNodes(currentSelection);
					
					// Create the image
					//final ByteArrayOutputStream bout = new ByteArrayOutputStream();
					//ImageIO.write(img, "png", bout);
					//retVal = Toolkit.getDefaultToolkit().createImage(bout.toByteArray());
					retVal = img;
				}
			} else if(flavor == DataFlavor.stringFlavor) {
				// Store XML data for string flavor
				final OpGraphSerializer serializer = OpGraphSerializerFactory.getDefaultSerializer();
				if(serializer == null) {
					// XXX Consider just spitting out the default java serializer byte data?
					LOGGER.severe("No default serializer available");
					return false;
				}
				
				// Write XML to byte stream
				final ByteArrayOutputStream bout = new ByteArrayOutputStream();
				try {
					serializer.write(subGraph, bout);
					final String graphString = bout.toString("UTF-8");
					retVal = graphString;
				} catch(IOException e) {
					LOGGER.severe(e.getMessage());
					retVal = "";
				}
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
			
			if(retVal != null)
				cachedData.put(flavor, retVal);
		}
		
		return retVal;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{copyFlavor, DataFlavor.imageFlavor, DataFlavor.stringFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.equals(copyFlavor) || flavor.equals(DataFlavor.imageFlavor) || flavor.equals(DataFlavor.stringFlavor));
	}
	
}
