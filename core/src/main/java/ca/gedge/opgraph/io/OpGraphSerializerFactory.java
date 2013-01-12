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
package ca.gedge.opgraph.io;

import java.util.List;
import java.util.logging.Logger;

import ca.gedge.opgraph.util.ServiceDiscovery;

/**
 * A factory for discovering and constructing {@link OpGraphSerializer}s.
 */
public abstract class OpGraphSerializerFactory {
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(OpGraphSerializerFactory.class.getName());

	/** System property for defining the default serializer */
	public static final String DEFAULT_SERIALIZER_PROPERTY = "ca.gedge.defaultSerializer";

	/**
	 * Gets a default serializer.
	 * 
	 * @return the serializer that is registered to be the default, or 
	 *         <code>null</code> if no such serializer exists
	 */
	public static OpGraphSerializer getDefaultSerializer() {
		OpGraphSerializer serializer = null;

		// See if there's a default one defined via a system property first...
		final String defaultClass = System.getProperty(DEFAULT_SERIALIZER_PROPERTY);
		if(defaultClass != null) {
			Class<? extends OpGraphSerializer> serializerCls = null;

			try {
				serializerCls = Class.forName(defaultClass).asSubclass(OpGraphSerializer.class); 
				serializer = serializerCls.newInstance();
			} catch(ClassNotFoundException exc) {
				LOGGER.severe("Service '" + defaultClass + "' does not provide an empty constructor!");
			} catch(InstantiationException exc) {
				LOGGER.severe("Service '" + defaultClass + "' does not provide an empty constructor!");
			} catch(IllegalAccessException exc) {
				LOGGER.severe("Service '" + defaultClass + "' does not provide an accessible empty constructor!");
			}
		}

		// ...No? Try to discover one, and take the first
		if(serializer == null) {
			for(Class<? extends OpGraphSerializer> serializerCls : getSerializers()) {
				try {
					serializer = serializerCls.newInstance();
				} catch(InstantiationException exc) {
					LOGGER.severe("Service '" + serializerCls.getName() + "' does not provide an empty constructor!");
				} catch(IllegalAccessException exc) {
					LOGGER.severe("Service '" + serializerCls.getName() + "' does not provide an accessible empty constructor!");
				}
			}
		}

		return serializer;
	}

	/**
	 * Gets a serializer by the file extension it understands.
	 * 
	 * @param extension  the extension
	 * 
	 * @return the serializer that reads and write files with the given
	 *         extension, or <code>null</code> if no such serializer exists
	 */
	public static OpGraphSerializer getSerializerByExtension(String extension) {
		OpGraphSerializer serializer = null;
		for(Class<? extends OpGraphSerializer> serializerCls : getSerializers()) {
			final OpGraphSerializerInfo info = serializerCls.getAnnotation(OpGraphSerializerInfo.class);
			if(info != null && info.extension().equalsIgnoreCase(extension)) {
				try {
					serializer = serializerCls.newInstance();
				} catch(InstantiationException exc) {
					LOGGER.severe("Service '" + serializerCls.getName() + "' does not provide an empty constructor!");
				} catch(IllegalAccessException exc) {
					LOGGER.severe("Service '" + serializerCls.getName() + "' does not provide an accessible empty constructor!");
				}
				break;
			}
		}

		return serializer;
	}

	/**
	 * Gets all registered serializers.
	 * 
	 * @return list of serializers
	 */
	public static List<Class<? extends OpGraphSerializer>> getSerializers() {
		return ServiceDiscovery.getInstance().findProviders(OpGraphSerializer.class);
	}
}
