/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_2;

  import CGAL.Kernel.Point_2;
  import java.lang.Iterable;
  import java.lang.UnsupportedOperationException;
  import java.util.Iterator;
  
public class Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator implements   Iterable<Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle>, Iterator<Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle>  {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Mesh_2JNI.delete_Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    public Iterator<Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle> iterator() {
      return this;
    }
    
    //we store an object of type Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle to avoid
    //creation and allocation of a java object at each iteration.
    private Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle objectInstance = new Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle();
    public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle next() {
      next(objectInstance);
      return objectInstance;
    }
  
  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator() {
    this(CGAL_Mesh_2JNI.new_Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator(), true);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle slow_next() {
    return new Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator_slow_next(swigCPtr, this), true);
  }

  public void next(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle r) {
    CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator_next(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertex_handle.getCPtr(r), r);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator clone() {
    return new Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator other) {
    CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator_clone__SWIG_1(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator.getCPtr(other), other);
  }

  public boolean hasNext() {
    return CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator_hasNext(swigCPtr, this);
  }

  public boolean equals(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator p) {
    return CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator_equals(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator.getCPtr(p), p);
  }

  public boolean not_equals(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator p) {
    return CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator_not_equals(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Vertices_in_constraint_iterator.getCPtr(p), p);
  }

}
