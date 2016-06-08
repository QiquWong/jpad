/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Kernel;

public class Iso_rectangle_2 {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Iso_rectangle_2(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Iso_rectangle_2 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_KernelJNI.delete_Iso_rectangle_2(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Iso_rectangle_2(Point_2 p, Point_2 q) {
    this(CGAL_KernelJNI.new_Iso_rectangle_2__SWIG_0(Point_2.getCPtr(p), p, Point_2.getCPtr(q), q), true);
  }

  public Iso_rectangle_2(Point_2 p, Point_2 q, int i) {
    this(CGAL_KernelJNI.new_Iso_rectangle_2__SWIG_1(Point_2.getCPtr(p), p, Point_2.getCPtr(q), q, i), true);
  }

  public Iso_rectangle_2(Point_2 left, Point_2 right, Point_2 bottom, Point_2 top) {
    this(CGAL_KernelJNI.new_Iso_rectangle_2__SWIG_2(Point_2.getCPtr(left), left, Point_2.getCPtr(right), right, Point_2.getCPtr(bottom), bottom, Point_2.getCPtr(top), top), true);
  }

  public Iso_rectangle_2(double min_hx, double min_hy, double max_hx, double max_hy, double hw) {
    this(CGAL_KernelJNI.new_Iso_rectangle_2__SWIG_3(min_hx, min_hy, max_hx, max_hy, hw), true);
  }

  public Iso_rectangle_2(double min_hx, double min_hy, double max_hx, double max_hy) {
    this(CGAL_KernelJNI.new_Iso_rectangle_2__SWIG_4(min_hx, min_hy, max_hx, max_hy), true);
  }

  public boolean equals(Iso_rectangle_2 p) {
    return CGAL_KernelJNI.Iso_rectangle_2_equals(swigCPtr, this, Iso_rectangle_2.getCPtr(p), p);
  }

  public boolean not_equals(Iso_rectangle_2 p) {
    return CGAL_KernelJNI.Iso_rectangle_2_not_equals(swigCPtr, this, Iso_rectangle_2.getCPtr(p), p);
  }

  public Point_2 vertex(int c) {
    return new Point_2(CGAL_KernelJNI.Iso_rectangle_2_vertex__SWIG_0(swigCPtr, this, c), true);
  }

  public void vertex(int c, Point_2 ret) {
    CGAL_KernelJNI.Iso_rectangle_2_vertex__SWIG_1(swigCPtr, this, c, Point_2.getCPtr(ret), ret);
  }

  public Point_2 min() {
    return new Point_2(CGAL_KernelJNI.Iso_rectangle_2_min__SWIG_0(swigCPtr, this), true);
  }

  public void min(Point_2 ref) {
    CGAL_KernelJNI.Iso_rectangle_2_min__SWIG_1(swigCPtr, this, Point_2.getCPtr(ref), ref);
  }

  public Point_2 max() {
    return new Point_2(CGAL_KernelJNI.Iso_rectangle_2_max__SWIG_0(swigCPtr, this), true);
  }

  public void max(Point_2 ref) {
    CGAL_KernelJNI.Iso_rectangle_2_max__SWIG_1(swigCPtr, this, Point_2.getCPtr(ref), ref);
  }

  public double xmin() {
    return CGAL_KernelJNI.Iso_rectangle_2_xmin(swigCPtr, this);
  }

  public double ymin() {
    return CGAL_KernelJNI.Iso_rectangle_2_ymin(swigCPtr, this);
  }

  public double xmax() {
    return CGAL_KernelJNI.Iso_rectangle_2_xmax(swigCPtr, this);
  }

  public double ymax() {
    return CGAL_KernelJNI.Iso_rectangle_2_ymax(swigCPtr, this);
  }

  public double min_coord(int c) {
    return CGAL_KernelJNI.Iso_rectangle_2_min_coord(swigCPtr, this, c);
  }

  public double max_coord(int c) {
    return CGAL_KernelJNI.Iso_rectangle_2_max_coord(swigCPtr, this, c);
  }

  public Bbox_2 bbox() {
    return new Bbox_2(CGAL_KernelJNI.Iso_rectangle_2_bbox__SWIG_0(swigCPtr, this), true);
  }

  public void bbox(Bbox_2 ref) {
    CGAL_KernelJNI.Iso_rectangle_2_bbox__SWIG_1(swigCPtr, this, Bbox_2.getCPtr(ref), ref);
  }

  public boolean is_degenerate() {
    return CGAL_KernelJNI.Iso_rectangle_2_is_degenerate(swigCPtr, this);
  }

  public Bounded_side bounded_side(Point_2 c) {
    return Bounded_side.swigToEnum(CGAL_KernelJNI.Iso_rectangle_2_bounded_side(swigCPtr, this, Point_2.getCPtr(c), c));
  }

  public boolean has_on_boundary(Point_2 c) {
    return CGAL_KernelJNI.Iso_rectangle_2_has_on_boundary(swigCPtr, this, Point_2.getCPtr(c), c);
  }

  public boolean has_on_bounded_side(Point_2 c) {
    return CGAL_KernelJNI.Iso_rectangle_2_has_on_bounded_side(swigCPtr, this, Point_2.getCPtr(c), c);
  }

  public boolean has_on_unbounded_side(Point_2 c) {
    return CGAL_KernelJNI.Iso_rectangle_2_has_on_unbounded_side(swigCPtr, this, Point_2.getCPtr(c), c);
  }

  public double area() {
    return CGAL_KernelJNI.Iso_rectangle_2_area(swigCPtr, this);
  }

}
