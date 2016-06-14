/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_2;
import CGAL.Kernel.Point_2; import java.util.Iterator; import java.util.Collection;  import CGAL.Triangulation_2.Constraint;
public class Mesh_2_Constrained_Delaunay_triangulation_2 extends Internal_Contrained_triangulation_2_Mesh_2_Constrained_Delaunay_triangulation_2 {
  private transient long swigCPtr;

  public Mesh_2_Constrained_Delaunay_triangulation_2(long cPtr, boolean cMemoryOwn) {
    super(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_2_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(Mesh_2_Constrained_Delaunay_triangulation_2 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Mesh_2JNI.delete_Mesh_2_Constrained_Delaunay_triangulation_2(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public Mesh_2_Constrained_Delaunay_triangulation_2() {
    this(CGAL_Mesh_2JNI.new_Mesh_2_Constrained_Delaunay_triangulation_2__SWIG_0(), true);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_2(Iterator<Constraint> range) {
    this(CGAL_Mesh_2JNI.new_Mesh_2_Constrained_Delaunay_triangulation_2__SWIG_1(range), true);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_2 clone() {
    return new Mesh_2_Constrained_Delaunay_triangulation_2(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_2_clone(swigCPtr, this), true);
  }

}
