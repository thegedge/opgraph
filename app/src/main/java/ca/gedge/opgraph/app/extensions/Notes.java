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
package ca.gedge.opgraph.app.extensions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import ca.gedge.opgraph.app.util.CollectionListener;
import ca.gedge.opgraph.app.util.CollectionListenerSupport;

/**
 * An extension that stores a set of notes. Notes are stored in memory as
 * components so that they can be placed and manipulated in a GUI.
 */
public class Notes implements Iterable<Note> {
	/** The collection of notes */
	private Collection<Note> notes;

	/**
	 * Default constructor. 
	 */
	public Notes() {
		this.notes = new ArrayList<Note>();
	}

	/**
	 * Adds a note.
	 * 
	 * @param note  the note to add
	 */
	public void add(Note note) {
		notes.add(note);
		listenerSupport.fireElementAdded(this, note);
	}

	/**
	 * Removes a note.
	 * 
	 * @param note  the note to remove
	 */
	public void remove(Note note) {
		if(notes.contains(note)) {
			notes.remove(note);
			listenerSupport.fireElementRemoved(this, note);
		}
	}

	/**
	 * Gets the number of notes.
	 * 
	 * @return the number of notes
	 */
	public int size() {
		return notes.size();
	}

	//
	// Iterable<Notes.Note>
	//

	@Override
	public Iterator<Note> iterator() {
		return notes.iterator();
	}

	//
	// CollectionListener support
	//

	private final CollectionListenerSupport<Notes, Note> listenerSupport = new CollectionListenerSupport<Notes, Note>();

	/**
	 * Adds a collection listener.
	 * 
	 * @param listener  the listener
	 */
	public void addCollectionListener(CollectionListener<Notes, Note> listener) {
		listenerSupport.addCollectionListener(listener);
	}

	/**
	 * Removes a collection listener.
	 * 
	 * @param listener  the listener
	 */
	public void removeCollectionListener(CollectionListener<Notes, Note> listener) {
		listenerSupport.removeCollectionListener(listener);
	}
}
