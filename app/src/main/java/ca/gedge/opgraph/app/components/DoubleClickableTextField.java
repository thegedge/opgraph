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
package ca.gedge.opgraph.app.components;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * A text field that begins editing on double-click. When the ENTER key is
 * pressed, editing finishes and the text is updated. When the ESCAPE key is
 * pressed, editing finishes and the text reverts to its state before editing
 * began.
 */
public class DoubleClickableTextField {
	/** The property name for the editing property */
	public static final String EDITING_PROPERTY = "editingValue";
	
	/** The property name for the text property */
	public static final String TEXT_PROPERTY = "textValue";
	
	/** Property change support */
	private final PropertyChangeSupport propertyChangeSupport;
	
	/** The text component we have attached to */
	private final JTextComponent textComponent;
	
	/** The currently set highlighter */
	private Highlighter highlighter;
	
	/** The currently set bg color */
	private Color bgColor;
	
	/** The text before editing began */
	private String oldText;
	
	/** Whether or not we are currently editing */
	private boolean editing;
	
	/**
	 * Constructs a double-clickable text field.
	 * 
	 * @param textComponent  the text component to attach to
	 */
	public DoubleClickableTextField(JTextComponent textComponent) {
		this.editing = false;
		this.propertyChangeSupport = new PropertyChangeSupport(this);
		
		this.textComponent = textComponent;
		this.textComponent.setFocusable(false);
		this.textComponent.setOpaque(false);
		this.textComponent.setDragEnabled(false);
		this.textComponent.addMouseListener(mouseAdapter);
		this.textComponent.addKeyListener(keyAdapter);
		this.textComponent.addFocusListener(focusAdapter);
		
		this.highlighter = textComponent.getHighlighter();
		this.bgColor = textComponent.getBackground();
	}
	
	/**
	 * Gets whether or not we are currently editing.
	 * 
	 * @return  <code>true</code> if currently editing, <code>false</code> otherwise
	 */
	public boolean isEditing() {
		return editing;
	}

	/**
	 * Sets whether or not we are editing the name.
	 * 
	 * @param editing  <code>true</code> if editing, <code>false</code> otherwise
	 */
	public void setEditing(boolean editing) {
		if(textComponent.isEditable() && this.editing != editing) {
			this.editing = editing;
			
			if(editing)
				oldText = textComponent.getText();
			
			textComponent.setFocusable(editing);
			textComponent.setOpaque(editing);
			textComponent.setBackground(editing ? bgColor : null);
			textComponent.setHighlighter(editing ? highlighter : null);
			
			if(editing) {
				textComponent.requestFocusInWindow();
				textComponent.selectAll();
			} else {
				propertyChangeSupport.firePropertyChange(TEXT_PROPERTY, oldText, textComponent.getText());
			}
			
			propertyChangeSupport.firePropertyChange(EDITING_PROPERTY, !editing, editing);
		}
	}
	
	//
	// MouseAdapter
	//

	private final MouseAdapter mouseAdapter = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			if(e.getClickCount() > 1 && !e.isPopupTrigger())
				setEditing(true);
		}
	};

	//
	// KeyAdapter
	//

	private final KeyAdapter keyAdapter = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyCode() == KeyEvent.VK_ENTER) {
				if((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != InputEvent.SHIFT_DOWN_MASK) {
					// Force an update of text
					//textComponent.setText(textComponent.getText()); 
					setEditing(false);
				}
			} else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				// Reset to text before editing
				textComponent.setText(oldText);
				setEditing(false);
			}
		}
	};

	//
	// FocusAdapter
	//

	private final FocusAdapter focusAdapter = new FocusAdapter() {
		@Override
		public void focusLost(FocusEvent e) {
			textComponent.setText(textComponent.getText());
			setEditing(false);
		}
	};
	
	//
	// PropertyChange listener support
	//
	
	/**
	 * Adds a property change listener to this component.
	 * 
	 * @param listener  the listener to add
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}
	
	/**
	 * Adds a property change listener for a specific property to this component.
	 * 
	 * @param propertyName  the property name
	 * @param listener  the listener to add
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}
	
	/**
	 * Removes a property change listener from this component.
	 * 
	 * @param listener  the listener to remove
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	/**
	 * Removes a property change listener for a specific property from this component.
	 * 
	 * @param propertyName  the property name
	 * @param listener  the listener to remove
	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}
}
