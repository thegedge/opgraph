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
import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.exceptions.ItemMissingException;

/**
 * A default serializer for reading/writing {@link OpLink} to/from XML.
 */
public class DefaultLinkXMLSerializer implements XMLSerializer {
	// qualified names
	static final QName LINK_QNAME = new QName(DEFAULT_NAMESPACE, "link", XMLConstants.DEFAULT_NS_PREFIX);

	@Override
	public void write(XMLSerializerFactory serializerFactory, Document doc, Element parentElem, Object obj) 
		throws IOException 
	{
		if(obj == null)
			throw new IOException("Null object given to serializer");

		if(!(obj instanceof OpLink))
			throw new IOException(DefaultLinkXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());

		// Create link element
		final OpLink link = (OpLink)obj;
		final Element linkElem = doc.createElementNS(LINK_QNAME.getNamespaceURI(), LINK_QNAME.getLocalPart());

		linkElem.setAttribute("source", link.getSource().getId());
		linkElem.setAttribute("dest", link.getDestination().getId());
		linkElem.setAttribute("sourceField", link.getSourceField().getKey());
		linkElem.setAttribute("destField", link.getDestinationField().getKey());

		parentElem.appendChild(linkElem);
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem)
		throws IOException 
	{
		OpLink link = null;
		if(LINK_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			final String sid = elem.getAttribute("source");
			final String did = elem.getAttribute("dest");
			final String sfkey = elem.getAttribute("sourceField");
			final String dfkey = elem.getAttribute("destField");

			final OpNode source = graph.getNodeById(sid, false);
			if(source == null)
				throw new IOException("Unknown source node in link: " + sid);

			final OpNode dest = graph.getNodeById(did, false);
			if(dest == null)
				throw new IOException("Unknown source node in link: " + did);

			final OutputField sourceField = source.getOutputFieldWithKey(sfkey);
			if(sourceField == null)
				throw new IOException("Unknown source field in link: " + sfkey);

			final InputField destField = dest.getInputFieldWithKey(dfkey);
			if(destField == null)
				throw new IOException("Unknown source node in link: " + dfkey);

			try {
				link = new OpLink(source, sourceField, dest, destField);
			} catch(ItemMissingException exc) {
				throw new IOException("Could not construct link", exc);
			}
		}

		return link;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return (cls == OpLink.class);
	}

	@Override
	public boolean handles(QName name) {
		return LINK_QNAME.equals(name);
	}
}
