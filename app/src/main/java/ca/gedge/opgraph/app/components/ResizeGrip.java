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
package ca.gedge.opgraph.app.components;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

/**
 * A grip component that will resize a given component when dragged over.
 */
public class ResizeGrip extends JComponent {
	/** The component whose size this resize grip will control */
	private Component component;

	/** The size of the grip */
	private Dimension size;

	/** The size of the component before this grip was clicked */
	private Dimension initialComponentSize;

	/**
	 * Constructs a default resize grip component.
	 * 
	 * @param component  the component which will be resized by this grip
	 */
	public ResizeGrip(Component component) {
		this(component, 10, 10);
	}

	/**
	 * Construct s a resize grip component with a given size.
	 * 
	 * @param component  the component which will be resized by this grip
	 * @param w  the width of the grip
	 * @param h  the height of the grip
	 */
	public ResizeGrip(Component component, int w, int h) {
		this.component = component;
		this.size = new Dimension(w, h);
		this.initialComponentSize = component.getSize();

		setOpaque(true);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}

	/**
	 * Gets the component this grip resizes.
	 * 
	 * @return the component
	 */
	public Component getComponent() {
		return component;
	}

	/**
	 * Gets the size of the component before this grip was clicked.
	 * 
	 * @return the size of the component
	 */
	public Dimension getInitialComponentSize() {
		return initialComponentSize;
	}

	//
	// Event adapters
	//

	private final MouseAdapter mouseAdapter = new MouseAdapter() {
		/** The initial click point on screen */
		private Point initialLocationOnScreen;

		@Override
		public void mouseDragged(MouseEvent e) {
			final Point p = e.getLocationOnScreen();
			if(initialLocationOnScreen != null) {
				final int dx = p.x - initialLocationOnScreen.x;
				final int dy = p.y - initialLocationOnScreen.y;
				if(component != null) {
					final Dimension dim = component.getMinimumSize();
					dim.width = Math.max(dim.width, initialComponentSize.width + dx);
					dim.height = Math.max(dim.height, initialComponentSize.height + dy);
					component.setPreferredSize(dim);
					component.invalidate();
					revalidate();
				}
			}	
		}

		@Override
		public void mousePressed(MouseEvent e) {
			initialComponentSize = component.getSize();
			initialLocationOnScreen = e.getLocationOnScreen();
		}
	};

	//
	// Overrides
	//

	@Override
	public Dimension getPreferredSize() {
		return size;
	}

	@Override
	protected void paintComponent(Graphics gfx) {
		super.paintComponent(gfx);

		final Graphics2D g = (Graphics2D)gfx;
		final int w = getWidth();
		final int h = getHeight();

		g.setColor(getBackground());
		g.setStroke(new BasicStroke(1.5f));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawLine(0, h - 1, w - 1, 0);
		g.drawLine((w - 1) / 2, h - 1, w - 1, (h - 1) / 2);
		g.drawLine(w - 2, h - 1, w - 1, h - 2);
	}
}
