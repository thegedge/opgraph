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
import java.util.HashMap;
import java.util.Map;

import javax.swing.border.Border;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.extensions.CompositeNode;

/**
 * A class containing styling information for a node.
 * 
 * TODO perhaps move this over to javax.swing.UIManager
 */
public class NodeStyle {
	private static final NodeStyle DEFAULT = new NodeStyle();
	private static final NodeStyle COMPOSITE = new NodeStyle();
	private static final Map<Class<? extends OpNode>, NodeStyle> installedStyles;
	
	static {
		DEFAULT.NodeBorderColor = Color.GRAY;
		DEFAULT.NodeBackgroundColor = new Color(255, 255, 255, 200);
		DEFAULT.NodeFocusColor = new Color(255, 200, 0, 255);
		DEFAULT.NodeNameTextColor = Color.BLACK;
		DEFAULT.NodeNameTextShadowColor = Color.LIGHT_GRAY;
		DEFAULT.NodeNameTopColor = new Color(200, 200, 200, 255);
		DEFAULT.NodeNameBottomColor = new Color(150, 150, 150, 255);
		DEFAULT.FieldsTextColor = Color.BLACK;
		DEFAULT.AnchorLinkFillColor = Color.ORANGE;
		DEFAULT.AnchorDefaultFillColor = new Color(100, 150, 255, 100);
		DEFAULT.AnchorPublishedFillColor = new Color(50, 255, 50, 150);

		COMPOSITE.NodeBorderColor = new Color(100, 155, 100);
		COMPOSITE.NodeBackgroundColor = new Color(200, 255, 200, 200);
		COMPOSITE.NodeFocusColor = new Color(255, 200, 0, 255);
		COMPOSITE.NodeNameTextColor = Color.WHITE;
		COMPOSITE.NodeNameTextShadowColor = Color.DARK_GRAY;
		COMPOSITE.NodeNameTopColor = new Color(150, 200, 100, 255);
		COMPOSITE.NodeNameBottomColor = new Color(100, 150, 50, 255);
		COMPOSITE.FieldsTextColor = Color.BLACK;
		COMPOSITE.AnchorLinkFillColor = Color.ORANGE;
		COMPOSITE.AnchorDefaultFillColor = new Color(100, 150, 255, 100);
		COMPOSITE.AnchorPublishedFillColor = new Color(50, 255, 50, 150);

		installedStyles = new HashMap<Class<? extends OpNode>, NodeStyle>();
		installedStyles.put(OpNode.class, DEFAULT);
	}
	
	/**
	 * Installs a node style for a specfied {@link OpNode} class.
	 * 
	 * @param cls  the {@link OpNode} class to register the style for
	 * @param style  the node style
	 */
	public static void installStyleForNode(Class<? extends OpNode> cls, NodeStyle style) {
		installedStyles.put(cls, style);
	}
	
	/**
	 * Gets the node style for a given node.
	 * 
	 * @param node  the node
	 * 
	 * @return the node style for the given node, or the default style if no node
	 *         style is installed for the given node class (or any of its superclasses)
	 */
	public static NodeStyle getStyleForNode(OpNode node) {
		if(node != null) {
			// CompositeNode extension is fixed
			if(node.getExtension(CompositeNode.class) != null)
				return COMPOSITE;
			
			// Go through superclasses to see if we can find something
			Class<?> cls = node.getClass();
			while(cls != null) {
				if(installedStyles.containsKey(cls))
					return installedStyles.get(cls);
				
				cls = cls.getSuperclass();
			}
		}
		return DEFAULT;
	}
	
	/** The top color for the background of the node name section */ 
	public Color NodeNameTopColor = Color.WHITE;
	
	/** The bottom color for the background of the node name section */ 
	public Color NodeNameBottomColor = Color.WHITE;
	
	/** The color for the node name text */
	public Color NodeNameTextColor = Color.BLACK;
	
	/** The color for the node name text shadow */
	public Color NodeNameTextShadowColor = Color.LIGHT_GRAY;
	
	/** The color for the node's bg */
	public Color NodeBackgroundColor = Color.WHITE;
	
	/** The color for the node's border */
	public Color NodeBorderColor = Color.BLACK;
	
	/** The color for the node's focus ring */
	public Color NodeFocusColor = Color.WHITE;
	
	/** The color for the node's input/output fields */
	public Color FieldsTextColor = Color.BLACK;
	
	/** The color for the fill in the anchor points for links when an link is attached */
	public Color AnchorLinkFillColor = Color.GRAY;
	
	/** The color for the fill in the anchor points for links when a default value is available */
	public Color AnchorDefaultFillColor = Color.GRAY;
	
	/** The color for the fill in the anchor points for links when it is a published input/output in a macro */
	public Color AnchorPublishedFillColor = Color.GRAY;
	
	/** The border used for rendering a node. */
	public Border NodeBorder = new DefaultNodeBorder();
	
	/** Whether or not to show the enabled field of a node. */
	public boolean ShowEnabledField = true;
}
