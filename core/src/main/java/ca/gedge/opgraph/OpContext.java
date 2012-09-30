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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A working context for {@link OpGraph}s. A context can have a parent
 * from which it can find values.
 */
public final class OpContext extends HashMap<String, Object> {
	/** The parent context */
	private OpContext parent;
	
	/** The child contexts */
	private WeakHashMap<OpNode, OpContext> childContexts;
	
	/**
	 * Constructs a global context (i.e., no parent context).
	 */
	public OpContext() {
		this(null);
	}
	
	/**
	 * Constructs a context with the given parent context.
	 * 
	 * @param parent  parent context
	 */
	public OpContext(OpContext parent) {
		this.parent = parent;
	}
	
	/**
	 * Gets the parent context for this context.
	 * 
	 * @return the parent context
	 */
	public OpContext getParent() {
		return parent;
	}
	
	/**
	 * Finds a context for the specified node. This is a deep operation which
	 * will recursively search through all child contexts to find one for the
	 * given node. 
	 * 
	 * @param node  the node to get a context for
	 * 
	 * @return the context for the specified node, or <code>null</code> if
	 *         no context could be found for the given node.
	 */
	public OpContext findChildContext(OpNode node) {
		OpContext context = null;
		if(childContexts != null) {
			// First do a shallow search
			for(Map.Entry<OpNode, OpContext> entry : childContexts.entrySet()) {
				if(entry.getKey() == node) {
					context = entry.getValue();
					break;
				}
			}
			
			// Didn't find one? Do a deep search
			if(context == null) {
				for(OpContext childContext : childContexts.values()) {
					context = childContext.findChildContext(node);
					if(context != null)
						break;
				}
			}
		}
		
		return context;
	}
	
	/**
	 * Gets all the child contexts of this context.
	 * 
	 * @return the mapping of node to context
	 */
	public Map<OpNode, OpContext> getChildContexts() {
		return Collections.unmodifiableMap(childContexts);
	}
	
	/**
	 * Gets a context for the specified node. If no child context currently 
	 * exists for the given node, a new context will be constructed. The 
	 * returned context will be parented to this context.
	 * 
	 * @param node  the node
	 * 
	 * @return the context
	 */
	public OpContext getChildContext(OpNode node) {
		if(childContexts == null)
			childContexts = new WeakHashMap<OpNode, OpContext>();
		
		if(!childContexts.containsKey(node))
			childContexts.put(node, new OpContext(this));
		
		return childContexts.get(node);
	}
	
	/**
	 * Collects values of a given field from all child contexts.
	 * 
	 * @param field  the field to search for
	 * 
	 * @return a {@link Map} of all the values, and their associated nodes
	 *         as keys (with this context having a <code>null</code> key)
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<OpNode, T> collectValues(ContextualItem field) {
		Map<OpNode, T> results = new HashMap<OpNode, T>();
		
		if(containsKey(field)) {
			final Object val = get(field);
			try {
				results.put(null, (T)val);
			} catch(ClassCastException exc) { }
		}
		
		for(Map.Entry<OpNode, OpContext> entry: childContexts.entrySet()) {
			final OpContext context = entry.getValue();
			if(context.containsKey(field)) {
				final Object val = context.get(field);
				try {
					results.put(entry.getKey(), (T)val);
				} catch(ClassCastException exc) { }
			}
		}
		
		return results;
	}
	
	/**
	 * Removes all child contexts in this context.
	 */
	public void clearChildContexts() {
		childContexts.clear();
	}
	
	//
	// Sort-of overrides
	//
	
	/**
	 * Maps the key of a given contextual item to an object.
	 * 
	 * @param item  the {@link ContextualItem} whose key will be used for mapping
	 * @param value  the value to store
	 * 
	 * @return same as {@link HashMap#put(Object, Object)}
	 */
	public Object put(ContextualItem item, Object value) {
		return (item == null ? null : put(item.getKey(), value));
	}
	
	/**
	 * Removes the value associated with the key of a given contextual item.
	 * 
	 * @param item  the {@link ContextualItem} whose key will be used for mapping 
	 * 
	 * @return same as {@link #remove(Object)}
	 */
	public Object remove(ContextualItem item) {
		return (item == null ? null : remove(item.getKey()));
	}
	
	/**
	 * Gets whether or not this context contains the key associated with a
	 * given contextual item. 
	 * 
	 * @param item  the {@link ContextualItem} whose key will be used for mapping
	 * 
	 * @return same as {@link #containsKey(Object)} 
	 */
	public boolean containsKey(ContextualItem item) {
		return (item == null ? false : containsKey(item.getKey()));
	}
	
	/**
	 * Gets the object associated with the key of a specified contextual item.
	 * 
	 * @param item  the {@link ContextualItem} whose key will be used for mapping
	 * 
	 * @return same as {@link #get(Object)}
	 */
	public Object get(ContextualItem item) {
		return (item == null ? null : get(item.getKey()));
	}
	
	//
	// Overrides
	//

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		final Set<java.util.Map.Entry<String, Object>> entries = super.entrySet(); 
		if(parent != null)
			entries.addAll(parent.entrySet());
		return entries;
	}

	@Override
	public Collection<Object> values() {
		final Collection<Object> values = super.values(); 
		if(parent != null)
			values.addAll(parent.values());
		return values;
	}

	@Override
	public Set<String> keySet() {
		final Set<String> keys = super.keySet(); 
		if(parent != null)
			keys.addAll(parent.keySet());
		return keys;
	}

	@Override
	public void clear() {
		super.clear();
		if(childContexts != null)
			childContexts.clear();
	}
	
	@Override
	public Object put(String key, Object value) {
	    return super.put(key, value);
	}
	
	@Override
	public Object remove(Object key) {
	    return super.remove(key);
	}
	
	@Override
	public Object get(Object key) {
		if(super.containsKey(key))
			return super.get(key);
		return (parent == null ? null : parent.get(key));
	}
	
	@Override
	public boolean containsKey(Object key) {
		boolean ret = super.containsKey(key);
		if(!ret && parent != null)
			ret = parent.containsKey(key);
		return ret;
	}
	
	@Override
	public boolean containsValue(Object value) {
		boolean ret = super.containsValue(value);
		if(!ret && parent != null)
			ret = parent.containsValue(value);
		return ret;
	}
}
