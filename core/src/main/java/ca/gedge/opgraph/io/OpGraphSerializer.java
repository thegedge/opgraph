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
package ca.gedge.opgraph.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ca.gedge.opgraph.OpGraph;

/**
 * An interface for any class providing OpGraph serialization services.
 */
public interface OpGraphSerializer {
	/**
	 * Writes a graph to a stream.
	 * 
	 * @param graph  the graph to write
	 * @param stream  the stream to write to
	 * 
	 * @throws IOException  if any I/O errors occur
	 */
	public abstract void write(OpGraph graph, OutputStream stream) throws IOException;
	
	/**
	 * Reads a graph from a given stream.
	 * 
	 * @param stream  the stream to read from
	 * 
	 * @return the {@link OpGraph} that was read from the given stream
	 * 
	 * @throws IOException  if any I/O errors occur
	 */
	public abstract OpGraph read(InputStream stream) throws IOException;
}
