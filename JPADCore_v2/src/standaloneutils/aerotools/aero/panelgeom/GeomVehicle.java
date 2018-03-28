/*
 *   GeomVehicle  -- A collection of components used to define an entire vehicle.
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

import java.util.Iterator;
import java.util.Collection;

/**
 * Defines a geometry element called a vehicle. A vehicle is a collection of components
 * that define an entire vehicle to be analyzed minus control surface deflections. The
 * geometry stored in the GeomVehicle object is always in reference length units (meters),
 * surface areas are always returned in reference area units (sq. meters), etc.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: April 14, 2000
 * @version April 3, 2014
 */
public class GeomVehicle extends GeomElementList {

    /**
     * Create an empty vehicle (one that does not contain any components).
     */
    public GeomVehicle() {
    }

    /**
     * Create an empty vehicle with the specified name.
     */
    public GeomVehicle(String name) {
        super(name);
    }

    /**
     * Create a vehicle made up of any components found in the specified collection. Any
     * objects that are not GeomComponent objects in the specified collection will be
     * ignored. If you pass a GeomVehicle object, all the components found in it will be
     * added to the new vehicle.
     *
     * @param name The name to be assigned to this vehicle.
     * @param components A collection that contains a set of components.
     */
    public GeomVehicle(String name, Collection components) {
        super(name);

        if (components instanceof GeomVehicle) {
            subElements.addAll(components);
        } else {
            for (Iterator i = components.iterator(); i.hasNext();) {
                Object obj = i.next();
                if (obj instanceof GeomComponent) {
                    subElements.add(obj);
                }
            }
        }
    }

    /**
     * Replaces the component at the specified position in this vehicle with the specified
     * component.
     *
     * @param index The index of the component to replace.
     * @param element The component to be stored a the specified position.
     * @return The component previously at the specified position in this vehicle.
     * @throws ClassCastException - if the specified element is not a GeomComponent type
     * object.
     */
    @Override
    public Object set(int index, Object element) {
        GeomComponent obj = (GeomComponent) element;
        return subElements.set(index, obj);
    }

    /**
     * Inserts the specified component at the specified position in this vehicle. Shifts
     * the component currently at that position (if any) and any subsequent components to
     * the right (adds one to their indices).
     *
     * @param index Index at which the specified component is to be inserted.
     * @param element Component to be inserted.
     * @throws ClassCastException - if the specified element is not a GeomComponent type
     * object.
     */
    @Override
    public void add(int index, Object element) {
        GeomComponent obj = (GeomComponent) element;
        subElements.add(index, obj);
    }

    /**
     * Return an enumeration of all the components in this vehicle.
     *
     * @return An iteration of all the components in this vehicle.
     */
    public Iterator components() {
        return this.iterator();
    }
}
