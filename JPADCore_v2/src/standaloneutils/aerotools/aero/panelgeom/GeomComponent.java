/*
*   GeomComponent  -- A collection of networks used to define a portion of a vehicle.
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
*  Defines a geometry element called a component.  A component
*  is an collection of networks that define a portion of the
*  surface of a vehicle.
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author Joseph A. Huwaldt    Date:  March 31, 2000
*  @version April 3, 2014
*/
public class GeomComponent extends GeomElementList {

	/**
	*  Create an empty component (one that does not contain any networks).
	*/
	public GeomComponent() {};
	
	/**
	*  Create an empty component with the specified name.
	*/
	public GeomComponent(String name) {
		super(name);
	}
	
	/**
	*  Create a component made up of any networks found in the
	*  specified collection.  Any objects that are not GeomNetwork
	*  objects in the specified collection will be ignored.  If
	*  you pass a GeomComponent object, all the networks found in
	*  it will be added to the new component.
	*
	*  @param  name      The name to be assigned to this component.
	*  @param  networks  A collection that contains a set of networks.
	*/
	public GeomComponent(String name, Collection networks) {
		super(name);
		
		if (networks instanceof GeomComponent) {
			subElements.addAll(networks);
		} else {
			for (Iterator i=networks.iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof GeomNetwork) {
					subElements.add(obj);
				}
			}
		}
	}
	
	
	/**
	*  Replaces the network at the specified position in this component
	*  with the specified network.
	*
	*  @param   index   The index of the network to replace.
	*  @param   element The network to be stored a the specified position.
	*  @return  The network previously at the specified position in this
	*           component.
	*  @throws  ClassCastException - if the specified element is not a
	*                                GeomNetwork type object.
	*/
    @Override
	public Object set(int index, Object element) {
		GeomNetwork obj = (GeomNetwork) element;
		return subElements.set(index, obj);
	}
	
	/**
	*  Inserts the specified network at the specified position in this
	*  component.  Shifts the network currently at that position (if
	*  any) and any subsequent networks to the right (adds one to their
	*  indices).
	*
	*  @param  index   Index at which the specified network is to be
	*                  inserted.
	*  @param  element Network to be inserted.
	*  @throws ClassCastException - if the specified element is not a
	*                               GeomNetwork type object.
	*/
    @Override
	public void add(int index, Object element) {
		GeomNetwork obj = (GeomNetwork) element;
		subElements.add(index, obj);
	}
	
	/**
	*  Return an enumeration of all the networks in this component.
	*
	*  @return An iteration of all the networks in this component.
	*/
	public Iterator networks() {
		return this.iterator();
	}
	
}


