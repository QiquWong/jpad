/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Interpolation;
import CGAL.Kernel.Point_3;
public class Point_3_and_double {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Point_3_and_double(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Point_3_and_double obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_InterpolationJNI.delete_Point_3_and_double(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Point_3_and_double() {
    this(CGAL_InterpolationJNI.new_Point_3_and_double__SWIG_0(), true);
  }

  public Point_3_and_double(Point_3 first, double second) {
    this(CGAL_InterpolationJNI.new_Point_3_and_double__SWIG_1(Point_3.getCPtr(first), first, second), true);
  }

  public Point_3_and_double(Point_3_and_double p) {
    this(CGAL_InterpolationJNI.new_Point_3_and_double__SWIG_2(Point_3_and_double.getCPtr(p), p), true);
  }

  public void setFirst(Point_3 value) {
    CGAL_InterpolationJNI.Point_3_and_double_first_set(swigCPtr, this, Point_3.getCPtr(value), value);
  }

  public Point_3 getFirst() {
    long cPtr = CGAL_InterpolationJNI.Point_3_and_double_first_get(swigCPtr, this);
    return (cPtr == 0) ? null : new Point_3(cPtr, false);
  }

  public void setSecond(double value) {
    CGAL_InterpolationJNI.Point_3_and_double_second_set(swigCPtr, this, value);
  }

  public double getSecond() {
    return CGAL_InterpolationJNI.Point_3_and_double_second_get(swigCPtr, this);
  }

}
