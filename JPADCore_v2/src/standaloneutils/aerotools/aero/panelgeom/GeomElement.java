/*
 *   GeomElement  -- Interface in common to all panel geometry elements.
 *
 *   Copyright (C) 2002-2014, Joseph A. Huwaldt
 *   All rights reserved.
 *   
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *   
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *   Or visit:  http://www.gnu.org/licenses/lgpl.html
 */
package standaloneutils.aerotools.aero.panelgeom;

import java.util.Map;

/**
 * Defines the interface in common to all panel geometry elements.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: March 31, 2000
 * @version April 3, 2014
 */
public interface GeomElement extends java.io.Serializable, Cloneable {

    /**
     * Return the name of this geometry element.
     *
     * @return The name of this geometry element as a String.
     */
    public String getName();

    /**
     * Change the name of this geometry element.
     *
     * @param newName The new name to be given to this geometry element.
     */
    public void setName(String newName);

    /**
     * Returns the number of panels that make up this geometry.
     */
    public int getNumberOfPanels();

    /**
     * Return the surface area of this geometry element.
     *
     * @return The surface area of this geometry element.
     */
    public double getArea();

    /**
     * Return the minimum bounding X coordinate for this geometry element.
     *
     * @return The minimum X coordinate found in this element.
     */
    public double getMinX();

    /**
     * Return the maximum bounding X coordinate for this geometry element.
     *
     * @return The maximum X coordinate found in this element.
     */
    public double getMaxX();

    /**
     * Return the minimum bounding Y coordinate for this geometry element.
     *
     * @return The minimum Y coordinate found in this element.
     */
    public double getMinY();

    /**
     * Return the maximum bounding Y coordinate for this geometry element.
     *
     * @return The maximum Y coordinate found in this element.
     */
    public double getMaxY();

    /**
     * Return the minimum bounding Z coordinate for this geometry element.
     *
     * @return The minimum Z coordinate found in this element.
     */
    public double getMinZ();

    /**
     * Return the maximum bounding Z coordinate for this geometry element.
     *
     * @return The maximum Z coordinate found in this element.
     */
    public double getMaxZ();

    /**
     * Return the coordinate point representing the minimum bounding box corner (min X,
     * min Y, min Z).
     *
     * @return The minimum bounding box coordinate for this geometry element.
     */
    public GeomPoint getBoundsMin();

    /**
     * Return the coordinate point representing the maximum bounding box corner (max X,
     * max Y, max Z).
     *
     * @return The maximum bounding box coordinate for this geometry element.
     */
    public GeomPoint getBoundsMax();

    /**
     * Translate this element by an incremental amount along each axis.
     *
     * @param dx The amount to translate in the X direction.
     * @param dy The amount to translate in the Y direction.
     * @param dz The amount to translate in the Z direction.
     */
    public void translate(double dx, double dy, double dz);

    /**
     * Scale this element by the given scale factor in each axis direction.
     *
     * @param sx The amount to scale the element in the X direction.
     * @param sy The amount to scale the element in the Y direction.
     * @param sz The amount to scale the element in the Z direction.
     */
    public void scale(double sx, double sy, double sz);

    /**
     * Store application specific data that can be assigned to this geometry element. This
     * can be used to store any application specific information that needs to be
     * associated with this geometry. If you make use of serialization, make sure any data
     * supplied is Serializable.
     *
     * @param key The key used to store and retrieve the application specific data
     *  assigned to this geometry element.
     * @param data The application specific object to be assigned to this geometry
     *  element.
     */
    public void putData(Object key, Object data);

    /**
     * Return the application specific object associated with the specified key that has
     * been assigned to this geometry element.
     *
     * @param key The key used to store and retrieve the application specific data.
     * @return The application specific object assigned to this geometry element. Returns
     *  null if no object using the specified key has been assigned to this element.
     */
    public Object getData(Object key);

    /**
     * Return a reference to a Map containing all application specific objects associated
     * with this geometry element. This allows a developer to easily manipulate
     * application specific data that is associated with this geometry element using the
     * Map interface.
     *
     * @return A reference to the Map object used to store application specific data along
     *  with this geometry element. Returns null if no objects have been assigned to this
     *  element.
     */
    public Map getAllData();

    /**
     * Make a copy of this geometry element object.
     *
     * @return A copy of this geometry element object.
     */
    public Object clone();
}
