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
package ca.gedge.opgraph.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * A default service discovery class which mimics the standard library's
 * {@link java.util.ServiceLoader} for service discovery.
 *
 * Static methods are provided ({@link #addClassLoader(ClassLoader)} and
 * {@link #removeClassLoader(ClassLoader)}) to provide additional, custom
 * class loaders to search through for service providers. Note that the
 * system class loader will always be used, along with the class loader used
 * to load the class given to {@link #findProviders(Class)}.
 *
 * TODO caching
 */
public class DefaultServiceDiscovery extends ServiceDiscovery {
	/** The resource prefix to search through */
	private static final String SERVICE_PREFIX = "META-INF/services/";

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(DefaultServiceDiscovery.class.getName());

	/** An additional set of classloaders to search through */
	private final static Set<ClassLoader> classloaders = new HashSet<ClassLoader>();

	/**
	 * Adds a custom classloader to search through for service providers.
	 *
	 * @param classloader  the classloader to add
	 */
	public static void addClassLoader(ClassLoader classloader) {
		classloaders.add(classloader);
	}

	/**
	 * Removes a custom classloader from the searchable classloaders.
	 *
	 * @param classloader  the classloader to remove
	 */
	public static void removeClassLoader(ClassLoader classloader) {
		classloaders.remove(classloader);
	}

	@Override
	public <T> List<Class<? extends T>> findProviders(final Class<T> service) {
		final Set<ClassLoader> classloaders = new HashSet<ClassLoader>();
		classloaders.addAll(DefaultServiceDiscovery.classloaders);
		classloaders.add(service.getClassLoader());
		classloaders.add(ClassLoader.getSystemClassLoader());

		// Get the resource URL and iterate through them
		final List<DiscoveryData> dataList = getResourceURLs(classloaders, SERVICE_PREFIX + service.getName(), false);
		final List<Class<? extends T>> providersList = new ArrayList<Class<? extends T>>();
		for(DiscoveryData data : dataList) {
			try {
				Iterator<String> linesPending = new LineIterator(data.url.openStream());

				while(linesPending.hasNext()) {
					final String line = linesPending.next().trim();
					try {
						if(line.length() > 0) {
							final Class<?> rawClass = Class.forName(line, false, data.classloader);
							providersList.add(rawClass.asSubclass(service));
						}
					} catch(ClassNotFoundException exc) {
						LOGGER.warning("Classloader '" + data.classloader + "' could not find class");
					} catch(ClassCastException exc) {
						LOGGER.warning("URL '" + data.url + "' contains invalid provider: " + line);
					}
				}
			} catch(IOException exc) {
				LOGGER.warning("Could not open service provider file " + data.url);
			}
		}

		return providersList;
	}

	@Override
	public List<URL> findResources(String path) {
		final Set<ClassLoader> classloaders = new HashSet<ClassLoader>();
		classloaders.addAll(DefaultServiceDiscovery.classloaders);
		classloaders.add(ClassLoader.getSystemClassLoader());

		final List<URL> resourceURLs = new ArrayList<URL>();
		final List<DiscoveryData> dataList = getResourceURLs(classloaders, path, false);
		for(DiscoveryData data : dataList)
			resourceURLs.add(data.url);

		return resourceURLs;
	}

	/**
	 * Data pertaining to provider discovery.
	 */
	private static class DiscoveryData {
		/** The classloader that discovered the URL */
		public final ClassLoader classloader;

		/** URL to the resource */
		public final URL url;

		/** A key associated with this resource */
		@SuppressWarnings("unused")
		public final String key;

		/**
		 * Default constructor.
		 *
		 * @param classloader  the classloader used to find the class/resournce
		 * @param url  URL to the resource
		 * @param key  key associated with the resource
		 */
		public DiscoveryData(ClassLoader classloader, URL url, String key) {
			this.classloader = classloader;
			this.url = url;
			this.key = key;
		}
	}

	/**
	 * Gets a list resource URLs for a service.
	 *
	 * @param path  the path to look for
	 * @param mapped  if <code>true</code>, the service acts as a prefix for
	 *                a directory containing files where the filename is the
	 *                key. Otherwise, the service itself is the file.
	 */
	private List<DiscoveryData> getResourceURLs(Iterable<ClassLoader> classloaders, String path, boolean mapped) {
		final ArrayList<DiscoveryData> data = new ArrayList<DiscoveryData>();
		for(ClassLoader classloader : classloaders) {
			String basePath = path;

			Enumeration<URL> baseURLs = null;
			try {
				baseURLs = classloader.getResources(basePath);
			} catch(IOException exc) {
				LOGGER.warning("Could not loaded resource URLs from classloader " + classloader);
				continue;
			}

			while(baseURLs.hasMoreElements()) {
				final URL baseURL = baseURLs.nextElement();
				if(mapped) {
					try {
						if(baseURL.getProtocol().equals("jar")) {
							// Make sure there's a path separator at the end
							if(!basePath.endsWith("/"))
								basePath += '/';

							// Jar file, look inside for entries
							final URLConnection connection = baseURL.openConnection();
							if(connection instanceof JarURLConnection) {
								final JarURLConnection jarConnection = (JarURLConnection)connection;
								final JarFile jarFile = jarConnection.getJarFile();

								// Iterate through entries
								final Enumeration<JarEntry> entries = jarFile.entries();
								while(entries.hasMoreElements()) {
									// Don't accept directories, or entries that aren't a part of
									// the base path
									final JarEntry jarEntry = entries.nextElement();
									if(jarEntry.isDirectory() || !jarEntry.getName().startsWith(basePath))
										continue;

									final String key = jarEntry.getName().substring(basePath.length());
									data.add(new DiscoveryData(classloader, new URL(baseURL, key), key));
								}
							}
						} else if(baseURL.getProtocol().equals("file")) {
							// Find files in directory
							final File dir = new File(URLDecoder.decode(baseURL.getPath(), "UTF-8"));
							if(dir.isDirectory()) {
								for(File file : dir.listFiles()) {
									if(!file.isDirectory()) {
										final URL url = file.toURI().toURL();
										data.add(new DiscoveryData(classloader, url, file.getName()));
									}
								}
							}
						}
					} catch(IOException exc) {
						LOGGER.warning("IOException getting resource url from " + baseURL);
					}
				} else {
					data.add(new DiscoveryData(classloader, baseURL, null));
				}
			}
		}

		return data;
	}
}
