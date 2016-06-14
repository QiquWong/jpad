/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_2;
import CGAL.Triangulation_2.Constrained_Delaunay_triangulation_plus_2;
public class Mesh_2_Constrained_Delaunay_triangulation_conformer_2 {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Mesh_2_Constrained_Delaunay_triangulation_conformer_2(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Mesh_2_Constrained_Delaunay_triangulation_conformer_2 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Mesh_2JNI.delete_Mesh_2_Constrained_Delaunay_triangulation_conformer_2(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Mesh_2_Constrained_Delaunay_triangulation_conformer_2(Mesh_2_Constrained_Delaunay_triangulation_2 cdt) {
    this(CGAL_Mesh_2JNI.new_Mesh_2_Constrained_Delaunay_triangulation_conformer_2(Mesh_2_Constrained_Delaunay_triangulation_2.getCPtr(cdt), cdt), true);
  }

  public void make_conforming_Delaunay() {
    CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_conformer_2_make_conforming_Delaunay(swigCPtr, this);
  }

  public void make_conforming_Gabriel() {
    CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_conformer_2_make_conforming_Gabriel(swigCPtr, this);
  }

  public boolean is_conforming_Delaunay() {
    return CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_conformer_2_is_conforming_Delaunay(swigCPtr, this);
  }

  public boolean is_conforming_Gabriel() {
    return CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_conformer_2_is_conforming_Gabriel(swigCPtr, this);
  }

  public void init_Delaunay() {
    CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_conformer_2_init_Delaunay(swigCPtr, this);
  }

  public boolean step_by_step_conforming_Delaunay() {
    return CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_conformer_2_step_by_step_conforming_Delaunay(swigCPtr, this);
  }

  public void init_Gabriel() {
    CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_conformer_2_init_Gabriel(swigCPtr, this);
  }

  public boolean step_by_step_conforming_Gabriel() {
    return CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_conformer_2_step_by_step_conforming_Gabriel(swigCPtr, this);
  }

  public boolean is_conforming_done() {
    return CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_conformer_2_is_conforming_done(swigCPtr, this);
  }

}
