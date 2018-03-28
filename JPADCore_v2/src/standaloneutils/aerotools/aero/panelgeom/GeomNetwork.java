/*
 *   GeomNetwork  -- A collection of points or panels used to define a portion of a component.
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

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.HashMap;


/**
 * Defines a geometry element called a network. A network is an ordered collection of
 * points or panels that defines a portion of the surface of a component. A network is
 * defined as columns and rows of points. It can also be thought of as a set of "strings"
 * (rows) of points, one adjacent to the other, or as a set of panels (bordered by 4
 * corner points).
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: December 11, 1999
 * @version April 3, 2014
 */
public class GeomNetwork implements GeomElement {

    // Some error messages.
    private static final String NUMSTRMSG = "{0} does not have the same number of strings as {1}";
    private static final String SAMELENMSG = "String #{0,number,integer} of {1} is not the same length as the other strings.";
    private static final String INCLENMSG = "{0} does not have consistant number of points per string.";
    
    /**
     * Name of the network.
     */
    private String name;
    
    /**
     * X,Y,Z coordinates of each point in the network.
     */
    private double[][] pntsX, pntsY, pntsZ;
    
    /**
     * The normal vector for each panel.
     */
    private double[][] nx, ny, nz;
    /**
     * The centroid location for each panel.
     */
    private double[][] xcent, ycent, zcent;
    /**
     * The surface area of each panel.
     */
    private double[][] area;
    
    /**
     * The total surface area of the network.
     */
    private double totArea;
    
    /**
     * The coordinates of a bounding box surrounding this network.
     */
    private double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
    private double maxX = -minX, maxY = -minY, maxZ = -minZ;
    
    /**
     * Application specific data that can be assigned to this network.
     */
    private Map appData;

    
    /**
     * Create a network made up of the X, Y, and Z coordinates given in 2D Java arrays.
     * Each row in the arrays is a string of points and each column is a point in each
     * string.
     *
     * @param xArr Array of X coordinate values: xArr[strings][points in string].
     * @param yArr Array of Y coordinate values: yArr[strings][points in string].
     * @param zArr Array of Z coordinate values: zArr[strings][points in string].
     * @param name Name of the network.
     */
    public GeomNetwork(double[][] xArr, double[][] yArr, double[][] zArr,
            String name) {

        // Check for consistant inputs.
        int numStrings = xArr.length;
        int numPntsPerString = xArr[0].length;

        if (yArr.length != numStrings)
            throw new ArrayIndexOutOfBoundsException(MessageFormat.format(NUMSTRMSG,"yArr","xArr."));

        if (zArr.length != numStrings)
            throw new ArrayIndexOutOfBoundsException(MessageFormat.format(NUMSTRMSG,"zArr","xArr."));

        for (int i = 1; i < numStrings; ++i) {
            if (xArr[i].length != numPntsPerString) {
                throw new ArrayIndexOutOfBoundsException(MessageFormat.format(SAMELENMSG,i,"xArr[][]"));
            }
            if (yArr[i].length != numPntsPerString) {
                throw new ArrayIndexOutOfBoundsException(MessageFormat.format(SAMELENMSG,i,"yArr[][]"));
            }
            if (zArr[i].length != numPntsPerString) {
                throw new ArrayIndexOutOfBoundsException(MessageFormat.format(SAMELENMSG,i,"zArr[][]"));
            }
        }

        this.name = name;
        pntsX = xArr;
        pntsY = yArr;
        pntsZ = zArr;

        calcNormalsAreasAndCentroids();
        findBounds();

    }

    /**
     * Create a network made up of a 2D array of 3D points. The rows in the array are the
     * strings and the columns are the points in the strings.
     *
     * @param pointArr Array of 3D points that make up the network:
     * pointArr[strings][points in strings].
     * @param name Name of the network.
     */
    public GeomNetwork(GeomPoint[][] pointArr, String name) {

        int numStrings = pointArr.length;
        int numPntsPerString = pointArr[0].length;

        // Check for consistant inputs.
        for (int i = 1; i < numStrings; ++i) {
            if (pointArr[i].length != numPntsPerString) {
                throw new ArrayIndexOutOfBoundsException(MessageFormat.format(INCLENMSG,"pointArr"));
            }
        }

        pntsX = new double[numStrings][numPntsPerString];
        pntsY = new double[numStrings][numPntsPerString];
        pntsZ = new double[numStrings][numPntsPerString];

        for (int i = 0; i < numStrings; ++i) {
            double[] rowXi = pntsX[i];   //  Get row "i" from array pntsX[rows][columns].
            double[] rowYi = pntsY[i];
            double[] rowZi = pntsZ[i];
            GeomPoint[] rowPntArri = pointArr[i];
            for (int j = 0; j < numPntsPerString; ++j) {
                rowXi[j] = rowPntArri[j].getX();
                rowYi[j] = rowPntArri[j].getY();
                rowZi[j] = rowPntArri[j].getZ();
            }
        }

        this.name = name;

        calcNormalsAreasAndCentroids();
        findBounds();
    }

    /**
     * Return the name of this network.
     *
     * @return The name of this network as a String.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Change the name of this network.
     *
     * @param newName The new name to be given to this network.
     */
    @Override
    public void setName(String newName) {
        name = newName;
    }

    /**
     * Return the surface area of this network.
     *
     * @return The surface area of this network.
     */
    @Override
    public double getArea() {
        return totArea;
    }

    /**
     * Return the total number of points in this network.
     *
     * @return Total number of points in this network.
     */
    public int getNumPoints() {
        return pntsX.length * pntsX[0].length;
    }

    /**
     * Return the number of strings in this network.
     *
     * @return Number of strings or rows in this network.
     */
    public int getNumStrings() {
        return pntsX.length;
    }

    /**
     * Return the number of points per string in this network.
     *
     * @return Number of points per string or columns in this network.
     */
    public int getNumPointsPerString() {
        return pntsX[0].length;
    }

    /**
     * Return the total number of panels in this network.
     *
     * @return Total number of panels in this network.
     */
    @Override
    public int getNumberOfPanels() {
        return (getNumStrings() - 1) * (getNumPointsPerString() - 1);
    }

    /**
     * Get the panel at the specified location on the network.
     *
     * @param row	The row (string wise) index of the panel to be returned.
     * @param col The column (point wise in string) index of the panel to be returned.
     * @return The panel at the specified index location on the network.
     */
    public GeomPanel getPanel(int row, int col) {

        double x1 = pntsX[row][col];
        double y1 = pntsY[row][col];
        double z1 = pntsZ[row][col];
        double x2 = pntsX[row + 1][col];
        double y2 = pntsY[row + 1][col];
        double z2 = pntsZ[row + 1][col];
        double x3 = pntsX[row + 1][col + 1];
        double y3 = pntsY[row + 1][col + 1];
        double z3 = pntsZ[row + 1][col + 1];
        double x4 = pntsX[row][col + 1];
        double y4 = pntsY[row][col + 1];
        double z4 = pntsZ[row][col + 1];

        GeomPanel aPanel = GeomPanel.getInstance(
                x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4,
                nx[row][col], ny[row][col],
                nz[row][col], xcent[row][col], ycent[row][col],
                zcent[row][col], area[row][col]);

        return aPanel;
    }

    /**
     * Return the X coordinate values for each point in this network.
     *
     * @return All the X-coordinate point values in this network.
     */
    public double[][] getAllX() {
        return pntsX;
    }

    /**
     * Return the Y coordinate values for each point in this network.
     *
     * @return All the Y-coordinate point values in this network.
     */
    public double[][] getAllY() {
        return pntsY;
    }

    /**
     * Return the Z coordinate values for each point in this network.
     *
     * @return All the Z-coordinate point values in this network.
     */
    public double[][] getAllZ() {
        return pntsZ;
    }

    /**
     * Return all the 3D points that make up this network.
     *
     * @return All the 3D points that make up this network.
     */
    public GeomPoint[][] getAllPoints() {
        int t = pntsX.length;
        int s = pntsX[0].length;

        GeomPoint[][] pointArr = new GeomPoint[t][s];

        for (int i = 0; i < t; ++i) {
            for (int j = 0; j < s; ++j) {
                pointArr[i][j] = GeomPoint.getInstance(pntsX[i][j],
                        pntsY[i][j], pntsZ[i][j]);
            }
        }

        return pointArr;
    }

    /**
     * Return the normal vector X component for each panel in this network.
     *
     * @return All the normal vector X components in this network.
     */
    public double[][] getAllNormalX() {
        return nx;
    }

    /**
     * Return the normal vector Y component for each panel in this network.
     *
     * @return All the normal vector Y components in this network.
     */
    public double[][] getAllNormalY() {
        return ny;
    }

    /**
     * Return the normal vector Z component for each panel in this network.
     *
     * @return All the normal vector Z components in this network.
     */
    public double[][] getAllNormalZ() {
        return nz;
    }

    /**
     * Return the surface area of the specified panel.
     *
     * @param row	The row (string wise) index of the panel who's area is to be returned.
     * @param col The column (point wise in string) index of the panel who's area is to be
     * returned.
     * @return The surface area for the specified panel in this network.
     */
    public double getArea(int row, int col) {
        return area[row][col];
    }

    /**
     * Return the area centroid X component for the specified panel in this network.
     *
     * @param row	The row (string wise) index of the panel who's normal vector is to be
     * returned.
     * @param col The column (point wise in string) index of the panel who's normal vector
     * is to be returned.
     * @return The centroid X component for the specified panel in this network.
     */
    public double getCentroidX(int row, int col) {
        return xcent[row][col];
    }

    /**
     * Return the area centroid Y component for the specified panel in this network.
     *
     * @param row	The row (string wise) index of the panel who's normal vector is to be
     * returned.
     * @param col The column (point wise in string) index of the panel who's normal vector
     * is to be returned.
     * @return The centroid Y component for the specified panel in this network.
     */
    public double getCentroidY(int row, int col) {
        return ycent[row][col];
    }

    /**
     * Return the area centroid Z component for the specified panel in this network.
     *
     * @param row	The row (string wise) index of the panel who's normal vector is to be
     * returned.
     * @param col The column (point wise in string) index of the panel who's normal vector
     * is to be returned.
     * @return The centroid Z component for the specified panel in this network.
     */
    public double getCentroidZ(int row, int col) {
        return zcent[row][col];
    }

    /**
     * Return the normal vector X component for the specified panel in this network.
     *
     * @param row	The row (string wise) index of the panel who's normal vector is to be
     * returned.
     * @param col The column (point wise in string) index of the panel who's normal vector
     * is to be returned.
     * @return The normal vector X component for the specified panel in this network.
     */
    public double getNormalX(int row, int col) {
        return nx[row][col];
    }

    /**
     * Return the normal vector Y component for the specified panel in this network.
     *
     * @param row	The row (string wise) index of the panel who's normal vector is to be
     * returned.
     * @param col The column (point wise in string) index of the panel who's normal vector
     * is to be returned.
     * @return The normal vector Y component for the specified panel in this network.
     */
    public double getNormalY(int row, int col) {
        return ny[row][col];
    }

    /**
     * Return the normal vector Z component for the specified panel in this network.
     *
     * @param row	The row (string wise) index of the panel who's normal vector is to be
     * returned.
     * @param col The column (point wise in string) index of the panel who's normal vector
     * is to be returned.
     * @return The normal vector Z component for the specified panel in this network.
     */
    public double getNormalZ(int row, int col) {
        return nz[row][col];
    }

    /**
     * Return the normal vector for the specified panel in this network.
     *
     * @param row	The row (string wise) index of the panel who's normal vector is to be
     * returned.
     * @param col The column (point wise in string) index of the panel who's normal vector
     * is to be returned.
     * @return The normal vector Z for the specified panel in this network.
     */
    public GeomPoint getNormal(int row, int col) {
        return GeomPoint.getInstance(nx[row][col], ny[row][col], nz[row][col]);
    }

    /**
     * Return the minimum bounding X coordinate for this network.
     *
     * @return The minimum X coordinate found in this network.
     */
    @Override
    public double getMinX() {
        return minX;
    }

    /**
     * Return the maximum bounding X coordinate for this network.
     *
     * @return The maximum X coordinate found in this network.
     */
    @Override
    public double getMaxX() {
        return maxX;
    }

    /**
     * Return the minimum bounding Y coordinate for this network.
     *
     * @return The minimum Y coordinate found in this network.
     */
    @Override
    public double getMinY() {
        return minY;
    }

    /**
     * Return the maximum bounding Y coordinate for this network.
     *
     * @return The maximum Y coordinate found in this network.
     */
    @Override
    public double getMaxY() {
        return maxY;
    }

    /**
     * Return the minimum bounding Z coordinate for this network.
     *
     * @return The minimum Z coordinate found in this network.
     */
    @Override
    public double getMinZ() {
        return minZ;
    }

    /**
     * Return the maximum bounding Z coordinate for this network.
     *
     * @return The maximum Z coordinate found in this network.
     */
    @Override
    public double getMaxZ() {
        return maxZ;
    }

    /**
     * Return the coordinate point representing the minimum bounding box corner (min X,
     * min Y, min Z).
     *
     * @return The minimum bounding box coordinate for this network.
     */
    @Override
    public GeomPoint getBoundsMin() {
        return GeomPoint.getInstance(minX, minY, minZ);
    }

    /**
     * Return the coordinate point representing the maximum bounding box corner (max X,
     * max Y, max Z).
     *
     * @return The maximum bounding box coordinate for this network.
     */
    @Override
    public GeomPoint getBoundsMax() {
        return GeomPoint.getInstance(maxX, maxY, maxZ);
    }

    /**
     * Translate this network by an incremental amount along each axis.
     *
     * @param dx The amount to translate the network in the X direction.
     * @param dy The amount to translate the network in the Y direction.
     * @param dz The amount to translate the network in the Z direction.
     */
    @Override
    public void translate(double dx, double dy, double dz) {

        // Translate all the points and panel centroids.
        int numStrings = getNumStrings();
        for (int i = 0; i < numStrings; ++i) {
            double[] rowXi = pntsX[i];   //  Get row "i" from array pntsX[rows][columns].
            double[] rowYi = pntsY[i];
            double[] rowZi = pntsZ[i];
            double[] rowXCi = xcent[i];
            double[] rowYCi = ycent[i];
            double[] rowZCi = zcent[i];

            int numPntsPerString = pntsX[0].length;
            for (int j = 0; j < numPntsPerString; ++j) {
                rowXi[j] += dx;
                rowYi[j] += dy;
                rowZi[j] += dz;
                rowXCi[j] += dx;
                rowYCi[j] += dy;
                rowZCi[j] += dz;
            }
        }

        // Translate the bounding box limits.
        minX += dx;
        maxX += dx;
        minY += dy;
        maxY += dy;
        minZ += dz;
        maxZ += dz;
    }

    /**
     * Scale this network by a specified factor along each axis.
     *
     * @param sx The amount to scale the network in the X direction.
     * @param sy The amount to scale the network in the Y direction.
     * @param sz The amount to scale the network in the Z direction.
     */
    @Override
    public void scale(double sx, double sy, double sz) {

        // Scale all the points and panel centroids.
        int numStrings = getNumStrings();
        for (int i = 0; i < numStrings; ++i) {
            double[] rowXi = pntsX[i];   //  Get the row "i" from the array pntsX[rows][columns].
            double[] rowYi = pntsY[i];
            double[] rowZi = pntsZ[i];
            double[] rowXCi = xcent[i];
            double[] rowYCi = ycent[i];
            double[] rowZCi = zcent[i];

            int numPntsPerString = pntsX[0].length;
            for (int j = 0; j < numPntsPerString; ++j) {
                rowXi[j] *= sx;
                rowYi[j] *= sy;
                rowZi[j] *= sz;
                rowXCi[j] *= sx;
                rowYCi[j] *= sy;
                rowZCi[j] *= sz;
            }
        }

        // Scale the bounding box limits.
        minX *= sx;
        maxX *= sx;
        minY *= sy;
        maxY *= sy;
        minZ *= sz;
        maxZ *= sz;
    }

    /**
     * Make a copy of this GeomNetwork object.
     *
     * @return A copy of this GeomNetwork object.
     */
    @Override
    public Object clone() {
        Object result = null;

        try {
            // Make a shallow copy.
            result = super.clone();

            // Now make a deep copy of the data in this network.
            GeomNetwork newNet = (GeomNetwork) result;

            newNet.pntsX = (double[][]) pntsX.clone();
            newNet.pntsY = (double[][]) pntsY.clone();
            newNet.pntsZ = (double[][]) pntsZ.clone();
            newNet.nx = (double[][]) nx.clone();
            newNet.ny = (double[][]) ny.clone();
            newNet.nz = (double[][]) nz.clone();
            newNet.xcent = (double[][]) xcent.clone();
            newNet.ycent = (double[][]) ycent.clone();
            newNet.zcent = (double[][]) zcent.clone();
            newNet.area = (double[][]) area.clone();

        } catch (Exception e) {
            // Shouldn't happen if this object implements Cloneable.
            System.err.println("Can not clone this object!");
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Return an iteration of all the panels in this network.
     *
     * @return An iteration of all the panels in this network.
     */
    public Iterator panels() {
        return new PanelItr();
    }

    /**
     * An iterator object that returns all the panels in this network one at a time.
     */
    private class PanelItr implements Iterator {

        /**
         * Row and column in data arrays of current element.
         */
        int row = 0, col = 0;

        @Override
        public boolean hasNext() {
            return row < getNumStrings() - 1;
        }

        @Override
        public Object next() {
            if (row < getNumStrings() - 1) {
                GeomPanel aPanel = getPanel(row, col);

                ++col;
                if (col >= getNumPointsPerString() - 1) {
                    col = 0;
                    ++row;
                }

                return aPanel;
            }

            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Store application specific data that can be assigned to this network. This can be
     * used to store any application specific information that needs to be associated with
     * this geometry. If you make use of serialization, make sure any data supplied is
     * Serializable.
     *
     * @param key The key used to store and retrieve the application specific data
     * assigned to this network.
     * @param data The application specific object to be assigned to this network.
     */
    @Override
    public void putData(Object key, Object data) {
        if (appData == null)
            appData = new HashMap();

        appData.put(key, data);
    }

    /**
     * Return the application specific object associated with the specified key that has
     * been assigned to this network.
     *
     * @param key The key used to store and retrieve the application specific data.
     * @return The application specific object assigned to this network. Returns null if
     * no object using the specified key has been assigned to this network.
     */
    @Override
    public Object getData(Object key) {
        Object data = null;
        if (appData != null)
            data = appData.get(key);

        return data;
    }

    /**
     * Returns a reference to a Map containing all application specific objects associated
     * with this network. This allows a developer to easily manipulate application
     * specific data that is associated with this network using the Map interface.
     *
     * @return A reference to the Map object used to store application specific data along
     * with this network. Returns null if no objects have been assigned to this network.
     */
    @Override
    public Map getAllData() {
        return appData;
    }

    /**
     * Create a string representation of this network by outputing the network name and
     * it's bounding rectangle coordinates.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getName());
        buf.append("[");

        GeomPoint pnt = getBoundsMin();
        buf.append(pnt.toString());
        buf.append(",");
        GeomPoint.freeInstance(pnt);

        pnt = getBoundsMax();
        buf.append(pnt.toString());
        GeomPoint.freeInstance(pnt);

        buf.append("]");

        return buf.toString();
    }

    /**
     * Compares the specified object with this network for equality. Returns <tt>true</tt>
     * if and only if the specified object is also a network and both networks have the
     * same name and the same 3D bounding box. It is possible that two networks that are
     * different will have the same name and bounding box, but it doesn't seem likely.
     *
     * @param obj The object to be compared for equality with this network.
     * @return <tt>true</tt> if the specified object is equal to this network.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (obj.getClass() != this.getClass()))
            return false;

        GeomNetwork that = (GeomNetwork) obj;
        if (!this.getName().equals(that.getName()))
            return false;
        if (this.getNumStrings() != that.getNumStrings())
            return false;
        if (this.getNumPointsPerString() != that.getNumPointsPerString())
            return false;

        for (int i = 0; i < pntsX.length; ++i) {
            double[] thisRow = pntsX[i];
            double[] thatRow = that.pntsX[i];
            for (int j = 0; j < thisRow.length; ++j) {
                if (thisRow[j] != thatRow[i])
                    return false;
            }
        }

        for (int i = 0; i < pntsY.length; ++i) {
            double[] thisRow = pntsY[i];
            double[] thatRow = that.pntsY[i];
            for (int j = 0; j < thisRow.length; ++j) {
                if (thisRow[j] != thatRow[i])
                    return false;
            }
        }

        for (int i = 0; i < pntsZ.length; ++i) {
            double[] thisRow = pntsZ[i];
            double[] thatRow = that.pntsZ[i];
            for (int j = 0; j < thisRow.length; ++j) {
                if (thisRow[j] != thatRow[i])
                    return false;
            }
        }

        return true;
    }

    /**
     * Returns the hash code value for this network.
     *
     * @return The hash code for this network.
     */
    @Override
    public int hashCode() {
        int hash = 7;

        hash = hash * 31 + (name == null ? 0 : name.hashCode());

        for (int i = 0; i < pntsX.length; ++i) {
            double[] rowX = pntsX[i];
            double[] rowY = pntsY[i];
            double[] rowZ = pntsZ[i];
            for (int j = 0; j < rowX.length; ++j) {
                hash = hash * 31 + makeVarCode(rowX[j]);
                hash = hash * 31 + makeVarCode(rowY[j]);
                hash = hash * 31 + makeVarCode(rowZ[j]);
            }
        }

        return hash;
    }

    private static int makeVarCode(double value) {
        long bits = Double.doubleToLongBits(value);
        int var_code = (int) (bits ^ (bits >>> 32));
        return var_code;
    }

    /**
     * Calculates the panel normals, surface areas, and centroids. This is called once
     * when a network is first created.
     */
    private void calcNormalsAreasAndCentroids() {
        int t = getNumStrings() - 1;
        int s = getNumPointsPerString() - 1;

        // Allocate memory for normal vector, area, and centroid arrays.
        nx = new double[t][s];
        ny = new double[t][s];
        nz = new double[t][s];
        xcent = new double[t][s];
        ycent = new double[t][s];
        zcent = new double[t][s];
        area = new double[t][s];
        totArea = 0;

        // Loop over all the panels and calculate the required information.
        for (int i = 0; i < t; ++i) {
            double[] rowXi = pntsX[i];   //  Get row "i" from array pntsX[rows][columns].
            double[] rowYi = pntsY[i];
            double[] rowZi = pntsZ[i];
            double[] rowXip1 = pntsX[i + 1];
            double[] rowYip1 = pntsY[i + 1];
            double[] rowZip1 = pntsZ[i + 1];

            for (int j = 0; j < s; ++j) {
                double x1 = rowXi[j];
                double x4 = rowXi[j + 1];
                double x2 = rowXip1[j];
                double x3 = rowXip1[j + 1];
                double y1 = rowYi[j];
                double y4 = rowYi[j + 1];
                double y2 = rowYip1[j];
                double y3 = rowYip1[j + 1];
                double z1 = rowZi[j];
                double z4 = rowZi[j + 1];
                double z2 = rowZip1[j];
                double z3 = rowZip1[j + 1];

                // Retrieve a panel from the pool.
                GeomPanel aPanel = GeomPanel.getInstance(
                        x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);

                // Extract info calculated by panels.
                nx[i][j] = aPanel.getNormalX();
                ny[i][j] = aPanel.getNormalY();
                nz[i][j] = aPanel.getNormalZ();

                area[i][j] = aPanel.getArea();
                totArea += area[i][j];

                xcent[i][j] = aPanel.getCentroidX();
                ycent[i][j] = aPanel.getCentroidY();
                zcent[i][j] = aPanel.getCentroidZ();

                // Return the panel to the pool (recycle).
                GeomPanel.freeInstance(aPanel);
            }
        }
    }

    /**
     * Find the bounding box that surrounds this geometry network. The bounding box
     * contains all the points in the network. This is called once when a network is first
     * created.
     */
    private void findBounds() {

        int numStrings = getNumStrings();
        int numPntsPerString = getNumPointsPerString();
        for (int i = 0; i < numStrings; ++i) {
            double[] rowXi = pntsX[i];   //  Get row "i" from the array pntsX[rows][columns].
            double[] rowYi = pntsY[i];
            double[] rowZi = pntsZ[i];

            for (int j = 0; j < numPntsPerString; ++j) {
                double value = rowXi[j];
                minX = Math.min(minX, value);
                maxX = Math.max(maxX, value);

                value = rowYi[j];
                minY = Math.min(minY, value);
                maxY = Math.max(maxY, value);

                value = rowZi[j];
                minZ = Math.min(minZ, value);
                maxZ = Math.max(maxZ, value);
            }
        }
    }
}
