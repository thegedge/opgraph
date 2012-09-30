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
package ca.gedge.opgraph.app.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * A dialog that displays an exception to the user. The dialog initially shows
 * a simple message explaining to the user that an error occurred, and the user
 * has the option to expand and see the stack trace.
 * 
 * TODO submit an error report (to github issues?)
 * TODO option to not show errors of a specific type again
 */
public class ErrorDialog extends JDialog {
	/**
	 * Displays a modal {@link ErrorDialog} for the given {@link Throwable}.
	 *  
	 * @param thrown  the {@link Throwable} to show
	 * 
	 * @see ErrorDialog#ErrorDialog(Throwable)
	 */
	public static void showError(Throwable thrown) {
		showError(thrown, null);
	}
	
	/**
	 * Displays a modal {@link ErrorDialog} with a given message.
	 *  
	 * @param message  the message to show 
	 * 
	 * @see ErrorDialog#ErrorDialog(String)
	 */
	public static void showError(String message) {
		showError(null, message);
	}
	
	/**
	 * Displays a modal {@link ErrorDialog} for the given {@link Throwable}
	 * and with a given message.
	 *  
	 * @param thrown  the {@link Throwable} to show
	 * @param message the message to show
	 * 
	 * @see ErrorDialog#ErrorDialog(Throwable, String)
	 */
	public static void showError(Throwable thrown, String message) {
		final ErrorDialog dialog = new ErrorDialog(thrown);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setLocationByPlatform(true);
		dialog.setModal(true);
		dialog.setVisible(true);
	}
	
	/** The {@link Throwable} this dialog is displaying */
	private Throwable thrown;
	
	/** The error message to display */
	private String message;
	
	/**
	 * Constructs an exception dialog from the given {@link Throwable} and
	 * with a default error message.
	 * 
	 * @param thrown  the {@link Throwable} to display in the dialog
	 */
	public ErrorDialog(Throwable thrown) {
		this(thrown, null);
	}

	/**
	 * Constructs an exception dialog with the given message.
	 * 
	 * @param message  the message to show
	 */
	public ErrorDialog(String message) {
		this(null, message);
	}

	/**
	 * Constructs an exception dialog from the given {@link Throwable}.
	 * 
	 * @param thrown  the {@link Throwable} to display in the dialog, or
	 *                <code>null</code> to show without an exception
	 * @param message  the error message to display, or a default message if
	 *                 <code>null</code>/empty. If <code>thrown</code> is not
	 *        <code>null</code> then <code>thrown.getLocalizedMessage()</code>
	 *        will be the default message if it is not <code>null</code>.
	 *        Otherwise, a fixed message will be used.
	 */
	public ErrorDialog(Throwable thrown, String message) {
		super(null, "Application Error", ModalityType.DOCUMENT_MODAL);

		this.thrown = thrown;
		this.message = (message == null ? "" : message.trim());
		if(message.length() == 0)
			this.message = "An error has occurred";

		initializeComponents();
	}

	/**
	 * Initialize the components in this dialog.
	 */
	private void initializeComponents() {
		setLayout(new BorderLayout(5, 5));
		setSize(600, 400);
		setResizable(false);
		getRootPane().setBorder(new EmptyBorder(5, 5, 5, 5));

		// Create error message label
		final Icon errorIcon = UIManager.getIcon("OptionPane.errorIcon");
		final JLabel errorLabel = new JLabel(message, errorIcon, SwingConstants.LEADING);
		add(errorLabel, BorderLayout.NORTH);

		// If we were given a Throwable, show its stack trace to the user
		if(thrown != null) {
			// Get the stack trace as a string
			final StringWriter stringWriter = new StringWriter(); 
			final PrintWriter stackTraceWriter = new PrintWriter(stringWriter);
			thrown.printStackTrace(stackTraceWriter);
			final String stackTraceString = stringWriter.toString(); 

			// Stack trace label
			final JLabel stackTraceLabel = new JLabel("<html><pre>" + stackTraceString + "</pre></html>");
			final JScrollPane stackTraceScrollPane = new JScrollPane(stackTraceLabel);
			stackTraceScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			stackTraceScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			add(stackTraceScrollPane, BorderLayout.CENTER);
		}

		// Create buttons
		final JButton okButton = new JButton(new AbstractAction("Ok") {
			@Override
			public void actionPerformed(ActionEvent e) {
				ErrorDialog.this.setVisible(false);
			}
		});

		// Create a panel for buttons
		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(okButton);
		add(buttonPanel, BorderLayout.SOUTH);

		okButton.requestFocusInWindow();
	}
}
