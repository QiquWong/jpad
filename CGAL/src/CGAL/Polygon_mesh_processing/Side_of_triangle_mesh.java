/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Polygon_mesh_processing;
import CGAL.Kernel.Point_3; import CGAL.Kernel.Bounded_side; import CGAL.Polyhedron_3.Polyhedron_3;
public class Side_of_triangle_mesh {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Side_of_triangle_mesh(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Side_of_triangle_mesh obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Polygon_mesh_processingJNI.delete_Side_of_triangle_mesh(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Side_of_triangle_mesh(Polyhedron_3 poly) {
    this(CGAL_Polygon_mesh_processingJNI.new_Side_of_triangle_mesh(Polyhedron_3.getCPtr(poly), poly), true);
  }

  public Bounded_side bounded_side(Point_3 p) {
    return Bounded_side.swigToEnum(CGAL_Polygon_mesh_processingJNI.Side_of_triangle_mesh_bounded_side(swigCPtr, this, Point_3.getCPtr(p), p));
  }

}
