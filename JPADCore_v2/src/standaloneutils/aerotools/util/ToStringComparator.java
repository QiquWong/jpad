/*
* ToStringComparator	-- A class that compares two objects using each object's toString() result.
*
* Copyright (C) 1999-2011 by Joseph A. Huwaldt
* All rights reserved.
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2 of the License, or (at your option) any later version.
*   
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Library General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
* Or visit:  http://www.gnu.org/licenses/lgpl.html
*/
package standaloneutils.aerotools.util;

import java.util.Comparator;


/**
*  A comparator that lexicographically compares objects based on the value returned
*  by each object's "toString()" method.
*
* @version October 2, 2011
**/
public class ToStringComparator<T> implements Comparator<T> {

	/**
	*  Compares the strings returned from each Object's "toString()" method lexicographically
	*  using the String.compareTo() method.
	*
	*  @param  o1 - the first object to be compared.
	*  @param  o2 - the second object to be compared.
	**/
	public int compare(T o1, T o2) {
		String s1 = o1.toString();
		String s2 = o2.toString();
		return s1.compareTo(s2);
	}
	
}
