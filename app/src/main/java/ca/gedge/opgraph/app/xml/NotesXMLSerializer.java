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

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JComponent;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.app.extensions.Note;
import ca.gedge.opgraph.app.extensions.Notes;
import ca.gedge.opgraph.extensions.Extendable;
import ca.gedge.opgraph.io.xml.XMLSerializer;
import ca.gedge.opgraph.io.xml.XMLSerializerFactory;

/**
 */
public class NotesXMLSerializer implements XMLSerializer {
	static final String NAMESPACE = "http://www.gedge.ca/ns/opgraph-app";
	static final String PREFIX = "oga";
	
	static final QName NOTES_QNAME = new QName(NAMESPACE, "notes", PREFIX);
	static final QName NOTE_QNAME = new QName(NAMESPACE, "note", PREFIX);
	
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
		
		if(obj instanceof Notes) {
			// Create notes element, if any exist
			final Notes notes = (Notes)obj;
			if(notes.size() > 0) {
				final Element notesElem = doc.createElementNS(NAMESPACE, PREFIX + ":notes");
	
				// Create elements for each note
				for(Note note : notes)
					write(serializerFactory, doc, notesElem, note);
				
				parentElem.appendChild(notesElem);
			}
		} else if(obj instanceof Note) {
			final Note note = (Note)obj;
			final Element noteElem = doc.createElementNS(NAMESPACE, PREFIX + ":note");
			noteElem.setAttribute("title", note.getTitle());
			noteElem.setTextContent(note.getBody());
			
			final JComponent noteComp = note.getExtension(JComponent.class);
			if(noteComp != null) {
				final String colorString = Integer.toHexString(noteComp.getBackground().getRGB() & 0xFFFFFF);
				
				noteElem.setAttribute("x", "" + noteComp.getX());
				noteElem.setAttribute("y", "" + noteComp.getY());
				noteElem.setAttribute("width", "" + noteComp.getWidth());
				noteElem.setAttribute("height", "" + noteComp.getHeight());
				noteElem.setAttribute("color", "0x" + colorString);
			}
			
			parentElem.appendChild(noteElem);
		} else {
			throw new IOException(NotesXMLSerializer.class.getName() + " cannot write objects of type " + obj.getClass().getName());
		}
	}

	@Override
	public Object read(XMLSerializerFactory serializerFactory, OpGraph graph, Object parent, Document doc, Element elem) 
		throws IOException
		{
		Object ret = null;
		if(NOTES_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			// Try to get the parent object
			if(parent == null || !(parent instanceof Extendable))
				throw new IOException("Notes extension requires parent to be extendable");

			final Extendable extendable = (Extendable)parent;
			final Notes notes = new Notes();

			// Read in each note
			final NodeList children = elem.getChildNodes();
			for(int childIndex = 0; childIndex < children.getLength(); ++childIndex) {
				final Node noteNode = children.item(childIndex);
				if(noteNode instanceof Element) {
					final Element noteElem = (Element) noteNode;
					read(serializerFactory, graph, notes, doc, noteElem);
				}
			}

			extendable.putExtension(Notes.class, notes);
			ret = notes;
		} else if(NOTE_QNAME.equals(XMLSerializerFactory.getQName(elem))) {
			if(parent == null || !(parent instanceof Notes))
				throw new IOException("Notes extension requires parent to be extendable");

			final Notes notes = (Notes)parent;
			final String title = elem.getAttribute("title");
			final String body = (elem.getTextContent() == null ? "" : elem.getTextContent().trim());
			final Note note = new Note(title, body);

			final JComponent noteComp = note.getExtension(JComponent.class);
			if(noteComp != null) {
				if(elem.hasAttribute("x") && elem.hasAttribute("y")) {
					final int x = Integer.parseInt(elem.getAttribute("x"));
					final int y = Integer.parseInt(elem.getAttribute("y"));
					noteComp.setLocation(x, y);
				}
	
				if(elem.hasAttribute("width") && elem.hasAttribute("height")) {
					final int w = Integer.parseInt(elem.getAttribute("width"));
					final int h = Integer.parseInt(elem.getAttribute("height"));
					noteComp.setPreferredSize(new Dimension(w, h));
				}
				
				if(elem.hasAttribute("color")) {
					noteComp.setBackground(Color.decode(elem.getAttribute("color")));
				}
			}

			notes.add(note);
			ret = note;
		}

		return ret;
	}

	@Override
	public boolean handles(Class<?> cls) {
		return (cls == Notes.class || cls == Note.class);
	}

	@Override
	public boolean handles(QName name) {
		return (NOTES_QNAME.equals(name) || NOTE_QNAME.equals(name));
	}
}
