/*
 *   GGPGeomReader  -- A class that can read a GGP formatted geometry file.
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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import standaloneutils.aerotools.util.ExponentialFormat;

/**
 * An class for reading a vehicle geometry from a GGP formatted geometry file. This is a
 * geometry file format often used in conjunction with solution output from A502 (PANAIR)
 * and A633 (TRANAIR). This class will only read in parameters from the GGP file with the
 * names "X", "Y", and "Z". All other parameters are ignored. This class also assumes that
 * all the parameters are contained in a single line and that the 1st format is duplicated
 * throughout the file.
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: May 11, 2000
 * @version April 3, 2014
 */
public class GGPGeomReader implements GeomReader {

    // The units used for the geometry in the specified file.
    private Unit<Length> units = Length.UNIT;
    
    // Define the comment characters that are possible.
    private static final String[] commentChars = {"*", "$", "("};
    
    /**
     * A description of the format of the arrays in the GGP file.
     */
    private ArrayList formatLst = new ArrayList();
    
    /**
     * A number format that can be used to parse and format exponential numbers.
     */
    private NumberFormat nformat = new ExponentialFormat();
    

    /**
     * Method that reads in a GGP formatted geometry file from the specified input stream
     * and returns a configuration geometry object that contains the geometry from the GGP
     * file. A GGP file does not support multiple vehicles or multiple components.
     * Therefore, the configuration returned by this method will always contain only a
     * single vehicle with a single component.
     *
     * @param inStream The input stream to geometry file that we are reading from.
     * @param name The name of the geometry being read in (null for no name).
     * @return A configuration geometry object containing the geometry read in from the
     * file.
     */
    @Override
    public GeomConfig read(InputStream inStream, String name) throws IOException {
        GeomConfig config = null;

        // Create a reader to access the ASCII file.
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(inStream));
        reader.mark(8192);

        // Start by parsing the "format" line (must be 1st line if it exists).
        String aLine = reader.readLine();
        parseFormatString(aLine);
        reader.reset();

        // Read in the networks.
        GeomComponent comp = readNetworks(reader);


        // If something was read in, create a configuration object for it.
        if (comp.size() != 0) {
            // Create a vehicle with the filename for the vehicle name.
            GeomVehicle vehicle = new GeomVehicle(name);
            vehicle.add(comp);

            // Now create the configuration containing this vehicle.
            config = new GeomConfig(name);
            config.add(vehicle);
        }

        return config;
    }

    /**
     * Method that writes out a GGP formatted geometry file for the geometry contained in
     * the supplied configuration geometry object.
     *
     * WARNING! This method has not yet been implemented and will throw an exception if
     * you try and use it.
     *
     * @param filename The name of the GGP geometry file to be written out.
     * @param theConfig The configuration geometry to be written out.
     */
    @Override
    public void write(File filename, GeomConfig theConfig)
            throws IOException {
        throw new IOException("Writing GGP files has not yet been implemented.");
    }

    /**
     * Method that specifies the units used for the geometry as stored in the file. The
     * geometry is converted from these units to the reference length units (meters) when
     * it is read in and converted from reference units to these units when written out.
     * If the units are not specified, then it is assumed that the data has units of
     * meters in the file.
     *
     * @param units The units used for the geometry in the disk file. If null is passed,
     * the units will default to the reference length units (meters).
     */
    @Override
    public void setFileUnits(Unit<Length> units) {
        if (units == null)
            this.units = Length.UNIT;
        this.units = units;
    }

    /**
     * Parse the format line of the GGP file. The format line is always the 1st line and
     * must contain a FORTRAN style number format declaration such as (3G15.7) or
     * (F5.0,7F13.4). All that this reader cares about is how many columns there are and
     * how many characters are contained in each column. This method sets a class variable
     * "formatLst" such that each entry corresponds to a column and each entry records how
     * many characters are in each column.
     *
     * @param formatStr A string containing the format statement.
     * @throws IOException if unable to parse the format line.
     */
    private void parseFormatString(String formatStr) throws IOException {
        formatStr = formatStr.trim();

        if (formatStr.startsWith("(")) {
            try {

                // Search for commas that separate different formats
                int start = 1;
                int end = formatStr.indexOf(",", start);
                while (end != -1) {
                    String subStr = formatStr.substring(start, end);
                    parseSingleFormat(subStr);
                    start = end + 1;
                    end = formatStr.indexOf(",", start);
                }
                parseSingleFormat(formatStr.substring(start, formatStr.length() - 1));

            } catch (NumberFormatException e) {
                throw new IOException("Unable to parse format line.");
            }


        } else {

            // Create the default format list if one isn't provided.
            for (int i = 0; i < 3; ++i) {
                formatLst.add(new Integer(15));
            }
        }
    }

    /**
     * Parse a single FORTRAN style number format declaration such as "3G15.7" or "F5.0"
     * or "7F13.4". All that this reader cares about is how many columns there are and how
     * many characters are contained in each column. This method sets a class variable
     * "formatLst" such that each entry corresponds to a column and each entry records how
     * many characters are in each column.
     *
     * @param formatStr A string containing the format statement.
     * @throws NumberFormatException if unable to parse the format declaration.
     */
    private void parseSingleFormat(String formatStr) throws NumberFormatException {
        int nCol;
        formatStr = formatStr.trim();

        // Convert string to a character array.
        char[] array = formatStr.toCharArray();
        int length = array.length;

        // Extract the number of columns using this format.
        int pos1 = 0;
        int pos2 = 0;
        while (Character.isDigit(array[pos2])) {
            ++pos2;
        }

        if (pos2 == pos1)
            nCol = 1;
        else
            nCol = Integer.parseInt(formatStr.substring(pos1, pos2));

        // Skip the format type (F,G, or E).
        pos1 = pos2 + 1;

        // Extract the number of characters per column.
        pos2 = pos1;
        while (Character.isDigit(array[pos2])) {
            ++pos2;
        }

        if (pos2 < pos1 + 1)
            throw new NumberFormatException();

        Integer nChar = Integer.valueOf(formatStr.substring(pos1, pos2));

        // Create list of formats, one for each column.
        for (int i = 0; i < nCol; ++i) {
            formatLst.add(nChar);
        }

    }

    /**
     * Determines if this line is a comment line.
     *
     * @param aLine The line to be tested.
     * @return true if the line is a comment line, false if it is not. If aLine is null,
     * false is returned.
     */
    private boolean isComment(String aLine) {
        boolean retVal = false;

        if (aLine != null) {
            aLine = aLine.trim();
            int length = commentChars.length;
            for (int i = 0; i < length; ++i) {
                if (aLine.startsWith(commentChars[i]) && !aLine.startsWith("*EOD")
                        && !aLine.startsWith("*EOF")) {
                    retVal = true;
                    break;
                }
            }
        }

        return retVal;
    }

    /**
     * Parse the network ID from the string name line. The network ID is a letter/number
     * combination that uniquely identifies the network that this string of points belongs
     * to. Immediately following the network ID is the string ID which this program
     * ignores.
     *
     * @param aLine The line containing the net/string ID.
     * @param The network ID is returned.
     */
    private String parseNetID(String aLine) {

        if (aLine == null)
            return null;

        aLine = aLine.trim();
        char[] array = aLine.toCharArray();
        int length = array.length;

        // Find end of letter part of ID.
        int pos = 0;
        while (pos < length) {
            if (Character.isDigit(array[pos]))
                break;
            ++pos;
        }

        // Find end of number part of ID.
        ++pos;
        while (pos < length) {
            if (!Character.isDigit(array[pos])) {
                ++pos;
                break;
            }
            ++pos;
        }

        // Extract the ID string.
        return aLine.substring(0, pos - 1);
    }

    /**
     * Parse the optional network name from the string name line. The network name is
     * different from the network ID. The optional network name comes after the network ID
     * and string ID and is separated from them by at least one space.
     *
     * @param aLine The line containing the string & network name.
     * @return A string containing the optional network name or null if that name is not
     * found.
     */
    private String parseOptionalNetName(String aLine) {
        aLine = aLine.trim();
        int pos = aLine.lastIndexOf(" ");

        String retVal = null;
        if (pos > 0)
            retVal = aLine.substring(pos);

        // Extract the ID string.
        return retVal;
    }

    /**
     * Read in lines from the GGP file while skipping comment lines.
     *
     * @param reader The reader for the GGP file.
     * @return The 1st line that is not a comment line.
     */
    private String readSkippingComments(LineNumberReader reader) throws IOException {
        String aLine = reader.readLine();
        while (isComment(aLine)) {
            aLine = reader.readLine();
        }

        return aLine;
    }

    /**
     * Parse the list of parameters.
     *
     * @param reader The reader for our GGP file.
     * @return An array of strings where each element is a parameter in this GGP file.
     */
    private String[] parseParamNames(LineNumberReader reader) throws IOException {

        String aLine = readSkippingComments(reader).trim();
        int nParams = formatLst.size();
        String[] array = new String[nParams];
        StringTokenizer tokenizer = new StringTokenizer(aLine);

        // Extract one token (param name) for each element idendtified
        // in the format line.
        for (int i = 0; i < nParams; ++i) {
            if (!tokenizer.hasMoreTokens()) {
                aLine = readSkippingComments(reader).trim();
                tokenizer = new StringTokenizer(aLine);

            }
            String token = tokenizer.nextToken();
            array[i] = token;
        }

        return array;
    }

    /**
     * Identify which parameter in the parameter string matches the target parameter.
     *
     * @param nameList The array of parameter names.
     * @param target The target parameter that we are looking for.
     * @return The data column corresponding the the target parameter. If the parameter
     * could not be found, -1 is returned.
     */
    private int findParam(String[] nameList, String target) {
        int length = nameList.length;
        int pos = 0;
        while (pos < length) {
            if (nameList[pos].equals(target))
                break;
            ++pos;
        }

        if (pos == length)
            pos = -1;

        return pos;
    }

    /**
     * Extracts the portion of the string indicated by the offsets, and parses that string
     * into a floating point number.
     *
     * @param buffer The string buffer containing the number.
     * @param offset1 The index into the string buffer of the start of the number.
     * @param offset2 The index into the string of the 1st character after the end of the
     * number.
     * @return The floating point number parsed from the string.
     * @throws ParseException if an error occurred while parsing.
     */
    private double parseNumber(String buffer, int offset1, int offset2)
            throws ParseException {
        if (offset2 > buffer.length())
            offset2 = buffer.length();
        if (offset2 <= offset1)
            throw new ParseException("Bad index range.", offset1);

        String subStr = buffer.substring(offset1, offset2).trim();
        double value = nformat.parse(subStr).doubleValue();

        // Do unit conversion if necissary.
        if (units != null) {
            UnitConverter convertToRef = units.toStandardUnit();
            value = convertToRef.convert(value);
        }

        return value;
    }

    /**
     * Read a string of points from the GGP file. A string of points consists of the
     * parameters read in, 1 point per line, until the *EOD record is encountered. This
     * version of this method does not require the string to be of any length. Data is
     * read in until the *EOD card is reached.
     *
     * @param reader The reader for the GGP file.
     * @param columns An array containing the indices of the data columns (parameters) to
     * be read in.
     * @return An array of arrays where the sub-arrays are the parameters read in for each
     * point and the overall array is a list of points.
     */
    private double[][] readString(LineNumberReader reader, int[] columns)
            throws IOException {

        ArrayList<double[]> data = new ArrayList();

        try {
            int nParams = formatLst.size();

            // Read a line of data.
            String aLine = readSkippingComments(reader);

            // If *EOD, then the string is finished.
            String trimLine = aLine.trim();
            while (!trimLine.equals("*EOD") && !trimLine.equals("*EOF")) {
                double[] point = new double[columns.length];

                // Loop over the parameters in the file.
                int off1 = 0;
                int pos = 0;
                while (pos < nParams) {
                    // Get the number of characters in this number from
                    // the parsed format data.
                    int nChar = ((Integer) (formatLst.get(pos))).intValue();
                    int off2 = off1 + nChar;

                    // Check if this parameter is one of the columns requested.
                    for (int i = 0; i < columns.length; ++i) {
                        if (columns[i] == pos) {

                            // This one is requested, so parse it.
                            point[i] = parseNumber(aLine, off1, off2);
                            break;
                        }
                    }
                    off1 = off2;
                    ++pos;
                }

                // Add this point to the data read in so far.
                data.add(point);

                // Read the next line.
                aLine = readSkippingComments(reader);
                trimLine = aLine.trim();
            }

        } catch (ParseException e) {
            throw new IOException("Unable to parse GGP file on line #"
                    + reader.getLineNumber() + ".");
        }

        // The string has been read in, convert it to a data array.
        int size = data.size();
        double[][] outData = new double[size][];
        for (int i = 0; i < size; ++i) {
            outData[i] = data.get(i);
        }

        return outData;
    }

    /**
     * Read a string of points from the GGP file. A string of points consists of the
     * parameters read in, 1 point per line, until the *EOD record is encountered. This
     * version of this method, for efficiency's sake, requires that the string of points
     * be a specified length. This method will not check for the *EOD card, but assumes
     * that it comes after "length" number of points.
     *
     * @param reader The reader for the GGP file.
     * @param columns An array containing the indices of the data columns (parameters) to
     * be read in.
     * @param length The length of the string of points (the number of points).
     * @return An array of arrays where the sub-arrays are the parameters read in for each
     * point and the overall array is a list of points.
     */
    private double[][] readFixedSizeString(LineNumberReader reader, int[] columns,
            int length) throws IOException {

        double[][] data = new double[length][];

        try {
            int nParams = formatLst.size();
            int dpos = 0;

            // Read a line of data.
            String aLine = readSkippingComments(reader);

            // Strings are supposed to be the specified size.
            while (dpos < length) {
                double[] point = new double[columns.length];

                // Loop over the parameters in the file.
                int off1 = 0;
                int pos = 0;
                while (pos < nParams) {
                    int nChar = ((Integer) (formatLst.get(pos))).intValue();
                    int off2 = off1 + nChar;

                    // Check if this parameter is one of the columns requested.
                    for (int i = 0; i < columns.length; ++i) {
                        if (columns[i] == pos) {

                            // This one is requested, so parse it.
                            point[i] = parseNumber(aLine, off1, off2);
                            break;
                        }
                    }
                    off1 = off2;
                    ++pos;
                }

                // Add this point to the data read in so far.
                data[dpos++] = point;

                // Read the next line.
                aLine = readSkippingComments(reader);
            }

        } catch (ParseException e) {
            throw new IOException("Unable to parse GGP file on line #"
                    + reader.getLineNumber() + ".");
        }


        return data;
    }

    /**
     * Read in all the networks in this geometry file.
     *
     */
    private GeomComponent readNetworks(LineNumberReader reader) throws IOException {

        // Create a component object to contain the networks read in.
        GeomComponent comp = new GeomComponent("Untitled");


        // Read until we get something that is not a comment.
        String aLine = readSkippingComments(reader);

        // Read in the network ID and optional name.
        String netID = parseNetID(aLine);

        // Parse the parameter name line.
        String[] paramNames = parseParamNames(reader);

        // Determine which columns contain the X,Y,Z parameters.
        int[] columns = new int[3];
        columns[0] = findParam(paramNames, "X");
        columns[1] = findParam(paramNames, "Y");
        columns[2] = findParam(paramNames, "Z");
        if (columns[0] < 0 || columns[1] < 0 || columns[2] < 0) {
            throw new IOException("Could not find X, Y, or Z parameter on line #"
                    + reader.getLineNumber() + ".");
        }


        // Keep reading until we reach the end of the file.
        while (netID != null) {

            // Parse the optional network name.
            String netName = parseOptionalNetName(aLine);
            if (netName == null)
                netName = netID;

            // Read in a single list of data points.
            double[][] dataStr = readString(reader, columns);

            // Create a list of strings (lists of data points) and add the
            // 1st one to it.
            ArrayList<double[][]> strList = new ArrayList();
            strList.add(dataStr);

            // Read in the remaining strings in this network.
            String oldNetID = netID;
            aLine = readSkippingComments(reader);
            netID = parseNetID(aLine);
            while (netID != null && netID.equals(oldNetID)) {

                // Read in the string.
                dataStr = readFixedSizeString(reader, columns, dataStr.length);

                // Add it to the list of strings.
                strList.add(dataStr);

                aLine = readSkippingComments(reader);
                netID = parseNetID(aLine);
            }

            // Finished reading network, create geometry network data structure.
            int nRows = strList.size();
            int nCols = dataStr.length;
            double[][] xArr = new double[nRows][nCols];
            double[][] yArr = new double[nRows][nCols];
            double[][] zArr = new double[nRows][nCols];
            for (int i = 0; i < nRows; ++i) {
                dataStr = strList.get(i);
                for (int j = 0; j < nCols; ++j) {
                    xArr[i][j] = dataStr[j][0];
                    yArr[i][j] = dataStr[j][1];
                    zArr[i][j] = dataStr[j][2];
                }
            }
            GeomNetwork net = new GeomNetwork(xArr, yArr, zArr, netName);

            // Add the new network to the component.
            comp.add(net);
        }


        return comp;
    }
}
