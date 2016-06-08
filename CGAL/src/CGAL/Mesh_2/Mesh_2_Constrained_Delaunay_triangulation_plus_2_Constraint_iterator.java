/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Mesh_2;

  
  import java.lang.Iterable;
  import java.lang.UnsupportedOperationException;
  import java.util.Iterator;
  
public class Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator implements   Iterable<Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint>, Iterator<Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint>  {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Mesh_2JNI.delete_Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    public Iterator<Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint> iterator() {
      return this;
    }
    
    //we store an object of type Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint to avoid
    //creation and allocation of a java object at each iteration.
    private Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint objectInstance = new Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint();
    public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint next() {
      next(objectInstance);
      return objectInstance;
    }
  
  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator() {
    this(CGAL_Mesh_2JNI.new_Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator(), true);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint slow_next() {
    return new Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator_slow_next(swigCPtr, this), true);
  }

  public void next(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint r) {
    CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator_next(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint.getCPtr(r), r);
  }

  public Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator clone() {
    return new Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator(CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator other) {
    CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator_clone__SWIG_1(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator.getCPtr(other), other);
  }

  public boolean hasNext() {
    return CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator_hasNext(swigCPtr, this);
  }

  public boolean equals(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator p) {
    return CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator_equals(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator.getCPtr(p), p);
  }

  public boolean not_equals(Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator p) {
    return CGAL_Mesh_2JNI.Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator_not_equals(swigCPtr, this, Mesh_2_Constrained_Delaunay_triangulation_plus_2_Constraint_iterator.getCPtr(p), p);
  }

}
