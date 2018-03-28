/*
*   POIGeomReader  -- A class that can read a POI formatted geometry file.
*
*   Copyright (C) 2002-2004, Joseph A. Huwaldt
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;



/**
*  A class for reading vehicle geometry from a POINTS (POI)
*  formatted geometry file.  This is the geometry file format used
*  by A502 (PANAIR) and A633 (TRANAIR) for input geometry.
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author Joseph A. Huwaldt    Date:  April 14, 2000
*  @version August 14, 2002
*/
public class POIGeomReader implements GeomReader {

	// Some error messages.	
	private static final String PARSEPOI_MSG = "Unable to parse POI file at line #";

	private static final String INCROWSCOLS_MSG = "The number of rows or columsn in the POI file are unreasonable on line #";
	
	// The number of characters per number stored in the POI file.
	private static final int FIELDSIZE = 10;
	
	// The units used for the geometry in the specified file.
	private Unit<Length> units = Length.UNIT;


	/**
	*  <p>  Method that reads in a POINTS (POI) geometry file from the
	*       specified input stream and returns a configuration geometry
	*       object that contains the geometry from the POI file.
	*  </p>
	*  <p>  A POI file does not support multiple vehicles.  Therefore,
	*       the configuration returned by this method will always contain
	*       only a single vehicle.
	*  </p>
	*  <p>  Each component will have an Integer object associated with it
	*       under the key "A502A633TypeCode" that contains the A502-A633
	*       array type code for all the networks contained in that component.
	*  </p>
	*
	*  @param   inStream  The input stream to geometry file that we are reading from.
	*  @param   name      The name of the geometry being read in (null for no name).
	*  @return A configuration geometry object containing the geometry
	*           read in from the file.
	*/
    @Override
	public GeomConfig read(InputStream inStream, String name) throws IOException {
		GeomConfig config = null;

		// Create an empty vehicle with the filename as the vehicle name.
		GeomVehicle vehicle = new GeomVehicle(name);
		
		// Create a reader to access the ASCII file.
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(inStream));
		
		// Loop over all the components stored in the file.
		String aLine = reader.readLine();
		while(aLine != null) {
			
			// Skip ahead to the next "$POI" line.
			while (aLine != null && !aLine.startsWith("$POI"))
				aLine = reader.readLine();
			
			if (aLine != null) {
				// A $POI line was found, read in the component.
				GeomComponent comp = readComponent(reader);
				vehicle.add(comp);
			}
			
			// Begin searching for the next component.
			aLine = reader.readLine();
		}
		
		// If something was read in, create a configuration object for it.
		if (vehicle.size() != 0) {
			// Create a configuration with the filename for the config name.
			config = new GeomConfig(name);
			config.add(vehicle);
		}
		
		return config;
	}


	/**
	*  Method that writes out a POINTS (POI) formatted geometry file
	*  for the geometry contained in the supplied configuration geometry
	*  object.
	*
	*  WARNING! This method has not yet been implemented and will throw an
	*  exception if you try and use it.
	*
	*  @param  filename   The name of the POI geometry file to be written out.
	*  @param  theConfig  The configuration geometry to be written out.
	*/
    @Override
	public void write(File filename, GeomConfig theConfig)
						throws IOException {
		throw new IOException("Writing POI files has not yet been implemented.");
	}

	
	/**
	*  Method that specifies the units used for the geometry as stored
	*  in the POI file.  The geometry is converted from these units to
	*  the reference length units (meters) when it is read in and converted
	*  from reference units to these units when written out.  If the
	*  units are not specified, then it is assumed that the data has
	*  units of meters in the file.
	*
	*  @param  units   The units used for the geometry in the disk file.
	*                  If null is passed, the units will default to
	*                  the reference length units (meters).
	*/
    @Override
	public void setFileUnits(Unit<Length> units) {
        if (units == null)
            this.units = Length.UNIT;
		this.units = units;
	}


	/**
	*  Reads a single component made up of multiple networks of the same type
	*  from an input stream pointing to a POI file.
	*  This method assumes that the stream starts immediately after the
	*  $POI line in a POI file.
	*
	*  @param  in  Reader for the POI file we are reading (positioned so that
	*              the next read will occur on the line following the $POI line).
	*  @return The component read in from the file.
	*/
	private GeomComponent readComponent(LineNumberReader in) throws IOException {
		GeomComponent comp = null;
		
		try {
		
			// Read in the number of arrays.
			String aLine = in.readLine();
			if (aLine == null)
				throw new IOException(EOFMSG + in.getLineNumber() + ".");
			
			String subStr = aLine.substring(0, FIELDSIZE-1).trim();
			int numArrs = Integer.parseInt(subStr);
		
			// Read in the component type.
			aLine = in.readLine();
			if (aLine == null)
				throw new IOException(EOFMSG + in.getLineNumber() + ".");
			
			subStr = aLine.substring(0, FIELDSIZE-1).trim();
			Integer typeCode = new Integer(subStr);
			String typeStr = "Type " + typeCode;

			// Loop over each array and read it in, adding it to a new component.
			comp = new GeomComponent(typeStr);
			comp.putData("A502A633TypeCode", typeCode);
			for (int i=0; i < numArrs; ++i) {
				GeomNetwork net = readNetwork( in );
				comp.add(net);
			}
			
		} catch (NumberFormatException e) {
			throw new IOException(PARSEPOI_MSG + in.getLineNumber() + ".");
		}

		return comp;
	}


	/**
	*  Reads a single network from an input stream (pointing to a POI file).
	*  This method assumes that the stream starts immediately after the
	*  line containing the component type in a POI file (two lines after
	*  the $POI line).
	*  </p>
	*
	*  @param  in  Reader for the POI file we are reading (positioned so that
	*              the next read will occur on the line following the
	*              line identifying component type).
	*  @return The network read in from the file.
	*/
	public GeomNetwork readNetwork(LineNumberReader in) throws IOException {
		GeomNetwork net = null;
		UnitConverter unitCvtr = units.toStandardUnit();

		try {
			// Create a number format to parse our input data.
			DecimalFormat nf = (DecimalFormat)NumberFormat.getInstance();

			// Read in the number of rows and columns.
			int pos = 0;
			String aLine = in.readLine();
			if (aLine == null)
				throw new IOException(EOFMSG + in.getLineNumber() + ".");
			
			String subStr = aLine.substring(pos, pos + FIELDSIZE).trim();
			int numCols = nf.parse(subStr).intValue();
			pos += FIELDSIZE;
			subStr = aLine.substring(pos, pos + FIELDSIZE).trim();
			int numRows = nf.parse(subStr).intValue();

			// Do a sanity check.
			if (numRows < 0 || numRows > 1000000 || numCols < 0 ||
						numCols > 1000000 )
				throw new IOException(INCROWSCOLS_MSG + in.getLineNumber() + ".");
			
			// Read in the name of this network.
			pos += FIELDSIZE;
			String name = aLine.substring(pos, aLine.length()).trim();
			

			// Allocate memory for data arrays.
			double[][] xArr = new double[numRows][numCols];
			double[][] yArr = new double[numRows][numCols];
			double[][] zArr = new double[numRows][numCols];

			// Loop over each row.
			for (int i=0; i < numRows; ++i) {
				aLine = in.readLine();
				if (aLine == null)
					throw new IOException(EOFMSG + in.getLineNumber() + ".");
				
				pos = 0;
				int count = 0;

				// Loop over each column.
				for (int j=0; j < numCols; ++j) {

					// There are two points per line.
					if (count > 1) {
						aLine = in.readLine();
						if (aLine == null)
							throw new IOException(EOFMSG + in.getLineNumber() + ".");
						
						count = 0;
						pos = 0;
					}

					// Read in X coordinate.
					subStr = aLine.substring(pos, pos + FIELDSIZE).trim();
					double value = nf.parse(subStr).doubleValue();
					xArr[i][j] = unitCvtr.convert(value);

					// Read in Y coordinate.
					pos += FIELDSIZE;
					subStr = aLine.substring(pos, pos + FIELDSIZE).trim();
					value = nf.parse(subStr).doubleValue();
					yArr[i][j] = unitCvtr.convert(value);

					// Read in Z coordinate.
					pos += FIELDSIZE;
					subStr = aLine.substring(pos, pos + FIELDSIZE).trim();
					value = nf.parse(subStr).doubleValue();
					zArr[i][j] = unitCvtr.convert(value);
					pos += FIELDSIZE;

					++count;
				}
			}
			
			
			// Create a new network from the points just read in.
			net = new GeomNetwork(xArr, yArr, zArr, name);


		} catch (ParseException e) {
			throw new IOException(PARSEPOI_MSG + in.getLineNumber() + ".");

		} catch (NumberFormatException e) {
			throw new IOException(PARSEPOI_MSG + in.getLineNumber() + ".");
			
		}

		return net;
	}


}
