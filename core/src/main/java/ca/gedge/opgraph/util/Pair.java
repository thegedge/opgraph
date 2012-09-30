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

/**
 * A 2-tuple.
 * 
 * @param <F>  type of the first element
 * @param <S>  type of the second element
 */
public class Pair<F, S> implements Comparable<Pair<? extends F, ? extends S>> {
	/** The first element */
	private F first;
	
	/** The second element */
	private S second;
	
	/**
	 * Constructs a pair with <code>null</code> elements.
	 */
	public Pair() {
		this(null, null);
	}
	
	/**
	 * Constructs a pair with a specified first/second object.
	 * 
	 * @param first  the first element
	 * @param second  the second element
	 */
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}
	
	/**
	 * Gets the first element.
	 * 
	 * @return the first element in this pair
	 */
	public F getFirst() {
		return first;
	}
	
	/**
	 * Sets the first element.
	 * 
	 * @param first  the first element
	 */
	public void setFirst(F first) {
		this.first = first;
	}
	
	/**
	 * Gets the second element.
	 * 
	 * @return the second element in this pair
	 */
	public S getSecond() {
		return second;
	}
	
	/**
	 * Sets the second element.
	 * 
	 * @param second  the second element
	 */
	public void setSecond(S second) {
		this.second = second;
	}
	
	/**
	 * Gets whether or not two objects are both <code>null</code>, or if they
	 * are equal, as defined by their implementation of {@link Object#equals(Object)}.
	 * 
	 * @param a  an object
	 * @param b  an object
	 * 
	 * @return <code>true</code> if objects both <code>null</code> or equal,
	 *         <code>false</code> otherwise
	 */
	public static boolean objectsEqual(Object a, Object b) {
		return (a == null && b == null) || (a != null && a.equals(b));
	}

	//
	// Overrides
	//
	
	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(o == null) return false;
		
		if(o instanceof Pair) {
			final Pair<?, ?> b = (Pair<?, ?>)o;
			return objectsEqual(first, b.first) && objectsEqual(second, b.second);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		final int hash1 = (first == null ? 0 : first.hashCode());
		final int hash2 = (second == null ? 0 : second.hashCode());
		return 19*hash1 + 31*hash2;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		
		builder.append('(');
		builder.append(String.valueOf(first));
		builder.append(", ");
		builder.append(String.valueOf(second));
		builder.append(')');
		
		return builder.toString();
	}
	
	//
	// Comparable
	//

	@SuppressWarnings("unchecked")
	private <T> int compare(T a, T b) {
		if(a == null && b == null) return 0;
		if(a == null) return 1;
		if(b == null) return -1;

		if(a instanceof Comparable)
			return ((Comparable<T>)a).compareTo(b);
		
		return (a.equals(b) ? 0 : 1); 
	}
	
	@Override
	public int compareTo(Pair<? extends F, ? extends S> o) {
		final int firstComp = compare(first, o.first);
		if(firstComp == 0)
			return compare(second, o.second);
		return firstComp;
	}	
}
