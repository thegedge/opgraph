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
package ca.gedge.opgraph.app.xml;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.extensions.NodeMetadata;
import ca.gedge.opgraph.io.xml.XMLSerializer;
import ca.gedge.opgraph.io.xml.XMLSerializerFactory;

/**
 */
public class NodeMetadataXMLSerializer implements XMLSerializer {
	static final String NAMESPACE = "http://www.gedge.ca/ns/opgraph-app";
	static final String PREFIX = "oga";
	static final QName META_QNAME = new QName(NAMESPACE, "meta", PREFIX);

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc, Element parentElem, Object obj)
		throws IOException
	{
		if(obj == null)
			throw new IOException("Null object given to serializer");

		if(!(obj instanceof NodeMetadata))
			throw new IOException(NodeMetadataXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());

		// setup namespace for document
		final Element rootEle = doc.getDocumentElement();
		rootEle.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
				XMLConstants.XMLNS_ATTRIBUTE + ":" + PREFIX, NAMESPACE);

		// Create metadata element
		final NodeMetadata meta = (NodeMetadata)obj;
		final Element metaElem = doc.createElementNS(NAMESPACE, PREFIX + ":" + META_QNAME.getLocalPart());
		metaElem.setAttribute("x", Integer.toString(meta.getX()));
		metaElem.setAttribute("y", Integer.toString(meta.getY()));

		// Create elements for default values
		for(Map.Entry<InputField, Object> entry : meta.getDefaults().entrySet()) {
			final InputField field = entry.getKey();
			final Object value = entry.getValue();

			final Element defaultElem = doc.createElementNS(NAMESPACE, PREFIX + ":default");
			defaultElem.setAttribute("for", field.getKey());
			defaultElem.setAttribute("type", value.getClass().getName());
			defaultElem.setTextContent(value.toString());

			metaElem.appendChild(defaultElem);
		}

		parentElem.appendChild(metaElem);
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem) 
		throws IOException
	{
		if(META_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			// Try to get the parent node
			if(parent == null || !(parent instanceof OpNode))
				throw new IOException("Node metadata requires parent node");

			final OpNode node = (OpNode)parent;

			// Read metadata
			final NodeMetadata meta = new NodeMetadata();
			meta.setX(Integer.parseInt(elem.getAttribute("x")));
			meta.setY(Integer.parseInt(elem.getAttribute("y")));

			// Read in defaults
			final NodeList children = elem.getChildNodes();
			for(int childIndex = 0; childIndex < children.getLength(); ++childIndex) {
				final Node defaultNode = children.item(childIndex);
				if(defaultNode instanceof Element) {
					final Element defaultElem = (Element) defaultNode;
					final String fieldKey = defaultElem.getAttribute("for");
					final InputField field = node.getInputFieldWithKey(fieldKey);
					if(field == null)
						throw new Error("Default value references unknown input field: " + fieldKey);

					final String valueTypeClassName = defaultElem.getAttribute("type");
					final String valueString = defaultElem.getTextContent();
					try {
						boolean parsed = false;
						final Class<?> valueClass = Class.forName(valueTypeClassName);
						for(Method method : valueClass.getMethods()) {
							if(/*valueClass.isAssignableFrom(method.getReturnType()) 
							   &&*/ Modifier.isStatic(method.getModifiers())
							   && method.getParameterTypes().length == 1 
							   && method.getParameterTypes()[0] == String.class
							   && method.getName().startsWith("parse"))
							{
								meta.setDefault(field, method.invoke(null, valueString));
								parsed = true;
								break;
							}
						}

						if(!parsed)
							throw new Error("Couldn't parse default value");
					} catch(ClassNotFoundException exc) {
						throw new Error("Unknown default value type: " + valueTypeClassName);
					} catch(IllegalArgumentException exc) {
						throw new Error("Couldn't parse default value", exc);
					} catch(IllegalAccessException exc) {
						throw new Error("Couldn't parse default value", exc);
					} catch(InvocationTargetException exc) {
						throw new Error("Couldn't parse default value", exc);
					}
				}
			}

			node.putExtension(NodeMetadata.class, meta);
		}

		return null;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return (cls == NodeMetadata.class);
	}

	@Override
	public boolean handles(QName name) {
		return META_QNAME.equals(name);
	}
}
