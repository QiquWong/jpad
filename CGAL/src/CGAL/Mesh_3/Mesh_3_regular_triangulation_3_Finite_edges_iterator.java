/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_3;

  
  import java.lang.Iterable;
  import java.lang.UnsupportedOperationException;
  import java.util.Iterator;
  
public class Mesh_3_regular_triangulation_3_Finite_edges_iterator implements   Iterable<Mesh_3_regular_triangulation_3_Edge>, Iterator<Mesh_3_regular_triangulation_3_Edge>  {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Mesh_3_regular_triangulation_3_Finite_edges_iterator(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Mesh_3_regular_triangulation_3_Finite_edges_iterator obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Mesh_3JNI.delete_Mesh_3_regular_triangulation_3_Finite_edges_iterator(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    public Iterator<Mesh_3_regular_triangulation_3_Edge> iterator() {
      return this;
    }
    
    //we store an object of type Mesh_3_regular_triangulation_3_Edge to avoid
    //creation and allocation of a java object at each iteration.
    private Mesh_3_regular_triangulation_3_Edge objectInstance = new Mesh_3_regular_triangulation_3_Edge();
    public Mesh_3_regular_triangulation_3_Edge next() {
      next(objectInstance);
      return objectInstance;
    }
  
  public Mesh_3_regular_triangulation_3_Finite_edges_iterator() {
    this(CGAL_Mesh_3JNI.new_Mesh_3_regular_triangulation_3_Finite_edges_iterator(), true);
  }

  public Mesh_3_regular_triangulation_3_Edge slow_next() {
    return new Mesh_3_regular_triangulation_3_Edge(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_Finite_edges_iterator_slow_next(swigCPtr, this), true);
  }

  public void next(Mesh_3_regular_triangulation_3_Edge r) {
    CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_Finite_edges_iterator_next(swigCPtr, this, Mesh_3_regular_triangulation_3_Edge.getCPtr(r), r);
  }

  public Mesh_3_regular_triangulation_3_Finite_edges_iterator clone() {
    return new Mesh_3_regular_triangulation_3_Finite_edges_iterator(CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_Finite_edges_iterator_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Mesh_3_regular_triangulation_3_Finite_edges_iterator other) {
    CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_Finite_edges_iterator_clone__SWIG_1(swigCPtr, this, Mesh_3_regular_triangulation_3_Finite_edges_iterator.getCPtr(other), other);
  }

  public boolean hasNext() {
    return CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_Finite_edges_iterator_hasNext(swigCPtr, this);
  }

  public boolean equals(Mesh_3_regular_triangulation_3_Finite_edges_iterator p) {
    return CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_Finite_edges_iterator_equals(swigCPtr, this, Mesh_3_regular_triangulation_3_Finite_edges_iterator.getCPtr(p), p);
  }

  public boolean not_equals(Mesh_3_regular_triangulation_3_Finite_edges_iterator p) {
    return CGAL_Mesh_3JNI.Mesh_3_regular_triangulation_3_Finite_edges_iterator_not_equals(swigCPtr, this, Mesh_3_regular_triangulation_3_Finite_edges_iterator.getCPtr(p), p);
  }

}
