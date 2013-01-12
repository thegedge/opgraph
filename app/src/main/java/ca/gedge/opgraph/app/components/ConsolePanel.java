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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import ca.gedge.opgraph.Processor;

/**
 * A component to show debug information on a node.
 */
public class ConsolePanel extends JEditorPane {
	/** Strong reference to processing context's logger */
	private static final Logger processingContextLogger = Logger.getLogger(Processor.class.getName());

	/**
	 * A writer that outputs data to an HTML document.
	 */
	private static class HTMLDocumentWriter extends Writer {
		/** The HTML document to write to */
		private HTMLDocument document;

		/** The HTML class used for this writer */
		private String cssClass;

		/** The UL element storing lines */
		private Element linesElement;

		/** The string buffer used to store lines */
		private StringBuffer buffer;

		/**
		 * Constructs a writer that writes to a document.
		 * 
		 * @param document  the document this writer will write to
		 * @param cssClass  CSS class to use for this writer
		 */
		public HTMLDocumentWriter(HTMLDocument document, String cssClass) {
			super();
			this.buffer = new StringBuffer();
			this.document = document;
			this.cssClass = (cssClass == null ? "" : cssClass);
			this.linesElement = document.getElement("lines");
		}

		//
		// Writer
		//

		@Override
		public void close() throws IOException {
			this.document = null;
		}

		@Override
		public void flush() throws IOException { }

		@Override
		public void write(char [] cbuf, int off, int len) throws IOException {
			if(this.document == null)
				throw new IOException("Document is null");

			buffer.append(cbuf, off, len);

			// Write at newlines
			final int pos = buffer.indexOf("\n", buffer.length() - len);
			if(pos > 0) {
				final String innerText = buffer.substring(0, pos);
				// TODO escape html
				final String itemText = String.format("<li class=\"%s\"><pre>%s</pre></li>", cssClass, innerText);
				buffer.delete(0, pos + 1);
				try {
					document.insertBeforeEnd(linesElement, itemText);
				} catch(BadLocationException exc) {
					throw new IOException("Could not write item", exc);
				}
			}
		}
	}

	/** Regular output stream */
	private PrintWriter out;

	/** Error output stream */
	private PrintWriter err;

	/**
	 * Default constructor.
	 */
	public ConsolePanel() {
		// Set up editor kit
		final HTMLEditorKit kit = new HTMLEditorKit();
		final StyleSheet style = kit.getStyleSheet();
		style.addRule("body { padding: 0; margin: 0; }");
		style.addRule("ul { margin: 0; padding: 0; list-style-type: none; }");
		style.addRule("li { padding: 2px 5px; }");
		style.addRule(".out { font-family: Courier,Courier New,Console,System; white-space: pre; }");
		style.addRule(".err { font-family: Courier,Courier New,Console,System; color: #ff0000; background: #ffeeee; white-space: pre; }");

		// Initialize
		setEditorKit(kit);
		setEditable(false);
		setText("<html><body><ul id=\"lines\"></ul></body></html>");

		// Create streams
		final HTMLDocument document = (HTMLDocument)getDocument();
		this.out = new PrintWriter(new HTMLDocumentWriter(document, "out"));
		this.err = new PrintWriter(new HTMLDocumentWriter(document, "err"));

		//
		processingContextLogger.addHandler(consoleHandler);
	}

	/**
	 * Attaches this component to the given logger.
	 * 
	 * @param logger  the logger to attach to
	 */
	public void attachLogger(Logger logger) {
		logger.addHandler(consoleHandler);
	}

	/**
	 * Attaches this component to the given logger.
	 * 
	 * @param logger  the logger to attach to
	 */
	public void detachLogger(Logger logger) {
		logger.removeHandler(consoleHandler);
	}

	//
	// Logging handler
	//

	private final Handler consoleHandler = new Handler() {
		@Override
		public void publish(LogRecord record) {
			if(record.getLevel().intValue() >= Level.SEVERE.intValue()) {
				err.println(record.getMessage());
			} else {
				out.println(record.getMessage());
			}
		}

		@Override
		public void flush() {
			out.flush();
			err.flush();
		}

		@Override
		public void close() throws SecurityException {}
	};

	//
	// Overrides
	//

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return (getUI().getPreferredSize(this).width <= getParent().getSize().width);
	}
}
