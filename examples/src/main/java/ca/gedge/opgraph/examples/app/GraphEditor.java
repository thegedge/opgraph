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
package ca.gedge.opgraph.examples.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.MenuProvider;
import ca.gedge.opgraph.app.components.PathAddressableMenuImpl;
import ca.gedge.opgraph.app.components.canvas.GridLayer;

/**
 * A frame holding a {@link GraphEditor}.
 */
public class GraphEditor extends JFrame {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(GraphEditor.class.getName());

	/** The graph editor model */
	private GraphEditorModel model; 

	/** A reference to the menu bar */
	private PathAddressableMenuImpl menuBar;

	/** Container component containing debug components */
	private JComponent debugComponents;

	/**
	 * Default constructor.
	 */
	GraphEditor() {
		super("OpGraph Editor");

		// Initialize model and document
		this.model = new GraphEditorModel();
		GraphEditorModel.setActiveEditorModel(model);

		// Set up components
		setupWindow();

		// Menu bar
		final JMenuBar mb = new JMenuBar();
		menuBar = new PathAddressableMenuImpl(mb);
		for(MenuProvider menu : model.getMenuProviders())
			menu.installItems(model, menuBar);

		setJMenuBar(mb);

		// Root pane state
		final GraphDocument document = model.getCanvas().getDocument();
		document.addPropertyChangeListener(GraphDocument.SOURCE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(getRootPane() != null)
					getRootPane().putClientProperty("Window.documentFile", evt.getNewValue());
			}
		});

		document.addPropertyChangeListener(GraphDocument.UNDO_STATE, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				getRootPane().putClientProperty("Window.documentModified", document.hasModifications());
			}
		});

		document.addPropertyChangeListener(GraphDocument.PROCESSING_CONTEXT, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				debugComponents.setVisible(evt.getNewValue() != null);
			}
		});

		// 
		setSize(1000, 650);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(windowAdapter);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				model.getCanvas().requestFocusInWindow();
			}
		});
	}

	/**
	 * Initializes the docking window.
	 */
	private void setupWindow() {
		// Scroll pane for canvas
		final JScrollPane canvasScrollPane = new JScrollPane();
		canvasScrollPane.setViewportView(model.getCanvas());
		canvasScrollPane.setColumnHeaderView(model.getBreadcrumb());
		canvasScrollPane.setBorder(null);

		canvasScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		canvasScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		canvasScrollPane.getHorizontalScrollBar().setUnitIncrement(GridLayer.DEFAULT_GRID_SPACING / 10);
		canvasScrollPane.getHorizontalScrollBar().setBlockIncrement(GridLayer.DEFAULT_GRID_SPACING);
		canvasScrollPane.getVerticalScrollBar().setUnitIncrement(GridLayer.DEFAULT_GRID_SPACING / 10);
		canvasScrollPane.getVerticalScrollBar().setBlockIncrement(GridLayer.DEFAULT_GRID_SPACING);

		// Scroll pane for console
		final JScrollPane consolePane = new JScrollPane(model.getConsolePanel());
		consolePane.setBorder(null);

		// Scroll pane for debug
		final JScrollPane debugScrollPane = new JScrollPane(model.getDebugInfoPanel());
		debugScrollPane.setBorder(null);

		// Tabbed pane for debug components
		final JTabbedPane debugTabPane = new JTabbedPane();
		debugTabPane.add("Debug", debugScrollPane);
		debugTabPane.add("Console", consolePane);
		debugTabPane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.LIGHT_GRAY));
		debugTabPane.setPreferredSize(new Dimension(300, 1));

		debugComponents = debugTabPane;
		debugComponents.setVisible(false);

		// Tabbed pane for debug components
		final JTabbedPane nodeTabPane = new JTabbedPane();
		nodeTabPane.add("Defaults", model.getNodeDefaults());
		nodeTabPane.add("Settings", model.getNodeSettings());
		nodeTabPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
		nodeTabPane.setPreferredSize(new Dimension(300, 1));

		// Node library, change border
		final JComponent nodeLibrary = model.getNodeLibrary();
		nodeLibrary.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

		// Layout top
		final JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(nodeTabPane, BorderLayout.WEST);
		topPanel.add(canvasScrollPane, BorderLayout.CENTER);
		topPanel.add(debugTabPane, BorderLayout.EAST);

		// Main split pane for top elements and library viewer
		final JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setBorder(null);
		mainSplitPane.setTopComponent(topPanel);
		mainSplitPane.setBottomComponent(nodeLibrary);
		mainSplitPane.setDividerSize(3);
		mainSplitPane.setDividerLocation(400);

		// Add to frame
		add(mainSplitPane, BorderLayout.CENTER);
	}

	//
	// WindowAdapter
	//

	private final WindowAdapter windowAdapter = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			final GraphDocument document = model.getCanvas().getDocument();
			if(document.checkForReset()) {
				GraphEditor.this.setVisible(false);
				GraphEditor.this.dispose();
			}
		}
	};

	/**
	 * Program entry point.
	 * 
	 * @param args  program arguments
	 */
	public static void main(String [] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch(ClassNotFoundException exc) {
					LOGGER.warning("Unable to set LaF");
				} catch(InstantiationException exc) {
					LOGGER.warning("Unable to set LaF");
				} catch(IllegalAccessException exc) {
					LOGGER.warning("Unable to set LaF");
				} catch(UnsupportedLookAndFeelException exc) {
					LOGGER.warning("Unable to set LaF");
				}

				final JFrame frame = new GraphEditor();
				frame.setLocationByPlatform(true);
				frame.setVisible(true);
			}
		});
	}
}
