/*
 *   GeomReaderFactory  -- Factory for creating the class that can read/write a particular geometry file.
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

import java.io.File;

/**
 * An class that provides a factory method for retrieving a specific class that can
 * read/write one of various panel geometry file formats.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: April 14, 2000
 * @version April 3, 2014
 */
public class GeomReaderFactory {

    /**
     * Type identifier for a POI ($POINTS) A502/A633 geometry input file.
     */
    public static final int POI_TYPE = 0;
    /**
     * Type identifier for a GGP format geometry file.
     */
    public static final int GGP_TYPE = 1;

    /**
     * Return a geometry reader capable of reading/writing a geometry file of the
     * specified type.
     *
     * @param type One of the type constants associated with this class.
     * @return A geometry reader for reading/writing the specified type. If the specified
     * type is unknown, null is returned.
     */
    public static GeomReader getInstance(int type) {
        GeomReader reader = null;

        switch (type) {
            case POI_TYPE:
                reader = new POIGeomReader();
                break;

            case GGP_TYPE:
                reader = new GGPGeomReader();
                break;
        }

        return reader;
    }

    /**
     * Return a geometry reader capable of reading a geometry file of the specified type.
     * The reader that is returned is determined by the file's extension as follows: "poi"
     * will return a POIGeomReader, "ggp" will return a GGPGeomReader, any other extension
     * will return null.
     *
     * @param theFile The file to be read in.
     * @return A geometry reader for reading/writing the specified file. If the specified
     *  type is unknown, null is returned.
     */
    public static GeomReader getInstance(File theFile) {
        GeomReader reader = null;

        String name = theFile.getName().toLowerCase();
        if (name.endsWith("poi")) {
            reader = new POIGeomReader();

        } else if (name.endsWith("ggp")) {
            reader = new GGPGeomReader();

        }

        return reader;
    }
}
