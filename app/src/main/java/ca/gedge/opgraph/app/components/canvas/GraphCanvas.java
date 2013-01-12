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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import ca.gedge.opgraph.ContextualItem;
import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.OpNodeListener;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.OpGraphListener;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.components.ErrorDialog;
import ca.gedge.opgraph.app.components.NullLayout;
import ca.gedge.opgraph.app.components.PathAddressableMenuImpl;
import ca.gedge.opgraph.app.components.ResizeGrip;
import ca.gedge.opgraph.app.components.canvas.CanvasNodeField.AnchorFillState;
import ca.gedge.opgraph.app.edits.graph.AddLinkEdit;
import ca.gedge.opgraph.app.edits.graph.AddNodeEdit;
import ca.gedge.opgraph.app.edits.graph.MoveNodesEdit;
import ca.gedge.opgraph.app.edits.graph.RemoveLinkEdit;
import ca.gedge.opgraph.app.edits.notes.MoveNoteEdit;
import ca.gedge.opgraph.app.edits.notes.ResizeNoteEdit;
import ca.gedge.opgraph.app.extensions.NodeMetadata;
import ca.gedge.opgraph.app.extensions.Note;
import ca.gedge.opgraph.app.extensions.NoteComponent;
import ca.gedge.opgraph.app.extensions.Notes;
import ca.gedge.opgraph.app.util.CollectionListener;
import ca.gedge.opgraph.app.util.GUIHelper;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.extensions.CompositeNode;
import ca.gedge.opgraph.extensions.Publishable;
import ca.gedge.opgraph.extensions.Publishable.PublishedInput;
import ca.gedge.opgraph.extensions.Publishable.PublishedOutput;
import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.util.BreadcrumbListener;
import ca.gedge.opgraph.util.Pair;

/**
 * A canvas for creating/modifying an {@link OpGraph}.
 * 
 * TODO Autoscrolling when moving a node, or moving/resizing notes, or really
 *      anytime when dealing with children components
 *      
 * XXX Use a delegate for handling some of this class' inner functionality.
 *     Some good places would be having the delegate handling the double
 *     clicking of nodes (specifically, macros). Then we could extract the
 *     breadcrumb from this class also, which doesn't really feel like it
 *     belongs here. This class should never set the model on itself.
 */
public class GraphCanvas extends JLayeredPane implements ClipboardOwner {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(GraphCanvas.class.getName());

	/** The application model this canvas uses */
	private GraphEditorModel model;

	/** The document model this canvas uses */
	private GraphDocument document;

	/** The mapping of nodes to node components */
	private HashMap<OpNode, CanvasNode> nodes;

	/** The layer that displays a grid */
	private final GridLayer gridLayer;

	/** The layer that displays links between nodes */
	private final LinksLayer linksLayer;

	/** The layer that overlays the whole canvas */
	private final CanvasOverlay canvasOverlay;

	/** The debug layer that overlays the whole canvas */
	private final DebugOverlay canvasDebugOverlay;

	//
	// Drag-based members
	//

	/**
	 * If link dragging is happening, <code>null</code> if this should be a new
	 * link, or a reference to an existing link if editing a link.
	 */
	private OpLink currentlyDraggedLink;

	/** If link dragging is happening, the input field from which this link originates */
	private CanvasNodeField currentlyDraggedLinkInputField;

	/** If link dragging is happening, the current location of the destination end */
	private Point currentDragLinkLocation;

	/**
	 * If link dragging is started, specifies whether or not the current
	 * position of the drag is a valid drop location for the link.
	 */
	private boolean dragLinkIsValid;

	/** The selection rectangle, or <code>null</code> if none */
	private Rectangle selectionRect;

	/** The initial click point (screen coordinates) within the canvas */
	private Point clickLocation;

	/** Component(s) whose location(s) will move during a mouse drag operation */
	private List<Pair<Component, Point>> componentsToMove = new ArrayList<Pair<Component, Point>>();

	//
	// Layers
	//

	private static final Integer GRID_LAYER = 1;

	@SuppressWarnings("unused")
	private static final Integer BACKGROUND_LAYER = 2;

	private static final Integer NOTES_LAYER = 10;
	private static final Integer LINKS_LAYER = 100;
	private static final Integer NODES_LAYER = 200;
	private static final Integer OVERLAY_LAYER = 10000;
	private static final Integer DEBUG_OVERLAY_LAYER = 10001;

	@SuppressWarnings("unused")
	private static final Integer FOREGROUND_LAYER = Integer.MAX_VALUE;

	//
	// Listener objects
	//

	private class MetaListener implements PropertyChangeListener {
		private OpNode node;

		public MetaListener(OpNode node) {
			this.node = node;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if(evt.getSource() instanceof NodeMetadata) {
				final NodeMetadata meta = (NodeMetadata)evt.getSource();
				final CanvasNode canvasNode = nodes.get(node);
				if(canvasNode != null) {
					if(evt.getPropertyName().equals(NodeMetadata.LOCATION_PROPERTY)) {
						canvasNode.setLocation(meta.getX(), meta.getY());
					} else if(evt.getPropertyName().equals(NodeMetadata.DEFAULTS_PROPERTY)) {
						updateAnchorFillStates(node);
					}
				}
			}
		}
	};

	/**
	 * Constructs a canvas that displays a given graph model.
	 * 
	 * @param model  the graph model
	 */
	public GraphCanvas(GraphEditorModel model) {
		super();

		// Components
		this.gridLayer = new GridLayer();
		this.linksLayer = new LinksLayer(this);
		this.canvasOverlay = new CanvasOverlay(this);
		this.canvasDebugOverlay = new DebugOverlay(this);

		// Class setup
		this.model = model;
		this.nodes = new HashMap<OpNode, CanvasNode>();

		this.document = new GraphDocument(this);
		this.document.getBreadcrumb().addBreadcrumbListener(breadcrumbListener);
		this.document.getSelectionModel().addSelectionListener(canvasSelectionListener);
		changeGraph(null, this.document.getGraph());

		// Initialize component
		setDoubleBuffered(true);
		setLayout(new NullLayout());
		setFocusable(true);
		setOpaque(false);
		setFocusCycleRoot(true);
		setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, dropTargetAdapter, true));

		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseMotionAdapter);

		final long eventMask = AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK;
		Toolkit.getDefaultToolkit().addAWTEventListener(awtEventListener, eventMask);

		//
		// Actions
		//

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
		getActionMap().put("cancel", new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				// If dragging link, null out these guys so selected nodes
				// don't get moved on mouse release
				if(currentlyDraggedLinkInputField != null)
					clickLocation = null;

				selectionRect = null;
				currentlyDraggedLink = null;
				currentlyDraggedLinkInputField = null;
				currentDragLinkLocation = null;

				repaint();
			}
		});

		// Other layers
		add(gridLayer, GRID_LAYER);
		add(linksLayer, LINKS_LAYER);
		add(canvasOverlay, OVERLAY_LAYER);
		add(canvasDebugOverlay, DEBUG_OVERLAY_LAYER);
	}

	/**
	 * Gets the bounding rectangle of the items currently being moved.
	 * 
	 * @return the bounding rectangle
	 */
	private Rectangle getBoundingRectOfMoved() {
		int xmin = Integer.MAX_VALUE;
		int xmax = Integer.MIN_VALUE;
		int ymin = Integer.MAX_VALUE;
		int ymax = Integer.MIN_VALUE;

		for(Pair<Component, Point> compLoc : componentsToMove) {
			final Point loc = compLoc.getSecond();
			final Component comp = compLoc.getFirst();
			final Dimension pref = comp.getPreferredSize();
			xmin = Math.min(xmin, loc.x);
			xmax = Math.max(xmax, loc.x + pref.width);
			ymin = Math.min(ymin, loc.y);
			ymax = Math.max(ymax, loc.y + pref.height);
		}

		return new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
	}

	/**
	 * Gets the selection model this canvas is using.
	 * 
	 * @return the selection model 
	 */
	public GraphCanvasSelectionModel getSelectionModel() {
		return document.getSelectionModel();
	}

	/**
	 * Gets the document this canvas is editing.
	 * 
	 * @return the document
	 */
	public GraphDocument getDocument() {
		return document;
	}

	/**
	 * Gets a mapping from {@link OpNode} to the respective node
	 * component that displays that node.
	 * 
	 * @return the mapping
	 */
	public Map<OpNode, CanvasNode> getNodeMap() {
		return Collections.unmodifiableMap(nodes);
	}

	/**
	 * Gets the node displaying the given node. 
	 * 
	 * @param node  the node
	 * 
	 * @return the node displaying the given node, or <code>null</code>
	 *         if no such node exists
	 */
	public CanvasNode getNode(OpNode node) {
		return nodes.get(node);
	}

	/**
	 * Gets the link currently being dragged.
	 * 
	 * @return the link, or <code>null</code> if no link being dragged 
	 */
	public OpLink getCurrentlyDraggedLink() {
		return currentlyDraggedLink;
	}

	/**
	 * Gets the input field of the current drag link.
	 * 
	 * @return the input field, or <code>null</code> if no link being dragged
	 */
	public CanvasNodeField getCurrentlyDraggedLinkInputField() {
		return currentlyDraggedLinkInputField;
	}

	/**
	 * Gets the location of the current link being dragged.
	 * 
	 * @return the location of the drag link, or <code>null</code> if no link
	 *         being dragged
	 */
	public Point getCurrentDragLinkLocation() {
		return currentDragLinkLocation;
	}

	/**
	 * Gets whether or not the currently dragged link is at a valid drop spot.
	 * 
	 * @return <code>true</code> if the currently dragged link can be dropped
	 *         at the curent drag location, <code>false</code> otherwise 
	 */
	public boolean isDragLinkValid() {
		return dragLinkIsValid;
	}

	/**
	 * Constructs a popup menu for a node.
	 * 
	 * @param event  the mouse event that created the popup
	 * 
	 * @return an appropriate popup menu for the given node
	 */
	public JPopupMenu constructPopup(MouseEvent event) {
		Object context = document.getGraph();

		// Try to find a more specific context
		final CanvasNode node = GUIHelper.getAncestorOrSelfOfClass(CanvasNode.class, event.getComponent());
		if(node != null) {
			context = node.getNode();
		} else {
			final NoteComponent note = GUIHelper.getAncestorOrSelfOfClass(NoteComponent.class, event.getComponent());
			if(note != null)
				context = note.getNote();
		}

		final JPopupMenu popup = new JPopupMenu();
		if(context != null) {
			final PathAddressableMenuImpl addressable = new PathAddressableMenuImpl(popup);
			for(MenuProvider menuProvider : model.getMenuProviders())
				menuProvider.installPopupItems(context, event, model, addressable);
		}

		if(popup.getComponentCount() == 0)
			return null;

		return popup;
	}

	/**
	 * Updates the debug state for this canvas. Currently, the debug state
	 * simply highlights the node being processed.
	 * 
	 * @param context  the processing context, or <code>null</code> if no debugging
	 */
	public void updateDebugState(Processor context) {
		if(context == null) {
			setEnabled(true);
		} else {
			setEnabled(false);

			if(document.getBreadcrumb().containsState(context.getGraph())) {
				document.getBreadcrumb().gotoState(context.getGraph());
			} else {
				// Given the current processing context, find the path that
				// gets to the current node
				final LinkedList<Pair<OpGraph, String>> path = new LinkedList<Pair<OpGraph, String>>();

				String id = context.getGraphOfContext().getId();
				Processor activeContext = context;
				while(activeContext != null) {
					final OpGraph graph = activeContext.getGraphOfContext();
					path.addLast(new Pair<OpGraph, String>(graph, id));

					if(activeContext.getCurrentNodeOfContext() != null)
						id = activeContext.getCurrentNodeOfContext().getName();
					else
						id = "Unknown";

					activeContext = activeContext.getMacroContext();
				}

				document.getBreadcrumb().set(path);
			}

			getSelectionModel().setSelectedNode(context.getCurrentNode());
		}

		canvasDebugOverlay.repaint();
	}

	/**
	 * Updates the fill states of all anchors for a given node.
	 * 
	 * @param node  the node to update
	 */
	public void updateAnchorFillStates(OpNode node) {
		final CanvasNode canvasNode = nodes.get(node);
		if(canvasNode != null) {
			final Map<ContextualItem, CanvasNodeField> fields = canvasNode.getFieldsMap();
			final NodeMetadata meta = node.getExtension(NodeMetadata.class);
			final Publishable publishable = document.getGraph().getExtension(Publishable.class);

			for(InputField field : node.getInputFields()) {
				final CanvasNodeField canvasField = fields.get(field);
				if(canvasField != null) {
					// Check to see if we should fill for a published field first
					boolean isPublished = false;
					if(publishable != null) {
						for(PublishedInput input : publishable.getPublishedInputs()) {
							if(node == input.destinationNode && field.equals(input.nodeInputField)) {
								canvasField.updateAnchorFillState(AnchorFillState.PUBLISHED);
								isPublished = true;
								break;
							}
						}
					}

					// It's not published, so is there a default value?
					if(!isPublished) {
						if(meta == null || meta.getDefault(field) == null)
							canvasField.updateAnchorFillState(AnchorFillState.NONE);
						else
							canvasField.updateAnchorFillState(AnchorFillState.DEFAULT);	
					}
				}
			}

			// Fill for published output fields
			if(publishable != null) {
				for(OutputField field : node.getOutputFields()) {
					final CanvasNodeField canvasField = fields.get(field);
					if(canvasField != null) {
						for(PublishedOutput output : publishable.getPublishedOutputs()) {
							if(node == output.sourceNode && field == output.nodeOutputField) {
								canvasField.updateAnchorFillState(AnchorFillState.PUBLISHED);
								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Start a drag operation for a given field.
	 * 
	 * @param fieldComponent  the field
	 */
	public void startLinkDrag(CanvasNodeField fieldComponent) {
		if(!isEnabled()) return;

		currentlyDraggedLink = null;
		currentlyDraggedLinkInputField = null;
		currentDragLinkLocation = getMousePosition();

		CanvasNode node = (CanvasNode)SwingUtilities.getAncestorOfClass(CanvasNode.class, fieldComponent);
		if(node != null) {
			ContextualItem field = fieldComponent.getField();
			if(field instanceof InputField) {
				// Check if link exists and, if so, start editing it
				for(OpLink e : document.getGraph().getIncomingEdges(node.getNode())) {
					if(e.getDestinationField() == field) {
						currentlyDraggedLink = e;
						break;
					}
				}

				// Found a link, but currentlyDraggedLinkField needs to be on
				// source end, so use the link we found to update those fields
				if(currentlyDraggedLink != null) {
					node = nodes.get(currentlyDraggedLink.getSource());
					if(node != null) {
						fieldComponent = node.getFieldsMap().get(currentlyDraggedLink.getSourceField());
						if(field != null) {
							currentlyDraggedLinkInputField = fieldComponent;
							repaint();
						}
					}
				}
			} else if(field instanceof OutputField) {
				currentlyDraggedLinkInputField = fieldComponent;
				repaint();
			}
		}
	}

	/**
	 * Called to update link dragging status.
	 * 
	 * @param p  the current point of the drag, in the coordinate system of this component
	 */
	public void updateLinkDrag(Point p) {
		if(!isEnabled()) return;

		if(currentlyDraggedLinkInputField == null) {
			dragLinkIsValid = false;
			return;
		}

		currentDragLinkLocation = p;
		dragLinkIsValid = true;

		// Get the source node
		final CanvasNode source = (CanvasNode)SwingUtilities.getAncestorOfClass(CanvasNode.class, currentlyDraggedLinkInputField);
		if(source == null)
			return;

		// Find the destination node
		for(Component comp : getComponentsInLayer(NODES_LAYER)) {
			final CanvasNode dest = (CanvasNode)comp;
			final Point nodeP = SwingUtilities.convertPoint(this, p, dest);
			if(dest.contains(nodeP)) {
				// See if we're hovering over a field
				CanvasNodeField field = dest.getFieldAt(nodeP);
				if(field != null) {
					// At this point, we default to an invalid link. If we're
					// not hovering over an InputField, then it isn't valid
					dragLinkIsValid = false;
					if(field.getField() instanceof InputField) {
						try {
							final OutputField out = (OutputField)currentlyDraggedLinkInputField.getField();
							final InputField in = (InputField)field.getField();
							final OpLink link = new OpLink(source.getNode(), out, dest.getNode(), in);

							// Now make sure the link can be added, and that it is a valid link
							dragLinkIsValid = (document.getGraph().canAddEdge(link) && link.isValid());
						} catch(ItemMissingException exc) {}
					}
				}
				break;
			}
		}

		repaint();
	}

	/**
	 * Called when link dragging should end.
	 * 
	 * @param p  the end point of the drag, in the coordinate system of this component
	 */
	public void endLinkDrag(Point p) {
		if(!isEnabled()) return; 

		updateLinkDrag(p);

		// If the drag link is valid, check to see which field this link
		// was fed into and try to add a new link
		if(currentlyDraggedLinkInputField != null) {
			final OpGraph graph = document.getGraph();
			if(dragLinkIsValid) {
				final CanvasNode sourceNode = (CanvasNode)SwingUtilities.getAncestorOfClass(CanvasNode.class, currentlyDraggedLinkInputField);
				if(sourceNode == null)
					return;

				boolean destinationFound = false;
				for(Component comp : getComponentsInLayer(NODES_LAYER)) {
					final CanvasNode destinationNode = (CanvasNode)comp;
					final Point nodeP = SwingUtilities.convertPoint(this, p, destinationNode);
					if(destinationNode.contains(nodeP)) {
						final CanvasNodeField destinationField = destinationNode.getFieldAt(nodeP);
						if(destinationField != null && destinationField.getField() instanceof InputField) {
							final OpNode source = sourceNode.getNode();
							final OpNode destination = destinationNode.getNode();
							final OutputField sourceField = (OutputField)currentlyDraggedLinkInputField.getField();
							final InputField destField = (InputField)destinationField.getField();

							// If no link being edited, just add the new link,
							// otherwise we need to see if any changes made
							try {
								final OpLink link = new OpLink(source, sourceField, destination, destField);
								if(currentlyDraggedLink == null) {
									document.getUndoSupport().postEdit(new AddLinkEdit(graph, link));
								} else if(!link.equals(currentlyDraggedLink)) {
									document.getUndoSupport().beginUpdate();
									document.getUndoSupport().postEdit(new RemoveLinkEdit(graph, currentlyDraggedLink));
									document.getUndoSupport().postEdit(new AddLinkEdit(graph, link));
									document.getUndoSupport().endUpdate();
								}
							} catch(ItemMissingException exc) {
								ErrorDialog.showError(exc);
							} catch(VertexNotFoundException exc) {
								ErrorDialog.showError(exc);
							} catch(CycleDetectedException exc) {
								ErrorDialog.showError(exc);
							}

							destinationFound = true;
						}

						break;
					}
				}

				// No destination found, so this means we were dragging over
				// the canvas area. If we were editing an existing link,
				// remove it
				if(!destinationFound && currentlyDraggedLink != null) {
					if(!graph.contains(currentlyDraggedLink)) {
						try {
							graph.add(currentlyDraggedLink);
						} catch (VertexNotFoundException e) {
						} catch (CycleDetectedException e) {
						}
					}
					document.getUndoSupport().postEdit(new RemoveLinkEdit(graph, currentlyDraggedLink));
				}

			} else if(currentlyDraggedLink != null) {
				if(!graph.contains(currentlyDraggedLink)) {
					try {
						graph.add(currentlyDraggedLink);
					} catch (VertexNotFoundException e) {
					} catch (CycleDetectedException e) {
					}
				}
				// Invalid link, and we are editing an existing link, so remove it
				document.getUndoSupport().postEdit(new RemoveLinkEdit(graph, currentlyDraggedLink));
			}
		}

		currentlyDraggedLink = null;
		currentlyDraggedLinkInputField = null;
		currentDragLinkLocation = null;

		repaint();
	}

	//
	// Overrides
	//

	@Override
	public void setLayout(LayoutManager mgr) {
		if(mgr != null && !(mgr instanceof NullLayout))
			throw new UnsupportedOperationException("GraphCanvas cannot use a custom layout");
		super.setLayout(mgr);
	}

	/**
	 * Gets the selection rectangle.
	 * 
	 * @return the selection rectangle, or <code>null</code> if there is
	 *         currently no selection rectangle
	 */
	public Rectangle getSelectionRect() {
		Rectangle ret = null;
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

			ret = new Rectangle(x, y, w, h);
		}
		return ret;
	}

	/**
	 * Switches the graph this canvas is viewing.
	 * 
	 * @param oldGraph  the graph that was displayed previously
	 * @param graph  the new graph to display
	 */
	protected void changeGraph(OpGraph oldGraph, OpGraph graph) {
		synchronized(getTreeLock()) {
			// Remove old components
			if(oldGraph != null) {
				final Notes notes = oldGraph.getExtension(Notes.class);
				if(notes != null) {
					for(Note note : notes)
						notesAdapter.elementRemoved(notes, note);
				}

				for(OpLink link : oldGraph.getEdges())
					graphAdapter.linkRemoved(oldGraph, link);

				for(OpNode node : oldGraph.getVertices())
					graphAdapter.nodeRemoved(oldGraph, node);
			}

			// Add new ones
			if(graph != null) {
				graph.addGraphListener(graphAdapter);

				for(OpNode node : document.getGraph().getVertices())
					graphAdapter.nodeAdded(graph, node);

				for(OpLink link : document.getGraph().getEdges())
					graphAdapter.linkAdded(graph, link);

				// Add any notes, or if none exist, make sure the extension
				// exists on the given graph
				Notes notes = document.getGraph().getExtension(Notes.class);
				if(notes == null) {
					notes = new Notes();
					document.getGraph().putExtension(Notes.class, notes);
				} else {
					for(Note note : notes)
						notesAdapter.elementAdded(notes, note);
				}

				notes.addCollectionListener(notesAdapter);
			}

		}

		// Update selection 
		for(OpNode node : getSelectionModel().getSelectedNodes()) {
			if(nodes.containsKey(node))
				nodes.get(node).setSelected(true);
		}

		// Update anchor fill states
		if(graph != null) {
			for(OpNode node : document.getGraph().getVertices())
				updateAnchorFillStates(node);
		}

		revalidate();
		repaint();
	}

	//
	// MouseAdapter
	//

	private final MouseAdapter mouseAdapter = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			selectionRect = new Rectangle(e.getPoint());
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// Find selected nodes, if necessary
			if(selectionRect != null) {
				final Rectangle rect = getSelectionRect(); 
				final Set<OpNode> selected = new HashSet<OpNode>();
				for(Component comp : getComponentsInLayer(NODES_LAYER)) {
					final Rectangle compRect = comp.getBounds();
					if((comp instanceof CanvasNode) && rect.intersects(compRect))
						selected.add( ((CanvasNode)comp).getNode() );
				}

				getSelectionModel().setSelectedNodes(selected);
			}

			// Reset variables
			selectionRect = null;
			repaint();
		}
	};

	//
	// MouseMotionListener
	//

	private final MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter() {
		@Override
		public void mouseDragged(MouseEvent e) {
			final Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), GraphCanvas.this);
			scrollRectToVisible(new Rectangle(p.x, p.y, 1, 1));

			if(selectionRect != null) {
				final Point src = selectionRect.getLocation();
				selectionRect.setSize(p.x - src.x, p.y - src.y);
			}

			repaint();
		}
	};

	//
	// GraphCanvasModelListener
	//

	private final GraphCanvasAdapter graphAdapter = new GraphCanvasAdapter();

	private class GraphCanvasAdapter implements OpGraphListener, OpNodeListener {
		@Override
		public void nodePropertyChanged(String propertyName, Object oldValue, Object newValue) {}

		@Override
		public void nodeAdded(final OpGraph graph, final OpNode v) {
			if(!nodes.containsKey(v)) {
				final CanvasNode node = new CanvasNode(v);
				final int cx = (int)getVisibleRect().getCenterX();
				final int cy = (int)getVisibleRect().getCenterY();

				// Place this node at the center if it has a negative location
				NodeMetadata meta = v.getExtension(NodeMetadata.class);
				if(meta == null) {
					meta = new NodeMetadata(cx, cy);
					v.putExtension(NodeMetadata.class, meta);
				} else {
					if(meta.getX() < 0) meta.setX(cx);
					if(meta.getY() < 0) meta.setY(cy);
				}

				node.setLocation(meta.getX(), meta.getY());
				meta.addPropertyChangeListener(new MetaListener(v));

				// Adjust links when component moves or resizes
				node.addComponentListener(new ComponentAdapter() {
					@Override
					public void componentResized(ComponentEvent e) {
						for(OpLink link : document.getGraph().getIncomingEdges(v)) linksLayer.updateLink(link);
						for(OpLink link : document.getGraph().getOutgoingEdges(v)) linksLayer.updateLink(link);
						GraphCanvas.this.revalidate();
					}

					@Override
					public void componentMoved(ComponentEvent e) {
						for(OpLink link : document.getGraph().getIncomingEdges(v)) linksLayer.updateLink(link);
						for(OpLink link : document.getGraph().getOutgoingEdges(v)) linksLayer.updateLink(link);
						GraphCanvas.this.revalidate();		
					}
				});

				node.addUndoableEditListener(document.getUndoManager());
				nodes.put(v, node);
				add(node, NODES_LAYER, 0);

				v.addNodeListener(this);
				v.putExtension(JComponent.class, node);

				revalidate();
				repaint();
			}
		}

		@Override
		public void nodeRemoved(OpGraph graph, OpNode v) {
			if(nodes.containsKey(v)) {
				remove(nodes.get(v));
				nodes.get(v).removeUndoableEditListener(document.getUndoManager());
				nodes.remove(v);
				getSelectionModel().removeNodeFromSelection(v);

				v.removeNodeListener(this);
				v.putExtension(JComponent.class, null);

				revalidate();
				repaint();
			}
		}

		@Override
		public void linkAdded(OpGraph graph, OpLink e) {
			final CanvasNode src = nodes.get(e.getSource());
			final CanvasNode dst = nodes.get(e.getDestination());

			final CanvasNodeField srcField = src.getFieldsMap().get(e.getSourceField());
			final CanvasNodeField dstField = dst.getFieldsMap().get(e.getDestinationField());

			if(srcField != null) srcField.setAnchorFillState(AnchorFillState.LINK);
			if(dstField != null) dstField.setAnchorFillState(AnchorFillState.LINK);

			linksLayer.updateLink(e);

			repaint();
		}

		@Override
		public void linkRemoved(OpGraph graph, OpLink e) {
			final CanvasNode src = nodes.get(e.getSource());
			final CanvasNode dst = nodes.get(e.getDestination());

			// Multiple outgoing links can exist, so before removing this link, make
			// sure there are no more outgoing links
			if(graph.getOutgoingEdges(src.getNode()).size() == 0) {
				final CanvasNodeField field = src.getFieldsMap().get(e.getSourceField());
				if(field != null)
					field.setAnchorFillState(AnchorFillState.NONE);
			}

			// Decide whether the anchor fill state is to be set to NONE or DEFAULT
			final CanvasNodeField dstField = dst.getFieldsMap().get(e.getDestinationField());
			if(dstField != null) {
				final NodeMetadata meta = dst.getNode().getExtension(NodeMetadata.class);
				if(meta == null || meta.getDefault(e.getDestinationField()) == null)
					dstField.setAnchorFillState(AnchorFillState.NONE);
				else
					dstField.setAnchorFillState(AnchorFillState.DEFAULT);
			}

			linksLayer.removeLink(e);

			// Remove link reference and repaint
			repaint();
		}

		@Override
		public void fieldAdded(OpNode node, InputField field) {
			final CanvasNode canvasNode = nodes.get(node);
			if(canvasNode != null)
				repaint();
		}

		@Override
		public void fieldRemoved(OpNode node, InputField field) {
			final CanvasNode canvasNode = nodes.get(node);
			if(canvasNode != null)
				repaint();
		}

		@Override
		public void fieldAdded(OpNode node, OutputField field) {
			final CanvasNode canvasNode = nodes.get(node);
			if(canvasNode != null)
				repaint();
		}

		@Override
		public void fieldRemoved(OpNode node, OutputField field) {
			final CanvasNode canvasNode = nodes.get(node);
			if(canvasNode != null)
				repaint();
		}
	}

	//
	// GraphCanvasSelectionListener
	//

	private final GraphCanvasSelectionListener canvasSelectionListener = new GraphCanvasSelectionListener() {
		@Override
		public void nodeSelectionChanged(Collection<OpNode> old, Collection<OpNode> selected) {
			for(OpNode node : old) {
				if(nodes.containsKey(node))
					nodes.get(node).setSelected(false);
			}

			for(OpNode node : selected) {
				if(nodes.containsKey(node))
					nodes.get(node).setSelected(true);
			}

			repaint();
		}
	};

	//
	// AWTEventListener
	//

	private final AWTEventListener awtEventListener = new AWTEventListener() {
		@Override
		public void eventDispatched(AWTEvent e) {
			// Only registered for mouse events
			if(!(e instanceof MouseEvent))
				return;

			final Component source = (Component)e.getSource();
			final MouseEvent me = (MouseEvent)e;

			if(!SwingUtilities.isDescendingFrom(source, GraphCanvas.this))
				return;

			if(e.getID() == MouseEvent.MOUSE_PRESSED) {
				// If the mouse is pressed, update the selection. If pressed
				// on the canvas area, clear the selection. If on a node, and
				// it isn't already selected, select it. Otherwise, do nothing.
				//

				// Request focus if not clicked in text field that is being edited
				if(source instanceof JTextComponent) {
					if( ((JTextComponent)source).hasFocus() == false )
						requestFocusInWindow();
				} else {
					requestFocusInWindow();
				}

				// Make sure the component the event was dispatched to is a child
				clickLocation = me.getLocationOnScreen();
				componentsToMove.clear();

				// No CanvasNode parent? Select nothing, otherwise select its node
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						final CanvasNode canvasNode = GUIHelper.getAncestorOrSelfOfClass(CanvasNode.class, source);
						if(canvasNode == null) {
							getSelectionModel().setSelectedNode(null);

							final NoteComponent note = GUIHelper.getAncestorOrSelfOfClass(NoteComponent.class, source);
							if(note != null) {
								moveToFront(note);

								final Component comp = (source instanceof ResizeGrip) ? source : note;
								final Point initialLocation = comp.getLocation();
								componentsToMove.add(new Pair<Component, Point>(comp, initialLocation));
							}
						} else {
							// If it's not already selected, then select it
							if(!getSelectionModel().getSelectedNodes().contains(canvasNode.getNode()))
								getSelectionModel().setSelectedNode(canvasNode.getNode());

							// Bring it to the top of the nodes layer
							moveToFront(canvasNode);

							// Set the selected nodes as the components to move on drag
							for(OpNode node : getSelectionModel().getSelectedNodes()) {
								final JComponent comp = node.getExtension(JComponent.class);
								if(comp != null) {
									final Point initialLocation = comp.getLocation();
									componentsToMove.add(new Pair<Component, Point>(comp, initialLocation));
								}
							}
						}
					}
				});
			} else if(e.getID() == MouseEvent.MOUSE_CLICKED && ((MouseEvent)e).getClickCount() == 2) {
				// If double clicked, we'll descend into a composite node
				boolean shouldDescend = true;

				// Check to see if this is an editable text component, and if
				// it isn't, we can go into a composite node
				if(source instanceof JTextComponent) 
					shouldDescend = !((JTextComponent)source).isEditable();

				// If double-clicked on a composite node, start editing it
				if(shouldDescend) {
					final CanvasNode node = GUIHelper.getAncestorOrSelfOfClass(CanvasNode.class, source);
					if(node != null) {
						final CompositeNode composite = node.getNode().getExtension(CompositeNode.class);
						final GraphDocument document = GraphCanvas.this.document;
						if(composite != null) {
							// Put the publishing extension in the graph (even if it's null)
							final Publishable publishable = node.getNode().getExtension(Publishable.class);
							composite.getGraph().putExtension(Publishable.class, publishable);

							// Set up the breadcrumb 
							document.getBreadcrumb().addState(composite.getGraph(), node.getNode().getName());
						}
					}
				}
			}

			// Show popup if a popup trigger
			if(me.isPopupTrigger()) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						final Point loc = SwingUtilities.convertPoint(source, me.getPoint(), GraphCanvas.this);
						final JPopupMenu popup = constructPopup(me);
						if(popup != null)
							popup.show(GraphCanvas.this, loc.x, loc.y);
					}
				});
			}

			// All other events won't be processed if the canvas is disabled 
			if(!isEnabled())
				return;

			if(e.getID() == MouseEvent.MOUSE_DRAGGED) {
				// Move the selected nodes, if not creating a link
				if(clickLocation != null && currentlyDraggedLinkInputField == null && selectionRect == null) {
					int deltaX = me.getLocationOnScreen().x - clickLocation.x;
					int deltaY = me.getLocationOnScreen().y - clickLocation.y;

					// Snap to grid if top left corner of selection is close to grid
					final Point topLeftBound = getBoundingRectOfMoved().getLocation();
					topLeftBound.translate(deltaX, deltaY);

					final Point snapDelta = gridLayer.snap(topLeftBound);
					deltaX += snapDelta.x;
					deltaY += snapDelta.y;

					// First, make sure that one of the components to move is
					// an ancestor of the source of the drag
					for(Pair<Component, Point> compLoc : componentsToMove) {
						final Component comp = compLoc.getFirst();
						if(!(comp instanceof ResizeGrip)) {
							final Point initialLoc = compLoc.getSecond();
							comp.setLocation(initialLoc.x + deltaX, initialLoc.y + deltaY);
						}
					}
				}
			} else if(e.getID() == MouseEvent.MOUSE_RELEASED) {
				// Post an undoable event for any dragging that occurred
				if(clickLocation != null && currentlyDraggedLinkInputField == null && selectionRect == null) {
					// XXX Right now this works, but generalizing this would be better. What if
					//     we could select both nodes and notes and move them simultaneously?
					int deltaX = me.getXOnScreen() - clickLocation.x;
					int deltaY = me.getYOnScreen() - clickLocation.y;
					if(deltaX != 0 || deltaY != 0) {
						final Collection<OpNode> selected = getSelectionModel().getSelectedNodes();
						if(selected.size() > 0) {
							// If nodes selected, post special edit for them
							document.getUndoSupport().postEdit(new MoveNodesEdit(selected, deltaX, deltaY));
						} else {
							// Otherwise, assume we have a note
							//
							// TODO generalized element movement
							//
							document.getUndoSupport().beginUpdate();
							for(Pair<Component, Point> compLoc : componentsToMove) {
								final Component comp = compLoc.getFirst();
								if(comp instanceof NoteComponent) {
									final Note note = ((NoteComponent)comp).getNote();
									comp.setLocation(comp.getX() - deltaX, comp.getY() - deltaY);
									document.getUndoSupport().postEdit(new MoveNoteEdit(note, deltaX, deltaY));
								}
							}
							document.getUndoSupport().endUpdate();
						}
					}
				}

				clickLocation = null;
			}
		}
	};

	//
	// CollectionListener<Notes.Note>
	//

	// TODO the xxxMouseListener calls below are ugly, so find a nicer way to do this 

	private final CollectionListener<Notes, Note> notesAdapter = new CollectionListener<Notes, Note>() {
		@Override
		public void elementAdded(Notes source, Note element) {
			final JComponent comp = element.getExtension(JComponent.class);
			if(comp != null) {
				add(comp, NOTES_LAYER);
				((NoteComponent)comp).getResizeGrip().addMouseListener(notesMouseAdapter);
				comp.revalidate();
			}
		}

		@Override
		public void elementRemoved(Notes source, Note element) {
			final JComponent comp = element.getExtension(JComponent.class);
			if(comp != null) {
				remove(comp);
				((NoteComponent)comp).getResizeGrip().removeMouseListener(notesMouseAdapter);
				repaint(comp.getBounds());
			}
		}
	};

	//
	// BreadcrumbListener
	//

	private final BreadcrumbListener<OpGraph, String> breadcrumbListener = new BreadcrumbListener<OpGraph, String>() {
		@Override
		public void stateChanged(OpGraph oldGraph, OpGraph newGraph) {
			// XXX Could this move to changeGraph instead?
			if(oldGraph != null){
				oldGraph.removeGraphListener(graphAdapter);

				final Notes notes = oldGraph.getExtension(Notes.class);
				if(notes != null)
					notes.removeCollectionListener(notesAdapter);
			}

			changeGraph(oldGraph, newGraph);
		}

		@Override
		public void stateAdded(OpGraph state, String value) {}
	};

	//
	// Adapter for creating undoable edits when notes are resized
	//

	private final MouseAdapter notesMouseAdapter = new MouseAdapter() {
		@Override
		public void mouseReleased(MouseEvent e) {
			if(e.getComponent() instanceof ResizeGrip) {
				final ResizeGrip grip = (ResizeGrip)e.getComponent();
				if(grip.getComponent() instanceof NoteComponent) {
					final Note note = ((NoteComponent)grip.getComponent()).getNote();
					final Dimension initialSize = grip.getInitialComponentSize();
					final Dimension newSize = grip.getComponent().getSize();
					document.getUndoSupport().postEdit(new ResizeNoteEdit(note, initialSize, newSize));
				}
			}
		}
	};

	//
	// DropTargetListener
	//

	private static DataFlavor accepted = new DataFlavor(NodeData.class, "NodeData");

	private final DropTargetAdapter dropTargetAdapter = new DropTargetAdapter() {		
		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			if(!dtde.isDataFlavorSupported(accepted))
				dtde.rejectDrag();
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			scrollRectToVisible(new Rectangle(dtde.getLocation(), new Dimension(1, 1)));
			if(!dtde.isDataFlavorSupported(accepted))
				dtde.rejectDrag();
		}

		@Override
		public void drop(final DropTargetDropEvent dtde) {
			if(dtde.isDataFlavorSupported(accepted)) {
				NodeData info = null;
				try {
					info = (NodeData)dtde.getTransferable().getTransferData(accepted);

					// Set up the initial location metadata and post the edit
					final int x = dtde.getLocation().x;
					final int y = dtde.getLocation().y;
					final OpGraph graph = document.getGraph();
					document.getUndoSupport().postEdit(new AddNodeEdit(graph, info, x, y));
				} catch(UnsupportedFlavorException e) {
					LOGGER.warning("Drop event says it supports NodeData flavor, but can't get data for that flavor");
				} catch(IOException e) {
					LOGGER.warning("IOException on drop, which should never happen");
				} catch(InstantiationException e) {
					LOGGER.warning("Could not instantiate node '" + info.name + "' from drop");
				}

				// Drag complete!
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				dtde.dropComplete(true);
			} else {
				dtde.rejectDrop();
			}
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
			if(!dtde.isDataFlavorSupported(accepted))
				dtde.rejectDrag();
		}
	};

	//
	// ClipboardOwner
	//

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) { }
}
