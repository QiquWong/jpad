/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Polyhedron_3;
import CGAL.Kernel.Point_3; import java.util.Iterator; import java.util.Collection; import CGAL.Java.JavaData;
public class CGAL_Polyhedron_3JNI {

    static {
      try {
          System.loadLibrary("CGAL_Polyhedron_3");
      } catch (UnsatisfiedLinkError e) {
        System.err.println("Native code library CGAL_Polyhedron_3 failed to load. \n" + e);
        throw e;
      }
    }
  

    static{
      try {
          System.loadLibrary("CGAL_Java");
      } catch (UnsatisfiedLinkError e) {
        System.err.println("Native code library CGAL_Java failed to load. \n" + e);
        throw e;
      }
    }
  
  public final static native long new_Polyhedron_3_Halfedge_handle();
  public final static native long Polyhedron_3_Halfedge_handle_opposite__SWIG_0(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native void Polyhedron_3_Halfedge_handle_opposite__SWIG_1(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_Halfedge_handle_next__SWIG_0(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native void Polyhedron_3_Halfedge_handle_next__SWIG_1(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_Halfedge_handle_prev__SWIG_0(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native void Polyhedron_3_Halfedge_handle_prev__SWIG_1(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_Halfedge_handle_next_on_vertex__SWIG_0(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native void Polyhedron_3_Halfedge_handle_next_on_vertex__SWIG_1(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_Halfedge_handle_prev_on_vertex__SWIG_0(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native void Polyhedron_3_Halfedge_handle_prev_on_vertex__SWIG_1(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native boolean Polyhedron_3_Halfedge_handle_is_border(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native boolean Polyhedron_3_Halfedge_handle_is_border_edge(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native long Polyhedron_3_Halfedge_handle_vertex_begin(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native long Polyhedron_3_Halfedge_handle_facet_begin(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native long Polyhedron_3_Halfedge_handle_vertex_degree(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native boolean Polyhedron_3_Halfedge_handle_is_bivalent(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native boolean Polyhedron_3_Halfedge_handle_is_trivalent(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native long Polyhedron_3_Halfedge_handle_facet_degree(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native boolean Polyhedron_3_Halfedge_handle_is_triangle(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native boolean Polyhedron_3_Halfedge_handle_is_quad(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native long Polyhedron_3_Halfedge_handle_vertex__SWIG_0(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native void Polyhedron_3_Halfedge_handle_vertex__SWIG_1(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Vertex_handle jarg2_);
  public final static native long Polyhedron_3_Halfedge_handle_facet__SWIG_0(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native void Polyhedron_3_Halfedge_handle_facet__SWIG_1(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Facet_handle jarg2_);
  public final static native boolean Polyhedron_3_Halfedge_handle_lt(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native boolean Polyhedron_3_Halfedge_handle_gt(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native boolean Polyhedron_3_Halfedge_handle_le(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native boolean Polyhedron_3_Halfedge_handle_ge(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native boolean Polyhedron_3_Halfedge_handle_equals(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native boolean Polyhedron_3_Halfedge_handle_not_equals(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native int Polyhedron_3_Halfedge_handle_hashCode(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native int Polyhedron_3_Halfedge_handle_id(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native void Polyhedron_3_Halfedge_handle_set_id(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, int jarg2);
  public final static native long Polyhedron_3_Halfedge_handle_clone__SWIG_0(long jarg1, Polyhedron_3_Halfedge_handle jarg1_);
  public final static native void Polyhedron_3_Halfedge_handle_clone__SWIG_1(long jarg1, Polyhedron_3_Halfedge_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native void delete_Polyhedron_3_Halfedge_handle(long jarg1);
  public final static native long new_Polyhedron_3_Vertex_handle();
  public final static native long Polyhedron_3_Vertex_handle_point__SWIG_0(long jarg1, Polyhedron_3_Vertex_handle jarg1_);
  public final static native void Polyhedron_3_Vertex_handle_point__SWIG_1(long jarg1, Polyhedron_3_Vertex_handle jarg1_, long jarg2, Point_3 jarg2_);
  public final static native long Polyhedron_3_Vertex_handle_halfedge__SWIG_0(long jarg1, Polyhedron_3_Vertex_handle jarg1_);
  public final static native void Polyhedron_3_Vertex_handle_halfedge__SWIG_1(long jarg1, Polyhedron_3_Vertex_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_Vertex_handle_vertex_begin(long jarg1, Polyhedron_3_Vertex_handle jarg1_);
  public final static native void Polyhedron_3_Vertex_handle_set_halfedge(long jarg1, Polyhedron_3_Vertex_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_Vertex_handle_vertex_degree(long jarg1, Polyhedron_3_Vertex_handle jarg1_);
  public final static native boolean Polyhedron_3_Vertex_handle_is_bivalent(long jarg1, Polyhedron_3_Vertex_handle jarg1_);
  public final static native boolean Polyhedron_3_Vertex_handle_is_trivalent(long jarg1, Polyhedron_3_Vertex_handle jarg1_);
  public final static native boolean Polyhedron_3_Vertex_handle_lt(long jarg1, Polyhedron_3_Vertex_handle jarg1_, long jarg2, Polyhedron_3_Vertex_handle jarg2_);
  public final static native boolean Polyhedron_3_Vertex_handle_gt(long jarg1, Polyhedron_3_Vertex_handle jarg1_, long jarg2, Polyhedron_3_Vertex_handle jarg2_);
  public final static native boolean Polyhedron_3_Vertex_handle_le(long jarg1, Polyhedron_3_Vertex_handle jarg1_, long jarg2, Polyhedron_3_Vertex_handle jarg2_);
  public final static native boolean Polyhedron_3_Vertex_handle_ge(long jarg1, Polyhedron_3_Vertex_handle jarg1_, long jarg2, Polyhedron_3_Vertex_handle jarg2_);
  public final static native boolean Polyhedron_3_Vertex_handle_equals(long jarg1, Polyhedron_3_Vertex_handle jarg1_, long jarg2, Polyhedron_3_Vertex_handle jarg2_);
  public final static native boolean Polyhedron_3_Vertex_handle_not_equals(long jarg1, Polyhedron_3_Vertex_handle jarg1_, long jarg2, Polyhedron_3_Vertex_handle jarg2_);
  public final static native int Polyhedron_3_Vertex_handle_hashCode(long jarg1, Polyhedron_3_Vertex_handle jarg1_);
  public final static native int Polyhedron_3_Vertex_handle_id(long jarg1, Polyhedron_3_Vertex_handle jarg1_);
  public final static native void Polyhedron_3_Vertex_handle_set_id(long jarg1, Polyhedron_3_Vertex_handle jarg1_, int jarg2);
  public final static native void Polyhedron_3_Vertex_handle_set_point(long jarg1, Polyhedron_3_Vertex_handle jarg1_, long jarg2, Point_3 jarg2_);
  public final static native long Polyhedron_3_Vertex_handle_clone__SWIG_0(long jarg1, Polyhedron_3_Vertex_handle jarg1_);
  public final static native void Polyhedron_3_Vertex_handle_clone__SWIG_1(long jarg1, Polyhedron_3_Vertex_handle jarg1_, long jarg2, Polyhedron_3_Vertex_handle jarg2_);
  public final static native void delete_Polyhedron_3_Vertex_handle(long jarg1);
  public final static native long new_Polyhedron_3_Facet_handle();
  public final static native long Polyhedron_3_Facet_handle_halfedge__SWIG_0(long jarg1, Polyhedron_3_Facet_handle jarg1_);
  public final static native void Polyhedron_3_Facet_handle_halfedge__SWIG_1(long jarg1, Polyhedron_3_Facet_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_Facet_handle_facet_begin(long jarg1, Polyhedron_3_Facet_handle jarg1_);
  public final static native void Polyhedron_3_Facet_handle_set_halfedge(long jarg1, Polyhedron_3_Facet_handle jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_Facet_handle_facet_degree(long jarg1, Polyhedron_3_Facet_handle jarg1_);
  public final static native boolean Polyhedron_3_Facet_handle_is_triangle(long jarg1, Polyhedron_3_Facet_handle jarg1_);
  public final static native boolean Polyhedron_3_Facet_handle_is_quad(long jarg1, Polyhedron_3_Facet_handle jarg1_);
  public final static native boolean Polyhedron_3_Facet_handle_lt(long jarg1, Polyhedron_3_Facet_handle jarg1_, long jarg2, Polyhedron_3_Facet_handle jarg2_);
  public final static native boolean Polyhedron_3_Facet_handle_gt(long jarg1, Polyhedron_3_Facet_handle jarg1_, long jarg2, Polyhedron_3_Facet_handle jarg2_);
  public final static native boolean Polyhedron_3_Facet_handle_le(long jarg1, Polyhedron_3_Facet_handle jarg1_, long jarg2, Polyhedron_3_Facet_handle jarg2_);
  public final static native boolean Polyhedron_3_Facet_handle_ge(long jarg1, Polyhedron_3_Facet_handle jarg1_, long jarg2, Polyhedron_3_Facet_handle jarg2_);
  public final static native boolean Polyhedron_3_Facet_handle_equals(long jarg1, Polyhedron_3_Facet_handle jarg1_, long jarg2, Polyhedron_3_Facet_handle jarg2_);
  public final static native boolean Polyhedron_3_Facet_handle_not_equals(long jarg1, Polyhedron_3_Facet_handle jarg1_, long jarg2, Polyhedron_3_Facet_handle jarg2_);
  public final static native int Polyhedron_3_Facet_handle_hashCode(long jarg1, Polyhedron_3_Facet_handle jarg1_);
  public final static native int Polyhedron_3_Facet_handle_id(long jarg1, Polyhedron_3_Facet_handle jarg1_);
  public final static native void Polyhedron_3_Facet_handle_set_id(long jarg1, Polyhedron_3_Facet_handle jarg1_, int jarg2);
  public final static native long Polyhedron_3_Facet_handle_clone__SWIG_0(long jarg1, Polyhedron_3_Facet_handle jarg1_);
  public final static native void Polyhedron_3_Facet_handle_clone__SWIG_1(long jarg1, Polyhedron_3_Facet_handle jarg1_, long jarg2, Polyhedron_3_Facet_handle jarg2_);
  public final static native void delete_Polyhedron_3_Facet_handle(long jarg1);
  public final static native long new_Polyhedron_3__SWIG_0();
  public final static native long new_Polyhedron_3__SWIG_1(String jarg1);
  public final static native long new_Polyhedron_3__SWIG_2(long jarg1, long jarg2, long jarg3);
  public final static native void Polyhedron_3_reserve(long jarg1, Polyhedron_3 jarg1_, long jarg2, long jarg3, long jarg4);
  public final static native long Polyhedron_3_make_tetrahedron__SWIG_0(long jarg1, Polyhedron_3 jarg1_);
  public final static native void Polyhedron_3_make_tetrahedron__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_make_tetrahedron__SWIG_2(long jarg1, Polyhedron_3 jarg1_, long jarg2, Point_3 jarg2_, long jarg3, Point_3 jarg3_, long jarg4, Point_3 jarg4_, long jarg5, Point_3 jarg5_);
  public final static native void Polyhedron_3_make_tetrahedron__SWIG_3(long jarg1, Polyhedron_3 jarg1_, long jarg2, Point_3 jarg2_, long jarg3, Point_3 jarg3_, long jarg4, Point_3 jarg4_, long jarg5, Point_3 jarg5_, long jarg6, Polyhedron_3_Halfedge_handle jarg6_);
  public final static native long Polyhedron_3_make_triangle__SWIG_0(long jarg1, Polyhedron_3 jarg1_);
  public final static native void Polyhedron_3_make_triangle__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_make_triangle__SWIG_2(long jarg1, Polyhedron_3 jarg1_, long jarg2, Point_3 jarg2_, long jarg3, Point_3 jarg3_, long jarg4, Point_3 jarg4_);
  public final static native void Polyhedron_3_make_triangle__SWIG_3(long jarg1, Polyhedron_3 jarg1_, long jarg2, Point_3 jarg2_, long jarg3, Point_3 jarg3_, long jarg4, Point_3 jarg4_, long jarg5, Polyhedron_3_Halfedge_handle jarg5_);
  public final static native boolean Polyhedron_3_empty(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_size_of_vertices(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_size_of_halfedges(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_size_of_facets(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_capacity_of_vertices(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_capacity_of_halfedges(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_capacity_of_facets(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_bytes(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_bytes_reserved(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_vertices(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_halfedges(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_facets(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_edges(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_points(long jarg1, Polyhedron_3 jarg1_);
  public final static native boolean Polyhedron_3_is_closed(long jarg1, Polyhedron_3 jarg1_);
  public final static native boolean Polyhedron_3_is_pure_bivalent(long jarg1, Polyhedron_3 jarg1_);
  public final static native boolean Polyhedron_3_is_pure_trivalent(long jarg1, Polyhedron_3 jarg1_);
  public final static native boolean Polyhedron_3_is_pure_triangle(long jarg1, Polyhedron_3 jarg1_);
  public final static native boolean Polyhedron_3_is_pure_quad(long jarg1, Polyhedron_3 jarg1_);
  public final static native boolean Polyhedron_3_is_triangle(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native boolean Polyhedron_3_is_tetrahedron(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_split_facet__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native void Polyhedron_3_split_facet__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_, long jarg4, Polyhedron_3_Halfedge_handle jarg4_);
  public final static native long Polyhedron_3_join_facet__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native void Polyhedron_3_join_facet__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native long Polyhedron_3_split_vertex__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native void Polyhedron_3_split_vertex__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_, long jarg4, Polyhedron_3_Halfedge_handle jarg4_);
  public final static native long Polyhedron_3_join_vertex__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native void Polyhedron_3_join_vertex__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native long Polyhedron_3_split_edge__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native void Polyhedron_3_split_edge__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native long Polyhedron_3_flip_edge__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native void Polyhedron_3_flip_edge__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native long Polyhedron_3_create_center_vertex__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native void Polyhedron_3_create_center_vertex__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native long Polyhedron_3_erase_center_vertex__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native void Polyhedron_3_erase_center_vertex__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native long Polyhedron_3_split_loop__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_, long jarg4, Polyhedron_3_Halfedge_handle jarg4_);
  public final static native void Polyhedron_3_split_loop__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_, long jarg4, Polyhedron_3_Halfedge_handle jarg4_, long jarg5, Polyhedron_3_Halfedge_handle jarg5_);
  public final static native long Polyhedron_3_join_loop__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native void Polyhedron_3_join_loop__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_, long jarg4, Polyhedron_3_Halfedge_handle jarg4_);
  public final static native long Polyhedron_3_make_hole__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native void Polyhedron_3_make_hole__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native long Polyhedron_3_fill_hole__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native void Polyhedron_3_fill_hole__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native long Polyhedron_3_add_vertex_and_facet_to_border__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native void Polyhedron_3_add_vertex_and_facet_to_border__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_, long jarg4, Polyhedron_3_Halfedge_handle jarg4_);
  public final static native long Polyhedron_3_add_facet_to_border__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_);
  public final static native void Polyhedron_3_add_facet_to_border__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_, long jarg3, Polyhedron_3_Halfedge_handle jarg3_, long jarg4, Polyhedron_3_Halfedge_handle jarg4_);
  public final static native void Polyhedron_3_erase_facet(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native void Polyhedron_3_erase_connected_component(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_keep_largest_connected_components(long jarg1, Polyhedron_3 jarg1_, long jarg2);
  public final static native void Polyhedron_3_clear(long jarg1, Polyhedron_3 jarg1_);
  public final static native void Polyhedron_3_normalize_border(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_size_of_border_halfedges(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_size_of_border_edges(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_border_halfedges(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_non_border_halfedges(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_border_edges(long jarg1, Polyhedron_3 jarg1_);
  public final static native long Polyhedron_3_non_border_edges(long jarg1, Polyhedron_3 jarg1_);
  public final static native void Polyhedron_3_inside_out(long jarg1, Polyhedron_3 jarg1_);
  public final static native boolean Polyhedron_3_is_valid(long jarg1, Polyhedron_3 jarg1_);
  public final static native boolean Polyhedron_3_normalized_border_is_valid(long jarg1, Polyhedron_3 jarg1_);
  public final static native void Polyhedron_3_delegate__SWIG_0(long jarg1, Polyhedron_3 jarg1_, long jarg2);
  public final static native void Polyhedron_3_delegate__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_modifier jarg2_);
  public final static native void Polyhedron_3_write_to_file__SWIG_0(long jarg1, Polyhedron_3 jarg1_, String jarg2, int jarg3);
  public final static native void Polyhedron_3_write_to_file__SWIG_1(long jarg1, Polyhedron_3 jarg1_, String jarg2);
  public final static native long Polyhedron_3_clone__SWIG_0(long jarg1, Polyhedron_3 jarg1_);
  public final static native void Polyhedron_3_clone__SWIG_1(long jarg1, Polyhedron_3 jarg1_, long jarg2, Polyhedron_3 jarg2_);
  public final static native void delete_Polyhedron_3(long jarg1);
  public final static native long new_Polyhedron_modifier();
  public final static native void Polyhedron_modifier_begin_surface__SWIG_0(long jarg1, Polyhedron_modifier jarg1_, int jarg2, int jarg3, int jarg4, int jarg5);
  public final static native void Polyhedron_modifier_begin_surface__SWIG_1(long jarg1, Polyhedron_modifier jarg1_, int jarg2, int jarg3, int jarg4);
  public final static native void Polyhedron_modifier_begin_surface__SWIG_2(long jarg1, Polyhedron_modifier jarg1_, int jarg2, int jarg3);
  public final static native void Polyhedron_modifier_end_surface(long jarg1, Polyhedron_modifier jarg1_);
  public final static native void Polyhedron_modifier_add_vertex(long jarg1, Polyhedron_modifier jarg1_, long jarg2, Point_3 jarg2_);
  public final static native void Polyhedron_modifier_begin_facet(long jarg1, Polyhedron_modifier jarg1_);
  public final static native void Polyhedron_modifier_end_facet(long jarg1, Polyhedron_modifier jarg1_);
  public final static native void Polyhedron_modifier_add_vertex_to_facet(long jarg1, Polyhedron_modifier jarg1_, int jarg2);
  public final static native void Polyhedron_modifier_rollback(long jarg1, Polyhedron_modifier jarg1_);
  public final static native void Polyhedron_modifier_clear(long jarg1, Polyhedron_modifier jarg1_);
  public final static native void delete_Polyhedron_modifier(long jarg1);
  public final static native long new_Polyhedron_3_Halfedge_iterator();
  public final static native long Polyhedron_3_Halfedge_iterator_slow_next(long jarg1, Polyhedron_3_Halfedge_iterator jarg1_);
  public final static native void Polyhedron_3_Halfedge_iterator_next(long jarg1, Polyhedron_3_Halfedge_iterator jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_Halfedge_iterator_clone__SWIG_0(long jarg1, Polyhedron_3_Halfedge_iterator jarg1_);
  public final static native void Polyhedron_3_Halfedge_iterator_clone__SWIG_1(long jarg1, Polyhedron_3_Halfedge_iterator jarg1_, long jarg2, Polyhedron_3_Halfedge_iterator jarg2_);
  public final static native boolean Polyhedron_3_Halfedge_iterator_hasNext(long jarg1, Polyhedron_3_Halfedge_iterator jarg1_);
  public final static native boolean Polyhedron_3_Halfedge_iterator_equals(long jarg1, Polyhedron_3_Halfedge_iterator jarg1_, long jarg2, Polyhedron_3_Halfedge_iterator jarg2_);
  public final static native boolean Polyhedron_3_Halfedge_iterator_not_equals(long jarg1, Polyhedron_3_Halfedge_iterator jarg1_, long jarg2, Polyhedron_3_Halfedge_iterator jarg2_);
  public final static native void delete_Polyhedron_3_Halfedge_iterator(long jarg1);
  public final static native long new_Polyhedron_3_Edge_iterator();
  public final static native long Polyhedron_3_Edge_iterator_slow_next(long jarg1, Polyhedron_3_Edge_iterator jarg1_);
  public final static native void Polyhedron_3_Edge_iterator_next(long jarg1, Polyhedron_3_Edge_iterator jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_Edge_iterator_clone__SWIG_0(long jarg1, Polyhedron_3_Edge_iterator jarg1_);
  public final static native void Polyhedron_3_Edge_iterator_clone__SWIG_1(long jarg1, Polyhedron_3_Edge_iterator jarg1_, long jarg2, Polyhedron_3_Edge_iterator jarg2_);
  public final static native boolean Polyhedron_3_Edge_iterator_hasNext(long jarg1, Polyhedron_3_Edge_iterator jarg1_);
  public final static native boolean Polyhedron_3_Edge_iterator_equals(long jarg1, Polyhedron_3_Edge_iterator jarg1_, long jarg2, Polyhedron_3_Edge_iterator jarg2_);
  public final static native boolean Polyhedron_3_Edge_iterator_not_equals(long jarg1, Polyhedron_3_Edge_iterator jarg1_, long jarg2, Polyhedron_3_Edge_iterator jarg2_);
  public final static native void delete_Polyhedron_3_Edge_iterator(long jarg1);
  public final static native long new_Polyhedron_3_Vertex_iterator();
  public final static native long Polyhedron_3_Vertex_iterator_slow_next(long jarg1, Polyhedron_3_Vertex_iterator jarg1_);
  public final static native void Polyhedron_3_Vertex_iterator_next(long jarg1, Polyhedron_3_Vertex_iterator jarg1_, long jarg2, Polyhedron_3_Vertex_handle jarg2_);
  public final static native long Polyhedron_3_Vertex_iterator_clone__SWIG_0(long jarg1, Polyhedron_3_Vertex_iterator jarg1_);
  public final static native void Polyhedron_3_Vertex_iterator_clone__SWIG_1(long jarg1, Polyhedron_3_Vertex_iterator jarg1_, long jarg2, Polyhedron_3_Vertex_iterator jarg2_);
  public final static native boolean Polyhedron_3_Vertex_iterator_hasNext(long jarg1, Polyhedron_3_Vertex_iterator jarg1_);
  public final static native boolean Polyhedron_3_Vertex_iterator_equals(long jarg1, Polyhedron_3_Vertex_iterator jarg1_, long jarg2, Polyhedron_3_Vertex_iterator jarg2_);
  public final static native boolean Polyhedron_3_Vertex_iterator_not_equals(long jarg1, Polyhedron_3_Vertex_iterator jarg1_, long jarg2, Polyhedron_3_Vertex_iterator jarg2_);
  public final static native void delete_Polyhedron_3_Vertex_iterator(long jarg1);
  public final static native long new_Polyhedron_3_Facet_iterator();
  public final static native long Polyhedron_3_Facet_iterator_slow_next(long jarg1, Polyhedron_3_Facet_iterator jarg1_);
  public final static native void Polyhedron_3_Facet_iterator_next(long jarg1, Polyhedron_3_Facet_iterator jarg1_, long jarg2, Polyhedron_3_Facet_handle jarg2_);
  public final static native long Polyhedron_3_Facet_iterator_clone__SWIG_0(long jarg1, Polyhedron_3_Facet_iterator jarg1_);
  public final static native void Polyhedron_3_Facet_iterator_clone__SWIG_1(long jarg1, Polyhedron_3_Facet_iterator jarg1_, long jarg2, Polyhedron_3_Facet_iterator jarg2_);
  public final static native boolean Polyhedron_3_Facet_iterator_hasNext(long jarg1, Polyhedron_3_Facet_iterator jarg1_);
  public final static native boolean Polyhedron_3_Facet_iterator_equals(long jarg1, Polyhedron_3_Facet_iterator jarg1_, long jarg2, Polyhedron_3_Facet_iterator jarg2_);
  public final static native boolean Polyhedron_3_Facet_iterator_not_equals(long jarg1, Polyhedron_3_Facet_iterator jarg1_, long jarg2, Polyhedron_3_Facet_iterator jarg2_);
  public final static native void delete_Polyhedron_3_Facet_iterator(long jarg1);
  public final static native long new_Polyhedron_3_Point_iterator();
  public final static native long Polyhedron_3_Point_iterator_slow_next(long jarg1, Polyhedron_3_Point_iterator jarg1_);
  public final static native void Polyhedron_3_Point_iterator_next(long jarg1, Polyhedron_3_Point_iterator jarg1_, long jarg2, Point_3 jarg2_);
  public final static native long Polyhedron_3_Point_iterator_clone__SWIG_0(long jarg1, Polyhedron_3_Point_iterator jarg1_);
  public final static native void Polyhedron_3_Point_iterator_clone__SWIG_1(long jarg1, Polyhedron_3_Point_iterator jarg1_, long jarg2, Polyhedron_3_Point_iterator jarg2_);
  public final static native boolean Polyhedron_3_Point_iterator_hasNext(long jarg1, Polyhedron_3_Point_iterator jarg1_);
  public final static native boolean Polyhedron_3_Point_iterator_equals(long jarg1, Polyhedron_3_Point_iterator jarg1_, long jarg2, Polyhedron_3_Point_iterator jarg2_);
  public final static native boolean Polyhedron_3_Point_iterator_not_equals(long jarg1, Polyhedron_3_Point_iterator jarg1_, long jarg2, Polyhedron_3_Point_iterator jarg2_);
  public final static native void delete_Polyhedron_3_Point_iterator(long jarg1);
  public final static native long new_Polyhedron_3_Halfedge_around_vertex_circulator();
  public final static native long Polyhedron_3_Halfedge_around_vertex_circulator_slow_next(long jarg1, Polyhedron_3_Halfedge_around_vertex_circulator jarg1_);
  public final static native void Polyhedron_3_Halfedge_around_vertex_circulator_next(long jarg1, Polyhedron_3_Halfedge_around_vertex_circulator jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_Halfedge_around_vertex_circulator_clone__SWIG_0(long jarg1, Polyhedron_3_Halfedge_around_vertex_circulator jarg1_);
  public final static native void Polyhedron_3_Halfedge_around_vertex_circulator_clone__SWIG_1(long jarg1, Polyhedron_3_Halfedge_around_vertex_circulator jarg1_, long jarg2, Polyhedron_3_Halfedge_around_vertex_circulator jarg2_);
  public final static native long Polyhedron_3_Halfedge_around_vertex_circulator_prev(long jarg1, Polyhedron_3_Halfedge_around_vertex_circulator jarg1_);
  public final static native boolean Polyhedron_3_Halfedge_around_vertex_circulator_hasNext(long jarg1, Polyhedron_3_Halfedge_around_vertex_circulator jarg1_);
  public final static native boolean Polyhedron_3_Halfedge_around_vertex_circulator_equals(long jarg1, Polyhedron_3_Halfedge_around_vertex_circulator jarg1_, long jarg2, Polyhedron_3_Halfedge_around_vertex_circulator jarg2_);
  public final static native boolean Polyhedron_3_Halfedge_around_vertex_circulator_not_equals(long jarg1, Polyhedron_3_Halfedge_around_vertex_circulator jarg1_, long jarg2, Polyhedron_3_Halfedge_around_vertex_circulator jarg2_);
  public final static native void delete_Polyhedron_3_Halfedge_around_vertex_circulator(long jarg1);
  public final static native long new_Polyhedron_3_Halfedge_around_facet_circulator();
  public final static native long Polyhedron_3_Halfedge_around_facet_circulator_slow_next(long jarg1, Polyhedron_3_Halfedge_around_facet_circulator jarg1_);
  public final static native void Polyhedron_3_Halfedge_around_facet_circulator_next(long jarg1, Polyhedron_3_Halfedge_around_facet_circulator jarg1_, long jarg2, Polyhedron_3_Halfedge_handle jarg2_);
  public final static native long Polyhedron_3_Halfedge_around_facet_circulator_clone__SWIG_0(long jarg1, Polyhedron_3_Halfedge_around_facet_circulator jarg1_);
  public final static native void Polyhedron_3_Halfedge_around_facet_circulator_clone__SWIG_1(long jarg1, Polyhedron_3_Halfedge_around_facet_circulator jarg1_, long jarg2, Polyhedron_3_Halfedge_around_facet_circulator jarg2_);
  public final static native long Polyhedron_3_Halfedge_around_facet_circulator_prev(long jarg1, Polyhedron_3_Halfedge_around_facet_circulator jarg1_);
  public final static native boolean Polyhedron_3_Halfedge_around_facet_circulator_hasNext(long jarg1, Polyhedron_3_Halfedge_around_facet_circulator jarg1_);
  public final static native boolean Polyhedron_3_Halfedge_around_facet_circulator_equals(long jarg1, Polyhedron_3_Halfedge_around_facet_circulator jarg1_, long jarg2, Polyhedron_3_Halfedge_around_facet_circulator jarg2_);
  public final static native boolean Polyhedron_3_Halfedge_around_facet_circulator_not_equals(long jarg1, Polyhedron_3_Halfedge_around_facet_circulator jarg1_, long jarg2, Polyhedron_3_Halfedge_around_facet_circulator jarg2_);
  public final static native void delete_Polyhedron_3_Halfedge_around_facet_circulator(long jarg1);
}
