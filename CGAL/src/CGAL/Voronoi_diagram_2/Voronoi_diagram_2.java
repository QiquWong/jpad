/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Voronoi_diagram_2;
 import CGAL.Triangulation_2.Delaunay_triangulation_2; import CGAL.Kernel.Point_2; import CGAL.Kernel.Weighted_point_2; import java.util.Iterator; import CGAL.Triangulation_2.Delaunay_triangulation_2_Face_handle; import CGAL.Triangulation_2.Delaunay_triangulation_2_Edge; import CGAL.Triangulation_2.Delaunay_triangulation_2_Vertex_handle; 
public class Voronoi_diagram_2 {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Voronoi_diagram_2(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Voronoi_diagram_2 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Voronoi_diagram_2JNI.delete_Voronoi_diagram_2(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Voronoi_diagram_2() {
    this(CGAL_Voronoi_diagram_2JNI.new_Voronoi_diagram_2__SWIG_0(), true);
  }

  public Voronoi_diagram_2(Delaunay_triangulation_2 triangulation, boolean swap_dg) {
    this(CGAL_Voronoi_diagram_2JNI.new_Voronoi_diagram_2__SWIG_1(Delaunay_triangulation_2.getCPtr(triangulation), triangulation, swap_dg), true);
  }

  public Voronoi_diagram_2(Delaunay_triangulation_2 triangulation) {
    this(CGAL_Voronoi_diagram_2JNI.new_Voronoi_diagram_2__SWIG_2(Delaunay_triangulation_2.getCPtr(triangulation), triangulation), true);
  }

  public Voronoi_diagram_2(Iterator<Point_2> range) {
    this(CGAL_Voronoi_diagram_2JNI.new_Voronoi_diagram_2__SWIG_3(range), true);
  }

  public Voronoi_diagram_2_Face_handle insert(Point_2 c) {
    return new Voronoi_diagram_2_Face_handle(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_insert__SWIG_0(swigCPtr, this, Point_2.getCPtr(c), c), true);
  }

  public void insert(Point_2 c, Voronoi_diagram_2_Face_handle ret) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_insert__SWIG_1(swigCPtr, this, Point_2.getCPtr(c), c, Voronoi_diagram_2_Face_handle.getCPtr(ret), ret);
  }

  public int insert(Iterator<Point_2> range) {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_insert__SWIG_2(swigCPtr, this, range);
  }

  public Delaunay_triangulation_2 dual() {
    return new Delaunay_triangulation_2(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_dual__SWIG_0(swigCPtr, this), true);
  }

  public void dual(Delaunay_triangulation_2 ref) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_dual__SWIG_1(swigCPtr, this, Delaunay_triangulation_2.getCPtr(ref), ref);
  }

  public Voronoi_diagram_2_Halfedge_handle dual(Delaunay_triangulation_2_Edge c) {
    return new Voronoi_diagram_2_Halfedge_handle(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_dual__SWIG_2(swigCPtr, this, Delaunay_triangulation_2_Edge.getCPtr(c), c), true);
  }

  public void dual(Delaunay_triangulation_2_Edge c, Voronoi_diagram_2_Halfedge_handle ret) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_dual__SWIG_3(swigCPtr, this, Delaunay_triangulation_2_Edge.getCPtr(c), c, Voronoi_diagram_2_Halfedge_handle.getCPtr(ret), ret);
  }

  public Voronoi_diagram_2_Vertex_handle dual(Delaunay_triangulation_2_Face_handle c) {
    return new Voronoi_diagram_2_Vertex_handle(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_dual__SWIG_4(swigCPtr, this, Delaunay_triangulation_2_Face_handle.getCPtr(c), c), true);
  }

  public void dual(Delaunay_triangulation_2_Face_handle c, Voronoi_diagram_2_Vertex_handle ret) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_dual__SWIG_5(swigCPtr, this, Delaunay_triangulation_2_Face_handle.getCPtr(c), c, Voronoi_diagram_2_Vertex_handle.getCPtr(ret), ret);
  }

  public Voronoi_diagram_2_Face_handle dual(Delaunay_triangulation_2_Vertex_handle c) {
    return new Voronoi_diagram_2_Face_handle(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_dual__SWIG_6(swigCPtr, this, Delaunay_triangulation_2_Vertex_handle.getCPtr(c), c), true);
  }

  public void dual(Delaunay_triangulation_2_Vertex_handle c, Voronoi_diagram_2_Face_handle ret) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_dual__SWIG_7(swigCPtr, this, Delaunay_triangulation_2_Vertex_handle.getCPtr(c), c, Voronoi_diagram_2_Face_handle.getCPtr(ret), ret);
  }

  public int number_of_vertices() {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_number_of_vertices(swigCPtr, this);
  }

  public int number_of_faces() {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_number_of_faces(swigCPtr, this);
  }

  public int number_of_halfedges() {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_number_of_halfedges(swigCPtr, this);
  }

  public int number_of_connected_components() {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_number_of_connected_components(swigCPtr, this);
  }

  public Voronoi_diagram_2_Face_handle unbounded_face() {
    return new Voronoi_diagram_2_Face_handle(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_unbounded_face__SWIG_0(swigCPtr, this), true);
  }

  public void unbounded_face(Voronoi_diagram_2_Face_handle ref) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_unbounded_face__SWIG_1(swigCPtr, this, Voronoi_diagram_2_Face_handle.getCPtr(ref), ref);
  }

  public Voronoi_diagram_2_Face_handle bounded_face() {
    return new Voronoi_diagram_2_Face_handle(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_bounded_face__SWIG_0(swigCPtr, this), true);
  }

  public void bounded_face(Voronoi_diagram_2_Face_handle ref) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_bounded_face__SWIG_1(swigCPtr, this, Voronoi_diagram_2_Face_handle.getCPtr(ref), ref);
  }

  public Voronoi_diagram_2_Halfedge_handle unbounded_halfedge() {
    return new Voronoi_diagram_2_Halfedge_handle(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_unbounded_halfedge__SWIG_0(swigCPtr, this), true);
  }

  public void unbounded_halfedge(Voronoi_diagram_2_Halfedge_handle ref) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_unbounded_halfedge__SWIG_1(swigCPtr, this, Voronoi_diagram_2_Halfedge_handle.getCPtr(ref), ref);
  }

  public Voronoi_diagram_2_Halfedge_handle bounded_halfedge() {
    return new Voronoi_diagram_2_Halfedge_handle(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_bounded_halfedge__SWIG_0(swigCPtr, this), true);
  }

  public void bounded_halfedge(Voronoi_diagram_2_Halfedge_handle ref) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_bounded_halfedge__SWIG_1(swigCPtr, this, Voronoi_diagram_2_Halfedge_handle.getCPtr(ref), ref);
  }

  public boolean is_valid() {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_is_valid(swigCPtr, this);
  }

  public void clear() {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_clear(swigCPtr, this);
  }

  public void swap(Voronoi_diagram_2 other) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_swap(swigCPtr, this, Voronoi_diagram_2.getCPtr(other), other);
  }

  public void file_output(String fname) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_file_output(swigCPtr, this, fname);
  }

  public void file_input(String fname) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_file_input(swigCPtr, this, fname);
  }

  public Voronoi_diagram_2_Face_iterator faces() {
    return new Voronoi_diagram_2_Face_iterator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_faces(swigCPtr, this), true);
  }

  public Voronoi_diagram_2_Unbounded_faces_iterator unbounded_faces() {
    return new Voronoi_diagram_2_Unbounded_faces_iterator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_unbounded_faces(swigCPtr, this), true);
  }

  public Voronoi_diagram_2_Bounded_faces_iterator bounded_faces() {
    return new Voronoi_diagram_2_Bounded_faces_iterator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_bounded_faces(swigCPtr, this), true);
  }

  public Voronoi_diagram_2_Edge_iterator edges() {
    return new Voronoi_diagram_2_Edge_iterator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_edges(swigCPtr, this), true);
  }

  public Voronoi_diagram_2_Halfedge_iterator halfedges() {
    return new Voronoi_diagram_2_Halfedge_iterator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_halfedges(swigCPtr, this), true);
  }

  public Voronoi_diagram_2_Unbounded_halfedges_iterator unbounded_halfedges() {
    return new Voronoi_diagram_2_Unbounded_halfedges_iterator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_unbounded_halfedges(swigCPtr, this), true);
  }

  public Voronoi_diagram_2_Bounded_halfedges_iterator bounded_halfedges() {
    return new Voronoi_diagram_2_Bounded_halfedges_iterator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_bounded_halfedges(swigCPtr, this), true);
  }

  public Voronoi_diagram_2_Vertex_iterator vertices() {
    return new Voronoi_diagram_2_Vertex_iterator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_vertices(swigCPtr, this), true);
  }

  public Voronoi_diagram_2_Site_iterator sites() {
    return new Voronoi_diagram_2_Site_iterator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_sites(swigCPtr, this), true);
  }

  public Voronoi_diagram_2_Ccb_halfedge_circulator ccb_halfedges(Voronoi_diagram_2_Face_handle c) {
    return new Voronoi_diagram_2_Ccb_halfedge_circulator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_ccb_halfedges__SWIG_0(swigCPtr, this, Voronoi_diagram_2_Face_handle.getCPtr(c), c), true);
  }

  public void ccb_halfedges(Voronoi_diagram_2_Face_handle c, Voronoi_diagram_2_Ccb_halfedge_circulator ret) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_ccb_halfedges__SWIG_1(swigCPtr, this, Voronoi_diagram_2_Face_handle.getCPtr(c), c, Voronoi_diagram_2_Ccb_halfedge_circulator.getCPtr(ret), ret);
  }

  public Voronoi_diagram_2_Ccb_halfedge_circulator ccb_halfedges(Voronoi_diagram_2_Face_handle c1, Voronoi_diagram_2_Halfedge_handle c2) {
    return new Voronoi_diagram_2_Ccb_halfedge_circulator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_ccb_halfedges__SWIG_2(swigCPtr, this, Voronoi_diagram_2_Face_handle.getCPtr(c1), c1, Voronoi_diagram_2_Halfedge_handle.getCPtr(c2), c2), true);
  }

  public void ccb_halfedges(Voronoi_diagram_2_Face_handle c1, Voronoi_diagram_2_Halfedge_handle c2, Voronoi_diagram_2_Ccb_halfedge_circulator ret) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_ccb_halfedges__SWIG_3(swigCPtr, this, Voronoi_diagram_2_Face_handle.getCPtr(c1), c1, Voronoi_diagram_2_Halfedge_handle.getCPtr(c2), c2, Voronoi_diagram_2_Ccb_halfedge_circulator.getCPtr(ret), ret);
  }

  public Voronoi_diagram_2_Halfedge_around_vertex_circulator incident_halfedges(Voronoi_diagram_2_Vertex_handle c) {
    return new Voronoi_diagram_2_Halfedge_around_vertex_circulator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_incident_halfedges__SWIG_0(swigCPtr, this, Voronoi_diagram_2_Vertex_handle.getCPtr(c), c), true);
  }

  public void incident_halfedges(Voronoi_diagram_2_Vertex_handle c, Voronoi_diagram_2_Halfedge_around_vertex_circulator ret) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_incident_halfedges__SWIG_1(swigCPtr, this, Voronoi_diagram_2_Vertex_handle.getCPtr(c), c, Voronoi_diagram_2_Halfedge_around_vertex_circulator.getCPtr(ret), ret);
  }

  public Voronoi_diagram_2_Halfedge_around_vertex_circulator incident_halfedges(Voronoi_diagram_2_Vertex_handle c1, Voronoi_diagram_2_Halfedge_handle c2) {
    return new Voronoi_diagram_2_Halfedge_around_vertex_circulator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_incident_halfedges__SWIG_2(swigCPtr, this, Voronoi_diagram_2_Vertex_handle.getCPtr(c1), c1, Voronoi_diagram_2_Halfedge_handle.getCPtr(c2), c2), true);
  }

  public void incident_halfedges(Voronoi_diagram_2_Vertex_handle c1, Voronoi_diagram_2_Halfedge_handle c2, Voronoi_diagram_2_Halfedge_around_vertex_circulator ret) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_incident_halfedges__SWIG_3(swigCPtr, this, Voronoi_diagram_2_Vertex_handle.getCPtr(c1), c1, Voronoi_diagram_2_Halfedge_handle.getCPtr(c2), c2, Voronoi_diagram_2_Halfedge_around_vertex_circulator.getCPtr(ret), ret);
  }

  public Voronoi_diagram_2_Locate_result locate(Point_2 c) {
    return new Voronoi_diagram_2_Locate_result(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_locate__SWIG_0(swigCPtr, this, Point_2.getCPtr(c), c), true);
  }

  public void locate(Point_2 c, Voronoi_diagram_2_Locate_result ret) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_locate__SWIG_1(swigCPtr, this, Point_2.getCPtr(c), c, Voronoi_diagram_2_Locate_result.getCPtr(ret), ret);
  }

}
