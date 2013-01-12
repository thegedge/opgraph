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
package ca.gedge.opgraph.app.components.library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.components.ErrorDialog;
import ca.gedge.opgraph.app.components.SearchField;
import ca.gedge.opgraph.app.edits.graph.AddNodeEdit;
import ca.gedge.opgraph.app.util.ObjectSelection;
import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.library.NodeLibrary;
import ca.gedge.opgraph.library.handlers.ClassHandler;
import ca.gedge.opgraph.util.ServiceDiscovery;

/**
 * A panel to display the node types available in a {@link NodeLibrary}.
 */
public class NodeLibraryViewer extends JPanel {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(NodeLibraryViewer.class.getName());

	/** The library this panel is viewing */
	private NodeLibrary library;

	/** The search field */
	private SearchField filterField;

	/** The list of nodes in the library */
	private JTree libraryTree;

	/** The tree model being used */
	private NodeLibraryTreeModel model;

	/** Tree renderer being used */
	private DefaultTreeCellRenderer renderer;

	/** A component showing information on the selected item */
	private JEditorPane infoPane;

	/** A list of the rows that are expanded */
	private HashSet<String> expandedCategories;

	/**
	 * An action and action listener that updates the filter of this node
	 * library viewer.
	 */
	private class UpdateFilterAction implements ActionListener {
		private NodeInfoFilter filter;

		/**
		 * 
		 */
		public UpdateFilterAction() {
			this.filter = new NodeInfoFullTextFilter();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e != null) {
				// Determine what command we have
				if("fulltext".equals(e.getActionCommand())) {
					this.filter = new NodeInfoFullTextFilter();
					this.filter.setFilter(filterField.getText());
				} else if("name".equals(e.getActionCommand())) {
					this.filter = new NodeInfoNameFilter();
					this.filter.setFilter(filterField.getText());
				} else if("description".equals(e.getActionCommand())) {
					this.filter = new NodeInfoDescriptionFilter();
					this.filter.setFilter(filterField.getText());
				} else if("category".equals(e.getActionCommand())) {
					this.filter = new NodeInfoCategoryFilter();
					this.filter.setFilter(filterField.getText());
				} else if("filter".equals(e.getActionCommand())) {
					this.filter.setFilter(filterField.getText());
				}

				// Update model
				final NodeLibrary library = NodeLibraryViewer.this.library;

				model = new NodeLibraryTreeModel(library, filter);
				libraryTree.setModel(model);

				final int count = model.getChildCount(model.getRoot());
				for(int index = 0; index < count; ++index) {
					final Object obj = model.getChild(model.getRoot(), index);
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
					if(expandedCategories.contains(node.getUserObject()))
						libraryTree.expandPath(new TreePath(model.getPathToRoot(node)));
				}
			}
		}
	}

	/** */
	private UpdateFilterAction updateFilterAction = new UpdateFilterAction(); 

	/**
	 * Constructs a viewer for the node library.
	 */
	public NodeLibraryViewer() {
		// Grab all OpNode providers and add to library
		final NodeLibrary library = new NodeLibrary();
		library.addURIHandler(new ClassHandler());

		final List<Class<? extends OpNode>> providers = ServiceDiscovery.getInstance().findProviders(OpNode.class);
		for(Class<? extends OpNode> provider : providers) {
			try {
				library.register(provider);
			} catch(Throwable exc) {
				LOGGER.warning("Could not register OpNode provider: " + provider);
			}
		}

		// Initialize component
		initializeComponents(library);
	}

	/**
	 * Constructs a viewer for a specified node library.
	 * 
	 * @param library  the library to view
	 */
	public NodeLibraryViewer(NodeLibrary library) {
		initializeComponents(library);
	}

	private void initializeComponents(NodeLibrary library) {
		setLayout(new BorderLayout());

		this.renderer = new DefaultTreeCellRenderer();
		this.renderer.setOpenIcon(null);
		this.renderer.setLeafIcon(null);
		this.renderer.setClosedIcon(null);

		this.expandedCategories = new HashSet<String>();
		this.libraryTree = new JTree();
		this.infoPane = new JEditorPane();
		this.filterField = new SearchField("Enter Filter Text");

		libraryTree.setRootVisible(false);
		libraryTree.setEditable(false);
		libraryTree.setBackground(Color.WHITE);
		libraryTree.setShowsRootHandles(true);
		libraryTree.setRowHeight(-1);
		libraryTree.setCellRenderer(renderer);
		libraryTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		libraryTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent e) {
				if(e.getPath().getPathCount() >= 2) {
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getPathComponent(1);
					if(node != null && (node.getUserObject() instanceof String))
						expandedCategories.add((String)node.getUserObject());
				}
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent e) {
				if(e.getPath().getPathCount() >= 2) {
					final DefaultMutableTreeNode node = (DefaultMutableTreeNode)e.getPath().getPathComponent(1);
					if(node != null && (node.getUserObject() instanceof String))
						expandedCategories.remove(node.getUserObject());
				}
			}
		});

		libraryTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)e.getPath().getLastPathComponent();
				if(selectedNode != null) {
					final Object selectedObject = selectedNode.getUserObject();
					if(selectedObject != null && selectedObject instanceof NodeData) {
						final NodeData info = (NodeData)selectedObject;
						infoPane.setText(getHTMLForNodeInfo(info));
					}
				}
			}
		});

		libraryTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2 && libraryTree.getSelectionPath() != null) {
					final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)libraryTree.getSelectionPath().getLastPathComponent();
					if(selectedNode != null) {
						final Object selectedObject = selectedNode.getUserObject();
						if(selectedObject != null && selectedObject instanceof NodeData) {
							final NodeData info = (NodeData)selectedObject;
							final GraphDocument document = GraphEditorModel.getActiveDocument();
							if(document != null) {
								try {
									final OpGraph graph = document.getGraph();
									document.getUndoSupport().postEdit(new AddNodeEdit(graph, info, -1, -1));
								} catch(InstantiationException exc) {
									final String message = "Unable to create '" + info.name + "'";
									LOGGER.severe(message);
									ErrorDialog.showError(exc, message);
								}
							}
						}
					}
				}
			}
		});

		setLibrary(library);

		// Create the filter field context menu
		final JRadioButtonMenuItem fullTextItem = new JRadioButtonMenuItem("Full Text");
		final JRadioButtonMenuItem nameItem = new JRadioButtonMenuItem("Name");
		final JRadioButtonMenuItem descriptionItem = new JRadioButtonMenuItem("Description");
		final JRadioButtonMenuItem categoryItem = new JRadioButtonMenuItem("Category");

		fullTextItem.setActionCommand("fulltext");
		fullTextItem.addActionListener(updateFilterAction);
		fullTextItem.setSelected(true);
		nameItem.setActionCommand("name");
		nameItem.addActionListener(updateFilterAction);
		descriptionItem.setActionCommand("description");
		descriptionItem.addActionListener(updateFilterAction);
		categoryItem.setActionCommand("category");
		categoryItem.addActionListener(updateFilterAction);

		final ButtonGroup group = new ButtonGroup();
		group.add(fullTextItem);
		group.add(nameItem);
		group.add(descriptionItem);
		group.add(categoryItem);

		final JPopupMenu filterPopup = new JPopupMenu();
		filterPopup.add(fullTextItem);
		filterPopup.addSeparator();
		filterPopup.add(nameItem);
		filterPopup.add(descriptionItem);
		filterPopup.add(categoryItem);

		filterField.setContextPopup(filterPopup);

		// Filter field initialization
		filterField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				final ActionEvent ae = new ActionEvent(filterField.getDocument(), 0, "filter");
				updateFilterAction.actionPerformed(ae);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				final ActionEvent ae = new ActionEvent(filterField.getDocument(), 0, "filter");
				updateFilterAction.actionPerformed(ae);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {}
		});

		// Node information pane
		final StyleSheet style = new StyleSheet();
		style.addRule("body { background: white; font: 12pt sans-serif; }");
		style.addRule("h1 { font: bold 16pt; margin: 5px; }");
		style.addRule("p { margin: 5px 10px; }");

		final HTMLEditorKit htmlKit = new HTMLEditorKit();
		htmlKit.setStyleSheet(style);

		infoPane.setEditable(false);
		infoPane.setEditorKit(htmlKit);

		// Drag support for creation of nodes
		DragSource.getDefaultDragSource()
		          .createDefaultDragGestureRecognizer(libraryTree,
		                                              DnDConstants.ACTION_COPY,
		                                              gestureListener);

		// Search field and library tree on the left
		final JPanel searchFieldPanel = new JPanel(new BorderLayout());
		searchFieldPanel.setOpaque(false);
		searchFieldPanel.add(filterField);
		searchFieldPanel.setBorder(new EmptyBorder(5, 2, 5, 2));

		final JScrollPane libraryScrollPane = new JScrollPane(libraryTree);
		libraryScrollPane.setBorder(null);

		final JPanel libraryPanel = new JPanel(new BorderLayout());
		libraryPanel.setBackground(Color.WHITE);
		libraryPanel.add(searchFieldPanel, BorderLayout.NORTH);
		libraryPanel.add(libraryScrollPane, BorderLayout.CENTER);

		// Split pane between tree and description

		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setBorder(null);
		splitPane.setLeftComponent(libraryPanel);
		splitPane.setRightComponent(infoPane);
		splitPane.setDividerSize(3);
		splitPane.setDividerLocation(libraryTree.getPreferredSize().width + 100);

		add(splitPane, BorderLayout.CENTER);
	}

	/**
	 * Gets the node library this viewer references.
	 *  
	 * @return the node library
	 */
	public NodeLibrary getLibrary() {
		return library;
	}

	/**
	 * Sets the node library this viewer will reference.
	 * 
	 * @param library  the library
	 */
	public void setLibrary(NodeLibrary library) {
		if(this.library != library) {
			if(this.library != null)
				this.library.removeNodeLibraryListener((NodeLibraryTreeModel)libraryTree.getModel());

			this.library = library;
			this.model = new NodeLibraryTreeModel(library);

			libraryTree.setModel(model);
			for(int row = libraryTree.getRowCount() - 1; row >= 0; --row)
				libraryTree.expandRow(row);

			if(this.library != null)
				this.library.addNodeLibraryListener(model);
		}
	}

	/**
	 * Gets HTML text representing specified info.
	 * 
	 * @param info  the node info
	 * 
	 * @return  HTML-ified version of the info  
	 */
	private String getHTMLForNodeInfo(NodeData info) {
		final StringBuilder builder = new StringBuilder();
		builder.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		builder.append(String.format("  <h1>%s</h1><p>%s</p>%n", info.name, info.description));
		builder.append("</html>\n");
		return builder.toString();
	}

	//
	// DragGestureListener
	//

	private final DragGestureListener gestureListener = new DragGestureListener() {
		@Override
		public void dragGestureRecognized(DragGestureEvent dge) {
			if(libraryTree.getSelectionPath() == null)
				return;

			final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)libraryTree.getSelectionPath().getLastPathComponent();
			if(selectedNode != null) {
				final Object selectedObject = selectedNode.getUserObject();
				if(selectedObject != null && selectedObject instanceof NodeData) {
					final String txt = selectedObject.toString();

					final Font font = getFont().deriveFont(Font.BOLD);
					final FontRenderContext frc = new FontRenderContext(null, true, true);
					final Rectangle2D bounds = font.getStringBounds(txt, frc);
					final int txtw = (int)(bounds.getWidth() + 20);
					final int txth = (int)(bounds.getHeight() + 10);

					final BufferedImage DRAG_IMG = new BufferedImage(txtw, txth, BufferedImage.TYPE_INT_ARGB);
					final Graphics2D g = DRAG_IMG.createGraphics();
					{
						// Draw background
						g.setColor(new Color(255, 255, 150, 200));
						g.fillRect(0, 0, txtw - 1, txth - 1);

						// Draw border
						g.setColor(Color.BLACK);
						g.drawRect(0, 0, txtw - 1, txth - 1);

						// Draw text
						g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g.setFont(font);
						g.setColor(Color.BLACK);
						final LineMetrics lm = font.getLineMetrics(txt, frc);
						final float txtx = (txtw - (float)bounds.getWidth()) * 0.5f;
						final float txty = (txth - (float)bounds.getHeight()) * 0.5f + lm.getAscent();
						g.drawString(txt, txtx, txty);
						g.dispose();
					}

					final Point p = new Point(DRAG_IMG.getWidth() / -2, DRAG_IMG.getHeight() / -2);
					final ObjectSelection sel = new ObjectSelection(selectedObject);
					dge.getDragSource().startDrag(dge, DragSource.DefaultCopyDrop, DRAG_IMG, p, sel, null);
				}
			}
		}
	};
}
