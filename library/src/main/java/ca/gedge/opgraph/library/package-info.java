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
 * Provides classes and interfaces for creating a node library. The library
 * provides two ways to find nodes:
 * <ul>
 *   <li>through explicit registration via an appropriate URI, and</li>
 *   <li>via service discovery (through <code>META-INF/services/ca.gedge.opgraph.OpNode</code>)</li>
 * </ul>
 */
package ca.gedge.opgraph.library;
