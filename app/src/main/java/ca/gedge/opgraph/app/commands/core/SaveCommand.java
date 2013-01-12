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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.GraphEditorModel;
import ca.gedge.opgraph.app.components.ErrorDialog;
import ca.gedge.opgraph.io.OpGraphSerializer;
import ca.gedge.opgraph.io.OpGraphSerializerFactory;
import ca.gedge.opgraph.io.OpGraphSerializerInfo;

/**
 * A command that saves the active graph and saves it to disk.
 */
public class SaveCommand extends AbstractAction {
	private static final Logger LOGGER = Logger.getLogger(SaveCommand.class.getName());

	/** Whether or not to force the save dialog */
	private boolean forceDialog;

	/**
	 * Constructs a save command that does not force the dialog.
	 */
	public SaveCommand() {
		this(false);
	}

	/**
	 * Constructs a save command.
	 * 
	 * @param forceDialog  if <code>true</code>, a save dialog will always be
	 *                     shown. If <code>false</code>, a save dialog is shown
	 *                     only if the file is currently not saved to disk.
	 */
	public SaveCommand(boolean forceDialog) {
		super(forceDialog ? "Save As..." : "Save");

		this.forceDialog = forceDialog;

		int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		if(forceDialog)
			modifiers |= InputEvent.SHIFT_MASK;

		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, modifiers));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final GraphDocument document = GraphEditorModel.getActiveDocument();
		if(document != null) {
			File saveFile = document.getSource();
			if(forceDialog)
				saveFile = null;
			saveDocument(document, saveFile);
		}
	}

	/**
	 * Saves an application model to a file. 
	 * 
	 * @param model  the model to save
	 * @param saveFile  The file to save to. if <code>null</code>, a dialog is
	 *                  popped up asking the user to select a file to save to.
	 * 
	 * @return <code>true</code> if the model was successfully saved to file,
	 *         in which case the model's source is set to the selected file.
	 *         <code>false</code> otherwise. 
	 */
	public static boolean saveDocument(GraphDocument model, File saveFile) {
		// Get the serializer
		final OpGraphSerializer serializer = OpGraphSerializerFactory.getDefaultSerializer();
		if(serializer == null) {
			final String message = "No default serializer available";
			LOGGER.severe(message);
			ErrorDialog.showError(message);
			return false;
		}

		// Get the serializer's info
		String description = "Opgraph Files";
		String extension = "";

		final OpGraphSerializerInfo info = serializer.getClass().getAnnotation(OpGraphSerializerInfo.class);
		if(info != null) {
			description = info.description();
			extension = info.extension();
		}

		// Find a save file
		boolean ret = true;
		if(model != null && model.getGraph() != null && (saveFile == null || model.hasModifications())) {
			ret = false;
			if(saveFile == null) {
				final JFileChooser chooser = new JFileChooser();
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(new FileNameExtensionFilter(description, extension));
				chooser.setDialogTitle("Save Visual Graph");

				// Loop for picking file
				boolean running = true;
				do {
					final int retVal = chooser.showSaveDialog(null);
					if(retVal == JFileChooser.APPROVE_OPTION) {
						saveFile = chooser.getSelectedFile();
						if(chooser.getSelectedFile().exists()) {
							final int overwriteVal =
									JOptionPane.showConfirmDialog(null, 
									                              "Overwrite selected file?",
									                              "Overwrite File",
									                              JOptionPane.YES_NO_CANCEL_OPTION,
									                              JOptionPane.QUESTION_MESSAGE);
							//
							//    Yes - overwrite file
							//     No - don't overwrite file, but ask for another file
							// Cancel - don't overwrite file, don't ask for another file
							//
							switch(overwriteVal) {
							case JOptionPane.YES_OPTION:
								running = false;
								break;
							case JOptionPane.NO_OPTION:
								saveFile = null;
								break;
							case JOptionPane.CANCEL_OPTION:
								saveFile = null;
								running = false;
								break;
							}
						} else running = false;
					} else running = false;
				} while(running);
			}
		}

		// If a non-null file, save the graph to disk
		if(saveFile != null) {
			try {
				// serialize xml into an in-memory stream
				final ByteArrayOutputStream bout = new ByteArrayOutputStream();
				serializer.write(model.getGraph(), bout);

				// assume overwrite warning was issued and accepted...
				final FileOutputStream stream = new FileOutputStream(saveFile);
				stream.write(bout.toByteArray());
				stream.flush();
				stream.close();

				model.setSource(saveFile);
				model.markAsUnmodified();

				ret = true;
			} catch(IOException exc) {
				final String message = "Could not save graph to file: " + exc.getLocalizedMessage();
				LOGGER.severe(message);
				ErrorDialog.showError(exc, message);
			}
		}

		return ret;
	}
}
