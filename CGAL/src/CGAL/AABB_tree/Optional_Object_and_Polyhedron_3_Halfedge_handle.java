/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.AABB_tree;

public class Optional_Object_and_Polyhedron_3_Halfedge_handle {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Optional_Object_and_Polyhedron_3_Halfedge_handle(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Optional_Object_and_Polyhedron_3_Halfedge_handle obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_AABB_treeJNI.delete_Optional_Object_and_Polyhedron_3_Halfedge_handle(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Optional_Object_and_Polyhedron_3_Halfedge_handle() {
    this(CGAL_AABB_treeJNI.new_Optional_Object_and_Polyhedron_3_Halfedge_handle(), true);
  }

  public boolean empty() {
    return CGAL_AABB_treeJNI.Optional_Object_and_Polyhedron_3_Halfedge_handle_empty(swigCPtr, this);
  }

  public Object_and_Polyhedron_3_Halfedge_handle value() {
    return new Object_and_Polyhedron_3_Halfedge_handle(CGAL_AABB_treeJNI.Optional_Object_and_Polyhedron_3_Halfedge_handle_value(swigCPtr, this), false);
  }

  public Optional_Object_and_Polyhedron_3_Halfedge_handle clone() {
    return new Optional_Object_and_Polyhedron_3_Halfedge_handle(CGAL_AABB_treeJNI.Optional_Object_and_Polyhedron_3_Halfedge_handle_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Optional_Object_and_Polyhedron_3_Halfedge_handle other) {
    CGAL_AABB_treeJNI.Optional_Object_and_Polyhedron_3_Halfedge_handle_clone__SWIG_1(swigCPtr, this, Optional_Object_and_Polyhedron_3_Halfedge_handle.getCPtr(other), other);
  }

}
