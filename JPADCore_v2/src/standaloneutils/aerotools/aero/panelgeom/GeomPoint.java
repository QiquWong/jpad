/*
 *   GeomPoint  -- Holds the floating point coordinates of a 3D point in space.
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

/**
 * A container that holds the coordinates of a point in 3D space.
 * 
 *  <p> The GeomPoint class uses an object pool.  This means that when
 *      you request a new object using "getInstance()", that method
 *      will attempt to recycle GeomPoint objects from a pool of unused
 *      objects if there are any available.  This reduces the overhead
 *      compared with creating new objects on the stack using "new".
 *      If the pool is empty, a new object is created and returned to you.
 *      It is your responsibility to return GeomPoint objects to the
 *      pool using "freeInstance()" when you are finished with them.
 *      If you forget to return an object to the pool, it will simply
 *      be garbage collected like any other object, but you loose
 *      the advantages provided by the object pool.
 *  </p>
 * 
 * <p> Modified by: Joseph A. Huwaldt </p>
 * 
 * @author Joseph A. Huwaldt Date: December 11, 1999
 * @version April 3, 2014
 */
public final class GeomPoint implements Cloneable, java.io.Serializable {

    /**
     * X,Y,Z coordinates of the 3D point.
     */
    private double x, y, z;
    
    // Used by the GeomPoint pool (static items are not serialized).
    private static final int POOLSIZE = 50;
    private static final GeomPoint[] freeStack = new GeomPoint[POOLSIZE];
    private static int countFree = 0;

    // Force the user to create a new instance using "getInstance()".
    protected GeomPoint() {
    }

    /**
     * Create an instance of a GeomPoint object where everything is set to zero.
     *
     * @return A GeomPoint object with all the values set to zero.
     * @see #freeInstance
     */
    public static GeomPoint getInstance() {
        return getInstance(0, 0, 0);
    }

    /**
     * Create an instance of a GeomPoint object by providing the coordinate values.
     *
     * @param x X-coordinate value.
     * @param y Y-coordinate value.
     * @param z Z-coordinate value.
     * @return A GeomPoint object with the specified location.
     * @see #freeInstance
     */
    public static synchronized GeomPoint getInstance(double x, double y, double z) {

        // Either create a new object (if pool is empty) or get on from the pool.
        GeomPoint result;
        if (countFree == 0) {
            // Create a new object.
            result = new GeomPoint();
        } else {
            // Remove object from the end of the pool stack.
            result = freeStack[--countFree];
        }

        // Initialize the new object.
        result.x = x;
        result.y = y;
        result.z = z;

        return result;
    }

    /**
     * Add an instance of a GeomPoint object to the object pool so that it can be re-used
     * again. Call this method when you are finished using an instance of a GeomPoint
     * object. Give up your reference to the specified GeomPoint immediately after calling
     * this routine. Remember, recycling is good for the environment!
     *
     * @param point The GeomPoint object to be recycled.
     */
    public static synchronized void freeInstance(GeomPoint point) {
        if (countFree < POOLSIZE)
            freeStack[countFree++] = point;
    }

    /**
     * Return the X-coordinate of the point.
     *
     * @return X-coordinate of the point.
     */
    public double getX() {
        return x;
    }

    /**
     * Return the Y-coordinate of the point.
     *
     * @return Y-coordinate of the point.
     */
    public double getY() {
        return y;
    }

    /**
     * Return the Z-coordinate of the point.
     *
     * @return Z-coordinate of the point.
     */
    public double getZ() {
        return z;
    }

    /**
     * Set the X-coordinate of the point.
     *
     * @param value X-coordinate of the point.
     */
    public void setX(double value) {
        x = value;
    }

    /**
     * Set the Y-coordinate of the point.
     *
     * @param value Y-coordinate of the point.
     */
    public void setY(double value) {
        y = value;
    }

    /**
     * Set the Z-coordinate of the point.
     *
     * @param value Z-coordinate of the point.
     */
    public void setZ(double value) {
        z = value;
    }

    /**
     * Translate this point by an incremental amount along each axis.
     *
     * @param dx The amount to translate the point in the X direction.
     * @param dy The amount to translate the point in the Y direction.
     * @param dz The amount to translate the point in the Z direction.
     */
    public void translate(double dx, double dy, double dz) {
        x += dx;
        y += dy;
        z += dz;
    }

    /**
     * Scale this point by a specified factor along each axis.
     *
     * @param sx The amount to scale the point in the X direction.
     * @param sy The amount to scale the point in the Y direction.
     * @param sz The amount to scale the point in the Z direction.
     */
    public void scale(double sx, double sy, double sz) {
        x *= sx;
        y *= sy;
        z *= sz;
    }

    /**
     * Make a copy of this GeomPoint object.
     *
     * @return A copy of this GeomPoint object.
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
     * Creates a string representation of this point by simply outputting the X,Y,Z
     * coordinates of the point.
     */
    @Override
    public String toString() {
        String str = MessageFormat.format("({0,number},{1,number},{2,number})", x, y, z);
        return str;
    }

    /**
     * Compares the specified object with this point for equality. Returns
     * <code>true</code> if and only if the specified object is also a point and both
     * points have the same coordinate values.
     *
     * @param obj The object to be compared for equality with this point.
     * @return <code>true</code> if the specified object is equal to this point.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (obj.getClass() != this.getClass()))
            return false;

        GeomPoint that = (GeomPoint) obj;
        if (this.x != that.x)
            return false;
        if (this.y != that.y)
            return false;

        return this.z == that.z;
    }

    /**
     * Returns the hash code value for this point.
     *
     * @return The hash code for this point.
     */
    @Override
    public int hashCode() {
        int hash = 7;

        hash = hash * 31 + makeVarCode(x);
        hash = hash * 31 + makeVarCode(y);
        hash = hash * 31 + makeVarCode(z);

        return hash;
    }

    private static int makeVarCode(double value) {
        long bits = Double.doubleToLongBits(value);
        int var_code = (int) (bits ^ (bits >>> 32));
        return var_code;
    }
}
