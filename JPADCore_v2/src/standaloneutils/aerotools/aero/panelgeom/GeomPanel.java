/*
 *   GeomPanel  -- Defines a quadralateral panel geometry element.
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

import static java.lang.Math.*;
import java.text.MessageFormat;

import standaloneutils.mathtools.MathTools;


 /**
 *  <p> Defines a geometry element called a panel.  A panel
 *      consists of 4 corner points in 3D space and a
 *      normal vector.
 *  </p>
 *  <p> Panel corner points are defined as follows:  Imagine a
 *      rectangle on a piece of paper.  The normal vector points
 *      out of the page.  The 4 corner points are ordered, starting 
 *      in the lower left corner and going around the panel in a
 *      counter clockwise direction.
 *  </p>
 *  <p> The GeomPanel class uses an object pool.  This means that when
 *      you request a new object using "getInstance()", that method
 *      will attempt to recycle GeomPanel objects from a pool of unused
 *      objects if there are any available.  This reduces the overhead
 *      compared with creating new objects on the stack using "new".
 *      If the pool is empty, a new object is created and returned to you.
 *      It is your responsibility to return GeomPanel objects to the
 *      pool using "freeInstance()" when you are finished with them.
 *      If you forget to return an object to the pool, it will simply
 *      be garbage collected like any other object, but you loose
 *      the advantages provided by the object pool.
 *  </p>
 *
 *  <p>  Modified by:  Joseph A. Huwaldt   </p>
 *
 *  @author Joseph A. Huwaldt    Date:  December 11, 1999
 *  @version April 3, 2014
 */
public class GeomPanel implements java.io.Serializable, Cloneable {

    //  Machine epsilon.
    private static final double EPS = MathTools.EPS;
    
    /**
     * X,Y,Z coordinates of the panel corner points.
     */
	private double x1, y1, z1;
	private double x2, y2, z2;
	private double x3, y3, z3;
	private double x4, y4, z4;
	
    /**
     * Panel unit normal vector.
     */
	private double nx, ny, nz;

    /**
     * Panel centroid.
     */
	private double xcent, ycent, zcent;

    /**
     * Panel surface area.
     */
	private double area;
	

	// Used by the GeomPanel pool (static items are not serialized).
	private static final int POOLSIZE = 50;
	private static final GeomPanel[] freeStack = new GeomPanel[POOLSIZE];
	private static int countFree=0;

    /**
     * Force the user to create a new instance using "getInstance()".
     */
	protected GeomPanel() {}

	
    /**
     * Create an instance of a GeomPanel object where everything is set to zero.
     *
     * @return A GeomPanel object with all the values set to zero.
     * @see #freeInstance
     */
    public static GeomPanel getInstance() {
        return getInstance(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Create an instance of a GeomPanel object by providing the corner point locations.
     *
     * @param x1 Corner point #1, X coordinate.
     * @param y1 Corner point #1, Y coordinate.
     * @param z1 Corner point #1, Z coordinate.
     * @param x2 Corner point #2, X coordinate.
     * @param y2 Corner point #2, Y coordinate.
     * @param z2 Corner point #2, Z coordinate.
     * @param x3 Corner point #3, X coordinate.
     * @param y3 Corner point #3, Y coordinate.
     * @param z3 Corner point #3, Z coordinate.
     * @param x4 Corner point #3, X coordinate.
     * @param y4 Corner point #3, Y coordinate.
     * @param z4 Corner point #3, Z coordinate.
     *
     * @return A GeomPanel object with the specified geometry.
     * @see #freeInstance
     */
    public static synchronized GeomPanel getInstance(double x1, double y1, double z1,
            double x2, double y2, double z2, double x3, double y3, double z3,
            double x4, double y4, double z4) {

        // Either create a new object (if pool is empty) or get from pool.
        GeomPanel result;
        if (countFree == 0) {
            // Create a new object.
            result = new GeomPanel();
        } else {
            // Remove object from the end of the pool stack.
            result = freeStack[--countFree];
        }

        // Initialize the new object.
        result.x1 = x1;
        result.x2 = x2;
        result.x3 = x3;
        result.x4 = x4;

        result.y1 = y1;
        result.y2 = y2;
        result.y3 = y3;
        result.y4 = y4;

        result.z1 = z1;
        result.z2 = z2;
        result.z3 = z3;
        result.z4 = z4;

        // Calculate the panel normal vector.
        result.calcNormal();

        // Find the panel centroid and surface area.
        result.calcCentroidAndArea();

        return result;

    }

    /**
     * Create an instance of a GeomPanel object by providing the corner point locations,
     * unit normal vector, panel centroid location and panel surface area.
     *
     * @param x1 Corner point #1, X coordinate.
     * @param y1 Corner point #1, Y coordinate.
     * @param z1 Corner point #1, Z coordinate.
     * @param x2 Corner point #2, X coordinate.
     * @param y2 Corner point #2, Y coordinate.
     * @param z2 Corner point #2, Z coordinate.
     * @param x3 Corner point #3, X coordinate.
     * @param y3 Corner point #3, Y coordinate.
     * @param z3 Corner point #3, Z coordinate.
     * @param x4 Corner point #3, X coordinate.
     * @param y4 Corner point #3, Y coordinate.
     * @param z4 Corner point #3, Z coordinate.
     * @param nx The pre-calculated unit normal vector X component.
     * @param ny The pre-calculated unit normal vector Y component.
     * @param nz The pre-calculated unit normal vector Z component.
     * @param xc The pre-calculated panel centroid X coordinate.
     * @param yc The pre-calculated panel centroid Y coordinate.
     * @param zc The pre-calculated panel centroid Z coordinate.
     * @param area The pre-calculated panel surface area.
     *
     * @return A GeomPanel object with the specified geometry.
     * @see #freeInstance
     */
    public static synchronized GeomPanel getInstance(double x1, double y1, double z1,
            double x2, double y2, double z2, double x3, double y3, double z3,
            double x4, double y4, double z4, double nx, double ny, double nz,
            double xc, double yc, double zc, double area) {

        // Either create new object (if pool is empty) or get one from pool.
        GeomPanel result;
        if (countFree == 0) {
            // Create a new object.
            result = new GeomPanel();
        } else {
            // Remove object from the end of the pool stack.
            result = freeStack[--countFree];
        }

        // Initialize the new object.
        result.x1 = x1;
        result.x2 = x2;
        result.x3 = x3;
        result.x4 = x4;

        result.y1 = y1;
        result.y2 = y2;
        result.y3 = y3;
        result.y4 = y4;

        result.z1 = z1;
        result.z2 = z2;
        result.z3 = z3;
        result.z4 = z4;

        result.nx = nx;
        result.ny = ny;
        result.nz = nz;

        result.xcent = xc;
        result.ycent = yc;
        result.zcent = zc;

        result.area = area;

        return result;

    }

    /**
     * Create an instance of a GeomPanel object that has the given points for corner
     * points.
     *
     * @param p1 The location of corner point #1.
     * @param p2 The location of corner point #2.
     * @param p3 The location of corner point #3.
     * @param p4 The location of corner point #4.
     * @return A GeomPanel object with all the given geometry.
     * @see #freeInstance
     */
    public static GeomPanel getInstance(GeomPoint p1, GeomPoint p2,
            GeomPoint p3, GeomPoint p4) {

        return getInstance(p1.getX(), p1.getY(), p1.getZ(),
                p2.getX(), p2.getY(), p2.getZ(),
                p3.getX(), p3.getY(), p3.getZ(),
                p4.getX(), p4.getY(), p4.getZ());
    }

    /**
     * Create an instance of a GeomPanel object that has the given points for corner
     * points, the given point for a unit normal vector and the given surface area.
     *
     * @param p1 The location of corner point #1.
     * @param p2 The location of corner point #2.
     * @param p3 The location of corner point #3.
     * @param p4 The location of corner point #4.
     * @param normal The pre-calculated panel unit normal vector.
     * @param centroid The pre-calculated panel centroid location.
     * @param area The pre-calculated panel surface area.
     * @return A GeomPanel object with all the given geometry.
     * @see #freeInstance
     */
    public static GeomPanel getInstance(GeomPoint p1, GeomPoint p2,
            GeomPoint p3, GeomPoint p4, GeomPoint normal,
            GeomPoint centroid, double area) {

        return getInstance(p1.getX(), p1.getY(), p1.getZ(),
                p2.getX(), p2.getY(), p2.getZ(),
                p3.getX(), p3.getY(), p3.getZ(),
                p4.getX(), p4.getY(), p4.getZ(),
                normal.getX(), normal.getY(), normal.getZ(),
                centroid.getX(), centroid.getY(), centroid.getZ(),
                area);
    }

    /**
     * Add an instance of a GeomPanel object to the object pool so that it can be re-used
     * again. Call this method when you are finished using an instance of a GeomPanel
     * object. Give up your reference to the specified GeomPanel immediately after calling
     * this routine. Remember, recycling is good for the environment!
     *
     * @param panel The GeomPanel object to be recycled.
     */
    public static synchronized void freeInstance(GeomPanel panel) {
        if (countFree < POOLSIZE) {
            freeStack[countFree++] = panel;
        }
    }

    /**
     * Return the X-coordinates of each corner point in an array.
     *
     * @return Array containing the X coordinate of each corner point where [0] = corner
     * #1, [1] = corner #2, etc.
     */
    public double[] getAllX() {
        double[] arr = new double[4];

        arr[0] = x1;
        arr[1] = x2;
        arr[2] = x3;
        arr[4] = x4;

        return arr;
    }

    /**
     * Return the Y-coordinates of each corner point in an array.
     *
     * @return Array containing the Y coordinate of each corner point where [0] = corner
     * #1, [1] = corner #2, etc.
     */
    public double[] getAllY() {
        double[] arr = new double[4];

        arr[0] = y1;
        arr[1] = y2;
        arr[2] = y3;
        arr[4] = y4;

        return arr;
    }

    /**
     * Return the Z-coordinates of each corner point in an array.
     *
     * @return Array containing the Z coordinate of each corner point where [0] = corner
     * #1, [1] = corner #2, etc.
     */
    public double[] getAllZ() {
        double[] arr = new double[4];

        arr[0] = z1;
        arr[1] = z2;
        arr[2] = z3;
        arr[4] = z4;

        return arr;
    }

    /**
     * Return the corner point #1.
     *
     * @return GeomPoint object containing the location of corner point #1.
     */
    public GeomPoint getPoint1() {
        return GeomPoint.getInstance(x1, y1, z1);
    }

    /**
     * Return the corner point #2.
     *
     * @return GeomPoint object containing the location of corner point #2.
     */
    public GeomPoint getPoint2() {
        return GeomPoint.getInstance(x2, y2, z2);
    }

    /**
     * Return the corner point #3.
     *
     * @return GeomPoint object containing the location of corner point #3.
     */
    public GeomPoint getPoint3() {
        return GeomPoint.getInstance(x3, y3, z3);
    }

    /**
     * Return the corner point #4.
     *
     * @return GeomPoint object containing the location of corner point #4.
     */
    public GeomPoint getPoint4() {
        return GeomPoint.getInstance(x4, y4, z4);
    }

    /**
     * Return the X-coordinates of corner point #1.
     *
     * @return X-coordinate of corner point #1
     */
    public double getX1() {
        return x1;
    }

    /**
     * Return the Y-coordinates of corner point #1.
     *
     * @return Y-coordinate of corner point #1
     */
    public double getY1() {
        return y1;
    }

    /**
     * Return the Z-coordinates of corner point #1.
     *
     * @return Z-coordinate of corner point #1
     */
    public double getZ1() {
        return z1;
    }

    /**
     * Return the X-coordinates of corner point #2.
     *
     * @return X-coordinate of corner point #2
     */
    public double getX2() {
        return x2;
    }

    /**
     * Return the Y-coordinates of corner point #2.
     *
     * @return Y-coordinate of corner point #2
     */
    public double getY2() {
        return y2;
    }

    /**
     * Return the Z-coordinates of corner point #2.
     *
     * @return Z-coordinate of corner point #2
     */
    public double getZ2() {
        return z2;
    }

    /**
     * Return the X-coordinates of corner point #3.
     *
     * @return X-coordinate of corner point #3
     */
    public double getX3() {
        return x3;
    }

    /**
     * Return the Y-coordinates of corner point #3.
     *
     * @return Y-coordinate of corner point #3
     */
    public double getY3() {
        return y3;
    }

    /**
     * Return the Z-coordinates of corner point #3.
     *
     * @return Z-coordinate of corner point #3
     */
    public double getZ3() {
        return z3;
    }

    /**
     * Return the X-coordinates of corner point #4.
     *
     * @return X-coordinate of corner point #4
     */
    public double getX4() {
        return x4;
    }

    /**
     * Return the Y-coordinates of corner point #4.
     *
     * @return Y-coordinate of corner point #4
     */
    public double getY4() {
        return y4;
    }

    /**
     * Return the Z-coordinates of corner point #4.
     *
     * @return Z-coordinate of corner point #4
     */
    public double getZ4() {
        return z4;
    }

    /**
     * Get the X-component of the panel unit normal vector.
     *
     * @return X-component of the panel unit normal vector.
     */
    public double getNormalX() {
        return nx;
    }

    /**
     * Get the Y-component of the panel unit normal vector.
     *
     * @return Y-component of the panel unit normal vector.
     */
    public double getNormalY() {
        return ny;
    }

    /**
     * Get the Z-component of the panel unit normal vector.
     *
     * @return Z-component of the panel unit normal vector.
     */
    public double getNormalZ() {
        return nz;
    }

    /**
     * Return the unit normal vector for this panel.
     *
     * @return GeomPoint object containing the unit normal vector.
     */
    public GeomPoint getNormal() {
        return GeomPoint.getInstance(nx, ny, nz);
    }

    /**
     * Get the panel centroid X coordinate.
     *
     * @return The panel centroid X coordinate location.
     */
    public double getCentroidX() {
        return xcent;
    }

    /**
     * Get the panel centroid Y coordinate.
     *
     * @return The panel centroid Y coordinate location.
     */
    public double getCentroidY() {
        return ycent;
    }

    /**
     * Get the panel centroid Z coordinate.
     *
     * @return The panel centroid Z coordinate location.
     */
    public double getCentroidZ() {
        return zcent;
    }

    /**
     * Return the centroid location for this panel.
     *
     * @return GeomPoint object containing the centroid location.
     */
    public GeomPoint getCentroid() {
        return GeomPoint.getInstance(xcent, ycent, zcent);
    }

    /**
     * Return the surface area of the panel.
     *
     * @return The panel surface area.
     */
    public double getArea() {
        return area;
    }

    /**
     * Return the minimum bounding X coordinate for this panel.
     *
     * @return The minimum X coordinate found in this panel.
     */
    public double getMinX() {
        double min = min(x1, x2);
        min = min(min, x3);
        min = min(min, x4);
        return min;
    }

    /**
     * Return the maximum bounding X coordinate for this panel.
     *
     * @return The maximum X coordinate found in this panel.
     */
    public double getMaxX() {
        double max = max(x1, x2);
        max = max(max, x3);
        max = max(max, x4);
        return max;
    }

    /**
     * Return the minimum bounding Y coordinate for this panel.
     *
     * @return The minimum Y coordinate found in this panel.
     */
    public double getMinY() {
        double min = min(y1, y2);
        min = min(min, y3);
        min = min(min, y4);
        return min;
    }

    /**
     * Return the maximum bounding Y coordinate for this panel.
     *
     * @return The maximum Y coordinate found in this panel.
     */
    public double getMaxY() {
        double max = max(y1, y2);
        max = max(max, y3);
        max = max(max, y4);
        return max;
    }

    /**
     * Return the minimum bounding Z coordinate for this panel.
     *
     * @return The minimum Z coordinate found in this panel.
     */
    public double getMinZ() {
        double min = min(z1, z2);
        min = min(min, z3);
        min = min(min, z4);
        return min;
    }

    /**
     * Return the maximum bounding Z coordinate for this panel.
     *
     * @return The maximum Z coordinate found in this panel.
     */
    public double getMaxZ() {
        double max = max(z1, z2);
        max = max(max, z3);
        max = max(max, z4);
        return max;
    }

    /**
     * Return the coordinate point representing the minimum bounding box corner (min X,
     * min Y, min Z).
     *
     * @return The minimum bounding box coordinate for this panel.
     */
    public GeomPoint getBoundsMin() {
        return GeomPoint.getInstance(getMinX(), getMinY(), getMinZ());
    }

    /**
     * Return the coordinate point representing the maximum bounding box corner (max X,
     * max Y, max Z).
     *
     * @return The maximum bounding box coordinate for this panel.
     */
    public GeomPoint getBoundsMax() {
        return GeomPoint.getInstance(getMaxX(), getMaxY(), getMaxZ());
    }

    /**
     * Translate this panel by an incremental amount along each axis.
     *
     * @param dx The amount to translate the panel in the X direction.
     * @param dy The amount to translate the panel in the Y direction.
     * @param dz The amount to translate the panel in the Z direction.
     */
    public void translate(double dx, double dy, double dz) {
        x1 += dx;
        x2 += dx;
        x3 += dx;
        x4 += dx;

        y1 += dy;
        y2 += dy;
        y3 += dy;
        y4 += dy;

        z1 += dz;
        z2 += dz;
        z3 += dz;
        z4 += dz;
    }

    /**
     * Scale this panel by the given factor along each axis.
     *
     * @param sx The amount to scale the panel in the X direction.
     * @param sy The amount to scale the panel in the Y direction.
     * @param sz The amount to scale the panel in the Z direction.
     */
    public void scale(double sx, double sy, double sz) {
        x1 *= sx;
        x2 *= sx;
        x3 *= sx;
        x4 *= sx;

        y1 *= sy;
        y2 *= sy;
        y3 *= sy;
        y4 *= sy;

        z1 *= sz;
        z2 *= sz;
        z3 *= sz;
        z4 *= sz;
    }

    /**
     * Make a copy of this GeomPanel object.
     *
     * @return A copy of this GeomPanel object.
     */
    @Override
    public Object clone() {
        Object result = null;

        try {
            result = super.clone();

        } catch (Exception e) {
            // Shouldn't happen if this object implements Cloneable.
            System.err.println("Can not clone this object!");
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Create a string representation of this panel by outputting the panel centroid
     * location.
     */
    @Override
    public String toString() {
        String str = MessageFormat.format("[{0,number},{1,number},{2,number}]", xcent, ycent, zcent);
        return str;
    }

    /**
     * Compares the specified object with this panel for equality. Returns <tt>true</tt>
     * if and only if the specified object is also a panel and both panels have the same
     * corner point coordinates.
     *
     * @param obj The object to be compared for equality with this panel.
     * @return <tt>true</tt> if the specified object is equal to this panel.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (obj.getClass() != this.getClass()))
            return false;

        GeomPanel that = (GeomPanel) obj;
        if (this.x1 != that.x1)
            return false;
        if (this.y1 != that.y1)
            return false;
        if (this.z1 != that.z1)
            return false;
        if (this.x2 != that.x2)
            return false;
        if (this.y2 != that.y2)
            return false;
        if (this.z2 != that.z2)
            return false;
        if (this.x3 != that.x3)
            return false;
        if (this.y3 != that.y3)
            return false;
        if (this.z3 != that.z3)
            return false;
        if (this.x4 != that.x4)
            return false;
        if (this.y4 != that.y4)
            return false;

        return this.z4 == that.z4;
    }

    /**
     * Returns the hash code value for this panel.
     *
     * @return The hash code for this panel.
     */
    @Override
    public int hashCode() {
        int hash = 7;

        hash = hash * 31 + makeVarCode(x1);
        hash = hash * 31 + makeVarCode(y1);
        hash = hash * 31 + makeVarCode(z1);
        hash = hash * 31 + makeVarCode(x2);
        hash = hash * 31 + makeVarCode(y2);
        hash = hash * 31 + makeVarCode(z2);
        hash = hash * 31 + makeVarCode(x3);
        hash = hash * 31 + makeVarCode(y3);
        hash = hash * 31 + makeVarCode(z3);
        hash = hash * 31 + makeVarCode(x4);
        hash = hash * 31 + makeVarCode(y4);
        hash = hash * 31 + makeVarCode(z4);

        return hash;
    }

    private static int makeVarCode(double value) {
        long bits = Double.doubleToLongBits(value);
        int var_code = (int) (bits ^ (bits >>> 32));
        return var_code;
    }

    /**
     * Calculates this panel's unit normal vector.
     */
    private void calcNormal() {
        // Compute diagonal vectors.
        double T1x = x3 - x1;
        double T1y = y3 - y1;
        double T1z = z3 - z1;
        double T2x = x4 - x2;
        double T2y = y4 - y2;
        double T2z = z4 - z2;

        // Compute normal vector as cross product:  N = T1 X T2
        double Nx = T1y * T2z - T2y * T1z;
        double Ny = T2x * T1z - T1x * T2z;
        double Nz = T1x * T2y - T2x * T1y;

        // Compute magnitude of normal vector.
        double VN = sqrt(Nx * Nx + Ny * Ny + Nz * Nz);

        // Form unit normal vector.
        if (abs(VN) > EPS) {      //  VN != 0
            Nx /= VN;
            Ny /= VN;
            Nz /= VN;
        }

        // Save off unit normal vector.
        nx = Nx;
        ny = Ny;
        nz = Nz;
    }

    /**
     * Finds this panel's centroid point and surface area.
     */
    private void calcCentroidAndArea() {

        // Compute the average point.
        double AVx = 0.25 * (x1 + x2 + x3 + x4);
        double AVy = 0.25 * (y1 + y2 + y3 + y4);
        double AVz = 0.25 * (z1 + z2 + z3 + z4);

        // Compute the projection distance.
        double dist = nx * (AVx - x1) + ny * (AVy - y1) + nz * (AVz - z1);

        // Compute diagonal vector.
        double T1x = x3 - x1;
        double T1y = y3 - y1;
        double T1z = z3 - z1;
        double Tmag = sqrt(T1x * T1x + T1y * T1y + T1z * T1z);

        // Form a unit diagonal vector.
        if (abs(Tmag) > EPS) {        //  Tmag != 0.0
            T1x /= Tmag;
            T1y /= Tmag;
            T1z /= Tmag;
        }

        // Form cross product of normal vector and diagonal vector (T2 = n X T1).
        double T2x = ny * T1z - nz * T1y;
        double T2y = nz * T1x - nx * T1z;
        double T2z = nx * T1y - ny * T1x;

        // Place coordinates in arrays for easier access in loop.
        double[] xin = {x1, x2, x3, x4};
        double[] yin = {y1, y2, y3, y4};
        double[] zin = {z1, z2, z3, z4};
        double[] xi = new double[4];
        double[] eta = new double[4];

        // Compute coordinates of corner points in reference coord. system.
        for (int i = 0; i < 4; ++i) {
            double xpa = xin[i] + nx * dist;
            double ypa = yin[i] + ny * dist;
            double zpa = zin[i] + nz * dist;

            dist *= -1;

            double xdif = xpa - AVx;
            double ydif = ypa - AVy;
            double zdif = zpa - AVz;

            // Transform corner points to element coordinate system (xi, eta)
            // with average point as origin.
            xi[i] = T1x * xdif + T1y * ydif + T1z * zdif;
            eta[i] = T2x * xdif + T2y * ydif + T2z * zdif;

        }

        double xi0;
        double etack = eta[1] - eta[3];
        if (abs(etack) < EPS)     //  etack == 0.0
            xi0 = 0.0F;
        else
            xi0 = (xi[3] * (eta[0] - eta[1]) + xi[1] * (eta[3] - eta[0])) / (eta[1] - eta[3]) / 3;

        double eta0 = -eta[0] / 3;

        // Obtain corner points in system with centroid as origin.
        for (int i = 0; i < 4; ++i) {
            xi[i] -= xi0;
            eta[i] -= eta0;
        }

        // Transform centroid to the reference coordinate system.
        this.xcent = AVx + T1x * xi0 + T2x * eta0;
        this.ycent = AVy + T1y * xi0 + T2y * eta0;
        this.zcent = AVz + T1z * xi0 + T2z * eta0;

        // Calculate the surface area of the panel.
        double xi3m1 = xi[2] - xi[0];
        double eta2m4 = eta[1] - eta[3];

        this.area = -0.5 * xi3m1 * eta2m4;

    }
}
