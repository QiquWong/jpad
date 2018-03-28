/*
*   IntegratorException -- Exception thrown when there is a problem integrating an equation.
*
*   Copyright (C) 2004-2012 by Joseph A. Huwaldt.
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
**/
package standaloneutils.mathtools;


/**
*  Integrator routines may throw this exception 
*  when an error occurs.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author   Joseph A. Huwaldt   Date:  October 27, 2004
*  @version  September 16, 2012
**/
public class IntegratorException extends Exception {

	/**
	*  Force users of this exception to supply a message by making
	*  the default constructor private.  A detail message is a
	*  String that describes this particular exception.
	**/
	protected IntegratorException() {}


	/**
	*  Constructs a IntegratorException with the specified detail message.
    *  A detail message is a String that describes this particular
    *  exception.
	*
	*  @param  msg  The String containing a detail message
	**/
	public IntegratorException(String msg) {
		super(msg);
	}


	/**
	*  Returns a short description of the IntegratorException.
	*
	*  @return  Returns this exceptions message as a string.
	**/
    @Override
    public String toString() {
		return getMessage();
    }


}


