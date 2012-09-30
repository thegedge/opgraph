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
package ca.gedge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

/**
 * A set of assertions that make comparing equality of collections and
 * arrays of objects simpler.
 */
public class CollectionsAssert extends Assert {
	protected CollectionsAssert() {}
	
	/**
	 * Assert if a collection of objects is equal to the given array. Order
	 * is insignificant in this comparison.
	 * 
	 * @param <T>  the type of elements contained in the collection
	 * 
	 * @param message  the message to display upon failure
	 * @param a  the collection of objects
	 * @param b  the array of objects
	 */
	public static <T> void assertCollectionEqualsArray(String message, Collection<T> a, T... b) {
		// Quick check to ensure sizes equal 
		if(a.size() != b.length)
			fail(message);
		
		// Go through all items in collection, and remove them from the list
		final List<T> listB = new ArrayList<T>(Arrays.asList(b));
		for(T value : a) {
			// If this item could not be removed from the list, the
			// collection and array are not equal
			if(!listB.remove(value))
				fail(message);
		}
	}
	
	/**
	 * Assert if a collection of objects is equal to the given array. Order
	 * is insignificant in this comparison. A default message is used.
	 * 
	 * @param <T>  the type of elements contained in the collection
	 * 
	 * @param a  the collection of objects
	 * @param b  the array of objects
	 * 
	 * @see #assertCollectionEqualsArray(String, Collection, Object...)
	 */
	public static <T> void assertCollectionEqualsArray(Collection<T> a, T... b) {
		assertCollectionEqualsArray("Collection and array not equal", a, b);
	}
}
