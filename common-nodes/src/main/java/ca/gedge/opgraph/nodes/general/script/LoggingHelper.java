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
package ca.gedge.opgraph.nodes.general.script;

import java.util.logging.Level;
import java.util.logging.Logger;

import ca.gedge.opgraph.Processor;

/**
 */
public class LoggingHelper {
	@SuppressWarnings("unused")
	private static class ScriptPrinter {
		private final Logger LOGGER = Logger.getLogger(Processor.class.getName());

		/** The level for logged messages */
		private Level level;

		/**
		 * Constructs a script printer that logs message at the given level.
		 * 
		 * @param level  the level of logs
		 */
		public ScriptPrinter(Level level) {
			this.level = level;
		}

		/**
		 * Prints a given message with a new line.
		 * 
		 * @param message  the message
		 */
		public void println(Object message) {
			String stringMessage = (message == null ? "null" : message.toString());
			LOGGER.log(level, stringMessage + "\n");
		}

		/**
		 * Prints a given message.
		 * 
		 * @param message  the message
		 */
		public void print(Object message) {
			String stringMessage = (message == null ? "null" : message.toString());
			LOGGER.log(level, stringMessage);
		}

		/**
		 * Formats a set of objects given a specified format string.
		 * 
		 * @param format  the format string
		 * @param args  the arguments to use in the format string
		 */
		public void format(String format, Object... args) {
			LOGGER.log(level, String.format(format, args));
		}
	}

	/** STDOUT printer */
	public final ScriptPrinter out = new ScriptPrinter(Level.INFO);

	/** STDERR printer */
	public final ScriptPrinter err = new ScriptPrinter(Level.SEVERE);
}
