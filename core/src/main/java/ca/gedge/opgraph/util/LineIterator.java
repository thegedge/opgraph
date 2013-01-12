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
package ca.gedge.opgraph.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Iterates over the lines of an input stream.
 */
public class LineIterator implements Iterator<String> {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(LineIterator.class.getName());

	/** Buffered reader from which lines will come from */
	private BufferedReader br;

	/** Current line */
	private String line;

	/**
	 * Constructs an iterator that will iterate over the lines in a given
	 * input stream.
	 *  
	 * @param is  the input stream
	 */
	public LineIterator(InputStream is) {
		this.br = new BufferedReader(new InputStreamReader(is));
	}

	/**
	 * Constructs an iterator that will iterate over the lines in a given
	 * buffered reader.
	 *  
	 * @param br  the buffered reader
	 */
	public LineIterator(BufferedReader br) {
		this.br = br;
	}

	/**
	 * Constructs an iterator that will iterate over the lines in a given URL.
	 *  
	 * @param url  the url
	 */
	public LineIterator(URL url) {
		try {
			this.br = new BufferedReader(new InputStreamReader(url.openStream()));
		} catch(IOException exc) {
			LOGGER.warning("Could not open line iterator stream for url: " + url);
		}
	}

	//
	// Iterator
	//

	@Override
	public boolean hasNext() {
		if(br != null && line == null) {
			try {
				line = br.readLine();
			} catch(IOException exc) {
				br = null;
				LOGGER.warning("IOException when attempting to read line. Closing reader...");
			}
		}

		return (line != null);
	}

	@Override
	public String next() {
		final String ret = line;
		line = null;
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove not supported");
	}
}
