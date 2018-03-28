/*
 *   GeomReader  -- Interface in common to any objects that read or write panel geometry.
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

import java.io.*;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

/**
 * An abstract interface that provides a common interface for classes that read and write
 * panel geometry files of various formats.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: April 14, 2000
 * @version April 3, 2014
 */
public interface GeomReader {

    /**
     * An error message for unexpected end-of-file.  Append the offending line number
     * to the end of this string in the reported error.
     */
    public static final String EOFMSG = "Unexpected end of file encountered on line #";

    /**
     * Method provided by sub-classes that reads in a geometry file from the supplied
     * input stream and returns a properly formatted configuration geometry object that
     * contains the geometry from the file.
     *
     * @param inStream The input stream to geometry file that we are reading from.
     * @param name The name of the geometry being read in (null for no name).
     * @return A configuration geometry object containing the geometry read in from the file.
     */
    public GeomConfig read(InputStream inStream, String name) throws IOException;

    /**
     * Method provided by sub-classes that writes out a geometry file for the geometry
     * contained in the supplied configuration geometry object.
     *
     * @param filename The name of the geometry file to be written out.
     * @param theConfig The configuration geometry to be written out.
     */
    public void write(File filename, GeomConfig theConfig) throws IOException;

    /**
     * Method provided by sub-classes that specified the units used for the geometry as
     * stored in the disk file. The geometry is converted from these units to the
     * reference length units (meters) when it is read in and converted from reference
     * units to these units when written out. If the file format specifies a particular
     * unit, this will be ignored.
     *
     * @param units The units used for the geometry in the disk file. If null is passed,
     *  the units will default to the reference length units (meters).
     */
    public void setFileUnits(Unit<Length> units);
}
