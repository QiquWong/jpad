/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Spatial_searching;

public class Point_with_info_with_transformed_distance_2 {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Point_with_info_with_transformed_distance_2(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Point_with_info_with_transformed_distance_2 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Spatial_searchingJNI.delete_Point_with_info_with_transformed_distance_2(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Point_with_info_with_transformed_distance_2() {
    this(CGAL_Spatial_searchingJNI.new_Point_with_info_with_transformed_distance_2__SWIG_0(), true);
  }

  public Point_with_info_with_transformed_distance_2(Point_with_info_2 first, double second) {
    this(CGAL_Spatial_searchingJNI.new_Point_with_info_with_transformed_distance_2__SWIG_1(Point_with_info_2.getCPtr(first), first, second), true);
  }

  public Point_with_info_with_transformed_distance_2(Point_with_info_with_transformed_distance_2 p) {
    this(CGAL_Spatial_searchingJNI.new_Point_with_info_with_transformed_distance_2__SWIG_2(Point_with_info_with_transformed_distance_2.getCPtr(p), p), true);
  }

  public void setFirst(Point_with_info_2 value) {
    CGAL_Spatial_searchingJNI.Point_with_info_with_transformed_distance_2_first_set(swigCPtr, this, Point_with_info_2.getCPtr(value), value);
  }

  public Point_with_info_2 getFirst() {
    long cPtr = CGAL_Spatial_searchingJNI.Point_with_info_with_transformed_distance_2_first_get(swigCPtr, this);
    return (cPtr == 0) ? null : new Point_with_info_2(cPtr, false);
  }

  public void setSecond(double value) {
    CGAL_Spatial_searchingJNI.Point_with_info_with_transformed_distance_2_second_set(swigCPtr, this, value);
  }

  public double getSecond() {
    return CGAL_Spatial_searchingJNI.Point_with_info_with_transformed_distance_2_second_get(swigCPtr, this);
  }

}
