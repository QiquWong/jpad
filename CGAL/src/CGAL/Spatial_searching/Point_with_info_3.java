/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Spatial_searching;
import CGAL.Kernel.Point_3; import CGAL.Java.JavaData;
public class Point_with_info_3 {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Point_with_info_3(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Point_with_info_3 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Spatial_searchingJNI.delete_Point_with_info_3(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Point_with_info_3() {
    this(CGAL_Spatial_searchingJNI.new_Point_with_info_3__SWIG_0(), true);
  }

  public Point_with_info_3(Point_3 first, JavaData second) {
    this(CGAL_Spatial_searchingJNI.new_Point_with_info_3__SWIG_1(Point_3.getCPtr(first), first, JavaData.getCPtr(second), second), true);
  }

  public Point_with_info_3(Point_with_info_3 p) {
    this(CGAL_Spatial_searchingJNI.new_Point_with_info_3__SWIG_2(Point_with_info_3.getCPtr(p), p), true);
  }

  public void setFirst(Point_3 value) {
    CGAL_Spatial_searchingJNI.Point_with_info_3_first_set(swigCPtr, this, Point_3.getCPtr(value), value);
  }

  public Point_3 getFirst() {
    long cPtr = CGAL_Spatial_searchingJNI.Point_with_info_3_first_get(swigCPtr, this);
    return (cPtr == 0) ? null : new Point_3(cPtr, false);
  }

  public void setSecond(JavaData value) {
    CGAL_Spatial_searchingJNI.Point_with_info_3_second_set(swigCPtr, this, JavaData.getCPtr(value), value);
  }

  public JavaData getSecond() {
    long cPtr = CGAL_Spatial_searchingJNI.Point_with_info_3_second_get(swigCPtr, this);
    return (cPtr == 0) ? null : new JavaData(cPtr, false);
  }

}
