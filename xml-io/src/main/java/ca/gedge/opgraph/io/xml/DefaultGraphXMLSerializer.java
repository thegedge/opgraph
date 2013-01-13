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

import static ca.gedge.opgraph.io.xml.XMLSerializerFactory.DEFAULT_NAMESPACE;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.extensions.Extendable;

/**
 * A default serializer for reading/writing {@link OpGraph} to/from XML.
 */
public class DefaultGraphXMLSerializer implements XMLSerializer {
	// qualified names
	static final QName OPGRAPH_QNAME = new QName(DEFAULT_NAMESPACE, "opgraph", XMLConstants.DEFAULT_NS_PREFIX);
	static final QName GRAPH_QNAME = new QName(DEFAULT_NAMESPACE, "graph", XMLConstants.DEFAULT_NS_PREFIX);

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc, Element parentElem, Object obj)
		throws IOException
	{
		if(obj == null)
			throw new IOException("Null object given to serializer");

		if(!(obj instanceof OpGraph))
			throw new IOException(DefaultGraphXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());

		// Create graph element
		final OpGraph graph = (OpGraph)obj;
		final Element graphElem = doc.createElementNS(GRAPH_QNAME.getNamespaceURI(), GRAPH_QNAME.getLocalPart());

		graphElem.setAttribute("id", graph.getId());

		// Nodes first
		for(OpNode node : graph.getVertices()) {
			final XMLSerializer serializer = serializerFactory.getHandler(node.getClass());
			if(serializer == null)
				throw new IOException("Cannot get handler for node: " + node.getClass().getName());

			serializer.write(serializerFactory, doc, graphElem, node);
		}

		// Link next
		for(OpLink link : graph.getEdges()) {
			final XMLSerializer serializer = serializerFactory.getHandler(link.getClass());
			if(serializer == null)
				throw new IOException("Cannot get handler for link: " + link.getClass().getName());

			serializer.write(serializerFactory, doc, graphElem, link);
		}

		// Extensions last
		if(graph.getExtensionClasses().size() > 0) {
			final XMLSerializer serializer = serializerFactory.getHandler(Extendable.class);
			if(serializer == null)
				throw new IOException("No XML serializer for extensions");

			serializer.write(serializerFactory, doc, graphElem, graph);
		}

		//
		parentElem.appendChild(graphElem);
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem)
		throws IOException
	{
		if(GRAPH_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			graph = new OpGraph();

			// Read children
			final NodeList children = elem.getChildNodes();
			for(int childIndex = 0; childIndex < children.getLength(); ++childIndex) {
				final Node node = children.item(childIndex);
				if(node instanceof Element) {
					// Get a handler for the element
					final Element childElem = (Element)node;
					final QName name = XMLSerializerFactory.getQName(childElem);
					final XMLSerializer serializer = serializerFactory.getHandler(name);
					if(serializer == null)
						throw new IOException("Could not get handler for element: " + name);

					// Determine what kind of element was read. If the element represented a
					// node/link, add it to the graph, otherwise it should be the <extensions>
					// element, and the extendable serializer handles adding the extensions
					//
					final Object objRead = serializer.read(serializerFactory, graph, graph, doc, childElem);
					if(objRead != null) {
						if(objRead instanceof OpNode) {
							graph.add((OpNode)objRead);
						} else if(objRead instanceof OpLink) {
							try {
								graph.add( (OpLink)objRead );
							} catch(VertexNotFoundException exc) {
								throw new IOException("Link references unknown node", exc);
							} catch(CycleDetectedException exc) {
								throw new IOException("Link induces a cycle", exc);
							} catch(NullPointerException exc) {
								throw new IOException("Could not construct link", exc);
							}
						}
					}
				}
			}
		} else if(OPGRAPH_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			final NodeList children = elem.getChildNodes();
			for(int childIndex = 0; childIndex < children.getLength(); ++childIndex) {
				final Node childNode = children.item(childIndex);
				if(childNode instanceof Element) {
					final Element childElem = (Element)childNode;

					if(GRAPH_QNAME.equals(XMLSerializerFactory.getQName(childElem))
					   && "root".equals(childElem.getAttribute("id")))
					{
						graph = (OpGraph)read(serializerFactory, graph, null, doc, childElem);
						break;
					}
				}
			}
		}

		return graph;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return (cls == OpGraph.class);
	}

	@Override
	public boolean handles(QName name) {
		return (GRAPH_QNAME.equals(name) || OPGRAPH_QNAME.equals(name));
	}
}
