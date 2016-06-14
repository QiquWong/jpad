/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.HalfedgeDS;
import CGAL.Kernel.Point_2;
public class HalfedgeDS {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public HalfedgeDS(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(HalfedgeDS obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_HalfedgeDSJNI.delete_HalfedgeDS(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public HalfedgeDS() {
    this(CGAL_HalfedgeDSJNI.new_HalfedgeDS__SWIG_0(), true);
  }

  public HalfedgeDS(int v, int h, int f) {
    this(CGAL_HalfedgeDSJNI.new_HalfedgeDS__SWIG_1(v, h, f), true);
  }

  public HalfedgeDS(HalfedgeDS hds2) {
    this(CGAL_HalfedgeDSJNI.new_HalfedgeDS__SWIG_2(HalfedgeDS.getCPtr(hds2), hds2), true);
  }

  public void reserve(int c1, int c2, int c3) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_reserve(swigCPtr, this, c1, c2, c3);
  }

  public int size_of_vertices() {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_size_of_vertices(swigCPtr, this);
  }

  public int size_of_halfedges() {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_size_of_halfedges(swigCPtr, this);
  }

  public int size_of_faces() {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_size_of_faces(swigCPtr, this);
  }

  public int capacity_of_vertices() {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_capacity_of_vertices(swigCPtr, this);
  }

  public int capacity_of_halfedges() {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_capacity_of_halfedges(swigCPtr, this);
  }

  public int capacity_of_faces() {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_capacity_of_faces(swigCPtr, this);
  }

  public int bytes() {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_bytes(swigCPtr, this);
  }

  public int bytes_reserved() {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_bytes_reserved(swigCPtr, this);
  }

  public HDS_Vertex_iterator vertices() {
    return new HDS_Vertex_iterator(CGAL_HalfedgeDSJNI.HalfedgeDS_vertices(swigCPtr, this), true);
  }

  public HDS_Halfedge_iterator halfedges() {
    return new HDS_Halfedge_iterator(CGAL_HalfedgeDSJNI.HalfedgeDS_halfedges(swigCPtr, this), true);
  }

  public HDS_Face_iterator faces() {
    return new HDS_Face_iterator(CGAL_HalfedgeDSJNI.HalfedgeDS_faces(swigCPtr, this), true);
  }

  public HDS_Vertex_handle vertices_push_back() {
    return new HDS_Vertex_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_vertices_push_back__SWIG_0(swigCPtr, this), true);
  }

  public HDS_Vertex_handle vertices_push_back(Point_2 p) {
    return new HDS_Vertex_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_vertices_push_back__SWIG_1(swigCPtr, this, Point_2.getCPtr(p), p), true);
  }

  public HDS_Halfedge_handle edges_push_back() {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_edges_push_back(swigCPtr, this), true);
  }

  public HDS_Face_handle faces_push_back() {
    return new HDS_Face_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_faces_push_back(swigCPtr, this), true);
  }

  public void vertices_pop_front() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_vertices_pop_front(swigCPtr, this);
  }

  public void vertices_pop_back() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_vertices_pop_back(swigCPtr, this);
  }

  public void vertices_erase(HDS_Vertex_handle c) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_vertices_erase__SWIG_0(swigCPtr, this, HDS_Vertex_handle.getCPtr(c), c);
  }

  public void vertices_erase(HDS_Vertex_handle c1, HDS_Vertex_handle c2) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_vertices_erase__SWIG_1(swigCPtr, this, HDS_Vertex_handle.getCPtr(c1), c1, HDS_Vertex_handle.getCPtr(c2), c2);
  }

  public void edges_pop_front() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_edges_pop_front(swigCPtr, this);
  }

  public void edges_pop_back() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_edges_pop_back(swigCPtr, this);
  }

  public void edges_erase(HDS_Halfedge_handle c) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_edges_erase__SWIG_0(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c);
  }

  public void edges_erase(HDS_Halfedge_handle c1, HDS_Halfedge_handle c2) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_edges_erase__SWIG_1(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c1), c1, HDS_Halfedge_handle.getCPtr(c2), c2);
  }

  public void faces_pop_front() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_faces_pop_front(swigCPtr, this);
  }

  public void faces_pop_back() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_faces_pop_back(swigCPtr, this);
  }

  public void faces_erase(HDS_Face_handle c) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_faces_erase__SWIG_0(swigCPtr, this, HDS_Face_handle.getCPtr(c), c);
  }

  public void faces_erase(HDS_Face_handle c1, HDS_Face_handle c2) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_faces_erase__SWIG_1(swigCPtr, this, HDS_Face_handle.getCPtr(c1), c1, HDS_Face_handle.getCPtr(c2), c2);
  }

  public void vertices_clear() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_vertices_clear(swigCPtr, this);
  }

  public void edges_clear() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_edges_clear(swigCPtr, this);
  }

  public void faces_clear() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_faces_clear(swigCPtr, this);
  }

  public void clear() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_clear(swigCPtr, this);
  }

  public void normalize_border() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_normalize_border(swigCPtr, this);
  }

  public int size_of_border_halfedges() {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_size_of_border_halfedges(swigCPtr, this);
  }

  public int size_of_border_edges() {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_size_of_border_edges(swigCPtr, this);
  }

  public HDS_Halfedge_iterator border_halfedges() {
    return new HDS_Halfedge_iterator(CGAL_HalfedgeDSJNI.HalfedgeDS_border_halfedges(swigCPtr, this), true);
  }

  public HalfedgeDS clone() {
    return new HalfedgeDS(CGAL_HalfedgeDSJNI.HalfedgeDS_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(HalfedgeDS other) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_clone__SWIG_1(swigCPtr, this, HalfedgeDS.getCPtr(other), other);
  }

  public void delegate(HalfedgeDS_modifier modifier) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_delegate(swigCPtr, this, HalfedgeDS_modifier.getCPtr(modifier), modifier);
  }

}
