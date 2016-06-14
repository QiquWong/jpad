/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Triangulation_3;

  
  import java.lang.Iterable;
  import java.lang.UnsupportedOperationException;
  import java.util.Iterator;
  
public class Triangulation_3_Finite_edges_iterator implements   Iterable<Triangulation_3_Edge>, Iterator<Triangulation_3_Edge>  {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Triangulation_3_Finite_edges_iterator(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Triangulation_3_Finite_edges_iterator obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Triangulation_3JNI.delete_Triangulation_3_Finite_edges_iterator(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    public Iterator<Triangulation_3_Edge> iterator() {
      return this;
    }
    
    //we store an object of type Triangulation_3_Edge to avoid
    //creation and allocation of a java object at each iteration.
    private Triangulation_3_Edge objectInstance = new Triangulation_3_Edge();
    public Triangulation_3_Edge next() {
      next(objectInstance);
      return objectInstance;
    }
  
  public Triangulation_3_Finite_edges_iterator() {
    this(CGAL_Triangulation_3JNI.new_Triangulation_3_Finite_edges_iterator(), true);
  }

  public Triangulation_3_Edge slow_next() {
    return new Triangulation_3_Edge(CGAL_Triangulation_3JNI.Triangulation_3_Finite_edges_iterator_slow_next(swigCPtr, this), true);
  }

  public void next(Triangulation_3_Edge r) {
    CGAL_Triangulation_3JNI.Triangulation_3_Finite_edges_iterator_next(swigCPtr, this, Triangulation_3_Edge.getCPtr(r), r);
  }

  public Triangulation_3_Finite_edges_iterator clone() {
    return new Triangulation_3_Finite_edges_iterator(CGAL_Triangulation_3JNI.Triangulation_3_Finite_edges_iterator_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Triangulation_3_Finite_edges_iterator other) {
    CGAL_Triangulation_3JNI.Triangulation_3_Finite_edges_iterator_clone__SWIG_1(swigCPtr, this, Triangulation_3_Finite_edges_iterator.getCPtr(other), other);
  }

  public boolean hasNext() {
    return CGAL_Triangulation_3JNI.Triangulation_3_Finite_edges_iterator_hasNext(swigCPtr, this);
  }

  public boolean equals(Triangulation_3_Finite_edges_iterator p) {
    return CGAL_Triangulation_3JNI.Triangulation_3_Finite_edges_iterator_equals(swigCPtr, this, Triangulation_3_Finite_edges_iterator.getCPtr(p), p);
  }

  public boolean not_equals(Triangulation_3_Finite_edges_iterator p) {
    return CGAL_Triangulation_3JNI.Triangulation_3_Finite_edges_iterator_not_equals(swigCPtr, this, Triangulation_3_Finite_edges_iterator.getCPtr(p), p);
  }

}
