/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.HalfedgeDS;
import CGAL.Kernel.Point_2;
public class HalfedgeDS_decorator {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public HalfedgeDS_decorator(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(HalfedgeDS_decorator obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_HalfedgeDSJNI.delete_HalfedgeDS_decorator(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public HalfedgeDS_decorator(HalfedgeDS hds) {
    this(CGAL_HalfedgeDSJNI.new_HalfedgeDS_decorator(HalfedgeDS.getCPtr(hds), hds), true);
  }

  public HDS_Vertex_handle vertices_push_back(Point_2 p) {
    return new HDS_Vertex_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_vertices_push_back__SWIG_0(swigCPtr, this, Point_2.getCPtr(p), p), true);
  }

  public HDS_Vertex_handle vertices_push_back() {
    return new HDS_Vertex_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_vertices_push_back__SWIG_1(swigCPtr, this), true);
  }

  public HDS_Face_handle faces_push_back() {
    return new HDS_Face_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_faces_push_back(swigCPtr, this), true);
  }

  public HDS_Halfedge_handle create_loop() {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_create_loop(swigCPtr, this), true);
  }

  public HDS_Halfedge_handle create_segment() {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_create_segment(swigCPtr, this), true);
  }

  public void vertices_pop_front() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_vertices_pop_front(swigCPtr, this);
  }

  public void vertices_pop_back() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_vertices_pop_back(swigCPtr, this);
  }

  public void vertices_erase(HDS_Vertex_handle c) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_vertices_erase__SWIG_0(swigCPtr, this, HDS_Vertex_handle.getCPtr(c), c);
  }

  public void vertices_erase(HDS_Vertex_handle c1, HDS_Vertex_handle c2) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_vertices_erase__SWIG_1(swigCPtr, this, HDS_Vertex_handle.getCPtr(c1), c1, HDS_Vertex_handle.getCPtr(c2), c2);
  }

  public void faces_pop_front() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_faces_pop_front(swigCPtr, this);
  }

  public void faces_pop_back() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_faces_pop_back(swigCPtr, this);
  }

  public void faces_erase(HDS_Face_handle c) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_faces_erase__SWIG_0(swigCPtr, this, HDS_Face_handle.getCPtr(c), c);
  }

  public void faces_erase(HDS_Face_handle c1, HDS_Face_handle c2) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_faces_erase__SWIG_1(swigCPtr, this, HDS_Face_handle.getCPtr(c1), c1, HDS_Face_handle.getCPtr(c2), c2);
  }

  public void erase_face(HDS_Halfedge_handle c) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_erase_face(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c);
  }

  public void erase_connected_component(HDS_Halfedge_handle c) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_erase_connected_component(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c);
  }

  public int keep_largest_connected_components(int c) {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_keep_largest_connected_components(swigCPtr, this, c);
  }

  public void make_hole(HDS_Halfedge_handle c) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_make_hole(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c);
  }

  public HDS_Halfedge_handle fill_hole(HDS_Halfedge_handle c) {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_fill_hole(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c), true);
  }

  public HDS_Halfedge_handle add_face_to_border(HDS_Halfedge_handle c1, HDS_Halfedge_handle c2) {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_add_face_to_border(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c1), c1, HDS_Halfedge_handle.getCPtr(c2), c2), true);
  }

  public HDS_Halfedge_handle split_face(HDS_Halfedge_handle c1, HDS_Halfedge_handle c2) {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_split_face__SWIG_0(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c1), c1, HDS_Halfedge_handle.getCPtr(c2), c2), true);
  }

  public void split_face(HDS_Halfedge_handle c1, HDS_Halfedge_handle c2, HDS_Halfedge_handle ret) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_split_face__SWIG_1(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c1), c1, HDS_Halfedge_handle.getCPtr(c2), c2, HDS_Halfedge_handle.getCPtr(ret), ret);
  }

  public HDS_Halfedge_handle join_face(HDS_Halfedge_handle c) {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_join_face__SWIG_0(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c), true);
  }

  public void join_face(HDS_Halfedge_handle c, HDS_Halfedge_handle ret) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_join_face__SWIG_1(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c, HDS_Halfedge_handle.getCPtr(ret), ret);
  }

  public HDS_Halfedge_handle split_vertex(HDS_Halfedge_handle c1, HDS_Halfedge_handle c2) {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_split_vertex__SWIG_0(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c1), c1, HDS_Halfedge_handle.getCPtr(c2), c2), true);
  }

  public void split_vertex(HDS_Halfedge_handle c1, HDS_Halfedge_handle c2, HDS_Halfedge_handle ret) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_split_vertex__SWIG_1(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c1), c1, HDS_Halfedge_handle.getCPtr(c2), c2, HDS_Halfedge_handle.getCPtr(ret), ret);
  }

  public HDS_Halfedge_handle join_vertex(HDS_Halfedge_handle c) {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_join_vertex__SWIG_0(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c), true);
  }

  public void join_vertex(HDS_Halfedge_handle c, HDS_Halfedge_handle ret) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_join_vertex__SWIG_1(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c, HDS_Halfedge_handle.getCPtr(ret), ret);
  }

  public HDS_Halfedge_handle create_center_vertex(HDS_Halfedge_handle c) {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_create_center_vertex__SWIG_0(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c), true);
  }

  public void create_center_vertex(HDS_Halfedge_handle c, HDS_Halfedge_handle ret) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_create_center_vertex__SWIG_1(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c, HDS_Halfedge_handle.getCPtr(ret), ret);
  }

  public HDS_Halfedge_handle erase_center_vertex(HDS_Halfedge_handle c) {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_erase_center_vertex__SWIG_0(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c), true);
  }

  public void erase_center_vertex(HDS_Halfedge_handle c, HDS_Halfedge_handle ret) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_erase_center_vertex__SWIG_1(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c), c, HDS_Halfedge_handle.getCPtr(ret), ret);
  }

  public HDS_Halfedge_handle split_loop(HDS_Halfedge_handle c1, HDS_Halfedge_handle c2, HDS_Halfedge_handle c3) {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_split_loop__SWIG_0(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c1), c1, HDS_Halfedge_handle.getCPtr(c2), c2, HDS_Halfedge_handle.getCPtr(c3), c3), true);
  }

  public void split_loop(HDS_Halfedge_handle c1, HDS_Halfedge_handle c2, HDS_Halfedge_handle c3, HDS_Halfedge_handle ret) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_split_loop__SWIG_1(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c1), c1, HDS_Halfedge_handle.getCPtr(c2), c2, HDS_Halfedge_handle.getCPtr(c3), c3, HDS_Halfedge_handle.getCPtr(ret), ret);
  }

  public HDS_Halfedge_handle join_loop(HDS_Halfedge_handle c1, HDS_Halfedge_handle c2) {
    return new HDS_Halfedge_handle(CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_join_loop__SWIG_0(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c1), c1, HDS_Halfedge_handle.getCPtr(c2), c2), true);
  }

  public void join_loop(HDS_Halfedge_handle c1, HDS_Halfedge_handle c2, HDS_Halfedge_handle ret) {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_join_loop__SWIG_1(swigCPtr, this, HDS_Halfedge_handle.getCPtr(c1), c1, HDS_Halfedge_handle.getCPtr(c2), c2, HDS_Halfedge_handle.getCPtr(ret), ret);
  }

  public boolean is_valid(boolean c1, int c2) {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_is_valid__SWIG_0(swigCPtr, this, c1, c2);
  }

  public boolean is_valid(boolean c) {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_is_valid__SWIG_1(swigCPtr, this, c);
  }

  public boolean is_valid() {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_is_valid__SWIG_2(swigCPtr, this);
  }

  public boolean normalized_border_is_valid(boolean c) {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_normalized_border_is_valid__SWIG_0(swigCPtr, this, c);
  }

  public boolean normalized_border_is_valid() {
    return CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_normalized_border_is_valid__SWIG_1(swigCPtr, this);
  }

  public void inside_out() {
    CGAL_HalfedgeDSJNI.HalfedgeDS_decorator_inside_out(swigCPtr, this);
  }

}
