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
package ca.gedge.opgraph.app.commands.core;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.components.ErrorDialog;
import ca.gedge.opgraph.io.OpGraphSerializer;
import ca.gedge.opgraph.io.OpGraphSerializerFactory;
import ca.gedge.opgraph.io.OpGraphSerializerInfo;

/**
 * A command which loads a graph from file.
 */
public class OpenCommand extends AbstractAction {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(OpenCommand.class.getName());

	/** File chooser */
	private final JFileChooser chooser = new JFileChooser();

	/**
	 * Constructs an open command.
	 */
	public OpenCommand() {
		super("Open...");

		final int CTRL = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, CTRL));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Get the serializer
		final OpGraphSerializer serializer = OpGraphSerializerFactory.getDefaultSerializer();
		if(serializer == null) {
			final String message = "No default serializer available";
			LOGGER.severe(message);
			ErrorDialog.showError(null, message);
			return;
		}

		// Get the serializer's info
		String description = "Opgraph Files";
		String extension = "";

		final OpGraphSerializerInfo info = serializer.getClass().getAnnotation(OpGraphSerializerInfo.class);
		if(info != null) {
			description = info.description();
			extension = info.extension();
		}

		// Save the graph
		final GraphDocument document = GraphEditorModel.getActiveDocument();
		if(document != null) {
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileFilter(new FileNameExtensionFilter(description, extension));
			chooser.setDialogTitle("Open Graph");

			final int retVal = chooser.showOpenDialog(null);
			if(retVal == JFileChooser.APPROVE_OPTION) {
				try {
					final FileInputStream stream = new FileInputStream(chooser.getSelectedFile());
					final OpGraph graph = serializer.read(stream);
					document.reset(chooser.getSelectedFile(), graph);
				} catch(IOException exc) {
					LOGGER.severe("Could not read graph from file: " + exc.getMessage());
				}
			}
		}
	}
}
