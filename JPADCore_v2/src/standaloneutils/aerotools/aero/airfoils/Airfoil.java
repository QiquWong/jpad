/*
*   Airfoil -- The interface in common to all airfoil type objects.
*
*   Copyright (C) 2000-2010 by Joseph A. Huwaldt
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
package standaloneutils.aerotools.aero.airfoils;

import java.util.List;
import java.awt.geom.Point2D;


/**
*  Defines the interface in common to all airfoil type objects.
*
*  <p>  Modified by:  Joseph A. Huwaldt  </p>
*
*  @author  Joseph A. Huwaldt   Date:  October 8, 2000
*  @version May 13, 2010
**/
public interface Airfoil extends java.io.Serializable {

	/**
	*  Returns a list of points containing the abscissas (X coordinate) and
	*  ordinates (Y coordinate) of the points defining the upper surface of the airfoil.
	**/
	public List<Point2D> getUpper();
	
	/**
	*  Returns a list of points containing the abscissas (X coordinate) and
	*  ordinates (Y coordinate) of the points defining the lower surface of the airfoil.
	**/
	public List<Point2D> getLower();
	
	/**
	*  Returns a list of points containing the camber line of the airfoil.
	**/
	public List<Point2D> getCamber();
	
	/**
	*  Returns a list containing the slope (dy/dx) of the upper
	*  surface of the airfoil at each ordinate.
	**/
	public List<Double> getUpperYp();
	
	/**
	*  Returns a list containing the slope (dy/dx) of the lower
	*  surface of the airfoil at each ordinate.
	**/
	public List<Double> getLowerYp();
	
}

