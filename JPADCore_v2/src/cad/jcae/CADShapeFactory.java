/* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
   modeler, Finite element mesher, Plugin architecture.

    Copyright (C) 2004,2005, by EADS CRC
    Copyright (C) 2007,2008, by EADS France

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package cad.jcae;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to provide factory methods
 */
public abstract class CADShapeFactory
{
	private static final Logger LOGGER=Logger.getLogger(CADShapeFactory.class.getName());
	private static CADShapeFactory factory;
	
	protected CADShapeFactory()
	{
	}

	public static void setFactory(CADShapeFactory f)
	{
		if(factory == null)
			factory = f;
		else
			throw new IllegalStateException("Factory already set to "+factory+".");
	}

	/**
	 * Return factory instance
	 * @return factory instance
	 */
	public static CADShapeFactory getFactory()
	{
		if(factory == null)
		{
			String cadType = System.getProperty("org.jcae.mesh.cad");
			if (cadType == null)
			{
				cadType = "org.jcae.mesh.cad.occ.OCCShapeFactory";
				System.setProperty("org.jcae.mesh.cad", cadType);
			}
			try
			{
				factory = (CADShapeFactory) Class.forName(cadType).newInstance();
			}
			catch (Exception e)
			{
				LOGGER.severe("Class "+cadType+" not found");
				LOGGER.log(Level.SEVERE, null, e);
				System.exit(1);
			}
		}
		return factory;
	}
	
	/**
	 * Create a new CADShape wrapping an object of the underlying implementation
	 * @param o An object of the underlying implementation
	 * @return The created CADShape
	 */
	public abstract CADShape newShape (Object o);
	
	/**
	 * Create a new CADShape with boolean operation on 2 sshapes.
	 * @param s1 First shape
	 * @param s2 Second shape
	 * @param op Boolean operator
	 * @return The created CADShape
	 */
	public abstract CADShape newShape (CADShape s1, CADShape s2, char op);
	
	/**
	 * Create a new CADShape by loading it from a file
	 * @param fileName The file to read
	 * @return The created CADShape
	 */
	public abstract CADShape newShape (String fileName);
	
	/**
	 * Create a new CADExplorer
	 * @return The created CADExplorer
	 */
	public abstract CADExplorer newExplorer ();
	
	/**
	 * Create a new CADWireExplorer
	 * @return The created CADWireExplorer
	 */
	public abstract CADWireExplorer newWireExplorer ();
	
	/*
	 * This method is needed to initialize CADShapeEnum
	 */
	protected abstract CADShapeTypes getShapeEnumInstance(String name);
	protected abstract Iterator<CADShapeTypes> newShapeEnumIterator(CADShapeTypes start, CADShapeTypes end);

	/**
	 * Create a new CADIterator
	 * @return The created CADIterator
	 */
	public abstract CADIterator newIterator ();
	
	/**
	 * Create a new CADGeomCurve2D
	 * @param E The edge owning the curve
	 * @param F The face owning the curve
	 * @return The created CADGeomCurve2D
	 */
	public abstract CADGeomCurve2D newCurve2D(CADEdge E, CADFace F);
	
	/**
	 * Create a new CADGeomCurve3D
	 * @param E The edge owning this curve
	 * @return The created CADGeomCurve3D
	 */
	public abstract CADGeomCurve3D newCurve3D(CADEdge E);

}
