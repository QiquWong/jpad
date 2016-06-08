/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Box_intersection_d;
import CGAL.Kernel.Segment_2;
public class Box_for_segment_polyline_2 {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Box_for_segment_polyline_2(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Box_for_segment_polyline_2 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Box_intersection_dJNI.delete_Box_for_segment_polyline_2(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Box_for_segment_polyline_2(Segment_2 s, int polyline_id, int id) {
    this(CGAL_Box_intersection_dJNI.new_Box_for_segment_polyline_2(Segment_2.getCPtr(s), s, polyline_id, id), true);
  }

}
