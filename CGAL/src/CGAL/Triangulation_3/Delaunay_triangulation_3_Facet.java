/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Triangulation_3;

public class Delaunay_triangulation_3_Facet {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Delaunay_triangulation_3_Facet(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Delaunay_triangulation_3_Facet obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Triangulation_3JNI.delete_Delaunay_triangulation_3_Facet(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Delaunay_triangulation_3_Facet() {
    this(CGAL_Triangulation_3JNI.new_Delaunay_triangulation_3_Facet__SWIG_0(), true);
  }

  public Delaunay_triangulation_3_Facet(Delaunay_triangulation_3_Cell_handle first, int second) {
    this(CGAL_Triangulation_3JNI.new_Delaunay_triangulation_3_Facet__SWIG_1(Delaunay_triangulation_3_Cell_handle.getCPtr(first), first, second), true);
  }

  public Delaunay_triangulation_3_Facet(Delaunay_triangulation_3_Facet p) {
    this(CGAL_Triangulation_3JNI.new_Delaunay_triangulation_3_Facet__SWIG_2(Delaunay_triangulation_3_Facet.getCPtr(p), p), true);
  }

  public void setFirst(Delaunay_triangulation_3_Cell_handle value) {
    CGAL_Triangulation_3JNI.Delaunay_triangulation_3_Facet_first_set(swigCPtr, this, Delaunay_triangulation_3_Cell_handle.getCPtr(value), value);
  }

  public Delaunay_triangulation_3_Cell_handle getFirst() {
    long cPtr = CGAL_Triangulation_3JNI.Delaunay_triangulation_3_Facet_first_get(swigCPtr, this);
    return (cPtr == 0) ? null : new Delaunay_triangulation_3_Cell_handle(cPtr, false);
  }

  public void setSecond(int value) {
    CGAL_Triangulation_3JNI.Delaunay_triangulation_3_Facet_second_set(swigCPtr, this, value);
  }

  public int getSecond() {
    return CGAL_Triangulation_3JNI.Delaunay_triangulation_3_Facet_second_get(swigCPtr, this);
  }

}
