/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Kernel;

public class Origin {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Origin(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Origin obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_KernelJNI.delete_Origin(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Point_2 plus(Vector_2 arg0) {
    return new Point_2(CGAL_KernelJNI.Origin_plus__SWIG_0(swigCPtr, this, Vector_2.getCPtr(arg0), arg0), true);
  }

  public Point_2 minus(Vector_2 arg0) {
    return new Point_2(CGAL_KernelJNI.Origin_minus__SWIG_0(swigCPtr, this, Vector_2.getCPtr(arg0), arg0), true);
  }

  public Point_3 plus(Vector_3 arg0) {
    return new Point_3(CGAL_KernelJNI.Origin_plus__SWIG_1(swigCPtr, this, Vector_3.getCPtr(arg0), arg0), true);
  }

  public Point_3 minus(Vector_3 arg0) {
    return new Point_3(CGAL_KernelJNI.Origin_minus__SWIG_1(swigCPtr, this, Vector_3.getCPtr(arg0), arg0), true);
  }

  public Vector_2 minus(Point_2 arg0) {
    return new Vector_2(CGAL_KernelJNI.Origin_minus__SWIG_2(swigCPtr, this, Point_2.getCPtr(arg0), arg0), true);
  }

  public Vector_3 minus(Point_3 arg0) {
    return new Vector_3(CGAL_KernelJNI.Origin_minus__SWIG_3(swigCPtr, this, Point_3.getCPtr(arg0), arg0), true);
  }

  public Origin() {
    this(CGAL_KernelJNI.new_Origin(), true);
  }

}
