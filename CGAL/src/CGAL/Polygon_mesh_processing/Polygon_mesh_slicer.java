/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Polygon_mesh_processing;
import CGAL.Kernel.Plane_3; import CGAL.Polyhedron_3.Polyhedron_3;
public class Polygon_mesh_slicer {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Polygon_mesh_slicer(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Polygon_mesh_slicer obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Polygon_mesh_processingJNI.delete_Polygon_mesh_slicer(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Polygon_mesh_slicer(Polyhedron_3 poly) {
    this(CGAL_Polygon_mesh_processingJNI.new_Polygon_mesh_slicer(Polyhedron_3.getCPtr(poly), poly), true);
  }

  public void slice(Plane_3 plane, Polylines out) {
    CGAL_Polygon_mesh_processingJNI.Polygon_mesh_slicer_slice(swigCPtr, this, Plane_3.getCPtr(plane), plane, Polylines.getCPtr(out), out);
  }

}
