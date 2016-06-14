/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Voronoi_diagram_2;
 import CGAL.Triangulation_2.Delaunay_triangulation_2_Vertex_handle; 
public class Voronoi_diagram_2_Face_handle {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Voronoi_diagram_2_Face_handle(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Voronoi_diagram_2_Face_handle obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Voronoi_diagram_2JNI.delete_Voronoi_diagram_2_Face_handle(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Voronoi_diagram_2_Face_handle() {
    this(CGAL_Voronoi_diagram_2JNI.new_Voronoi_diagram_2_Face_handle(), true);
  }

  public Voronoi_diagram_2_Halfedge_handle halfedge() {
    return new Voronoi_diagram_2_Halfedge_handle(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_halfedge__SWIG_0(swigCPtr, this), true);
  }

  public void halfedge(Voronoi_diagram_2_Halfedge_handle ref) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_halfedge__SWIG_1(swigCPtr, this, Voronoi_diagram_2_Halfedge_handle.getCPtr(ref), ref);
  }

  public Voronoi_diagram_2_Ccb_halfedge_circulator outer_ccb() {
    return new Voronoi_diagram_2_Ccb_halfedge_circulator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_outer_ccb__SWIG_0(swigCPtr, this), true);
  }

  public void outer_ccb(Voronoi_diagram_2_Ccb_halfedge_circulator ref) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_outer_ccb__SWIG_1(swigCPtr, this, Voronoi_diagram_2_Ccb_halfedge_circulator.getCPtr(ref), ref);
  }

  public Delaunay_triangulation_2_Vertex_handle dual() {
    return new Delaunay_triangulation_2_Vertex_handle(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_dual__SWIG_0(swigCPtr, this), true);
  }

  public void dual(Delaunay_triangulation_2_Vertex_handle ref) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_dual__SWIG_1(swigCPtr, this, Delaunay_triangulation_2_Vertex_handle.getCPtr(ref), ref);
  }

  public boolean is_unbounded() {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_is_unbounded(swigCPtr, this);
  }

  public boolean is_halfedge_on_ccb(Voronoi_diagram_2_Halfedge_handle c) {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_is_halfedge_on_ccb(swigCPtr, this, Voronoi_diagram_2_Halfedge_handle.getCPtr(c), c);
  }

  public boolean is_valid() {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_is_valid(swigCPtr, this);
  }

  public boolean equals(Voronoi_diagram_2_Face_handle p) {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_equals(swigCPtr, this, Voronoi_diagram_2_Face_handle.getCPtr(p), p);
  }

  public boolean not_equals(Voronoi_diagram_2_Face_handle p) {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_not_equals(swigCPtr, this, Voronoi_diagram_2_Face_handle.getCPtr(p), p);
  }

  public int hashCode() {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_hashCode(swigCPtr, this);
  }

  public Voronoi_diagram_2_Face_handle clone() {
    return new Voronoi_diagram_2_Face_handle(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Voronoi_diagram_2_Face_handle other) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_handle_clone__SWIG_1(swigCPtr, this, Voronoi_diagram_2_Face_handle.getCPtr(other), other);
  }

}
