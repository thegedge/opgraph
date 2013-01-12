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

import java.awt.Color;
import java.util.Collection;

import javax.swing.JComponent;

import ca.gedge.opgraph.extensions.Extendable;
import ca.gedge.opgraph.extensions.ExtendableSupport;

/**
 * A note, containing a textual title and body. Also contains display info
 * such as color, location, and size.
 */
public class Note implements Extendable {
	/** The title string */
	private String title;

	/** The body string */
	private String body;

	/**
	 * Constructs a note with a given title, body, and color.
	 * 
	 * @param title  the title
	 * @param body  the body
	 */
	public Note(String title, String body) {
		this.title = title;
		this.body = body;
		this.extendableSupport.putExtension(JComponent.class, new NoteComponent(this));
	}

	/**
	 * Gets the note's title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the note's title.
	 * 
	 * @param title  the title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Gets the note's body text.
	 * 
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Sets the note's body text.
	 * 
	 * @param body  the body text
	 */
	public void setBody(String body) {
		this.body = body;
	}

	//
	// Extendable
	//

	private final ExtendableSupport extendableSupport = new ExtendableSupport(Note.class);

	@Override
	public <T> T getExtension(Class<T> type) {
		return extendableSupport.getExtension(type);
	}

	@Override
	public Collection<Class<?>> getExtensionClasses() {
		return extendableSupport.getExtensionClasses();
	}

	@Override
	public <T> T putExtension(Class<T> type, T extension) {
		return extendableSupport.putExtension(type, extension);
	}
}
