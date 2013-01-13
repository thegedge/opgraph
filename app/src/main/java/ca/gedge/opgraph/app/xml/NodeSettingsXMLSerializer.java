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
import java.util.Map;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.io.xml.XMLSerializer;
import ca.gedge.opgraph.io.xml.XMLSerializerFactory;

/**
 */
public class NodeSettingsXMLSerializer implements XMLSerializer {
	static final String NAMESPACE = "http://gedge.ca/ns/opgraph-app";
	static final String PREFIX = "oga";
	static final QName SETTINGS_QNAME = new QName(NAMESPACE, "settings", PREFIX);

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc, Element parentElem, Object obj)
		throws IOException
	{
		if(obj == null)
			throw new IOException("Null object given to serializer");

		if(!(obj instanceof NodeSettings))
			throw new IOException(NodeSettingsXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());

		// setup namespace for document
		final Element rootEle = doc.getDocumentElement();
		rootEle.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
				XMLConstants.XMLNS_ATTRIBUTE + ":" + PREFIX, NAMESPACE);

		// Create metadata element
		final Properties props = ((NodeSettings)obj).getSettings();
		final Element settingsElem = doc.createElementNS(NAMESPACE, PREFIX + ":" + SETTINGS_QNAME.getLocalPart());

		// Create elements for default values
		for(Map.Entry<Object, Object> entry : props.entrySet()) {
			if(entry.getKey() != null) {
				final Element propertyElem = doc.createElementNS(NAMESPACE, PREFIX + ":property");
				propertyElem.setAttribute("key", entry.getKey().toString());
				if(entry.getValue() != null)
					propertyElem.appendChild( doc.createCDATASection(entry.getValue().toString()) );

				settingsElem.appendChild(propertyElem);
			}
		}

		parentElem.appendChild(settingsElem);
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem)
		throws IOException
	{
		if(SETTINGS_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			// Try to get the parent node
			if(parent == null || !(parent instanceof OpNode))
				throw new IOException("Node metadata requires parent node");

			final OpNode node = (OpNode)parent;
			final NodeSettings settings = node.getExtension(NodeSettings.class);
			if(settings == null)
				throw new IOException("Parent node does not have settings extension, but is specified in XML");

			// Read in properties
			final Properties properties = new Properties();
			final NodeList children = elem.getChildNodes();
			for(int childIndex = 0; childIndex < children.getLength(); ++childIndex) {
				final Node defaultNode = children.item(childIndex);
				if(defaultNode instanceof Element) {
					final Element propertyElem = (Element)defaultNode;
					final String key = propertyElem.getAttribute("key");

					final String value = (propertyElem.getChildNodes().getLength() == 0
					                      ? null
					                      : propertyElem.getTextContent());

					if(value != null)
						properties.setProperty(key, value);
				}
			}

			settings.loadSettings(properties);
		}

		return null;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return (cls == NodeSettings.class);
	}

	@Override
	public boolean handles(QName name) {
		return SETTINGS_QNAME.equals(name);
	}
}
