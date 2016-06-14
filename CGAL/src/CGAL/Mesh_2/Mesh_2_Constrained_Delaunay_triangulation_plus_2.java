/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_2;
import CGAL.Kernel.Point_2; import java.util.Iterator; import java.util.Collection; import CGAL.Triangulation_2.Constraint;
public class Mesh_2_Constrained_Delaunay_triangulation_plus_2 extends Internal_Constrained_Delaunay_triangulation_2_Mesh_2_Constrained_Delaunay_triangulation_plus_2 {
  private transient long swigCPtr;

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2(long cPtr, boolean cMemoryOwn) {
    super(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(Mesh_2_Constrained_Delaunay_triangulation_plus_2 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Mesh_2JNI.delete_Mesh_2_Constrained_Delaunay_triangulation_plus_2(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2() {
    this(CGAL_Mesh_2JNI.new_Mesh_2_Constrained_Delaunay_triangulation_plus_2__SWIG_0(), true);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2(Iterator<Constraint> range) {
    this(CGAL_Mesh_2JNI.new_Mesh_2_Constrained_Delaunay_triangulation_plus_2__SWIG_1(range), true);
  }

  public void remove_constraint(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle c1, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle c2) {
    CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_remove_constraint(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(c1), c1, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(c2), c2);
  }

  public int number_of_enclosing_constraints(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle c1, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle c2) {
    return CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_number_of_enclosing_constraints(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(c1), c1, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(c2), c2);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator constraints() {
    return new Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_constraints(swigCPtr, this), true);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Subconstraint_iterator subconstraints() {
    return new Mesh_2_Constrained_Delaunay_triangulation_plus_2_Subconstraint_iterator(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_subconstraints(swigCPtr, this), true);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator vertices_in_constraint(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle va, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle vb) {
    return new Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_vertices_in_constraint(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(va), va, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(vb), vb), true);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Context context(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle c1, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle c2) {
    return new Mesh_2_Constrained_Delaunay_triangulation_plus_2_Context(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_context__SWIG_0(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(c1), c1, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(c2), c2), true);
  }

  public void context(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle c1, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle c2, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Context ret) {
    CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_context__SWIG_1(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(c1), c1, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(c2), c2, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Context.getCPtr(ret), ret);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Context_iterator contexts(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle va, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle vb) {
    return new Mesh_2_Constrained_Delaunay_triangulation_plus_2_Context_iterator(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_contexts(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(va), va, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(vb), vb), true);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2 clone() {
    return new Mesh_2_Constrained_Delaunay_triangulation_plus_2(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_clone(swigCPtr, this), true);
  }

}
