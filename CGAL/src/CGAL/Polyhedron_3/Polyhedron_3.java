/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Polyhedron_3;
import CGAL.Kernel.Point_3;
public class Polyhedron_3 {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Polyhedron_3(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Polyhedron_3 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Polyhedron_3JNI.delete_Polyhedron_3(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Polyhedron_3() {
    this(CGAL_Polyhedron_3JNI.new_Polyhedron_3__SWIG_0(), true);
  }

  public Polyhedron_3(String off_filename) {
    this(CGAL_Polyhedron_3JNI.new_Polyhedron_3__SWIG_1(off_filename), true);
  }

  public Polyhedron_3(long v, long h, long f) {
    this(CGAL_Polyhedron_3JNI.new_Polyhedron_3__SWIG_2(v, h, f), true);
  }

  public void reserve(long c1, long c2, long c3) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_reserve(swigCPtr, this, c1, c2, c3);
  }

  public Polyhedron_3_Halfedge_handle make_tetrahedron() {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_make_tetrahedron__SWIG_0(swigCPtr, this), true);
  }

  public void make_tetrahedron(Polyhedron_3_Halfedge_handle ref) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_make_tetrahedron__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(ref), ref);
  }

  public Polyhedron_3_Halfedge_handle make_tetrahedron(Point_3 c1, Point_3 c2, Point_3 c3, Point_3 c4) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_make_tetrahedron__SWIG_2(swigCPtr, this, Point_3.getCPtr(c1), c1, Point_3.getCPtr(c2), c2, Point_3.getCPtr(c3), c3, Point_3.getCPtr(c4), c4), true);
  }

  public void make_tetrahedron(Point_3 c1, Point_3 c2, Point_3 c3, Point_3 c4, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_make_tetrahedron__SWIG_3(swigCPtr, this, Point_3.getCPtr(c1), c1, Point_3.getCPtr(c2), c2, Point_3.getCPtr(c3), c3, Point_3.getCPtr(c4), c4, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle make_triangle() {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_make_triangle__SWIG_0(swigCPtr, this), true);
  }

  public void make_triangle(Polyhedron_3_Halfedge_handle ref) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_make_triangle__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(ref), ref);
  }

  public Polyhedron_3_Halfedge_handle make_triangle(Point_3 c1, Point_3 c2, Point_3 c3) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_make_triangle__SWIG_2(swigCPtr, this, Point_3.getCPtr(c1), c1, Point_3.getCPtr(c2), c2, Point_3.getCPtr(c3), c3), true);
  }

  public void make_triangle(Point_3 c1, Point_3 c2, Point_3 c3, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_make_triangle__SWIG_3(swigCPtr, this, Point_3.getCPtr(c1), c1, Point_3.getCPtr(c2), c2, Point_3.getCPtr(c3), c3, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public boolean empty() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_empty(swigCPtr, this);
  }

  public long size_of_vertices() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_size_of_vertices(swigCPtr, this);
  }

  public long size_of_halfedges() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_size_of_halfedges(swigCPtr, this);
  }

  public long size_of_facets() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_size_of_facets(swigCPtr, this);
  }

  public long capacity_of_vertices() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_capacity_of_vertices(swigCPtr, this);
  }

  public long capacity_of_halfedges() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_capacity_of_halfedges(swigCPtr, this);
  }

  public long capacity_of_facets() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_capacity_of_facets(swigCPtr, this);
  }

  public long bytes() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_bytes(swigCPtr, this);
  }

  public long bytes_reserved() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_bytes_reserved(swigCPtr, this);
  }

  public Polyhedron_3_Vertex_iterator vertices() {
    return new Polyhedron_3_Vertex_iterator(CGAL_Polyhedron_3JNI.Polyhedron_3_vertices(swigCPtr, this), true);
  }

  public Polyhedron_3_Halfedge_iterator halfedges() {
    return new Polyhedron_3_Halfedge_iterator(CGAL_Polyhedron_3JNI.Polyhedron_3_halfedges(swigCPtr, this), true);
  }

  public Polyhedron_3_Facet_iterator facets() {
    return new Polyhedron_3_Facet_iterator(CGAL_Polyhedron_3JNI.Polyhedron_3_facets(swigCPtr, this), true);
  }

  public Polyhedron_3_Edge_iterator edges() {
    return new Polyhedron_3_Edge_iterator(CGAL_Polyhedron_3JNI.Polyhedron_3_edges(swigCPtr, this), true);
  }

  public Polyhedron_3_Point_iterator points() {
    return new Polyhedron_3_Point_iterator(CGAL_Polyhedron_3JNI.Polyhedron_3_points(swigCPtr, this), true);
  }

  public boolean is_closed() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_is_closed(swigCPtr, this);
  }

  public boolean is_pure_bivalent() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_is_pure_bivalent(swigCPtr, this);
  }

  public boolean is_pure_trivalent() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_is_pure_trivalent(swigCPtr, this);
  }

  public boolean is_pure_triangle() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_is_pure_triangle(swigCPtr, this);
  }

  public boolean is_pure_quad() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_is_pure_quad(swigCPtr, this);
  }

  public boolean is_triangle(Polyhedron_3_Halfedge_handle c) {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_is_triangle(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c);
  }

  public boolean is_tetrahedron(Polyhedron_3_Halfedge_handle c) {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_is_tetrahedron(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c);
  }

  public Polyhedron_3_Halfedge_handle split_facet(Polyhedron_3_Halfedge_handle c1, Polyhedron_3_Halfedge_handle c2) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_split_facet__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c1), c1, Polyhedron_3_Halfedge_handle.getCPtr(c2), c2), true);
  }

  public void split_facet(Polyhedron_3_Halfedge_handle c1, Polyhedron_3_Halfedge_handle c2, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_split_facet__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c1), c1, Polyhedron_3_Halfedge_handle.getCPtr(c2), c2, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle join_facet(Polyhedron_3_Halfedge_handle c) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_join_facet__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c), true);
  }

  public void join_facet(Polyhedron_3_Halfedge_handle c, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_join_facet__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle split_vertex(Polyhedron_3_Halfedge_handle c1, Polyhedron_3_Halfedge_handle c2) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_split_vertex__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c1), c1, Polyhedron_3_Halfedge_handle.getCPtr(c2), c2), true);
  }

  public void split_vertex(Polyhedron_3_Halfedge_handle c1, Polyhedron_3_Halfedge_handle c2, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_split_vertex__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c1), c1, Polyhedron_3_Halfedge_handle.getCPtr(c2), c2, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle join_vertex(Polyhedron_3_Halfedge_handle c) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_join_vertex__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c), true);
  }

  public void join_vertex(Polyhedron_3_Halfedge_handle c, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_join_vertex__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle split_edge(Polyhedron_3_Halfedge_handle c) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_split_edge__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c), true);
  }

  public void split_edge(Polyhedron_3_Halfedge_handle c, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_split_edge__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle flip_edge(Polyhedron_3_Halfedge_handle c) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_flip_edge__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c), true);
  }

  public void flip_edge(Polyhedron_3_Halfedge_handle c, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_flip_edge__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle create_center_vertex(Polyhedron_3_Halfedge_handle c) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_create_center_vertex__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c), true);
  }

  public void create_center_vertex(Polyhedron_3_Halfedge_handle c, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_create_center_vertex__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle erase_center_vertex(Polyhedron_3_Halfedge_handle c) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_erase_center_vertex__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c), true);
  }

  public void erase_center_vertex(Polyhedron_3_Halfedge_handle c, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_erase_center_vertex__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle split_loop(Polyhedron_3_Halfedge_handle c1, Polyhedron_3_Halfedge_handle c2, Polyhedron_3_Halfedge_handle c3) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_split_loop__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c1), c1, Polyhedron_3_Halfedge_handle.getCPtr(c2), c2, Polyhedron_3_Halfedge_handle.getCPtr(c3), c3), true);
  }

  public void split_loop(Polyhedron_3_Halfedge_handle c1, Polyhedron_3_Halfedge_handle c2, Polyhedron_3_Halfedge_handle c3, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_split_loop__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c1), c1, Polyhedron_3_Halfedge_handle.getCPtr(c2), c2, Polyhedron_3_Halfedge_handle.getCPtr(c3), c3, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle join_loop(Polyhedron_3_Halfedge_handle c1, Polyhedron_3_Halfedge_handle c2) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_join_loop__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c1), c1, Polyhedron_3_Halfedge_handle.getCPtr(c2), c2), true);
  }

  public void join_loop(Polyhedron_3_Halfedge_handle c1, Polyhedron_3_Halfedge_handle c2, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_join_loop__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c1), c1, Polyhedron_3_Halfedge_handle.getCPtr(c2), c2, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle make_hole(Polyhedron_3_Halfedge_handle c) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_make_hole__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c), true);
  }

  public void make_hole(Polyhedron_3_Halfedge_handle c, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_make_hole__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle fill_hole(Polyhedron_3_Halfedge_handle c) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_fill_hole__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c), true);
  }

  public void fill_hole(Polyhedron_3_Halfedge_handle c, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_fill_hole__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle add_vertex_and_facet_to_border(Polyhedron_3_Halfedge_handle c1, Polyhedron_3_Halfedge_handle c2) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_add_vertex_and_facet_to_border__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c1), c1, Polyhedron_3_Halfedge_handle.getCPtr(c2), c2), true);
  }

  public void add_vertex_and_facet_to_border(Polyhedron_3_Halfedge_handle c1, Polyhedron_3_Halfedge_handle c2, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_add_vertex_and_facet_to_border__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c1), c1, Polyhedron_3_Halfedge_handle.getCPtr(c2), c2, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public Polyhedron_3_Halfedge_handle add_facet_to_border(Polyhedron_3_Halfedge_handle c1, Polyhedron_3_Halfedge_handle c2) {
    return new Polyhedron_3_Halfedge_handle(CGAL_Polyhedron_3JNI.Polyhedron_3_add_facet_to_border__SWIG_0(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c1), c1, Polyhedron_3_Halfedge_handle.getCPtr(c2), c2), true);
  }

  public void add_facet_to_border(Polyhedron_3_Halfedge_handle c1, Polyhedron_3_Halfedge_handle c2, Polyhedron_3_Halfedge_handle ret) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_add_facet_to_border__SWIG_1(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c1), c1, Polyhedron_3_Halfedge_handle.getCPtr(c2), c2, Polyhedron_3_Halfedge_handle.getCPtr(ret), ret);
  }

  public void erase_facet(Polyhedron_3_Halfedge_handle c) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_erase_facet(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c);
  }

  public void erase_connected_component(Polyhedron_3_Halfedge_handle c) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_erase_connected_component(swigCPtr, this, Polyhedron_3_Halfedge_handle.getCPtr(c), c);
  }

  public long keep_largest_connected_components(long c) {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_keep_largest_connected_components(swigCPtr, this, c);
  }

  public void clear() {
    CGAL_Polyhedron_3JNI.Polyhedron_3_clear(swigCPtr, this);
  }

  public void normalize_border() {
    CGAL_Polyhedron_3JNI.Polyhedron_3_normalize_border(swigCPtr, this);
  }

  public long size_of_border_halfedges() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_size_of_border_halfedges(swigCPtr, this);
  }

  public long size_of_border_edges() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_size_of_border_edges(swigCPtr, this);
  }

  public Polyhedron_3_Halfedge_iterator border_halfedges() {
    return new Polyhedron_3_Halfedge_iterator(CGAL_Polyhedron_3JNI.Polyhedron_3_border_halfedges(swigCPtr, this), true);
  }

  public Polyhedron_3_Halfedge_iterator non_border_halfedges() {
    return new Polyhedron_3_Halfedge_iterator(CGAL_Polyhedron_3JNI.Polyhedron_3_non_border_halfedges(swigCPtr, this), true);
  }

  public Polyhedron_3_Edge_iterator border_edges() {
    return new Polyhedron_3_Edge_iterator(CGAL_Polyhedron_3JNI.Polyhedron_3_border_edges(swigCPtr, this), true);
  }

  public Polyhedron_3_Edge_iterator non_border_edges() {
    return new Polyhedron_3_Edge_iterator(CGAL_Polyhedron_3JNI.Polyhedron_3_non_border_edges(swigCPtr, this), true);
  }

  public void inside_out() {
    CGAL_Polyhedron_3JNI.Polyhedron_3_inside_out(swigCPtr, this);
  }

  public boolean is_valid() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_is_valid(swigCPtr, this);
  }

  public boolean normalized_border_is_valid() {
    return CGAL_Polyhedron_3JNI.Polyhedron_3_normalized_border_is_valid(swigCPtr, this);
  }

  public void delegate(SWIGTYPE_p_Modifier_baseT_Polyhedron_3__t modifier) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_delegate__SWIG_0(swigCPtr, this, SWIGTYPE_p_Modifier_baseT_Polyhedron_3__t.getCPtr(modifier));
  }

  public void delegate(Polyhedron_modifier modifier) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_delegate__SWIG_1(swigCPtr, this, Polyhedron_modifier.getCPtr(modifier), modifier);
  }

  public void write_to_file(String off_filename, int prec) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_write_to_file__SWIG_0(swigCPtr, this, off_filename, prec);
  }

  public void write_to_file(String off_filename) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_write_to_file__SWIG_1(swigCPtr, this, off_filename);
  }

  public Polyhedron_3 clone() {
    return new Polyhedron_3(CGAL_Polyhedron_3JNI.Polyhedron_3_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Polyhedron_3 other) {
    CGAL_Polyhedron_3JNI.Polyhedron_3_clone__SWIG_1(swigCPtr, this, Polyhedron_3.getCPtr(other), other);
  }

}
