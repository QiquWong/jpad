/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Triangulation_2;
import CGAL.Kernel.Point_2;
public class Triangulation_2_Vertex_handle {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Triangulation_2_Vertex_handle(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Triangulation_2_Vertex_handle obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Triangulation_2JNI.delete_Triangulation_2_Vertex_handle(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Triangulation_2_Vertex_handle() {
    this(CGAL_Triangulation_2JNI.new_Triangulation_2_Vertex_handle(), true);
  }

  public Point_2 point() {
    return new Point_2(CGAL_Triangulation_2JNI.Triangulation_2_Vertex_handle_point__SWIG_0(swigCPtr, this), true);
  }

  public void point(Point_2 ref) {
    CGAL_Triangulation_2JNI.Triangulation_2_Vertex_handle_point__SWIG_1(swigCPtr, this, Point_2.getCPtr(ref), ref);
  }

  public void set_point(Point_2 c) {
    CGAL_Triangulation_2JNI.Triangulation_2_Vertex_handle_set_point(swigCPtr, this, Point_2.getCPtr(c), c);
  }

  public boolean lt(Triangulation_2_Vertex_handle p) {
    return CGAL_Triangulation_2JNI.Triangulation_2_Vertex_handle_lt(swigCPtr, this, Triangulation_2_Vertex_handle.getCPtr(p), p);
  }

  public boolean gt(Triangulation_2_Vertex_handle p) {
    return CGAL_Triangulation_2JNI.Triangulation_2_Vertex_handle_gt(swigCPtr, this, Triangulation_2_Vertex_handle.getCPtr(p), p);
  }

  public boolean le(Triangulation_2_Vertex_handle p) {
    return CGAL_Triangulation_2JNI.Triangulation_2_Vertex_handle_le(swigCPtr, this, Triangulation_2_Vertex_handle.getCPtr(p), p);
  }

  public boolean ge(Triangulation_2_Vertex_handle p) {
    return CGAL_Triangulation_2JNI.Triangulation_2_Vertex_handle_ge(swigCPtr, this, Triangulation_2_Vertex_handle.getCPtr(p), p);
  }

  public boolean equals(Triangulation_2_Vertex_handle p) {
    return CGAL_Triangulation_2JNI.Triangulation_2_Vertex_handle_equals(swigCPtr, this, Triangulation_2_Vertex_handle.getCPtr(p), p);
  }

  public boolean not_equals(Triangulation_2_Vertex_handle p) {
    return CGAL_Triangulation_2JNI.Triangulation_2_Vertex_handle_not_equals(swigCPtr, this, Triangulation_2_Vertex_handle.getCPtr(p), p);
  }

  public int hashCode() {
    return CGAL_Triangulation_2JNI.Triangulation_2_Vertex_handle_hashCode(swigCPtr, this);
  }

  public Triangulation_2_Vertex_handle clone() {
    return new Triangulation_2_Vertex_handle(CGAL_Triangulation_2JNI.Triangulation_2_Vertex_handle_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Triangulation_2_Vertex_handle other) {
    CGAL_Triangulation_2JNI.Triangulation_2_Vertex_handle_clone__SWIG_1(swigCPtr, this, Triangulation_2_Vertex_handle.getCPtr(other), other);
  }

}
