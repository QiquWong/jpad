/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Alpha_shape_2;

public class Ref_Alpha_shape_2_Vertex_handle {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Ref_Alpha_shape_2_Vertex_handle(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Ref_Alpha_shape_2_Vertex_handle obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Alpha_shape_2JNI.delete_Ref_Alpha_shape_2_Vertex_handle(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Ref_Alpha_shape_2_Vertex_handle() {
    this(CGAL_Alpha_shape_2JNI.new_Ref_Alpha_shape_2_Vertex_handle__SWIG_0(), true);
  }

  public Ref_Alpha_shape_2_Vertex_handle(Alpha_shape_2_Vertex_handle k) {
    this(CGAL_Alpha_shape_2JNI.new_Ref_Alpha_shape_2_Vertex_handle__SWIG_1(Alpha_shape_2_Vertex_handle.getCPtr(k), k), true);
  }

  public void set(Alpha_shape_2_Vertex_handle t) {
    CGAL_Alpha_shape_2JNI.Ref_Alpha_shape_2_Vertex_handle_set(swigCPtr, this, Alpha_shape_2_Vertex_handle.getCPtr(t), t);
  }

  public Alpha_shape_2_Vertex_handle object() {
    return new Alpha_shape_2_Vertex_handle(CGAL_Alpha_shape_2JNI.Ref_Alpha_shape_2_Vertex_handle_object(swigCPtr, this), true);
  }

  public Ref_Alpha_shape_2_Vertex_handle clone() {
    return new Ref_Alpha_shape_2_Vertex_handle(CGAL_Alpha_shape_2JNI.Ref_Alpha_shape_2_Vertex_handle_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Ref_Alpha_shape_2_Vertex_handle other) {
    CGAL_Alpha_shape_2JNI.Ref_Alpha_shape_2_Vertex_handle_clone__SWIG_1(swigCPtr, this, Ref_Alpha_shape_2_Vertex_handle.getCPtr(other), other);
  }

}
