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
package ca.gedge.opgraph.nodes.xml;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.extensions.Extendable;
import ca.gedge.opgraph.extensions.Publishable.PublishedInput;
import ca.gedge.opgraph.extensions.Publishable.PublishedOutput;
import ca.gedge.opgraph.io.xml.XMLSerializer;
import ca.gedge.opgraph.io.xml.XMLSerializerFactory;
import ca.gedge.opgraph.nodes.general.MacroNode;

/**
 * A default serializer for reading/writing published {@link InputField}s and
 * {@link OutputField}s to/from XML.
 */
public class PublishedFieldXMLSerializer implements XMLSerializer {
	static final String NAMESPACE = "http://www.gedge.ca/ns/common-nodes";
	static final String PREFIX = "ogcn";

	// qualified names
	static final QName INPUT_QNAME = new QName(NAMESPACE, "published_input", PREFIX);
	static final QName OUTPUT_QNAME = new QName(NAMESPACE, "published_output", PREFIX);

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc, Element parentElem, Object obj) 
		throws IOException 
	{
		if(obj == null)
			throw new IOException("Null object given to serializer");

		// setup namespace for document
		final Element rootEle = doc.getDocumentElement();
		rootEle.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
				XMLConstants.XMLNS_ATTRIBUTE + ":" + PREFIX, NAMESPACE);

		if(obj instanceof PublishedInput) {
			final PublishedInput field = (PublishedInput)obj;

			// Only write if field is non-fixed, or fixed but with extensions
			final Element fieldElem = doc.createElementNS(NAMESPACE, PREFIX + ":" + INPUT_QNAME.getLocalPart());
			fieldElem.setAttribute("key", field.getKey());
			fieldElem.setAttribute("dest", field.destinationNode.getId());
			fieldElem.setAttribute("destField", field.nodeInputField.getKey());

			// Extensions
			if(field.getExtensionClasses().size() > 0) {
				final XMLSerializer serializer = serializerFactory.getHandler(Extendable.class);
				if(serializer == null)
					throw new IOException("No XML serializer for extensions");

				serializer.write(serializerFactory, doc, fieldElem, field);
			}

			parentElem.appendChild(fieldElem);
		} else if(obj instanceof PublishedOutput) {
			final PublishedOutput field = (PublishedOutput)obj;

			// Only write if field is non-fixed, or fixed but with extensions
			final Element fieldElem = doc.createElementNS(NAMESPACE, PREFIX + ":" + INPUT_QNAME.getLocalPart());
			fieldElem.setAttribute("key", field.getKey());
			fieldElem.setAttribute("source", field.sourceNode.getId());
			fieldElem.setAttribute("sourceField", field.nodeOutputField.getKey());

			// Extensions
			if(field.getExtensionClasses().size() > 0) {
				final XMLSerializer serializer = serializerFactory.getHandler(Extendable.class);
				if(serializer == null)
					throw new IOException("No XML serializer for extensions");

				serializer.write(serializerFactory, doc, fieldElem, field);
			}

			parentElem.appendChild(fieldElem);
		} else {
			throw new IOException(PublishedFieldXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());
		}
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem)
		throws IOException 
	{
		if(INPUT_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			if(!(parent instanceof MacroNode))
				throw new IOException("Trying to read published field, but parent object is not a macro");

			// Find published info
			final String key = elem.getAttribute("key");
			final String destNodeId = elem.getAttribute("dest");
			final String destFieldKey = elem.getAttribute("destField");

			final MacroNode macro = (MacroNode)parent;
			final OpNode destNode = macro.getGraph().getNodeById(destNodeId, true);
			final InputField destField = destNode.getInputFieldWithKey(destFieldKey);

			// Create
			final InputField published = macro.publish(key, destNode, destField);

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

					serializer.read(serializerFactory, graph, published, doc, childElem);
				}
			}
		} else if(OUTPUT_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			if(!(parent instanceof MacroNode))
				throw new IOException("Trying to read published field, but parent object is not a macro");

			// Find published info
			final String key = elem.getAttribute("key");
			final String sourceNodeId = elem.getAttribute("source");
			final String sourceFieldKey = elem.getAttribute("sourceField");

			final MacroNode macro = (MacroNode)parent;
			final OpNode sourceNode = macro.getGraph().getNodeById(sourceNodeId, true);
			final OutputField sourceField = sourceNode.getOutputFieldWithKey(sourceFieldKey);

			// Create
			final OutputField published = macro.publish(key, sourceNode, sourceField);

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

					serializer.read(serializerFactory, graph, published, doc, childElem);
				}
			}
		}

		return null;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return ((cls == PublishedInput.class) || (cls == PublishedOutput.class));
	}

	@Override
	public boolean handles(QName name) {
		return (INPUT_QNAME.equals(name) || OUTPUT_QNAME.equals(name));
	}
}
