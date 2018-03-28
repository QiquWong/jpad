/*
 *   GeomElementList  -- Interface and implementation in common to all panel geometry element lists.
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

import java.util.*;

/**
 * Defines the interface and implementation in common to all panel geometry element lists.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: March 31, 2000
 * @version April 3, 2014
 */
public abstract class GeomElementList extends AbstractList implements GeomElement {

    /**
     * Name of this geometry element.
     */
    private String name;
    
    /**
     * Array of sub-elements.
     */
    protected List subElements = new ArrayList();
    
    /**
     * Application specific data/objects that can be assigned to this geometry element.
     */
    private Map appData;
    

    /**
     * Create an empty geometry element list (one that does not contain any sub-elements).
     */
    protected GeomElementList() {
    }

	/**
	*  Create an empty geometry element list with the specified name.
	*/
	protected GeomElementList(String name) {
        this.name = name;
    }


	//**** Methods required to implement AbstractList  ******
	
	/**
	*  Returns the number of sub-elements in this geometry element.
	*
	*  @return The number of sub-elements in this geometry element.
	*/
    @Override
    public int size() {
        return subElements.size();
    }

    /**
     * Returns the sub-element at the specified position in this geometry element.
     *
     * @param index The index of the sub-element to return.
     * @return The sub-element at the specified position in this geometry element.
     */
    @Override
    public Object get(int index) {
        return subElements.get(index);
    }

    /**
     * Remove the sub-element at the specified position in this geometry element. Shifts
     * any subsequent sub-elements to the left (subtracts (one from their indices).
     * Returns the sub-element that was removed from this geometry element.
     *
     * @param index The index of the sub-element to remove.
     * @return The sub-element previously at the specified position.
     */
    @Override
    public Object remove(int index) {
        return subElements.remove(index);
    }

    /**
     * Removes all the sub-elements from this geometry element. The geometry element will
     * be empty after this call returns (unless it throws an exception).
     */
    @Override
    public void clear() {
        subElements.clear();
    }

    /**
     * Return an enumeration of all the sub-elements in this geometry element.
     *
     * @return An iteration of all the sub-elements in this geometry element.
     */
    @Override
    public Iterator iterator() {
        return subElements.iterator();
    }

    // *******  The following support geometry element requirements *****
    /**
     * Return the name of this geometry element.
     *
     * @return The name of this geometry element as a String.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Change the name of this geometry element.
     *
     * @param newName The new name to be given to this geometry element.
     */
    @Override
    public void setName(String newName) {
        name = newName;
    }

    /**
     * Return the total number of panels in this geometry element.
     *
     * @return Total number of panels in this geometry element.
     */
    @Override
    public int getNumberOfPanels() {
        int count = 0;

        int size = size();
        for (int i = 0; i < size; ++i) {
            GeomElement obj = (GeomElement) get(i);
            count += obj.getNumberOfPanels();
        }

        return count;
    }

    /**
     * Return the surface area of this geometry element.
     *
     * @return The surface area of this geometry element.
     */
    @Override
    public double getArea() {
        double area = 0;

        int size = size();
        for (int i = 0; i < size; ++i) {
            GeomElement obj = (GeomElement) get(i);
            area += obj.getArea();
        }
        return area;
    }

    /**
     * Return the minimum bounding X coordinate for this geometry element.
     *
     * @return The minimum X coordinate found in this element.
     */
    @Override
    public double getMinX() {
        double minX = Double.MAX_VALUE;

        int size = size();
        for (int i = 0; i < size; ++i) {
            GeomElement obj = (GeomElement) get(i);
            minX = Math.min(minX, obj.getMinX());
        }

        if (size() == 0)
            minX = 0;

        return minX;
    }

    /**
     * Return the maximum bounding X coordinate for this geometry element.
     *
     * @return The maximum X coordinate found in this element.
     */
    @Override
    public double getMaxX() {
        double maxX = -Double.MAX_VALUE;

        int size = size();
        for (int i = 0; i < size; ++i) {
            GeomElement obj = (GeomElement) get(i);
            maxX = Math.max(maxX, obj.getMaxX());
        }

        if (size() == 0)
            maxX = 0;

        return maxX;
    }

    /**
     * Return the minimum bounding Y coordinate for this geometry element.
     *
     * @return The minimum Y coordinate found in this element.
     */
    @Override
    public double getMinY() {
        double minY = Double.MAX_VALUE;

        int size = size();
        for (int i = 0; i < size; ++i) {
            GeomElement obj = (GeomElement) get(i);
            minY = Math.min(minY, obj.getMinY());
        }

        if (size() == 0)
            minY = 0;

        return minY;
    }

    /**
     * Return the maximum bounding Y coordinate for this geometry element.
     *
     * @return The maximum Y coordinate found in this element.
     */
    @Override
    public double getMaxY() {
        double maxY = -Double.MAX_VALUE;

        int size = size();
        for (int i = 0; i < size; ++i) {
            GeomElement obj = (GeomElement) get(i);
            maxY = Math.max(maxY, obj.getMaxY());
        }

        if (size() == 0)
            maxY = 0;

        return maxY;
    }

    /**
     * Return the minimum bounding Z coordinate for this geometry element.
     *
     * @return The minimum Z coordinate found in this element.
     */
    @Override
    public double getMinZ() {
        double minZ = Double.MAX_VALUE;

        int size = size();
        for (int i = 0; i < size; ++i) {
            GeomElement obj = (GeomElement) get(i);
            minZ = Math.min(minZ, obj.getMinZ());
        }

        if (size() == 0)
            minZ = 0;

        return minZ;
    }

    /**
     * Return the maximum bounding Z coordinate for this geometry element.
     *
     * @return The maximum Z coordinate found in this element.
     */
    @Override
    public double getMaxZ() {
        double maxZ = -Double.MAX_VALUE;

        int size = size();
        for (int i = 0; i < size; ++i) {
            GeomElement obj = (GeomElement) get(i);
            maxZ = Math.max(maxZ, obj.getMaxZ());
        }

        if (size() == 0)
            maxZ = 0;

        return maxZ;
    }

    /**
     * Return the coordinate point representing the minimum bounding box corner (min X,
     * min Y, min Z).
     *
     * @return The minimum bounding box coordinate for this geometry element.
     */
    @Override
    public GeomPoint getBoundsMin() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;

        int size = size();
        for (int i = 0; i < size; ++i) {
            GeomElement obj = (GeomElement) get(i);
            minX = Math.min(minX, obj.getMinX());
            minY = Math.min(minY, obj.getMinY());
            minZ = Math.min(minZ, obj.getMinZ());
        }

        if (size() == 0) {
            minX = 0;
            minY = 0;
            minZ = 0;
        }

        return GeomPoint.getInstance(minX, minY, minZ);
    }

    /**
     * Return the coordinate point representing the maximum bounding box corner (max X,
     * max Y, max Z).
     *
     * @return The maximum bounding box coordinate for this geometry element.
     */
    @Override
    public GeomPoint getBoundsMax() {
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

        int size = size();
        for (int i = 0; i < size; ++i) {
            GeomElement obj = (GeomElement) get(i);
            maxX = Math.max(maxX, obj.getMaxX());
            maxY = Math.max(maxY, obj.getMaxY());
            maxZ = Math.max(maxZ, obj.getMaxZ());
        }

        if (size() == 0) {
            maxX = 0;
            maxY = 0;
            maxZ = 0;
        }

        return GeomPoint.getInstance(maxX, maxY, maxZ);
    }

    /**
     * Translate this element by an incremental amount along each axis.
     *
     * @param dx The amount to translate in the X direction.
     * @param dy The amount to translate in the Y direction.
     * @param dz The amount to translate in the Z direction.
     */
    public void translate(double dx, double dy, double dz) {

        int size = size();
        for (int i = 0; i < size; ++i) {
            GeomElement obj = (GeomElement) get(i);
            obj.translate(dx, dy, dz);
        }
    }

    /**
     * Scale this element by the given scale factor in each axis direction.
     *
     * @param sx The amount to scale the element in the X direction.
     * @param sy The amount to scale the element in the Y direction.
     * @param sz The amount to scale the element in the Z direction.
     */
    public void scale(double sx, double sy, double sz) {

        int size = size();
        for (int i = 0; i < size; ++i) {
            GeomElement obj = (GeomElement) get(i);
            obj.scale(sx, sy, sz);
        }

    }

    /**
     * Store application specific data that can be assigned to this geometry element. This
     * can be used to store any application specific information that needs to be
     * associated with this geometry. If you make use of serialization, make sure any data
     * supplied is Serializable.
     *
     * @param key The key used to store and retrieve the application specific data
     * assigned to this geometry element.
     * @param data The application specific object to be assigned to this geometry
     * element.
     */
    @Override
    public void putData(Object key, Object data) {
        if (appData == null)
            appData = new HashMap();

        appData.put(key, data);
    }

    /**
     * Return the application specific object associated with the specified key that has
     * been assigned to this geometry element.
     *
     * @param key The key used to store and retrieve the application specific data.
     * @return The application specific object assigned to this geometry element. Returns
     * null if no object using the specified key has been assigned to this element.
     */
    @Override
    public Object getData(Object key) {
        Object data = null;
        if (appData != null)
            data = appData.get(key);

        return data;
    }

    /**
     * Returns a reference to a Map containing all application specific objects associated
     * with this geometry element. This allows a developer to easily manipulate
     * application specific data that is associated with this geometry element using the
     * Map interface.
     *
     * @return A reference to the Map object used to store application specific data along
     * with this geometry element. Returns null if no objects have been assigned to this
     * element.
     */
    @Override
    public Map getAllData() {
        return appData;
    }

    /**
     * Create a string representation of this geometry element by outputting the element's
     * name and it's bounding rectangle coordinates.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getName());
        buf.append("[");

        GeomPoint pnt = getBoundsMin();
        buf.append(pnt.toString());
        buf.append(",");
        GeomPoint.freeInstance(pnt);

        pnt = getBoundsMax();
        buf.append(pnt.toString());
        GeomPoint.freeInstance(pnt);

        buf.append("]");

        return buf.toString();
    }

    /**
     * Compares the specified object with this list for equality. Returns
     * <code>true</code> if and only if the specified object is also a GeomElementList and
     * both lists have the same contents.
     *
     * @param obj The object to be compared for equality with this list.
     * @return <code>true</code> if the specified object is equal to this this.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final GeomElementList other = (GeomElementList) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
            return false;
        if (this.subElements != other.subElements && (this.subElements == null || !this.subElements.equals(other.subElements)))
            return false;
        return true;
    }

    /**
     * Returns the hash code value for this geometry element.
     *
     * @return The hash code for this geometry element.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 31 * hash + (this.subElements != null ? this.subElements.hashCode() : 0);
        return hash;
    }

    /**
     * Make a copy of this geometry element object.
     *
     * @return A copy of this geometry element object.
     */
    @Override
    public Object clone() {
        Object result = null;

        try {
            // Make a shallow copy.
            result = super.clone();

            // Now make a deep copy of the data in this object.
            GeomElementList newObj = (GeomElementList) result;

            newObj.subElements = new ArrayList();
            int size = size();
            for (int i = 0; i < size; ++i) {
                GeomElement obj = (GeomElement) get(i);
                newObj.subElements.add(obj.clone());
            }

        } catch (Exception e) {
            // Shouldn't happen if this object implements Cloneable.
            System.err.println("Can not clone this object!");
            e.printStackTrace();
        }

        return result;
    }
}
