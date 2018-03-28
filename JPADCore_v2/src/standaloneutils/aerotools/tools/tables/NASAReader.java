/*
*   NASAReader -- A class that can read and write NASA 2,3 or 4 argument multi-table files.
*
*   Copyright (C) 2000-2004 by Joseph A. Huwaldt
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
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.DateFormat;


/**
*  A class that provides methods for reading
*  or writing a NASA 2, 3, or 4 argument multi-table file.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  April 3, 2000
*  @version   June 15, 2004
**/
public class NASAReader implements FTableReader {

	//  The preferred file extension for files of this reader's type.
	public static final String kExtension = "nasa";

	//  A brief description of the format read by this reader.
	public static final String kDescription = "NASA 2,3,4 Argument";

	private static final String kIncDimErrMsg = "NASA table files may only have between\n2 and 4 dimensions.  Problem table: ";

	private static final String kBPErrMsg = "NASA table files must have more than 1 breakpoint per dimension.\nProblem table: ";

	private static final String kParseErrMsg = "Error parsing NASA formatted file on line ";

	private static final  String kTenSpaces = "          ";


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
	*  Will return FTableReader.MAYBE if the file name has the extension "nasa".
	*
	*  @param   name   The name of the file.
	*  @param   input  An input stream containing the data to be read.
	*  @return FTableReader.NO if the file is not recognized at all or FTableReader.MAYBE if the
	*           filename extension is "*.nasa".
	**/
	public int canReadData(String name, InputStream input) throws IOException {
		name = name.toLowerCase().trim();
		
		int response = NO;
		if (name.endsWith(".nasa"))
			response = MAYBE;
		
		return response;
	}
	
	/**
	*  Returns true.  This class can write data to a NASA multi-table formatted file.
	**/
	public boolean canWriteData() {
		return true;
	}
	
	/**
	*  Method that reads in NASA 2,3, or 4 argument table formatted data
	*  from the specified input stream and returns that data as an
	*  FTableDatabase object.
	*
	*  @param   input  An input stream containing the NASA formatted data.
	*  @return An FTableDatabase object that contains the data read
	*           in from the specified stream.
	*  @throws IOException If there is a problem reading the specified stream.
	**/
	public FTableDatabase read(InputStream input) throws IOException {
		
		// Get a reference to the file and create a stream reader.
		LineNumberReader reader = new LineNumberReader( new InputStreamReader( input ) );
		
		// Create an empty table database.
		FTableDatabase db = new FTableDatabase();
		
		// Read in the comment lines (if there are any) and add them
		// as database notes to the table database.
		readComments( reader, db );
		
		// Read in the tables in the file, one after the other.
		String aLine = null;
		do {
			FloatTable table = readNASATable( reader );
			db.put( table );
			
			// Is there more data?
			reader.mark( 8192 );
			aLine = reader.readLine();
			reader.reset();
			
		} while ( aLine != null );
		
		return db;
	}

	/**
	*  Method for writing out all the data stored in the specified
	*  list of FloatTable objects to the specified output stream in
	*  NASA 2,3, or 4 argument multi-table format.
	*
	*  @param   output  The output stream to which the data is to be written.
	*  @param   tables  The list of FloatTable objects to be written out.
	*  @throws IOException If there is a problem writing to the specified stream.
	**/
	public void write( OutputStream output, FTableDatabase tables ) throws IOException {
		
		// Check for consistant tables.
		checkTables( tables );
		
		// Get a reference to the output file.
		BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( output ) );
		
		// Write out the tables.
		writeNASAFile( writer, tables );
		
		//  Flush the output.
		writer.flush();
	}

	/**
	*  Read in a single NASA 2, 3 or 4 argument table and return the
	*  result.
	*
	*  @param   in  Reader for the file being read in.
	*  @returns A table containing the data read in from the file.
	**/
	private static FloatTable readNASATable( LineNumberReader in ) throws IOException {
		boolean finished = false;
		FloatTable table = null;
		try {
			// Skip over possible comments.
			String line0 = null;
			String line1 = in.readLine();
			String line2 = in.readLine();
			String line3 = in.readLine();
			while ( line3 != null && ! line3.trim().startsWith( "&" ) ) {
				line0 = line1;
				line1 = line2;
				line2 = line3;
				line3 = in.readLine();
			}
			if ( line3 == null )
				throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
			
			// Line 0 may contain the names of the independent variables.
			// Try to extract them if they are there.
			String[] indepNames = null;
			if ( line0 != null ) {
				
				// Independent names are surrounded by parentheses.
				int pos1 = line0.indexOf( "(" );
				int posn = line0.indexOf( ")" );
				if ( pos1 >= 0 && posn > pos1 ) {
					List list = new ArrayList();
					String trimmedLine = line0.substring( pos1 + 1, posn );
					pos1 = 0;
					posn = trimmedLine.length();
					
					// Independent names in the list are separated by commas.
					while ( pos1 < posn ) {
						int pos2 = trimmedLine.indexOf( ",", pos1 );
						if ( pos2 < 0 )
							pos2 = posn;
						
						String subStr = trimmedLine.substring( pos1, pos2 );
						list.add( subStr.trim() );
						pos1 = pos2 + 1;
					}
					
					// Create an array of independent names.
					int size = list.size();
					indepNames = new String [ size ];
					for ( int i = 0; i < size; ++i )
						indepNames[i] = (String) list.get( i );
				}
			}
			
			// Line 1 contains the table name (or number, but we'll use it as a name).
			String tblName = line1.trim();
			
			// Line 2 has the total number of elements in the table (which we ignore).
			
			// Read in 1st independent variable breakpoints.
			List indepVect = new ArrayList();
			DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
			float[] bp1 = null;
			String aLine = in.readLine();
			if ( aLine == null )
				throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
			
			int pos = 10;
			String subStr = aLine.substring( pos, pos + 9 ).trim();
			int numBP1 = (int) nf.parse( subStr ).floatValue();
			if ( numBP1 > 1 ) {
				bp1 = new float [ numBP1 ];
				indepVect.add( bp1 );
				pos = readNASA_BP( in, aLine, bp1, pos, nf );
			}
			
			// Read in the 2nd set of breakpoints.
			aLine = in.readLine();
			if ( aLine == null )
				throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
			
			pos = 10;
			subStr = aLine.substring( pos, pos + 9 ).trim();
			numBP1 = (int) nf.parse( subStr ).floatValue();
			if ( numBP1 > 1 ) {
				bp1 = new float [ numBP1 ];
				indepVect.add( bp1 );
				pos = readNASA_BP( in, aLine, bp1, pos, nf );
			}
			
			// Read in the 3rd & 4th set of breakpoints.
			// Mark this spot so we can return to it when we read in the dependent data.
			in.mark( 8192 );
			aLine = in.readLine();
			if ( aLine == null )
				throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
			
			pos = 10;
			subStr = aLine.substring( pos, pos + 9 ).trim();
			numBP1 = (int) nf.parse( subStr ).floatValue();
			pos += 10;
			subStr = aLine.substring( pos, pos + 9 ).trim();
			int numBP2 = (int) nf.parse( subStr ).floatValue();
			if ( numBP1 < 2 || numBP2 < 2 )
				throw new IOException( kInvNumBPErrMsg + in.getLineNumber() + "." );
			
			// Read in the 3rd & 4th breakpoint values and return the number
			// of lines that they take up.
			bp1 = new float [ numBP1 ];
			float[] bp2 = new float [ numBP2 ];
			indepVect.add( bp1 );
			indepVect.add( bp2 );
			int numBPLines = readNASA_BP2( in, aLine, bp1, bp2, pos, nf );
			
			// Create an array of breakpoint arrays.
			int numDims = indepVect.size();
			boolean haveNames = true;
			if ( indepNames == null || indepNames.length != numDims ) {
				indepNames = new String [ numDims ];
				haveNames = false;
			}
			float[][] breakpoints = new float [ numDims ][];
			for ( int i = 0; i < numDims; ++i ) {
				float[] bpArr = (float[]) indepVect.get( i );
				breakpoints[i] = bpArr;
				if ( haveNames == false )
					indepNames[i] = "Indep" + (i + 1);
			}
			indepVect = null;
			
			// Create an empty table with the breakpoints just read in.
			table = new FloatTable( tblName, indepNames, breakpoints, null );
			
			// Begin reading in the dependent data.
			in.reset();
			int[] indep = new int [ numDims ];
			rReadNASA( 0, in, numBPLines, indep, table, nf );
			
			// Read in &END line.
			aLine = in.readLine();
			
		} catch( ParseException e ) {
			throw new IOException( kParseErrMsg + in.getLineNumber() + "." );
		} catch( NumberFormatException e ) {
			throw new IOException( kParseErrMsg + in.getLineNumber() + "." );
		}
		
		return table;
	}

	/**
	*  Recursively read in arrays of dependent data from a NASA 2,3, or 4
	*  argument table file into the parameter depData.
	*
	*  @param  dim  The dimension (independent) we are currently looping through.
	*               Should initially be set to 0.
	*  @param  in  Reader for our table file stream.
	*  @param  numBPLines The number of lines taken up by the repeated breakpoints.
	*  @param  indep    An array of indexes into the data table (independents).
	*  @param  table    The table we are reading into.
	*  @param  nf       The number format object used to parse the numbers read in.
	**/
	private static void rReadNASA( int dim, LineNumberReader in, int numBPLines,
								int[] indep, FloatTable table, NumberFormat nf )
														throws IOException, ParseException {
		int numDims = indep.length;
		if ( dim >= numDims - 2 ) {
			// Reached the highest two dimensions, indep[] is now ready.
			// Read in the data.
			readNASADataArray( in, indep, table, numBPLines, nf );
		
		} else {
			
			// Loop over all the breakpoints in this dimension.
			int length = table.getNumBreakpoints( dim );
			for ( int i = 0; i < length; ++i ) {
				
				// Fill in the required value for indep[] at this dimension.
				indep[dim] = i;
				
				// Go on to the next higher dimension.
				rReadNASA( dim + 1, in, numBPLines, indep, table, nf );
			}
		}
	}

	/**
	*  Read in the dependent data associated with the last two breakpoints
	*  in a NASA 2,3, or 4 argument table file.
	*
	*  @param  in       Reader for the input stream.
	*  @param  indep    An array of indexes into the data table (independents).
	*  @param  table    The table we are reading into.
	*  @param  numBPLines The number of lines taken up by the repeated breakpoints.
	*  @param  nf       The number format object used to parse the numbers read in.
	**/
	private static void readNASADataArray( LineNumberReader in, int[] indep,
									FloatTable table, int numBPLines, NumberFormat nf )
														throws IOException, ParseException {
		// Skip over the repeated breakpoint lines.
		for ( int i = 0; i < numBPLines; ++i )
			in.readLine();
		
		// Read in the 1st line.
		String aLine = in.readLine();
		if ( aLine == null )
			throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
		
		int pos = 0;
		int count = 0;
		int numDims = indep.length;
		int length1 = table.getNumBreakpoints( numDims - 2 );
		int length2 = table.getNumBreakpoints( numDims - 1 );
		
		// Loop over all the breakpoints in the last 2 indepenents
		// and read them in.
		for ( int i = 0; i < length1; ++i ) {
			indep[numDims - 2] = i;
			for ( int j = 0; j < length2; ++j ) {
				indep[numDims - 1] = j;
				if ( count == 6 || pos + 10 >= aLine.length() ) {
					aLine = in.readLine();
					if ( aLine == null )
						throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
					pos = 0;
					count = 0;
				}
				pos += 10;
				++count;
				String subStr = aLine.substring( pos, pos + 9 ).trim();
				float value = nf.parse( subStr ).floatValue();
				table.set( indep, value );
			}
		}
	}

	/**
	*  Read in a set of breakpoints for independent variables 3 & 4 (if
	*  they have any breakpoints) in a NASA 2,3 or 4 argument table file.
	*
	*  @param  in       Reader for the input file stream.
	*  @param  aLine    The current line being processed from the input file.
	*  @param  bpArr    The array to contain the breakpoint values.
	*  @param  pos      The current position on the line "aLine".
	*  @param  nf       A number format for parsing the values as they are read in.
	**/
	private static int readNASA_BP( LineNumberReader in, String aLine, float[] bpArr,
								int pos, NumberFormat nf ) throws IOException, ParseException {
		int numBP = bpArr.length;
		int count = pos / 10;
		for ( int i = 0; i < numBP; ++i ) {
			if ( count == 6 ) {
				aLine = in.readLine();
				if ( aLine == null )
					throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
				
				pos = 0;
				count = 0;
			}
			pos += 10;
			++count;
			String subStr = aLine.substring( pos, pos + 9 ).trim();
			float BP = nf.parse( subStr ).floatValue();
			bpArr[i] = BP;
		}
		
		return pos;
	}

	/**
	*  Read in a set of breakpoints for independent variables 1 & 2
	*  in a NASA 2,3 or 4 argument table file.
	*
	*  @param  in       Reader for the input file stream.
	*  @param  aLine    The current line being processed from the input file.
	*  @param  bpArr2   The array to contain the breakpoint values for indep #2.
	*  @param  bpArr1   The array to contain the breakpoint values for indep #1.
	*  @param  pos      The current position on the line "aLine".
	*  @param  nf       A number format for parsing the values as they are read in.
	**/
	private static int readNASA_BP2( LineNumberReader in, String aLine,
								float[] bpArr2, float[] bpArr1, int pos, NumberFormat nf )
										throws IOException, ParseException {
		int numBP2 = bpArr2.length;
		int numBP1 = bpArr1.length;
		int count = pos / 10;
		int numLines = 1;
		for ( int i = 0; i < numBP2; ++i ) {
			if ( count == 6 ) {
				aLine = in.readLine();
				if ( aLine == null )
					throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
				
				pos = 0;
				count = 0;
				++numLines;
			}
			pos += 10;
			++count;
			String subStr = aLine.substring( pos, pos + 9 ).trim();
			float BP = nf.parse( subStr ).floatValue();
			bpArr2[i] = BP;
		}
		
		for ( int i = 0; i < numBP1; ++i ) {
			if ( count == 6 ) {
				aLine = in.readLine();
				if ( aLine == null )
					throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
				
				pos = 0;
				count = 0;
				++numLines;
			}
			pos += 10;
			++count;
			String subStr = aLine.substring( pos, pos + 9 ).trim();
			float BP = nf.parse( subStr ).floatValue();
			bpArr1[i] = BP;
		}
		
		return numLines;
	}

	/**
	*  Read in the comment lines from a NASA 2, 3, or 4 argument
	*  table file and add them as notes to the specified table
	*  database.
	*
	*  @param   tdb  The table database to add the comments to.
	**/
	private static void readComments( LineNumberReader in, FTableDatabase tdb ) throws IOException {
		if ( tdb != null && in != null ) {
			in.mark( 8192 );
			
			// Read in lines until we encounter an "&" character.
			String line1 = in.readLine();
			String line2 = in.readLine();
			String line3 = in.readLine();
			while ( line3 != null && !line3.startsWith( " &" ) && !line3.startsWith( "&" ) ) {
				
				// Since line1 wasn't part of the NASA table structure,
				// it must be a comment.
				tdb.addNote( line1.trim() );
				line1 = line2;
				line2 = line3;
				line3 = in.readLine();
			}
			if ( line3 == null )
				throw new IOException( kEOFErrMsg + in.getLineNumber() + "." );
			
			in.reset();
		}
	}

	/**
	*  Check the tables that are to be written out to make sure that
	*  they have the required number of independent variables.  NASA
	*  tables must have between 2 and 4 independent variables.  Also,
	*  each dimension must have at least 2 breakpoint values.
	*
	*  @param  tables  A collection of tables that we are writing out.
	*  @throws IOException if the tables do not have the required number
	*                      of independent variables.
	**/
	private static void checkTables( FTableDatabase tables ) throws IOException {
		// Loop over all the tables in this collection and check each.
		for ( Iterator i=tables.iterator(); i.hasNext(); ) {
			FloatTable aTable = (FloatTable)i.next();
			int dim = aTable.dimensions();
			if ( dim < 2 || dim > 4 )
				throw new IOException( kIncDimErrMsg + aTable.getTableName() );
			
			// Each dimension must have more than one breakpoint.
			for ( int j = 0; j < dim; ++j ) {
				int numBP = aTable.getNumBreakpoints( j );
				if ( numBP < 2 )
					throw new IOException( kBPErrMsg + aTable.getTableName() );
			}
		}
	}

	/**
	*  Write out a collection of FloatTable objects as a NASA 2, 3,
	*  or 4 argument multi-table file.
	*
	*  @param  output  Reference to a file to write the tables to.
	*  @param  tables  Collection of tables to be written to the file.
	**/
	private static void writeNASAFile( BufferedWriter output, FTableDatabase tables )
															throws IOException {
		DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
		nf.setMinimumIntegerDigits( 0 );
		nf.setGroupingUsed(false);
		
		// Write out the database notes as table file comments (if there are any).
		writeFileComments( output, tables );
		
		// Loop over all the tables writing each one out in turn.
		int pos = 0;
		for ( Iterator i=tables.iterator(); i.hasNext(); ++pos) {
			FloatTable theTable = (FloatTable)i.next();
			
			// Write out comment lines (1st already contains file comments).
			if ( pos != 0 ) {
				output.newLine();
				output.newLine();
			}
			
			// Write out independent variables.
			output.write( theTable.toString() );
			output.newLine();
			
			// Write out the table name.
			output.write( theTable.getTableName() );
			output.newLine();
			
			// Calculate and write out the number of data elements.
			String buffer = " " + calcNumElements( theTable );
			output.write( buffer );
			output.newLine();
			
			// Write out NASA card.
			output.write( " &TABLE DNASA=" );
			output.newLine();
			
			// Write out fillers for non-existant independent variables.
			int numDim = theTable.dimensions();
			switch (numDim)
			{
			case 2 :
				output.write( "              1.000,    0.000," );
				output.newLine();
			
			case 3 :
				output.write( "              1.000,    0.000," );
				output.newLine();
			
			default :
				break;
			}
			
			// Set up the number format for writing out breakpoints.
			nf.setMaximumFractionDigits( 3 );
			nf.setMinimumFractionDigits( 3 );
			
			// Write out breakpoints for dimensions > 2.
			int dim = 0;
			while ( dim < numDim - 2 ) {
				// Get the breakpoints for this dimension.
				float[] breakpoints = theTable.getBreakpoints( dim );
				int numBP = breakpoints.length;
				
				output.write( kTenSpaces );
				
				// Write out the number of breakpoints in this dimension.
				output.write( formatNumber( 10, nf, numBP ) );
				
				// Write out each breakpoint value.
				int count = 1;
				for ( int j = 0; j < numBP; ++j ) {
					if ( count == 6 ) {
						output.newLine();
						output.write( kTenSpaces );
						count = 0;
					}
					output.write( formatNumber( 10, nf, breakpoints[j] ) );
					++count;
				}
				output.newLine();
				++dim;
			}
			
			// Recursively write out all the arrays of dependent data.
			int[] indep = new int [ numDim ];
			rWriteNASA( 0, output, indep, theTable, nf );
			
			// Write out NASA END card.
			output.write( " &END" );
			output.newLine();
		}
	}

	/**
	*  Calculate the total number of elements in a NASA table including
	*  repeated and non-repeated breakpoints.
	*
	*  @param   table  The table we are writing out.
	*  @returns The total number of elements in the NADA table array
	*           (everything between the starting "&TABLE" and "&END").
	**/
	private static int calcNumElements( FloatTable table ) {
	
		int nElements = 1;
		int numDim = table.dimensions();
		
		// Calculate the total number of dependent data points.
		for ( int i = 0; i < numDim; ++i ) {
			int num = table.getNumBreakpoints( i );
			nElements *= num;
		}
		
		
		int numBP1 = table.getNumBreakpoints( numDim - 2 );
		int numBP2 = table.getNumBreakpoints( numDim - 1 );
		
		// Add in the number of 3rd and 4th independent breakpoints (which arn't
		// repeated).
		int count = 1;
		for ( int i = 0; i < numDim - 2; ++i ) {
			int num = table.getNumBreakpoints( i );
			nElements += num + 1;
			count *= num;
		}
		
		// Add in the 1st & 2nd breakpoint lines (which are repeated).
		int num1p2 = numBP1 + numBP2 + 2;
		nElements += num1p2*count;
		
		// Finally, add in the 1,0 elements for missing dimensions.
		while ( numDim < 4 ) {
			nElements += 2;
			++numDim;
		}
		
		return nElements;
	}

	/**
	*  Recursively write out arrays of dependent data for a NASA 2,3, or 4
	*  argument table file into the specified stream.
	*
	*  @param  dim  The dimension (independent) we are currently looping through.
	*               Should initially be set to 0.
	*  @param  out  Writer for our table file stream.
	*  @param  indep    An array of indexes into the data table (independents).
	*  @param  table    The table we are writing out.
	*  @param  nf       The number format object used to format the numbers written out.
	**/
	private static void rWriteNASA( int dim, BufferedWriter out, int[] indep,
									FloatTable table, NumberFormat nf ) throws IOException {
		int numDims = indep.length;
		if ( dim >= numDims - 2 ) {
			// Reached the highest 2 dimensions, indep[] is now ready.
			// Write out the data array.
			writeNASADataArray( out, indep, table, nf );
		
		} else {
			
			// Loop over all the breakpoints in this dimension.
			int length = table.getNumBreakpoints( dim );
			for ( int i = 0; i < length; ++i ) {
				// Fill in the required value for indep[] at this dimension.
				indep[dim] = i;
				
				// Go on to the next higher dimension.
				rWriteNASA( dim + 1, out, indep, table, nf );
			}
		}
	}

	/**
	*  Write out the dependent data associated with the last two breakpoints
	*  in a NASA 2,3, or 4 argument table file.
	*
	*  @param  out      Writer for the output stream.
	*  @param  indep    An array of indexes into the data table (independents).
	*  @param  table    The table we are writing out to.
	*  @param  nf       The number format object used to format the numbers written out.
	**/
	private static void writeNASADataArray( BufferedWriter out, int[] indep,
									FloatTable table, NumberFormat nf ) throws IOException {
		// Write out the repeated breakpoint lines.
		writeBPLines( out, table, nf );
		
		// Setup the number format for formatting data array numbers.
		nf.setMaximumFractionDigits( 5 );
		nf.setMinimumFractionDigits( 5 );
		int count = 0;
		int numDims = indep.length;
		int length1 = table.getNumBreakpoints( numDims - 2 );
		int length2 = table.getNumBreakpoints( numDims - 1 );
		
		// Loop over all the breakpoints in the last 2 indepenents
		// and write the dependent data out.
		out.write( kTenSpaces );
		for ( int i = 0; i < length1; ++i ) {
			indep[numDims - 2] = i;
			for ( int j = 0; j < length2; ++j ) {
				indep[numDims - 1] = j;
				if ( count == 6 ) {
					out.newLine();
					out.write( kTenSpaces );
					count = 0;
				}
				++count;
				out.write( formatNumber( 10, nf, table.get( indep ) ) );
			}
			out.newLine();
			if ( i < length1 - 1 )
				out.write( kTenSpaces );
			count = 0;
		}
	}

	/**
	*  Write out the repeated breakpoint lines for each data array.
	*
	*  @param  out   Writer used to write to the output stream.
	*  @param  table The table being written out.
	*  @param  nf    The number format used to format numbers for writing out.
	**/
	private static void writeBPLines( BufferedWriter out, FloatTable table, NumberFormat nf )
											throws IOException {
		// Set up the number format for breakpoints.
		nf.setMaximumFractionDigits( 3 );
		nf.setMinimumFractionDigits( 3 );
		int numDim = table.dimensions();
		
		// Extract the breakpoints for the last 2 dimensions.
		float[] bp2 = table.getBreakpoints( numDim - 1 );
		int numBP2 = bp2.length;
		float[] bp1 = table.getBreakpoints( numDim - 2 );
		int numBP1 = bp1.length;
		
		// Write out the number of breakpoints.
		out.write( kTenSpaces );
		out.write( formatNumber( 10, nf, numBP1 ) );
		out.write( formatNumber( 10, nf, numBP2 ) );
		
		// Loop over the breakpoint arrays and write them out.
		int count = 2;
		for ( int i = 0; i < numBP1; ++i ) {
			if ( count == 6 ) {
				out.newLine();
				out.write( kTenSpaces );
				count = 0;
			}
			++count;
			out.write( formatNumber( 10, nf, bp1[i] ) );
		}
		for ( int i = 0; i < numBP2; ++i ) {
			if ( count == 6 ) {
				out.newLine();
				out.write( kTenSpaces );
				count = 0;
			}
			++count;
			out.write( formatNumber( 10, nf, bp2[i] ) );
		}
		out.newLine();
	}

	/**
	*  Format a number for output to the NASA table file.
	*  Numbers in the NASA table file are of a fixed size and include
	*  a comma separator.  This method formats the given number using
	*  the supplied NumberFormat object.  It then adds a comma to the
	*  end of the formatted number and finally adds spaces to the start
	*  of the formatted number until it reaches the specified length.
	*
	*  @param  size   The overall size of the formatted number including
	*                 the separating comma and decimal point.
	*  @param  nf     The NumberFormat to use when formatting this number.
	*  @param  number The number to be formatted.
	**/
	private static String formatNumber( int size, NumberFormat nf, float number ) {
		StringBuffer buffer = new StringBuffer( nf.format( number ) );
		buffer.append( "," );
		TRUtils.addSpaces( size, buffer );
		return buffer.toString();
	}

	/**
	*  Write out any database notes as table comments to the NASA file.
	*  A NASA table file may have up to 3 lines of comments per table,
	*  but one line is, by convention, reserved for a listing of the
	*  table's independent variables.
	*
	*  @param  output  A writer used to output the data.
	*  @param  tables  The table database we are writing out.
	**/
	private static void writeFileComments( BufferedWriter output, FTableDatabase tables )
															throws IOException {
		int numNotes = tables.numberOfNotes();
		if ( numNotes == 0 ) {
			
			// Write out some generic table comments into the header lines.
			output.write( tables.toString() );
			output.newLine();
			output.write( "Date:, " );
			Date theDate = new Date();
			output.write( DateFormat.getDateInstance( DateFormat.SHORT ).format( theDate ) );
			output.write( ", " );
			output.write( DateFormat.getTimeInstance().format( theDate ) );
			output.newLine();
		
		} else {
			// Write out the table notes as the header of the file.
			if ( numNotes > 2 )
				numNotes = 2;
			
			for ( int i = 0; i < numNotes; ++i ) {
				String note = tables.getNote( i );
				output.write( note );
				output.newLine();
			}
			
			if ( numNotes == 1 ) {
				Date theDate = new Date();
				output.write( DateFormat.getDateInstance( DateFormat.SHORT ).format( theDate ) );
				output.write( ", " );
				output.write( DateFormat.getTimeInstance().format( theDate ) );
				output.newLine();
			}
		}
	}


}


