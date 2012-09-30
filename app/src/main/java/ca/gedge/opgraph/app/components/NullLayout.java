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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 * A special version of a <code>null</code> layout which takes care of
 * resizing children to their preferred size, and computing sizing
 * preferences based on the children.
 */
public class NullLayout implements LayoutManager {
	@Override
	public void layoutContainer(Container parent) {
		for(Component comp : parent.getComponents()) {
			Dimension dim = comp.getPreferredSize();
			if(dim != null) {
				final Dimension min = comp.getMinimumSize();
				if(min != null) {
					dim.width = Math.max(min.width, dim.width);
					dim.height = Math.max(min.height, dim.height);
				}
					
				final Dimension max = comp.getMaximumSize();
				if(max != null) {
					dim.width = Math.min(max.width, dim.width);
					dim.height = Math.min(max.height, dim.height);
				}
			} else {
				dim = parent.getSize();
			}
			
			comp.setSize(dim);
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}
	
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int maxX = 0;
		int maxY = 0;
		for(Component comp : parent.getComponents()) {
			final Dimension dim = comp.getPreferredSize();
			if(dim != null) {
				final Dimension min = comp.getMinimumSize();
				if(min != null) {
					dim.width = Math.max(min.width, dim.width);
					dim.height = Math.max(min.height, dim.height);
				}
					
				final Dimension max = comp.getMaximumSize();
				if(max != null) {
					dim.width = Math.min(max.width, dim.width);
					dim.height = Math.min(max.height, dim.height);
				}
				
				maxX = Math.max(maxX, comp.getX() + dim.width);
				maxY = Math.max(maxY, comp.getY() + dim.height);
			}
		}
		return new Dimension(maxX, maxY);
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {}
	
	@Override
	public void removeLayoutComponent(Component comp) {}
}
