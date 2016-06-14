/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_3;
import CGAL.Kernel.Weighted_point_3; import CGAL.Kernel.Bounded_side; import java.util.Iterator; import java.util.Collection;
public class Mesh_3_regular_triangulation_3 extends Internal_Triangulation_3_Mesh_3_regular_triangulation_3 {
  private transient long swigCPtr;

  public Mesh_3_regular_triangulation_3(long cPtr, boolean cMemoryOwn) {
    super(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(Mesh_3_regular_triangulation_3 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Mesh_3JNI.delete_Mesh_3_regular_triangulation_3(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public Mesh_3_regular_triangulation_3() {
    this(CGAL_Mesh_3JNI.new_Mesh_3_regular_triangulation_3__SWIG_0(), true);
  }

  public Mesh_3_regular_triangulation_3(Iterator<Weighted_point_3> range) {
    this(CGAL_Mesh_3JNI.new_Mesh_3_regular_triangulation_3__SWIG_1(range), true);
  }

  public void remove(Mesh_3_regular_triangulation_3_Vertex_handle c) {
    CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_remove(swigCPtr, this, Mesh_3_regular_triangulation_3_Vertex_handle.getCPtr(c), c);
  }

  public Bounded_side side_of_power_sphere(Mesh_3_regular_triangulation_3_Cell_handle c1, Weighted_point_3 c2) {
    return Bounded_side.swigToEnum(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_side_of_power_sphere(swigCPtr, this, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c1), c1, Weighted_point_3.getCPtr(c2), c2));
  }

  public Bounded_side side_of_power_circle(Mesh_3_regular_triangulation_3_Facet c1, Weighted_point_3 c2) {
    return Bounded_side.swigToEnum(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_side_of_power_circle__SWIG_0(swigCPtr, this, Mesh_3_regular_triangulation_3_Facet.getCPtr(c1), c1, Weighted_point_3.getCPtr(c2), c2));
  }

  public Bounded_side side_of_power_circle(Mesh_3_regular_triangulation_3_Cell_handle c1, int c2, Weighted_point_3 c3) {
    return Bounded_side.swigToEnum(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_side_of_power_circle__SWIG_1(swigCPtr, this, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c1), c1, c2, Weighted_point_3.getCPtr(c3), c3));
  }

  public Bounded_side side_of_power_segment(Mesh_3_regular_triangulation_3_Cell_handle c1, Weighted_point_3 c2) {
    return Bounded_side.swigToEnum(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_side_of_power_segment(swigCPtr, this, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c1), c1, Weighted_point_3.getCPtr(c2), c2));
  }

  public Mesh_3_regular_triangulation_3_Vertex_handle nearest_power_vertex(Weighted_point_3 c) {
    return new Mesh_3_regular_triangulation_3_Vertex_handle(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_nearest_power_vertex__SWIG_0(swigCPtr, this, Weighted_point_3.getCPtr(c), c), true);
  }

  public void nearest_power_vertex(Weighted_point_3 c, Mesh_3_regular_triangulation_3_Vertex_handle ret) {
    CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_nearest_power_vertex__SWIG_1(swigCPtr, this, Weighted_point_3.getCPtr(c), c, Mesh_3_regular_triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public Mesh_3_regular_triangulation_3_Vertex_handle nearest_power_vertex(Weighted_point_3 c1, Mesh_3_regular_triangulation_3_Cell_handle c2) {
    return new Mesh_3_regular_triangulation_3_Vertex_handle(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_nearest_power_vertex__SWIG_2(swigCPtr, this, Weighted_point_3.getCPtr(c1), c1, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c2), c2), true);
  }

  public void nearest_power_vertex(Weighted_point_3 c1, Mesh_3_regular_triangulation_3_Cell_handle c2, Mesh_3_regular_triangulation_3_Vertex_handle ret) {
    CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_nearest_power_vertex__SWIG_3(swigCPtr, this, Weighted_point_3.getCPtr(c1), c1, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c2), c2, Mesh_3_regular_triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public Mesh_3_regular_triangulation_3_Vertex_handle nearest_power_vertex_in_cell(Weighted_point_3 c1, Mesh_3_regular_triangulation_3_Cell_handle c2) {
    return new Mesh_3_regular_triangulation_3_Vertex_handle(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_nearest_power_vertex_in_cell__SWIG_0(swigCPtr, this, Weighted_point_3.getCPtr(c1), c1, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c2), c2), true);
  }

  public void nearest_power_vertex_in_cell(Weighted_point_3 c1, Mesh_3_regular_triangulation_3_Cell_handle c2, Mesh_3_regular_triangulation_3_Vertex_handle ret) {
    CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_nearest_power_vertex_in_cell__SWIG_1(swigCPtr, this, Weighted_point_3.getCPtr(c1), c1, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c2), c2, Mesh_3_regular_triangulation_3_Vertex_handle.getCPtr(ret), ret);
  }

  public boolean is_Gabriel(Mesh_3_regular_triangulation_3_Cell_handle c1, int c2) {
    return CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_is_Gabriel__SWIG_0(swigCPtr, this, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c1), c1, c2);
  }

  public boolean is_Gabriel(Mesh_3_regular_triangulation_3_Cell_handle c1, int c2, int c3) {
    return CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_is_Gabriel__SWIG_1(swigCPtr, this, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c1), c1, c2, c3);
  }

  public boolean is_Gabriel(Mesh_3_regular_triangulation_3_Facet c) {
    return CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_is_Gabriel__SWIG_2(swigCPtr, this, Mesh_3_regular_triangulation_3_Facet.getCPtr(c), c);
  }

  public boolean is_Gabriel(Mesh_3_regular_triangulation_3_Edge c) {
    return CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_is_Gabriel__SWIG_3(swigCPtr, this, Mesh_3_regular_triangulation_3_Edge.getCPtr(c), c);
  }

  public boolean is_Gabriel(Mesh_3_regular_triangulation_3_Vertex_handle c) {
    return CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_is_Gabriel__SWIG_4(swigCPtr, this, Mesh_3_regular_triangulation_3_Vertex_handle.getCPtr(c), c);
  }

  public Weighted_point_3 dual(Mesh_3_regular_triangulation_3_Cell_handle c) {
    return new Weighted_point_3(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_dual__SWIG_0(swigCPtr, this, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c), c), true);
  }

  public void dual(Mesh_3_regular_triangulation_3_Cell_handle c, Weighted_point_3 ret) {
    CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_dual__SWIG_1(swigCPtr, this, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c), c, Weighted_point_3.getCPtr(ret), ret);
  }

  public Object dual(Mesh_3_regular_triangulation_3_Facet c) {
    return new Object(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_dual__SWIG_2(swigCPtr, this, Mesh_3_regular_triangulation_3_Facet.getCPtr(c), c), true);
  }

  public void dual(Mesh_3_regular_triangulation_3_Facet c, Object ret) {
    CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_dual__SWIG_3(swigCPtr, this, Mesh_3_regular_triangulation_3_Facet.getCPtr(c), c, Object.getCPtr(ret), ret);
  }

  public Object dual(Mesh_3_regular_triangulation_3_Cell_handle c1, int c2) {
    return new Object(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_dual__SWIG_4(swigCPtr, this, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c1), c1, c2), true);
  }

  public void dual(Mesh_3_regular_triangulation_3_Cell_handle c1, int c2, Object ret) {
    CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_dual__SWIG_5(swigCPtr, this, Mesh_3_regular_triangulation_3_Cell_handle.getCPtr(c1), c1, c2, Object.getCPtr(ret), ret);
  }

}
