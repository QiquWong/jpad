/*
*   OTISTableReader -- A class that can read and write OTIS table (*.itd or *.dat) multi-table files.
*
*   Copyright (C) 2008-2011 by Joseph A. Huwaldt
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
*   Library General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program; if not, write to the Free Software
*  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*  Or visit:  http://www.gnu.org/licenses/lgpl.html
**/
package standaloneutils.aerotools.tools.tables;

import java.io.*;
import java.util.StringTokenizer;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.ParseException;


/**
*  A class that provides methods for reading
*  or writing an OTIS table (*.itd or *.dat) formatted multi-table file.
*  These files are used by the OTIS trajectory simulation system to
*  store aerodynamic and propulsion data among other things.  See
*  OTIS documentation for more information.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  January 10, 2008
*  @version   October 2, 2011
**/
public class OTISTableReader implements FTableReader {

	//  The preferred file extension for files of this reader's type.
	public static final String kExtension = "dat";

	//  A brief description of the format read by this reader.
	public static final String kDescription = "OTIS Table";

	//	The character used to indicate that a line is commented out.
	private static final String kComment = "*";
	
	private static final String kDelimiters = "\t ";
	
	private static final String kParseErrMsg = "Error parsing OTIS table file on line #";

	/**
	*  Returns a string representation of the object.  This will return a brief
	*  description of the format read by this reader.
	**/
	public String toString() {
		return kDescription;
	}
	
	/**
	*  Returns the preferred file extension (not including the ".") for files of
	*  this reader's type.
	**/
	public String getExtension() {
		return kExtension;
	}
	
	/**
	*  Method that determines if this reader can read data from the specified input stream.
	*  Will return FTableReader.YES if the stream is recognized as an OTIS table file.
	*
	*  @param   name   The name of the file.
	*  @param   input  An input stream containing the data to be read.
	*  @return FTableReader.NO if the file is not recognized at all or FTableReader.MAYBE if the
	*           filename extension is "*.itd" or "*.dat".
	**/
	public int canReadData(String name, InputStream input) throws IOException {
		int response = NO;
		
		name = name.toLowerCase().trim();
		if (name.endsWith(".dat") || name.endsWith(".itd"))
			response = MAYBE;
		
		return response;
	}
	
	/**
	*  Returns true.  This class can write data to an OTIS table file.
	**/
	public boolean canWriteData() {
		return false;
	}
	
	/**
	*  Method that reads in OTIS table formatted data
	*  from the specified input stream and returns that data as an
	*  FTableDatabase object.
	*
	*  @param   input  An input stream containing the OTIS table formatted data.
	*  @return An FTableDatabase object that contains the data read
	*           in from the specified stream.
	*  @throws IOException If there is a problem reading the specified stream.
	**/
	public FTableDatabase read(InputStream input) throws IOException {
		
		// Create a reader to translate the input stream.
		LineNumberReader reader = new LineNumberReader( new InputStreamReader( input ) );
		
		FTableDatabase db = readStream( reader );
		
		return db;
	}

	/**
	*  Method for writing out all the data stored in the specified
	*  list of FloatTable objects to the specified output stream in OTIS table format.
	*
	*  @param   output  The output stream to which the data is to be written.
	*  @param   tables  The list of FloatTable objects to be written out.
	*  @throws IOException If there is a problem writing to the specified stream.
	**/
	public void write( OutputStream output, FTableDatabase tables ) throws IOException {
		throw new IOException("Writing OTIS table files has not yet been implemented.");
	}

	/**
	*  Read in an OTIS table formatted multi-table stream and create
	*  a new table database with the new files.
	*
	*  @param  in  A file reader that reads in the characters from the file.
	*  @returns A table database containing the tables read in from the file.
	**/
	private static FTableDatabase readStream( LineNumberReader in ) throws IOException {
		
		// Create an empty table database.
		FTableDatabase db = new FTableDatabase();

		//	Read in the 1st line to see if there is any data.
		in.mark(10240);
		String aLine = readLineNoComments(in);
		if (aLine == null)	throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
		
		while (aLine != null) {
			in.reset();
			
			//	Right now this reads only a single table from the file!!
			FloatTable theTable = readSingleTable(in);
		
			//	Add this table to the table database.
			db.put(theTable);
			
			//	Is there more data?
			in.mark(10240);
			aLine = readLineNoComments(in);
			
		}
		
        return db;
	}


	/**
	*  Read a single table from an OTIS formatted table file.
	*
	*  @param  in  A file reader that reads in the characters from the file.
	*  @returns A table containing the data read in from the disk file.
	**/
	private static FloatTable readSingleTable( LineNumberReader in ) throws IOException {
		DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
		FloatTable theTable = null;
		
		try {
			//	Read in the table name.
			String aLine = readLineNoComments(in);
			if (aLine == null)	throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
			String tblName = aLine.trim();
			
			//	Read the table descriptor.
			aLine = readLineNoComments(in);
			if (aLine == null)	throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
			String descriptor = aLine.trim();
			
			//	Read in the scale factor.
			aLine = readLineNoComments(in);
			if (aLine == null)	throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
			float scaleF = nf.parse( aLine.trim() ).floatValue();
			
			//	Read in the number of terms.
			aLine = readLineNoComments(in);
			if (aLine == null)	throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
			int numTerms = nf.parse( aLine.trim() ).intValue();
			if (numTerms != 1) throw new IOException("Reading of multiple term tables not supported for table \"" + tblName + "\".");
			
			//	Skip in the number of term 1 coefficients.
			aLine = readLineNoComments(in);
			if (aLine == null)	throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
		
			//	Read in the number of independent variables in the table.
			aLine = readLineNoComments(in);
			if (aLine == null)	throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
			int numIndep = nf.parse( aLine.trim() ).intValue();
			if (numIndep < 1) throw new IOException("Must be at least 1 independent variable in table \"" + tblName + "\".");
			
			//	Loop over all the independent variables.
			String indepNames[] = new String[numIndep];
			int[] elemPerDim = new int [ numIndep ];
			float breakpoints[][] = new float[numIndep][];
			for (int i=0; i < numIndep; ++i) {
				
				//	Read in the name of the independent variable.
				aLine = readLineNoComments(in);
				if (aLine == null)	throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
				indepNames[i] = aLine.trim();
				
				//	Read in the number of breakpoints in this independent variable.
				aLine = readLineNoComments(in);
				if (aLine == null)	throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
				int numBP = nf.parse( aLine.trim() ).intValue();
				elemPerDim[i] = numBP;
				breakpoints[i] = new float[numBP];
				
				//	Loop over and read in all the breakpoints.
				int count = 0;
				while (count < numBP) {
					aLine = readLineNoComments(in);
					if (aLine == null)	throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
					
					StringTokenizer tokenizer = new StringTokenizer(aLine, kDelimiters);
					while (tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken().trim();
						float value = nf.parse( token ).floatValue();
						breakpoints[i][count] = value;
						++count;
					}
					
				}
				
			}	//	Next i
			
			
			//	Create an n-dimensional array to hold the dependent data.
			FloatArrayNDim depData = new FloatArrayNDim( elemPerDim );
			
			//	Recursively read in the dependent data.
			int[] indep = new int [ numIndep ];
			rReadData( numIndep - 1, in, nf, indep, depData, breakpoints, scaleF );
			
			// Create a new floatTable object using this information.
			theTable = new FloatTable( tblName, indepNames, breakpoints, depData );
			
			//	Add some notes to the table.
			theTable.addNote(descriptor);
		
		} catch( ParseException e) {
			throw new IOException( kParseErrMsg + in.getLineNumber() + "." );
		} catch( NumberFormatException e ) {
			throw new IOException( kParseErrMsg + in.getLineNumber() + "." );
		}

		return theTable;
	}

	/**
	*  Recursively read in arrays of dependent data from an OTIS table
	*  formatted stream into the parameter depData.
	*
	*  @param  dim  The dimension (independent) we are currently looping through.
	*               Should initially be set to zero.
	*  @param  in   A file reader that reads in the characters from the file.
	*  @param  nf       The number format used to parse inputs.
	*  @param  indep    An array of indexes into the data table (independents).
	*  @param  depData  The dependent data array we are reading into.
	*  @param  breakpoints  The array of breakpoint arrays.
	**/
	private static void rReadData( int dim, LineNumberReader in, NumberFormat nf, int[] indep, FloatArrayNDim depData,
								float[][] breakpoints, float scaleF ) throws IOException, NumberFormatException, ParseException {
		if ( dim == 0 ) {
			// Reached the lowest dimension, indep[] is now ready.
			// Read in an array.
			readDataArray( in, nf, indep, depData, breakpoints[dim].length, scaleF );
		
		} else {
			
			// Loop over all the breakpoints in this dimension.
			for ( int i = 0; i < breakpoints[dim].length; ++i ) {
				
				// Fill in the required value for indep[] at this dimension.
				indep[dim] = i;
				
				// Go on to the next lower dimension.
				rReadData( dim - 1, in, nf, indep, depData, breakpoints, scaleF );
			}
		}
	}


	/**
	*  Read a single array of dependent values from an OTIS table file stream.
	*  The data read in is stored in the parameter depData.
	*
	*  @param  in       A file reader that reads in the characters from the file.
	*  @param  nf       The number format used to parse inputs.
	*  @param  indep    An array of indexes into the data table (independents).
	*  @param  depData  The array of dependent values we are reading into.
	*  @param  length   The length of most rapidly varying breakpoint array
	*                   ( breakpoints[numDims-1].length).
	*  @param  scaleF   A scale factor used to pre-multiply all the data in the table.
	**/
	private static void readDataArray( LineNumberReader in, NumberFormat nf, int[] indep,
									FloatArrayNDim depData, int length, float scaleF ) throws IOException, NumberFormatException, ParseException {
		// Loop over all the breakpoints in the last indepenent (the array) and read in the array.
		int count = 0;
		while (count < length - 1) {
			String aLine = readLineNoComments(in);
			if (aLine == null)	throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
			
			StringTokenizer tokenizer = new StringTokenizer(aLine, kDelimiters);
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken().trim();
				float value = nf.parse( token ).floatValue();
				value *= scaleF;
				indep[0] = count;
				depData.set( indep, value );
				++count;
			}
			
		}

	}


	/**
	*  Reads a line of text from the supplied file reader stripping out any
	*  lines that are commented out.
	**/
	private static String readLineNoComments(LineNumberReader reader) throws IOException {
	
		String aLine = reader.readLine();
		while (aLine != null && aLine.startsWith(kComment))
			aLine = reader.readLine();
		
		return aLine;
	}

}


