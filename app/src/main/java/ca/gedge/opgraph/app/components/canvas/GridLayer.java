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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JComponent;

import ca.gedge.opgraph.app.util.GUIHelper;

/**
 * A grid component intended for displaying on the background of a
 * {@link GraphCanvas} component.
 */
public class GridLayer extends JComponent {
	/** Grid line spacing */
	public static final int DEFAULT_GRID_SPACING = 50;

	/** Snap distance */
	public static final int DEFAULT_SNAP_DISTANCE = 5;

	/**
	 * Constructs a viewport for the specified canvas.
	 */
	public GridLayer() {
		setOpaque(true);
		setBackground(Color.DARK_GRAY);
	}

	/**
	 * Snaps a point to this grid.
	 * 
	 * @param p  the point which will be snapped
	 * 
	 * @return the delta to apply to the given point to enforce snapping   
	 */
	public Point snap(Point p) {
		final int mx = p.x % DEFAULT_GRID_SPACING;
		final int my = p.y % DEFAULT_GRID_SPACING;
		final Point snapped = new Point();

		if(Math.abs(mx) <= DEFAULT_SNAP_DISTANCE) {
			snapped.x = -mx;
		} else if(Math.abs(DEFAULT_GRID_SPACING - mx) <= DEFAULT_SNAP_DISTANCE) {
			snapped.x = DEFAULT_GRID_SPACING - mx;
		}

		if(Math.abs(my) <= DEFAULT_SNAP_DISTANCE) {
			snapped.y = -my;
		} else if(Math.abs(DEFAULT_GRID_SPACING - my) <= DEFAULT_SNAP_DISTANCE) {
			snapped.y = DEFAULT_GRID_SPACING - my;
		}

		return snapped;
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
		//g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		super.paintComponent(g);

		// Fill background
		g.setColor(getBackground());
		g.fill(getVisibleRect());

		// Draw grid lines
		final Rectangle view = getVisibleRect();
		final int startx = ((view.x / DEFAULT_GRID_SPACING - 1) * DEFAULT_GRID_SPACING); 
		final int starty = ((view.y / DEFAULT_GRID_SPACING - 1) * DEFAULT_GRID_SPACING); 
		final int endx = view.x + view.width + 1;
		final int endy = view.y + view.height + 1;

		g.setColor(GUIHelper.highlightColor(getBackground()));
		for(int y = starty; y < endy; y += DEFAULT_GRID_SPACING)
			g.drawLine(view.x, y, endx, y);

		for(int x = startx; x < endx; x += DEFAULT_GRID_SPACING)
			g.drawLine(x, view.y, x, endy);
	}
}
