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

import static ca.gedge.opgraph.io.xml.XMLSerializerFactory.*;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.gedge.opgraph.ContextualItem;
import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.extensions.Extendable;

/**
 * A default serializer for reading/writing {@link InputField}s and
 * {@link OutputField}s to/from XML.
 */
public class DefaultFieldXMLSerializer implements XMLSerializer {
	// qualified names
	static final QName INPUT_QNAME = new QName(DEFAULT_NAMESPACE, "input", XMLConstants.DEFAULT_NS_PREFIX);
	static final QName OUTPUT_QNAME = new QName(DEFAULT_NAMESPACE, "output", XMLConstants.DEFAULT_NS_PREFIX);

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc, Element parentElem, Object obj) 
		throws IOException 
	{
		if(obj == null)
			throw new IOException("Null object given to serializer");

		if(obj instanceof InputField) {
			final InputField field = (InputField)obj;

			// Only write if field is non-fixed, or fixed but with extensions
			if(!field.isFixed() || field.getExtensionClasses().size() > 0) {
				final Element fieldElem = doc.createElementNS(INPUT_QNAME.getNamespaceURI(), INPUT_QNAME.getLocalPart());
				fieldElem.setAttribute("key", field.getKey());
				fieldElem.setAttribute("optional", Boolean.toString(field.isOptional()));
				fieldElem.setTextContent(field.getDescription());

				// XXX Store type validators?

				// Extensions
				if(field.getExtensionClasses().size() > 0) {
					final XMLSerializer serializer = serializerFactory.getHandler(Extendable.class);
					if(serializer == null)
						throw new IOException("No XML serializer for extensions");

					serializer.write(serializerFactory, doc, fieldElem, field);
				}

				parentElem.appendChild(fieldElem);
			}
		} else if(obj instanceof OutputField) {
			final OutputField field = (OutputField)obj;

			// Only write if field is non-fixed, or fixed but with extensions
			if(!field.isFixed() || field.getExtensionClasses().size() > 0) {
				final Element fieldElem = doc.createElementNS(OUTPUT_QNAME.getNamespaceURI(), OUTPUT_QNAME.getLocalPart());
				fieldElem.setAttribute("key", field.getKey());
				fieldElem.setAttribute("type", field.getOutputType().getName());
				fieldElem.setTextContent(field.getDescription());

				// Extensions
				if(field.getExtensionClasses().size() > 0) {
					final XMLSerializer serializer = serializerFactory.getHandler(Extendable.class);
					if(serializer == null)
						throw new IOException("No XML serializer for extensions");

					serializer.write(serializerFactory, doc, fieldElem, field);
				}

				parentElem.appendChild(fieldElem);
			}
		} else {
			throw new IOException(DefaultFieldXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());
		}
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem)
		throws IOException 
	{
		ContextualItem item = null;
		if(INPUT_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			// Create
			final String key = elem.getAttribute( "key");
			final String description = elem.getTextContent();
			final InputField field = new InputField(key, description);

			if(elem.hasAttribute("optional"))
				field.setOptional(Boolean.parseBoolean(elem.getAttributeNS(DEFAULT_NAMESPACE, "optional")));

			// XXX Read type validator?

			// Read children
			final NodeList children = elem.getChildNodes();
			for(int childIndex = 0; childIndex < children.getLength(); ++childIndex) {
				final Node node = children.item(childIndex);
				if(node instanceof Element) {
					final Element childElem = (Element)node;
					final QName name = XMLSerializerFactory.getQName(childElem);

					// Get a handler for the element
					final XMLSerializer serializer = serializerFactory.getHandler(name);
					if(serializer == null)
						throw new IOException("Could not get handler for element: " + name);
				}
			}

			item = field;
		} else if(OUTPUT_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			// Create
			Class<?> outputType = Object.class;
			if(elem.hasAttribute("type")) {
				final String outputTypeClassName = elem.getAttribute("type");
				try {
					outputType = Class.forName(outputTypeClassName);
				} catch(ClassNotFoundException exc) {
					throw new IOException("Unknown output type for field: " + outputTypeClassName);
				}
			}

			final String key = elem.getAttribute("key");
			final String description = elem.getTextContent();
			final OutputField field = new OutputField(key, description, false, outputType);

			// Read children
			final NodeList children = elem.getChildNodes();
			for(int childIndex = 0; childIndex < children.getLength(); ++childIndex) {
				final Node node = children.item(childIndex);
				if(node instanceof Element) {
					final Element childElem = (Element)node;
					final QName name = XMLSerializerFactory.getQName(childElem);

					// Get a handler for the element
					final XMLSerializer serializer = serializerFactory.getHandler(name);
					if(serializer == null)
						throw new IOException("Could not get handler for element: " + name);
				}
			}

			item = field;
		}

		return item;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return ((cls == InputField.class) || (cls == OutputField.class));
	}

	@Override
	public boolean handles(QName name) {
		return (INPUT_QNAME.equals(name) || OUTPUT_QNAME.equals(name));
	}
}
