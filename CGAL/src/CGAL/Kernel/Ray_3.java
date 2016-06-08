/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Kernel;

public class Ray_3 {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Ray_3(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Ray_3 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_KernelJNI.delete_Ray_3(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Ray_3(Point_3 p, Point_3 q) {
    this(CGAL_KernelJNI.new_Ray_3__SWIG_0(Point_3.getCPtr(p), p, Point_3.getCPtr(q), q), true);
  }

  public Ray_3(Point_3 p, Direction_3 d) {
    this(CGAL_KernelJNI.new_Ray_3__SWIG_1(Point_3.getCPtr(p), p, Direction_3.getCPtr(d), d), true);
  }

  public Ray_3(Point_3 p, Vector_3 v) {
    this(CGAL_KernelJNI.new_Ray_3__SWIG_2(Point_3.getCPtr(p), p, Vector_3.getCPtr(v), v), true);
  }

  public Ray_3(Point_3 p, Line_3 l) {
    this(CGAL_KernelJNI.new_Ray_3__SWIG_3(Point_3.getCPtr(p), p, Line_3.getCPtr(l), l), true);
  }

  public Point_3 source() {
    return new Point_3(CGAL_KernelJNI.Ray_3_source__SWIG_0(swigCPtr, this), true);
  }

  public void source(Point_3 ref) {
    CGAL_KernelJNI.Ray_3_source__SWIG_1(swigCPtr, this, Point_3.getCPtr(ref), ref);
  }

  public Point_3 point(int c) {
    return new Point_3(CGAL_KernelJNI.Ray_3_point__SWIG_0(swigCPtr, this, c), true);
  }

  public void point(int c, Point_3 ret) {
    CGAL_KernelJNI.Ray_3_point__SWIG_1(swigCPtr, this, c, Point_3.getCPtr(ret), ret);
  }

  public Direction_3 direction() {
    return new Direction_3(CGAL_KernelJNI.Ray_3_direction__SWIG_0(swigCPtr, this), true);
  }

  public void direction(Direction_3 ref) {
    CGAL_KernelJNI.Ray_3_direction__SWIG_1(swigCPtr, this, Direction_3.getCPtr(ref), ref);
  }

  public Vector_3 to_vector() {
    return new Vector_3(CGAL_KernelJNI.Ray_3_to_vector__SWIG_0(swigCPtr, this), true);
  }

  public void to_vector(Vector_3 ref) {
    CGAL_KernelJNI.Ray_3_to_vector__SWIG_1(swigCPtr, this, Vector_3.getCPtr(ref), ref);
  }

  public Line_3 supporting_line() {
    return new Line_3(CGAL_KernelJNI.Ray_3_supporting_line__SWIG_0(swigCPtr, this), true);
  }

  public void supporting_line(Line_3 ref) {
    CGAL_KernelJNI.Ray_3_supporting_line__SWIG_1(swigCPtr, this, Line_3.getCPtr(ref), ref);
  }

  public Ray_3 opposite() {
    return new Ray_3(CGAL_KernelJNI.Ray_3_opposite__SWIG_0(swigCPtr, this), true);
  }

  public void opposite(Ray_3 ref) {
    CGAL_KernelJNI.Ray_3_opposite__SWIG_1(swigCPtr, this, Ray_3.getCPtr(ref), ref);
  }

  public boolean is_degenerate() {
    return CGAL_KernelJNI.Ray_3_is_degenerate(swigCPtr, this);
  }

  public boolean has_on(Point_3 c) {
    return CGAL_KernelJNI.Ray_3_has_on(swigCPtr, this, Point_3.getCPtr(c), c);
  }

  public boolean equals(Ray_3 p) {
    return CGAL_KernelJNI.Ray_3_equals(swigCPtr, this, Ray_3.getCPtr(p), p);
  }

  public boolean not_equals(Ray_3 p) {
    return CGAL_KernelJNI.Ray_3_not_equals(swigCPtr, this, Ray_3.getCPtr(p), p);
  }

  public String toString() {
    return CGAL_KernelJNI.Ray_3_toString(swigCPtr, this);
  }

  public Ray_3 clone() {
    return new Ray_3(CGAL_KernelJNI.Ray_3_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Ray_3 other) {
    CGAL_KernelJNI.Ray_3_clone__SWIG_1(swigCPtr, this, Ray_3.getCPtr(other), other);
  }

}
