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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.extensions.Extendable;
import ca.gedge.opgraph.io.OpGraphSerializer;
import ca.gedge.opgraph.io.OpGraphSerializerInfo;
import ca.gedge.opgraph.util.ServiceDiscovery;

/**
 * A factory that maps qualified names to serializers that handle them.
 */
@OpGraphSerializerInfo(extension="xml", description="XML Files")
public final class XMLSerializerFactory implements OpGraphSerializer {
	/** The default namespace */
	static final String DEFAULT_NAMESPACE = "http://www.gedge.ca/ns/opgraph";
	
	/** The default prefix used for writing */
	static final String DEFAULT_PREFIX = "og";
	
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(XMLSerializerFactory.class.getName());
	
	/** The serializers to use */
	private Collection<XMLSerializer> serializers;
	
	/**
	 * Default constructor.
	 */
	public XMLSerializerFactory() {
		this.serializers = new ArrayList<XMLSerializer>();
		
		for(Class<? extends XMLSerializer> provider : ServiceDiscovery.getInstance().findProviders(XMLSerializer.class)) {
			try {
				serializers.add( provider.newInstance() );
			} catch(InstantiationException exc) {
				LOGGER.warning("Could not instantiate XMLSerializer provider: " + provider.getName());
			} catch(IllegalAccessException exc) {
				LOGGER.warning("Could not instantiate XMLSerializer provider: " + provider.getName());
			}
		}
	}
	
	/**
	 * Gets the qualified name of an element. 
	 * 
	 * @param elem  the element
	 * 
	 * @return the qualified name of the element
	 */
	public static QName getQName(Element elem) {
		String localName = elem.getLocalName();
		String prefix = (elem.getPrefix() == null ? XMLConstants.DEFAULT_NS_PREFIX : elem.getPrefix());
		return new QName(elem.getNamespaceURI(), localName, prefix);
	}
	
	/**
	 * Writes an element's extensions to a parent element.
	 * 
	 * @param doc  the document
	 * @param parent  the parent element to write to
	 * @param ext  the {@link Extendable}
	 * 
	 * @throws IOException  if any errors occur when serializing
	 */
	public void writeExtensions(Document doc, Element parent, Extendable ext) throws IOException {
		final Element extensionsElem = doc.createElementNS(DEFAULT_NAMESPACE, "extensions");
		for(Class<?> extension : ext.getExtensionClasses()) {
			final XMLSerializer serializer = getHandler(extension);
			if(serializer == null)
				LOGGER.warning("Node contains an unwritable extension: " + extension.getName());
			else
				serializer.write(this, doc, extensionsElem, ext.getExtension(extension));
		}
		
		if(extensionsElem.getChildNodes().getLength() > 0)
			parent.appendChild(extensionsElem);
	}
	
	/**
	 * Gets the handler for a specified qualified name. 
	 * 
	 * @param name  qualified name for which a serializer is needed
	 * 
	 * @return an XML serializer for the given qualified name, or <code>null</code>
	 *         if no handler is registered for the given qualified name
	 */
	public XMLSerializer getHandler(QName name) {
		for(XMLSerializer serializer : serializers) {
			if(serializer.handles(name))
				return serializer;
		}
		return null;
	}
	
	/**
	 * Gets the handler for a specified class. Ascends the inheritance chain
	 * of the given class to see if there is a handler for a super class.
	 * 
	 * @param cls  class for which a serializer is needed
	 * 
	 * @return an XML serializer for the given class, or <code>null</code> if
	 *         no handler is registered for the class
	 */
	public XMLSerializer getHandler(Class<?> cls) {
		while(cls != null) {
			for(XMLSerializer serializer : serializers) {
				if(serializer.handles(cls))
					return serializer;
			}
			
			cls = cls.getSuperclass();
		}
		return null;
	}
	
	//
	// Overrides
	//

	/**
	 * Writes a graph to a stream.
	 * 
	 * @param graph  the graph to write
	 * @param stream  the stream to write to
	 * 
	 * @throws IOException  if any I/O errors occur
	 */
	@Override
	public void write(OpGraph graph, OutputStream stream) throws IOException {
		Document doc;
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);

			// Construct a DOM document
			final DocumentBuilder docBuilder = factory.newDocumentBuilder();
			final DOMImplementation domImpl = docBuilder.getDOMImplementation();

			// use NAMESPACE as default namespace for document
			doc = domImpl.createDocument(DEFAULT_NAMESPACE, "opgraph", null);
		} catch(ParserConfigurationException exc) {
			throw new IOException("Could not create document builder", exc);
		}

		final Element root = doc.getDocumentElement();

		final XMLSerializer serializer = getHandler(graph.getClass());
		if(serializer != null)
			serializer.write(this, doc, root, graph);
		
		doc.normalize();
		
		// Write to stream
		try {
			final Source source = new DOMSource(doc);
			final Result result = new StreamResult(stream);
			final Transformer transformer = TransformerFactory.newInstance().newTransformer();

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			transformer.transform(source, result);
		} catch(TransformerConfigurationException exc) {
			throw new IOException("Could not write DOM tree to stream", exc);
		} catch(TransformerFactoryConfigurationError exc) {
			throw new IOException("Could not write DOM tree to stream", exc);
		} catch(TransformerException exc) {
			throw new IOException("Could not write DOM tree to stream", exc);
		}
	}

	/**
	 * Reads a graph from a stream.
	 * 
	 * @param stream  the stream to read from
	 * 
	 * @throws IOException  if any I/O errors occur
	 */
	@Override
	public OpGraph read(InputStream stream) throws IOException {
		// Create document
		Document doc;
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			doc = factory.newDocumentBuilder().parse(stream);
		} catch(SAXException exc) {
			throw new IOException("Could not parse stream as XML", exc);
		} catch(ParserConfigurationException exc) {
			throw new IOException("Could not create document builder", exc);
		}

		// Read from stream
		OpGraph ret = null;

		final XMLSerializer serializer = getHandler(getQName(doc.getDocumentElement()));
		if(serializer != null) {
			final Object objRead = serializer.read(this, null, null, doc, doc.getDocumentElement());
			if(objRead instanceof OpGraph)
				ret = (OpGraph)objRead;
		}

		if(ret == null)
			throw new IOException("Graph could not be read from stream");

		return ret;
	}
}
