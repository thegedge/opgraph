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
package ca.gedge.opgraph.io.xml;

import java.io.IOException;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ca.gedge.opgraph.OpGraph;

/**
 * An extension for classes that require custom XML serialization.
 */
public interface XMLSerializer {	
	/**
	 * Writes an object to a parent element.
	 * 
	 * @param serializerFactory  a factory to fetch XML serializers
	 * @param doc  the DOM document
	 * @param parentElem  the parent DOM element
	 * @param obj  the object to write
	 * 
	 * @throws IOException  if the given object cannot be written by this handler
	 */
	public abstract void write(XMLSerializerFactory serializerFactory,
	                           Document doc,
	                           Element parentElem,
	                           Object obj) throws IOException;

	/**
	 * Reads an object from an XML event stream.
	 * 
	 * @param serializerFactory  a factory to fetch XML serializers
	 * @param graph  the graph currently being read
	 * @param parent  the parent object from which reading occured
	 * @param doc  the DOM document
	 * @param elem  the parent DOM element
	 * 
	 * @return the object described in the given element
	 * 
	 * @throws IOException  if the given stream does not contain XML data
	 *                      which this handler understands
	 */
	public abstract Object read(XMLSerializerFactory serializerFactory,
	                            OpGraph graph,
	                            Object parent,
	                            Document doc,
	                            Element elem) throws IOException;

	/**
	 * Gets whether or not this serializer writes the given class.
	 * 
	 * @param cls  the class to check
	 * 
	 * @return <code>true</code> if this serializer can write the given class,
	 *         <code>false</code> otherwise
	 */
	public abstract boolean handles(Class<?> cls);

	/**
	 * Gets whether or not this serializer can read a given qualified name.
	 * 
	 * @param name  the qualified name to check
	 * 
	 * @return <code>true</code> if this serializer can read the given
	 *         qualified name, <code>false</code> otherwise
	 */
	public abstract boolean handles(QName name);
}
