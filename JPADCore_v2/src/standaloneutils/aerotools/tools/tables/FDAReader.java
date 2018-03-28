/*
*   FDAReader	-- A class that can read and write FDA formatted multi-table files.
*
*   Copyright (C) 2000-2011 by Joseph A. Huwaldt
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
import java.util.ArrayList;
import java.util.Date;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DateFormat;


/**
*  A class that provides a method for reading or writing
*  an FDA (Functional Data Handling system ASCII) multi-table file.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  April 3, 2000
*  @version   October 2, 2011
**/
public class FDAReader implements FTableReader {

	//  The preferred file extension for files of this reader's type.
	public static final String kExtension = "fda";

	//  A brief description of the format read by this reader.
	public static final String kDescription = "FDHS ASCII";

	/**
	*  The number of digits used when formatting a number for writing
	*  out to the FDA format file.
	**/
	private static final  int kNumDigits = 14;
	private static final  int kNumFracDigits = 5;


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
	*  Will return FTableReader.MAYBE if the file name has the extension "fda".  Will return
	*  FTableReader.YES if, and only if, the stream contains "*FDHS" on the 1st line.
	*
	*  @param   name   The name of the file.
	*  @param   input  An input stream containing the data to be read.
	*  @return FTableReader.NO if the file is not recognized at all or FTableReader.MAYBE if the
	*           filename extension is "*.fda".  Will return FTableReader.YES if the 1st line
	*           of the file is "*FDHS".
	**/
	public int canReadData(String name, InputStream input) throws IOException {
		name = name.toLowerCase().trim();
		
		int response = NO;
		if (name.endsWith(".fda") || name.endsWith(".fdhs"))
			response = MAYBE;
		
		//	Convert the stream into a character reader.
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(input));
	
		//	If the file starts with *FDHS, then we've got the right type.
		String line = lnr.readLine();
		if (line != null && line.trim().equals("*FDHS"))
			response = YES;
		
		return response;
	}
	
	/**
	*  Returns true.  This class can write data to a CSV formatted file.
	**/
	public boolean canWriteData() {
		return true;
	}
	
	/**
	*  Method that reads in Functional Data Handling ASCII system (FDA)
	*  formatted data from the specified input stream and returns that data as an
	*  FTableDatabase object.
	*
	*  @param   input  An input stream containing the FDA formatted data.
	*  @return An FTableDatabase object that contains the data read
	*           in from the specified stream.
	*  @throws IOException If there is a problem reading the specified stream.
	**/
	public FTableDatabase read(InputStream input) throws IOException {
		
		// Create a tokenizer that can parse the input stream.
		BufferedReader reader = new BufferedReader( new InputStreamReader( input ) );
		StreamTokenizer tokenizer = new StreamTokenizer( reader );
		
		FTableDatabase db = readFDAStream( tokenizer );
		
		return db;
	}

	/**
	*  Method for writing out all the data stored in the specified
	*  list of FloatTable objects to the specified output stream in FDA format.
	*
	*  @param   output  The output stream to which the data is to be written.
	*  @param   tables  The list of FloatTable objects to be written out.
	*  @throws IOException If there is a problem writing to the specified stream.
	**/
	public void write( OutputStream output, FTableDatabase tables ) throws IOException {
	
		// Get a reference to the output file.
		BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( output ) );
		
		// Identify this as an FDHS formatted file.
		writer.write( "*FDHS" );
		writer.newLine();
		
		// Write out the file level notes if there are any.
		writeFileComments( writer, tables );
		
		// Loop over all the tables in the collection and write them out
		// to the same file.
		for (FloatTable table : tables ) {
			writeFDATable( writer, table );
		}
		
		//  Flush the output.
		writer.flush();
	}

	/**
	*  Read in an FDA formatted multi-table stream and create
	*  a new table database with the new files.
	*
	*  @param   tokenizer  Tokenizer for the file being read in.
	*  @return A table database containing the tables read in from the file.
	**/
	private static FTableDatabase readFDAStream( StreamTokenizer tokenizer ) throws IOException {
		
		// Set some tokenizer options.
		tokenizer.wordChars('!', 'z');
		tokenizer.commentChar( '*' );
		tokenizer.commentChar( ';' );
		tokenizer.parseNumbers();
		// Create a new (empty) table database.
		FTableDatabase db = new FTableDatabase();
		
		// Loop over all the tables in the file, reading them in one after the other.
		int tokentype = tokenizer.nextToken();
		while ( tokentype != StreamTokenizer.TT_EOF ) {
			tokenizer.pushBack();
			db.put( readFDATable( tokenizer ) );
			tokentype = tokenizer.nextToken();
		}
		return db;
	}

	/**
	*  Read a single FDHS table from an FDA formatted file.
	*
	*  @param   tokenizer  Tokenizer for the file being read in.
	*  @return A table containing the data read in from the disk file.
	**/
	private static FloatTable readFDATable( StreamTokenizer tokenizer ) throws IOException {
		int i;
		int tokentype;
		double value = 0;
		
		// Set up the tokenizer options.
		tokenizer.eolIsSignificant( false );
		
		// Create a vector to temporarily hold the names read in from the file.
		ArrayList<String> names = new ArrayList<String>();
		
		// *** Read in the FDA file header.
		
		// Read in the table name, independent names and 1st number of breakpoints.
		while ( (tokentype = tokenizer.nextToken()) != StreamTokenizer.TT_EOF ) {
			if ( tokentype == StreamTokenizer.TT_NUMBER ) {
				// Stop when we encounter the 1st number.
				value = tokenizer.nval;
				break;
				
			} else {
				if ( tokentype == StreamTokenizer.TT_WORD ) {
					// Add each parameter name to our list of names.
					names.add( tokenizer.sval );
				}
			}
		}
		if ( tokentype == StreamTokenizer.TT_EOF )
			throw new IOException( kEOFErrMsg + tokenizer.lineno() );
		
		// Start filling in some variables.
		int numDims = names.size() - 1;
		String tblName = names.get( 0 );
		String[] indepNames = new String [ numDims ];
		i = 0;
		for ( int j = numDims - 1; j >= 0; --j ) {
			indepNames[i] = names.get( j + 1 );
			++i;
		}
		names = null;
		
		// Start checking for End-Of-Line (EOL).
		tokenizer.eolIsSignificant( true );
		
		// Read in the number of breakpoints per dimension
		// (first one has already been read in).
		int[] elemPerDim = new int [ numDims ];
		elemPerDim[numDims - 1] = (int) value;
		for ( i = numDims - 2; i >= 0; --i ) {
			value = TRUtils.nextNumber( tokenizer );
			elemPerDim[i] = (int) value;
		}
		
		// Skip optional "Logical" keyword and go to next line.
		TRUtils.nextLine( tokenizer );
		
		// Skip the frequency card (read to start of next line).
		TRUtils.nextLine( tokenizer );
		
		// Stop checking for EOL.
		tokenizer.eolIsSignificant( false );
		
		// *** Read in the breakpoints.
		
		// Read in breakpoints for each independent variable.
		float[][] breakpoints = new float[ numDims ][];
		for ( i = 0; i < numDims; ++i )
			breakpoints[i] = new float [ elemPerDim[i] ];
		
		for ( i = 0; i < numDims; ++i ) {
			for ( int j = 0; j < breakpoints[i].length; ++j ) {
				value = TRUtils.nextNumber( tokenizer );
				breakpoints[i][j] = (float) value;
			}
		}
		
		// *** Read in the dependent values.
		
		// Recursively read in the dependent data arrays.
		FloatArrayNDim depData = new FloatArrayNDim( elemPerDim );
		int[] indep = new int [ numDims ];
		rReadFDA( 0, tokenizer, indep, depData, breakpoints );
		
		// Create a new floatTable object using this information.
		FloatTable theTable = new FloatTable( tblName, indepNames, breakpoints, depData );
		
		return theTable;
	}

	/**
	*  Recursively read in arrays of dependent data from an FDA
	*  formatted table file into the parameter depData.
	*
	*  @param  dim  The dimension (independent) we are currently looping through.
	*               Should initially be set to zero.
	*  @param  tokenizer  A tokenizer for the input file stream.
	*  @param  indep    An array of indexes into the data table (independents).
	*  @param  depData  The dependent data array we are reading into.
	*  @param  breakpoints  The array of breakpoint arrays.
	**/
	private static void rReadFDA( int dim, StreamTokenizer tokenizer, int[] indep,
								FloatArrayNDim depData, float[][] breakpoints ) throws IOException {
		if ( dim == indep.length - 1 ) {
			// Reached the highest dimension, indep[] is now ready.
			// Read in an array.
			readFDADataArray( tokenizer, indep, depData,
			breakpoints[dim].length );
		
		} else {
			
			// Loop over all the breakpoints in this dimension.
			for ( int i = 0; i < breakpoints[dim].length; ++i ) {
				
				// Fill in the required value for indep[] at this dimension.
				indep[dim] = i;
				
				// Go on to the next higher dimension.
				rReadFDA( dim + 1, tokenizer, indep, depData, breakpoints );
			}
		}
	}

	/**
	*  Read a single array of dependent values from an FDA table file.
	*  The data read in is stored in the parameter depData.
	*
	*  @param  tokenizer  A tokenizer for the input file stream.
	*  @param  indep    An array of indexes into the data table (independents).
	*  @param  depData  The array of dependent values we are reading into.
	*  @param  length   The length of most rapidly varying breakpoint array
	*                   ( breakpoints[numDims-1].length).
	**/
	private static void readFDADataArray( StreamTokenizer tokenizer, int[] indep,
										FloatArrayNDim depData, int length ) throws IOException {
		// Loop over all the breakpoints in the last indepenent (the array)
		// and read in the array.
		for ( int j = 0; j < length; ++j ) {
			indep[indep.length - 1] = j;
			double value = TRUtils.nextNumber( tokenizer );
			depData.set( indep, (float) value );
		}
	}

	/**
	*  Write out a single FDHS formatted table to an FDA file.
	*
	*  @param  output  Reference to a file to write the specified table to.
	*  @param  table   The table to be written out.
	**/
	private static void writeFDATable( BufferedWriter output, FloatTable table ) throws IOException {
		int i;
		String space = "     ";
		String buffer;
		DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
		nf.setMinimumFractionDigits( kNumFracDigits );
		nf.setMaximumFractionDigits( kNumFracDigits );
		
		// Write out some generic table comments.
		output.write( "; " + table.toString() );
		output.newLine();
		output.write( "; Date and time written:  " );
		output.write( DateFormat.getDateTimeInstance().format( new Date() ) );
		output.newLine();
		output.write( "; " );
		output.newLine();
		
		// Write out the table name and independent variable names.
		output.write( table.getTableName() );
		output.write( space );
		int numDims = table.dimensions();
		for ( i = numDims - 1; i >= 0; --i ) {
			output.write( table.getIndepName( i ) );
			output.write( space );
		}
		
		// Write out number of breakpoints in each dimension.
		for ( i = numDims - 1; i > 0; --i ) {
			buffer = Integer.toString( table.getNumBreakpoints( i ) );
			output.write( buffer );
			output.write( space );
		}
		buffer = Integer.toString( table.getNumBreakpoints( 0 ) );
		output.write( buffer );
		output.newLine();
		
		// Write out the frequency card.
		output.write( "all       all       " );
		output.newLine();
		
		// Loop over the breakpoint list and write out all the breakpoints.
		for ( i = 0; i < numDims; ++i ) {
			
			// Write out the breakpoints for each dimension.
			float[] breakpoints = table.getBreakpoints( i );
			int length = breakpoints.length;
			int count = 0;
			for ( int j = 0; j < length; ++j ) {
				buffer = formatNumber( kNumDigits, nf, breakpoints[j] );
				output.write( buffer );
				++count;
				if ( count > 5 ) {
					count = 0;
					output.newLine();
				}
			}
			if ( count != 0 )
				output.newLine();
		}
		
		// *** Write out the dependent values.
		
		// Recursively write out the dependent data arrays.
		int[] indep = new int [ numDims ];
		rWriteFDA( 0, output, indep, table, nf );
	}

	/**
	*  Recursively write out arrays of dependent data to an FDA
	*  formatted table file.
	*
	*  @param  dim  The dimension (independent) we are currently looping through.
	*               Should initially be set to zero.
	*  @param  output  Reference to the writer being used to output the data.
	*  @param  indep   An array of indexes into the data table (independents).
	*  @param  theTable  The table we are writing out.
	*  @return No return value
	**/
	private static void rWriteFDA( int dim, BufferedWriter output, int[] indep,
									FloatTable theTable, NumberFormat nf ) throws IOException {
		if ( dim == indep.length - 1 ) {
			// Reached the highest dimension, indep[] is now ready.
			// Write out an array.
			writeFDADataArray( output, indep, theTable, nf );
		
		} else {
			
			// Loop over all the breakpoints in this dimension.
			int length = theTable.getNumBreakpoints( dim );
			for ( int i = 0; i < length; ++i ) {
				
				// Fill in the required value for indep[] at this dimension.
				indep[dim] = i;
				
				// Go on to the next higher dimension.
				rWriteFDA( dim + 1, output, indep, theTable, nf );
			}
		}
	}

	/**
	*  Write a single array of dependent values to an FDA table file.
	*
	*  @param  output  A writer used to output the data.
	*  @param  indep   An array of indexes into the data table (independents).
	*  @param  theTable The table we are writing out.
	**/
	private static void writeFDADataArray( BufferedWriter output, int[] indep,
										FloatTable theTable, NumberFormat nf ) throws IOException {
		int count = 0;
		int numDims = indep.length;
		int length = theTable.getNumBreakpoints( numDims - 1 );
		
		// Loop over all the breakpoints in the last indepenent (the array)
		// and write out the data array.
		for ( int j = 0; j < length; ++j ) {
			indep[numDims - 1] = j;
			float value = theTable.get( indep );
			output.write( formatNumber( kNumDigits, nf, value ) );
			++count;
			if ( count > 5 ) {
				count = 0;
				output.newLine();
			}
		}
		if ( count != 0 )
			output.newLine();
	}

	/**
	*  Format a number for output to a table file.
	*  This method formats the given number using the supplied
	*  NumberFormat object.  It then adds spaces to the start
	*  of the formatted number until the string reaches the
	*  specified length.
	*
	*  @param  size   The overall length of the formatted number including
	*                 the decimal point, minus sign, "E" notation, etc.
	*  @param  nf     The NumberFormat to use when formatting this number.
	*  @param  number The number to be formatted.
	**/
	private static String formatNumber( int size, NumberFormat nf, float number ) {
		StringBuffer buffer = new StringBuffer( nf.format( number ) );
		TRUtils.addSpaces( size, buffer );
		return buffer.toString();
	}

	/**
	*  Write out any database notes as file level comments to the FDA file.
	*
	*  @param  output  A writer used to output the data.
	*  @param  tables  The table database we are writing out.
	**/
	private static void writeFileComments( BufferedWriter output, FTableDatabase tables )
												throws IOException {
		int numNotes = tables.numberOfNotes();
		for ( int i = 0; i < numNotes; ++i ) {
			output.write( "; " );
			output.write( tables.getNote( i ) );
			output.newLine();
		}
	}


}


