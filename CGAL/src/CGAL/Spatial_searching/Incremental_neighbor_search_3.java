/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Spatial_searching;
import CGAL.Kernel.Point_3;
public class Incremental_neighbor_search_3 {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Incremental_neighbor_search_3(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Incremental_neighbor_search_3 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Spatial_searchingJNI.delete_Incremental_neighbor_search_3(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Incremental_neighbor_search_3(Incremental_neighbor_search_tree_3 tree, Point_3 query) {
    this(CGAL_Spatial_searchingJNI.new_Incremental_neighbor_search_3__SWIG_0(Incremental_neighbor_search_tree_3.getCPtr(tree), tree, Point_3.getCPtr(query), query), true);
  }

  public Incremental_neighbor_search_3(Incremental_neighbor_search_tree_3 tree, Point_3 query, double eps) {
    this(CGAL_Spatial_searchingJNI.new_Incremental_neighbor_search_3__SWIG_1(Incremental_neighbor_search_tree_3.getCPtr(tree), tree, Point_3.getCPtr(query), query, eps), true);
  }

  public Incremental_neighbor_search_3(Incremental_neighbor_search_tree_3 tree, Point_3 query, double eps, boolean search_nearest) {
    this(CGAL_Spatial_searchingJNI.new_Incremental_neighbor_search_3__SWIG_2(Incremental_neighbor_search_tree_3.getCPtr(tree), tree, Point_3.getCPtr(query), query, eps, search_nearest), true);
  }

  public Incremental_neighbor_search_iterator_3 iterator() {
    return new Incremental_neighbor_search_iterator_3(CGAL_Spatial_searchingJNI.Incremental_neighbor_search_3_iterator(swigCPtr, this), true);
  }

  public Point_3 value() {
    return new Point_3(CGAL_Spatial_searchingJNI.Incremental_neighbor_search_3_value(swigCPtr, this), true);
  }

}
