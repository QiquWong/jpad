/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package CGAL.Java;

public class CGAL_JavaJNI {

    static{
      try {
          System.loadLibrary("CGAL_Java");
      } catch (UnsatisfiedLinkError e) {
        System.err.println("Native code library CGAL_Java failed to load. \n" + e);
        throw e;
      }
    }
  
  public final static native long new_JavaData__SWIG_0(Object jarg1);
  public final static native long new_JavaData__SWIG_1();
  public final static native void delete_JavaData(long jarg1);
  public final static native Object JavaData_get_data(long jarg1, JavaData jarg1_);
  public final static native void JavaData_set_data(long jarg1, JavaData jarg1_, Object jarg2);
}
