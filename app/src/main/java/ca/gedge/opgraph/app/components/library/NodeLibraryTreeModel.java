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
package ca.gedge.opgraph.app.components.library;

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.library.NodeLibrary;
import ca.gedge.opgraph.library.NodeLibraryListener;

/**
 * A tree model for viewing a node library. 
 */
class NodeLibraryTreeModel
	extends DefaultTreeModel
	implements NodeLibraryListener
{
	/** The node library */
	private NodeLibrary library;

	/** The filter being used */
	private NodeInfoFilter filter;

	/**
	 * Constructs a model that references a specified library.
	 * 
	 * @param library  the library
	 */
	public NodeLibraryTreeModel(NodeLibrary library) {
		this(library, null);
	}

	/**
	 * Constructs a model that references a specified library and filters
	 * nodes based on a given string.
	 * 
	 * @param library  the library
	 * @param filter  the filter string
	 */
	public NodeLibraryTreeModel(NodeLibrary library, NodeInfoFilter filter) {
		super(new DefaultMutableTreeNode());

		this.library = library;
		this.filter = filter;

		if(this.library != null) {
			for(NodeData nodeData : this.library)
				nodeRegistered(nodeData);
		}
	}

	/**
	 * Gets whether or not a given node matches the filter.
	 * 
	 * @param node  the node to check
	 * 
	 * @return <code>true</code> if the node fits the filter pattern,
	 *         <code>false</code> otherwise
	 */
	public boolean matches(DefaultMutableTreeNode node) {
		boolean ret = true;

		if(filter != null) {
			final Object obj = node.getUserObject();
			if(obj != null && (obj instanceof NodeData))
				ret = filter.isAccepted((NodeData)obj);
		}

		return ret;
	}

	/**
	 * Gets whether or not a node contains a child that matches the filter.
	 * 
	 * @param node  the node to begin searching from
	 * 
	 * @return <code>true</code> if the given node contains a child that
	 *         matches the filter, <code>false</code> otherwise
	 */
	public boolean containsMatchingChild(DefaultMutableTreeNode node) {
		final Enumeration<?> e = node.breadthFirstEnumeration();
		while(e.hasMoreElements()) {
			final Object unknownNode = e.nextElement();
			if(unknownNode instanceof DefaultMutableTreeNode) {
				if(matches( (DefaultMutableTreeNode)unknownNode ))
					return true;
			}
		}

		return false;
	}

	/**
	 * Gets the child index for a given category. If a node does not exist
	 * for the category, one is created.
	 * 
	 * @param category  the category
	 * 
	 * @return the node associated with the given category
	 */
	private DefaultMutableTreeNode getNodeForCategory(String category) {
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode)getRoot(); 

		DefaultMutableTreeNode ret = null;

		int index = 0;
		for(; index < root.getChildCount(); ++index) {
			final DefaultMutableTreeNode node = (DefaultMutableTreeNode)root.getChildAt(index);
			int comp = category.compareTo(node.getUserObject().toString());
			if(comp == 0) {
				ret = node;
				break;
			} else if(comp == 1) {
				break;
			}
		}

		if(ret == null) {
			ret = new DefaultMutableTreeNode(category);
			insertNodeInto(ret, (DefaultMutableTreeNode)getRoot(), index);
		}

		return ret;
	}

	//
	// NodeLibraryListener 
	//

	@Override
	public void nodeRegistered(NodeData info) {
		final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(info, false);
		if(matches(newNode)) {
			final String category = (info.category.length() == 0 ? "General" : info.category);
			final DefaultMutableTreeNode node = getNodeForCategory(category);
			if(node != null)
				node.add(newNode);
		}
	}

	@Override
	public void nodeUnregistered(NodeData info) {
		final String category = (info.category.length() == 0 ? "General" : info.category);

		DefaultMutableTreeNode node = getNodeForCategory(category);
		if(node != null) {
			for(int index = 0; index < node.getChildCount(); ++index) {
				final DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(index);
				if(child.getUserObject() == info) {
					child.removeFromParent();
					nodesWereRemoved(node, new int[]{index}, new Object[]{child});
					break;
				}
			}

			// Now that we've unregistered the node, recurse up through ancestors
			// and remove them from their parents if they contain no children
			while(node != null && node.getChildCount() == 0) {
				final TreeNode parent = node.getParent();
				if(parent != null) {
					node.removeFromParent();
					nodesWereRemoved(node.getParent(), new int[]{parent.getIndex(node)}, new Object[]{node});
				}

				if(node instanceof DefaultMutableTreeNode)
					node = (DefaultMutableTreeNode)parent;
				else
					break;
			}
		}
	}
}
