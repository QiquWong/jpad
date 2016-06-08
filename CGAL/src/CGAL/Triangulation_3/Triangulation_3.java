/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Triangulation_3;
import CGAL.Kernel.Point_3;import CGAL.Kernel.Segment_3; import CGAL.Kernel.Triangle_3; import CGAL.Kernel.Tetrahedron_3; import CGAL.Kernel.Ref_int; import CGAL.Kernel.Bounded_side; import java.util.Iterator; import java.util.Collection;
public class Triangulation_3 {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Triangulation_3(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Triangulation_3 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Triangulation_3JNI.delete_Triangulation_3(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Triangulation_3() {
    this(CGAL_Triangulation_3JNI.new_Triangulation_3__SWIG_0(), true);
  }

  public Triangulation_3(Iterator<Point_3> range) {
    this(CGAL_Triangulation_3JNI.new_Triangulation_3__SWIG_1(range), true);
  }

  public void clear() {
    CGAL_Triangulation_3JNI.Triangulation_3_clear(swigCPtr, this);
  }

  public int dimension() {
    return CGAL_Triangulation_3JNI.Triangulation_3_dimension(swigCPtr, this);
  }

  public int number_of_vertices() {
    return CGAL_Triangulation_3JNI.Triangulation_3_number_of_vertices(swigCPtr, this);
  }

  public int number_of_cells() {
    return CGAL_Triangulation_3JNI.Triangulation_3_number_of_cells(swigCPtr, this);
  }

  public Triangulation_3_Vertex_handle infinite_vertex() {
    return new Triangulation_3_Vertex_handle(CGAL_Triangulation_3JNI.Triangulation_3_infinite_vertex__SWIG_0(swigCPtr, this), true);
  }

  public void infinite_vertex(Triangulation_3_Vertex_handle ref) {
    CGAL_Triangulation_3JNI.Triangulation_3_infinite_vertex__SWIG_1(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(ref), ref);
  }

  public Triangulation_3_Cell_handle infinite_cell() {
    return new Triangulation_3_Cell_handle(CGAL_Triangulation_3JNI.Triangulation_3_infinite_cell__SWIG_0(swigCPtr, this), true);
  }

  public void infinite_cell(Triangulation_3_Cell_handle ref) {
    CGAL_Triangulation_3JNI.Triangulation_3_infinite_cell__SWIG_1(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(ref), ref);
  }

  public int number_of_facets() {
    return CGAL_Triangulation_3JNI.Triangulation_3_number_of_facets(swigCPtr, this);
  }

  public int number_of_edges() {
    return CGAL_Triangulation_3JNI.Triangulation_3_number_of_edges(swigCPtr, this);
  }

  public int number_of_finite_cells() {
    return CGAL_Triangulation_3JNI.Triangulation_3_number_of_finite_cells(swigCPtr, this);
  }

  public int number_of_finite_facets() {
    return CGAL_Triangulation_3JNI.Triangulation_3_number_of_finite_facets(swigCPtr, this);
  }

  public int number_of_finite_edges() {
    return CGAL_Triangulation_3JNI.Triangulation_3_number_of_finite_edges(swigCPtr, this);
  }

  public boolean is_infinite(Triangulation_3_Vertex_handle c) {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_infinite__SWIG_0(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(c), c);
  }

  public boolean is_infinite(Triangulation_3_Cell_handle c) {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_infinite__SWIG_1(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c), c);
  }

  public boolean is_infinite(Triangulation_3_Cell_handle c1, int c2) {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_infinite__SWIG_2(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2);
  }

  public boolean is_infinite(Triangulation_3_Facet c) {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_infinite__SWIG_3(swigCPtr, this, Triangulation_3_Facet.getCPtr(c), c);
  }

  public boolean is_infinite(Triangulation_3_Cell_handle c1, int c2, int c3) {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_infinite__SWIG_4(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3);
  }

  public boolean is_infinite(Triangulation_3_Edge c) {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_infinite__SWIG_5(swigCPtr, this, Triangulation_3_Edge.getCPtr(c), c);
  }

  public boolean is_vertex(Triangulation_3_Vertex_handle c) {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_vertex(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(c), c);
  }

  public boolean is_cell(Triangulation_3_Cell_handle c) {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_cell__SWIG_0(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c), c);
  }

  public boolean has_vertex(Triangulation_3_Facet c1, Triangulation_3_Vertex_handle c2) {
    return CGAL_Triangulation_3JNI.Triangulation_3_has_vertex__SWIG_0(swigCPtr, this, Triangulation_3_Facet.getCPtr(c1), c1, Triangulation_3_Vertex_handle.getCPtr(c2), c2);
  }

  public boolean has_vertex(Triangulation_3_Cell_handle c1, int c2, Triangulation_3_Vertex_handle c3) {
    return CGAL_Triangulation_3JNI.Triangulation_3_has_vertex__SWIG_1(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, Triangulation_3_Vertex_handle.getCPtr(c3), c3);
  }

  public boolean are_equal(Triangulation_3_Cell_handle c1, int c2, Triangulation_3_Cell_handle c3, int c4) {
    return CGAL_Triangulation_3JNI.Triangulation_3_are_equal__SWIG_0(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, Triangulation_3_Cell_handle.getCPtr(c3), c3, c4);
  }

  public boolean are_equal(Triangulation_3_Facet c1, Triangulation_3_Facet c2) {
    return CGAL_Triangulation_3JNI.Triangulation_3_are_equal__SWIG_1(swigCPtr, this, Triangulation_3_Facet.getCPtr(c1), c1, Triangulation_3_Facet.getCPtr(c2), c2);
  }

  public boolean are_equal(Triangulation_3_Facet c1, Triangulation_3_Cell_handle c2, int c3) {
    return CGAL_Triangulation_3JNI.Triangulation_3_are_equal__SWIG_2(swigCPtr, this, Triangulation_3_Facet.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2, c3);
  }

  public Triangulation_3_Cell_handle locate(Point_3 c) {
    return new Triangulation_3_Cell_handle(CGAL_Triangulation_3JNI.Triangulation_3_locate__SWIG_0(swigCPtr, this, Point_3.getCPtr(c), c), true);
  }

  public Triangulation_3_Cell_handle locate(Point_3 c1, Triangulation_3_Cell_handle c2) {
    return new Triangulation_3_Cell_handle(CGAL_Triangulation_3JNI.Triangulation_3_locate__SWIG_1(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2), true);
  }

  public void locate(Point_3 c1, Triangulation_3_Cell_handle c2, Triangulation_3_Cell_handle ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_locate__SWIG_2(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2, Triangulation_3_Cell_handle.getCPtr(ret), ret);
  }

  public Triangulation_3_Cell_handle locate(Point_3 c1, Triangulation_3_Vertex_handle c2) {
    return new Triangulation_3_Cell_handle(CGAL_Triangulation_3JNI.Triangulation_3_locate__SWIG_3(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Vertex_handle.getCPtr(c2), c2), true);
  }

  public void locate(Point_3 c1, Triangulation_3_Vertex_handle c2, Triangulation_3_Cell_handle ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_locate__SWIG_4(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Vertex_handle.getCPtr(c2), c2, Triangulation_3_Cell_handle.getCPtr(ret), ret);
  }

  public Triangulation_3_Cell_handle locate(Point_3 query, Ref_Locate_type_3 lt, Ref_int li, Ref_int lj) {
    return new Triangulation_3_Cell_handle(CGAL_Triangulation_3JNI.Triangulation_3_locate__SWIG_5(swigCPtr, this, Point_3.getCPtr(query), query, Ref_Locate_type_3.getCPtr(lt), lt, Ref_int.getCPtr(li), li, Ref_int.getCPtr(lj), lj), true);
  }

  public Triangulation_3_Cell_handle locate(Point_3 query, Ref_Locate_type_3 lt, Ref_int li, Ref_int lj, Triangulation_3_Cell_handle cell) {
    return new Triangulation_3_Cell_handle(CGAL_Triangulation_3JNI.Triangulation_3_locate__SWIG_6(swigCPtr, this, Point_3.getCPtr(query), query, Ref_Locate_type_3.getCPtr(lt), lt, Ref_int.getCPtr(li), li, Ref_int.getCPtr(lj), lj, Triangulation_3_Cell_handle.getCPtr(cell), cell), true);
  }

  public Triangulation_3_Cell_handle locate(Point_3 query, Ref_Locate_type_3 lt, Ref_int li, Ref_int lj, Triangulation_3_Vertex_handle hint) {
    return new Triangulation_3_Cell_handle(CGAL_Triangulation_3JNI.Triangulation_3_locate__SWIG_7(swigCPtr, this, Point_3.getCPtr(query), query, Ref_Locate_type_3.getCPtr(lt), lt, Ref_int.getCPtr(li), li, Ref_int.getCPtr(lj), lj, Triangulation_3_Vertex_handle.getCPtr(hint), hint), true);
  }

  public Bounded_side side_of_cell(Point_3 p, Triangulation_3_Cell_handle c, Ref_Locate_type_3 lt, Ref_int li, Ref_int lj) {
    return Bounded_side.swigToEnum(CGAL_Triangulation_3JNI.Triangulation_3_side_of_cell(swigCPtr, this, Point_3.getCPtr(p), p, Triangulation_3_Cell_handle.getCPtr(c), c, Ref_Locate_type_3.getCPtr(lt), lt, Ref_int.getCPtr(li), li, Ref_int.getCPtr(lj), lj));
  }

  public Bounded_side side_of_facet(Point_3 p, Triangulation_3_Facet f, Ref_Locate_type_3 lt, Ref_int li, Ref_int lj) {
    return Bounded_side.swigToEnum(CGAL_Triangulation_3JNI.Triangulation_3_side_of_facet(swigCPtr, this, Point_3.getCPtr(p), p, Triangulation_3_Facet.getCPtr(f), f, Ref_Locate_type_3.getCPtr(lt), lt, Ref_int.getCPtr(li), li, Ref_int.getCPtr(lj), lj));
  }

  public Bounded_side side_of_edge(Point_3 p, Triangulation_3_Edge e, Ref_Locate_type_3 lt, Ref_int li) {
    return Bounded_side.swigToEnum(CGAL_Triangulation_3JNI.Triangulation_3_side_of_edge__SWIG_0(swigCPtr, this, Point_3.getCPtr(p), p, Triangulation_3_Edge.getCPtr(e), e, Ref_Locate_type_3.getCPtr(lt), lt, Ref_int.getCPtr(li), li));
  }

  public Bounded_side side_of_edge(Point_3 p, Triangulation_3_Cell_handle c, Ref_Locate_type_3 lt, Ref_int li) {
    return Bounded_side.swigToEnum(CGAL_Triangulation_3JNI.Triangulation_3_side_of_edge__SWIG_1(swigCPtr, this, Point_3.getCPtr(p), p, Triangulation_3_Cell_handle.getCPtr(c), c, Ref_Locate_type_3.getCPtr(lt), lt, Ref_int.getCPtr(li), li));
  }

  public boolean flip(Triangulation_3_Edge c) {
    return CGAL_Triangulation_3JNI.Triangulation_3_flip__SWIG_0(swigCPtr, this, Triangulation_3_Edge.getCPtr(c), c);
  }

  public boolean flip(Triangulation_3_Cell_handle c1, int c2, int c3) {
    return CGAL_Triangulation_3JNI.Triangulation_3_flip__SWIG_1(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3);
  }

  public void flip_flippable(Triangulation_3_Edge c) {
    CGAL_Triangulation_3JNI.Triangulation_3_flip_flippable__SWIG_0(swigCPtr, this, Triangulation_3_Edge.getCPtr(c), c);
  }

  public void flip_flippable(Triangulation_3_Cell_handle c1, int c2, int c3) {
    CGAL_Triangulation_3JNI.Triangulation_3_flip_flippable__SWIG_1(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3);
  }

  public boolean flip(Triangulation_3_Facet c) {
    return CGAL_Triangulation_3JNI.Triangulation_3_flip__SWIG_2(swigCPtr, this, Triangulation_3_Facet.getCPtr(c), c);
  }

  public boolean flip(Triangulation_3_Cell_handle c1, int c2) {
    return CGAL_Triangulation_3JNI.Triangulation_3_flip__SWIG_3(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2);
  }

  public void flip_flippable(Triangulation_3_Facet c) {
    CGAL_Triangulation_3JNI.Triangulation_3_flip_flippable__SWIG_2(swigCPtr, this, Triangulation_3_Facet.getCPtr(c), c);
  }

  public void flip_flippable(Triangulation_3_Cell_handle c1, int c2) {
    CGAL_Triangulation_3JNI.Triangulation_3_flip_flippable__SWIG_3(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2);
  }

  public Triangulation_3_Vertex_handle insert(Point_3 c) {
    return new Triangulation_3_Vertex_handle(CGAL_Triangulation_3JNI.Triangulation_3_insert__SWIG_0(swigCPtr, this, Point_3.getCPtr(c), c), true);
  }

  public Triangulation_3_Vertex_handle insert(Point_3 c1, Triangulation_3_Cell_handle c2) {
    return new Triangulation_3_Vertex_handle(CGAL_Triangulation_3JNI.Triangulation_3_insert__SWIG_1(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2), true);
  }

  public void insert(Point_3 c1, Triangulation_3_Cell_handle c2, Triangulation_3_Vertex_handle ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_insert__SWIG_2(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2, Triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public Triangulation_3_Vertex_handle insert(Point_3 c1, Triangulation_3_Vertex_handle c2) {
    return new Triangulation_3_Vertex_handle(CGAL_Triangulation_3JNI.Triangulation_3_insert__SWIG_3(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Vertex_handle.getCPtr(c2), c2), true);
  }

  public void insert(Point_3 c1, Triangulation_3_Vertex_handle c2, Triangulation_3_Vertex_handle ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_insert__SWIG_4(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Vertex_handle.getCPtr(c2), c2, Triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public int insert(Iterator<Point_3> range) {
    return CGAL_Triangulation_3JNI.Triangulation_3_insert__SWIG_5(swigCPtr, this, range);
  }

  public Triangulation_3_Vertex_handle insert_in_cell(Point_3 c1, Triangulation_3_Cell_handle c2) {
    return new Triangulation_3_Vertex_handle(CGAL_Triangulation_3JNI.Triangulation_3_insert_in_cell__SWIG_0(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2), true);
  }

  public void insert_in_cell(Point_3 c1, Triangulation_3_Cell_handle c2, Triangulation_3_Vertex_handle ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_insert_in_cell__SWIG_1(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2, Triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public Triangulation_3_Vertex_handle insert_in_facet(Point_3 c1, Triangulation_3_Facet c2) {
    return new Triangulation_3_Vertex_handle(CGAL_Triangulation_3JNI.Triangulation_3_insert_in_facet__SWIG_0(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Facet.getCPtr(c2), c2), true);
  }

  public void insert_in_facet(Point_3 c1, Triangulation_3_Facet c2, Triangulation_3_Vertex_handle ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_insert_in_facet__SWIG_1(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Facet.getCPtr(c2), c2, Triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public Triangulation_3_Vertex_handle insert_in_facet(Point_3 c1, Triangulation_3_Cell_handle c2, int c3) {
    return new Triangulation_3_Vertex_handle(CGAL_Triangulation_3JNI.Triangulation_3_insert_in_facet__SWIG_2(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2, c3), true);
  }

  public void insert_in_facet(Point_3 c1, Triangulation_3_Cell_handle c2, int c3, Triangulation_3_Vertex_handle ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_insert_in_facet__SWIG_3(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2, c3, Triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public Triangulation_3_Vertex_handle insert_in_edge(Point_3 c1, Triangulation_3_Edge c2) {
    return new Triangulation_3_Vertex_handle(CGAL_Triangulation_3JNI.Triangulation_3_insert_in_edge__SWIG_0(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Edge.getCPtr(c2), c2), true);
  }

  public void insert_in_edge(Point_3 c1, Triangulation_3_Edge c2, Triangulation_3_Vertex_handle ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_insert_in_edge__SWIG_1(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Edge.getCPtr(c2), c2, Triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public Triangulation_3_Vertex_handle insert_in_edge(Point_3 c1, Triangulation_3_Cell_handle c2, int c3, int c4) {
    return new Triangulation_3_Vertex_handle(CGAL_Triangulation_3JNI.Triangulation_3_insert_in_edge__SWIG_2(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2, c3, c4), true);
  }

  public void insert_in_edge(Point_3 c1, Triangulation_3_Cell_handle c2, int c3, int c4, Triangulation_3_Vertex_handle ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_insert_in_edge__SWIG_3(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2, c3, c4, Triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public Triangulation_3_Vertex_handle insert_outside_convex_hull(Point_3 c1, Triangulation_3_Cell_handle c2) {
    return new Triangulation_3_Vertex_handle(CGAL_Triangulation_3JNI.Triangulation_3_insert_outside_convex_hull__SWIG_0(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2), true);
  }

  public void insert_outside_convex_hull(Point_3 c1, Triangulation_3_Cell_handle c2, Triangulation_3_Vertex_handle ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_insert_outside_convex_hull__SWIG_1(swigCPtr, this, Point_3.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2, Triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public Triangulation_3_Vertex_handle insert_outside_affine_hull(Point_3 c) {
    return new Triangulation_3_Vertex_handle(CGAL_Triangulation_3JNI.Triangulation_3_insert_outside_affine_hull__SWIG_0(swigCPtr, this, Point_3.getCPtr(c), c), true);
  }

  public void insert_outside_affine_hull(Point_3 c, Triangulation_3_Vertex_handle ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_insert_outside_affine_hull__SWIG_1(swigCPtr, this, Point_3.getCPtr(c), c, Triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public Triangulation_3_Finite_vertices_iterator finite_vertices() {
    return new Triangulation_3_Finite_vertices_iterator(CGAL_Triangulation_3JNI.Triangulation_3_finite_vertices(swigCPtr, this), true);
  }

  public Triangulation_3_Finite_edges_iterator finite_edges() {
    return new Triangulation_3_Finite_edges_iterator(CGAL_Triangulation_3JNI.Triangulation_3_finite_edges(swigCPtr, this), true);
  }

  public Triangulation_3_Finite_facets_iterator finite_facets() {
    return new Triangulation_3_Finite_facets_iterator(CGAL_Triangulation_3JNI.Triangulation_3_finite_facets(swigCPtr, this), true);
  }

  public Triangulation_3_Finite_cells_iterator finite_cells() {
    return new Triangulation_3_Finite_cells_iterator(CGAL_Triangulation_3JNI.Triangulation_3_finite_cells(swigCPtr, this), true);
  }

  public Triangulation_3_All_vertices_iterator all_vertices() {
    return new Triangulation_3_All_vertices_iterator(CGAL_Triangulation_3JNI.Triangulation_3_all_vertices(swigCPtr, this), true);
  }

  public Triangulation_3_All_edges_iterator all_edges() {
    return new Triangulation_3_All_edges_iterator(CGAL_Triangulation_3JNI.Triangulation_3_all_edges(swigCPtr, this), true);
  }

  public Triangulation_3_All_facets_iterator all_facets() {
    return new Triangulation_3_All_facets_iterator(CGAL_Triangulation_3JNI.Triangulation_3_all_facets(swigCPtr, this), true);
  }

  public Triangulation_3_All_cells_iterator all_cells() {
    return new Triangulation_3_All_cells_iterator(CGAL_Triangulation_3JNI.Triangulation_3_all_cells(swigCPtr, this), true);
  }

  public Triangulation_3_Point_iterator points() {
    return new Triangulation_3_Point_iterator(CGAL_Triangulation_3JNI.Triangulation_3_points(swigCPtr, this), true);
  }

  public Triangulation_3_Cell_circulator incident_cells(Triangulation_3_Edge c) {
    return new Triangulation_3_Cell_circulator(CGAL_Triangulation_3JNI.Triangulation_3_incident_cells__SWIG_0(swigCPtr, this, Triangulation_3_Edge.getCPtr(c), c), true);
  }

  public void incident_cells(Triangulation_3_Edge c, Triangulation_3_Cell_circulator ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_incident_cells__SWIG_1(swigCPtr, this, Triangulation_3_Edge.getCPtr(c), c, Triangulation_3_Cell_circulator.getCPtr(ret), ret);
  }

  public Triangulation_3_Cell_circulator incident_cells(Triangulation_3_Cell_handle c1, int c2, int c3) {
    return new Triangulation_3_Cell_circulator(CGAL_Triangulation_3JNI.Triangulation_3_incident_cells__SWIG_2(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3), true);
  }

  public void incident_cells(Triangulation_3_Cell_handle c1, int c2, int c3, Triangulation_3_Cell_circulator ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_incident_cells__SWIG_3(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3, Triangulation_3_Cell_circulator.getCPtr(ret), ret);
  }

  public Triangulation_3_Cell_circulator incident_cells(Triangulation_3_Edge c1, Triangulation_3_Cell_handle c2) {
    return new Triangulation_3_Cell_circulator(CGAL_Triangulation_3JNI.Triangulation_3_incident_cells__SWIG_4(swigCPtr, this, Triangulation_3_Edge.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2), true);
  }

  public void incident_cells(Triangulation_3_Edge c1, Triangulation_3_Cell_handle c2, Triangulation_3_Cell_circulator ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_incident_cells__SWIG_5(swigCPtr, this, Triangulation_3_Edge.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2, Triangulation_3_Cell_circulator.getCPtr(ret), ret);
  }

  public Triangulation_3_Cell_circulator incident_cells(Triangulation_3_Cell_handle c1, int c2, int c3, Triangulation_3_Cell_handle c4) {
    return new Triangulation_3_Cell_circulator(CGAL_Triangulation_3JNI.Triangulation_3_incident_cells__SWIG_6(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3, Triangulation_3_Cell_handle.getCPtr(c4), c4), true);
  }

  public void incident_cells(Triangulation_3_Cell_handle c1, int c2, int c3, Triangulation_3_Cell_handle c4, Triangulation_3_Cell_circulator ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_incident_cells__SWIG_7(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3, Triangulation_3_Cell_handle.getCPtr(c4), c4, Triangulation_3_Cell_circulator.getCPtr(ret), ret);
  }

  public Triangulation_3_Facet_circulator incident_facets(Triangulation_3_Edge c) {
    return new Triangulation_3_Facet_circulator(CGAL_Triangulation_3JNI.Triangulation_3_incident_facets__SWIG_0(swigCPtr, this, Triangulation_3_Edge.getCPtr(c), c), true);
  }

  public void incident_facets(Triangulation_3_Edge c, Triangulation_3_Facet_circulator ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_incident_facets__SWIG_1(swigCPtr, this, Triangulation_3_Edge.getCPtr(c), c, Triangulation_3_Facet_circulator.getCPtr(ret), ret);
  }

  public Triangulation_3_Facet_circulator incident_facets(Triangulation_3_Cell_handle c1, int c2, int c3) {
    return new Triangulation_3_Facet_circulator(CGAL_Triangulation_3JNI.Triangulation_3_incident_facets__SWIG_2(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3), true);
  }

  public void incident_facets(Triangulation_3_Cell_handle c1, int c2, int c3, Triangulation_3_Facet_circulator ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_incident_facets__SWIG_3(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3, Triangulation_3_Facet_circulator.getCPtr(ret), ret);
  }

  public Triangulation_3_Facet_circulator incident_facets(Triangulation_3_Edge c1, Triangulation_3_Facet c2) {
    return new Triangulation_3_Facet_circulator(CGAL_Triangulation_3JNI.Triangulation_3_incident_facets__SWIG_4(swigCPtr, this, Triangulation_3_Edge.getCPtr(c1), c1, Triangulation_3_Facet.getCPtr(c2), c2), true);
  }

  public void incident_facets(Triangulation_3_Edge c1, Triangulation_3_Facet c2, Triangulation_3_Facet_circulator ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_incident_facets__SWIG_5(swigCPtr, this, Triangulation_3_Edge.getCPtr(c1), c1, Triangulation_3_Facet.getCPtr(c2), c2, Triangulation_3_Facet_circulator.getCPtr(ret), ret);
  }

  public Triangulation_3_Facet_circulator incident_facets(Triangulation_3_Edge c1, Triangulation_3_Cell_handle c2, int c3) {
    return new Triangulation_3_Facet_circulator(CGAL_Triangulation_3JNI.Triangulation_3_incident_facets__SWIG_6(swigCPtr, this, Triangulation_3_Edge.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2, c3), true);
  }

  public void incident_facets(Triangulation_3_Edge c1, Triangulation_3_Cell_handle c2, int c3, Triangulation_3_Facet_circulator ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_incident_facets__SWIG_7(swigCPtr, this, Triangulation_3_Edge.getCPtr(c1), c1, Triangulation_3_Cell_handle.getCPtr(c2), c2, c3, Triangulation_3_Facet_circulator.getCPtr(ret), ret);
  }

  public Triangulation_3_Facet_circulator incident_facets(Triangulation_3_Cell_handle c1, int c2, int c3, Triangulation_3_Facet c4) {
    return new Triangulation_3_Facet_circulator(CGAL_Triangulation_3JNI.Triangulation_3_incident_facets__SWIG_8(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3, Triangulation_3_Facet.getCPtr(c4), c4), true);
  }

  public void incident_facets(Triangulation_3_Cell_handle c1, int c2, int c3, Triangulation_3_Facet c4, Triangulation_3_Facet_circulator ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_incident_facets__SWIG_9(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3, Triangulation_3_Facet.getCPtr(c4), c4, Triangulation_3_Facet_circulator.getCPtr(ret), ret);
  }

  public Triangulation_3_Facet_circulator incident_facets(Triangulation_3_Cell_handle c1, int c2, int c3, Triangulation_3_Cell_handle c4, int c5) {
    return new Triangulation_3_Facet_circulator(CGAL_Triangulation_3JNI.Triangulation_3_incident_facets__SWIG_10(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3, Triangulation_3_Cell_handle.getCPtr(c4), c4, c5), true);
  }

  public void incident_cells(Triangulation_3_Vertex_handle v, Collection<Triangulation_3_Cell_handle> out) {
    CGAL_Triangulation_3JNI.Triangulation_3_incident_cells__SWIG_8(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(v), v, out);
  }

  public void finite_incident_cells(Triangulation_3_Vertex_handle v, Collection<Triangulation_3_Cell_handle> out) {
    CGAL_Triangulation_3JNI.Triangulation_3_finite_incident_cells(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(v), v, out);
  }

  public void incident_facets(Triangulation_3_Vertex_handle v, Collection<Triangulation_3_Facet> out) {
    CGAL_Triangulation_3JNI.Triangulation_3_incident_facets__SWIG_11(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(v), v, out);
  }

  public void finite_incident_facets(Triangulation_3_Vertex_handle v, Collection<Triangulation_3_Facet> out) {
    CGAL_Triangulation_3JNI.Triangulation_3_finite_incident_facets(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(v), v, out);
  }

  public void incident_edges(Triangulation_3_Vertex_handle v, Collection<Triangulation_3_Edge> out) {
    CGAL_Triangulation_3JNI.Triangulation_3_incident_edges(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(v), v, out);
  }

  public void finite_incident_edges(Triangulation_3_Vertex_handle v, Collection<Triangulation_3_Edge> out) {
    CGAL_Triangulation_3JNI.Triangulation_3_finite_incident_edges(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(v), v, out);
  }

  public void adjacent_vertices(Triangulation_3_Vertex_handle v, Collection<Triangulation_3_Vertex_handle> out) {
    CGAL_Triangulation_3JNI.Triangulation_3_adjacent_vertices(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(v), v, out);
  }

  public void finite_adjacent_vertices(Triangulation_3_Vertex_handle v, Collection<Triangulation_3_Vertex_handle> out) {
    CGAL_Triangulation_3JNI.Triangulation_3_finite_adjacent_vertices(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(v), v, out);
  }

  public int degree(Triangulation_3_Vertex_handle c) {
    return CGAL_Triangulation_3JNI.Triangulation_3_degree(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(c), c);
  }

  public int mirror_index(Triangulation_3_Cell_handle c1, int c2) {
    return CGAL_Triangulation_3JNI.Triangulation_3_mirror_index(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2);
  }

  public Triangulation_3_Vertex_handle mirror_vertex(Triangulation_3_Cell_handle c1, int c2) {
    return new Triangulation_3_Vertex_handle(CGAL_Triangulation_3JNI.Triangulation_3_mirror_vertex__SWIG_0(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2), true);
  }

  public void mirror_vertex(Triangulation_3_Cell_handle c1, int c2, Triangulation_3_Vertex_handle ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_mirror_vertex__SWIG_1(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, Triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public Triangulation_3_Facet mirror_facet(Triangulation_3_Facet c) {
    return new Triangulation_3_Facet(CGAL_Triangulation_3JNI.Triangulation_3_mirror_facet__SWIG_0(swigCPtr, this, Triangulation_3_Facet.getCPtr(c), c), true);
  }

  public void mirror_facet(Triangulation_3_Facet c, Triangulation_3_Facet ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_mirror_facet__SWIG_1(swigCPtr, this, Triangulation_3_Facet.getCPtr(c), c, Triangulation_3_Facet.getCPtr(ret), ret);
  }

  public Tetrahedron_3 tetrahedron(Triangulation_3_Cell_handle c) {
    return new Tetrahedron_3(CGAL_Triangulation_3JNI.Triangulation_3_tetrahedron__SWIG_0(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c), c), true);
  }

  public void tetrahedron(Triangulation_3_Cell_handle c, Tetrahedron_3 ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_tetrahedron__SWIG_1(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c), c, Tetrahedron_3.getCPtr(ret), ret);
  }

  public Triangle_3 triangle(Triangulation_3_Cell_handle c1, int c2) {
    return new Triangle_3(CGAL_Triangulation_3JNI.Triangulation_3_triangle__SWIG_0(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2), true);
  }

  public void triangle(Triangulation_3_Cell_handle c1, int c2, Triangle_3 ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_triangle__SWIG_1(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, Triangle_3.getCPtr(ret), ret);
  }

  public Triangle_3 triangle(Triangulation_3_Facet c) {
    return new Triangle_3(CGAL_Triangulation_3JNI.Triangulation_3_triangle__SWIG_2(swigCPtr, this, Triangulation_3_Facet.getCPtr(c), c), true);
  }

  public void triangle(Triangulation_3_Facet c, Triangle_3 ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_triangle__SWIG_3(swigCPtr, this, Triangulation_3_Facet.getCPtr(c), c, Triangle_3.getCPtr(ret), ret);
  }

  public Segment_3 segment(Triangulation_3_Edge c) {
    return new Segment_3(CGAL_Triangulation_3JNI.Triangulation_3_segment__SWIG_0(swigCPtr, this, Triangulation_3_Edge.getCPtr(c), c), true);
  }

  public void segment(Triangulation_3_Edge c, Segment_3 ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_segment__SWIG_1(swigCPtr, this, Triangulation_3_Edge.getCPtr(c), c, Segment_3.getCPtr(ret), ret);
  }

  public Segment_3 segment(Triangulation_3_Cell_handle c1, int c2, int c3) {
    return new Segment_3(CGAL_Triangulation_3JNI.Triangulation_3_segment__SWIG_2(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3), true);
  }

  public void segment(Triangulation_3_Cell_handle c1, int c2, int c3, Segment_3 ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_segment__SWIG_3(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3, Segment_3.getCPtr(ret), ret);
  }

  public Point_3 point(Triangulation_3_Cell_handle c1, int c2) {
    return new Point_3(CGAL_Triangulation_3JNI.Triangulation_3_point__SWIG_0(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2), true);
  }

  public void point(Triangulation_3_Cell_handle c1, int c2, Point_3 ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_point__SWIG_1(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2, Point_3.getCPtr(ret), ret);
  }

  public Point_3 point(Triangulation_3_Vertex_handle c) {
    return new Point_3(CGAL_Triangulation_3JNI.Triangulation_3_point__SWIG_2(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(c), c), true);
  }

  public void point(Triangulation_3_Vertex_handle c, Point_3 ret) {
    CGAL_Triangulation_3JNI.Triangulation_3_point__SWIG_3(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(c), c, Point_3.getCPtr(ret), ret);
  }

  public boolean is_valid() {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_valid__SWIG_0(swigCPtr, this);
  }

  public boolean is_valid(boolean c) {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_valid__SWIG_1(swigCPtr, this, c);
  }

  public boolean is_valid(Triangulation_3_Cell_handle c) {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_valid__SWIG_2(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c), c);
  }

  public boolean is_valid(Triangulation_3_Cell_handle c1, boolean c2) {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_valid__SWIG_3(swigCPtr, this, Triangulation_3_Cell_handle.getCPtr(c1), c1, c2);
  }

  public String toString() {
    return CGAL_Triangulation_3JNI.Triangulation_3_toString(swigCPtr, this);
  }

  public void write_to_file(String fname, int prec) {
    CGAL_Triangulation_3JNI.Triangulation_3_write_to_file__SWIG_0(swigCPtr, this, fname, prec);
  }

  public void write_to_file(String fname) {
    CGAL_Triangulation_3JNI.Triangulation_3_write_to_file__SWIG_1(swigCPtr, this, fname);
  }

  public void read_from_file(String fname) {
    CGAL_Triangulation_3JNI.Triangulation_3_read_from_file(swigCPtr, this, fname);
  }

  public boolean is_cell(Triangulation_3_Vertex_handle u, Triangulation_3_Vertex_handle v, Triangulation_3_Vertex_handle w, Triangulation_3_Vertex_handle x, Triangulation_3_Cell_handle c, Ref_int i, Ref_int j, Ref_int k, Ref_int l) {
    return CGAL_Triangulation_3JNI.Triangulation_3_is_cell__SWIG_1(swigCPtr, this, Triangulation_3_Vertex_handle.getCPtr(u), u, Triangulation_3_Vertex_handle.getCPtr(v), v, Triangulation_3_Vertex_handle.getCPtr(w), w, Triangulation_3_Vertex_handle.getCPtr(x), x, Triangulation_3_Cell_handle.getCPtr(c), c, Ref_int.getCPtr(i), i, Ref_int.getCPtr(j), j, Ref_int.getCPtr(k), k, Ref_int.getCPtr(l), l);
  }

  public boolean equal(Triangulation_3 t) {
    return CGAL_Triangulation_3JNI.Triangulation_3_equal(swigCPtr, this, Triangulation_3.getCPtr(t), t);
  }

  public boolean equals(Triangulation_3 p) {
    return CGAL_Triangulation_3JNI.Triangulation_3_equals(swigCPtr, this, Triangulation_3.getCPtr(p), p);
  }

  public boolean not_equals(Triangulation_3 p) {
    return CGAL_Triangulation_3JNI.Triangulation_3_not_equals(swigCPtr, this, Triangulation_3.getCPtr(p), p);
  }

  public Triangulation_3 clone() {
    return new Triangulation_3(CGAL_Triangulation_3JNI.Triangulation_3_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Triangulation_3 other) {
    CGAL_Triangulation_3JNI.Triangulation_3_clone__SWIG_1(swigCPtr, this, Triangulation_3.getCPtr(other), other);
  }

  public boolean same_internal_object(Triangulation_3 other) {
    return CGAL_Triangulation_3JNI.Triangulation_3_same_internal_object(swigCPtr, this, Triangulation_3.getCPtr(other), other);
  }

}
