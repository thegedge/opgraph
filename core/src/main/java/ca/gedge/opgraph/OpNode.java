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
package ca.gedge.opgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ca.gedge.opgraph.dag.Vertex;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.extensions.Extendable;
import ca.gedge.opgraph.extensions.ExtendableSupport;

/**
 * A node in an {@link OpGraph}.
 */
public abstract class OpNode implements Extendable, Vertex {
	/** The key for the id property */
	public static final String ID_PROPERTY = "id";
	
	/** The key for the name property */
	public static final String NAME_PROPERTY = "name";
	
	/** The key for the description property */
	public static final String DESCRIPTION_PROPERTY = "description";
	
	/** The key for the category property */
	public static final String CATEGORY_PROPERTY = "category"; 
	
	/** Default enabled field */
	public final static InputField ENABLED_FIELD = new InputField(
			"enabled",
			"if true, disables processing of this node",
			true,
			true,
			Boolean.class);
	
	/** A unique id for this node */
	private String id;
	
	/** The name of this node */
	private String name;
	
	/** The category of this node */
	private String category;
	
	/** A short description of this node and what it does */
	private String description;
	
	/** The list of input fields this node has */
	private List<InputField> inputFields;
	
	/** The list of output fields this node has */
	private List<OutputField> outputFields;
	
	/**
	 * Constructs a node with a generated id, this class' name as the node name
	 * and an empty description. Also adds the enabled field.
	 */
	protected OpNode() {
		this(null, null, null);
	}

	/**
	 * Constructs a node with a specified id, this class' name as the node name
	 * and an empty description. Also adds the enabled field.
	 * 
	 * @param id  a unique id
	 */
	protected OpNode(String id) {
		this(id, null, null);
	}

	/**
	 * Constructs a node with a generated id, specified name, and specified
	 * description. Also adds the enabled field.
	 * 
	 * @param name  the name of the node
	 * @param description the description for the node
	 */
	protected OpNode(String name, String description) {
		this(null, name, description);
	}
	
	
	/**
	 * Constructs a node with a given id, name, and description. Also adds a
	 * default &quot;enabled&quot; field.
	 * 
	 * @param id  a unique identifier for the node
	 * @param name  the name of the node
	 * @param description the description for the node
	 */
	protected OpNode(String id, String name, String description) {
		setId(id);
		setName(name);
		setDescription(description);
		setCategory(null);
		
		this.outputFields = new ArrayList<OutputField>();
		this.inputFields = new ArrayList<InputField>();
		this.inputFields.add(ENABLED_FIELD);
	}

	/**
	 * Gets the id for this node.
	 * 
	 * @return the id
	 */
	public final String getId() {
		return id;
	}

	/**
	 * Sets the id for this node.
	 * 
	 * @param id the id to set
	 */
	public final void setId(String id) {
		if(id == null || (id = id.trim()).length() == 0)
			id = Integer.toHexString(System.identityHashCode(this));
		
		if(!id.equals(this.id)) {
			final String oldId = this.id;
			this.id = id;
			firePropertyChange(ID_PROPERTY, oldId, this.id);
		}
	}
	
	/**
	 * Gets a descriptive name for this node.
	 * 
	 * @return the name
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * Gets a default name for this node. First priority is given to the
	 * {@link OpNodeInfo} annotation, if it is present on this node class. If not,
	 * the node class name is used  (i.e., {@link Class#getCanonicalName()}).
	 * 
	 * @return the name
	 */
	public final String getDefaultName() {
		final OpNodeInfo info = getClass().getAnnotation(OpNodeInfo.class);
		return (info == null ? getClass().getCanonicalName() : info.name());
	}

	/**
	 * Sets a descriptive name for this node.
	 * 
	 * @param name  the name, or {@link #getDefaultName()} if <code>null</code>
	 */
	public final void setName(String name) {
		name = (name == null ? "" : name.trim());
		if(name.length() == 0)
			name = getDefaultName();
		
		if(!name.equals(this.name)) {
			final String oldName = this.name;
			this.name = name;
			firePropertyChange(NAME_PROPERTY, oldName, this.name);
		}
	}

	/**
	 * Gets a short description of this node and what it does.
	 * 
	 * @return the description
	 */
	public final String getDescription() {
		return description;
	}
	
	/**
	 * Gets a default description for this node. First priority is given to
	 * the {@link OpNodeInfo} annotation, if it is present on this
	 * node's class. If not, an empty string is used.
	 * 
	 * @return the description
	 */
	public final String getDefaultDescription() {
		final OpNodeInfo info = getClass().getAnnotation(OpNodeInfo.class);
		return (info == null ? "" : info.description());
	}
	
	/**
	 * Sets this node's category.
	 * 
	 * @param description  the description, or {@link #getDefaultDescription()}
	 *                     if <code>null</code>
	 */
	public final void setDescription(String description) {
		if(description == null || (description = description.trim()).length() == 0)
			description = getDefaultDescription();
		
		if(!description.equals(this.description)) {
			final String oldDescription = this.description;
			this.description = description;
			firePropertyChange(DESCRIPTION_PROPERTY, oldDescription, this.description);
		}
	}

	/**
	 * Gets this node's category.
	 * 
	 * @return the category
	 */
	public final String getCategory() {
		return category;
	}
	
	/**
	 * Gets a default category for this node. First priority is given to the
	 * {@link OpNodeInfo} annotation, if it is present on this node's class.
	 * If not, an empty string is used.
	 * 
	 * @return the description
	 */
	public final String getDefaultCategory() {
		final OpNodeInfo info = getClass().getAnnotation(OpNodeInfo.class);
		return (info == null ? "" : info.category());
	}
	
	/**
	 * Sets this node's category.
	 * 
	 * @param category  the category, or {@link #getDefaultCategory()} if
	 *                  <code>null</code>
	 */
	public final void setCategory(String category) {
		if(category == null || (category = category.trim()).length() == 0)
			category = getDefaultCategory();
		
		if(!category.equals(this.category)) {
			final String oldCategory = this.category;
			this.category = category;
			firePropertyChange(CATEGORY_PROPERTY, oldCategory, this.category);
		}
	}
	
	/**
	 * Adds an input field to this node.
	 * 
	 * @param field  the input field
	 * 
	 * @throws IllegalArgumentException  if the given field will overwrite a fixed field
	 */
	public final void putField(InputField field) {
		if(field != null) {
			int index = 0;
			InputField foundField = null;
			for(; index < inputFields.size(); ++index) {
				if(inputFields.get(index).getKey().equals(field.getKey())) {
					if(inputFields.get(index).isFixed())
						throw new IllegalArgumentException("Cannot overwrite fixed input field '" + field.getKey() + "' in node '" + getName() + "'");
			
					foundField = inputFields.get(index);
					break;
				}
			}

			if(foundField == null) {
				inputFields.add(field);
				fireFieldAdded(field);
			} else {
				foundField.setDescription(field.getDescription());
				foundField.setOptional(field.isOptional());
				foundField.setValidator(field.getValidator());
			}
		}
	}
	
	/**
	 * Adds an output field to this node.
	 * 
	 * @param field  the input field
	 * 
	 * @throws IllegalArgumentException  if the given field will overwrite a fixed field
	 */
	public final void putField(OutputField field) {
		if(field != null) {
			int index = 0;
			OutputField foundField = null;
			for(; index < outputFields.size(); ++index) {
				if(outputFields.get(index).getKey().equals(field.getKey())) {
					if(outputFields.get(index).isFixed())
						throw new IllegalArgumentException("Cannot overwrite fixed output field '" + field.getKey() + "' in node '" + getName() + "'");
			
					foundField = outputFields.get(index);
					break;
				}
			}

			if(foundField == null) {
				outputFields.add(field);
				fireFieldAdded(field);
			} else {
				foundField.setDescription(field.getDescription());
				foundField.setOutputType(field.getOutputType());
			}
		}
	}
	
	/**
	 * Removes an input field from this node. Note that {@link #ENABLED_FIELD}
	 * cannot be removed.
	 * 
	 * @param field  the field
	 */
	public final void removeField(InputField field) {
		if(field != ENABLED_FIELD) {
			if(inputFields.remove(field))
				fireFieldRemoved(field);
		}
	}
	
	/**
	 * Removes all input fields from this node, except for {@link #ENABLED_FIELD}.
	 */
	public final void removeAllInputFields() {
		final ArrayList<InputField> fieldsCopy = new ArrayList<InputField>(inputFields);
		for(InputField field : fieldsCopy)
			removeField(field);
	}
	
	/**
	 * Removes an output field from this node.
	 * 
	 * @param field  the field
	 */
	public final void removeField(OutputField field) {
		if(outputFields.remove(field))
			fireFieldRemoved(field);
	}
	
	/**
	 * Removes all output fields from this node.
	 */
	public final void removeAllOutputFields() {
		final ArrayList<OutputField> fieldsCopy = new ArrayList<OutputField>(outputFields);
		for(OutputField field : fieldsCopy)
			removeField(field);
	}
	
	/**
	 * Gets the input field with a specified key
	 *  
	 * @param key  the key
	 * 
	 * @return the input field that has the specified key, or <code>null</code>
	 *         if no input field exists with this key
	 */
	public final InputField getInputFieldWithKey(String key) {
		for(InputField field : inputFields) {
			if(field.getKey().equals(key))
				return field;
		}
		return null;
	}
	
	/**
	 * Gets the output field with a specified key
	 *  
	 * @param key  the key
	 * 
	 * @return the output field that has the specified key, or <code>null</code>
	 *         if no output field exists with this key
	 */
	public final OutputField getOutputFieldWithKey(String key) {
		for(OutputField field : outputFields) {
			if(field.getKey().equals(key))
				return field;
		}
		return null;
	}
	
	/**
	 * Gets the list of input fields.
	 * 
	 * @return  the {@link List} of input fields (immutable)
	 */
	public final List<InputField> getInputFields() {
		return Collections.unmodifiableList(inputFields);
	}
	
	/**
	 * Gets the list of output fields.
	 * 
	 * @return  the {@link List} of output fields (immutable)
	 */
	public final List<OutputField> getOutputFields() {
		return Collections.unmodifiableList(outputFields);
	}
	
	/**
	 * Have this node perform its operation.
	 * 
	 * The given {@link OpContext} contains all inputs supplied by other nodes
	 * (if any) and can be acquired by {@link OpContext#get(Object)}. If an input
	 * is optional, it is up to the implementing class to call {@link OpContext#containsKey(Object)}
	 * to see if the input was supplied. Finally, computed outputs should be placed
	 * into the given context under the appropriate {@link OutputField}s.
	 * 
	 * @param context  the working context
	 * 
	 * @throws ProcessingException  if any errors occurred during the operation
	 */
	public abstract void operate(OpContext context) throws ProcessingException;
	
	//
	// Extendable
	//
	
	private ExtendableSupport extendableSupport = new ExtendableSupport(OpNode.class);
	
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
	
	//
	// Listeners
	//
	
	private final ArrayList<OpNodeListener> listeners = new ArrayList<OpNodeListener>();

	/**
	 * Adds a listener to this node.
	 * 
	 * @param listener  the listener to add
	 */
	public void addNodeListener(OpNodeListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes a listener from this node.
	 * 
	 * @param listener  the listener to remove
	 */
	public void removeNodeListener(OpNodeListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}

	private void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		synchronized(listeners) {
			for(OpNodeListener listener : listeners)
				listener.nodePropertyChanged(propertyName, oldValue, newValue);
		}
	}

	private void fireFieldAdded(InputField field) {
		synchronized(listeners) {
			for(OpNodeListener listener : listeners)
				listener.fieldAdded(this, field);
		}
	}

	private void fireFieldRemoved(InputField field) {
		synchronized(listeners) {
			for(OpNodeListener listener : listeners)
				listener.fieldRemoved(this, field);
		}
	}

	private void fireFieldAdded(OutputField field) {
		synchronized(listeners) {
			for(OpNodeListener listener : listeners)
				listener.fieldAdded(this, field);
		}
	}

	private void fireFieldRemoved(OutputField field) {
		synchronized(listeners) {
			for(OpNodeListener listener : listeners)
				listener.fieldRemoved(this, field);
		}
	}
}
