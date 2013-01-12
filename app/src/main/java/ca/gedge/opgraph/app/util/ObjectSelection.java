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
package ca.gedge.opgraph.app.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * A transferable that deals with the transfer of an object of a specific class.
 */
public class ObjectSelection implements Transferable {
	/** The object being transferred */
	private Object obj;

	/**
	 * Constructs a transferable with a given object.
	 * 
	 * @param obj  the object
	 */
	public ObjectSelection(Object obj) {
		this.obj = obj;
	}

	//
	// Transferable
	//

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if(isDataFlavorSupported(flavor))
			return obj;
		throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor [] getTransferDataFlavors() {
		final Class<?> clz = (obj == null ? Object.class : obj.getClass());
		return new DataFlavor[]{new DataFlavor(clz, clz.getSimpleName())};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (obj == null || flavor.getRepresentationClass().isAssignableFrom(obj.getClass()));
	}

}
