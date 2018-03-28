/*
*   C81Reader	-- A class that can read C-81 airfoil aerodynamic tables.
*
*   Copyright (C) 2003-2004 by Joseph A. Huwaldt
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
*  A class that provides a method for reading C-81 airfoil
*  aerodynamics data tables.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  November 13, 2002
*  @version   June 15, 2004
**/
public class C81Reader implements FTableReader {

	//	Debugging flag.
	private static final boolean DEBUG = false;

	//  The preferred file extension for files of this reader's type.
	public static final String kExtension = "c81";

	//  A brief description of the format read by this reader.
	public static final String kDescription = "C81 Airfoil Aero";


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
	*  Will return FTableReader.MAYBE if the file name has the extension "c81".
	*
	*  @param   name   The name of the file.
	*  @param   input  An input stream containing the data to be read.
	*  @return FTableReader.NO if the file is not recognized at all or FTableReader.MAYBE if the
	*           filename extension is "*.c81".
	**/
	public int canReadData(String name, InputStream input) throws IOException {
		name = name.toLowerCase().trim();
		
		int response = NO;
		if (name.endsWith(".c81"))
			response = MAYBE;
		
		return response;
	}
	
	/**
	*  Returns false.  This class can NOT write any data to a C-81 airfoil
	*  aerodynamics formatted file at this time.
	**/
	public boolean canWriteData() {
		return false;
	}
	
	/**
	*  Method that reads in C-81 airfoil aerodynamics formatted data
	*  from the specified input stream and returns that data as an
	*  FTableDatabase object.
	*
	*  @param   input  An input stream containing the C-81 formatted data.
	*  @return An FTableDatabase object that contains the data read
	*           in from the specified stream.
	*  @throws IOException If there is a problem reading the specified stream.
	**/
	public FTableDatabase read(InputStream input) throws IOException {

		// Create a reader to translate the input stream.
		LineNumberReader reader = new LineNumberReader( new InputStreamReader( input ) );

		//	Parse the file's contents.
		FTableDatabase db = readC81(reader);

		return db;
	}

	/**
	*  Method for writing out all the data stored in the specified
	*  list of FloatTable objects to the specified output stream.  This
	*  method is not implemented and will throw an IOException if called.
	*
	*  @param   output  The output stream to which the data is to be written.
	*  @param   tables  The list of FloatTable objects to be written out.
	*  @throws IOException If there is a problem writing to the specified stream.
	**/
	public void write( OutputStream output, FTableDatabase tables ) throws IOException {
		throw new IOException("Writing C-81 files has not yet been implemented.");
	}

	/**
	*  Read in a C-81 airfoil aero data table file using the specified reader.
	*
	*  @param  reader  A file reader that reads in the characters from the file.
	*  @return A collection of tables read in using the reader.
	**/
	private static FTableDatabase readC81(LineNumberReader reader) throws IOException {

		//	Start reading lines looking for the start of the data.
		String aLine = reader.readLine();
		while (aLine != null && aLine.length() < 34 && !aLine.regionMatches(0," ",0,1)) {
			aLine = reader.readLine();
		}
		if (aLine == null)	throw new IOException("Did not find format line for C-81 file.");
		
		// Create an empty table database.
		FTableDatabase db = new FTableDatabase();

		//	Parse out any notes.
		String buffer = aLine.substring(0,29).trim();
		if (buffer.length() > 1)
			db.addNote(buffer);
		
		try {
			//	Create a number format for parsing our numbers.
			DecimalFormat nf = (DecimalFormat)NumberFormat.getInstance();
			
			String tableName;
			String[] indepNames = {"Mach", "Alpha"};

			int[] numMachs = new int[3];
			int[] numAlphas = new int[3];

			//	Extract the number of breakpoints in each table.
			int pos = 30;
			for (int tbl=0; tbl < 3; ++tbl, pos += 2) {

				//	Extract the number of Mach breakpoints in table #0.
				buffer = aLine.substring(pos,pos+2).trim();
				numMachs[tbl] = nf.parse(buffer).intValue();
				if ( aLine == null )
					throw new IOException( kEOFErrMsg + reader.getLineNumber() + "." );

				//	Extract the number of alpha breakpoints in table #0.
				pos += 2;
				buffer = aLine.substring(pos,pos+2).trim();
				numAlphas[tbl] = nf.parse(buffer).intValue();

			}

			//	Loop over the 3 tables to be read in (Cl, Cd, & Cm).
			for (int tbl=0; tbl < 3; ++tbl) {

				//	Allocate arrays needed to build tables.
				float[][] depData = new float[numMachs[tbl]][numAlphas[tbl]];
				float[] machBP = new float[numMachs[tbl]];
				float[] alphaBP = new float[numAlphas[tbl]];
				
				//	Read in the Mach number breakpoints.
				aLine = reader.readLine();
				if ( aLine == null )
					throw new IOException( kEOFErrMsg + reader.getLineNumber() + "." );

				if (DEBUG) {
					System.out.println("Reading table #" + tbl);
					System.out.println("numMachs = " + numMachs[tbl] + ", numAlphas = " + numAlphas[tbl]);
					System.out.print("machBP[] =");
				}

				pos = 7;
				for (int i=0; i < numMachs[tbl]; ++i, pos += 7) {
					if (pos > 64) {
						aLine = reader.readLine();
						if ( aLine == null )
							throw new IOException( kEOFErrMsg + reader.getLineNumber() + "." );
						pos = 7;
					}
					buffer = aLine.substring(pos, pos+7).trim();
					float value = nf.parse(buffer).floatValue();
					machBP[i] = value;

					if (DEBUG)
						System.out.print(" " + value);
				}
				if (DEBUG) {
					System.out.println();
					System.out.print("alphaBP[] =");
				}


				//	Start reading in the alpha breakpoints and the dependent data.
				for (int i=0; i < numAlphas[tbl]; ++i) {

					//	Read in the next alpha line #1.
					aLine = reader.readLine();
					if ( aLine == null )
						throw new IOException( kEOFErrMsg + reader.getLineNumber() + "." );

					//	Parse out the alpha.
					buffer = aLine.substring(0,7).trim();
					float value = nf.parse(buffer).floatValue();
					alphaBP[i] = value;

					if (DEBUG)
						System.out.print(" " + value);

					//	Parse out the dependent data as a function of Mach number.
					pos = 7;
					for (int j=0; j < numMachs[tbl]; ++j, pos += 7) {
						if (pos > 64) {
							aLine = reader.readLine();
						if ( aLine == null )
							throw new IOException( kEOFErrMsg + reader.getLineNumber() + "." );
							pos = 7;
						}

						buffer = aLine.substring(pos, pos+7).trim();
						value = nf.parse(buffer).floatValue();
						depData[j][i] = value;
					}
				}

				//	Determine the table name.
				if (tbl == 0)
					tableName = "Cl";
				else if (tbl == 1)
					tableName = "Cd";
				else
					tableName = "Cm";
				
				//	Create a new table object and add to database.
				FloatTable table = new FloatTable(tableName, indepNames, machBP, alphaBP, depData);
				db.put(table);

			}


		} catch (ParseException e) {
			throw new IOException("Parsing error on line #" + reader.getLineNumber() + ".");
		}

		return db;
	}


}


