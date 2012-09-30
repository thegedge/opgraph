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
import java.net.URI;

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

/**
 * A default serializer for reading/writing {@link OpNode} to/from XML.
 */
public class DefaultNodeXMLSerializer implements XMLSerializer {
	// qualified names
	static final QName NODE_QNAME = new QName(DEFAULT_NAMESPACE, "node", XMLConstants.DEFAULT_NS_PREFIX);
	static final QName DESCRIPTION_QNAME = new QName(DEFAULT_NAMESPACE, "description", XMLConstants.DEFAULT_NS_PREFIX);

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc, Element parentElem, Object obj) 
		throws IOException 
	{
		if(obj == null)
			throw new IOException("Null object given to serializer");
		
		if(!(obj instanceof OpNode))
			throw new IOException(DefaultNodeXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());
		
		// Create node element
		final OpNode node = (OpNode)obj;
		final Element nodeElem = doc.createElementNS(NODE_QNAME.getNamespaceURI(), NODE_QNAME.getLocalPart());
		
		nodeElem.setAttribute("id", node.getId());
		nodeElem.setAttribute("type", "class:" + node.getClass().getName());
		
		if(!node.getName().equals(node.getDefaultName()))
			nodeElem.setAttribute("name", node.getName());
			
		if(!node.getCategory().equals(node.getDefaultCategory()))
			nodeElem.setAttribute("category", node.getCategory());
		
		if(!node.getDescription().equals(node.getDefaultDescription())) {
			final Element descriptionElem = doc.createElementNS(DESCRIPTION_QNAME.getNamespaceURI(), DESCRIPTION_QNAME.getLocalPart());
			descriptionElem.setTextContent(node.getDescription());
			nodeElem.appendChild(descriptionElem);
		}
		
		// Input fields
		for(InputField field : node.getInputFields()) {
			final XMLSerializer serializer = serializerFactory.getHandler(field.getClass());
			if(serializer == null)
				throw new IOException("Cannot get handler for input field: " + field.getClass().getName());
			
			serializer.write(serializerFactory, doc, nodeElem, field);
		}
		
		// Output fields
		for(OutputField field : node.getOutputFields()) {
			final XMLSerializer serializer = serializerFactory.getHandler(field.getClass());
			if(serializer == null)
				throw new IOException("Cannot get handler for output field: " + field.getClass().getName());
			
			serializer.write(serializerFactory, doc, nodeElem, field);
		}
		
		// Extensions last
		if(node.getExtensionClasses().size() > 0) {
			final XMLSerializer serializer = serializerFactory.getHandler(Extendable.class);
			if(serializer == null)
				throw new IOException("No XML serializer for extensions");
			
			serializer.write(serializerFactory, doc, nodeElem, node);
		}
		
		//
		parentElem.appendChild(nodeElem);
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem)
		throws IOException 
	{
		OpNode node = null;
		if(NODE_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			// Attempt to instantiate  
			final String type = elem.getAttribute("type");
			final URI uri = URI.create(type);
			if(uri == null || !"class".equals(uri.getScheme()))
				throw new IOException("Node has unknown type: " + type);
			
			try {
				node = (OpNode)Class.forName(uri.getSchemeSpecificPart()).newInstance();
			} catch(InstantiationException exc) {
				throw new IOException("Could not instantiate node of type " + type, exc);
			} catch(IllegalAccessException exc) {
				throw new IOException("Could not instantiate node of type " + type, exc);
			} catch(ClassNotFoundException exc) {
				throw new IOException("Node has unknown type " + type, exc);
			} catch(ClassCastException exc) {
				throw new IOException("Node type is not OpNode: " + type, exc);
			}

			// Set attributes
			if(elem.hasAttribute("id"))
				node.setId(elem.getAttribute("id"));
			
			if(elem.hasAttribute("name"))
				node.setName(elem.getAttribute("name"));
			
			if(elem.hasAttribute("category"))
				node.setName(elem.getAttribute("category"));
			
			// Read children
			final NodeList children = elem.getChildNodes();
			for(int childIndex = 0; childIndex < children.getLength(); ++childIndex) {
				final Node childNode = children.item(childIndex);
				if(childNode instanceof Element) {
					final Element childElem = (Element)childNode;
					final QName name = XMLSerializerFactory.getQName(childElem);
					if(DESCRIPTION_QNAME.equals(name)) {
						node.setDescription(childElem.getTextContent());
					} else {
						// Get a handler for the element
						final XMLSerializer serializer = serializerFactory.getHandler(name);
						if(serializer == null)
							throw new IOException("Could not get handler for element: " + name);
						
						// Determine what kind of element was read. If the element represented a
						// input/output field, add it to the node. Otherwise, the only element should
						// be the <extensions> element, and its serializer handles adding extensions.
						//
						final Object objRead = serializer.read(serializerFactory, graph, node, doc, childElem);
						if(objRead != null) {
							if(objRead instanceof InputField) {
								node.putField((InputField)objRead);
							} else if(objRead instanceof OutputField) {
								node.putField((OutputField)objRead);
							}
						}
					}
				}
			}
		}
			
		return node;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return (cls == OpNode.class);
	}

	@Override
	public boolean handles(QName name) {
		return NODE_QNAME.equals(name);
	}
}
