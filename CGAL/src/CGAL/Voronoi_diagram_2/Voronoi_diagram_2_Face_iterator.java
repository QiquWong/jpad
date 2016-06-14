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
  
public class Voronoi_diagram_2_Face_iterator implements   Iterable<Voronoi_diagram_2_Face_handle>, Iterator<Voronoi_diagram_2_Face_handle>  {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Voronoi_diagram_2_Face_iterator(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Voronoi_diagram_2_Face_iterator obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Voronoi_diagram_2JNI.delete_Voronoi_diagram_2_Face_iterator(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

    public void remove() {
      throw new UnsupportedOperationException();
    }
    
    public Iterator<Voronoi_diagram_2_Face_handle> iterator() {
      return this;
    }
    
    //we store an object of type Voronoi_diagram_2_Face_handle to avoid
    //creation and allocation of a java object at each iteration.
    private Voronoi_diagram_2_Face_handle objectInstance = new Voronoi_diagram_2_Face_handle();
    public Voronoi_diagram_2_Face_handle next() {
      next(objectInstance);
      return objectInstance;
    }
  
  public Voronoi_diagram_2_Face_iterator() {
    this(CGAL_Voronoi_diagram_2JNI.new_Voronoi_diagram_2_Face_iterator(), true);
  }

  public Voronoi_diagram_2_Face_handle slow_next() {
    return new Voronoi_diagram_2_Face_handle(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_iterator_slow_next(swigCPtr, this), true);
  }

  public void next(Voronoi_diagram_2_Face_handle r) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_iterator_next(swigCPtr, this, Voronoi_diagram_2_Face_handle.getCPtr(r), r);
  }

  public Voronoi_diagram_2_Face_iterator clone() {
    return new Voronoi_diagram_2_Face_iterator(CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_iterator_clone__SWIG_0(swigCPtr, this), true);
  }

  public void clone(Voronoi_diagram_2_Face_iterator other) {
    CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_iterator_clone__SWIG_1(swigCPtr, this, Voronoi_diagram_2_Face_iterator.getCPtr(other), other);
  }

  public boolean hasNext() {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_iterator_hasNext(swigCPtr, this);
  }

  public boolean equals(Voronoi_diagram_2_Face_iterator p) {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_iterator_equals(swigCPtr, this, Voronoi_diagram_2_Face_iterator.getCPtr(p), p);
  }

  public boolean not_equals(Voronoi_diagram_2_Face_iterator p) {
    return CGAL_Voronoi_diagram_2JNI.Voronoi_diagram_2_Face_iterator_not_equals(swigCPtr, this, Voronoi_diagram_2_Face_iterator.getCPtr(p), p);
  }

}
