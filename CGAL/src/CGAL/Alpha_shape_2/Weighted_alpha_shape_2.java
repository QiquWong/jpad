/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.9
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Alpha_shape_2;
import CGAL.Kernel.Weighted_point_2; import java.util.Iterator; import java.util.Collection;
public class Weighted_alpha_shape_2 extends Internal_regular_Weighted_alpha_shape_2 {
  private transient long swigCPtr;

  public Weighted_alpha_shape_2(long cPtr, boolean cMemoryOwn) {
    super(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  public static long getCPtr(Weighted_alpha_shape_2 obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        CGAL_Alpha_shape_2JNI.delete_Weighted_alpha_shape_2(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public Weighted_alpha_shape_2() {
    this(CGAL_Alpha_shape_2JNI.new_Weighted_alpha_shape_2__SWIG_0(), true);
  }

  public Weighted_alpha_shape_2(double alpha) {
    this(CGAL_Alpha_shape_2JNI.new_Weighted_alpha_shape_2__SWIG_1(alpha), true);
  }

  public Weighted_alpha_shape_2(Iterator<Weighted_point_2> range, double alpha) {
    this(CGAL_Alpha_shape_2JNI.new_Weighted_alpha_shape_2__SWIG_2(range, alpha), true);
  }

  public Weighted_alpha_shape_2(double alpha, Mode m) {
    this(CGAL_Alpha_shape_2JNI.new_Weighted_alpha_shape_2__SWIG_3(alpha, m.swigValue()), true);
  }

  public Weighted_alpha_shape_2(Iterator<Weighted_point_2> range, double alpha, Mode m) {
    this(CGAL_Alpha_shape_2JNI.new_Weighted_alpha_shape_2__SWIG_4(range, alpha, m.swigValue()), true);
  }

  public Mode set_mode() {
    return Mode.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_set_mode__SWIG_0(swigCPtr, this));
  }

  public Mode set_mode(Mode m) {
    return Mode.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_set_mode__SWIG_1(swigCPtr, this, m.swigValue()));
  }

  public Mode get_mode() {
    return Mode.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_get_mode(swigCPtr, this));
  }

  public void clear() {
    CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_clear(swigCPtr, this);
  }

  public double set_alpha(double c) {
    return CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_set_alpha(swigCPtr, this, c);
  }

  public double get_alpha() {
    return CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_get_alpha(swigCPtr, this);
  }

  public double get_nth_alpha(int c) {
    return CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_get_nth_alpha(swigCPtr, this, c);
  }

  public int number_of_alphas() {
    return CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_number_of_alphas(swigCPtr, this);
  }

  public int make_alpha_shape(Iterator<Weighted_point_2> range) {
    return CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_make_alpha_shape(swigCPtr, this, range);
  }

  public Weighted_alpha_shape_2_Alpha_iterator alpha() {
    return new Weighted_alpha_shape_2_Alpha_iterator(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_alpha(swigCPtr, this), true);
  }

  public Weighted_alpha_shape_2_Alpha_iterator alpha_find(double a) {
    return new Weighted_alpha_shape_2_Alpha_iterator(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_alpha_find(swigCPtr, this, a), true);
  }

  public Weighted_alpha_shape_2_Alpha_iterator alpha_lower_bound(double a) {
    return new Weighted_alpha_shape_2_Alpha_iterator(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_alpha_lower_bound(swigCPtr, this, a), true);
  }

  public Weighted_alpha_shape_2_Alpha_iterator alpha_upper_bound(double a) {
    return new Weighted_alpha_shape_2_Alpha_iterator(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_alpha_upper_bound(swigCPtr, this, a), true);
  }

  public int number_of_solid_components() {
    return CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_number_of_solid_components__SWIG_0(swigCPtr, this);
  }

  public int number_of_solid_components(double c) {
    return CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_number_of_solid_components__SWIG_1(swigCPtr, this, c);
  }

  public Weighted_alpha_shape_2_Alpha_iterator find_optimal_alpha(int i) {
    return new Weighted_alpha_shape_2_Alpha_iterator(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_find_optimal_alpha(swigCPtr, this, i), true);
  }

  public Weighted_alpha_shape_2_Alpha_shape_vertices_iterator alpha_shape_vertices() {
    return new Weighted_alpha_shape_2_Alpha_shape_vertices_iterator(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_alpha_shape_vertices(swigCPtr, this), true);
  }

  public Weighted_alpha_shape_2_Alpha_shape_edges_iterator alpha_shape_edges() {
    return new Weighted_alpha_shape_2_Alpha_shape_edges_iterator(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_alpha_shape_edges(swigCPtr, this), true);
  }

  public Classification_type classify(Weighted_point_2 c) {
    return Classification_type.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_classify__SWIG_0(swigCPtr, this, Weighted_point_2.getCPtr(c), c));
  }

  public Classification_type classify(Weighted_alpha_shape_2_Face_handle c) {
    return Classification_type.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_classify__SWIG_1(swigCPtr, this, Weighted_alpha_shape_2_Face_handle.getCPtr(c), c));
  }

  public Classification_type classify(Weighted_alpha_shape_2_Edge c) {
    return Classification_type.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_classify__SWIG_2(swigCPtr, this, Weighted_alpha_shape_2_Edge.getCPtr(c), c));
  }

  public Classification_type classify(Weighted_alpha_shape_2_Face_handle c1, int c2) {
    return Classification_type.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_classify__SWIG_3(swigCPtr, this, Weighted_alpha_shape_2_Face_handle.getCPtr(c1), c1, c2));
  }

  public Classification_type classify(Weighted_alpha_shape_2_Vertex_handle c) {
    return Classification_type.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_classify__SWIG_4(swigCPtr, this, Weighted_alpha_shape_2_Vertex_handle.getCPtr(c), c));
  }

  public Classification_type classify(Weighted_point_2 c1, double c2) {
    return Classification_type.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_classify__SWIG_5(swigCPtr, this, Weighted_point_2.getCPtr(c1), c1, c2));
  }

  public Classification_type classify(Weighted_alpha_shape_2_Face_handle c1, double c2) {
    return Classification_type.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_classify__SWIG_6(swigCPtr, this, Weighted_alpha_shape_2_Face_handle.getCPtr(c1), c1, c2));
  }

  public Classification_type classify(Weighted_alpha_shape_2_Edge c1, double c2) {
    return Classification_type.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_classify__SWIG_7(swigCPtr, this, Weighted_alpha_shape_2_Edge.getCPtr(c1), c1, c2));
  }

  public Classification_type classify(Weighted_alpha_shape_2_Face_handle c1, int c2, double c3) {
    return Classification_type.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_classify__SWIG_8(swigCPtr, this, Weighted_alpha_shape_2_Face_handle.getCPtr(c1), c1, c2, c3));
  }

  public Classification_type classify(Weighted_alpha_shape_2_Vertex_handle c1, double c2) {
    return Classification_type.swigToEnum(CGAL_Alpha_shape_2JNI.Weighted_alpha_shape_2_classify__SWIG_9(swigCPtr, this, Weighted_alpha_shape_2_Vertex_handle.getCPtr(c1), c1, c2));
  }

}
