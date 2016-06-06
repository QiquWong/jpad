/* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
   modeler, Finite element mesher, Plugin architecture.

    Copyright (C) 2006, by EADS CRC

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

/**
 * Iterator on sub-shapes.
 * @author Denis Barbier
 *
 */
public interface CADIterator
{
    /**
     * Initialize this explorer
     * @param shape The shape to explore
     */    
	public void initialize(CADShape shape);
    /**
     * Return true if there are more shapes to explore
     * @return true if there are more shapes to explore
     */    
	public boolean more();
    /**
     * Move on to the next shape in the exploration
     */    
	public void next();
    /**
     * Return the current shape in the exploration
     * @return the current shape in the exploration
     */    
	public CADShape value();
}
