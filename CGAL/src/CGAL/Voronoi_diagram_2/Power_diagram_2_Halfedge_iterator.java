/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Voronoi_diagram_2;

  
  import java.lang.Iterable;
  import java.lang.UnsupportedOperationException;
  import java.util.Iterator;
  
public class Power_diagram_2_Halfedge_iterator implements   Iterable<Power_diagram_2_Halfedge_handle>, Iterator<Power_diagram_2_Halfedge_handle>  {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Power_diagram_2_Halfedge_iterator(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Power_diagram_2_Halfedge_iterator obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Voronoi_diagram_2JNI.delete_Power_diagram_2_Halfedge_iterator(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    public Iterator<Power_diagram_2_Halfedge_handle> iterator() {
      return this;
    }
    
    //we store an object of type Power_diagram_2_Halfedge_handle to avoid
    //creation and allocation of a java object at each iteration.
    private Power_diagram_2_Halfedge_handle objectInstance = new Power_diagram_2_Halfedge_handle();
    public Power_diagram_2_Halfedge_handle next() {
      next(objectInstance);
      return objectInstance;
    }
  
  public Power_diagram_2_Halfedge_iterator() {
    this(CGAL_Voronoi_diagram_2JNI.new_Power_diagram_2_Halfedge_iterator(), true);
  }

  public Power_diagram_2_Halfedge_handle slow_next() {
    return new Power_diagram_2_Halfedge_handle(CGAL_Voronoi_diagram_2JNI.Power_diagram_2_Halfedge_iterator_slow_next(swigCPtr, this), true);
  }

  public void next(Power_diagram_2_Halfedge_handle r) {
    CGAL_Voronoi_diagram_2JNI.Power_diagram_2_Halfedge_iterator_next(swigCPtr, this, Power_diagram_2_Halfedge_handle.getCPtr(r), r);
  }

  public Power_diagram_2_Halfedge_iterator clone() {
    return new Power_diagram_2_Halfedge_iterator(CGAL_Voronoi_diagram_2JNI.Power_diagram_2_Halfedge_iterator_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Power_diagram_2_Halfedge_iterator other) {
    CGAL_Voronoi_diagram_2JNI.Power_diagram_2_Halfedge_iterator_clone__SWIG_1(swigCPtr, this, Power_diagram_2_Halfedge_iterator.getCPtr(other), other);
  }

  public boolean hasNext() {
    return CGAL_Voronoi_diagram_2JNI.Power_diagram_2_Halfedge_iterator_hasNext(swigCPtr, this);
  }

  public boolean equals(Power_diagram_2_Halfedge_iterator p) {
    return CGAL_Voronoi_diagram_2JNI.Power_diagram_2_Halfedge_iterator_equals(swigCPtr, this, Power_diagram_2_Halfedge_iterator.getCPtr(p), p);
  }

  public boolean not_equals(Power_diagram_2_Halfedge_iterator p) {
    return CGAL_Voronoi_diagram_2JNI.Power_diagram_2_Halfedge_iterator_not_equals(swigCPtr, this, Power_diagram_2_Halfedge_iterator.getCPtr(p), p);
  }

}
