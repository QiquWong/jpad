/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_3;

public class User_mesh_criteria {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public User_mesh_criteria(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(User_mesh_criteria obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Mesh_3JNI.delete_User_mesh_criteria(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public User_mesh_criteria(Cell_predicate ccall, Facet_predicate fcall) {
    this(CGAL_Mesh_3JNI.new_User_mesh_criteria(Cell_predicate.getCPtr(ccall), ccall, Facet_predicate.getCPtr(fcall), fcall), true);
  }

}
