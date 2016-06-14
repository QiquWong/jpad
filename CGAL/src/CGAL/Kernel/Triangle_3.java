/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Kernel;

public class Triangle_3 {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Triangle_3(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Triangle_3 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_KernelJNI.delete_Triangle_3(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Triangle_3() {
    this(CGAL_KernelJNI.new_Triangle_3__SWIG_0(), true);
  }

  public Triangle_3(Point_3 p, Point_3 q, Point_3 r) {
    this(CGAL_KernelJNI.new_Triangle_3__SWIG_1(Point_3.getCPtr(p), p, Point_3.getCPtr(q), q, Point_3.getCPtr(r), r), true);
  }

  public Point_3 vertex(int c) {
    return new Point_3(CGAL_KernelJNI.Triangle_3_vertex__SWIG_0(swigCPtr, this, c), true);
  }

  public void vertex(int c, Point_3 ret) {
    CGAL_KernelJNI.Triangle_3_vertex__SWIG_1(swigCPtr, this, c, Point_3.getCPtr(ret), ret);
  }

  public Plane_3 supporting_plane() {
    return new Plane_3(CGAL_KernelJNI.Triangle_3_supporting_plane(swigCPtr, this), true);
  }

  public boolean is_degenerate() {
    return CGAL_KernelJNI.Triangle_3_is_degenerate(swigCPtr, this);
  }

  public boolean has_on(Point_3 c) {
    return CGAL_KernelJNI.Triangle_3_has_on(swigCPtr, this, Point_3.getCPtr(c), c);
  }

  public double squared_area() {
    return CGAL_KernelJNI.Triangle_3_squared_area(swigCPtr, this);
  }

  public Bbox_3 bbox() {
    return new Bbox_3(CGAL_KernelJNI.Triangle_3_bbox__SWIG_0(swigCPtr, this), true);
  }

  public void bbox(Bbox_3 ref) {
    CGAL_KernelJNI.Triangle_3_bbox__SWIG_1(swigCPtr, this, Bbox_3.getCPtr(ref), ref);
  }

  public boolean equals(Triangle_3 p) {
    return CGAL_KernelJNI.Triangle_3_equals(swigCPtr, this, Triangle_3.getCPtr(p), p);
  }

  public boolean not_equals(Triangle_3 p) {
    return CGAL_KernelJNI.Triangle_3_not_equals(swigCPtr, this, Triangle_3.getCPtr(p), p);
  }

  public String toString() {
    return CGAL_KernelJNI.Triangle_3_toString(swigCPtr, this);
  }

  public Triangle_3 clone() {
    return new Triangle_3(CGAL_KernelJNI.Triangle_3_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Triangle_3 other) {
    CGAL_KernelJNI.Triangle_3_clone__SWIG_1(swigCPtr, this, Triangle_3.getCPtr(other), other);
  }

}
