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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

/**
 */
public class DefaultNodeBorder implements Border {
	static final int MAX_SIZE = 5;
	
	@Override
	public Insets getBorderInsets(Component c) {
		return new Insets(MAX_SIZE, MAX_SIZE, MAX_SIZE, MAX_SIZE);
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
		if(c instanceof CanvasNode) {
			final CanvasNode canvasNode = (CanvasNode)c;
			if(canvasNode.isSelected()) {
				g.setColor(canvasNode.getStyle().NodeFocusColor);
				g.fillRect(x, y, w, MAX_SIZE);
				g.fillRect(x, y + MAX_SIZE, MAX_SIZE, h - MAX_SIZE);
				g.fillRect(w - MAX_SIZE, y + MAX_SIZE, MAX_SIZE, h - MAX_SIZE);
				g.fillRect(x + MAX_SIZE, h - MAX_SIZE, w - 2*MAX_SIZE, MAX_SIZE);
			}
			
			x += MAX_SIZE - 1;
			y += MAX_SIZE - 1;
			w -= 2*MAX_SIZE - 1;
			h -= 2*MAX_SIZE - 1;
			
			g.setColor(canvasNode.getStyle().NodeBorderColor);
			g.drawRect(x, y, w, h);
		}
	}

}
