/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_3;

public class Facet_predicate {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Facet_predicate(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Facet_predicate obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Mesh_3JNI.delete_Facet_predicate(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Facet_predicate(java.lang.Object jobj, String fname, String input_type, String output_type) {
    this(CGAL_Mesh_3JNI.new_Facet_predicate(jobj, fname, input_type, output_type), true);
  }

}
