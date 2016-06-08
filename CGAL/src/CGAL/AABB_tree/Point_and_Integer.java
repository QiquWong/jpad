/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.AABB_tree;
import CGAL.Kernel.Point_3;
public class Point_and_Integer {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Point_and_Integer(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Point_and_Integer obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_AABB_treeJNI.delete_Point_and_Integer(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Point_and_Integer() {
    this(CGAL_AABB_treeJNI.new_Point_and_Integer__SWIG_0(), true);
  }

  public Point_and_Integer(Point_3 first, int second) {
    this(CGAL_AABB_treeJNI.new_Point_and_Integer__SWIG_1(Point_3.getCPtr(first), first, second), true);
  }

  public Point_and_Integer(Point_and_Integer p) {
    this(CGAL_AABB_treeJNI.new_Point_and_Integer__SWIG_2(Point_and_Integer.getCPtr(p), p), true);
  }

  public void setFirst(Point_3 value) {
    CGAL_AABB_treeJNI.Point_and_Integer_first_set(swigCPtr, this, Point_3.getCPtr(value), value);
  }

  public Point_3 getFirst() {
    long cPtr = CGAL_AABB_treeJNI.Point_and_Integer_first_get(swigCPtr, this);
    return (cPtr == 0) ? null : new Point_3(cPtr, false);
  }

  public void setSecond(int value) {
    CGAL_AABB_treeJNI.Point_and_Integer_second_set(swigCPtr, this, value);
  }

  public int getSecond() {
    return CGAL_AABB_treeJNI.Point_and_Integer_second_get(swigCPtr, this);
  }

}
