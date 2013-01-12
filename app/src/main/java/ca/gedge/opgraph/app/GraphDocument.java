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
package ca.gedge.opgraph.app;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.app.commands.core.SaveCommand;
import ca.gedge.opgraph.app.components.canvas.GraphCanvas;
import ca.gedge.opgraph.app.components.canvas.GraphCanvasSelectionModel;
import ca.gedge.opgraph.app.extensions.NodeMetadata;
import ca.gedge.opgraph.extensions.CompositeNode;
import ca.gedge.opgraph.util.Breadcrumb;

/**
 * Document model used for graphs.
 */
public class GraphDocument {
	/** Key for the processing context property */
	public static final String PROCESSING_CONTEXT = "processor";

	/** Key for the processing source */
	public static final String SOURCE = "source";

	/** Key for the processing undo state property */
	public static final String UNDO_STATE = "undoState";

	/** Active canvas */
	private final WeakReference<GraphCanvas> canvas; // REMOVEME eventually

	/** The selection model this canvas uses */
	private GraphCanvasSelectionModel selectionModel;

	/** The set of models this canvas is displaying */
	private Breadcrumb<OpGraph, String> breadcrumb;

	/** Undo manager for the application */
	private UndoManager undoManager;

	/** The source file, or <code>null</code> if not editing a file */
	private File source;

	/** The processing context for the currently viewed graph */
	private Processor processor; // XXX should this be here or in GraphEditorModel?

	/** Support for undoable edits */
	private UndoableEditSupport undoSupport;

	/** Support for property changes */
	private PropertyChangeSupport changeSupport;

	/**
	 * Constructs a graph document.
	 * 
	 * @param canvas  the canvas that is displaying this document
	 */
	public GraphDocument(GraphCanvas canvas) {
		this.canvas = new WeakReference<GraphCanvas>(canvas);
		this.selectionModel = new GraphCanvasSelectionModel();
		this.breadcrumb = new Breadcrumb<OpGraph, String>();

		this.undoManager = new UndoManager() {
			@Override
			public void undoableEditHappened(UndoableEditEvent e) {
				super.undoableEditHappened(e);
				changeSupport.firePropertyChange(UNDO_STATE, null, this);
			}

			@Override
			public synchronized void discardAllEdits() {
				if(super.canUndoOrRedo()) {
					super.discardAllEdits();
					changeSupport.firePropertyChange(UNDO_STATE, null, this);
				}
			}

			@Override
			public synchronized void undo() throws CannotUndoException {
				if(super.canUndo()) {
					super.undo();
					changeSupport.firePropertyChange(UNDO_STATE, null, this);
				}
			}

			@Override
			public synchronized void redo() throws CannotRedoException {
				if(super.canRedo()) {
					super.redo();
					changeSupport.firePropertyChange(UNDO_STATE, null, this);
				}
			}

			@Override
			public synchronized void undoOrRedo() throws CannotRedoException, CannotUndoException {
				if(super.canUndoOrRedo()) {
					super.undoOrRedo();
					changeSupport.firePropertyChange(UNDO_STATE, null, this);
				}
			}
		};
		this.undoManager.setLimit(500);
		this.undoSupport = new UndoableEditSupport();
		this.undoSupport.addUndoableEditListener(undoManager);
		this.changeSupport = new PropertyChangeSupport(this);

		// Reset for freshnesss
		reset(null, null);
	}

	/**
	 * Gets whether or not the model has modifications.
	 * 
	 * @return <code>true</code> if there are modifications to this model,
	 *         <code>false</code> otherwise
	 */
	public boolean hasModifications() {
		return undoManager.canUndo();
	}

	/**
	 * Checks to see if the graph can be reset without losing any modifications.
	 * 
	 * @return <code>true</code> if the graph can be reset without losing
	 *         any modifications, <code>false</code> otherwise.  
	 */
	public boolean checkForReset() {
		// If unsaved changes, ask the user if s/he would like to save 
		boolean canReset = true;
		if(hasModifications()) {
			final int retVal =
				JOptionPane.showConfirmDialog(null,
				                              "There are unsaved changes in the current graph. Would you like to save these changes?",
				                              "Save Current Graph",
				                              JOptionPane.YES_NO_CANCEL_OPTION,
				                              JOptionPane.QUESTION_MESSAGE);

			if(retVal == JOptionPane.YES_OPTION) {
				canReset = SaveCommand.saveDocument(this, source);
			} else if(retVal == JOptionPane.CANCEL_OPTION) {
				canReset = false;
			}
		}

		return canReset;
	}

	/**
	 * Reset the state of the application to a new graph.
	 * 
	 * @param source  the file the graph was read from, or <code>null</code>
	 *                if the graph wasn't read from a file
	 * @param root  the graph to now use as root
	 */
	public void reset(File source, OpGraph root) {
		if(root == null) root = new OpGraph();

		if(checkForReset()) {
			root.setId("root");

			setSource(source);
			setProcessingContext(null);

			this.breadcrumb.clear();
			this.breadcrumb.addState(root, "root");
			this.undoManager.discardAllEdits();

			markAsUnmodified();
		}
	}

	/**
	 * @return the breadcrumb
	 */
	public Breadcrumb<OpGraph, String> getBreadcrumb() {
		return breadcrumb;
	}

	/**
	 * @return the graph
	 */
	public OpGraph getGraph() {
		return breadcrumb.getCurrentState();
	}

	/**
	 * @return the canvas
	 */
	public GraphCanvas getCanvas() {
		return canvas.get();
	}

	/**
	 * @return the selectionModel
	 */
	public GraphCanvasSelectionModel getSelectionModel() {
		return selectionModel;
	}

	/**
	 * @return the undo
	 */
	public UndoManager getUndoManager() {
		return undoManager;
	}

	/**
	 * @return the source
	 */
	public File getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(File source) {
		if(this.source != source) {
			final File oldSource = this.source;
			this.source = source;
			changeSupport.firePropertyChange(SOURCE, oldSource, source);
		}
	}

	/**
	 * @return the undoSupport
	 */
	public UndoableEditSupport getUndoSupport() {
		return undoSupport;
	}

	/**
	 * @return the processor
	 */
	public Processor getProcessingContext() {
		return processor;
	}

	/**
	 * @param processor the processor to set
	 */
	public void setProcessingContext(Processor processor) {
		if(this.processor != processor) {
			final Processor oldContext = this.processor;
			this.processor = processor;

			// Setup context for default values
			if(processor != null) {
				OpContext context = new OpContext();
				installNodeDefaults(processor.getGraph(), context);
				processor.reset(context);
			}

			changeSupport.firePropertyChange(PROCESSING_CONTEXT, oldContext, processor);
		}
	}

	/**
	 * Marks the model as unmodified in its current state.
	 * 
	 * Note that currently this will discard all undoable edits!!!
	 */
	public void markAsUnmodified() {
		undoManager.discardAllEdits();
	}

	/**
	 * Install default values into a given context.
	 * 
	 * @param graph  the graph to operator on
	 * @param context  the context to install default values into
	 */
	private void installNodeDefaults(OpGraph graph, OpContext context) {
		for(OpNode node : graph.getVertices()) {
			// Add defaults, if any exist
			final NodeMetadata meta = node.getExtension(NodeMetadata.class);
			if(meta != null) {
				for(Map.Entry<InputField, Object> entry : meta.getDefaults().entrySet())
					context.getChildContext(node).put(entry.getKey(), entry.getValue());
			}

			// If composite, recursively descend
			final CompositeNode composite = node.getExtension(CompositeNode.class);
			if(composite != null)
				installNodeDefaults(composite.getGraph(), context.getChildContext(node));
		}
	}

	/**
	 * Call whenever the debug state changes on a processing context.
	 * 
	 * @param processor  the processing context
	 */
	public void updateDebugState(Processor processor) {
		if(processor != null && this.processor == processor)
			changeSupport.firePropertyChange(PROCESSING_CONTEXT, new Object(), processor);
	}

	//
	// Property changes
	//

	/**
	 * Adds a property change listener to this document.
	 * 
	 * @param listener  the listener to add
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Adds a property change listener for a specific property to this document.
	 * 
	 * @param property  the property name
	 * @param listener  the listener to add
	 */
	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(property, listener);
	}

	/**
	 * Removes a property change listener from this document.
	 * 
	 * @param listener  the listener to remove
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * Removes a property change listener for a specific property from this document.
	 * 
	 * @param property  the property name
	 * @param listener  the listener to remove
	 */
	public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(property, listener);
	}
}
