/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_2;

public class User_mesh_criteria_simple_2 {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public User_mesh_criteria_simple_2(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(User_mesh_criteria_simple_2 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Mesh_2JNI.delete_User_mesh_criteria_simple_2(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public User_mesh_criteria_simple_2() {
    this(CGAL_Mesh_2JNI.new_User_mesh_criteria_simple_2__SWIG_0(), true);
  }

  public User_mesh_criteria_simple_2(Mesh_2_predicate call) {
    this(CGAL_Mesh_2JNI.new_User_mesh_criteria_simple_2__SWIG_1(Mesh_2_predicate.getCPtr(call), call), true);
  }

}
