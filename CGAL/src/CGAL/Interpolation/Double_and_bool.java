/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Interpolation;

public class Double_and_bool {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Double_and_bool(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Double_and_bool obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_InterpolationJNI.delete_Double_and_bool(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Double_and_bool() {
    this(CGAL_InterpolationJNI.new_Double_and_bool__SWIG_0(), true);
  }

  public Double_and_bool(double first, boolean second) {
    this(CGAL_InterpolationJNI.new_Double_and_bool__SWIG_1(first, second), true);
  }

  public Double_and_bool(Double_and_bool p) {
    this(CGAL_InterpolationJNI.new_Double_and_bool__SWIG_2(Double_and_bool.getCPtr(p), p), true);
  }

  public void setFirst(double value) {
    CGAL_InterpolationJNI.Double_and_bool_first_set(swigCPtr, this, value);
  }

  public double getFirst() {
    return CGAL_InterpolationJNI.Double_and_bool_first_get(swigCPtr, this);
  }

  public void setSecond(boolean value) {
    CGAL_InterpolationJNI.Double_and_bool_second_set(swigCPtr, this, value);
  }

  public boolean getSecond() {
    return CGAL_InterpolationJNI.Double_and_bool_second_get(swigCPtr, this);
  }

}
