/*
*   RootException -- Exception thrown when there is a problem finding the root of an equation.
*
*   Copyright (C) 1999 - 2012 by Joseph A. Huwaldt.
*   All rights reserved.
*   
*   This library is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Lesser General Public
*   License as published by the Free Software Foundation; either
*   version 2 of the License, or (at your option) any later version.
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
**/
package standaloneutils.mathtools;


/**
*  Root finding routines may throw this exception 
*  when an error occurs in the root finding routine.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  October 8, 1997
*  @version  September 16, 2012
**/
public class RootException extends Exception {

	/**
	*  Force users of this exception to supply a message by making
	*  the default constructor private.  A detail message is a
	*  String that describes this particular exception.
	**/
	protected RootException() {}


	/**
	*  Constructs a RootException with the specified detail message.
    *  A detail message is a String that describes this particular
    *  exception.
	*
	*  @param  msg  The String containing a detail message
	**/
	public RootException(String msg) {
		super(msg);
	}


	/**
	*  Returns a short description of the RootException.
	*
	*  @return  Returns this exceptions message as a string.
	**/
    @Override
    public String toString() {
		return getMessage();
    }


}


