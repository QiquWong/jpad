/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_3;

public class Mesh_3_Quality {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Mesh_3_Quality(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Mesh_3_Quality obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Mesh_3JNI.delete_Mesh_3_Quality(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Mesh_3_Quality() {
    this(CGAL_Mesh_3JNI.new_Mesh_3_Quality__SWIG_0(), true);
  }

  public Mesh_3_Quality(int first, double second) {
    this(CGAL_Mesh_3JNI.new_Mesh_3_Quality__SWIG_1(first, second), true);
  }

  public Mesh_3_Quality(Mesh_3_Quality p) {
    this(CGAL_Mesh_3JNI.new_Mesh_3_Quality__SWIG_2(Mesh_3_Quality.getCPtr(p), p), true);
  }

  public void setFirst(int value) {
    CGAL_Mesh_3JNI.Mesh_3_Quality_first_set(swigCPtr, this, value);
  }

  public int getFirst() {
    return CGAL_Mesh_3JNI.Mesh_3_Quality_first_get(swigCPtr, this);
  }

  public void setSecond(double value) {
    CGAL_Mesh_3JNI.Mesh_3_Quality_second_set(swigCPtr, this, value);
  }

  public double getSecond() {
    return CGAL_Mesh_3JNI.Mesh_3_Quality_second_get(swigCPtr, this);
  }

}
