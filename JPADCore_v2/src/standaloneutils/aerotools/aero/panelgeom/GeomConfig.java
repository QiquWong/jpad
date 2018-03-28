/*
 *   GeomConfig  -- A collection of vehicles used to define an entire configuration.
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
 * Defines a geometry element called a configuration. A configuration is a collection of
 * vehicles that define an entire configuration to be analyzed minus control surface
 * deflections. The geometry stored in the GeomConfig object is always in reference length
 * units (meters), surface areas are always returned in reference area units (sq. meters),
 * etc.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: May 3, 2000
 * @version April 3, 2014
 */
public class GeomConfig extends GeomElementList {

    /**
     * Create an empty configuration (one that does not contain any vehicles).
     */
    public GeomConfig() {
    }

    /**
     * Create an empty configuration with the specified name.
     */
    public GeomConfig(String name) {
        super(name);
    }

    /**
     * Create a configuration made up of any vehicles found in the specified collection.
     * Any objects that are not GeomVehicle objects in the specified collection will be
     * ignored. If you pass a GeomConfig object, all the vehicles found in it will be
     * added to the new configuration.
     *
     * @param name The name to be assigned to this configuration.
     * @param vehicles A collection that contains a set of vehicles.
     */
    public GeomConfig(String name, Collection vehicles) {
        super(name);

        if (vehicles instanceof GeomConfig) {
            subElements.addAll(vehicles);
        } else {
            for (Iterator i = vehicles.iterator(); i.hasNext();) {
                Object obj = i.next();
                if (obj instanceof GeomVehicle) {
                    subElements.add(obj);
                }
            }
        }
    }

    /**
     * Replaces the vehicle at the specified position in this configuration with the
     * specified vehicle.
     *
     * @param index The index of the vehicle to replace.
     * @param element The vehicle to be stored a the specified position.
     * @return The vehicle previously at the specified position in this configuration.
     * @throws ClassCastException - if the specified element is not a GeomVehicle type
     * object.
     */
    @Override
    public Object set(int index, Object element) {
        GeomVehicle obj = (GeomVehicle) element;
        return subElements.set(index, obj);
    }

    /**
     * Inserts the specified vehicle at the specified position in this configuration.
     * Shifts the vehicle currently at that position (if any) and any subsequent vehicles
     * to the right (adds one to their indices).
     *
     * @param index Index at which the specified vehicle is to be inserted.
     * @param element Vehicle to be inserted.
     * @throws ClassCastException - if the specified element is not a GeomVehicle type
     * object.
     */
    @Override
    public void add(int index, Object element) {
        GeomVehicle obj = (GeomVehicle) element;
        subElements.add(index, obj);
    }

    /**
     * Return an enumeration of all the vehicles in this configuration.
     *
     * @return An iteration of all the vehicles in this configuration.
     */
    public Iterator vehicles() {
        return this.iterator();
    }

}
