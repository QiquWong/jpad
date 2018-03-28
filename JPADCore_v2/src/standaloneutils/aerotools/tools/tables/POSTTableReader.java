/*
*   POSTTableReader -- A class that can read and write POST table (*.dat) multi-table files.
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
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Date;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.ParseException;


/**
*  A class that provides methods for reading
*  or writing a POST table (*.dat) formatted multi-table file.
*  These files are used by the POST trajectory simulation system to
*  store aerodynamic and propulsion data among other things.  See
*  POST documentation for more information.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  December 8, 2008
*  @version   October 2, 2011
**/
public class POSTTableReader implements FTableReader {

	//  The preferred file extension for files of this reader's type.
	public static final String kExtension = "dat";

	//  A brief description of the format read by this reader.
	public static final String kDescription = "POST Table";

	//	The characters used to indicate that a line is commented out.
	private static final String kComment1 = "c";
	private static final String kComment2 = "/";
	
	private static final String kDelimiters = ",";
	
	private static final String kParseErrMsg = "Error parsing POST table file on line #";

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
	*  Will return FTableReader.YES if the stream is recognized as an POST table file.
	*
	*  @param   name   The name of the file.
	*  @param   input  An input stream containing the data to be read.
	*  @return FTableReader.NO if the file is not recognized at all or FTableReader.YES if the
	*           file appears to contain POST tables.
	**/
	public int canReadData(String name, InputStream input) throws IOException {
		
		//	Look for the "[pl ]$tab" sequence.
		LineNumberReader reader = new LineNumberReader( new InputStreamReader( input ) );
		String aLine = reader.readLine();
		while(aLine != null && (aLine.length() < 5 || !aLine.substring(1).trim().equals("$tab")) )
			aLine = reader.readLine();
		
		if (aLine == null)
			return NO;
		
		return YES;
	}
	
	/**
	*  Returns true.  This class can write data to an POST table file.
	**/
	public boolean canWriteData() {
		return true;
	}
	
	/**
	*  Method that reads in POST table formatted data
	*  from the specified input stream and returns that data as an
	*  FTableDatabase object.
	*
	*  @param   input  An input stream containing the POST table formatted data.
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
	*  list of FloatTable objects to the specified output stream in POST table format.
	*
	*  @param   output  The output stream to which the data is to be written.
	*  @param   tables  The list of FloatTable objects to be written out.
	*  @throws IOException If there is a problem writing to the specified stream.
	**/
	public void write( OutputStream output, FTableDatabase tables ) throws IOException {
		if (tables.size() < 1)	return;
		
		// Get a reference to the output stream.
		PrintWriter writer = new PrintWriter( new OutputStreamWriter( output ) );
		
		//	Write header for 1st table.
		writer.println(" $");
		writer.println("p$tab");
		
		//	Write out the comments.
		writeFileComments(writer, tables);
		
		//	Write out some generic table comments.
		writer.print("c Date and time written: ");
		writer.println(  DateFormat.getDateTimeInstance().format( new Date() ) );
		writer.println("c");
		
		//	Write out the 1st table.
		Iterator<FloatTable> i = tables.iterator();
		FloatTable table = (FloatTable)i.next();
		writeTable(writer, table);
		
		//	Write out the remaining tables.
		while ( i.hasNext() ) {
			writer.println(" $");
			writer.println("p$tab");
			table = i.next();
			writeTable(writer, table);
		}
		
		//	Flush the output.
		writer.flush();
	}

	/**
	*  Read in a POST table formatted multi-table stream and create
	*  a new table database with the new files.
	*
	*  @param  in  A file reader that reads in the characters from the file.
	*  @returns A table database containing the tables read in from the file.
	**/
	private static FTableDatabase readStream( LineNumberReader in ) throws IOException {
		
		// Create an empty table database.
		FTableDatabase db = new FTableDatabase();
		
		try {
			//	Look for the "[pl ]$tab" sequence.
			String aLine = in.readLine();
			while(aLine != null && (aLine.length() < 5 || !aLine.substring(1).trim().equals("$tab")) )
				aLine = in.readLine();
			if (aLine == null)
				throw new IOException("Could not find a \" $tab\" namelist.");
			
			while (aLine != null) {
				//	Read in any comments.
				ArrayList<String> noteList = new ArrayList<String>();
				aLine = readComments(in, noteList);
				if (aLine == null)
					throw new IOException( kEOFErrMsg + in.getLineNumber() + ".");
				if (noteList.size() > 0)
					db.addAllNotes((String[])noteList.toArray());
				
				//	Parse the table definition line.
				aLine = aLine.trim();
				StringTokenizer tokenizer = new StringTokenizer(aLine, kDelimiters);
				
				//	Get the table name.
				String token = tokenizer.nextToken().trim();
				if (!token.startsWith("table") || !token.contains("="))
					throw new IOException("Could not find the table name on line #" + in.getLineNumber() + ".");
				String tblName = parseQuotedText(token, '\'');
				if (tblName == null || tblName.length() == 0)
					throw new IOException("Could not parse the table name on line #" + in.getLineNumber() + ".");
				
				//	Get the number of dimensions.
				token = tokenizer.nextToken().trim();
				int numDim = Integer.valueOf(token).intValue();
				
				if (numDim < 0 || numDim > 3)
					throw new IOException("Number of breakpoints on line #" + in.getLineNumber() + " must be 1, 2 or 3.");
				
				if (numDim > 0) {
					//	Get the independent variable names.
					String[] indepNames = new String[numDim];
					for (int i=0; i < numDim; ++i) {
						token = tokenizer.nextToken();
						String varName = parseQuotedText(token, '\'');
						if (varName == null || varName.length() == 0)
							throw new IOException("Could not parse a table independent variable name on line #" + in.getLineNumber() + ".");
						indepNames[i] = varName;
					}
					
					//	Get the number of breakpoints in each dimension.
					int[] numBP = new int[numDim];
					for (int i=0; i < numDim; ++i) {
						token = tokenizer.nextToken().trim();
						int num = Integer.valueOf(token).intValue();
						if (num < 1)	throw new IOException("Invalid number of breakpoints found on line #" + in.getLineNumber() + ".");
						numBP[i] = num;
					}
					
					//	Read in the table based on the number of dimensions.
					switch (numDim) {
						case 1:	db.put(read1DTable(in, tblName, indepNames[0], numBP[0]));	break;
						case 2: db.put(read2DTable(in, tblName, indepNames, numBP));		break;
						case 3: db.put(read3DTable(in, tblName, indepNames, numBP));		break;
					}
				
				} else
					System.out.println("Warning: table \"" + tblName + "\" has dimension = 0.");
				
				//	Look for the next table (if there is one).
				aLine = in.readLine();
				while(aLine != null && (aLine.length() < 5 || !aLine.substring(1).trim().equals("$tab")) )
					aLine = in.readLine();
				
			}	//	end while (aLine != null)
			
		} catch( ParseException e) {
			throw new IOException( kParseErrMsg + in.getLineNumber() + "." );
		} catch( NumberFormatException e ) {
			throw new IOException( kParseErrMsg + in.getLineNumber() + "." );
		}
		
        return db;
	}
	
	
	/**
	*  Method that reads in and returns a 1D table from the input stream.
	*
	*  @param  reader  A file reader that reads in the characters from the file.
	*  @return A table containing the 1D table data read in from the file.
	**/
	private static FloatTable read1DTable(LineNumberReader reader, String tblName, String indepName, int numBP)
												throws IOException, NumberFormatException, ParseException {
		DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
		
		//	Create an independent and dependent variable array.
		float[] indepArr = new float[numBP];
		float[] depArray = new float[numBP];
		
		//	Begin reading in data by looping over the breakpoints.
		StringTokenizer tokenizer = null;
		for (int i=0; i < numBP; ++i) {
			//	Get a tokenizer that has some data.
			tokenizer = getTokenizer(reader, tokenizer);
			
			//	Get the independent variable value.
			String token = tokenizer.nextToken().trim();
			float value = nf.parse( token ).floatValue();
			indepArr[i] = value;
			
			//	Get a tokenizer that has some data.
			tokenizer = getTokenizer(reader, tokenizer);
			
			//	Get the dependent value.
			token = tokenizer.nextToken().trim();
			value = nf.parse( token ).floatValue();
			depArray[i] = value;
		}
		
		FloatTable table = new FloatTable(tblName, indepName, indepArr, depArray);
		
		return table;
	}
	
	
	/**
	*  Method that reads in and returns a 2D table from the input stream.
	*
	*  @param  reader  A file reader that reads in the characters from the file.
	*  @return A table containing the 2D table data read in from the file.
	**/
	private static FloatTable read2DTable(LineNumberReader reader, String tblName, String[] indepNames, int[] numBP)
												throws IOException, NumberFormatException, ParseException {
		DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
		
		//	Reverse the independent variables.
		int tmp = numBP[0];
		numBP[0] = numBP[1];
		numBP[1] = tmp;
		String tmpStr = indepNames[0];
		indepNames[0] = indepNames[1];
		indepNames[1] = tmpStr;
		
		//	Create a dependent variable array.
		float[][] depArray = new float[numBP[0]][numBP[1]];
		
		//	Create the independent variable arrays.
		float[] indepArr1 = new float[numBP[0]];
		float[] indepArr2 = new float[numBP[1]];
		
		//	Begin reading in data by looping over the breakpoints.
		StringTokenizer tokenizer = null;
		for (int i=0; i < numBP[0]; ++i) {
		
			//	Get a tokenizer that has some data.
			tokenizer = getTokenizer(reader, tokenizer);
			
			//	Get the independent value.
			String token = tokenizer.nextToken().trim();
			float value = nf.parse( token ).floatValue();
			indepArr1[i] = value;
			
			for (int j=0; j < numBP[1]; ++j) {
			
				//	Get a tokenizer that has some data.
				tokenizer = getTokenizer(reader, tokenizer);
				
				//	Get the independent variable value.
				token = tokenizer.nextToken().trim();
				value = nf.parse( token ).floatValue();
				if (i == 0)
					indepArr2[j] = value;
				else
					if (indepArr2[j] != value)
						throw new IOException("Inconnsistant independent variable values on line #" + reader.getLineNumber() + ".");
				
				//	Get a tokenizer that has some data.
				tokenizer = getTokenizer(reader, tokenizer);
				
				//	Get the dependent value.
				token = tokenizer.nextToken().trim();
				value = nf.parse( token ).floatValue();
				depArray[i][j] = value;
			}
		}
		
		//	Create the table.
		FloatTable table = new FloatTable(tblName, indepNames, indepArr1, indepArr2, depArray);
		
		return table;
	}
	
	/**
	*  Method that reads in and returns a 3D table from the input stream.
	*
	*  @param  reader  A file reader that reads in the characters from the file.
	*  @return A table containing the 3D table data read in from the file.
	**/
	private static FloatTable read3DTable(LineNumberReader reader, String tblName, String[] indepNames, int[] numBP)
												throws IOException, NumberFormatException, ParseException {
		DecimalFormat nf = (DecimalFormat) NumberFormat.getInstance();
		
		//	Reverse the independent variables.
		int tmp = numBP[0];
		numBP[0] = numBP[2];
		numBP[2] = tmp;
		String tmpStr = indepNames[0];
		indepNames[0] = indepNames[2];
		indepNames[2] = tmpStr;
		
		//	Create a dependent variable array.
		float[][][] depArray = new float[numBP[0]][numBP[1]][numBP[2]];
		
		//	Create the independent variable arrays.
		float[] indepArr1 = new float[numBP[0]];
		float[] indepArr2 = new float[numBP[1]];
		float[] indepArr3 = new float[numBP[2]];
		
		//	Begin reading in data by looping over the breakpoints.
		StringTokenizer tokenizer = null;
		for (int i=0; i < numBP[0]; ++i) {
		
			//	Get a tokenizer that has some data.
			tokenizer = getTokenizer(reader, tokenizer);
			
			//	Get the independent value.
			String token = tokenizer.nextToken().trim();
			float value = nf.parse( token ).floatValue();
			indepArr1[i] = value;
			
			for (int j=0; j < numBP[1]; ++j) {
			
				//	Get a tokenizer that has some data.
				tokenizer = getTokenizer(reader, tokenizer);
				
				//	Get the independent variable value.
				token = tokenizer.nextToken().trim();
				value = nf.parse( token ).floatValue();
				if (i == 0)
					indepArr2[j] = value;
				else
					if (indepArr2[j] != value)
						throw new IOException("Inconnsistant independent variable values on line #" + reader.getLineNumber() + ".");
				
				for (int k=0; k < numBP[1]; ++k) {
				
					//	Get a tokenizer that has some data.
					tokenizer = getTokenizer(reader, tokenizer);
					
					//	Get the independent variable value.
					token = tokenizer.nextToken().trim();
					value = nf.parse( token ).floatValue();
					if (j == 0)
						indepArr3[k] = value;
					else
						if (indepArr3[k] != value)
							throw new IOException("Inconnsistant independent variable values on line #" + reader.getLineNumber() + ".");
					
					//	Get a tokenizer that has some data.
					tokenizer = getTokenizer(reader, tokenizer);
					
					//	Get the dependent value.
					token = tokenizer.nextToken().trim();
					value = nf.parse( token ).floatValue();
					depArray[i][j][k] = value;
				}
			}
		}
		
		//	Create the table.
		FloatTable table = new FloatTable(tblName, indepNames, indepArr1, indepArr2, indepArr3, depArray);
		
		return table;
	}
	
	
	/**
	*  Return a reference to a tokenizer that has at least one token in it.
	*  This is used when data may stretch across many lines or all be on one.
	**/
	private static StringTokenizer getTokenizer(LineNumberReader reader, StringTokenizer existing)
					throws IOException {
		
		while (existing == null || !existing.hasMoreTokens()) {
			String aLine = readLineNoComments(reader);
			if (aLine == null)	throw new IOException( kEOFErrMsg + reader.getLineNumber() + ".");
			existing = new StringTokenizer(aLine, kDelimiters);
		}
		
		return existing;
	}
	
	
	/**
	*  Method that reads in any table comments and places them in the specified list.
	*  Returns the 1st non-comment line read.
	**/
	private static String readComments(LineNumberReader reader, List<String> noteList) throws IOException {
		
		String aLine = reader.readLine();
		while (aLine != null && (aLine.startsWith(kComment1) || aLine.startsWith(kComment2)) ) {
			aLine = aLine.substring(1);
			noteList.add(aLine);
			aLine = reader.readLine();
		}
		
		return aLine;
	}
	

	/**
	*  Reads a line of text from the supplied file reader stripping out any
	*  lines that are commented out.
	**/
	private static String readLineNoComments(LineNumberReader reader) throws IOException {
	
		String aLine = reader.readLine();
		while (aLine != null && (aLine.startsWith(kComment1) || aLine.startsWith(kComment2)) )
			aLine = reader.readLine();
		
		//	Strip off anything after the "/" comment character.
		int indx = aLine.indexOf("/");
		if (indx >= 0)
			aLine = aLine.substring(0,indx);
		
		return aLine;
	}
	
	
	/**
	*  Method that parses out quoted text from a string and returns whatever is between the quotes.
	*
	*  @param input     The text containing a quote.
	*  @param quoteChar The character that delimits the quote.
	*  @return All the text between the 1st and last occurance of quoteChar in the input String.
	**/
	private static String parseQuotedText(String input, int quoteChar) {
		int idx1 = input.indexOf(quoteChar) + 1;
		int idx2 = input.lastIndexOf(quoteChar);
		return input.substring(idx1,idx2);
	}
	
	
	/**
	*  Write out the file level comments.
	**/
	private static void writeFileComments(PrintWriter output, FTableDatabase tables) {
		int numNotes = tables.numberOfNotes();
		for ( int i = 0; i < numNotes; ++i ) {
			output.print( "c" );
			output.println( tables.getNote( i ) );
		}
	}
	
	/**
	* Write out a single table to a POST formatted output stream.
	* This method assumes that the "p$tab" line has already been output.
	*
	*  @param  output  Reference to a PrintWriter to write the specified table to.
	*  @param  table   The table to be written out.
	**/
	private static void writeTable(PrintWriter output, FloatTable table) {
		
		//	Write out the table description.
		output.print(" table = '");
		output.print(table.getTableName());
		output.print("', ");
		
		int numDim = table.dimensions();
		output.print(numDim);
		output.print(", ");
		for (int i=numDim-1; i >= 0; --i) {
			String varName = table.getIndepName(i);
			output.print("'");
			output.print(varName);
			output.print("', ");
		}
		
		for (int i=numDim-1; i >= 0; --i) {
			int numBP = table.getNumBreakpoints(i);
			output.print(numBP);
			output.print(", ");
		}
		
		output.println("8*1,");
		
		//	Write out the table based on the number of dimensions.
		switch (numDim) {
			case 1:	write1DTable(output, table);	break;
			case 2: write2DTable(output, table);	break;
			case 3: write3DTable(output, table);	break;
		}
		
		output.println(" $");
	}
	
	/**
	*  Write a 1D table to a POST table formatted output stream.
	*
	*  @param  output  Reference to a PrintWriter to write the specified table to.
	*  @param  table   The table to be written out.
	**/
	private static void write1DTable(PrintWriter output, FloatTable table) {
		
		//	Get the breakpoints.
		float[] indepArr = table.getBreakpoints(0);
		
		//	Loop over all the data and write them out.
		int numIndep = indepArr.length;
		for (int i=0; i < numIndep; ++i) {
			output.print("          ");
			output.print(formatNumber(8, indepArr[i]));
			output.print(",  ");
			output.print(formatNumber(16, table.get(i)));
			output.println(",");
		}
		
	}
	
	/**
	*  Write a 2D table to a POST table formatted output stream.
	*
	*  @param  output  Reference to a PrintWriter to write the specified table to.
	*  @param  table   The table to be written out.
	**/
	private static void write2DTable(PrintWriter output, FloatTable table) {
		
		//	Get the breakpoints.
		float[] indepArr1 = table.getBreakpoints(0);
		float[] indepArr2 = table.getBreakpoints(1);
		
		//	Loop over all the 1st independent variable.
		int[] pos = new int[2];
		int numIndep1 = indepArr1.length;
		int numIndep2 = indepArr2.length;
		for (int i=0; i < numIndep1; ++i) {
			pos[0] = i;
			
			//	Output this independent
			output.print("      ");
			output.print(formatNumber(8, indepArr1[i]));
			output.println(",");
			
			//	Loop over the 2nd independent variable.
			for (int j=0; j < numIndep2; ++j) {
				pos[1] = j;
				
				output.print("          ");
				output.print(formatNumber(8, indepArr2[j]));
				output.print(",  ");
				output.print(formatNumber(16, table.get(pos)));
				output.println(",");
			}
		}
		
	}
	
	/**
	*  Write a 3D table to a POST table formatted output stream.
	*
	*  @param  output  Reference to a PrintWriter to write the specified table to.
	*  @param  table   The table to be written out.
	**/
	private static void write3DTable(PrintWriter output, FloatTable table) {
		
		//	Get the breakpoints.
		float[] indepArr1 = table.getBreakpoints(0);
		float[] indepArr2 = table.getBreakpoints(1);
		float[] indepArr3 = table.getBreakpoints(2);
		
		//	Loop over all the 1st independent variable.
		int[] pos = new int[3];
		int numIndep1 = indepArr1.length;
		int numIndep2 = indepArr2.length;
		int numIndep3 = indepArr3.length;
		for (int i=0; i < numIndep1; ++i) {
			pos[0] = i;
			
			//	Output this independent
			output.print("      ");
			output.print(formatNumber(8, indepArr1[i]));
			output.println(",");
			
			//	Loop over the 2nd independent variable.
			for (int j=0; j < numIndep2; ++j) {
				pos[1] = j;
				
				//	Output this independent
				output.print("      ");
				output.print(formatNumber(8, indepArr2[j]));
				output.println(",");
				
				//	Loop over the 3rd independent variable.
				for (int k=0; k < numIndep3; ++k) {
					pos[2] = k;
					
					output.print("          ");
					output.print(formatNumber(8, indepArr3[k]));
					output.print(",  ");
					output.print(formatNumber(16, table.get(pos)));
					output.println(",");
				}
			}
		}
		
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
	private static String formatNumber( int size, float number ) {
		StringBuffer buffer = new StringBuffer(Float.toString(number));
		TRUtils.addSpaces( size, buffer );
		return buffer.toString();
	}

}


