/*
*   FLABSavReader -- A class that can read and write FlightLab SAV multi-table files.
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
import java.util.Date;

import standaloneutils.aerotools.util.ExponentialFormat;
import standaloneutils.mathtools.MathTools;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DateFormat;

/**
*  A class that provides methods for reading
*  or writing an ART FlightLab SAV formatted multi-table file.
*  These files are used by the FlightLab simulation system to
*  store fuselage and airfoil aerodynamics data.  The format is
*  very restrictive and can only be used to store the particular
*  tables that FlightLab is looking for.  See ART's FlightLab
*  documentation for more information.
*
*  <p>  Modified by:  Joseph A. Huwaldt    </p>
*
*  @author    Joseph A. Huwaldt    Date:  April 3, 2000
*  @version   June 15, 2004
*  @see <a href="http://www.flightlab.com">http://www.flightlab.com</a>
**/
public class FLABSavReader implements FTableReader {

	//  The preferred file extension for files of this reader's type.
	public static final String kExtension = "sav";

	//  A brief description of the format read by this reader.
	public static final String kDescription = "FlightLab SAV";

    //  Different sub-types of FlightLab SAV files.
    private static final int kUnknown = 0;
    private static final int kHighLowAirfoil = 2;
    private static final int kFuselage2DUniform = 3;

    //  The airfoil table with low/high table names.
    private static final String[] kAFLowHigh= { "CLL", "CDL", "CML", "CLH", "CDH", "CMH" };
	private static final int[] kAFLowHighDim = { 2, 2, 2, 1, 1, 1 };
	
	//	The airfoil low/high independent names.
	private static final String[] kAFLowHighInd = { "AOATL", "AOATH", "MACHT" };
	
	//	The fuselage 2D uniform low/high table names.
    private static final String[] kFuselage2DU = {
		"CDFAH", "CDFAL", "CDFBH", "CDFBL", "CLFAH", "CLFAL",
		"CLFBAH", "CLFBAL", "CMFAL", "CMFAH", "CMFBAH", "CMFBAL",
		"CNFBAH", "CNFBAL", "CRFBAH", "CRFBAL", "CYFBAH", "CYFBAL"};
	private static final int[] kFuselage2DUdim = {
		1, 1, 1, 1, 1, 1,
		2, 2, 1, 1, 2, 2,
		2, 2, 2, 2, 2, 2	};

	//	The fuselage 2D uniform low/high independent names.
	private static final String[] kFuselage2DUi = {
		"FAOAT1", "FAOAT2", "FAOST1", "FAOST2", "FAOAT3" };
		
	//	The fuselage 2D uniform low/high independent table names that go with each dependent.
	private static final String[] kFuselage2DUti = {
		"FAOAT2", "FAOAT1", "FAOST2", "FAOST1", "FAOAT2", "FAOAT1",
		"FAOST2", "FAOST1", "FAOAT1", "FAOAT2", "FAOST2", "FAOST1",
		"FAOST2", "FAOST1", "FAOST2", "FAOST1", "FAOST2", "FAOST1" };
		

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
	*  Will return FTableReader.YES if the stream is recognized as one of the two SAV format
    *  subtypes that this reader recognizes (airfoil or fuselage aero).
	*
	*  @param   name   The name of the file.
	*  @param   input  An input stream containing the data to be read.
	*  @return FTableReader.NO if the file is not recognized at all or FTableReader.SAV if the
	*           stream is recognize as a FlightLab SAV file.
	**/
	public int canReadData(String name, InputStream input) throws IOException {
		name = name.toLowerCase().trim();
		
		int response = NO;
		try {
			//  Look for one of the specific SAV file sub-types that we can read.
			StreamTokenizer tokenizer = new StreamTokenizer( new InputStreamReader( input ) );

			// Set some tokenizer options.
			tokenizer.wordChars('!', 'z');
			tokenizer.commentChar( '#' );
			tokenizer.commentChar( ';' );
			tokenizer.parseNumbers();
			tokenizer.eolIsSignificant( false );
			
			// Determine SAV file type (kHighLowAirfoil or kFuselage2DUniform).
			int type = getType(tokenizer);
			if (type != kUnknown)
				response = YES;
			
		} catch (Throwable ignore) {}
		
		return response;
	}
	
	/**
	*  Returns true.  This class can write data to a FlightLab SAV file.
	**/
	public boolean canWriteData() {
		return true;
	}
	
	/**
	*  Method that reads in ART FlightLab SAV formatted data
	*  from the specified input stream and returns that data as an
	*  FTableDatabase object.
	*
	*  @param   input  An input stream containing the SAV formatted data.
	*  @return An FTableDatabase object that contains the data read
	*           in from the specified stream.
	*  @throws IOException If there is a problem reading the specified stream.
	**/
	public FTableDatabase read(InputStream input) throws IOException {
		
		// Get a reference to the file and create a tokenizer.
		BufferedReader reader = new BufferedReader( new InputStreamReader( input ) );
		StreamTokenizer tokenizer = new StreamTokenizer( reader );
		
		FTableDatabase db = readStream( tokenizer );
		
		return db;
	}

	/**
	*  Method for writing out all the data stored in the specified
	*  list of FloatTable objects to the specified output stream in SAV format.
	*  The FlightLab SAV format requires a specific number of tables and each
	*  table must have very specific names and size constraints.  See ART
	*  documentation for more information.
	*
	*  @param   output  The output stream to which the data is to be written.
	*  @param   tables  The list of FloatTable objects to be written out.
	*  @throws IOException If there is a problem writing to the specified stream.
	**/
	public void write( OutputStream output, FTableDatabase tables ) throws IOException {
		BufferedWriter writer=null;
		
		// Check for consistant tables and get the appropriate table type.
		int type = checkTables( tables );
		
		// Write out the tables.
		switch(type) {
		    case kHighLowAirfoil:
		        writer = new BufferedWriter( new OutputStreamWriter( output ) );
		        writeHighLowAirfoil( writer, tables );
		        break;

		    case kFuselage2DUniform:
		        writer = new BufferedWriter( new OutputStreamWriter( output ) );
		        writeFuselage2DUniform( writer, tables);
		        break;

		    default:
                throw new IOException("The specified tables are not appropriate for any known SAV file.");
		}
		
		//  Flush the output.
		writer.flush();
	}

	/**
	*  Read in an FlightLab SAV formatted multi-table stream and create
	*  a new table database with the new files.
	*
	*  @param   tokenizer  Tokenizer for the file being read in.
	*  @returns A table database containing the tables read in from the file.
	**/
	private static FTableDatabase readStream( StreamTokenizer tokenizer ) throws IOException {
		
		// Set some tokenizer options.
		tokenizer.wordChars('!', 'z');
		tokenizer.commentChar( '#' );
		tokenizer.commentChar( ';' );
		tokenizer.parseNumbers();
		tokenizer.eolIsSignificant( false );
		
		// Determine SAV file type (kHighLowAirfoil or kFuselage2DUniform).
		int type = getType(tokenizer);
        
		//  Read in the table data based on SAV file sub-type.
		FTableDatabase db = null;
		
		switch (type) {
            case kHighLowAirfoil:
                db = readHighLowAirfoil(tokenizer);
                break;

            case kFuselage2DUniform:
                db = readFuselage2DUniform(tokenizer);
                break;

            default:
                throw new IOException("Unknown SAV file sub-type.");
 		}

        return db;
	}


    /**
    *  Return the FlightLab SAV file sub-type.  SAV files are used in FlightLab
    *  to save a variety of specific data sets.  This checks the file to determine
    *  what specific data set is stored in this SAV file.
    *  Returns one of the constants at the top of this class.
    **/
    private static int getType(StreamTokenizer tokenizer) throws IOException {

		int tokentype = tokenizer.nextToken();
		
        if ( tokentype == StreamTokenizer.TT_EOF )
			throw new IOException( kEOFErrMsg + tokenizer.lineno() );
            
		else if ( tokentype == StreamTokenizer.TT_NUMBER )
			throw new IOException( kExpWordErrMsg + tokenizer.lineno() );
				
		else if ( tokentype == StreamTokenizer.TT_WORD ) {
			//  We found a word.  Should be the name of the 1st parameter.
		    //  This should tell us the file sub-type.
		    String word = tokenizer.sval;
		    tokenizer.pushBack();

		    if (word.equals("AOATL"))
		        //  We've got an airfoil table with low/high AOA and uniform increment.
		        return kHighLowAirfoil;

		    else if (word.startsWith("FAOA") || word.startsWith("FAOS"))
		        //  We've got a fuselage 2D uniform table.
		        return kFuselage2DUniform;
		}

        return kUnknown;
    }


	/**
	*  Read in an FlightLab SAV airfoil table with low/high and uniform increment
	*  formatted multi-table stream and create a new table database.
	*
	*  @param   tokenizer  Tokenizer for the file being read in.
	*  @returns A table database containing the tables read in from the file.
	**/
	private static FTableDatabase readHighLowAirfoil( StreamTokenizer tokenizer )
	                        throws IOException {
        
		// Start checking for End-Of-Line (EOL).
		tokenizer.eolIsSignificant( true );

		//	Read in all the independent tables.
		int numIndeps = kAFLowHighInd.length;
		boolean[] found = new boolean[numIndeps];
		float[] indepMin = new float[numIndeps];
		float[] indepMax = new float[numIndeps];
		float[] indepDelta = new float[numIndeps];
		for (int i=0; i < numIndeps; ++i) {

			//  Get the next token.  Should be one of the independent table names.
			String word = TRUtils.nextWord(tokenizer);
			
			//	Did we find a valid independent table name?
			int idx = idx2str(word, kAFLowHighInd, found, "Unexpected independent table name in SAV file.");

			// Skip over parameter count data as it should always be "3 1 0".
			TRUtils.nextLine( tokenizer );

			//  Retrieve the min, max and delta values for this independent.
			float min = (float)TRUtils.nextNumber(tokenizer);
			float max = (float)TRUtils.nextNumber(tokenizer);
			float delta = (float)TRUtils.nextNumber(tokenizer);
			TRUtils.nextLine( tokenizer );
			TRUtils.nextLine( tokenizer );
			
			indepMin[idx] = min;
			indepMax[idx] = max;
			indepDelta[idx] = delta;
        }

        
		//  Create names for independent variables.
		String[] indepL = new String[2];
		indepL[0] = "MACHT";
		indepL[1] = "AOATL";
		String indepH = "AOATH";
		
		//	Extract range data.
		float aoatlmax = indepMax[0];
		float aoatlmin = indepMin[0];
		float aoatldelta = indepDelta[0];
		float aoathmax = indepMax[1];
		float aoathmin = indepMin[1];
		float aoathdelta = indepDelta[1];
		float machtmax = indepMax[2];
		float machtmin = indepMin[2];
		float machtdelta = indepDelta[2];

		//  Create breakpoint tables for independent variables.
		int numBPaoatl = (int)((aoatlmax - aoatlmin)/aoatldelta) + 1;
		int numBPaoath = (int)((aoathmax - aoathmin)/aoathdelta) + 1;
		int numBPmacht = (int)((machtmax - machtmin)/machtdelta) + 1;
		
		float[][] breakpointsL = new float[2][];
		breakpointsL[0] = new float[numBPmacht];
		breakpointsL[1] = new float[numBPaoatl];
		float[] breakpointsH = new float[numBPaoath];
		
		for (int i=0; i < numBPmacht; ++i)
		    breakpointsL[0][i] = machtmin + machtdelta*i;
		    
		for (int i=0; i < numBPaoatl; ++i)
		    breakpointsL[1][i] = aoatlmin + aoatldelta*i;

		for (int i=0; i < numBPaoath; ++i)
		    breakpointsH[i] = aoathmin + aoathdelta*i;

        //  Create an array to contain the tables to be read in (6 tables).
		FloatTable[] tables = new FloatTable [6];


		//	Read in all the tables one after another.
		found = new boolean[6];
		for (int i=0; i < 6; ++i) {

			//  Get the next token (should be a dependent table name).
			tokenizer.eolIsSignificant( false );
			String tblName = TRUtils.nextWord(tokenizer);

			//	Did we find a valid dependent table name?
			int idx = idx2str(tblName, kAFLowHigh, found, "Unexpected dependent table name in SAV file.");

			//	Deal with 1D and 2D tables a little differently.
			if (kAFLowHighDim[idx] == 1) {
				//  Create the table without any dependent data.
				FloatTable table = new FloatTable(tblName, indepH, breakpointsH, null);
				
				//  Put table into array of tables.
				tables[idx] = table;
				
				//  Read in the data for this table.
				read1DTableData(tokenizer, tblName, "AOATH", breakpointsH.length, table);
				
			} else {
				//	Must be a 2D table.
				
				FloatTable table = new FloatTable(tblName, indepL, breakpointsL, null);
				tables[idx] = table;
				read2DTableData(tokenizer, tblName, "AOATL", breakpointsL[1].length, breakpointsL[0].length, table);
			}
		
		}

		// Create a new table database.
		FTableDatabase db = new FTableDatabase( tables );

		return db;
	}


	/**
	*  Read in an FlightLab SAV fuselage 2D uniform increment
	*  formatted multi-table stream and create a new table database.
	*
	*  @param   tokenizer  Tokenizer for the file being read in.
	*  @returns A table database containing the tables read in from the file.
	**/
	private static FTableDatabase readFuselage2DUniform( StreamTokenizer tokenizer )
	                        throws IOException {
        
		// Start checking for End-Of-Line (EOL).
		tokenizer.eolIsSignificant( true );

		//	Read in all the independent tables.
		int numIndeps = kFuselage2DUi.length;
		boolean[] found = new boolean[numIndeps];
		float[] indepMin = new float[numIndeps];
		float[] indepMax = new float[numIndeps];
		float[] indepDelta = new float[numIndeps];
		for (int i=0; i < numIndeps; ++i) {
		
			//  Get the next token.  Should be one of the independent table names.
			String word = TRUtils.nextWord(tokenizer);

			//	Did we find a valid independent table name?
			int idx = idx2str(word, kFuselage2DUi, found, "Unexpected independent table name in SAV file.");
			
			// Skip over parameter count data as it should always be "3 1 0".
			TRUtils.nextLine( tokenizer );

			//  Retrieve the min, max and delta values for this independent.
			float min = (float)TRUtils.nextNumber(tokenizer);
			float max = (float)TRUtils.nextNumber(tokenizer);
			float delta = (float)TRUtils.nextNumber(tokenizer);
			TRUtils.nextLine( tokenizer );
			TRUtils.nextLine( tokenizer );
			
			indepMin[idx] = min;
			indepMax[idx] = max;
			indepDelta[idx] = delta;
        }
		

		//  Create names for independent variables.
		String[] indepLAS = new String[2];
		indepLAS[0] = kFuselage2DUi[4];		//	FAOAT3
		indepLAS[1] = kFuselage2DUi[2];		//	FAOST1
		String[] indepHAS = new String[2];
		indepHAS[0] = kFuselage2DUi[4];		//	FAOAT3
		indepHAS[1] = kFuselage2DUi[3];		//	FAOST2
		
		//	Extract range data.
		float faoat1max = indepMax[0];
		float faoat1min = indepMin[0];
		float faoat1delta = indepDelta[0];
		float faoat2max = indepMax[1];
		float faoat2min = indepMin[1];
		float faoat2delta = indepDelta[1];
		float faost1max = indepMax[2];
		float faost1min = indepMin[2];
		float faost1delta = indepDelta[2];
		float faost2max = indepMax[3];
		float faost2min = indepMin[3];
		float faost2delta = indepDelta[3];
		float faoat3max = indepMax[4];
		float faoat3min = indepMin[4];
		float faoat3delta = indepDelta[4];
		
		//  Create breakpoint tables for independent variables.
		int numBPfaoat1 = (int)((faoat1max - faoat1min)/faoat1delta) + 1;
		int numBPfaoat2 = (int)((faoat2max - faoat2min)/faoat2delta) + 1;
		int numBPfaoat3 = (int)((faoat3max - faoat3min)/faoat3delta) + 1;
		int numBPfaost1 = (int)((faost1max - faost1min)/faost1delta) + 1;
		int numBPfaost2 = (int)((faost2max - faost2min)/faost2delta) + 1;
		
		float[] bpLA = new float[numBPfaoat1];
		float[] bpHA = new float[numBPfaoat2];
		float[] bpLS = new float[numBPfaost1];
		float[] bpHS = new float[numBPfaost2];
		float[][] bpLAS = new float[2][];
		bpLAS[0] = new float[numBPfaoat3];
		bpLAS[1] = new float[numBPfaost1];
		float[][] bpHAS = new float[2][];
		bpHAS[0] = new float[numBPfaoat3];
		bpHAS[1] = new float[numBPfaost2];
		
		for (int i=0; i < numBPfaoat3; ++i) {
		    float value = faoat3min + faoat3delta*i;
		    bpLAS[0][i] = value;
		    bpHAS[0][i] = value;
		}
		for (int i=0; i < numBPfaost1; ++i)
		    bpLAS[1][i] = faost1min + faost1delta*i;
		for (int i=0; i < numBPfaost2; ++i)
		    bpHAS[1][i] = faost2min + faost2delta*i;

		for (int i=0; i < numBPfaoat1; ++i)
		    bpLA[i] = faoat1min + faoat1delta*i;

		for (int i=0; i < numBPfaoat2; ++i)
		    bpHA[i] = faoat2min + faoat2delta*i;

		for (int i=0; i < numBPfaost1; ++i)
		    bpLS[i] = faost1min + faost1delta*i;

		for (int i=0; i < numBPfaost2; ++i)
		    bpHS[i] = faost2min + faost2delta*i;

        //  Create an array to contain the tables to be read in (18 tables).
		FloatTable[] tables = new FloatTable[18];


		//	Read in all the tables one after another.
		found = new boolean[18];
		for (int i=0; i < 18; ++i) {
		
			//  Get the next token (should be a dependent table name).
			tokenizer.eolIsSignificant( false );
			String tblName = TRUtils.nextWord(tokenizer);

			//	Did we find a valid dependent table name?
			int idx = idx2str(tblName, kFuselage2DU, found, "Unexpected dependent table name in SAV file.");

			//	Deal with 1D and 2D tables a little differently.
			if (kFuselage2DUdim[idx] == 1) {
			
				String indepName = kFuselage2DUti[idx];
				float indepBP[] = null;
				if (indepName.endsWith("AT1"))
					indepBP = bpLA;
				else if (indepName.endsWith("AT2"))
					indepBP = bpHA;
				else if (indepName.endsWith("ST1"))
					indepBP = bpLS;
				else if (indepName.endsWith("ST2"))
					indepBP = bpHS;
				
				//  Create the table without any dependent data.
				FloatTable table = new FloatTable(tblName, indepName, indepBP, null);
				
				//  Put table into array of tables.
				tables[idx] = table;
				
				//  Read in the data for this table.
				read1DTableData(tokenizer, tblName, indepName, indepBP.length, table);
				
			} else {
				//	Must be a 2D table.
				
				String indepName = kFuselage2DUti[idx];
				float indepBP[][] = null;
				String[] indepNames = null;
				if (indepName.endsWith("ST1")) {
					indepBP = bpLAS;
					indepNames = indepLAS;
				} else if (indepName.endsWith("ST2")) {
					indepBP = bpHAS;
					indepNames = indepHAS;
				}
				
				FloatTable table = new FloatTable(tblName, indepNames, indepBP, null);
				tables[idx] = table;
				read2DTableData(tokenizer, tblName, indepName, indepBP[1].length, indepBP[0].length, table);
			}
		}

		// Create a new table database.
		FTableDatabase db = new FTableDatabase( tables );

		return db;
	}


	/**
	*  Method that searches through an array of strings and returns the index of
	*  the string matching the input string.
	*
	*  @param str    The string we are looking for.
	*  @param list   The list of strings to search through.
	*  @param found  A list of booleans that indicate if the particular string has already
	*                been found (and so shouldn't be found again).
	*  @param  msg   The error message to place in the exception thrown if the string is
	*                not found.
	*  @return The index in the list of the input string.  If the string is not found,
	*          an exception is thrown.
	**/
	private static int idx2str(String str, String[] list, boolean[] found, String msg) throws IOException {
	
		int length = list.length;
		int j;
		for (j=0; j < length; ++j) {
			if (!found[j] && str.equals(list[j])) {
				found[j] = true;
				break;
			}
		}
		
		if (j >= length) {
			StringBuffer buffer = new StringBuffer(msg);
			buffer.append("\nFound \"");
			buffer.append(str);
			buffer.append("\" when looking for");
			for (j=0; j < length; ++j) {
				if (!found[j]) {
					if (j != 0)	buffer.append(",");
					buffer.append(" ");
					buffer.append(list[j]);
				}
			}
			buffer.append(".");
			throw new IOException(	buffer.toString() );
		}

		return j;
	}
	
    
	/**
	*  Method that reads in the data for a single 1D table from the SAV file.
	**/
	private static void read1DTableData(StreamTokenizer tokenizer, String tblName, String aoaName,
	                        int numAOABP, FloatTable table) throws IOException {

 		//  Read in the parameter count to make sure it jives.
        int count = (int)TRUtils.nextNumber(tokenizer);
        if (count != numAOABP)
            throw new IOException("Inconsistant number of breakpoints for " + tblName + " and " + aoaName + ".");
        count = (int)TRUtils.nextNumber(tokenizer);
        if (count != 1)
            throw new IOException("Wrong number of dimensions for " + tblName + "; expected 1.");

        //  Skip to the next line.
		tokenizer.eolIsSignificant( true );
		TRUtils.nextLine( tokenizer );
		tokenizer.eolIsSignificant( false );

        //  Read in the dependent data.
		int[] pos = new int[1];
		for (int j=0; j < numAOABP; ++j) {
		    pos[0] = j;
            double value = TRUtils.nextNumber(tokenizer);
            table.set(pos, (float)value);
		}

	}
	

	/**
	*  Method that reads in the data for a single 2D table from the SAV file.
	**/
	private static void read2DTableData(StreamTokenizer tokenizer, String tblName, String aoaName,
	                        int numAOABP, int numMachBP, FloatTable table) throws IOException {

 		//  Read in the parameter count to make sure it jives.
        int count = (int)TRUtils.nextNumber(tokenizer);
        if (count != numAOABP)
            throw new IOException("Inconsistant number of breakpoints for " + tblName + " and " + aoaName + ".");
        count = (int)TRUtils.nextNumber(tokenizer);
        if (count != numMachBP)
            throw new IOException("Inconsistant number of breakpoints for " + tblName + " and MACHT or FAOAT3.");

        //  Go to next line.
		tokenizer.eolIsSignificant( true );
		TRUtils.nextLine( tokenizer );
		tokenizer.eolIsSignificant( false );

        //  Read in the dependent data.
		int[] pos = new int[2];
		for (int i=0; i < numMachBP; ++i) {
            pos[0] = i;
		    for (int j=0; j < numAOABP; ++j) {
		        pos[1] = j;
                double value = TRUtils.nextNumber(tokenizer);
                table.set(pos, (float)value);
		    }
		}

	}
	

	/**
	*  Check the tables that are to be written out to make sure that
	*  they meet the requirements of either a 2D airfoil data table set
	*  or a fuselage 2D uniform table set.
	*
	*  @param  tables  A collection of tables that we are writing out.
	*  @return The type of the table set we are dealing with.  A value
	*          of 0 is returned if the tables do not meet any of our
	*          SAV sub-type format requirements.
	**/
	private static int checkTables( FTableDatabase tables ) throws IOException {
	    int type = kUnknown;
	    
        int size = tables.size();
        if (size == 6) {
            //  We may have an airfoil high/low table set.

            //  Build up an ordered array of tables with the correct names.
            //  If the properly named table can't be found, bail out.
            FloatTable[] tblArray = new FloatTable[6];
            for (int i=0; i < 6; ++i) {
                FloatTable tbl = tables.get(kAFLowHigh[i]);
                if (tbl == null)    return kUnknown;
                tblArray[i] = tbl;
            }

            //  Make sure the 3 low alpha tables have the correct independents.
            for (int i=0; i < 3; ++i) {
                FloatTable tbl = tblArray[i];
                if (tbl.dimensions() != 2)  return kUnknown;
                String[] indeps = tbl.getIndepNames();
                if ( !indeps[0].equals("MACHT") || !indeps[1].equals("AOATL") )
                    return kUnknown;
            }

            //  Make sure the 3 high alpha tables have the correct independents.
            for (int i=3; i < 6; ++i) {
                FloatTable tbl = tblArray[i];
                if (tbl.dimensions() != 1)  return kUnknown;
                String[] indeps = tbl.getIndepNames();
                if ( !indeps[0].equals("AOATH") )   return kUnknown;
            }

            //  We must have an airfoil low/high alpha table set.
            type = kHighLowAirfoil;

            
        } else if (size == 18) {
            //  We may have a fuselage 2D uniform table set.
            
            //  Make sure we have all the required tables.
            for (int i=0; i < 18; ++i) {
                if ( !tables.containsName(kFuselage2DU[i]) )
                    return kUnknown;
            }

            //  Check the independents of each table.
            String[] indeps = null;
            FloatTable tbl = null;
            
            tbl = tables.get("CLFAL");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT1") )  return kUnknown;

            tbl = tables.get("CLFAH");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT2") )  return kUnknown;

            tbl = tables.get("CLFBAL");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT3") || !indeps[1].equals("FAOST1") )  return kUnknown;

            tbl = tables.get("CLFBAH");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT3") || !indeps[1].equals("FAOST2") )  return kUnknown;

            tbl = tables.get("CDFAL");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT1") )  return kUnknown;

            tbl = tables.get("CDFAH");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT2") )  return kUnknown;

            tbl = tables.get("CMFAL");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT1") )  return kUnknown;

            tbl = tables.get("CDFAH");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT2") )  return kUnknown;

            tbl = tables.get("CDFBL");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOST1") )  return kUnknown;

            tbl = tables.get("CDFBH");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOST2") )  return kUnknown;

            tbl = tables.get("CMFBAL");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT3") || !indeps[1].equals("FAOST1") )  return kUnknown;

            tbl = tables.get("CMFBAH");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT3") || !indeps[1].equals("FAOST2") )  return kUnknown;

            tbl = tables.get("CYFBAL");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT3") || !indeps[1].equals("FAOST1") )  return kUnknown;

            tbl = tables.get("CYFBAH");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT3") || !indeps[1].equals("FAOST2") )  return kUnknown;

            tbl = tables.get("CNFBAL");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT3") || !indeps[1].equals("FAOST1") )  return kUnknown;

            tbl = tables.get("CNFBAH");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT3") || !indeps[1].equals("FAOST2") )  return kUnknown;

            tbl = tables.get("CRFBAL");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT3") || !indeps[1].equals("FAOST1") )  return kUnknown;

            tbl = tables.get("CRFBAH");
            indeps = tbl.getIndepNames();
            if ( !indeps[0].equals("FAOAT3") || !indeps[1].equals("FAOST2") )  return kUnknown;

            //  We must have a fuselage 2D uniform table set.
            type = kFuselage2DUniform;

        }
 	    
        return type;
	}


	/**
	*  Write out a FlightLab SAV airfoil table with low/high alpha uniform increment
	*  formatted multi-table stream and create a new table database.
	*
	*  @param   output  A buffered output stream writer to the output file.
	*  @param   tabels  The database of tabels to be written out to the file.
	**/
	private static void writeHighLowAirfoil( BufferedWriter output, FTableDatabase tables ) throws IOException {

		//	Create an exponential number format for the numbers we are writing out.
		ExponentialFormat nf = new ExponentialFormat();
		nf.setMaximumFractionDigits(16);
		nf.setMinimumFractionDigits(16);
		nf.setMaximumIntegerDigits(1);
		nf.setMinimumExponentDigits(2);
		
		//	Write out the comments to the header of the file.
		writeFileComments(output, tables);
		
		//	Write out the names of the tables.
		output.write("#Contents:"); output.newLine();
		output.write("# AOATL");	output.newLine();
		output.write("# AOATH");	output.newLine();
		output.write("# MACHT");	output.newLine();

		for (int i=0; i < 6; ++i) {
			output.write("# " + kAFLowHigh[i]);
			output.newLine();
		}

		
		//	Write out the info for the AOATL independent variable.
		FloatTable table = tables.get("CLL");
		float[] bp = table.getBreakpoints(1);
		double min = bp[0];
		double max = bp[bp.length-1];
		double step = bp[1] - bp[0];
		output.write("AOATL 3 1 0");
		output.newLine();
		output.write(formatNumber(24,nf,min) + formatNumber(25,nf,max) + formatNumber(25,nf,step));
		output.newLine();
		output.newLine();
		
		//	Write out the info for the AOATH independent variable.
		table = tables.get("CLH");
		bp = table.getBreakpoints(0);
		min = bp[0];
		max = bp[bp.length-1];
		step = bp[1] - bp[0];
		output.write("AOATH 3 1 0");
		output.newLine();
		output.write(formatNumber(24,nf,min) + formatNumber(25,nf,max) + formatNumber(25,nf,step));
		output.newLine();
		output.newLine();
		
		//	Write out the info for the MACHT independent variable.
		table = tables.get("CLL");
		bp = table.getBreakpoints(0);
		min = bp[0];
		max = bp[bp.length-1];
		step = bp[1] - bp[0];
		output.write("MACHT 3 1 0");
		output.newLine();
		output.write(formatNumber(24,nf,min) + formatNumber(25,nf,max) + formatNumber(25,nf,step));
		output.newLine();
		output.newLine();
		
		//	Write out the CLL table.
		table = tables.get("CLL");
		write2DTable(table, nf, output);
		
		//	Write out the CDL table.
		table = tables.get("CDL");
		write2DTable(table, nf, output);
		
		//	Write out the CML table.
		table = tables.get("CML");
		write2DTable(table, nf, output);
		
		//	Write out the CLH table.
		table = tables.get("CLH");
		write1DTable(table, nf, output);
		
		//	Write out the CDH table.
		table = tables.get("CDH");
		write1DTable(table, nf, output);
		
		//	Write out the CMH table.
		table = tables.get("CMH");
		write1DTable(table, nf, output);
		
	}


    /**
    *  Write out a FlightLab SAV fuselage 2D uniform increment
	*  formatted multi-table stream and create a new table database.
	*
	*  @param   output  A buffered output stream writer to the output file.
	*  @param   tabels  The database of tabels to be written out to the file.
	**/
	private static void writeFuselage2DUniform( BufferedWriter output, FTableDatabase tables ) throws IOException {

		//	Create an exponential number format for the numbers we are writing out.
		ExponentialFormat nf = new ExponentialFormat();
		nf.setMaximumFractionDigits(16);
		nf.setMinimumFractionDigits(16);
		nf.setMaximumIntegerDigits(1);
		nf.setMinimumExponentDigits(2);

		
		//	Write out the comments to the header of the file.
		writeFileComments(output, tables);

		
		//	Write out the names of the tables.
		output.write("#Contents:"); output.newLine();
		output.write("# FAOAT1");	output.newLine();
		output.write("# FAOAT2");	output.newLine();
		output.write("# FAOAT3");	output.newLine();
		output.write("# FAOST1");	output.newLine();
		output.write("# FAOST2");	output.newLine();

		for (int i=0; i < 18; ++i) {
			output.write("# " + kFuselage2DU[i]);
			output.newLine();
		}

		
		//	Write out the info for the FAOAT1 independent variable.
		FloatTable table = tables.get("CLFAL");
		float[] bp = table.getBreakpoints(0);
		double min = bp[0];
		double max = bp[bp.length-1];
		double step = bp[1] - bp[0];
		output.write("FAOAT1 3 1 0");
		output.newLine();
		output.write(formatNumber(24,nf,min) + formatNumber(25,nf,max) + formatNumber(25,nf,step));
		output.newLine();
		output.newLine();
		
		//	Write out the info for the FAOAT2 independent variable.
		table = tables.get("CLFAH");
		bp = table.getBreakpoints(0);
		min = bp[0];
		max = bp[bp.length-1];
		step = bp[1] - bp[0];
		output.write("FAOAT2 3 1 0");
		output.newLine();
		output.write(formatNumber(24,nf,min) + formatNumber(25,nf,max) + formatNumber(25,nf,step));
		output.newLine();
		output.newLine();
		
		//	Write out the info for the FAOAT3 independent variable.
		table = tables.get("CLFBAH");
		bp = table.getBreakpoints(0);
		min = bp[0];
		max = bp[bp.length-1];
		step = bp[1] - bp[0];
		output.write("FAOAT3 3 1 0");
		output.newLine();
		output.write(formatNumber(24,nf,min) + formatNumber(25,nf,max) + formatNumber(25,nf,step));
		output.newLine();
		output.newLine();
		
		//	Write out the info for the FAOST1 independent variable.
		table = tables.get("CDFBL");
		bp = table.getBreakpoints(0);
		min = bp[0];
		max = bp[bp.length-1];
		step = bp[1] - bp[0];
		output.write("FAOST1 3 1 0");
		output.newLine();
		output.write(formatNumber(24,nf,min) + formatNumber(25,nf,max) + formatNumber(25,nf,step));
		output.newLine();
		output.newLine();
		
		//	Write out the info for the FAOST2 independent variable.
		table = tables.get("CDFBH");
		bp = table.getBreakpoints(0);
		min = bp[0];
		max = bp[bp.length-1];
		step = bp[1] - bp[0];
		output.write("FAOST2 3 1 0");
		output.newLine();
		output.write(formatNumber(24,nf,min) + formatNumber(25,nf,max) + formatNumber(25,nf,step));
		output.newLine();
		output.newLine();
		
		//	Write out the CDFAH table.
		table = tables.get("CDFAH");
		write1DTable(table, nf, output);
		
		//	Write out the CDFAL table.
		table = tables.get("CDFAL");
		write1DTable(table, nf, output);
		
		//	Write out the CDFBH table.
		table = tables.get("CDFBH");
		write1DTable(table, nf, output);
		
		//	Write out the CDFBL table.
		table = tables.get("CDFBL");
		write1DTable(table, nf, output);
		
		//	Write out the CLFAH table.
		table = tables.get("CLFAH");
		write1DTable(table, nf, output);
		
		//	Write out the CLFAL table.
		table = tables.get("CLFAL");
		write1DTable(table, nf, output);
		
		//	Write out the CLFBAH table.
		table = tables.get("CLFBAH");
		write2DTable(table, nf, output);
		
		//	Write out the CLFBAL table.
		table = tables.get("CLFBAL");
		write2DTable(table, nf, output);
		
		//	Write out the CMFAL table.
		table = tables.get("CMFAL");
		write1DTable(table, nf, output);
		
		//	Write out the CMFAH table.
		table = tables.get("CMFAH");
		write1DTable(table, nf, output);
		
		//	Write out the CMFBAH table.
		table = tables.get("CMFBAH");
		write2DTable(table, nf, output);
		
		//	Write out the CMFBAL table.
		table = tables.get("CMFBAL");
		write2DTable(table, nf, output);
		
		//	Write out the CNFBAH table.
		table = tables.get("CNFBAH");
		write2DTable(table, nf, output);
		
		//	Write out the CNFBAL table.
		table = tables.get("CNFBAL");
		write2DTable(table, nf, output);
		
		//	Write out the CRFBAH table.
		table = tables.get("CRFBAH");
		write2DTable(table, nf, output);
		
		//	Write out the CRFBAL table.
		table = tables.get("CRFBAL");
		write2DTable(table, nf, output);
		
		//	Write out the CYFBAH table.
		table = tables.get("CYFBAH");
		write2DTable(table, nf, output);
		
		//	Write out the CYFBAL table.
		table = tables.get("CYFBAL");
		write2DTable(table, nf, output);
				
	}


	/**
	*  Write out any database notes as comments to the header of the file.
	*
	*  @param  output  A writer used to output the data.
	*  @param  tables  The table database we are writing out.
	**/
	private static void writeFileComments( BufferedWriter output, FTableDatabase tables ) throws IOException {

		// Write out the table notes as the header of the file.
		int numNotes = tables.numberOfNotes();
		for ( int i = 0; i < numNotes; ++i ) {
			String note = tables.getNote( i );
			output.write( "# " + note );
			output.newLine();
		}

		// Write out some generic table comments into the header lines.
		output.write("#.dat");
		output.newLine();
		output.write( "# Date:, " );
		Date theDate = new Date();
		output.write( DateFormat.getDateInstance( DateFormat.SHORT ).format( theDate ) );
		output.write( ", " );
		output.write( DateFormat.getTimeInstance().format( theDate ) );
		output.newLine();
		output.write( "# File created by TableReader." );
		output.newLine();
			
	}


	/**
	*  Method that writes out a single 1D table to a file.
	*
	*  @param  table  The table to be written out (must be 1D).
	*  @param  nf     The NumberFormat object used to format the output.
	*  @param  output A writer that writes out the data to a file.
	**/
	private static void write1DTable(FloatTable table, NumberFormat nf, BufferedWriter output) throws IOException {
		
		String tblName = table.getTableName();
		int numBP = table.getNumBreakpoints(0);
		output.write(tblName + " " + numBP + " 1 0");
		output.newLine();
		
		int[] pos = new int[1];
		int count=0;
		for (int j=0; j < numBP; ++j) {
			pos[0] = j;
			float value = table.get(pos);
			output.write(formatNumber(24, nf, value));
			++count;
			if (count == 3) {
				count = 0;
				output.newLine();
			} else
				output.write(" ");
		}
		output.newLine();
//		if (count != 0)
//			output.newLine();
	}
	
	
	/**
	*  Method that writes out a single 2D table to a file.
	*
	*  @param  table  The table to be written out (must be 2D).
	*  @param  nf     The NumberFormat object used to format the output.
	*  @param  output A writer that writes out the data to a file.
	**/
	private static void write2DTable(FloatTable table, NumberFormat nf, BufferedWriter output) throws IOException {
		
		String tblName = table.getTableName();
		int numBP1 = table.getNumBreakpoints(1);
		int numBP2 = table.getNumBreakpoints(0);
		output.write(tblName + " " + numBP1 + " " + numBP2 + " 0");
		output.newLine();
		
		int count=0;
		int[] pos = new int[2];
		for (int i=0; i < numBP2; ++i) {
			pos[0] = i;
			for (int j=0; j < numBP1; ++j) {
				pos[1] = j;
				float value = table.get(pos);
				output.write(formatNumber(24, nf, value));
				++count;
				if (count == 3) {
					count = 0;
					output.newLine();
				} else
					output.write(" ");
			}
		}
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
	private static String formatNumber( int size, NumberFormat nf, double number ) {
	    //  Remove a bunch of extraneous digits from the float's converted to doubles
	    //  by rounding them properly.
	    number = MathTools.roundToPlace(number, -8);
	    
		StringBuffer buffer = new StringBuffer( nf.format( number ) );
		TRUtils.addSpaces( size, buffer );
		return buffer.toString();
	}

}


