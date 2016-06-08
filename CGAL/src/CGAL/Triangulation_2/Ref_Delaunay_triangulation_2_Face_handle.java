/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Triangulation_2;

public class Ref_Delaunay_triangulation_2_Face_handle {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Ref_Delaunay_triangulation_2_Face_handle(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Ref_Delaunay_triangulation_2_Face_handle obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Triangulation_2JNI.delete_Ref_Delaunay_triangulation_2_Face_handle(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Ref_Delaunay_triangulation_2_Face_handle() {
    this(CGAL_Triangulation_2JNI.new_Ref_Delaunay_triangulation_2_Face_handle__SWIG_0(), true);
  }

  public Ref_Delaunay_triangulation_2_Face_handle(Delaunay_triangulation_2_Face_handle k) {
    this(CGAL_Triangulation_2JNI.new_Ref_Delaunay_triangulation_2_Face_handle__SWIG_1(Delaunay_triangulation_2_Face_handle.getCPtr(k), k), true);
  }

  public void set(Delaunay_triangulation_2_Face_handle t) {
    CGAL_Triangulation_2JNI.Ref_Delaunay_triangulation_2_Face_handle_set(swigCPtr, this, Delaunay_triangulation_2_Face_handle.getCPtr(t), t);
  }

  public Delaunay_triangulation_2_Face_handle object() {
    return new Delaunay_triangulation_2_Face_handle(CGAL_Triangulation_2JNI.Ref_Delaunay_triangulation_2_Face_handle_object(swigCPtr, this), true);
  }

  public Ref_Delaunay_triangulation_2_Face_handle clone() {
    return new Ref_Delaunay_triangulation_2_Face_handle(CGAL_Triangulation_2JNI.Ref_Delaunay_triangulation_2_Face_handle_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Ref_Delaunay_triangulation_2_Face_handle other) {
    CGAL_Triangulation_2JNI.Ref_Delaunay_triangulation_2_Face_handle_clone__SWIG_1(swigCPtr, this, Ref_Delaunay_triangulation_2_Face_handle.getCPtr(other), other);
  }

}
